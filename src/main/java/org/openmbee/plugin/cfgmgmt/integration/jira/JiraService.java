package org.openmbee.plugin.cfgmgmt.integration.jira;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.ModalRestHandler;
import org.openmbee.plugin.cfgmgmt.integration.jira.json.JiraIssueJson;
import org.openmbee.plugin.cfgmgmt.integration.jira.json.JiraIssuesJson;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;
import static org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants.*;

public class JiraService {
    private ConfigurationManagementService configurationManagementService;
    private JiraClientManager jiraClientManager;
    private ModalRestHandler modalRestHandler;

    public JiraService(ConfigurationManagementService configurationManagementService, JiraClientManager jiraClientManager, ModalRestHandler modalRestHandler) {
        this.configurationManagementService = configurationManagementService;
        this.jiraClientManager = jiraClientManager;
        this.modalRestHandler = modalRestHandler;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected UIDomain getUIDomain() {
        return configurationManagementService.getUIDomain();
    }

    protected ModalRestHandler getModalRestHandler() {
        return modalRestHandler;
    }

    public JiraClientManager getJiraClientManager() {
        return jiraClientManager;
    }

    /**
     * Invoked when project starts and sets the active connection info for the project: activeJIRAConnectionInfo
     */
    public void updateJIRAConnectionInfo() {
        ApiDomain apiDomain = getConfigurationManagementService().getApiDomain();
        Stereotype jcsStereotype = getConfigurationManagementService().getJcsStereotype();
        List<Element> elements = apiDomain.getStereotypedElements(jcsStereotype);
        if (elements.isEmpty()) {
            getJiraClientManager().cleanJIRAConnectionInfo();
            return;
        }

        Element element = elements.stream()
                .filter(e -> apiDomain.isElementInCurrentProject(e))
                .findFirst()
                .orElse(elements.get(0));

        String elementName = FIRST_CONFIG_OBJECT_FOUND;
        if (element instanceof NamedElement) {
            elementName = OPEN_SQUARE_BRACE + ((NamedElement) element).getQualifiedName() + CLOSE_SQUARE_BRACE;
        }

        if (elements.size() > 1) {
            getConfigurationManagementService().getUIDomain().log(String.format(MULTIPLE_JIRA_CONFIGURATIONS_WARNING, elementName));
        }

        List<String> jiraUrls = apiDomain.getStereotypePropertyValueAsString(element, jcsStereotype, PluginConstant.JIRA_URL);
        List<String> wssoUrls = apiDomain.getStereotypePropertyValueAsString(element, jcsStereotype, PluginConstant.WSSO_URL);
        List<String> jiraRestPaths = apiDomain.getStereotypePropertyValueAsString(element, jcsStereotype, PluginConstant.JIRA_REST_PATH);
        List<String> issueQueries = apiDomain.getStereotypePropertyValueAsString(element, jcsStereotype, PluginConstant.ISSUE_QUERY);

        if (jiraUrls == null || jiraRestPaths == null || issueQueries == null ||
                jiraUrls.isEmpty() || jiraRestPaths.isEmpty() || issueQueries.isEmpty()) {
            getConfigurationManagementService().getUIDomain().showErrorMessage(
                String.format(JIRA_CONNECTION_SETTINGS_NOT_SET, elementName),
                JIRA_CONNECTION_SETTINGS_ERROR);
            return;
        }

        // processing wssoUrl separately because it is allowed to be empty
        String wssoUrl = null;
        if (wssoUrls != null && !wssoUrls.isEmpty()) {
            wssoUrl = wssoUrls.get(0);
        }

        getJiraClientManager().getActiveJIRAConnectionInfo().setInfo(jiraUrls.get(0), wssoUrl, jiraRestPaths.get(0), issueQueries.get(0));
    }

    public JiraClient getClient() throws JiraIntegrationException {
        JiraConnectionInfo connectionInfo = getJiraClientManager().getActiveJIRAConnectionInfo();
        if (!connectionInfo.hasInfo()) {
            throw new JiraIntegrationException(MISSING_JIRA_CONNECTION_SETTING);
        }

        if (getJiraClientManager().containsConnection(connectionInfo)) {
            return getJiraClientManager().getClientFromConnectionInfo(connectionInfo);
        } else {
            throw new JiraIntegrationException(NO_JIRA_LOGIN_ESTABLISHED_OR_EXPIRED);
        }
    }

    public void acquireToken() {
        JiraClient client;
        JiraConnectionInfo connectionInfo = getJiraClientManager().getActiveJIRAConnectionInfo();
        if (getJiraClientManager().containsConnection(connectionInfo)) {
            client = getJiraClientManager().getClientFromConnectionInfo(connectionInfo);
        } else if (connectionInfo.hasInfo()) {
            client = createJiraClient(connectionInfo);
            getJiraClientManager().putEntryIntoClientMap(connectionInfo, client);
        } else {
            getConfigurationManagementService().getUIDomain().showErrorMessage(JIRA_CONNECTION_NOT_INITIALIZED,
                NO_JIRA_CONNECTION);
            return;
        }

        getConfigurationManagementService().getWssoService().acquireToken(connectionInfo, client,
            connectionInfo.getWssoURL(), ATLASSIAN_XSRF_TOKEN);
    }

    protected JiraClient createJiraClient(JiraConnectionInfo connectionInfo) {
        return new JiraClient(connectionInfo.getUrl()); // used for unit tests
    }

    public List<Map<String, String>> getIssues(int startAt, int maxResults) throws JiraIntegrationException {
        JiraClient client = getClient();
        String url = buildJiraUrl();

        Map<String, String> params = new HashMap<>();
        params.put(JQL, getJiraClientManager().getActiveJIRAConnectionInfo().getIssueQuery());
        params.put(START_AT, String.valueOf(startAt));
        if (maxResults != MAX_RESULTS_DEFAULT_VALUE && maxResults > 0) {
            params.put(MAX_RESULTS, String.valueOf(maxResults)); // only bother with parameter if it overrides default
        } else if(maxResults == -1) {
            throw new JiraIntegrationException(NO_JIRA_ISSUES_CAN_BE_FETCHED);
        }
        Map<String, String> headers = new HashMap<>();
        headers.put(COOKIE, client.getToken());

        try {
            JsonElement json = getModalRestHandler().loadUrlAsJsonWithStatusRunner(url, HTTP_GET, JIRA_SEARCH_ENDPOINT, null,
                    null, params, headers, FETCHING_JIRA_ISSUES, true, 0);
            return extractIssues(json);
        } catch(Exception e) {
            getUIDomain().logError(e.getMessage());
            throw new JiraIntegrationException(e.getMessage());
        }
    }

    protected List<Map<String, String>> extractIssues(JsonElement json) {
        JiraIssuesJson issuesJson = deserializeIssuesQueryResult(json);
        List<JiraIssueJson> issuesList = issuesJson.getIssues();
        List<Map<String, String>> issues = new ArrayList<>();
        AtomicBoolean isValid = new AtomicBoolean(true);
        issuesList.forEach(i -> {
            Map<String, String> issue = new HashMap<>();
            issue.put(ID, i.getId());
            issue.put(DESCRIPTION, i.getFields().getDescription());
            issue.put(NAME, i.getFields().getSummary());
            issue.put(STATUS, i.getFields().getStatus().getName());
            issue.put(CREATOR, i.getFields().getCreator().getDisplayName());
            issue.put(TYPE, i.getFields().getIssueType().getName());

            issue.forEach((k, v) -> {
                if(v == null) {
                    isValid.set(false); // if any values in issue are null, the issue is invalid
                }
            });

            if(isValid.get()) {
                issues.add(issue);
            }

            isValid.set(true); // reset for next validation
        });

        return issues;
    }

    protected JiraIssuesJson deserializeIssuesQueryResult(JsonElement json) {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(json, JiraIssuesJson.class);
    }

    public JiraIssueJson getIssue(String id) throws JiraIntegrationException {
        JiraClient client = getClient();
        String url = buildJiraUrl();

        String endpoint = JIRA_ISSUE_ENDPOINT + id;
        Map<String, String> headers = new HashMap<>();
        headers.put(COOKIE, client.getToken());

        try {
            JsonElement json = getModalRestHandler().loadUrlAsJsonWithStatusRunner(url, HTTP_GET, endpoint, null,
                    null, null, headers, FETCHING_JIRA_ISSUE, true, 0);
            return deserializeIssueQueryResult(json);
        } catch(Exception e) {
            getUIDomain().logError(e.getMessage());
            throw new JiraIntegrationException(e.getMessage());
        }
    }

    protected String buildJiraUrl() {
        URI host = URI.create(getJiraClientManager().getActiveJIRAConnectionInfo().getUrl());
        URI uri = URI.create(String.format("%s://%s", host.getScheme(), host.getHost()));
        return uri.resolve(getJiraClientManager().getActiveJIRAConnectionInfo().getJiraRestPath()).toString();
    }

    protected JiraIssueJson deserializeIssueQueryResult(JsonElement json) {
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(json, JiraIssueJson.class);
    }
}
