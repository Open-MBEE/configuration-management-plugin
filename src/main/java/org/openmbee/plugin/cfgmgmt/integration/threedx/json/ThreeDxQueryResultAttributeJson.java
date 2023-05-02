package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.annotations.Expose;

public class ThreeDxQueryResultAttributeJson {
    @Expose
    private String name;
    @Expose
    private String value;

    public ThreeDxQueryResultAttributeJson(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
