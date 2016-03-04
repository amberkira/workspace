
package com.routegis.users.util;

import java.awt.*;

import core.routegis.engine.MainClass;
import core.routegis.engine.geom.*;
import core.routegis.engine.render.*;
import core.routegis.engine.util.Logging;


public class LabeledPath implements Renderable
{
    
    protected Iterable<? extends LatLon> locations;
    
    protected int altitudeMode = MainClass.ABSOLUTE;
    
    protected ScreenAnnotation annotation;
    
    protected long frameNumber = -1;
    
    protected int labelLocationIndex = -1;

    
    public LabeledPath()
    {
    }

    
    public LabeledPath(Iterable<? extends LatLon> locations)
    {
        this.setLocations(locations);
    }

    
    public LabeledPath(ScreenAnnotation annotation)
    {
        this.setAnnotation(annotation);
    }

    
    public LabeledPath(Iterable<? extends LatLon> locations, ScreenAnnotation annotation)
    {
        this.setLocations(locations);
        this.setAnnotation(annotation);
    }

    
    public Iterable<? extends LatLon> getLocations()
    {
        return locations;
    }

    
    public void setLocations(Iterable<? extends LatLon> locations)
    {
        if (locations == null)
        {
            String message = Logging.getMessage("nullValue.LocationsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.locations = locations;
        this.reset();
    }

    
    public int getAltitudeMode()
    {
        return altitudeMode;
    }

    
    public void setAltitudeMode(int altitudeMode)
    {
        this.altitudeMode = altitudeMode;
        this.reset();
    }

    
    public ScreenAnnotation getAnnotation()
    {
        return this.annotation;
    }

    
    public void setAnnotation(ScreenAnnotation annotation)
    {
        this.annotation = annotation;
        this.reset();
    }

    
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Determine the label's location only once per frame.
        if (this.frameNumber != dc.getFrameTimeStamp())
        {
            this.determineLabelLocation(dc);
            this.frameNumber = dc.getFrameTimeStamp();
        }

        this.drawLabel(dc);
    }

    
    protected void reset()
    {
        this.labelLocationIndex = -1;
    }

    
    protected void determineLabelLocation(DrawContext dc)
    {
        // Reuse the current label location if its inside the view frustum and the label is completely visible when
        // placed there. Otherwise we find a new location that maximizes the label's visible area and is closest to the
        // current location.
        Vec4 lastPoint = this.getLabelPoint(dc);
        if (lastPoint != null && dc.getView().getFrustumInModelCoordinates().contains(lastPoint))
        {
            // Project the current location's model point into screen coordinates, and place the label at the
            // projected point. We do this to measure the label's visible area when placed at that point.
            Vec4 screenPoint = dc.getView().project(lastPoint);
            this.setLabelLocation(dc, screenPoint);

            // If the label is completely visible, just reuse its current location.
            if (this.isLabelCompletelyVisible(dc))
                return;
        }

        this.labelLocationIndex = -1;

        if (this.getLocations() == null)
            return;

        double maxArea = 0;
        double minDistance = Double.MAX_VALUE;
        int locationIndex = -1;

        for (LatLon ll : this.getLocations())
        {
            ++locationIndex;

            if (ll == null)
                continue;

            // Compute the specified location's point in model coordinates. Ignore locations who's model coordinate
            // point cannot be computed for any reason, or are outside the view frustum.
            Vec4 point = this.computePoint(dc, ll);
            if (point == null || !dc.getView().getFrustumInModelCoordinates().contains(point))
                continue;

            // Project the specified location's model point into screen coordinates, and place the label at the
            // projected point. We do this to measure the label's visible area when placed at that point.
            Vec4 screenPoint = dc.getView().project(point);
            this.setLabelLocation(dc, screenPoint);

            // Find the location that maximizes the label's visible area.
            double area = this.getLabelVisibleArea(dc);
            if (maxArea < area)
            {
                maxArea = area;
                this.labelLocationIndex = locationIndex;

                if (lastPoint != null)
                    minDistance = lastPoint.distanceToSquared3(point);
            }
            // If two or more locations cause the label to have the same visible area, give priority to the location
            // closest to the previous location.
            else if (maxArea == area && lastPoint != null)
            {
                double dist = lastPoint.distanceToSquared3(point);
                if (minDistance > dist)
                {
                    minDistance = dist;
                    this.labelLocationIndex = locationIndex;
                }
            }
        }
    }

    
    protected void drawLabel(DrawContext dc)
    {
        if (this.getAnnotation() == null)
            return;

        // Get the label's model point from the location iterable.
        Vec4 point = this.getLabelPoint(dc);
        if (point == null)
            return;

        // Project the label's model point into screen coordinates, place the annotation at the projected point then
        // draw the annotation.
        Vec4 screenPoint = dc.getView().project(point);
        this.setLabelLocation(dc, screenPoint);
        this.getAnnotation().render(dc);
    }

    
    @SuppressWarnings({"UnusedDeclaration"})
    protected void setLabelLocation(DrawContext dc, Vec4 screenPoint)
    {
        if (this.getAnnotation() != null)
            this.getAnnotation().setScreenPoint(new Point((int) screenPoint.x, (int) screenPoint.y));
    }

    
    protected double getLabelVisibleArea(DrawContext dc)
    {
        if (this.getAnnotation() == null)
            return 0;

        Rectangle bounds = this.getAnnotation().getBounds(dc);
        if (bounds == null)
            return 0;

        Rectangle intersection = dc.getView().getViewport().intersection(bounds);
        return intersection.width * intersection.height;
    }

    
    protected boolean isLabelCompletelyVisible(DrawContext dc)
    {
        if (this.getAnnotation() == null)
            return false;

        Rectangle bounds = this.getAnnotation().getBounds(dc);
        return bounds == null || dc.getView().getViewport().contains(bounds);
    }

    
    protected Vec4 getLabelPoint(DrawContext dc)
    {
        if (this.getLocations() == null)
            return null;

        if (this.labelLocationIndex == -1)
            return null;

        int i = 0;
        LatLon location = null;
        for (LatLon ll : this.getLocations())
        {
            if (i++ == this.labelLocationIndex)
                location = ll;
        }

        if (location == null)
            return null;

        return this.computePoint(dc, location);
    }

    
    protected Vec4 computePoint(DrawContext dc, LatLon location)
    {
        double elevation = (location instanceof Position) ? ((Position) location).getElevation() : 0;

        if (this.getAltitudeMode() == MainClass.CLAMP_TO_GROUND)
            return dc.computeTerrainPoint(location.getLatitude(), location.getLongitude(), 0d);
        else if (this.getAltitudeMode() == MainClass.RELATIVE_TO_GROUND)
            return dc.computeTerrainPoint(location.getLatitude(), location.getLongitude(), elevation);

        double height = elevation * dc.getVerticalExaggeration();
        return dc.getGlobe().computePointFromPosition(location.getLatitude(), location.getLongitude(), height);
    }
}
