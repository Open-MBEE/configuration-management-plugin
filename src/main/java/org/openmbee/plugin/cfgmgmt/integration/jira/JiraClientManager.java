package org.openmbee.plugin.cfgmgmt.integration.jira;

import java.util.HashMap;
import java.util.Map;

public class JiraClientManager {
    private Map<JiraConnectionInfo, JiraClient> clientMap;
    private JiraConnectionInfo activeJIRAConnectionInfo;

    public JiraClientManager() {
        clientMap = new HashMap<>();
        activeJIRAConnectionInfo = new JiraConnectionInfo(null, null, null);
    }

    public JiraConnectionInfo getActiveJIRAConnectionInfo() {
        return activeJIRAConnectionInfo;
    }

    protected void setActiveJIRAConnectionInfo(JiraConnectionInfo activeJIRAConnectionInfo) {
        this.activeJIRAConnectionInfo = activeJIRAConnectionInfo; // unit test use only
    }

    public boolean hasJiraConnectionSettings() {
        return activeJIRAConnectionInfo.hasInfo();
    }

    public void cleanJIRAConnectionInfo() {
        activeJIRAConnectionInfo.clear();
    }

    public boolean containsConnection(JiraConnectionInfo key) {
        return clientMap.containsKey(key);
    }

    public JiraClient getClientFromConnectionInfo(JiraConnectionInfo key) {
        return clientMap.get(key);
    }

    public void putEntryIntoClientMap(JiraConnectionInfo key, JiraClient value) {
        clientMap.put(key, value);
    }
}
