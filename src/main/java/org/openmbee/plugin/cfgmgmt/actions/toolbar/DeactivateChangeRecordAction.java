package org.openmbee.plugin.cfgmgmt.actions.toolbar;

import org.openmbee.plugin.cfgmgmt.IConfigurationManagementPlugin;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.actions.MDAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;

public class DeactivateChangeRecordAction extends MDAction {
    private static final Logger logger = LoggerFactory.getLogger(DeactivateChangeRecordAction.class);
    private final transient IConfigurationManagementPlugin plugin;
    private final transient ConfigurationManagementService configurationManagementService;

    public DeactivateChangeRecordAction(IConfigurationManagementPlugin plugin,
            ConfigurationManagementService configurationManagementService) {
        super("DEACTIVATE_CHANGE_RECORD_ACTION", "Deactivate", null, null);
        this.plugin = plugin;
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        plugin.getSelectChangeRecordAction().resetSelections();
        plugin.getChangeRecordExpAction().clearState();
        plugin.getChangeRecordIWAction().clearState();
        plugin.getChangeRecordStatusAction().clearState();
        getConfigurationManagementService().setChangeRecordName(null);
    }

    @Override
    public void updateState() {
        setEnabled(configurationManagementService.isCmActive() &&
                configurationManagementService.getSelectedChangeRecord() != null);
    }
}
