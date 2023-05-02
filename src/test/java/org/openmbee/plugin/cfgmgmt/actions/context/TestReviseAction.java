package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.LifecycleObjectDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;

import static org.mockito.Mockito.*;

public class TestReviseAction {
    private ReviseAction reviseAction;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private LifecycleObjectDomain lifecycleObjectDomain;
    private ActionEvent actionEvent;
    private Element element;
    private ConfiguredElement configuredElement;
    private Stereotype stereotype;
    private Logger logger;
    private String action;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        reviseAction = spy(new ReviseAction(configurationManagementService));
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        lifecycleObjectDomain = mock(LifecycleObjectDomain.class);
        actionEvent = mock(ActionEvent.class);
        element = mock(Element.class);
        configuredElement = mock(ConfiguredElement.class);
        stereotype = mock(Stereotype.class);
        logger = mock(Logger.class);
        action = "revision";

        when(reviseAction.getApiDomain()).thenReturn(apiDomain);
        when(reviseAction.getUIDomain()).thenReturn(uiDomain);
        when(reviseAction.getLifecycleObjectDomain()).thenReturn(lifecycleObjectDomain);
        when(reviseAction.getLogger()).thenReturn(logger);
    }

    @Test
    public void actionPerformed_NullElement() {
        doReturn(null).when(reviseAction).getSelectedObjectOverride();

        reviseAction.actionPerformed(actionEvent);

        verify(reviseAction).getSelectedObjectOverride();
    }

    @Test
    public void actionPerformed_NotElement() {
        Object object = new Object();
        doReturn(object).when(reviseAction).getSelectedObjectOverride();

        reviseAction.actionPerformed(actionEvent);

        verify(reviseAction).getSelectedObjectOverride();
    }

    @Test
    public void actionPerformed_noConfiguredElement() {
        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(null).when(configurationManagementService).getConfiguredElement(element);

        reviseAction.actionPerformed(actionEvent);

        verify(configurationManagementService).getConfiguredElement(element);
    }

    @Test
    public void actionPerformed_permissionDenied() {
        String message = "Cannot revise element: Insufficient permissions.";
        String title = String.format("Configured element %s failure", action);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(stereotype).when(configuredElement).getAppliedStereotype();
        doReturn(false).when(lifecycleObjectDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doNothing().when(uiDomain).showErrorMessage(message, title);

        reviseAction.actionPerformed(actionEvent);

        verify(uiDomain).showErrorMessage(message, title);
        verify(configuredElement, never()).getCCZOwner();
    }

    @Test
    public void actionPerformed_noCczOwnerButSuccessfulRevision() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        String revision = "AA";
        String changeText = String.format(PluginConstant.REVISING_ACTION, revision);
        String elementName = "name";

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(stereotype).when(configuredElement).getAppliedStereotype();
        doReturn(true).when(lifecycleObjectDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(null).when(configuredElement).getCCZOwner();
        doReturn(true).when(configuredElement).revise();
        when(configuredElement.getName()).thenReturn(elementName);
        doReturn(revision).when(configuredElement).getRevision();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doNothing().when(changeRecord).addAffectedElement(configuredElement, changeText);
        doNothing().when(apiDomain).setCurrentProjectHardDirty();

        reviseAction.actionPerformed(actionEvent);

        verify(configuredElement, never()).getInitialStatus();
        verify(apiDomain).setCurrentProjectHardDirty();
        verify(uiDomain, never()).logErrorAndShowMessage(logger,
                String.format(ExceptionConstants.ERROR_WHILE_REVISING_ELEMENT + ExceptionConstants.ERROR_DURING_SINGLE_REVISE_SUFFIX, elementName),
                "Revising failure");
    }

    @Test
    public void actionPerformed_noInitialStatus() {
        ConfiguredElement owner = mock(ConfiguredElement.class);
        int ownerRating = 1;
        String message = "Cannot revise element: No initial status.";

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(stereotype).when(configuredElement).getAppliedStereotype();
        doReturn(true).when(lifecycleObjectDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(owner).when(configuredElement).getCCZOwner();
        doReturn(ownerRating).when(owner).getStatusMaturityRating();
        doReturn(null).when(configuredElement).getInitialStatus();
        doNothing().when(uiDomain).showErrorMessage(message, ExceptionConstants.LIFECYCLE_STATUS_ERROR_TITLE);

        reviseAction.actionPerformed(actionEvent);

        verify(uiDomain).showErrorMessage(message, ExceptionConstants.LIFECYCLE_STATUS_ERROR_TITLE);
        verify(configuredElement, never()).revise();
    }

    @Test
    public void actionPerformed_initialRatingLessThanOwnerRating() {
        ConfiguredElement owner = mock(ConfiguredElement.class);
        int ownerRating = 1;
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        int initialRating = 0;
        String qualifiedName = "qualifiedName";
        String id = "id";
        String message = String.format("Cannot revise element due to the status of the CCZ owner: %s[%s]",
                qualifiedName, id);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(stereotype).when(configuredElement).getAppliedStereotype();
        doReturn(true).when(lifecycleObjectDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(owner).when(configuredElement).getCCZOwner();
        doReturn(ownerRating).when(owner).getStatusMaturityRating();
        doReturn(lifecycleStatus).when(configuredElement).getInitialStatus();
        doReturn(initialRating).when(lifecycleStatus).getMaturityRating();
        doReturn(qualifiedName).when(owner).getQualifiedName();
        doReturn(id).when(owner).getID();
        doNothing().when(uiDomain).showErrorMessage(message, ExceptionConstants.LIFECYCLE_STATUS_ERROR_TITLE);

        reviseAction.actionPerformed(actionEvent);

        verify(uiDomain).showErrorMessage(message, ExceptionConstants.LIFECYCLE_STATUS_ERROR_TITLE);
        verify(configuredElement, never()).revise();
    }

    @Test
    public void actionPerformed_revisionUnsuccessful() {
        ConfiguredElement owner = mock(ConfiguredElement.class);
        int ownerRating = 1;
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        int initialRating = 2;
        String elementName = "name";

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(stereotype).when(configuredElement).getAppliedStereotype();
        doReturn(true).when(lifecycleObjectDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(owner).when(configuredElement).getCCZOwner();
        doReturn(ownerRating).when(owner).getStatusMaturityRating();
        doReturn(lifecycleStatus).when(configuredElement).getInitialStatus();
        doReturn(initialRating).when(lifecycleStatus).getMaturityRating();
        doReturn(false).when(configuredElement).revise();
        when(configuredElement.getName()).thenReturn(elementName);

        reviseAction.actionPerformed(actionEvent);

        verify(uiDomain).logErrorAndShowMessage(logger,
                String.format(ExceptionConstants.ERROR_WHILE_REVISING_ELEMENT + ExceptionConstants.ERROR_DURING_SINGLE_REVISE_SUFFIX, elementName),
                "Revising failure");
        verify(apiDomain, never()).setCurrentProjectHardDirty();
    }

    @Test
    public void actionPerformed_exceptionDuringRevision() {
        ConfiguredElement owner = mock(ConfiguredElement.class);
        int ownerRating = 1;
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        int initialRating = 2;
        String elementName = "name";
        Exception exception = mock(NullPointerException.class);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(stereotype).when(configuredElement).getAppliedStereotype();
        doReturn(true).when(lifecycleObjectDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(owner).when(configuredElement).getCCZOwner();
        doReturn(ownerRating).when(owner).getStatusMaturityRating();
        doReturn(lifecycleStatus).when(configuredElement).getInitialStatus();
        doReturn(initialRating).when(lifecycleStatus).getMaturityRating();
        doThrow(exception).when(configuredElement).revise();
        when(configuredElement.getName()).thenReturn(elementName);

        reviseAction.actionPerformed(actionEvent);

        verify(uiDomain).logErrorAndShowMessage(logger,
                String.format(ExceptionConstants.ERROR_WHILE_REVISING_ELEMENT + ExceptionConstants.ERROR_DURING_SINGLE_REVISE_SUFFIX, elementName),
                "Revising failure");
        verify(apiDomain, never()).setCurrentProjectHardDirty();
    }

    @Test
    public void actionPerformed_revisionSuccessful() {
        ConfiguredElement owner = mock(ConfiguredElement.class);
        int ownerRating = 1;
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        int initialRating = 2;
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        String revision = "rev";
        String changeText = String.format(PluginConstant.REVISING_ACTION, revision);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(stereotype).when(configuredElement).getAppliedStereotype();
        doReturn(true).when(lifecycleObjectDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(owner).when(configuredElement).getCCZOwner();
        doReturn(ownerRating).when(owner).getStatusMaturityRating();
        doReturn(lifecycleStatus).when(configuredElement).getInitialStatus();
        doReturn(initialRating).when(lifecycleStatus).getMaturityRating();
        doReturn(true).when(configuredElement).revise();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(revision).when(configuredElement).getRevision();
        doNothing().when(changeRecord).addAffectedElement(configuredElement, changeText);
        doNothing().when(apiDomain).setCurrentProjectHardDirty();

        reviseAction.actionPerformed(actionEvent);

        verify(apiDomain).setCurrentProjectHardDirty();
        verify(uiDomain, never()).logErrorAndShowMessage(logger, ExceptionConstants.ERROR_WHILE_REVISING_ELEMENT, "Revising failure");
    }

    @Test
    public void updateState_nullSelection() {
        doReturn(null).when(reviseAction).getSelectedObjectOverride();
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(apiDomain, never()).isElementInEditableState(any());
    }

    @Test
    public void updateState_wrongType() {
        Object item = mock(Object.class);

        doReturn(item).when(reviseAction).getSelectedObjectOverride();
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(apiDomain, never()).isElementInEditableState(any());
    }

    @Test
    public void updateState_notEditable() {
        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(false).when(apiDomain).isElementInEditableState(element);
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(configurationManagementService, never()).isCmActive();
    }

    @Test
    public void updateState_cmNotActiveAndNoSelectedChangeRecord() {
        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(false).when(configurationManagementService).isCmActive();
        doReturn(false).when(configurationManagementService).isChangeRecordSelected();
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(configurationManagementService, never()).getSelectedChangeRecord();
    }

    @Test
    public void updateState_cmActiveButChangeRecordNotExpendable() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(false).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isExpandable();
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(configurationManagementService, never()).getConfiguredElement(element);
    }

    @Test
    public void updateState_changeRecordSelectedButChangeRecordNotExpendable() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(false).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).isExpandable();
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(configurationManagementService, never()).getConfiguredElement(element);
    }

    @Test
    public void updateState_selectedChangeRecordButChangeRecordNotExpendable() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(true).when(changeRecord).hasStatus();
        doReturn(false).when(changeRecord).isExpandable();
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(configurationManagementService, never()).getConfiguredElement(element);
    }

    @Test
    public void updateState_changeRecordExpendableButConfiguredElementNull() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).hasStatus();
        doReturn(true).when(changeRecord).isExpandable();
        doReturn(null).when(configurationManagementService).getConfiguredElement(element);
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(configuredElement, never()).isReleased();
    }

    @Test
    public void updateState_configuredElementNotReleasedNotCommitted() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(true).when(changeRecord).isExpandable();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(false).when(configuredElement).isReleased();
        doReturn(false).when(configuredElement).isCommitted();
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(reviseAction, never()).setEnabled(true);
    }

    @Test
    public void updateState_configuredElementReleasedButNotCommitted() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).hasStatus();
        doReturn(true).when(changeRecord).isExpandable();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(true).when(configuredElement).isReleased();
        doReturn(false).when(configuredElement).isCommitted();
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(reviseAction, never()).setEnabled(true);
    }

    @Test
    public void updateState_configuredElementNotReleasedButCommitted() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(true).when(changeRecord).hasStatus();
        doReturn(true).when(changeRecord).isExpandable();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(false).when(configuredElement).isReleased();
        doReturn(true).when(configuredElement).isCommitted();
        doNothing().when(reviseAction).setEnabled(false);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
        verify(reviseAction, never()).setEnabled(true);
    }

    @Test
    public void updateState_configuredElementReleasedAndCommitted() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(true).when(changeRecord).isExpandable();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);
        doReturn(true).when(configuredElement).isReleased();
        doReturn(true).when(configuredElement).isCommitted();
        doNothing().when(reviseAction).setEnabled(true);

        reviseAction.updateState();

        verify(reviseAction).setEnabled(true);
        verify(reviseAction, never()).setEnabled(false);
    }

    @Test
    public void updateState_ChangeRecordNull() {

        doReturn(element).when(reviseAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(null).when(configurationManagementService).getSelectedChangeRecord();

        reviseAction.updateState();

        verify(reviseAction).setEnabled(false);
    }
}