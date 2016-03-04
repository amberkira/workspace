package com.routegis.typerender;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.util.ArrayList;
import javax.swing.Timer;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.geom.Sector;
import core.routegis.engine.layers.AnalyticSurface;
import core.routegis.engine.layers.AnalyticSurfaceAttributes;
import core.routegis.engine.layers.AnalyticSurfaceLegend;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.util.BufferFactory;
import core.routegis.engine.util.BufferWrapper;
import core.routegis.engine.util.WWMath;

public class PresentationLayer {
	public  void addAltitudeLayer(ArrayList<Observation> oblist,int width,int height,RenderableLayer outLayer){
		   SectorDivision sd=new SectorDivision();
	       Sector s=sd.SectorConstructor(oblist, width, height);
	       sd.LocateOfOb(oblist);
	       double[] values1=sd.SpreadValue(oblist);
	       double[] values2=sd.SpreadValue(oblist);
	       AnalyticSurface surface = new AnalyticSurface();
	       surface.setSector(s);
	       surface.setAltitude(0);
	       surface.setDimensions(width, height);
	       surface.setClientLayer(outLayer);
	       outLayer.addRenderable(surface);
	       
	       AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
	       attr.setShadowOpacity(0.5);
	       surface.setSurfaceAttributes(attr); 
	       BufferFactory f=new BufferFactory.DoubleBufferFactory();
	       BufferWrapper firstBuffer = f.newBuffer(width*height);
	       firstBuffer.putDouble(0, values1, 0, width*height);;
	       BufferWrapper secondBuffer = f.newBuffer(width*height);
	       secondBuffer.putDouble(0, values2, 0, width*height);;
	       mixValuesOverTime(2000L, firstBuffer, secondBuffer, 0, 3, 0, 0.6666666, surface);		
	}
	
	protected void mixValuesOverTime(
	        final long timeToMix,
	        final BufferWrapper firstBuffer, final BufferWrapper secondBuffer,
	        final double minValue, final double maxValue, final double minHue, final double maxHue,
	        final AnalyticSurface surface)
	    {
	        Timer timer = new Timer(20, new ActionListener()
	        {
	            protected long startTime = -1;

	            public void actionPerformed(ActionEvent e)
	            {
	                if (this.startTime < 0)
	                    this.startTime = System.currentTimeMillis();

	                double t = (double) (e.getWhen() - this.startTime) / (double) timeToMix;
	                int ti = (int) Math.floor(t);

	                double a = t - ti;
	                if ((ti % 2) == 0)
	                    a = 1d - a;

	                surface.setValues(createMixedColorGradientGridValues(
	                    a, firstBuffer, secondBuffer, minValue, maxValue, minHue, maxHue));

	                if (surface.getClientLayer() != null)
	                    surface.getClientLayer().firePropertyChange(AVKey.LAYER, null, surface.getClientLayer());
	            }
	        });
	        timer.start();
	    }

	    public Iterable<? extends AnalyticSurface.GridPointAttributes> createMixedColorGradientGridValues(double a,
	        BufferWrapper firstBuffer, BufferWrapper secondBuffer, double minValue, double maxValue,
	        double minHue, double maxHue)
	    {
	        ArrayList<AnalyticSurface.GridPointAttributes> attributesList
	            = new ArrayList<AnalyticSurface.GridPointAttributes>();

	        long length = Math.min(firstBuffer.length(), secondBuffer.length());
	        for (int i = 0; i < length; i++)
	        {
	            double value = WWMath.mixSmooth(a, firstBuffer.getDouble(i), secondBuffer.getDouble(i));
	            attributesList.add(
	                AnalyticSurface.createColorGradientAttributes(value, minValue, maxValue, minHue, maxHue));
	        }

	        return attributesList;
	    }

}
