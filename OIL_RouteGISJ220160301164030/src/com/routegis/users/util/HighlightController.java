

package com.routegis.users.util;

import com.routegis.applications.window.util.Util;

import core.routegis.engine.WorldWindow;
import core.routegis.engine.event.*;
import core.routegis.engine.render.Highlightable;


public class HighlightController implements SelectListener
{
    protected WorldWindow wwd;
    protected Object highlightEventType = SelectEvent.ROLLOVER;
    protected Highlightable lastHighlightObject;

    
    public HighlightController(WorldWindow wwd, Object highlightEventType)
    {
        this.wwd = wwd;
        this.highlightEventType = highlightEventType;

        this.wwd.addSelectListener(this);
    }

    public void dispose()
    {
        this.wwd.removeSelectListener(this);
    }

    public void selected(SelectEvent event)
    {
        try
        {
            if (this.highlightEventType != null && event.getEventAction().equals(this.highlightEventType))
                highlight(event.getTopObject());
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    protected void highlight(Object o)
    {
        if (this.lastHighlightObject == o)
            return; // same thing selected

        // Turn off highlight if on.
        if (this.lastHighlightObject != null)
        {
            this.lastHighlightObject.setHighlighted(false);
            this.lastHighlightObject = null;
        }

        // Turn on highlight if object selected.
        if (o instanceof Highlightable)
        {
            this.lastHighlightObject = (Highlightable) o;
            this.lastHighlightObject.setHighlighted(true);
        }
    }
}
