package com.routegis.applications.window.core;

import javax.swing.*;

import com.routegis.applications.window.features.AbstractFeature;
import com.routegis.applications.window.util.Util;

import core.routegis.engine.*;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.awt.WindowGLCanvas;
import core.routegis.engine.event.RenderingExceptionListener;
import core.routegis.engine.exception.PrivateAbsentRequirementException;
import core.routegis.engine.layers.*;

import java.awt.*;


public class GISBPanelImpl extends AbstractFeature implements GISBPanel
{
    private JPanel panel;
    private WindowGLCanvas wwd;

    public GISBPanelImpl(Registry registry)
    {
        super("RouteGIS SDK Panel", Constants.WW_PANEL, registry);

        this.panel = new JPanel(new BorderLayout());
        this.wwd = new WindowGLCanvas();
        this.wwd.addRenderingExceptionListener(new RenderingExceptionListener()
        {
            public void exceptionThrown(Throwable t)
            {
                if (t instanceof PrivateAbsentRequirementException)
                {
                    String msg = "This computer is not capable of running ";
                    msg += Configuration.getStringValue(Constants.APPLICATION_DISPLAY_NAME);
                    msg += ".";
                    Util.getLogger().severe(msg);
                    System.exit(-1);
                }
            }
        });

        // Create the default model as described in the current RouteGIS SDK properties.
        Model m = (Model) MainClass.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        this.wwd.setModel(m);

        // Disable screen credits.
        this.wwd.getSceneController().getScreenCreditController().setEnabled(false);

        this.wwd.setPreferredSize(new Dimension(1024, 768));
        this.panel.add(this.wwd, BorderLayout.CENTER);
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);
    }

    public WorldWindow getWWd()
    {
        return wwd;
    }

    public JPanel getJPanel()
    {
        return this.panel;
    }

    public Dimension getSize()
    {
        return this.panel.getSize();
    }

    public void addLayer(Layer layer)
    {
        if (layer != null)
            this.wwd.getModel().getLayers().add(layer);
    }

    public void removeLayer(Layer layer)
    {
        this.wwd.getModel().getLayers().remove(layer);
    }

    public void insertBeforeNamedLayer(Layer layer, String targetLayerName)
    {
        if (layer == null)
            return;

        if (targetLayerName == null)
        {
            this.wwd.getModel().getLayers().add(layer);
            return;
        }

        // Insert the layer into the layer list just before the target layer.
        int targetPosition = 0;
        LayerList layers = this.wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l.getName().indexOf(targetLayerName) != -1)
            {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        layers.add(targetPosition, layer);
    }

    public void insertAfterNamedLayer(Layer layer, String targetLayerName)
    {
        if (layer == null)
            return;

        if (targetLayerName == null)
        {
            this.wwd.getModel().getLayers().add(layer);
            return;
        }

        // Insert the layer into the layer list just after the target layer.
        int targetPosition = 0;
        LayerList layers = this.wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l.getName().indexOf(targetLayerName) != -1)
                targetPosition = layers.indexOf(l);
        }
        layers.add(targetPosition + 1, layer);
    }
}
