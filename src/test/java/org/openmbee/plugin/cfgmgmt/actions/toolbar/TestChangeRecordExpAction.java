package org.openmbee.plugin.cfgmgmt.actions.toolbar;

import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class TestChangeRecordExpAction {
    private ChangeRecordExpAction changeRecordExpAction;
    private ConfigurationManagementService configurationManagementService;
    private ChangeRecord changeRecord;
    private LifecycleStatus lifecycleStatus;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        changeRecordExpAction = spy(new ChangeRecordExpAction(configurationManagementService));
        changeRecord = mock(ChangeRecord.class);
        lifecycleStatus = mock(LifecycleStatus.class);
        Optional<LifecycleStatus> statusOptional = spy(Optional.of(lifecycleStatus));

        when(changeRecordExpAction.getConfigurationManagementService()).thenReturn(configurationManagementService);
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        when(changeRecord.getStatus()).thenReturn(statusOptional);
    }

    @Test
    public void updateState_CRIsNull() {
        doReturn(null).when(configurationManagementService).getSelectedChangeRecord();
        changeRecordExpAction.updateState();
        verify(changeRecordExpAction).setState(false);
    }

    @Test
    public void updateState_CRIsExpendable() {
        try {
            when(lifecycleStatus.isExpandable()).thenReturn(true);
            changeRecordExpAction.updateState();
            verify(changeRecordExpAction).setState(true);
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
    }

    @Test
    public void updateState_CRNotExpendable() {
        try {
            when(lifecycleStatus.isExpandable()).thenReturn(false);
            changeRecordExpAction.updateState();
            verify(changeRecordExpAction).setState(false);
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
    }

    @Test
    public void updateState_hasNoStatus() {
        try {
            when(changeRecord.getStatus()).thenReturn(Optional.empty());
            changeRecordExpAction.updateState();
            verify(changeRecordExpAction).setState(false);
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
    }
}
