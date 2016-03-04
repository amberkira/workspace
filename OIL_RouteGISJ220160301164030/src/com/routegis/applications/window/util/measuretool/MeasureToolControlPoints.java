

package com.routegis.applications.window.util.measuretool;

import java.awt.*;
import java.util.ArrayList;

import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.geom.Position;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.render.*;


public class MeasureToolControlPoints implements MeasureTool.ControlPointList, Renderable
{
    public class ControlPoint extends GlobeAnnotation implements MeasureTool.ControlPoint
    {
        public ControlPoint(Position position)
        {
            super("", position, MeasureToolControlPoints.this.controlPointAttributes);
        }

        public MeasureTool getParent()
        {
            return MeasureToolControlPoints.this.measureTool;
        }

        @Override
        public void setPosition(Position position)
        {
            super.setPosition(position);
        }

        @Override
        public Position getPosition()
        {
            return super.getPosition();
        }

        public void highlight(boolean tf)
        {
            this.getAttributes().setHighlighted(tf);
            this.getAttributes().setBackgroundColor(tf ? this.getAttributes().getTextColor() : null);
        }

        @Override
        public Object setValue(String key, Object value)
        {
            return super.setValue(key, value);
        }

        @Override
        public Object getValue(String key)
        {
            return super.getValue(key);
        }
    }

    protected MeasureTool measureTool;
    protected ArrayList<ControlPoint> points = new ArrayList<ControlPoint>();
    protected AnnotationAttributes controlPointAttributes;

    public MeasureToolControlPoints(MeasureTool measureTool)
    {
        this.measureTool = measureTool;

        this.controlPointAttributes = new AnnotationAttributes();
        // Define an 8x8 square centered on the screen point
        this.controlPointAttributes.setFrameShape(AVKey.SHAPE_RECTANGLE);
        this.controlPointAttributes.setLeader(AVKey.SHAPE_NONE);
        this.controlPointAttributes.setAdjustWidthToText(AVKey.SIZE_FIXED);
        this.controlPointAttributes.setSize(new Dimension(8, 8));
        this.controlPointAttributes.setDrawOffset(new Point(0, -4));
        this.controlPointAttributes.setInsets(new Insets(0, 0, 0, 0));
        this.controlPointAttributes.setBorderWidth(0);
        this.controlPointAttributes.setCornerRadius(0);
        this.controlPointAttributes.setBackgroundColor(Color.BLUE);    // Normal color
        this.controlPointAttributes.setTextColor(Color.GREEN);         // Highlighted color
        this.controlPointAttributes.setHighlightScale(1.2);
        this.controlPointAttributes.setDistanceMaxScale(1);            // No distance scaling
        this.controlPointAttributes.setDistanceMinScale(1);
        this.controlPointAttributes.setDistanceMinOpacity(1);
    }

    public void addToLayer(RenderableLayer layer)
    {
        layer.addRenderable(this);
    }

    public void removeFromLayer(RenderableLayer layer)
    {
        layer.removeRenderable(this);
    }

    public int size()
    {
        return this.points.size();
    }

    public MeasureTool.ControlPoint createControlPoint(Position position)
    {
        return new ControlPoint(position);
    }

    public MeasureTool.ControlPoint get(int index)
    {
        return this.points.get(index);
    }

    public void add(MeasureTool.ControlPoint controlPoint)
    {
        this.points.add((ControlPoint) controlPoint);
    }

    public void remove(MeasureTool.ControlPoint controlPoint)
    {
        this.points.remove((ControlPoint) controlPoint);
    }

    public void remove(int index)
    {
        this.points.remove(index);
    }

    public void clear()
    {
        this.points.clear();
    }

    public void render(DrawContext dc)
    {
        for (ControlPoint cp : this.points)
        {
            cp.render(dc);
        }
    }
}
