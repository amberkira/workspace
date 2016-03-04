package com.routegis.applications.datachoose;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import core.routegis.engine.MainClass;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.formats.gpx.GpxReader;
import core.routegis.engine.geom.Angle;
import core.routegis.engine.geom.LatLon;
import core.routegis.engine.geom.Position;
import core.routegis.engine.layers.IconLayer;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.ogc.kml.KMLRoot;
import core.routegis.engine.ogc.kml.impl.KMLController;
import core.routegis.engine.render.BasicShapeAttributes;
import core.routegis.engine.render.GlobeAnnotation;
import core.routegis.engine.render.Material;
import core.routegis.engine.render.Path;
import core.routegis.engine.render.Polygon;
import core.routegis.engine.render.ShapeAttributes;
import core.routegis.engine.render.SurfaceCircle;
import core.routegis.engine.render.UserFacingIcon;
import core.routegis.engine.render.WWIcon;
import core.routegis.engine.util.layertree.KMLLayerTreeNode;
import core.routegis.engine.util.layertree.KMLNetworkLinkTreeNode;

public class SharpAdder {

	
	public static void addLabel(String text, double lat, double lon, String font, String color,RenderableLayer labelsLayer)
    {
        GlobeAnnotation ga = new GlobeAnnotation(text, Position.fromDegrees(lat, lon, 0),
            Font.decode(font), Color.decode(color));
        ga.getAttributes().setBackgroundColor(Color.BLACK);
        ga.getAttributes().setDrawOffset(new Point(0, 0));
        ga.getAttributes().setFrameShape(AVKey.SHAPE_NONE);
        ga.getAttributes().setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        ga.getAttributes().setTextAlign(AVKey.CENTER);
        labelsLayer.addRenderable(ga);
    }
    
    public static void addIcon(String text, double lat, double lon, String iconpath,IconLayer IconLayer)
    {
    	Iterable<WWIcon> iconlist =  IconLayer.getIcons();
    	Iterator<? extends WWIcon> iterator = iconlist.iterator();
    	while (iterator.hasNext()) {
    		WWIcon icon = iterator.next();
    		if (icon.getToolTipText().equals(text)){
    			icon.setPosition(new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), 0));
    			if (!iconpath.isEmpty()){
    				Dimension d=new Dimension(32, 32);
    				icon.setSize(d);
    				icon.setImageSource(iconpath);
    			}
    			return;
    		}
    	}
    	
    	UserFacingIcon icon = new UserFacingIcon(iconpath,
    			new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), 0));
    	icon.setSize(new Dimension(64, 64));
    	icon.setToolTipText(text);
    	icon.setShowToolTip(true);
    	
    	IconLayer.addIcon(icon);
    }
    
    public static void addNode(String text, InputStream in,RenderableLayer NodeLayer)
    {
    	try {
    		ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(Material.RED);
            attrs.setOutlineWidth(2d);
            
			GpxReader reader = new GpxReader();
			reader.readStream(in);
            Iterator<Position> positions = reader.getTrackPositionIterator();
            
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            while (positions.hasNext())
            {
            	pathPositions.add(positions.next());
            }
            
            Path path = new Path(pathPositions);

            path.setAttributes(attrs);
            path.setVisible(true);
            path.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
            path.setPathType(AVKey.GREAT_CIRCLE);
            
            NodeLayer.addRenderable(path);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
    public static void addPolygon(String name, String uri,RenderableLayer SpecialLayer){
		try {
			
			URL url = new URL(uri);
			URLConnection connection = url.openConnection();
			//connection.setRequestProperty("User-Agent", getAppletInfo()); //$NON-NLS-1$
			connection.setConnectTimeout(35000);
		
			InputStream is = connection.getInputStream();
			StringBuilder responseBody = new StringBuilder();
			if (is != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8")); //$NON-NLS-1$
				String s;
				boolean first = true;
				while ((s = in.readLine()) != null) {
					if(first){
						first = false;
					} else {
						responseBody.append("\n"); //$NON-NLS-1$
					}
					responseBody.append(s);
					
				}
				is.close();
			}
			addPolygon(name, new ByteArrayInputStream(responseBody.toString().getBytes()),SpecialLayer);  
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public static void addPolygon(String text, InputStream in,RenderableLayer SpecialLayer)
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
            SpecialLayer.addRenderable(pgon);
            
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public static void addwind(RenderableLayer windLayer){
    	LatLon position = new LatLon(Angle.fromDegrees(47), Angle.fromDegrees(120));
    	   BasicShapeAttributes attr=new BasicShapeAttributes();
    	  // attr.setImageSource("images/wind/111.png");
    	   attr.setOutlineMaterial(Material.BLACK);
    	   attr.setDrawInterior(true);
    	   SurfaceCircle C=new SurfaceCircle(position, 32);
    	   //C.setAttributes(attr);
    	   windLayer.addRenderable(C);
    }
    
    public static void streamCopy(InputStream in, OutputStream out) throws IOException{
		byte[] b = new byte[1024];
		int read;

		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

}
