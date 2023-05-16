package org.openmbee.plugin.cfgmgmt.integration.twc;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.JsonConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.ModalRestHandler;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.TwcRevisionDifferenceJson;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.TwcRoleJson;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.element.*;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.element.TwcElementJson;
import com.google.gson.*;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import org.eclipse.emf.common.util.URI;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpCookie;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;

public class TeamworkCloudService {
    private UIDomain uiDomain;
    protected Map<String, TeamworkCloudConnectionInfo> connections = new HashMap<>();
    private Map<String, Map> userRoleMaps = new HashMap<>();
    private Map<String, List<String>> osmcUsers;
    private String username;
    protected GsonBuilder regularGsonBuilder;
    protected Gson onlyExposedFieldsGson;

    public String getUsername() {
        return this.username;
    }

    public Map<String, List<String>> getOsmcUsers() {
        return this.osmcUsers;
    }

    public void setUiDomain(UIDomain uiDomain) {
        this.uiDomain = uiDomain;
    }

    protected UIDomain getUiDomain() {
        return uiDomain;
    }

    protected TeamworkCloudConnectionInfo getCloudInfoFromServerLogin(ApiDomain apiDomain) {
        Project project = apiDomain.getCurrentProject();
        if (project == null) {
            return null;
        }
        return connections.get(project.getID());
    }

    protected TeamworkCloudConnectionInfo getServerInfo(ApiDomain apiDomain){
        return apiDomain.getServerInfo(); // used for unit testing purposes
    }

    protected void removeLoginDueToLogout(String key) {
        connections.remove(key);
    }

    protected void putEntryForConnections(String key, TeamworkCloudConnectionInfo value) {
        connections.put(key, value); // used for unit testing purposes
    }

    protected Map<String, Boolean> getUserRoleMap(String key) {
        return userRoleMaps.get(key); // used for unit testing purposes
    }

    protected void putUserRoleMap(String key, Map<String, Boolean> userRoleMap) {
        userRoleMaps.put(key, userRoleMap); // used for unit testing purposes
    }

    public TeamworkCloudConnectionInfo getConnectionInfo(ApiDomain apiDomain) {
        TeamworkCloudConnectionInfo storedConnectionInfo = getCloudInfoFromServerLogin(apiDomain);
        TeamworkCloudConnectionInfo currentConnectionInfo = getServerInfo(apiDomain);

        if (currentConnectionInfo == null) {
            return null; // if we can't verify the stored connection don't bother
        } else if (storedConnectionInfo != null && storedConnectionInfo.isConnectionSimilar(currentConnectionInfo)) {
            return storedConnectionInfo; // case where the stored connection is sane and has a token
        }

        // case where we need to make a new connection
        try {
            currentConnectionInfo.setToken(obtainAuthTokenWithStatusRunner(apiDomain));
            String user = loginToTwcAndGetUsername(currentConnectionInfo.getFormattedTwcUrl(), currentConnectionInfo.getToken());
            if (user == null || !user.equals(currentConnectionInfo.getUsername())) {
                getUiDomain().logError(ExceptionConstants.TWC_USERNAME_MISMATCH);
                return null;
            }
            putEntryForConnections(apiDomain.getCurrentProject().getID(), currentConnectionInfo);
            return currentConnectionInfo;
        } catch (Exception e) {
            getUiDomain().logError(ExceptionConstants.INDETERMINATE_TWC_ROLES);
            return null;
        }
    }

    protected String loginToTwcAndGetUsername(String url, String token) throws TWCIntegrationException {
        try {
            getModalRestHandler().loadUrlWithStatusRunner(url, HTTP_GET, LOGIN_ENDPOINT, null, null,
                null, setupTwcAuthHeaders(token), FETCH_TWC_USERNAME, true, 0);
            List<HttpCookie> cookies = getModalRestHandler().getCookies();
            for (HttpCookie cookie : cookies) {
                if (cookie.getName().equals(TWC_USER_COOKIE)) {
                    return cookie.getValue();
                }
            }
        } catch(Exception e) {
            getUiDomain().logError(ExceptionConstants.ERROR_LOGGING_INTO_TWC + "\n" + e.getMessage());
            throw new TWCIntegrationException(e.getMessage());
        }
        getUiDomain().logError(ExceptionConstants.UNABLE_TO_DETERMINE_USER);
        throw new TWCIntegrationException(ExceptionConstants.UNABLE_TO_DETERMINE_USER);
    }

