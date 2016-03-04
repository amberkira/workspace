
package com.routegis.applications.window.features;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import core.routegis.engine.MainClass;
import core.routegis.engine.avlist.*;
import core.routegis.engine.cache.FileStore;
import core.routegis.engine.data.*;
import core.routegis.engine.util.*;

import java.io.File;


public class DataImportUtil
{
    
    public static boolean isDataRaster(Object source, AVList params)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        DataRasterReaderFactory readerFactory;
        try
        {
            readerFactory = (DataRasterReaderFactory) MainClass.createConfigurationComponent(
                AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
        }
        catch (Exception e)
        {
            readerFactory = new BasicDataRasterReaderFactory();
        }

        params = (null == params) ? new AVListImpl() : params;
        DataRasterReader reader = readerFactory.findReaderFor(source, params);
        if (reader == null)
            return false;

        if (!params.hasKey(AVKey.PIXEL_FORMAT))
        {
            try
            {
                reader.readMetadata(source, params);
            }
            catch (Exception e)
            {
                String message = Logging.getMessage("generic.ExceptionWhileReading", e.getMessage());
                Logging.logger().finest(message);
            }
        }

        return AVKey.IMAGE.equals(params.getStringValue(AVKey.PIXEL_FORMAT))
            || AVKey.ELEVATION.equals(params.getStringValue(AVKey.PIXEL_FORMAT));
    }

    
    public static boolean isWWDotNetLayerSet(Object source)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String path = InOut.getSourcePath(source);
        if (path != null)
        {
            String suffix = InOut.getSuffix(path);
            if (suffix != null && !suffix.toLowerCase().endsWith("xml"))
                return false;
        }

        // Open the document in question as an XML event stream. Since we're only interested in testing the document
        // element, we avoiding any unnecessary overhead incurred from parsing the entire document as a DOM.
        XMLEventReader eventReader = null;
        try
        {
            eventReader = PrivateXML.openEventReader(source);

            // Get the first start element event, if any exists, then determine if it represents a LayerSet
            // configuration document.
            XMLEvent event = PrivateXML.nextStartElementEvent(eventReader);
            return event != null && DataConfigurationUtils.isWWDotNetLayerSetConfigEvent(event);
        }
        catch (Exception e)
        {
            Logging.logger().fine(Logging.getMessage("generic.ExceptionAttemptingToParseXml", source));
            return false;
        }
        finally
        {
            PrivateXML.closeEventReader(eventReader, source.toString());
        }
    }

    
    public static File getDefaultImportLocation(FileStore fileStore)
    {
        if (fileStore == null)
        {
            String message = Logging.getMessage("nullValue.FileStoreIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (File location : fileStore.getLocations())
        {
            if (fileStore.isInstallLocation(location.getPath()))
                return location;
        }

        return fileStore.getWriteLocation();
    }
}
