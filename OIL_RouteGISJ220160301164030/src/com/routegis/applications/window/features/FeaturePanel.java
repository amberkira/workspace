

package com.routegis.applications.window.features;

import javax.swing.*;

import com.routegis.applications.window.core.GISPanel;


public interface FeaturePanel extends GISPanel, Feature
{
    JComponent[] getDialogControls();
}
