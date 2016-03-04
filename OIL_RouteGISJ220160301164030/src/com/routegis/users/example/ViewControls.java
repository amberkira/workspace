
package com.routegis.users.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.layers.*;

import java.awt.*;
import java.awt.event.*;


public class ViewControls extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected ViewControlsLayer viewControlsLayer;

        public AppFrame()
        {
            super(true, true, false);

            // Find ViewControls layer and keep reference to it
            for (Layer layer : this.getMainWin().getModel().getLayers())
            {
                if (layer instanceof ViewControlsLayer)
                {
                    viewControlsLayer = (ViewControlsLayer) layer;
                    getLayerPanel().update(getMainWin());
                }
            }

            // Add view controls selection panel
            this.getLayerPanel().add(makeControlPanel(), BorderLayout.SOUTH);
        }

        private JPanel makeControlPanel()
        {
            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
            controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("View Controls")));
            controlPanel.setToolTipText("Select active view controls");

            // Radio buttons - layout
            JPanel layoutPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            layoutPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            ButtonGroup group = new ButtonGroup();
            JRadioButton button = new JRadioButton("Horizontal", true);
            group.add(button);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setLayout(AVKey.HORIZONTAL);
                    getMainWin().redraw();
                }
            });
            layoutPanel.add(button);
            button = new JRadioButton("Vertical", false);
            group.add(button);
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setLayout(AVKey.VERTICAL);
                    getMainWin().redraw();
                }
            });
            layoutPanel.add(button);

            // Scale slider
            JPanel scalePanel = new JPanel(new GridLayout(0, 1, 0, 0));
            scalePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            scalePanel.add(new JLabel("Scale:"));
            JSlider scaleSlider = new JSlider(1, 20, 10);
            scaleSlider.addChangeListener(new ChangeListener()
            {
                public void stateChanged(ChangeEvent event)
                {
                    viewControlsLayer.setScale(((JSlider) event.getSource()).getValue() / 10d);
                    getMainWin().redraw();
                }
            });
            scalePanel.add(scaleSlider);

            // Check boxes
            JPanel checkPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            checkPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            JCheckBox check = new JCheckBox("Pan");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowPanControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getMainWin().redraw();
                }
            });
            checkPanel.add(check);

            check = new JCheckBox("Look");
            check.setSelected(false);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowLookControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getMainWin().redraw();
                }
            });
            checkPanel.add(check);

            check = new JCheckBox("Zoom");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowZoomControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getMainWin().redraw();
                }
            });
            checkPanel.add(check);

            check = new JCheckBox("Heading");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowHeadingControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getMainWin().redraw();
                }
            });
            checkPanel.add(check);

            check = new JCheckBox("Pitch");
            check.setSelected(true);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowPitchControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getMainWin().redraw();
                }
            });
            checkPanel.add(check);

            check = new JCheckBox("Field of view");
            check.setSelected(false);
            check.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    viewControlsLayer.setShowFovControls(((JCheckBox) actionEvent.getSource()).isSelected());
                    getMainWin().redraw();
                }
            });
            checkPanel.add(check);

            controlPanel.add(layoutPanel);
            controlPanel.add(scalePanel);
            controlPanel.add(checkPanel);
            return controlPanel;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("RouteGIS SDK View Controls", AppFrame.class);
    }
}
