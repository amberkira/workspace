package com.routegis.applications.window.core;

import org.w3c.dom.*;

import com.routegis.applications.window.util.Util;

import core.routegis.engine.util.*;

import javax.swing.*;
import javax.xml.xpath.XPathConstants;

import java.util.ArrayList;
import java.util.logging.Level;


public class AppConfiguration implements Initializable
{
    protected Controller controller;
    protected String configurationLocation;

    public AppConfiguration()
    {
    }

    public void initialize(Controller controller)
    {
        this.controller = controller;
    }

    public boolean isInitialized()
    {
        return this.controller != null;
    }

    public void configure(final String appConfigurationLocation)
    {
        if (PrivateUtil.isEmpty(appConfigurationLocation))
            throw new IllegalArgumentException("The application configuration location name is null or empty");

        this.configurationLocation = appConfigurationLocation;

        ImageLibrary.setInstance(new ImageLibrary());

        this.configureFeatures(appConfigurationLocation);
    }

    protected void configureFeatures(final String appConfigurationLocation)
    {
        // Configure the application objects on the EDT
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    registerConfiguration(appConfigurationLocation);
                }
                catch (Exception e)
                {
                    Util.getLogger().log(Level.SEVERE, "Unable to create initial configuration for {0}",
                        appConfigurationLocation);
                }
            }
        });
    }

    // Registers the objects in the configuration.
    protected void registerConfiguration(String config) throws Exception
    {
        // TODO: this call can return null
        Document doc = PrivateXML.openDocumentFile(config, this.getClass());
        NodeList emNodes = (NodeList) PrivateXML.makeXPath().evaluate("//Feature", doc, XPathConstants.NODESET);
        ArrayList<Object> objects = new ArrayList<Object>();

        for (int i = 0; i < emNodes.getLength(); i++)
        {
            String featureID = null;
            String className = null;
            String actuate = null;

            try
            {
                Element element = (Element) emNodes.item(i);

                featureID = PrivateXML.getText(element, "@featureID");
                className = PrivateXML.getText(element, "@className");
                actuate = PrivateXML.getText(element, "@actuate");

                if (className == null || className.length() == 0)
                {
                    Util.getLogger().log(Level.WARNING,
                        "Configuration entry in {0} missing feature ID ({1})or classname ({2})",
                        new Object[]
                            {config, featureID != null ? featureID : "null", className != null ? className : "null"});
                    continue;
                }

                if (!PrivateUtil.isEmpty(featureID))
                {
                    if (actuate != null && actuate.equals("onDemand"))
                        this.controller.registerObject(featureID, Class.forName(className));
                    else
                        objects.add(this.controller.createAndRegisterObject(featureID, className));
                }
                else
                {
                    objects.add(this.controller.createRegistryObject(className));
                }

                String accelerator = PrivateXML.getText(element, "@accelerator");
                if (accelerator != null && accelerator.length() > 0)
                    this.controller.registerObject(className + Constants.ACCELERATOR_SUFFIX, accelerator);
            }
            catch (Exception e)
            {
                String msg = String.format(
                    "Error creating configuration entry in %s for feature ID (%s), classname (%s), activate (%s)",
                    config, featureID != null ? featureID : "null",
                    className != null ? className : "null",
                    actuate != null ? actuate : "null");
                Util.getLogger().log(Level.WARNING, msg, e);
                //noinspection UnnecessaryContinue
                continue;
            }
        }

        for (Object o : objects)
        {
            try
            {
                if (o instanceof Initializable)
                    ((Initializable) o).initialize(this.controller);
            }
            catch (Exception e)
            {
                String msg = String.format("Error initializing object %s", o.getClass().getName());
                Util.getLogger().log(Level.WARNING, msg, e);
            }
        }
    }
}
