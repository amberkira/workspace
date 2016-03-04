

package com.routegis.users.example;

import javax.swing.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.geom.*;
import core.routegis.engine.globes.Globe;
import core.routegis.engine.layers.AnnotationLayer;
import core.routegis.engine.render.ScreenAnnotation;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


public class GetBestElevations extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        
        public double[] getBestElevations(List<LatLon> locations)
        {
            Globe globe = this.getMainWin().getModel().getGlobe();
            Sector sector = Sector.boundingSector(locations);
            double[] elevations = new double[locations.size()];

            // Iterate until the best resolution is achieved. Use the elevation model to determine the best elevation.
            double targetResolution = globe.getElevationModel().getBestResolution(sector);
            double actualResolution = Double.MAX_VALUE;
            while (actualResolution > targetResolution)
            {
                actualResolution = globe.getElevations(sector, locations, targetResolution, elevations);
                // Uncomment the two lines below if you want to watch the resolution converge
//                System.out.printf("Target resolution = %s, Actual resolution = %s\n",
//                    Double.toString(targetResolution), Double.toString(actualResolution));
                try
                {
                    Thread.sleep(200); // give the system a chance to retrieve data from the disk cache or the server
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

            return elevations;
        }

        public AppFrame()
        {
            super(true, true, false);

            final ScreenAnnotation annotation = new ScreenAnnotation("Shift-click to select a location",
                new Point(100, 50));
            AnnotationLayer layer = new AnnotationLayer();
            layer.addAnnotation(annotation);
            insertBeforeCompass(this.getMainWin(), layer);

            this.getMainWin().getInputHandler().addMouseListener(new MouseListener()
            {
                public void mouseClicked(MouseEvent mouseEvent)
                {
                    if ((mouseEvent.getModifiers() & ActionEvent.SHIFT_MASK) == 0)
                        return;
                    mouseEvent.consume();

                    final Position pos = getMainWin().getCurrentPosition();
                    if (pos == null)
                        return;

                    annotation.setText(String.format("Elevation = "));
                    getMainWin().redraw();

                    // Run the elevation query in a separate thread to avoid locking up the user interface
                    Thread t = new Thread(new Runnable()
                    {
                        public void run()
                        {
                            // We want elevation for only one location, so add a second location that's very near the
                            // desired one. This causes fewer requests to the disk or server, and causes faster
                            // convergence.
                            List<LatLon> locations = Arrays.asList(pos, pos.add(LatLon.fromDegrees(0.00001, 0.00001)));
                            final double[] elevations = getBestElevations(locations);
                            SwingUtilities.invokeLater(new Runnable()
                            {
                                public void run()
                                {
                                    annotation.setText(String.format("Elevation = %d m", (int) elevations[0]));
                                    getMainWin().redraw();
                                }
                            });
                        }
                    });
                    t.start();
                }

                public void mouseEntered(MouseEvent mouseEvent)
                {
                }

                public void mouseExited(MouseEvent mouseEvent)
                {
                }

                public void mousePressed(MouseEvent mouseEvent)
                {
                }

                public void mouseReleased(MouseEvent mouseEvent)
                {
                }
            });
        }
    }

    public static void main(String[] args)
    {
        // Adjust configuration values before instantiation
        ApplicationTemplate.start("RouteGIS SDK Get Best Elevations", AppFrame.class);
    }
}
