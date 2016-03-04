package com.routegis.applications.window.features;

import javax.swing.*;

import com.routegis.applications.window.core.*;


public abstract class AbstractFeaturePanel extends AbstractFeature implements FeaturePanel
{
    protected JPanel panel;

    public AbstractFeaturePanel(String s, String featureID, JPanel panel, Registry registry)
    {
        super(s, featureID, registry);

        panel.putClientProperty(Constants.FEATURE, this);
        this.panel = panel;
    }

    public AbstractFeaturePanel(String s, String featureID, String largeIconPath, JPanel panel, Registry registry)
    {
        super(s, featureID, largeIconPath, registry);

        panel.putClientProperty(Constants.FEATURE, this);
        this.panel = panel;
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        if (this.panel != null)
            this.panel.putClientProperty(Constants.FEATURE_OWNER_PROPERTY, this);
    }

    public JPanel getJPanel()
    {
        return this.panel;
    }

    public JComponent[] getDialogControls()
    {
        return null;
    }
}
