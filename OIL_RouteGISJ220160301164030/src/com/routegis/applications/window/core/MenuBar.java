package com.routegis.applications.window.core;

import javax.swing.*;

import com.routegis.applications.window.features.Feature;


public interface MenuBar extends Feature
{
    JMenuBar getJMenuBar();

    void addMenu(Menu menu);
}
