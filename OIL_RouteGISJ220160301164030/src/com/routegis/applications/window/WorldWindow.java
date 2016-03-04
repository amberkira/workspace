package com.routegis.applications.window;

import java.awt.*;
import java.util.logging.Level;

import com.routegis.applications.window.core.*;
import com.routegis.applications.window.util.Util;

import core.routegis.engine.Configuration;


public class WorldWindow
{
    static
    {
        System.setProperty("core.routegis.app.config.document",
            "config/worldwindow.xml");
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            String s = Configuration.getStringValue(Constants.APPLICATION_DISPLAY_NAME);
            if (s == null)
                s = "RouteGIS Window";
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", s);
        }
        else if (Configuration.isWindowsOS())
        {
            System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
        }
    }

    private static final String APP_CONFIGURATION
        = "config/AppConfiguration.xml";

    public static void main(String[] args)
    {
        Controller controller = new Controller();

        Dimension appSize = null;
        if (args.length >= 2) // The first two arguments are the application width and height.
            appSize = new Dimension(Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        try
        {
            controller.start(APP_CONFIGURATION, appSize);
        }
        catch (Exception e)
        {
            String msg = "Fatal application error";
            controller.showErrorDialog(null, "Cannot Start Application", msg);
            Util.getLogger().log(Level.SEVERE, msg);
        }
    }
}
