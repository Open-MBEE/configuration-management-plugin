package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.annotations.Expose;

public class ThreeDxCredentialsOrganizationJson {
    @Expose
    private String name;

    public ThreeDxCredentialsOrganizationJson(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
