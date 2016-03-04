

package com.routegis.users.example;

import java.util.ArrayList;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.MainClass;
import core.routegis.engine.geom.Position;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.*;


public class ExtrudedPolygonWithBaseDepth extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes sideAttributes = new BasicShapeAttributes();
            sideAttributes.setInteriorMaterial(Material.MAGENTA);
            sideAttributes.setOutlineOpacity(0.5);
            sideAttributes.setInteriorOpacity(0.25);
            sideAttributes.setOutlineMaterial(Material.GREEN);
            sideAttributes.setOutlineWidth(1);
            sideAttributes.setDrawOutline(true);
            sideAttributes.setDrawInterior(true);

            ShapeAttributes capAttributes = new BasicShapeAttributes(sideAttributes);
            capAttributes.setInteriorMaterial(Material.YELLOW);
            capAttributes.setInteriorOpacity(0.25);
            capAttributes.setDrawInterior(true);

            // Create a path, set some of its properties and set its attributes.
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(43.84344, -114.63673, 20));
            pathPositions.add(Position.fromDegrees(43.84343, -114.63468, 20));
            pathPositions.add(Position.fromDegrees(43.84316, -114.63468, 20));
            pathPositions.add(Position.fromDegrees(43.84314, -114.63675, 20));
            pathPositions.add(Position.fromDegrees(43.84344, -114.63673, 20));
            ExtrudedPolygon pgon = new ExtrudedPolygon(pathPositions);

            pgon.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
            pgon.setSideAttributes(sideAttributes);
            pgon.setCapAttributes(capAttributes);
            pgon.setBaseDepth(20); // Set the base depth to the extruded polygon's height.
            layer.addRenderable(pgon);

            Path path = new Path(Position.fromDegrees(43.8425, -114.6355, 0),
                Position.fromDegrees(43.8442, -114.6356, 0));

            ShapeAttributes pathAttributes = new BasicShapeAttributes();
            pathAttributes.setOutlineOpacity(1);
            pathAttributes.setOutlineMaterial(Material.GREEN);
            pathAttributes.setOutlineWidth(4);
            path.setAttributes(pathAttributes);
            path.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
            layer.addRenderable(path);

            // Add the layer to the model.
            insertBeforeCompass(getMainWin(), layer);

            // Update layer panel
            this.getLayerPanel().update(this.getMainWin());

            getMainWin().getView().setEyePosition(
                Position.fromDegrees(43.843162670564354, -114.63551647988652, 2652.865781935775));
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Extruded Polygon with Base Depth", AppFrame.class);
    }
}
