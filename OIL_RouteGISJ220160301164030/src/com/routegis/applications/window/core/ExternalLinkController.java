package com.routegis.applications.window.core;

import com.routegis.applications.window.features.AbstractFeature;

import core.routegis.engine.Disposable;
import core.routegis.engine.avlist.*;
import core.routegis.engine.event.*;


public class ExternalLinkController extends AbstractFeature implements SelectListener, Disposable
{
    public ExternalLinkController(Registry registry)
    {
        super("External Link Controller", Constants.FEATURE_EXTERNAL_LINK_CONTROLLER, registry);
    }

    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.controller.getWWd().addSelectListener(this);
    }

    public void dispose()
    {
        this.controller.getWWd().removeSelectListener(this);
    }

    public void selected(SelectEvent event)
    {
        if (event.isLeftDoubleClick() && event.getTopObject() instanceof AVList)
        {
            String link = ((AVList) event.getTopObject()).getStringValue(AVKey.EXTERNAL_LINK);
            if (link == null)
                return;

            this.controller.openLink(link);
        }
    }
}
