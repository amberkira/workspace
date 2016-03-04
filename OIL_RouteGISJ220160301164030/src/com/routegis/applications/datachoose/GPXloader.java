package com.routegis.applications.datachoose;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.routegis.users.applet.CommonApplet;

import core.routegis.engine.Configuration;
import core.routegis.engine.MainClass;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.formats.gpx.GpxReader;
import core.routegis.engine.geom.Position;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.BasicShapeAttributes;
import core.routegis.engine.render.Material;
import core.routegis.engine.render.Polygon;
import core.routegis.engine.render.ShapeAttributes;
import sun.misc.BASE64Decoder;


public class GPXloader implements loaderface {
	
    protected RenderableLayer mlayer;
	
	static{
		final String SERVERIP="42.62.65.182";
	}
	
	@Override
	public void fetchURI() {
		// TODO 与服务器交互 发送ID继而获取uri
        
	}

	@Override
	public void analysedata(String uri, String cachefile) {
		// TODO Auto-generated method stub
		BufferedInputStream inputStream = null;
		FileOutputStream stream = null;
		File fileToSave = null;
		File dir=new File(Configuration.getUserHomeDirectory());
		if(dir.getParentFile().canWrite()){
			dir.mkdirs();
			if(dir.exists()){
				fileToSave=new File(dir,cachefile);
			}
		}

			try {
				URL url=new URL(uri);
				URLConnection conn=url.openConnection();
				conn.setConnectTimeout(35000);
				inputStream = new BufferedInputStream(conn.getInputStream(), 8 * 1024);
				if(fileToSave!=null){
					stream=new FileOutputStream(fileToSave);
					streamCopy(inputStream, stream);
					stream.flush();
				    DocumentBuilder db=DocumentBuilderFactory.
								     newInstance().newDocumentBuilder();
					Document document = db.parse(new FileInputStream(fileToSave));
					NodeList gpxlist = document.getElementsByTagName("GPX"); 
					for(int i = 0; i < gpxlist.getLength(); i++)  
					{
						Element gpx = (Element)gpxlist.item(i);  
						String name = gpx.getAttribute("name");
						String type = gpx.getAttribute("type");
						String b64data = gpx.getTextContent();
						
						byte[] bytegpx = (new BASE64Decoder()).decodeBuffer(b64data);
						ByteArrayInputStream is = new ByteArrayInputStream(bytegpx);
						if (type.equalsIgnoreCase("polygon")){
							addPolygon(name, is);
						}else{
							//addNode(name, is);
						}   
					}
				}
			
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 		
	}

	@Override
	public void cleardata() {
		mlayer.removeAllRenderables();
	}

	@Override
	public void streamCopy(InputStream in, OutputStream out) throws IOException {
		byte[] b = new byte[1024];
		int read;

		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
		
	}

	 public void addPolygon(String text, InputStream in)
	    {
	    	GpxReader reader;
			try {
				reader = new GpxReader();
			
				reader.readStream(in);
		        Iterator<Position> positions = reader.getTrackPositionIterator();
		        
		        ArrayList<Position> pathPositions = new ArrayList<Position>();
		        while (positions.hasNext())
		        {
		        	pathPositions.add(positions.next());
		        }
		        
		        ShapeAttributes normalAttributes = new BasicShapeAttributes();
	            normalAttributes.setInteriorMaterial(Material.YELLOW);
	            normalAttributes.setOutlineOpacity(0.5);
	            normalAttributes.setInteriorOpacity(0.8);
	            normalAttributes.setOutlineMaterial(Material.GREEN);
	            normalAttributes.setOutlineWidth(2);
	            normalAttributes.setDrawOutline(true);
	            normalAttributes.setDrawInterior(true);
	            normalAttributes.setEnableLighting(true);

	            ShapeAttributes highlightAttributes = new BasicShapeAttributes(normalAttributes);
	            highlightAttributes.setOutlineMaterial(Material.WHITE);
	            highlightAttributes.setOutlineOpacity(1);
	            
		        Polygon pgon = new Polygon(pathPositions);
		        pgon.setValue(AVKey.DISPLAY_NAME, text);
	            pgon.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
	            pgon.setAttributes(normalAttributes);
	            pgon.setHighlightAttributes(highlightAttributes);
	            //pgon.setRotation(-170d);
	            if(mlayer!=null){
	            	mlayer.addRenderable(pgon);
	            }else{
	            	System.out.println("mlayer is null");
	            }
	            
			} catch (ParserConfigurationException | SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
/**
 * 获取当前引用接口的RenderabelLayer
 */
	@Override
	public RenderableLayer getRenderableLayer(RenderableLayer rLayer) {
		this.mlayer=rLayer;
		return mlayer;
	}
}
