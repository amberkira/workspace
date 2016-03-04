package com.routegis.applications.window.core;

import javax.swing.*;

import com.routegis.applications.window.features.AbstractFeature;

import java.awt.*;


public class AppPanelImpl extends AbstractFeature implements AppPanel
{
    private JPanel panel;

    public AppPanelImpl(Registry registry)
    {
        super("App Panel", Constants.APP_PANEL, registry);

        this.panel = new JPanel(new BorderLayout());
        this.panel.setPreferredSize(new Dimension(1280, 800));
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);

        Dimension appSize = controller.getAppSize();
        if (appSize != null)
            this.panel.setPreferredSize(appSize);

        GISBPanel wwPanel = controller.getWWPanel();
        if (wwPanel != null)
            this.panel.add(wwPanel.getJPanel(), BorderLayout.CENTER);
    }

    public JPanel getJPanel()
    {
        return this.panel;
    }
}
