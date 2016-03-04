package com.routegis.applications.window.core;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.routegis.applications.window.util.Util;

import core.routegis.engine.util.*;

import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ImageLibrary
{
    // These images are available for situation where a desired image is not available.
    private static final String[] WARNING_IMAGES = new String[]
        {
            "images/warning16.png",
            "images/warning24.png",
            "images/warning32.png",
            "images/warning64.png"
        };

    private static ImageLibrary instance;

    
    public static void setInstance(ImageLibrary library)
    {
        instance = library;
    }

    private ConcurrentHashMap<String, BufferedImage> imageMap = new ConcurrentHashMap<String, BufferedImage>();
    private ConcurrentHashMap<String, ImageIcon> iconMap = new ConcurrentHashMap<String, ImageIcon>();

    public ImageLibrary()
    {
        this.loadWarningImages();
    }

    protected void loadWarningImages()
    {
        for (String imageName : WARNING_IMAGES)
        {
            try
            {
                InputStream is = InOut.openFileOrResourceStream(imageName, this.getClass());
                this.imageMap.put(imageName, ImageUtil.toCompatibleImage(ImageIO.read(is)));
            }
            catch (Exception e)
            {
                Util.getLogger().log(java.util.logging.Level.WARNING,
                    e.getMessage() + " Stand-in image, name is " + imageName, e);
            }
        }
    }

    
    public static BufferedImage getWarningImage(int size)
    {
        if (size < 24)
            return getImage(WARNING_IMAGES[0]);
        else if (size < 32)
            return getImage(WARNING_IMAGES[1]);
        else if (size < 64)
            return getImage(WARNING_IMAGES[2]);
        else
            return getImage(WARNING_IMAGES[3]);
    }

    
    public static Icon getWarningIcon(int size)
    {
        if (size < 24)
            return getIcon(WARNING_IMAGES[0]);
        else if (size < 32)
            return getIcon(WARNING_IMAGES[1]);
        else if (size < 64)
            return getIcon(WARNING_IMAGES[2]);
        else
            return getIcon(WARNING_IMAGES[3]);
    }

    
    public static synchronized BufferedImage getImage(String imageName)
    {
        try
        {
            BufferedImage image = !PrivateUtil.isEmpty(imageName) ? instance.imageMap.get(imageName) : null;
            if (image != null)
                return image;

            URL url = getImageURL(imageName);
            if (url != null)
            {
                image = ImageIO.read(url);
                if (image != null)
                {
                    image = ImageUtil.toCompatibleImage(image);
                    register(imageName, image);
                    return image;
                }
            }

            return null;
        }
        catch (IOException e)
        {
            Util.getLogger().log(java.util.logging.Level.SEVERE,
                e.getMessage() + " Image name " + (imageName != null ? imageName : null), e);
            return null;
        }
    }

    public static synchronized URL getImageURL(String imageName)
    {
        URL url = instance.getClass().getResource(imageName); // look locallly
        if (url == null)
            url = instance.getClass().getResource("/" + imageName); // look locallly
        if (url == null)
            url = instance.getClass().getResource("images" + File.separatorChar + imageName);
        if (url == null)
            url = instance.getClass().getResource("/images" + File.separatorChar + imageName);

        return url;
    }

    
    public static synchronized ImageIcon getIcon(String iconName)
    {
        try
        {
            ImageIcon icon = !PrivateUtil.isEmpty(iconName) ? instance.iconMap.get(iconName) : null;
            if (icon != null)
                return icon;

            // Load it as an image first, because image failures occur immediately.
            BufferedImage image = getImage(iconName);
            if (image != null)
            {
                icon = new ImageIcon(image);
                register(iconName, icon);
                return icon;
            }

            return null;
        }
        catch (Exception e)
        {
            Util.getLogger().log(java.util.logging.Level.SEVERE,
                e.getMessage() + " Icon name " + (iconName != null ? iconName : null), e);
            return null;
        }
    }

    
    public static BufferedImage getImageForIcon(Icon icon)
    {
        if (icon == null)
            return null;

        return getImage(getIconName(icon));
    }

    
    public static synchronized Object register(String name, Object image)
    {
        if (!PrivateUtil.isEmpty(name) && image != null)
        {
            if (image instanceof BufferedImage)
                instance.imageMap.put(name, (BufferedImage) image);
            else if (image instanceof ImageIcon)
                instance.iconMap.put(name, (ImageIcon) image);
        }

        return image;
    }

    
    public static String getImageName(BufferedImage image)
    {
        for (Map.Entry<String, BufferedImage> entry : instance.imageMap.entrySet())
        {
            if (entry.getValue() == image)
                return entry.getKey();
        }

        return null;
    }

    
    public static String getIconName(Icon icon)
    {
        for (Map.Entry<String, ImageIcon> entry : instance.iconMap.entrySet())
        {
            if (entry.getValue() == icon)
                return entry.getKey();
        }

        return null;
    }
}
