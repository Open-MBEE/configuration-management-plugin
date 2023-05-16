package org.openmbee.plugin.cfgmgmt.integration.threedx;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.ModalRestHandler;
import org.openmbee.plugin.cfgmgmt.integration.threedx.json.*;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;

public class ThreeDxService {
    private final Logger logger = LoggerFactory.getLogger(ThreeDxService.class);

    private ConfigurationManagementService configurationManagementService;
    private ThreeDxClientManager threeDxClientManager;
    private ModalRestHandler modalRestHandler;

    public ThreeDxService(ConfigurationManagementService configurationManagementService, ThreeDxClientManager threeDxClientManager, ModalRestHandler modalRestHandler) {
        this.configurationManagementService = configurationManagementService;
        this.threeDxClientManager = threeDxClientManager;
        this.modalRestHandler = modalRestHandler;
    }

    protected Logger getLogger() {
        return logger; // used for unit testing
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected ModalRestHandler getModalRestHandler() {
        return modalRestHandler;
    }

    public ThreeDxClientManager getThreeDxClientManager() {
        return threeDxClientManager;
    }

    public void acquireToken() {
        try {
            getConfigurationManagementService().getWssoService().acquireToken(getThreeDxClientManager().getActive3DxConnectionInfo(),
                    getOrCreateClient(), null, "CASTGC");
        } catch (Exception e) {
            getUIDomain().logErrorAndShowMessage(getLogger(), e.getMessage(), e.getMessage(), e);
        }
    }

    protected ThreeDxClient getOrCreateClient() throws ThreeDxIntegrationException {
        if (!getThreeDxClientManager().has3DxConnectionSettings()) {
            throw new ThreeDxIntegrationException(MISSING_3DX_CONNECTION_SETTING);
        }

        if (getThreeDxClientManager().containsConnection(getThreeDxClientManager().getActive3DxConnectionInfo())) {
            return getThreeDxClientManager().getClientFromConnectionInfo(getThreeDxClientManager().getActive3DxConnectionInfo());
        } else {
            ThreeDxClient client = newThreeDxClient();
            getThreeDxClientManager().putEntryIntoClientMap(getThreeDxClientManager().getActive3DxConnectionInfo(), client);
            return client;
        }
    }

    protected ThreeDxClient newThreeDxClient() {
        return new ThreeDxClient(getThreeDxClientManager().getActive3DxConnectionInfo().getPass3dsURL(),
            getThreeDxClientManager().getActive3DxConnectionInfo().getSpace3dsURL(),
            getThreeDxClientManager().getActive3DxConnectionInfo().getSearch3dsURL(), configurationManagementService);
    }

    /**
     * Invoked when project starts and sets the active connection info for the project: active3DxConnectionInfo
     */
    public void update3DxConnectionInfo() {
        List<Element> elements = getApiDomain().getStereotypedElements(configurationManagementService.getTdxcsStereotype());
        if (elements.isEmpty()) {
            getThreeDxClientManager().clean3DxConnectionInfo();
            return;
        }

        Element element = elements.stream()
                .filter(e -> getConfigurationManagementService().getApiDomain().isElementInCurrentProject(e))
                .findFirst()
                .orElse(elements.get(0));

        String elementName = EMPTY_STRING;
        if (element instanceof NamedElement) {
            elementName = ((NamedElement) element).getQualifiedName();
        }

        if (elements.size() > 1) {
            getConfigurationManagementService().getUIDomain().log(String.format(MULTIPLE_THREEDX_CONFIGURATIONS_WARNING, elementName));
        }

        List<String> pass3dsUrls = getApiDomain().getStereotypePropertyValueAsString(element,
            configurationManagementService.getTdxcsStereotype(), PluginConstant.URL_3DPASSPORT);
        List<String> space3dsUrls = getApiDomain().getStereotypePropertyValueAsString(element,
            configurationManagementService.getTdxcsStereotype(), PluginConstant.URL_3DSPACE);
        List<String> search3dsUrls = getApiDomain().getStereotypePropertyValueAsString(element,
            configurationManagementService.getTdxcsStereotype(), PluginConstant.URL_3DSEARCH);
        List<String> changeActionQueries = getApiDomain().getStereotypePropertyValueAsString(element,
            configurationManagementService.getTdxcsStereotype(), PluginConstant.CHANGE_ACTION_QUERY);

        if (pass3dsUrls == null || space3dsUrls == null || search3dsUrls == null || changeActionQueries == null ||
            pass3dsUrls.isEmpty() || space3dsUrls.isEmpty() || search3dsUrls.isEmpty() || changeActionQueries.isEmpty()) {
            getUIDomain().showErrorMessage(String.format(THREEDX_CONNECTION_SETTINGS_NOT_SET, elementName),
                THREEDX_CONNECTION_SETTINGS_ERROR);
            return;
        }

        getThreeDxClientManager().getActive3DxConnectionInfo().setInfo(pass3dsUrls.get(ZERO), space3dsUrls.get(ZERO), search3dsUrls.get(ZERO),
            changeActionQueries.get(ZERO));
    }

    /**
     * Get changeRecord
     *
     * @return a JSON of the current user
     */
    public JsonObject getChangeAction(String id) throws ThreeDxIntegrationException {
        ThreeDxClient client = getOrCreateClient();
        if (!client.isLoggedInToSpace()) {
            if (!client.isLoggedIn()) {
                throw new ThreeDxIntegrationException(NO_3DX_LOGIN_ESTABLISHED_OR_EXPIRED);
            }
            client.loginToSpace();
        }

        // - Build web service url
        String endpoint = String.format(THREEDX_CHANGE_ACTION_ENDPOINT, id);

        // - Send query and catch response
        Map<String, String> headers = new HashMap<>();
        headers.put(SECURITY_CONTEXT, client.getPreferredCredentials());

        try {
            JsonElement json = getModalRestHandler().loadUrlAsJsonWithStatusRunner(client.get3DSpaceServer(), HTTP_GET, endpoint,
                null, null, null, headers, FETCHING_CHANGE_ACTION, true, ZERO);
            return json.getAsJsonObject();
        } catch (Exception e) {
            throw new ThreeDxIntegrationException(e.getMessage());
        }
    }

    /**
     * Get change actions
     *
     * @return a JSON of the current user
     */
    public List<Map<String, String>> getChangeActions(int nresults) throws ThreeDxIntegrationException {
        ThreeDxClient client = getOrCreateClient();
        if (!client.isLoggedInToSearch()) {
            if (!client.isLoggedIn()) {
                throw new ThreeDxIntegrationException(NO_3DX_LOGIN_ESTABLISHED_OR_EXPIRED);
            }
            client.loginToSearch();
        }

        // Prepare data to send
        ThreeDxQueryJson queryJson = prepareQueryJson(client.getNextStart(), nresults);
        byte[] postData = serializeForQuery(queryJson).getBytes(StandardCharsets.UTF_8);
        Map<String, String> params = new HashMap<>();
        params.put("xrequestedwith", "xmlhttprequest");
        Map<String, String> headers = new HashMap<>();
        headers.put(SECURITY_CONTEXT, client.getPreferredCredentials());

        // Send query and catch response
        JsonElement json;
        try {
            json = getModalRestHandler().loadUrlAsJsonWithStatusRunner(client.get3DSearchServer(), HTTP_POST, THREEDX_FEDERATED_SEARCH_ENDPOINT,
                    JSON_CONTENT_TYPE, postData, params, headers, FETCHING_CHANGE_ACTIONS, true, ZERO);
        } catch (Exception e) {
            throw new ThreeDxIntegrationException(e.getMessage());
        }

        ThreeDxQueryResultsJson resultsJson = deserializeQueryResults(json);
        client.setNextStart(resultsJson.getInfos().getNextStart());
        return extractChangeActions(resultsJson);
    }

    protected String serializeForQuery(ThreeDxQueryJson threeDxQueryJson) {
        // used for unit testing
        return new GsonBuilder().registerTypeAdapter(ThreeDxQueryJson.class, new ThreeDxQueryJsonSerializer()).create().toJson(threeDxQueryJson);
    }

    protected ThreeDxQueryJson prepareQueryJson(String nextStart, int nresults) {
        ThreeDxQueryJson queryJson = createQuery(preparePredicate(), prepareSelectFile(), prepareSnippets());
        if (getThreeDxClientManager().isNewRun() || nextStart == null || nextStart.isEmpty()) {
            queryJson.setStart("0");
            queryJson.setNextStart(null); // make sure nextStart isn't serialized in the case of a new run after a reset
            getThreeDxClientManager().setNewRun(false);
        } else {
            queryJson.setNextStart(nextStart);
        }

        if(nresults != PluginConstant.MAX_RESULTS_DEFAULT_VALUE) {
            queryJson.setNresults(nresults);
        }
        return queryJson;
    }

    protected List<String> preparePredicate() {
        return List.of("ds6w:label", "ds6w:type", "ds6w:description", "ds6w:identifier", "ds6w:modified", "ds6w:created",
            "ds6wg:revision", "ds6w:status", "ds6w:responsible", "owner", "ds6w:responsibleUid", "ds6wg:filesize",
            "ds6w:project", "ds6w:dataSource", "ds6w:community");
    }

    protected List<String> prepareSelectFile() {
        return List.of("icon", "thumbnail_2d");
    }

    protected List<String> prepareSnippets() {
        return List.of("ds6w:snippet", "ds6w:label:snippet", "ds6w:responsible:snippet",
            "ds6w:community:snippet", "swym:message_text:snippet");
    }

    protected ThreeDxQueryJson createQuery(List<String> predicate, List<String> selectFile, List<String> snippets) {
        return new ThreeDxQueryJson(true, true, false, THREEDX_SEARCH, LOCALE,
            predicate, selectFile, getThreeDxClientManager().getActive3DxConnectionInfo().getChangeActionQuery(),
                new JsonObject(), THREED_DESC, RELEVANCE, snippets, PluginConstant.MAX_RESULTS_DEFAULT_VALUE,
                null, null, List.of(THREEDSPACE), ON_PREMISE);
    }

    protected List<Map<String, String>> extractChangeActions(ThreeDxQueryResultsJson resultsJson) {
        if(resultsJson == null) {
            return List.of();
        }
        if(resultsJson.getError() != null && resultsJson.getErrorDescription() != null) {
            getUIDomain().logError(String.format(ExceptionConstants.THREEDX_ERROR_GETTING_CHANGE_ACTIONS, resultsJson.getErrorDescription()));
            return List.of();
        }

        if(resultsJson.getInfos().getNresults() == ZERO) {
            return List.of();
        }

        return collectResults(resultsJson.getResults());
    }

    protected List<Map<String, String>> collectResults(List<ThreeDxQueryResultJson> resultsJson) {
        List<Map<String, String>> changeActions = new ArrayList<>();
        Set<String> collisionPairs = new HashSet<>();
        AtomicReference<String> key = new AtomicReference<>();
        resultsJson.forEach(r -> {
            if(r.getAttributes() != null && !r.getAttributes().isEmpty()) {
                changeActions.add(r.getAttributes().stream().collect(Collectors.toMap(
                    item -> { key.set(item.getName()); return item.getName(); },
                    ThreeDxQueryResultAttributeJson::getValue,
                    (value1, value2) -> {
                        collisionPairs.add(String.format(ExceptionConstants.THREEDX_KEY_COLLISION_PAIR, key.get(), value1));
                        collisionPairs.add(String.format(ExceptionConstants.THREEDX_KEY_COLLISION_PAIR, key.get(), value2));
                        return null; // returning a null here effectively removes both key-value pairs
                    }
                )));
            }
        });

        if(!collisionPairs.isEmpty()) {
            getUIDomain().showErrorMessage(String.format(ExceptionConstants.THREEDX_KEY_COLLISION,
                    String.join(EMPTY_STRING, collisionPairs)), ExceptionConstants.THREEDX_KEY_COLLISION_TITLE);
        }

        return changeActions;
    }

    protected ThreeDxQueryResultsJson deserializeQueryResults(JsonElement json) {
        // used for unit testing
        return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().fromJson(json, ThreeDxQueryResultsJson.class);
    }
}
