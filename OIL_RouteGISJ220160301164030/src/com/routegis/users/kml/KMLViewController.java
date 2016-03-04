

package com.routegis.users.kml;

import java.util.*;

import core.routegis.engine.*;
import core.routegis.engine.geom.*;
import core.routegis.engine.globes.Globe;
import core.routegis.engine.ogc.kml.*;
import core.routegis.engine.ogc.kml.impl.KMLUtil;
import core.routegis.engine.util.Logging;
import core.routegis.engine.view.firstperson.BasicFlyView;
import core.routegis.engine.view.orbit.OrbitView;


public abstract class KMLViewController
{
    
    public static final double DEFAULT_VIEW_ALTITUDE = 10000;

    
    protected double viewAltitude = DEFAULT_VIEW_ALTITUDE;

    
    protected WorldWindow wwd;

    
    public static KMLViewController create(WorldWindow wwd)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        View view = wwd.getView();

        if (view instanceof OrbitView)
            return new KMLOrbitViewController(wwd);
        else if (view instanceof BasicFlyView)
            return new KMLFlyViewController(wwd);
        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnrecognizedView", view));
            return null; // Unknown view
        }
    }

    
    protected KMLViewController(WorldWindow wwd)
    {
        this.wwd = wwd;
    }

    
    public void goTo(KMLAbstractFeature feature)
    {
        if (feature == null)
        {
            String message = Logging.getMessage("nullValue.FeatureIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        // First look for a KML view in the feature
        KMLAbstractView kmlView = feature.getView();
        if (kmlView instanceof KMLLookAt)
            this.goTo((KMLLookAt) kmlView);
        else if (kmlView instanceof KMLCamera)
            this.goTo((KMLCamera) kmlView);
        else
            this.goToDefaultView(feature);
    }

    
    public void goTo(KMLAbstractView view)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (view instanceof KMLLookAt)
            this.goTo((KMLLookAt) view);
        else if (view instanceof KMLCamera)
            this.goTo((KMLCamera) view);
        else
            Logging.logger().warning(Logging.getMessage("generic.UnrecognizedView", view));
    }

    
    protected abstract void goTo(KMLLookAt lookAt);

    
    protected abstract void goTo(KMLCamera camera);

    
    public void goToDefaultView(KMLAbstractFeature feature)
    {
        if (feature instanceof KMLPlacemark)
        {
            this.goToDefaultPlacemarkView((KMLPlacemark) feature);
        }
        else if (feature instanceof KMLGroundOverlay)
        {
            this.goToDefaultGroundOverlayView((KMLGroundOverlay) feature);
        }
    }

    
    protected void goToDefaultPlacemarkView(KMLPlacemark placemark)
    {
        View view = this.wwd.getView();
        List<Position> positions = new ArrayList<Position>();

        // Find all the points in the placemark. We want to bring the entire placemark into view.
        KMLAbstractGeometry geometry = placemark.getGeometry();
        KMLUtil.getPositions(view.getGlobe(), geometry, positions);

        this.goToDefaultView(positions);
    }

    
    protected void goToDefaultGroundOverlayView(KMLGroundOverlay overlay)
    {
        // Positions are specified either as a kml:LatLonBox or a gx:LatLonQuad
        List<? extends Position> corners = overlay.getPositions().list;

        String altitudeMode = overlay.getAltitudeMode() != null ? overlay.getAltitudeMode() : "clampToGround";
        corners = KMLUtil.computeAltitude(this.wwd.getModel().getGlobe(), corners, altitudeMode);

        this.goToDefaultView(corners);
    }

    
    protected void goToDefaultView(List<? extends Position> positions)
    {
        View view = this.wwd.getView();

        // If there is only one point, move the view over that point, maintaining the current elevation.
        if (positions.size() == 1) // Only one point
        {
            Position pos = positions.get(0);
            view.goTo(pos, pos.getAltitude() + this.getViewAltitude());
        }
        else if (positions.size() > 1)// Many points
        {
            // Compute the sector that bounds all of the points in the list. Move the view so that this entire
            // sector is visible.
            Sector sector = Sector.boundingSector(positions);
            Globe globe = view.getGlobe();
            double ve = this.wwd.getSceneController().getVerticalExaggeration();

            // Find the highest point in the geometry. Make sure that our bounding cylinder encloses this point.
            double maxAltitude = this.findMaxAltitude(positions);

            double[] minAndMaxElevations = globe.getMinAndMaxElevations(sector);
            double minElevation = minAndMaxElevations[0];
            double maxElevation = Math.max(minAndMaxElevations[1], maxAltitude);

            Extent extent = Sector.computeBoundingCylinder(globe, ve, sector, minElevation, maxElevation);
            if (extent == null)
            {
                String message = Logging.getMessage("nullValue.SectorIsNull");
                Logging.logger().warning(message);
                return;
            }
            Angle fov = view.getFieldOfView();

            Position centerPos = new Position(sector.getCentroid(), maxAltitude);
            double zoom = extent.getRadius() / (fov.tanHalfAngle() * fov.cosHalfAngle()) + this.getViewAltitude();

            view.goTo(centerPos, zoom);
        }
    }

    
    protected double findMaxAltitude(List<? extends Position> positions)
    {
        double maxAltitude = -Double.MAX_VALUE;
        for (Position p : positions)
        {
            double altitude = p.getAltitude();
            if (altitude > maxAltitude)
                maxAltitude = altitude;
        }

        return maxAltitude;
    }

    
    public double getViewAltitude()
    {
        return this.viewAltitude;
    }

    
    public void setViewAltitude(double viewAltitude)
    {
        this.viewAltitude = viewAltitude;
    }
}
