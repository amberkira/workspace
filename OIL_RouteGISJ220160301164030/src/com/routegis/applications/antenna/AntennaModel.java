package com.routegis.applications.antenna;

import com.jogamp.common.nio.Buffers;

import core.routegis.engine.*;
import core.routegis.engine.geom.*;
import core.routegis.engine.globes.Globe;
import core.routegis.engine.render.*;
import core.routegis.engine.terrain.Terrain;
import core.routegis.engine.util.Logging;

import javax.media.opengl.*;
import javax.xml.stream.*;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.nio.*;
import java.util.List;


public class AntennaModel extends AbstractShape
{
    public static final int DISPLAY_MODE_FILL = GL2.GL_FILL;
    public static final int DISPLAY_MODE_LINE = GL2.GL_LINE;
    public static final int DISPLAY_MODE_POINT = GL2.GL_POINT;

    protected int nThetaIntervals;
    protected int nPhiIntervals;
    protected PrivateTexture texture;

    protected Interpolator2D interpolator;
    protected Position position = Position.ZERO;
    protected Angle azimuth;
    protected Angle elevationAngle;
    protected double gainOffset = 0;
    protected double gainScale = 1;
    protected int nThetaPoints = 61;
    protected int nPhiPoints = 121; // TODO: gap shows if nPhiPoints -1 does not evenly divide 360

    
    protected static class ShapeData extends AbstractShapeData
    {
        protected FloatBuffer vertices;
        protected FloatBuffer texCoords;
        protected IntBuffer[] indices;
        protected FloatBuffer normals;

        
        public ShapeData(DrawContext dc, AntennaModel shape)
        {
            super(dc, shape.minExpiryTime, shape.maxExpiryTime);
        }
    }

    protected AbstractShapeData createCacheEntry(DrawContext dc)
    {
        return new ShapeData(dc, this);
    }

    
    protected ShapeData getCurrent()
    {
        return (ShapeData) this.getCurrentData();
    }

    public AntennaModel(Interpolator2D interpolator)
    {
        this.interpolator = interpolator;

        this.nThetaIntervals = this.nThetaPoints - 1;
        this.nPhiIntervals = this.nPhiPoints - 1;
    }

    @Override
    protected void initialize()
    {
        // Nothing unique to initialize.
    }

    public Position getPosition()
    {
        return position;
    }

    
    public void setPosition(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.position = position;
        this.reset();
    }

    public Angle getAzimuth()
    {
        return azimuth;
    }

    
    public void setAzimuth(Angle azimuth)
    {
        this.azimuth = azimuth;
    }

    public Angle getElevationAngle()
    {
        return elevationAngle;
    }

    
    public void setElevationAngle(Angle elevationAngle)
    {
        this.elevationAngle = elevationAngle;
    }

    public double getGainOffset()
    {
        return gainOffset;
    }

    
    public void setGainOffset(double gainOffset)
    {
        this.gainOffset = gainOffset;
        this.reset();
    }

    public double getGainScale()
    {
        return gainScale;
    }

    
    public void setGainScale(double gainScale)
    {
        this.gainScale = gainScale;
        this.reset();
    }

    public int getThetaResolution()
    {
        return this.nThetaPoints;
    }

    
    public void setThetaResolution(int numSPoints)
    {
        this.nThetaPoints = numSPoints;
        this.nThetaIntervals = numSPoints - 1;
        this.reset();
    }

    public int getPhiResolution()
    {
        return this.nPhiPoints;
    }

    
    public void setPhiResolution(int numTPoints)
    {
        this.nPhiPoints = numTPoints;
        this.nPhiIntervals = numTPoints - 1;
        this.reset();
    }

    public double getRadius()
    {
        return this.interpolator.getMaxValue() + this.gainOffset;
    }

    public Position getReferencePosition()
    {
        return this.getPosition();
    }

    
    public void setColorRamp(BufferedImage image)
    {
        if (image != null)
            this.texture = new BasicTexture(image, true);
    }

    public BufferedImage getColorRamp()
    {
        if (this.texture != null && this.texture.getImageSource() instanceof BufferedImage)
            return (BufferedImage) this.texture.getImageSource();

        return null;
    }

    public Extent getExtent(Globe globe, double verticalExaggeration)
    {
        // See if we've cached an extent associated with the globe.
        Extent extent = super.getExtent(globe, verticalExaggeration);
        if (extent != null)
            return extent;

        this.getCurrent().setExtent(new Sphere(globe.computePointFromPosition(this.getReferencePosition()),
            this.getRadius()));

        return this.getCurrent().getExtent();
    }

