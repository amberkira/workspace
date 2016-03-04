

package com.routegis.users.util;

import java.io.*;
import java.nio.ByteBuffer;

import core.routegis.engine.WorldWindow;
import core.routegis.engine.geom.*;
import core.routegis.engine.util.*;


public class Util
{
    
    public static File unzipAndSaveToTempFile(String path, String suffix)
    {
        if (PrivateUtil.isEmpty(path))
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        InputStream stream = null;

        try
        {
            stream = InOut.openStream(path);

            ByteBuffer buffer = InOut.readStreamToBuffer(stream);
            File file = InOut.saveBufferToTempFile(buffer, InOut.getFilename(path));

            buffer = InOut.readZipEntryToBuffer(file, null);
            return InOut.saveBufferToTempFile(buffer, suffix);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            InOut.closeStream(stream, path);
        }

        return null;
    }

    
    public static File saveResourceToTempFile(String path, String suffix)
    {
        if (PrivateUtil.isEmpty(path))
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        InputStream stream = null;
        try
        {
            stream = InOut.openStream(path);

            ByteBuffer buffer = InOut.readStreamToBuffer(stream);
            return InOut.saveBufferToTempFile(buffer, suffix);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            InOut.closeStream(stream, path);
        }

        return null;
    }

    
    public static void goTo(WorldWindow wwd, Sector sector)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Create a bounding box for the specified sector in order to estimate its size in model coordinates.
        Box extent = Sector.computeBoundingBox(wwd.getModel().getGlobe(),
            wwd.getSceneController().getVerticalExaggeration(), sector);

        // Estimate the distance between the center position and the eye position that is necessary to cause the sector to
        // fill a viewport with the specified field of view. Note that we change the distance between the center and eye
        // position here, and leave the field of view constant.
        Angle fov = wwd.getView().getFieldOfView();
        double zoom = extent.getRadius() / fov.cosHalfAngle() / fov.tanHalfAngle();

        // Configure OrbitView to look at the center of the sector from our estimated distance. This causes OrbitView to
        // animate to the specified position over several seconds. To affect this change immediately use the following:
        // ((OrbitView) wwd.getView()).setCenterPosition(new Position(sector.getCentroid(), 0d));
        // ((OrbitView) wwd.getView()).setZoom(zoom);
        wwd.getView().goTo(new Position(sector.getCentroid(), 0d), zoom);
    }
}
