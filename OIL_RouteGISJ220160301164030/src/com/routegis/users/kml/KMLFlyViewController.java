

package com.routegis.users.kml;

import core.routegis.engine.*;
import core.routegis.engine.animation.*;
import core.routegis.engine.geom.*;
import core.routegis.engine.ogc.kml.*;
import core.routegis.engine.ogc.kml.impl.KMLUtil;
import core.routegis.engine.view.ViewPropertyAccessor;
import core.routegis.engine.view.firstperson.*;
import core.routegis.engine.view.orbit.*;


public class KMLFlyViewController extends KMLViewController
{
    
    protected final long MIN_LENGTH_MILLIS = 4000;
    
    protected final long MAX_LENGTH_MILLIS = 16000;

    
    protected BasicFlyView flyView;

    
    protected KMLFlyViewController(WorldWindow wwd)
    {
        super(wwd);
        this.flyView = (BasicFlyView) wwd.getView();
    }

    
    @Override
    protected void goTo(KMLLookAt lookAt)
    {
        double latitude = lookAt.getLatitude() != null ? lookAt.getLatitude() : 0.0;
        double longitude = lookAt.getLongitude() != null ? lookAt.getLongitude() : 0.0;
        double altitude = lookAt.getAltitude() != null ? lookAt.getAltitude() : 0.0;
        double heading = lookAt.getHeading() != null ? lookAt.getHeading() : 0.0;
        double tilt = lookAt.getTilt() != null ? lookAt.getTilt() : 0.0;
        double range = lookAt.getRange();

        String altitudeMode = lookAt.getAltitudeMode();

        Position lookAtPosition = Position.fromDegrees(latitude, longitude, altitude);

        // If the current view has a center position on the globe, use this center to compute animation time. If not,
        // just use current eye position.
        Position currentCenterPosition;
        Vec4 centerPoint = this.flyView.getCenterPoint();
        if (centerPoint != null)
            currentCenterPosition = this.flyView.getGlobe().computePositionFromPoint(centerPoint);
        else
            currentCenterPosition = this.flyView.getCurrentEyePosition();

        long timeToMove = AnimationSupport.getScaledTimeMillisecs(
            currentCenterPosition, lookAtPosition,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);

        Animator animator = createFlyToLookAtAnimator(this.flyView, lookAtPosition,
            Angle.fromDegrees(heading), Angle.fromDegrees(tilt), range,
            timeToMove, KMLUtil.convertAltitudeMode(altitudeMode, MainClass.CLAMP_TO_GROUND)); // KML default

        FlyViewInputHandler inputHandler = (FlyViewInputHandler) this.flyView.getViewInputHandler();
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
            this.flyView.getEyePosition(), cameraPosition,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);

        Animator animator = FlyToFlyViewAnimator.createFlyToFlyViewAnimator(this.flyView,
            this.flyView.getEyePosition(), cameraPosition,
            this.flyView.getHeading(), Angle.fromDegrees(heading),
            this.flyView.getPitch(), Angle.fromDegrees(tilt),
            this.flyView.getRoll(), Angle.fromDegrees(roll),
            this.flyView.getEyePosition().getElevation(), cameraPosition.getElevation(),
            timeToMove, KMLUtil.convertAltitudeMode(altitudeMode, MainClass.RELATIVE_TO_GROUND)); // Camera default, differs from KML default

