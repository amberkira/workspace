package com.routegis.examplewinddata;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.util.ArrayList;

import javax.swing.Timer;

import com.routegis.typerender.*;
import com.sun.javafx.geom.transform.BaseTransform.Degree;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.geom.Angle;
import core.routegis.engine.geom.Extent;
import core.routegis.engine.geom.LatLon;
import core.routegis.engine.geom.Sector;
import core.routegis.engine.layers.AnalyticSurface;
import core.routegis.engine.layers.AnalyticSurfaceAttributes;
import core.routegis.engine.layers.AnalyticSurfaceLegend;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.DrawContext;
import core.routegis.engine.render.Renderable;
import core.routegis.engine.util.BufferFactory;
import core.routegis.engine.util.BufferWrapper;
import core.routegis.engine.util.PrivateMath;

public class wavedata {
	ArrayList<Observation> obs=new ArrayList<>();
	SectorDivision sd=new SectorDivision();
	ArrayList<Integer> mlatValues=new ArrayList<>();;
	ArrayList<Integer> mlonValues=new ArrayList<>();
	double minlat=9000;
	double maxlat=-9000;
	double minlon=18000;
	double maxlon=-18000;
	public void exampleValues(double minHue, double maxHue, int width, int height,
	        RenderableLayer outLayer){
		double minValue=0;
		double maxValue=4;
		for(int i=0;i<3;i++){
			for(int j=0;j<=5;j++){
			mlatValues.add(3767-i);
			mlonValues.add(11905-i+j);		
			}
		}
		for(int i=0;i<5;i++){
			for(int j=0;j<19+i;j++){
			mlatValues.add(3764-i);
			mlonValues.add(11903-i+j);
			}
		}
		for(int i=0;i<5;i++){
			for(int j=0;j<40;j++){
			//Observation sta=new Observation();
			//LatLon latlon=new LatLon(Angle.fromDegrees(37.59-i*0.01),Angle.fromDegrees(118.94+j*0.01));
			//sta.setLatLon(latlon);
			//obs.add(sta);
				mlatValues.add(3759-i);

				mlonValues.add(11894+j);
			}
		}
		for(int i=0;i<27;i++){
			for(int j=0;j<40;j++){
			//Observation sta=new Observation();
			//LatLon latlon=new LatLon(Angle.fromDegrees(37.54 -i*0.01),Angle.fromDegrees(118.94+j*0.01));
			//sta.setLatLon(latlon);
			//obs.add(sta);
				mlatValues.add(3754 -i);
				mlonValues.add(11894+j);
			}
		}	
		for(int i=0;i<mlatValues.size();i++){
			minlat=Math.min(minlat, mlatValues.get(i));
			minlon=Math.min(minlon, mlonValues.get(i));
			maxlat=Math.max(maxlat, mlatValues.get(i));
			maxlon=Math.max(maxlon, mlonValues.get(i));
		}
		double Xstride=Math.abs((maxlon-minlon)/(40-1));//妯悜姝ヨ繘
		double Ystride=Math.abs((maxlat-minlat)/(40-1));//鑾峰彇缃戞牸绾靛悜姝ヨ繘
		for(int i=0;i<mlatValues.size();i++){
			Observation sta=new Observation();
			int x=(int) (((mlonValues.get(i)-minlon))/Xstride);
			int y=(int) (((mlatValues.get(i)-minlat))/Ystride);
				sta.setLoc(x+(39-y)*40);
				obs.add(sta);
		}
		for(int i=0;i<obs.size();i++){
			double data=Math.random()*5;
			LatLon latlon=new LatLon(Angle.fromDegrees(mlatValues.get(i)*0.01),Angle.fromDegrees(mlonValues.get(i)*0.01));
			obs.get(i).setData(data);
			obs.get(i).setLatLon(latlon);
		}
		
		Sector s=sd.SectorConstructor(obs, 40, 40);
		double[] values=new double[1600];
		for (int i=0;i<obs.size();i++){
			values[obs.get(i).getLoc()]=obs.get(i).getData(0);
		}
		smoothValues(40, 40, values, 0.5);
        smoothValues(40, 40, values, 0.7);
		scaleValues(values, 1600, -4, 4);

		BufferFactory factory=new BufferFactory.DoubleBufferFactory();
		BufferWrapper buffer = factory.newBuffer(1600);
        buffer.putDouble(0, values, 0, 1600);
        
        
        AnalyticSurface surface = new AnalyticSurface();
        surface.setSector(s);
        surface.setAltitude(100);
        surface.setDimensions(40, 40);
        surface.setClientLayer(outLayer);
        outLayer.addRenderable(surface);

        ArrayList<AnalyticSurface.GridPointAttributes> attributesList
        = new ArrayList<AnalyticSurface.GridPointAttributes>();
        for (int i = 0; i < values.length; i++)
        {            
            attributesList.add(
                AnalyticSurface.createColorGradientAttributes(values[i], 0, 4, minHue, maxHue));
        }
        surface.setValues(attributesList);
        
       
        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setShadowOpacity(0);
        attr.setDrawOutline(false);
        surface.setSurfaceAttributes(attr);
        
        final double altitude =0;
        System.out.println("altitude "+altitude);
        final double verticalScale = surface.getVerticalScale();
        System.out.println("verticalScale "+verticalScale);
        Format legendLabelFormat = new DecimalFormat("# m")
        {
            public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition)
            {
                double altitudeMeters = altitude + verticalScale * number;
                System.out.println(number+" num");
                //double altitudeKm = altitudeMeters * WWMath.METERS_TO_KILOMETERS;
                return super.format(altitudeMeters, result, fieldPosition);
            }
        };