    public void logout(ApiDomain apiDomain) {
        TeamworkCloudConnectionInfo connInfo = getCloudInfoFromServerLogin(apiDomain);
        if (connInfo != null && connInfo.getToken() != null) {
            logoutFromTwc(connInfo);
            removeLoginDueToLogout(apiDomain.getCurrentProject().getID());
        }
    }

    protected void logoutFromTwc(TeamworkCloudConnectionInfo connectionInfo) {
        try {
            getModalRestHandler().loadUrlWithStatusRunner(connectionInfo.getFormattedTwcUrl(), HTTP_GET, LOGOUT_ENDPOINT,
                null, null, null, setupTwcAuthHeaders(connectionInfo.getToken()), LOGOUT_FROM_TWC,
                true, 0);
        } catch (Exception e) {
            getUiDomain().logError(ExceptionConstants.ERROR_LOGGING_OUT_OF_TWC + "\n" + e.getMessage());
        }
    }

    protected String obtainAuthTokenWithStatusRunner(ApiDomain apiDomain) throws TWCIntegrationException {
        AtomicReference<TWCIntegrationException> exception = getAtomicReferenceForTwcException();
        AtomicReference<String> token = getAtomicReferenceForToken();
        runWithProgressStatus(setupRunnable(apiDomain, exception, token));
        if (exception.get() != null) {
            throw exception.get();
        }
        return token.get();
    }

    protected RunnableWithProgress setupRunnable(ApiDomain apiDomain, AtomicReference<TWCIntegrationException> exception, AtomicReference<String> token) {
        return progressStatus -> {
            try {
                token.set(obtainAuthToken(apiDomain));
            } catch (TWCIntegrationException e) {
                exception.set(e);
            }
        };
    }

    protected AtomicReference<TWCIntegrationException> getAtomicReferenceForTwcException() {
        return new AtomicReference<>(); // used for unit testing
    }

    protected AtomicReference<String> getAtomicReferenceForToken() {
        return new AtomicReference<>(); // used for unit testing
    }

    protected void runWithProgressStatus(RunnableWithProgress runnableWithProgress) {
        ProgressStatusRunner.runWithProgressStatus(runnableWithProgress, PluginConstant.FETCH_TWC_TOKEN, true, 0);
    }

    protected String obtainAuthToken(ApiDomain apiDomain) throws TWCIntegrationException {
        try {
            return apiDomain.getSecondaryAuthToken(MAGICDRAW);
        } catch (ConnectException e) {
            if (e.getMessage() != null) {
                getUiDomain().logError(ExceptionConstants.ERROR_WHILE_CONNECTING_TO_TWC + "\n" + e.getMessage());
                throw new TWCIntegrationException(e.getMessage());
            } else {
                getUiDomain().logError(ExceptionConstants.ERROR_WHILE_CONNECTING_TO_TWC);
                throw new TWCIntegrationException(e.getClass().getName());
            }
        }
    }

    protected JsonArray getOsmcAdminRoles() {
        Gson gson = getGsonBuilder().setPrettyPrinting().create();
        InputStream inputStream = getResourceAsStream(OSMC_ROLES_PATH);
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return gson.fromJson(bufferedReader, JsonArray.class);
    }

    public String getRoleId(String roleName) {
        JsonArray osmcAdminRoles = getOsmcAdminRoles();
        if (osmcAdminRoles == null) {
            return null;
        }
        Iterator<JsonElement> iterator = osmcAdminRoles.iterator();
        JsonObject jsonObject = (JsonObject) iterator.next();
        do {
            if (jsonObject == null) {
                return null;
            }
            if (jsonObject.get(ROLE_NAME).getAsString().equals(roleName)) {
                return jsonObject.get(ROLE_ID).getAsString();
            }
            jsonObject = (JsonObject) iterator.next();
        } while ( iterator.hasNext());
        return null;
    }

    private InputStream getResourceAsStream(String jsonInfo) {
        return TeamworkCloudService.class.getResourceAsStream(jsonInfo);
    }

    protected GsonBuilder getGsonBuilder() {
        if(regularGsonBuilder == null) {
            regularGsonBuilder = new GsonBuilder();
        }
        return regularGsonBuilder;
    }

    protected Gson getGsonWithoutExposeAnnotations() {
        if(onlyExposedFieldsGson == null) {
            onlyExposedFieldsGson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        }
        return onlyExposedFieldsGson;
    }

