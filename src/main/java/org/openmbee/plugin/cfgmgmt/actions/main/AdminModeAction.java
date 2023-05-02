package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.actions.MDAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;

public class AdminModeAction extends MDAction {
    private static final Logger logger = LoggerFactory.getLogger(AdminModeAction.class);
    private final transient ConfigurationManagementService configurationManagementService;

    protected static final String TURN_STATE_ON = "Turn admin mode on";
    protected static final String TURN_STATE_OFF = "Turn admin mode off";

    public AdminModeAction(ConfigurationManagementService configurationManagementService) {
        super("ADMIN_MODE_ACTION", TURN_STATE_ON, null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (getName().equals(TURN_STATE_ON)) {
            if(!getConfigurationManagementService().enableAdminMode()) {
                getUIDomain().logErrorAndShowMessage(getLogger(), "Cannot activate admin mode.",
                        "Admin mode activation failure");
                return;
            }

            setName(TURN_STATE_OFF);

            getUIDomain().showWarningMessage(
                    "The admin mode is now on. All CM protections are off.\nPROCEED WITH CAUTION!",
                    "Admin mode on");
        } else {
            getConfigurationManagementService().disableAdminMode();
            setName(TURN_STATE_ON);
        }
    }

    @Override
    public void updateState() {
        try {
            setEnabled(getConfigurationManagementService().isCmActive());
            setName(getConfigurationManagementService().getAdminMode() ? TURN_STATE_OFF : TURN_STATE_ON);
        } catch (Exception e) {
            getUIDomain().logError(getLogger(), "Error updating state for AdminModeAction", e);
        }
    }
}
