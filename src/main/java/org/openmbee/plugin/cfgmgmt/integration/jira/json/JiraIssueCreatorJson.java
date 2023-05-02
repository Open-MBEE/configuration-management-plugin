package org.openmbee.plugin.cfgmgmt.integration.jira.json;

import com.google.gson.annotations.Expose;

public class JiraIssueCreatorJson {
    @Expose
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
