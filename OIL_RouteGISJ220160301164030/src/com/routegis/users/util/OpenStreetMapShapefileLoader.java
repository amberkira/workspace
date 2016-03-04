
package com.routegis.users.util;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import core.routegis.engine.avlist.*;
import core.routegis.engine.formats.shapefile.*;
import core.routegis.engine.geom.*;
import core.routegis.engine.layers.*;
import core.routegis.engine.render.*;
import core.routegis.engine.util.*;


public class OpenStreetMapShapefileLoader
{
    
    public static boolean isOSMPlacesSource(Object source)
    {
        if (source == null || PrivateUtil.isEmpty(source))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String path = InOut.getSourcePath(source);
        return path != null && InOut.getFilename(path).equalsIgnoreCase("places.shp");
    }

    
    public static Layer makeLayerFromOSMPlacesSource(Object source)
    {
        if (source == null || PrivateUtil.isEmpty(source))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Shapefile shp = null;
        Layer layer = null;
        try
        {
            shp = new Shapefile(source);
            layer = makeLayerFromOSMPlacesShapefile(shp);
        }
        finally
        {
            if (shp != null)
                shp.close();
        }

        return layer;
    }

    
    public static Layer makeLayerFromOSMPlacesShapefile(Shapefile shp)
    {
        if (shp == null)
        {
            String message = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OSMShapes[] shapeArray =
            {
                new OSMShapes(Color.BLACK, .3, 30e3), // hamlet
                new OSMShapes(Color.GREEN, .5, 100e3), // village
                new OSMShapes(Color.CYAN, 1, 500e3), // town
                new OSMShapes(Color.YELLOW, 2, 3000e3) // city
            };

        // Filter records for a particular sector
        while (shp.hasNext())
        {
            ShapefileRecord record = shp.nextRecord();
            if (record == null || !record.getShapeType().equals(Shapefile.SHAPE_POINT))
                continue;

            Object o = record.getAttributes().getValue("type");
            if (o == null || !(o instanceof String))
                continue;

            // Add points with different rendering attribute for different subsets
            OSMShapes shapes = null;
            String type = (String) o;
            if (type.equalsIgnoreCase("hamlet"))
            {
                shapes = shapeArray[0];
            }
            else if (type.equalsIgnoreCase("village"))
            {
                shapes = shapeArray[1];
            }
            else if (type.equalsIgnoreCase("town"))
            {
                shapes = shapeArray[2];
            }
            else if (type.equalsIgnoreCase("city"))
            {
                shapes = shapeArray[3];
            }

            if (shapes == null)
                continue;

            String name = null;

            AVList attr = record.getAttributes();
            if (attr.getEntries() != null)
            {
                for (Map.Entry<String, Object> entry : attr.getEntries())
                {
                    if (entry.getKey().equalsIgnoreCase("name"))
                    {
                        name = (String) entry.getValue();
                        break;
                    }
                }
            }

            // Note: points are stored in the buffer as a sequence of X and Y with X = longitude, Y = latitude.
            double[] pointCoords = ((ShapefileRecordPoint) record).getPoint();
            LatLon location = LatLon.fromDegrees(pointCoords[1], pointCoords[0]);

            if (!PrivateUtil.isEmpty(name))
            {
                Label label = new Label(name, new Position(location, 0));
                label.setFont(shapes.font);
                label.setColor(shapes.foreground);
                label.setBackgroundColor(shapes.background);
                label.setMaxActiveAltitude(shapes.labelMaxAltitude);
                label.setPriority(shapes.labelMaxAltitude);
                shapes.labels.add(label);
            }

            shapes.locations.add(location);
        }

        TextAndShapesLayer layer = new TextAndShapesLayer();

        for (OSMShapes shapes : shapeArray)
        {
            // Use one SurfaceIcons instance for all points.
            BufferedImage image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, .8f, shapes.foreground);
            SurfaceIcons sis = new SurfaceIcons(image, shp.getPointBuffer().getLocations());
            sis.setMaxSize(4e3 * shapes.scale); // 4km
            sis.setMinSize(100);  // 100m
            sis.setScale(shapes.scale);
            sis.setOpacity(.8);
            layer.addRenderable(sis);
            shapes.locations.clear();

            for (Label label : shapes.labels)
            {
                layer.addLabel(label);
            }
            shapes.labels.clear();
        }

        return layer;
    }

    //
    //
    //

    protected static class OSMShapes
    {
        public ArrayList<LatLon> locations = new ArrayList<LatLon>();
        public ArrayList<Label> labels = new ArrayList<Label>();
        public Color foreground;
        public Color background;
        public Font font;
        public double scale;
        public double labelMaxAltitude;

        public OSMShapes(Color color, double scale, double labelMaxAltitude)
        {
            this.foreground = color;
            this.background = PrivateUtil.computeContrastingColor(color);
            this.font = new Font("Arial", Font.BOLD, 10 + (int) (3 * scale));
            this.scale = scale;
            this.labelMaxAltitude = labelMaxAltitude;
        }
    }

    protected static class TextAndShapesLayer extends RenderableLayer
    {
        protected ArrayList<GeographicText> labels = new ArrayList<GeographicText>();
        protected GeographicTextRenderer textRenderer = new GeographicTextRenderer();

        public TextAndShapesLayer()
        {
            this.textRenderer.setCullTextEnabled(true);
            this.textRenderer.setCullTextMargin(2);
            this.textRenderer.setDistanceMaxScale(2);
            this.textRenderer.setDistanceMinScale(.5);
            this.textRenderer.setDistanceMinOpacity(.5);
            this.textRenderer.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        }

        public void addLabel(GeographicText label)
        {
            this.labels.add(label);
        }

        public void doRender(DrawContext dc)
        {
            super.doRender(dc);
            this.setActiveLabels(dc);
            this.textRenderer.render(dc, this.labels);
        }

        protected void setActiveLabels(DrawContext dc)
        {
            for (GeographicText text : this.labels)
            {
                if (text instanceof Label)
                    text.setVisible(((Label) text).isActive(dc));
            }
        }
    }

    protected static class Label extends UserFacingText
    {
        protected double minActiveAltitude = -Double.MAX_VALUE;
        protected double maxActiveAltitude = Double.MAX_VALUE;

        public Label(String text, Position position)
        {
            super(text, position);
        }

        public void setMinActiveAltitude(double altitude)
        {
            this.minActiveAltitude = altitude;
        }

        public void setMaxActiveAltitude(double altitude)
        {
            this.maxActiveAltitude = altitude;
        }

        public boolean isActive(DrawContext dc)
        {
            double eyeElevation = dc.getView().getEyePosition().getElevation();
            return this.minActiveAltitude <= eyeElevation && eyeElevation <= this.maxActiveAltitude;
        }
    }
}
