package org.openmbee.plugin.cfgmgmt.controller;

import org.openmbee.plugin.cfgmgmt.application.BaseModule;
import org.openmbee.plugin.cfgmgmt.application.BaseModuleStart;
import org.openmbee.plugin.cfgmgmt.application.BaseModuleStop;
import javafx.application.Platform;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestBaseFxController {
    @Spy
    @InjectMocks
    private BaseFxController baseFxController;
    @Mock
    private BaseModule baseModule;
    @Mock
    private BaseModuleStart baseModuleStart;
    @Mock
    private BaseModuleStop baseModuleStop;

    @Test
    public void startModule_notFxApplicationThread() {
        try (MockedStatic<Platform> platformMockedStatic = mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(false);
            platformMockedStatic.when(() -> Platform.runLater(baseModuleStart)).thenAnswer((Answer<?>) platform -> null);
            doReturn(baseModuleStart).when(baseFxController).getBaseModuleStart(baseModule);

            baseFxController.startModule(baseModule);

            verify(baseModuleStart, never()).run();
            platformMockedStatic.verify(() -> Platform.runLater(baseModuleStart));
            platformMockedStatic.clearInvocations();
        }
    }

    @Test
    public void startModule_isFxApplicationThread() {
        try (MockedStatic<Platform> platformMockedStatic = mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);
            doReturn(baseModuleStart).when(baseFxController).getBaseModuleStart(baseModule);

            baseFxController.startModule(baseModule);

            verify(baseModuleStart).run();
            platformMockedStatic.verify(() -> Platform.runLater(baseModuleStart), never());
            platformMockedStatic.clearInvocations();
        }
    }

    @Test
    public void stopModule_notFxApplicationThread() {
        try (MockedStatic<Platform> platformMockedStatic = mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(false);
            platformMockedStatic.when(() -> Platform.runLater(baseModuleStop)).thenAnswer((Answer<?>) platform -> null);
            doReturn(baseModuleStop).when(baseFxController).getBaseModuleStop(baseModule);

            baseFxController.stopModule(baseModule);

            verify(baseModuleStop, never()).run();
            platformMockedStatic.verify(() -> Platform.runLater(baseModuleStop));
            platformMockedStatic.clearInvocations();
        }
    }

    @Test
    public void stopModule_isFxApplicationThread() {
        try (MockedStatic<Platform> platformMockedStatic = mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);
            doReturn(baseModuleStop).when(baseFxController).getBaseModuleStop(baseModule);

            baseFxController.stopModule(baseModule);

            verify(baseModuleStop).run();
            platformMockedStatic.verify(() -> Platform.runLater(baseModuleStop), never());
            platformMockedStatic.clearInvocations();
        }
    }
}
