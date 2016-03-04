

package com.routegis.applications.window.features.swinglayermanager;

import com.routegis.applications.window.core.WMSLayerInfo;

import core.routegis.engine.layers.Layer;


public interface LayerNode
{
    Object getID();

    String getTitle();

    void setTitle(String title);

    Layer getLayer();

    void setLayer(Layer layer);

    boolean isSelected();

    void setSelected(boolean selected);

    WMSLayerInfo getWmsLayerInfo();

    String getToolTipText();

    void setToolTipText(String toolTipText);

    void setEnableSelectionBox(boolean tf);

    boolean isEnableSelectionBox();

    void setAllowsChildren(boolean tf);
}
