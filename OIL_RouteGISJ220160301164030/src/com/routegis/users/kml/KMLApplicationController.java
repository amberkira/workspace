

package com.routegis.users.kml;

import java.awt.*;
import java.beans.*;

import com.routegis.users.util.BalloonController;

import core.routegis.engine.WorldWindow;
import core.routegis.engine.avlist.AVKey;
import core.routegis.engine.event.*;
import core.routegis.engine.ogc.kml.*;
import core.routegis.engine.util.Logging;
import core.routegis.engine.util.tree.TreeNode;


public class KMLApplicationController implements SelectListener, PropertyChangeListener
{
    
    protected WorldWindow wwd;
    
    protected TreeNode highlightedNode;
    
    protected BalloonController balloonController;

    
    public KMLApplicationController(WorldWindow wwd)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.wwd = wwd;
        this.wwd.addSelectListener(this);
        this.wwd.getSceneController().addPropertyChangeListener(this);
    }

    
    public BalloonController getBalloonController()
    {
        return this.balloonController;
    }

    
    public void setBalloonController(BalloonController balloonController)
    {
        this.balloonController = balloonController;
    }

    
    public void selected(SelectEvent event)
    {
        if (event.isLeftClick())
        {
            Object topObject = event.getTopObject();
            if (topObject instanceof TreeNode)
            {
                // The KML feature should be attached to the node as the CONTEXT
                Object context = ((TreeNode) topObject).getValue(AVKey.CONTEXT);
                if (context instanceof KMLAbstractFeature)
                {
                    this.onFeatureSelected((KMLAbstractFeature) context);
                }
            }
        }
        else if (event.isRollover())
        {
            Object topObject = event.getTopObject();

            if (this.highlightedNode == topObject)
            {
                return; // Same thing selected
            }

            if (this.highlightedNode != null) // Something different selected
            {
                this.highlightedNode = null;
                this.setCursor(null); // Reset to default
            }

            if (topObject instanceof TreeNode)
            {
                // The KML feature should be attached to the node as the CONTEXT
                TreeNode treeNode = (TreeNode) topObject;

                Object context = treeNode.getValue(AVKey.CONTEXT);
                if (context instanceof KMLAbstractFeature && this.canSelect((KMLAbstractFeature) context))
                {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    this.highlightedNode = treeNode;
                }
            }
        }
    }

    
    public void propertyChange(PropertyChangeEvent event)
    {
        try
        {
            if (AVKey.RETRIEVAL_STATE_SUCCESSFUL.equals(event.getPropertyName())
                && event.getNewValue() instanceof KMLNetworkLink)
            {
                this.onNetworkLinkRefreshed((KMLNetworkLink) event.getNewValue());
            }
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up.
            Logging.logger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    
    protected void onNetworkLinkRefreshed(KMLNetworkLink networkLink)
    {
        if (networkLink == null)
            return;

        KMLRoot kmlRoot = networkLink.getNetworkResource();
        if (kmlRoot == null)
            return;

        if (Boolean.TRUE.equals(networkLink.getFlyToView()))
        {
            if (kmlRoot.getNetworkLinkControl() != null
                && kmlRoot.getNetworkLinkControl().getView() != null)
            {
                this.moveTo(kmlRoot.getNetworkLinkControl().getView());
                this.wwd.redraw();
            }
            else if (kmlRoot.getFeature() != null
                && kmlRoot.getFeature().getView() != null)
            {
                this.moveTo(kmlRoot.getFeature().getView());
                this.wwd.redraw();
            }
        }
    }

    
    protected void onFeatureSelected(KMLAbstractFeature feature)
    {
        this.moveTo(feature);

        if (this.balloonController != null)
            this.balloonController.showBalloon(feature);
    }

    
    protected void moveTo(KMLAbstractFeature feature)
    {
        KMLViewController viewController = KMLViewController.create(this.wwd);
        if (viewController == null)
            return;

        viewController.goTo(feature);
    }

    
    protected void moveTo(KMLAbstractView view)
    {
        KMLViewController viewController = KMLViewController.create(this.wwd);
        if (viewController == null)
            return;

        viewController.goTo(view);
    }

    
    protected void setCursor(Cursor cursor)
    {
        if (this.wwd instanceof Component)
            ((Component) this.wwd).setCursor(cursor);
    }

    
    protected boolean canSelect(KMLAbstractFeature feature)
    {
        return this.canMoveTo(feature) || this.canShowBalloon(feature);
    }

    
    protected boolean canMoveTo(KMLAbstractFeature feature)
    {
        return (feature.getView() != null) || feature instanceof KMLPlacemark || feature instanceof KMLGroundOverlay;
    }

    
    protected boolean canShowBalloon(KMLAbstractFeature feature)
    {
        BalloonController balloonController = this.getBalloonController();
        return balloonController != null && balloonController.canShowBalloon(feature);
    }
}
