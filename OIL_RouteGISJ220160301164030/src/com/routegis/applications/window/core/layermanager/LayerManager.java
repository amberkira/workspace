package com.routegis.applications.window.core.layermanager;

import com.routegis.applications.window.features.Feature;
import com.routegis.applications.window.features.swinglayermanager.LayerNode;

import core.routegis.engine.layers.*;


public interface LayerManager extends Feature
{
    Layer findLayerByTitle(String layerTitle, String groupTitle);

    void addGroup(LayerPath pathToGroup);

    void addLayer(Layer layer, LayerPath pathToParent);

    void removeLayer(Layer layer);

    void redraw();

    void scrollToLayer(Layer layer);

    void selectLayer(Layer layer, boolean tf);

    
    LayerPath getDefaultGroupPath();

    void removeLayers(LayerList layerList);

    void removeLayer(LayerPath path);

    
    Layer getLayerFromPath(LayerPath path);

    
    void expandGroup(String groupName);

    
    void enableGroupSelection(LayerPath path, boolean tf);

    boolean containsPath(LayerPath pathToGroup);

    void expandPath(LayerPath path);

    LayerNode getNode(LayerPath path);
}
