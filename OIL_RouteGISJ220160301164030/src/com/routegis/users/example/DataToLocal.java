package com.routegis.users.example;

import java.util.ArrayList;

import core.routegis.engine.BasicFactory;
import core.routegis.engine.avlist.*;
import core.routegis.engine.event.*;
import core.routegis.engine.geom.Sector;
import core.routegis.engine.retrieve.*;
import core.routegis.engine.terrain.CompoundElevationModel;


public class DataToLocal
{
    public static void main(String[] args)
    {
        try
        {
            // Use three sectors to avoid capturing a lot of ocean area.
            ArrayList<Sector> sectors = new ArrayList<Sector>(3);
            // China sector
            sectors.add(Sector.fromDegrees(26, 33, 87, 130));
            // World sector
            //sectors.add(Sector.fromDegrees(-90, -180, 90, 180));
            
            BulkRetrievable layer;
            BulkRetrievalThread thread;
            AVListImpl params = new AVListImpl();

            for (Sector sector : sectors)
            {
                layer = (BulkRetrievable) BasicFactory.create(AVKey.LAYER_FACTORY, "config/Earth/BMNG256.xml");
                System.out.println(layer.getName());
                thread = layer.makeLocal(sector, 0, new BulkRetrievalListener()
                {
                    @Override
                    public void eventOccurred(BulkRetrievalEvent event)
                    {
                        System.out.println(event.getItem());
                    }
                });
                thread.join();

                params.setValue(AVKey.NUM_LEVELS, 9); // More than 9 levels is too large for TAIGA
                CompoundElevationModel cem = (CompoundElevationModel) BasicFactory.create(AVKey.ELEVATION_MODEL_FACTORY,
                    "config/Earth/CustomElevationModel.xml");
                layer = (BulkRetrievable) cem.getElevationModels().get(0);
                System.out.println(layer.getName());
                thread = layer.makeLocal(sector, 0, new BulkRetrievalListener()
                {
                    @Override
                    public void eventOccurred(BulkRetrievalEvent event)
                    {
                        System.out.println(event.getItem());
                    }
                });
                thread.join();
            }


        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