    public boolean hasRole(ApiDomain apiDomain, List<String> roles) throws TWCIntegrationException {
        TeamworkCloudConnectionInfo connInfo = getConnectionInfo(apiDomain);
        if (connInfo == null) {
            return false;
        }

        if (connInfo.getUsername() == null || connInfo.getUsername().isEmpty()) {
            getUiDomain().logError(ExceptionConstants.UNABLE_TO_DETERMINE_USER);
            throw new TWCIntegrationException(ExceptionConstants.UNABLE_TO_DETERMINE_USER);
        }

        if (getOsmcUsers() == null || !connInfo.getUsername().equals(getUsername())) {
            osmcUsers = getOsmcUsers(connInfo);
            username = connInfo.getUsername();
        }

        Map<String, Boolean> userRoleMap = getUserRoleMap((connInfo.getUsername() + AT_THE_RATE_CHARACTER + connInfo.getUrl()));
        if (userRoleMap == null) {
            userRoleMap = new HashMap<>();
            putUserRoleMap(connInfo.getUsername() + AT_THE_RATE_CHARACTER + connInfo.getUrl(), userRoleMap);
        }

        String projectURI = getCurrentProjectUri(apiDomain);
        if (projectURI == null) {
            getUiDomain().logError(ExceptionConstants.UNABLE_TO_FIND_PROJECT_URI);
            throw new TWCIntegrationException(ExceptionConstants.UNABLE_TO_FIND_PROJECT_URI);
        }

        String projectId = getProjectIdFromUri(projectURI);
        if (projectId == null) {
            return false;
        }

        for (String role : roles) {
            String roleId = getRoleId(role);
            if (roleId != null && getOsmcUsers().get(roleId) != null && getOsmcUsers().get(roleId).contains(projectId)) {
                return true;
            }
        }
        return false;
    }

    protected Map<String, List<String>> getOsmcUsers(TeamworkCloudConnectionInfo connInfo) throws TWCIntegrationException {
        Map<String, List<String>> userRoles = new HashMap<>();

        JsonElement json = useModalRestHandlerForOsmcRoles(connInfo);
        TwcRoleJson[] rolesJson = getGsonBuilder().create().fromJson(json, TwcRoleJson[].class);
        for (TwcRoleJson role : rolesJson) {
            List<String> resources = new ArrayList<>();
            role.getProtectedObjects().forEach(resource -> resources.add(resource.getId()));
            userRoles.put(role.getRoleID(), resources);
        }

        return userRoles;
    }

    public String getProjectIdFromUri(String projectURI) {
        Pattern pattern = Pattern.compile(PluginConstant.TWC_ID_PATTERN);
        Matcher matcher = pattern.matcher(projectURI);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(2);
    }

    public String getProjectIdFromCurrentUri(ApiDomain apiDomain) {
        String uri = apiDomain.getEsiUriFromCurrentProject();
        return uri != null ? getProjectIdFromUri(uri) : null;
    }

    public TwcRevisionDifferenceJson getRevisionDifference(ApiDomain apiDomain, String projectId, String sourceRevision,
            String targetRevision) throws TWCIntegrationException {
        TeamworkCloudConnectionInfo connInfo = getConnectionInfo(apiDomain);
        TwcRevisionDifferenceJson revisionDifferenceJson = null;
        if(connInfo != null && projectId != null && !projectId.isBlank() && sourceRevision != null &&
                !sourceRevision.isBlank() && sourceRevision.matches(INTEGER_PATTERN) && targetRevision != null &&
                !targetRevision.isBlank() && targetRevision.matches(INTEGER_PATTERN)) {
            JsonElement json = useModalRestHandlerForRevisionDifference(connInfo, projectId, sourceRevision, targetRevision);
            revisionDifferenceJson = getGsonBuilder().create().fromJson(json, TwcRevisionDifferenceJson.class);
        }
        return revisionDifferenceJson;
    }

    public List<TwcElementJson> getElementsAtRevision(ApiDomain apiDomain, String projectId, String revision, String elementIds) throws TWCIntegrationException {
        TeamworkCloudConnectionInfo connInfo = getConnectionInfo(apiDomain);
        if(connInfo != null && projectId != null && !projectId.isBlank() && revision != null && !revision.isBlank() &&
                revision.matches(INTEGER_PATTERN) && elementIds != null && !elementIds.isBlank()) {
            return deserializeBatchElementResults(useModalRestHandleForBatchElementsAtRevision(connInfo, projectId, revision, elementIds));
        }
        return List.of();
    }

