

package com.routegis.applications.window.features;

import com.routegis.applications.window.core.*;

import core.routegis.engine.layers.*;


public class LatLonGraticule extends GraticuleLayer
{
    public LatLonGraticule()
    {
        this(null);
    }

    public LatLonGraticule(Registry registry)
    {
        super("Lat/Lon Graticule", Constants.FEATURE_LATLON_GRATICULE, null, null, registry);
    }

    @Override
    protected Layer doCreateLayer()
    {
        return new LatLonGraticuleLayer();
    }
}
