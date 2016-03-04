

package com.routegis.users;

import javax.swing.*;

import com.routegis.users.util.ScreenShotAction;

import core.routegis.engine.*;
import core.routegis.engine.awt.WindowGLCanvas;


public class ScreenShots extends JFrame
{
    static
    {
        // Ensure that menus and tooltips interact successfully with the WWJ window.
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
    }

    private WorldWindow wwd;

    public ScreenShots()
    {
        WindowGLCanvas wwd = new WindowGLCanvas();
        this.wwd = wwd;
        wwd.setPreferredSize(new java.awt.Dimension(1000, 800));
        this.getContentPane().add(wwd, java.awt.BorderLayout.CENTER);
        wwd.setModel(new BasicModel());
    }

    private JMenuBar createMenuBar()
    {
        JMenu menu = new JMenu("File");

        JMenuItem snapItem = new JMenuItem("Save Snapshot...");
        snapItem.addActionListener(new ScreenShotAction(this.wwd));
        menu.add(snapItem);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);

        return menuBar;
    }

    public static void main(String[] args)
    {
        // Swing components should always be instantiated on the event dispatch thread.
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                ScreenShots frame = new ScreenShots();

                frame.setJMenuBar(frame.createMenuBar()); // Create menu and associate with frame

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}
