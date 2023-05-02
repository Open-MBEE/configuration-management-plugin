package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ConfigureAction extends DefaultBrowserAction {
    private static final Logger logger = LoggerFactory.getLogger(ConfigureAction.class);
    private final transient ConfigurationManagementService configurationManagementService;

    public ConfigureAction(ConfigurationManagementService configurationManagementService) {
        super("CONFIGURE_ACTION", "Configure Element", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected ConfiguredElementDomain getConfiguredElementDomain() {
        return getConfigurationManagementService().getConfiguredElementDomain();
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Element element = getElement();
        if(element == null) {
            return;
        }

        // getting the stereotype to apply
        List<Stereotype> applicableStereotypes = getConfigurationManagementService().getApplicableStereotypes(element);

        Stereotype stereotype;
        if (applicableStereotypes.isEmpty()) {
            getUIDomain().logDebug(getLogger(), "No applicable stereotypes available for ConfigureAction");
            return;
        } else if (applicableStereotypes.size() == 1) {
            stereotype = applicableStereotypes.get(0);
        } else {
            Object[] options = applicableStereotypes.stream().map(NamedElement::getName).toArray();
            int selection = getUIDomain().promptForSelection(PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE,
                    PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION, options);
            if (selection == JOptionPane.CLOSED_OPTION) {
                return;
            }
            stereotype = applicableStereotypes.get(selection);
        }

        // Checking if the user has the required permission
        if(!getConfiguredElementDomain().checkConfiguredElementPermissions(getConfigurationManagementService(),
                    stereotype, getConfigurationManagementService().getCCZOwner(element))) {
            getUIDomain().logErrorAndShowMessage(getLogger(),
                    String.format(ExceptionConstants.CONFIGURED_ELEMENT_PERMISSIONS_FAILURE, element.getHumanName()),
                    ExceptionConstants.CONFIGURED_ELEMENT_PERMISSIONS_FAILURE_TITLE);
            return;
        }

        // Getting the ID
        String id;
        Object object = getApiDomain().getDefaultValue(stereotype, PluginConstant.USE_CUSTOM_IDS);
        if (object != null && object.equals(Boolean.TRUE)) {
            //TODO: The ID will eventually be pulled from SIDCOMS
            id = getUIDomain().askForInput("Please enter the Configured Element ID");
            if (id == null || id.isEmpty()) {
                return;
            }
        } else {
            id = element.getLocalID();
        }

        ConfiguredElement configuredElement;
        try {
            getConfigurationManagementService().setLifecycleStatusChanging(true);
            configuredElement = getConfigurationManagementService().configureElement(element, id, stereotype);
        } catch(Exception e) {
            getUIDomain().logErrorAndShowMessage(getLogger(), ExceptionConstants.ERROR_WHILE_CONFIGURING_ELEMENT,
                    "Configure Element Failure");
            return;
        } finally {
            getConfigurationManagementService().setLifecycleStatusChanging(false);
        }

        if (configuredElement == null) {
            getUIDomain().logErrorAndShowMessage(getLogger(), ExceptionConstants.ERROR_WHILE_CONFIGURING_ELEMENT,
                    "Configure Element Failure");
            return;
        }
        getApiDomain().setCurrentProjectHardDirty();
    }

    protected Element getElement() {
        Object selected = getSelectedObject();
        if(selected instanceof Element) {
            return (Element) selected;
        }
        return null;
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        Element element = getElement();
        if(element != null) {
            setEnabled(getConfiguredElementDomain().canBeConfigured(getElement(), getConfigurationManagementService()));
        } else {
            setEnabled(false);
        }
    }
}
