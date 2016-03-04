
package com.routegis.users.example;

import org.xml.sax.SAXException;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;
import com.routegis.users.util.DirectedPath;

import core.routegis.engine.MainClass;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.event.*;
import core.routegis.engine.formats.gpx.GpxReader;
import core.routegis.engine.geom.Position;
import core.routegis.engine.layers.CompassLayer;
import core.routegis.engine.layers.Layer;
import core.routegis.engine.layers.LayerList;
import core.routegis.engine.layers.MarkerLayer;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.pick.PickedObject;
import core.routegis.engine.render.BasicShapeAttributes;
import core.routegis.engine.render.Material;
import core.routegis.engine.render.Path;
import core.routegis.engine.render.ShapeAttributes;
import core.routegis.engine.render.markers.*;
import core.routegis.engine.util.InOut;

import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.util.*;


public class Tracks extends ApplicationTemplate
{
    protected static final String TRACK_PATH = "data/tuolumne.gpx";

    protected static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            LayerList ret = this.buildTracksLayer();
            LayerList layers = this.getMainWin().getModel().getLayers();
            
            for (Layer l : ret)
            {
            	layers.add(l);
            }
 
            this.getLayerPanel().update(this.getMainWin());

            this.getMainWin().addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    if (event.getTopObject() != null)
                    {
                        if (event.getTopPickedObject().getParentLayer() instanceof MarkerLayer)
                        {
                            PickedObject po = event.getTopPickedObject();
                            //noinspection RedundantCast
                            System.out.printf("Track position %s, %s, size = %f\n",
                                po.getValue(AVKey.PICKED_OBJECT_ID).toString(),
                                po.getPosition(), (Double) po.getValue(AVKey.PICKED_OBJECT_SIZE));
                        }
                    }
                }
            });
        }

        protected LayerList buildTracksLayer()
        {
            try
            {
                GpxReader reader = new GpxReader();
                reader.readStream(InOut.openFileOrResourceStream(TRACK_PATH, this.getClass()));
                Iterator<Position> positions = reader.getTrackPositionIterator();

                ShapeAttributes ShapeAttrs = new BasicShapeAttributes();
                ShapeAttrs.setOutlineMaterial(Material.RED);
                ShapeAttrs.setOutlineWidth(2d);
                
                BasicMarkerAttributes MarkerAttrs =
                    new BasicMarkerAttributes(Material.WHITE, BasicMarkerShape.SPHERE, 1d);

                ArrayList<Position> pathPositions = new ArrayList<Position>();
                
                ArrayList<Marker> markerslist = new ArrayList<Marker>();
                Position pos = Position.ZERO;
                while (positions.hasNext())
                {
                	pos = positions.next();
                	pathPositions.add(pos);
                	if (markerslist.isEmpty()){
                		markerslist.add(new BasicMarker(pos, MarkerAttrs));
                	}
                }
                if (pathPositions.size()>1 && !pos.equals(Position.ZERO)){
                	markerslist.add(new BasicMarker(pos, MarkerAttrs));
                }

                Path path = new DirectedPath(pathPositions);

                RenderableLayer layer = new RenderableLayer();
                
                path.setAttributes(ShapeAttrs);
                path.setVisible(true);
                path.setAltitudeMode(MainClass.RELATIVE_TO_GROUND);
                path.setPathType(AVKey.GREAT_CIRCLE);
                layer.addRenderable(path);
                
                MarkerLayer markers = new MarkerLayer(markerslist);
                markers.setOverrideMarkerElevation(true);
                markers.setElevation(0);
                markers.setEnablePickSizeReturn(true);
                
                LayerList list = new LayerList();
                list.add(layer);
                list.add(markers);
                return list;
            }
            catch (ParserConfigurationException e)
            {
                e.printStackTrace();
            }
            catch (SAXException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK Tracks", AppFrame.class);
    }
}
