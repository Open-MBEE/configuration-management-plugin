package org.openmbee.plugin.cfgmgmt.integration.twc.json;

import java.util.List;

public class TwcRoleJson {
    private String roleID;
    private List<TwcRoleProtectedObjectJson> protectedObjects;

    public String getRoleID() {
        return roleID;
    }

    public void setRoleID(String roleID) {
        this.roleID = roleID;
    }

    public List<TwcRoleProtectedObjectJson> getProtectedObjects() {
        return protectedObjects;
    }

    public void setProtectedObjects(List<TwcRoleProtectedObjectJson> protectedObjects) {
        this.protectedObjects = protectedObjects;
    }
}
