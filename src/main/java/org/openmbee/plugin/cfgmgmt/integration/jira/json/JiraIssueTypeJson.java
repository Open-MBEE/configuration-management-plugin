package org.openmbee.plugin.cfgmgmt.integration.jira.json;

import com.google.gson.annotations.Expose;

public class JiraIssueTypeJson {
    @Expose
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
