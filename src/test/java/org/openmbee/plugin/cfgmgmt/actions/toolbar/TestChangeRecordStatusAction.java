package org.openmbee.plugin.cfgmgmt.actions.toolbar;

import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class TestChangeRecordStatusAction {
    private ChangeRecordStatusAction changeRecordStatusAction;
    private ConfigurationManagementService configurationManagementService;
    private ChangeRecord changeRecord;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        changeRecordStatusAction = spy(new ChangeRecordStatusAction(configurationManagementService));
        changeRecord = mock(ChangeRecord.class);

        doNothing().when(changeRecordStatusAction).setEnabled(false);
    }

    @Test
    public void updateState() {
        String status = "status";
        String formatted = String.format("Status: %s", status);
        try {
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(status).when(changeRecord).getStatusName();
            changeRecordStatusAction.updateState();
            verify(changeRecordStatusAction).setName(formatted);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_ChangeRecordNull() {
        String status = "-";
        String formatted = String.format("Status: %s", status);
        try {
            doReturn(null).when(configurationManagementService).getSelectedChangeRecord();
            changeRecordStatusAction.updateState();
            verify(changeRecordStatusAction).setName(formatted);
            verify(changeRecord, never()).getStatusName();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_ChangeRecordNullStatus() {
        String status = "-";
        String formatted = String.format("Status: %s", status);
        try {
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(null).when(changeRecord).getStatusName();
            changeRecordStatusAction.updateState();
            verify(changeRecordStatusAction).setName(formatted);

        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }
}
