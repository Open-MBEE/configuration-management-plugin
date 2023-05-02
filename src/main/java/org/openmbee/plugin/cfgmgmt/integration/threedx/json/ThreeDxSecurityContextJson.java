package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ThreeDxSecurityContextJson {
    private String pid;
    private String name;
    @Expose
    @SerializedName(value = "preferredcredentials")
    private ThreeDxPreferredCredentialsJson preferredCredentials;

    public ThreeDxSecurityContextJson(String pid, String name, ThreeDxPreferredCredentialsJson preferredCredentials) {
        this.pid = pid;
        this.name = name;
        this.preferredCredentials = preferredCredentials;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ThreeDxPreferredCredentialsJson getPreferredCredentials() {
        return preferredCredentials;
    }

    public void setPreferredCredentials(ThreeDxPreferredCredentialsJson preferredCredentials) {
        this.preferredCredentials = preferredCredentials;
    }
}
