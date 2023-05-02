package org.openmbee.plugin.cfgmgmt.integration.twc.json.element;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class TwcIdJson {
    @SerializedName(value = "@id")
    @Expose
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        TwcIdJson that = (TwcIdJson) o;
        return Objects.equals(this.getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }
}