        FlyViewInputHandler inputHandler = (FlyViewInputHandler) this.flyView.getViewInputHandler();
        inputHandler.stopAnimators();
        inputHandler.addAnimator(animator);
    }

    
    protected FlyToFlyViewAnimator createFlyToLookAtAnimator(BasicFlyView view, Position lookAtPosition,
        Angle heading, Angle pitch, double range, long timeToMove, int altitudeMode)
    {
        // Create a BasicOrbitView to let the OrbitView do the math of finding the eye position based on the LookAt
        // position. The OrbitView is never set to be the active view, but it will be used by the animator to
        // iteratively re-calcuate the final eye position.
        BasicOrbitView targetView = new BasicOrbitView();
        targetView.setGlobe(view.getGlobe());

        targetView.setCenterPosition(lookAtPosition);
        targetView.setHeading(heading);
        targetView.setPitch(pitch);
        targetView.setZoom(range);

        Position beginEyePosition = view.getCurrentEyePosition();
        Position endEyePosition = targetView.getCurrentEyePosition();

        FlyToFlyViewAnimator.OnSurfacePositionAnimator centerAnimator
            = new FlyToFlyViewAnimator.OnSurfacePositionAnimator(
            view.getGlobe(), new ScheduledInterpolator(timeToMove),
            beginEyePosition, endEyePosition,
            ViewPropertyAccessor.createEyePositionAccessor(view), altitudeMode);

        // Create the elevation animator with altitude mode ABSOLUTE because the FlyToLookAtAnimator computes and sets
        // the end eye position based on the LookAt position, and applies the altitude mode in that calculation.
        FlyToFlyViewAnimator.FlyToElevationAnimator elevAnimator = new FlyToFlyViewAnimator.FlyToElevationAnimator(view,
            view.getGlobe(), beginEyePosition.getElevation(), endEyePosition.getElevation(), beginEyePosition,
            endEyePosition, MainClass.ABSOLUTE, ViewPropertyAccessor.createElevationAccessor(view));

        AngleAnimator headingAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            view.getHeading(), heading,
            ViewPropertyAccessor.createHeadingAccessor(view));

        AngleAnimator pitchAnimator = new AngleAnimator(
            new ScheduledInterpolator(timeToMove),
            view.getPitch(), pitch,
            ViewPropertyAccessor.createPitchAccessor(view));

        return new FlyToLookAtAnimator(new ScheduledInterpolator(timeToMove), targetView, lookAtPosition,
            altitudeMode, centerAnimator, elevAnimator, headingAnimator, pitchAnimator, null);
    }

    
    protected class FlyToLookAtAnimator extends FlyToFlyViewAnimator
    {
        protected int altitudeMode;
        protected OrbitView targetView;
        protected Position lookAtPosition;

        protected PositionAnimator eyePositionAnimator;
        protected DoubleAnimator elevationAnimator;

        
        public FlyToLookAtAnimator(Interpolator interpolator, OrbitView targetView, Position lookAtPosition,
            int altitudeMode, PositionAnimator eyePositionAnimator, DoubleAnimator elevationAnimator,
            AngleAnimator headingAnimator, AngleAnimator pitchAnimator, AngleAnimator rollAnimator)
        {
            super(interpolator, altitudeMode, eyePositionAnimator, elevationAnimator, headingAnimator, pitchAnimator,
                rollAnimator);

            this.targetView = targetView;
            this.altitudeMode = altitudeMode;
            this.lookAtPosition = lookAtPosition;
            this.eyePositionAnimator = eyePositionAnimator;
            this.elevationAnimator = elevationAnimator;
        }

        
        @Override
        protected void setImpl(double interpolant)
        {
            // Re-compute the end eye position based on the LookAt position if the LookAt position depends on the surface
            // elevation
            double lookAtElevation = 0.0;
            boolean overrideEndElevation = false;

            if (this.altitudeMode == MainClass.CLAMP_TO_GROUND)
            {
                overrideEndElevation = true;
                lookAtElevation = this.targetView.getGlobe().getElevation(this.lookAtPosition.getLatitude(),
                    this.lookAtPosition.getLongitude());
            }
            else if (this.altitudeMode == MainClass.RELATIVE_TO_GROUND)
            {
                overrideEndElevation = true;
                lookAtElevation =
                    this.targetView.getGlobe().getElevation(this.lookAtPosition.getLatitude(),
                        this.lookAtPosition.getLongitude()) + this.lookAtPosition.getAltitude();
            }

            if (overrideEndElevation)
            {
                Position centerPosition = new Position(this.lookAtPosition, lookAtElevation);
                this.targetView.setCenterPosition(centerPosition);

                Position endEyePosition = this.targetView.getCurrentEyePosition();

                // Update the end position in the eye position and elevation animators
                this.eyePositionAnimator.setEnd(endEyePosition);
                this.elevationAnimator.setEnd(endEyePosition.getElevation());
            }

            super.setImpl(interpolant);
        }
    }
}
