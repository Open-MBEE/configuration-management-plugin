package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
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

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class TestCreateChangeRecordAction {
    private CreateChangeRecordAction createChangeRecordAction;
    private ConfigurationManagementService configurationManagementService;
    private UIDomain uiDomain;
    private Logger logger;
    private ApiDomain apiDomain;
    private Project project;
    private ConfiguredElementDomain configuredElementDomain;
    private ActionEvent actionEvent;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        uiDomain = mock(UIDomain.class);
        logger = mock(Logger.class);
        apiDomain = mock(ApiDomain.class);
        project = mock(Project.class);
        configuredElementDomain = mock(ConfiguredElementDomain.class);
        actionEvent = mock(ActionEvent.class);
        createChangeRecordAction = Mockito.spy(new CreateChangeRecordAction(configurationManagementService));

        when(createChangeRecordAction.getApiDomain()).thenReturn(apiDomain);
        when(createChangeRecordAction.getUIDomain()).thenReturn(uiDomain);
        when(createChangeRecordAction.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(createChangeRecordAction.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
        when(createChangeRecordAction.getLogger()).thenReturn(logger);
    }

    @Test
    public void actionPerformed_cancelledInput() {
        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn(null).when(uiDomain).askForInput("Please enter the Change Record ID");

        createChangeRecordAction.actionPerformed(actionEvent);

        verify(uiDomain).askForInput("Please enter the Change Record ID");
        verify(configurationManagementService, never()).getChangeRecords();
    }

    @Test
    public void actionPerformed_emptyInput() {
        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn("").when(uiDomain).askForInput("Please enter the Change Record ID");

        createChangeRecordAction.actionPerformed(actionEvent);

        verify(uiDomain).askForInput("Please enter the Change Record ID");
        verify(configurationManagementService, never()).getChangeRecords();
    }

    @Test
    public void actionPerformed_duplicateChangeRecord() {
        String id = "id";
        List<ChangeRecord> changeRecords = new ArrayList<>();
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        changeRecords.add(changeRecord);

        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn(id).when(uiDomain).askForInput("Please enter the Change Record ID");
        doReturn(changeRecords).when(configurationManagementService).getChangeRecords();
        doReturn(id).when(changeRecord).getName();
        doNothing().when(uiDomain).showErrorMessage(String.format("A change record with ID [%s] already exists", id),
            "Change Record Creation failure");

        createChangeRecordAction.actionPerformed(actionEvent);

        verify(uiDomain).showErrorMessage(String.format("A change record with ID [%s] already exists", id),
            "Change Record Creation failure");
        verify(configurationManagementService, never()).getCustomChangeRecordStereotypes(PluginConstant.CAMEO);
    }

    @Test
    public void actionPerformed_userCancelsChoosingDesiredStereotype() {
        String id = "id";
        List<ChangeRecord> changeRecords = new ArrayList<>();
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        changeRecords.add(changeRecord);
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        stereotypes.add(stereotype);
        String action = "creation";

        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn(id).when(uiDomain).askForInput("Please enter the Change Record ID");
        doReturn(changeRecords).when(configurationManagementService).getChangeRecords();
        doReturn("id2").when(changeRecord).getName();
        doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.CAMEO);
        doReturn(null).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
            "Select change record type", "Change record type selection");

        createChangeRecordAction.actionPerformed(actionEvent);

        verify(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.CAMEO);
        verify(configuredElementDomain, never()).canUserPerformAction(configurationManagementService, stereotype, action);
    }

    @Test
    public void actionPerformed_userLacksPermission() {
        String id = "id";
        List<ChangeRecord> changeRecords = new ArrayList<>();
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        changeRecords.add(changeRecord);
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        stereotypes.add(stereotype);
        String action = "creation";

        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn(id).when(uiDomain).askForInput("Please enter the Change Record ID");
        doReturn(changeRecords).when(configurationManagementService).getChangeRecords();
        doReturn("id2").when(changeRecord).getName();
        doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.CAMEO);
        doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
            "Select change record type", "Change record type selection");
        doReturn(false).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doNothing().when(uiDomain).logErrorAndShowMessage(logger, "Configured element failure for " + action,
            "Permissions error");

        createChangeRecordAction.actionPerformed(actionEvent);

        verify(uiDomain).logErrorAndShowMessage(logger, "Configured element failure for " + action,
            "Permissions error");
        verify(configurationManagementService, never()).getChangeRecordsPackage(true);
    }

    @Test
    public void actionPerformed_changeRecordsPackageNotFound() {
        String id = "id";
        List<ChangeRecord> changeRecords = new ArrayList<>();
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        changeRecords.add(changeRecord);
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        stereotypes.add(stereotype);
        String action = "creation";

        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn(id).when(uiDomain).askForInput("Please enter the Change Record ID");
        doReturn(changeRecords).when(configurationManagementService).getChangeRecords();
        doReturn("id2").when(changeRecord).getName();
        doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.CAMEO);
        doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
                "Select change record type", "Change record type selection");
        doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(null).when(configurationManagementService).getChangeRecordsPackage(true);
        doNothing().when(uiDomain).logErrorAndShowMessage(logger, ExceptionConstants.CHANGE_RECORDS_PACKAGE_MISSING,
                ExceptionConstants.PACKAGE_NOT_FOUND);

        createChangeRecordAction.actionPerformed(actionEvent);

        verify(uiDomain).logErrorAndShowMessage(logger, ExceptionConstants.CHANGE_RECORDS_PACKAGE_MISSING,
                ExceptionConstants.PACKAGE_NOT_FOUND);
        verify(project, never()).getElementsFactory();
    }

    @Test
    public void actionPerformed_noIssues() {
        String id = "id";
        List<ChangeRecord> changeRecords = new ArrayList<>();
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        changeRecords.add(changeRecord);
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        stereotypes.add(stereotype);
        String action = "creation";
        Package pkg = mock(Package.class);
        ElementsFactory elementsFactory = mock(ElementsFactory.class);
        Class classInstance = mock(Class.class);

        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn(id).when(uiDomain).askForInput("Please enter the Change Record ID");
        doReturn(changeRecords).when(configurationManagementService).getChangeRecords();
        doReturn("id2").when(changeRecord).getName();
        doReturn(stereotypes).when(configurationManagementService).getCustomChangeRecordStereotypes(PluginConstant.CAMEO);
        doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(stereotypes,
            "Select change record type", "Change record type selection");
        doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(true);
        doReturn(elementsFactory).when(project).getElementsFactory();
        doReturn(classInstance).when(elementsFactory).createClassInstance();
        doReturn(null).when(configurationManagementService).initializeChangeRecord(classInstance, id, stereotype, pkg);
        doNothing().when(apiDomain).setCurrentProjectHardDirty();

        createChangeRecordAction.actionPerformed(actionEvent);

        verify(apiDomain).setCurrentProjectHardDirty();
    }

    @Test
    public void updateState() {
        doReturn(true).when(configurationManagementService).isCmActive();
        doNothing().when(createChangeRecordAction).setEnabled(true);
        createChangeRecordAction.updateState();
        verify(createChangeRecordAction).setEnabled(true);
    }

    @Test
    public void updateState_ifCmInActive() {
        doReturn(false).when(configurationManagementService).isCmActive();
        doNothing().when(createChangeRecordAction).setEnabled(false);
        createChangeRecordAction.updateState();
        verify(createChangeRecordAction).setEnabled(false);
    }
}