package org.openmbee.plugin.cfgmgmt.integration.threedx.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ThreeDxPreferredCredentialsJson {
    @Expose
    @SerializedName(value = "collabspace")
    private ThreeDxCredentialsCollabSpaceJson collabSpace;
    @Expose
    private ThreeDxCredentialsOrganizationJson organization;
    @Expose
    private ThreeDxCredentialsRoleJson role;

    public ThreeDxPreferredCredentialsJson(ThreeDxCredentialsCollabSpaceJson collabSpace, ThreeDxCredentialsOrganizationJson organization, ThreeDxCredentialsRoleJson role) {
        this.collabSpace = collabSpace;
        this.organization = organization;
        this.role = role;
    }

    public ThreeDxCredentialsCollabSpaceJson getCollabSpace() {
        return collabSpace;
    }

    public void setCollabSpace(ThreeDxCredentialsCollabSpaceJson collabSpace) {
        this.collabSpace = collabSpace;
    }

    public ThreeDxCredentialsOrganizationJson getOrganization() {
        return organization;
    }

    public void setOrganization(ThreeDxCredentialsOrganizationJson organization) {
        this.organization = organization;
    }

    public ThreeDxCredentialsRoleJson getRole() {
        return role;
    }

    public void setRole(ThreeDxCredentialsRoleJson role) {
        this.role = role;
    }
}
