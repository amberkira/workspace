

package com.routegis.applications.window.features;

import javax.swing.*;

import com.routegis.applications.window.core.*;

import java.awt.event.*;


public class OpenFile extends AbstractOpenResourceFeature
{
    public OpenFile(Registry registry)
    {
        super("Open File...", Constants.FEATURE_OPEN_FILE, null, registry);
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
        JFileChooser fc = this.getController().getFileChooser();
        fc.setDialogTitle("Open File");
        fc.setMultiSelectionEnabled(false);

        try
        {
            int status = fc.showOpenDialog(this.getController().getFrame());
            if (status == JFileChooser.APPROVE_OPTION)
            {
                this.runOpenThread(fc.getSelectedFile());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        fc.setDialogTitle("");
        fc.setMultiSelectionEnabled(true);

        super.doActionPerformed(actionEvent);
    }
}
