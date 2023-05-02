package org.openmbee.plugin.cfgmgmt.actions.bulk;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Optional;

import static org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants.GUI_LOG_MESSAGE;
import static org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants.TRANSITION_FAILURE;
import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.SELECT_TRANSITION_PROMPT_MESSAGE;
import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.SELECT_TRANSITION_PROMPT_TITLE;

public class ChangeStatusBulkAction implements Action {
    private static final Logger logger = LoggerFactory.getLogger(ChangeStatusBulkAction.class);
    private final ConfigurationManagementService configurationManagementService;
    List<ConfiguredElement> configuredElements;
    boolean isEnabled;

    public ChangeStatusBulkAction(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
        this.isEnabled = false;
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

    public void setEnabled(List<ConfiguredElement> configuredElements) {
        try {
            this.configuredElements = configuredElements;

            if (configuredElements.isEmpty()) {
                setEnabled(false);
                return;
            }

            ChangeRecord changeRecord = getConfigurationManagementService().getSelectedChangeRecord();
            if (changeRecord.isReleased()) {
                setEnabled(false);
                return;
            }

            LifecycleStatus status = null;
            String typeId = null;

            for (ConfiguredElement element : configuredElements) {
                // all selected configured elements need to be of same type and at the same status
                if (configuredElements.size() != 1) {
                    Optional<LifecycleStatus> elementStatus = element.getStatus();
                    if (status == null) {
                        if(elementStatus.isEmpty()) {
                            setEnabled(false);
                            return;
                        }
                        status = elementStatus.get();
                        typeId = element.getAppliedStereotype().getID();
                    } else if ((elementStatus.isPresent() && !status.equals(elementStatus.get())) ||
                            typeId != null && !typeId.equals(element.getAppliedStereotype().getID())) {
                        setEnabled(false);
                        return;
                    }
                }

                // this disables the action when the element is not locked
                if (!element.canBePromoted()) {
                    setEnabled(false);
                    return;
                }
            }

            setEnabled(true);
        } catch (Exception ex) {
            getUIDomain().logError(ex.getMessage());
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
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
            if (configuredElements.isEmpty()) {
                setEnabled(false);
                return;
            }

            List<LifecycleTransition> transitions = configuredElements.get(0).getTransitions();

            boolean autoRelease = getConfigurationManagementService().getAutomateReleaseSwitch();

            Object[] options = transitions.stream()
                    .filter(tr -> !tr.isReleasingTransition() || !autoRelease)
                    .map(LifecycleTransition::getName).toArray();
            int sel = getUIDomain().promptForSelection(SELECT_TRANSITION_PROMPT_MESSAGE, SELECT_TRANSITION_PROMPT_TITLE, options);
            if (sel == -1) {
                return;
            }

            LifecycleTransition transition = transitions.get(sel);
            LifecycleStatus newStatus = transition.getTargetStatus();
            ChangeRecord changeRecord = getConfigurationManagementService().getSelectedChangeRecord();

            for (LifecycleObject element : configuredElements) {
                if(!changeStatus(element, transition, changeRecord, newStatus)) {
                    getUIDomain().logErrorAndShowMessage(getLogger(), ExceptionConstants.ERROR_DURING_BULK_CHANGE_STATUS, "Bulk Change Status Failure");
                    return; // early escape if a problem happens while changing the status of an element
                }
            }
        } catch (Exception ex) {
            getUIDomain().showErrorMessage(String.format(GUI_LOG_MESSAGE, ex.getMessage()), TRANSITION_FAILURE);
        }
    }

    protected boolean changeStatus(LifecycleObject element, LifecycleTransition transition, ChangeRecord changeRecord,
                                   LifecycleStatus newStatus) {
        // validating maturity rating consistency and permissions
        if(!element.canBePromoted(transition, changeRecord)) {
            getUIDomain().logError(TRANSITION_FAILURE);
            return false;
        }

        try {
            getConfigurationManagementService().setLifecycleStatusChanging(true);
            // performing the status change
            element.changeStatus(newStatus, changeRecord);
        } catch(Exception e) {
            getUIDomain().logError(getLogger(), ExceptionConstants.ERROR_WHILE_CHANGING_STATUS);
            return false;
        } finally {
            getConfigurationManagementService().setLifecycleStatusChanging(false);
        }
        getApiDomain().setCurrentProjectHardDirty();
        return true;
    }
}
