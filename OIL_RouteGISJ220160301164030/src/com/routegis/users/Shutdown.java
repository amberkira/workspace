

package com.routegis.users;

import javax.swing.*;

import core.routegis.engine.*;
import core.routegis.engine.awt.WindowGLCanvas;

import java.awt.*;
import java.awt.event.*;


public class Shutdown
{
    private static class AppFrame extends javax.swing.JFrame
    {
        private WorldWindow wwd;
        private ShutdownWindowAction shutdownAction;
        private CreateWindowAction createWindowAction;

        public AppFrame()
        {
            this.getContentPane().setLayout(new BorderLayout(10, 10));

            JPanel controlPanel = new JPanel(new BorderLayout(10, 10));
            this.getContentPane().add(controlPanel, BorderLayout.WEST);

            JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 10, 10));
            controlPanel.add(buttonPanel, BorderLayout.NORTH);

            this.shutdownAction = new ShutdownWindowAction();
            buttonPanel.add(new JButton(this.shutdownAction));
            this.shutdownAction.setEnabled(true);

            this.createWindowAction = new CreateWindowAction();
            buttonPanel.add(new JButton(this.createWindowAction));

            buttonPanel.add(new JButton(new ShutdownWorldWindAction()));

            this.createWindow();

            this.pack();
        }

        private void createWindow()
        {
            WindowGLCanvas wwc = new WindowGLCanvas();
            wwc.setPreferredSize(new java.awt.Dimension(800, 600));
            this.getContentPane().add(wwc, java.awt.BorderLayout.CENTER);
            wwc.setModel(new BasicModel());
            this.wwd = wwc;
        }

        private void destroyCurrentWindow()
        {
            if (this.wwd != null)
            {
                getContentPane().remove((Component) wwd);
                wwd = null;
            }
        }

        private class ShutdownWindowAction extends AbstractAction
        {
            public ShutdownWindowAction()
            {
                super("Shutdown Window");
            }

            public void actionPerformed(ActionEvent e)
            {
                if (wwd != null)
                {
                    wwd.shutdown();
                    destroyCurrentWindow();
                    this.setEnabled(false);
                    createWindowAction.setEnabled(true);
                }
            }
        }

        private class CreateWindowAction extends AbstractAction
        {
            public CreateWindowAction()
            {
                super("Create Window");
                this.setEnabled(false);
            }

            public void actionPerformed(ActionEvent e)
            {
                createWindow();
                pack();
                this.setEnabled(false);
                shutdownAction.setEnabled(true);
            }
        }

        private class ShutdownWorldWindAction extends AbstractAction
        {
            public ShutdownWorldWindAction()
            {
                super("Shutdown RouteGIS SDK");
            }

            public void actionPerformed(ActionEvent e)
            {
                MainClass.shutDown();
                destroyCurrentWindow();
                createWindowAction.setEnabled(true);
            }
        }
    }

    public static void main(String[] args)
    {
        if (Configuration.isMacOS())
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Shutdown RouteGIS SDK");
        }

        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                // Create an AppFrame and immediately make it visible. As per Swing convention, this
                // is done within an invokeLater call so that it executes on an AWT thread.
                AppFrame appFrame = new AppFrame();
                appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                appFrame.setVisible(true);
            }
        });
    }
}
