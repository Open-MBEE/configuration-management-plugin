package org.openmbee.plugin.cfgmgmt.integration.jira;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.ModalRestHandler;
import org.openmbee.plugin.cfgmgmt.integration.WssoService;
import org.openmbee.plugin.cfgmgmt.integration.jira.json.*;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmbee.plugin.cfgmgmt.integration.jira.json.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestJiraService {
    private JiraService jiraService;
    private ConfigurationManagementService configurationManagementService;
    private JiraClientManager jiraClientManager;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private ModalRestHandler modalRestHandler;
    private WssoService wssoService;
    private Element element;
    private NamedElement namedElement;
    private Stereotype stereotype;
    private JiraConnectionInfo connectionInfo;
    private JiraClient client;
    private JiraIssueJson jiraIssueJson;
    private JiraIssuesJson jiraIssuesJson;
    private Map<JiraConnectionInfo, JiraClient> clientMap;

    @Before
    public void setup(){
        configurationManagementService = mock(ConfigurationManagementService.class);
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        modalRestHandler = mock(ModalRestHandler.class);
        wssoService = mock(WssoService.class);
        element = mock(Element.class);
        namedElement = mock(NamedElement.class);
        stereotype = mock(Stereotype.class);
        connectionInfo = mock(JiraConnectionInfo.class);
        client = mock(JiraClient.class);
        clientMap = new HashMap<>();
        jiraIssueJson = mock(JiraIssueJson.class);
        jiraIssuesJson = mock(JiraIssuesJson.class);
        jiraClientManager = mock(JiraClientManager.class);
        jiraService = spy(new JiraService(configurationManagementService, jiraClientManager, modalRestHandler));

        doReturn(apiDomain).when(configurationManagementService).getApiDomain();
        doReturn(uiDomain).when(configurationManagementService).getUIDomain();
        doReturn(modalRestHandler).when(jiraService).getModalRestHandler();
        doReturn(jiraClientManager).when(jiraService).getJiraClientManager();
        doReturn(wssoService).when(configurationManagementService).getWssoService();
        doReturn(stereotype).when(configurationManagementService).getJcsStereotype();
        when(jiraClientManager.getActiveJIRAConnectionInfo()).thenReturn(connectionInfo);
    }

    @Test
    public void updateJIRAConnectionInfo_noConfigurationObjects() {
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypedElements(stereotype);

        jiraService.updateJIRAConnectionInfo();

        verify(jiraClientManager).cleanJIRAConnectionInfo();
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString());
    }

    @Test
    public void updateJIRAConnectionInfo_multipleConfigurationsNullJiraUrlWithNamedElement() {
        List<Element> configurationElements = new ArrayList<>();
        configurationElements.add(namedElement);
        configurationElements.add(element);
        String name = "na::me";
        String formattedName = "[na::me]";
        String warning = String.format(MULTIPLE_JIRA_CONFIGURATIONS_WARNING, formattedName);
        String error = String.format(JIRA_CONNECTION_SETTINGS_NOT_SET, formattedName);

        doReturn(configurationElements).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(name).when(namedElement).getQualifiedName();
        doNothing().when(uiDomain).showWarningMessage(warning, MULTIPLE_CONFIGURATIONS_WARNING);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(namedElement, stereotype, PluginConstant.JIRA_URL);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(namedElement, stereotype, PluginConstant.JIRA_REST_PATH);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(namedElement, stereotype, PluginConstant.ISSUE_QUERY);
        doNothing().when(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);

        jiraService.updateJIRAConnectionInfo();

        verify(uiDomain).showWarningMessage(warning, MULTIPLE_CONFIGURATIONS_WARNING);
        verify(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString());
    }

    @Test
    public void updateJIRAConnectionInfo_multipleConfigurationsNullRestPathWithElement() {
        List<Element> configurationElements = new ArrayList<>();
        configurationElements.add(element);
        configurationElements.add(namedElement);
        String name = FIRST_CONFIG_OBJECT_FOUND;
        String warning = String.format(MULTIPLE_JIRA_CONFIGURATIONS_WARNING, name);
        String error = String.format(JIRA_CONNECTION_SETTINGS_NOT_SET, name);

        doReturn(configurationElements).when(apiDomain).getStereotypedElements(stereotype);
        doNothing().when(uiDomain).showWarningMessage(warning, MULTIPLE_CONFIGURATIONS_WARNING);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_URL);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_REST_PATH);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.ISSUE_QUERY);
        doNothing().when(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);

        jiraService.updateJIRAConnectionInfo();

        verify(uiDomain).showWarningMessage(warning, MULTIPLE_CONFIGURATIONS_WARNING);
        verify(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString());
    }

    @Test
    public void updateJIRAConnectionInfo_singleConfigurationNullIssueQueryWithElement() {
        List<Element> configurationElements = new ArrayList<>();
        configurationElements.add(element);
        String error = String.format(JIRA_CONNECTION_SETTINGS_NOT_SET, FIRST_CONFIG_OBJECT_FOUND);

        doReturn(configurationElements).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_URL);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_REST_PATH);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.ISSUE_QUERY);
        doNothing().when(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);

        jiraService.updateJIRAConnectionInfo();

        verify(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);
        verify(uiDomain, never()).showWarningMessage(anyString(), anyString());
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString());
    }

    @Test
    public void updateJIRAConnectionInfo_singleConfigurationEmptyJiraUrlWithElement() {
        List<Element> configurationElements = new ArrayList<>();
        configurationElements.add(element);
        String error = String.format(JIRA_CONNECTION_SETTINGS_NOT_SET, FIRST_CONFIG_OBJECT_FOUND);

        doReturn(configurationElements).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_URL);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_REST_PATH);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.ISSUE_QUERY);
        doNothing().when(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);

        jiraService.updateJIRAConnectionInfo();

        verify(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);
        verify(uiDomain, never()).showWarningMessage(anyString(), anyString());
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString());
    }

    @Test
    public void updateJIRAConnectionInfo_singleConfigurationEmptyRestPathWithElement() {
        List<Element> configurationElements = new ArrayList<>();
        configurationElements.add(element);
        String error = String.format(JIRA_CONNECTION_SETTINGS_NOT_SET, FIRST_CONFIG_OBJECT_FOUND);
        String url = "url";
        List<String> jiraUrl = new ArrayList<>();
        jiraUrl.add(url);

        doReturn(configurationElements).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(jiraUrl).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_URL);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_REST_PATH);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.ISSUE_QUERY);
        doNothing().when(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);

        jiraService.updateJIRAConnectionInfo();

        verify(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);
        verify(uiDomain, never()).showWarningMessage(anyString(), anyString());
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString());
    }

    @Test
    public void updateJIRAConnectionInfo_singleConfigurationEmptyIssueQueryWithElement() {
        List<Element> configurationElements = new ArrayList<>();
        configurationElements.add(element);
        String error = String.format(JIRA_CONNECTION_SETTINGS_NOT_SET, FIRST_CONFIG_OBJECT_FOUND);
        List<String> jiraUrl = new ArrayList<>();
        String url = "url";
        jiraUrl.add(url);
        List<String> restPath = new ArrayList<>();
        String rest = "rest";
        restPath.add(rest);

        doReturn(configurationElements).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(jiraUrl).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_URL);
        doReturn(restPath).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_REST_PATH);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.ISSUE_QUERY);
        doNothing().when(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);

        jiraService.updateJIRAConnectionInfo();

        verify(uiDomain).showErrorMessage(error, JIRA_CONNECTION_SETTINGS_ERROR);
        verify(uiDomain, never()).showWarningMessage(anyString(), anyString());
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString());
    }

    @Test
    public void updateJIRAConnectionInfo_singleConfigurationInfoAvailableWithElement() {
        List<Element> configurationElements = new ArrayList<>();
        configurationElements.add(element);
        List<String> jiraUrl = new ArrayList<>();
        String url = "url";
        jiraUrl.add(url);
        List<String> restPath = new ArrayList<>();
        String rest = "rest";
        restPath.add(rest);
        List<String> issueQuery = new ArrayList<>();
        String query = "query";
        issueQuery.add(query);

        doReturn(configurationElements).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(jiraUrl).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_URL);
        doReturn(restPath).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.JIRA_REST_PATH);
        doReturn(issueQuery).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.ISSUE_QUERY);
        doNothing().when(connectionInfo).setInfo(url, rest, query);

        jiraService.updateJIRAConnectionInfo();

        verify(connectionInfo).setInfo(url, rest, query);
        verify(uiDomain, never()).showErrorMessage(anyString(), anyString());
        verify(uiDomain, never()).showWarningMessage(anyString(), anyString());
    }

    @Test
    public void getClient_lacksInfo() {
        when(jiraClientManager.getActiveJIRAConnectionInfo()).thenReturn(connectionInfo);
        doReturn(false).when(connectionInfo).hasInfo();
        try {
            jiraService.getClient();
            fail("Exception did not occur");
        } catch (Exception e) {
            assertEquals(MISSING_JIRA_CONNECTION_SETTING, e.getMessage());
        }
    }

    @Test
    public void getClient_noEntryInMap() {
        when(jiraClientManager.getActiveJIRAConnectionInfo()).thenReturn(connectionInfo);
        doReturn(true).when(connectionInfo).hasInfo();
        when(jiraClientManager.containsConnection(connectionInfo)).thenReturn(false);
        try {
            jiraService.getClient();
            fail("Exception did not occur");
        } catch (Exception e) {
            assertEquals(NO_JIRA_LOGIN_ESTABLISHED_OR_EXPIRED, e.getMessage());
        }
    }

    @Test
    public void getClient_containsKey() {
        when(jiraClientManager.getActiveJIRAConnectionInfo()).thenReturn(connectionInfo);
        doReturn(true).when(connectionInfo).hasInfo();
        when(jiraClientManager.containsConnection(connectionInfo)).thenReturn(true);
        when(jiraClientManager.getClientFromConnectionInfo(connectionInfo)).thenReturn(client);
        try {
            assertEquals(client, jiraService.getClient());
        } catch (Exception e) {
            fail("Exception did not occur");
        }
    }

    @Test
    public void acquireToken_mapHasConnectionInfo() {
        when(jiraClientManager.getActiveJIRAConnectionInfo()).thenReturn(connectionInfo);
        when(jiraClientManager.containsConnection(connectionInfo)).thenReturn(true);
        when(jiraClientManager.getClientFromConnectionInfo(connectionInfo)).thenReturn(client);
        doNothing().when(wssoService).acquireToken(connectionInfo, client, true, ATLASSIAN_XSRF_TOKEN);

        jiraService.acquireToken();

        verify(wssoService).acquireToken(connectionInfo, client, true, ATLASSIAN_XSRF_TOKEN);
    }

    @Test
    public void acquireToken_mapLacksConnectionInfoButAnActiveConnectionExists() {
        when(jiraClientManager.getActiveJIRAConnectionInfo()).thenReturn(connectionInfo);
        when(jiraClientManager.containsConnection(connectionInfo)).thenReturn(false);
        doReturn(true).when(connectionInfo).hasInfo();
        doReturn(client).when(jiraService).createJiraClient(connectionInfo);
        doNothing().when(jiraClientManager).putEntryIntoClientMap(connectionInfo, client);
        doNothing().when(wssoService).acquireToken(connectionInfo, client, true, ATLASSIAN_XSRF_TOKEN);

        jiraService.acquireToken();

        verify(wssoService).acquireToken(connectionInfo, client, true, ATLASSIAN_XSRF_TOKEN);
    }

    @Test
    public void acquireToken_noConnectionInfoExists() {
        when(jiraClientManager.getActiveJIRAConnectionInfo()).thenReturn(connectionInfo);
        when(jiraClientManager.containsConnection(connectionInfo)).thenReturn(false);
        doReturn(false).when(connectionInfo).hasInfo();
        doNothing().when(uiDomain).showErrorMessage(JIRA_CONNECTION_NOT_INITIALIZED, NO_JIRA_CONNECTION);

        jiraService.acquireToken();

        verify(uiDomain).showErrorMessage(JIRA_CONNECTION_NOT_INITIALIZED, NO_JIRA_CONNECTION);
        verify(wssoService, never()).acquireToken(connectionInfo, client, true, ATLASSIAN_XSRF_TOKEN);
    }

    private void setupForGetIssues(String url, String issueQuery, String token) throws JiraIntegrationException {
        doReturn(client).when(jiraService).getClient();
        doReturn(url).when(jiraService).buildJiraUrl();
        doReturn(connectionInfo).when(jiraClientManager).getActiveJIRAConnectionInfo();
        doReturn(issueQuery).when(connectionInfo).getIssueQuery();
        doReturn(token).when(client).getToken();
    }

    @Test
    public void getIssues_initialCall() {
        String url = "url";
        String issueQuery = "query";
        String token = "token";
        Map<String, String> params = new HashMap<>();
        params.put(JQL, issueQuery);
        params.put(START_AT, String.valueOf(0));
        Map<String, String> headers = new HashMap<>();
        headers.put(COOKIE, token);
        JsonElement json = spy(new JsonObject());
        List<Map<String, String>> issues = new ArrayList<>();

        try {
            setupForGetIssues(url, issueQuery, token);
            doReturn(json).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(url, HTTP_GET, JIRA_SEARCH_ENDPOINT,
                    null, null, params, headers, PluginConstant.FETCHING_JIRA_ISSUES, true, 0);
            doReturn(issues).when(jiraService).extractIssues(json);

            List<Map<String, String>> results = jiraService.getIssues(0, PluginConstant.MAX_RESULTS_DEFAULT_VALUE);

            assertEquals(issues, results);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getIssues_notUsingDefaultMaxResults() {
        String url = "url";
        String issueQuery = "query";
        String token = "token";
        Map<String, String> params = new HashMap<>();
        params.put(JQL, issueQuery);
        params.put(START_AT, String.valueOf(0));
        params.put(MAX_RESULTS, String.valueOf(5));
        Map<String, String> headers = new HashMap<>();
        headers.put(COOKIE, token);
        JsonElement json = spy(new JsonObject());
        List<Map<String, String>> issues = new ArrayList<>();

        try {
            setupForGetIssues(url, issueQuery, token);
            doReturn(json).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(url, HTTP_GET, JIRA_SEARCH_ENDPOINT,
                    null, null, params, headers, PluginConstant.FETCHING_JIRA_ISSUES, true, 0);
            doReturn(issues).when(jiraService).extractIssues(json);

            List<Map<String, String>> results = jiraService.getIssues(0, 5);

            assertEquals(issues, results);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getIssues_negativeMaxResults() {
        String url = "url";
        String issueQuery = "query";
        String token = "token";
        String errorMessage = "No Jira Issues can be fetched";

        try {
            setupForGetIssues(url, issueQuery, token);

            jiraService.getIssues(0, -1);

            fail("Expected exception did not occur");
        } catch(Exception e) {
            assertTrue(e instanceof  JiraIntegrationException);
            assertEquals(errorMessage, e.getMessage());
        }
    }


    @Test
    public void getIssues_errorDuringGet() {
        String url = "url";
        String issueQuery = "query";
        String token = "token";
        Map<String, String> params = new HashMap<>();
        params.put(JQL, issueQuery);
        params.put(START_AT, "1");
        Map<String, String> headers = new HashMap<>();
        headers.put(COOKIE, token);
        String error = "error";
        Exception exception = spy(new Exception(error));

        try {
            setupForGetIssues(url, issueQuery, token);
            doThrow(exception).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(url, HTTP_GET, JIRA_SEARCH_ENDPOINT,
                    null, null, params, headers, PluginConstant.FETCHING_JIRA_ISSUES, true, 0);

            jiraService.getIssues(1, PluginConstant.MAX_RESULTS_DEFAULT_VALUE);

            fail("Expected exception did not occur");
        } catch(Exception e) {
            assertEquals(error, e.getMessage());
        }
    }

    @Test
    public void extractIssues_noIssues() {
        JsonElement json = mock(JsonElement.class);
        doReturn(jiraIssuesJson).when(jiraService).deserializeIssuesQueryResult(json);
        doReturn(new ArrayList<>()).when(jiraIssuesJson).getIssues();
        List<Map<String, String>> result = jiraService.extractIssues(json);

        assertTrue(result.isEmpty());
    }

    @Test
    public void extractIssues_ValidAndInvalidIssue() {
        JsonElement jsonElement = mock(JsonElement.class);
        jiraIssuesJson = spy(new JiraIssuesJson());
        ArrayList<JiraIssueJson> issuesArray = new ArrayList<>();
        jiraIssueJson = spy(new JiraIssueJson());
        JiraIssueJson invalidIssue = spy(new JiraIssueJson());
        issuesArray.add(jiraIssueJson);
        issuesArray.add(invalidIssue);
        jiraIssuesJson.setIssues(issuesArray);
        jiraIssuesJson.setTotal(2);
        // prepare for valid and invalid issue
        String id1 = "id1";
        String id2 = "id2";
        JiraIssueFieldsJson validFields = spy(new JiraIssueFieldsJson());
        JiraIssueFieldsJson invalidFields = spy(new JiraIssueFieldsJson());
        String description = "description";
        String summary = "summary";
        JiraIssueStatusJson statusJson = spy(new JiraIssueStatusJson());
        String statusName = "statusName";
        statusJson.setName(statusName);
        JiraIssueCreatorJson creatorJson = spy(new JiraIssueCreatorJson());
        String creatorDisplayName = "creatorDisplayName";
        creatorJson.setDisplayName(creatorDisplayName);
        JiraIssueTypeJson issueTypeJson = spy(new JiraIssueTypeJson());
        String issueTypeName = "issueTypeName";
        issueTypeJson.setName(issueTypeName);
        // set up the valid and invalid fields
        validFields.setDescription(description);
        validFields.setSummary(summary);
        validFields.setStatus(statusJson);
        validFields.setCreator(creatorJson);
        validFields.setIssueType(issueTypeJson);
        invalidFields.setDescription(description);
        invalidFields.setSummary(null);
        invalidFields.setStatus(statusJson);
        invalidFields.setCreator(creatorJson);
        invalidFields.setIssueType(issueTypeJson);
        // set up the valid and invalid issue
        jiraIssueJson.setId(id1);
        jiraIssueJson.setFields(validFields);
        invalidIssue.setId(id2);
        invalidIssue.setFields(invalidFields);
        // expected map
        Map<String, String> expected = new HashMap<>();
        expected.put(ID, id1);
        expected.put(DESCRIPTION, validFields.getDescription());
        expected.put(NAME, validFields.getSummary());
        expected.put(STATUS, validFields.getStatus().getName());
        expected.put(CREATOR, validFields.getCreator().getDisplayName());
        expected.put(TYPE, validFields.getIssueType().getName());

        doReturn(jiraIssuesJson).when(jiraService).deserializeIssuesQueryResult(jsonElement);

        List<Map<String, String>> result = jiraService.extractIssues(jsonElement);

        assertEquals(1, result.size());
        expected.forEach((k, v) -> {
            assertTrue(result.get(0).containsKey(k));
            assertEquals(v, result.get(0).get(k));
        });
    }

    @Test
    public void getIssue() {
        String id = ID;
        String url = "url";
        String endpoint = JIRA_ISSUE_ENDPOINT + id;
        String token = "token";
        Map<String, String> headers = new HashMap<>();
        headers.put(COOKIE, token);
        JsonElement json = spy(new JsonObject());
        JsonObject jsonObject = spy(new JsonObject());

        try {
            doReturn(client).when(jiraService).getClient();
            doReturn(url).when(jiraService).buildJiraUrl();
            doReturn(token).when(client).getToken();
            doReturn(json).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(url, HTTP_GET, endpoint, null, null, null, headers, "Fetching JIRA issue", true, 0);
            doReturn(jiraIssueJson).when(jiraService).deserializeIssueQueryResult(json);

            Assert.assertEquals(jiraIssueJson, jiraService.getIssue(id));
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getIssue_exceptionTest() {
        String id = ID;
        String url = "url";
        String endpoint = JIRA_ISSUE_ENDPOINT + id;
        String token = "token";
        Map<String, String> headers = new HashMap<>();
        headers.put(COOKIE, token);
        String error = "error";
        Exception exception = spy(new Exception(error));

        try {
            doReturn(client).when(jiraService).getClient();
            doReturn(url).when(jiraService).buildJiraUrl();
            doReturn(token).when(client).getToken();
            doThrow(exception).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(url, HTTP_GET, endpoint, null, null, null, headers, "Fetching JIRA issue", true, 0);

            jiraService.getIssue(id);

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(error, e.getMessage());
        }
    }

    @Test
    public void buildJiraURL_confirmResolution() {
        String loginURL = "https://login.url/"; // purposeful trailing slash
        String endpoint = "/path/"; // purposeful initial slash
        String expected = "https://login.url/path/"; // we want resolve to accomplish this
        doReturn(loginURL).when(connectionInfo).getUrl();
        doReturn(endpoint).when(connectionInfo).getJiraRestPath();
        String string = jiraService.buildJiraUrl();
        assertEquals(expected, string);
    }

    @Test
    public void createJiraClient(){
        String jiraURL = "jiraURL";
        doReturn(jiraURL).when(connectionInfo).getUrl();
        jiraService.createJiraClient(connectionInfo);
        assertTrue(jiraService.createJiraClient(connectionInfo) instanceof JiraClient);
    }
}
