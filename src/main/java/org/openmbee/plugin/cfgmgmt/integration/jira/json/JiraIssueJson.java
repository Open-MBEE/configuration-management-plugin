package org.openmbee.plugin.cfgmgmt.integration.jira.json;

import com.google.gson.annotations.Expose;

public class JiraIssueJson {
    @Expose
    private String id;
    @Expose
    private JiraIssueFieldsJson fields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JiraIssueFieldsJson getFields() {
        return fields;
    }

    public void setFields(JiraIssueFieldsJson fields) {
        this.fields = fields;
    }
}
