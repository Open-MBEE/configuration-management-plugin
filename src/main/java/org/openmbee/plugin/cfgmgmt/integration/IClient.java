package org.openmbee.plugin.cfgmgmt.integration;

import com.teamdev.jxbrowser.cookie.Cookie;

import java.net.URISyntaxException;
import java.util.List;

public interface IClient {
    boolean setToken(String url, List<Cookie> cookies) throws URISyntaxException;

    boolean isLoggedIn();
}
