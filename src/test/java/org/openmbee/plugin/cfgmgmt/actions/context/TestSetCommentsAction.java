package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleObject;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class TestSetCommentsAction {
    private SetCommentsAction setCommentsAction;
    private ConfigurationManagementService configurationManagementService;
    private LifecycleObject lifecycleObject;
    private ConfiguredElement configuredElement;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private ActionEvent actionEvent;
    private Element element;
    private ChangeRecord changeRecord;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        setCommentsAction = spy(new SetCommentsAction(configurationManagementService));
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        actionEvent = mock(ActionEvent.class);
        configuredElement = mock(ConfiguredElement.class);
        element = mock(Element.class);
        lifecycleObject = mock(LifecycleObject.class);
        changeRecord = mock(ChangeRecord.class);

        doReturn(apiDomain).when(configurationManagementService).getApiDomain();
        doReturn(uiDomain).when(configurationManagementService).getUIDomain();
        doReturn(element).when(setCommentsAction).getSelectedObjectOverride();
    }

    @Test
    public void actionPerformed_nullLifeCycleObject() {
        doReturn(null).when(configurationManagementService).getChangeRecord(element);
        doReturn(null).when(configurationManagementService).getConfiguredElement(element);

        setCommentsAction.actionPerformed(actionEvent);
        verify(uiDomain).showErrorMessage("Cannot set comments on selected object.", "Set comments failure");
        verify(uiDomain, never()).askForConfirmation(any(), any());
    }

    @Test
    public void actionPerformed_cancelSelection() {
        String comments = "comments";
        JPanel jPanel = mock(JPanel.class);
        doReturn(changeRecord).when(configurationManagementService).getChangeRecord(element);
        doReturn(comments).when(changeRecord).getComments();
        doReturn(jPanel).when(setCommentsAction).getPanel(comments);
        doReturn(1).when(uiDomain).askForConfirmation(jPanel, "Please provide the revision comments");

        setCommentsAction.actionPerformed(actionEvent);
        verify(uiDomain).askForConfirmation(jPanel, "Please provide the revision comments");
        verify(setCommentsAction, never()).getTextArea();
    }

    @Test
    public void actionPerformed_okSelection() {
        String comments = "comments";
        JPanel jPanel = mock(JPanel.class);
        JTextArea textArea = mock(JTextArea.class);
        String text = "text";
        doReturn(changeRecord).when(configurationManagementService).getChangeRecord(element);
        doReturn(comments).when(changeRecord).getComments();
        doReturn(jPanel).when(setCommentsAction).getPanel(comments);
        doReturn(0).when(uiDomain).askForConfirmation(jPanel, "Please provide the revision comments");
        doReturn(textArea).when(setCommentsAction).getTextArea();
        doReturn(text).when(textArea).getText();

        setCommentsAction.actionPerformed(actionEvent);
        verify(apiDomain).setCurrentProjectHardDirty();
    }

    @Test
    public void updateState_nullSelection() {
        doReturn(null).when(setCommentsAction).getSelectedObjectOverride();

        setCommentsAction.updateState();

        verify(setCommentsAction).setEnabled(false);
    }

    @Test
    public void updateState_notNamedElement() {
        doReturn(mock(Object.class)).when(setCommentsAction).getSelectedObjectOverride();

        setCommentsAction.updateState();

        verify(setCommentsAction).setEnabled(false);
    }

    @Test
    public void updateState_namedElementNotEditable() {
        Element namedElement = mock(NamedElement.class);

        doReturn(namedElement).when(setCommentsAction).getSelectedObjectOverride();
        doReturn(false).when(apiDomain).isElementInEditableState(namedElement);

        setCommentsAction.updateState();

        verify(apiDomain).isElementInEditableState(namedElement);
        verify(apiDomain, never()).hasStereotypeOrDerived(any(), any());
    }

    @Test
    public void updateState_namedElementHasStereotypeButNotReleased() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setCommentsAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(true).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(namedElement);
            doReturn(false).when(changeRecord).isReleased();
            doNothing().when(setCommentsAction).setEnabled(true);

            setCommentsAction.updateState();

            verify(setCommentsAction).setEnabled(true);
            verify(configurationManagementService, never()).isCmActive();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementHasStereotypeAndReleased() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);
        try {
            doReturn(namedElement).when(setCommentsAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(true).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(changeRecord).when(configurationManagementService).getChangeRecord(namedElement);
            doReturn(true).when(changeRecord).isReleased();
            doNothing().when(setCommentsAction).setEnabled(false);

            setCommentsAction.updateState();

            verify(setCommentsAction).setEnabled(false);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementCmInactive() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        doReturn(namedElement).when(setCommentsAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
        doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
        doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
        doReturn(false).when(configurationManagementService).isCmActive();
        doNothing().when(setCommentsAction).setEnabled(false);

        setCommentsAction.updateState();

        verify(setCommentsAction).setEnabled(false);
        verify(configurationManagementService, never()).isConfigured(namedElement);
    }

    @Test
    public void updateState_namedElementCRNotSelected() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        doReturn(namedElement).when(setCommentsAction).getSelectedObjectOverride();
        doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
        doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
        doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(false).when(configurationManagementService).isChangeRecordSelected();
        doNothing().when(setCommentsAction).setEnabled(false);

        setCommentsAction.updateState();

        verify(setCommentsAction).setEnabled(false);
        verify(configurationManagementService, never()).isConfigured(namedElement);
    }

    @Test
    public void updateState_namedElementNotConfigured() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setCommentsAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(false).when(configurationManagementService).isConfigured(namedElement);
            doNothing().when(setCommentsAction).setEnabled(false);

            setCommentsAction.updateState();

            verify(setCommentsAction).setEnabled(false);
            verify(configuredElement, never()).isReleased();
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementIsReleasedFalse() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setCommentsAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(true).when(configurationManagementService).isConfigured(namedElement);
            doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(namedElement);
            doReturn(false).when(configuredElement).isReleased();
            doNothing().when(setCommentsAction).setEnabled(true);

            setCommentsAction.updateState();

            verify(setCommentsAction).setEnabled(true);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState_namedElementIsReleasedTrue() {
        Element namedElement = mock(NamedElement.class);
        Stereotype stereotype = mock(Stereotype.class);

        try {
            doReturn(namedElement).when(setCommentsAction).getSelectedObjectOverride();
            doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
            doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
            doReturn(false).when(apiDomain).hasStereotypeOrDerived(namedElement, stereotype);
            doReturn(true).when(configurationManagementService).isCmActive();
            doReturn(true).when(configurationManagementService).isChangeRecordSelected();
            doReturn(true).when(configurationManagementService).isConfigured(namedElement);
            doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(namedElement);
            doReturn(true).when(configuredElement).isReleased();
            doNothing().when(setCommentsAction).setEnabled(false);

            setCommentsAction.updateState();

            verify(setCommentsAction).setEnabled(false);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }
}
