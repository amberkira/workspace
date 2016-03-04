

package com.routegis.users.example;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.Configuration;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.geom.Position;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.SurfaceText;


public class SurfaceTextUsage extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            SurfaceText surfaceText = new SurfaceText("Desolation Wilderness", Position.fromDegrees(38.9345, -120.1670, 0));
            layer.addRenderable(surfaceText);

            this.getMainWin().getModel().getLayers().add(layer);
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 38.9345);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -120.1670);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 50000);

        ApplicationTemplate.start("RouteGIS SDK Surface Text", AppFrame.class);
    }
}
