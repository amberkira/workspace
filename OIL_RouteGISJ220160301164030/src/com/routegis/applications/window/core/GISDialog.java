package com.routegis.applications.window.core;

import javax.swing.*;

import com.routegis.applications.window.features.Feature;


public interface GISDialog extends Feature
{
    JDialog getJDialog();

    void setVisible(boolean tf);
}
