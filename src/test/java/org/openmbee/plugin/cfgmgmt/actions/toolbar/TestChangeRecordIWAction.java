package org.openmbee.plugin.cfgmgmt.actions.toolbar;

import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

public class TestChangeRecordIWAction {
    private ChangeRecordIWAction changeRecordIWAction;
    private ConfigurationManagementService configurationManagementService;
    private ChangeRecord changeRecord;
    private LifecycleStatus lifecycleStatus;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        changeRecordIWAction = spy(new ChangeRecordIWAction(configurationManagementService));
        changeRecord = mock(ChangeRecord.class);
        lifecycleStatus = mock(LifecycleStatus.class);
        Optional<LifecycleStatus> statusOptional = spy(Optional.of(lifecycleStatus));

        when(changeRecordIWAction.getConfigurationManagementService()).thenReturn(configurationManagementService);
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        when(changeRecord.getStatus()).thenReturn(statusOptional);
    }

    @Test
    public void updateState_CRIsNull() {
        doReturn(null).when(configurationManagementService).getSelectedChangeRecord();
        changeRecordIWAction.updateState();
        verify(changeRecordIWAction).setState(false);
    }

    @Test
    public void updateState_CRIsReadOnly() {
        try {
            when(lifecycleStatus.isReadOnly()).thenReturn(true);
            changeRecordIWAction.updateState();
            verify(changeRecordIWAction).setState(false);
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
    }

    @Test
    public void updateState_CRIsNotReadOnly() {
        try {
            when(lifecycleStatus.isReadOnly()).thenReturn(false);
            changeRecordIWAction.updateState();
            verify(changeRecordIWAction).setState(true);
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
    }

    @Test
    public void updateState_noStatus() {
        try {
            when(changeRecord.getStatus()).thenReturn(Optional.empty());
            changeRecordIWAction.updateState();
            verify(changeRecordIWAction).setState(false);
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
    }
}
