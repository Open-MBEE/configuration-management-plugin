package org.openmbee.plugin.cfgmgmt.integration.twc.json.element;

import com.google.gson.annotations.SerializedName;

public class TwcUmlElementContainerJson {
    @SerializedName(value = "@context")
    private String context;
    @SerializedName(value = "@id")
    private String id;
    @SerializedName(value = "@type")
    private String[] type;
    @SerializedName(value = "ldp:contains")
    private TwcIdJson[] contains;
    @SerializedName(value = "ldp:hasMemberRelation")
    private String hasMemberRelation;
    @SerializedName(value = "ldp:membershipResource")
    private TwcIdJson membershipResource;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getType() {
        return type;
    }

    public void setType(String[] type) {
        this.type = type;
    }

    public TwcIdJson[] getContains() {
        return contains;
    }

    public void setContains(TwcIdJson[] contains) {
        this.contains = contains;
    }

    public String getHasMemberRelation() {
        return hasMemberRelation;
    }

    public void setHasMemberRelation(String hasMemberRelation) {
        this.hasMemberRelation = hasMemberRelation;
    }

    public TwcIdJson getMembershipResource() {
        return membershipResource;
    }

    public void setMembershipResource(TwcIdJson membershipResource) {
        this.membershipResource = membershipResource;
    }
}
