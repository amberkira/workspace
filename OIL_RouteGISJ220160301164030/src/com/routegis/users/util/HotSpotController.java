

package com.routegis.users.util;

import java.awt.*;
import java.awt.event.*;

import core.routegis.engine.WorldWindow;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.event.*;
import core.routegis.engine.pick.*;
import core.routegis.engine.util.*;


public class HotSpotController implements SelectListener, MouseMotionListener
{
    protected WorldWindow hwnd;
    protected HotSpot activeHotSpot;
    protected boolean dragging = false;
    
    protected boolean customCursor;

    
    public HotSpotController(WorldWindow wwd)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.hwnd = wwd;
        this.hwnd.addSelectListener(this);
        this.hwnd.getInputHandler().addMouseMotionListener(this);
    }

    
    public void selected(SelectEvent event)
    {
        if (event == null)
            return;

        try
        {
            this.doSelected(event);
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up.
            Logging.logger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    
    protected void doSelected(SelectEvent event)
    {
        HotSpot activeHotSpot = this.getActiveHotSpot();

        if (event.isDragEnd())
        {
            // Forward the drag end event to the active HotSpot (if any), and mark the controller as not dragging.
            // We forward the drag end event here because the active HotSpot potentially changes on a drag end, and
            // the currently active HotSpot might need to know the drag ended.
            if (activeHotSpot != null)
                activeHotSpot.selected(event);

            this.setDragging(false);

            PickedObjectList list = this.hwnd.getObjectsAtCurrentPosition();
            PickedObject po = list != null ? list.getTopPickedObject() : null;

            this.updateActiveHotSpot(po);
        }
        else if (!this.isDragging() && (event.isRollover() || event.isLeftPress()))
        {
            // Update the active HotSpot and the currently displayed cursor on drag end events, and on rollover and left
            // press events when we're not dragging. This ensures that the active HotSpot remains active while it's
            // being dragged, regardless of what's under the cursor. It's necessary to do this on left press to handle
            // cases in which the mouse starts dragging without a hover event, which can happen if the user starts
            // dragging while the RouteGIS SDK window is in the background.
            PickedObject po = event.getTopPickedObject();
            this.updateActiveHotSpot(po);
        }

        // Forward the event to the active HotSpot
        if (activeHotSpot != null)
        {
            if (event.isDrag())
            {
                boolean wasConsumed = event.isConsumed();

                // Forward the drag event to the active HotSpot. If the HotSpot consumes the event, track that the
                // HotSpot is dragging so that we can continue to deliver events to the HotSpot for the duration of the drag.
                activeHotSpot.selected(event);
                //noinspection ConstantConditions
                this.setDragging(event.isConsumed() && !wasConsumed);
            }
            else if (!event.isDragEnd())
            {
                // Forward all other the select event (except drag end) to the active HotSpot. We ignore drag end events
                // because we've already forwarded them to the previously active HotSpot in the logic above.
                activeHotSpot.selected(event);
            }
        }
    }

    
    public void mouseMoved(MouseEvent e)
    {
        // Give the active HotSpot a chance to set a custom cursor based on the new mouse position.
        HotSpot hotSpot = this.getActiveHotSpot();
        if (hotSpot != null)
        {
            Cursor cursor = hotSpot.getCursor();

            if (cursor != null)
            {
                ((Component) this.hwnd).setCursor(cursor);
                this.customCursor = true;
            }
        }
    }

    
    protected boolean isDragging()
    {
        return this.dragging;
    }

    
    protected void setDragging(boolean dragging)
    {
        this.dragging = dragging;
    }

    
    protected HotSpot getActiveHotSpot()
    {
        return this.activeHotSpot;
    }

    
    protected void setActiveHotSpot(HotSpot hotSpot)
    {
        // Update the World Window's cursor to the cursor associated with the active HotSpot. We
        // specify null if there's no active HotSpot, which tells the World Window to use the default
        // cursor, or inherit its cursor from the parent Component.
        if (this.hwnd instanceof Component)
        {
            // If the active HotSpot is changing, and a custom cursor was set by the previous HotSpot, reset the cursor.
            if (this.activeHotSpot != hotSpot && this.customCursor)
            {
                ((Component) this.hwnd).setCursor(null);
                this.customCursor = false;
            }

            // Give the new HotSpot a chance to set a custom cursor.
            if (hotSpot != null)
            {
                Cursor cursor = hotSpot.getCursor();

                if (cursor != null)
                {
                    ((Component) this.hwnd).setCursor(cursor);
                    this.customCursor = true;
                }
            }
        }

        if (this.activeHotSpot == hotSpot) // The specified HotSpot is already active.
            return;

        if (this.activeHotSpot != null)
        {
            this.hwnd.getInputHandler().removeKeyListener(this.activeHotSpot);
            this.hwnd.getInputHandler().removeMouseListener(this.activeHotSpot);
            this.hwnd.getInputHandler().removeMouseMotionListener(this.activeHotSpot);
            this.hwnd.getInputHandler().removeMouseWheelListener(this.activeHotSpot);
            this.activeHotSpot.setActive(false);
        }

        this.activeHotSpot = hotSpot;

        if (this.activeHotSpot != null)
        {
            this.activeHotSpot.setActive(true);
            this.hwnd.getInputHandler().addKeyListener(this.activeHotSpot);
            this.hwnd.getInputHandler().addMouseListener(this.activeHotSpot);
            this.hwnd.getInputHandler().addMouseMotionListener(this.activeHotSpot);
            this.hwnd.getInputHandler().addMouseWheelListener(this.activeHotSpot);
        }
    }

    
    protected void updateActiveHotSpot(PickedObject po)
    {
        if (po != null && po.getValue(AVKey.HOT_SPOT) instanceof HotSpot)
        {
            this.setActiveHotSpot((HotSpot) po.getValue(AVKey.HOT_SPOT));
        }
        else if (po != null && po.getObject() instanceof HotSpot)
        {
            this.setActiveHotSpot((HotSpot) po.getObject());
        }
        else
        {
            this.setActiveHotSpot(null);
        }
    }

    
    public void mouseDragged(MouseEvent e)
    {
        // No action
    }
}
