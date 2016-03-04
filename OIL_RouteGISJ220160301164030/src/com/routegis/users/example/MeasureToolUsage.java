
package com.routegis.users.example;

import javax.swing.*;
import javax.swing.event.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.MeasureToolPanel;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.geom.LatLon;
import core.routegis.engine.layers.TerrainProfileLayer;
import core.routegis.engine.util.measure.MeasureTool;
import core.routegis.engine.util.measure.MeasureToolController;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;


public class MeasureToolUsage extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private int lastTabIndex = -1;
        private final JTabbedPane tabbedPane = new JTabbedPane();
        private TerrainProfileLayer profile = new TerrainProfileLayer();
        private PropertyChangeListener measureToolListener = new MeasureToolListener();

        public AppFrame()
        {
            super(true, false, true); // no layer panel

            // Add terrain profile layer
            profile.setEventSource(getMainWin());
            profile.setFollow(TerrainProfileLayer.FOLLOW_PATH);
            profile.setShowProfileLine(false);
            insertBeforePlacenames(getMainWin(), profile);

            // Add + tab
            tabbedPane.add(new JPanel());
            tabbedPane.setTitleAt(0, "+");
            tabbedPane.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent changeEvent)
                {
                    if (tabbedPane.getSelectedIndex() == 0)
                    {
                        // Add new measure tool in a tab when '+' selected
                        MeasureTool measureTool = new MeasureTool(getMainWin());
                        measureTool.setController(new MeasureToolController());
                        tabbedPane.add(new MeasureToolPanel(getMainWin(), measureTool));
                        tabbedPane.setTitleAt(tabbedPane.getTabCount() - 1, "" + (tabbedPane.getTabCount() - 1));
                        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
                        switchMeasureTool();
                    }
                    else
                    {
                        switchMeasureTool();
                    }
                }
            });

            // Add measure tool control panel to tabbed pane
            MeasureTool measureTool = new MeasureTool(this.getMainWin());
            measureTool.setController(new MeasureToolController());
            tabbedPane.add(new MeasureToolPanel(this.getMainWin(), measureTool));
            tabbedPane.setTitleAt(1, "1");
            tabbedPane.setSelectedIndex(1);
            switchMeasureTool();

            this.getContentPane().add(tabbedPane, BorderLayout.WEST);
        }

        public class MeasureToolListener implements PropertyChangeListener
        {
            public void propertyChange(PropertyChangeEvent event)
            {
                // Measure shape position list changed - update terrain profile
                if (event.getPropertyName().equals(MeasureTool.EVENT_POSITION_ADD)
                        || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REMOVE)
                        || event.getPropertyName().equals(MeasureTool.EVENT_POSITION_REPLACE))
                {
                    updateProfile(((MeasureTool)event.getSource()));
                }
            }
        }

        private void switchMeasureTool()
        {
            // Disarm last measure tool when changing tab and switching tool
            if (lastTabIndex != -1)
            {
                MeasureTool mt = ((MeasureToolPanel)tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
                mt.setArmed(false);
                mt.removePropertyChangeListener(measureToolListener);
            }
            // Update terrain profile from current measure tool
            lastTabIndex = tabbedPane.getSelectedIndex();
            MeasureTool mt = ((MeasureToolPanel)tabbedPane.getComponentAt(lastTabIndex)).getMeasureTool();
            mt.addPropertyChangeListener(measureToolListener);
            updateProfile(mt);
        }

        private void updateProfile(MeasureTool mt)
        {
            ArrayList<? extends LatLon> positions = mt.getPositions();
            if (positions != null && positions.size() > 1)
            {
                profile.setPathPositions(positions);
                profile.setEnabled(true);
            }
            else
                profile.setEnabled(false);
            
            getMainWin().redraw();
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("测量工具用例", MeasureToolUsage.AppFrame.class);
    }

}
