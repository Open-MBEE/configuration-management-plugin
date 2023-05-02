package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.actions.bulk.ChangeStatusBulkAction;
import org.openmbee.plugin.cfgmgmt.actions.bulk.ConfigureBulkAction;
import org.openmbee.plugin.cfgmgmt.actions.bulk.LockAction;
import org.openmbee.plugin.cfgmgmt.actions.bulk.ReviseBulkAction;
import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.exception.ConfigurationManagementMissingStatusException;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestManageAffectedElementsAction {
    private ChangeRecord changeRecord;
    private ConfigurationManagementService configurationManagementService;
    private UIDomain uiDomain;
    private ApiDomain apiDomain;
    private ActionEvent event;
    private Logger logger;
    private JPanel jpanel;
    private LifecycleObjectFactory lifecycleObjectFactory;
    private ManageAffectedElementsAction manageAffectedElementsAction;

    private JButton jbutton;
    private JButton changeCEStatus;
    private JButton revise;
    private JButton configure;
    private JButton changeCRStatus;
    private JButton lock;
    private ChangeStatusBulkAction changeCEStatusAction;
    private ReviseBulkAction reviseAction;
    private ConfigureBulkAction configureAction;
    private ChangeStatusAction changeCRStatusAction;
    private LockAction lockAction;
    private JTable table;
    private DefaultTableModel defaultTableModel;
    private JScrollPane jscrollPane;
    private BorderLayout borderLayout;
    private ListSelectionEvent listSelectionEvent;
    private List<ConfiguredElement> configuredElementList;
    ManageAffectedElementsAction.MyListener myListener;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        changeRecord = mock(ChangeRecord.class);
        uiDomain = mock(UIDomain.class);
        apiDomain = mock(ApiDomain.class);
        jpanel = mock(JPanel.class);
        lifecycleObjectFactory = mock(LifecycleObjectFactory.class);
        event = mock(ActionEvent.class);
        logger = mock(Logger.class);
        manageAffectedElementsAction = spy(new ManageAffectedElementsAction(configurationManagementService));

        jbutton = mock(JButton.class);
        changeCRStatus = mock(JButton.class);
        revise = mock(JButton.class);
        configure = mock(JButton.class);
        lock = mock(JButton.class);
        changeCEStatus = mock(JButton.class);
        table = mock(JTable.class);
        changeCEStatusAction = mock(ChangeStatusBulkAction.class);
        reviseAction = mock(ReviseBulkAction.class);
        configureAction = mock(ConfigureBulkAction.class);
        changeCRStatusAction = mock(ChangeStatusAction.class);
        lockAction = mock(LockAction.class);
        jscrollPane = mock(JScrollPane.class);
        defaultTableModel = mock(DefaultTableModel.class);
        borderLayout = mock(BorderLayout.class);
        listSelectionEvent = mock(ListSelectionEvent.class);
        configuredElementList = new ArrayList<>();
        myListener = spy(manageAffectedElementsAction.new MyListener(changeCEStatus, revise, configure, changeCRStatus,
                lock, changeCEStatusAction, reviseAction, configureAction, lockAction, table));

        when(manageAffectedElementsAction.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(manageAffectedElementsAction.getUIDomain()).thenReturn(uiDomain);
        when(manageAffectedElementsAction.getApiDomain()).thenReturn(apiDomain);
        when(manageAffectedElementsAction.getCurrentChangeRecord()).thenReturn(changeRecord);
        when(manageAffectedElementsAction.getLifecycleObjectFactory()).thenReturn(lifecycleObjectFactory);
        when(manageAffectedElementsAction.getLogger()).thenReturn(logger);
        when(manageAffectedElementsAction.getMyListener()).thenReturn(myListener);
        when(manageAffectedElementsAction.getConfigureAction()).thenReturn(configureAction);
        when(manageAffectedElementsAction.getChangeCRStatusAction()).thenReturn(changeCRStatusAction);
        when(manageAffectedElementsAction.getReviseAction()).thenReturn(reviseAction);
        when(manageAffectedElementsAction.getChangeCEStatusAction()).thenReturn(changeCEStatusAction);
        when(manageAffectedElementsAction.createTableModel()).thenReturn(defaultTableModel);
        when(myListener.createSelectedElementsArray()).thenReturn(configuredElementList);
        when(manageAffectedElementsAction.createBorderLayout()).thenReturn(borderLayout);
        when(manageAffectedElementsAction.createButton(anyString())).thenReturn(jbutton);

    }

   @Test
    public void actionPerformed() {
        String title = "Affected Element Management";

        doReturn(changeRecord).when(manageAffectedElementsAction).getChangeRecord();
        doReturn(jpanel).when(manageAffectedElementsAction).getAffectedElementsPanel();

        manageAffectedElementsAction.actionPerformed(event);

        verify(uiDomain).showPlainMessage(jpanel, title);
    }

    @Test
    public void updateState_NullElement() {
        doReturn(null).when(manageAffectedElementsAction).getSelectedObjectOverride();
        doNothing().when(manageAffectedElementsAction).setEnabled(false);

        manageAffectedElementsAction.updateState();

        verify(manageAffectedElementsAction).setEnabled(false);
    }

    @Test
    public void updateState_NotElement() {
        doReturn(new Object()).when(manageAffectedElementsAction).getSelectedObjectOverride();
        doNothing().when(manageAffectedElementsAction).setEnabled(false);

        manageAffectedElementsAction.updateState();

        verify(manageAffectedElementsAction).setEnabled(false);
    }

    @Test
    public void updateState_ElementNotEditable() {
        Package innerPkg = mock(Package.class);

        doReturn(innerPkg).when(manageAffectedElementsAction).getSelectedObjectOverride();
        doReturn(false).when(apiDomain).isElementInEditableState(innerPkg);
        doNothing().when(manageAffectedElementsAction).setEnabled(false);

        manageAffectedElementsAction.updateState();

        verify(manageAffectedElementsAction).setEnabled(false);
    }

    @Test
    public void updateState_NullChangeRecord() {
        Package innerPkgElement = mock(Package.class);

        doReturn(innerPkgElement).when(manageAffectedElementsAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(innerPkgElement);
        doReturn(null).when(configurationManagementService).getChangeRecord(innerPkgElement);
        doNothing().when(manageAffectedElementsAction).setEnabled(false);

        manageAffectedElementsAction.updateState();

        verify(manageAffectedElementsAction).setEnabled(false);
    }

    @Test
    public void updateState_UnmatchedCRAndActiveCR() {
        Package innerPkgElement = mock(Package.class);
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        ChangeRecord selectedChangeRecord = mock(ChangeRecord.class);

        doReturn(innerPkgElement).when(manageAffectedElementsAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(innerPkgElement);
        doReturn(changeRecord).when(configurationManagementService).getChangeRecord(innerPkgElement);
        doReturn(selectedChangeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doNothing().when(manageAffectedElementsAction).setEnabled(false);

        manageAffectedElementsAction.updateState();

        verify(manageAffectedElementsAction).setEnabled(false);
    }


    @Test
    public void updateState_MatchingCRAndActiveCR_ChangeRecordNotReleased() {
        Package innerPkgElement = mock(Package.class);
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        try{
            doReturn(innerPkgElement).when(manageAffectedElementsAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(innerPkgElement);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(innerPkgElement);
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(false).when(changeRecord).isReleased();

            manageAffectedElementsAction.updateState();

            verify(manageAffectedElementsAction , never()).setEnabled(false);
            verify(changeRecord).isReleased();
        } catch(Exception e) {
            fail("Unexpected exception");
        }

    }

    @Test
    public void updateState_MatchingCRAndActiveCR_ChangeRecordReleased() {
        Package innerPkgElement = mock(Package.class);
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        try{
            doReturn(innerPkgElement).when(manageAffectedElementsAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(innerPkgElement);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(innerPkgElement);
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(changeRecord).isReleased();

            manageAffectedElementsAction.updateState();

            verify(manageAffectedElementsAction).setEnabled(false);
            verify(changeRecord).isReleased();
          } catch(Exception e) {
              fail("Unexpected exception");
          }

    }


    @Test
    public void updateState_ActiveChangeRecordNullAndMissingStatus() {
        Package innerPkgElement = mock(Package.class);
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        
        try {
            doReturn(innerPkgElement).when(manageAffectedElementsAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(innerPkgElement);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(innerPkgElement);
            doReturn(null).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(changeRecord).isReleased();

            manageAffectedElementsAction.updateState();

            verify(manageAffectedElementsAction).setEnabled(false);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_ActiveChangeRecordSameAndNotReleased() {
        Package innerPkgElement = mock(Package.class);
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        String error = "error";
        ConfigurationManagementMissingStatusException missingStatusException = spy(new ConfigurationManagementMissingStatusException(error));

        try {
            doReturn(innerPkgElement).when(manageAffectedElementsAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(innerPkgElement);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(innerPkgElement);
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(false).when(changeRecord).isReleased();
            doNothing().when(manageAffectedElementsAction).setEnabled(true);

            manageAffectedElementsAction.updateState();

            verify(manageAffectedElementsAction).setEnabled(true);
            verify(uiDomain, never()).logErrorAndShowMessage(logger, "Error attempting to update state in ManageAffectedElementsAction",
                    ExceptionConstants.ACTION_STATE_FAILURE, missingStatusException);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void fillTableModelContents_noChangeRecord() {
        DefaultTableModel defaultTableModel = mock(DefaultTableModel.class);
        try {
            doReturn(null).when(manageAffectedElementsAction).getChangeRecord();

            manageAffectedElementsAction.fillTableModelContents(defaultTableModel);

            verify(changeRecord, never()).getAffectedElements();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void fillTableModelContents_emptyStatus() {
        DefaultTableModel defaultTableModel = mock(DefaultTableModel.class);
        List<ConfiguredElement> affectedElements = new ArrayList<>();
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);
        affectedElements.add(configuredElement);
        NamedElement namedElement = mock(NamedElement.class);
        String lockUser = "user";
        Optional<LifecycleStatus> status = Optional.empty();

        try {
            doReturn(changeRecord).when(manageAffectedElementsAction).getChangeRecord();
            when(changeRecord.getAffectedElements()).thenReturn(affectedElements);
            doReturn(namedElement).when(configuredElement).getElement();
            doReturn(lockUser).when(apiDomain).getLockingUser(namedElement);
            doReturn(status).when(configuredElement).getStatus();

            manageAffectedElementsAction.fillTableModelContents(defaultTableModel);

            verify(configuredElement, never()).getID();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void fillTableModelContents_rowAdded() {
        DefaultTableModel defaultTableModel = mock(DefaultTableModel.class);
        List<ConfiguredElement> affectedElements = new ArrayList<>();
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);
        affectedElements.add(configuredElement);
        NamedElement namedElement = mock(NamedElement.class);
        String lockUser = "user";
        String statusName = "statusName";
        State state = mock(State.class);
        LifecycleStatus lifecycleStatus = new LifecycleStatus(configurationManagementService , state);
        Optional<LifecycleStatus> status = Optional.of(lifecycleStatus);
        String id = "id";
        String revision = "revision";
        String elementName = "elementName";
        Stereotype stereotype = mock(Stereotype.class);
        String stereotypeName = "stereotypeName";
        Object[] row = new Object[] {configuredElement, id, revision, elementName, stereotypeName, statusName, lockUser};

        try {
            doReturn(changeRecord).when(manageAffectedElementsAction).getChangeRecord();
            doReturn(affectedElements).when(changeRecord).getAffectedElements();
            doReturn(namedElement).when(configuredElement).getElement();
            doReturn(lockUser).when(apiDomain).getLockingUser(namedElement);
            doReturn(status).when(configuredElement).getStatus();
            doReturn(id).when(configuredElement).getID();
            doReturn(revision).when(configuredElement).getRevision();
            doReturn(elementName).when(namedElement).getName();
            doReturn(stereotype).when(configuredElement).getAppliedStereotype();
            doReturn(stereotypeName).when(stereotype).getName();
            doReturn(statusName).when(state).getName();

            manageAffectedElementsAction.fillTableModelContents(defaultTableModel);

            verify(defaultTableModel).addRow(row);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void setButtonsActive_changeRecordExpendableAndReleased_HasStatus() {
        int[] selectedRows = new int[] {1};
        List<ConfiguredElement> selectedElements = new ArrayList<>();
        TableModel tableModel = mock(TableModel.class);
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);
        ChangeRecord activeRecord = mock(ChangeRecord.class);

        try {
            doReturn(selectedRows).when(table).getSelectedRows();
            doReturn(tableModel).when(table).getModel();
            doReturn(selectedElements).when(myListener).createSelectedElementsArray();
            doReturn(configuredElement).when(tableModel).getValueAt(1, 0);
            doReturn(activeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(changeRecord).when(manageAffectedElementsAction).getChangeRecord();
            doReturn(true).when(changeRecord).isExpandable();
            doReturn(true).when(changeRecord).hasStatus();
            doReturn(true).when(changeRecord).isReleased();
            doReturn(true).when(changeRecord).hasAvailableTransitions();
            doReturn(true).when(lockAction).isEnabled();

            myListener.setButtonsActive();

            verify(revise).setEnabled(true);
            verify(revise, never()).setEnabled(false);
            verify(changeCEStatus).setEnabled(false);
            verify(changeCRStatus).setEnabled(true);
            verify(changeCRStatus, never()).setEnabled(false);
            verify(lock).setEnabled(true);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void setButtonsActive_changeRecordNotExpendableAndNotReleasedButMatchesActive() {
        int[] selectedRows = new int[] {1};
        List<ConfiguredElement> selectedElements = new ArrayList<>();
        TableModel tableModel = mock(TableModel.class);
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);

        try {
            doReturn(selectedRows).when(table).getSelectedRows();
            doReturn(tableModel).when(table).getModel();
            doReturn(selectedElements).when(myListener).createSelectedElementsArray();
            doReturn(configuredElement).when(tableModel).getValueAt(1, 0);
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(changeRecord).when(manageAffectedElementsAction).getChangeRecord();
            doReturn(false).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReleased();
            doReturn(false).when(changeRecord).hasStatus();
            doNothing().when(changeCEStatusAction).setEnabled(any());
            doReturn(true).when(changeCEStatusAction).isEnabled();
            doReturn(false).when(changeRecord).hasAvailableTransitions();
            doReturn(false).when(lockAction).isEnabled();

            myListener.setButtonsActive();

            verify(revise).setEnabled(false);
            verify(revise, never()).setEnabled(true);
            verify(changeCEStatus).setEnabled(true);
            verify(changeCRStatus).setEnabled(false);
            verify(changeCRStatus, never()).setEnabled(true);
            verify(lock).setEnabled(false);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }


    @Test
    public void setButtonsActive_changeRecordNotExpendableAndNotReleased_HasActiveCRStatus() {
        int[] selectedRows = new int[] {1};
        List<ConfiguredElement> selectedElements = new ArrayList<>();
        TableModel tableModel = mock(TableModel.class);
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);

        try {
            doReturn(selectedRows).when(table).getSelectedRows();
            doReturn(tableModel).when(table).getModel();
            doReturn(selectedElements).when(myListener).createSelectedElementsArray();
            doReturn(configuredElement).when(tableModel).getValueAt(1, 0);
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(changeRecord).when(manageAffectedElementsAction).getChangeRecord();
            doReturn(false).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReleased();
            doReturn(true).when(changeRecord).hasStatus();
            doNothing().when(changeCEStatusAction).setEnabled(any());
            doReturn(true).when(changeCEStatusAction).isEnabled();
            doReturn(false).when(changeRecord).hasAvailableTransitions();
            doReturn(false).when(lockAction).isEnabled();

            myListener.setButtonsActive();

            verify(revise).setEnabled(false);
            verify(revise, never()).setEnabled(true);
            verify(changeCEStatus).setEnabled(true);
            verify(changeCRStatus).setEnabled(false);
            verify(changeCRStatus, never()).setEnabled(true);
            verify(lock).setEnabled(false);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }


    @Test
    public void setButtonsActive_changeRecordExpendableAndNotReleased_HasNoActiveCRStatus() {
        int[] selectedRows = new int[] {1};
        List<ConfiguredElement> selectedElements = new ArrayList<>();
        TableModel tableModel = mock(TableModel.class);
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);

        try {
            doReturn(selectedRows).when(table).getSelectedRows();
            doReturn(tableModel).when(table).getModel();
            doReturn(selectedElements).when(myListener).createSelectedElementsArray();
            doReturn(configuredElement).when(tableModel).getValueAt(1, 0);
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(changeRecord).when(manageAffectedElementsAction).getChangeRecord();
            doReturn(true).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReleased();
            doReturn(false).when(changeRecord).hasStatus();
            doNothing().when(changeCEStatusAction).setEnabled(any());
            doReturn(true).when(changeCEStatusAction).isEnabled();
            doReturn(false).when(changeRecord).hasAvailableTransitions();
            doReturn(false).when(lockAction).isEnabled();

            myListener.setButtonsActive();

            verify(revise).setEnabled(false);
            verify(revise, never()).setEnabled(true);
            verify(changeCEStatus).setEnabled(true);
            verify(changeCRStatus).setEnabled(false);
            verify(changeCRStatus, never()).setEnabled(true);
            verify(lock).setEnabled(false);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void setButtonsActive_changeRecordNotExpendableAndNotReleasedButDoesNotMatchesActive() {
        int[] selectedRows = new int[] {1};
        List<ConfiguredElement> selectedElements = new ArrayList<>();
        TableModel tableModel = mock(TableModel.class);
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);
        ChangeRecord activeChangeRecord  = mock(ChangeRecord.class);

        try {
            doReturn(selectedRows).when(table).getSelectedRows();
            doReturn(tableModel).when(table).getModel();
            doReturn(selectedElements).when(myListener).createSelectedElementsArray();
            doReturn(configuredElement).when(tableModel).getValueAt(1, 0);
            doReturn(activeChangeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(changeRecord).when(manageAffectedElementsAction).getChangeRecord();
            doReturn(false).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReleased();
            doReturn(false).when(changeRecord).hasAvailableTransitions();
            doReturn(false).when(lockAction).isEnabled();

            myListener.setButtonsActive();

            verify(changeCEStatus  , never()).setEnabled(true);
            verify(changeCEStatusAction  , never()).isEnabled();
            verify(changeCEStatusAction  , never()).setEnabled(any());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    private void actionListenerBasicSetup(DefaultTableModel tableModel) {
        doNothing().when(manageAffectedElementsAction).fillTableModelContents(tableModel);
    }

    @Test
    public void changeCEStatusActionListener() {
        changeCEStatus = spy(new JButton());
        ActionEvent actionEvent = mock(ActionEvent.class);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        ListSelectionModel selectionModel = mock(ListSelectionModel.class);

        actionListenerBasicSetup(tableModel);

        JButton result = manageAffectedElementsAction.changeCEStatusActionListener(changeCEStatus, changeCEStatusAction, tableModel, selectionModel);
        changeCEStatus.doClick();

        assertSame(changeCEStatus, result);
        verify(selectionModel).clearSelection();
    }

    @Test
    public void reviseActionListener() {
        revise = spy(new JButton());
        ActionEvent actionEvent = mock(ActionEvent.class);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        ListSelectionModel selectionModel = mock(ListSelectionModel.class);

        doReturn(changeRecord).when(manageAffectedElementsAction).getCurrentChangeRecord();
        actionListenerBasicSetup(tableModel);

        JButton result = manageAffectedElementsAction.reviseActionListener(revise, reviseAction, tableModel, selectionModel);
        revise.doClick();

        assertSame(revise, result);
        verify(selectionModel).clearSelection();
    }

    @Test
    public void configureActionListener() {
        configure = spy(new JButton());
        ActionEvent actionEvent = mock(ActionEvent.class);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        ListSelectionModel selectionModel = mock(ListSelectionModel.class);

        doReturn(changeRecord).when(manageAffectedElementsAction).getCurrentChangeRecord();
        actionListenerBasicSetup(tableModel);

        JButton result = manageAffectedElementsAction.configureActionListener(configure, configureAction, tableModel, selectionModel);
        configure.doClick();

        assertSame(configure, result);
        verify(selectionModel).clearSelection();
    }

    @Test
    public void changeCRStatusActionListener() {
        changeCRStatus = spy(new JButton());
        ActionEvent actionEvent = mock(ActionEvent.class);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        ListSelectionModel selectionModel = mock(ListSelectionModel.class);

        doNothing().when(myListener).setButtonsActive();
        actionListenerBasicSetup(tableModel);

        JButton result = manageAffectedElementsAction.changeCRStatusActionListener(changeCRStatus, changeCRStatusAction, myListener, tableModel, selectionModel);
        changeCRStatus.doClick();

        assertSame(changeCRStatus, result);
        verify(selectionModel).clearSelection();
    }

    @Test
    public void lockActionListener() {
        lock = spy(new JButton());
        ActionEvent actionEvent = mock(ActionEvent.class);
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        ListSelectionModel selectionModel = mock(ListSelectionModel.class);

        actionListenerBasicSetup(tableModel);

        JButton result = manageAffectedElementsAction.lockActionListener(lock, lockAction, tableModel, selectionModel);
        lock.doClick();

        assertSame(lock, result);
        verify(selectionModel).clearSelection();
    }

    @Test
    public void getAffectedElementsPanel() {
        DefaultTableModel tableModel = mock(DefaultTableModel.class);
        Object[] columnNames = {"element", "ID", "Revision", "Name", "Type", "Status", "Lock"};
        TableColumnModel columnModel = mock(TableColumnModel.class);
        TableColumn column = mock(TableColumn.class);
        ListSelectionModel selectionModel = mock(ListSelectionModel.class);
        JButton ceStatus = mock(JButton.class);
        JButton revise = mock(JButton.class);
        JButton configure = mock(JButton.class);
        JButton crStatus = mock(JButton.class);
        JButton lock = mock(JButton.class);
        ChangeStatusBulkAction statusBulkAction = mock(ChangeStatusBulkAction.class);
        ReviseBulkAction reviseBulkAction = mock(ReviseBulkAction.class);
        ConfigureBulkAction configureBulkAction = mock(ConfigureBulkAction.class);
        ChangeStatusAction statusAction = mock(ChangeStatusAction.class);
        LockAction lockAction = mock(LockAction.class);
        ManageAffectedElementsAction.MyListener listener = mock(ManageAffectedElementsAction.MyListener.class);
        JPanel buttonsPanel = mock(JPanel.class);
        JPanel panel = mock(JPanel.class);


        doReturn(tableModel).when(manageAffectedElementsAction).createTableModel();
        doNothing().when(manageAffectedElementsAction).fillTableModelContents(tableModel);
        doReturn(table).when(manageAffectedElementsAction).makeTableWithModel(tableModel);
        doReturn(columnModel).when(table).getColumnModel();
        doReturn(column).when(columnModel).getColumn(0);
        doReturn(selectionModel).when(table).getSelectionModel();
        doReturn(ceStatus).when(manageAffectedElementsAction).createButton("Modify Elements Status");
        doReturn(revise).when(manageAffectedElementsAction).createButton("Revise Elements");
        doReturn(configure).when(manageAffectedElementsAction).createButton("Configure Elements");
        doReturn(crStatus).when(manageAffectedElementsAction).createButton("Modify Change Status");
        doReturn(lock).when(manageAffectedElementsAction).createButton("Lock");
        doReturn(statusBulkAction).when(manageAffectedElementsAction).getChangeCEStatusAction();
        doReturn(reviseBulkAction).when(manageAffectedElementsAction).getReviseAction();
        doReturn(configureBulkAction).when(manageAffectedElementsAction).getConfigureAction();
        doReturn(statusAction).when(manageAffectedElementsAction).getChangeCRStatusAction();
        doReturn(lockAction).when(manageAffectedElementsAction).getLockAction();
        doReturn(listener).when(manageAffectedElementsAction).makeListener(table, ceStatus, revise, configure, crStatus,
                lock, statusBulkAction, reviseAction, configureAction, lockAction);
        doReturn(listener).when(manageAffectedElementsAction).getMyListener();
        doReturn(ceStatus).when(manageAffectedElementsAction).changeCEStatusActionListener(ceStatus, statusBulkAction, tableModel, selectionModel);
        doReturn(revise).when(manageAffectedElementsAction).reviseActionListener(revise, reviseBulkAction, tableModel, selectionModel);
        doReturn(configure).when(manageAffectedElementsAction).configureActionListener(configure, configureBulkAction, tableModel, selectionModel);
        doReturn(crStatus).when(manageAffectedElementsAction).changeCRStatusActionListener(crStatus, statusAction, listener, tableModel, selectionModel);
        doReturn(lock).when(manageAffectedElementsAction).lockActionListener(lock, lockAction, tableModel, selectionModel);
        doReturn(buttonsPanel).when(manageAffectedElementsAction).makeButtonsPanel(ceStatus, revise, configure, crStatus, lock);
        doReturn(panel).when(manageAffectedElementsAction).finishPanelSetup(table, buttonsPanel);

        JPanel result = manageAffectedElementsAction.getAffectedElementsPanel();

        assertSame(panel, result);
    }

    @Test
    public void createJPanel() {
        doReturn(borderLayout).when(manageAffectedElementsAction).createBorderLayout();
        JPanel panelWithBorderLayout =  manageAffectedElementsAction.createJPanel(borderLayout);
        assertSame(borderLayout , panelWithBorderLayout.getLayout() );
    }

    @Test
    public void createJPanel_Null() {
        JPanel panelWithoutBorderLayout =  manageAffectedElementsAction.createJPanel(null);
        assertNotNull(panelWithoutBorderLayout);
    }

    @Test
    public void actionPerformedListener() {
        doNothing().when(myListener).setButtonsActive();
        myListener.actionPerformed(event);
        verify(myListener).setButtonsActive();
    }

    @Test
    public void valueChanged() {
        doNothing().when(myListener).setButtonsActive();
        doReturn(true).when(listSelectionEvent).getValueIsAdjusting();
        myListener.valueChanged(listSelectionEvent);
        verify(myListener).setButtonsActive();
    }

    @Test
    public void getChangeRecord() {

        Class clazz = mock(Class.class);
        ChangeRecord changeRecordObject = mock(ChangeRecord.class);
        doReturn(clazz).when(manageAffectedElementsAction).getSelectedObjectOverride();
        doReturn(changeRecordObject).when(configurationManagementService).getChangeRecord(any());

        manageAffectedElementsAction.getChangeRecord();

        verify(manageAffectedElementsAction).getSelectedObjectOverride();
        verify(configurationManagementService).getChangeRecord(any());

    }
}
