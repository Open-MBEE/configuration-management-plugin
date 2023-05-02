package org.openmbee.plugin.cfgmgmt.integration.twc.json;

import com.google.gson.annotations.SerializedName;

public class TwcRoleProtectedObjectJson {
    private String protectedType;
    @SerializedName(value = "ID")
    private String id;
    private String containerId;

    public String getProtectedType() {
        return protectedType;
    }

    public void setProtectedType(String protectedType) {
        this.protectedType = protectedType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }
}
