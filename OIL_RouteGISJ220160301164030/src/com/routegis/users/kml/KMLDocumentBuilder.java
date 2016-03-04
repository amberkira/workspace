

package com.routegis.users.kml;

import javax.xml.stream.*;

import core.routegis.engine.Exportable;
import core.routegis.engine.ogc.kml.KMLConstants;
import core.routegis.engine.ogc.kml.gx.GXConstants;

import java.io.*;


public class KMLDocumentBuilder
{
    protected XMLStreamWriter writer;

    
    public KMLDocumentBuilder(Writer writer) throws XMLStreamException
    {
        this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
        this.startDocument();
    }

    
    public KMLDocumentBuilder(OutputStream stream) throws XMLStreamException
    {
        this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(stream);
        this.startDocument();
    }

    
    protected void startDocument() throws XMLStreamException
    {
        this.writer.writeStartDocument();
        this.writer.writeStartElement("kml");
        this.writer.writeDefaultNamespace(KMLConstants.KML_NAMESPACE);
        this.writer.setPrefix("gx", GXConstants.GX_NAMESPACE);
        this.writer.writeNamespace("gx", GXConstants.GX_NAMESPACE);
        this.writer.writeStartElement("Document");
    }

    
    protected void endDocument() throws XMLStreamException
    {
        this.writer.writeEndElement(); // Document
        this.writer.writeEndElement(); // kml
        this.writer.writeEndDocument();

        this.writer.close();
    }

    
    public void close() throws XMLStreamException
    {
        this.endDocument();
        this.writer.close();
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
            String supported = exportable.isExportFormatSupported(KMLConstants.KML_MIME_TYPE);
            if (Exportable.FORMAT_SUPPORTED.equals(supported)
                || Exportable.FORMAT_PARTIALLY_SUPPORTED.equals(supported))
            {
                exportable.export(KMLConstants.KML_MIME_TYPE, this.writer);
            }
        }
    }
}
