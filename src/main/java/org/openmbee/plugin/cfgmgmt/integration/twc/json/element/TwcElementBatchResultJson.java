package org.openmbee.plugin.cfgmgmt.integration.twc.json.element;

import com.google.gson.annotations.Expose;

public class TwcElementBatchResultJson {
    @Expose
    private TwcElementResultJson[] data;
    private Integer status;

    public TwcElementResultJson[] getData() {
        return data;
    }

    public void setData(TwcElementResultJson[] data) {
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
