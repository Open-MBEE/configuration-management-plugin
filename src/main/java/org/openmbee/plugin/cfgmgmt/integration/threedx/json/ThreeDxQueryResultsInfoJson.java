package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ThreeDxQueryResultsInfoJson {
    @Expose
    private int nresults;
    @Expose
    @SerializedName(value = "next_start")
    private String nextStart;

    public ThreeDxQueryResultsInfoJson(int nresults, String nextStart) {
        this.nresults = nresults;
        this.nextStart = nextStart;
    }

    public int getNresults() {
        return nresults;
    }

    public void setNresults(int nresults) {
        this.nresults = nresults;
    }

    public String getNextStart() {
        return nextStart;
    }

    public void setNextStart(String nextStart) {
        this.nextStart = nextStart;
    }
}
