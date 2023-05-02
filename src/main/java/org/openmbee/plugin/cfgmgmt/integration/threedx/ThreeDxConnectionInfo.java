package org.openmbee.plugin.cfgmgmt.integration.threedx;

import org.openmbee.plugin.cfgmgmt.integration.IConnectionInfo;

import java.util.Objects;

public class ThreeDxConnectionInfo implements IConnectionInfo {
    private String pass3dsURL;
    private String space3dsURL;
    private String search3dsURL;
    private String changeActionQuery;

    public ThreeDxConnectionInfo(String pass3dsURL, String space3dsURL, String search3dsURL, String changeActionQuery) {
        this.pass3dsURL = pass3dsURL;
        this.space3dsURL = space3dsURL;
        this.search3dsURL = search3dsURL;
        this.changeActionQuery = changeActionQuery;
    }

    @Override
    public String getUrl() {
        return getPass3dsURL();
    }

    public String getPass3dsURL() {
        return pass3dsURL;
    }

    public String getSpace3dsURL() {
        return space3dsURL;
    }

    public String getSearch3dsURL() {
        return search3dsURL;
    }

    public String getChangeActionQuery() {
        return changeActionQuery;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ThreeDxConnectionInfo that = (ThreeDxConnectionInfo) object;
        return pass3dsURL.equals(that.pass3dsURL) &&
            space3dsURL.equals(that.space3dsURL) &&
            search3dsURL.equals(that.search3dsURL) &&
            changeActionQuery.equals(that.changeActionQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
    }

    public void setInfo(String pass3dsURL, String space3dsURL, String search3dsURL, String changeActionQuery) {
        this.pass3dsURL = pass3dsURL;
        this.space3dsURL = space3dsURL;
        this.search3dsURL = search3dsURL;
        this.changeActionQuery = changeActionQuery;
    }

    public boolean hasInfo() {
        return pass3dsURL != null && !pass3dsURL.isEmpty() && space3dsURL != null && !space3dsURL.isEmpty() &&
            search3dsURL != null && !search3dsURL.isEmpty() && changeActionQuery != null && !changeActionQuery.isEmpty();
    }

    public void clear() {
        pass3dsURL = null;
        space3dsURL = null;
        search3dsURL = null;
        changeActionQuery = null;
    }
}
