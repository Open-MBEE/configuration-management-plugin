package org.openmbee.plugin.cfgmgmt.integration.jira;

import com.teamdev.jxbrowser.cookie.Cookie;
import com.teamdev.jxbrowser.time.Timestamp;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestJiraClient {
    JiraClient jiraClient;
    String jiraURL;
    List<Cookie> cookies;
    String unMatchingURL;
    Cookie cookie_first;
    Cookie cookie_second;
    Logger logger;

    @Before
    public void setup() {
        jiraURL = "jiraURL";
        unMatchingURL = "unMatchingURL";
        cookie_first = mock(Cookie.class);
        cookie_second = mock(Cookie.class);
        cookies = new ArrayList<>();
        logger = mock(Logger.class);
        jiraClient = spy(new JiraClient(jiraURL));
    }

    @Test
    public void setToken_UnMatchedURL() {
        assertFalse(jiraClient.isLoggedIn());
        assertFalse(jiraClient.setToken(unMatchingURL, cookies));
        assertFalse(jiraClient.isLoggedIn());
    }

    @Test
    public void setToken_MatchedURL_ExpiredCookie() {
        cookies.add(cookie_first);
        cookies.add(cookie_second);

        String cookieOne = "ObSSOCookie";
        String cookieTwo = "WSSOLogonTime";

        Timestamp expiredTime = Timestamp.fromMillis(System.currentTimeMillis());

        doReturn(cookieOne).when(cookie_first).name();
        doReturn("value1").when(cookie_first).value();
        doReturn(expiredTime).when(cookie_first).expirationTime();
        doReturn(cookieTwo).when(cookie_second).name();
        doReturn("value2").when(cookie_second).value();
        doReturn(expiredTime).when(cookie_second).expirationTime();

        assertFalse(jiraClient.isLoggedIn());
        assertFalse(jiraClient.setToken(jiraURL, cookies));
        assertFalse(jiraClient.isLoggedIn());

        verify(cookie_first,times(2)).expirationTime();
        verify(cookie_second,times(2)).expirationTime();

    }

    @Test
    public void setToken_MatchedURL_UnexpiredCookie() {
        cookies.add(cookie_first);
        cookies.add(cookie_second);

        String cookieOne = "ObSSOCookie";
        String cookieTwo = "WSSOLogonTime";

        Timestamp unexpiredTime = Timestamp.fromMillis(System.currentTimeMillis() + 50000);

        doReturn(cookieOne).when(cookie_first).name();
        doReturn("value1").when(cookie_first).value();
        doReturn(unexpiredTime).when(cookie_first).expirationTime();
        doReturn(cookieTwo).when(cookie_second).name();
        doReturn("value2").when(cookie_second).value();
        doReturn(unexpiredTime).when(cookie_second).expirationTime();

        assertFalse(jiraClient.isLoggedIn());
        assertTrue(jiraClient.setToken(jiraURL, cookies));
        assertTrue(jiraClient.isLoggedIn());

        verify(cookie_first,times(2)).expirationTime();
        verify(cookie_second,times(2)).expirationTime();
    }

    @Test
    public void setToken_MatchedURL_ZeroCookie() {
        cookies.add(cookie_first);
        cookies.add(cookie_second);

        String cookieOne = "ObSSOCookie";
        String cookieTwo = "WSSOLogonTime";

        Timestamp zeroTime = Timestamp.fromMillis(0);

        doReturn(cookieOne).when(cookie_first).name();
        doReturn("value1").when(cookie_first).value();
        doReturn(zeroTime).when(cookie_first).expirationTime();
        doReturn(cookieTwo).when(cookie_second).name();
        doReturn("value2").when(cookie_second).value();
        doReturn(zeroTime).when(cookie_second).expirationTime();

        assertFalse(jiraClient.isLoggedIn());
        assertTrue(jiraClient.setToken(jiraURL, cookies));
        assertTrue(jiraClient.isLoggedIn());

        verify(cookie_first).expirationTime();
        verify(cookie_second).expirationTime();
    }

    @Test
    public void processCookieToken() {
        Map<String, String> filteredCookies = new HashMap<>();
        String cookie_first_name = "ObSSOCookie";
        String cookie_first_value = "ObSSOCookie_value";
        String cookie_second_name = "WSSOLogonTime";
        String cookie_second_value = "WSSOLogonTime_value";
        filteredCookies.put(cookie_first_name, cookie_first_value);
        filteredCookies.put(cookie_second_name, cookie_second_value);

        String expectedResult = "ObSSOCookie=ObSSOCookie_value;WSSOLogonTime=WSSOLogonTime_value";
        assertEquals(expectedResult, jiraClient.processCookieToken(filteredCookies));
    }

    @Test
    public void processCookieToken_ZeroSize() {
        assertEquals("", jiraClient.processCookieToken(new HashMap<>()));
    }
}
