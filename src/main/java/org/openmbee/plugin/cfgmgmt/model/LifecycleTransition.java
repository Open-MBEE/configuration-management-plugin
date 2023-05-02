package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Transition;

import java.util.ArrayList;
import java.util.List;

public class LifecycleTransition {
    private final ConfigurationManagementService configurationManagementService;
    private Transition transition;

    public LifecycleTransition(ConfigurationManagementService configurationManagementService, Transition transition) {
        this.configurationManagementService = configurationManagementService;
        this.transition = transition;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    public List<String> getRoles() {
        List<Object> policies = getApiDomain().getStereotypePropertyValue(transition,
            PluginConstant.LIFECYCLE_TRANSITION_STEREOTYPE, PluginConstant.POLICY);
        if (policies != null && !policies.isEmpty() && policies.get(0) instanceof Class) {
            List<String> roles = getApiDomain().getStereotypePropertyValueAsString((Class) policies.get(0),
                PluginConstant.POLICY_STEREOTYPE, PluginConstant.ROLE);
            return roles != null ? roles : new ArrayList<>();
        }

        return new ArrayList<>();
    }

    public boolean isReleasingTransition() {
        List<Object> statuses = getApiDomain().getStereotypePropertyValue(transition.getTarget(),
            PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.IS_RELEASED_STATUS);
        if (statuses != null && statuses.size() == 1 && statuses.get(0) instanceof Boolean) {
            return ((Boolean) statuses.get(0)).booleanValue();
        }
        return false;
    }

    public String getTargetStatusName() {
        if (transition != null && transition.getTarget() != null) {
            return transition.getTarget().getName();
        }
        return null;
    }

    public LifecycleStatus getTargetStatus() {
        return getConfigurationManagementService().getLifecycleObjectFactory().getLifecycleStatus(getConfigurationManagementService(), (State) transition.getTarget());
    }

    public String getName() {
        return transition.getName();
    }
}
