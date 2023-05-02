package org.openmbee.plugin.cfgmgmt.actions.bulk;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.model.LifecycleTransition;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants.GUI_LOG_MESSAGE;
import static org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants.TRANSITION_FAILURE;
import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.SELECT_TRANSITION_PROMPT_MESSAGE;
import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.SELECT_TRANSITION_PROMPT_TITLE;
import static org.mockito.Mockito.*;

public class TestChangeStatusBulkAction {
    private ChangeStatusBulkAction changeStatusBulkAction;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private ActionEvent actionEvent;
    private ConfiguredElement configuredElement;
    private LifecycleTransition lifecycleTransition;
    private LifecycleStatus lifecycleStatus;
    private Logger logger;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        actionEvent = mock(ActionEvent.class);
        changeStatusBulkAction = Mockito.spy(new ChangeStatusBulkAction(configurationManagementService));
        configuredElement = mock(ConfiguredElement.class);
        lifecycleTransition = mock(LifecycleTransition.class);
        lifecycleStatus = mock(LifecycleStatus.class);
        logger = mock(Logger.class);

        doReturn(apiDomain).when(configurationManagementService).getApiDomain();
        doReturn(uiDomain).when(configurationManagementService).getUIDomain();
        doReturn(logger).when(changeStatusBulkAction).getLogger();
    }

    @Test
    public void setEnabled_emptyList() {
        List<ConfiguredElement> elements = new ArrayList<>();

        doNothing().when(changeStatusBulkAction).setEnabled(false);

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void setEnabled_crIsReleased() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        List<ConfiguredElement> elements = new ArrayList<>();
        elements.add(configuredElement);

        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(true).when(changeRecord).isReleased();

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void setEnabled_oneElementCanBePromoted() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        List<ConfiguredElement> elements = new ArrayList<>();
        elements.add(configuredElement);

        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isReleased();
        doReturn(true).when(configuredElement).canBePromoted();

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(true);
    }

    @Test
    public void setEnabled_oneElementCannotBePromoted() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        List<ConfiguredElement> elements = new ArrayList<>();
        elements.add(configuredElement);

        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isReleased();
        doReturn(false).when(configuredElement).canBePromoted();

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void setEnabled_elementsWithEmptyStatus() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        Stereotype stereotype1 = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        List<ConfiguredElement> elements = new ArrayList<>();
        ConfiguredElement element2 = mock(ConfiguredElement.class);
        elements.add(configuredElement);
        elements.add(element2);

        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isReleased();
        doReturn(stereotype1).when(configuredElement).getAppliedStereotype();
        doReturn(stereotype2).when(element2).getAppliedStereotype();
        doReturn(Optional.empty()).when(configuredElement).getStatus();

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void setEnabled_elementsWithDifferentStatusAndSameID() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        Stereotype stereotype1 = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        List<ConfiguredElement> elements = new ArrayList<>();
        ConfiguredElement element2 = mock(ConfiguredElement.class);
        elements.add(configuredElement);
        elements.add(element2);

        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isReleased();
        doReturn(stereotype1).when(configuredElement).getAppliedStereotype();
        doReturn(stereotype2).when(element2).getAppliedStereotype();
        doReturn(Optional.of(lifecycleStatus)).when(configuredElement).getStatus();
        doReturn("TypeID-1").when(stereotype1).getID();
        doReturn("TypeID-1").when(stereotype2).getID();
        doReturn(true).when(configuredElement).canBePromoted();

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void setEnabled_elementsWithNullStatusAndDifferentID() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        Stereotype stereotype1 = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        List<ConfiguredElement> elements = new ArrayList<>();
        ConfiguredElement element2 = mock(ConfiguredElement.class);
        elements.add(configuredElement);
        elements.add(element2);

        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isReleased();
        doReturn(stereotype1).when(configuredElement).getAppliedStereotype();
        doReturn(stereotype2).when(element2).getAppliedStereotype();
        doReturn(Optional.of(lifecycleStatus)).when(configuredElement).getStatus();
        doReturn(Optional.of(lifecycleStatus)).when(element2).getStatus();
        doReturn(null).when(stereotype1).getID();
        doReturn(true).when(configuredElement).canBePromoted();

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void setEnabled_elementsWithDifferentStatusAndSameID2() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        Stereotype stereotype1 = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        List<ConfiguredElement> elements = new ArrayList<>();
        ConfiguredElement element2 = mock(ConfiguredElement.class);
        elements.add(configuredElement);
        elements.add(element2);
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isReleased();
        doReturn(stereotype1).when(configuredElement).getAppliedStereotype();
        doReturn(stereotype2).when(element2).getAppliedStereotype();
        doReturn(Optional.of(lifecycleStatus)).when(configuredElement).getStatus();
        doReturn("TypeID-1").when(stereotype1).getID();
        doReturn("TypeID-1").when(stereotype2).getID();
        doReturn(true).when(configuredElement).canBePromoted();
        doReturn(false).when(element2).canBePromoted();

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void setEnabled_elementsWithSametatusAndNullID() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        Stereotype stereotype1 = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        List<ConfiguredElement> elements = new ArrayList<>();
        ConfiguredElement element2 = mock(ConfiguredElement.class);
        elements.add(configuredElement);
        elements.add(element2);

        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isReleased();
        doReturn(stereotype1).when(configuredElement).getAppliedStereotype();
        doReturn(stereotype2).when(element2).getAppliedStereotype();
        doReturn(Optional.of(lifecycleStatus)).when(configuredElement).getStatus();
        doReturn(Optional.of(lifecycleStatus)).when(element2).getStatus();
        doReturn(null).when(stereotype1).getID();
        doReturn(true).when(configuredElement).canBePromoted();
        doReturn(false).when(element2).canBePromoted();

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void setEnabled_elementsWithSameStatusDifferentID() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        Stereotype stereotype1 = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        List<ConfiguredElement> elements = new ArrayList<>();
        ConfiguredElement element2 = mock(ConfiguredElement.class);
        elements.add(configuredElement);
        elements.add(element2);

        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isReleased();
        doReturn(stereotype1).when(configuredElement).getAppliedStereotype();
        doReturn(stereotype2).when(element2).getAppliedStereotype();
        doReturn(Optional.of(lifecycleStatus)).when(configuredElement).getStatus();
        doReturn(Optional.of(lifecycleStatus)).when(element2).getStatus();
        doReturn("typeID-1").when(stereotype1).getID();
        doReturn("typeID-2").when(stereotype2).getID();
        doReturn(true).when(configuredElement).canBePromoted();

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void setEnabled_elementsWithSameStatusAndID() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        Stereotype stereotype1 = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        List<ConfiguredElement> elements = new ArrayList<>();
        ConfiguredElement element2 = mock(ConfiguredElement.class);
        elements.add(configuredElement);
        elements.add(element2);

        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isReleased();
        doReturn(stereotype1).when(configuredElement).getAppliedStereotype();
        doReturn(stereotype2).when(element2).getAppliedStereotype();
        doReturn(Optional.of(lifecycleStatus)).when(configuredElement).getStatus();
        doReturn(Optional.of(lifecycleStatus)).when(element2).getStatus();
        doReturn("typeID-1").when(stereotype1).getID();
        doReturn("typeID-1").when(stereotype2).getID();
        doReturn(true).when(configuredElement).canBePromoted();
        doReturn(false).when(element2).canBePromoted();

        changeStatusBulkAction.setEnabled(elements);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void setEnabled_twoElementsCanBePromoted() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        Stereotype stereotype1 = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        List<ConfiguredElement> els = new ArrayList<>();
        ConfiguredElement element2 = mock(ConfiguredElement.class);
        els.add(configuredElement);
        els.add(element2);

        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isReleased();
        doReturn(Optional.of(lifecycleStatus)).when(configuredElement).getStatus();
        doReturn(stereotype1).when(configuredElement).getAppliedStereotype();
        doReturn(stereotype2).when(element2).getAppliedStereotype();
        doReturn("typeID-1").when(stereotype1).getID();
        doReturn("typeID-2").when(stereotype2).getID();
        doReturn(true).when(configuredElement).canBePromoted();
        doReturn(true).when(element2).canBePromoted();

        changeStatusBulkAction.setEnabled(els);

        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void actionPerformed_onEmptyElements() {
        changeStatusBulkAction.configuredElements = new ArrayList<>();

        changeStatusBulkAction.actionPerformed(actionEvent);
        verify(changeStatusBulkAction).setEnabled(false);
    }

    @Test
    public void actionPerformed_noSelection() {
        changeStatusBulkAction.configuredElements = new ArrayList<>();
        changeStatusBulkAction.configuredElements.add(configuredElement);
        List<LifecycleTransition> transitionList = new ArrayList<>();
        transitionList.add(lifecycleTransition);
        Object[] options = {};

        doReturn(transitionList).when(configuredElement).getTransitions();
        doReturn(true).when(configurationManagementService).getAutomateReleaseSwitch();
        doReturn(true).when(lifecycleTransition).isReleasingTransition();
        doReturn(-1).when(uiDomain).promptForSelection(SELECT_TRANSITION_PROMPT_MESSAGE, SELECT_TRANSITION_PROMPT_TITLE, options);

        changeStatusBulkAction.actionPerformed(actionEvent);
        verify(uiDomain).promptForSelection(SELECT_TRANSITION_PROMPT_MESSAGE, SELECT_TRANSITION_PROMPT_TITLE, options);
        verify(configurationManagementService, never()).getSelectedChangeRecord();
    }

    @Test
    public void actionPerformed_elementSelectedButStatusNotChanged() {
        changeStatusBulkAction.configuredElements = new ArrayList<>();
        changeStatusBulkAction.configuredElements.add(configuredElement);
        List<LifecycleTransition> transitionList = new ArrayList<>();
        transitionList.add(lifecycleTransition);
        String name = "name";
        Object[] options = new Object[] {name};
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(transitionList).when(configuredElement).getTransitions();
        doReturn(false).when(configurationManagementService).getAutomateReleaseSwitch();
        doReturn(true).when(lifecycleTransition).isReleasingTransition();
        doReturn(0).when(uiDomain).promptForSelection(SELECT_TRANSITION_PROMPT_MESSAGE, SELECT_TRANSITION_PROMPT_TITLE, options);
        doReturn("name").when(lifecycleTransition).getName();
        doReturn(transitionList).when(configuredElement).getTransitions();
        when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        doReturn(false).when(changeStatusBulkAction).changeStatus(configuredElement, lifecycleTransition, changeRecord, lifecycleStatus);

        changeStatusBulkAction.actionPerformed(actionEvent);

        verify(uiDomain).logErrorAndShowMessage(logger, ExceptionConstants.ERROR_DURING_BULK_CHANGE_STATUS, "Bulk Change Status Failure");
    }

    @Test
    public void actionPerformed_exceptionDuringChangeStatus() {
        changeStatusBulkAction.configuredElements = new ArrayList<>();
        changeStatusBulkAction.configuredElements.add(configuredElement);
        List<LifecycleTransition> transitionList = new ArrayList<>();
        transitionList.add(lifecycleTransition);
        String name = "name";
        Object[] options = new Object[] {name};
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        String error = "error";
        Exception exception = spy(new NullPointerException(error));

        doReturn(transitionList).when(configuredElement).getTransitions();
        doReturn(false).when(configurationManagementService).getAutomateReleaseSwitch();
        doReturn(false).when(lifecycleTransition).isReleasingTransition();
        doReturn(0).when(uiDomain).promptForSelection(SELECT_TRANSITION_PROMPT_MESSAGE, SELECT_TRANSITION_PROMPT_TITLE, options);
        doReturn("name").when(lifecycleTransition).getName();
        doReturn(transitionList).when(configuredElement).getTransitions();
        when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        doThrow(exception).when(changeStatusBulkAction).changeStatus(configuredElement, lifecycleTransition, changeRecord, lifecycleStatus);

        changeStatusBulkAction.actionPerformed(actionEvent);

        verify(uiDomain).showErrorMessage(String.format(GUI_LOG_MESSAGE, error), TRANSITION_FAILURE);
        verify(uiDomain, never()).logErrorAndShowMessage(logger, ExceptionConstants.ERROR_DURING_BULK_CHANGE_STATUS,
                "Bulk Change Status Failure");
    }

    @Test
    public void actionPerformed_elementSelectedAndStatusChanged() {
        changeStatusBulkAction.configuredElements = new ArrayList<>();
        changeStatusBulkAction.configuredElements.add(configuredElement);
        List<LifecycleTransition> transitionList = new ArrayList<>();
        transitionList.add(lifecycleTransition);
        String name = "name";
        Object[] options = new Object[] {name};
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(transitionList).when(configuredElement).getTransitions();
        doReturn(false).when(configurationManagementService).getAutomateReleaseSwitch();
        doReturn(false).when(lifecycleTransition).isReleasingTransition();
        doReturn(0).when(uiDomain).promptForSelection(SELECT_TRANSITION_PROMPT_MESSAGE, SELECT_TRANSITION_PROMPT_TITLE, options);
        doReturn("name").when(lifecycleTransition).getName();
        doReturn(transitionList).when(configuredElement).getTransitions();
        when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        doReturn(true).when(changeStatusBulkAction).changeStatus(configuredElement, lifecycleTransition, changeRecord, lifecycleStatus);

        changeStatusBulkAction.actionPerformed(actionEvent);

        verify(uiDomain, never()).logErrorAndShowMessage(logger, ExceptionConstants.ERROR_DURING_BULK_CHANGE_STATUS,
                "Bulk Change Status Failure");
    }

    @Test
    public void changeStatus_cannotBePromoted() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        when(configuredElement.canBePromoted(lifecycleTransition, changeRecord)).thenReturn(false);

        changeStatusBulkAction.changeStatus(configuredElement, lifecycleTransition, changeRecord, lifecycleStatus);

        verify(uiDomain).logError(TRANSITION_FAILURE);
        verify(apiDomain, never()).setCurrentProjectHardDirty();
    }

    @Test
    public void changeStatus_exceptionDuringStatusChange() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        Exception exception = mock(NullPointerException.class);

        when(configuredElement.canBePromoted(lifecycleTransition, changeRecord)).thenReturn(true);
        doThrow(exception).when(configuredElement).changeStatus(lifecycleStatus, changeRecord);

        changeStatusBulkAction.changeStatus(configuredElement, lifecycleTransition, changeRecord, lifecycleStatus);

        verify(uiDomain).logError(logger, ExceptionConstants.ERROR_WHILE_CHANGING_STATUS);
        verify(apiDomain, never()).setCurrentProjectHardDirty();
    }

    @Test
    public void changeStatus_StatusChangedSuccessfully() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        when(configuredElement.canBePromoted(lifecycleTransition, changeRecord)).thenReturn(true);
        when(configuredElement.changeStatus(lifecycleStatus, changeRecord)).thenReturn(true);

        changeStatusBulkAction.changeStatus(configuredElement, lifecycleTransition, changeRecord, lifecycleStatus);

        verify(uiDomain, never()).logError(logger, ExceptionConstants.ERROR_WHILE_CHANGING_STATUS);
        verify(apiDomain).setCurrentProjectHardDirty();
    }
}
