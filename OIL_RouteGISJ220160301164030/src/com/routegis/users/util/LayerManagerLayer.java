
package com.routegis.users.util;

import java.awt.*;

import core.routegis.engine.WorldWindow;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.event.*;
import core.routegis.engine.geom.Vec4;
import core.routegis.engine.layers.*;
import core.routegis.engine.pick.PickedObject;
import core.routegis.engine.render.*;
import core.routegis.engine.util.Logging;


public class LayerManagerLayer extends RenderableLayer implements SelectListener
{
    protected WorldWindow wwd;
    protected boolean update = true;

    private ScreenAnnotation annotation;
    protected Dimension size;
    private int selectedIndex = -1;
    private Color color = Color.decode("#b0b0b0");
    private Color highlightColor = Color.decode("#ffffff");
    private double minOpacity = .6;
    private double maxOpacity = 1;
    private char layerEnabledSymbol = '\u25a0';
    private char layerDisabledSymbol = '\u25a1';
    private Font font = new Font("SansSerif", Font.PLAIN, 14);
    private boolean minimized = false;
    private int borderWidth = 20; // TODO: make configurable
    private String position = AVKey.NORTHWEST; // TODO: make configurable
    private Vec4 locationCenter = null;
    private Vec4 locationOffset = null;

    // Dragging
    private boolean componentDragEnabled = true;
    private boolean layerDragEnabled = true;
    private boolean snapToCorners = true;
    protected boolean draggingComponent = false;
    protected boolean draggingLayer = false;
    protected Point dragRefCursorPoint;
    protected Point dragRefPoint;
    protected int dragRefIndex = -1;
    protected Color dragColor = Color.RED;

