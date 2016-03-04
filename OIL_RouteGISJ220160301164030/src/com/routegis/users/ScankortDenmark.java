
package com.routegis.users;

import core.routegis.engine.*;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.geom.*;
import core.routegis.engine.globes.*;
import core.routegis.engine.layers.*;
import core.routegis.engine.terrain.*;


public class ScankortDenmark
{
    public static void setupConfiguration()
    {
        Sector sector = getDenmarkSector();
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, computeZoomForExtent(sector));
        Configuration.setValue(AVKey.INITIAL_LATITUDE, sector.getCentroid().getLatitude().degrees);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, sector.getCentroid().getLongitude().degrees);
    }

    public static Sector getDenmarkSector()
    {
        return new Sector(
            Angle.fromDMS("54\u00B0 33\u2019 35\u201d"), Angle.fromDMS("57\u00B0 45\u2019 7\u201d"),
            Angle.fromDMS("8\u00B0 4\u2019 22\u201d"), Angle.fromDMS("15\u00B0 11\u2019 55\u201d"));
    }

    public static double computeZoomForExtent(Sector sector)
    {
        Angle delta = sector.getDeltaLat();
        if (sector.getDeltaLon().compareTo(delta) > 0)
            delta = sector.getDeltaLon();
        double arcLength = delta.radians * Earth.WGS84_EQUATORIAL_RADIUS;
        double fieldOfView = Configuration.getDoubleValue(AVKey.FOV, 45.0);
        return arcLength / (2 * Math.tan(fieldOfView / 2.0));
    }

    private static class MyAppFrame extends ApplicationTemplate.AppFrame
    {
        public MyAppFrame()
        {
            super();

            // Add the high resolution imagery.
            Factory layerFactory = new BasicLayerFactory();
            Layer layer = (Layer) layerFactory.createFromConfigSource("config/Earth/ScankortDenmarkImageLayer.xml",
                null);
            layer.setEnabled(true);
            ApplicationTemplate.insertBeforePlacenames(this.getMainWin(), layer);
            this.getLayerPanel().update(this.getMainWin());

            // Add the high resolution elevations.
            Factory emf = new BasicElevationModelFactory();
            ElevationModel em = (ElevationModel)
                emf.createFromConfigSource("config/Earth/ScankortDenmarkDSMElevationModel.xml", null);
            if (this.getMainWin().getModel().getGlobe().getElevationModel() instanceof CompoundElevationModel)
                ((CompoundElevationModel)
                    this.getMainWin().getModel().getGlobe().getElevationModel()).addElevationModel(em);
            else
                this.getMainWin().getModel().getGlobe().setElevationModel(em); // replace current EM
        }
    }

    public static void main(String[] args)
    {
        setupConfiguration();
        ApplicationTemplate.start("Scankort Denmark Data", MyAppFrame.class);
    }
}
