package org.openmbee.plugin.cfgmgmt.integration.jira;

import org.openmbee.plugin.cfgmgmt.integration.IConnectionInfo;

import java.util.Objects;

public class JiraConnectionInfo implements IConnectionInfo {
    private String jiraURL;
    private String wssoURL;
    private String jiraRESTPath;
    private String issueQuery;

    public JiraConnectionInfo(String jiraURL, String wssoURL, String jiraRESTPath, String issueQuery) {
        this.jiraURL = jiraURL;
        this.wssoURL = wssoURL;
        this.jiraRESTPath = jiraRESTPath;
        this.issueQuery = issueQuery;
    }

    @Override
    public String getUrl() {
        return jiraURL;
    }

    public String getWssoURL() {
        return wssoURL;
    }

    public String getJiraRestPath() {
        return jiraRESTPath;
    }

    public String getIssueQuery() {
        return issueQuery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JiraConnectionInfo that = (JiraConnectionInfo) o;
        return Objects.equals(jiraURL, that.jiraURL) &&
                Objects.equals(wssoURL, that.wssoURL) &&
                Objects.equals(jiraRESTPath, that.jiraRESTPath) &&
                Objects.equals(issueQuery, that.issueQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jiraURL, wssoURL, jiraRESTPath, issueQuery);
    }

    public void setInfo(String jiraUrl, String wssoURL, String jiraRESTPath, String issueQuery) {
        this.jiraURL = jiraUrl;
        this.wssoURL = wssoURL;
        this.jiraRESTPath = jiraRESTPath;
        this.issueQuery = issueQuery;
    }

    public boolean hasInfo() {
        // wssoURL is optional
        return jiraURL != null && !jiraURL.isEmpty() && jiraRESTPath != null && !jiraRESTPath.isEmpty() && issueQuery != null && !issueQuery.isEmpty();
    }

    public void clear() {
        this.jiraURL = null;
        this.jiraRESTPath = null;
        this.issueQuery = null;
    }
}
