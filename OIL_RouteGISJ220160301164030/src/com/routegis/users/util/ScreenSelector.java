
package com.routegis.users.util;

import javax.media.opengl.*;

import com.routegis.applications.window.util.Util;

import core.routegis.engine.*;
import core.routegis.engine.event.*;
import core.routegis.engine.layers.*;
import core.routegis.engine.render.*;
import core.routegis.engine.util.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


public class ScreenSelector extends WWObjectImpl implements MouseListener, MouseMotionListener, SelectListener
{
    protected static class SelectionRectangle implements OrderedRenderable
    {
        protected static final Color DEFAULT_INTERIOR_COLOR = new Color(255, 255, 255, 64);
        protected static final Color DEFAULT_BORDER_COLOR = Color.WHITE;

        protected Rectangle rect;
        protected Point startPoint;
        protected Point endPoint;
        protected Color interiorColor;
        protected Color borderColor;
        protected OGLStackHandler BEogsh = new OGLStackHandler();

        public SelectionRectangle()
        {
            this.rect = new Rectangle();
            this.startPoint = new Point();
            this.endPoint = new Point();
        }

        public boolean hasSelection()
        {
            return !this.rect.isEmpty();
        }

        public Rectangle getSelection()
        {
            return this.rect;
        }

        public void startSelection(Point point)
        {
            if (point == null)
            {
                String msg = Logging.getMessage("nullValue.PointIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            this.startPoint.setLocation(point);
            this.endPoint.setLocation(point);
            this.rect.setRect(point.x, point.y, 0, 0);
        }

        public void endSelection(Point point)
        {
            if (point == null)
            {
                String msg = Logging.getMessage("nullValue.PointIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            this.endPoint.setLocation(point);

            // Compute the selection's extremes along the x axis.
            double minX, maxX;
            if (this.startPoint.x < this.endPoint.x)
            {
                minX = this.startPoint.x;
                maxX = this.endPoint.x;
            }
            else
            {
                minX = this.endPoint.x;
                maxX = this.startPoint.x;
            }

            // Compute the selection's extremes along the y axis. The selection is defined in AWT screen coordinates, so
            // the origin is in the upper left corner and the y axis points down.
            double minY, maxY;
            if (this.startPoint.y < this.endPoint.y)
            {
                minY = this.startPoint.y;
                maxY = this.endPoint.y;
            }
            else
            {
                minY = this.endPoint.y;
                maxY = this.startPoint.y;
            }

            // If only one of the selection rectangle's dimensions is zero, then the selection is either a horizontal or
            // vertical line. In this case, we set the zero dimension to 1 because both dimensions must be nonzero to
            // perform a selection.
            if (minX == maxX && minY < maxY)
                maxX = minX + 1;
            if (minY == maxY && minX < maxX)
                minY = maxY - 1;

            this.rect.setRect(minX, maxY, maxX - minX, maxY - minY);
        }

        public void clearSelection()
        {
            this.startPoint.setLocation(0, 0);
            this.endPoint.setLocation(0, 0);
            this.rect.setRect(0, 0, 0, 0);
        }

        public Color getInteriorColor()
        {
            return this.interiorColor;
        }

        public void setInteriorColor(Color color)
        {
            this.interiorColor = color;
        }

        public Color getBorderColor()
        {
            return this.borderColor;
        }

        public void setBorderColor(Color color)
        {
            this.borderColor = color;
        }

        public double getDistanceFromEye()
        {
            return 0; // Screen rectangle is drawn on top of other ordered renderables, except other screen objects.
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            // Intentionally left blank. SelectionRectangle is not pickable.
        }

        public void render(DrawContext dc)
        {
            if (dc == null)
            {
                String msg = Logging.getMessage("nullValue.DrawContextIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            if (dc.isOrderedRenderingMode())
                this.drawOrderedRenderable(dc);
            else
                this.makeOrderedRenderable(dc);
        }

        protected void makeOrderedRenderable(DrawContext dc)
        {
            if (this.hasSelection())
                dc.addOrderedRenderable(this);
        }

        protected void drawOrderedRenderable(DrawContext dc)
        {
            int attrs = GL2.GL_COLOR_BUFFER_BIT // For blend enable, alpha enable, blend func, alpha func.
                | GL2.GL_CURRENT_BIT // For current color.
                | GL2.GL_DEPTH_BUFFER_BIT; // For depth test disable.

            Rectangle viewport = dc.getView().getViewport();
            Rectangle selection = this.getSelection();

            GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            this.BEogsh.pushAttrib(gl, attrs);
            this.BEogsh.pushClientAttrib(gl, GL2.GL_VERTEX_ARRAY);
            try
            {
                // Configure the modelview-projection matrix to transform vertex points from screen rectangle
                // coordinates to clip coordinates without any perspective transformation. We offset the rectangle by
                // 0.5 pixels to ensure that the line loop draws a line without a 1-pixel gap between the line's
                // beginning and its end. We scale by (width - 1, height - 1) to ensure that only the actual selected
                // area is filled. If we scaled by (width, height), GL line rasterization would fill one pixel beyond
                // the actual selected area.
                this.BEogsh.pushProjectionIdentity(gl);
                gl.glOrtho(0, viewport.getWidth(), 0, viewport.getHeight(), -1, 1); // l, r, b, t, n, f
                this.BEogsh.pushModelviewIdentity(gl);
                gl.glTranslated(0.5, 0.5, 0.0);
                gl.glTranslated(selection.getX(), viewport.getHeight() - selection.getY(), 0);
                gl.glScaled(selection.getWidth() - 1, selection.getHeight() - 1, 1);

                // Disable the depth test and enable blending so this screen rectangle appears on top of the existing
                // framebuffer contents.
                gl.glDisable(GL.GL_DEPTH_TEST);
                gl.glEnable(GL.GL_BLEND);
                OGLUtil.applyBlending(gl, false); // SelectionRectangle does not use pre-multiplied colors.

                // Draw this screen rectangle's interior as a filled quadrilateral.
                Color c = this.getInteriorColor() != null ? this.getInteriorColor() : DEFAULT_INTERIOR_COLOR;
                gl.glColor4ub((byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue(), (byte) c.getAlpha());
                dc.drawUnitQuad();

                // Draw this screen rectangle's border as a line loop. This assumes the default line width of 1.0.
                c = this.getBorderColor() != null ? this.getBorderColor() : DEFAULT_BORDER_COLOR;
                gl.glColor4ub((byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue(), (byte) c.getAlpha());
                dc.drawUnitQuadOutline();
            }
            finally
            {
                this.BEogsh.pop(gl);
            }
        }
    }

    
    public static final String SELECTION_STARTED = "ScreenSelector.SelectionStarted";
    
    public static final String SELECTION_CHANGED = "ScreenSelector.SelectionChanged";
    
    public static final String SELECTION_ENDED = "ScreenSelector.SelectionEnded";

    protected WorldWindow wwd;
    protected Layer layer;
    protected SelectionRectangle selectionRect;
    protected List<Object> selectedObjects = new ArrayList<Object>();
    protected List<MessageListener> messageListeners = new ArrayList<MessageListener>();
    protected boolean armed;

    public ScreenSelector(WorldWindow worldWindow)
    {
        if (worldWindow == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = worldWindow;
        this.layer = this.createLayer();
        this.layer.setPickEnabled(false); // The screen selector is not pickable.
        this.selectionRect = this.createSelectionRectangle();
        ((RenderableLayer) this.layer).addRenderable(this.selectionRect);
    }

    protected Layer createLayer()
    {
        return new RenderableLayer();
    }

    protected SelectionRectangle createSelectionRectangle()
    {
        return new SelectionRectangle();
    }

    public WorldWindow getWwd()
    {
        return this.wwd;
    }

    public Layer getLayer()
    {
        return this.layer;
    }

    public Color getInteriorColor()
    {
        return this.selectionRect.getInteriorColor();
    }

    public void setInteriorColor(Color color)
    {
        this.selectionRect.setInteriorColor(color);
    }

    public Color getBorderColor()
    {
        return this.selectionRect.getBorderColor();
    }

    public void setBorderColor(Color color)
    {
        this.selectionRect.setBorderColor(color);
    }

    public void enable()
    {
        // Clear any existing selection and clear set the SceneController's pick rectangle. This ensures that this
        // ScreenSelector starts with the correct state when enabled.
        this.selectionRect.clearSelection();
        this.getWwd().getSceneController().setPickRectangle(null);

        // Add and enable the layer that displays this ScreenSelector's selection rectangle.
        LayerList layers = this.getWwd().getModel().getLayers();

        if (!layers.contains(this.getLayer()))
            layers.add(this.getLayer());

        if (!this.getLayer().isEnabled())
            this.getLayer().setEnabled(true);

        // Listen for mouse input on the World Window.
        this.getWwd().getInputHandler().addMouseListener(this);
        this.getWwd().getInputHandler().addMouseMotionListener(this);
    }

    public void disable()
    {
        // Clear the selection, clear the SceneController's pick rectangle, and stop listening for changes in the pick
        // rectangle selection. These steps should have been done when the selection ends, but do them here in case the
        // caller disables this ScreenSelector before the selection ends.
        this.selectionRect.clearSelection();
        this.getWwd().getSceneController().setPickRectangle(null);
        this.getWwd().removeSelectListener(this);

        // Remove the layer that displays this ScreenSelector's selection rectangle.
        this.getWwd().getModel().getLayers().remove(this.getLayer());

        // Stop listening for mouse input on the world window.
        this.getWwd().getInputHandler().removeMouseListener(this);
        this.getWwd().getInputHandler().removeMouseMotionListener(this);
    }

    public List<?> getSelectedObjects()
    {
        return this.selectedObjects;
    }

    public void addMessageListener(MessageListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener)
    {
        if (listener == null)
        {
            String msg = Logging.getMessage("nullValue.ListenerIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.messageListeners.remove(listener);
    }

    protected void sendMessage(Message message)
    {
        for (MessageListener listener : this.messageListeners)
        {
            try
            {
                listener.onMessage(message);
            }
            catch (Exception e)
            {
                String msg = Logging.getMessage("generic.ExceptionInvokingMessageListener");
                Logging.logger().severe(msg);
                // Don't throw an exception, just log a severe message and continue to the next listener.
            }
        }
    }

    public void mouseClicked(MouseEvent mouseEvent)
    {
        // Intentionally left blank. ScreenSelector does not respond to mouse clicked events.
    }

    public void mousePressed(MouseEvent mouseEvent)
    {
        if (mouseEvent == null) // Ignore null events.
            return;

        if (MouseEvent.BUTTON1_DOWN_MASK != mouseEvent.getModifiersEx()) // Respond to button 1 down w/o modifiers.
            return;

        this.armed = true;
        this.selectionStarted(mouseEvent);
        mouseEvent.consume(); // Consume the mouse event to prevent the view from responding to it.
    }

    public void mouseReleased(MouseEvent mouseEvent)
    {
        if (mouseEvent == null) // Ignore null events.
            return;

        if (!this.armed) // Respond to mouse released events when armed.
            return;

        this.armed = false;
        this.selectionEnded(mouseEvent);
        mouseEvent.consume(); // Consume the mouse event to prevent the view from responding to it.
    }

    public void mouseEntered(MouseEvent mouseEvent)
    {
        // Intentionally left blank. ScreenSelector does not respond to mouse entered events.
    }

    public void mouseExited(MouseEvent mouseEvent)
    {
        // Intentionally left blank. ScreenSelector does not respond to mouse exited events.
    }

    public void mouseDragged(MouseEvent mouseEvent)
    {
        if (mouseEvent == null) // Ignore null events.
            return;

        if (!this.armed) // Respond to mouse dragged events when armed.
            return;

        this.selectionChanged(mouseEvent);
        mouseEvent.consume(); // Consume the mouse event to prevent the view from responding to it.
    }

    public void mouseMoved(MouseEvent mouseEvent)
    {
        // Intentionally left blank. ScreenSelector does not respond to mouse moved events.
    }

    protected void selectionStarted(MouseEvent mouseEvent)
    {
        this.selectionRect.startSelection(mouseEvent.getPoint());
        this.getWwd().getSceneController().setPickRectangle(null);
        this.getWwd().addSelectListener(this); // Listen for changes in the pick rectangle selection.
        this.getWwd().redraw();

        // Clear the list of selected objects and send a message indicating that the user has started a selection.
        this.selectedObjects.clear();
        this.sendMessage(new Message(SELECTION_STARTED, this));
    }

    @SuppressWarnings({"UnusedParameters"})
    protected void selectionEnded(MouseEvent mouseEvent)
    {
        this.selectionRect.clearSelection();
        this.getWwd().getSceneController().setPickRectangle(null);
        this.getWwd().removeSelectListener(this); // Stop listening for changes the pick rectangle selection.
        this.getWwd().redraw();

        // Send a message indicating that the user has completed their selection. We don't clear the list of selected
        // objects in order to preserve the list of selected objects for the caller.
        this.sendMessage(new Message(SELECTION_ENDED, this));
    }

    protected void selectionChanged(MouseEvent mouseEvent)
    {
        // Limit the end point to the World Window's viewport rectangle. This ensures that a user drag event to define
        // the selection does not exceed the viewport and the viewing frustum. This is only necessary during mouse drag
        // events because those events are reported when the cursor is outside the World Window's viewport.
        Point p = this.limitPointToWorldWindow(mouseEvent.getPoint());

        // Specify the selection's end point and set the scene controller's pick rectangle to the selected rectangle.
        // We create a copy of the selected rectangle to insulate the scene controller from changes to rectangle
        // returned by ScreenRectangle.getSelection.
        this.selectionRect.endSelection(p);
        this.getWwd().getSceneController().setPickRectangle(
            this.selectionRect.hasSelection() ? new Rectangle(this.selectionRect.getSelection()) : null);
        this.getWwd().redraw();
    }

    
    protected Point limitPointToWorldWindow(Point point)
    {
        Rectangle viewport = this.getWwd().getView().getViewport();

        int x = point.x;
        if (x < viewport.x)
            x = viewport.x;
        if (x > viewport.x + viewport.width)
            x = viewport.x + viewport.width;

        int y = point.y;
        if (y < viewport.y)
            y = viewport.y;
        if (y > viewport.y + viewport.height)
            y = viewport.y + viewport.height;

        return new Point(x, y);
    }

    public void selected(SelectEvent event)
    {
        try
        {
            // Respond to box rollover select events when armed.
            if (event.getEventAction().equals(SelectEvent.BOX_ROLLOVER) && this.armed)
                this.selectObjects(event.getAllTopObjects());
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Util.getLogger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    protected void selectObjects(List<?> list)
    {
        if (this.selectedObjects.equals(list))
            return; // Same thing selected.

        this.selectedObjects.clear();

        // If the selection is empty, then we've cleared the list of selected objects and there's nothing left to do.
        // Otherwise, we add the selected objects to our list.
        if (list != null)
            this.selectedObjects.addAll(list);

        // Send a message indicating that the user has ended selection. We don't clear the list of selected objects
        // in order to preserve the list of selected objects for the caller.
        this.sendMessage(new Message(SELECTION_CHANGED, this));
    }
}
