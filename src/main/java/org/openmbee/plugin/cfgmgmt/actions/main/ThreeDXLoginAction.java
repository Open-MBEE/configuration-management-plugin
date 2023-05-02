package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.actions.MDAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;

public class ThreeDXLoginAction extends MDAction {
    private static final Logger logger = LoggerFactory.getLogger(ThreeDXLoginAction.class);
    private final transient ConfigurationManagementService configurationManagementService;

    public ThreeDXLoginAction(ConfigurationManagementService configurationManagementService) {
        super(THREE_DX_LOGIN, LOGIN_TO_3DX, null, null);
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
        if (getConfigurationManagementService().getThreeDxService().getThreeDxClientManager().getActive3DxConnectionInfo() == null) {
            getUIDomain().showErrorMessage(PluginConstant.MISSING_3DX_CONNECTION_SETTING, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
            return;
        }

        getConfigurationManagementService().getThreeDxService().acquireToken();
    }

    @Override
    public void updateState() {
        setEnabled(configurationManagementService.isCmActive());
    }
}
