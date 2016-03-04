

package com.routegis.users.util;

import com.jogamp.common.nio.Buffers;

import core.routegis.engine.View;
import core.routegis.engine.geom.*;
import core.routegis.engine.render.*;
import core.routegis.engine.terrain.Terrain;
import core.routegis.engine.util.Logging;

import javax.media.opengl.*;

import java.nio.*;
import java.util.List;


public class DirectedPath extends Path
{
    
    public static final double DEFAULT_ARROW_LENGTH = 300;
    
    public static final Angle DEFAULT_ARROW_ANGLE = Angle.fromDegrees(45.0);
    
    public static final double DEFAULT_MAX_SCREEN_SIZE = 20.0;

    
    protected double arrowLength = DEFAULT_ARROW_LENGTH;
    
    protected Angle arrowAngle = DEFAULT_ARROW_ANGLE;
    
    protected double maxScreenSize = DEFAULT_MAX_SCREEN_SIZE;

    
    public DirectedPath()
    {
        super();
    }

    
    public DirectedPath(Iterable<? extends Position> positions)
    {
        super(positions);
    }

    
    public DirectedPath(Position.PositionList positions)
    {
        super(positions.list);
    }

    
    public DirectedPath(Position posA, Position posB)
    {
        super(posA, posB);
    }

    
    public double getArrowLength()
    {
        return this.arrowLength;
    }

    
    public void setArrowLength(double arrowLength)
    {
        if (arrowLength <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", arrowLength);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.arrowLength = arrowLength;
    }

    
    public double getMaxScreenSize()
    {
        return this.maxScreenSize;
    }

    
    public void setMaxScreenSize(double maxScreenSize)
    {
        if (maxScreenSize <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", maxScreenSize);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.maxScreenSize = maxScreenSize;
    }

    
    public Angle getArrowAngle()
    {
        return this.arrowAngle;
    }

    
    public void setArrowAngle(Angle arrowAngle)
    {
        if (arrowAngle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull", arrowAngle);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if ((arrowAngle.compareTo(Angle.ZERO) <= 0) || (arrowAngle.compareTo(Angle.POS90) >= 0))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", arrowAngle);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.arrowAngle = arrowAngle;
    }

    protected static final String ARROWS_KEY = "DirectedPath.DirectionArrows";

    
    @Override
    protected void computePath(DrawContext dc, List<Position> positions, PathData pathData)
    {
        super.computePath(dc, positions, pathData);
//        this.computeDirectionArrows(dc, pathData);
    }

    
    protected void computeDirectionArrows(DrawContext dc, PathData pathData)
    {
        IntBuffer polePositions = pathData.getPolePositions();
        int numPositions = polePositions.limit() / 2; // One arrow head for each path segment
        List<Position> tessellatedPositions = pathData.getTessellatedPositions();

        final int FLOATS_PER_ARROWHEAD = 9; // 3 points * 3 coordinates per point
        FloatBuffer buffer = (FloatBuffer) pathData.getValue(ARROWS_KEY);
        if (buffer == null || buffer.capacity() < numPositions * FLOATS_PER_ARROWHEAD)
            buffer = Buffers.newDirectFloatBuffer(FLOATS_PER_ARROWHEAD * numPositions);
        pathData.setValue(ARROWS_KEY, buffer);

        buffer.clear();

        Terrain terrain = dc.getTerrain();

        double arrowBase = this.getArrowLength() * this.getArrowAngle().tanHalfAngle();

        // Step through polePositions to find the original path locations.
        int thisPole = polePositions.get(0) / 2;
        Position poleA = tessellatedPositions.get(thisPole);
        Vec4 polePtA = this.computePoint(terrain, poleA);

        // Draw one arrowhead for each segment in the original position list. The path may be tessellated,
        // so we need to find the tessellated segment halfway between each pair of original positions.
        // polePositions holds indices into the rendered path array of the original vertices. Step through
        // polePositions by 2 because we only care about where the top of the pole is, not the bottom.
        for (int i = 2; i < polePositions.limit(); i += 2)
        {
            // Find the position of this pole and the next pole. Divide by 2 to convert an index in the
            // renderedPath buffer to a index in the tessellatedPositions list.
            int nextPole = polePositions.get(i) / 2;

            Position poleB = tessellatedPositions.get(nextPole);

            Vec4 polePtB = this.computePoint(terrain, poleB);

            // Find the segment that is midway between the two poles.
            int midPoint = (thisPole + nextPole) / 2;

            Position posA = tessellatedPositions.get(midPoint);
            Position posB = tessellatedPositions.get(midPoint + 1);

            Vec4 ptA = this.computePoint(terrain, posA);
            Vec4 ptB = this.computePoint(terrain, posB);

            this.computeArrowheadGeometry(dc, polePtA, polePtB, ptA, ptB, this.getArrowLength(), arrowBase, buffer,
                pathData);

            thisPole = nextPole;
            polePtA = polePtB;
        }
    }

    
    protected void computeArrowheadGeometry(DrawContext dc, Vec4 polePtA, Vec4 polePtB, Vec4 ptA, Vec4 ptB,
        double arrowLength, double arrowBase, FloatBuffer buffer, PathData pathData)
    {
        // Build a triangle to represent the arrowhead. The triangle is built from two vectors, one parallel to the
        // segment, and one perpendicular to it. The plane of the arrowhead will be parallel to the surface.

        double poleDistance = polePtA.distanceTo3(polePtB);

        // Compute parallel component
        Vec4 parallel = ptA.subtract3(ptB);

        Vec4 surfaceNormal = dc.getGlobe().computeSurfaceNormalAtPoint(ptB);

        // Compute perpendicular component
        Vec4 perpendicular = surfaceNormal.cross3(parallel);

        // Compute midpoint of segment
        Vec4 midPoint = ptA.add3(ptB).divide3(2.0);

        if (!this.isArrowheadSmall(dc, midPoint, 1))
        {
            // Compute the size of the arrowhead in pixels to ensure that the arrow does not exceed the maximum
            // screen size.
            View view = dc.getView();
            double midpointDistance = view.getEyePoint().distanceTo3(midPoint);
            double pixelSize = view.computePixelSizeAtDistance(midpointDistance);
            if (arrowLength / pixelSize > this.maxScreenSize)
            {
                arrowLength = this.maxScreenSize * pixelSize;
                arrowBase = arrowLength * this.getArrowAngle().tanHalfAngle();
            }

            // Don't draw an arrowhead if the path segment is smaller than the arrow
            if (poleDistance <= arrowLength)
                return;

            perpendicular = perpendicular.normalize3().multiply3(arrowBase);
            parallel = parallel.normalize3().multiply3(arrowLength);

            // If the distance between the poles is greater than the arrow length, center the arrow on the midpoint.
            // Otherwise position the tip of the arrow at the midpoint. On short segments it looks weird if the
            // tip of the arrow does not fall on the path, but on longer segments it looks better to center the
            // arrow on the segment.
            if (poleDistance > arrowLength)
                midPoint = midPoint.subtract3(parallel.divide3(2.0));

            // Compute geometry of direction arrow
            Vec4 vertex1 = midPoint.add3(parallel).add3(perpendicular);
            Vec4 vertex2 = midPoint.add3(parallel).add3(perpendicular.multiply3(-1.0));

            // Add geometry to the buffer
            Vec4 referencePoint = pathData.getReferencePoint();
            buffer.put((float) (vertex1.x - referencePoint.x));
            buffer.put((float) (vertex1.y - referencePoint.y));
            buffer.put((float) (vertex1.z - referencePoint.z));

            buffer.put((float) (vertex2.x - referencePoint.x));
            buffer.put((float) (vertex2.y - referencePoint.y));
            buffer.put((float) (vertex2.z - referencePoint.z));

            buffer.put((float) (midPoint.x - referencePoint.x));
            buffer.put((float) (midPoint.y - referencePoint.y));
            buffer.put((float) (midPoint.z - referencePoint.z));
        }
    }
//
//    
//    @Override
//    protected boolean mustRegenerateGeometry(DrawContext dc)
//    {
//        // Path never regenerates geometry for absolute altitude mode paths, but the direction arrows in DirectedPath
//        // need to be recomputed because the view may have changed and the size of the arrows needs to be recalculated.
//        if (this.getCurrentPathData().isExpired(dc))
//            return true;
//
//        return super.mustRegenerateGeometry(dc);
//    }

    
    protected boolean isArrowheadSmall(DrawContext dc, Vec4 arrowPt, int numPixels)
    {
        return this.getArrowLength() <= numPixels * dc.getView().computePixelSizeAtDistance(
            dc.getView().getEyePoint().distanceTo3(arrowPt));
    }

    
    @Override
    protected void doDrawOutline(DrawContext dc)
    {
        this.computeDirectionArrows(dc, this.getCurrentPathData());
        this.drawDirectionArrows(dc, this.getCurrentPathData());
        super.doDrawOutline(dc);
    }

    
    protected void drawDirectionArrows(DrawContext dc, PathData pathData)
    {
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        boolean projectionOffsetPushed = false; // keep track for error recovery

        try
        {
            if (this.isSurfacePath())
            {
                // Pull the arrow triangles forward just a bit to ensure they show over the terrain.
                dc.pushProjectionOffest(SURFACE_PATH_DEPTH_OFFSET);
                gl.glDepthMask(false);
                projectionOffsetPushed = true;
            }

            FloatBuffer directionArrows = (FloatBuffer) pathData.getValue(ARROWS_KEY);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, directionArrows.rewind());
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, directionArrows.limit() / 3);
        }
        finally
        {
            if (projectionOffsetPushed)
            {
                dc.popProjectionOffest();
                gl.glDepthMask(true);
            }
        }
    }
}
