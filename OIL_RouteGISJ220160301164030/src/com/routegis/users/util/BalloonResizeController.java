

package com.routegis.users.util;

import java.awt.*;
import java.awt.event.*;

import core.routegis.engine.WorldWindow;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.pick.*;
import core.routegis.engine.render.*;
import core.routegis.engine.util.*;


public class BalloonResizeController extends AbstractResizeHotSpot
{
    protected WorldWindow wwd;
    protected Rectangle bounds;
    protected Balloon balloon;

    protected static final Dimension DEFAULT_MIN_SIZE = new Dimension(50, 50);

    
    public BalloonResizeController(WorldWindow wwd, Balloon balloon)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (balloon == null)
        {
            String message = Logging.getMessage("nullValue.BalloonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.wwd = wwd;
        this.balloon = balloon;

        this.wwd.addSelectListener(this);
        this.wwd.getInputHandler().addMouseMotionListener(this);
    }

    
    public void detach()
    {
        this.wwd.removeSelectListener(this);
        this.wwd.getInputHandler().removeMouseMotionListener(this);
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        if (e == null || e.isConsumed())
            return;
        
        PickedObjectList pickedObjects = wwd.getObjectsAtCurrentPosition();
        if (pickedObjects != null)
        {
            Rectangle rect = this.getBounds(pickedObjects.getTopPickedObject());
            if (rect != null)
            {
                this.setBounds(rect);
            }
        }

        super.mouseMoved(e);
        this.updateCursor();
    }

    
    public boolean isResizing()
    {
        return this.isDragging();
    }

    
    protected Dimension getSize()
    {
        Rectangle bounds = this.getBounds();
        if (bounds != null)
            return bounds.getSize();
        else
            return null;
    }

    
    public Rectangle getBounds()
    {
        return this.bounds;
    }

    
    public void setBounds(Rectangle bounds)
    {
        this.bounds = bounds;
    }

    
    protected void updateCursor()
    {
        if (this.wwd instanceof Component)
        {
            ((Component) this.wwd).setCursor(this.getCursor());
        }
    }

    
    protected void setSize(Dimension newSize)
    {
        Size size = Size.fromPixels(newSize.width, newSize.height);

        BalloonAttributes attributes = this.balloon.getAttributes();

        // If the balloon is using default attributes, create a new set of attributes that we can customize
        if (attributes == null)
        {
            attributes = new BasicBalloonAttributes();
            this.balloon.setAttributes(attributes);
        }

        attributes.setSize(size);

        // Clear the balloon's maximum size. The user should be able to resize the balloon to any size.
        attributes.setMaximumSize(null);

        // If the balloon also has highlight attributes, change the highlight size as well.
        BalloonAttributes highlightAttributes = this.balloon.getHighlightAttributes();
        if (highlightAttributes != null)
        {
            highlightAttributes.setSize(size);
            highlightAttributes.setMaximumSize(null);
        }
    }

    
    protected Rectangle getBounds(PickedObject pickedObject)
    {
        if (pickedObject != null)
        {
            Object bounds = pickedObject.getValue(AVKey.BOUNDS);
            if (bounds instanceof Rectangle)
            {
                return (Rectangle) bounds;
            }
        }
        return null;
    }

    
    protected Point getScreenPoint()
    {
        Rectangle bounds = this.getBounds();
        if (bounds != null)
            return bounds.getLocation();
        else
            return null;
    }

    
    protected void setScreenPoint(Point newPoint)
    {
        // Do not set the screen point. The balloon is attached to a particular screen point, and we do not want to
        // change it. When the balloon is resized, the attachment point should remain constant, and the balloon should
        // move.
    }

    
    @Override
    protected Dimension getMinimumSize()
    {
        return DEFAULT_MIN_SIZE;
    }
}
