
package com.routegis.users;

import javax.swing.*;

import core.routegis.engine.WorldWindow;
import core.routegis.engine.layers.LayerList;
import core.routegis.engine.layers.placename.PlaceNameLayer;
import core.routegis.engine.layers.placename.PlaceNameService;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;
import java.util.List;


public class PlaceNamesPanel extends JPanel implements ItemListener
{
    List<PlaceNameService> nameServices;
    PlaceNameLayer nameLayer;
    WorldWindow wwd;
    ArrayList<JCheckBox> cbList = new ArrayList<JCheckBox>();

    public PlaceNamesPanel(WorldWindow wwd)
    {
        super(new BorderLayout());
        this.wwd=wwd;
        LayerList layers = this.wwd.getModel().getLayers();
        for (Object layer : layers)
        {
            if (layer instanceof PlaceNameLayer)
            {
                nameLayer = (PlaceNameLayer) layer;
                break;
            }
        }

        if (nameLayer !=null)
        {
            nameServices = nameLayer.getPlaceNameServiceSet().getServices();
            this.makePanel();
        }
    }
   
    private void makePanel()
    {
        JPanel namesPanel = new JPanel(new GridLayout(0, 1, 0, 0));
        namesPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JPanel comboPanel = new JPanel(new GridLayout(0, 2, 0, 0));
        
        for (PlaceNameService s: nameServices)
        {
          JCheckBox cb=new JCheckBox(s.getDataset(),true);
          cb.addItemListener(this);
          comboPanel.add(cb);
          cbList.add(cb);
        }

        namesPanel.add(comboPanel);
        this.add(namesPanel, BorderLayout.CENTER);
    }

    public void itemStateChanged(ItemEvent e)
    {

        for (PlaceNameService s: nameServices)
        {
            if (s.getDataset().equalsIgnoreCase(((JCheckBox)e.getSource()).getText()))
            {
                s.setEnabled(!s.isEnabled());
                break;
            }
        }


        update();
    }

    private void update()
    {
        wwd.redraw();
    }
}
