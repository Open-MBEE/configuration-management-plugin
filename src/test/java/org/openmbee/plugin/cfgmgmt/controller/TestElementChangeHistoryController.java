package org.openmbee.plugin.cfgmgmt.controller;

import org.openmbee.plugin.cfgmgmt.application.BaseModule;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ChangeRecordDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.rules.JavaFxThreadingRule;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.view.ElementHistoryRowView;
import com.sun.javafx.collections.ImmutableObservableList;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestElementChangeHistoryController {
    @Rule
    public JavaFxThreadingRule javaFxThreadingRule = new JavaFxThreadingRule();

    private TableView tableView;
    private CheckBox checkBox;
    private ElementChangeHistoryController elementChangeHistoryController;
    private ObjectProperty objectProperty;
    private ConfiguredElement configuredElement;
    private BaseModule baseModule;
    private CmControllerSettings cmControllerSettings;

    @Before
    public void setup() {
        cmControllerSettings = mock(CmControllerSettings.class);
        tableView = mock(TableView.class);
        objectProperty = mock(ObjectProperty.class);
        configuredElement = mock(ConfiguredElement.class);
        baseModule = mock(BaseModule.class);
        checkBox = mock(CheckBox.class);

        elementChangeHistoryController = spy(new ElementChangeHistoryController(cmControllerSettings));
    }

    protected void setupColumns() {
        elementChangeHistoryController.changeRecordNameColumn = mock(TableColumn.class);
        elementChangeHistoryController.startTimeColumn = mock(TableColumn.class);
        elementChangeHistoryController.completionTimeColumn = mock(TableColumn.class);
        elementChangeHistoryController.revisionColumn = mock(TableColumn.class);
        elementChangeHistoryController.ceNameAndID = mock(Label.class);
        elementChangeHistoryController.ehTable = tableView;

        doReturn(objectProperty).when(elementChangeHistoryController.changeRecordNameColumn).cellValueFactoryProperty();
        doReturn(objectProperty).when(elementChangeHistoryController.startTimeColumn).cellValueFactoryProperty();
        doReturn(objectProperty).when(elementChangeHistoryController.completionTimeColumn).cellValueFactoryProperty();
        doReturn(objectProperty).when(elementChangeHistoryController.revisionColumn).cellValueFactoryProperty();
        doNothing().when(elementChangeHistoryController).initializeColumns();
    }

    @Test
    public void filterInWorkChangeRecords() {
        ElementHistoryRowView elementHistoryRowView = mock(ElementHistoryRowView.class);
        ElementHistoryRowView elementHistoryRowView2 = mock(ElementHistoryRowView.class);
        ObservableList<ElementHistoryRowView> list = spy(FXCollections.observableList(List.of(elementHistoryRowView, elementHistoryRowView2)));

        when(elementHistoryRowView.getRevisionColumn()).thenReturn(PluginConstant.NO_REVISION_AND_NOT_COMPLETED);
        when(elementHistoryRowView2.getRevisionColumn()).thenReturn(PluginConstant.NO_REVISION_BUT_COMPLETED);

        ObservableList<ElementHistoryRowView> result = elementChangeHistoryController.filterInWorkChangeRecords(list);
        assertFalse(result.contains(elementHistoryRowView));
        assertTrue(result.contains(elementHistoryRowView2));
    }

    @Test
    public void setConfiguredElementName() {
        String configuredElementName = "ElementName";

        elementChangeHistoryController.setConfiguredElementName(configuredElementName);

        assertEquals(configuredElementName, elementChangeHistoryController.getConfiguredElementName());

        verify(elementChangeHistoryController).getConfiguredElementName();
    }

    @Test
    public void setRowEntries() {
        ElementHistoryRowView elementHistoryRowView = mock(ElementHistoryRowView.class);
        List<ElementHistoryRowView> rowEntries = new ArrayList<>();
        rowEntries.add(elementHistoryRowView);

        elementChangeHistoryController.setRowEntries(rowEntries);

        assertEquals(1, elementChangeHistoryController.rowEntries.size());

        verify(elementChangeHistoryController).setRowEntries(rowEntries);
    }

    @Test
    public void setConfiguredElementID() {
        String configuredElementID = "ElementID";

        elementChangeHistoryController.setConfiguredElementID(configuredElementID);

        assertEquals(configuredElementID, elementChangeHistoryController.configuredElementID);
        assertEquals(configuredElementID, elementChangeHistoryController.getConfiguredElementID());

        verify(elementChangeHistoryController).setConfiguredElementID(configuredElementID);
        verify(elementChangeHistoryController).getConfiguredElementID();
    }

    @Test
    public void setModule() {

        elementChangeHistoryController.setModule(baseModule);

        assertNotNull(elementChangeHistoryController.getModule());

        verify(elementChangeHistoryController).getModule();
    }

    @Test
    public void refreshTable_isFxApplicationThread_false() {
        try (MockedStatic<Platform> platformMockedStatic = mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(false);

            elementChangeHistoryController.refreshTable();

            platformMockedStatic.verify(() -> Platform.runLater(any()));
            platformMockedStatic.clearInvocations();
        }
    }

    @Test
    public void refreshTable_isFxApplicationThread_True() {
        try (MockedStatic<Platform> platformMockedStatic = mockStatic(Platform.class)) {
            platformMockedStatic.when(Platform::isFxApplicationThread).thenReturn(true);
            doNothing().when(elementChangeHistoryController).populateElementHistoryTable();

            elementChangeHistoryController.refreshTable();

            verify(elementChangeHistoryController).populateElementHistoryTable();
            platformMockedStatic.verify(() -> Platform.runLater(any()), never());
            platformMockedStatic.clearInvocations();
        }
    }

    @Test
    public void setMainScene() {
        Scene mainScene = mock(Scene.class);
        elementChangeHistoryController.module = baseModule;

        elementChangeHistoryController.setMainScene(mainScene);
        verify(baseModule).setScene(mainScene);
    }

    @Test
    public void startModule_nullModule() {
        BaseFxController baseFxController = mock(BaseFxController.class);
        elementChangeHistoryController.baseFxController = baseFxController;
        doReturn(null).when(elementChangeHistoryController).getModule();
        elementChangeHistoryController.startModule();
        verify(baseFxController, never()).startModule(any());
    }

    @Test
    public void startModule_nonNullModule() {
        BaseFxController baseFxController = mock(BaseFxController.class);
        elementChangeHistoryController.baseFxController = baseFxController;
        doReturn(baseModule).when(elementChangeHistoryController).getModule();
        doNothing().when(baseFxController).startModule(baseModule);
        elementChangeHistoryController.startModule();
        verify(baseFxController).startModule(baseModule);
    }

    @Test
    public void stopModule_nonNullModule() {
        BaseFxController baseFxController = mock(BaseFxController.class);
        elementChangeHistoryController.baseFxController = baseFxController;
        doReturn(baseModule).when(elementChangeHistoryController).getModule();
        doNothing().when(baseFxController).stopModule(baseModule);
        elementChangeHistoryController.stopModule();
        verify(baseFxController).stopModule(baseModule);
    }

    @Test
    public void stopModule_nullModule() {
        BaseFxController baseFxController = mock(BaseFxController.class);
        elementChangeHistoryController.baseFxController = baseFxController;
        doReturn(null).when(elementChangeHistoryController).getModule();
        elementChangeHistoryController.stopModule();
        verify(baseFxController, never()).stopModule(any());
    }

    @Test
    public void resetForNextStart() {
        ElementHistoryRowView elementHistoryRowView = mock(ElementHistoryRowView.class);
        List<ElementHistoryRowView> rowEntries = new ArrayList<>();
        rowEntries.add(elementHistoryRowView);
        elementChangeHistoryController.setRowEntries(rowEntries);
        elementChangeHistoryController.ehTable = tableView;

        elementChangeHistoryController.resetForNextStart();

        verify(tableView).refresh();
    }

    @Test
    public void updateElementHistoryUI_elementLacksIdButHasName() {
        List<ElementHistoryRowView> rows = List.of();
        String name = "name";

        doNothing().when(elementChangeHistoryController).setRowEntries(rows);
        when(configuredElement.getID()).thenReturn(null);
        when(configuredElement.getName()).thenReturn(name);
        doNothing().when(elementChangeHistoryController).setConfiguredElementName(name);

        elementChangeHistoryController.updateElementHistoryUI(rows, null, name);

        verify(elementChangeHistoryController, never()).setConfiguredElementID(anyString());
        verify(elementChangeHistoryController).setConfiguredElementName(name);
    }

    @Test
    public void updateElementHistoryUI_elementHasIdButNotName() {
        List<ElementHistoryRowView> rows = List.of(mock(ElementHistoryRowView.class));
        String id = "id";

        doNothing().when(elementChangeHistoryController).setRowEntries(rows);
        when(configuredElement.getID()).thenReturn(id);
        when(configuredElement.getName()).thenReturn(null);
        doNothing().when(elementChangeHistoryController).setConfiguredElementID(id);

        elementChangeHistoryController.updateElementHistoryUI(rows, id, null);

        verify(elementChangeHistoryController).setConfiguredElementID(id);
        verify(elementChangeHistoryController, never()).setConfiguredElementName(anyString());
    }

    @Test
    public void populateElementHistoryTable_emptyObservableListAndClickBehaviorNotAdded() {
        setupColumns();
        elementChangeHistoryController.completedChangesCheckBox = checkBox;
        String id = "id";
        String name = "CE_name";
        String formatted = String.format(PluginConstant.CE_NAME_AND_ID, id, name);
        elementChangeHistoryController.rowEntries = spy(FXCollections.observableList(List.of()));
        elementChangeHistoryController.ehTable = tableView;
        EventHandler<MouseEvent> eventHandler = mock(EventHandler.class);

        doReturn(id).when(elementChangeHistoryController).getConfiguredElementID();
        doReturn(name).when(elementChangeHistoryController).getConfiguredElementName();
        doNothing().when(elementChangeHistoryController.ceNameAndID).setText(formatted);
        doReturn(eventHandler).when(elementChangeHistoryController).setupTableMouseClickEvent();
        doNothing().when(tableView).setOnMouseClicked(eventHandler);
        doNothing().when(elementChangeHistoryController).showDiffButtonUpdate();

        elementChangeHistoryController.populateElementHistoryTable();

        verify(elementChangeHistoryController.completedChangesCheckBox, never()).isSelected();
    }

    @Test
    public void populateElementHistoryTable_checkboxNotSelected() {
        setupColumns();
        elementChangeHistoryController.clickBehaviorAdded = true;
        elementChangeHistoryController.completedChangesCheckBox = checkBox;
        String id = "id";
        String name = "CE_name";
        String formatted = String.format(PluginConstant.CE_NAME_AND_ID, id, name);
        ElementHistoryRowView elementHistoryRowView = mock(ElementHistoryRowView.class);
        ObservableList<ElementHistoryRowView> list = spy(FXCollections.observableList(List.of(elementHistoryRowView)));
        elementChangeHistoryController.rowEntries = list;

        doReturn(id).when(elementChangeHistoryController).getConfiguredElementID();
        doReturn(name).when(elementChangeHistoryController).getConfiguredElementName();
        doNothing().when(elementChangeHistoryController.ceNameAndID).setText(formatted);
        when(elementChangeHistoryController.completedChangesCheckBox.isSelected()).thenReturn(false);
        doNothing().when(elementChangeHistoryController.ehTable).setItems(list);

        elementChangeHistoryController.populateElementHistoryTable();

        verify(elementChangeHistoryController, never()).filterInWorkChangeRecords(list);
        verify(elementChangeHistoryController, never()).setupTableMouseClickEvent();
    }

    @Test
    public void populateElementHistoryTable_checkboxSelected() {
        setupColumns();
        elementChangeHistoryController.clickBehaviorAdded = true;
        elementChangeHistoryController.completedChangesCheckBox = checkBox;
        String id = "id";
        String name = "CE_name";
        String formatted = String.format(PluginConstant.CE_NAME_AND_ID, id, name);
        ElementHistoryRowView elementHistoryRowView = mock(ElementHistoryRowView.class);
        ObservableList<ElementHistoryRowView> list = spy(FXCollections.observableList(List.of(elementHistoryRowView)));
        elementChangeHistoryController.rowEntries = list;
        Label label = mock(Label.class);
        elementChangeHistoryController.selectCrLabel = label;

        doReturn(id).when(elementChangeHistoryController).getConfiguredElementID();
        doReturn(name).when(elementChangeHistoryController).getConfiguredElementName();
        doNothing().when(elementChangeHistoryController.ceNameAndID).setText(formatted);
        when(elementChangeHistoryController.completedChangesCheckBox.isSelected()).thenReturn(true);
        doReturn(list).when(elementChangeHistoryController).filterInWorkChangeRecords(list);
        doNothing().when(elementChangeHistoryController.ehTable).setItems(list);
        doNothing().when(label).setText(PluginConstant.SELECT_CR_TO_COMPARE);

        elementChangeHistoryController.populateElementHistoryTable();

        verify(elementChangeHistoryController).filterInWorkChangeRecords(list);
    }

    @Test
    public void showElementContainmentTree_noSelectedItem() {
        elementChangeHistoryController.ehTable = tableView;
        TableView.TableViewSelectionModel selectionModel = mock(TableView.TableViewSelectionModel.class);
        ObservableList<ElementHistoryRowView> observableList = new ImmutableObservableList<>();

        when(tableView.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(observableList);

        elementChangeHistoryController.showElementContainmentTree();
        verify(cmControllerSettings, never()).getConfigurationManagementService();
    }

    @Test
    public void showElementContainmentTree_tooManySelectedItems() {
        elementChangeHistoryController.ehTable = tableView;
        TableView.TableViewSelectionModel selectionModel = mock(TableView.TableViewSelectionModel.class);
        ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
        ElementHistoryRowView rowView2 = mock(ElementHistoryRowView.class);
        ObservableList<ElementHistoryRowView> observableList = new ImmutableObservableList<>(rowView, rowView2);

        when(tableView.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(observableList);

        elementChangeHistoryController.showElementContainmentTree();
        verify(cmControllerSettings, never()).getConfigurationManagementService();
    }

    @Test
    public void showElementContainmentTree_displayAttempted() {
        elementChangeHistoryController.ehTable = tableView;
        TableView.TableViewSelectionModel selectionModel = mock(TableView.TableViewSelectionModel.class);
        ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
        ObservableList<ElementHistoryRowView> observableList = new ImmutableObservableList<>(rowView);
        ConfigurationManagementService configurationManagementService = mock(ConfigurationManagementService.class);
        ChangeRecordDomain changeRecordDomain = mock(ChangeRecordDomain.class);
        elementChangeHistoryController.optimizeForMemoryCheckBox = checkBox;
        String id = "id";
        List<ChangeRecord> changeRecords = new ArrayList<>();
        elementChangeHistoryController.module = baseModule;

        when(tableView.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(observableList);
        doNothing().when(baseModule).showStage(false);
        when(cmControllerSettings.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(configurationManagementService.getChangeRecordDomain()).thenReturn(changeRecordDomain);
        when(cmControllerSettings.getConfiguredElementLocalId()).thenReturn(id);
        when(cmControllerSettings.getRelevantChangeRecords()).thenReturn(changeRecords);
        when(elementChangeHistoryController.optimizeForMemoryCheckBox.isSelected()).thenReturn(false);
        doNothing().when(changeRecordDomain).displayDifferenceViewer(rowView, id, changeRecords, false, configurationManagementService);
        doNothing().when(baseModule).showStage(true);

        elementChangeHistoryController.showElementContainmentTree();
        verify(changeRecordDomain).displayDifferenceViewer(rowView, id, changeRecords, false, configurationManagementService);
    }

    @Test
    public void showDiffButtonUpdate_selectionNotSingleRow() {
        elementChangeHistoryController.ehTable = tableView;
        TableView.TableViewSelectionModel selectionModel = mock(TableView.TableViewSelectionModel.class);
        ObservableList<ElementHistoryRowView> observableList = new ImmutableObservableList<>();
        Label label = mock(Label.class);
        Button button = mock(Button.class);
        elementChangeHistoryController.selectCrLabel = label;
        elementChangeHistoryController.showDiffButton = button;

        when(tableView.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(observableList);
        doNothing().when(label).setText(PluginConstant.SELECT_CR_TO_COMPARE);
        doNothing().when(button).setDisable(true);

        elementChangeHistoryController.showDiffButtonUpdate();
        verify(label, never()).setText(PluginConstant.EMPTY_STRING);
    }

    @Test
    public void showDiffButtonUpdate_selectionSingleRow() {
        elementChangeHistoryController.ehTable = tableView;
        TableView.TableViewSelectionModel selectionModel = mock(TableView.TableViewSelectionModel.class);
        ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
        ObservableList<ElementHistoryRowView> observableList = new ImmutableObservableList<>(rowView);
        Label label = mock(Label.class);
        Button button = mock(Button.class);
        elementChangeHistoryController.selectCrLabel = label;
        elementChangeHistoryController.showDiffButton = button;

        when(tableView.getSelectionModel()).thenReturn(selectionModel);
        when(selectionModel.getSelectedItems()).thenReturn(observableList);
        doNothing().when(label).setText(PluginConstant.EMPTY_STRING);
        doNothing().when(button).setDisable(false);

        elementChangeHistoryController.showDiffButtonUpdate();
        verify(label, never()).setText(PluginConstant.SELECT_CR_TO_COMPARE);
    }
}
