package com.routegis.applications.window.core;

import javax.swing.*;


public interface Menu extends Initializable
{
    JMenu getJMenu();

    void addMenu(String featureID);
}
