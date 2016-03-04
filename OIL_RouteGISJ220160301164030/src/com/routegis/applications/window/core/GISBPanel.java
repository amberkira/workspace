package com.routegis.applications.window.core;

import javax.swing.*;

import core.routegis.engine.WorldWindow;
import core.routegis.engine.layers.Layer;

import java.awt.*;


public interface GISBPanel extends GISPanel, Initializable
{
    Dimension getSize();

    WorldWindow getWWd();

    void insertBeforeNamedLayer(Layer layer, String targetLayerName);

    void insertAfterNamedLayer(Layer layer, String targetLayerName);

    void addLayer(Layer layer);

    public JPanel getJPanel();

    void removeLayer(Layer layer);
}
