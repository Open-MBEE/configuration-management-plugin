package org.openmbee.plugin.cfgmgmt.managers;

import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.exception.NoLabelException;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.browser.TextAdornment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class TextAdornmentManager implements TextAdornment {

    private final ConfigurationManagementService configurationManagementService;

    public TextAdornmentManager(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    @Override
    public String adorn(String label, Element element) {
        if(label != null) {
            if(element != null) {
                try {
                    ConfiguredElement configuredElement = getConfigurationManagementService().getConfiguredElement(element);
                    if (configuredElement != null) {
                        return configuredElement.getDisplayName(label);
                    }
                } catch (Exception e) {
                    getUIDomain().logError(e.getMessage());
                }
            }
            return label;
        }
        throw new NoLabelException("Cannot adorn because a null label was provided."); // runtime issue at this point
    }
}
