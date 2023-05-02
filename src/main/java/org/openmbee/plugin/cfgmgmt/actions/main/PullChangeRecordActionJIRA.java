package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.LifecycleObjectDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.Liaison;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.Lifecycle;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;
import static org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants.*;

public class PullChangeRecordActionJIRA extends MDAction {
    private static final Logger logger = LoggerFactory.getLogger(PullChangeRecordActionJIRA.class);
    private final transient ConfigurationManagementService configurationManagementService;
    private transient JTable actionsTable;
    protected transient List<Map<String, String>> issues;
    private transient Liaison jiraIssueLiaison;

    public PullChangeRecordActionJIRA(ConfigurationManagementService configurationManagementService) {
        super(PULL_CHANGE_RECORD_ACTION_JIRA, PULL_CHANGE_RECORD_FROM_JIRA, null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected JiraService getJiraService() {
        return getConfigurationManagementService().getJiraService();
    }

    protected LifecycleObjectDomain getConfiguredElementDomain() {
        return getConfigurationManagementService().getConfiguredElementDomain();
    }

    protected LifecycleObjectDomain getLifecycleObjectDomain() {
        return getConfigurationManagementService().getLifecycleObjectDomain();
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected Logger getLogger() {
        return logger;
    }

    protected List<Map<String, String>> getIssues() {
        return issues;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (!connectToJiraAndGetIssues()) {
            return;
        }

        if (!getUIDomain().isOkOption(getUIDomain().askForConfirmation(getIssuesPanel(getIssues()), SELECT_JIRA_ISSUES_TO_PULL))) {
            issues.clear();
            return;
        }

        int actionIndex = getActionsTable().getSelectedRow();
        if (actionIndex == -1) {
            getUIDomain().showErrorMessage(NO_ISSUE_WAS_SELECTED, JIRA_INTEGRATION);
            issues.clear();
            return;
        }

        accrueDataAndCreateChangeRecord(actionIndex);
    }

    protected Liaison getJiraIssueLiaison() {
        if(jiraIssueLiaison == null) {
            jiraIssueLiaison = new Liaison(getJiraService());
        }
        return jiraIssueLiaison;
    }

    protected boolean connectToJiraAndGetIssues() {
        if (!getJiraService().getJiraClientManager().hasJiraConnectionSettings()) {
            getUIDomain().showErrorMessage(PluginConstant.MISSING_JIRA_CONNECTION_SETTING, PluginConstant.JIRA_CONNECTION_SETTINGS_ERROR);
            return false;
        }

        try {
            issues = getJiraIssueLiaison().getIssues();
            if(issues.isEmpty()) {
                getJiraIssueLiaison().reset();
                issues = getJiraIssueLiaison().getIssues();
            }
        } catch (JiraIntegrationException e) {
            getUIDomain().logErrorAndShowMessage(getLogger(), String.format("JIRA Communication error: [%s]", e.getMessage()),
                    "Jira connection issue", e);
            return false;
        }
        removeAlreadyPulledIssues(getIssues(), "name"); // eliminating the actions that are already pulled

        if (getIssues().isEmpty()) {
            getUIDomain().showErrorMessage(NO_ISSUES_FOUND_MATCHING_THE_SEARCH_CRITERIA, JIRA_INTEGRATION);
            return false;
        }
        return true;
    }

    protected void accrueDataAndCreateChangeRecord(int actionIndex) {
        String name = issues.get(actionIndex).get(PluginConstant.NAME);
        String statusName = issues.get(actionIndex).get(PluginConstant.STATUS);
        String source = PluginConstant.JIRA_SOURCE;
        String sourceId = issues.get(actionIndex).get(PluginConstant.ID);
        String description = issues.get(actionIndex).get(PluginConstant.DESCRIPTION);
        issues.clear();

        Stereotype stereotype = getCustomChangeRecordStereoType();
        if (stereotype == null) {
            return;
        }

        if (!verifyUserPermission(stereotype)) {
            getUIDomain().logErrorAndShowMessage(getLogger(), "Permissions error","Jira connection issue");
            return;
        }

        // Create Change Record
        LifecycleStatus status;
        Lifecycle lifecycle = getConfigurationManagementService().getLifecycle(stereotype);
        if (lifecycle == null) {
            return; // an error is logged in LifecycleObjectDomain
        }

        status = lifecycle.getStatusByName(statusName);
        if (status == null) {
            getUIDomain().showErrorMessage(
                    String.format("Status [%s] is incompatible with Change Record lifecycle [%s]", statusName, lifecycle.getName()),
                    "Lifecycle incompatibility");
            return;
        }

        Class clazz = getApiDomain().createClassInstance(getApiDomain().getCurrentProject());
        Package changeRecordsPackage = getConfigurationManagementService().getChangeRecordsPackage(true);
        ChangeRecord changeRecord = getConfigurationManagementService().initializeChangeRecord(clazz, name, stereotype, changeRecordsPackage, status);
        getConfigurationManagementService().setChangeRecordParametersForDataSource(clazz, source, sourceId, description);
        getConfigurationManagementService().resetChangeRecordSelectionAfterStatusChange(changeRecord, status);
        getApiDomain().setCurrentProjectHardDirty();
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        try {
            setEnabled(getConfigurationManagementService().isCmActive());
        } catch (Exception e) {
            getUIDomain().logErrorAndShowMessage(getLogger(), "Cannot update state for PullChangeRecordActionJIRA",
                    ExceptionConstants.ACTION_STATE_FAILURE, e);
        }
    }

    public JPanel getIssuesPanel(List<Map<String, String>> issues) {
        DefaultTableModel tableModel = prepareTableModel(issues, createDefaultTableModel(), false);
        actionsTable = createActionsTable(tableModel);
        actionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = createScrollPaneForActionsTable();
        JButton loadMore = setupLoadMoreButton();
        return createFinalPanel(scrollPane, loadMore);
    }

    protected DefaultTableModel createDefaultTableModel() {
        return new DefaultTableModel(); // used for unit testing
    }

    protected JTable createActionsTable(DefaultTableModel tableModel) {
        return new JTable(tableModel); // used for unit testing
    }

    protected JScrollPane createScrollPaneForActionsTable() {
        return new JScrollPane(actionsTable); // used for unit testing
    }

    protected JButton setupLoadMoreButton() {
        JButton loadMore = new JButton(LOAD_MORE);
        setupLoadMoreActionListener(loadMore);
        loadMore.setPreferredSize(new Dimension(50, 20));
        loadMore.setText(LOAD_MORE_EXTENDED);
        loadMore.setEnabled(true);
        return loadMore;
    }

    protected JPanel createFinalPanel(JScrollPane scrollPane, JButton loadMore) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setSize(500, 150);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(loadMore, BorderLayout.SOUTH);
        return panel;
    }

    protected void setupLoadMoreActionListener(JButton loadMore) {
        loadMore.addActionListener(a -> {
            List<Map<String, String>> newIssues;
            try {
                newIssues = getJiraIssueLiaison().getIssues();
            } catch (JiraIntegrationException ex) {
                getUIDomain().logError(getLogger(), ERROR_WHILE_PULLING_JIRA_ISSUES, ex);
                return;
            }

            if (newIssues.isEmpty()) {
                loadMore.setEnabled(false);
                getUIDomain().showPlainMessage(PluginConstant.ALL_JIRA_ISSUES_PULLED_MESSAGE, PluginConstant.ALL_JIRA_ISSUES_PULLED_TITLE);
                return;
            }

            // eliminating the issues that are already pulled
            removeAlreadyPulledIssues(newIssues, ID);
            issues.addAll(newIssues);

            DefaultTableModel tableModel = prepareTableModel(newIssues, (DefaultTableModel) (getActionsTable().getModel()), true);
            tableModel.fireTableDataChanged();
        });
    }

    protected JTable getActionsTable() {
        return actionsTable; // used for unit testing
    }

    protected void removeAlreadyPulledIssues(List<Map<String, String>> issueList, String key) {
        String changeRecordPath = getConfigurationManagementService().getChangeRecordsPackagePath();
        issueList.removeIf(issue -> getApiDomain().findClassRelativeToCurrentPrimary(changeRecordPath + "::" + issue.get(key)) != null);
    }

    protected DefaultTableModel prepareTableModel(List<Map<String, String>> issues, DefaultTableModel tableModel, boolean update) {
        if (!update) {
            Object[] columnNames = {"ID", "Name", "Type", "Status", "Creator"};
            tableModel.setColumnIdentifiers(columnNames);
        }

        List<Map<String, String>> tempList = new ArrayList<>();
        for (Map<String, String> map : issues) {
            Object[] row = {
                map.get("id"),
                map.get("name"),
                map.get("type"),
                map.get("status"),
                map.get("creator")};
            tableModel.addRow(row);
            if (update) {
                tempList.add(map);
            }
        }
        issues.addAll(tempList);
        return tableModel;
    }

    protected boolean verifyUserPermission(Stereotype stereotype) {
        // Checking if the user has the required permission
        String action = "pull";
        return getConfiguredElementDomain().canUserPerformAction(getConfigurationManagementService(), stereotype, action);
    }

    public Stereotype getCustomChangeRecordStereoType() {
        List<Stereotype> stereotypes = getConfigurationManagementService().getCustomChangeRecordStereotypes(PluginConstant.JIRA);
        return getConfigurationManagementService().userChoosesDesiredStereotype(stereotypes, "Select change record type",
                "Change record type selection");
    }
}
