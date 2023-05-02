package org.openmbee.plugin.cfgmgmt.integration.threedx;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.ModalRestHandler;
import org.openmbee.plugin.cfgmgmt.integration.threedx.json.ThreeDxCredentialsCollabSpaceJson;
import org.openmbee.plugin.cfgmgmt.integration.threedx.json.ThreeDxCredentialsOrganizationJson;
import org.openmbee.plugin.cfgmgmt.integration.threedx.json.ThreeDxCredentialsRoleJson;
import org.openmbee.plugin.cfgmgmt.integration.threedx.json.ThreeDxPreferredCredentialsJson;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.google.gson.JsonElement;
import com.teamdev.jxbrowser.cookie.Cookie;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpCookie;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestThreeDxClient {
    private ThreeDxClient threeDxClient;
    private String pass3dsUrl;
    private String space3dsUrl;
    private String search3dsUrl;
    private ModalRestHandler modalRestHandler;
    private ConfigurationManagementService configurationManagementService;
    private UIDomain uiDomain;
    private Cookie cookie;
    private HttpCookie httpCookie;

    @Before
    public void setup() {
        pass3dsUrl = "pass3dsUrl";
        space3dsUrl = "space3dsUrl";
        search3dsUrl = "search3dsUrl";
        modalRestHandler = mock(ModalRestHandler.class);
        configurationManagementService = mock(ConfigurationManagementService.class);
        uiDomain = mock(UIDomain.class);
        cookie = mock(Cookie.class);
        httpCookie = mock(HttpCookie.class);
        threeDxClient = spy(new ThreeDxClient(pass3dsUrl, space3dsUrl, search3dsUrl, configurationManagementService));

        doReturn(modalRestHandler).when(threeDxClient).getModalRestHandler();
        doReturn(uiDomain).when(threeDxClient).getUIDomain();
    }

    @Test
    public void setToken_urlMismatch() {
        String url = "url";

        try {
            assertFalse(threeDxClient.setToken(url, null));
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void setToken_exceptionAddingCookieToManager() {
        String url = pass3dsUrl;
        List<Cookie> cookies = new ArrayList<>();
        cookies.add(cookie);
        String domain = "domain";
        String path = "path";
        String input = "input";
        String reason = "reason";
        URISyntaxException uriSyntaxException = spy(new URISyntaxException(input, reason));

        try {
            doReturn(httpCookie).when(threeDxClient).createHttpCookie(cookie);
            doReturn(domain).when(cookie).domain();
            doReturn(path).when(cookie).path();
            doThrow(uriSyntaxException).when(threeDxClient).addCookieToManager(cookie, httpCookie);

            threeDxClient.setToken(url, cookies);
            verify(threeDxClient).addCookieToManager(cookie, httpCookie);
            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(reason + ": " + input, e.getMessage());
        }
    }

    @Test
    public void setToken_cookieAddedSuccessfully() {
        String url = pass3dsUrl;
        List<Cookie> cookies = new ArrayList<>();
        cookies.add(cookie);
        String domain = "domain";
        String path = "path";

        try {
            doReturn(httpCookie).when(threeDxClient).createHttpCookie(cookie);
            doReturn(domain).when(cookie).domain();
            doReturn(path).when(cookie).path();
            doNothing().when(threeDxClient).addCookieToManager(cookie, httpCookie);

            assertTrue(threeDxClient.setToken(url, cookies));
            verify(threeDxClient).addCookieToManager(cookie, httpCookie);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void loginToSearch_exceptionLoggingIn() {
        String searchUrl = search3dsUrl + "/federated/search?query=CA";
        String error = "error";
        ThreeDxIntegrationException integrationException = spy(new ThreeDxIntegrationException(error));

        try {
            when(threeDxClient.get3DSearchServer()).thenReturn(search3dsUrl);
            doThrow(integrationException).when(threeDxClient).handleThreeDxLogin(searchUrl, "Logging in to 3DSearch", "Issue logging in to 3DSearch");

            threeDxClient.loginToSearch();

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(error, e.getMessage());
        }
    }

    @Test
    public void loginToSearch_goodLogin() {
        String searchUrl = search3dsUrl + "/federated/search?query=CA";

        try {
            doReturn(search3dsUrl).when(threeDxClient).get3DSearchServer();
            doNothing().when(threeDxClient).handleThreeDxLogin(searchUrl, "Logging in to 3DSearch", "Issue logging in to 3DSearch");

            assertFalse(threeDxClient.isLoggedInToSearch());
            threeDxClient.loginToSearch();
            assertTrue(threeDxClient.isLoggedInToSearch());
        } catch (Exception e) {
            fail("Expected exception did not occur");
        }
    }

    @Test
    public void loginToSpace_exceptionLoggingIn() {
        String error = "error";
        ThreeDxIntegrationException integrationException = spy(new ThreeDxIntegrationException(error));

        try {
            when(threeDxClient.get3DSpaceServer()).thenReturn(space3dsUrl);
            doThrow(integrationException).when(threeDxClient).handleThreeDxLogin(space3dsUrl, "Logging in to 3DSpace", "Issue logging in to 3DSpace");

            threeDxClient.loginToSpace();

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(error, e.getMessage());
        }
    }

    @Test
    public void loginToSpace_goodLogin() {
        try {
            doReturn(space3dsUrl).when(threeDxClient).get3DSpaceServer();
            doNothing().when(threeDxClient).handleThreeDxLogin(space3dsUrl, "Logging in to 3DSpace", "Issue logging in to 3DSpace");

            assertFalse(threeDxClient.isLoggedInToSpace());
            threeDxClient.loginToSpace();
            assertTrue(threeDxClient.isLoggedInToSpace());
        } catch (Exception e) {
            fail("Expected exception did not occur");
        }
    }

    @Test
    public void handleThreeDxLogin_exceptionDuringRestCall() {
        String url = "url";
        String description = "description";
        Map<String, String> params = new HashMap<>();
        params.put("service", url);

        String error = "error";
        Exception exception = spy(new Exception(error));

        try {
            doReturn(null).when(threeDxClient).getSecurityContext();
            doNothing().when(threeDxClient).loginToSpace();
            when(threeDxClient.get3DPassportServer()).thenReturn(pass3dsUrl);
            doThrow(exception).when(modalRestHandler).loadUrlWithStatusRunner(pass3dsUrl, "GET", "login", null,
                null, params, null, description, true, 0);

            threeDxClient.handleThreeDxLogin(url, description, "");

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(error, e.getMessage());
        }
    }

    @Test
    public void handleThreeDxLogin_responseCodeNotOk() {
        String url = "url";
        String description = "description";
        String errorMessage = "errorMessage";
        Map<String, String> params = new HashMap<>();
        params.put("service", url);
        URL redirect = mock(URL.class);

        try {
            doReturn(pass3dsUrl).when(threeDxClient).get3DPassportServer();
            doReturn(new ArrayList<>()).when(modalRestHandler).loadUrlWithStatusRunner(pass3dsUrl, "GET", "login", null,
                null, params, null, description, true, 0);
            doReturn(redirect).when(modalRestHandler).getLastRedirectedUrl();
            doReturn(401).when(modalRestHandler).getResponseCode();

            threeDxClient.handleThreeDxLogin(url, description, errorMessage);

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    public void handleThreeDxLogin_nullRedirect() {
        String url = "url";
        String description = "description";
        String errorMessage = "errorMessage";
        Map<String, String> params = new HashMap<>();
        params.put("service", url);

        try {
            doReturn(pass3dsUrl).when(threeDxClient).get3DPassportServer();
            doReturn(new ArrayList<>()).when(modalRestHandler).loadUrlWithStatusRunner(pass3dsUrl, "GET", "login", null,
                null, params, null, description, true, 0);
            doReturn(null).when(modalRestHandler).getLastRedirectedUrl();
            doReturn(200).when(modalRestHandler).getResponseCode();

            threeDxClient.handleThreeDxLogin(url, description, errorMessage);

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    public void handleThreeDxLogin_noTicketInRedirectQuery() {
        String url = "url";
        String description = "description";
        String errorMessage = "errorMessage";
        Map<String, String> params = new HashMap<>();
        params.put("service", url);
        URL redirect = mock(URL.class);
        String queryUrl = "queryUrl";

        try {
            doReturn(pass3dsUrl).when(threeDxClient).get3DPassportServer();
            doReturn(new ArrayList<>()).when(modalRestHandler).loadUrlWithStatusRunner(pass3dsUrl, "GET", "login", null,
                null, params, null, description, true, 0);
            doReturn(redirect).when(modalRestHandler).getLastRedirectedUrl();
            doReturn(200).when(modalRestHandler).getResponseCode();
            doReturn(queryUrl).when(redirect).getQuery();

            threeDxClient.handleThreeDxLogin(url, description, errorMessage);

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    public void handleThreeDxLogin_redirectQueryNull() {
        String url = "url";
        String description = "description";
        String errorMessage = "errorMessage";
        Map<String, String> params = new HashMap<>();
        params.put("service", url);
        URL redirect = mock(URL.class);

        try {
            doReturn(pass3dsUrl).when(threeDxClient).get3DPassportServer();
            doReturn(new ArrayList<>()).when(modalRestHandler).loadUrlWithStatusRunner(pass3dsUrl, "GET", "login", null,
                null, params, null, description, true, 0);
            doReturn(redirect).when(modalRestHandler).getLastRedirectedUrl();
            doReturn(200).when(modalRestHandler).getResponseCode();
            doReturn(null).when(redirect).getQuery();

            threeDxClient.handleThreeDxLogin(url, description, errorMessage);

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    public void handleThreeDxLogin_ticketFoundInRedirectQuery() {
        String url = "url";
        String description = "description";
        String errorMessage = "errorMessage";
        Map<String, String> params = new HashMap<>();
        params.put("service", url);
        URL redirect = mock(URL.class);
        String queryUrl = "queryUrl?ticket=something";

        try {
            doReturn(pass3dsUrl).when(threeDxClient).get3DPassportServer();
            doReturn(new ArrayList<>()).when(modalRestHandler).loadUrlWithStatusRunner(pass3dsUrl, "GET", "login", null,
                null, params, null, description, true, 0);
            doReturn(redirect).when(modalRestHandler).getLastRedirectedUrl();
            doReturn(200).when(modalRestHandler).getResponseCode();
            doReturn(queryUrl).when(redirect).getQuery();

            threeDxClient.handleThreeDxLogin(url, description, errorMessage);

            verify(redirect, times(2)).getQuery();
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getPreferredCredentials_securityContextExists() {
        String securityContext = "securityContext";

        try {
            threeDxClient.setSecurityContext(securityContext);

            assertEquals(securityContext, threeDxClient.getPreferredCredentials());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getPreferredCredentials_exceptionDuringRestCall() {
        Map<String, String> params = new HashMap<>();
        params.put("current", "true");
        params.put("select", "preferredcredentials");

        String error = "error";
        Exception exception = spy(new Exception(error));

        try {
            doReturn(null).when(threeDxClient).getSecurityContext();
            doNothing().when(threeDxClient).loginToSpace();
            doReturn(pass3dsUrl).when(threeDxClient).get3DPassportServer();
            doThrow(exception).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(space3dsUrl, "GET", "/resources/modeler/pno/person", null,
                null, params, null, "Fetching Preferred Security Context", true, 0);

            threeDxClient.getPreferredCredentials();

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(error, e.getMessage());
        }
    }

    @Test
    public void getPreferredCredentials_securityContextNotFound() {
        threeDxClient.setLoggedInToSpace(true);
        Map<String, String> params = new HashMap<>();
        params.put("current", "true");
        params.put("select", "preferredcredentials");
        JsonElement json = mock(JsonElement.class);

        try {
            doReturn(null).when(threeDxClient).getSecurityContext();
            doReturn(pass3dsUrl).when(threeDxClient).get3DPassportServer();
            doReturn(json).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(space3dsUrl, "GET", "/resources/modeler/pno/person", null,
                    null, params, null, "Fetching Preferred Security Context", true, 0);
            doReturn(null).when(threeDxClient).extractCredentials(json);


            threeDxClient.getPreferredCredentials();

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(ExceptionConstants.THREEDX_INVALID_CREDENTIALS, e.getMessage());
        }
    }

    @Test
    public void getPreferredCredentials_securityContextFound() {
        threeDxClient.setLoggedInToSpace(true);
        Map<String, String> params = new HashMap<>();
        params.put("current", "true");
        params.put("select", "preferredcredentials");
        JsonElement json = mock(JsonElement.class);
        ThreeDxPreferredCredentialsJson credentialsJson = mock(ThreeDxPreferredCredentialsJson.class);
        ThreeDxCredentialsRoleJson roleJson = mock(ThreeDxCredentialsRoleJson.class);
        String roleName = "roleName";
        ThreeDxCredentialsOrganizationJson organizationJson = mock(ThreeDxCredentialsOrganizationJson.class);
        String orgName = "orgName";
        ThreeDxCredentialsCollabSpaceJson collabSpaceJson = mock(ThreeDxCredentialsCollabSpaceJson.class);
        String collabName = "collabName";
        String formatted = String.format("%s.%s.%s", roleName, orgName, collabName);

        try {
            doReturn(null).when(threeDxClient).getSecurityContext();
            doReturn(pass3dsUrl).when(threeDxClient).get3DPassportServer();
            doReturn(json).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(space3dsUrl, "GET", "/resources/modeler/pno/person", null,
                null, params, null, "Fetching Preferred Security Context", true, 0);
            doReturn(credentialsJson).when(threeDxClient).extractCredentials(json);
            doReturn(roleJson).when(credentialsJson).getRole();
            doReturn(roleName).when(roleJson).getName();
            doReturn(organizationJson).when(credentialsJson).getOrganization();
            doReturn(orgName).when(organizationJson).getName();
            doReturn(collabSpaceJson).when(credentialsJson).getCollabSpace();
            doReturn(collabName).when(collabSpaceJson).getName();

            assertEquals(formatted, threeDxClient.getPreferredCredentials());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void logout_exceptionDuringRestCall() {
        String error = "error";
        Exception exception = spy(new Exception(error));
        try {
            doNothing().when(threeDxClient).setSecurityContext(null);
            doThrow(exception).when(modalRestHandler).loadUrlWithStatusRunner(pass3dsUrl, "GET", "/logout",
                null, null, null, null, "Logging out", false, 0);

            threeDxClient.logout();

            fail("Expected exception did not occur");
        } catch (Exception exec) {
            assertTrue(exec instanceof ThreeDxIntegrationException);
            assertEquals(error, exec.getMessage());
        }
    }

    @Test
    public void logout_restCall() {
        List<Byte> byteList = new ArrayList<>();
        try {
            doReturn(byteList).when(modalRestHandler).loadUrlWithStatusRunner(pass3dsUrl, "GET", "/logout",
                null, null, null, null, "Logging out", false, 0);
            threeDxClient.logout();

            verify(threeDxClient).setSecurityContext(null);
            verify(modalRestHandler).loadUrlWithStatusRunner(pass3dsUrl, "GET", "/logout",
                null, null, null, null, "Logging out", false, 0);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void createHttpCookie() {
        doReturn("cookieValue").when(cookie).value();
        doReturn("cookieName").when(cookie).name();
        assertNotNull(threeDxClient.createHttpCookie(cookie));
    }
}
