
package com.routegis.users.example;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;
import com.routegis.users.util.LayerManagerLayer;


public class OnScreenLayerManager extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, false, false);

            // Add the layer manager layer to the model layer list
            getMainWin().getModel().getLayers().add(new LayerManagerLayer(getMainWin()));
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK On-Screen Layer Manager", AppFrame.class);
    }
}
