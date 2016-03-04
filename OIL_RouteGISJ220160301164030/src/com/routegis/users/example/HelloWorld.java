
package com.routegis.users.example;

import core.routegis.engine.*;
import core.routegis.engine.awt.WindowGLCanvas;


public class HelloWorld
{
    // An inner class is used rather than directly subclassing JFrame in the main class so
    // that the main can configure system properties prior to invoking Swing. This is
    // necessary for instance on OS X (Macs) so that the application name can be specified.

    private static class AppFrame extends javax.swing.JFrame
    {
        public AppFrame()
        {
            WindowGLCanvas wwd = new WindowGLCanvas();
            wwd.setPreferredSize(new java.awt.Dimension(1000, 800));
            this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
            this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
            this.pack();

            wwd.setModel(new BasicModel());
        }
    }

    public static void main(String[] args)
    {
        if (Configuration.isMacOS())
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Hello World");
        }

        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                // Create an AppFrame and immediately make it visible. As per Swing convention, this
                // is done within an invokeLater call so that it executes on an AWT thread.
                new AppFrame().setVisible(true);
            }
        });
    }
}