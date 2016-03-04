
package com.routegis.users.example;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;
import com.routegis.users.util.*;

import core.routegis.engine.Configuration;
import core.routegis.engine.layers.Layer;
import core.routegis.engine.util.*;

import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import java.util.List;


public class Shapefiles extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected List<Layer> layers = new ArrayList<Layer>();
        protected BasicDragger dragger;
        protected JFileChooser fc = new JFileChooser(Configuration.getUserHomeDirectory());
        protected JCheckBox pickCheck, dragCheck;

        public AppFrame()
        {
            // Add our control panel.
            this.makeControlPanel();

            // Create a select listener for shape dragging but do not add it yet. Dragging can be enabled via the user
            // interface.
            this.dragger = new BasicDragger(this.getMainWin());

            // Setup file chooser
            this.fc = new JFileChooser();
            this.fc.addChoosableFileFilter(new FileNameExtensionFilter("ESRI Shapefile", "shp"));
        }

        protected void makeControlPanel()
        {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
            panel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(0, 9, 9, 9),
                new TitledBorder("Shapefiles")));

            // Open shapefile buttons.
            JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 5)); // nrows, ncols, hgap, vgap
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right
            panel.add(buttonPanel);
            // Open shapefile from File button.
            JButton openFileButton = new JButton("Open File...");
            openFileButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    showOpenFileDialog();
                }
            });
            buttonPanel.add(openFileButton);
            // Open shapefile from URL button.
            JButton openURLButton = new JButton("Open URL...");
            openURLButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    showOpenURLDialog();
                }
            });
            buttonPanel.add(openURLButton);

            // Picking and dragging checkboxes
            JPanel pickPanel = new JPanel(new GridLayout(1, 1, 10, 10)); // nrows, ncols, hgap, vgap
            pickPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // top, left, bottom, right
            this.pickCheck = new JCheckBox("Allow picking");
            this.pickCheck.setSelected(true);
            this.pickCheck.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    enablePicking(((JCheckBox) actionEvent.getSource()).isSelected());
                }
            });
            pickPanel.add(this.pickCheck);

            this.dragCheck = new JCheckBox("Allow dragging");
            this.dragCheck.setSelected(false);
            this.dragCheck.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    enableDragging(((JCheckBox) actionEvent.getSource()).isSelected());
                }
            });
            pickPanel.add(this.dragCheck);

            panel.add(pickPanel);

            this.getLayerPanel().add(panel, BorderLayout.SOUTH);
        }

        protected void enablePicking(boolean enabled)
        {
            for (Layer layer : this.layers)
            {
                layer.setPickEnabled(enabled);
            }

            // Disable the drag check box. Dragging is implicitly disabled since the objects cannot be picked.
            this.dragCheck.setEnabled(enabled);
        }

        protected void enableDragging(boolean enabled)
        {
            if (enabled)
                this.getMainWin().addSelectListener(this.dragger);
            else
                this.getMainWin().removeSelectListener(this.dragger);
        }

        public void showOpenFileDialog()
        {
            int retVal = AppFrame.this.fc.showOpenDialog(this);
            if (retVal != JFileChooser.APPROVE_OPTION)
                return;

            Thread t = new WorkerThread(this.fc.getSelectedFile(), this);
            t.start();
            ((Component) getMainWin()).setCursor(new Cursor(Cursor.WAIT_CURSOR));
        }

        public void showOpenURLDialog()
        {
            String retVal = JOptionPane.showInputDialog(this, "Enter Shapefile URL", "Open",
                JOptionPane.INFORMATION_MESSAGE);
            if (PrivateUtil.isEmpty(retVal)) // User cancelled the operation entered an empty URL.
                return;

            URL url = InOut.makeURL(retVal);
            if (url == null)
            {
                JOptionPane.showMessageDialog(this, retVal + " is not a valid URL.", "Open", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Thread t = new WorkerThread(url, this);
            t.start();
            ((Component) getMainWin()).setCursor(new Cursor(Cursor.WAIT_CURSOR));
        }
    }

    public static class WorkerThread extends Thread
    {
        protected Object source;
        protected AppFrame appFrame;

        public WorkerThread(Object source, AppFrame appFrame)
        {
            this.source = source;
            this.appFrame = appFrame;
        }

        public void run()
        {
            try
            {
                final List<Layer> layers = this.makeShapefileLayers();
                for (int i = 0; i < layers.size(); i++)
                {
                    String name = this.makeDisplayName(this.source);
                    layers.get(i).setName(i == 0 ? name : name + "-" + Integer.toString(i));
                    layers.get(i).setPickEnabled(this.appFrame.pickCheck.isSelected());
                }

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        for (Layer layer : layers)
                        {
                            insertBeforePlacenames(appFrame.getMainWin(), layer);
                            appFrame.layers.add(layer);
                        }

                        appFrame.layerPanel.update(appFrame.getMainWin());
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        ((Component) appFrame.getMainWin()).setCursor(Cursor.getDefaultCursor());
                    }
                });
            }
        }

        protected List<Layer> makeShapefileLayers()
        {
            if (OpenStreetMapShapefileLoader.isOSMPlacesSource(this.source))
            {
                Layer layer = OpenStreetMapShapefileLoader.makeLayerFromOSMPlacesSource(source);
                List<Layer> layers = new ArrayList<Layer>();
                layers.add(layer);
                return layers;
            }
            else
            {
                ShapefileLoader loader = new ShapefileLoader();
                return loader.createLayersFromSource(this.source);
            }
        }

        protected String makeDisplayName(Object source)
        {
            String name = InOut.getSourcePath(source);
            if (name != null)
                name = InOut.getFilename(name);
            if (name == null)
                name = "Shapefile";

            return name;
        }
    }

    public static void main(String[] args)
    {
        start("RouteGIS SDK Shapefiles", AppFrame.class);
    }
}
