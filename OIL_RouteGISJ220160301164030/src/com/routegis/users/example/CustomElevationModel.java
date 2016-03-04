
package com.routegis.users.example;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.Configuration;
import core.routegis.engine.avlist.AVKey;


public class CustomElevationModel extends ApplicationTemplate
{
    public static void main(String[] args)
    {
        // Specify the configuration file for the elevation model prior to starting RouteGIS SDK:
        Configuration.setValue(AVKey.EARTH_ELEVATION_MODEL_CONFIG_FILE,
            "config/CustomElevationModel.xml");

        ApplicationTemplate.start("World Custom Elevation Model", AppFrame.class);
    }
}
