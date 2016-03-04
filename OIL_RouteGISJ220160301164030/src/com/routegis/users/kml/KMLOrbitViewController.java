

package com.routegis.users.kml;

import core.routegis.engine.*;
import core.routegis.engine.animation.*;
import core.routegis.engine.geom.*;
import core.routegis.engine.globes.Globe;
import core.routegis.engine.ogc.kml.*;
import core.routegis.engine.ogc.kml.impl.KMLUtil;
import core.routegis.engine.util.PropertyAccessor;
import core.routegis.engine.view.*;
import core.routegis.engine.view.firstperson.BasicFlyView;
import core.routegis.engine.view.orbit.*;


public class KMLOrbitViewController extends KMLViewController
{
    
    protected final long MIN_LENGTH_MILLIS = 4000;
    
    protected final long MAX_LENGTH_MILLIS = 16000;

    
    protected OrbitView orbitView;

    
    protected KMLOrbitViewController(WorldWindow wwd)
    {
        super(wwd);
        this.orbitView = (OrbitView) wwd.getView();
    }

    
    @Override
    protected void goTo(KMLLookAt lookAt)
    {
        double latitude = lookAt.getLatitude() != null ? lookAt.getLatitude() : 0.0;
        double longitude = lookAt.getLongitude() != null ? lookAt.getLongitude() : 0.0;
        double altitude = lookAt.getAltitude() != null ? lookAt.getAltitude() : 0.0;
        double heading = lookAt.getHeading() != null ? lookAt.getHeading() : 0.0;
        double tilt = lookAt.getTilt() != null ? lookAt.getTilt() : 0.0;
        double range = lookAt.getRange() != null ? lookAt.getRange() : 0.0;

        String altitudeMode = lookAt.getAltitudeMode();

        Position lookAtPosition = Position.fromDegrees(latitude, longitude, altitude);

        long timeToMove = AnimationSupport.getScaledTimeMillisecs(
            this.orbitView.getCenterPosition(), lookAtPosition,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);

        FlyToOrbitViewAnimator animator = FlyToOrbitViewAnimator.createFlyToOrbitViewAnimator(this.orbitView,
            this.orbitView.getCenterPosition(), lookAtPosition, this.orbitView.getHeading(),
            Angle.fromDegrees(heading), this.orbitView.getPitch(), Angle.fromDegrees(tilt),
            this.orbitView.getZoom(), range, timeToMove, KMLUtil.convertAltitudeMode(altitudeMode, MainClass.CLAMP_TO_GROUND)); // KML default

        OrbitViewInputHandler inputHandler = (OrbitViewInputHandler) this.orbitView.getViewInputHandler();
        inputHandler.stopAnimators();
        inputHandler.addAnimator(animator);
    }

    
    @Override
    protected void goTo(KMLCamera camera)
    {
        double latitude = camera.getLatitude() != null ? camera.getLatitude() : 0.0;
        double longitude = camera.getLongitude() != null ? camera.getLongitude() : 0.0;
        double altitude = camera.getAltitude() != null ? camera.getAltitude() : 0.0;
        double heading = camera.getHeading() != null ? camera.getHeading() : 0.0;
        double tilt = camera.getTilt() != null ? camera.getTilt() : 0.0;
        double roll = camera.getRoll() != null ? camera.getRoll() : 0.0;

        // Roll in WWJ is opposite to KML, so change the sign of roll.
        roll = -roll;        

        String altitudeMode = camera.getAltitudeMode();

        Position cameraPosition = Position.fromDegrees(latitude, longitude, altitude);

        long timeToMove = AnimationSupport.getScaledTimeMillisecs(
            this.orbitView.getEyePosition(), cameraPosition,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);

        FlyToOrbitViewAnimator panAnimator = createFlyToOrbitViewAnimator(this.orbitView, cameraPosition,
            Angle.fromDegrees(heading), Angle.fromDegrees(tilt), Angle.fromDegrees(roll), timeToMove,
            KMLUtil.convertAltitudeMode(altitudeMode, MainClass.RELATIVE_TO_GROUND)); // Camera default, differs from KML default

        OrbitViewInputHandler inputHandler = (OrbitViewInputHandler) this.orbitView.getViewInputHandler();
        inputHandler.stopAnimators();
        inputHandler.addAnimator(panAnimator);
    }

    
    protected FlyToOrbitViewAnimator createFlyToOrbitViewAnimator(OrbitView orbitView, Position eyePosition,
        Angle heading, Angle pitch, Angle roll, long timeToMove, int altitudeMode)
    {
        Globe globe = orbitView.getGlobe();

        // Create a FlyView to represent the camera position. We do not actually set this view to be the active view;
        // we just use it to do the math of figuring out the forward vector and eye point and then we throw it away
        // and set the configuration in the active OrbitView.
        BasicFlyView flyView = new BasicFlyView();
        flyView.setGlobe(globe);

        flyView.setEyePosition(eyePosition);
        flyView.setHeading(heading);
        flyView.setPitch(pitch);

        Vec4 eyePoint = globe.computePointFromPosition(eyePosition);
        Vec4 forward = flyView.getCurrentForwardVector();
        Position lookAtPosition = this.computeCenterPosition(eyePosition, forward, pitch, altitudeMode);

        double range = eyePoint.distanceTo3(globe.computePointFromPosition(lookAtPosition));

        EyePositionAnimator centerAnimator = new EyePositionAnimator(new ScheduledInterpolator(timeToMove),
            orbitView.getCenterPosition(), lookAtPosition, eyePosition, forward, pitch,
            OrbitViewPropertyAccessor.createCenterPositionAccessor(orbitView), altitudeMode);

        // Create an elevation animator with ABSOLUTE altitude mode because the OrbitView altitude mode applies to the
        // center position, not the zoom.
        ViewElevationAnimator zoomAnimator = new ViewElevationAnimator(globe,
            orbitView.getZoom(), range, orbitView.getCenterPosition(), eyePosition, MainClass.ABSOLUTE,
            OrbitViewPropertyAccessor.createZoomAccessor(orbitView));

        centerAnimator.useMidZoom = zoomAnimator.getUseMidZoom();

        AngleAnimator headingAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            orbitView.getHeading(), heading,
            ViewPropertyAccessor.createHeadingAccessor(orbitView));

