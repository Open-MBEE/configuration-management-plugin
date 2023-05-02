package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.Liaison;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxIntegrationException;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.Lifecycle;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Project;
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

public class PullChangeRecordAction3Dx extends MDAction {
    private static final Logger logger = LoggerFactory.getLogger(PullChangeRecordAction3Dx.class);
    private static final String DS_6_W_IDENTIFIER = "ds6w:identifier";
    private static final String DS_6_W_WHAT_DS_6_W_STATUS = "ds6w:what/ds6w:status";
    private static final String RESOURCEID = "resourceid";
    private static final String DS_6_W_DESCRIPTION = "ds6w:description";
    private final transient ConfigurationManagementService configurationManagementService;
    protected transient List<Map<String, String>> changeActions;
    private transient Liaison threeDxChangeActionLiaison;
    private transient JTable actionsTable;

    public PullChangeRecordAction3Dx(ConfigurationManagementService configurationManagementService) {
        super("PULL_CHANGE_RECORD_ACTION", "Pull change record from 3Dx", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected ConfiguredElementDomain getConfiguredElementDomain() {
        return getConfigurationManagementService().getConfiguredElementDomain();
    }

    protected Logger getLogger() {
        return logger;
    }

    protected Liaison getThreeDxChangeActionLiaison() {
        if(threeDxChangeActionLiaison == null) {
            threeDxChangeActionLiaison = new Liaison(configurationManagementService.getThreeDxService());
        }
        return threeDxChangeActionLiaison;
    }

    protected JTable getActionsTable() {
        return actionsTable; // used for unit testing
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        try {
            Project project = getApiDomain().getCurrentProject();
            changeActions = getThreeDxChangeActionLiaison().getChangeActions();
            if(changeActions.isEmpty()) {
                getThreeDxChangeActionLiaison().reset();
                changeActions = getThreeDxChangeActionLiaison().getChangeActions();
            }

            // eliminating the actions that are already pulled
            removeAlreadyPulledActions(changeActions);
            if (changeActions.isEmpty()) {
                getUIDomain().showErrorMessage("No Change Actions found matching the search criteria", "3Dx integration");
                return;
            }

            if (!getUIDomain().isOkOption(getUIDomain().askForConfirmation(getChangeActionPanel(changeActions),
                    "Select the 3Dx Change Action to pull"))) {
                changeActions.clear();
                return;
            }

            int actionIndex = getActionsTable().getSelectedRow();
            if (actionIndex == -1) {
                getUIDomain().showErrorMessage("No Change Action was selected", "3Dx integration");
                changeActions.clear();
                return;
            }

            String id = changeActions.get(actionIndex).get(DS_6_W_IDENTIFIER);
            String statusName = changeActions.get(actionIndex).get(DS_6_W_WHAT_DS_6_W_STATUS);
            statusName = statusName.substring(statusName.indexOf('.') + 1);
            String source = PluginConstant.THREEDX_SOURCE;
            String sourceId = changeActions.get(actionIndex).get(RESOURCEID);
            String description = changeActions.get(actionIndex).get(DS_6_W_DESCRIPTION);
            changeActions.clear();

            List<Stereotype> stereotypes = getConfigurationManagementService().getCustomChangeRecordStereotypes(PluginConstant.THREEDX);
            Stereotype stereotype = getConfigurationManagementService().userChoosesDesiredStereotype(stereotypes,
                    "Select change record type", "Change record type selection");
            if(stereotype == null) {
                return;
            }

            // Checking if the user has the required permission
            String action = "pull";

            if(!getConfiguredElementDomain().canUserPerformAction(getConfigurationManagementService(), stereotype, action)) {
                getUIDomain().logErrorAndShowMessage(getLogger(), "Configured element failure for " + action,
                        "Pull change record failure");
                return;
            }

            // get the change management package and status
            Package changeRecordsPackage = getConfigurationManagementService().getChangeRecordsPackage(true);
            if(changeRecordsPackage == null) {
                getUIDomain().logErrorAndShowMessage(getLogger(), ExceptionConstants.CHANGE_RECORDS_PACKAGE_MISSING,
                        ExceptionConstants.PACKAGE_NOT_FOUND);
                return;
            }

            Lifecycle lifecycle = getConfigurationManagementService().getLifecycle(stereotype);
            if(lifecycle == null) {
                return; // a null lifecycle means an error shown by LifecycleObjectDomain
            }
            LifecycleStatus status = lifecycle.getStatusByName(statusName);
            if (status == null) {
                getUIDomain().showErrorMessage(String.format("Status [%s] is incompatible with Change Record lifecycle [%s]",
                        statusName, lifecycle.getName()), "Lifecycle incompatibility");
                return;
            }

            // Create Change Record
            Class clazz = project.getElementsFactory().createClassInstance();
            ChangeRecord changeRecord = getConfigurationManagementService().initializeChangeRecord(clazz, id, stereotype, changeRecordsPackage, status);
            getConfigurationManagementService().setChangeRecordParametersForDataSource(clazz, source, sourceId, description);
            getConfigurationManagementService().resetChangeRecordSelectionAfterStatusChange(changeRecord, status);
            getApiDomain().setCurrentProjectHardDirty();
        } catch (Exception e) {
            getUIDomain().logErrorAndShowMessage(getLogger(),
                    String.format(ExceptionConstants.THREEDX_CONNECTION_ISSUE, e.getMessage()),
                    "Communication error", e);
        }
    }

    protected void removeAlreadyPulledActions(List<Map<String, String>> actions) {
        for (int i = actions.size() - 1; i >= 0; i--) {
            if (getApiDomain().findClassRelativeToCurrentPrimary(
                    getConfigurationManagementService().getChangeRecordsPackagePath() + "::" +
                            actions.get(i).get(DS_6_W_IDENTIFIER)) != null) {
                actions.remove(i);
            }
        }
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        setEnabled(getConfigurationManagementService().isCmActive());
    }

    public JPanel getChangeActionPanel(List<Map<String, String>> changeActions) {
        DefaultTableModel tableModel = prepareTableModel(changeActions, createDefaultTableModel(), false);

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
        JButton loadMore = new JButton("Load more");
        setupLoadMoreActionListener(loadMore);
        loadMore.setPreferredSize(new Dimension(50, 20));
        loadMore.setText("Load more...");
        loadMore.setEnabled(true);
        return loadMore;
    }

    protected JPanel createFinalPanel(JScrollPane scrollPane, JButton loadMore) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(loadMore, BorderLayout.SOUTH);
        return panel;
    }

    protected void setupLoadMoreActionListener(JButton loadMore) {
        loadMore.addActionListener(a -> {
            List<Map<String, String>> newChangeActions;
            try {
                newChangeActions = getThreeDxChangeActionLiaison().getChangeActions();
            } catch (ThreeDxIntegrationException ex) {
                getUIDomain().logErrorAndShowMessage(getLogger(),
                        String.format(ExceptionConstants.THREEDX_CONNECTION_ISSUE, ex.getMessage()),
                        "Communication error", ex);
                return;
            }

            if (newChangeActions.isEmpty()) {
                loadMore.setEnabled(false);
                getUIDomain().showPlainMessage(PluginConstant.ALL_THREEDX_CHANGE_ACTIONS_PULLED_MESSAGE, PluginConstant.ALL_THREEDX_CHANGE_ACTIONS_PULLED_TITLE);
                return;
            }

            // eliminating the actions that are already pulled
            removeAlreadyPulledActions(newChangeActions);
            changeActions.addAll(newChangeActions);

            DefaultTableModel tableModel = prepareTableModel(newChangeActions, (DefaultTableModel) (getActionsTable().getModel()), true);
            tableModel.fireTableDataChanged();
        });
    }

    protected DefaultTableModel prepareTableModel(List<Map<String, String>> changeActions, DefaultTableModel tableModel, boolean update) {
        if(!update) {
            Object[] columnNames = {"Name", "Title", "Type", "Status", "Owner"};
            tableModel.setColumnIdentifiers(columnNames);
        }

        List<Map<String, String>> tempList = new ArrayList<>();
        for (Map<String, String> map : changeActions) {
            String status = map.get(DS_6_W_WHAT_DS_6_W_STATUS);
            status = status.substring(status.indexOf('.') + 1);

            Object[] row = {map.get(DS_6_W_IDENTIFIER),
                    map.get("ds6w:label"),
                    map.get("ds6w:what/ds6w:type"),
                    status,
                    map.get("ds6w:who/ds6w:responsible")};
            tableModel.addRow(row);
            if(update) {
                tempList.add(map);
            }
        }
        changeActions.addAll(tempList);
        return tableModel;
    }
}
