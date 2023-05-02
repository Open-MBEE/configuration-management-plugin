package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class TestAdminModeAction {
    private AdminModeAction adminModeAction;
    private ConfigurationManagementService configurationManagementService;
    private UIDomain uiDomain;
    private ActionEvent event;
    private Logger logger;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        uiDomain = mock(UIDomain.class);
        adminModeAction = spy(new AdminModeAction(configurationManagementService));
        event = mock(ActionEvent.class);
        logger = mock(Logger.class);

        doReturn(uiDomain).when(adminModeAction).getUIDomain();
        doReturn(logger).when(adminModeAction).getLogger();
        doReturn(AdminModeAction.TURN_STATE_ON).when(adminModeAction).getName();
        doNothing().when(adminModeAction).setName(any());
    }

    @Test
    public void actionPerformed_off() {
        Mockito.doReturn(AdminModeAction.TURN_STATE_OFF).when(adminModeAction).getName();
        doNothing().when(configurationManagementService).disableAdminMode();

        adminModeAction.actionPerformed(event);

        verify(adminModeAction, never()).setName(AdminModeAction.TURN_STATE_OFF);
        verify(adminModeAction).setName(AdminModeAction.TURN_STATE_ON);
    }

    @Test
    public void actionPerformed_onButAdminDisabled() {
        doReturn(false).when(configurationManagementService).enableAdminMode();

        adminModeAction.actionPerformed(event);

        verify(configurationManagementService).enableAdminMode();
        verify(adminModeAction, never()).setName(AdminModeAction.TURN_STATE_ON);
        verify(adminModeAction, never()).setName(AdminModeAction.TURN_STATE_OFF);
        verify(uiDomain).logErrorAndShowMessage(logger, "Cannot activate admin mode.", "Admin mode activation failure");
    }

    @Test
    public void actionPerformed_onAndAdminEnabled() {
        doReturn(true).when(configurationManagementService).enableAdminMode();

        adminModeAction.actionPerformed(event);

        verify(configurationManagementService).enableAdminMode();
        verify(adminModeAction, never()).setName(AdminModeAction.TURN_STATE_ON);
        verify(adminModeAction).setName(AdminModeAction.TURN_STATE_OFF);
        verify(uiDomain).showWarningMessage("The admin mode is now on. All CM protections are off.\nPROCEED WITH CAUTION!",
                "Admin mode on");
    }

    @Test
    public void updateState_ifAdminModeTrueAndCMActive(){
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).getAdminMode();
        adminModeAction.updateState();
        verify(adminModeAction).setEnabled(true);
        verify(adminModeAction).setName(AdminModeAction.TURN_STATE_OFF);
    }

    @Test
    public void updateState_ifAdminModeFalseAndCMActive(){
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(false).when(configurationManagementService).getAdminMode();
        adminModeAction.updateState();
        verify(adminModeAction).setEnabled(true);
        verify(adminModeAction).setName(AdminModeAction.TURN_STATE_ON);
    }

    @Test
    public void updateState_ifAdminModeTrueAndCMActiveFalse(){
        doReturn(false).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).getAdminMode();
        adminModeAction.updateState();
        verify(adminModeAction).setEnabled(false);
        verify(adminModeAction).setName(AdminModeAction.TURN_STATE_OFF);
    }

    @Test
    public void updateState_ifAdminModeFalseAndCMActiveFalse(){
        doReturn(false).when(configurationManagementService).isCmActive();
        doReturn(false).when(configurationManagementService).getAdminMode();
        adminModeAction.updateState();
        verify(adminModeAction).setEnabled(false);
        verify(adminModeAction).setName(AdminModeAction.TURN_STATE_ON);
    }

    @Test
    public void updateState_exception() {
        String error = "error";
        NullPointerException exception = spy(new NullPointerException(error));

        try {
            doThrow(exception).when(adminModeAction).getConfigurationManagementService();
            adminModeAction.updateState();
            verify(uiDomain).logError(logger, "Error updating state for AdminModeAction", exception);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }
}
