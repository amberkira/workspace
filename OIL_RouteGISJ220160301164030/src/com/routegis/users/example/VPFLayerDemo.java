
package com.routegis.users.example;

import javax.swing.*;
import javax.swing.filechooser.*;

import com.routegis.users.ApplicationTemplate;
import com.routegis.users.ApplicationTemplate.AppFrame;

import core.routegis.engine.Configuration;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.formats.vpf.*;
import core.routegis.engine.util.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;


public class VPFLayerDemo extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            this.makeControlPanel();
        }

        protected void addVPFLayer(File file)
        {
            VPFDatabase db = VPFUtils.readDatabase(file);
            VPFLayer layer = new VPFLayer(db);
            insertBeforePlacenames(this.getMainWin(), layer);
            this.getLayerPanel().update(this.getMainWin());
            this.openVPFCoveragePanel(db, layer);
        }

        protected void openVPFCoveragePanel(VPFDatabase db, VPFLayer layer)
        {
            VPFCoveragePanel panel = new VPFCoveragePanel(getMainWin(), db);
            panel.setLayer(layer);
            JFrame frame = new JFrame(db.getName());
            frame.setResizable(true);
            frame.setAlwaysOnTop(true);
            frame.add(panel);
            frame.pack();
            PrivateUtil.alignComponent(this, frame, AVKey.CENTER);
            frame.setVisible(true);
        }

        protected void showOpenDialog()
        {
            JFileChooser fc = new JFileChooser(Configuration.getUserHomeDirectory());
            fc.addChoosableFileFilter(new VPFFileFilter());

            int retVal = fc.showOpenDialog(this);
            if (retVal != JFileChooser.APPROVE_OPTION)
                return;

            File file = fc.getSelectedFile();
            this.addVPFLayer(file);
        }

        protected void makeControlPanel()
        {
            JButton button = new JButton("Open VPF Database");
            button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    showOpenDialog();
                }
            });

            Box box = Box.createHorizontalBox();
            box.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // top, left, bottom, right
            box.add(button);

            this.getLayerPanel().add(box, BorderLayout.SOUTH);
        }
    }

    public static class VPFFileFilter extends FileFilter
    {
        protected VPFDatabaseFilter filter;

        public VPFFileFilter()
        {
            this.filter = new VPFDatabaseFilter();
        }

        public boolean accept(File file)
        {
            if (file == null)
            {
                String message = Logging.getMessage("nullValue.FileIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            return file.isDirectory() || this.filter.accept(file);
        }

        public String getDescription()
        {
            return "VPF Databases (dht)";
        }
    }

    public static void main(String[] args)
    {
        start("RouteGIS SDK VPF Shapes", AppFrame.class);
    }
}
