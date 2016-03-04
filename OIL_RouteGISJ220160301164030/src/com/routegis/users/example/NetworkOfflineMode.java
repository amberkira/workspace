

package com.routegis.users.example;

import javax.swing.*;
import javax.swing.border.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.MainClass;

import java.awt.*;
import java.awt.event.*;


public class NetworkOfflineMode extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            this.getLayerPanel().add(makeControlPanel(), BorderLayout.SOUTH);
        }

        protected JPanel makeControlPanel()
        {
            JPanel panel = new JPanel(new BorderLayout(5, 5));
            panel.setBorder(new CompoundBorder(new EmptyBorder(0, 10, 15, 10), new EtchedBorder()));

            JCheckBox modeSwitch = new JCheckBox(new AbstractAction(" Online")
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    // Get the current status
                    boolean offline = MainClass.getNetworkStatus().isOfflineMode();

                    // Change it to its opposite
                    offline = !offline;
                    MainClass.getNetworkStatus().setOfflineMode(offline);

                    // Cause data retrieval to resume if now online
                    if (!offline)
                        getMainWin().redraw();
                }
            });
            modeSwitch.setSelected(true); // WW starts out online
            panel.add(modeSwitch, BorderLayout.CENTER);

            return panel;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Network Offline Mode", AppFrame.class);
    }
}
