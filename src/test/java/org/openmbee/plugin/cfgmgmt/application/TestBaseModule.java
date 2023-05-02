package org.openmbee.plugin.cfgmgmt.application;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.rules.JavaFxThreadingRule;
import org.openmbee.plugin.cfgmgmt.ui.IConfigurationManagementUI;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestBaseModule {
    private BaseModule module;
    private Stage stage;
    private Scene scene;
    private String title;
    private IConfigurationManagementUI.ConfigurationManagementUiType uiType;
    private Logger logger;

    @Rule
    public JavaFxThreadingRule javaFxThreadingRule = new JavaFxThreadingRule();

    @Before
    public void setup() {
        module = spy(new ElementHistoryMatrixModule());
        stage = mock(Stage.class);
        scene = mock(Scene.class);
        logger = mock(Logger.class);
        uiType = IConfigurationManagementUI.ConfigurationManagementUiType.ELEMENT_CHANGE_HISTORY_MATRIX;

        doReturn(title).when(module).getTitle();
        doReturn(stage).when(module).createStage();
        doNothing().when(stage).setResizable(false);
        doNothing().when(stage).setTitle(title);
        doReturn(logger).when(module).getLogger();
        Platform.setImplicitExit(false);
    }

    @After
    public void teardown() {
        Platform.setImplicitExit(true);
    }

    @Test
    public void getClassLoader_notFxApplicationThread() {
        try(MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            Thread currentThread = mock(Thread.class);
            String name = "name";
            ClassLoader classLoader = mock(ClassLoader.class);
            String errorMessage = ExceptionConstants.JAVAFX_ILLEGAL_STATE_MESSAGE + name;

            when(currentThread.getContextClassLoader()).thenReturn(classLoader);
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(false);
            when(currentThread.getName()).thenReturn(name);

            assertEquals(classLoader, module.getClassLoader(currentThread));

            verify(logger).error(ExceptionConstants.JAVAFX_THREAD_EXCEPTION, errorMessage);
        } catch(Exception e) {
            fail("Unexpected Exception");
        }
    }

    @Test
    public void getClassLoader_noIssues() {
        try(MockedStatic<Platform> platformMockedStatic = Mockito.mockStatic(Platform.class)) {
            Thread currentThread = mock(Thread.class);
            ClassLoader classLoader = mock(ClassLoader.class);

            when(currentThread.getContextClassLoader()).thenReturn(classLoader);
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);

            assertEquals(classLoader, module.getClassLoader(currentThread));

            verify(logger, never()).error(anyString(), anyString());
        } catch(Exception e) {
            fail("Unexpected Exception");
        }
    }

    @Test
    public void moduleStartRoutine_nullPrimaryStage() throws IOException {
        doReturn(null).when(module).getPrimaryStage();

        module.moduleStartRoutine();

        verify(module, never()).start(any());
    }

    @Test
    public void moduleStartRoutine_primaryStageExists() throws IOException {
        doReturn(stage).when(module).getPrimaryStage();
        doNothing().when(module).start(stage);
        doNothing().when(module).stop(true, stage);

        module.moduleStartRoutine();

        verify(module).start(stage);
    }

    @Test
    public void moduleStartRoutine_exceptionGettingPrimaryStage() {
        String error = "error";
        IOException ioException = spy(new IOException(error));

        try {
            doThrow(ioException).when(module).getPrimaryStage();

            module.moduleStartRoutine();

            fail("Expected exception did not occur");
        } catch (IOException e) {
            assertEquals(error, e.getMessage());
        }

        verify(module, never()).start(any());
    }

    @Test
    public void stop_exitIsFalse() {
        module.stop(false, stage);

        verify(stage, never()).hide();
    }

    @Test
    public void stop_exitIsTrueButNullPrimaryStage() {
        module.stop(true, null);

        verify(stage, never()).hide();
    }

    @Test
    public void stop_exitIsTrueAndPrimaryStage() {
        module.stop(true, stage);

        doNothing().when(stage).hide();

        verify(stage).hide();
    }

    @Test
    public void getScene_alreadyHasScene() throws IOException {
        module.setScene(scene);

        assertSame(scene, module.getScene(uiType));
    }

    @Test
    public void getScene_nullSceneAndNullLoader() throws IOException {
        module.setScene(null);

        doReturn(null).when(module).getLoader(uiType.getLocation());

        assertNull(module.getScene(uiType));
    }

    @Test
    public void getScene_nullSceneAndHasLoader() throws IOException {
        module.setScene(null);
        FXMLLoader loader = mock(FXMLLoader.class);
        Parent parent = mock(Parent.class);

        doReturn(loader).when(module).getLoader(uiType.location);
        when(loader.load()).thenReturn(parent);
        doReturn(scene).when(module).createScene(parent);

        assertSame(scene, module.getScene(uiType));
    }

    @Test
    public void getPrimaryStage_alreadyHasPrimaryStage() throws IOException {
        module.setPrimaryStage(stage);

        assertSame(stage, module.getPrimaryStage());
    }

    @Test
    public void getPrimaryStage_noPrimaryStage() throws IOException {
        doReturn(scene).when(module).getScene(uiType);
        doNothing().when(stage).setScene(scene);

        assertSame(stage, module.getPrimaryStage());
        verify(stage).setScene(scene);
    }

    @Test
    public void getPrimaryStage_exceptionGettingScene() {
        String error = "error";
        IOException exception = spy(new IOException(error));

        try {
            doThrow(exception).when(module).getScene(uiType);

            module.getPrimaryStage();
        } catch (Exception e) {
            assertEquals(error, e.getMessage());
        }
    }

    @Test
    public void showStage_parameterTrue() {
        module.primaryStage = stage;

        doNothing().when(stage).show();

        module.showStage(true);

        verify(stage).show();
        verify(stage, never()).hide();
    }

    @Test
    public void showStage_parameterFalse() {
        module.primaryStage = stage;

        doNothing().when(stage).hide();

        module.showStage(false);

        verify(stage, never()).show();
        verify(stage).hide();
    }
}
