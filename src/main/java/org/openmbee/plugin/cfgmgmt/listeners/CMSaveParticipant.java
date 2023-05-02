package org.openmbee.plugin.cfgmgmt.listeners;

import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.SaveParticipant;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CMSaveParticipant implements SaveParticipant {

    private final ConfigurationManagementService configurationManagementService;

    public CMSaveParticipant(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
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

    @Override
    public boolean isReadyForSave(Project project, ProjectDescriptor projectDescriptor) {
        return true;
    }

    @Override
    public void doBeforeSave(Project project, ProjectDescriptor projectDescriptor) {
        if (getConfigurationManagementService().isCmActive()) {
            Collection<Element> elements = getApiDomain().getExtendedElementsIncludingDerived(getConfigurationManagementService().getBaseCEStereotype());
            List<ConfiguredElement> configuredElements = elements.stream()
                    .map(el -> getConfigurationManagementService().getConfiguredElement(el))
                    .filter(el -> el != null && !el.isCommitted() && el.isReleased()).collect(Collectors.toList());
            configuredElements.forEach(el -> el.setIsCommitted(true));
        }
    }

    @Override
    public void doAfterSave(Project project, ProjectDescriptor projectDescriptor) {

    }
}
