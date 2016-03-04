

package com.routegis.applications.window.features.swinglayermanager;

import javax.swing.*;

import com.routegis.applications.window.core.*;
import com.routegis.applications.window.features.AbstractFeature;
import com.routegis.applications.window.util.Util;

import java.awt.*;


public class LayerManagerDialog extends AbstractFeature
{
    private static final String ICON_PATH
        = "images/layer-manager-64x64.png";

    protected JDialog dialog;
    protected boolean positionInitialized = false;

    public LayerManagerDialog(Registry registry)
    {
        super("Layer Manager", Constants.FEATURE_LAYER_MANAGER_DIALOG, ICON_PATH, registry);
        setEnabled(true);
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);

        this.dialog = new JDialog(this.controller.getFrame());
        this.dialog.setPreferredSize(new Dimension(350, 700));
        this.dialog.getContentPane().setLayout(new BorderLayout());
        this.dialog.setResizable(true);
        this.dialog.setModal(false);
        this.dialog.setTitle("Layer Manager");

        ControlsPanel controlsPanel = (ControlsPanel) controller.getRegisteredObject(Constants.CONTROLS_PANEL);
        if (controlsPanel != null)
            this.dialog.getContentPane().add(controlsPanel.getJPanel(), BorderLayout.CENTER);
        else
            Util.getLogger().severe("Control panel is not registered.");

        this.dialog.pack();

        this.addToToolBar();
    }

    @Override
    public boolean isTwoState()
    {
        return true;
    }

    public boolean isOn()
    {
        return this.dialog.isVisible();
    }

    @Override
    public void turnOn(boolean tf)
    {
        if (tf && !this.positionInitialized)
        {
            Util.positionDialogInContainer(this.dialog, this.controller.getAppPanel().getJPanel(),
                SwingConstants.WEST, SwingConstants.NORTH);
            this.positionInitialized = true;

            // Make the default layer group visible the first time the dialog is raised.
            this.controller.getLayerManager().scrollToLayer(null);
        }

        this.setVisible(tf);
    }

    protected void setVisible(boolean tf)
    {
        this.dialog.setVisible(tf);
    }
}