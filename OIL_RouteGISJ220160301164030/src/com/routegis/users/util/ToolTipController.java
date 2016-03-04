

package com.routegis.users.util;

import com.routegis.users.ApplicationTemplate;

import core.routegis.engine.*;
import core.routegis.engine.avlist.*;
import core.routegis.engine.event.*;
import core.routegis.engine.layers.*;
import core.routegis.engine.util.*;


public class ToolTipController implements SelectListener, Disposable
{
    protected WorldWindow wwd;
    protected String hoverKey = AVKey.HOVER_TEXT;
    protected String rolloverKey = AVKey.ROLLOVER_TEXT;
    protected Object lastRolloverObject;
    protected Object lastHoverObject;
    protected AnnotationLayer layer;
    protected ToolTipAnnotation annotation;

    
    public ToolTipController(WorldWindow wwd, String rolloverKey, String hoverKey)
    {
        this.wwd = wwd;
        this.hoverKey = hoverKey;
        this.rolloverKey = rolloverKey;

        this.wwd.addSelectListener(this);
    }

    
    public ToolTipController(WorldWindow wwd)
    {
        this.wwd = wwd;
        this.rolloverKey = AVKey.DISPLAY_NAME;

        this.wwd.addSelectListener(this);
    }

    public void dispose()
    {
        this.wwd.removeSelectListener(this);
    }

    protected String getHoverText(SelectEvent event)
    {
        return event.getTopObject() != null && event.getTopObject() instanceof AVList ?
            ((AVList) event.getTopObject()).getStringValue(this.hoverKey) : null;
    }

    protected String getRolloverText(SelectEvent event)
    {
        return event.getTopObject() != null && event.getTopObject() instanceof AVList ?
            ((AVList) event.getTopObject()).getStringValue(this.rolloverKey) : null;
    }

    public void selected(SelectEvent event)
    {
        try
        {
            if (event.isRollover() && this.rolloverKey != null)
                this.handleRollover(event);
            else if (event.isHover() && this.hoverKey != null)
                this.handleHover(event);
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Logging.logger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    protected void handleRollover(SelectEvent event)
    {
        if (this.lastRolloverObject != null)
        {
            if (this.lastRolloverObject == event.getTopObject() && !PrivateUtil.isEmpty(getRolloverText(event)))
                return;

            this.hideToolTip();
            this.lastRolloverObject = null;
            this.wwd.redraw();
        }

        if (getRolloverText(event) != null)
        {
            this.lastRolloverObject = event.getTopObject();
            this.showToolTip(event, getRolloverText(event).replace("\\n", "\n"));
            this.wwd.redraw();
        }
    }

    protected void handleHover(SelectEvent event)
    {
        if (this.lastHoverObject != null)
        {
            if (this.lastHoverObject == event.getTopObject())
                return;

            this.hideToolTip();
            this.lastHoverObject = null;
            this.wwd.redraw();
        }

        if (getHoverText(event) != null)
        {
            this.lastHoverObject = event.getTopObject();
            this.showToolTip(event, getHoverText(event).replace("\\n", "\n"));
            this.wwd.redraw();
        }
    }

    protected void showToolTip(SelectEvent event, String text)
    {
        if (annotation != null)
        {
            annotation.setText(text);
            annotation.setScreenPoint(event.getPickPoint());
        }
        else
        {
            annotation = new ToolTipAnnotation(text);
        }

        if (layer == null)
        {
            layer = new AnnotationLayer();
            layer.setPickEnabled(false);
        }

        layer.removeAllAnnotations();
        layer.addAnnotation(annotation);
        this.addLayer(layer);
    }

    protected void hideToolTip()
    {
        if (this.layer != null)
        {
            this.layer.removeAllAnnotations();
            this.removeLayer(this.layer);
            this.layer.dispose();
            this.layer = null;
        }

        if (this.annotation != null)
        {
            this.annotation.dispose();
            this.annotation = null;
        }
    }

    protected void addLayer(Layer layer)
    {
        if (!this.wwd.getModel().getLayers().contains(layer))
            ApplicationTemplate.insertBeforeCompass(this.wwd, layer);
    }

    protected void removeLayer(Layer layer)
    {
        this.wwd.getModel().getLayers().remove(layer);
    }
}
