package com.routegis.examplewinddata;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.routegis.users.applet.CommonApplet;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import core.routegis.engine.View;
import core.routegis.engine.WorldWindow;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.avlist.AVList;
import core.routegis.engine.awt.WindowGLCanvas;
import core.routegis.engine.geom.LatLon;
import core.routegis.engine.geom.Position;
import core.routegis.engine.layers.Layer;
import core.routegis.engine.layers.LayerList;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.Polyline;
import core.routegis.engine.render.SurfaceImage;
import core.routegis.engine.render.SurfaceText;

public class winddata {
    private int RotationAngle;
    private String FilePath;
    private double stride=0.03;//璁＄畻杈圭晫澶у皬鐨勬杩涘��
    public void drawwind(WindowGLCanvas wwd,ArrayList<LatLon> windlatlon,double[] speedValues,double[] direction) throws IOException{
    	 RenderableLayer layer = new RenderableLayer();
         layer.setName("Surface Images");
         layer.setPickEnabled(false);
    	
    	for(int i=0;i<windlatlon.size();i++){
    		ArrayList<LatLon> corners=boundaryCalculate(windlatlon.get(i));
    		windScale(speedValues[i], direction[i]);
    		SurfaceImage si1 = new SurfaceImage(makeImage(null, RotationAngle, FilePath),corners);
    		
    		//Polyline boundary = new Polyline(si1.getCorners(), 0);
           // boundary.setFollowTerrain(false);
            //boundary.setClosed(true);
           // boundary.setPathType(Polyline.RHUMB_LINE);
            //boundary.setColor(new Color(0, 255, 0,0));
    		Double meters=format(Math.random()*3);
    		String mhigh=meters+"绫�";
    		Double meters1=format(Math.random()*5);
    		String mhigh1=meters1+"绫�";
    		String total=mhigh+" "+mhigh1;
            SurfaceText surfaceText = new SurfaceText(total, Position.fromDegrees(windlatlon.get(i).getLatitude().degrees-0.02,
            		windlatlon.get(i).getLongitude().degrees,0));
            surfaceText.setTextSize(1500);
            surfaceText.setColor(new Color(0,255,255));      
            layer.addRenderable(si1);
            //layer.addRenderable(boundary);
            layer.addRenderable(surfaceText);
            
    	}
    	insertBeforeLayerName(wwd, layer);
    }
    
    protected ArrayList<LatLon> boundaryCalculate(LatLon latlon){
    	ArrayList<LatLon> boundary=new ArrayList<>();
    	double lat=latlon.latitude.degrees;
    	double lon=latlon.longitude.degrees;
    	LatLon latlon1=new LatLon(LatLon.fromDegrees(lat-stride, lon-stride));
    	LatLon latlon2=new LatLon(LatLon.fromDegrees(lat-stride, lon+stride));
    	LatLon latlon3=new LatLon(LatLon.fromDegrees(lat+stride, lon+stride));
    	LatLon latlon4=new LatLon(LatLon.fromDegrees(lat+stride, lon-stride));
    	boundary.add(latlon1);
    	boundary.add(latlon2);
    	boundary.add(latlon3);
    	boundary.add(latlon4); 	
    	return boundary;
    }
	private double format(double degrees) {
		BigDecimal b=new BigDecimal(degrees);  
		double f1 =b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();  
		return f1;
	}
    /**
     * 椋庣骇鍒ゆ柇锛岃緭鍑篠cale
     * @param speed
     * @return
     */
	public void windScale(double speed,double angle){
		String filePath="images/wind/";//姝ゅ涓哄浘鐗囦繚瀛樼殑涓婄骇鐩綍
        int Scale;
		if(speed>0&&speed<0.3){
			Scale=0;
		}
		else if(speed>0.3&&speed<1.6){
			Scale=1;
		}
		else if(speed>1.6&&speed<3.4){
			Scale=2;
		}
		else if(speed>3.4&&speed<5.5){
			Scale=3;
		}
		else if(speed>5.5&&speed<8.0){
			Scale=4;
		}
		else if(speed>8.0&&speed<10.8){
			Scale=5;
		}
		else if(speed>10.8&&speed<13.9){
			Scale=6;
		}
		else if(speed>13.9&&speed<17.2){
			Scale=7;
		}
		else if(speed>17.2&&speed<20.8){
			Scale=8;
		}
		else if(speed>20.8&&speed<24.5){
			Scale=9;
		}
		else if(speed>24.5&&speed<28.5){
			Scale=10;
		}
		else if(speed>28.5&&speed<32.6){
			Scale=11;
		}
		else{
			Scale=12;
		}
        filePath=filePath+Scale+".png";
        FilePath=filePath;
        
        
        double offset=22.5;
		int Quadrant=(int)((angle+offset)/45);
		switch (Quadrant) {
		case 0:
			RotationAngle=0;//瀹氫箟姝ｅ寳鏂瑰悜涓鸿捣濮嬫柟鍚�
			break;
        case 1:
        	RotationAngle=45;//涓滃寳椋�
			break;
        case 2:
        	RotationAngle=90;//涓滈
	        break;
        case 3:
        	RotationAngle=135;//涓滃崡椋�
	        break;
        case 4:
        	RotationAngle=180;//鍗楅
	        break;
 
        case 5:
        	RotationAngle=225;//瑗垮崡椋�
	        break;
 
        case 6:
        	RotationAngle=270;//瑗块
	        break;
 
        case 7:
        	RotationAngle=315;//瑗垮寳椋�
	        break;
 
        case 8:
        	RotationAngle=0;//鍖楅
	        break;
		}
	}
	
	protected BufferedImage makeImage(View v,int rotation,String filepath) throws IOException
    {
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = image.createGraphics();
        
        //BufferedImage icon = ImageIO.read(new File(filepath));
        InputStream is = (InputStream) this.getClass().getClassLoader().getResourceAsStream(filepath);   
        BufferedImage icon = ImageIO.read(is);
    	g.drawImage(rotateImage(icon,rotation), 0, 0, 512, 512, null);
        g.dispose();
        
        /**if (r>350){
        	r = 0;
        }
        r+=1;**/

        return image;
    }
    public static BufferedImage rotateImage(final BufferedImage bufferedimage,
            final int degree) {
        int w = bufferedimage.getWidth();
        int h = bufferedimage.getHeight();
        int type = bufferedimage.getColorModel().getTransparency();
        BufferedImage img;
        Graphics2D graphics2d;
        (graphics2d = (img = new BufferedImage(h, w, type)).createGraphics())
                .setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2d.rotate(Math.toRadians(degree), w/2, h/2);
        graphics2d.drawImage(bufferedimage, 0, 0, null);
        graphics2d.dispose();
        return img;
    }
    
    public static void insertBeforeLayerName(WorldWindow wwd, Layer layer)
    {
        // Insert the layer into the layer list just before the target layer.
      wwd.getModel().getLayers().add(layer);

    }
	
	

	public static void main(String[] args) throws IOException {
		winddata wd=new winddata();
		double[] speedValues={3.5,10.6};
		double[] direction={45,55};
		LatLon latlon1=new LatLon(LatLon.fromDegrees(27, 55));
		LatLon latlon2=new LatLon(LatLon.fromDegrees(28, 56));
		ArrayList<LatLon> windlatlon=new ArrayList<>();
		windlatlon.add(latlon1);
		windlatlon.add(latlon2);
		WindowGLCanvas wwd=new WindowGLCanvas();
		wd.drawwind(wwd, windlatlon, speedValues, direction);

	}
}
