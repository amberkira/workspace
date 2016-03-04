

package com.routegis.users.example;

import java.util.ArrayList;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;
import com.routegis.users.util.DirectedPath;

import core.routegis.engine.*;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.geom.*;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.*;


public class PathsWithDirection extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(2d);

            // Create a path, set some of its properties and set its attributes.
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(49.01653274909177, -122.7349081128505, 1));
            pathPositions.add(Position.fromDegrees(49.01715024535254, -122.7596194200486, 10));
            pathPositions.add(Position.fromDegrees(49.02781845803761, -122.7651733463364, 100));
            pathPositions.add(Position.fromDegrees(49.05312411976134, -122.7926787136435, 1000));
            pathPositions.add(Position.fromDegrees(49.0747697644625, -122.8224152286015, 1000));
            pathPositions.add(Position.fromDegrees(49.09727187849899, -122.8187118695457, 1000));
            pathPositions.add(Position.fromDegrees(49.1002974270654, -122.7348314826556, 100));
            pathPositions.add(Position.fromDegrees(49.11190305133165, -122.7345541413842, 100));
            pathPositions.add(Position.fromDegrees(49.11101764617014, -122.7455553490629, 10));
            pathPositions.add(Position.fromDegrees(49.11509767012883, -122.7459193678911, 10));
            pathPositions.add(Position.fromDegrees(49.11467371318521, -122.7563706291131, 10));

            Path path = new DirectedPath(pathPositions);

            // To ensure that the arrowheads resize smoothly, refresh each time the path is drawn.
            path.setAttributes(attrs);
            path.setVisible(true);
            path.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
            path.setPathType(AVKey.GREAT_CIRCLE);
            layer.addRenderable(path);

            // Add the layer to the model.
            insertBeforeCompass(getMainWin(), layer);

            // Update layer panel
            this.getLayerPanel().update(this.getMainWin());
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 49.06);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -122.77);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 22000);

        ApplicationTemplate.start("RouteGIS SDK Paths With Direction", AppFrame.class);
    }
}