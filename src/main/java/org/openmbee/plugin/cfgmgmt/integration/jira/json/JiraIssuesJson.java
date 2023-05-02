package org.openmbee.plugin.cfgmgmt.integration.jira.json;

import com.google.gson.annotations.Expose;

import java.util.List;

public class JiraIssuesJson {
    @Expose
    private int total;
    @Expose
    private List<JiraIssueJson> issues;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<JiraIssueJson> getIssues() {
        return issues;
    }

    public void setIssues(List<JiraIssueJson> issues) {
        this.issues = issues;
    }
}
