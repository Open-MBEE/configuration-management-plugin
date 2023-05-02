package org.openmbee.plugin.cfgmgmt.settings;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestConfigurationManagementSystemProperties {
    private ConfigurationManagementSystemProperties configurationManagementSystemProperties;
    Properties properties;
    Logger logger;

    @Before
    public void setup() {
        properties = mock(Properties.class);
        logger = mock(Logger.class);
        configurationManagementSystemProperties = spy(new ConfigurationManagementSystemProperties());

        when(configurationManagementSystemProperties.getLogger()).thenReturn(logger);
    }

    @Test
    public void loadTestNormal() {
        InputStream inputStream = new ByteArrayInputStream("a=1\nb=2\n".getBytes(StandardCharsets.UTF_8));
        doReturn(inputStream).when(configurationManagementSystemProperties).getPropertiesStream();

        configurationManagementSystemProperties.loadProperties();

        assertEquals("1", configurationManagementSystemProperties.getValue("a"));
        assertEquals("2", configurationManagementSystemProperties.getValue("b"));
    }

    @Test
    public void loadTestExcepton_Test1() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("a=1\nb=2\n".getBytes(StandardCharsets.UTF_8));
        String error = "error";
        IOException ioException = spy(new IOException(error));

        doReturn(inputStream).when(configurationManagementSystemProperties).getPropertiesStream();
        doReturn(properties).when(configurationManagementSystemProperties).getProperties();
        doThrow(ioException).when(properties).load(inputStream);

        configurationManagementSystemProperties.loadProperties();
        verify(configurationManagementSystemProperties).getLogger();
    }

    @Test
    public void loadTestExcepton_Test2() throws IOException {
        InputStream inputStream = new ByteArrayInputStream("a=1\nb=2\n".getBytes(StandardCharsets.UTF_8));
        String error = "error";
        FileNotFoundException fileNotFoundException = spy(new FileNotFoundException(error));

        doReturn(inputStream).when(configurationManagementSystemProperties).getPropertiesStream();
        doReturn(properties).when(configurationManagementSystemProperties).getProperties();
        doThrow(fileNotFoundException).when(properties).load(inputStream);

        configurationManagementSystemProperties.loadProperties();
        verify(configurationManagementSystemProperties).getLogger();
    }

    @Test
    public void loadTestStatic() {
        //Save configuration
        ConfigurationManagementSystemProperties normalInstance = ConfigurationManagementSystemProperties.getInstance();
        assertNotNull(normalInstance);

        InputStream inputStream = new ByteArrayInputStream("a=1\nb=2\n".getBytes(StandardCharsets.UTF_8));
        doReturn(inputStream).when(configurationManagementSystemProperties).getPropertiesStream();

        configurationManagementSystemProperties.loadProperties();

        ConfigurationManagementSystemProperties.setInstance(configurationManagementSystemProperties);

        assertEquals("1", ConfigurationManagementSystemProperties.getPropertyValue("a"));
        assertEquals("2", ConfigurationManagementSystemProperties.getPropertyValue("b"));

        //reset configuration
        ConfigurationManagementSystemProperties.setInstance(normalInstance);
    }

    @Test
    public void loadProperties_nullTest() {
        InputStream inputStream = null;

        try {
            doReturn(inputStream).when(configurationManagementSystemProperties).getPropertiesStream();
            doReturn(properties).when(configurationManagementSystemProperties).getProperties();

            configurationManagementSystemProperties.loadProperties();
            verify(properties, never()).load(inputStream);
            verify(configurationManagementSystemProperties).getPropertiesStream();
            verify(configurationManagementSystemProperties).getProperties();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }
}