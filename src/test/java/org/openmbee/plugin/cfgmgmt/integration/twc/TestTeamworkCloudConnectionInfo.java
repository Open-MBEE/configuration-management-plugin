package org.openmbee.plugin.cfgmgmt.integration.twc;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

public class TestTeamworkCloudConnectionInfo {
    private TeamworkCloudConnectionInfo teamworkCloudConnectionInfo;
    private String url;
    private String username;
    private String token;
    private String protocol;

    @Before
    public void setup() {
        url = "url:8443";
        username = "username";
        token = "token";
        protocol = PluginConstant.PROTOCOL_HTTPS;

        teamworkCloudConnectionInfo = spy(new TeamworkCloudConnectionInfo(url, username, protocol));
    }

    @Test
    public void setupPortForUrl_nullUrl() {
        teamworkCloudConnectionInfo.setUrl(null);

        teamworkCloudConnectionInfo.setupPortForUrl();

        assertNull(teamworkCloudConnectionInfo.getUrl());
    }

    @Test
    public void setupPortForUrl_blankUrl() {
        teamworkCloudConnectionInfo.setUrl(" ");

        teamworkCloudConnectionInfo.setupPortForUrl();

        assertTrue(teamworkCloudConnectionInfo.getUrl().isBlank());
    }

    @Test
    public void setupPortForUrl_urlWithoutColon() {
        String plainUrl = "url";
        String expected = plainUrl + PluginConstant.COLON + PluginConstant.PORT;
        teamworkCloudConnectionInfo.setUrl(plainUrl);

        teamworkCloudConnectionInfo.setupPortForUrl();

        assertEquals(expected, teamworkCloudConnectionInfo.getUrl());
    }

    @Test
    public void setupPortForUrl_urlWithColon() {
        String expected = url.substring(0, url.indexOf(PluginConstant.COLON) + 1) + PluginConstant.PORT;

        teamworkCloudConnectionInfo.setupPortForUrl();

        assertEquals(expected, teamworkCloudConnectionInfo.getUrl());
    }

    @Test
    public void getFormattedTwcUrl() {
        String expected = PluginConstant.PROTOCOL_HTTPS + "://" + teamworkCloudConnectionInfo.getUrl();

        assertEquals(expected, teamworkCloudConnectionInfo.getFormattedTwcUrl());
    }

    @Test
    public void isConnectionSimilar_firstUrlNull() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(null, null, null));

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_secondUrlNull() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(url, null, null));
        teamworkCloudConnectionInfo.setUrl(null);

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_urlsDifferent() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo("newUrl", null, null));

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_firstUsernameNull() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(url, null, null));

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_secondUsernameNull() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(url, username, null));
        teamworkCloudConnectionInfo.setUsername(null);

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_usernamesDifferent() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(url, "newUsername", null));

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_tokenNull() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(url, username, null));

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_tokenBlank() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(url, username, null));
        connectionInfo.setToken(" ");

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_firstProtocolNull() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(url, username, null));
        connectionInfo.setToken("token");

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_secondProtocolNull() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(url, username, PluginConstant.PROTOCOL_HTTPS));
        connectionInfo.setToken("token");
        teamworkCloudConnectionInfo.setProtocol(null);

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_protocolsDifferent() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(url, username, PluginConstant.PROTOCOL_HTTP));
        connectionInfo.setToken("token");

        assertFalse(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }

    @Test
    public void isConnectionSimilar_actuallySimilar() {
        TeamworkCloudConnectionInfo connectionInfo = spy(new TeamworkCloudConnectionInfo(url, username, PluginConstant.PROTOCOL_HTTPS));
        connectionInfo.setToken("token");

        assertTrue(connectionInfo.isConnectionSimilar(teamworkCloudConnectionInfo));
    }
}
