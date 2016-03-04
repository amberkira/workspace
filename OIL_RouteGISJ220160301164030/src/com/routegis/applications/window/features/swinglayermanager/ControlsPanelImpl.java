

package com.routegis.applications.window.features.swinglayermanager;

import javax.swing.*;

import com.routegis.applications.window.core.*;
import com.routegis.applications.window.core.layermanager.*;
import com.routegis.applications.window.features.*;

import java.awt.*;


public class ControlsPanelImpl extends AbstractFeature implements ControlsPanel
{
    private static final int DEFAULT_DIVIDER_LOCATION = 250;

    private JPanel panel;

    public ControlsPanelImpl(Registry registry)
    {
        super("Controls Panel", Constants.CONTROLS_PANEL, registry);

        this.panel = new JPanel(new BorderLayout());
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel(new BorderLayout());

        LayerManager layerManager = (LayerManager) this.controller.getRegisteredObject(Constants.FEATURE_LAYER_MANAGER);
        if (layerManager != null && layerManager instanceof FeaturePanel)
            centerPanel.add(((FeaturePanel) layerManager).getJPanel(), BorderLayout.CENTER);

        ActiveLayersManager layerList = (ActiveLayersManager) this.controller.getRegisteredObject(
            Constants.FEATURE_ACTIVE_LAYERS_PANEL);
        if (layerList != null && layerList instanceof FeaturePanel)
            topPanel.add(((FeaturePanel) layerList).getJPanel(), BorderLayout.CENTER);

        final JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(DEFAULT_DIVIDER_LOCATION);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        splitPane.setTopComponent(topPanel);
        splitPane.setBottomComponent(centerPanel);

        this.panel.add(splitPane, BorderLayout.CENTER);
    }

    public JPanel getJPanel()
    {
        return this.panel;
    }
}
