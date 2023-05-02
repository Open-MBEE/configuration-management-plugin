package org.openmbee.plugin.cfgmgmt.controller;

import org.openmbee.plugin.cfgmgmt.application.BaseModule;
import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.ui.IConfigurationManagementUI;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class TestAbstractCmDispatcher {
    private AbstractCmDispatcher abstractCmDispatcher;
    private ElementChangeHistoryController currentController;
    private CmControllerSettings cmControllerSettings;
    private BaseModule module;
    private Logger logger;
    private IConfigurationManagementUI.ConfigurationManagementUiType sourceType;

    @Before
    public void setUp() {
        cmControllerSettings = mock(CmControllerSettings.class);
        currentController = mock(ElementChangeHistoryController.class);
        CmControllerFactory cmControllerFactory = mock(CmControllerFactory.class);
        abstractCmDispatcher = Mockito.spy(new CmDispatcher(cmControllerFactory));
        module = mock(BaseModule.class);
        sourceType = IConfigurationManagementUI.ConfigurationManagementUiType.ELEMENT_CHANGE_HISTORY_MATRIX;
        logger = mock(Logger.class);

        doReturn(currentController).when(abstractCmDispatcher).getCurrentController();
        doReturn(logger).when(abstractCmDispatcher).getLogger();
    }

    @Test
    public void invokeCmController_noIssues() {
        doNothing().when(abstractCmDispatcher).prepareCmController(module);
        doNothing().when(abstractCmDispatcher).initializeAndStartController(cmControllerSettings);

        abstractCmDispatcher.invokeCmController(cmControllerSettings, module);

        verify(logger, never()).error(anyString());
    }

    @Test
    public void prepareCmController_nullController() {
        doReturn(null).when(abstractCmDispatcher).getCurrentController();

        abstractCmDispatcher.prepareCmController(module);

        assertSame(module, abstractCmDispatcher.currentControllerModule);
        verify(currentController, never()).stopModule();
    }

    @Test
    public void prepareCmController_controllerExists() {
        doNothing().when(currentController).stopModule();
        doNothing().when(currentController).resetForNextStart();

        abstractCmDispatcher.prepareCmController(module);

        assertSame(module, abstractCmDispatcher.currentControllerModule);
        verify(currentController).stopModule();
    }

    @Test
    public void setController_unknownType() {
        sourceType = mock(IConfigurationManagementUI.ConfigurationManagementUiType.class);
        Object unknownController = mock(Object.class);

        doReturn(unknownController).when(abstractCmDispatcher).getController();

        assertNull(abstractCmDispatcher.currentController);
        abstractCmDispatcher.setController(sourceType);
        assertNull(abstractCmDispatcher.currentController);

    }

    @Test
    public void setController_elementHistoryType() {
        doReturn(currentController).when(abstractCmDispatcher).getController();

        assertNull(abstractCmDispatcher.currentController);
        abstractCmDispatcher.setController(sourceType);
        assertSame(currentController, abstractCmDispatcher.currentController);
    }
}
