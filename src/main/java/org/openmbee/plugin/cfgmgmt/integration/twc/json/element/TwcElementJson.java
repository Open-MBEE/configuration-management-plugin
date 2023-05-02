package org.openmbee.plugin.cfgmgmt.integration.twc.json.element;

import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TwcElementJson {
    @SerializedName(value = "@id")
    private String id;
    @SerializedName(value = "kerml:esiID")
    @Expose
    private String esiId;
    @SerializedName(value = "@type")
    @Expose
    private String type;
    @SerializedName(value = "kerml:esiData")
    @Expose
    private JsonElement esiData;
    @SerializedName(value = "kerml:name")
    @Expose
    private String name;
    @SerializedName(value = "kerml:nsURI")
    private String nsUri;
    @SerializedName(value = "kerml:ownedElement")
    @Expose
    private TwcIdJson[] ownedElement;
    @SerializedName(value = "kerml:owner")
    @Expose
    private TwcIdJson owner;
    @SerializedName(value = "kerml:resource")
    private String resource;
    @SerializedName(value = "kerml:revision")
    private String revision;
    @SerializedName(value = "kerml:modifiedTime")
    private String modifiedTime;
    @SerializedName(value = "@context")
    private JsonElement context;
    @SerializedName(value = "@base")
    private String base;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEsiId() {
        return esiId;
    }

    public void setEsiId(String esiId) {
        this.esiId = esiId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonElement getEsiData() {
        return esiData;
    }

    public void setEsiData(JsonElement esiData) {
        this.esiData = esiData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNsUri() {
        return nsUri;
    }

    public void setNsUri(String nsUri) {
        this.nsUri = nsUri;
    }

    public TwcIdJson[] getOwnedElement() {
        return ownedElement;
    }

    public void setOwnedElement(TwcIdJson[] ownedElement) {
        this.ownedElement = ownedElement;
    }

    public TwcIdJson getOwner() {
        return owner;
    }

    public void setOwner(TwcIdJson owner) {
        this.owner = owner;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(String modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public JsonElement getContext() {
        return context;
    }

    public void setContext(JsonElement context) {
        this.context = context;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }
}
