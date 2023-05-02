package org.openmbee.plugin.cfgmgmt.controller;

import org.openmbee.plugin.cfgmgmt.application.BaseModule;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.view.ElementHistoryRowView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.stream.Collectors;

public class ElementChangeHistoryController extends AbstractElementHistoryController {
    protected BaseModule module;
    protected BaseFxController baseFxController;
    protected ObservableList<ElementHistoryRowView> rowEntries = FXCollections.observableArrayList();
    protected String configuredElementID;
    protected String configuredElementName;

    @FXML
    protected TableView<ElementHistoryRowView> ehTable;
    protected boolean clickBehaviorAdded = false;
    @FXML
    protected TableColumn<ElementHistoryRowView, String> changeRecordNameColumn;
    @FXML
    protected TableColumn<ElementHistoryRowView, String> startTimeColumn;
    @FXML
    protected TableColumn<ElementHistoryRowView, String> completionTimeColumn;
    @FXML
    protected TableColumn<ElementHistoryRowView, String> revisionColumn;

    @FXML
    protected Label ceNameAndID;
    @FXML
    protected Label selectCrLabel;

    @FXML
    protected CheckBox completedChangesCheckBox;
    @FXML
    protected CheckBox optimizeForMemoryCheckBox;

    @FXML
    protected Button showDiffButton;

    public ElementChangeHistoryController(CmControllerSettings cmControllerSettings) {
        this.cmControllerSettings = cmControllerSettings;
        baseFxController = new BaseFxController();
    }

    protected String getConfiguredElementID() {
        return configuredElementID;
    }

    protected String getConfiguredElementName() {
        return configuredElementName;
    }

    protected ObservableList<ElementHistoryRowView> filterInWorkChangeRecords(ObservableList<ElementHistoryRowView> observableList){
        return observableList.stream().filter(re -> !re.getRevisionColumn().equals(PluginConstant.NO_REVISION_AND_NOT_COMPLETED))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    @FXML
    public void refreshTable() {
        if (Platform.isFxApplicationThread()) {
            populateElementHistoryTable();
        } else {
            Platform.runLater(this::populateElementHistoryTable);
        }
    }

    public void setRowEntries(List<ElementHistoryRowView> rowEntries) {
        this.rowEntries.setAll(rowEntries);
    }

    public void setConfiguredElementID(String configuredElementID) {
        this.configuredElementID = configuredElementID;
    }

    public void setConfiguredElementName(String configuredElementName) {
        this.configuredElementName = configuredElementName;
    }

    public BaseModule getModule() {
        return module;
    }

    public void setModule(BaseModule module) {
        this.module = module;
    }

    public void setMainScene(Scene mainScene) {
        module.setScene(mainScene);
    }

    @Override
    public void startModule() {
        if(getModule() != null) {
            baseFxController.startModule(getModule());
        }
    }

    @Override
    public void stopModule() {
        if(getModule() != null) {
            baseFxController.stopModule(getModule());
        }
    }

    @Override
    protected void resetForNextStart() {
        rowEntries.clear();
        ehTable.refresh();
    }

    @Override
    protected void initializeColumns() {
        changeRecordNameColumn.cellValueFactoryProperty().set(new PropertyValueFactory<>(PluginConstant.CHANGE_RECORD_NAME_COLUMN));
        startTimeColumn.cellValueFactoryProperty().set(new PropertyValueFactory<>(PluginConstant.START_TIME_COLUMN));
        completionTimeColumn.cellValueFactoryProperty().set(new PropertyValueFactory<>(PluginConstant.COMPLETION_TIME_COLUMN));
        revisionColumn.cellValueFactoryProperty().set(new PropertyValueFactory<>(PluginConstant.REVISION_COLUMN));
    }

    @Override
    protected void updateElementHistoryUI(List<ElementHistoryRowView> rowEntries, String id, String name) {
        setRowEntries(rowEntries);
        if (id != null) {
            setConfiguredElementID(id);
        }
        if (name != null) {
            setConfiguredElementName(name);
        }
    }

    @Override
    protected void populateElementHistoryTable() {
        ceNameAndID.setText(String.format(PluginConstant.CE_NAME_AND_ID, getConfiguredElementName(), getConfiguredElementID()));
        if (!rowEntries.isEmpty() && completedChangesCheckBox.isSelected()) {
            ehTable.setItems(filterInWorkChangeRecords(rowEntries));
        } else {
            ehTable.setItems(rowEntries);
        }

        if (!clickBehaviorAdded) {
            ehTable.setOnMouseClicked(setupTableMouseClickEvent());
            showDiffButtonUpdate();
            clickBehaviorAdded = true;
        }
    }

    protected EventHandler<MouseEvent> setupTableMouseClickEvent() {
        return (MouseEvent evt) -> showDiffButtonUpdate(); // enables unit testing
    }

    @FXML
    public void showElementContainmentTree() {
        ObservableList<ElementHistoryRowView> selectedItems = ehTable.getSelectionModel().getSelectedItems();
        if (selectedItems.size() == 1) {
            module.showStage(false);
            cmControllerSettings.getConfigurationManagementService().getChangeRecordDomain()
                    .displayDifferenceViewer(selectedItems.get(0), cmControllerSettings.getConfiguredElementLocalId(),
                    cmControllerSettings.getRelevantChangeRecords(), optimizeForMemoryCheckBox.isSelected(), cmControllerSettings.getConfigurationManagementService());
            module.showStage(true);
        }
    }

    protected void showDiffButtonUpdate() {
        ObservableList<ElementHistoryRowView> selectedItems = ehTable.getSelectionModel().getSelectedItems();
        if (selectedItems.size() == 1) {
            selectCrLabel.setText(PluginConstant.EMPTY_STRING);
            showDiffButton.setDisable(false);
        } else {
            selectCrLabel.setText(PluginConstant.SELECT_CR_TO_COMPARE);
            showDiffButton.setDisable(true);
        }
    }
}
