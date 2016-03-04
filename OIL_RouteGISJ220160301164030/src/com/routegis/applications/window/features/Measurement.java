

package com.routegis.applications.window.features;

import java.awt.event.*;

import com.routegis.applications.window.core.*;
import com.routegis.applications.window.util.Util;


public class Measurement extends AbstractFeature
{
    private GISDialog dialog;

    public Measurement()
    {
        this(null);
    }

    public Measurement(Registry registry)
    {
        super("Measurement", Constants.FEATURE_MEASUREMENT,
            "images/globe-sextant-64x64.png", registry);
        setEnabled(true);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.addToToolBar();
    }

    @Override
    public boolean isTwoState()
    {
        return true;
    }

    public boolean isOn()
    {
        return this.dialog != null && this.dialog.getJDialog().isVisible();
    }

    @Override
    public void turnOn(boolean tf)
    {
        if (this.dialog != null)
            this.dialog.setVisible(tf);
    }

    @Override
    protected void doActionPerformed(ActionEvent actionEvent)
    {
        if (this.dialog == null)
            this.dialog = (GISDialog) this.controller.getRegisteredObject(Constants.FEATURE_MEASUREMENT_DIALOG);
        if (this.dialog == null)
        {
            Util.getLogger().severe("Measurement dialog not registered");
            return;
        }

        this.turnOn(!this.isOn());
    }
}
