package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.LifecycleObjectDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestChangeStatusAction {
    private ChangeStatusAction changeStatusAction;
    private ConfigurationManagementService configurationManagementService;
    private LifecycleObjectDomain lifecycleObjectDomain;
    private UIDomain uiDomain;
    private Stereotype stereotype;
    private ApiDomain apiDomain;
    private ChangeRecord changeRecord;
    private LifecycleObjectFactory lifecycleObjectFactory;
    private LifecycleObject lifecycleObject;
    private Element element;
    private LifecycleTransition lifecycleTransition;
    private LifecycleStatus lifecycleStatus;
    private Logger logger;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        changeStatusAction = Mockito.spy(new ChangeStatusAction(configurationManagementService));
        lifecycleObjectDomain = mock(LifecycleObjectDomain.class);
        uiDomain = mock(UIDomain.class);
        stereotype = mock(Stereotype.class);
        apiDomain = mock(ApiDomain.class);
        changeRecord = mock(ChangeRecord.class);
        lifecycleObjectFactory = mock(LifecycleObjectFactory.class);
        element = mock(Element.class);
        lifecycleTransition = mock(LifecycleTransition.class);
        lifecycleObject = mock(LifecycleObject.class);
        lifecycleStatus = mock(LifecycleStatus.class);
        logger = mock(Logger.class);

        when(changeStatusAction.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(changeStatusAction.getUIDomain()).thenReturn(uiDomain);
        when(changeStatusAction.getApiDomain()).thenReturn(apiDomain);
        when(changeStatusAction.getLifecycleObjectFactory()).thenReturn(lifecycleObjectFactory);
        when(changeStatusAction.getLogger()).thenReturn(logger);
        when(changeStatusAction.getLifecycleObjectDomain()).thenReturn(lifecycleObjectDomain);
    }

    @Test
    public void actionPerformed_noSelectedElement() {
        doReturn(null).when(changeStatusAction).getSelectedObjectOverride();

        changeStatusAction.actionPerformed(mock(ActionEvent.class));

        verify(changeStatusAction).getSelectedObjectOverride();
        verify(changeStatusAction, never()).attemptToGetConfiguredElement(any());
    }

    @Test
    public void actionPerformed_selectedObjectNotElement() {
        Object selected = "selected";

        doReturn(selected).when(changeStatusAction).getSelectedObjectOverride();

        changeStatusAction.actionPerformed(mock(ActionEvent.class));

        verify(changeStatusAction).getSelectedObjectOverride();
        verify(uiDomain).logDebug(logger, "No selected object for ChangeStatusAction action");
        verify(changeStatusAction, never()).attemptToGetConfiguredElement(any());
    }

    @Test
    public void actionPerformed_noConfiguredElement() {
        doReturn(element).when(changeStatusAction).getSelectedObjectOverride();
        doReturn(null).when(changeStatusAction).attemptToGetConfiguredElement(element);

        changeStatusAction.actionPerformed(mock(ActionEvent.class));

        verify(changeStatusAction).attemptToGetConfiguredElement(element);
        verify(configurationManagementService, never()).getAutomateReleaseSwitch();
    }

    @Test
    public void actionPerformed_noTransitionsLeftAfterFiltering() {
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);
        List<LifecycleTransition> filtered = new ArrayList<>();

        try {
            doReturn(element).when(changeStatusAction).getSelectedObjectOverride();
            doReturn(lifecycleObject).when(changeStatusAction).attemptToGetConfiguredElement(element);
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(filtered).when(changeStatusAction).filterTransitions(lifecycleObject);

            changeStatusAction.actionPerformed(mock(ActionEvent.class));

            verify(uiDomain).logError(logger, "Transitions Not Present");
            verify(lifecycleTransition, never()).getName();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_userCancelsAction() {
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);
        String name = "name";
        Object[] options = new Object[] {name};

        try {
            doReturn(element).when(changeStatusAction).getSelectedObjectOverride();
            doReturn(lifecycleObject).when(changeStatusAction).attemptToGetConfiguredElement(element);
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(transitions).when(changeStatusAction).filterTransitions(lifecycleObject);
            doReturn(name).when(lifecycleTransition).getName();
            doReturn(-1).when(uiDomain).promptForSelection(PluginConstant.SELECT_TRANSITION_PROMPT_MESSAGE,
                PluginConstant.SELECT_TRANSITION_PROMPT_TITLE, options);

            changeStatusAction.actionPerformed(mock(ActionEvent.class));

            verify(lifecycleTransition).getName();
            verify(lifecycleTransition, never()).getTargetStatus();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_noActiveChangeRecord() {
        lifecycleObject = mock(ConfiguredElement.class);
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);
        String name = "name";
        Object[] options = new Object[] {name};

        try {
            doReturn(element).when(changeStatusAction).getSelectedObjectOverride();
            doReturn(lifecycleObject).when(changeStatusAction).attemptToGetConfiguredElement(element);
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(transitions).when(changeStatusAction).filterTransitions(lifecycleObject);
            doReturn(name).when(lifecycleTransition).getName();
            doReturn(0).when(uiDomain).promptForSelection(PluginConstant.SELECT_TRANSITION_PROMPT_MESSAGE,
                    PluginConstant.SELECT_TRANSITION_PROMPT_TITLE, options);
            doReturn(lifecycleStatus).when(lifecycleTransition).getTargetStatus();
            doReturn(null).when(configurationManagementService).getSelectedChangeRecord();

            changeStatusAction.actionPerformed(mock(ActionEvent.class));

            verify(uiDomain).logError(logger, ExceptionConstants.ACTIVATE_CHANGE_RECORD_BEFORE_CHANGING_STATUS);
            verify(lifecycleObject, never()).canBePromoted(lifecycleTransition, changeRecord);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_cannotBePromoted() {
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);
        String name = "name";
        Object[] options = new Object[] {name};

        try {
            doReturn(element).when(changeStatusAction).getSelectedObjectOverride();
            doReturn(lifecycleObject).when(changeStatusAction).attemptToGetConfiguredElement(element);
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(transitions).when(changeStatusAction).filterTransitions(lifecycleObject);
            doReturn(name).when(lifecycleTransition).getName();
            doReturn(0).when(uiDomain).promptForSelection(PluginConstant.SELECT_TRANSITION_PROMPT_MESSAGE,
                PluginConstant.SELECT_TRANSITION_PROMPT_TITLE, options);
            doReturn(lifecycleStatus).when(lifecycleTransition).getTargetStatus();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(false).when(lifecycleObject).canBePromoted(lifecycleTransition, changeRecord);

            changeStatusAction.actionPerformed(mock(ActionEvent.class));

            verify(uiDomain).logErrorAndShowMessage(logger, ExceptionConstants.PROMOTION_FAILURE_CHANGE_STATUS_ACTION,
                "Promotion failure");
            verify(lifecycleObject, never()).changeStatus(lifecycleStatus, changeRecord);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_cannotChangeStatus() {
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);
        String name = "name";
        Object[] options = new Object[] {name};

        try {
            doReturn(element).when(changeStatusAction).getSelectedObjectOverride();
            doReturn(lifecycleObject).when(changeStatusAction).attemptToGetConfiguredElement(element);
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(transitions).when(changeStatusAction).filterTransitions(lifecycleObject);
            doReturn(name).when(lifecycleTransition).getName();
            doReturn(0).when(uiDomain).promptForSelection(PluginConstant.SELECT_TRANSITION_PROMPT_MESSAGE,
                PluginConstant.SELECT_TRANSITION_PROMPT_TITLE, options);
            doReturn(lifecycleStatus).when(lifecycleTransition).getTargetStatus();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(lifecycleObject).canBePromoted(lifecycleTransition, changeRecord);
            doReturn(false).when(lifecycleObject).changeStatus(lifecycleStatus, changeRecord);

            changeStatusAction.actionPerformed(mock(ActionEvent.class));

            verify(apiDomain).setCurrentProjectHardDirty();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_exceptionWhenTryingToChangeStatus() {
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);
        String name = "name";
        Object[] options = new Object[] {name};
        Exception exception = mock(NullPointerException.class);
        String elementName = "elementName";

        try {
            doReturn(element).when(changeStatusAction).getSelectedObjectOverride();
            doReturn(lifecycleObject).when(changeStatusAction).attemptToGetConfiguredElement(element);
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(transitions).when(changeStatusAction).filterTransitions(lifecycleObject);
            doReturn(name).when(lifecycleTransition).getName();
            doReturn(0).when(uiDomain).promptForSelection(PluginConstant.SELECT_TRANSITION_PROMPT_MESSAGE,
                    PluginConstant.SELECT_TRANSITION_PROMPT_TITLE, options);
            doReturn(lifecycleStatus).when(lifecycleTransition).getTargetStatus();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(lifecycleObject).canBePromoted(lifecycleTransition, changeRecord);
            doThrow(exception).when(lifecycleObject).changeStatus(lifecycleStatus, changeRecord);
            when(lifecycleObject.getName()).thenReturn(elementName);

            changeStatusAction.actionPerformed(mock(ActionEvent.class));

            verify(uiDomain).logErrorAndShowMessage(logger,
                    String.format(ExceptionConstants.ERROR_WHILE_CHANGING_STATUS_SINGLE_OBJECT, lifecycleObject.getName()),
                    "ChangeStatus failure");
            verify(apiDomain, never()).setCurrentProjectHardDirty();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_noErrors() {
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);
        String name = "name";
        Object[] options = new Object[] {name};

        try {
            doReturn(element).when(changeStatusAction).getSelectedObjectOverride();
            doReturn(lifecycleObject).when(changeStatusAction).attemptToGetConfiguredElement(element);
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(transitions).when(changeStatusAction).filterTransitions(lifecycleObject);
            doReturn(name).when(lifecycleTransition).getName();
            doReturn(0).when(uiDomain).promptForSelection(PluginConstant.SELECT_TRANSITION_PROMPT_MESSAGE,
                PluginConstant.SELECT_TRANSITION_PROMPT_TITLE, options);
            doReturn(lifecycleStatus).when(lifecycleTransition).getTargetStatus();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(lifecycleObject).canBePromoted(transitions.get(0), changeRecord);
            doReturn(true).when(lifecycleObject).changeStatus(lifecycleStatus, changeRecord);

            changeStatusAction.actionPerformed(mock(ActionEvent.class));

            verify(apiDomain).setCurrentProjectHardDirty();
            verify(uiDomain, never()).logErrorAndShowMessage(any(), anyString(), anyString(), any());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void filterTransitions_transitionFilteredOut() {
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);

        try {
            doReturn(true).when(configurationManagementService).getAutomateReleaseSwitch();
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(true).when(lifecycleTransition).isReleasingTransition();

            List<LifecycleTransition> results = changeStatusAction.filterTransitions(lifecycleObject);

            assertTrue(results.isEmpty());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void filterTransitions_objectIsChangeRecord() {
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);
        lifecycleObject = mock(ChangeRecord.class);

        try {
            doReturn(true).when(configurationManagementService).getAutomateReleaseSwitch();
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(true).when(lifecycleTransition).isReleasingTransition();

            List<LifecycleTransition> results = changeStatusAction.filterTransitions(lifecycleObject);

            assertFalse(results.isEmpty());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void filterTransitions_transitionNotReleasing() {
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);

        try {
            doReturn(true).when(configurationManagementService).getAutomateReleaseSwitch();
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(false).when(lifecycleTransition).isReleasingTransition();

            List<LifecycleTransition> results = changeStatusAction.filterTransitions(lifecycleObject);

            assertFalse(results.isEmpty());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void filterTransitions_autoReleaseOff() {
        List<LifecycleTransition> transitions = new ArrayList<>();
        transitions.add(lifecycleTransition);

        try {
            doReturn(false).when(configurationManagementService).getAutomateReleaseSwitch();
            doReturn(transitions).when(lifecycleObject).getTransitions();
            doReturn(true).when(lifecycleTransition).isReleasingTransition();

            List<LifecycleTransition> results = changeStatusAction.filterTransitions(lifecycleObject);

            assertFalse(results.isEmpty());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void attemptToGetConfiguredElement_cannotGetConfiguredElement() {
        doReturn(null).when(configurationManagementService).getBaseCEStereotype();
        doReturn(null).when(configurationManagementService).getBaseCRStereotype();

        assertNull(changeStatusAction.attemptToGetConfiguredElement(element));
    }

    @Test
    public void attemptToGetConfiguredElement_hasCeStereotypeButNotOnElement() {
        doReturn(stereotype).when(configurationManagementService).getBaseCEStereotype();
        doReturn(null).when(configurationManagementService).getBaseCRStereotype();
        doReturn(false).when(apiDomain).hasStereotypeOrDerived(element, stereotype);

        assertNull(changeStatusAction.attemptToGetConfiguredElement(element));
    }

    @Test
    public void attemptToGetConfiguredElement_hasCeStereotype() {
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);

        doReturn(stereotype).when(configurationManagementService).getBaseCEStereotype();
        doReturn(null).when(configurationManagementService).getBaseCRStereotype();
        doReturn(true).when(apiDomain).hasStereotypeOrDerived(element, stereotype);
        doReturn(configuredElement).when(lifecycleObjectFactory).getConfiguredElement(configurationManagementService, element);

        LifecycleObject result = changeStatusAction.attemptToGetConfiguredElement(element);

        assertNotNull(result);
        assertEquals(configuredElement, result);
    }

    @Test
    public void attemptToGetConfiguredElement_hasCrStereotypeButNotOnElement() {
        doReturn(null).when(configurationManagementService).getBaseCEStereotype();
        doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
        doReturn(false).when(apiDomain).hasStereotypeOrDerived(element, stereotype);

        assertNull(changeStatusAction.attemptToGetConfiguredElement(element));
    }

    @Test
    public void attemptToGetConfiguredElement_hasCrStereotype() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        Class element = mock(Class.class);

        doReturn(null).when(configurationManagementService).getBaseCEStereotype();
        doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
        doReturn(true).when(apiDomain).hasStereotypeOrDerived(element, stereotype);
        doReturn(changeRecord).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, element);

        LifecycleObject result = changeStatusAction.attemptToGetConfiguredElement(element);

        assertNotNull(result);
        assertEquals(changeRecord, result);
    }

    @Test
    public void updateState_selectedObjectAvailable() {
        doReturn(element).when(changeStatusAction).getSelectedObjectOverride();
        doReturn(lifecycleObjectDomain).when(changeStatusAction).getLifecycleObjectDomain();
        doReturn(true).when(lifecycleObjectDomain).canBePromoted(configurationManagementService, element);
        doNothing().when(changeStatusAction).setEnabled(true);

        changeStatusAction.updateState();

        verify(changeStatusAction).setEnabled(true);
        verify(changeStatusAction, never()).setEnabled(false);
    }

    @Test
    public void updateState_selectedObjectNull() {
        doReturn(null).when(changeStatusAction).getSelectedObjectOverride();
        doReturn(lifecycleObjectDomain).when(changeStatusAction).getLifecycleObjectDomain();

        changeStatusAction.updateState();

        verify(changeStatusAction).setEnabled(false);
        verify(changeStatusAction, never()).setEnabled(true);
        verify(uiDomain).logDebug(logger, ExceptionConstants.CHANGE_STATUS_ACTION_UPDATE_FAILURE);
    }

    @Test
    public void updateState_selectedObjectNotElement() {
        Object selected = "selected";

        doReturn(selected).when(changeStatusAction).getSelectedObjectOverride();
        doReturn(lifecycleObjectDomain).when(changeStatusAction).getLifecycleObjectDomain();

        changeStatusAction.updateState();

        verify(changeStatusAction).setEnabled(false);
        verify(changeStatusAction, never()).setEnabled(true);
        verify(uiDomain).logDebug(logger, ExceptionConstants.CHANGE_STATUS_ACTION_UPDATE_FAILURE);
    }
}