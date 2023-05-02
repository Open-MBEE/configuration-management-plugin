package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.exception.ConfigurationManagementException;
import org.openmbee.plugin.cfgmgmt.integration.Liaison;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxIntegrationException;
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
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestPullChangeRecordAction3Dx {
    private PullChangeRecordAction3Dx pullChangeRecordAction3Dx;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private Project project;
    private UIDomain uiDomain;
    private ConfiguredElementDomain configuredElementDomain;
    private ElementsFactory elementsFactory;
    private Logger logger;
    private ActionEvent actionEvent;
    private List<Map<String, String>> changeActions;
    private Map<String, String> actionMap;
    private JPanel jPanel;
    private JTable jTable;
    private Liaison liaison;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        pullChangeRecordAction3Dx = spy(new PullChangeRecordAction3Dx(configurationManagementService));
        liaison = mock(Liaison.class);
        apiDomain = mock(ApiDomain.class);
        project = mock(Project.class);
        uiDomain = mock(UIDomain.class);
        configuredElementDomain = mock(ConfiguredElementDomain.class);
        elementsFactory = mock(ElementsFactory.class);
        logger = mock(Logger.class);
        actionEvent = mock(ActionEvent.class);
        changeActions = new ArrayList<>();
        pullChangeRecordAction3Dx.changeActions = changeActions;
        actionMap = new HashMap<>();
        jPanel = mock(JPanel.class);
        jTable = mock(JTable.class);

        when(pullChangeRecordAction3Dx.getApiDomain()).thenReturn(apiDomain);
        doReturn(project).when(apiDomain).getCurrentProject();
        when(pullChangeRecordAction3Dx.getUIDomain()).thenReturn(uiDomain);
        when(uiDomain.isOkOption(JOptionPane.OK_OPTION)).thenReturn(true);
        when(pullChangeRecordAction3Dx.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
        doReturn(elementsFactory).when(project).getElementsFactory();
        when(pullChangeRecordAction3Dx.getLogger()).thenReturn(logger);
        when(pullChangeRecordAction3Dx.getConfigurationManagementService()).thenReturn(configurationManagementService);
        doReturn(liaison).when(pullChangeRecordAction3Dx).getThreeDxChangeActionLiaison();
        when(pullChangeRecordAction3Dx.getActionsTable()).thenReturn(jTable);
    }

    private void populateChangeActions(String id, String status, String resourceId, String description) {
        actionMap.put("ds6w:identifier", id);
        actionMap.put("ds6w:what/ds6w:status", status);
        actionMap.put("resourceid", resourceId);
        actionMap.put("ds6w:description", description);
        changeActions.add(actionMap);
    }

    @Test
    public void getThreeDxChangeActionLiaison() {
        doCallRealMethod().when(pullChangeRecordAction3Dx).getThreeDxChangeActionLiaison();
        assertNotNull(pullChangeRecordAction3Dx.getThreeDxChangeActionLiaison());
    }

    @Test
    public void actionPerformed_zeroChangeActions() {
        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(pullChangeRecordAction3Dx).removeAlreadyPulledActions(changeActions);;
            doReturn(jPanel).when(pullChangeRecordAction3Dx).getChangeActionPanel(changeActions);

            pullChangeRecordAction3Dx.actionPerformed(actionEvent);

            verify(uiDomain).showErrorMessage("No Change Actions found matching the search criteria",
                    "3Dx integration");
            verify(uiDomain, never()).askForConfirmation(jPanel, "Select the 3Dx Change Action to pull");
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_errorWhileGettingChangeActions() {
        String error = "error";
        ThreeDxIntegrationException integrationException = spy(new ThreeDxIntegrationException(error));
        try {
            doThrow(integrationException).when(liaison).getChangeActions();
            pullChangeRecordAction3Dx.actionPerformed(actionEvent);

            verify(uiDomain).logErrorAndShowMessage(logger,
                    String.format(ExceptionConstants.THREEDX_CONNECTION_ISSUE, error),
                    "Communication error", integrationException);
            verify(pullChangeRecordAction3Dx, never()).removeAlreadyPulledActions(changeActions);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_userDoesNotConfirm() {
        String id = "id";
        String status = "status";
        String resourceId = "resourceId";
        String description = "description";
        populateChangeActions(id, status, resourceId, description);
        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(pullChangeRecordAction3Dx).removeAlreadyPulledActions(changeActions);
            doReturn(jPanel).when(pullChangeRecordAction3Dx).getChangeActionPanel(changeActions);
            doReturn(JOptionPane.CLOSED_OPTION).when(uiDomain).askForConfirmation(jPanel, "Select the 3Dx Change Action to pull");

            pullChangeRecordAction3Dx.actionPerformed(actionEvent);

            verify(uiDomain).askForConfirmation(jPanel, "Select the 3Dx Change Action to pull");
            verify(jTable, never()).getSelectedRow();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_noSelectedRow() {
        String id = "id";
        String status = "status";
        String resourceId = "resourceId";
        String description = "description";
        populateChangeActions(id, status, resourceId, description);
        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(pullChangeRecordAction3Dx).removeAlreadyPulledActions(changeActions);
            doReturn(jPanel).when(pullChangeRecordAction3Dx).getChangeActionPanel(changeActions);
            doReturn(JOptionPane.OK_OPTION).when(uiDomain).askForConfirmation(jPanel, "Select the 3Dx Change Action to pull");
            doReturn(-1).when(jTable).getSelectedRow();

            pullChangeRecordAction3Dx.actionPerformed(actionEvent);

            verify(uiDomain).showErrorMessage("No Change Action was selected", "3Dx integration");
            verify(configurationManagementService, never()).getCustomChangeRecordStereotypes(PluginConstant.THREEDX);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_userCancelsSelectingStereotype() {
        String id = "id";
        String status = "stat.us";
        String resourceId = "resourceId";
        String description = "description";
        populateChangeActions(id, status, resourceId, description);
        List<Stereotype> stereotypes = new ArrayList<>();

        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(pullChangeRecordAction3Dx).removeAlreadyPulledActions(changeActions);
            doReturn(jPanel).when(pullChangeRecordAction3Dx).getChangeActionPanel(changeActions);
            doReturn(JOptionPane.OK_OPTION).when(uiDomain).askForConfirmation(jPanel, "Select the 3Dx Change Action to pull");
            doReturn(0).when(jTable).getSelectedRow();
            doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.THREEDX);
            doReturn(null).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
                    "Select change record type", "Change record type selection");

            pullChangeRecordAction3Dx.actionPerformed(actionEvent);

            verify(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
                    "Select change record type", "Change record type selection");
            verify(configuredElementDomain, never()).canUserPerformAction(any(), any(), anyString());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_userLacksPermission() {
        String id = "id";
        String status = "stat.us";
        String resourceId = "resourceId";
        String description = "description";
        populateChangeActions(id, status, resourceId, description);
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        stereotypes.add(stereotype);
        String action = "pull";
        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(pullChangeRecordAction3Dx).removeAlreadyPulledActions(changeActions);
            doReturn(jPanel).when(pullChangeRecordAction3Dx).getChangeActionPanel(changeActions);
            doReturn(JOptionPane.OK_OPTION).when(uiDomain).askForConfirmation(jPanel, "Select the 3Dx Change Action to pull");
            doReturn(0).when(jTable).getSelectedRow();
            doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.THREEDX);
            doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
                    "Select change record type", "Change record type selection");
            doReturn(false).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);

            pullChangeRecordAction3Dx.actionPerformed(actionEvent);

            verify(uiDomain).logErrorAndShowMessage(logger, "Configured element failure for " + action,
                    "Pull change record failure");
            verify(configurationManagementService, never()).getChangeRecordsPackage(true);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_changeRecordsPackageNotFound() {
        String id = "id";
        String status = "stat.us";
        String resourceId = "resourceId";
        String description = "description";
        populateChangeActions(id, status, resourceId, description);
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        stereotypes.add(stereotype);
        String action = "pull";
        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(pullChangeRecordAction3Dx).removeAlreadyPulledActions(changeActions);
            doReturn(jPanel).when(pullChangeRecordAction3Dx).getChangeActionPanel(changeActions);
            doReturn(JOptionPane.OK_OPTION).when(uiDomain).askForConfirmation(jPanel, "Select the 3Dx Change Action to pull");
            doReturn(0).when(jTable).getSelectedRow();
            doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.THREEDX);
            doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
                    "Select change record type", "Change record type selection");
            doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
            doReturn(null).when(configurationManagementService).getChangeRecordsPackage(true);
            doNothing().when(uiDomain).logErrorAndShowMessage(logger, ExceptionConstants.CHANGE_RECORDS_PACKAGE_MISSING,
                    ExceptionConstants.PACKAGE_NOT_FOUND);

            pullChangeRecordAction3Dx.actionPerformed(actionEvent);

            verify(uiDomain).logErrorAndShowMessage(logger, ExceptionConstants.CHANGE_RECORDS_PACKAGE_MISSING,
                    ExceptionConstants.PACKAGE_NOT_FOUND);
            verify(configurationManagementService, never()).getLifecycle(stereotype);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_lifecycleNull() {
        String id = "id";
        String status = "stat.us";
        String statusName = status.substring(status.indexOf(".") + 1);
        String resourceId = "resourceId";
        String description = "description";
        populateChangeActions(id, status, resourceId, description);
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        Lifecycle lifecycle = mock(Lifecycle.class);
        stereotypes.add(stereotype);
        String action = "pull";
        Package pkg = mock(Package.class);
        String lifecycleName = "lifecycleName";
        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(pullChangeRecordAction3Dx).removeAlreadyPulledActions(changeActions);
            doReturn(jPanel).when(pullChangeRecordAction3Dx).getChangeActionPanel(changeActions);
            doReturn(JOptionPane.OK_OPTION).when(uiDomain).askForConfirmation(jPanel, "Select the 3Dx Change Action to pull");
            doReturn(0).when(jTable).getSelectedRow();
            doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.THREEDX);
            doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
                    "Select change record type", "Change record type selection");
            doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(true);
            doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
            doReturn(null).when(configurationManagementService).getLifecycle(stereotype);

            pullChangeRecordAction3Dx.actionPerformed(actionEvent);

            verify(configurationManagementService).getChangeRecordsPackage(true);
            verify(apiDomain, never()).setCurrentProjectHardDirty();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_lifecycleStatusNull() {
        String id = "id";
        String status = "stat.us";
        String statusName = status.substring(status.indexOf(".") + 1);
        String resourceId = "resourceId";
        String description = "description";
        populateChangeActions(id, status, resourceId, description);
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        Lifecycle lifecycle = mock(Lifecycle.class);
        stereotypes.add(stereotype);
        String action = "pull";
        Package pkg = mock(Package.class);
        String lifecycleName = "lifecycleName";
        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(pullChangeRecordAction3Dx).removeAlreadyPulledActions(changeActions);
            doReturn(jPanel).when(pullChangeRecordAction3Dx).getChangeActionPanel(changeActions);
            doReturn(JOptionPane.OK_OPTION).when(uiDomain).askForConfirmation(jPanel, "Select the 3Dx Change Action to pull");
            doReturn(0).when(jTable).getSelectedRow();
            doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.THREEDX);
            doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
                    "Select change record type", "Change record type selection");
            doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(true);
            doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
            doReturn(lifecycle).when(configurationManagementService).getLifecycle(stereotype);
            doReturn(null).when(lifecycle).getStatusByName(statusName);
            doReturn(lifecycleName).when(lifecycle).getName();
            pullChangeRecordAction3Dx.actionPerformed(actionEvent);

            verify(uiDomain).showErrorMessage(String.format("Status [%s] is incompatible with Change Record lifecycle [%s]",
                statusName, lifecycle.getName()), "Lifecycle incompatibility");
            verify(apiDomain, never()).setCurrentProjectHardDirty();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_changeRecordCreated() {
        String id = "id";
        String status = "stat.us";
        String statusName = status.substring(status.indexOf(".") + 1);
        String resourceId = "resourceId";
        String description = "description";
        populateChangeActions(id, status, resourceId, description);
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        stereotypes.add(stereotype);
        String action = "pull";
        Lifecycle lifecycle = mock(Lifecycle.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        Class changeRecord = mock(Class.class);
        String error = "error";
        Package pkg = mock(Package.class);
        ConfigurationManagementException managementException = spy(new ConfigurationManagementException(error));
        ChangeRecord newChangeRecord = mock(ChangeRecord.class);
        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(pullChangeRecordAction3Dx).removeAlreadyPulledActions(changeActions);
            doReturn(jPanel).when(pullChangeRecordAction3Dx).getChangeActionPanel(changeActions);
            doReturn(JOptionPane.OK_OPTION).when(uiDomain).askForConfirmation(jPanel, "Select the 3Dx Change Action to pull");
            doReturn(0).when(jTable).getSelectedRow();
            doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.THREEDX);
            doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
                    "Select change record type", "Change record type selection");
            doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
            doReturn(lifecycle).when(configurationManagementService).getLifecycle(stereotype);
            doReturn(lifecycleStatus).when(lifecycle).getStatusByName(statusName);
            doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(true);
            doReturn(changeRecord).when(elementsFactory).createClassInstance();
            doReturn(newChangeRecord).when(configurationManagementService).initializeChangeRecord(changeRecord, id,
                    stereotype, pkg, lifecycleStatus);
            doNothing().when(configurationManagementService).setChangeRecordParametersForDataSource(changeRecord,
                    PluginConstant.THREEDX_SOURCE, resourceId, description);

            pullChangeRecordAction3Dx.actionPerformed(actionEvent);

            verify(apiDomain).setCurrentProjectHardDirty();
            verify(uiDomain, never()).logErrorAndShowMessage(logger, "3Dx connection issues",
                    "Communication error", managementException);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void removeAlreadyPulledActions() {
        changeActions.add(actionMap);
        String crPath = "cr::Path";
        String ds6wId = "ds6wId";
        actionMap.put("ds6w:identifier", ds6wId);
        String primary = crPath + "::" + ds6wId;
        Class relative = mock(Class.class);

        doReturn(crPath).when(configurationManagementService).getChangeRecordsPackagePath();
        doReturn(relative).when(apiDomain).findClassRelativeToCurrentPrimary(primary);

        assertFalse(changeActions.isEmpty());
        pullChangeRecordAction3Dx.removeAlreadyPulledActions(changeActions);
        assertTrue(changeActions.isEmpty());
    }

    @Test
    public void removeAlreadyPulledActions_null() {
        changeActions.add(actionMap);
        String crPath = "cr::Path";
        String Id = "id";
        actionMap.put("id", Id);
        String primary = crPath + "::" + Id;

        doReturn(crPath).when(configurationManagementService).getChangeRecordsPackagePath();
        doReturn(null).when(apiDomain).findClassRelativeToCurrentPrimary(primary);

        pullChangeRecordAction3Dx.removeAlreadyPulledActions(changeActions);
        assertFalse(changeActions.isEmpty());
    }

    @Test
    public void updateState() {
        doReturn(true).when(configurationManagementService).isCmActive();

        pullChangeRecordAction3Dx.updateState();

        verify(pullChangeRecordAction3Dx).setEnabled(true);
    }

    @Test
    public void getChangeActionPanel() {
        DefaultTableModel defaultTableModel = mock(DefaultTableModel.class);
        JScrollPane scrollPane = mock(JScrollPane.class);
        JButton loadMore = mock(JButton.class);

        when(pullChangeRecordAction3Dx.createDefaultTableModel()).thenReturn(defaultTableModel);
        doReturn(defaultTableModel).when(pullChangeRecordAction3Dx).prepareTableModel(changeActions, defaultTableModel, false);
        doReturn(jTable).when(pullChangeRecordAction3Dx).createActionsTable(defaultTableModel);
        when(pullChangeRecordAction3Dx.createScrollPaneForActionsTable()).thenReturn(scrollPane);
        when(pullChangeRecordAction3Dx.setupLoadMoreButton()).thenReturn(loadMore);
        doReturn(jPanel).when(pullChangeRecordAction3Dx).createFinalPanel(scrollPane, loadMore);

        JPanel result = pullChangeRecordAction3Dx.getChangeActionPanel(changeActions);

        assertSame(jPanel, result);
    }

    @Test
    public void setupLoadMoreActionListener_errorDuringAction() {
        JButton loadMore = spy(new JButton());
        String error = "error";
        ThreeDxIntegrationException threeDxIntegrationException = spy(new ThreeDxIntegrationException(error));

        try {
            doThrow(threeDxIntegrationException).when(liaison).getChangeActions();

            pullChangeRecordAction3Dx.setupLoadMoreActionListener(loadMore);
            loadMore.doClick();

            verify(uiDomain).logErrorAndShowMessage(logger,
                    String.format(ExceptionConstants.THREEDX_CONNECTION_ISSUE, error),
                    "Communication error", threeDxIntegrationException);
            verify(loadMore, never()).setEnabled(false);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Unexpected exception");
        }
    }

    @Test
    public void setupLoadMoreActionListener_emptyActionList() {
        JButton loadMore = spy(new JButton());
        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(uiDomain).showPlainMessage(PluginConstant.ALL_THREEDX_CHANGE_ACTIONS_PULLED_MESSAGE, PluginConstant.ALL_THREEDX_CHANGE_ACTIONS_PULLED_TITLE);

            pullChangeRecordAction3Dx.setupLoadMoreActionListener(loadMore);
            loadMore.doClick();

            assertFalse(loadMore.isEnabled());
            verify(uiDomain).showPlainMessage(PluginConstant.ALL_THREEDX_CHANGE_ACTIONS_PULLED_MESSAGE, PluginConstant.ALL_THREEDX_CHANGE_ACTIONS_PULLED_TITLE);
            verify(pullChangeRecordAction3Dx, never()).removeAlreadyPulledActions(changeActions);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void setupLoadMoreActionListener_newActionsLoaded() {
        JButton loadMore = spy(new JButton());
        String id = "id";
        String status = "stat.us";
        String resourceId = "resourceId";
        String description = "description";
        populateChangeActions(id, status, resourceId, description);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        try {
            doReturn(changeActions).when(liaison).getChangeActions();
            doNothing().when(pullChangeRecordAction3Dx).removeAlreadyPulledActions(changeActions);
            doReturn(tableModel).when(jTable).getModel();
            doReturn(tableModel).when(pullChangeRecordAction3Dx).prepareTableModel(changeActions, tableModel, true);
            doNothing().when(tableModel).fireTableDataChanged();
            pullChangeRecordAction3Dx.setupLoadMoreActionListener(loadMore);
            loadMore.doClick();

            verify(tableModel).fireTableDataChanged();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void prepareTableModel_newTableModel() {
        String id = "id";
        String label = "label";
        String type = "type";
        String status = "stat.us";
        String owner = "owner";
        populateChangeActionsForTableModel(id, label, type, status, owner);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        Object[] columnNames = {"Name", "Title", "Type", "Status", "Owner"};
        Object[] row = makeRowForTableModel(id, label, type, status, owner);

        DefaultTableModel result = pullChangeRecordAction3Dx.prepareTableModel(changeActions, tableModel, false);

        assertSame(tableModel, result);
        verify(tableModel).setColumnIdentifiers(columnNames);
        verify(tableModel).addRow(row);
    }

    @Test
    public void prepareTableModel_updateTableModel() {
        String id = "id";
        String label = "label";
        String type = "type";
        String status = "stat.us";
        String owner = "owner";
        populateChangeActionsForTableModel(id, label, type, status, owner);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        Object[] columnNames = {"Name", "Title", "Type", "Status", "Owner"};
        Object[] row = makeRowForTableModel(id, label, type, status, owner);

        doNothing().when(tableModel).setColumnIdentifiers(columnNames);
        doNothing().when(tableModel).addRow(row);

        DefaultTableModel result = pullChangeRecordAction3Dx.prepareTableModel(changeActions, tableModel, true);

        assertSame(tableModel, result);
        verify(tableModel, never()).setColumnIdentifiers(columnNames);
        verify(tableModel).addRow(row);
    }

    private void populateChangeActionsForTableModel(String id, String label, String type, String status, String owner) {
        actionMap.put("ds6w:identifier", id);
        actionMap.put("ds6w:label", label);
        actionMap.put("ds6w:what/ds6w:type", type);
        actionMap.put("ds6w:what/ds6w:status", status);
        actionMap.put("ds6w:who/ds6w:responsible", owner);
        changeActions.add(actionMap);
    }

    private Object[] makeRowForTableModel(String id, String label, String type, String status, String owner) {
        String statusName = status.substring(status.indexOf(".") + 1);
        return new Object[] {id, label, type, statusName, owner};
    }
}
