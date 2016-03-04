package com.routegis.applications.window.core;

import javax.swing.*;

import com.routegis.applications.window.features.Feature;


public interface ToolBar
{
    JToolBar getJToolBar();

    void addFeature(Feature feature);
}
