package com.routegis.applications.datachoose;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import core.routegis.engine.Configuration;
import core.routegis.engine.layers.RenderableLayer;

public class EmployeeLoader implements loaderface {

	public ArrayList<String> mArray;
	private String itemName;
	@Override
	public void fetchURI() {
		// TODO Auto-generated method stub

	}

	@Override
	public void analysedata(String uri, String cachefile) {
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


				URL url;
				try {
					url = new URL(uri);
				
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
					NodeList list=document.getElementsByTagName("Name");
					for(int i=0;i<=list.getLength();i++){
						Element ID=(Element)list.item(i);
						itemName=ID.getAttribute("Name");
						mArray.add(itemName);
					}
				}

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	@Override
	public void cleardata() {
		// TODO Auto-generated method stub

	}

	@Override
	public void streamCopy(InputStream in, OutputStream out) throws IOException {
		byte[] b = new byte[1024];
		int read;

		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}

	}

	@Override
	public RenderableLayer getRenderableLayer(RenderableLayer rLayer) {
		// TODO Auto-generated method stub
		return null;
	}

}