    protected List<TwcElementJson> deserializeBatchElementResults(JsonElement json) {
        List<TwcElementJson> results = createTwcElementJsonList();
        if(json != null && json.isJsonObject()) {
            json.getAsJsonObject().entrySet().forEach(entry -> {
                JsonElement value = entry.getValue();
                if(value != null && value.isJsonObject()) {
                    JsonElement data = value.getAsJsonObject().get(JsonConstants.DATA);
                    if(data != null && data.isJsonArray()) {
                        populateBatchElementResults(results, data.getAsJsonArray());
                    }
                }
            });
        }
        return results;
    }

    protected List<TwcElementJson> createTwcElementJsonList() {
        return new ArrayList<>(); // enables unit testing
    }

    protected void populateBatchElementResults(List<TwcElementJson> results, JsonArray array) {
        for(JsonElement item : array) {
            if(isTwcElementJson(item)) {
                TwcElementJson elementJson = getGsonWithoutExposeAnnotations().fromJson(item, TwcElementJson.class);
                if(elementJson != null) {
                    results.add(elementJson);
                }
            }
        }
    }

    protected boolean isTwcElementJson(JsonElement item) {
         return item != null && item.isJsonObject() && item.getAsJsonObject().has(JsonConstants.ESI_DATA) &&
                (!item.getAsJsonObject().has(JsonConstants.KERML_OWNER) || item.getAsJsonObject().get(JsonConstants.KERML_OWNER).isJsonObject());
    }

    protected JsonElement useModalRestHandler(TeamworkCloudConnectionInfo connInfo, String method,
            Map<String, String> params, String description) throws TWCIntegrationException {
        try {
            return getModalRestHandler().loadUrlAsJsonWithStatusRunner(connInfo.getFormattedTwcUrl(), HTTP_GET, method,
                null, null, params, setupTwcAuthHeaders(connInfo.getToken()), description,
                true, 0);
        } catch (Exception e) {
            getUiDomain().logError(e.getMessage());
            throw new TWCIntegrationException(e.getMessage());
        }
    }

    protected JsonElement useModalRestHandlerForPost(TeamworkCloudConnectionInfo connInfo, String method,
            String contentType, byte[] data, Map<String, String> params, String description) throws TWCIntegrationException {
        try {
            return getModalRestHandler().loadUrlAsJsonWithStatusRunner(connInfo.getFormattedTwcUrl(), HTTP_POST, method,
                    contentType, data, params, setupTwcAuthHeaders(connInfo.getToken()), description,
                    true, 0);
        } catch (Exception e) {
            getUiDomain().logError(e.getMessage());
            throw new TWCIntegrationException(e.getMessage());
        }
    }

    protected JsonElement useModalRestHandlerForOsmcRoles(TeamworkCloudConnectionInfo connInfo) throws TWCIntegrationException {
        Map<String, String> params = new HashMap<>();
        params.put(RESOURCE_NAME, TRUE);
        params.put(FILTER, FALSE);

        return useModalRestHandler(connInfo, String.format(PluginConstant.OSMC_ROLES_URL_FROM_ADMIN,
                connInfo.getUsername()), params, CHECK_PERMISSIONS);
    }

    protected JsonElement useModalRestHandlerForRevisionDifference(TeamworkCloudConnectionInfo connInfo,
            String projectId, String sourceRevision, String targetRevision) throws TWCIntegrationException {
        Map<String, String> params = new HashMap<>();
        params.put(SOURCE_REVISION, sourceRevision);
        params.put(TARGET_REVISION, targetRevision);

        return useModalRestHandler(connInfo, String.format(OSMC_REVISION_DIFF, projectId), params, GETTING_REVISION_DIFF);
    }

    protected JsonElement useModalRestHandleForBatchElementsAtRevision(TeamworkCloudConnectionInfo connInfo,
            String projectId, String revision, String elementIds) throws TWCIntegrationException {
        return useModalRestHandlerForPost(connInfo, String.format(OSMC_BATCH_ELEMENTS_AT_REVISION, projectId, revision),
                PLAIN_TEXT_CONTENT_TYPE, elementIds.getBytes(StandardCharsets.UTF_8),
                null, GETTING_ELEMENT_BATCH_AT_REVISION);
    }

    protected Map<String, String> setupTwcAuthHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, TWC_AUTHORIZATION_TOKEN_PREFIX + token);
        return headers;
    }

    protected ModalRestHandler getModalRestHandler() {
        return new ModalRestHandler();
    }

    protected String getCurrentProjectUri(ApiDomain apiDomain) {
        Project project = apiDomain.getCurrentProject();
        if (project != null && project.getPrimaryProject() != null) {
            URI locationUri = project.getPrimaryProject().getLocationURI();
            if (locationUri != null) {
                return locationUri.toString();
            }
        }
        return null;
    }
}