    public LayerManagerLayer(WorldWindow wwd)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
        this.initialize();
    }

    protected void initialize()
    {
        // Set up screen annotation that will display the layer list
        this.annotation = new ScreenAnnotation("", new Point(0, 0));

        // Set annotation so that it will not force text to wrap (large width) and will adjust it's width to
        // that of the text. A height of zero will have the annotation height follow that of the text too.
        this.annotation.getAttributes().setSize(new Dimension(Integer.MAX_VALUE, 0));
        this.annotation.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);

        // Set appearance attributes
        this.annotation.getAttributes().setCornerRadius(0);
        this.annotation.getAttributes().setFont(this.font);
        this.annotation.getAttributes().setHighlightScale(1);
        this.annotation.getAttributes().setTextColor(Color.WHITE);
        this.annotation.getAttributes().setBackgroundColor(new Color(0f, 0f, 0f, .5f));
        this.annotation.getAttributes().setInsets(new Insets(6, 6, 6, 6));
        this.annotation.getAttributes().setBorderWidth(1);
        this.addRenderable(this.annotation);

        // Listen to world window for select event
        this.wwd.addSelectListener(this);
    }

    
    public ScreenAnnotation getAnnotation()
    {
        return this.annotation;
    }

    public void setEnabled(boolean enabled)
    {
        this.setMinimized(!enabled);
    }

    public boolean isEnabled()
    {
        return !this.isMinimized();
    }

    
    public Font getFont()
    {
        return this.font;
    }

    
    public void setFont(Font font)
    {
        if (font == null)
        {
            String message = Logging.getMessage("nullValue.FontIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.font.equals(font))
        {
            this.font = font;
            this.annotation.getAttributes().setFont(font);
            this.update();
        }
    }

    
    public Color getColor()
    {
        return this.color;
    }

    
    public void setColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.color = color;
        this.update();
    }

    
    public Color getHighlightColor()
    {
        return this.highlightColor;
    }

    
    public void setHighlightColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.highlightColor = color;
        this.update();
    }

    
    public double getMinOpacity()
    {
        return this.minOpacity;
    }

    
    public void setMinOpacity(double opacity)
    {
        this.minOpacity = opacity;
        this.update();
    }

    
    public double getMaxOpacity()
    {
        return this.maxOpacity;
    }

    
    public void setMaxOpacity(double opacity)
    {
        this.maxOpacity = opacity;
        this.update();
    }

    
    public char getLayerEnabledSymbol()
    {
        return this.layerEnabledSymbol;
    }

    
    public void setLayerEnabledSymbol(char c)
    {
        this.layerEnabledSymbol = c;
        this.update();
    }

    
    public char getLayerDisabledSymbol()
    {
        return this.layerDisabledSymbol;
    }

    
    public void setLayerDisabledSymbol(char c)
    {
        this.layerDisabledSymbol = c;
        this.update();
    }

    
    public int getBorderWidth()
    {
        return borderWidth;
    }

    
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
        this.update();
    }

    
    public String getPosition()
    {
        return position;
    }

    
    public void setPosition(String position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.ScreenPositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.position = position;
        this.update();
    }

    
    public Vec4 getLocationCenter()
    {
        return locationCenter;
    }

    
    public void setLocationCenter(Vec4 locationCenter)
    {
        this.locationCenter = locationCenter;
        this.update();
    }

    
    public Vec4 getLocationOffset()
    {
        return locationOffset;
    }

    
    public void setLocationOffset(Vec4 locationOffset)
    {
        this.locationOffset = locationOffset;
        this.update();
    }

    
    public boolean isMinimized()
    {
        return this.minimized;
    }

    
    public void setMinimized(boolean minimized)
    {
        this.minimized = minimized;
        this.update();
    }

    
    public boolean isComponentDragEnabled()
    {
        return this.componentDragEnabled;
    }

    
    public void setComponentDragEnabled(boolean enabled)
    {
        this.componentDragEnabled = enabled;
    }

    
    public boolean isLayerDragEnabled()
    {
        return this.layerDragEnabled;
    }

    
    public void setLayerDragEnabled(boolean enabled)
    {
        this.layerDragEnabled = enabled;
    }

    
    public boolean isSnapToCorners()
    {
        return this.snapToCorners;
    }

    
    public void setSnapToCorners(boolean enabled)
    {
        this.snapToCorners = enabled;
    }

    
    public int getSelectedIndex()
    {
        return this.selectedIndex;
    }

    
    public void setSelectedIndex(int index)
    {
        this.selectedIndex = index;
        this.update();
    }

    
    public void selected(SelectEvent event)
    {
        if (event.hasObjects() && event.getTopObject() == this.annotation)
        {
            boolean update = false;
            if (event.getEventAction().equals(SelectEvent.ROLLOVER)
                || event.getEventAction().equals(SelectEvent.LEFT_CLICK))
            {
                // Highlight annotation
                if (!this.annotation.getAttributes().isHighlighted())
                {
                    this.annotation.getAttributes().setHighlighted(true);
                    update = true;
                }
                // Check for text or url
                PickedObject po = event.getTopPickedObject();
                if (po.getValue(AVKey.URL) != null)
                {
                    // Set cursor hand on hyperlinks
                    ((Component) this.wwd).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    int i = Integer.parseInt((String) po.getValue(AVKey.URL));
                    // Select current hyperlink
                    if (this.selectedIndex != i)
                    {
                        this.selectedIndex = i;
                        update = true;
                    }
                    // Enable/disable layer on left click
                    if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
                    {
                        LayerList layers = wwd.getModel().getLayers();
                        if (i >= 0 && i < layers.size())
                        {
                            layers.get(i).setEnabled(!layers.get(i).isEnabled());
                            update = true;
                        }
                    }
                }
                else
                {
                    // Unselect if not on an hyperlink
                    if (this.selectedIndex != -1)
                    {
                        this.selectedIndex = -1;
                        update = true;
                    }
                    // Set cursor
                    if (this.isComponentDragEnabled())
                        ((Component) this.wwd).setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    else
                        ((Component) this.wwd).setCursor(Cursor.getDefaultCursor());
                }
            }
            if (event.getEventAction().equals(SelectEvent.DRAG)
                || event.getEventAction().equals(SelectEvent.DRAG_END))
            {
                // Handle dragging
                if (this.isComponentDragEnabled() || this.isLayerDragEnabled())
                {
                    boolean wasDraggingLayer = this.draggingLayer;
                    this.drag(event);
                    // Update list if dragging a layer, otherwise just redraw the world window
                    if (this.draggingLayer || wasDraggingLayer)
                        update = true;
                    else
                        this.wwd.redraw();
                    event.consume();
                }
            }
            // Redraw annotation if needed
            if (update)
                this.update();
        }
        else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && this.annotation.getAttributes().isHighlighted())
        {
            // de-highlight annotation
            this.annotation.getAttributes().setHighlighted(false);
            ((Component) this.wwd).setCursor(Cursor.getDefaultCursor());
            this.update();
        }
    }

    protected void drag(SelectEvent event)
    {
        if (event.getEventAction().equals(SelectEvent.DRAG))
        {
            if ((this.isComponentDragEnabled() && this.selectedIndex == -1 && this.dragRefIndex == -1)
                || this.draggingComponent)
            {
                // Dragging the whole list
                if (!this.draggingComponent)
                {
                    this.dragRefCursorPoint = event.getMouseEvent().getPoint();
                    this.dragRefPoint = this.annotation.getScreenPoint();
                    this.draggingComponent = true;
                }
                Point cursorOffset = new Point(event.getMouseEvent().getPoint().x - this.dragRefCursorPoint.x,
                    event.getMouseEvent().getPoint().y - this.dragRefCursorPoint.y);
                Point targetPoint = new Point(this.dragRefPoint.x + cursorOffset.x,
                    this.dragRefPoint.y - cursorOffset.y);
                this.moveTo(targetPoint);
                event.consume();
            }
            else if (this.isLayerDragEnabled())
            {
                // Dragging a layer inside the list
                if (!this.draggingLayer)
                {
                    this.dragRefIndex = this.selectedIndex;
                    this.draggingLayer = true;
                }
                if (this.selectedIndex != -1 && this.dragRefIndex != -1 && this.dragRefIndex != this.selectedIndex)
                {
                    // Move dragged layer
                    LayerList layers = this.wwd.getModel().getLayers();
                    int insertIndex = this.dragRefIndex > this.selectedIndex ?
                        this.selectedIndex : this.selectedIndex + 1;
                    int removeIndex = this.dragRefIndex > this.selectedIndex ?
                        this.dragRefIndex + 1 : this.dragRefIndex;
                    layers.add(insertIndex, layers.get(this.dragRefIndex));
                    layers.remove(removeIndex);
                    this.dragRefIndex = this.selectedIndex;
                    event.consume();
                }
            }
        }
        else if (event.getEventAction().equals(SelectEvent.DRAG_END))
        {
            this.draggingComponent = false;
            this.draggingLayer = false;
            this.dragRefIndex = -1;
        }
    }

    protected void moveTo(Point targetPoint)
    {
        Point refPoint = this.annotation.getScreenPoint();
        if (this.locationOffset == null)
            this.locationOffset = Vec4.ZERO;

        // Compute appropriate offset
        int x = (int) this.locationOffset.x - (refPoint.x - targetPoint.x);
        int y = (int) this.locationOffset.y - (refPoint.y - targetPoint.y);
        this.locationOffset = new Vec4(x, y, 0);

        // Compensate for rounding errors
        Point computedPoint = this.computeLocation(this.wwd.getView().getViewport());
        x += targetPoint.x - computedPoint.x;
        y += targetPoint.y - computedPoint.y;
        this.locationOffset = new Vec4(x, y, 0);

        if (this.snapToCorners)
            this.snapToCorners();
    }

    protected void snapToCorners()
    {
        // TODO: handle annotation scaling
        int width = this.size.width;
        int height = this.size.height;
        Rectangle viewport = this.wwd.getView().getViewport();
        Point refPoint = this.computeLocation(viewport);
        Point centerPoint = new Point(refPoint.x + width / 2, refPoint.y + height / 2);

        // Find closest corner position
        String newPos;
        if (centerPoint.x > viewport.width / 2)
            newPos = (centerPoint.y > viewport.height / 2) ? AVKey.NORTHEAST : AVKey.SOUTHEAST;
        else
            newPos = (centerPoint.y > viewport.height / 2) ? AVKey.NORTHWEST : AVKey.SOUTHWEST;

        // Adjust offset if position changed
        int x = 0, y = 0;
        if (newPos.equals(this.getPosition()))
        {
            x = (int) this.locationOffset.x;
            y = (int) this.locationOffset.y;
        }
        else
        {
            if (newPos.equals(AVKey.NORTHEAST))
            {
                x = refPoint.x - (viewport.width - width - this.borderWidth);
                y = refPoint.y - (viewport.height - height - this.borderWidth);
            }
            else if (newPos.equals(AVKey.SOUTHEAST))
            {
                x = refPoint.x - (viewport.width - width - this.borderWidth);
                y = refPoint.y - this.borderWidth;
            }
            if (newPos.equals(AVKey.NORTHWEST))
            {
                x = refPoint.x - this.borderWidth;
                y = refPoint.y - (viewport.height - height - this.borderWidth);
            }
            else if (newPos.equals(AVKey.SOUTHWEST))
            {
                x = refPoint.x - this.borderWidth;
                y = refPoint.y - this.borderWidth;
            }
        }

        // Snap to edges
        x = Math.abs(x) < 16 ? 0 : x;
        y = Math.abs(y) < 16 ? 0 : y;

        this.position = newPos;
        this.locationOffset = new Vec4(x, y, 0);
    }

    
    public void update()
    {
        this.update = true;
        this.wwd.redraw();
    }

    
    public void updateNow(DrawContext dc)
    {
        // Adjust annotation appearance to highlighted state
        this.highlight(this.annotation.getAttributes().isHighlighted());

        // Compose html text
        String text = this.makeAnnotationText(this.wwd.getModel().getLayers());
        this.annotation.setText(text);

        // Update current size and adjust annotation draw offset according to it's width
        // TODO: handle annotation scaling
        this.size = this.annotation.getPreferredSize(dc);
        this.annotation.getAttributes().setDrawOffset(new Point(this.size.width / 2, 0));

        // Clear update flag
        this.update = false;
    }

    
    protected void highlight(boolean highlighted)
    {
        // Adjust border color and annotation opacity
        if (highlighted)
        {
            this.annotation.getAttributes().setBorderColor(this.highlightColor);
            this.annotation.getAttributes().setOpacity(this.maxOpacity);
        }
        else
        {
            this.annotation.getAttributes().setBorderColor(this.color);
            this.annotation.getAttributes().setOpacity(this.minOpacity);
        }
    }

    
    protected String makeAnnotationText(LayerList layers)
    {
        // Compose html text
        StringBuilder text = new StringBuilder();
        Color color;
        int i = 0;
        for (Layer layer : layers)
        {
            if (!this.isMinimized() || layer == this)
            {
            	String name = layer.getName();

                color = (i == this.selectedIndex) ? this.highlightColor : this.color;
                color = (i == this.dragRefIndex) ? dragColor : color;
                text.append("<a href=\"");
                text.append(i);
                text.append("\"><font color=\"");
                text.append(encodeHTMLColor(color));
                text.append("\">");
                text.append((layer.isEnabled() ? layerEnabledSymbol : layerDisabledSymbol));
                text.append(" ");
                text.append((layer.isEnabled() ? "<b>" : "<i>"));
                text.append(name);
                text.append((layer.isEnabled() ? "</b>" : "</i>"));
                text.append("</a><br />");
            }
            i++;
        }
        return text.toString();
    }

    protected static String encodeHTMLColor(Color c)
    {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public void render(DrawContext dc)
    {
        if (this.update)
            this.updateNow(dc);

        this.annotation.setScreenPoint(computeLocation(dc.getView().getViewport()));
        super.render(dc);
    }

    
    protected Point computeLocation(Rectangle viewport)
    {
        // TODO: handle annotation scaling
        int width = this.size.width;
        int height = this.size.height;

        int x;
        int y;

        if (this.locationCenter != null)
        {
            x = (int) this.locationCenter.x - width / 2;
            y = (int) this.locationCenter.y - height / 2;
        }
        else if (this.position.equals(AVKey.NORTHEAST))
        {
            x = (int) viewport.getWidth() - width - this.borderWidth;
            y = (int) viewport.getHeight() - height - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHEAST))
        {
            x = (int) viewport.getWidth() - width - this.borderWidth;
            //noinspection SuspiciousNameCombination
            y = this.borderWidth;
        }
        else if (this.position.equals(AVKey.NORTHWEST))
        {
            x = this.borderWidth;
            y = (int) viewport.getHeight() - height - this.borderWidth;
        }
        else if (this.position.equals(AVKey.SOUTHWEST))
        {
            x = this.borderWidth;
            //noinspection SuspiciousNameCombination
            y = this.borderWidth;
        }
        else // use North East as default
        {
            x = (int) viewport.getWidth() - width - this.borderWidth;
            y = (int) viewport.getHeight() - height - this.borderWidth;
        }

        if (this.locationOffset != null)
        {
            x += this.locationOffset.x;
            y += this.locationOffset.y;
        }

        return new Point(x, y);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.LayerManagerLayer.Name");
    }
}
