package org.openmbee.plugin.cfgmgmt.integration.jira.json;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class JiraIssueFieldsJson {
    @Expose
    private String description;
    @Expose
    private String summary;
    @Expose
    private JiraIssueStatusJson status;
    @Expose
    private JiraIssueCreatorJson creator;
    @Expose
    @SerializedName(value = PluginConstant.ISSUETYPE)
    private JiraIssueTypeJson issueType;

    public String getDescription() {
        return description == null ? "" : description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public JiraIssueStatusJson getStatus() {
        return status;
    }

    public void setStatus(JiraIssueStatusJson status) {
        this.status = status;
    }

    public JiraIssueCreatorJson getCreator() {
        return creator;
    }

    public void setCreator(JiraIssueCreatorJson creator) {
        this.creator = creator;
    }

    public JiraIssueTypeJson getIssueType() {
        return issueType;
    }

    public void setIssueType(JiraIssueTypeJson issueType) {
        this.issueType = issueType;
    }
}
