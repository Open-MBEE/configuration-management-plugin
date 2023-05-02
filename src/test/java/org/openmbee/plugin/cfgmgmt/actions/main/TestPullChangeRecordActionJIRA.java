package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.LifecycleObjectDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.Liaison;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraClientManager;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.Lifecycle;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestPullChangeRecordActionJIRA {
    private PullChangeRecordActionJIRA pullChangeRecordActionJIRA;
    private ConfigurationManagementService configurationManagementService;
    private JiraClientManager jiraClientManager;
    private JiraService jiraService;
    private ApiDomain apiDomain;
    private Project project;
    private UIDomain uiDomain;
    private ConfiguredElementDomain configuredElementDomain;
    private ElementsFactory elementsFactory;
    private Logger logger;
    private ActionEvent actionEvent;
    private List<Map<String, String>> issues;
    private Map<String, String> issueMap;
    private Liaison issueLiaison;
    private JPanel jPanel;
    private JTable jTable;
    private Stereotype baseCRStereotype;
    private  LifecycleObjectDomain lifecycleObjectDomain;
    private String action = "pull";

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        jiraClientManager = mock(JiraClientManager.class);
        jiraService = mock(JiraService.class);
        pullChangeRecordActionJIRA = Mockito.spy(new PullChangeRecordActionJIRA(configurationManagementService));
        pullChangeRecordActionJIRA.issues = new ArrayList<>();
        apiDomain = mock(ApiDomain.class);
        project = mock(Project.class);
        uiDomain = mock(UIDomain.class);
        configuredElementDomain = mock(ConfiguredElementDomain.class);
        elementsFactory = mock(ElementsFactory.class);
        logger = mock(Logger.class);
        actionEvent = mock(ActionEvent.class);
        issues = new ArrayList<>();
        issueMap = new HashMap<>();
        issueLiaison = mock(Liaison.class);
        jPanel = mock(JPanel.class);
        jTable = mock(JTable.class);
        baseCRStereotype = mock(Stereotype.class);
        lifecycleObjectDomain = mock(LifecycleObjectDomain.class);

        doReturn(apiDomain).when(pullChangeRecordActionJIRA).getApiDomain();
        doReturn(uiDomain).when(pullChangeRecordActionJIRA).getUIDomain();
        doReturn(configuredElementDomain).when(pullChangeRecordActionJIRA).getConfiguredElementDomain();
        doReturn(lifecycleObjectDomain).when(pullChangeRecordActionJIRA).getLifecycleObjectDomain();
        doReturn(jiraService).when(pullChangeRecordActionJIRA).getJiraService();
        when(jiraService.getJiraClientManager()).thenReturn(jiraClientManager);
        doReturn(logger).when(pullChangeRecordActionJIRA).getLogger();
        doReturn(issueLiaison).when(pullChangeRecordActionJIRA).getJiraIssueLiaison();
        when(apiDomain.getCurrentProject()).thenReturn(project);
        when(project.getElementsFactory()).thenReturn(elementsFactory);

        try {
            when(issueLiaison.getIssues()).thenReturn(issues);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
        doReturn(issues).when(pullChangeRecordActionJIRA).getIssues();
        doReturn(jTable).when(pullChangeRecordActionJIRA).getActionsTable();
    }


    private void populateIssues(String id, String name, String type, String status, String creator) {
        issueMap.put("id", id);
        issueMap.put("name", name);
        issueMap.put("type", type);
        issueMap.put("status", status);
        issueMap.put("creator", creator);
        issues.add(issueMap);
    }

    private void populateIssuesForTableModel(String id, String name, String type, String status, String creator) {
        issueMap.put("id", id);
        issueMap.put("name", name);
        issueMap.put("type", type);
        issueMap.put("status", status);
        issueMap.put("creator", creator);
        issues.add(issueMap);
    }

    private Object[] makeRowForTableModel(String id, String name, String type, String status, String creator) {
        String statusName = status.substring(status.indexOf(".") + 1);
        return new Object[] {id, name, type, statusName, creator};
    }

    @Test
    public void getJiraIssueLiaison() {
        doCallRealMethod().when(pullChangeRecordActionJIRA).getJiraIssueLiaison();
        assertNotNull(pullChangeRecordActionJIRA.getJiraIssueLiaison());
    }

    @Test
    public void actionPerformed_get_failed() {
        doReturn(false).when(pullChangeRecordActionJIRA).connectToJiraAndGetIssues();

        pullChangeRecordActionJIRA.actionPerformed(actionEvent);

        verify(pullChangeRecordActionJIRA).connectToJiraAndGetIssues();
        verify(pullChangeRecordActionJIRA, never()).getActionsTable();
    }

    @Test
    public void actionPerformed_userCancelsAction() {
        int option = -1;

        doReturn(true).when(pullChangeRecordActionJIRA).connectToJiraAndGetIssues();
        doReturn(jPanel).when(pullChangeRecordActionJIRA).getIssuesPanel(issues);
        when(uiDomain.askForConfirmation(jPanel, "Select the JIRA issue to pull")).thenReturn(option);
        when(uiDomain.isOkOption(option)).thenReturn(false);

        pullChangeRecordActionJIRA.actionPerformed(actionEvent);

        verify(jTable, never()).getSelectedRow();
    }

    @Test
    public void actionPerformed_no_selected_row() {
        int option = -1;

        doReturn(true).when(pullChangeRecordActionJIRA).connectToJiraAndGetIssues();
        doReturn(jPanel).when(pullChangeRecordActionJIRA).getIssuesPanel(issues);
        when(uiDomain.askForConfirmation(jPanel, "Select the JIRA issue to pull")).thenReturn(option);
        when(uiDomain.isOkOption(option)).thenReturn(true);
        when(pullChangeRecordActionJIRA.getActionsTable()).thenReturn(jTable);
        when(jTable.getSelectedRow()).thenReturn(-1);

        pullChangeRecordActionJIRA.actionPerformed(actionEvent);

        verify(uiDomain).showErrorMessage(anyString(), anyString());
        verify(pullChangeRecordActionJIRA, never()).accrueDataAndCreateChangeRecord(anyInt());
    }

    @Test
    public void actionPerformed_selected_row() {
        int option = -1;

        doReturn(true).when(pullChangeRecordActionJIRA).connectToJiraAndGetIssues();
        doReturn(jPanel).when(pullChangeRecordActionJIRA).getIssuesPanel(issues);
        when(uiDomain.askForConfirmation(jPanel, "Select the JIRA issue to pull")).thenReturn(option);
        when(uiDomain.isOkOption(option)).thenReturn(true);
        when(pullChangeRecordActionJIRA.getActionsTable()).thenReturn(jTable);
        when(jTable.getSelectedRow()).thenReturn(1);
        doNothing().when(pullChangeRecordActionJIRA).accrueDataAndCreateChangeRecord(anyInt());

        pullChangeRecordActionJIRA.actionPerformed(actionEvent);

        verify(pullChangeRecordActionJIRA).accrueDataAndCreateChangeRecord(1);
    }

    @Test
    public void updateState() {
        doReturn(true).when(configurationManagementService).isCmActive();
        pullChangeRecordActionJIRA.updateState();
        verify(pullChangeRecordActionJIRA).setEnabled(true);
    }

    @Test
    public void updateState_Exception() {
        String error = "error";
        NullPointerException nullPointerException = spy(new NullPointerException(error));
        try {
            doReturn(logger).when(pullChangeRecordActionJIRA).getLogger();
            doThrow(nullPointerException).when(pullChangeRecordActionJIRA).setEnabled(false);
            pullChangeRecordActionJIRA.updateState();
            verify(uiDomain).logErrorAndShowMessage(logger, "Cannot update state for PullChangeRecordActionJIRA",
                ExceptionConstants.ACTION_STATE_FAILURE, nullPointerException);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }


    @Test
    public void connectToJiraAndGetIssues_nullConnection() {
        try {
            when(jiraClientManager.hasJiraConnectionSettings()).thenReturn(false);

            assertFalse(pullChangeRecordActionJIRA.connectToJiraAndGetIssues());
            verify(issueLiaison, never()).getIssues();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void connectToJiraAndGetIssues_errorGettingIssues() {
        String error = "error";
        JiraIntegrationException integrationException = spy(new JiraIntegrationException(error));

        try {
            when(jiraClientManager.hasJiraConnectionSettings()).thenReturn(true);
            doThrow(integrationException).when(issueLiaison).getIssues();

            assertFalse(pullChangeRecordActionJIRA.connectToJiraAndGetIssues());
            verify(pullChangeRecordActionJIRA, never()).removeAlreadyPulledIssues(issues, "name");
        } catch(Exception e) {
            e.printStackTrace();
            fail("Unexpected exception");
        }
    }

    @Test
    public void connectToJiraAndGetIssues_noMatchingIssues() {
        try {
            when(jiraClientManager.hasJiraConnectionSettings()).thenReturn(true);
            doReturn(issues).when(issueLiaison).getIssues();
            doNothing().when(issueLiaison).reset();
            doNothing().when(pullChangeRecordActionJIRA).removeAlreadyPulledIssues(issues, "name");

            assertFalse(pullChangeRecordActionJIRA.connectToJiraAndGetIssues());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void connectToJiraAndGetIssues_issuesFound() {
        try {
            issues.add(issueMap);

            when(jiraClientManager.hasJiraConnectionSettings()).thenReturn(true);
            doReturn(issues).when(issueLiaison).getIssues();
            doNothing().when(pullChangeRecordActionJIRA).removeAlreadyPulledIssues(issues, "name");

            assertTrue(pullChangeRecordActionJIRA.connectToJiraAndGetIssues());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void accrueDataAndCreateDataChangeRecord_nullStereotype() {
        String name = "name";
        String statusName = "statusName";
        String sourceId = "sourceId";
        String description = "description";
        Map<String, String> bareIssue = new HashMap<>();
        bareIssue.put(PluginConstant.NAME, name);
        bareIssue.put(PluginConstant.STATUS, statusName);
        bareIssue.put(PluginConstant.ID, sourceId);
        bareIssue.put(PluginConstant.DESCRIPTION, description);
        pullChangeRecordActionJIRA.issues.add(bareIssue);

        doReturn(null).when(pullChangeRecordActionJIRA).getCustomChangeRecordStereoType();

        pullChangeRecordActionJIRA.accrueDataAndCreateChangeRecord(0);

        verify(pullChangeRecordActionJIRA).getCustomChangeRecordStereoType();
        verify(pullChangeRecordActionJIRA, never()).verifyUserPermission(any());
    }

    @Test
    public void accrueDataAndCreateDataChangeRecord_errorWithPermissions() {
        String name = "name";
        String statusName = "statusName";
        String sourceId = "sourceId";
        String description = "description";
        Map<String, String> bareIssue = new HashMap<>();
        bareIssue.put(PluginConstant.NAME, name);
        bareIssue.put(PluginConstant.STATUS, statusName);
        bareIssue.put(PluginConstant.ID, sourceId);
        bareIssue.put(PluginConstant.DESCRIPTION, description);
        pullChangeRecordActionJIRA.issues.add(bareIssue);
        try {
            doReturn(baseCRStereotype).when(pullChangeRecordActionJIRA).getCustomChangeRecordStereoType();
            doReturn(false).when(pullChangeRecordActionJIRA).verifyUserPermission(baseCRStereotype);

            pullChangeRecordActionJIRA.accrueDataAndCreateChangeRecord(0);

            verify(uiDomain).logErrorAndShowMessage(logger, "Permissions error", "Jira connection issue");
            verify(configurationManagementService, never()).getLifecycle(baseCRStereotype);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void accrueDataAndCreateDataChangeRecord_errorGettingLifecycle() {
        String name = "name";
        String statusName = "statusName";
        String sourceId = "sourceId";
        String description = "description";
        Map<String, String> bareIssue = new HashMap<>();
        bareIssue.put(PluginConstant.NAME, name);
        bareIssue.put(PluginConstant.STATUS, statusName);
        bareIssue.put(PluginConstant.ID, sourceId);
        bareIssue.put(PluginConstant.DESCRIPTION, description);
        pullChangeRecordActionJIRA.issues.add(bareIssue);
        try {
            doReturn(baseCRStereotype).when(pullChangeRecordActionJIRA).getCustomChangeRecordStereoType();
            doReturn(true).when(pullChangeRecordActionJIRA).verifyUserPermission(baseCRStereotype);
            doReturn(null).when(configurationManagementService).getLifecycle(baseCRStereotype);

            pullChangeRecordActionJIRA.accrueDataAndCreateChangeRecord(0);

            verify(apiDomain, never()).createClassInstance(any());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void accrueDataAndCreateDataChangeRecord_nullStatus() {
        String name = "name";
        String statusName = "statusName";
        String sourceId = "sourceId";
        String description = "description";
        Map<String, String> bareIssue = new HashMap<>();
        bareIssue.put(PluginConstant.NAME, name);
        bareIssue.put(PluginConstant.STATUS, statusName);
        bareIssue.put(PluginConstant.ID, sourceId);
        bareIssue.put(PluginConstant.DESCRIPTION, description);
        pullChangeRecordActionJIRA.issues.add(bareIssue);
        Lifecycle lifecycle = mock(Lifecycle.class);
        String lifecycleName = "lifecycleName";
        String formatted = String.format("Status [%s] is incompatible with Change Record lifecycle [%s]", statusName, lifecycleName);

        try {
            doReturn(baseCRStereotype).when(pullChangeRecordActionJIRA).getCustomChangeRecordStereoType();
            doReturn(true).when(pullChangeRecordActionJIRA).verifyUserPermission(baseCRStereotype);
            doReturn(lifecycle).when(configurationManagementService).getLifecycle(baseCRStereotype);
            doReturn(null).when(lifecycle).getStatusByName(statusName);
            doReturn(lifecycleName).when(lifecycle).getName();

            pullChangeRecordActionJIRA.accrueDataAndCreateChangeRecord(0);

            verify(uiDomain).showErrorMessage(formatted, "Lifecycle incompatibility");
            verify(apiDomain, never()).createClassInstance(any());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void accrueDataAndCreateDataChangeRecord_createChangeRecord() {
        String name = "name";
        String statusName = "statusName";
        String sourceId = "sourceId";
        String description = "description";
        Map<String, String> bareIssue = new HashMap<>();
        bareIssue.put(PluginConstant.NAME, name);
        bareIssue.put(PluginConstant.STATUS, statusName);
        bareIssue.put(PluginConstant.ID, sourceId);
        bareIssue.put(PluginConstant.DESCRIPTION, description);
        pullChangeRecordActionJIRA.issues.add(bareIssue);
        Lifecycle lifecycle = mock(Lifecycle.class);
        LifecycleStatus status = mock(LifecycleStatus.class);
        Class classInstance = mock(Class.class);
        Package pkg = mock(Package.class);
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        try {
            doReturn(baseCRStereotype).when(pullChangeRecordActionJIRA).getCustomChangeRecordStereoType();
            doReturn(true).when(pullChangeRecordActionJIRA).verifyUserPermission(baseCRStereotype);
            doReturn(lifecycle).when(configurationManagementService).getLifecycle(baseCRStereotype);
            doReturn(status).when(lifecycle).getStatusByName(statusName);
            doReturn(classInstance).when(apiDomain).createClassInstance(project);
            doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(true);
            doReturn(changeRecord).when(configurationManagementService).initializeChangeRecord(classInstance, name, baseCRStereotype,
                    pkg, status);
            doNothing().when(configurationManagementService).setChangeRecordParametersForDataSource(classInstance,
                    PluginConstant.JIRA_SOURCE, sourceId, description);

            pullChangeRecordActionJIRA.accrueDataAndCreateChangeRecord(0);

            verify(apiDomain).setCurrentProjectHardDirty();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getIssuesPanel() {
        DefaultTableModel defaultTableModel = mock(DefaultTableModel.class);
        JScrollPane scrollPane = mock(JScrollPane.class);
        JButton loadMore = mock(JButton.class);

        when(pullChangeRecordActionJIRA.createDefaultTableModel()).thenReturn(defaultTableModel);
        doReturn(defaultTableModel).when(pullChangeRecordActionJIRA).prepareTableModel(issues, defaultTableModel, false);
        doReturn(jTable).when(pullChangeRecordActionJIRA).createActionsTable(defaultTableModel);
        when(pullChangeRecordActionJIRA.createScrollPaneForActionsTable()).thenReturn(scrollPane);
        when(pullChangeRecordActionJIRA.setupLoadMoreButton()).thenReturn(loadMore);
        doReturn(jPanel).when(pullChangeRecordActionJIRA).createFinalPanel(scrollPane, loadMore);

        JPanel result = pullChangeRecordActionJIRA.getIssuesPanel(issues);

        assertSame(jPanel, result);
    }

    @Test
    public void setupLoadMoreActionListener_errorDuringAction() {
        JButton loadMore = spy(new JButton());
        String error = "error";
        JiraIntegrationException jiraIntegrationException = spy(new JiraIntegrationException(error));

        try {
            doThrow(jiraIntegrationException).when(issueLiaison).getIssues();

            pullChangeRecordActionJIRA.setupLoadMoreActionListener(loadMore);
            loadMore.doClick();

            verify(uiDomain).logError(logger, "Error while pulling jira issues", jiraIntegrationException);
            verify(loadMore, never()).setEnabled(false);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void setupLoadMoreActionListener_emptyActionList() {
        JButton loadMore = spy(new JButton());
        try {
            doReturn(issues).when(issueLiaison).getIssues();
            doNothing().when(uiDomain).showPlainMessage(PluginConstant.ALL_JIRA_ISSUES_PULLED_MESSAGE, PluginConstant.ALL_JIRA_ISSUES_PULLED_TITLE);

            pullChangeRecordActionJIRA.setupLoadMoreActionListener(loadMore);
            loadMore.doClick();

            assertFalse(loadMore.isEnabled());
            verify(uiDomain).showPlainMessage(PluginConstant.ALL_JIRA_ISSUES_PULLED_MESSAGE, PluginConstant.ALL_JIRA_ISSUES_PULLED_TITLE);
            verify(pullChangeRecordActionJIRA, never()).removeAlreadyPulledIssues(issues, "id");
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void setupLoadMoreActionListener_newActionsLoaded() {
        JButton loadMore = spy(new JButton());
        String id = "id";
        String name = "name";
        String type = "type";
        String status = "status";
        String creator = "creator";
        populateIssues(id, name, type, status, creator);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        try {
            doReturn(issues).when(issueLiaison).getIssues();
            doNothing().when(pullChangeRecordActionJIRA).removeAlreadyPulledIssues(issues, "id");
            doReturn(tableModel).when(jTable).getModel();
            doReturn(tableModel).when(pullChangeRecordActionJIRA).prepareTableModel(issues, tableModel, true);

            pullChangeRecordActionJIRA.setupLoadMoreActionListener(loadMore);
            loadMore.doClick();

            verify(tableModel).fireTableDataChanged();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void removeAlreadyPulledIssues() {
        String id = "id";
        issueMap.put(id, id);
        issues.add(issueMap);
        String crPath = "cr::Path";
        String primary = crPath + "::" + id;
        Class relative = mock(Class.class);

        doReturn(crPath).when(configurationManagementService).getChangeRecordsPackagePath();
        doReturn(relative).when(apiDomain).findClassRelativeToCurrentPrimary(primary);

        assertFalse(issues.isEmpty());
        pullChangeRecordActionJIRA.removeAlreadyPulledIssues(issues, "id");
        assertTrue(issues.isEmpty());
    }

    @Test
    public void removeAlreadyPulledIssues_null() {
        String id = "id";
        issueMap.put(id, id);
        issues.add(issueMap);
        String crPath = "cr::Path";
        String primary = crPath + "::" + id;

        doReturn(crPath).when(configurationManagementService).getChangeRecordsPackagePath();
        doReturn(null).when(apiDomain).findClassRelativeToCurrentPrimary(primary);

        pullChangeRecordActionJIRA.removeAlreadyPulledIssues(issues, "id");
        assertFalse(issues.isEmpty());
    }

    @Test
    public void prepareTableModel_newTableModel() {
        String id = "id";
        String name = "name";
        String type = "type";
        String status = "status";
        String creator = "creator";
        populateIssuesForTableModel(id, name, type, status, creator);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        Object[] columnNames = {"ID", "Name", "Type", "Status", "Creator"};
        Object[] row = makeRowForTableModel(id, name, type, status, creator);

        DefaultTableModel result = pullChangeRecordActionJIRA.prepareTableModel(issues, tableModel, false);

        assertSame(tableModel, result);
        verify(tableModel).setColumnIdentifiers(columnNames);
        verify(tableModel).addRow(row);
    }

    @Test
    public void prepareTableModel_updateTableModel() {
        String id = "id";
        String name = "name";
        String type = "type";
        String status = "status";
        String creator = "creator";
        populateIssuesForTableModel(id, name, type, status, creator);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        Object[] columnNames = {"Name", "Title", "Type", "Status", "Owner"};
        Object[] row = makeRowForTableModel(id, name, type, status, creator);

        DefaultTableModel result = pullChangeRecordActionJIRA.prepareTableModel(issues, tableModel, true);

        assertSame(tableModel, result);
        verify(tableModel, never()).setColumnIdentifiers(columnNames);
        verify(tableModel).addRow(row);
    }

    @Test
    public void getCustomChangeRecordStereoType() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype jiraStereotype = mock(Stereotype.class);
        stereotypes.add(baseCRStereotype);
        stereotypes.add(jiraStereotype);

        doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.JIRA);
        doReturn(jiraStereotype).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
                "Select change record type", "Change record type selection");

        assertSame(jiraStereotype, pullChangeRecordActionJIRA.getCustomChangeRecordStereoType());
    }

    @Test
    public void verifyUserPermission() {
        try {
            pullChangeRecordActionJIRA.verifyUserPermission(baseCRStereotype);

            verify(configuredElementDomain).canUserPerformAction(configurationManagementService, baseCRStereotype, action );
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }
}
