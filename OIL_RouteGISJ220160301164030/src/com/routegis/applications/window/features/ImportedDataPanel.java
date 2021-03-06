

package com.routegis.applications.window.features;

import org.w3c.dom.Element;

import com.routegis.applications.window.core.Controller;
import com.routegis.applications.window.core.layermanager.*;
import com.routegis.applications.window.util.*;

import core.routegis.engine.*;
import core.routegis.engine.avlist.*;
import core.routegis.engine.geom.*;
import core.routegis.engine.globes.ElevationModel;
import core.routegis.engine.layers.Layer;
import core.routegis.engine.terrain.CompoundElevationModel;
import core.routegis.engine.util.*;

import javax.swing.*;
import javax.swing.Box;

import java.awt.*;
import java.awt.event.*;


public class ImportedDataPanel extends ShadedPanel
{
    protected Controller controller;
    protected JPanel dataConfigPanel;

    
    public ImportedDataPanel(String title, Controller controller)
    {
        super(new BorderLayout());

        if (controller == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.controller = controller;
        this.layoutComponents(title);
    }

    
    public void addImportedData(final Element domElement, final AVList params)
    {
        if (domElement == null)
        {
            String message = Logging.getMessage("nullValue.DocumentIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.addToWorldWindow(domElement, params);

        String description = this.getDescription(domElement);
        Sector sector = this.getSector(domElement);

        Box box = Box.createHorizontalBox();
        box.setOpaque(false);
        box.add(new JButton(new GoToSectorAction(sector)));
        box.add(Box.createHorizontalStrut(10));
        JLabel descLabel = new JLabel(description);
        descLabel.setOpaque(false);
        box.add(descLabel);

        this.dataConfigPanel.add(box);
        this.revalidate();
    }

    protected void layoutComponents(String title)
    {
        this.add(new PanelTitle(title, SwingConstants.CENTER), BorderLayout.NORTH);

        this.dataConfigPanel = new JPanel(new GridLayout(0, 1, 0, 4));
        this.dataConfigPanel.setOpaque(false);
        this.dataConfigPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // top, left, bottom, right

        // Put the grid in a container to prevent scroll panel from stretching its vertical spacing.
        JPanel dummyPanel = new JPanel(new BorderLayout());
        dummyPanel.setOpaque(false);
        dummyPanel.add(this.dataConfigPanel, BorderLayout.NORTH);

        // Add the dummy panel to a scroll pane.
        JScrollPane scrollPane = new JScrollPane(dummyPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // top, left, bottom, right

        // Add the scroll pane to a titled panel that will resize with the main window.
        JPanel bodyPanel = new JPanel(new GridLayout(0, 1, 0, 10)); // rows, cols, hgap, vgap
        bodyPanel.setOpaque(false);
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
        bodyPanel.add(scrollPane, BorderLayout.CENTER);

        this.add(bodyPanel, BorderLayout.CENTER);
    }

    //
    //
    //

    protected String getDescription(Element domElement)
    {
        String displayName = DataConfigurationUtils.getDataConfigDisplayName(domElement);
        String type = DataConfigurationUtils.getDataConfigType(domElement);

        StringBuilder sb = new StringBuilder(displayName);

        if (type.equalsIgnoreCase("Layer"))
        {
            sb.append(" (Layer)");
        }
        else if (type.equalsIgnoreCase("ElevationModel"))
        {
            sb.append(" (Elevations)");
        }

        return sb.toString();
    }

    protected Sector getSector(Element domElement)
    {
        return PrivateXML.getSector(domElement, "Sector", null);
    }

    protected void addToWorldWindow(Element domElement, AVList params)
    {
        String type = DataConfigurationUtils.getDataConfigType(domElement);
        if (type == null)
            return;

        if (type.equalsIgnoreCase("Layer"))
        {
            this.addLayerToWorldWindow(domElement, params);
        }
        else if (type.equalsIgnoreCase("ElevationModel"))
        {
            this.addElevationModelToWorldWindow(domElement, params);
        }
    }

    protected void addLayerToWorldWindow(Element domElement, AVList params)
    {
        try
        {
            Factory factory = (Factory) MainClass.createConfigurationComponent(AVKey.LAYER_FACTORY);
            Layer layer = (Layer) factory.createFromConfigSource(domElement, params);
            if (layer != null)
            {
                layer.setEnabled(true);
                this.addLayer(layer, new LayerPath("Imported"));
            }
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.CreationFromConfigurationFailed",
                DataConfigurationUtils.getDataConfigDisplayName(domElement));
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    protected void addLayer(final Layer layer, final LayerPath pathToParent)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                LayerPath path = new LayerPath(pathToParent, layer.getName());
                doAddLayer(layer, path);
            }
        });
    }

    protected void doAddLayer(final Layer layer, final LayerPath path)
    {
        LayerManager layerManager = controller.getLayerManager();
        layerManager.addLayer(layer, path.lastButOne());
        layerManager.selectLayer(layer, true);
        layerManager.expandPath(path.lastButOne());
    }

    protected void addElevationModelToWorldWindow(Element domElement, AVList params)
    {
        ElevationModel em = null;
        try
        {
            Factory factory = (Factory) MainClass.createConfigurationComponent(AVKey.ELEVATION_MODEL_FACTORY);
            em = (ElevationModel) factory.createFromConfigSource(domElement, params);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.CreationFromConfigurationFailed",
                DataConfigurationUtils.getDataConfigDisplayName(domElement));
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }

        if (em == null)
            return;

        ElevationModel defaultElevationModel = this.controller.getWWd().getModel().getGlobe().getElevationModel();
        if (defaultElevationModel instanceof CompoundElevationModel)
        {
            if (!((CompoundElevationModel) defaultElevationModel).containsElevationModel(em))
                ((CompoundElevationModel) defaultElevationModel).addElevationModel(em);
        }
        else
        {
            CompoundElevationModel cm = new CompoundElevationModel();
            cm.addElevationModel(defaultElevationModel);
            cm.addElevationModel(em);
            this.controller.getWWd().getModel().getGlobe().setElevationModel(cm);
        }
    }

    //
    //
    //

    protected class GoToSectorAction extends AbstractAction
    {
        protected Sector sector;

        public GoToSectorAction(Sector sector)
        {
            super("Go To");
            this.sector = sector;
            this.setEnabled(this.sector != null);
        }

        public void actionPerformed(ActionEvent e)
        {
            Extent extent = Sector.computeBoundingCylinder(controller.getWWd().getModel().getGlobe(),
                controller.getWWd().getSceneController().getVerticalExaggeration(), this.sector);

            Angle fov = controller.getWWd().getView().getFieldOfView();
            Position centerPos = new Position(this.sector.getCentroid(), 0d);
            double zoom = extent.getRadius() / fov.cosHalfAngle() / fov.tanHalfAngle();

            controller.getWWd().getView().goTo(centerPos, zoom);
        }
    }
}
