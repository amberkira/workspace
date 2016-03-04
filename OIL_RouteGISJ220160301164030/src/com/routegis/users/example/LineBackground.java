

package com.routegis.users.example;

import java.awt.*;
import java.util.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.View;
import core.routegis.engine.geom.*;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.*;


public class LineBackground extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                // Specify attributes for the foreground line.
                ShapeAttributes foregroundAttrs = new BasicShapeAttributes();
                foregroundAttrs.setOutlineMaterial(new Material(Color.BLUE));
                foregroundAttrs.setOutlineStipplePattern((short) 0xAAAA);
                foregroundAttrs.setOutlineStippleFactor(8);

                // Specify attributes for the background line.
                ShapeAttributes backgroundAttrs = new BasicShapeAttributes();
                backgroundAttrs.setOutlineMaterial(new Material(Color.WHITE));
                backgroundAttrs.setOutlineOpacity(0.1);
                backgroundAttrs.setOutlineWidth(foregroundAttrs.getOutlineWidth() + 2);

                // Create the primary line as a SurfacePolyline and set its attributes.
                SurfacePolyline si1 = new SurfacePolyline(new ArrayList<LatLon>(Arrays.asList(
                    LatLon.fromDegrees(33.7, 119.6),
                    LatLon.fromDegrees(33.5, 125),
                    LatLon.fromDegrees(35.1, 129.1),
                    LatLon.fromDegrees(35.8, 127.1)
                )));
                si1.setClosed(true);
                si1.setAttributes(foregroundAttrs);

                // Create the background SurfacePolyline and set its attributes.
                SurfacePolyline si2 = new SurfacePolyline(si1.getLocations());
                si2.setClosed(true);
                si2.setAttributes(backgroundAttrs);

                // Now do the same for the Polyline version, which is placed 2 degrees above the SurfacePolyline.
                ArrayList<LatLon> plPoints = new ArrayList<LatLon>();
                for (LatLon ll : si1.getLocations())
                {
                    plPoints.add(ll.add(LatLon.fromDegrees(2, 0))); // add 2 degrees of latitude to separate the lines
                }
                Polyline pl1 = new Polyline(plPoints, 0); // the primary Polyline
                pl1.setFollowTerrain(true);
                pl1.setClosed(true);
                pl1.setPathType(Polyline.RHUMB_LINE);
                pl1.setColor(foregroundAttrs.getOutlineMaterial().getDiffuse());
                pl1.setLineWidth(foregroundAttrs.getOutlineWidth());
                pl1.setStipplePattern(foregroundAttrs.getOutlineStipplePattern());
                pl1.setStippleFactor(foregroundAttrs.getOutlineStippleFactor());

                Polyline pl2 = new Polyline(plPoints, 0); // the background Polyline
                pl2.setFollowTerrain(true);
                pl2.setClosed(true);
                pl2.setPathType(Polyline.RHUMB_LINE);
                float[] c = backgroundAttrs.getOutlineMaterial().getDiffuse().getColorComponents(new float[3]);
                pl2.setColor(new Color(c[0], c[1], c[2], (float) backgroundAttrs.getOutlineOpacity()));
                pl2.setLineWidth(backgroundAttrs.getOutlineWidth());
                pl2.setStipplePattern(backgroundAttrs.getOutlineStipplePattern());
                pl2.setStippleFactor(backgroundAttrs.getOutlineStippleFactor());

                // Add all the lines to the scene.
                RenderableLayer layer = new RenderableLayer();
                layer.setName("Lines");
                layer.setPickEnabled(false);
                layer.addRenderable(si2);
                layer.addRenderable(si1);
                layer.addRenderable(pl1); // Must draw the primary Polyline before drawing the background Polyline
                layer.addRenderable(pl2);

                insertBeforeCompass(this.getMainWin(), layer);

                this.getLayerPanel().update(this.getMainWin());

                // Move the view to the line locations.
                View view = getMainWin().getView();
                view.setEyePosition(Position.fromDegrees(35.3, 124.6, 1500e3));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Line Backgrounds", AppFrame.class);
    }
}
