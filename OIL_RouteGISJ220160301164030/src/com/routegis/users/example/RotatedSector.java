

package com.routegis.users.example;

import javax.swing.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.geom.*;
import core.routegis.engine.globes.Globe;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.SurfaceQuad;

import java.awt.event.*;


public class RotatedSector extends ApplicationTemplate
{
    private static final Sector sector = Sector.fromDegrees(45, 47, -123, -122);

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                // Create the Quad from a Sector
                Globe globe = this.getMainWin().getModel().getGlobe();
                double radius = globe.getRadiusAt(sector.getCentroid());
                double quadWidth = sector.getDeltaLonRadians() * radius;
                double quadHeight = sector.getDeltaLatRadians() * radius;
                final SurfaceQuad quad = new SurfaceQuad(sector.getCentroid(), quadWidth, quadHeight, Angle.ZERO);

                // Create the layer to hold it
                final RenderableLayer layer = new RenderableLayer();
                layer.setName("Rotating Sector");
                layer.addRenderable(quad);

                // Add the layer to the model and update the ApplicationTemplate's layer manager
                insertBeforeCompass(this.getMainWin(), layer);
                this.getLayerPanel().update(this.getMainWin());

                // Rotate the quad continuously
                Timer timer = new Timer(50, new ActionListener()
                {
                    public void actionPerformed(ActionEvent actionEvent)
                    {
                        // Increment the current heading if the layer is visible
                        if (layer.isEnabled())
                        {
                            quad.setHeading(Angle.fromDegrees((quad.getHeading().getDegrees() + 1) % 360));
                            getMainWin().redraw();
                        }
                    }
                });
                timer.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Rotated Sector", AppFrame.class);
    }
}
