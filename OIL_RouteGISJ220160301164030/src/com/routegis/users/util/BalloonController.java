

package com.routegis.users.util;

import javax.swing.*;

import com.routegis.users.kml.KMLViewController;

import core.routegis.engine.*;
import core.routegis.engine.avlist.*;
import core.routegis.engine.event.*;
import core.routegis.engine.exception.PrivateTimeoutException;
import core.routegis.engine.geom.*;
import core.routegis.engine.layers.RenderableLayer;
import core.routegis.engine.ogc.kml.*;
import core.routegis.engine.ogc.kml.impl.*;
import core.routegis.engine.pick.*;
import core.routegis.engine.render.*;
import core.routegis.engine.terrain.Terrain;
import core.routegis.engine.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.Timer;


public class BalloonController extends MouseAdapter implements SelectListener
{
    
    public static final int DEFAULT_BALLOON_OFFSET = 60;

    public static final String FLY_TO = "flyto";
    public static final String BALLOON = "balloon";
    public static final String BALLOON_FLY_TO = "balloonFlyto";

    protected WorldWindow wwd;

    protected Object lastSelectedObject;
    protected Balloon balloon;

    
    protected int balloonOffset = DEFAULT_BALLOON_OFFSET;

    
    protected long retrievalTimeout = 30 * 1000; // 30 seconds
    
    protected long retrievalPollInterval = 1000; // 1 second

    
    protected BalloonResizeController resizeController;

    
    public BalloonController(WorldWindow wwd)
    {
        if (wwd == null)
        {
            String message = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.wwd = wwd;
        this.wwd.addSelectListener(this);
        this.wwd.getInputHandler().addMouseListener(this);
        this.wwd.getInputHandler().addMouseMotionListener(this);
    }

    
    public int getBalloonOffset()
    {
        return this.balloonOffset;
    }

    
    public void setBalloonOffset(int balloonOffset)
    {
        this.balloonOffset = balloonOffset;
    }

    //
    //
    //

    
    @Override
    public void mouseClicked(MouseEvent e)
    {
        if (e == null || e.isConsumed())
            return;

        // Implementation note: handle the balloon with a mouse listener instead of a select listener so that the balloon
        // can be turned off if the user clicks on the terrain.
        try
        {
            if (this.isBalloonTrigger(e))
            {
                PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();
                if (pickedObjects == null || pickedObjects.getTopPickedObject() == null)
                {
                    this.hideBalloon();
                    return;
                }

                Object topObject = pickedObjects.getTopObject();
                PickedObject topPickedObject = pickedObjects.getTopPickedObject();

                boolean sameObjectSelected = this.lastSelectedObject == topObject || this.balloon == topObject;
                boolean balloonVisible = this.balloon != null && this.balloon.isVisible();

                // Do nothing if the same thing is selected and the balloon is already visible.
                if (sameObjectSelected && balloonVisible)
                    return;

                // Hide the active balloon if the selection has changed, or if terrain was selected.
                if (this.balloon != null && !(topObject instanceof Balloon))
                {
                    this.hideBalloon(); // Something else selected
                }

                Balloon balloon = this.getBalloon(topPickedObject);

                // Don't change balloons that are already visible
                if (balloon != null && !balloon.isVisible())
                {
                    this.lastSelectedObject = topObject;
                    this.showBalloon(balloon, topObject, e.getPoint());
                }
            }
        }
        catch (Exception ex)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Logging.logger().warning(ex.getMessage() != null ? ex.getMessage() : ex.toString());
        }
    }

    @Override
    public void mouseMoved(MouseEvent e)
    {
        if (e == null || e.isConsumed())
            return;

        PickedObjectList list = this.wwd.getObjectsAtCurrentPosition();
        PickedObject pickedObject = list != null ? list.getTopPickedObject() : null;

        // Handle balloon resize events. Create a resize controller when the mouse enters the resize area.
        // While the mouse is in the resize area, the resize controller will handle select events to resize the
        // balloon. The controller will be destroyed when the mouse exists the resize area.
        if (pickedObject != null && this.isResizeControl(pickedObject))
        {
            this.createResizeController((Balloon) pickedObject.getObject());
        }
        else if (this.resizeController != null && !this.resizeController.isResizing())
        {
            // Destroy the resize controller if the mouse is out of the resize area and the controller
            // is not resizing the balloon. The mouse is allowed to move out of the resize area during the resize
            // operation. If this event is a drag end, check the top object at the current position to determine if
            // the cursor is still over the resize area.

            this.destroyResizeController(null);
        }
    }

