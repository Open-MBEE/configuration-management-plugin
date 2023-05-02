package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ThreeDxQueryResultJson {
    @Expose
    private List<ThreeDxQueryResultAttributeJson> attributes;

    public ThreeDxQueryResultJson(List<ThreeDxQueryResultAttributeJson> attributes) {
        this.attributes = attributes;
    }

    public List<ThreeDxQueryResultAttributeJson> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ThreeDxQueryResultAttributeJson> attributes) {
        this.attributes = attributes;
    }
}