    public Sector getSector()
    {
        if (this.sector == null)
            this.sector = null; // TODO

        return this.sector;
    }

    protected boolean mustApplyTexture(DrawContext dc)
    {
        return true;
    }

    @Override
    protected boolean shouldUseVBOs(DrawContext dc)
    {
        return false;
    }

    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        ShapeData shapeData = this.getCurrent();

        if (shapeData.vertices == null)
            return true;

        if (this.getAltitudeMode() == MainClass.ABSOLUTE
            && shapeData.getGlobeStateKey() != null
            && shapeData.getGlobeStateKey().equals(dc.getGlobe().getGlobeStateKey(dc)))
            return false;

        // Determine whether the reference point has changed. If it hasn't, then no other points need to change.
        Vec4 rp = this.computePoint(dc.getTerrain(), this.getPosition());
        if (shapeData.getReferencePoint() != null && shapeData.getReferencePoint().equals(rp))
            return false;

        return super.mustRegenerateGeometry(dc);
    }

    protected boolean doMakeOrderedRenderable(DrawContext dc)
    {
        if (!this.intersectsFrustum(dc))
            return false;

        this.makeVertices(dc);

        ShapeData shapeData = this.getCurrent();

        if (shapeData.indices == null)
            this.makeIndices();

        if (shapeData.normals == null)
            this.makeNormals();

        return true;
    }

    protected boolean isOrderedRenderableValid(DrawContext dc)
    {
        ShapeData shapeData = this.getCurrent();

        return shapeData.vertices != null && shapeData.indices != null && shapeData.normals != null;
    }

    protected void doDrawOutline(DrawContext dc)
    {
        this.drawModel(dc, DISPLAY_MODE_LINE, !this.isHighlighted());
    }

    protected void doDrawInterior(DrawContext dc)
    {
        this.drawModel(dc, DISPLAY_MODE_FILL, true);
    }

    public void drawModel(DrawContext dc, int displayMode, boolean showTexture)
    {
        ShapeData shapeData = this.getCurrent();
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.

        if (this.texture == null)
            this.makeTexture();

        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, displayMode);

        if (!dc.isPickingMode() && showTexture)
        {
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, shapeData.texCoords.rewind());
            this.texture.bind(dc);
        }

        gl.glPushMatrix();

        // Rotate to align with longitude.
        gl.glRotated(this.getPosition().getLongitude().degrees, 0, 1, 0);

        // Rotate to align with latitude.
        gl.glRotated(Math.abs(90 - this.getPosition().getLatitude().degrees), 1, 0, 0);

        // Apply the azimuth.
        if (this.getAzimuth() != null)
            gl.glRotated(-this.getAzimuth().degrees, 0, 1, 0);

        // Apply the elevation angle.
        if (this.getElevationAngle() != null)
            gl.glRotated(this.getElevationAngle().degrees, 1, 0, 0);

        gl.glVertexPointer(3, GL.GL_FLOAT, 0, shapeData.vertices.rewind());

        if (!dc.isPickingMode() && this.mustApplyLighting(dc, null))
            gl.glNormalPointer(GL.GL_FLOAT, 0, shapeData.normals.rewind());

        for (IntBuffer iBuffer : shapeData.indices)
        {
            gl.glDrawElements(GL.GL_TRIANGLE_STRIP, iBuffer.limit(), GL.GL_UNSIGNED_INT, iBuffer.rewind());
        }

        gl.glPopMatrix();

        if (!dc.isPickingMode())
            gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
    }

    private void makeVertices(DrawContext dc)
    {
        ShapeData shapeData = this.getCurrent();

        Vec4 rp = this.computePoint(dc.getTerrain(), this.getPosition());
        if (shapeData.getReferencePoint() != null && shapeData.getReferencePoint().equals(rp))
            return; // no need to regenerate the vertices

        shapeData.setReferencePoint(rp);

        int nVertices = (this.nThetaIntervals + 1) * (this.nPhiIntervals + 1);
        shapeData.vertices = Buffers.newDirectFloatBuffer(3 * nVertices);
        shapeData.texCoords = Buffers.newDirectFloatBuffer(2 * nVertices);

        double rScale = 1 / (this.getMaxR() - this.getMinR()); // to keep texture coords in [0,1]. see comment below

        double xMax = -Double.MAX_VALUE;
        double yMax = -Double.MAX_VALUE;
        double zMax = -Double.MAX_VALUE;

        double dTheta = 180 / this.nThetaIntervals;
        double dPhi = 360 / this.nPhiIntervals;

        for (int it = 0; it <= this.nThetaIntervals; it++)
        {
            for (int ip = 0; ip <= this.nPhiIntervals; ip++)
            {
                double theta = it * dTheta;
                double phi = ip * dPhi;
                double t = theta * Math.PI / 180;
                double p = phi * Math.PI / 180;

                Double r = this.interpolator.getValue(theta, phi);

                // Scale r to use full range of texture coordinates. Use 0 if r is undefined at these coordinates.
                double s = r != null ? (r - this.getMinR()) * rScale : 0;
                shapeData.texCoords.put((float) s).put(0);

                // Scale and offset r per application's specifications. Use 0 if r is undefined at these coordinates.
                double rScaled = r != null ? (r + this.gainOffset) * this.gainScale : 0;

                double z = rScaled * Math.sin(t) * Math.cos(p);
                double x = rScaled * Math.sin(t) * Math.sin(p);
                double y = rScaled * Math.cos(t);

                double xa = Math.abs(x);
                double ya = Math.abs(y);
                double za = Math.abs(z);
                if (xa > xMax)
                    xMax = xa;
                if (ya > yMax)
                    yMax = ya;
                if (za > zMax)
                    zMax = za;

                shapeData.vertices.put((float) x).put((float) y).put((float) z);
            }
        }

        shapeData.setExtent(new Sphere(rp, Math.sqrt(xMax * xMax + yMax * yMax + zMax * zMax)));
    }

    private double getMinR()
    {
        Double minR = this.interpolator.getMinValue();

        return minR != null ? minR : 0;
    }

    private double getMaxR()
    {
        Double maxR = this.interpolator.getMaxValue();

        return maxR != null ? maxR : 1;
    }

    private void makeIndices()
    {
        ShapeData shapeData = this.getCurrent();

        shapeData.indices = new IntBuffer[this.nThetaIntervals];

        for (int j = 0; j < this.nThetaIntervals; j++)
        {
            shapeData.indices[j] = Buffers.newDirectIntBuffer(2 * this.nPhiIntervals + 2);

            for (int i = 0; i <= this.nPhiIntervals; i++)
            {
                int k1 = i + j * (this.nPhiIntervals + 1);
                int k2 = k1 + this.nPhiIntervals + 1;
                shapeData.indices[j].put(k1).put(k2);
            }
        }
    }

    private void makeTexture()
    {
        BufferedImage image = new BufferedImage(240, 2, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        for (int i = 0; i < image.getWidth(); i++)
        {
            g.setPaint(Color.getHSBColor((float) ((image.getWidth() - i) / 360d), 1f, 1f));
            g.fillRect(i, 0, 1, 2);
        }

        this.texture = new BasicTexture(image, true);
    }

    protected void makeNormals()
    {
        ShapeData shapeData = this.getCurrent();

        Vec4 vecA, vecB, vecC, vecD, vecX1, vecX2;

        shapeData.normals = Buffers.newDirectFloatBuffer(shapeData.vertices.limit());

        for (int j = 0; j <= this.nThetaIntervals; j++)
        {
            for (int i = 0; i <= this.nPhiIntervals; i++)
            {
                Vec4 vec0 = this.getVec(shapeData, i, j);

                if (i == 0 && j == 0)
                {
                    vecA = this.getVec(shapeData, i, j + 1).subtract3(vec0);
                    vecB = this.getVec(shapeData, i + 1, j).subtract3(vec0);
                    this.putVec(i, j, vecA.cross3(vecB).normalize3(), shapeData.normals);
                }
                else if (i == this.nPhiIntervals && j == 0)
                {
                    vecA = this.getVec(shapeData, i - 1, j).subtract3(vec0);
                    vecB = this.getVec(shapeData, i, j + 1).subtract3(vec0);
                    this.putVec(i, j, vecA.cross3(vecB).normalize3(), shapeData.normals);
                }
                else if (i == 0 && j == this.nThetaIntervals)
                {
                    vecA = this.getVec(shapeData, i + 1, j).subtract3(vec0);
                    vecB = this.getVec(shapeData, i, j - 1).subtract3(vec0);
                    this.putVec(i, j, vecA.cross3(vecB).normalize3(), shapeData.normals);
                }
                else if (i == this.nPhiIntervals && j == this.nThetaIntervals)
                {
                    vecA = this.getVec(shapeData, i, j - 1).subtract3(vec0);
                    vecB = this.getVec(shapeData, i - 1, j).subtract3(vec0);
                    this.putVec(i, j, vecA.cross3(vecB).normalize3(), shapeData.normals);
                }
                else if (i == 0)
                {
                    vecA = this.getVec(shapeData, i, j - 1).subtract3(vec0);
                    vecB = this.getVec(shapeData, i + 1, j).subtract3(vec0);
                    vecC = this.getVec(shapeData, i, j - 1).subtract3(vec0);

                    vecX1 = vecA.cross3(vecB).multiply3(0.5);
                    vecX2 = vecB.cross3(vecC).multiply3(0.5);

                    this.putVec(i, j, vecX1.add3(vecX2).normalize3(), shapeData.normals);
                }
                else if (i == this.nPhiIntervals)
                {
                    vecA = this.getVec(shapeData, i, j - 1).subtract3(vec0);
                    vecB = this.getVec(shapeData, i - 1, j).subtract3(vec0);
                    vecC = this.getVec(shapeData, i, j + 1).subtract3(vec0);

                    vecX1 = vecA.cross3(vecB).multiply3(0.5);
                    vecX2 = vecB.cross3(vecC).multiply3(0.5);

                    this.putVec(i, j, vecX1.add3(vecX2).normalize3(), shapeData.normals);
                }
                else if (j == 0)
                {
                    vecA = this.getVec(shapeData, i - 1, j).subtract3(vec0);
                    vecB = this.getVec(shapeData, i, j + 1).subtract3(vec0);
                    vecC = this.getVec(shapeData, i + 1, j).subtract3(vec0);

                    vecX1 = vecA.cross3(vecB).multiply3(0.5);
                    vecX2 = vecB.cross3(vecC).multiply3(0.5);

                    this.putVec(i, j, vecX1.add3(vecX2).normalize3(), shapeData.normals);
                }
                else if (j == this.nThetaIntervals)
                {
                    vecA = this.getVec(shapeData, i + 1, j).subtract3(vec0);
                    vecB = this.getVec(shapeData, i, j - 1).subtract3(vec0);
                    vecC = this.getVec(shapeData, i - 1, j).subtract3(vec0);

                    vecX1 = vecA.cross3(vecB).multiply3(0.5);
                    vecX2 = vecB.cross3(vecC).multiply3(0.5);

                    this.putVec(i, j, vecX1.add3(vecX2).normalize3(), shapeData.normals);
                }
                else
                {
                    vecA = this.getVec(shapeData, i, j - 1).subtract3(vec0);
                    vecB = this.getVec(shapeData, i - 1, j).subtract3(vec0);
                    vecC = this.getVec(shapeData, i, j + 1).subtract3(vec0);
                    vecD = this.getVec(shapeData, i + 1, j).subtract3(vec0);

                    vecX1 = vecA.cross3(vecB).multiply3(0.25);
                    vecX2 = vecB.cross3(vecC).multiply3(0.25);
                    Vec4 vecX3 = vecC.cross3(vecD).multiply3(0.25);
                    Vec4 vecX4 = vecD.cross3(vecA).multiply3(0.25);

                    this.putVec(i, j, vecX1.add3(vecX2).add3(vecX3).add3(vecX4).normalize3(), shapeData.normals);
                }
            }
        }
    }

    protected Vec4 getVec(ShapeData shapeData, int i, int j)
    {
        int k = 3 * (j * this.nPhiIntervals + i);

        float x = shapeData.vertices.get(k);
        float y = shapeData.vertices.get(k + 1);
        float z = shapeData.vertices.get(k + 2);

        return new Vec4(x, y, z);
    }

    protected void putVec(int i, int j, Vec4 vec, FloatBuffer buffer)
    {
        int k = 3 * (j * this.nPhiIntervals + i);

        buffer.put(k, (float) vec.getX());
        buffer.put(k + 1, (float) vec.getY());
        buffer.put(k + 2, (float) vec.getZ());
    }

    @Override
    protected void fillVBO(DrawContext dc)
    {
    }

    public void moveTo(Position position)
    {
    }

    @Override
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException
    {
        return null;
    }

    @Override
    public String isExportFormatSupported(String mimeType)
    {
        return Exportable.FORMAT_NOT_SUPPORTED;
    }

    @Override
    protected void doExportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        throw new UnsupportedOperationException("KML output not supported for AntennaModel");
    }
}
