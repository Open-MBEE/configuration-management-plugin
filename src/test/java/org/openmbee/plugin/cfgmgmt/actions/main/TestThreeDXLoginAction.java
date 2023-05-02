package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxClientManager;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxConnectionInfo;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxService;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;

import static org.mockito.Mockito.*;

public class TestThreeDXLoginAction {
    private ThreeDXLoginAction threeDXLoginAction;
    private ConfigurationManagementService configurationManagementService;
    private ThreeDxClientManager threeDxClientManager;
    private ThreeDxService threeDxService;
    private UIDomain uiDomain;
    private ActionEvent actionEvent;
    private ThreeDxConnectionInfo connectionInfo;
    private Logger logger;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        threeDXLoginAction = Mockito.spy(new ThreeDXLoginAction(configurationManagementService));
        threeDxClientManager = mock(ThreeDxClientManager.class);
        threeDxService = mock(ThreeDxService.class);
        uiDomain = mock(UIDomain.class);
        actionEvent = mock(ActionEvent.class);
        connectionInfo = mock(ThreeDxConnectionInfo.class);
        logger = mock(Logger.class);

        when(threeDXLoginAction.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(configurationManagementService.getThreeDxService()).thenReturn(threeDxService);
        when(threeDxService.getThreeDxClientManager()).thenReturn(threeDxClientManager);
        when(threeDXLoginAction.getLogger()).thenReturn(logger);
        when(threeDXLoginAction.getUIDomain()).thenReturn(uiDomain);
    }

    @Test
    public void actionPerformed_nullConnection() {
        doReturn(null).when(threeDxClientManager).getActive3DxConnectionInfo();
        doNothing().when(uiDomain).showErrorMessage(PluginConstant.MISSING_3DX_CONNECTION_SETTING, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
        threeDXLoginAction.actionPerformed(actionEvent);
        verify(uiDomain).showErrorMessage(PluginConstant.MISSING_3DX_CONNECTION_SETTING, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
        verify(threeDxService, never()).acquireToken();
    }

    @Test
    public void actionPerformed_tokenAcquired() {
        doReturn(connectionInfo).when(threeDxClientManager).getActive3DxConnectionInfo();
        doNothing().when(threeDxService).acquireToken();
        threeDXLoginAction.actionPerformed(actionEvent);
        verify(threeDxService).acquireToken();
        verify(uiDomain, never()).showErrorMessage(anyString(), anyString());
    }

    @Test
    public void updateState_cmActive() {
        doReturn(true).when(configurationManagementService).isCmActive();
        doNothing().when(threeDXLoginAction).setEnabled(true);
        threeDXLoginAction.updateState();
        verify(threeDXLoginAction).setEnabled(true);
    }

    @Test
    public void updateStateTest_cmInactive() {
        doReturn(false).when(configurationManagementService).isCmActive();
        doNothing().when(threeDXLoginAction).setEnabled(false);
        threeDXLoginAction.updateState();
        verify(threeDXLoginAction).setEnabled(false);
    }
}
