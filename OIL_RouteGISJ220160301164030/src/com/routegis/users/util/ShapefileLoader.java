
package com.routegis.users.util;

import java.util.*;

import core.routegis.engine.MainClass;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.formats.shapefile.*;
import core.routegis.engine.geom.*;
import core.routegis.engine.layers.*;
import core.routegis.engine.render.*;
import core.routegis.engine.util.*;

public class ShapefileLoader
{
    protected static final RandomShapeAttributes randomAttrs = new RandomShapeAttributes();

    
    protected int numPolygonsPerLayer = 5000;

    
    public ShapefileLoader()
    {
    }

    
    public Layer createLayerFromSource(Object source)
    {
        if (PrivateUtil.isEmpty(source))
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
            layer = this.createLayerFromShapefile(shp);
        }
        finally
        {
            InOut.closeStream(shp, source.toString());
        }

        return layer;
    }

    
    public List<Layer> createLayersFromSource(Object source)
    {
        if (PrivateUtil.isEmpty(source))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Shapefile shp = null;

        try
        {
            shp = new Shapefile(source);
            return this.createLayersFromShapefile(shp);
        }
        finally
        {
            InOut.closeStream(shp, source.toString());
        }
    }

    
    public Layer createLayerFromShapefile(Shapefile shp)
    {
        if (shp == null)
        {
            String message = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Layer layer = null;

        if (Shapefile.isPointType(shp.getShapeType()))
        {
            layer = new RenderableLayer();
            this.addRenderablesForPoints(shp, (RenderableLayer) layer);
        }
        else if (Shapefile.isMultiPointType(shp.getShapeType()))
        {
            layer = new RenderableLayer();
            this.addRenderablesForMultiPoints(shp, (RenderableLayer) layer);
        }
        else if (Shapefile.isPolylineType(shp.getShapeType()))
        {
            layer = new RenderableLayer();
            this.addRenderablesForPolylines(shp, (RenderableLayer) layer);
        }
        else if (Shapefile.isPolygonType(shp.getShapeType()))
        {
            List<Layer> layers = new ArrayList<Layer>();
            this.addRenderablesForPolygons(shp, layers);
            layer = layers.get(0);
        }
        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnrecognizedShapeType", shp.getShapeType()));
        }

        return layer;
    }

    
    public List<Layer> createLayersFromShapefile(Shapefile shp)
    {
        if (shp == null)
        {
            String message = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        List<Layer> layers = new ArrayList<Layer>();

        if (Shapefile.isPointType(shp.getShapeType()))
        {
            Layer layer = new RenderableLayer();
            this.addRenderablesForPoints(shp, (RenderableLayer) layer);
            layers.add(layer);
        }
        else if (Shapefile.isMultiPointType(shp.getShapeType()))
        {
            Layer layer = new RenderableLayer();
            this.addRenderablesForMultiPoints(shp, (RenderableLayer) layer);
            layers.add(layer);
        }
        else if (Shapefile.isPolylineType(shp.getShapeType()))
        {
            Layer layer = new RenderableLayer();
            this.addRenderablesForPolylines(shp, (RenderableLayer) layer);
            layers.add(layer);
        }
        else if (Shapefile.isPolygonType(shp.getShapeType()))
        {
            this.addRenderablesForPolygons(shp, layers);
        }
        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnrecognizedShapeType", shp.getShapeType()));
        }

        return layers;
    }

    
    public int getNumPolygonsPerLayer()
    {
        return this.numPolygonsPerLayer;
    }

    
    public void setNumPolygonsPerLayer(int numPolygonsPerLayer)
    {
        if (numPolygonsPerLayer < 1)
        {
            String message = Logging.getMessage("generic.InvalidSize", numPolygonsPerLayer);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.numPolygonsPerLayer = numPolygonsPerLayer;
    }
    //
    //
    //

    protected void addRenderablesForPoints(Shapefile shp, RenderableLayer layer)
    {
        PointPlacemarkAttributes attrs = this.createPointAttributes(null);

        while (shp.hasNext())
        {
            ShapefileRecord record = shp.nextRecord();

            if (!Shapefile.isPointType(record.getShapeType()))
                continue;

            double[] point = ((ShapefileRecordPoint) record).getPoint();
            layer.addRenderable(this.createPoint(record, point[1], point[0], attrs));
        }
    }

    protected void addRenderablesForMultiPoints(Shapefile shp, RenderableLayer layer)
    {
        PointPlacemarkAttributes attrs = this.createPointAttributes(null);

        while (shp.hasNext())
        {
            ShapefileRecord record = shp.nextRecord();

            if (!Shapefile.isMultiPointType(record.getShapeType()))
                continue;

            Iterable<double[]> iterable = ((ShapefileRecordMultiPoint) record).getPoints(0);

            for (double[] point : iterable)
            {
                layer.addRenderable(this.createPoint(record, point[1], point[0], attrs));
            }
        }
    }

    protected void addRenderablesForPolylines(Shapefile shp, RenderableLayer layer)
    {
        // Reads all records from the Shapefile, but ignores each records unique information. We do this to create one
        // WWJ object representing the entire shapefile, which as of 8/10/2010 is required to display very large
        // polyline Shapefiles. To create one WWJ object for each Shapefile record, replace this method's contents with
        // the following:
        //
        //while (shp.hasNext())
        //{
        //    ShapefileRecord record = shp.nextRecord();
        //
        //    if (!Shapefile.isPolylineType(record.getShapeType()))
        //        continue;
        //
        //    ShapeAttributes attrs = this.createPolylineAttributes(record);
        //    layer.addRenderable(this.createPolyline(record, attrs));
        //}

        while (shp.hasNext())
        {
            shp.nextRecord();
        }

        ShapeAttributes attrs = this.createPolylineAttributes(null);
        layer.addRenderable(this.createPolyline(shp, attrs));
    }

    
    protected void addRenderablesForPolygons(Shapefile shp, List<Layer> layers)
    {
        RenderableLayer layer = new RenderableLayer();
        layers.add(layer);

        int recordNumber = 0;
        while (shp.hasNext())
        {
            try
            {
                ShapefileRecord record = shp.nextRecord();
                recordNumber = record.getRecordNumber();

                if (!Shapefile.isPolygonType(record.getShapeType()))
                    continue;

                ShapeAttributes attrs = this.createPolygonAttributes(record);
                this.createPolygon(record, attrs, layer);

                if (layer.getNumRenderables() > this.numPolygonsPerLayer)
                {
                    layer = new RenderableLayer();
                    layer.setEnabled(false);
                    layers.add(layer);
                }
            }
            catch (Exception e)
            {
                Logging.logger().warning(Logging.getMessage("SHP.ExceptionAttemptingToConvertShapefileRecord",
                    recordNumber, e));
                // continue with the remaining records
            }
        }
    }

    //
    //
    //

    @SuppressWarnings({"UnusedDeclaration"})
    protected Renderable createPoint(ShapefileRecord record, double latDegrees, double lonDegrees,
        PointPlacemarkAttributes attrs)
    {
        PointPlacemark placemark = new PointPlacemark(Position.fromDegrees(latDegrees, lonDegrees, 0));
        placemark.setAltitudeMode(MainClass.CLAMP_TO_GROUND);
        placemark.setAttributes(attrs);

        return placemark;
    }

    protected Renderable createPolyline(ShapefileRecord record, ShapeAttributes attrs)
    {
        SurfacePolylines shape = new SurfacePolylines(
            Sector.fromDegrees(((ShapefileRecordPolyline) record).getBoundingRectangle()),
            record.getCompoundPointBuffer());
        shape.setAttributes(attrs);

        return shape;
    }

    protected Renderable createPolyline(Shapefile shp, ShapeAttributes attrs)
    {
        SurfacePolylines shape = new SurfacePolylines(Sector.fromDegrees(shp.getBoundingRectangle()),
            shp.getPointBuffer());
        shape.setAttributes(attrs);

        return shape;
    }

    protected void createPolygon(ShapefileRecord record, ShapeAttributes attrs, RenderableLayer layer)
    {
        Double height = this.getHeight(record);
        if (height != null) // create extruded polygons
        {
            ExtrudedPolygon ep = new ExtrudedPolygon(height);
            ep.setAttributes(attrs);
            layer.addRenderable(ep);

            for (int i = 0; i < record.getNumberOfParts(); i++)
            {
                // Although the shapefile spec says that inner and outer boundaries can be listed in any order, it's
                // assumed here that inner boundaries are at least listed adjacent to their outer boundary, either
                // before or after it. The below code accumulates inner boundaries into the extruded polygon until an
                // outer boundary comes along. If the outer boundary comes before the inner boundaries, the inner
                // boundaries are added to the polygon until another outer boundary comes along, at which point a new
                // extruded polygon is started.

                VecBuffer buffer = record.getCompoundPointBuffer().subBuffer(i);
                if (PrivateMath.computeWindingOrderOfLocations(buffer.getLocations()).equals(AVKey.CLOCKWISE))
                {
                    if (!ep.getOuterBoundary().iterator().hasNext()) // has no outer boundary yet
                    {
                        ep.setOuterBoundary(buffer.getLocations());
                    }
                    else
                    {
                        ep = new ExtrudedPolygon();
                        ep.setAttributes(attrs);
                        ep.setOuterBoundary(record.getCompoundPointBuffer().getLocations());
                        layer.addRenderable(ep);
                    }
                }
                else
                {
                    ep.addInnerBoundary(buffer.getLocations());
                }
            }
        }
        else // create surface polygons
        {
            SurfacePolygons shape = new SurfacePolygons(
                Sector.fromDegrees(((ShapefileRecordPolygon) record).getBoundingRectangle()),
                record.getCompoundPointBuffer());
            shape.setAttributes(attrs);
            // Configure the SurfacePolygons as a single large polygon.
            // Configure the SurfacePolygons to correctly interpret the Shapefile polygon record. Shapefile polygons may
            // have rings defining multiple inner and outer boundaries. Each ring's winding order defines whether it's an
            // outer boundary or an inner boundary: outer boundaries have a clockwise winding order. However, the
            // arrangement of each ring within the record is not significant; inner rings can precede outer rings and vice
            // versa.
            //
            // By default, SurfacePolygons assumes that the sub-buffers are arranged such that each outer boundary precedes
            // a set of corresponding inner boundaries. SurfacePolygons traverses the sub-buffers and tessellates a new
            // polygon each  time it encounters an outer boundary. Outer boundaries are sub-buffers whose winding order
            // matches the SurfacePolygons' windingRule property.
            //
            // This default behavior does not work with Shapefile polygon records, because the sub-buffers of a Shapefile
            // polygon record can be arranged arbitrarily. By calling setPolygonRingGroups(new int[]{0}), the
            // SurfacePolygons interprets all sub-buffers as boundaries of a single tessellated shape, and configures the
            // GLU tessellator's winding rule to correctly interpret outer and inner boundaries (in any arrangement)
            // according to their winding order. We set the SurfacePolygons' winding rule to clockwise so that sub-buffers
            // with a clockwise winding ordering are interpreted as outer boundaries.
            shape.setWindingRule(AVKey.CLOCKWISE);
            shape.setPolygonRingGroups(new int[] {0});
            shape.setPolygonRingGroups(new int[] {0});
            layer.addRenderable(shape);
        }
    }

    
    protected Double getHeight(ShapefileRecord record)
    {
        return ShapefileUtils.extractHeightAttribute(record);
    }

    //
    //
    //

    @SuppressWarnings({"UnusedDeclaration"})
    protected PointPlacemarkAttributes createPointAttributes(ShapefileRecord record)
    {
        return randomAttrs.nextPointAttributes();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected ShapeAttributes createPolylineAttributes(ShapefileRecord record)
    {
        return randomAttrs.nextPolylineAttributes();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected ShapeAttributes createPolygonAttributes(ShapefileRecord record)
    {
        return randomAttrs.nextPolygonAttributes();
    }
}
