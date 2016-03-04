

package com.routegis.users.kml;

import javax.xml.stream.*;

import core.routegis.engine.Exportable;
import core.routegis.engine.ogc.kml.KMLConstants;
import core.routegis.engine.ogc.kml.gx.GXConstants;
import core.routegis.engine.util.Logging;

import java.io.*;
import java.util.zip.*;


public class KMZDocumentBuilder
{
    protected ZipOutputStream zipStream;
    protected XMLStreamWriter writer;

    
    public KMZDocumentBuilder(OutputStream stream) throws XMLStreamException, IOException
    {
        if (stream == null)
        {
            String message = Logging.getMessage("nullValue.OutputStreamIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.zipStream = new ZipOutputStream(stream);
        this.startDocument();
    }

    
    protected String getMainFileName()
    {
        return "doc.kml";
    }

    
    protected void startDocument() throws XMLStreamException, IOException
    {
        // Create a zip entry for the main KML file
        this.zipStream.putNextEntry(new ZipEntry(this.getMainFileName()));

        this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(this.zipStream);

        this.writer.writeStartDocument();
        this.writer.writeStartElement("kml");
        this.writer.writeDefaultNamespace(KMLConstants.KML_NAMESPACE);
        this.writer.setPrefix("gx", GXConstants.GX_NAMESPACE);
        this.writer.writeNamespace("gx", GXConstants.GX_NAMESPACE);
        this.writer.writeStartElement("Document");
    }

    
    protected void endDocument() throws XMLStreamException, IOException
    {
        this.writer.writeEndElement(); // Document
        this.writer.writeEndElement(); // kml
        this.writer.writeEndDocument();

        this.writer.close();

        this.zipStream.closeEntry();
        this.zipStream.finish();
    }

    
    public void close() throws XMLStreamException, IOException
    {
        this.endDocument();
    }

    
    public void writeObject(Exportable exportable) throws IOException
    {
        String supported = exportable.isExportFormatSupported(KMLConstants.KML_MIME_TYPE);
        if (Exportable.FORMAT_SUPPORTED.equals(supported) || Exportable.FORMAT_PARTIALLY_SUPPORTED.equals(supported))
        {
            exportable.export(KMLConstants.KML_MIME_TYPE, this.writer);
        }
    }

    
    public void writeObjects(Exportable... exportables) throws IOException
    {
        for (Exportable exportable : exportables)
        {
            exportable.export(KMLConstants.KML_MIME_TYPE, this.writer);
        }
    }
}
