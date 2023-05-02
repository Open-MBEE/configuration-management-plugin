package org.openmbee.plugin.cfgmgmt.domain;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.Lifecycle;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.utils.Policy;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;

import java.util.List;

public class LifecycleObjectDomain {
    private final LifecycleObjectFactory lifeCycleObjectFactory;
    private final ApiDomain apiDomain;
    private final UIDomain uiDomain;

    public LifecycleObjectDomain(LifecycleObjectFactory lifecycleObjectFactory, ApiDomain apiDomain, UIDomain uiDomain) {
        this.lifeCycleObjectFactory = lifecycleObjectFactory;
        this.apiDomain = apiDomain;
        this.uiDomain = uiDomain;
    }

    public LifecycleObjectFactory getLifeCycleObjectFactory() {
        return lifeCycleObjectFactory;
    }

    public ApiDomain getApiDomain() {
        return apiDomain;
    }

    public UIDomain getUIDomain() {
        return uiDomain;
    }

    public boolean canUserPerformAction(ConfigurationManagementService configurationManagementService, Stereotype stereo, String action) {
        Object o = getApiDomain().getDefaultValue(stereo, action + "Policy");
        if (o instanceof Class) {
            Policy policy = getPolicyFromClass(configurationManagementService, (Class) o);
            List<String> roles = policy.getRoles();
            return configurationManagementService.userHasPrivileges(roles);
        }
        return true;
    }

    protected Object getDefaultValue(Stereotype stereotype, String key) {
        return getApiDomain().getDefaultValue(stereotype, key);
    }

    protected Policy getPolicyFromClass(ConfigurationManagementService configurationManagementService, Class c) {
        return getLifeCycleObjectFactory().getPolicy(configurationManagementService, c);
    }

    public Lifecycle getLifecycle(ConfigurationManagementService configurationManagementService, Stereotype stereo) {
        Object o = getApiDomain().getDefaultValue(stereo, PluginConstant.LIFECYCLE);
        if (o instanceof StateMachine) {
            return getLifeCycleObjectFactory().getLifecycle(configurationManagementService, (StateMachine) o);
        } else {
            getUIDomain().logError(ExceptionConstants.THROW_CONFIGURATION_MANAGEMENT_EXCEPTION + ExceptionConstants.LIFECYCLE_PROPERTY_NOT_ON_STATE_MACHINE);
            return null;
        }
    }

    public LifecycleStatus getInitialStatus(ConfigurationManagementService configurationManagementService, Stereotype stereo) {
        Lifecycle lifecycle = getLifecycle(configurationManagementService, stereo);
        if (lifecycle == null) {
            return null;
        }

        return lifecycle.getInitialStatus();
    }

    public boolean canBePromoted(ConfigurationManagementService configurationManagementService, Object object) {
        if (!(object instanceof Element)) {
            getUIDomain().logDebug(ExceptionConstants.NOT_AN_ELEMENT_INSTANCE);
            return false;
        }

        Element element = (Element) object;

        // this disables the action when the element is not editable, not locked and not new
        if (!apiDomain.isElementInEditableState(element)) {
            getUIDomain().logDebug(ExceptionConstants.ELEMENT_CANNOT_BE_EDITED_LOCKED);
            return false;
        }

        if (apiDomain.hasStereotypeOrDerived(element, configurationManagementService.getBaseCEStereotype())) {
            ConfiguredElement configuredElement = getLifeCycleObjectFactory().getConfiguredElement(configurationManagementService, element);
            ChangeRecord releaseAuthority = configuredElement.getReleaseAuthority();
            ChangeRecord changeRecord = configurationManagementService.getSelectedChangeRecord();
            if(!configuredElement.hasAvailableTransitions()) {
                return false;
            } else if (releaseAuthority != null) {
                if(changeRecord != null && changeRecord.equals(releaseAuthority) && !changeRecord.isReleased() &&
                        configurationManagementService.getAutomateReleaseSwitch()) {
                    return true;
                }
                return !configurationManagementService.getAutomateReleaseSwitch() && !releaseAuthority.isReleased();
            }
            getUIDomain().logError(ExceptionConstants.CHANGERECORD_ERROR_MESSAGE);
        } else if (apiDomain.hasStereotypeOrDerived(element, configurationManagementService.getBaseCRStereotype())) {
            ChangeRecord changeRecord = getLifeCycleObjectFactory().getChangeRecord(configurationManagementService, (Class) element);
            return changeRecord != null && changeRecord.hasAvailableTransitions();
        } else {
            getUIDomain().logDebug(ExceptionConstants.CANNOT_BE_PROMOTED);
        }
        return false;
    }
}
