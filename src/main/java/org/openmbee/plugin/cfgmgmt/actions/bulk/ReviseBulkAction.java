package org.openmbee.plugin.cfgmgmt.actions.bulk;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ReviseBulkAction implements Action {
    private static final Logger logger = LoggerFactory.getLogger(ReviseBulkAction.class);
    private boolean isEnabled;

    private final ConfigurationManagementService configurationManagementService;

    public ReviseBulkAction(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
        this.isEnabled = true;
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
    public void actionPerformed(ActionEvent e) {
        try {
            ElementSelectionDlg elementSelectionDlg = getUIDomain().createElementSelectionDialog(getConfigurationManagementService());
            elementSelectionDlg.setVisible(true);

            if (elementSelectionDlg.isOkClicked()) {
                List<BaseElement> selectedElements = elementSelectionDlg.getSelectedElements();

                for (BaseElement cameoEl : selectedElements) {
                    if(!reviseElement(getConfigurationManagementService().getConfiguredElement((NamedElement) cameoEl))) {
                        getUIDomain().logErrorAndShowMessage(getLogger(), ExceptionConstants.ERROR_DURING_BULK_REVISE, "Bulk Revise Failure");
                    }
                }
            }
        } catch (Exception ex) {
            getUIDomain().showErrorMessage(ex.getMessage(), ExceptionConstants.BULK_CONFIGURED_ELEMENT_REVISE_FAILURE);
        }
    }

    protected boolean reviseElement(ConfiguredElement element) {
        if(element == null || !element.canBeRevised()) {
            getUIDomain().logDebug(ExceptionConstants.BULK_CONFIGURED_ELEMENT_REVISE_FAILURE);
            return false;
        }

        try {
            getConfigurationManagementService().setLifecycleStatusChanging(true);
            if(!element.revise()) {
                getUIDomain().logError(getLogger(), String.format(ExceptionConstants.ERROR_WHILE_REVISING_ELEMENT, element.getName()));
                return false;
            }
        } catch(Exception e) {
            getUIDomain().logError(getLogger(), String.format(ExceptionConstants.ERROR_WHILE_REVISING_ELEMENT, element.getName()));
            return false;
        } finally {
            getConfigurationManagementService().setLifecycleStatusChanging(false);
        }

        configurationManagementService.getSelectedChangeRecord().addAffectedElement(element,
                String.format(PluginConstant.REVISING_ACTION, element.getRevision()));
        getApiDomain().setCurrentProjectHardDirty();
        return true;
    }
}
