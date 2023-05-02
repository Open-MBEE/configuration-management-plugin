package org.openmbee.plugin.cfgmgmt.actions.toolbar;

import org.openmbee.plugin.cfgmgmt.IConfigurationManagementPlugin;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;

import static org.mockito.Mockito.*;

public class TestDeactivateChangeRecordAction {

    private DeactivateChangeRecordAction deactivateChangeRecordActionSpy;
    private IConfigurationManagementPlugin iConfigurationManagementPlugin;
    private ConfigurationManagementService configurationManagementService;
    private ActionEvent event;
    private Logger logger;
    private ChangeRecord changeRecord;
    private SelectChangeRecordAction selectChangeRecordAction;
    private ChangeRecordExpAction changeRecordExpAction;
    private ChangeRecordIWAction changeRecordIWAction;
    private ChangeRecordStatusAction changeRecordStatusAction;

    @Before
    public void setUp() {
        iConfigurationManagementPlugin = mock(IConfigurationManagementPlugin.class);
        configurationManagementService = mock(ConfigurationManagementService.class);
        event = mock(ActionEvent.class);
        logger = mock(Logger.class);
        changeRecord = mock(ChangeRecord.class);
        selectChangeRecordAction = mock(SelectChangeRecordAction.class);
        changeRecordExpAction = mock(ChangeRecordExpAction.class);
        changeRecordIWAction = mock(ChangeRecordIWAction.class);
        changeRecordStatusAction = mock(ChangeRecordStatusAction.class);
        deactivateChangeRecordActionSpy = spy(new DeactivateChangeRecordAction(iConfigurationManagementPlugin, configurationManagementService));

        when(deactivateChangeRecordActionSpy.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(deactivateChangeRecordActionSpy.getLogger()).thenReturn(logger);
        when(iConfigurationManagementPlugin.getSelectChangeRecordAction()).thenReturn(selectChangeRecordAction);
        when(iConfigurationManagementPlugin.getChangeRecordExpAction()).thenReturn(changeRecordExpAction);
        when(iConfigurationManagementPlugin.getChangeRecordIWAction()).thenReturn(changeRecordIWAction);
        when(iConfigurationManagementPlugin.getChangeRecordStatusAction()).thenReturn(changeRecordStatusAction);
    }

    @Test
    public void actionPerformed() {
        doNothing().when(selectChangeRecordAction).resetSelections();
        doNothing().when(changeRecordExpAction).clearState();
        doNothing().when(changeRecordIWAction).clearState();
        doNothing().when(changeRecordStatusAction).clearState();
        doNothing().when(configurationManagementService).setChangeRecordName(null);

        deactivateChangeRecordActionSpy.actionPerformed(event);

        verify(iConfigurationManagementPlugin).getSelectChangeRecordAction();
        verify(selectChangeRecordAction).resetSelections();
    }

    @Test
    public void updateState() {
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doNothing().when(deactivateChangeRecordActionSpy).setEnabled(true);

        deactivateChangeRecordActionSpy.updateState();

        verify(deactivateChangeRecordActionSpy).setEnabled(true);
    }

    @Test
    public void updateState_NullChangeRecord() {
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(null).when(configurationManagementService).getSelectedChangeRecord();
        doNothing().when(deactivateChangeRecordActionSpy).setEnabled(false);

        deactivateChangeRecordActionSpy.updateState();

        verify(deactivateChangeRecordActionSpy).setEnabled(false);
    }

    @Test
    public void updateState_NotCmActive() {
        doReturn(false).when(configurationManagementService).isCmActive();
        doNothing().when(deactivateChangeRecordActionSpy).setEnabled(false);

        deactivateChangeRecordActionSpy.updateState();

        verify(deactivateChangeRecordActionSpy).setEnabled(false);
    }
}
