package org.openmbee.plugin.cfgmgmt.integration.jira;

import org.openmbee.plugin.cfgmgmt.integration.IClient;
import com.google.common.base.Joiner;
import com.teamdev.jxbrowser.cookie.Cookie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JiraClient implements IClient {
    private String jiraURL;
    private String token = "";
    private boolean loggedIn = false;

    public JiraClient(String jiraURL) {
        this.jiraURL = jiraURL;
    }

    @Override
    public boolean setToken(String url, List<Cookie> cookies) {
        if (jiraURL.equals(url)) {
            long expireTime = System.currentTimeMillis();
            Map<String, String> filteredCookies = new HashMap<>();
            for(Cookie c : cookies) {
                if(c.expirationTime().toMillis() == 0 || c.expirationTime().toMillis() > expireTime) {
                    filteredCookies.put(c.name(), c.value()); // note: using for loop because a lambda collector fails
                }
            }

            token = processCookieToken(filteredCookies);
            if (!token.isEmpty()) {
                loggedIn = true;
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
    protected String processCookieToken(Map<String, String> filteredCookies) {
        if (!filteredCookies.isEmpty()) {
            return Joiner.on(";").withKeyValueSeparator("=").join(filteredCookies);
        } else {
            return "";
        }
    }
    public String getToken() { return token; }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }
}