        AnalyticSurfaceLegend legend = AnalyticSurfaceLegend.fromColorGradient(minValue, maxValue, minHue, maxHue,
            AnalyticSurfaceLegend.createDefaultColorGradientLabels(minValue, maxValue, legendLabelFormat),
            AnalyticSurfaceLegend.createDefaultTitle("GNSS-R DATA"));
        legend.setOpacity(0.8);
        legend.setScreenLocation(new Point(650, 300));
        outLayer.addRenderable(createLegendRenderable(surface, 300, legend));
	}	
	private double format(double degrees) {
		BigDecimal b=new BigDecimal(degrees);  
		double f1 =b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();  
		return f1;
	}
	
	 //鏈�鍚庡姞宸ヨ幏寰楀钩婊戞暟鎹�
    public void smoothValues(int width, int height, double[] values, double smoothness)
    {
        // top to bottom
        for (int x = 0; x < width; x++)
        {
            smoothBand(values, x, width, height, smoothness);
        }

        // bottom to top
        int lastRowOffset = (height - 1) * width;
        for (int x = 0; x < width; x++)
        {
            smoothBand(values, x + lastRowOffset, -width, height, smoothness);
        }

        // left to right
        for (int y = 0; y < height; y++)
        {
            smoothBand(values, y * width, 1, width, smoothness);
        }

        // right to left
        int lastColOffset = width - 1;
        for (int y = 0; y < height; y++)
        {
            smoothBand(values, lastColOffset + y * width, -1, width, smoothness);
        }
    }
//鎸夌収骞虫粦绯绘暟瀵逛竴鍒楁垨鑰呬竴琛岃繘琛屽墠鍚庢瘡鏍肩殑骞虫粦澶勭悊锛堝墠涓�涓牸瀛愮殑閮ㄥ垎鍔犱笂鑷繁鐨勯儴鍒嗭級
    protected void smoothBand(double[] values, int start, int stride, int count, double smoothness)
    {
        double prevValue = values[start];
        int j = start + stride;
        for (int i = 0; i < count - 1; i++)
        {
        	if(values[j]!=0&&prevValue!=0){
        		values[j] = smoothness * prevValue + (1 - smoothness) * values[j];
        	}
            prevValue = values[j];
            j += stride;
        }
    }
    
    public void scaleValues(double[] values, int count, double minValue, double maxValue)
    {
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < count; i++)
        {
            if (min > values[i])
                min = values[i];
            if (max < values[i])
                max = values[i];
        }//骞虫粦鍚庢暟鎹殑鏈�澶ф渶灏忓�兼煡鎵俱��

        for (int i = 0; i < count; i++)
        {
            values[i] = (values[i] - min) / (max - min);
            values[i] = minValue + values[i] * (maxValue - minValue);
        }
    }
    
    protected Renderable createLegendRenderable(final AnalyticSurface surface, final double surfaceMinScreenSize,
            final AnalyticSurfaceLegend legend)
        {
            return new Renderable()
            {
                public void render(DrawContext dc)
                {
                    Extent extent = surface.getExtent(dc);
                    if (!extent.intersects(dc.getView().getFrustumInModelCoordinates()))
                        return;

                    if (PrivateMath.computeSizeInWindowCoordinates(dc, extent) < surfaceMinScreenSize)
                        return;

                    legend.render(dc);
                }
            };
        }
	public static void main(String[] args) {
		wavedata wd=new wavedata();
		wd.exampleValues(0, 4, 40, 40, new RenderableLayer());
	}
}
