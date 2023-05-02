package org.openmbee.plugin.cfgmgmt.settings;

import org.openmbee.plugin.cfgmgmt.ConfigurationManagementPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Behaves like System properties, but only for CM plugin usage.
 * Unit testable by allowing singleton to be overridden
 */
public class ConfigurationManagementSystemProperties {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementSystemProperties.class);

    protected Logger getLogger() {
        return logger;
    }

    private static ConfigurationManagementSystemProperties instance;

    public static ConfigurationManagementSystemProperties getInstance() {
        if(instance == null) {
            instance = new ConfigurationManagementSystemProperties();
        }
        return instance;
    }

    //For unit testing
    public static void setInstance(ConfigurationManagementSystemProperties newInstance) {
        instance = newInstance;
    }

    public static String getPropertyValue(String key) {
        return getInstance().getValue(key);
    }

    //************* Non Static ******************

    private Properties properties;

    protected ConfigurationManagementSystemProperties() {
        loadProperties();
    }

    protected void loadProperties() {
        try (InputStream inputStream = getPropertiesStream()) {
            properties = getProperties();
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (FileNotFoundException fileNotFoundException) {
            getLogger().error("Configuration Management properties file not found", fileNotFoundException);
        } catch (IOException ioException) {
            getLogger().error("Error loading Configuration Management plugin properties", ioException);
        }
    }

    protected Properties getProperties() {
        return new Properties();
    }

    protected InputStream getPropertiesStream() {
        return ConfigurationManagementPlugin.class.getResourceAsStream("/cm.properties");
    }

    public String getValue(String key) {
        return properties.getProperty(key);
    }
}
