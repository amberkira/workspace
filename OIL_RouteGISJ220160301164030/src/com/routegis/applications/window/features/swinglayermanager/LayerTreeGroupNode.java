

package com.routegis.applications.window.features.swinglayermanager;

import com.routegis.applications.window.core.WMSLayerInfo;


public class LayerTreeGroupNode extends LayerTreeNode
{
    public LayerTreeGroupNode()
    {
    }

    public LayerTreeGroupNode(String title)
    {
        super(title);
    }

    public LayerTreeGroupNode(WMSLayerInfo layerInfo)
    {
        super(layerInfo);
    }

    public LayerTreeGroupNode(LayerTreeGroupNode layerNode)
    {
        super(layerNode);
    }
}
