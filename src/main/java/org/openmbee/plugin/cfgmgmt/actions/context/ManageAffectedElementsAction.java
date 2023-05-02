package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.actions.bulk.ChangeStatusBulkAction;
import org.openmbee.plugin.cfgmgmt.actions.bulk.ConfigureBulkAction;
import org.openmbee.plugin.cfgmgmt.actions.bulk.LockAction;
import org.openmbee.plugin.cfgmgmt.actions.bulk.ReviseBulkAction;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ManageAffectedElementsAction extends DefaultBrowserAction {
    private static final Logger logger = LoggerFactory.getLogger(ManageAffectedElementsAction.class);
    private final transient ConfigurationManagementService configurationManagementService;
    private transient ChangeRecord changeRecord;
    private transient MyListener listener;

    public ManageAffectedElementsAction(ConfigurationManagementService configurationManagementService) {
        super("MANAGE_AFFECTED_ELEMENTS", "Manage affected elements", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected LifecycleObjectFactory getLifecycleObjectFactory() {
        return getConfigurationManagementService().getLifecycleObjectFactory();
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected ChangeRecord getChangeRecord() {
        return getConfigurationManagementService().getChangeRecord((Class) getSelectedObjectOverride());
    }

    protected ChangeRecord getCurrentChangeRecord() {
        return changeRecord; // used for unit testing
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        changeRecord = getChangeRecord();
        getUIDomain().showPlainMessage(getAffectedElementsPanel(),"Affected Element Management");
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        // this disables the contextual menu for non-element items in the tree such as "Project Usages"
        Object selectedObject = getSelectedObjectOverride();
        if (!(selectedObject instanceof Element)) {
            setEnabled(false);
            return;
        }

        Element element = (Element) selectedObject;

        // this disables the action when the element is not editable, not locked and not new
        if(!getApiDomain().isElementInEditableState(element)) {
            setEnabled(false);
            return;
        }

        // this disables the contextual menu item if the Change Record is not active and not expendable
        ChangeRecord changeRecordForSelectedElement = getConfigurationManagementService().getChangeRecord(element);
        if(changeRecordForSelectedElement != null) {
            ChangeRecord activeChangeRecord = getConfigurationManagementService().getSelectedChangeRecord();
            if (activeChangeRecord != null && !changeRecordForSelectedElement.equals(activeChangeRecord)) {
                setEnabled(false);
                return;
            }
            setEnabled(!changeRecordForSelectedElement.isReleased());
        } else {
            setEnabled(false);
        }
    }

    protected void fillTableModelContents(DefaultTableModel tableModel) {
        tableModel.setRowCount(0);
        changeRecord = getChangeRecord();
        if(changeRecord != null) {
            List<ConfiguredElement> affectedElements = changeRecord.getAffectedElements();
            for (ConfiguredElement configuredElement : affectedElements) {
                String lockUser = getApiDomain().getLockingUser(configuredElement.getElement());
                if(lockUser != null) {
                    Optional<LifecycleStatus> status = configuredElement.getStatus();
                    if(status.isPresent()) {
                        Object[] row = {configuredElement, configuredElement.getID(), configuredElement.getRevision(),
                                ((NamedElement) configuredElement.getElement()).getName(),
                                configuredElement.getAppliedStereotype().getName(), status.get().getName(), lockUser};
                        tableModel.addRow(row);
                    }
                }
            }
        }
    }

    protected class MyListener implements ListSelectionListener, ActionListener {
        JButton changeCEStatus;
        JButton revise;
        JButton configure;
        JButton changeCRStatus;
        JButton lock;
        ChangeStatusBulkAction changeCEStatusAction;
        ReviseBulkAction reviseAction;
        ConfigureBulkAction configureAction;
        LockAction lockAction;
        JTable table;

        public MyListener(JButton changeCEStatus, JButton revise, JButton configure, JButton changeCRStatus, JButton lock,
                          ChangeStatusBulkAction changeCEStatusAction, ReviseBulkAction reviseAction,
                          ConfigureBulkAction configureAction, LockAction lockAction, JTable table) {
            this.changeCEStatus = changeCEStatus;
            this.revise = revise;
            this.configure = configure;
            this.changeCRStatus = changeCRStatus;
            this.lock = lock;
            this.changeCEStatusAction = changeCEStatusAction;
            this.reviseAction = reviseAction;
            this.configureAction = configureAction;
            this.lockAction = lockAction;
            this.table = table;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setButtonsActive();
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if(e != null && e.getValueIsAdjusting()) {
                setButtonsActive();
            }
        }

        protected void setButtonsActive() {
            int[] selectedRows = table.getSelectedRows();
            List<ConfiguredElement> selectedElements = createSelectedElementsArray();
            for (int selectedRow : selectedRows) {
                selectedElements.add(((ConfiguredElement) table.getModel().getValueAt(selectedRow, 0)));
            }

            ChangeRecord activeCR = configurationManagementService.getSelectedChangeRecord();
            changeRecord = getChangeRecord();

            if (changeRecord.hasStatus() && changeRecord.isExpandable()) {
                configureAction.setEnabled(true);
                configure.setEnabled(true);
                reviseAction.setEnabled(true);
                revise.setEnabled(true);
            } else {
                configureAction.setEnabled(false);
                configure.setEnabled(false);
                reviseAction.setEnabled(false);
                revise.setEnabled(false);
            }

            if (changeRecord.isReleased()) {
                changeCEStatus.setEnabled(false);
            } else if (changeRecord.equals(activeCR)) {
                changeCEStatusAction.setEnabled(selectedElements);
                changeCEStatus.setEnabled(changeCEStatusAction.isEnabled());
            }

            changeCRStatus.setEnabled(changeRecord.hasAvailableTransitions());

            lockAction.setEnabled(selectedElements);
            lock.setEnabled(lockAction.isEnabled());
        }

        protected List<ConfiguredElement> createSelectedElementsArray() {
            return new ArrayList<>(); // used for unit testing
        }
    }

    protected MyListener getMyListener() {
        return listener; // used for unit tests
    }

    public JButton changeCEStatusActionListener(JButton changeCEStatus, ChangeStatusBulkAction changeCEStatusAction,
                                                DefaultTableModel tableModel, ListSelectionModel selectionModel) {
        changeCEStatus.addActionListener(e -> {
            changeCEStatusAction.actionPerformed(e);
            fillTableModelContents(tableModel);
            selectionModel.clearSelection();
        });
        return changeCEStatus ;
    }

    public JButton reviseActionListener(JButton revise, ReviseBulkAction reviseAction, DefaultTableModel tableModel,
                                        ListSelectionModel selectionModel) {
        revise.addActionListener(e -> {
            e.setSource(getCurrentChangeRecord());
            reviseAction.actionPerformed(e);
            fillTableModelContents(tableModel);
            selectionModel.clearSelection();
        });
        return revise;
    }

    public JButton configureActionListener(JButton configure, ConfigureBulkAction configureAction,
                                           DefaultTableModel tableModel, ListSelectionModel selectionModel) {
        configure.addActionListener(e -> {
            e.setSource(getCurrentChangeRecord());
            configureAction.actionPerformed(e);
            fillTableModelContents(tableModel);
            selectionModel.clearSelection();
        });
        return configure;
    }

    public JButton changeCRStatusActionListener(JButton changeCRStatus, ChangeStatusAction changeCRStatusAction, MyListener listener,
                                                DefaultTableModel tableModel, ListSelectionModel selectionModel) {
        changeCRStatus.addActionListener(e -> {
            changeCRStatusAction.actionPerformed(e);
            listener.setButtonsActive();
            fillTableModelContents(tableModel);
            selectionModel.clearSelection();
        });
        return  changeCRStatus;
    }

    public JButton lockActionListener(JButton lock, LockAction lockAction, DefaultTableModel tableModel,
                                      ListSelectionModel selectionModel) {
        lock.addActionListener(e -> {
            lockAction.actionPerformed(e);
            fillTableModelContents(tableModel);
            selectionModel.clearSelection();
        });
        return lock;
    }

    protected JPanel getAffectedElementsPanel() {
        DefaultTableModel tableModel = createTableModel();
        Object[] columnNames = {"element", "ID", "Revision", "Name", "Type", "Status", "Lock"};
        tableModel.setColumnIdentifiers(columnNames);
        fillTableModelContents(tableModel);

        JTable table = makeTableWithModel(tableModel);
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        ListSelectionModel selectionModel = table.getSelectionModel();

        // buttons
        JButton changeCEStatus = createButton("Modify Elements Status");
        JButton revise = createButton("Revise Elements");
        JButton configure = createButton("Configure Elements");
        JButton changeCRStatus = createButton("Modify Change Status");
        JButton lock = createButton("Lock");

        // actions
        ChangeStatusBulkAction changeCEStatusAction = getChangeCEStatusAction();
        ReviseBulkAction reviseAction = getReviseAction();
        ConfigureBulkAction configureAction = getConfigureAction();
        ChangeStatusAction changeCRStatusAction = getChangeCRStatusAction();
        LockAction lockAction = getLockAction();

        listener = makeListener(table, changeCEStatus, revise, configure, changeCRStatus, lock,
                changeCEStatusAction, reviseAction, configureAction, lockAction);
        selectionModel.addListSelectionListener(getMyListener());

        changeCEStatus = changeCEStatusActionListener(changeCEStatus, changeCEStatusAction, tableModel, selectionModel);
        changeCEStatus.setText("Modify Elements Status");
        revise = reviseActionListener(revise, reviseAction, tableModel, selectionModel);
        revise.setText("Revise Elements");
        configure = configureActionListener(configure, configureAction,tableModel, selectionModel);
        configure.setText("Configure Elements");
        changeCRStatus = changeCRStatusActionListener(changeCRStatus, changeCRStatusAction, listener, tableModel, selectionModel);
        changeCRStatus.setText("Modify Change Status");
        lock = lockActionListener(lock, lockAction, tableModel, selectionModel);
        lock.setText("Lock");

        JPanel buttonsPanel = makeButtonsPanel(changeCEStatus, revise, configure, changeCRStatus, lock);

        JPanel panel = finishPanelSetup(table, buttonsPanel);
        getMyListener().valueChanged(null);
        return panel;
    }

    protected DefaultTableModel createTableModel() {
        return new DefaultTableModel(); // used for unit tests
    }

    protected JTable makeTableWithModel(DefaultTableModel tableModel) {
        return new JTable(tableModel); // used for unit tests
    }

    protected JButton createButton(String buttonText) {
        return new JButton(buttonText); // used for unit tests
    }

    protected JPanel createJPanel(BorderLayout borderLayout){   // used for unit test
        if(null == borderLayout)
            return new JPanel();
        else
            return  new JPanel(borderLayout);
    }

    protected BorderLayout createBorderLayout(){  // used for unit test
        return new BorderLayout();
    }

    protected ChangeStatusBulkAction getChangeCEStatusAction() {
        return new ChangeStatusBulkAction(getConfigurationManagementService()); // used for unit tests
    }

    protected ReviseBulkAction getReviseAction() {
        return new ReviseBulkAction(getConfigurationManagementService()); // used for unit tests
    }

    protected ConfigureBulkAction getConfigureAction() {
        return new ConfigureBulkAction(getConfigurationManagementService()); // used for unit tests
    }

    protected ChangeStatusAction getChangeCRStatusAction() {
        return new ChangeStatusAction(getConfigurationManagementService()); // used for unit tests
    }

    protected LockAction getLockAction() {
        return new LockAction(getApiDomain()); // used for unit tests
    }

    protected MyListener makeListener(JTable table, JButton changeCEStatus, JButton revise, JButton configure,
                                      JButton changeCRStatus, JButton lock, ChangeStatusBulkAction changeCEStatusAction,
                                      ReviseBulkAction reviseAction, ConfigureBulkAction configureAction, LockAction lockAction) {
        return new MyListener(changeCEStatus, revise, configure, changeCRStatus, lock, changeCEStatusAction,
                reviseAction, configureAction, lockAction, table); // used for unit tests
    }

    protected JPanel makeButtonsPanel(JButton changeCEStatus, JButton revise, JButton configure, JButton changeCRStatus, JButton lock) {
        JPanel buttonsPanel = createJPanel(null); // used for unit tests
        buttonsPanel.add(configure);
        buttonsPanel.add(changeCEStatus);
        buttonsPanel.add(revise);
        buttonsPanel.add(changeCRStatus);
        buttonsPanel.add(lock);
        return buttonsPanel;
    }

    protected JPanel finishPanelSetup(JTable table, JPanel buttonsPanel) {
        JScrollPane scrollPane = new JScrollPane(table); // used for unit tests
        JPanel panel =  createJPanel(createBorderLayout());
        panel.setSize(800, 600);
        panel.add(buttonsPanel, BorderLayout.PAGE_END);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    public Object getSelectedObjectOverride() {
        return getSelectedObject(); // needed for unit tests
    }
}
