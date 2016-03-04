
package com.routegis.users.example;

import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.View;
import core.routegis.engine.WorldWindow;
import core.routegis.engine.formats.tiff.GeotiffImageReaderSpi;
import core.routegis.engine.geom.LatLon;
import core.routegis.engine.geom.Position;
import core.routegis.engine.layers.LayerList;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class SurfaceImages extends ApplicationTemplate
{
    static
    {
        IIORegistry reg = IIORegistry.getDefaultInstance();
        reg.registerServiceProvider(GeotiffImageReaderSpi.inst());
    }

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
    	LatLon ll = LatLon.fromDegrees(31.8313, -105.0653);
        public AppFrame()
        {
            super(true, true, false);
            final WorldWindow ww = this.getMainWin();
            try
            {
                final SurfaceImage si1 = new SurfaceImage(makeImage(null), new ArrayList<LatLon>(Arrays.asList(
                        LatLon.fromDegrees(31.8313, -105.0653),
                        LatLon.fromDegrees(31.8313, -100.0396),
                        LatLon.fromDegrees(37.8539, -100.04),
                        LatLon.fromDegrees(37.8539, -105.0653)
                )));

                Polyline boundary = new Polyline(si1.getCorners(), 0);
                boundary.setFollowTerrain(true);
                boundary.setClosed(true);
                boundary.setPathType(Polyline.RHUMB_LINE);
                boundary.setColor(new Color(0, 255, 0));

                RenderableLayer layer = new RenderableLayer();
                layer.setName("Surface Images");
                layer.setPickEnabled(false);
                layer.addRenderable(si1);

                layer.addRenderable(boundary);

                LayerList layers = this.getMainWin().getModel().getLayers();
                layers.add(layer);
                
                javax.swing.Timer timer = new javax.swing.Timer(50, new ActionListener()
                {
                    public void actionPerformed(ActionEvent actionEvent)
                    {
                        try {
                        	Position eyePosition = ww.getView().getEyePosition();
                        	
                        	double d = (eyePosition.getAltitude()/8000000.0) ;
                        	
							si1.setImageSource(makeImage(ww.getView()), new ArrayList<LatLon>(Arrays.asList(
                                    LatLon.fromDegrees(ll.getLatitude().getDegrees()-d, ll.getLongitude().getDegrees()-d),
                                    LatLon.fromDegrees(ll.getLatitude().getDegrees()-d, ll.getLongitude().getDegrees()+d*2),
                                    LatLon.fromDegrees(ll.getLatitude().getDegrees()+d*2, ll.getLongitude().getDegrees()+d*2),
                                    LatLon.fromDegrees(ll.getLatitude().getDegrees()+d*2, ll.getLongitude().getDegrees()-d))));
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        getMainWin().redraw();
                    }
                });
                timer.start();

                this.getLayerPanel().update(this.getMainWin());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        int r = 0;
        protected BufferedImage makeImage(View v) throws IOException
        {
            BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = image.createGraphics();
            
            BufferedImage icon = ImageIO.read(new File("c:h_arrow.png"));
            
        	g.drawImage(rotateImage(icon,r), 0, 0, 512, 512, null);

            g.dispose();
            /*
            if (r>350){
            	r = 0;
            }
            r+=1;*/

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
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Surface Images", SurfaceImages.AppFrame.class);
    }
}
