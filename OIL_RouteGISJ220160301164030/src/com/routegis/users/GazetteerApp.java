
package com.routegis.users;

import java.awt.*;


public class GazetteerApp extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame() throws IllegalAccessException, InstantiationException, ClassNotFoundException
        {
            super(true, false, false);

            this.getContentPane().add(new GazetteerPanel(this.getMainWin(), null),   //use default yahoo service
                BorderLayout.NORTH);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Gazetteer Example", AppFrame.class);
    }
}
