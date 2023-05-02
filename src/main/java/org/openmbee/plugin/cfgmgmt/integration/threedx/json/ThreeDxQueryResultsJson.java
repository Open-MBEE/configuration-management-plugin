package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ThreeDxQueryResultsJson {
    @Expose
    private JsonElement error;
    @Expose
    @SerializedName(value = "error_description")
    private String errorDescription;
    @Expose
    private ThreeDxQueryResultsInfoJson infos;
    @Expose
    private List<ThreeDxQueryResultJson> results;

    public ThreeDxQueryResultsJson(JsonElement error, String errorDescription, ThreeDxQueryResultsInfoJson infos,
                                   List<ThreeDxQueryResultJson> results) {
        this.error = error;
        this.errorDescription = errorDescription;
        this.infos = infos;
        this.results = results;
    }

    public JsonElement getError() {
        return error;
    }

    public void setError(JsonElement error) {
        this.error = error;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public ThreeDxQueryResultsInfoJson getInfos() {
        return infos;
    }

    public void setInfos(ThreeDxQueryResultsInfoJson infos) {
        this.infos = infos;
    }

    public List<ThreeDxQueryResultJson> getResults() {
        return results;
    }

    public void setResults(List<ThreeDxQueryResultJson> results) {
        this.results = results;
    }
}
