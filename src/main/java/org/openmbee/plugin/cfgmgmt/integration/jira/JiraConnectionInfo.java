package org.openmbee.plugin.cfgmgmt.integration.jira;

import org.openmbee.plugin.cfgmgmt.integration.IConnectionInfo;

import java.util.Objects;

public class JiraConnectionInfo implements IConnectionInfo {
    private String jiraURL;
    private String jiraRESTPath;
    private String issueQuery;

    public JiraConnectionInfo(String jiraURL, String jiraRESTPath, String issueQuery) {
        this.jiraURL = jiraURL;
        this.jiraRESTPath = jiraRESTPath;
        this.issueQuery = issueQuery;
    }

    @Override
    public String getUrl() {
        return jiraURL;
    }

    public String getJiraRestPath() {
        return jiraRESTPath;
    }

    public String getIssueQuery() {
        return issueQuery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JiraConnectionInfo that = (JiraConnectionInfo) o;
        return jiraURL.equals(that.jiraURL) &&
                jiraRESTPath.equals(that.jiraRESTPath) &&
                issueQuery.equals(that.issueQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jiraURL, issueQuery);
    }

    public void setInfo(String jiraUrl, String jiraRESTPath, String issueQuery) {
        this.jiraURL = jiraUrl;
        this.jiraRESTPath = jiraRESTPath;
        this.issueQuery = issueQuery;
    }

    public boolean hasInfo() {
        return jiraURL != null && !jiraURL.isEmpty() && jiraRESTPath != null && !jiraRESTPath.isEmpty() && issueQuery != null && !issueQuery.isEmpty();
    }

    public void clear() {
        this.jiraURL = null;
        this.jiraRESTPath = null;
        this.issueQuery = null;
    }
}
