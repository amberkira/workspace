

package com.routegis.users.example;

import java.util.ArrayList;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.MainClass;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.geom.Position;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.*;
import core.routegis.engine.util.BasicDragger;


public class Polygons extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Add a dragger to enable shape dragging
            this.getMainWin().addSelectListener(new BasicDragger(this.getMainWin()));

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes normalAttributes = new BasicShapeAttributes();
            normalAttributes.setInteriorMaterial(Material.YELLOW);
            normalAttributes.setOutlineOpacity(0.5);
            normalAttributes.setInteriorOpacity(0.8);
            normalAttributes.setOutlineMaterial(Material.GREEN);
            normalAttributes.setOutlineWidth(2);
            normalAttributes.setDrawOutline(true);
            normalAttributes.setDrawInterior(true);
            normalAttributes.setEnableLighting(true);

            ShapeAttributes highlightAttributes = new BasicShapeAttributes(normalAttributes);
            highlightAttributes.setOutlineMaterial(Material.WHITE);
            highlightAttributes.setOutlineOpacity(1);

            // Create a polygon, set some of its properties and set its attributes.
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(28, -106, 3e4));
            pathPositions.add(Position.fromDegrees(35, -104, 3e4));
            pathPositions.add(Position.fromDegrees(35, -107, 9e4));
            pathPositions.add(Position.fromDegrees(28, -107, 9e4));
            pathPositions.add(Position.fromDegrees(28, -106, 3e4));
            
            Polygon pgon = new Polygon(pathPositions);
            pgon.setValue(AVKey.DISPLAY_NAME, "Has a hole\nRotated -170\u00b0");
            pathPositions.clear();
            pathPositions.add(Position.fromDegrees(29, -106.4, 4e4));
            pathPositions.add(Position.fromDegrees(30, -106.4, 4e4));
            pathPositions.add(Position.fromDegrees(29, -106.8, 7e4));
            pathPositions.add(Position.fromDegrees(29, -106.4, 4e4));
            pgon.addInnerBoundary(pathPositions);
            pgon.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
            pgon.setAttributes(normalAttributes);
            pgon.setHighlightAttributes(highlightAttributes);
            pgon.setRotation(-170d);
            layer.addRenderable(pgon);

            ArrayList<Position> pathLocations = new ArrayList<Position>();
            pathLocations.add(Position.fromDegrees(28, -110, 5e4));
            pathLocations.add(Position.fromDegrees(35, -108, 5e4));
            pathLocations.add(Position.fromDegrees(35, -111, 5e4));
            pathLocations.add(Position.fromDegrees(28, -111, 5e4));
            pathLocations.add(Position.fromDegrees(28, -110, 5e4));
            pgon = new Polygon(pathLocations);
            pgon.setValue(AVKey.DISPLAY_NAME, "Has an image");
            normalAttributes = new BasicShapeAttributes(normalAttributes);
            normalAttributes.setDrawInterior(true);
            normalAttributes.setInteriorMaterial(Material.WHITE);
            normalAttributes.setInteriorOpacity(1);
            pgon.setAttributes(normalAttributes);
            pgon.setHighlightAttributes(highlightAttributes);
            float[] texCoords = new float[] {0, 0, 1, 0, 1, 1, 0, 1, 0, 0};
            pgon.setTextureImageSource("images/32x32-icon-nasa.png", texCoords, 5);
            layer.addRenderable(pgon);

            pathLocations.clear();
            pathLocations.add(Position.fromDegrees(28, -170, 29e4));
            pathLocations.add(Position.fromDegrees(35, -174, 29e4));
            pathLocations.add(Position.fromDegrees(35, 174, 29e4));
            pathLocations.add(Position.fromDegrees(28, 170, 29e4));
            pathLocations.add(Position.fromDegrees(28, -170, 29e4));
            pgon = new Polygon(pathLocations);
            pgon.setValue(AVKey.DISPLAY_NAME, "Spans dateline\nRotated -45\u00b0");
            normalAttributes = new BasicShapeAttributes(normalAttributes);
            normalAttributes.setDrawInterior(true);
            pgon.setAttributes(normalAttributes);
            pgon.setHighlightAttributes(highlightAttributes);
            pgon.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
            pgon.setRotation(-45d);
            layer.addRenderable(pgon);

            // Add the layer to the model.
            insertBeforeCompass(getMainWin(), layer);

            // Update layer panel
            this.getLayerPanel().update(this.getMainWin());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Extruded Polygons", AppFrame.class);
    }
}
