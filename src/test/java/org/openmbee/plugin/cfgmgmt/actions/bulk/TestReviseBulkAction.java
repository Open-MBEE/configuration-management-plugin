package org.openmbee.plugin.cfgmgmt.actions.bulk;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class TestReviseBulkAction {
    private ReviseBulkAction reviseBulkAction;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private Logger logger;
    private ActionEvent actionEvent;
    private ElementSelectionDlg elementSelectionDlg;
    private List<BaseElement> selectedElements;
    private BaseElement baseElement;
    private ConfiguredElement configuredElement;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        reviseBulkAction = Mockito.spy(new ReviseBulkAction(configurationManagementService));
        logger = mock(Logger.class);

        actionEvent = mock(ActionEvent.class);
        elementSelectionDlg = mock(ElementSelectionDlg.class);
        selectedElements = new ArrayList<>();
        baseElement = mock(NamedElement.class);
        selectedElements.add(baseElement);
        configuredElement = mock(ConfiguredElement.class);

        doReturn(apiDomain).when(configurationManagementService).getApiDomain();
        doReturn(uiDomain).when(configurationManagementService).getUIDomain();
        doReturn(logger).when(reviseBulkAction).getLogger();
        doNothing().when(elementSelectionDlg).setVisible(true);
        doReturn(configurationManagementService).when(reviseBulkAction).getConfigurationManagementService();
        doReturn(elementSelectionDlg).when(uiDomain).createElementSelectionDialog(configurationManagementService);
    }

    @Test
    public void actionPerformed_OkNotClicked() {
        doReturn(false).when(elementSelectionDlg).isOkClicked();

        reviseBulkAction.actionPerformed(actionEvent);

        verify(elementSelectionDlg).setVisible(true);
        verify(elementSelectionDlg, never()).getSelectedElements();
    }

    @Test
    public void actionPerformed_elementNotRevised() {
        doReturn(true).when(elementSelectionDlg).isOkClicked();
        doReturn(selectedElements).when(elementSelectionDlg).getSelectedElements();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement((NamedElement) baseElement);
        doReturn(false).when(reviseBulkAction).reviseElement(configuredElement);

        reviseBulkAction.actionPerformed(actionEvent);

        verify(uiDomain).logErrorAndShowMessage(logger, ExceptionConstants.ERROR_DURING_BULK_REVISE, "Bulk Revise Failure");
    }

    @Test
    public void actionPerformed_exceptionDuringReviseElement() {
        String error = "error";
        Exception exception = spy(new NullPointerException(error));

        doReturn(true).when(elementSelectionDlg).isOkClicked();
        doReturn(selectedElements).when(elementSelectionDlg).getSelectedElements();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement((NamedElement) baseElement);
        doThrow(exception).when(reviseBulkAction).reviseElement(configuredElement);

        reviseBulkAction.actionPerformed(actionEvent);

        verify(uiDomain).showErrorMessage(error, ExceptionConstants.BULK_CONFIGURED_ELEMENT_REVISE_FAILURE);
    }

    @Test
    public void actionPerformed_elementRevised() {
        doReturn(true).when(elementSelectionDlg).isOkClicked();
        doReturn(selectedElements).when(elementSelectionDlg).getSelectedElements();
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement((NamedElement) baseElement);
        doReturn(true).when(reviseBulkAction).reviseElement(configuredElement);

        reviseBulkAction.actionPerformed(actionEvent);

        verify(uiDomain, never()).logErrorAndShowMessage(logger, ExceptionConstants.ERROR_DURING_BULK_REVISE, "Bulk Revise Failure");
    }

    @Test
    public void reviseElement_nullElement() {
        reviseBulkAction.reviseElement(null);

        verify(configurationManagementService, never()).setLifecycleStatusChanging(true);
    }

    @Test
    public void reviseElement_elementCannotBeRevised() {
        doReturn(false).when(configuredElement).canBeRevised();

        reviseBulkAction.reviseElement(configuredElement);

        verify(configurationManagementService, never()).setLifecycleStatusChanging(true);
    }

    @Test
    public void reviseElement_canBeRevisedButFailsDuringAttempt() {
        String elementName = "name";

        doReturn(true).when(configuredElement).canBeRevised();
        doReturn(false).when(configuredElement).revise();
        when(configuredElement.getName()).thenReturn(elementName);

        assertFalse(reviseBulkAction.reviseElement(configuredElement));

        verify(uiDomain).logError(logger, String.format(ExceptionConstants.ERROR_WHILE_REVISING_ELEMENT, elementName));
    }

    @Test
    public void reviseElement_exceptionDuringRevise() {
        Exception exception = mock(NullPointerException.class);
        String elementName = "name";

        doReturn(true).when(configuredElement).canBeRevised();
        doThrow(exception).when(configuredElement).revise();
        when(configuredElement.getName()).thenReturn(elementName);

        assertFalse(reviseBulkAction.reviseElement(configuredElement));

        verify(uiDomain).logError(logger, String.format(ExceptionConstants.ERROR_WHILE_REVISING_ELEMENT, elementName));
    }

    @Test
    public void reviseElement_canBeRevisedAndSucceeds() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        String revision = "AA";

        doReturn(true).when(configuredElement).canBeRevised();
        doReturn(true).when(configuredElement).revise();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(revision).when(configuredElement).getRevision();
        doNothing().when(changeRecord).addAffectedElement(configuredElement, String.format(PluginConstant.REVISING_ACTION, revision));
        doNothing().when(apiDomain).setCurrentProjectHardDirty();

        reviseBulkAction.reviseElement(configuredElement);

        verify(apiDomain).setCurrentProjectHardDirty();
        verify(uiDomain, never()).showErrorMessage(any(), any());
    }
}
