package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.LifecycleObjectDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;

public class ReviseAction extends DefaultBrowserAction {
    private static final Logger logger = LoggerFactory.getLogger(ReviseAction.class);
    private final transient ConfigurationManagementService configurationManagementService;

    public ReviseAction(ConfigurationManagementService configurationManagementService) {
        super("REVISE_ACTION", "Revise the Configured Element", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected LifecycleObjectFactory getLifecycleObjectFactory() {
        return getConfigurationManagementService().getLifecycleObjectFactory();
    }

    protected LifecycleObjectDomain getLifecycleObjectDomain() {
        return getConfigurationManagementService().getLifecycleObjectDomain();
    }

    protected ConfiguredElementDomain getConfiguredElementDomain() {
        return getConfigurationManagementService().getConfiguredElementDomain();
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
        Object selectedObject = getSelectedObjectOverride();
        if(!(selectedObject instanceof Element)) {
            return;
        }

        Element element = (Element) selectedObject;
        ConfiguredElement configuredElement = getConfigurationManagementService().getConfiguredElement(element);
        if(configuredElement == null) {
            return;
        }

        // Checking if the user has the required permission
        String action = "revision";
        if(!getLifecycleObjectDomain().canUserPerformAction(getConfigurationManagementService(), configuredElement.getAppliedStereotype(), action)) {
            getUIDomain().showErrorMessage("Cannot revise element: Insufficient permissions.", String.format("Configured element %s failure", action));
            return;
        }

        // Checking if the status of the CCZ owner is compatible
        ConfiguredElement cczOwner = configuredElement.getCCZOwner();
        if(cczOwner != null) {
            int cczOwnerMaturityRating = cczOwner.getStatusMaturityRating();
            LifecycleStatus initialStatus = configuredElement.getInitialStatus();
            if(initialStatus == null) {
                getUIDomain().showErrorMessage("Cannot revise element: No initial status.", ExceptionConstants.LIFECYCLE_STATUS_ERROR_TITLE);
                return;
            }
            int initialMaturityRating = initialStatus.getMaturityRating();
            if (initialMaturityRating < cczOwnerMaturityRating) {
                String message = String.format("Cannot revise element due to the status of the CCZ owner: %s[%s]",
                        cczOwner.getQualifiedName(), cczOwner.getID());
                getUIDomain().showErrorMessage(message, ExceptionConstants.LIFECYCLE_STATUS_ERROR_TITLE);
                return;
            }
        }

        try {
            getConfigurationManagementService().setLifecycleStatusChanging(true);
            if(!configuredElement.revise()) {
                getUIDomain().logErrorAndShowMessage(getLogger(),
                        String.format(ExceptionConstants.ERROR_WHILE_REVISING_ELEMENT + ExceptionConstants.ERROR_DURING_SINGLE_REVISE_SUFFIX, configuredElement.getName()),
                        "Revising failure");
                return;
            }
        } catch(Exception e) {
            getUIDomain().logErrorAndShowMessage(getLogger(),
                    String.format(ExceptionConstants.ERROR_WHILE_REVISING_ELEMENT + ExceptionConstants.ERROR_DURING_SINGLE_REVISE_SUFFIX, configuredElement.getName()),
                    "Revising failure");
            return;
        } finally {
            getConfigurationManagementService().setLifecycleStatusChanging(false);
        }

        getConfigurationManagementService().getSelectedChangeRecord().addAffectedElement(configuredElement,
                String.format(PluginConstant.REVISING_ACTION, configuredElement.getRevision()));
        getApiDomain().setCurrentProjectHardDirty();
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        // this disables the contextual menu for non-element items in the tree such as "Project Usages"
        Object selectedObject = getSelectedObjectOverride();
        if (!(selectedObject instanceof Element)) {
            setEnabled(false);
            return;
        }
        Element element = (Element) selectedObject;

        // this disables the action when the element is not editable, not locked and not new
        if(!getApiDomain().isElementInEditableState(element)) {
            setEnabled(false);
            return;
        }

        // this disables the contextual menu when the CM profile is not present or when no change record is selected
        if (!getConfigurationManagementService().isCmActive()
                || !getConfigurationManagementService().isChangeRecordSelected()) {
            setEnabled(false);
            return;
        }

        ChangeRecord changeRecord = getConfigurationManagementService().getSelectedChangeRecord();
        // this disables the contextual menu when the selected CR is not at an expendable status
        if (changeRecord == null || (changeRecord.hasStatus() && !changeRecord.isExpandable())) {
            setEnabled(false);
            return;
        }

        // this disables the contextual menu when the element is not released or released but not committed
        ConfiguredElement configuredElement = getConfigurationManagementService().getConfiguredElement(element);
        if (configuredElement != null) {
            setEnabled(configuredElement.isReleased() && configuredElement.isCommitted());
        } else {
            setEnabled(false);
        }
    }

    public Object getSelectedObjectOverride() {
        return getSelectedObject(); // needed for unit tests
    }
}
