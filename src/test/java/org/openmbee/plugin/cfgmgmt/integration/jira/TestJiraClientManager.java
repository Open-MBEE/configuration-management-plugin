package org.openmbee.plugin.cfgmgmt.integration.jira;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class TestJiraClientManager {
    @Test
    public void basicOperations() {
        JiraClientManager jiraClientManager = spy(new JiraClientManager());
        JiraClient jiraClient = mock(JiraClient.class);
        String url = "url";
        String restPath = "restPath";
        String query = "query";
        JiraConnectionInfo jiraConnectionInfo = spy(new JiraConnectionInfo(url, restPath, query));

        jiraClientManager.setActiveJIRAConnectionInfo(jiraConnectionInfo);
        // active connection operations
        assertEquals(jiraConnectionInfo, jiraClientManager.getActiveJIRAConnectionInfo());
        assertTrue(jiraClientManager.hasJiraConnectionSettings());
        jiraClientManager.cleanJIRAConnectionInfo();
        assertFalse(jiraClientManager.hasJiraConnectionSettings());
        // client map operations
        jiraClientManager.putEntryIntoClientMap(jiraConnectionInfo, jiraClient);
        assertTrue(jiraClientManager.containsConnection(jiraConnectionInfo));
        assertEquals(jiraClient, jiraClientManager.getClientFromConnectionInfo(jiraConnectionInfo));
    }
}