    public void selected(SelectEvent event)
    {
        if (event == null || event.isConsumed()
            || (event.getMouseEvent() != null && event.getMouseEvent().isConsumed()))
        {
            return;
        }

        try
        {
            PickedObject pickedObject = event.getTopPickedObject();
            if (pickedObject == null)
                return;
            Object topObject = event.getTopObject();

            // Destroy the resize controller the event is a drag end and the mouse is out of the resize area, and the
            // controller is not resizing the balloon. The mouse is allowed to move out of the resize area during the
            // resize operation.
            if (event.isDragEnd() && this.resizeController != null && !this.resizeController.isResizing())
            {
                PickedObject po;
                PickedObjectList list = this.wwd.getObjectsAtCurrentPosition();
                po = list != null ? list.getTopPickedObject() : null;

                if (!this.isResizeControl(po))
                {
                    this.destroyResizeController(event);
                }
            }

            // Check to see if the event is a link activation or other balloon event
            if (event.isLeftClick())
            {
                String url = this.getUrl(pickedObject);
                if (url != null)
                {
                    this.onLinkActivated(event, url);
                }
                else if (pickedObject.hasKey(AVKey.ACTION) && topObject instanceof AbstractBrowserBalloon)
                {
                    this.onBalloonAction((AbstractBrowserBalloon) topObject, pickedObject.getStringValue(AVKey.ACTION));
                }
            }
            else if (event.isLeftDoubleClick())
            {
                // Call onLinkActivated for left double click even though we don't want to follow links when these
                // events occur. onLinkActivated determines if the URL is something that the controller should handle,
                // and consume the event if so. onLinkActivated does not perform the associated link action unless the
                // event is a left click. If we don't consume the event, the balloon may take action when a left press
                // event occurs on a link that the balloon controller will handle (for example, a link to a KML file.)
                // We avoid consuming left press events, since doing so prevents the WorldWindow from gaining focus.
                String url = this.getUrl(pickedObject);
                if (url != null)
                {
                    this.onLinkActivated(event, url);
                }
            }
        }
        catch (Exception e)
        {
            // Wrap the handler in a try/catch to keep exceptions from bubbling up
            Logging.logger().warning(e.getMessage() != null ? e.getMessage() : e.toString());
        }
    }

    protected boolean isResizeControl(PickedObject po)
    {
        return po != null
            && AVKey.RESIZE.equals(po.getStringValue(AVKey.ACTION))
            && po.getObject() instanceof Balloon;
    }

    
    protected String getUrl(PickedObject pickedObject)
    {
        return pickedObject.getStringValue(AVKey.URL);
    }

    
    protected KMLAbstractFeature getContext(PickedObject pickedObject)
    {
        Object topObject = pickedObject.getObject();

        Object context = pickedObject.getValue(AVKey.CONTEXT);

        // If there was no context in the PickedObject, look for it in the top user object.
        if (context == null && topObject instanceof AVList)
        {
            context = ((AVList) topObject).getValue(AVKey.CONTEXT);
        }

        if (context instanceof KMLAbstractFeature)
            return (KMLAbstractFeature) context;
        else
            return null;
    }

    
    protected void onBalloonAction(AbstractBrowserBalloon browserBalloon, String action)
    {
        if (AVKey.CLOSE.equals(action))
        {
            // If the balloon closing is the balloon we manage, call hideBalloon to clean up state.
            // Otherwise just make the balloon invisible.
            if (browserBalloon == this.balloon)
                this.hideBalloon();
            else
                browserBalloon.setVisible(false);
        }
        else if (AVKey.BACK.equals(action))
            browserBalloon.goBack();

        else if (AVKey.FORWARD.equals(action))
            browserBalloon.goForward();
    }

