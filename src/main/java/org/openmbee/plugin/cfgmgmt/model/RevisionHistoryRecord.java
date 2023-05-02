package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class RevisionHistoryRecord {
    private final Logger logger = LoggerFactory.getLogger(RevisionHistoryRecord.class);
    private final ConfigurationManagementService configurationManagementService;
    protected Element element;
    protected Element configuredElement;
    protected Class releaseAuthority;
    protected String revision;
    protected Integer modelVersion;
    protected String creationDate;
    protected String releaseDate;
    private boolean interleavedWithAnotherRevision;

    public RevisionHistoryRecord(ConfigurationManagementService configurationManagementService, Element element) {
        this.configurationManagementService = configurationManagementService;
        this.element = element;
        this.interleavedWithAnotherRevision = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevisionHistoryRecord that = (RevisionHistoryRecord) o;
        return element.equals(that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    protected Logger getLogger() {
        return logger;
    }

    public ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    public Element getElement() {
        return element;
    }

    public String getName() {
        return ((Class) element).getName(); // is of type Class in profile
    }

    public ConfiguredElement getConfiguredElement() {
        if (configuredElement == null) {
            configuredElement = getConfigurationManagementService().getRevisionHistoryPropertyFirstValueGivenType(getElement(), PluginConstant.CONFIGURED_ELEMENT_PROPERTY_NAME, Element.class);
        }
        return configuredElement != null ? configurationManagementService.getConfiguredElement(configuredElement) : null;
    }

    public void setConfiguredElement(ConfiguredElement configuredElement) {
        this.configuredElement = configuredElement.getElement(); // only use for backwards compatibility
    }

    public ChangeRecord getRevisionReleaseAuthority() {
        if (releaseAuthority == null) {
            releaseAuthority = getConfigurationManagementService().getRevisionHistoryPropertyFirstValueGivenType(getElement(), PluginConstant.REVISION_RELEASE_AUTHORITY, Class.class);
        }
        return releaseAuthority != null ? configurationManagementService.getChangeRecord(releaseAuthority) : null;
    }

    public String getRevision() {
        if (revision == null) {
            revision = getConfigurationManagementService().getRevisionHistoryPropertyFirstValueAsString(getElement(), PluginConstant.REVISION);
        }
        return revision;
    }

    public Integer getModelVersion() {
        if (modelVersion == null) {
            modelVersion = getConfigurationManagementService().getRevisionHistoryPropertyFirstValueGivenType(getElement(), PluginConstant.MODEL_VERSION, Integer.class);
        }
        return modelVersion;
    }

    public String getCreationDate() {
        if (creationDate == null) {
            creationDate = getConfigurationManagementService().getRevisionHistoryPropertyFirstValueAsString(getElement(), PluginConstant.REVISION_CREATION_DATE);
        }
        return creationDate;
    }

    public String getReleaseDate() {
        if (releaseDate == null) {
            releaseDate = getConfigurationManagementService().getRevisionHistoryPropertyFirstValueAsString(getElement(), PluginConstant.REVISION_RELEASE_DATE);
        }
        return releaseDate;
    }

    public void setInterleavedWithAnotherRevision(boolean interleavedWithAnotherRevision) {
        this.interleavedWithAnotherRevision = interleavedWithAnotherRevision;
    }

    public boolean isInterleavedWithAnotherRevision() {
        return interleavedWithAnotherRevision;
    }
}
