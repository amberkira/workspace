

package com.routegis.users.example;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.MainClass;


public class LocalDataOnly extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Tell the status bar not to check for network activity
            this.getStatusBar().setShowNetworkStatus(false);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Local Data Only", AppFrame.class);

        // Force RouteGIS SDK not to use the network
        MainClass.getNetworkStatus().setOfflineMode(true);
    }
}
