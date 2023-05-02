package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.actions.MDAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;

public class JIRALoginAction extends MDAction {
    private static final Logger logger = LoggerFactory.getLogger(JIRALoginAction.class);
    private final transient ConfigurationManagementService configurationManagementService;

    public JIRALoginAction(ConfigurationManagementService configurationManagementService) {
        super("JIRA_LOGIN", "Login to JIRA", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected JiraService getJiraService() {
        return getConfigurationManagementService().getJiraService();
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if (! getJiraService().getJiraClientManager().hasJiraConnectionSettings()) {
            getUIDomain().showErrorMessage(PluginConstant.MISSING_JIRA_CONNECTION_SETTING, PluginConstant.JIRA_CONNECTION_SETTINGS_ERROR);
            return;
        }

        acquireTokenFromJIRAUtils();
    }

    protected void acquireTokenFromJIRAUtils() {
        getJiraService().acquireToken();
    }

    @Override
    public void updateState() {
        setEnabled(getConfigurationManagementService().isCmActive());
    }
}
