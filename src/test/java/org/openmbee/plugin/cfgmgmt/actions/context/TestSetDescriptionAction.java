package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.exception.ConfigurationManagementMissingStatusException;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleObject;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestSetDescriptionAction {
    private SetDescriptionAction setDescriptionAction;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private ActionEvent actionEvent;
    private Logger logger;
    private Element element;
    private ConfiguredElement configuredElement;
    private ChangeRecord changeRecord;
    private LifecycleObject lifecycleObject;
    private LifecycleObjectFactory lifecycleObjectFactory;
    private ConfiguredElementDomain configuredElementDomain;

    @Before
    public void setup() {
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        actionEvent = mock(ActionEvent.class);
        configurationManagementService = mock(ConfigurationManagementService.class);
        element = mock(Element.class);
        configuredElement = mock(ConfiguredElement.class);
        changeRecord = mock(ChangeRecord.class);
        lifecycleObject = mock(LifecycleObject.class);
        lifecycleObjectFactory = mock(LifecycleObjectFactory .class);
        configuredElementDomain = mock(ConfiguredElementDomain.class);
        logger = mock(Logger.class);
        setDescriptionAction = spy(new SetDescriptionAction(configurationManagementService));

        when(setDescriptionAction.getUIDomain()).thenReturn(uiDomain);
        doReturn(element).when(setDescriptionAction).getSelectedObjectOverride();
        when(setDescriptionAction.getLogger()).thenReturn(logger);
        when(setDescriptionAction.getApiDomain()).thenReturn(apiDomain);
        when(setDescriptionAction.getLifecycleObjectFactory()).thenReturn(lifecycleObjectFactory);
        when(setDescriptionAction.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(setDescriptionAction.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
    }

    @Test
    public void getUpdatedDescriptionFromUser_Ok() {
        String currentDescription = "Description";
        JPanel panel = mock(JPanel.class);
        when(setDescriptionAction.getPanel(currentDescription)).thenReturn(panel);
        when(uiDomain.askForConfirmation(panel, "Please provide the description")).thenReturn(0);
        try {
            setDescriptionAction.getUpdatedDescriptionFromUser(currentDescription);
            verify(uiDomain, times(1)).askForConfirmation(panel, "Please provide the description");
        } catch (Exception exception) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getUpdatedDescriptionFromUser_NotOk() {
        String currentDescription = "Description";
        JPanel panel = mock(JPanel.class);
        when(setDescriptionAction.getPanel(currentDescription)).thenReturn(panel);
        when(uiDomain.askForConfirmation(panel, "Please provide the description")).thenReturn(1);
        try {
            setDescriptionAction.getUpdatedDescriptionFromUser(currentDescription);
            verify(uiDomain, times(1)).askForConfirmation(panel, "Please provide the description");
        } catch (Exception exception) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_Exception() {
        NullPointerException exception = mock(NullPointerException.class);
        doReturn(changeRecord).when(configurationManagementService).getChangeRecord(element);
        doThrow(exception).when(setDescriptionAction).getSelectedObjectOverride();

        try{
            setDescriptionAction.actionPerformed(actionEvent);

            verify(setDescriptionAction).getSelectedObjectOverride();
            verify(uiDomain).logErrorAndShowMessage(logger, "Error occurred while setting a description.", "Set description failure", exception);
        }catch(Exception e){
            fail("Undetected Exception");
        }
    }

    @Test
    public void actionPerformed_nullLifeCycleObject() {
        doReturn(null).when(configurationManagementService).getChangeRecord(element);
        doReturn(null).when(configurationManagementService).getConfiguredElement(element);

        setDescriptionAction.actionPerformed(actionEvent);
        verify(uiDomain).showErrorMessage(any(), any());
    }

    @Test
    public void actionPerformed_DescriptionNotNull() {
        doReturn(configurationManagementService).when(setDescriptionAction).getConfigurationManagementService();
        when(configurationManagementService.getChangeRecord(element)).thenReturn(changeRecord);
        LifecycleObject lifecycleObject1 = changeRecord;
        String currentDescription = "CurrentDescription";
        String description = "Description";
        doReturn(currentDescription).when(lifecycleObject1).getDescription();
        doReturn(description).when(setDescriptionAction).getUpdatedDescriptionFromUser(currentDescription);
        setDescriptionAction.actionPerformed(actionEvent);
        verify(lifecycleObject1, times(1)).setDescription(description);
        verify(lifecycleObject1, times(1)).getDescription();
        verify(apiDomain, times(1)).setCurrentProjectHardDirty();
    }

    @Test
    public void actionPerformed_DescriptionNull() {
        String currentDescription = "CurrentDescription";

        doReturn(changeRecord).when(configurationManagementService).getChangeRecord(element);
        doReturn(currentDescription).when(changeRecord).getDescription();
        doReturn(null).when(setDescriptionAction).getUpdatedDescriptionFromUser(currentDescription);

        setDescriptionAction.actionPerformed(actionEvent);

        verify(setDescriptionAction).getUpdatedDescriptionFromUser(currentDescription);
        verify(lifecycleObject, never()).setDescription(anyString());
    }

    @Test
    public void updateState_notNamedElement() {
        setDescriptionAction.updateState();
        verify(setDescriptionAction).setEnabled(false);
    }

    @Test
    public void updateState_namedElementNotEditable() {
        Element namedElement = mock(NamedElement.class);
        doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();

        setDescriptionAction.updateState();
        verify(apiDomain).isElementInEditableState(namedElement);
    }

    @Test
    public void updateState_cmNotActive() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(false).when(configurationManagementService).isCmActive();
            doReturn(false).when(configurationManagementService).isChangeRecordSelected();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(configurationManagementService, never()).getSelectedChangeRecord();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_cmActive() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(false).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(configurationManagementService, never()).getSelectedChangeRecord();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_changeRecordSelected() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(false).when(configurationManagementService).isChangeRecordSelected();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(configurationManagementService, never()).getSelectedChangeRecord();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_selectedChangeRecordNull() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(null).when(configurationManagementService).getSelectedChangeRecord();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(configurationManagementService, never()).isConfigured(namedElement);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_changeRecordNotNull_notExpendable_isReadOnly_hasStatus() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(changeRecord).hasStatus();
            doReturn(false).when(changeRecord).isExpandable();
            doReturn(true).when(changeRecord).isReadOnly();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_changeRecordNotNull_expendable_isNotReadOnly_hasNoStatus() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(false).when(changeRecord).hasStatus();
            doReturn(true).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReadOnly();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(configurationManagementService , never()).isConfigured(element);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_changeRecordNotNull_hasStatus_expendable_isNotReadOnly() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(changeRecord).hasStatus();
            doReturn(true).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReadOnly();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(configurationManagementService , never()).isConfigured(element);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_changeRecordNotNull_hasStatus_notExpendable_isNotReadOnly() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(changeRecord).hasStatus();
            doReturn(false).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReadOnly();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(configurationManagementService , never()).isConfigured(element);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }


    @Test
    public void updateState_namedElementNotConfigured() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);
        ConfigurationManagementMissingStatusException missingStatusException = mock(ConfigurationManagementMissingStatusException.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReadOnly();
            doReturn(false).when(configurationManagementService).isConfigured(namedElement);

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(uiDomain, never()).logErrorAndShowMessage(logger, "Cannot update state for SetDescriptionAction, forcing disable",
                ExceptionConstants.ACTION_STATE_FAILURE, missingStatusException);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementNotConfigured2() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);
        ConfigurationManagementMissingStatusException missingStatusException = mock(ConfigurationManagementMissingStatusException.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(false).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReadOnly();
            doReturn(false).when(configurationManagementService).isConfigured(namedElement);

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(uiDomain, never()).logErrorAndShowMessage(logger, "Cannot update state for SetDescriptionAction, forcing disable",
                ExceptionConstants.ACTION_STATE_FAILURE, missingStatusException);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementIsReadOnly() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);
        ConfigurationManagementMissingStatusException missingStatusException = mock(ConfigurationManagementMissingStatusException.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReadOnly();
            doReturn(true).when(configurationManagementService).isConfigured(namedElement);
            doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(namedElement);
            doReturn(true).when(configuredElement).isReadOnly();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(uiDomain, never()).logErrorAndShowMessage(logger, "Cannot update state for SetDescriptionAction, forcing disable",
                ExceptionConstants.ACTION_STATE_FAILURE, missingStatusException);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementIsNotReadOnly() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);
        ConfigurationManagementMissingStatusException missingStatusException = mock(ConfigurationManagementMissingStatusException.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
            doReturn(true).when(changeRecord).isExpandable();
            doReturn(false).when(changeRecord).isReadOnly();
            doReturn(true).when(configurationManagementService).isConfigured(namedElement);
            doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(namedElement);
            doReturn(false).when(configuredElement).isReadOnly();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(true);
            verify(uiDomain, never()).logErrorAndShowMessage(logger, "Cannot update state for SetDescriptionAction, forcing disable",
                ExceptionConstants.ACTION_STATE_FAILURE, missingStatusException);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementIsChangeRecordButReadOnlyAndNotExpendable() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);
        ConfigurationManagementMissingStatusException missingStatusException = mock(ConfigurationManagementMissingStatusException.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(true).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(namedElement);
            doReturn(true).when(changeRecord).isReadOnly();
            doReturn(false).when(changeRecord).isExpandable();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(uiDomain, never()).logErrorAndShowMessage(logger, "Cannot update state for SetDescriptionAction, forcing disable",
                ExceptionConstants.ACTION_STATE_FAILURE, missingStatusException);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementIsChangeRecordButNotReadOnlyAndExpendable() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);
        ConfigurationManagementMissingStatusException missingStatusException = mock(ConfigurationManagementMissingStatusException.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(true).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(namedElement);
            doReturn(false).when(changeRecord).isReadOnly();
            doReturn(true).when(changeRecord).isExpandable();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(true);
            verify(uiDomain, never()).logErrorAndShowMessage(logger, "Cannot update state for SetDescriptionAction, forcing disable",
                ExceptionConstants.ACTION_STATE_FAILURE, missingStatusException);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementIsChangeRecordButReadOnlyAndExpendable() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);
        ConfigurationManagementMissingStatusException missingStatusException = mock(ConfigurationManagementMissingStatusException.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(true).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(namedElement);
            doReturn(true).when(changeRecord).isReadOnly();
            doReturn(true).when(changeRecord).isExpandable();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(true);
            verify(uiDomain, never()).logErrorAndShowMessage(logger, "Cannot update state for SetDescriptionAction, forcing disable",
                ExceptionConstants.ACTION_STATE_FAILURE, missingStatusException);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementIsChangeRecordButNotReadOnlyAndNotExpendable() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);
        ConfigurationManagementMissingStatusException missingStatusException = mock(ConfigurationManagementMissingStatusException.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(true).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(namedElement);
            doReturn(false).when(changeRecord).isReadOnly();
            doReturn(false).when(changeRecord).isExpandable();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(true);
            verify(uiDomain, never()).logErrorAndShowMessage(logger, "Cannot update state for SetDescriptionAction, forcing disable",
                ExceptionConstants.ACTION_STATE_FAILURE, missingStatusException);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_Exception() {

        NullPointerException nullPointerException = mock(NullPointerException.class);
        doThrow(nullPointerException).when(setDescriptionAction).getSelectedObjectOverride();

        try{
            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(setDescriptionAction).getSelectedObjectOverride();
            verify(uiDomain).logErrorAndShowMessage(logger, "Cannot update state for SetDescriptionAction, forcing disable", ExceptionConstants.ACTION_STATE_FAILURE, nullPointerException);
        }catch(Exception e){
            fail("Undetected Exception");
        }
    }

    @Test
    public void updateState_changeRecordNotNull_isExpendable_isNotReadOnly() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(true).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(namedElement);
            doReturn(false).when(changeRecord).isReadOnly();
            doReturn(true).when(changeRecord).isExpandable();

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(true);
            verify(configurationManagementService , never()).getSelectedChangeRecord();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_changeRecordIsNull() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setDescriptionAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(true).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(null).when(configurationManagementService).getChangeRecord(namedElement);

            setDescriptionAction.updateState();

            verify(setDescriptionAction).setEnabled(false);
            verify(configurationManagementService , never()).getSelectedChangeRecord();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

}
