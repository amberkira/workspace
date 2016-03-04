package com.routegis.applications.window.core;

import javax.swing.*;

import com.routegis.applications.window.features.AbstractFeature;


public class MenuBarImpl extends AbstractFeature implements MenuBar
{
    // These are the menus in the menu bar. To add new menus, add them to this list in the order they should appear.
    private static final String[] menuIDs = new String[]
        {
//            Constants.SDF_MENU,
        };

    private JMenuBar menuBar;

    public MenuBarImpl(Registry registry)
    {
        super("Menu Bar", Constants.MENU_BAR, registry);
    }

    public void initialize(Controller controller)
    {
        this.menuBar = new JMenuBar();

        for (String menuID : menuIDs)
        {
            Menu menu = (Menu) controller.getRegisteredObject(menuID);
            if (menu != null)
            {
                getJMenuBar().add(menu.getJMenu());
            }
        }
    }

    public JMenuBar getJMenuBar()
    {
        return this.menuBar;
    }

    public void addMenu(Menu menu)
    {
        if (menu != null)
            getJMenuBar().add(menu.getJMenu());
    }
}
