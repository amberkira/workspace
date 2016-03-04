
package com.routegis.users.util;

import javax.imageio.ImageIO;

import core.routegis.engine.util.*;

import java.awt.*;
import java.awt.image.*;
import java.io.InputStream;


public class PowerOfTwoPaddedImage
{
    protected BufferedImage image;
    protected int width;
    protected int height;

    protected PowerOfTwoPaddedImage(BufferedImage image, int width, int height)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (width <= 0)
        {
            String message = Logging.getMessage("Geom.WidthInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("Geom.HeightInvalid", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.image = image;
        this.width = width;
        this.height = height;
    }

    
    public static PowerOfTwoPaddedImage fromBufferedImage(BufferedImage image)
    {
        if (image == null)
        {
            String message = Logging.getMessage("nullValue.ImageIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        BufferedImage potImage = image;

        // Create a new image with power of two dimensions and an alpha channel. If the original image has non power
        // of two dimensions, or if it does not have alpha channel, it won't display correctly as an Annotation
        // background image.
        if (!PrivateMath.isPowerOfTwo(image.getWidth()) || !PrivateMath.isPowerOfTwo(image.getHeight())
            || image.getTransparency() == Transparency.OPAQUE)
        {
            int potWidth = PrivateMath.powerOfTwoCeiling(image.getWidth());
            int potHeight = PrivateMath.powerOfTwoCeiling(image.getHeight());

            potImage = ImageUtil.createCompatibleImage(potWidth, potHeight, BufferedImage.TRANSLUCENT);
            Graphics2D g2d = potImage.createGraphics();
            try
            {
                g2d.drawImage(image, 0, 0, null);
            }
            finally
            {
                g2d.dispose();
            }
        }

        return new PowerOfTwoPaddedImage(potImage, image.getWidth(), image.getHeight());
    }

    
    public static PowerOfTwoPaddedImage fromPath(String path)
    {
        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object streamOrException = InOut.getFileOrResourceAsStream(path, null);
        if (streamOrException == null || streamOrException instanceof Exception)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, "generic.ExceptionAttemptingToReadImageFile",
                streamOrException != null ? streamOrException : path);
            return null;
        }

        try
        {
            BufferedImage image = ImageIO.read((InputStream) streamOrException);
            return fromBufferedImage(image);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToReadImageFile", path);
            Logging.logger().severe(message);
            return null;
        }
    }

    
    public int getOriginalWidth()
    {
        return this.width;
    }

    
    public int getOriginalHeight()
    {
        return this.height;
    }

    
    public BufferedImage getPowerOfTwoImage()
    {
        return this.image;
    }

    
    public int getPowerOfTwoWidth()
    {
        return this.image.getWidth();
    }

    
    public int getPowerOfTwoHeight()
    {
        return this.image.getHeight();
    }
}