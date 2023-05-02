package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.ui.IConfigurationManagementUI;
import org.openmbee.plugin.cfgmgmt.view.ElementHistoryRowView;

import java.util.List;

public class CmControllerSettings {
    private final String configuredElementLocalId;
    private final String configuredElementId;
    private final String configuredElementName;
    private final String title;
    private final List<ElementHistoryRowView> rowViews;
    private final List<ChangeRecord> relevantChangeRecords;
    private final ConfigurationManagementService configurationManagementService;
    private final IConfigurationManagementUI.ConfigurationManagementUiType sourceType;

    public CmControllerSettings(String configuredElementLocalId, String configuredElementId, String configuredElementName, String title,
            List<ElementHistoryRowView> rowViews, List<ChangeRecord> relevantChangeRecords,
            ConfigurationManagementService configurationManagementService,
            IConfigurationManagementUI.ConfigurationManagementUiType sourceType) {
        this.configuredElementLocalId = configuredElementLocalId;
        this.configuredElementId = configuredElementId;
        this.configuredElementName = configuredElementName;
        this.title = title;
        this.rowViews = rowViews;
        this.relevantChangeRecords = relevantChangeRecords;
        this.configurationManagementService = configurationManagementService;
        this.sourceType = sourceType;
    }

    public String getConfiguredElementLocalId() {
        return configuredElementLocalId;
    }

    public String getConfiguredElementId() {
        return configuredElementId;
    }

    public String getConfiguredElementName() {
        return configuredElementName;
    }

    public String getTitle() {
        return title;
    }

    public List<ElementHistoryRowView> getRowViews() {
        return rowViews;
    }

    public List<ChangeRecord> getRelevantChangeRecords() {
        return relevantChangeRecords;
    }

    public ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    public IConfigurationManagementUI.ConfigurationManagementUiType getSourceType() {
        return sourceType;
    }
}
