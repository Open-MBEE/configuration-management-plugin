package org.openmbee.plugin.cfgmgmt.utils;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import java.util.ArrayList;
import java.util.List;

public class Policy {
    private final ApiDomain apiDomain;
    private Class element;

    public Policy(ApiDomain apiDomain, Class element) {
        this.apiDomain = apiDomain;
        this.element = element;
    }

    protected ApiDomain getApiDomain() {
        return apiDomain;
    }

    public List<String> getRoles() {
        List<String> roles = getApiDomain().getStereotypePropertyValueAsString(element, PluginConstant.POLICY_STEREOTYPE, PluginConstant.ROLE);
        return roles != null ? roles : new ArrayList<>();
    }
}
