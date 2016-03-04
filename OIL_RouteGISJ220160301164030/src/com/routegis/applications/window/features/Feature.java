

package com.routegis.applications.window.features;

import javax.swing.*;

import com.routegis.applications.window.core.Initializable;

import java.beans.PropertyChangeListener;


public interface Feature extends Initializable, Action, PropertyChangeListener
{
    String getFeatureID();

    boolean isOn();

    
    boolean isTwoState();

    void turnOn(boolean tf);

    String getName();
}