    //
    //
    //

    
    protected void createResizeController(Balloon balloon)
    {
        // If a resize controller is already active, don't start another one.
        if (this.resizeController != null)
            return;

        this.resizeController = new BalloonResizeController(this.wwd, balloon);
    }

    
    protected void destroyResizeController(SelectEvent event)
    {
        if (this.resizeController != null)
        {
            try
            {
                // Pass the last event to the controller so that it can clean up internal state if it needs to.
                if (event != null)
                    this.resizeController.selected(event);

                this.resizeController.detach();
                this.resizeController = null;
            }
            finally
            {
                // Reset the cursor to default. The resize controller may have changed it.
                if (this.wwd instanceof Component)
                {
                    ((Component) this.wwd).setCursor(Cursor.getDefaultCursor());
                }
            }
        }
    }

    //
    //
    //

    
    protected void onLinkActivated(SelectEvent event, String url)
    {
        PickedObject pickedObject = event.getTopPickedObject();
        String type = pickedObject.getStringValue(AVKey.MIME_TYPE);

        // Break URL into base and reference
        String linkBase;
        String linkRef;

        int hashSign = url.indexOf("#");
        if (hashSign != -1)
        {
            linkBase = url.substring(0, hashSign);
            linkRef = url.substring(hashSign);
        }
        else
        {
            linkBase = url;
            linkRef = null;
        }

        KMLRoot targetDoc; // The document to load and/or fly to
        KMLRoot contextDoc = null; // The local KML document that initiated the link
        KMLAbstractFeature kmlFeature;

        boolean isKmlUrl = this.isKmlUrl(linkBase, type);
        boolean foundLocalFeature = false;

        // Look for a KML feature attached to the picked object. If present, the link will be interpreted relative
        // to this feature.
        kmlFeature = this.getContext(pickedObject);
        if (kmlFeature != null)
            contextDoc = kmlFeature.getRoot();

        // If this link is to a KML or KMZ document we will load the document into a new layer.
        if (isKmlUrl)
        {
            targetDoc = this.findOpenKmlDocument(linkBase);
            if (targetDoc == null)
            {
                // Asynchronously request the document if the event is a link activation trigger.
                if (this.isLinkActivationTrigger(event))
                    this.requestDocument(linkBase, contextDoc, linkRef);

                // We are opening a document, consume the event to prevent balloon from trying to load the document.
                event.consume();
                return;
            }
        }
        else
        {
            // URL does not refer to a remote KML document, assume that it refers to a feature in the current doc
            targetDoc = contextDoc;
        }

        // If the link also has a feature reference, we will move to the feature
        if (linkRef != null)
        {
            if (this.onFeatureLinkActivated(targetDoc, linkRef, event))
            {
                foundLocalFeature = true;
                event.consume(); // Consume event if the target feature was found
            }
        }

        // If the link is not to a KML file or feature, and the link targets a new browser window, launch the system web
        // browser. BrowserBalloon ignores link events that target new windows, so we need to handle them here.
        if (!isKmlUrl && !foundLocalFeature)
        {
            String target = pickedObject.getStringValue(AVKey.TARGET);
            if ("_blank".equalsIgnoreCase(target))
            {
                // Invoke the system browser to open the link if the event is link activation trigger.
                if (this.isLinkActivationTrigger(event))
                    this.openInNewBrowser(event, url);
                event.consume();
            }
        }
    }

    
    protected boolean isLinkActivationTrigger(SelectEvent event)
    {
        return event.isLeftClick();
    }

    
    protected void openInNewBrowser(SelectEvent event, String url)
    {
        try
        {
            BrowserOpener.browse(new URL(url));
            event.consume();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToInvokeWebBrower", url);
            Logging.logger().warning(message);
        }
    }

    
    protected boolean onFeatureLinkActivated(KMLRoot doc, String linkFragment, SelectEvent event)
    {
        // Split the reference into the feature id and the display directive (flyto, balloon, etc)
        String[] parts = linkFragment.split(";");
        String featureId = parts[0];
        String directive = parts.length > 1 ? parts[1] : FLY_TO;

        if (!PrivateUtil.isEmpty(featureId) && doc != null)
        {
            Object o = doc.resolveReference(featureId);
            if (o instanceof KMLAbstractFeature)
            {
                // Perform the link action if the event is a link activation event.
                if (event == null || this.isLinkActivationTrigger(event))
                    this.doFeatureLinkActivated((KMLAbstractFeature) o, directive);
                return true;
            }
        }
        return false;
    }

    
    protected void doFeatureLinkActivated(KMLAbstractFeature feature, String directive)
    {
        if (FLY_TO.equals(directive) || BALLOON_FLY_TO.equals(directive))
        {
            this.moveToFeature(feature);
        }

        if (BALLOON.equals(directive) || BALLOON_FLY_TO.equals(directive))
        {
            this.showBalloon(feature);
        }
    }

    
    protected boolean isKmlUrl(String url, String contentType)
    {
        if (PrivateUtil.isEmpty(url))
            return false;

        String suffix = InOut.getSuffix(url);

        return "kml".equalsIgnoreCase(suffix)
            || "kmz".equalsIgnoreCase(suffix)
            || KMLConstants.KML_MIME_TYPE.equals(contentType)
            || KMLConstants.KMZ_MIME_TYPE.equals(contentType);
    }

    
    protected void moveToFeature(KMLAbstractFeature feature)
    {
        KMLViewController viewController = KMLViewController.create(this.wwd);
        viewController.goTo(feature);
    }

