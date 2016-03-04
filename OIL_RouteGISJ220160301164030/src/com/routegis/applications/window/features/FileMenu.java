

package com.routegis.applications.window.features;

import com.routegis.applications.window.core.*;


public class FileMenu extends AbstractMenu
{
    public FileMenu(Registry registry)
    {
        super("File", Constants.FILE_MENU, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.addToMenuBar();
    }
}
