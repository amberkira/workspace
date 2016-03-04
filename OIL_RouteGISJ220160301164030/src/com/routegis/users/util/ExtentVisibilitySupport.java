
package com.routegis.users.util;

import java.awt.*;
import java.util.ArrayList;

import core.routegis.engine.View;
import core.routegis.engine.geom.*;
import core.routegis.engine.globes.Globe;
import core.routegis.engine.util.Logging;
import core.routegis.engine.view.ViewUtil;


public class ExtentVisibilitySupport
{
    
    public static class ScreenExtent
    {
        protected Vec4 modelReferencePoint;
        protected Rectangle screenBounds;

        
        public ScreenExtent(Vec4 modelReferencePoint, Rectangle screenBounds)
        {
            this.modelReferencePoint = modelReferencePoint;
            this.screenBounds = (screenBounds != null) ? new Rectangle(screenBounds) : null;
        }

        
        public Vec4 getModelReferencePoint()
        {
            return this.modelReferencePoint;
        }

        
        public Rectangle getScreenBounds()
        {
            return (this.screenBounds != null) ? new Rectangle(this.screenBounds) : null;
        }
    }

    protected static final double EPSILON = 1.0e-6;
    protected static final double SCREEN_POINT_PADDING_PIXELS = 4;

    protected Iterable<? extends Extent> extentIterable;
    protected Iterable<? extends ScreenExtent> screenExtentIterable;

    
    public ExtentVisibilitySupport()
    {
    }

    
    public static Iterable<Extent> extentsFromExtentHolders(Iterable<? extends ExtentHolder> extentHolders,
        Globe globe, double verticalExaggeration)
    {
        if (extentHolders == null)
            return null;

        ArrayList<Extent> list = new ArrayList<Extent>();

        for (ExtentHolder eh : extentHolders)
        {
            if (eh == null)
                continue;

            Extent e = eh.getExtent(globe, verticalExaggeration);
            if (e == null)
                continue;

            list.add(e);
        }

        if (list.isEmpty())
            return null;

        return list;
    }

    
    public Iterable<? extends Extent> getExtents()
    {
        return this.extentIterable;
    }

    
    public void setExtents(Iterable<? extends Extent> extents)
    {
        this.extentIterable = extents;
    }

    
    public Iterable<? extends ScreenExtent> getScreenExtents()
    {
        return this.screenExtentIterable;
    }

    
    public void setScreenExtents(Iterable<? extends ScreenExtent> screenExtents)
    {
        this.screenExtentIterable = screenExtents;
    }

    
    public boolean areExtentsContained(Frustum frustum, Rectangle viewport)
    {
        if (frustum == null)
        {
            String message = Logging.getMessage("nullValue.FrustumIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (viewport == null)
        {
            String message = Logging.getMessage("nullValue.ViewportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (viewport.getWidth() <= 0d)
        {
            String message = Logging.getMessage("Geom.ViewportWidthInvalid", viewport.getWidth());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (viewport.getHeight() <= 0d)
        {
            String message = Logging.getMessage("Geom.ViewportHeightInvalid", viewport.getHeight());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Iterable<? extends Extent> extents = this.getExtents();
        if (extents != null)
        {
            for (Extent e : extents)
            {
                if (e == null)
                    continue;

                if (!frustum.contains(e))
                    return false;
            }
        }

        Iterable<? extends ScreenExtent> screenExtents = this.getScreenExtents();
        if (screenExtents != null)
        {
            for (ScreenExtent se : screenExtents)
            {
                if (se == null)
                    continue;

                if (se.getScreenBounds() != null && !viewport.contains(se.getScreenBounds()))
                    return false;

                if (se.getModelReferencePoint() != null && !frustum.contains(se.getModelReferencePoint()))
                    return false;
            }
        }

        return true;
    }

    
    public boolean areExtentsContained(View view)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.areExtentsContained(view.getFrustumInModelCoordinates(), view.getViewport());
    }

    
    public Vec4[] computeViewLookAtContainingExtents(Globe globe, double verticalExaggeration,
        Vec4 eyePoint, Vec4 centerPoint, Vec4 upVector, Angle fieldOfView, Rectangle viewport,
        double nearClipDistance, double farClipDistance)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (eyePoint == null)
        {
            String message = Logging.getMessage("nullValue.EyeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (centerPoint == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (upVector == null)
        {
            String message = Logging.getMessage("nullValue.UpIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (fieldOfView == null)
        {
            String message = Logging.getMessage("nullValue.FOVIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (viewport == null)
        {
            String message = Logging.getMessage("nullValue.ViewportIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String message = this.validate(eyePoint, centerPoint, upVector, fieldOfView, viewport,
            nearClipDistance, farClipDistance);
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Gather the model coordinate and screen coordinate extents associated with this ExtentVisibilitySupport.
        Iterable<? extends Extent> modelExtents = this.getExtents();
        Iterable<? extends ScreenExtent> screenExtents = this.getScreenExtents();

        // Compute a new view center point optimal for viewing the extents on the specified Globe.
        Vec4 newCenterPoint = this.computeCenterPoint(globe, verticalExaggeration, modelExtents, screenExtents);
        if (newCenterPoint == null)
            newCenterPoint = centerPoint;

        // Compute a local model coordinate origin transforms at the current center position and at the new center
        // position. We transform the view's model coordinates from the current local origin to the new local origin in
        // order to preserve the view's orientation relative to the Globe.
        Position centerPos = globe.computePositionFromPoint(centerPoint);
        Position newCenterPos = globe.computePositionFromPoint(newCenterPoint);
        Matrix localCoords = globe.computeSurfaceOrientationAtPosition(centerPos);
        Matrix newLocalCoords = globe.computeSurfaceOrientationAtPosition(newCenterPos);

        // Compute the modelview matrix from the specified model coordinate look-at parameters, and the projection
        // matrix from the specified projection parameters.
        Matrix modelview = Matrix.fromViewLookAt(eyePoint, centerPoint, upVector);
        Matrix projection = Matrix.fromPerspective(fieldOfView, viewport.getWidth(), viewport.getHeight(),
            nearClipDistance, farClipDistance);

        // Compute the eye point and up vector in model coordinates at the new center position on the Globe, while
        // preserving the view's orientation relative to the Globe. We accomplish this by transforming the identity
        // eye point and up vector by the matrix which maps identity model coordinates to the model coordinates at the
        // new center position on the Globe.
        Matrix m = Matrix.IDENTITY;
        m = m.multiply(newLocalCoords);
        m = m.multiply(localCoords.getInverse());
        m = m.multiply(modelview.getInverse());
        Vec4 newEyePoint = Vec4.UNIT_W.transformBy4(m);
        Vec4 newUpVector = Vec4.UNIT_Y.transformBy4(m);

        // Compute the new modelview matrix from the new look at parameters, and adjust the screen extents for the
        // change in modelview parameters.
        Matrix newModelview = Matrix.fromViewLookAt(newEyePoint, newCenterPoint, newUpVector);
        if (screenExtents != null)
            screenExtents = this.translateScreenExtents(screenExtents, modelview, newModelview, projection, viewport);

        // Compute the optimal eye point for viewing the extents on the specified Globe.
        Vec4 p = this.computeEyePoint(newEyePoint, newCenterPoint, newUpVector, fieldOfView, viewport, nearClipDistance,
            farClipDistance, modelExtents, screenExtents);
        if (p != null)
            newEyePoint = p;

        return new Vec4[] {newEyePoint, newCenterPoint, newUpVector};
    }

    
    public Vec4[] computeViewLookAtContainingExtents(Globe globe, double verticalExaggeration, View view)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 eye = view.getEyePoint();
        Vec4 center = view.getCenterPoint();
        Vec4 up = view.getUpVector();

        if (center == null)
            center = eye.add3(view.getForwardVector());

        return this.computeViewLookAtContainingExtents(globe, verticalExaggeration, eye, center, up,
            view.getFieldOfView(), view.getViewport(), view.getNearClipDistance(), view.getFarClipDistance());
    }

    protected String validate(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView, Rectangle viewport,
        double nearClipDistance, double farClipDistance)
    {
        Vec4 f = center.subtract3(eye).normalize3();
        Vec4 u = up.normalize3();

        if (eye.distanceTo3(center) <= EPSILON)
            return Logging.getMessage("Geom.EyeAndCenterInvalid", eye, center);

        if (f.dot3(u) >= 1d - EPSILON)
            return Logging.getMessage("Geom.UpAndLineOfSightInvalid", up, f);

        if (fieldOfView.compareTo(Angle.ZERO) < 0 || fieldOfView.compareTo(Angle.POS180) > 0)
            return Logging.getMessage("Geom.ViewFrustum.FieldOfViewOutOfRange");

        if (viewport.getWidth() <= 0d)
            return Logging.getMessage("Geom.ViewportWidthInvalid", viewport.getWidth());

        if (viewport.getHeight() <= 0d)
            return Logging.getMessage("Geom.ViewportHeightInvalid", viewport.getHeight());

        if (nearClipDistance < 0d || farClipDistance < 0d || nearClipDistance > farClipDistance)
            return Logging.getMessage("Geom.ViewFrustum.ClippingDistanceOutOfRange");

        return null;
    }

    //
    //
    //

    @SuppressWarnings({"UnusedDeclaration"})
    protected Vec4 computeCenterPoint(Globe globe, double verticalExaggeration,
        Iterable<? extends Extent> modelExtents, Iterable<? extends ScreenExtent> screenExtents)
    {
        ArrayList<Vec4> list = new ArrayList<Vec4>();

        if (modelExtents != null)
        {
            for (Extent e : modelExtents)
            {
                if (e == null || e.getCenter() == null)
                    continue;

                list.add(e.getCenter());
            }
        }

        if (screenExtents != null)
        {
            for (ScreenExtent se : screenExtents)
            {
                if (se == null || se.getModelReferencePoint() == null)
                    continue;

                list.add(se.getModelReferencePoint());
            }
        }

        if (list.isEmpty())
            return null;

        return Vec4.computeAveragePoint(list);
    }

    //
    //
    //

    protected Vec4 computeEyePoint(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView, Rectangle viewport,
        double nearClipDistance, double farClipDistance,
        Iterable<? extends Extent> modelExtents, Iterable<? extends ScreenExtent> screenExtents)
    {
        // Compute the modelview matrix from the specified model coordinate look-at parameters, and the projection
        // matrix from the specified projection parameters.
        Matrix modelview = Matrix.fromViewLookAt(eye, center, up);
        Matrix projection = Matrix.fromPerspective(fieldOfView, viewport.getWidth(), viewport.getHeight(),
            nearClipDistance, farClipDistance);

        Vec4 newEye = null;

        // Compute the eye point which contains the specified model coordinate extents. We compute the model coordinate
        // eye point first to provide a baseline eye point which the screen extent computation can be compared against.
        Vec4 p = this.computeEyePointForModelExtents(eye, center, up, fieldOfView, viewport, modelExtents);
        if (p != null)
        {
            newEye = p;

            // Compute the new modelview matrix from the new look at parameters, and adjust the screen extents for the
            // change in modelview parameters.
            Matrix newModelview = Matrix.fromViewLookAt(newEye, center, up);
            if (screenExtents != null)
            {
                screenExtents = this.translateScreenExtents(screenExtents, modelview, newModelview, projection,
                    viewport);
            }
        }

        // Compute the eye point which contains the specified screen extents. If the model extent eye point is null, or
        // if it's closer to the center position than the screen extent eye point.
        p = this.computeEyePointForScreenExtents((newEye != null) ? newEye : eye, center, up, fieldOfView, viewport,
            nearClipDistance, farClipDistance, screenExtents);
        if (p != null && (newEye == null || newEye.distanceTo3(center) < p.distanceTo3(center)))
        {
            newEye = p;
        }

        return newEye;
    }

    //
    //
    //

    protected Vec4 computeEyePointForModelExtents(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView,
        Rectangle viewport, Iterable<? extends Extent> modelExtents)
    {
        if (modelExtents == null)
            return null;

        // Compute the modelview matrix from the specified model coordinate look-at parameters.
        Matrix modelview = Matrix.fromViewLookAt(eye, center, up);
        // Compute the forward vector in model coordinates, and the center point in eye coordinates.
        Vec4 f = Vec4.UNIT_NEGATIVE_Z.transformBy4(modelview.getInverse());
        Vec4 c = center.transformBy4(modelview);

        Angle verticalFieldOfView = ViewUtil.computeVerticalFieldOfView(fieldOfView, viewport);
        double hcos = fieldOfView.cosHalfAngle();
        double htan = fieldOfView.tanHalfAngle();
        double vcos = verticalFieldOfView.cosHalfAngle();
        double vtan = verticalFieldOfView.tanHalfAngle();

        double maxDistance = -Double.MAX_VALUE;
        double d;

        // Compute the smallest distance from the center point needed to contain the model coordinate extents in the
        // viewport.
        for (Extent e : modelExtents)
        {
            if (e == null || e.getCenter() == null)
                continue;

            Vec4 p = e.getCenter().transformBy4(modelview);

            d = (p.z - c.z) + (Math.abs(p.x) + (e.getRadius() / hcos)) / htan;
            if (maxDistance < d)
                maxDistance = d;

            d = (p.z - c.z) + (Math.abs(p.y) + (e.getRadius() / vcos)) / vtan;
            if (maxDistance < d)
                maxDistance = d;
        }

        if (maxDistance == -Double.MAX_VALUE)
            return null;

        return center.add3(f.multiply3(-maxDistance));
    }

    //
    //
    //

    protected Vec4 computeEyePointForScreenExtents(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView,
        Rectangle viewport, double nearClipDistance, double farClipDistance,
        Iterable<? extends ScreenExtent> screenExtents)
    {
        if (screenExtents == null)
            return null;

        // Compute the modelview matrix from the specified model coordinate look-at parameters, and the projection
        // matrix from the specified projection parameters.
        Matrix modelview = Matrix.fromViewLookAt(eye, center, up);
        Matrix projection = Matrix.fromPerspective(fieldOfView, viewport.getWidth(), viewport.getHeight(),
            nearClipDistance, farClipDistance);

        Vec4 newEye;

        // Compute the eye point which contains the specified model coordinate reference points before computing the eye
        // point which contains the screen bounds. The screen bound computation only resolves intersections between
        // the screen bound and the viewport, it does not attempt to find the nearest eye point containing the bounds.
        // By first computing the nearest eye point containing the model coordinate reference points, we provide a
        // minimum distance eye point to the next computation. The final result is the nearest eye point which contains
        // the screen bounds.
        newEye = this.computeEyePointForScreenReferencePoints(eye, center, up, fieldOfView, viewport, screenExtents);
        if (newEye == null)
            return null;

        // Compute the new modelview matrix from the new look at parameters, and adjust the screen extents for the
        // change in modelview parameters.
        Matrix newModelview = Matrix.fromViewLookAt(newEye, center, up);
        screenExtents = this.translateScreenExtents(screenExtents, modelview, newModelview, projection, viewport);

        // Compute the eye point which contains the specified screen coordinate bounding rectangles.
        Vec4 p = this.computeEyePointForScreenBounds(newEye, center, up, fieldOfView, viewport, nearClipDistance,
            farClipDistance, screenExtents);
        if (p != null)
            newEye = p;

        return newEye;
    }

    protected Vec4 computeEyePointForScreenReferencePoints(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView,
        Rectangle viewport, Iterable<? extends ScreenExtent> screenExtents)
    {
        if (screenExtents == null)
            return null;

        // Compute the modelview matrix from the specified model coordinate look-at parameters.
        Matrix modelview = Matrix.fromViewLookAt(eye, center, up);
        // Compute the forward vector in model coordinates, and the center point in eye coordinates.
        Vec4 f = Vec4.UNIT_NEGATIVE_Z.transformBy4(modelview.getInverse());
        Vec4 c = center.transformBy4(modelview);

        Angle verticalFieldOfView = ViewUtil.computeVerticalFieldOfView(fieldOfView, viewport);
        double htan = fieldOfView.tanHalfAngle();
        double vtan = verticalFieldOfView.tanHalfAngle();

        double maxDistance = -Double.MAX_VALUE;
        double d;

        // Compute the smallest distance from the center point needed to contain the screen extent's model coordinate
        // reference points visible in the viewport.
        for (ScreenExtent se : screenExtents)
        {
            if (se == null || se.getModelReferencePoint() == null)
                continue;

            Vec4 p = se.getModelReferencePoint().transformBy4(modelview);
            double metersPerPixel = ViewUtil.computePixelSizeAtDistance(-p.z, fieldOfView, viewport);
            double metersOffset = SCREEN_POINT_PADDING_PIXELS * metersPerPixel;

            d = (p.z - c.z) + (metersOffset + Math.abs(p.x)) / htan;
            if (maxDistance < d)
                maxDistance = d;

            d = (p.z - c.z) + (metersOffset + Math.abs(p.y)) / vtan;
            if (maxDistance < d)
                maxDistance = d;
        }

        if (maxDistance == -Double.MAX_VALUE)
            return null;

        return center.add3(f.multiply3(-maxDistance));
    }

    protected Vec4 computeEyePointForScreenBounds(Vec4 eye, Vec4 center, Vec4 up, Angle fieldOfView,
        Rectangle viewport, double nearClipDistance, double farClipDistance,
        Iterable<? extends ScreenExtent> screenExtents)
    {
        if (screenExtents == null)
            return null;

        // Compute the modelview matrix from the specified model coordinate look-at parameters, and the projection
        // matrix from the specified projection parameters.
        Matrix modelview = Matrix.fromViewLookAt(eye, center, up);
        Matrix projection = Matrix.fromPerspective(fieldOfView, viewport.getWidth(), viewport.getHeight(),
            nearClipDistance, farClipDistance);

        // Compute the forward vector in model coordinates, and the center point in eye coordinates.
        Vec4 f = Vec4.UNIT_NEGATIVE_Z.transformBy4(modelview.getInverse());
        Vec4 c = center.transformBy4(modelview);

        double maxDistance = -Double.MAX_VALUE;
        double d;

        // If possible, estimate an eye distance which makes the entire screen bounds visible.
        for (ScreenExtent se : screenExtents)
        {
            if (se == null || se.getModelReferencePoint() == null || se.getScreenBounds() == null)
                continue;

            Vec4 ep = se.getModelReferencePoint().transformBy4(modelview);
            Vec4 sp = ViewUtil.project(se.getModelReferencePoint(), modelview, projection, viewport);
            Rectangle r = se.getScreenBounds();

            if (r.getWidth() < viewport.getWidth()
                && (r.getMinX() < viewport.getMinX() || r.getMaxX() > viewport.getMaxX()))
            {
                double x0 = Math.abs(viewport.getCenterX() - sp.x);
                double x1 = (r.getMinX() < viewport.getMinX()) ?
                    (viewport.getMinX() - r.getMinX()) :
                    (r.getMaxX() - viewport.getMaxX());

                if (x1 < x0)
                {
                    d = (ep.z - c.z) + Math.abs(ep.z) * x0 / (x0 - x1);
                    if (maxDistance < d)
                        maxDistance = d;
                }
            }

            if (r.getHeight() < viewport.getHeight()
                && (r.getMinY() < viewport.getMinY() || r.getMaxY() > viewport.getMaxY()))
            {
                double y0 = Math.abs(viewport.getCenterY() - sp.y);
                double y1 = (r.getMinY() < viewport.getMinY()) ?
                    (viewport.getMinY() - r.getMinY()) :
                    (r.getMaxY() - viewport.getMaxY());

                if (y1 < y0)
                {
                    d = (ep.z - c.z) + Math.abs(ep.z) * y0 / (y0 - y1);
                    if (maxDistance < d)
                        maxDistance = d;
                }
            }
        }

        if (maxDistance == -Double.MAX_VALUE)
            return null;

        return center.add3(f.multiply3(-maxDistance));
    }

    protected Iterable<ScreenExtent> translateScreenExtents(Iterable<? extends ScreenExtent> screenExtents,
        Matrix oldModelview, Matrix newModelview, Matrix projection, Rectangle viewport)
    {
        ArrayList<ScreenExtent> adjustedScreenExtents = new ArrayList<ScreenExtent>();

        for (ScreenExtent se : screenExtents)
        {
            if (se.getModelReferencePoint() != null && se.getScreenBounds() != null)
            {
                Vec4 sp1 = ViewUtil.project(se.getModelReferencePoint(), oldModelview, projection, viewport);
                Vec4 sp2 = ViewUtil.project(se.getModelReferencePoint(), newModelview, projection, viewport);
                Vec4 d = sp2.subtract3(sp1);

                Rectangle newBounds = new Rectangle(se.getScreenBounds());
                newBounds.translate((int) d.x, (int) d.y);

                adjustedScreenExtents.add(new ScreenExtent(se.getModelReferencePoint(), newBounds));
            }
            else if (se.getModelReferencePoint() != null)
            {
                adjustedScreenExtents.add(se);
            }
        }

        return adjustedScreenExtents;
    }
}