    //
    //
    //

    
    public void showBalloon(KMLAbstractFeature feature)
    {
        Balloon balloon = feature.getBalloon();

        // Create a new balloon if the feature does not have one
        if (balloon == null && this.canShowBalloon(feature))
            balloon = this.createBalloon(feature);

        // Don't change balloons that are already visible
        if (balloon != null && !balloon.isVisible())
        {
            this.lastSelectedObject = feature;

            Position pos = this.getBalloonPosition(feature);
            if (pos != null)
            {
                this.hideBalloon(); // Hide previously displayed balloon, if any
                this.showBalloon(balloon, pos);
            }
            else
            {
                // The feature may be attached to the screen, not the globe
                Point point = this.getBalloonPoint(feature);
                if (point != null)
                {
                    this.hideBalloon(); // Hide previously displayed balloon, if any
                    this.showBalloon(balloon, null, point);
                }
                // If the feature is not attached to a particular point, just put it in the middle of the viewport
                else
                {
                    Rectangle viewport = this.wwd.getView().getViewport();

                    Point center = new Point((int) viewport.getCenterX(), (int) viewport.getCenterY());

                    this.hideBalloon();
                    this.showBalloon(balloon, null, center);
                }
            }
        }
    }

    
    public boolean canShowBalloon(KMLAbstractFeature feature)
    {
        KMLBalloonStyle style = (KMLBalloonStyle) feature.getSubStyle(new KMLBalloonStyle(null), KMLConstants.NORMAL);

        boolean isBalloonHidden = "hide".equals(style.getDisplayMode());

        // Determine if the balloon style actually has fields.
        boolean hasBalloonStyle = style.hasStyleFields() && !style.hasField(AVKey.UNRESOLVED);

        // Do not create a balloon if there is no balloon style and the feature has no description.
        return (hasBalloonStyle || !PrivateUtil.isEmpty(feature.getDescription()) || feature.getExtendedData() != null)
            && !isBalloonHidden;
    }

    
    protected boolean isBalloonTrigger(MouseEvent e)
    {
        // Handle only left click
        return (e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() % 2 == 1);
    }

    
    protected Balloon getBalloon(PickedObject pickedObject)
    {
        Object topObject = pickedObject.getObject();
        Object balloonObj = null;

        // Look for a KMLAbstractFeature context. If the top picked object is part of a KML feature, the
        // feature will determine the balloon.
        if (pickedObject.hasKey(AVKey.CONTEXT))
        {
            Object contextObj = pickedObject.getValue(AVKey.CONTEXT);
            if (contextObj instanceof KMLAbstractFeature)
            {
                KMLAbstractFeature feature = (KMLAbstractFeature) contextObj;
                balloonObj = feature.getBalloon();

                // Create a new balloon if the feature does not have one
                if (balloonObj == null && this.canShowBalloon(feature))
                    balloonObj = this.createBalloon(feature);
            }
        }

        // If we didn't find a balloon on the KML feature, look for a balloon in the AVList
        if (balloonObj == null && topObject instanceof AVList)
        {
            AVList avList = (AVList) topObject;
            balloonObj = avList.getValue(AVKey.BALLOON);
        }

        if (balloonObj instanceof Balloon)
            return (Balloon) balloonObj;
        else
            return null;
    }

    
    protected Balloon createBalloon(KMLAbstractFeature feature)
    {
        KMLBalloonStyle balloonStyle = (KMLBalloonStyle) feature.getSubStyle(new KMLBalloonStyle(null),
            KMLConstants.NORMAL);

        String text = balloonStyle.getText();
        if (text == null)
            text = "";

        // Create the balloon based on the features attachment mode and the browser balloon settings. Wrap the balloon
        // in a KMLBalloonImpl to handle balloon style resolution.  
        KMLAbstractBalloon kmlBalloon;
        if (AVKey.GLOBE.equals(this.getAttachmentMode(feature)))
        {
            GlobeBalloon balloon;
            if (this.isUseBrowserBalloon())
                balloon = new GlobeBrowserBalloon(text, Position.ZERO); // 0 is dummy position
            else
                balloon = new GlobeAnnotationBalloon(text, Position.ZERO); // 0 is dummy position

            kmlBalloon = new KMLGlobeBalloonImpl(balloon, feature);
        }
        else
        {
            ScreenBalloon balloon;
            if (this.isUseBrowserBalloon())
                balloon = new ScreenBrowserBalloon(text, new Point(0, 0)); // 0,0 is dummy position
            else
                balloon = new ScreenAnnotationBalloon(text, new Point(0, 0)); // 0,0 is dummy position

            kmlBalloon = new KMLScreenBalloonImpl(balloon, feature);
        }

        kmlBalloon.setVisible(false);
        kmlBalloon.setAlwaysOnTop(true);

        // Attach the balloon to the feature
        feature.setBalloon(kmlBalloon);

        this.configureBalloon(kmlBalloon, feature);

        return kmlBalloon;
    }

    
    protected void configureBalloon(Balloon balloon, KMLAbstractFeature feature)
    {
        // Configure the balloon for a container to not have a leader. These balloons will display in the middle of the
        // viewport.
        if (feature instanceof KMLAbstractContainer)
        {
            BalloonAttributes attrs = new BasicBalloonAttributes();

            // Size the balloon to match the size of the content.
            Size size = new Size(Size.NATIVE_DIMENSION, 0.0, null, Size.NATIVE_DIMENSION, 0.0, null);

            // Do not allow the balloon to be auto-sized larger than 80% of the viewport. The user may resize the balloon
            // larger than this size.
            Size maxSize = new Size(Size.EXPLICIT_DIMENSION, 0.8, AVKey.FRACTION,
                Size.EXPLICIT_DIMENSION, 0.8, AVKey.FRACTION);

            attrs.setSize(size);
            attrs.setMaximumSize(maxSize);
            attrs.setOffset(new Offset(0.5, 0.5, AVKey.FRACTION, AVKey.FRACTION));
            attrs.setLeaderShape(AVKey.SHAPE_NONE);
            balloon.setAttributes(attrs);
        }
        else
        {
            BalloonAttributes attrs = new BasicBalloonAttributes();

            // Size the balloon to match the size of the content.
            Size size = new Size(Size.NATIVE_DIMENSION, 0.0, null, Size.NATIVE_DIMENSION, 0.0, null);

            // Do not allow the balloon to be auto-sized larger than 50% of the viewport width, and 40% of the height.
            // The user may resize the balloon larger than this size.
            Size maxSize = new Size(Size.EXPLICIT_DIMENSION, 0.5, AVKey.FRACTION,
                Size.EXPLICIT_DIMENSION, 0.4, AVKey.FRACTION);

            attrs.setSize(size);
            attrs.setMaximumSize(maxSize);
            balloon.setAttributes(attrs);
        }
    }

    
    protected String getAttachmentMode(KMLAbstractFeature feature)
    {
        if (feature instanceof KMLPlacemark || feature instanceof KMLGroundOverlay)
            return AVKey.GLOBE;
        else
            return AVKey.SCREEN;
    }

    
    protected boolean isUseBrowserBalloon()
    {
        return Configuration.isWindowsOS() || Configuration.isMacOS();
    }

    
    protected void showBalloon(Balloon balloon, Object balloonObject, Point point)
    {
        // If the balloon is attached to the screen rather than the globe, move it to the
        // current point. Otherwise move it to the position under the current point.
        if (balloon instanceof ScreenBalloon)
            ((ScreenBalloon) balloon).setScreenLocation(point);
        else if (balloon instanceof GlobeBalloon)
        {
            Position position = this.getBalloonPosition(balloonObject, point);
            if (position != null)
            {
                GlobeBalloon globeBalloon = (GlobeBalloon) balloon;
                globeBalloon.setPosition(position);
                globeBalloon.setAltitudeMode(this.getBalloonAltitudeMode(balloonObject));
            }
        }

        if (this.mustAdjustPosition(balloon))
            this.adjustPosition(balloon, point);

        this.balloon = balloon;
        this.balloon.setVisible(true);
    }

    
    protected void showBalloon(Balloon balloon, Position position)
    {
        Vec4 screenVec4 = this.wwd.getView().project(
            this.wwd.getModel().getGlobe().computePointFromPosition(position));

        Point screenPoint = new Point((int) screenVec4.x,
            (int) (this.wwd.getView().getViewport().height - screenVec4.y));

        // If the balloon is attached to the screen rather than the globe, move it to the
        // current point. Otherwise move it to the position under the current point.
        if (balloon instanceof ScreenBalloon)
        {
            ((ScreenBalloon) balloon).setScreenLocation(screenPoint);
        }
        else
        {
            ((GlobeBalloon) balloon).setPosition(position);
        }

        if (this.mustAdjustPosition(balloon))
            this.adjustPosition(balloon, screenPoint);

        this.balloon = balloon;
        this.balloon.setVisible(true);
    }

    
    protected boolean mustAdjustPosition(Balloon balloon)
    {
        // Look at the balloon leader shape. If there is no leader shape, assume that the balloon itself is positioned
        // over the point of interest, and cannot be moved. Otherwise, assume that the balloon must be adjusted.
        BalloonAttributes attrs = balloon.getAttributes();
        return !(AVKey.SHAPE_NONE.equals(attrs.getLeaderShape()));
    }

    
    protected void adjustPosition(Balloon balloon, Point screenPoint)
    {
        // Create an offset that will ensure that the balloon is visible. This method assumes that the balloon
        // width is less than half of the viewport width, and that the balloon height is less half of the viewport
        // height, the default maximum size applied to balloons created by the controller.

        Rectangle viewport = this.wwd.getView().getViewport();

        double x, y;
        String xUnits, yUnits;

        // If the balloon point is in the right 25% of the viewport, place the balloon to the left.
        xUnits = AVKey.FRACTION;
        if (screenPoint.x > viewport.width * 0.75)
        {
            x = 1.0;
        }
        // If the point is in the left 25% of the viewport, place the balloon to the right.
        else if (screenPoint.x < viewport.width * 0.25)
        {
            x = 0;
        }
        // Otherwise, center the balloon on the point.
        else
        {
            x = 0.5;
        }

        int vertOffset = this.getBalloonOffset();
        y = -vertOffset;

        // If the point is in the top half of the viewport, place the balloon below the point.
        if (screenPoint.y < viewport.height * 0.5)
        {
            yUnits = AVKey.INSET_PIXELS;
        }
        // Otherwise, place the balloon above the point.
        else
        {
            yUnits = AVKey.PIXELS;
        }

        Offset offset = new Offset(x, y, xUnits, yUnits);

        BalloonAttributes attributes = balloon.getAttributes();
        if (attributes == null)
        {
            attributes = new BasicBalloonAttributes();
            balloon.setAttributes(attributes);
        }
        attributes.setOffset(offset);

        BalloonAttributes highlightAttributes = balloon.getHighlightAttributes();
        if (highlightAttributes != null)
            highlightAttributes.setOffset(offset);
    }

    
    protected void hideBalloon()
    {
        if (this.balloon != null)
        {
            this.balloon.setVisible(false);
            this.balloon = null;
        }
        this.lastSelectedObject = null;
    }

