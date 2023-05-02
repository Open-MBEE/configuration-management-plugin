package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraClientManager;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;

import static org.mockito.Mockito.*;

public class TestJIRALoginAction {
    private JIRALoginAction jiraLoginAction;
    private ConfigurationManagementService configurationManagementService;
    private JiraClientManager jiraClientManager;
    private JiraService jiraService;
    private UIDomain uiDomain;
    private ActionEvent actionEvent;
    private Logger logger;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        jiraClientManager = mock(JiraClientManager.class);
        jiraService = mock(JiraService.class);
        jiraLoginAction = spy(new JIRALoginAction(configurationManagementService));
        uiDomain = mock(UIDomain.class);
        actionEvent = mock(ActionEvent.class);
        logger = mock(Logger.class);

        when(jiraLoginAction.getLogger()).thenReturn(logger);
        when(jiraLoginAction.getUIDomain()).thenReturn(uiDomain);
        when(jiraLoginAction.getJiraService()).thenReturn(jiraService);
        when(jiraService.getJiraClientManager()).thenReturn(jiraClientManager);
    }

    @Test
    public void actionPerformed_noActiveJiraConnectionInfo() {
        when(jiraClientManager.hasJiraConnectionSettings()).thenReturn(false);
        doNothing().when(uiDomain).showErrorMessage(PluginConstant.MISSING_JIRA_CONNECTION_SETTING, PluginConstant.JIRA_CONNECTION_SETTINGS_ERROR);

        jiraLoginAction.actionPerformed(actionEvent);

        verify(uiDomain).showErrorMessage(PluginConstant.MISSING_JIRA_CONNECTION_SETTING, PluginConstant.JIRA_CONNECTION_SETTINGS_ERROR);
    }

    @Test
    public void actionPerformed_jiraTokenAcquired() {
        when(jiraClientManager.hasJiraConnectionSettings()).thenReturn(true);

        jiraLoginAction.actionPerformed(actionEvent);

        verify(jiraService).acquireToken();
        verify(jiraLoginAction).acquireTokenFromJIRAUtils();
    }

    @Test
    public void updateStateTest() {
        doReturn(true).when(configurationManagementService).isCmActive();
        doNothing().when(jiraLoginAction).setEnabled(true);

        jiraLoginAction.updateState();

        verify(jiraLoginAction).setEnabled(true);
    }

    @Test
    public void updateStateTest_ifCmInActive() {
        doReturn(false).when(configurationManagementService).isCmActive();
        doNothing().when(jiraLoginAction).setEnabled(false);

        jiraLoginAction.updateState();

        verify(jiraLoginAction).setEnabled(false);
    }
}
