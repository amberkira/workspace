
package com.routegis.users;

import javax.swing.*;

import com.routegis.users.util.*;

import core.routegis.engine.*;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.awt.*;
import core.routegis.engine.event.*;
import core.routegis.engine.exception.PrivateAbsentRequirementException;
import core.routegis.engine.layers.*;
import core.routegis.engine.layers.placename.PlaceNameLayer;
import core.routegis.engine.util.*;

import java.awt.*;


public class ApplicationTemplate
{
    public static class AppPanel extends JPanel
    {
        protected WorldWindow wmain;
        protected StatusBar statusBar;
        protected ToolTipController toolTipController;
        public HighlightController highlightController;

        public AppPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            super(new BorderLayout());

            this.wmain = this.createWorldWindow();
            ((Component) this.wmain).setPreferredSize(canvasSize);

            // Create the default model as described in the current properties.
            Model m = (Model) MainClass.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wmain.setModel(m);

            // Setup a select listener for the worldmap click-and-go feature
            this.wmain.addSelectListener(new ClickAndGoSelectListener(this.getMainWin(), WorldMapLayer.class));

            this.add((Component) this.wmain, BorderLayout.CENTER);
            if (includeStatusBar)
            {
                this.statusBar = new StatusBar();
                this.add(statusBar, BorderLayout.PAGE_END);
                this.statusBar.setEventSource(wmain);
            }

            // Add controllers to manage highlighting and tool tips.
            this.toolTipController = new ToolTipController(this.getMainWin(), AVKey.DISPLAY_NAME, null);
            this.highlightController = new HighlightController(this.getMainWin(), SelectEvent.ROLLOVER);
        }

        protected WorldWindow createWorldWindow()
        {
            return new WindowGLCanvas();
        }

        public WorldWindow getMainWin()
        {
            return wmain;
        }

        public StatusBar getStatusBar()
        {
            return statusBar;
        }
    }

    public static class AppFrame extends JFrame
    {
        private Dimension canvasSize = new Dimension(800, 600);

        protected AppPanel wwjPanel;
        public LayerPanel layerPanel;
        protected StatisticsPanel statsPanel;

        public AppFrame()
        {
            this.initialize(true, true, false);
        }

        public AppFrame(Dimension size)
        {
            this.canvasSize = size;
            this.initialize(true, true, false);
        }

        public AppFrame(boolean includeStatusBar, boolean includeLayerPanel, boolean includeStatsPanel)
        {
            this.initialize(includeStatusBar, includeLayerPanel, includeStatsPanel);
        }

        protected void initialize(boolean includeStatusBar, boolean includeLayerPanel, boolean includeStatsPanel)
        {
            // Create the WorldWindow.
            this.wwjPanel = this.createAppPanel(this.canvasSize, includeStatusBar);
            this.wwjPanel.setPreferredSize(canvasSize);

            // Put the pieces together.
            this.getContentPane().add(wwjPanel, BorderLayout.CENTER);
            if (includeLayerPanel)
            {
                this.layerPanel = new LayerPanel(this.wwjPanel.getMainWin(), null);
                this.getContentPane().add(this.layerPanel, BorderLayout.WEST);
            }

            if (includeStatsPanel || System.getProperty("core.routegis.engine.showStatistics") != null)
            {
                this.statsPanel = new StatisticsPanel(this.wwjPanel.getMainWin(), new Dimension(250, canvasSize.height));
                this.getContentPane().add(this.statsPanel, BorderLayout.EAST);
            }

            // Create and install the view controls layer and register a controller for it with the World Window.
            ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
            insertBeforeCompass(getMainWin(), viewControlsLayer);
            this.getMainWin().addSelectListener(new ViewControlsSelectListener(this.getMainWin(), viewControlsLayer));

            // Register a rendering exception listener that's notified when exceptions occur during rendering.
            this.wwjPanel.getMainWin().addRenderingExceptionListener(new RenderingExceptionListener()
            {
                public void exceptionThrown(Throwable t)
                {
                    if (t instanceof PrivateAbsentRequirementException)
                    {
                        String message = "Computer does not meet minimum graphics requirements.\n";
                        message += "Please install up-to-date graphics driver and try again.\n";
                        message += "Reason: " + t.getMessage() + "\n";
                        message += "This program will end when you press OK.";

                        JOptionPane.showMessageDialog(AppFrame.this, message, "Unable to Start Program",
                            JOptionPane.ERROR_MESSAGE);
                        System.exit(-1);
                    }
                }
            });

            // Search the layer list for layers that are also select listeners and register them with the World
            // Window. This enables interactive layers to be included without specific knowledge of them here.
            for (Layer layer : this.wwjPanel.getMainWin().getModel().getLayers())
            {
                if (layer instanceof SelectListener)
                {
                    this.getMainWin().addSelectListener((SelectListener) layer);
                }
            }

            this.pack();

            // Center the application on the screen.
            PrivateUtil.alignComponent(null, this, AVKey.CENTER);
            this.setResizable(true);
        }

        protected AppPanel createAppPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            return new AppPanel(canvasSize, includeStatusBar);
        }

        public Dimension getCanvasSize()
        {
            return canvasSize;
        }

        public AppPanel getMainPanel()
        {
            return wwjPanel;
        }

        public WorldWindow getMainWin()
        {
            return this.wwjPanel.getMainWin();
        }

        public StatusBar getStatusBar()
        {
            return this.wwjPanel.getStatusBar();
        }

        public LayerPanel getLayerPanel()
        {
            return layerPanel;
        }

        public StatisticsPanel getStatsPanel()
        {
            return statsPanel;
        }

        public void setToolTipController(ToolTipController controller)
        {
            if (this.wwjPanel.toolTipController != null)
                this.wwjPanel.toolTipController.dispose();

            this.wwjPanel.toolTipController = controller;
        }

        public void setHighlightController(HighlightController controller)
        {
            if (this.wwjPanel.highlightController != null)
                this.wwjPanel.highlightController.dispose();

            this.wwjPanel.highlightController = controller;
        }
    }

    public static void insertBeforeCompass(WorldWindow wwd, Layer layer)
    {
        // Insert the layer into the layer list just before the compass.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof CompassLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    }

    public static void insertBeforePlacenames(WorldWindow wwd, Layer layer)
    {
        // Insert the layer into the layer list just before the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof PlaceNameLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition, layer);
    }

    public static void insertAfterPlacenames(WorldWindow wwd, Layer layer)
    {
        // Insert the layer into the layer list just after the placenames.
        int compassPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l instanceof PlaceNameLayer)
                compassPosition = layers.indexOf(l);
        }
        layers.add(compassPosition + 1, layer);
    }

    public static void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName)
    {
        // Insert the layer into the layer list just before the target layer.
        int targetPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l.getName().indexOf(targetName) != -1)
            {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        layers.add(targetPosition, layer);
    }

    static
    {
        System.setProperty("java.net.useSystemProxies", "true");
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RouteGIS Application");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        }
        else if (Configuration.isWindowsOS())
        {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }

    public static AppFrame start(String appName, Class appFrameClass)
    {
        if (Configuration.isMacOS() && appName != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", appName);
        }

        try
        {
            final AppFrame frame = (AppFrame) appFrameClass.newInstance();
            frame.setTitle(appName);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            java.awt.EventQueue.invokeLater(new Runnable()
            {
                public void run()
                {
                    frame.setVisible(true);
                }
            });

            return frame;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args)
    {
        // Call the static start method like this from the main method of your derived class.
        // Substitute your application's name for the first argument.
        ApplicationTemplate.start("RouteGIS Application", AppFrame.class);
    }
}
