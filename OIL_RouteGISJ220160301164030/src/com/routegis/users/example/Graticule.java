
package com.routegis.users.example;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.layers.LatLonGraticuleLayer;


public class Graticule extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Add the graticule layer
            insertBeforePlacenames(getMainWin(), new LatLonGraticuleLayer());

            // Update layer panel
            this.getLayerPanel().update(this.getMainWin());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Lat-Lon Graticule", AppFrame.class);
    }
}