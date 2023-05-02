package org.openmbee.plugin.cfgmgmt.actions.toolbar;

import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestSelectChangeRecordAction {
    private SelectChangeRecordAction selectChangeRecordAction;
    private ConfigurationManagementService configurationManagementService;
    private LifecycleObjectFactory lifecycleObjectFactory;
    private UIDomain uiDomain;
    private Logger logger;
    private PropertyChangeEvent propertyChangeEvent;
    private Object object;
    private Package pkg;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        lifecycleObjectFactory = mock(LifecycleObjectFactory.class);
        uiDomain = mock(UIDomain.class);
        logger = mock(Logger.class);
        propertyChangeEvent = mock(PropertyChangeEvent.class);
        object = mock(Object.class);
        pkg = mock(Package.class);

        selectChangeRecordAction = spy(new SelectChangeRecordAction(configurationManagementService));

        when(selectChangeRecordAction.getUIDomain()).thenReturn(uiDomain);
        when(selectChangeRecordAction.getLogger()).thenReturn(logger);
        when(selectChangeRecordAction.getLifecycleObjectFactory()).thenReturn(lifecycleObjectFactory);
    }

    @Test
    public void propertyChange() {
        String value = "Get value name";
        String newValue = "Get new value";

        doReturn(value).when(propertyChangeEvent).getPropertyName();
        doReturn(value).when(selectChangeRecordAction).getValueName();
        doReturn(object).when(propertyChangeEvent).getSource();
        doReturn(newValue).when(propertyChangeEvent).getNewValue();
        doNothing().when(selectChangeRecordAction).setValue(newValue);
        doNothing().when(configurationManagementService).setChangeRecordName(newValue);
        doNothing().when(selectChangeRecordAction).updateState();

        selectChangeRecordAction.propertyChange(propertyChangeEvent);

        verify(configurationManagementService).setChangeRecordName(newValue);
        verify(selectChangeRecordAction).updateState();
    }

    @Test
    public void propertyChange_newValueNotAString() {
        String value = "Get value name";
        Object newValue = mock(Object.class);

        doReturn(value).when(propertyChangeEvent).getPropertyName();
        doReturn(value).when(selectChangeRecordAction).getValueName();
        doReturn(object).when(propertyChangeEvent).getSource();
        doReturn(newValue).when(propertyChangeEvent).getNewValue();
        doNothing().when(selectChangeRecordAction).setValue(newValue);
        doNothing().when(selectChangeRecordAction).updateState();

        selectChangeRecordAction.propertyChange(propertyChangeEvent);

        verify(selectChangeRecordAction).updateState();
        verify(configurationManagementService, never()).setChangeRecordName(anyString());
    }

    @Test
    public void propertyChange_eventNameAndActionNameDoNotMatch() {
        String value = "Get value name";
        String v2 = "value false";
        doReturn(value).when(propertyChangeEvent).getPropertyName();
        doReturn(v2).when(selectChangeRecordAction).getValueName();
        doReturn(object).when(propertyChangeEvent).getSource();

        selectChangeRecordAction.propertyChange(propertyChangeEvent);

        verify(selectChangeRecordAction, never()).setValue(anyString());
    }

    @Test
    public void propertyChange_sourceIsTheAction() {
        String value = "Get value name";
        doReturn(value).when(propertyChangeEvent).getPropertyName();
        doReturn(value).when(selectChangeRecordAction).getValueName();
        doReturn(selectChangeRecordAction).when(propertyChangeEvent).getSource();

        selectChangeRecordAction.propertyChange(propertyChangeEvent);

        verify(selectChangeRecordAction, never()).setValue(anyString());
    }

    @Test
    public void actionPerformed() {
        ActionEvent actionEvent = mock(ActionEvent.class);
        selectChangeRecordAction.actionPerformed(actionEvent);
        verify(configurationManagementService).updateCRStatus();
    }

    @Test
    public void setSelectedChangeRecord_noSelections() {
        doReturn(null).when(selectChangeRecordAction).getSelections();

        selectChangeRecordAction.setSelectedChangeRecord("name");

        verify(selectChangeRecordAction, never()).setValue(anyString());
    }

    @Test
    public void setSelectedChangeRecord_emptySelections() {
        doReturn(new ArrayList<>()).when(selectChangeRecordAction).getSelections();

        selectChangeRecordAction.setSelectedChangeRecord("name");

        verify(selectChangeRecordAction, never()).setValue(anyString());
    }

    @Test
    public void setSelectedChangeRecord_noMatches() {
        List<String> selections = new ArrayList<>();
        String current = "current";
        selections.add(current);

        doReturn(selections).when(selectChangeRecordAction).getSelections();

        selectChangeRecordAction.setSelectedChangeRecord("name");

        verify(selectChangeRecordAction, never()).setValue(anyString());
    }

    @Test
    public void setSelectedChangeRecord_match() {
        List<String> selections = new ArrayList<>();
        String current = "current";
        selections.add(current);

        doReturn(selections).when(selectChangeRecordAction).getSelections();

        selectChangeRecordAction.setSelectedChangeRecord(current);

        verify(selectChangeRecordAction).setValue(current);
    }

    @Test
    public void resetSelections() {
        doNothing().when(selectChangeRecordAction).setChangeRecordSelections();

        selectChangeRecordAction.resetSelections();

        assertNull(selectChangeRecordAction.getValue());
        verify(selectChangeRecordAction).setChangeRecordSelections();
    }

    @Test
    public void clearList() {
        Object changeRecordObject = mock(Object.class);
        List<Object> objectList = new ArrayList<>();
        objectList.add(changeRecordObject);
        assertTrue(selectChangeRecordAction.getSelections().size() == 1);
        selectChangeRecordAction.setValue(changeRecordObject);

        selectChangeRecordAction.clearList();

        assertNotNull(objectList);
        assertTrue(selectChangeRecordAction.getSelections().size() == 0);
        verify(selectChangeRecordAction).setValue(null);
    }

    @Test
    public void setChangeRecordSelections_noChangeRecordsPackage() {
        doReturn(null).when(configurationManagementService).getChangeRecordsPackage(false);
        selectChangeRecordAction.setChangeRecordSelections();
        verify(selectChangeRecordAction).setValue(null);
    }

    @Test
    public void setChangeRecordSelections_ReleasedChangeRecord() {
        Collection<PackageableElement> pkgContents = new ArrayList<>();
        PackageableElement namedElement = mock(PackageableElement.class);
        pkgContents.add(namedElement);
        Class changeRecord = mock(Class.class);
        Class missingStatus = mock(Class.class);
        pkgContents.add(changeRecord);
        pkgContents.add(missingStatus);
        ChangeRecord released = mock(ChangeRecord.class);
        ChangeRecord badStatus = mock(ChangeRecord.class);
        String changeRecordName = "name";
        List<String> selections = new ArrayList<>();
        selections.add("Test");

        doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(false);
        doReturn(pkgContents).when(pkg).getPackagedElement();
        doReturn(released).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, changeRecord);
        doReturn(badStatus).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, missingStatus);
        doReturn(true).when(released).isReleased();
        doReturn(changeRecordName).when(changeRecord).getName();
        doNothing().when(selectChangeRecordAction).setItems(any());
        when(selectChangeRecordAction.getSelectedChangeRecord()).thenReturn(changeRecordName);
        when(selectChangeRecordAction.getSelections()).thenReturn(selections);

        selectChangeRecordAction.setChangeRecordSelections();

        verify(selectChangeRecordAction).setValue(null);
    }

    @Test
    public void setChangeRecordSelections_nullSelectedChangeRecord() {
        Collection<PackageableElement> pkgContents = new ArrayList<>();
        PackageableElement namedElement = mock(PackageableElement.class);
        pkgContents.add(namedElement);
        Class changeRecord = mock(Class.class);
        Class missingStatus = mock(Class.class);
        pkgContents.add(changeRecord);
        pkgContents.add(missingStatus);
        ChangeRecord released = mock(ChangeRecord.class);
        ChangeRecord badStatus = mock(ChangeRecord.class);
        String changeRecordName = "name";

        doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(false);
        doReturn(pkgContents).when(pkg).getPackagedElement();
        doReturn(released).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, changeRecord);
        doReturn(badStatus).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, missingStatus);
        doReturn(true).when(released).isReleased();
        doReturn(changeRecordName).when(changeRecord).getName();
        doNothing().when(selectChangeRecordAction).setItems(any());
        doReturn(null).when(selectChangeRecordAction).getSelectedChangeRecord();

        selectChangeRecordAction.setChangeRecordSelections();

        verify(selectChangeRecordAction).setItems(any());
        verify(selectChangeRecordAction, never()).setValue(null);
    }

    @Test
    public void setChangeRecordSelections_currentSelectionInExpectedPlace() {
        Collection<PackageableElement> pkgContents = new ArrayList<>();
        PackageableElement namedElement = mock(PackageableElement.class);
        pkgContents.add(namedElement);
        Class changeRecord = mock(Class.class);
        Class missingStatus = mock(Class.class);
        pkgContents.add(changeRecord);
        pkgContents.add(missingStatus);
        ChangeRecord released = mock(ChangeRecord.class);
        ChangeRecord badStatus = mock(ChangeRecord.class);
        String changeRecordName = "name";
        List<String> selections = new ArrayList<>();
        selections.add(changeRecordName);

        doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(false);
        doReturn(pkgContents).when(pkg).getPackagedElement();
        doReturn(released).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, changeRecord);
        doReturn(badStatus).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, missingStatus);
        doReturn(true).when(released).isReleased();
        doReturn(changeRecordName).when(changeRecord).getName();
        doNothing().when(selectChangeRecordAction).setItems(any());
        when(selectChangeRecordAction.getSelectedChangeRecord()).thenReturn(changeRecordName);
        doReturn(selections).when(selectChangeRecordAction).getSelections();

        selectChangeRecordAction.setChangeRecordSelections();

        verify(selectChangeRecordAction, never()).setValue(null);
    }

    @Test
    public void setChangeRecordSelections_currentSelectionNotInExpectedPlace() {
        Collection<PackageableElement> pkgContents = new ArrayList<>();
        PackageableElement namedElement = mock(PackageableElement.class);
        pkgContents.add(namedElement);
        Class changeRecord = mock(Class.class);
        Class missingStatus = mock(Class.class);
        pkgContents.add(changeRecord);
        pkgContents.add(missingStatus);
        ChangeRecord released = mock(ChangeRecord.class);
        ChangeRecord badStatus = mock(ChangeRecord.class);
        String changeRecordName = "name";
        List<String> selections = new ArrayList<>();
        selections.add("randomSelection");

        doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(false);
        doReturn(pkgContents).when(pkg).getPackagedElement();
        doReturn(released).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, changeRecord);
        doReturn(badStatus).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, missingStatus);
        doReturn(true).when(released).isReleased();
        doReturn(changeRecordName).when(changeRecord).getName();
        doNothing().when(selectChangeRecordAction).setItems(any());
        when(selectChangeRecordAction.getSelectedChangeRecord()).thenReturn(changeRecordName);
        doReturn(selections).when(selectChangeRecordAction).getSelections();

        selectChangeRecordAction.setChangeRecordSelections();

        verify(selectChangeRecordAction).setValue(null);
    }
}