    //
    //
    //

    
    protected Position getBalloonPosition(KMLAbstractFeature feature)
    {
        if (feature instanceof KMLPlacemark)
        {
            return this.getBalloonPositionForPlacemark((KMLPlacemark) feature);
        }
        else if (feature instanceof KMLGroundOverlay)
        {
            return this.getBalloonPositionForGroundOverlay(((KMLGroundOverlay) feature));
        }
        return null;
    }

    
    protected Position getBalloonPosition(Object topObject, Point pickPoint)
    {
        Position position = null;

        if (topObject instanceof Locatable)
        {
            position = ((Locatable) topObject).getPosition();
        }
        else if (topObject instanceof AbstractShape)
        {
            position = this.computeIntersection((AbstractShape) topObject, pickPoint);
        }

        // Fall back to a terrain intersection if we still don't have a position.
        if (position == null)
        {
            Line ray = this.wwd.getView().computeRayFromScreenPoint(pickPoint.x, pickPoint.y);
            Intersection[] inter = this.wwd.getSceneController().getDrawContext().getSurfaceGeometry().intersect(ray);
            if (inter != null && inter.length > 0)
            {
                position = this.wwd.getModel().getGlobe().computePositionFromPoint(inter[0].getIntersectionPoint());
            }

            // We still don't have a position, fall back to intersection with the ellipsoid.
            if (position == null)
            {
                position = this.wwd.getView().computePositionFromScreenPoint(pickPoint.x, pickPoint.y);
            }
        }

        return position;
    }

    
    protected int getBalloonAltitudeMode(Object balloonObject)
    {
        // Balloons are often attached to PointPlacemarks, so handle this case specially. The balloon altitude mode
        // needs to match the placemark altitude mode. Shapes do not have this problem because an intersection calculation
        // can place the balloon.
        if (balloonObject instanceof PointPlacemark)
        {
            return ((PointPlacemark) balloonObject).getAltitudeMode();
        }
        return MainClass.ABSOLUTE; // Default to absolute
    }

    
    protected Position computeIntersection(AbstractShape shape, Point screenPoint)
    {
        try
        {
            // Compute the intersection using whatever terrain is available. This calculation does not need to be very
            // precise, it just needs to place the balloon close to the shape.
            Terrain terrain = this.wwd.getSceneController().getDrawContext().getTerrain();

            // Compute a line through the pick point.
            Line line = this.wwd.getView().computeRayFromScreenPoint(screenPoint.x, screenPoint.y);

            // Find the intersection of the line and the shape.
            List<Intersection> intersections = shape.intersect(line, terrain);
            if (intersections != null && !intersections.isEmpty())
                return intersections.get(0).getIntersectionPosition();
        }
        catch (InterruptedException ignored)
        {
            // Do nothing
        }

        return null;
    }

    
    protected Position getBalloonPositionForPlacemark(KMLPlacemark placemark)
    {
        List<Position> positions = new ArrayList<Position>();

        KMLAbstractGeometry geometry = placemark.getGeometry();
        KMLUtil.getPositions(this.wwd.getModel().getGlobe(), geometry, positions);

        return this.getBalloonPosition(positions);
    }

    
    protected Position getBalloonPositionForGroundOverlay(KMLGroundOverlay overlay)
    {
        Position.PositionList positionsList = overlay.getPositions();
        return this.getBalloonPosition(positionsList.list);
    }

    
    protected Position getBalloonPosition(List<? extends Position> positions)
    {
        if (positions.size() == 1) // Only one point, just return the point
        {
            return positions.get(0);
        }
        else if (positions.size() > 1)// Many points, find center point of bounding sector
        {
            Sector sector = Sector.boundingSector(positions);

            return new Position(sector.getCentroid(), this.findMaxAltitude(positions));
        }
        return null;
    }

    
    protected Point getBalloonPoint(KMLAbstractFeature feature)
    {
        if (feature instanceof KMLScreenOverlay)
        {
            return this.getBalloonPointForScreenOverlay((KMLScreenOverlay) feature);
        }
        return null;
    }

    
    protected Point getBalloonPointForScreenOverlay(KMLScreenOverlay overlay)
    {
        KMLVec2 xy = overlay.getScreenXY();

        Offset offset = new Offset(xy.getX(), xy.getY(), KMLUtil.kmlUnitsToWWUnits(xy.getXunits()),
            KMLUtil.kmlUnitsToWWUnits(xy.getYunits()));

        Rectangle viewport = this.wwd.getView().getViewport();
        Point2D point2D = offset.computeOffset(viewport.width, viewport.height, 1d, 1d);

        int y = (int) point2D.getY();
        return new Point((int) point2D.getX(), viewport.height - y);
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

    //
    //
    //

    
    protected KMLRoot findOpenKmlDocument(String url)
    {
        Object o = MainClass.getSessionCache().get(url);
        if (o instanceof KMLRoot)
            return (KMLRoot) o;
        else
            return null;
    }

    
    protected void requestDocument(String url, KMLRoot context, String featureRef)
    {
        Timer docLoader = new Timer("BalloonController document retrieval");

        // Schedule a task that will request the document periodically until the document becomes available or the
        // request timeout is reached.
        docLoader.scheduleAtFixedRate(new DocumentRetrievalTask(url, context, featureRef, this.retrievalTimeout),
            0, this.retrievalPollInterval);
    }

    
    protected void onDocumentLoaded(String url, KMLRoot document, String featureRef)
    {
        // Use the URL as the document's DISPLAY_NAME. This field is used by addDocumentLayer to determine the layer's
        // name.
        document.setField(AVKey.DISPLAY_NAME, url);
        this.addDocumentLayer(document);

        if (featureRef != null)
            this.onFeatureLinkActivated(document, featureRef, null);
    }

    
    protected void onDocumentFailed(String url, Exception e)
    {
        String message = Logging.getMessage("generic.ExceptionWhileReading", url + ": " + e.getMessage());
        Logging.logger().warning(message);
    }

    
    protected void addDocumentLayer(KMLRoot document)
    {
        KMLController controller = new KMLController(document);

        // Load the document into a new layer.
        RenderableLayer kmlLayer = new RenderableLayer();
        kmlLayer.setName((String) document.getField(AVKey.DISPLAY_NAME));
        kmlLayer.addRenderable(controller);

        this.wwd.getModel().getLayers().add(kmlLayer);
    }

    
    protected class DocumentRetrievalTask extends TimerTask
    {
        
        protected String docUrl;
        
        protected KMLRoot context;
        
        protected String featureRef;
        
        protected long timeout;
        
        protected long start;

        
        public DocumentRetrievalTask(String url, KMLRoot context, String featureRef, long timeout)
        {
            this.docUrl = url;
            this.context = context;
            this.featureRef = featureRef;
            this.timeout = timeout;
        }

        
        public void run()
        {
            KMLRoot root = null;

            try
            {
                // If this is the first execution, capture the start time so that we can evaluate the timeout later.
                if (this.start == 0)
                    this.start = System.currentTimeMillis();

                // Check for timeout before doing any work
                if (System.currentTimeMillis() > this.start + this.timeout)
                    throw new PrivateTimeoutException(Logging.getMessage("generic.CannotOpenFile", this.docUrl));

                // If we have a context document, let that doc resolve the reference. Otherwise, request it from the
                // file store.
                Object docSource;
                if (this.context != null)
                    docSource = this.context.resolveReference(this.docUrl);
                else
                    docSource = MainClass.getDataFileStore().requestFile(this.docUrl);

                if (docSource instanceof KMLRoot)
                {
                    root = (KMLRoot) docSource;
                    // Roots returned by resolveReference are already parsed, no need to parse here
                }
                else if (docSource != null)
                {
                    root = KMLRoot.create(docSource);
                    root.parse();
                }

                // If root is non-null we have succeeded in loading the document.
                if (root != null)
                {
                    // Schedule a callback on the EDT to let the BalloonController finish loading the document.
                    final KMLRoot pinnedRoot = root; // Final ref that can be accessed by anonymous class
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        public void run()
                        {
                            BalloonController.this.onDocumentLoaded(docUrl, pinnedRoot, featureRef);
                        }
                    });

                    this.cancel();
                }
            }
            catch (final Exception e)
            {
                // Schedule a callback on the EDT to report the error to the BalloonController
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        BalloonController.this.onDocumentFailed(docUrl, e);
                    }
                });
                this.cancel();
            }
        }
    }
}
