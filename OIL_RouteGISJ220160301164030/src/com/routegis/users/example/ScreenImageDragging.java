

package com.routegis.users.example;

import org.xml.sax.SAXException;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.ScreenImage;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import java.awt.event.*;
import java.io.*;


public class ScreenImageDragging extends ApplicationTemplate
{
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame() throws IOException, ParserConfigurationException, SAXException
        {
            super(true, true, false);

            // Create a screen image and containing layer for the image/icon
            final ScreenImage screenImage = new ScreenImage();
            screenImage.setImageSource(ImageIO.read(new File("src/images/Clock.png")));

            RenderableLayer layer = new RenderableLayer();
            layer.setName("Screen Image");
            layer.addRenderable(screenImage);

            this.getMainWin().getModel().getLayers().add(layer);
            this.getLayerPanel().update(this.getMainWin());

            // Tell the input handler to pass mouse events here
            this.getMainWin().getInputHandler().addMouseMotionListener(new MouseMotionAdapter()
            {
                public void mouseDragged(MouseEvent event)
                {
                    // Update the layer's image location
                    screenImage.setScreenLocation(event.getPoint());
                    event.consume(); // tell the input handler that we've handled the event
                }
            });
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Screen Image Dragging", AppFrame.class);
    }
}
