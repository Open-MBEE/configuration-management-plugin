package org.openmbee.plugin.cfgmgmt.settings;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomSettings {

    private final Map<String, Object> customSettings = new HashMap<>();
    private final ConfigurationManagementService configurationManagementService;

    public CustomSettings(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
    }

    public ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    public Object get(String key) {
        return customSettings.get(key);
    }

    public void clear() {
        customSettings.clear();
    }

    public void setToDefault(Stereotype stereo) {
        String cmPackagePath = (String) getApiDomain().getDefaultValue(stereo, PluginConstant.CM_PACKAGE_PATH);
        boolean diagramAdornment = (boolean) getApiDomain().getDefaultValue(stereo, PluginConstant.CM_DIAGRAM_ADORNMENT);
        boolean enforceActiveCR = (boolean) getApiDomain().getDefaultValue(stereo, PluginConstant.ENFORCE_ACTIVE_CR);
        boolean automateRelease = (boolean) getApiDomain().getDefaultValue(stereo, PluginConstant.AUTOMATE_RELEASE);

        customSettings.clear();
        customSettings.put(PluginConstant.CM_DIAGRAM_ADORNMENT, diagramAdornment);
        customSettings.put(PluginConstant.CM_PACKAGE_PATH, cmPackagePath);
        customSettings.put(PluginConstant.ENFORCE_ACTIVE_CR, enforceActiveCR);
        customSettings.put(PluginConstant.AUTOMATE_RELEASE, automateRelease);
    }

    public void update() {
        // set the active connection info for the project: activeJIRAConnectionInfo
        //TODO: This line throws an exception during project migration (java.lang.UnsupportedOperationException: FreeObjectStore.load(UUID))
        Stereotype stereotype = getConfigurationManagementService().getCmcsStereotype();
        if(stereotype == null) {
            customSettings.clear();
            return;
        }

        List<Element> elements = getApiDomain().getStereotypedElements(stereotype);
        if (elements.isEmpty()) {
            setToDefault(stereotype);
            return;
        }

        Element element = elements.get(0);

        String elementName = "";
        if (element instanceof NamedElement) {
            elementName = ((NamedElement) element).getQualifiedName();
        }

        if (elements.size() > 1) {
            getConfigurationManagementService().getUIDomain().log(String.format(ExceptionConstants.MULTIPLE_CUSTOM_SETTINGS_FOUND_WARNING, elementName));
        }

        List<Object> diagramAdornments = getApiDomain().getStereotypePropertyValue(element, stereotype, PluginConstant.CM_DIAGRAM_ADORNMENT);
        List<Object> cmPackagePaths = getApiDomain().getStereotypePropertyValue(element, stereotype, PluginConstant.CM_PACKAGE_PATH);
        List<Object> enforceActiveCRs = getApiDomain().getStereotypePropertyValue(element, stereotype, PluginConstant.ENFORCE_ACTIVE_CR);
        List<Object> automateReleases = getApiDomain().getStereotypePropertyValue(element, stereotype, PluginConstant.AUTOMATE_RELEASE);
        List<Object> adminModePolicies = getApiDomain().getStereotypePropertyValue(element, stereotype, PluginConstant.ADMIN_MODE);

        boolean diagramAdornment = (boolean) (!diagramAdornments.isEmpty() ? diagramAdornments.get(0) :
                getApiDomain().getDefaultValue(stereotype, PluginConstant.CM_DIAGRAM_ADORNMENT));
        String cmPackagePath = (String) (!cmPackagePaths.isEmpty() ? cmPackagePaths.get(0) :
                getApiDomain().getDefaultValue(stereotype, PluginConstant.CM_PACKAGE_PATH));
        boolean enforceActiveCR = (boolean) (!enforceActiveCRs.isEmpty() ? enforceActiveCRs.get(0) :
                getApiDomain().getDefaultValue(stereotype, PluginConstant.ENFORCE_ACTIVE_CR));
        boolean automateRelease = (boolean) (!automateReleases.isEmpty() ? automateReleases.get(0) :
                getApiDomain().getDefaultValue(stereotype, PluginConstant.AUTOMATE_RELEASE));
        Class adminModePolicy = (Class) (!adminModePolicies.isEmpty() ? adminModePolicies.get(0) :
                getApiDomain().getDefaultValue(stereotype, PluginConstant.ADMIN_MODE));

        customSettings.clear();
        customSettings.put(PluginConstant.CM_DIAGRAM_ADORNMENT, diagramAdornment);
        customSettings.put(PluginConstant.CM_PACKAGE_PATH, cmPackagePath);
        customSettings.put(PluginConstant.ENFORCE_ACTIVE_CR, enforceActiveCR);
        customSettings.put(PluginConstant.AUTOMATE_RELEASE, automateRelease);
        customSettings.put(PluginConstant.ADMIN_MODE, adminModePolicy);
    }
}
