
package com.routegis.users.example;

import java.awt.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.FlatWorldPanel;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.Configuration;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.globes.EarthFlat;
import core.routegis.engine.layers.*;
import core.routegis.engine.view.orbit.FlatOrbitView;


public class FlatWorld extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Change atmosphere SkyGradientLayer for SkyColorLayer
            LayerList layers = this.getMainWin().getModel().getLayers();
            for (int i = 0; i < layers.size(); i++)
            {
                if (layers.get(i) instanceof SkyGradientLayer)
                    layers.set(i, new SkyColorLayer());
            }
            this.getLayerPanel().update(this.getMainWin());

            // Add flat world projection control panel
            this.getLayerPanel().add(new FlatWorldPanel(this.getMainWin()), BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args)
    {
        // Adjust configuration values before instantiation
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        Configuration.setValue(AVKey.VIEW_CLASS_NAME, FlatOrbitView.class.getName());
        ApplicationTemplate.start("RouteGIS SDK Flat World", AppFrame.class);
    }
}
