package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.LifecycleObjectDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

public class ChangeStatusAction extends DefaultBrowserAction {
    private static final Logger logger = LoggerFactory.getLogger(ChangeStatusAction.class);
    private final transient ConfigurationManagementService configurationManagementService;

    public ChangeStatusAction(ConfigurationManagementService configurationManagementService) {
        super("CHANGE_STATUS_ACTION", "Change Status", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected LifecycleObjectFactory getLifecycleObjectFactory() {
        return configurationManagementService.getLifecycleObjectFactory();
    }

    protected LifecycleObjectDomain getLifecycleObjectDomain() {
        return configurationManagementService.getLifecycleObjectDomain();
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
    public void actionPerformed(ActionEvent evt) {
        LifecycleTransition transition;

        Object selected = getSelectedObjectOverride();
        if(!(selected instanceof Element)) {
            getUIDomain().logDebug(getLogger(), "No selected object for ChangeStatusAction action");
            return;
        }

        LifecycleObject lifecycleObject = attemptToGetConfiguredElement((Element) selected);
        if (lifecycleObject == null) {
            getUIDomain().logDebug(getLogger(), "No lifecycle object for ChangeStatusAction action");
            return;
        }

        List<LifecycleTransition> transitions = filterTransitions(lifecycleObject);
        if (transitions.isEmpty()) {
            getUIDomain().logError(getLogger(), "Transitions Not Present");
            return;
        }

        Object[] options = transitions.stream().map(LifecycleTransition::getName).toArray();
        int selection = getUIDomain().promptForSelection(PluginConstant.SELECT_TRANSITION_PROMPT_MESSAGE,
                PluginConstant.SELECT_TRANSITION_PROMPT_TITLE, options);
        if (selection == -1) {
            return;
        }
        transition = transitions.get(selection);


        LifecycleStatus newStatus = transition.getTargetStatus();
        ChangeRecord changeRecord = getConfigurationManagementService().getSelectedChangeRecord();
        if(lifecycleObject instanceof ConfiguredElement && changeRecord == null) {
            getUIDomain().logError(getLogger(), ExceptionConstants.ACTIVATE_CHANGE_RECORD_BEFORE_CHANGING_STATUS);
            return;
        }
        if(!lifecycleObject.canBePromoted(transition, changeRecord)) {
            getUIDomain().logErrorAndShowMessage(getLogger(), ExceptionConstants.PROMOTION_FAILURE_CHANGE_STATUS_ACTION, "Promotion failure");
            return;
        }

        try {
            getConfigurationManagementService().setLifecycleStatusChanging(true);
            lifecycleObject.changeStatus(newStatus, changeRecord);
        } catch(Exception e) {
            getUIDomain().logErrorAndShowMessage(getLogger(),
                    String.format(ExceptionConstants.ERROR_WHILE_CHANGING_STATUS_SINGLE_OBJECT, lifecycleObject.getName()),
                    "ChangeStatus failure");
            return;
        } finally {
            getConfigurationManagementService().setLifecycleStatusChanging(false);
        }

        getConfigurationManagementService().resetChangeRecordSelectionAfterStatusChange(lifecycleObject, newStatus);
        getApiDomain().setCurrentProjectHardDirty();
    }

    protected List<LifecycleTransition> filterTransitions(LifecycleObject lifecycleObject) {
        boolean autoRelease = getConfigurationManagementService().getAutomateReleaseSwitch();
        boolean isChangeRecordInstance = lifecycleObject instanceof ChangeRecord;
        List<LifecycleTransition> transitions = lifecycleObject.getTransitions();
        return transitions.stream().filter(tr -> isChangeRecordInstance || !tr.isReleasingTransition() || !autoRelease)
                .collect(Collectors.toList());
    }

    protected LifecycleObject attemptToGetConfiguredElement(Element element) {
        Stereotype ceStereotype = getConfigurationManagementService().getBaseCEStereotype();
        Stereotype crStereotype = getConfigurationManagementService().getBaseCRStereotype();

        if(ceStereotype != null && getApiDomain().hasStereotypeOrDerived(element, ceStereotype)) {
            return getLifecycleObjectFactory().getConfiguredElement(getConfigurationManagementService(), element);
        } else if(crStereotype != null && getApiDomain().hasStereotypeOrDerived(element, crStereotype)) {
            return getLifecycleObjectFactory().getChangeRecord(getConfigurationManagementService(), (Class) element);
        }
        return null;
    }

    protected Object getSelectedObjectOverride() {
        return getSelectedObject();
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        Object selected = getSelectedObjectOverride();
        if(!(selected instanceof Element)) {
            setEnabled(false);
            getUIDomain().logDebug(getLogger(), ExceptionConstants.CHANGE_STATUS_ACTION_UPDATE_FAILURE);
        } else {
            // this disables the contextual menu for non-element items in the tree such as "Project Usages"
            setEnabled(getLifecycleObjectDomain().canBePromoted(getConfigurationManagementService(), selected));
        }
    }
}
