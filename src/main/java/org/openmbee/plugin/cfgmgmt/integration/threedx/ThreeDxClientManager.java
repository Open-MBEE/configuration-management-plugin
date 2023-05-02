package org.openmbee.plugin.cfgmgmt.integration.threedx;

import java.util.HashMap;
import java.util.Map;

public class ThreeDxClientManager {
    private Map<ThreeDxConnectionInfo, ThreeDxClient> clientMap;
    private ThreeDxConnectionInfo active3DxConnectionInfo;
    private boolean newRun;

    public ThreeDxClientManager() {
        clientMap = new HashMap<>();
        active3DxConnectionInfo = new ThreeDxConnectionInfo(null, null, null, null);
        newRun = false;
    }

    protected Map<ThreeDxConnectionInfo, ThreeDxClient> getClientMap() {
        return clientMap;
    }

    public ThreeDxConnectionInfo getActive3DxConnectionInfo() {
        return active3DxConnectionInfo;
    }

    protected void setActive3DxConnectionInfo(ThreeDxConnectionInfo active3DxConnectionInfo) {
        this.active3DxConnectionInfo = active3DxConnectionInfo; // unit test use only
    }

    public boolean has3DxConnectionSettings() {
        return active3DxConnectionInfo.hasInfo();
    }

    public void clean3DxConnectionInfo() {
        active3DxConnectionInfo.clear();
    }

    public boolean containsConnection(ThreeDxConnectionInfo key) {
        return clientMap.containsKey(key);
    }

    public ThreeDxClient getClientFromConnectionInfo(ThreeDxConnectionInfo key) {
        return clientMap.get(key);
    }

    public void putEntryIntoClientMap(ThreeDxConnectionInfo key, ThreeDxClient value) {
        clientMap.put(key, value);
    }

    public void setNewRun(boolean newRun) {
        this.newRun = newRun;
    }

    public boolean isNewRun() {
        return newRun;
    }
}
