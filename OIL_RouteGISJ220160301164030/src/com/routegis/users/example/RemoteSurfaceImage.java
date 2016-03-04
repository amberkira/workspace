
package com.routegis.users.example;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.geom.Sector;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.SurfaceImage;


public class RemoteSurfaceImage extends ApplicationTemplate
{
    // The remote image to display.
    protected static final String IMAGE_URL = "http://eoimages.gsfc.nasa.gov/ve//1438/earth_lights_lrg.jpg";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            SurfaceImage image = new SurfaceImage(IMAGE_URL, Sector.FULL_SPHERE);

            RenderableLayer layer = new RenderableLayer();
            layer.setName("Remote Surface Image");
            layer.addRenderable(image);
            // Disable picking for the layer because it covers the full sphere and will override a terrain pick.
            layer.setPickEnabled(false);

            insertBeforePlacenames(this.getMainWin(), layer);
            this.getLayerPanel().update(this.getMainWin());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Remote Surface Image", RemoteSurfaceImage.AppFrame.class);
    }
}
