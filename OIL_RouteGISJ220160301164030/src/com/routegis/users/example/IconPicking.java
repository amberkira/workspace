
package com.routegis.users.example;

import java.awt.BorderLayout;
import java.awt.Dimension;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.FlatWorldPanel;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.Configuration;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.event.SelectEvent;
import core.routegis.engine.event.SelectListener;
import core.routegis.engine.geom.Angle;
import core.routegis.engine.geom.Position;
import core.routegis.engine.globes.EarthFlat;
import core.routegis.engine.layers.IconLayer;
import core.routegis.engine.layers.LayerList;
import core.routegis.engine.layers.SkyColorLayer;
import core.routegis.engine.layers.SkyGradientLayer;
import core.routegis.engine.pick.PickedObject;
import core.routegis.engine.pick.PickedObjectList;
import core.routegis.engine.render.UserFacingIcon;
import core.routegis.engine.view.orbit.FlatOrbitView;


public class IconPicking extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            IconLayer layer = new IconLayer();
            layer.setPickEnabled(true);
            layer.setAllowBatchPicking(false);
            layer.setRegionCulling(true);

            UserFacingIcon icon = new UserFacingIcon("images/32x32-icon-earth.png",
                new Position(Angle.fromRadians(0), Angle.fromRadians(0), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);

            icon = new UserFacingIcon("images/32x32-icon-earth.png",
                new Position(Angle.fromRadians(0.1), Angle.fromRadians(0.0), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);

            icon = new UserFacingIcon("images/32x32-icon-earth.png",
                new Position(Angle.fromRadians(0.0), Angle.fromRadians(0.1), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);

            icon = new UserFacingIcon("images/32x32-icon-earth.png",
                new Position(Angle.fromRadians(0.1), Angle.fromRadians(0.1), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);

            icon = new UserFacingIcon("images/32x32-icon-earth.png",
                new Position(Angle.fromRadians(0), Angle.fromDegrees(180), 0));
            icon.setSize(new Dimension(24, 24));
            layer.addIcon(icon);

            ApplicationTemplate.insertAfterPlacenames(this.getMainWin(), layer);
            // Change atmosphere SkyGradientLayer for SkyColorLayer
            LayerList layers = this.getMainWin().getModel().getLayers();
            for (int i = 0; i < layers.size(); i++)
            {
                if (layers.get(i) instanceof SkyGradientLayer)
                    layers.set(i, new SkyColorLayer());
            }
            this.getLayerPanel().update(this.getMainWin());

            this.getMainWin().addSelectListener(new SelectListener()
            {
                @Override
                public void selected(SelectEvent event)
                {
                    if (event.getEventAction().equals(SelectEvent.ROLLOVER))
                    {
                        PickedObjectList pol = event.getObjects();
                        System.out.println(" Picked Objects Size " + pol.size());
                        for (PickedObject po : pol)
                        {
                            System.out.println(" Class " + po.getObject().getClass().getName() + "  isTerrian=" + po.isTerrain());
                        }
                    }
                }
            });
            this.getMainWin().getSceneController().setDeepPickEnabled(true);
            // Add flat world projection control panel
            this.getLayerPanel().add(new FlatWorldPanel(this.getMainWin()), BorderLayout.SOUTH);
        }
    }

    public static void main(String[] args)
    {
        // Adjust configuration values before instantiation
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        Configuration.setValue(AVKey.VIEW_CLASS_NAME, FlatOrbitView.class.getName());
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 27e6);
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 0);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, 88);
        ApplicationTemplate.start("RouteGIS SDK Flat World", AppFrame.class);
    }
}
