package org.openmbee.plugin.cfgmgmt.actions.bulk;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ConfigureBulkAction implements Action {
    private static final Logger logger = LoggerFactory.getLogger(ConfigureBulkAction.class);
    private final ConfigurationManagementService configurationManagementService;
    private boolean isEnabled;

    public ConfigureBulkAction(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
        isEnabled = true;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    protected UIDomain getUiDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected ConfiguredElementDomain getConfiguredElementDomain() {
        return getConfigurationManagementService().getConfiguredElementDomain();
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public Object getValue(String key) {
        return null;
    }

    @Override
    public void putValue(String key, Object value) {
    }

    @Override
    public void setEnabled(boolean b) {
        isEnabled = b;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        try {
            ElementSelectionDlg elementSelectionDlg = getUiDomain().createElementSelectionDialog(getConfiguredElementDomain(),
                    getConfigurationManagementService());
            elementSelectionDlg.setVisible(true);

            if(elementSelectionDlg.isOkClicked()) {
                List<BaseElement> selectedElements = elementSelectionDlg.getSelectedElements();

                for (BaseElement cameoEl : selectedElements) {
                    if (!configureElement(cameoEl)) {
                        getUiDomain().logErrorAndShowMessage(getLogger(), ExceptionConstants.ERROR_DURING_BULK_CONFIGURE, "Bulk Configure Failure");
                        return; // early escape if a problem happens while configuring an element
                    }
                }
            }
        } catch (Exception e) {
            getUiDomain().showErrorMessage(String.format(PluginConstant.AN_ERROR_OCCURRED, e.getMessage()), "Configuring element failure");
        }
    }

    protected boolean configureElement(BaseElement cameoEl) {
        // getting the stereotype to apply
        List<Stereotype> applicableStereos = getConfigurationManagementService().getApplicableStereotypes((Element) cameoEl);

        Stereotype stereo = getConfigurationManagementService().userChoosesDesiredStereotype(applicableStereos, PluginConstant.SELECT_CONFIGURED_ELEMENT_TYPE,
                PluginConstant.CONFIGURED_ELEMENT_TYPE_SELECTION);
        if(stereo == null) {
            return false;
        }

        // Checking if the user has the required permission
        if(!getConfiguredElementDomain().checkConfiguredElementPermissions(
                getConfigurationManagementService(), stereo,
                getConfigurationManagementService().getCCZOwner((Element) cameoEl))) {
            getUiDomain().logDebug(String.format(ExceptionConstants.CONFIGURED_ELEMENT_PERMISSIONS_FAILURE, cameoEl.getHumanName()));
            return false;
        }


        // Getting the ID
        String id;
        if (getApiDomain().getDefaultValue(stereo, PluginConstant.USE_CUSTOM_IDS).equals(Boolean.TRUE)) {
            //TODO: The ID will eventually be pulled from SIDCOMS
            id = getUiDomain().askForInput(String.format(PluginConstant.PLEASE_ENTER_THE_CONFIGURED_ELEMENT_ID, ((NamedElement) cameoEl).getName()));
            if (id == null || id.isEmpty()) {
                return false;
            }
        } else {
            id = ((Element) cameoEl).getLocalID();
        }


        try {
            getConfigurationManagementService().setLifecycleStatusChanging(true);
            getConfigurationManagementService().configureElement((Element) cameoEl, id, stereo);
        } catch(Exception e) {
            getUiDomain().logError(getLogger(), ExceptionConstants.ERROR_WHILE_CONFIGURING_ELEMENT);
            return false;
        } finally {
            getConfigurationManagementService().setLifecycleStatusChanging(false);
        }
        getApiDomain().setCurrentProjectHardDirty();
        return true;
    }
}
