package org.openmbee.plugin.cfgmgmt.actions.bulk;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TestConfigureBulkAction {
    private ConfigureBulkAction configureBulkAction;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private ActionEvent actionEvent;
    private ElementSelectionDlg elementSelectionDlg;
    private BaseElement baseElement;
    private Stereotype stereotype;
    private ConfiguredElementDomain configuredElementDomain;
    private ConfiguredElement configuredElement;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        uiDomain = mock(UIDomain.class);
        apiDomain = mock(ApiDomain.class);
        elementSelectionDlg = mock(ElementSelectionDlg.class);
        configuredElementDomain = mock(ConfiguredElementDomain.class);
        actionEvent = mock(ActionEvent.class);
        baseElement = mock(NamedElement.class);
        stereotype = mock(Stereotype.class);
        configuredElement = mock(ConfiguredElement.class);

        configureBulkAction = Mockito.spy(new ConfigureBulkAction(configurationManagementService));

        doReturn(apiDomain).when(configurationManagementService).getApiDomain();
        doReturn(uiDomain).when(configurationManagementService).getUIDomain();
        doReturn(configuredElementDomain).when(configurationManagementService).getConfiguredElementDomain();
        doReturn(elementSelectionDlg).when(uiDomain).createElementSelectionDialog(configuredElementDomain, configurationManagementService);
    }

    @Test
    public void actionPerformed_userDoesNotClickOk() {
        doReturn(false).when(elementSelectionDlg).isOkClicked();

        configureBulkAction.actionPerformed(actionEvent);

        verify(elementSelectionDlg).isOkClicked();
        verify(elementSelectionDlg, never()).getSelectedElements();
    }

    @Test
    public void actionPerformed_userClicksOkButBadElementCannotBeConfigured() {
        List<BaseElement> selectedElements = new ArrayList<>();
        BaseElement badElement = mock(BaseElement.class);
        selectedElements.add(badElement);
        selectedElements.add(baseElement);

        doReturn(true).when(elementSelectionDlg).isOkClicked();
        doReturn(selectedElements).when(elementSelectionDlg).getSelectedElements();
        doReturn(false).when(configureBulkAction).configureElement(badElement);
        doReturn(true).when(configureBulkAction).configureElement(baseElement);

        configureBulkAction.actionPerformed(actionEvent);

        verify(configureBulkAction).configureElement(badElement);
        verify(configureBulkAction, never()).configureElement(baseElement);
    }

    @Test
    public void actionPerformed_exceptionDuringConfigureAttempt() {
        List<BaseElement> selectedElements = new ArrayList<>();
        selectedElements.add(baseElement);
        String error = "error";
        Exception exception = spy(new NullPointerException(error));

        doReturn(true).when(elementSelectionDlg).isOkClicked();
        doReturn(selectedElements).when(elementSelectionDlg).getSelectedElements();
        doThrow(exception).when(configureBulkAction).configureElement(baseElement);

        configureBulkAction.actionPerformed(actionEvent);

        verify(uiDomain).showErrorMessage(String.format(PluginConstant.AN_ERROR_OCCURRED, error), "Configuring element failure");
    }

    @Test
    public void actionPerformed_userClicksOkAndAllElementsGetConfigured() {
        List<BaseElement> selectedElements = new ArrayList<>();
        BaseElement goodElement = mock(BaseElement.class);
        selectedElements.add(goodElement);
        selectedElements.add(baseElement);

        doReturn(true).when(elementSelectionDlg).isOkClicked();
        doReturn(selectedElements).when(elementSelectionDlg).getSelectedElements();
        doReturn(true).when(configureBulkAction).configureElement(goodElement);
        doReturn(true).when(configureBulkAction).configureElement(baseElement);

        configureBulkAction.actionPerformed(actionEvent);

        verify(configureBulkAction).configureElement(goodElement);
        verify(configureBulkAction).configureElement(baseElement);
    }

    @Test
    public void configureElement_noApplicableStereotype() {
        List<Stereotype> applicableStereotypes = new ArrayList<>();

        doReturn(applicableStereotypes).when(configurationManagementService).getApplicableStereotypes((Element) baseElement);
        doReturn(null).when(configurationManagementService).userChoosesDesiredStereotype(applicableStereotypes,
                PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE, PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION);

        assertFalse(configureBulkAction.configureElement(baseElement));

        verify(configurationManagementService).getApplicableStereotypes((Element) baseElement);
        verify(configuredElementDomain, never()).checkConfiguredElementPermissions(any(), any(), any());
    }

    @Test
    public void configureElement_lacksPermissions() {
        List<Stereotype> applicableStereotypes = new ArrayList<>();
        applicableStereotypes.add(stereotype);
        String elementName = "name";

        doReturn(applicableStereotypes).when(configurationManagementService).getApplicableStereotypes((Element) baseElement);
        doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(applicableStereotypes,
                PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE, PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION);
        doReturn(configuredElement).when(configurationManagementService).getCCZOwner((Element) baseElement);
        doReturn(false).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement);
        when(baseElement.getHumanName()).thenReturn(elementName);

        assertFalse(configureBulkAction.configureElement(baseElement));

        verify(apiDomain, never()).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
    }

    @Test
    public void configureElement_badIdInput() {
        List<Stereotype> applicableStereotypes = new ArrayList<>();
        applicableStereotypes.add(stereotype);
        String id = "";
        String name = "name";
        String message = String.format(PluginConstant.PLEASE_ENTER_THE_CONFIGURED_ELEMENT_ID, name);

        doReturn(applicableStereotypes).when(configurationManagementService).getApplicableStereotypes((Element) baseElement);
        doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(applicableStereotypes,
                PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE, PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION);
        doReturn(configuredElement).when(configurationManagementService).getCCZOwner((Element) baseElement);
        doReturn(true).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement);
        doReturn(true).when(apiDomain).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
        doReturn(name).when((NamedElement) baseElement).getName();
        doReturn(id).when(uiDomain).askForInput(message);

        assertFalse(configureBulkAction.configureElement(baseElement));

        verify(uiDomain).askForInput(message);
        verify(apiDomain, never()).setCurrentProjectHardDirty();
    }

    @Test
    public void configureElement_nullFromInput() {
        List<Stereotype> applicableStereotypes = new ArrayList<>();
        applicableStereotypes.add(stereotype);
        String name = "name";
        String message = String.format(PluginConstant.PLEASE_ENTER_THE_CONFIGURED_ELEMENT_ID, name);

        doReturn(applicableStereotypes).when(configurationManagementService).getApplicableStereotypes((Element) baseElement);
        doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(applicableStereotypes,
                PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE, PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION);
        doReturn(configuredElement).when(configurationManagementService).getCCZOwner((Element) baseElement);
        doReturn(true).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement);
        doReturn(true).when(apiDomain).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
        doReturn(name).when((NamedElement) baseElement).getName();
        doReturn(null).when(uiDomain).askForInput(message);

        assertFalse(configureBulkAction.configureElement(baseElement));

        verify(uiDomain).askForInput(message);
        verify(apiDomain, never()).setCurrentProjectHardDirty();
    }

    @Test
    public void configureElement_validInput() {
        List<Stereotype> applicableStereotypes = new ArrayList<>();
        applicableStereotypes.add(stereotype);
        String name = "name";
        String id = "id";
        String message = String.format(PluginConstant.PLEASE_ENTER_THE_CONFIGURED_ELEMENT_ID, name);

        doReturn(applicableStereotypes).when(configurationManagementService).getApplicableStereotypes((Element) baseElement);
        doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(applicableStereotypes,
                PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE, PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION);
        doReturn(configuredElement).when(configurationManagementService).getCCZOwner((Element) baseElement);
        doReturn(true).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement);
        doReturn(true).when(apiDomain).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
        doReturn(name).when((NamedElement) baseElement).getName();
        doReturn(id).when(uiDomain).askForInput(message);
        doReturn(configuredElement).when(configurationManagementService).configureElement((Element) baseElement, id, stereotype);

        assertTrue(configureBulkAction.configureElement(baseElement));

        verify(uiDomain).askForInput(message);
        verify(apiDomain).setCurrentProjectHardDirty();
    }

    @Test
    public void configureElement_notUsingCustomIds() {
        List<Stereotype> applicableStereotypes = new ArrayList<>();
        applicableStereotypes.add(stereotype);
        String id = "id";

        doReturn(applicableStereotypes).when(configurationManagementService).getApplicableStereotypes((Element) baseElement);
        doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(applicableStereotypes,
                PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE, PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION);
        doReturn(configuredElement).when(configurationManagementService).getCCZOwner((Element) baseElement);
        doReturn(true).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement);
        doReturn(false).when(apiDomain).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
        doReturn(id).when((Element) baseElement).getLocalID();
        doReturn(configuredElement).when(configurationManagementService).configureElement((Element) baseElement, id, stereotype);

        assertTrue(configureBulkAction.configureElement(baseElement));

        verify(apiDomain).setCurrentProjectHardDirty();
    }

    @Test
    public void configureElement_exceptionDuringConfigure() {
        List<Stereotype> applicableStereotypes = new ArrayList<>();
        applicableStereotypes.add(stereotype);
        String id = "id";
        Exception exception = mock(NullPointerException.class);
        Logger logger = mock(Logger.class);

        doReturn(applicableStereotypes).when(configurationManagementService).getApplicableStereotypes((Element) baseElement);
        doReturn(stereotype).when(configurationManagementService).userChoosesDesiredStereotype(applicableStereotypes,
                PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE, PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION);
        doReturn(configuredElement).when(configurationManagementService).getCCZOwner((Element) baseElement);
        doReturn(true).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement);
        doReturn(false).when(apiDomain).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
        doReturn(id).when((Element) baseElement).getLocalID();
        doThrow(exception).when(configurationManagementService).configureElement((Element) baseElement, id, stereotype);
        doReturn(logger).when(configureBulkAction).getLogger();

        assertFalse(configureBulkAction.configureElement(baseElement));
        verify(uiDomain).logError(logger, ExceptionConstants.ERROR_WHILE_CONFIGURING_ELEMENT);
        verify(apiDomain, never()).setCurrentProjectHardDirty();
    }
}
