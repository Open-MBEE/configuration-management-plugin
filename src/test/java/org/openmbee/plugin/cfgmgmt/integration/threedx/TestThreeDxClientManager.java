package org.openmbee.plugin.cfgmgmt.integration.threedx;

import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestThreeDxClientManager {

    private ThreeDxConnectionInfo threeDxConnectionInfo;
    private ThreeDxClient threeDxClient;
    private ThreeDxClientManager threeDxClientManager;
    private Map<ThreeDxConnectionInfo, ThreeDxClient> clientMap;

    @Before
    public void setup() {
        String pass3dsUrl = "pass3dsUrl";
        String space3dsUrl = "space3dsUrl";
        String search3dsUrl = "search3dsUrl";
        String query = "query";

        clientMap = new HashMap<>();
        threeDxClient = mock(ThreeDxClient.class);
        threeDxClientManager = spy(new ThreeDxClientManager());
        threeDxConnectionInfo = spy(new ThreeDxConnectionInfo(pass3dsUrl, space3dsUrl, search3dsUrl, query));
    }

    @Test
    public void getActive3DxConnectionInfoTest() {
        threeDxClientManager.setActive3DxConnectionInfo(threeDxConnectionInfo);

        assertNotNull(threeDxClientManager.getActive3DxConnectionInfo());
    }

    @Test
    public void containsConnectionTest() {
        threeDxClientManager.putEntryIntoClientMap(threeDxConnectionInfo, threeDxClient);

        assertTrue(threeDxClientManager.containsConnection(threeDxConnectionInfo));
    }

    @Test
    public void has3DxConnectionSettings_PresentTest() {
        when(threeDxConnectionInfo.hasInfo()).thenReturn(true);

        boolean has3DXSettings  =  threeDxClientManager.has3DxConnectionSettings();

        assertFalse(has3DXSettings);
    }

    @Test
    public void has3DxConnectionSettings_NotPresentTest() {
        when(threeDxConnectionInfo.hasInfo()).thenReturn(false);

        boolean has3DXSettings  =  threeDxClientManager.has3DxConnectionSettings();

        assertFalse(has3DXSettings);
    }

    @Test
    public void clean3DxConnectionInfoTest() {
        threeDxClientManager.clean3DxConnectionInfo();

        assertFalse(threeDxClientManager.has3DxConnectionSettings());
    }

    @Test
    public void getClientFromConnectionInfoTest() {
        threeDxClientManager.putEntryIntoClientMap(threeDxConnectionInfo, threeDxClient);

        when(threeDxClientManager.getClientMap()).thenReturn(clientMap);

        assertEquals(threeDxClient, threeDxClientManager.getClientFromConnectionInfo(threeDxConnectionInfo));
    }

    @Test
    public void isNewRun_OldRunTest() {
        threeDxClientManager.setNewRun(false);

        assertFalse(threeDxClientManager.isNewRun());
    }

    @Test
    public void isNewRun_Test() {
        threeDxClientManager.setNewRun(true);

        assertTrue(threeDxClientManager.isNewRun());
    }
}
