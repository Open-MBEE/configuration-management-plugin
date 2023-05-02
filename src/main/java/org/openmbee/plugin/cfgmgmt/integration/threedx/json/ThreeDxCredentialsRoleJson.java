package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.annotations.Expose;

public class ThreeDxCredentialsRoleJson {
    @Expose
    private String name;

    public ThreeDxCredentialsRoleJson(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
