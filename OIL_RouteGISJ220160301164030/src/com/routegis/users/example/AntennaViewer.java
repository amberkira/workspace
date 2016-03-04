package com.routegis.users.example;

import java.io.InputStream;

import com.routegis.applications.antenna.AntennaAxes;
import com.routegis.applications.antenna.AntennaModel;
import com.routegis.applications.antenna.Interpolator2D;
import com.routegis.users.ApplicationTemplate;

import core.routegis.engine.*;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.geom.*;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.*;
import core.routegis.engine.util.InOut;


public class AntennaViewer extends ApplicationTemplate
{
    protected static Position ANTENNA_POSITION = Position.fromDegrees(35, -120, 1e3);

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            ShapeAttributes normalAttributes = new BasicShapeAttributes();
            normalAttributes.setOutlineOpacity(0.6);
            normalAttributes.setInteriorOpacity(0.4);
            normalAttributes.setOutlineMaterial(Material.WHITE);

            ShapeAttributes highlightAttributes = new BasicShapeAttributes(normalAttributes);
            highlightAttributes.setOutlineOpacity(0.3);
            highlightAttributes.setInteriorOpacity(0.6);

            AntennaModel gain = new AntennaModel(makeInterpolator());
            gain.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
            gain.setPosition(ANTENNA_POSITION);
            gain.setAzimuth(Angle.fromDegrees(30));
            gain.setElevationAngle(Angle.fromDegrees(20));
            gain.setAttributes(normalAttributes);
            gain.setHighlightAttributes(highlightAttributes);
            gain.setGainOffset(640);
            gain.setGainScale(10);

            AntennaAxes axes = new AntennaAxes();
            axes.setLength(2 * gain.getGainOffset());
            axes.setRadius(0.02 * axes.getLength());
            axes.setAltitudeMode(gain.getAltitudeMode());
            axes.setPosition(gain.getPosition());
            axes.setAzimuth(gain.getAzimuth());
            axes.setElevationAngle(gain.getElevationAngle());

            ShapeAttributes normalAxesAttributes = new BasicShapeAttributes();
            normalAttributes.setInteriorOpacity(0.5);
            normalAxesAttributes.setInteriorMaterial(Material.RED);
            normalAxesAttributes.setEnableLighting(true);
            axes.setAttributes(normalAxesAttributes);

            RenderableLayer layer = new RenderableLayer();
            layer.addRenderable(gain);
            layer.setName("Antenna Gain");
            insertBeforeCompass(getMainWin(), layer);

            layer = new RenderableLayer();
            layer.addRenderable(axes);
            layer.setName("Antenna Axes");
            layer.setPickEnabled(false);
            insertBeforeCompass(getMainWin(), layer);

            this.getLayerPanel().update(this.getMainWin());
        }
    }

    private static Interpolator2D makeInterpolator()
    {
        Interpolator2D interpolator = new Interpolator2D();
        interpolator.setWrapT(true); // wrap along "phi"

        try
        {
            InputStream is = InOut.openFileOrResourceStream(
                "data/ThetaPhi3.antennaTestFile.txt", AntennaViewer.class);
            interpolator.addFromStream(is);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return interpolator;
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.INITIAL_LATITUDE, ANTENNA_POSITION.getLatitude().degrees);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, ANTENNA_POSITION.getLongitude().degrees);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 10e3);

        ApplicationTemplate.start("RouteGIS SDK Antenna Gain Visualization", AppFrame.class);
    }
}
