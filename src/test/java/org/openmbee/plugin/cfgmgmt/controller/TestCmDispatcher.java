package org.openmbee.plugin.cfgmgmt.controller;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.rules.JavaFxThreadingRule;
import org.openmbee.plugin.cfgmgmt.ui.IConfigurationManagementUI;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class TestCmDispatcher {
    private CmDispatcher cmDispatcher;
    private ElementChangeHistoryController currentController;
    private CmControllerFactory cmControllerFactory;
    private CmControllerSettings cmControllerSettings;
    private Scene scene;
    private FXMLLoader currentLoader;
    private Parent parent;
    private Logger logger;
    private IConfigurationManagementUI.ConfigurationManagementUiType sourceType;

    @Rule
    public JavaFxThreadingRule javaFxThreadingRule = new JavaFxThreadingRule();

    @Before
    public void setUp() {
        currentController = mock(ElementChangeHistoryController.class);
        cmControllerFactory = mock(CmControllerFactory.class);
        cmControllerSettings = mock(CmControllerSettings.class);
        scene = mock(Scene.class);
        currentLoader = mock(FXMLLoader.class);
        parent = mock(Parent.class);
        logger = mock(Logger.class);
        sourceType = mock(IConfigurationManagementUI.ConfigurationManagementUiType.class);

        cmDispatcher = Mockito.spy(new CmDispatcher(cmControllerFactory));

        doReturn(currentController).when(cmDispatcher).getCurrentController();
        doReturn(logger).when(cmDispatcher).getLogger();
        doReturn(currentLoader).when(cmDispatcher).getCurrentLoader();
    }

    @Test
    public void loadSceneBySourceType_LoaderNull() {
        doReturn(null).when(cmDispatcher).getLoader(sourceType);

        assertNull(cmDispatcher.loadSceneBySourceType(sourceType, cmControllerSettings));
        verify(cmDispatcher, never()).setCmControllerFactory(sourceType, cmControllerSettings);
    }

    @Test
    public void loadSceneBySourceType_exceptionDuringLoad() throws IOException {
        String message = "message";
        IOException exception = spy(new IOException(message));

        doReturn(currentLoader).when(cmDispatcher).getLoader(sourceType);
        doNothing().when(cmDispatcher).setCmControllerFactory(sourceType, cmControllerSettings);
        doThrow(exception).when(currentLoader).load();

        assertNull(cmDispatcher.loadSceneBySourceType(sourceType, cmControllerSettings));
        verify(cmDispatcher, never()).setController(sourceType);
        verify(logger).error(String.format(ExceptionConstants.ERROR_DURING_FXML_CONSTRUCTOR_INSTANTIATION , message));
    }

    @Test
    public void loadSceneBySourceType_noIssue() throws IOException {
        doReturn(currentLoader).when(cmDispatcher).getLoader(sourceType);
        doNothing().when(cmDispatcher).setCmControllerFactory(sourceType, cmControllerSettings);
        when(currentLoader.load()).thenReturn(parent);
        doNothing().when(cmDispatcher).setController(sourceType);
        doReturn(scene).when(cmDispatcher).getScene(parent);

        assertSame(scene, cmDispatcher.loadSceneBySourceType(sourceType, cmControllerSettings));
        verify(cmDispatcher).setController(sourceType);
    }

    @Test
    public void setFtaControllerFactory_unknownType() {
        sourceType = mock(IConfigurationManagementUI.ConfigurationManagementUiType.class);

        cmDispatcher.setCmControllerFactory(sourceType, cmControllerSettings);

        verify(cmControllerFactory, never()).setExpectedParameters(cmControllerSettings);
    }

    @Test
    public void setCmControllerFactory_regularType() {
        sourceType = IConfigurationManagementUI.ConfigurationManagementUiType.ELEMENT_CHANGE_HISTORY_MATRIX;

        doNothing().when(cmControllerFactory).setExpectedParameters(cmControllerSettings);
        doNothing().when(currentLoader).setControllerFactory(cmControllerFactory);

        cmDispatcher.setCmControllerFactory(sourceType, cmControllerSettings);

        verify(cmControllerFactory).setExpectedParameters(cmControllerSettings);
    }

    @Test
    public void initializeAndStartController_nullController() {
        when(cmControllerSettings.getSourceType()).thenReturn(sourceType);
        doReturn(scene).when(cmDispatcher).loadSceneBySourceType(sourceType, cmControllerSettings);
        doReturn(null).when(cmDispatcher).getCurrentController();

        cmDispatcher.initializeAndStartController(cmControllerSettings);

        verify(cmDispatcher, never()).setCurrentControllerModule();
    }

    @Test
    public void initializeAndStartController_nullScene() {
        when(cmControllerSettings.getSourceType()).thenReturn(sourceType);
        doReturn(null).when(cmDispatcher).loadSceneBySourceType(sourceType, cmControllerSettings);
        doReturn(currentController).when(cmDispatcher).getCurrentController();

        cmDispatcher.initializeAndStartController(cmControllerSettings);

        verify(cmDispatcher, never()).setCurrentControllerModule();
    }

    @Test
    public void initializeAndStartController_readyToStart() {
        when(cmControllerSettings.getSourceType()).thenReturn(sourceType);
        doReturn(scene).when(cmDispatcher).loadSceneBySourceType(sourceType, cmControllerSettings);
        doReturn(currentController).when(cmDispatcher).getCurrentController();
        doNothing().when(cmDispatcher).setCurrentControllerModule();
        doNothing().when(currentController).setMainScene(scene);
        doNothing().when(currentController).startCmModule();

        cmDispatcher.initializeAndStartController(cmControllerSettings);

        verify(cmDispatcher).setCurrentControllerModule();
    }
}
