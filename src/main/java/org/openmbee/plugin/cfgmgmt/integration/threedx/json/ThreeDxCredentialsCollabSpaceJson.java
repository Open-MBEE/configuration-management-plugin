package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.annotations.Expose;

public class ThreeDxCredentialsCollabSpaceJson {
    @Expose
    private String name;

    public ThreeDxCredentialsCollabSpaceJson(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
