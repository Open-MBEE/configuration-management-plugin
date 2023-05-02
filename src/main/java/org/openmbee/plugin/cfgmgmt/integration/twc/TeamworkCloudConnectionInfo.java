package org.openmbee.plugin.cfgmgmt.integration.twc;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.integration.IConnectionInfo;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.TWC_URL_PATTERN;

public class TeamworkCloudConnectionInfo implements IConnectionInfo {
    private String url;
    private String username;
    private String token;
    private String protocol;

    public TeamworkCloudConnectionInfo(String url, String username, String protocol) {
        this.url = url;
        this.username = username;
        this.protocol = protocol;
        setupPortForUrl();
    }

    protected void setupPortForUrl() {
        if (url != null && !url.isBlank()) {
            if (url.contains(PluginConstant.COLON)) {
                url = url.substring(0, url.lastIndexOf(PluginConstant.COLON) + 1) + PluginConstant.PORT;
            } else {
                url = url + PluginConstant.COLON + PluginConstant.PORT;
            }
        }
    }

    public String getFormattedTwcUrl() {
        return String.format(TWC_URL_PATTERN, protocol, url);
    }

    public boolean isConnectionSimilar(TeamworkCloudConnectionInfo connectionInfo) {
        return getUrl() != null && connectionInfo.getUrl() != null && getUrl().equals(connectionInfo.getUrl()) &&
                getUsername() != null && connectionInfo.getUsername() != null && getUsername().equals(connectionInfo.getUsername()) &&
                getToken() != null && !getToken().isBlank() && getProtocol() != null && connectionInfo.getProtocol() != null &&
                getProtocol().equals(connectionInfo.getProtocol());
    }

    @Override
    public String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url; // intended for unit test use
    }

    public String getUsername() {
        return username;
    }

    protected void setUsername(String username) {
        this.username = username; // intended for unit test use
    }

    public String getToken() {
        return token;
    }

    protected void setToken(String token) {
        this.token = token;
    }

    public String getProtocol() {
        return protocol;
    }

    protected void setProtocol(String protocol) {
        this.protocol = protocol; // intended for unit test use
    }
}
