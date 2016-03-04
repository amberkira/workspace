
package com.routegis.users.example;

import javax.swing.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.PlaceNamesPanel;
import com.routegis.users.ApplicationTemplate.AppFrame;

import java.awt.*;


public class PlaceNames extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);
            this.getLayerPanel().add(makeControlPanel(),  BorderLayout.SOUTH);
        }


        private JPanel makeControlPanel()
        { 
            return new PlaceNamesPanel(this.getMainWin());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Place Names", AppFrame.class);
    }
}
