

package com.routegis.users.example;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;
import com.routegis.users.util.Util;

import core.routegis.engine.View;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.event.*;
import core.routegis.engine.geom.Position;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.*;
import core.routegis.engine.render.airspaces.*;
import core.routegis.engine.render.airspaces.Polygon;
import core.routegis.engine.util.InOut;


public class ExtrudedShapes extends ApplicationTemplate
{
    protected static final String DEMO_AIRSPACES_PATH
        = "com/rotuegis/users/data/AirspaceBuilder-DemoShapes.zip";
    protected static String DEFAULT_IMAGE_URL = "images/build123sm.jpg";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                // Create a layer for the shapes.
                RenderableLayer layer = new RenderableLayer();
                layer.setName("Extruded Shapes");
                layer.setPickEnabled(true);

                // Retrieve the geometry from the RouteGIS SDK demo site.
                List<Airspace> airspaces = new ArrayList<Airspace>();
                loadAirspacesFromPath(DEMO_AIRSPACES_PATH, airspaces);

                // Define attributes for the shapes.
                ShapeAttributes sideAttributes = new BasicShapeAttributes();
                sideAttributes.setInteriorMaterial(Material.LIGHT_GRAY);
                sideAttributes.setOutlineMaterial(Material.DARK_GRAY);

                ShapeAttributes capAttributes = new BasicShapeAttributes(sideAttributes);
                capAttributes.setInteriorMaterial(Material.GRAY);

                // Construct the extruded polygons from the demo data.
                int n = 0, m = 0;
                for (Airspace airspace : airspaces)
                {
                    if (airspace instanceof Polygon) // only polygons in the demo data are used
                    {
                        Polygon pgonAirspace = (Polygon) airspace;

                        // Collect the images to be applied to the shape's sides.
                        ArrayList<String> textures = new ArrayList<String>();
                        for (int i = 0; i < pgonAirspace.getLocations().size(); i++)
                        {
                            textures.add(DEFAULT_IMAGE_URL);
                        }

                        // Construct the extruded polygon. Use the default texture coordinates.
                        double height = 40; // building height
                        ExtrudedPolygon quad = new ExtrudedPolygon(pgonAirspace.getLocations(), height, textures);

                        // Apply the shape's attributes. Note the separate attributes for cap and sides.
                        quad.setSideAttributes(sideAttributes);
                        quad.setCapAttributes(capAttributes);

                        // Specify a cap for the extruded polygon, specifying its texture coordinates and image.
                        if (pgonAirspace.getLocations().size() == 4)
                        {
                            float[] texCoords = new float[] {0, 0, 1, 0, 1, 1, 0, 1};
                            quad.setCapImageSource("images/32x32-icon-nasa.png", texCoords, 4);
                        }

                        // Add the shape to the layer.
                        layer.addRenderable(quad);

                        ++n;
                        m += ((Polygon) airspace).getLocations().size();
                    }
                }

                System.out.printf("NUM SHAPES = %d, NUM SIDES = %d\n", n, m);

                // Add the layer to the model.
                insertBeforePlacenames(this.getMainWin(), layer);

                // Make sure the new layer is shown in the layer manager.
                this.getLayerPanel().update(this.getMainWin());

                // Adjust the view so that it looks at the buildings.
                View view = getMainWin().getView();
                view.setEyePosition(Position.fromDegrees(47.656, -122.306, 1e3));

                // This is how a select listener would notice that one of the shapes was picked.
                getMainWin().addSelectListener(new SelectListener()
                {
                    public void selected(SelectEvent event)
                    {
                        if (event.getTopObject() instanceof ExtrudedPolygon)
                            System.out.println("EXTRUDED POLYGON");
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    protected static void loadAirspacesFromPath(String path, Collection<Airspace> airspaces)
    {
        File file = Util.saveResourceToTempFile(path, ".zip");
        if (file == null)
            return;

        try
        {
            ZipFile zipFile = new ZipFile(file);

            ZipEntry entry = null;
            for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); entry = e.nextElement())
            {
                if (entry == null)
                    continue;

                String name = InOut.getFilename(entry.getName());

                if (!(name.startsWith("core.routegis.engine.render.airspaces") && name.endsWith(".xml")))
                    continue;

                String[] tokens = name.split("-");

                try
                {
                    Class c = Class.forName(tokens[0]);
                    Airspace airspace = (Airspace) c.newInstance();
                    BufferedReader input = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
                    String s = input.readLine();
                    airspace.restoreState(s);
                    airspaces.add(airspace);

                    if (tokens.length >= 2)
                    {
                        airspace.setValue(AVKey.DISPLAY_NAME, tokens[1]);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Extruded Polygons on Ground", AppFrame.class);
    }
}
