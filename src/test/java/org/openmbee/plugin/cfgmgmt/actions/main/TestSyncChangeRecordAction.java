package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.integration.jira.json.JiraIssueFieldsJson;
import org.openmbee.plugin.cfgmgmt.integration.jira.json.JiraIssueJson;
import org.openmbee.plugin.cfgmgmt.integration.jira.json.JiraIssueStatusJson;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxService;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.Lifecycle;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestSyncChangeRecordAction {
    private SyncChangeRecordAction syncChangeRecordAction;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private ActionEvent actionEvent;
    private ChangeRecord changeRecord;
    private Element element;
    private Stereotype stereotype;
    private JsonObject jsonObject;
    private JsonElement jsonElement;
    private List<String> list;
    private List<String> sourceList;
    private List<ChangeRecord> changeRecordList;
    private Logger logger;
    private JiraIssueJson jiraIssueJson;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        syncChangeRecordAction = Mockito.spy(new SyncChangeRecordAction(configurationManagementService));
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        actionEvent = mock(ActionEvent.class);
        changeRecord = mock(ChangeRecord.class);
        element = mock(Element.class);
        stereotype = mock(Stereotype.class);
        jsonObject = mock(JsonObject.class);
        jiraIssueJson = mock(JiraIssueJson.class);
        jsonElement = mock(JsonElement.class);
        list = new ArrayList<>();
        sourceList = new ArrayList<>();
        logger = mock(Logger.class);

        changeRecordList = new ArrayList<>();
        changeRecordList.add(changeRecord);

        doReturn(apiDomain).when(configurationManagementService).getApiDomain();
        doReturn(uiDomain).when(configurationManagementService).getUIDomain();
        doReturn(changeRecordList).when(configurationManagementService).getChangeRecords();
        doReturn(element).when(changeRecord).getElement();
        doReturn(stereotype).when(changeRecord).getBaseStereotype();
        when(syncChangeRecordAction.getLogger()).thenReturn(logger);
    }

    @Test
    public void actionPerformed_nullSourceIds() {
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE_ID);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE);

        syncChangeRecordAction.actionPerformed(actionEvent);

        verify(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE_ID);
        verify(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE);
        verify(syncChangeRecordAction, never()).getStatusNameFromSource(anyString(), any());
        verify(syncChangeRecordAction, never()).applyStatusToChangeRecord(any(), anyString());
    }

    @Test
    public void actionPerformed_emptySourceIds() {
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE_ID);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE);

        syncChangeRecordAction.actionPerformed(actionEvent);

        verify(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE_ID);
        verify(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE);
        verify(syncChangeRecordAction, never()).getStatusNameFromSource(anyString(), any());
        verify(syncChangeRecordAction, never()).applyStatusToChangeRecord(any(), anyString());
    }

    @Test
    public void actionPerformed_nullSources() {
        String sourceId = "sourceId";
        list.add(sourceId);

        doReturn(list).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE_ID);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE);

        syncChangeRecordAction.actionPerformed(actionEvent);

        verify(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE_ID);
        verify(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE);
        verify(syncChangeRecordAction, never()).getStatusNameFromSource(anyString(), any());
        verify(syncChangeRecordAction, never()).applyStatusToChangeRecord(any(), anyString());
    }

    @Test
    public void actionPerformed_emptySources() {
        String sourceId = "sourceId";
        list.add(sourceId);

        doReturn(list).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE_ID);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE);

        syncChangeRecordAction.actionPerformed(actionEvent);

        verify(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE_ID);
        verify(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE);
        verify(syncChangeRecordAction, never()).getStatusNameFromSource(anyString(), any());
        verify(syncChangeRecordAction, never()).applyStatusToChangeRecord(any(), anyString());
    }

    @Test
    public void actionPerformed_nullNewStatusName() {
        String sourceId = "sourceId";
        list.add(sourceId);
        String source = "source";
        sourceList.add(source);

        doReturn(list).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE_ID);
        doReturn(sourceList).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE);
        doReturn(null).when(syncChangeRecordAction).getStatusNameFromSource(source, list);

        syncChangeRecordAction.actionPerformed(actionEvent);

        verify(syncChangeRecordAction).getStatusNameFromSource(source, list);
        verify(syncChangeRecordAction, never()).applyStatusToChangeRecord(any(), anyString());
    }

    @Test
    public void actionPerformed_attemptsToApplyNewStatusName() {
        String sourceId = "sourceId";
        list.add(sourceId);
        String source = "source";
        sourceList.add(source);
        String statusName = "statusName";

        doReturn(list).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE_ID);
        doReturn(sourceList).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.SOURCE);
        doReturn(statusName).when(syncChangeRecordAction).getStatusNameFromSource(source, list);
        doNothing().when(syncChangeRecordAction).applyStatusToChangeRecord(changeRecord, statusName);

        syncChangeRecordAction.actionPerformed(actionEvent);

        verify(syncChangeRecordAction).getStatusNameFromSource(source, list);
        verify(syncChangeRecordAction).applyStatusToChangeRecord(changeRecord, statusName);
    }

    @Test
    public void getStatusNameFromSource_unexpectedSource() {
        String source = "unexpectedSource";

        assertNull(syncChangeRecordAction.getStatusNameFromSource(source, list));
    }

    @Test
    public void getStatusNameFromSource_errorWith3dxAttempt() {
        String source = PluginConstant.THREEDX_SOURCE;

        String error = "error";
        ThreeDxIntegrationException integrationException = spy(new ThreeDxIntegrationException(error));
        try {
            doThrow(integrationException).when(syncChangeRecordAction).getChangeActionFrom3DX(list);

            assertNull(syncChangeRecordAction.getStatusNameFromSource(source, list));

            verify(uiDomain).logErrorAndShowMessage(logger,
                    String.format(ExceptionConstants.THREEDX_ERROR_GETTING_CHANGE_ACTION, error),
                    "3Dx Communication Error", integrationException);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getStatusNameFromSource_successful3dxAttempt() {
        String source = PluginConstant.THREEDX_SOURCE;
        String statusName = "statusName";
        try {
            doReturn(jsonObject).when(syncChangeRecordAction).getChangeActionFrom3DX(list);
            doReturn(jsonElement).when(jsonObject).get("state");
            doReturn(statusName).when(jsonElement).getAsString();

            String result = syncChangeRecordAction.getStatusNameFromSource(source, list);

            assertNotNull(result);
            assertEquals(statusName, result);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getStatusNameFromSource_errorWithJiraAttempt() {
        String source = PluginConstant.JIRA_SOURCE;

        String error = "error";
        JiraIntegrationException integrationException = spy(new JiraIntegrationException(error));
        try {
            doThrow(integrationException).when(syncChangeRecordAction).getIssueFromJIRA(list);

            assertNull(syncChangeRecordAction.getStatusNameFromSource(source, list));

            verify(uiDomain).logErrorAndShowMessage(logger,
                    String.format(ExceptionConstants.JIRA_ERROR_GETTING_ISSUE, error),
                    "Jira Communication Error", integrationException);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getStatusNameFromSource_successfulJiraAttempt() {
        String source = PluginConstant.JIRA_SOURCE;
        String statusName = "statusName";
        JiraIssueFieldsJson jiraIssueFieldsJson = mock(JiraIssueFieldsJson.class);
        JiraIssueStatusJson jiraIssueStatusJson = mock(JiraIssueStatusJson.class);
        try {
            doReturn(jiraIssueJson).when(syncChangeRecordAction).getIssueFromJIRA(list);
            doReturn(jiraIssueFieldsJson).when(jiraIssueJson).getFields();
            doReturn(jiraIssueStatusJson).when(jiraIssueFieldsJson).getStatus();
            doReturn(statusName).when(jiraIssueStatusJson).getName();

            String result = syncChangeRecordAction.getStatusNameFromSource(source, list);

            assertNotNull(result);
            assertEquals(statusName, result);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getIssueFromJIRATest() throws JiraIntegrationException {
        List<String> issueIds = new ArrayList<>();
        issueIds.add("id");
        JiraService jiraService = mock(JiraService.class);
        when(syncChangeRecordAction.getJiraService()).thenReturn(jiraService);
        syncChangeRecordAction.getIssueFromJIRA(issueIds);
        verify(jiraService).getIssue(issueIds.get(0));
    }

    @Test
    public void getChangeActionFrom3DX() throws ThreeDxIntegrationException {
        List<String> changeActions = new ArrayList<>();
        changeActions.add("change");
        ThreeDxService threeDxService = mock(ThreeDxService.class);
        doReturn(threeDxService).when(configurationManagementService).getThreeDxService();
        syncChangeRecordAction.getChangeActionFrom3DX(changeActions);
        verify(threeDxService).getChangeAction(changeActions.get(0));
    }

    @Test
    public void applyStatusToChangeRecord_statusNameAndCrStatusNameMismatch() {
        String statusName = "statusName";

        try {
            doReturn(statusName).when(changeRecord).getStatusName();

            syncChangeRecordAction.applyStatusToChangeRecord(changeRecord, statusName);

            verify(changeRecord).getStatusName();
            verify(changeRecord, never()).getLifecycle();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void applyStatusToChangeRecord_nullLifecycleStatus() {
        String statusName = "statusName";
        String lifecycleName = "lifecycleName";
        Lifecycle lifecycle = mock(Lifecycle.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        String formatted = String.format("Status [%s] is incompatible with Change Record lifecycle [%s]", statusName, lifecycleName);
        try {
            doReturn("").when(changeRecord).getStatusName();
            doReturn(Optional.of(lifecycle)).when(changeRecord).getLifecycle();
            doReturn(null).when(lifecycle).getStatusByName(statusName);
            doReturn(lifecycleName).when(lifecycle).getName();
            doNothing().when(uiDomain).showErrorMessage(formatted, "Lifecycle incompatibility");

            syncChangeRecordAction.applyStatusToChangeRecord(changeRecord, statusName);

            verify(uiDomain).showErrorMessage(formatted, "Lifecycle incompatibility");
            verify(changeRecord, never()).checkCEsForReadiness(lifecycleStatus);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void applyStatusToChangeRecord_statusApplied() {
        String statusName = "statusName";
        Lifecycle lifecycle = mock(Lifecycle.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        try {
            doReturn("").when(changeRecord).getStatusName();
            doReturn(Optional.of(lifecycle)).when(changeRecord).getLifecycle();
            doReturn(lifecycleStatus).when(lifecycle).getStatusByName(statusName);
            doReturn(true).when(changeRecord).checkCEsForReadiness(lifecycleStatus);
            doReturn(true).when(changeRecord).changeStatus(lifecycleStatus, null);

            syncChangeRecordAction.applyStatusToChangeRecord(changeRecord, statusName);

            verify(changeRecord).changeStatus(lifecycleStatus, null);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void applyStatusToChangeRecord_errorWhileApplyingStatus() {
        String statusName = "statusName";
        Lifecycle lifecycle = mock(Lifecycle.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);

        String changeRecordName = "changeRecordName";
        Exception exception = spy(new NullPointerException());
        try {
            doReturn("").when(changeRecord).getStatusName();
            doReturn(Optional.of(lifecycle)).when(changeRecord).getLifecycle();
            doReturn(lifecycleStatus).when(lifecycle).getStatusByName(statusName);
            doReturn(true).when(changeRecord).checkCEsForReadiness(lifecycleStatus);
            when(changeRecord.getName()).thenReturn(changeRecordName);
            doThrow(exception).when(changeRecord).changeStatus(lifecycleStatus, null);

            syncChangeRecordAction.applyStatusToChangeRecord(changeRecord, statusName);

            verify(uiDomain).logErrorAndShowMessage(logger,
                    String.format(ExceptionConstants.ERROR_WHILE_CHANGING_STATUS_SINGLE_OBJECT, changeRecordName),
                    ExceptionConstants.SYNCHRONIZATION_ERROR);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void applyStatusToChangeRecord_checkCEsForReadinessReturnFalse() {
        String statusName = "statusName";
        Lifecycle lifecycle = mock(Lifecycle.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        try {
            doReturn("").when(changeRecord).getStatusName();
            doReturn(Optional.of(lifecycle)).when(changeRecord).getLifecycle();
            doReturn(lifecycleStatus).when(lifecycle).getStatusByName(statusName);
            doReturn(false).when(changeRecord).checkCEsForReadiness(lifecycleStatus);

            syncChangeRecordAction.applyStatusToChangeRecord(changeRecord, statusName);

            verify(changeRecord, never()).changeStatus(lifecycleStatus, null);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_cmActive() {
        syncChangeRecordAction.updateState();
        verify(configurationManagementService).isCmActive();
    }
}
