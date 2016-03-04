

package com.routegis.users;

import javax.swing.*;

import core.routegis.engine.BasicModel;
import core.routegis.engine.awt.WindowGLCanvas;


public class SimplestPossibleExample extends JFrame
{
    public SimplestPossibleExample()
    {
        WindowGLCanvas wwd = new WindowGLCanvas();
        wwd.setPreferredSize(new java.awt.Dimension(1000, 800));
        this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
        wwd.setModel(new BasicModel());
    }

    public static void main(String[] args)
    {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame frame = new SimplestPossibleExample();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}