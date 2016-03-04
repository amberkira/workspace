

package com.routegis.applications.window.features;

import com.routegis.applications.window.core.Registry;
import com.routegis.applications.window.core.layermanager.LayerPath;

import core.routegis.engine.layers.Layer;


public abstract class GraticuleLayer extends AbstractOnDemandLayerFeature
{
    protected abstract Layer doCreateLayer();

    public GraticuleLayer(String name, String featureID, String iconPath, String group, Registry registry)
    {
        super(name, featureID, iconPath, group, registry);
    }

    @Override
    protected Layer createLayer()
    {
        Layer layer = this.doCreateLayer();

        layer.setPickEnabled(false);

        return layer;
    }

    @Override
    protected void addLayer(LayerPath path)
    {
        controller.addInternalActiveLayer(this.layer);
    }

    @Override
    protected void removeLayer()
    {
        this.controller.getWWPanel().removeLayer(this.layer);
    }
}