        AngleAnimator pitchAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            orbitView.getPitch(), pitch,
            ViewPropertyAccessor.createPitchAccessor(orbitView));

        AngleAnimator rollAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            orbitView.getRoll(), roll,
            ViewPropertyAccessor.createRollAccessor(orbitView));

        return new FlyToOrbitViewAnimator(orbitView, new ScheduledInterpolator(timeToMove), altitudeMode,
            centerAnimator, zoomAnimator, headingAnimator, pitchAnimator, rollAnimator);
    }

    
    protected Position computeCenterPosition(Position eyePosition, Vec4 forward, Angle pitch, int altitudeMode)
    {
        double height;
        Angle latitude = eyePosition.getLatitude();
        Angle longitude = eyePosition.getLongitude();
        Globe globe = this.wwd.getModel().getGlobe();

        if (altitudeMode == MainClass.CLAMP_TO_GROUND)
            height = globe.getElevation(latitude, longitude);
        else if (altitudeMode == MainClass.RELATIVE_TO_GROUND)
            height = globe.getElevation(latitude, longitude) + eyePosition.getAltitude();
        else
            height = eyePosition.getAltitude();

        Vec4 eyePoint = globe.computePointFromPosition(new Position(latitude, longitude, height));

        // Find the intersection of the globe and the camera's forward vector. Looking at the horizon (tilt == 90)
        // is a special case because it is a valid view, but the view vector does not intersect the globe.
        Position lookAtPosition;
        final double tolerance = 0.001;
        if (Math.abs(pitch.degrees - 90.0) > tolerance)
            lookAtPosition = globe.getIntersectionPosition(new Line(eyePoint, forward));
        else // Use the camera position as the center position when looking at the horizon.
            lookAtPosition = globe.computePositionFromPoint(eyePoint);

        return lookAtPosition;
    }

    
    protected class EyePositionAnimator extends PositionAnimator
    {
        protected Position endEyePosition;
        protected int eyeAltitudeMode;
        protected boolean useMidZoom = true;
        protected Vec4 forward;
        protected Angle pitch;

        
        public EyePositionAnimator(Interpolator interpolator, Position beginCenter, Position endCenter,
            Position endEyePosition, Vec4 forward, Angle pitch, PropertyAccessor.PositionAccessor propertyAccessor,
            int altitudeMode)
        {
            super(interpolator, beginCenter, endCenter, propertyAccessor);

            this.forward = forward;
            this.pitch = pitch;
            this.endEyePosition = endEyePosition;
            this.eyeAltitudeMode = altitudeMode;
        }

        
        @Override
        protected Position nextPosition(double interpolant)
        {
            // Re-compute the center position if the center depends on surface elevation.

            final int MAX_SMOOTHING = 1;

            final double CENTER_START = this.useMidZoom ? 0.2 : 0.0;
            final double CENTER_STOP = this.useMidZoom ? 0.8 : 0.8;
            double latLonInterpolant = AnimationSupport.basicInterpolant(interpolant, CENTER_START, CENTER_STOP,
                MAX_SMOOTHING);

            // Invoke the standard next position functionality.
            Position pos = super.nextPosition(latLonInterpolant);

            // Evaluate altitude mode, if necessary
            if (this.eyeAltitudeMode == MainClass.CLAMP_TO_GROUND
                || this.eyeAltitudeMode == MainClass.RELATIVE_TO_GROUND)
            {
                Position endPos = computeCenterPosition(this.endEyePosition, this.forward, this.pitch,
                    this.eyeAltitudeMode);

                LatLon ll = pos; // Use interpolated lat/lon.
                double e1 = getBegin().getElevation();
                pos = new Position(ll, (1 - latLonInterpolant) * e1 + latLonInterpolant * endPos.getElevation());
            }

            return pos;
        }
    }
}
