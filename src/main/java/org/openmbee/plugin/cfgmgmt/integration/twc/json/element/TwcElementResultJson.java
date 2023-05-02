package org.openmbee.plugin.cfgmgmt.integration.twc.json.element;

import com.google.gson.annotations.Expose;

public class TwcElementResultJson {
    private TwcUmlElementContainerJson twcUmlElementContainerJson;
    @Expose
    private TwcElementJson twcElementJson;

    public TwcUmlElementContainerJson getTwcUmlElementContainerJson() {
        return twcUmlElementContainerJson;
    }

    public void setTwcUmlElementContainerJson(TwcUmlElementContainerJson twcUmlElementContainerJson) {
        this.twcUmlElementContainerJson = twcUmlElementContainerJson;
    }

    public TwcElementJson getTwcElementJson() {
        return twcElementJson;
    }

    public void setTwcElementJson(TwcElementJson twcElementJson) {
        this.twcElementJson = twcElementJson;
    }
}
