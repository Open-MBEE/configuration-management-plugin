package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class TestConfigureAction {
    private ConfigureAction configureAction;
    private ConfigurationManagementService configurationManagementService;
    private ConfiguredElementDomain configuredElementDomain;
    private ConfiguredElement configuredElement;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private Logger logger;
    private ActionEvent actionEvent;
    private Element element;
    private Stereotype stereotype;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        configuredElementDomain = mock(ConfiguredElementDomain.class);
        configureAction = Mockito.spy(new ConfigureAction(configurationManagementService));
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        logger = mock(Logger.class);
        actionEvent = mock(ActionEvent.class);
        configuredElement = mock(ConfiguredElement.class);
        element = mock(Element.class);
        stereotype = mock(Stereotype.class);

        doReturn(apiDomain).when(configurationManagementService).getApiDomain();
        doReturn(uiDomain).when(configurationManagementService).getUIDomain();
        when(configureAction.getLogger()).thenReturn(logger);
        doReturn(configuredElementDomain).when(configurationManagementService).getConfiguredElementDomain();
        doReturn(configuredElement).when(configurationManagementService).getCCZOwner(element);
    }

    @Test
    public void actionPerformed_emptyElements() {
        doReturn(null).when(configureAction).getElement();
        configureAction.actionPerformed(actionEvent);
        verify(configureAction).actionPerformed(actionEvent);
    }

    @Test
    public void actionPerformed_emptyApplicableStereotypes() {
        List<Stereotype> stereotypes = new ArrayList<>();

        try {
            doReturn(element).when(configureAction).getElement();
            doReturn(stereotypes).when(configurationManagementService).getApplicableStereotypes(element);

            configureAction.actionPerformed(actionEvent);

            verify(uiDomain).logDebug(logger, "No applicable stereotypes available for ConfigureAction");
            verify(configuredElementDomain, never()).checkConfiguredElementPermissions(any(), any(), any());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_singleStereotypeButNoPermission() {
        List<Stereotype> stereotypes = new ArrayList<>();
        stereotypes.add(stereotype);
        ConfiguredElement owner = mock(ConfiguredElement.class);
        String elementName = "name";

        try {
            doReturn(element).when(configureAction).getElement();
            doReturn(stereotypes).when(configurationManagementService).getApplicableStereotypes(element);
            doReturn(owner).when(configurationManagementService).getCCZOwner(element);
            doReturn(false).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService , stereotype,owner);
            when(element.getHumanName()).thenReturn(elementName);

            configureAction.actionPerformed(actionEvent);

            verify(uiDomain).logErrorAndShowMessage(logger,
                    String.format(ExceptionConstants.CONFIGURED_ELEMENT_PERMISSIONS_FAILURE, elementName),
                    ExceptionConstants.CONFIGURED_ELEMENT_PERMISSIONS_FAILURE_TITLE);
            verify(apiDomain, never()).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_multipleStereotypesButCancelledSelection() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype2 = mock(Stereotype.class);
        stereotypes.add(stereotype);
        stereotypes.add(stereotype2);
        String name = "name";
        String name2 = "name2";
        Object[] options = new Object[] {name, name2};

        try {
            doReturn(element).when(configureAction).getElement();
            doReturn(stereotypes).when(configurationManagementService).getApplicableStereotypes(element);
            doReturn(name).when(stereotype).getName();
            doReturn(name2).when(stereotype2).getName();
            doReturn(JOptionPane.CLOSED_OPTION).when(uiDomain).promptForSelection(PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE,
                    PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION, options);

            configureAction.actionPerformed(actionEvent);

            verify(uiDomain).promptForSelection(PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE,
                    PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION, options);
            verify(configuredElementDomain, never()).checkConfiguredElementPermissions(any(), any(), any());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_configuredElementIdNull() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype2 = mock(Stereotype.class);
        stereotypes.add(stereotype);
        stereotypes.add(stereotype2);
        String name = "name";
        String name2 = "name2";
        Object[] options = new Object[] {name, name2};
        ConfiguredElement owner = mock(ConfiguredElement.class);

        try {
            doReturn(element).when(configureAction).getElement();
            doReturn(stereotypes).when(configurationManagementService).getApplicableStereotypes(element);
            doReturn(name).when(stereotype).getName();
            doReturn(name2).when(stereotype2).getName();
            doReturn(0).when(uiDomain).promptForSelection(PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE,
                    PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION, options);
            doReturn(owner).when(configurationManagementService).getCCZOwner(element);
            doReturn(true).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService , stereotype,owner);
            doReturn(Boolean.TRUE).when(apiDomain).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
            doReturn(null).when(uiDomain).askForInput("Please enter the Configured Element ID");

            configureAction.actionPerformed(actionEvent);

            verify(uiDomain).askForInput("Please enter the Configured Element ID");
            verify(configurationManagementService, never()).configureElement(any(), anyString(), any());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_configuredElementIdEmpty() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype2 = mock(Stereotype.class);
        stereotypes.add(stereotype);
        stereotypes.add(stereotype2);
        String name = "name";
        String name2 = "name2";
        Object[] options = new Object[] {name, name2};
        ConfiguredElement owner = mock(ConfiguredElement.class);

        try {
            doReturn(element).when(configureAction).getElement();
            doReturn(stereotypes).when(configurationManagementService).getApplicableStereotypes(element);
            doReturn(name).when(stereotype).getName();
            doReturn(name2).when(stereotype2).getName();
            doReturn(0).when(uiDomain).promptForSelection(PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE,
                    PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION, options);
            doReturn(owner).when(configurationManagementService).getCCZOwner(element);
            doReturn(Boolean.TRUE).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService , stereotype,owner);
            doReturn(Boolean.TRUE).when(apiDomain).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
            doReturn("").when(uiDomain).askForInput("Please enter the Configured Element ID");

            configureAction.actionPerformed(actionEvent);

            verify(uiDomain).askForInput("Please enter the Configured Element ID");
            verify(configurationManagementService, never()).configureElement(any(), anyString(), any());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_configuredElementValidCustomIdButStillNull() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype2 = mock(Stereotype.class);
        stereotypes.add(stereotype);
        stereotypes.add(stereotype2);
        String name = "name";
        String name2 = "name2";
        Object[] options = new Object[] {name, name2};
        ConfiguredElement owner = mock(ConfiguredElement.class);
        String id = "id";

        try {
            doReturn(element).when(configureAction).getElement();
            doReturn(stereotypes).when(configurationManagementService).getApplicableStereotypes(element);
            doReturn(name).when(stereotype).getName();
            doReturn(name2).when(stereotype2).getName();
            doReturn(0).when(uiDomain).promptForSelection(PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE,
                    PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION, options);
            doReturn(owner).when(configurationManagementService).getCCZOwner(element);
            doReturn(true).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService , stereotype,owner);
            doReturn(Boolean.TRUE).when(apiDomain).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
            doReturn(id).when(uiDomain).askForInput("Please enter the Configured Element ID");
            doReturn(null).when(configurationManagementService).configureElement(element, id, stereotype);

            configureAction.actionPerformed(actionEvent);

            verify(uiDomain).logErrorAndShowMessage(logger, ExceptionConstants.ERROR_WHILE_CONFIGURING_ELEMENT,
                    "Configure Element Failure");
            verify(apiDomain, never()).setCurrentProjectHardDirty();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_configuredElementValidCustomIdButExceptionDuringConfigure() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype2 = mock(Stereotype.class);
        stereotypes.add(stereotype);
        stereotypes.add(stereotype2);
        String name = "name";
        String name2 = "name2";
        Object[] options = new Object[] {name, name2};
        ConfiguredElement owner = mock(ConfiguredElement.class);
        String id = "id";
        Exception exception = mock(NullPointerException.class);

        try {
            doReturn(element).when(configureAction).getElement();
            doReturn(stereotypes).when(configurationManagementService).getApplicableStereotypes(element);
            doReturn(name).when(stereotype).getName();
            doReturn(name2).when(stereotype2).getName();
            doReturn(0).when(uiDomain).promptForSelection(PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE,
                    PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION, options);
            doReturn(owner).when(configurationManagementService).getCCZOwner(element);
            doReturn(true).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService , stereotype,owner);
            doReturn(Boolean.TRUE).when(apiDomain).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
            doReturn(id).when(uiDomain).askForInput("Please enter the Configured Element ID");
            doThrow(exception).when(configurationManagementService).configureElement(element, id, stereotype);

            configureAction.actionPerformed(actionEvent);

            verify(uiDomain).logErrorAndShowMessage(logger, ExceptionConstants.ERROR_WHILE_CONFIGURING_ELEMENT,
                    "Configure Element Failure");
            verify(apiDomain, never()).setCurrentProjectHardDirty();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void actionPerformed_configuredElementConstructed() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype2 = mock(Stereotype.class);
        stereotypes.add(stereotype);
        stereotypes.add(stereotype2);
        String name = "name";
        String name2 = "name2";
        Object[] options = new Object[] {name, name2};
        ConfiguredElement owner = mock(ConfiguredElement.class);
        String id = "id";
        String error = "error";
        String expected = String.format(PluginConstant.AN_ERROR_OCCURRED, error);

        try {
            doReturn(element).when(configureAction).getElement();
            doReturn(stereotypes).when(configurationManagementService).getApplicableStereotypes(element);
            doReturn(name).when(stereotype).getName();
            doReturn(name2).when(stereotype2).getName();
            doReturn(0).when(uiDomain).promptForSelection(PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE,
                    PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION, options);
            doReturn(owner).when(configurationManagementService).getCCZOwner(element);
            doReturn(true).when(configuredElementDomain).checkConfiguredElementPermissions(configurationManagementService , stereotype,owner);
            doReturn(Boolean.FALSE).when(apiDomain).getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
            doReturn(id).when(element).getLocalID();
            doReturn(configuredElement).when(configurationManagementService).configureElement(element, id, stereotype);

            configureAction.actionPerformed(actionEvent);

            verify(apiDomain).setCurrentProjectHardDirty();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void updateState() {
        doReturn(element).when(configureAction).getElement();
        doReturn(true).when(configuredElementDomain).canBeConfigured(element, configurationManagementService);
        doNothing().when(configureAction).setEnabled(true);

        configureAction.updateState();

        verify(configureAction).setEnabled(true);
    }

    @Test
    public void updateState_nullElement() {
        doReturn(null).when(configureAction).getElement();
        doNothing().when(configureAction).setEnabled(false);

        configureAction.updateState();

        verify(configureAction, never()).getConfiguredElementDomain();
    }
}
