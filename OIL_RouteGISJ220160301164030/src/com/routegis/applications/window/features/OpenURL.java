

package com.routegis.applications.window.features;

import javax.swing.*;

import com.routegis.applications.window.core.*;

import core.routegis.engine.util.PrivateUtil;

import java.awt.event.*;


public class OpenURL extends AbstractOpenResourceFeature
{
    public OpenURL(Registry registry)
    {
        super("Open URL...", Constants.FEATURE_OPEN_URL, null, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        GISMenu fileMenu = (GISMenu) this.getController().getRegisteredObject(Constants.FILE_MENU);
        if (fileMenu != null)
            fileMenu.addMenu(this.getFeatureID());
    }

    @Override
    protected void doActionPerformed(ActionEvent actionEvent)
    {
        try
        {
            String status = JOptionPane.showInputDialog(getController().getFrame(), "URL");
            if (!PrivateUtil.isEmpty(status))
            {
                this.runOpenThread(status);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        super.doActionPerformed(actionEvent);
    }
}
