package com.routegis.applications.window.core.layermanager;

import com.routegis.applications.window.core.Constants;
import com.routegis.applications.window.features.Feature;

import core.routegis.engine.layers.LayerList;


public interface ActiveLayersManager extends Feature
{
    
    boolean isIncludeInternalLayers();

    
    void setIncludeInternalLayers(boolean includeInternalLayers);

    
    void updateLayerList(LayerList layerList);
}
