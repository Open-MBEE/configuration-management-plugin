package org.openmbee.plugin.cfgmgmt.integration.threedx;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.IClient;
import org.openmbee.plugin.cfgmgmt.integration.ModalRestHandler;
import org.openmbee.plugin.cfgmgmt.integration.threedx.json.ThreeDxPreferredCredentialsJson;
import org.openmbee.plugin.cfgmgmt.integration.threedx.json.ThreeDxSecurityContextJson;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.teamdev.jxbrowser.cookie.Cookie;

import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThreeDxClient implements IClient {
    private String space3dsURL;// 3DSpace Server URL
    private String search3dsURL;// 3DSpace Server URL
    private String pass3dsURL;// 3DPassport Server URL
    private String securityContext = null;
    private String nextStart = null;
    private ModalRestHandler modalRestHandler;
    private ConfigurationManagementService configurationManagementService;

    private boolean loggedInToSpace = false;
    private boolean loggedInToSearch = false;
    private boolean loggedIn = false;

    public ThreeDxClient(String pass3dsURL, String space3dsURL, String search3dsURL, ConfigurationManagementService configurationManagementService) {
        this.space3dsURL = space3dsURL;
        this.pass3dsURL = pass3dsURL;
        this.search3dsURL = search3dsURL;
        this.configurationManagementService = configurationManagementService;
        modalRestHandler = new ModalRestHandler();
    }

    @Override
    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isLoggedInToSpace() {
        return loggedInToSpace;
    }

    protected void setLoggedInToSpace(boolean loggedInToSpace) {
        this.loggedInToSpace = loggedInToSpace; // intended for unit tests
    }

    public boolean isLoggedInToSearch() {
        return loggedInToSearch;
    }

    /**
     * getter
     *
     * @return String 3DSpace server URL
     */
    public String get3DPassportServer() {
        return pass3dsURL;
    }

    /**
     * getter
     *
     * @return String 3DSpace server URL
     */
    public String get3DSpaceServer() {
        return space3dsURL;
    }

    /**
     * getter
     *
     * @return String 3DSearch server URL
     */
    public String get3DSearchServer() {
        return search3dsURL;
    }

    protected UIDomain getUIDomain() {
        return configurationManagementService.getUIDomain();
    }

    protected ModalRestHandler getModalRestHandler() {
        return modalRestHandler;
    }

    protected String getSecurityContext() {
        return securityContext;
    }

    protected void setSecurityContext(String securityContext) {
        this.securityContext = securityContext; // used for unit testing only
    }

    public String getNextStart() {
        return nextStart;
    }

    public void setNextStart(String nextStart) {
        this.nextStart = nextStart;
    }

    @Override
    public boolean setToken(String url, List<Cookie> cookies) throws URISyntaxException {
        if (pass3dsURL.equals(url)) {
            for (Cookie cookie : cookies) {
                HttpCookie httpCookie = createHttpCookie(cookie);
                httpCookie.setDomain(cookie.domain());
                httpCookie.setPath(cookie.path());
                httpCookie.setHttpOnly(true);
                httpCookie.setSecure(true);
                httpCookie.setVersion(0);
                addCookieToManager(cookie, httpCookie);
            }
            loggedIn = true;
            loggedInToSpace = false;
            loggedInToSearch = false;
            return true;
        } else {
            return false;
        }
    }

    protected HttpCookie createHttpCookie(Cookie cookie) {
        return new HttpCookie(cookie.name(), cookie.value());
    }

    protected void addCookieToManager(Cookie cookie, HttpCookie httpCookie) throws URISyntaxException {
        ((CookieManager) CookieManager.getDefault()).getCookieStore().add(new URI(cookie.path()), httpCookie);
    }

    public void loginToSearch() throws ThreeDxIntegrationException {
        // LOGIN TO 3DSearch
        String searchURL = get3DSearchServer() + "/federated/search?query=CA";
        handleThreeDxLogin(searchURL, "Logging in to 3DSearch", "Issue logging in to 3DSearch");
        loggedInToSearch = true;
    }

    public void loginToSpace() throws ThreeDxIntegrationException {
        // LOGIN TO 3DSpace
        handleThreeDxLogin(get3DSpaceServer(), "Logging in to 3DSpace", "Issue logging in to 3DSpace");
        loggedInToSpace = true;
    }

    protected void handleThreeDxLogin(String serviceUrl, String description, String errorMessage) throws ThreeDxIntegrationException {
        Map<String, String> params = new HashMap<>();
        params.put("service", serviceUrl);

        try {
            getModalRestHandler().loadUrlWithStatusRunner(get3DPassportServer(), "GET", "login", null,
                null, params, null, description, true, 0);
        } catch (Exception e) {
            getUIDomain().logError(e.getMessage());
            throw new ThreeDxIntegrationException(e.getMessage());
        }

        // Check the successful login to the service:
        //     - The url of redirection is the input url service in junction with a service ticket.
        //     - The response (to the redirection) is 200.
        URL lastRedirectedUrl = getModalRestHandler().getLastRedirectedUrl();
        if (getModalRestHandler().getResponseCode() != 200 || lastRedirectedUrl == null ||
                lastRedirectedUrl.getQuery() == null || !lastRedirectedUrl.getQuery().contains("ticket=")) {
            throw new ThreeDxIntegrationException(errorMessage);
        }
    }

    public String getPreferredCredentials() throws ThreeDxIntegrationException {
        if (getSecurityContext() == null) {
            if (!loggedInToSpace) {
                loginToSpace();
            }

            // Load URL
            Map<String, String> params = new HashMap<>();
            params.put("current", "true");
            params.put("select", "preferredcredentials");

            JsonElement json;
            try {
                json = getModalRestHandler().loadUrlAsJsonWithStatusRunner(get3DSpaceServer(), "GET",
                        "/resources/modeler/pno/person", null, null, params, null,
                        "Fetching Preferred Security Context", true, 0);
            } catch (Exception e) {
                getUIDomain().logError(e.getMessage());
                throw new ThreeDxIntegrationException(e.getMessage());
            }

            ThreeDxPreferredCredentialsJson credentialsJson = extractCredentials(json);
            if(credentialsJson != null) {
                securityContext = String.format("%s.%s.%s", credentialsJson.getRole().getName(),
                        credentialsJson.getOrganization().getName(), credentialsJson.getCollabSpace().getName());
            } else {
                securityContext = null;
                throw new ThreeDxIntegrationException(ExceptionConstants.THREEDX_INVALID_CREDENTIALS);
            }
        }

        return securityContext;
    }

    protected ThreeDxPreferredCredentialsJson extractCredentials(JsonElement json) {
        ThreeDxSecurityContextJson securityContextJson = deserializeCredentialsJson(json);
        return securityContextJson != null && securityContextJson.getPreferredCredentials() != null ?
                securityContextJson.getPreferredCredentials() : null;
    }

    protected ThreeDxSecurityContextJson deserializeCredentialsJson(JsonElement json) {
        // used for unit testing
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(json, ThreeDxSecurityContextJson.class);
    }

    /**
     * log out from 3DPassport
     */
    public void logout() throws ThreeDxIntegrationException {
        // - Remove the SecurityContext
        setSecurityContext(null);

        try {
            getModalRestHandler().loadUrlWithStatusRunner(pass3dsURL, "GET", "/logout", null,
                    null, null, null, "Logging out", false, 0);
        } catch (Exception e) {
            getUIDomain().logError(e.getMessage());
            throw new ThreeDxIntegrationException(e.getMessage());
        }
    }
}
