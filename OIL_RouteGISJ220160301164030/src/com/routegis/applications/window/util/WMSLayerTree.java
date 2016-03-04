

package com.routegis.applications.window.util;

import java.util.List;

import com.routegis.applications.window.core.Controller;

import core.routegis.engine.avlist.*;
import core.routegis.engine.layers.TiledImageLayer;
import core.routegis.engine.ogc.wms.*;
import core.routegis.engine.util.PrivateUtil;
import core.routegis.engine.wms.WMSTiledImageLayer;


public class WMSLayerTree extends LayerTree
{
    public WMSLayerTree(Controller controller)
    {
        super(controller);
    }

    public void createLayers(Object infoItem, AVList commonLayerParams)
    {
        if (infoItem instanceof WMSCapabilities)
        {
            WMSCapabilities capsDoc = (WMSCapabilities) infoItem;

            String serviceTitle = capsDoc.getServiceInformation().getServiceTitle();
            if (!PrivateUtil.isEmpty(serviceTitle))
                this.setDisplayName(serviceTitle);

            List<WMSLayerCapabilities> layerCaps = capsDoc.getCapabilityInformation().getLayerCapabilities();
            if (layerCaps == null)
                return; // TODO: issue warning

            for (WMSLayerCapabilities caps : layerCaps)
            {
                LayerTree subTree = this.createSubTree(capsDoc, caps, commonLayerParams);
                if (subTree != null)
                    this.children.add(subTree);
            }
        }
    }

    public LayerTree createSubTree(WMSCapabilities capsDoc, WMSLayerCapabilities layerCaps, AVList commonLayerParams)
    {
        WMSLayerTree tree = new WMSLayerTree(this.controller);

        // Determine the tree's display name.
        if (!PrivateUtil.isEmpty(layerCaps.getTitle()))
            tree.setDisplayName(layerCaps.getTitle());
        else if (!PrivateUtil.isEmpty(layerCaps.getName()))
            tree.setDisplayName(layerCaps.getName());
        else
            tree.setDisplayName("No name");

        // Create an image layer if this is a named layer.
        if (layerCaps.getName() != null)
        {
            TiledImageLayer layer = tree.createImageLayer(capsDoc, layerCaps, commonLayerParams);
            if (layer == null)
                return null;

            tree.getLayers().add(layer);
        }

        // Create any sublayers.
        if (layerCaps.getLayers() != null)
        {
            for (WMSLayerCapabilities subLayerCaps : layerCaps.getLayers())
            {
                if (subLayerCaps.isLeaf())
                {
                    TiledImageLayer layer = tree.createImageLayer(capsDoc, subLayerCaps, commonLayerParams);
                    if (layer != null)
                        tree.getLayers().add(layer);
                }
                else
                {
                    LayerTree subTree = this.createSubTree(capsDoc, subLayerCaps, commonLayerParams);
                    if (subTree != null)
                        tree.children.add(subTree);
                }
            }
        }

        return tree;
    }

    protected TiledImageLayer createImageLayer(WMSCapabilities capsDoc, WMSLayerCapabilities layerCaps,
        AVList commonLayerParams)
    {
        AVList layerParams = new AVListImpl();
        if (commonLayerParams != null)
            layerParams.setValues(commonLayerParams);
        layerParams.setValue(AVKey.LAYER_NAMES, layerCaps.getName());

        return new WMSTiledImageLayer(capsDoc, layerParams);
    }
}
