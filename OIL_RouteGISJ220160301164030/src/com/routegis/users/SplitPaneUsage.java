

package com.routegis.users;

import javax.swing.*;
import javax.swing.border.*;

import core.routegis.engine.*;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.awt.WindowGLCanvas;
import core.routegis.engine.layers.WorldMapLayer;
import core.routegis.engine.util.StatusBar;

import java.awt.*;


public class SplitPaneUsage
{
    public static class AppPanel extends JPanel
    {
        private WindowGLCanvas wwd;

        // Constructs a JPanel to hold the WorldWindow
        public AppPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            super(new BorderLayout());

            // Create the WorldWindow and set its preferred size.
            this.wwd = new WindowGLCanvas();
            this.wwd.setPreferredSize(canvasSize);

            // THIS IS THE TRICK: Set the panel's minimum size to (0,0);
            this.setMinimumSize(new Dimension(0, 0));

            // Create the default model as described in the current RouteGIS SDK properties.
            Model m = (Model) MainClass.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            // Setup a select listener for the worldmap click-and-go feature
            this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));

            // Add the WorldWindow to this JPanel.
            this.add(this.wwd, BorderLayout.CENTER);

            // Add the status bar if desired.
            if (includeStatusBar)
            {
                StatusBar statusBar = new StatusBar();
                this.add(statusBar, BorderLayout.PAGE_END);
                statusBar.setEventSource(wwd);
            }
        }
    }

    private static class AppFrame extends JFrame
    {
        private Dimension canvasSize = new Dimension(800, 600); // the desired WorldWindow size

        public AppFrame()
        {
            // Create the WorldWindow.
            final AppPanel wwjPanel = new AppPanel(this.canvasSize, true);
            LayerPanel layerPanel = new LayerPanel(wwjPanel.wwd);

            // Create a horizontal split pane containing the layer panel and the WorldWindow panel.
            JSplitPane horizontalSplitPane = new JSplitPane();
            horizontalSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            horizontalSplitPane.setLeftComponent(layerPanel);
            horizontalSplitPane.setRightComponent(wwjPanel);
            horizontalSplitPane.setOneTouchExpandable(true);
            horizontalSplitPane.setContinuousLayout(true); // prevents the pane's being obscured when expanding right

            // Create a panel for the bottom component of a vertical split-pane.
            JPanel bottomPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel("Bottom Panel");
            label.setBorder(new EmptyBorder(10, 10, 10, 10));
            label.setHorizontalAlignment(SwingConstants.CENTER);
            bottomPanel.add(label, BorderLayout.CENTER);

            // Create a vertical split-pane containing the horizontal split plane and the button panel.
            JSplitPane verticalSplitPane = new JSplitPane();
            verticalSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
            verticalSplitPane.setTopComponent(horizontalSplitPane);
            verticalSplitPane.setBottomComponent(bottomPanel);
            verticalSplitPane.setOneTouchExpandable(true);
            verticalSplitPane.setContinuousLayout(true);
            verticalSplitPane.setResizeWeight(1);

            // Add the vertical split-pane to the frame.
            this.getContentPane().add(verticalSplitPane, BorderLayout.CENTER);
            this.pack();

            // Center the application on the screen.
            Dimension prefSize = this.getPreferredSize();
            Dimension parentSize;
            java.awt.Point parentLocation = new java.awt.Point(0, 0);
            parentSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
            int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
            this.setLocation(x, y);
            this.setResizable(true);
        }
    }

    public static void main(String[] args)
    {
        start("RouteGIS SDK Split Pane Usage");
    }

    public static void start(String appName)
    {
        if (Configuration.isMacOS() && appName != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try
        {
            final AppFrame frame = new AppFrame();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
