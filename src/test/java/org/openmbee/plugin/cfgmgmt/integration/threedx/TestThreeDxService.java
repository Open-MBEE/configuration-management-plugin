package org.openmbee.plugin.cfgmgmt.integration.threedx;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.ModalRestHandler;
import org.openmbee.plugin.cfgmgmt.integration.WssoService;
import org.openmbee.plugin.cfgmgmt.integration.threedx.json.*;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmbee.plugin.cfgmgmt.integration.threedx.json.*;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;
import static org.junit.Assert.*;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.Mockito.*;

public class TestThreeDxService {

    private ThreeDxService threeDxService;
    private ConfigurationManagementService configurationManagementService;
    private ThreeDxClientManager threeDxClientManager;
    private WssoService wssoService;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private ThreeDxConnectionInfo connectionInfo;
    private ThreeDxClient client;
    private Map<ThreeDxConnectionInfo, ThreeDxClient> clientMap;
    private ModalRestHandler modalRestHandler;
    private Stereotype stereotype;
    private Logger logger;

    @Before
    public void setup() {
        connectionInfo = mock(ThreeDxConnectionInfo.class);
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        client = mock(ThreeDxClient.class);
        clientMap = new HashMap<>();
        modalRestHandler = mock(ModalRestHandler.class);
        stereotype = mock(Stereotype.class);
        logger = mock(Logger.class);

        configurationManagementService = mock(ConfigurationManagementService.class);
        threeDxClientManager = mock(ThreeDxClientManager.class);
        threeDxService = Mockito.spy(new ThreeDxService(configurationManagementService, threeDxClientManager, modalRestHandler));
        wssoService = mock(WssoService.class);

        when(configurationManagementService.getApiDomain()).thenReturn(apiDomain);
        when(configurationManagementService.getUIDomain()).thenReturn(uiDomain);
        when(configurationManagementService.getTdxcsStereotype()).thenReturn(stereotype);
        when(configurationManagementService.getWssoService()).thenReturn(wssoService);

        doReturn(threeDxClientManager).when(threeDxService).getThreeDxClientManager();
        when(threeDxClientManager.getActive3DxConnectionInfo()).thenReturn(connectionInfo);
        doReturn(logger).when(threeDxService).getLogger();
        doReturn(modalRestHandler).when(threeDxService).getModalRestHandler();
    }

    @Test
    public void acquireToken() {
        try {
            doReturn(client).when(threeDxService).getOrCreateClient();
            doNothing().when(wssoService).acquireToken(connectionInfo, client, false, "CASTGC");

            threeDxService.acquireToken();
        } catch (Exception exec) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void acquireToken_exceptionGettingToken() {
        String error = "error";
        ThreeDxIntegrationException integrationException = spy(new ThreeDxIntegrationException(error));

        try {
            doThrow(integrationException).when(threeDxService).getOrCreateClient();

            threeDxService.acquireToken();

            verify(uiDomain).logErrorAndShowMessage(logger, error, error, integrationException);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getOrCreateClient_noSettingsInfo() {
        String errorMessage = PluginConstant.MISSING_3DX_CONNECTION_SETTING;
        when(threeDxClientManager.has3DxConnectionSettings()).thenReturn(false);
        try {
            threeDxService.getOrCreateClient();
            fail("Exception did not occur");
        } catch (ThreeDxIntegrationException e) {
            assertEquals(errorMessage, e.getMessage());
        }
    }

    @Test
    public void getOrCreateClient_hasKey() {
        try {
            when(threeDxClientManager.has3DxConnectionSettings()).thenReturn(true);
            when(threeDxClientManager.containsConnection(connectionInfo)).thenReturn(true);
            when(threeDxClientManager.getClientFromConnectionInfo(connectionInfo)).thenReturn(client);
            assertSame(client, threeDxService.getOrCreateClient());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getOrCreateClient_noEntryInMap() {
        try {
            when(threeDxClientManager.has3DxConnectionSettings()).thenReturn(true);
            when(threeDxClientManager.containsConnection(connectionInfo)).thenReturn(false);
            doReturn(client).when(threeDxService).newThreeDxClient();
            doNothing().when(threeDxClientManager).putEntryIntoClientMap(connectionInfo, client);

            assertSame(client, threeDxService.getOrCreateClient());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void update3DxConnectionInfo_noThreeDxConfiguration() {
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypedElements(stereotype);
        doNothing().when(threeDxClientManager).clean3DxConnectionInfo();

        threeDxService.update3DxConnectionInfo();

        verify(threeDxClientManager).clean3DxConnectionInfo();
        verify(apiDomain, never()).getStereotypePropertyValueAsString(any(), anyString(), anyString());
    }

    @Test
    public void update3DxConnectionInfo_multipleThreeDxConfigurationsAndFirstElementIsNamed_pass3dsNull() {
        List<Element> configurations = new ArrayList<>();
        NamedElement config1 = mock(NamedElement.class);
        Element config2 = mock(Element.class);
        configurations.add(config1);
        configurations.add(config2);
        String configName = "config::Name";
        String warning = String.format(PluginConstant.MULTIPLE_THREEDX_CONFIGURATIONS_WARNING, configName);
        String error = String.format(PluginConstant.THREEDX_CONNECTION_SETTINGS_NOT_SET, configName);

        doReturn(configurations).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(configName).when(config1).getQualifiedName();
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(config1, stereotype, PluginConstant.URL_3DPASSPORT);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(config1, stereotype, PluginConstant.URL_3DSPACE);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(config1, stereotype, PluginConstant.URL_3DSEARCH);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(config1, stereotype, PluginConstant.CHANGE_ACTION_QUERY);

        threeDxService.update3DxConnectionInfo();

        verify(uiDomain).showWarningMessage(warning, PluginConstant.MULTIPLE_CONFIGURATIONS_WARNING);
        verify(uiDomain).showErrorMessage(error, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void update3DxConnectionInfo_multipleThreeDxConfigurationsButFirstElementNotNamed_space3dsNull() {
        List<Element> configurations = new ArrayList<>();
        Element config1 = mock(Element.class);
        Element config2 = mock(Element.class);
        configurations.add(config1);
        configurations.add(config2);
        String configName = "";
        String warning = String.format(PluginConstant.MULTIPLE_THREEDX_CONFIGURATIONS_WARNING, configName);
        String error = String.format(PluginConstant.THREEDX_CONNECTION_SETTINGS_NOT_SET, configName);

        doReturn(configurations).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config1, stereotype, PluginConstant.URL_3DPASSPORT);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(config1, stereotype, PluginConstant.URL_3DSPACE);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(config1, stereotype, PluginConstant.URL_3DSEARCH);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(config1, stereotype, PluginConstant.CHANGE_ACTION_QUERY);

        threeDxService.update3DxConnectionInfo();

        verify(uiDomain).showWarningMessage(warning, PluginConstant.MULTIPLE_CONFIGURATIONS_WARNING);
        verify(uiDomain).showErrorMessage(error, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void update3DxConnectionInfo_singleConfigurationAndFirstElementNamed_search3dsNull() {
        List<Element> configurations = new ArrayList<>();
        NamedElement config = mock(NamedElement.class);
        configurations.add(config);
        String configName = "config::Name";
        String error = String.format(PluginConstant.THREEDX_CONNECTION_SETTINGS_NOT_SET, configName);

        doReturn(configurations).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(configName).when(config).getQualifiedName();
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DPASSPORT);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSPACE);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSEARCH);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.CHANGE_ACTION_QUERY);

        threeDxService.update3DxConnectionInfo();

        verify(uiDomain).showErrorMessage(error, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void update3DxConnectionInfo_singleConfigurationAndFirstElementNamed_changeActionQueryNull() {
        List<Element> configurations = new ArrayList<>();
        NamedElement config = mock(NamedElement.class);
        configurations.add(config);
        String configName = "config::Name";
        String error = String.format(PluginConstant.THREEDX_CONNECTION_SETTINGS_NOT_SET, configName);

        doReturn(configurations).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(configName).when(config).getQualifiedName();
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DPASSPORT);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSPACE);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSEARCH);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.CHANGE_ACTION_QUERY);

        threeDxService.update3DxConnectionInfo();

        verify(uiDomain).showErrorMessage(error, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void update3DxConnectionInfo_singleConfigurationAndFirstElementNamed_pass3dsEmpty() {
        List<Element> configurations = new ArrayList<>();
        NamedElement config = mock(NamedElement.class);
        configurations.add(config);
        String configName = "config::Name";
        String error = String.format(PluginConstant.THREEDX_CONNECTION_SETTINGS_NOT_SET, configName);

        doReturn(configurations).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(configName).when(config).getQualifiedName();
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DPASSPORT);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSPACE);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSEARCH);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.CHANGE_ACTION_QUERY);

        threeDxService.update3DxConnectionInfo();

        verify(uiDomain).showErrorMessage(error, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void update3DxConnectionInfo_singleConfigurationAndFirstElementNamed_space3dsEmpty() {
        List<Element> configurations = new ArrayList<>();
        NamedElement config = mock(NamedElement.class);
        configurations.add(config);
        String configName = "config::Name";
        String error = String.format(PluginConstant.THREEDX_CONNECTION_SETTINGS_NOT_SET, configName);
        String url = "url";
        List<String> pass3dsUrl = new ArrayList<>();
        pass3dsUrl.add(url);

        doReturn(configurations).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(configName).when(config).getQualifiedName();
        doReturn(pass3dsUrl).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DPASSPORT);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSPACE);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSEARCH);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.CHANGE_ACTION_QUERY);

        threeDxService.update3DxConnectionInfo();

        verify(uiDomain).showErrorMessage(error, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void update3DxConnectionInfo_singleConfigurationAndFirstElementNamed_search3dsEmpty() {
        List<Element> configurations = new ArrayList<>();
        NamedElement config = mock(NamedElement.class);
        configurations.add(config);
        String configName = "config::Name";
        String error = String.format(PluginConstant.THREEDX_CONNECTION_SETTINGS_NOT_SET, configName);
        String url = "url";
        List<String> pass3dsUrl = new ArrayList<>();
        List<String> space3dsUrl = new ArrayList<>();
        pass3dsUrl.add(url);
        space3dsUrl.add(url);

        doReturn(configurations).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(configName).when(config).getQualifiedName();
        doReturn(pass3dsUrl).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DPASSPORT);
        doReturn(space3dsUrl).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSPACE);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSEARCH);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.CHANGE_ACTION_QUERY);

        threeDxService.update3DxConnectionInfo();

        verify(uiDomain).showErrorMessage(error, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void update3DxConnectionInfo_singleConfigurationAndFirstElementNamed_changeActionQueryEmpty() {
        List<Element> configurations = new ArrayList<>();
        NamedElement config = mock(NamedElement.class);
        configurations.add(config);
        String configName = "config::Name";
        String error = String.format(PluginConstant.THREEDX_CONNECTION_SETTINGS_NOT_SET, configName);
        String url = "url";
        List<String> pass3dsUrl = new ArrayList<>();
        List<String> space3dsUrl = new ArrayList<>();
        List<String> search3dsUrl = new ArrayList<>();
        pass3dsUrl.add(url);
        space3dsUrl.add(url);
        search3dsUrl.add(url);

        doReturn(configurations).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(configName).when(config).getQualifiedName();
        doReturn(pass3dsUrl).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DPASSPORT);
        doReturn(space3dsUrl).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSPACE);
        doReturn(search3dsUrl).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSEARCH);
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.CHANGE_ACTION_QUERY);

        threeDxService.update3DxConnectionInfo();

        verify(uiDomain).showErrorMessage(error, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
        verify(connectionInfo, never()).setInfo(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void update3DxConnectionInfo_singleConfigurationAndFirstElementNamed_allNeededValuesFound() {
        List<Element> configurations = new ArrayList<>();
        NamedElement config = mock(NamedElement.class);
        configurations.add(config);
        String configName = "config::Name";
        String error = String.format(PluginConstant.THREEDX_CONNECTION_SETTINGS_NOT_SET, configName);
        String url = "url";
        List<String> pass3dsUrl = new ArrayList<>();
        List<String> space3dsUrl = new ArrayList<>();
        List<String> search3dsUrl = new ArrayList<>();
        pass3dsUrl.add(url);
        space3dsUrl.add(url);
        search3dsUrl.add(url);
        List<String> changeActionQuery = new ArrayList<>();
        String query = "query";
        changeActionQuery.add(query);

        doReturn(configurations).when(apiDomain).getStereotypedElements(stereotype);
        doReturn(configName).when(config).getQualifiedName();
        doReturn(pass3dsUrl).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DPASSPORT);
        doReturn(space3dsUrl).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSPACE);
        doReturn(search3dsUrl).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.URL_3DSEARCH);
        doReturn(changeActionQuery).when(apiDomain).getStereotypePropertyValueAsString(config, stereotype, PluginConstant.CHANGE_ACTION_QUERY);

        threeDxService.update3DxConnectionInfo();

        verify(connectionInfo).setInfo(url, url, url, query);
        verify(uiDomain, never()).showErrorMessage(error, PluginConstant.THREEDX_CONNECTION_SETTINGS_ERROR);
    }

    @Test
    public void getChangeAction_NotLoggedInToSpace() {
        String id = "id";
        String error = PluginConstant.NO_3DX_LOGIN_ESTABLISHED_OR_EXPIRED;
        try {
            doReturn(client).when(threeDxService).getOrCreateClient();
            doReturn(false).when(client).isLoggedInToSpace();
            doReturn(false).when(client).isLoggedIn();
            threeDxService.getChangeAction(id);
            fail("Exception did not occur");
        } catch (Exception e) {
            assertSame(error, e.getMessage());
        }
    }

    @Test
    public void getChangeAction_logsIntoSpace() {
        JsonElement jsonElement = spy(new JsonObject());
        String id = "id";
        String endpoint = String.format(PluginConstant.THREEDX_CHANGE_ACTION_ENDPOINT, id);
        String serverURL = "serverUrl";
        String credentials = "role.org.collabSpace";
        Map<String, String> headers = new HashMap<>();
        headers.put("SecurityContext", credentials);
        try {
            doReturn(client).when(threeDxService).getOrCreateClient();
            doReturn(false).when(client).isLoggedInToSpace();
            doReturn(true).when(client).isLoggedIn();
            when(client.getPreferredCredentials()).thenReturn(credentials);
            when(client.get3DSpaceServer()).thenReturn(serverURL);
            doReturn(jsonElement).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(serverURL, "GET", endpoint,
                null, null, null, headers, PluginConstant.FETCHING_CHANGE_ACTION, true, 0);

            assertSame(jsonElement.getAsJsonObject(), threeDxService.getChangeAction(id));
            verify(client).loginToSpace();
        } catch (Exception e) {
            fail("Exception occurred");
        }
    }

    @Test
    public void getChangeAction_isLoggedInToSpaceTrue() {
        JsonElement jsonElement = spy(new JsonObject());
        String id = "id";
        String endpoint = String.format(PluginConstant.THREEDX_CHANGE_ACTION_ENDPOINT, id);
        String serverURL = "serverUrl";
        String credentials = "role.org.collabSpace";
        Map<String, String> headers = new HashMap<>();
        headers.put("SecurityContext", credentials);
        try {
            doReturn(client).when(threeDxService).getOrCreateClient();
            doReturn(true).when(client).isLoggedInToSpace();
            when(client.getPreferredCredentials()).thenReturn(credentials);
            when(client.get3DSpaceServer()).thenReturn(serverURL);
            doReturn(jsonElement).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(serverURL, "GET", endpoint,
                null, null, null, headers, PluginConstant.FETCHING_CHANGE_ACTION, true, 0);

            assertSame(jsonElement.getAsJsonObject(), threeDxService.getChangeAction(id));
            verify(client, never()).isLoggedIn();
            verify(client, never()).loginToSpace();
        } catch (Exception e) {
            fail("Exception occurred");
        }
    }

    @Test
    public void getChangeAction_genericExceptionPassedFromModalRestHandler() {
        String id = "id";
        String endpoint = String.format(PluginConstant.THREEDX_CHANGE_ACTION_ENDPOINT, id);
        String serverURL = "serverUrl";
        String credentials = "role.org.collabSpace";
        Map<String, String> headers = new HashMap<>();
        headers.put("SecurityContext", credentials);
        String error = "error";
        Exception exception = spy(new Exception(error));
        try {
            doReturn(client).when(threeDxService).getOrCreateClient();
            doReturn(false).when(client).isLoggedInToSpace();
            doReturn(true).when(client).isLoggedIn();
            when(client.getPreferredCredentials()).thenReturn(credentials);
            when(client.get3DSpaceServer()).thenReturn(serverURL);
            doThrow(exception).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(serverURL, "GET", endpoint,
                null, null, null, headers, PluginConstant.FETCHING_CHANGE_ACTION, true, 0);
            threeDxService.getChangeAction(id);

            fail("Exception did not occur");
        } catch (Exception e) {
            assertSame(error, e.getMessage());
        }
    }

    @Test
    public void getChangeActions_clientNotLoggedIn() {
        String nextStart = "-1";
        int nResults = PluginConstant.MAX_RESULTS_DEFAULT_VALUE;
        try {
            doReturn(client).when(threeDxService).getOrCreateClient();
            doReturn(false).when(client).isLoggedInToSearch();
            doReturn(false).when(client).isLoggedIn();

            threeDxService.getChangeActions(nResults);

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertSame(PluginConstant.NO_3DX_LOGIN_ESTABLISHED_OR_EXPIRED, e.getMessage());
            verify(threeDxService, never()).prepareQueryJson(nextStart, nResults);
        }
    }

    @Test
    public void getChangeActions_exceptionGettingCredentials() {
        ThreeDxQueryJson queryJson = mock(ThreeDxQueryJson.class);
        String post_string = "post_data";
        String error = "error";
        String nextStart = "-1";
        int nResults = PluginConstant.MAX_RESULTS_DEFAULT_VALUE;
        clientMap.put(connectionInfo, client);
        ThreeDxIntegrationException threeDxIntegrationException = spy(new ThreeDxIntegrationException(error));
        try {
            doReturn(client).when(threeDxService).getOrCreateClient();
            doReturn(false).when(client).isLoggedInToSearch();
            doReturn(true).when(client).isLoggedIn();
            doReturn(queryJson).when(threeDxService).prepareQueryJson(nextStart, nResults);
            when(threeDxService.serializeForQuery(queryJson)).thenReturn(post_string);
            doThrow(threeDxIntegrationException).when(client).getPreferredCredentials();

            threeDxService.getChangeActions(nResults);

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertSame(error, e.getMessage());
            verify(threeDxService, never()).extractChangeActions(any());
        }
    }

    @Test
    public void getChangeActions_genericExceptionFromRestHandler() {
        ThreeDxQueryJson queryJson = mock(ThreeDxQueryJson.class);
        String post_string = "post_data";
        Map<String, String> params = new HashMap<>();
        params.put("xrequestedwith", "xmlhttprequest");
        String preferredCredentials = "preferredCredentials";
        Map<String, String> headers = new HashMap<>();
        headers.put("SecurityContext", preferredCredentials);
        String url = "url";
        String contentType = "application/json";
        byte[] post_data = post_string.getBytes();
        String nextStart = "-1";
        int nResults = PluginConstant.MAX_RESULTS_DEFAULT_VALUE;
        clientMap.put(connectionInfo, client);
        String error = "error";
        Exception exception = spy(new Exception(error));
        try {
            doReturn(client).when(threeDxService).getOrCreateClient();
            doReturn(true).when(client).isLoggedInToSearch();
            when(client.getNextStart()).thenReturn(nextStart);
            doReturn(queryJson).when(threeDxService).prepareQueryJson(nextStart, nResults);
            doReturn(post_string).when(threeDxService).serializeForQuery(queryJson);
            doReturn(preferredCredentials).when(client).getPreferredCredentials();
            doReturn(url).when(client).get3DSearchServer();
            doThrow(exception).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(url, "POST", "/federated/search",
                contentType, post_data, params, headers, PluginConstant.FETCHING_CHANGE_ACTIONS, true, 0);

            threeDxService.getChangeActions(nResults);

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertSame(error, e.getMessage());
            verify(threeDxService, never()).extractChangeActions(any());
        }
    }

    @Test
    public void getChangeActions_jsonRetrievedAndExtracted() {
        ThreeDxQueryJson queryJson = mock(ThreeDxQueryJson.class);
        String post_string = "post_data";
        Map<String, String> params = new HashMap<>();
        params.put("xrequestedwith", "xmlhttprequest");
        String preferredCredentials = "preferredCredentials";
        Map<String, String> headers = new HashMap<>();
        headers.put("SecurityContext", preferredCredentials);
        String url = "url";
        byte[] postData = post_string.getBytes(StandardCharsets.UTF_8);
        String nextStart = "-1";
        int nResults = PluginConstant.MAX_RESULTS_DEFAULT_VALUE;
        clientMap.put(connectionInfo, client);
        JsonElement json = mock(JsonElement.class);
        ThreeDxQueryResultsJson resultsJson = mock(ThreeDxQueryResultsJson.class);
        ThreeDxQueryResultsInfoJson infoJson = mock(ThreeDxQueryResultsInfoJson.class);
        List<Map<String, String>> changeActions = new ArrayList<>();

        try {
            doReturn(client).when(threeDxService).getOrCreateClient();
            doReturn(true).when(client).isLoggedInToSearch();
            when(client.getNextStart()).thenReturn(nextStart);
            doReturn(queryJson).when(threeDxService).prepareQueryJson(nextStart, nResults);
            doReturn(post_string).when(threeDxService).serializeForQuery(queryJson);
            doReturn(preferredCredentials).when(client).getPreferredCredentials();
            doReturn(url).when(client).get3DSearchServer();
            doReturn(json).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(url, HTTP_POST, THREEDX_FEDERATED_SEARCH_ENDPOINT,
                    JSON_CONTENT_TYPE, postData, params, headers, FETCHING_CHANGE_ACTIONS, true, 0);
            doReturn(resultsJson).when(threeDxService).deserializeQueryResults(json);
            when(resultsJson.getInfos()).thenReturn(infoJson);
            when(infoJson.getNextStart()).thenReturn(nextStart);
            doNothing().when(client).setNextStart(nextStart);
            doReturn(changeActions).when(threeDxService).extractChangeActions(resultsJson);

            assertSame(changeActions, threeDxService.getChangeActions(nResults));
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getChangeActions_clientNotLoggedIn_jsonRetrievedAndExtracted() {
        ThreeDxQueryJson queryJson = mock(ThreeDxQueryJson.class);
        String post_string = "post_data";
        Map<String, String> params = new HashMap<>();
        params.put("xrequestedwith", "xmlhttprequest");
        String preferredCredentials = "preferredCredentials";
        Map<String, String> headers = new HashMap<>();
        headers.put("SecurityContext", preferredCredentials);
        String url = "url";
        byte[] postData = post_string.getBytes(StandardCharsets.UTF_8);
        String nextStart = "-1";
        int nResults = PluginConstant.MAX_RESULTS_DEFAULT_VALUE;
        clientMap.put(connectionInfo, client);
        JsonElement json = mock(JsonElement.class);
        ThreeDxQueryResultsJson resultsJson = mock(ThreeDxQueryResultsJson.class);
        ThreeDxQueryResultsInfoJson infoJson = mock(ThreeDxQueryResultsInfoJson.class);
        List<Map<String, String>> changeActions = new ArrayList<>();
        try {
            doReturn(client).when(threeDxService).getOrCreateClient();
            doReturn(false).when(client).isLoggedInToSearch();
            doReturn(true).when(client).isLoggedIn();
            when(client.getNextStart()).thenReturn(nextStart);
            doReturn(queryJson).when(threeDxService).prepareQueryJson(nextStart, nResults);
            doReturn(post_string).when(threeDxService).serializeForQuery(queryJson);
            doReturn(preferredCredentials).when(client).getPreferredCredentials();
            doReturn(url).when(client).get3DSearchServer();
            doReturn(json).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(url, HTTP_POST, THREEDX_FEDERATED_SEARCH_ENDPOINT,
                    JSON_CONTENT_TYPE, postData, params, headers, FETCHING_CHANGE_ACTIONS, true, 0);
            doReturn(resultsJson).when(threeDxService).deserializeQueryResults(json);
            when(resultsJson.getInfos()).thenReturn(infoJson);
            when(infoJson.getNextStart()).thenReturn(nextStart);
            doNothing().when(client).setNextStart(nextStart);
            doReturn(changeActions).when(threeDxService).extractChangeActions(resultsJson);

            assertSame(changeActions, threeDxService.getChangeActions(nResults));
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void prepareQueryJson_nextStart_newRun() {
        List<String> predicate = new ArrayList<>();
        String predicateString = "predicate";
        predicate.add(predicateString);
        List<String> selectFile = new ArrayList<>();
        String selectFileString = "selectFile";
        predicate.add(selectFileString);
        List<String> snippets = new ArrayList<>();
        String snippetsString = "snippets";
        predicate.add(snippetsString);
        int nResults = PluginConstant.MAX_RESULTS_DEFAULT_VALUE;
        clientMap.put(connectionInfo, client);
        ThreeDxQueryJson queryJson = mock(ThreeDxQueryJson.class);

        doReturn(threeDxClientManager).when(threeDxService).getThreeDxClientManager();
        when(threeDxClientManager.isNewRun()).thenReturn(true);
        doReturn(predicate).when(threeDxService).preparePredicate();
        doReturn(selectFile).when(threeDxService).prepareSelectFile();
        doReturn(snippets).when(threeDxService).prepareSnippets();
        doReturn(queryJson).when(threeDxService).createQuery(predicate, selectFile, snippets);

        ThreeDxQueryJson result = threeDxService.prepareQueryJson(null, nResults);

        verify(queryJson, never()).setNextStart(anyString());
        verify(queryJson, times(1)).setNextStart(null);
        verify(threeDxClientManager, times(1)).setNewRun(anyBoolean());
    }

    @Test
    public void prepareQueryJson_nextStartNull() {
        List<String> predicate = new ArrayList<>();
        String predicateString = "predicate";
        predicate.add(predicateString);
        List<String> selectFile = new ArrayList<>();
        String selectFileString = "selectFile";
        predicate.add(selectFileString);
        List<String> snippets = new ArrayList<>();
        String snippetsString = "snippets";
        predicate.add(snippetsString);
        int nResults = PluginConstant.MAX_RESULTS_DEFAULT_VALUE;
        clientMap.put(connectionInfo, client);
        ThreeDxQueryJson queryJson = mock(ThreeDxQueryJson.class);

        doReturn(threeDxClientManager).when(threeDxService).getThreeDxClientManager();
        when(threeDxClientManager.isNewRun()).thenReturn(false);
        doReturn(predicate).when(threeDxService).preparePredicate();
        doReturn(selectFile).when(threeDxService).prepareSelectFile();
        doReturn(snippets).when(threeDxService).prepareSnippets();
        doReturn(queryJson).when(threeDxService).createQuery(predicate, selectFile, snippets);
        doCallRealMethod().when(queryJson).setStart(anyString());
        doCallRealMethod().when(queryJson).getStart();

        ThreeDxQueryJson result = threeDxService.prepareQueryJson(null, nResults);

        assertEquals("0", result.getStart());
        verify(queryJson, never()).setNextStart(anyString());
        verify(queryJson, times(1)).setNextStart(null);
    }

    @Test
    public void prepareQueryJson_nextStartEmpty() {
        List<String> predicate = new ArrayList<>();
        String predicateString = "predicate";
        predicate.add(predicateString);
        List<String> selectFile = new ArrayList<>();
        String selectFileString = "selectFile";
        predicate.add(selectFileString);
        List<String> snippets = new ArrayList<>();
        String snippetsString = "snippets";
        predicate.add(snippetsString);
        int nResults = PluginConstant.MAX_RESULTS_DEFAULT_VALUE;
        clientMap.put(connectionInfo, client);
        ThreeDxQueryJson queryJson = mock(ThreeDxQueryJson.class);

        doReturn(threeDxClientManager).when(threeDxService).getThreeDxClientManager();
        when(threeDxClientManager.isNewRun()).thenReturn(false);
        doReturn(predicate).when(threeDxService).preparePredicate();
        doReturn(selectFile).when(threeDxService).prepareSelectFile();
        doReturn(snippets).when(threeDxService).prepareSnippets();
        doReturn(queryJson).when(threeDxService).createQuery(predicate, selectFile, snippets);
        doCallRealMethod().when(queryJson).setStart(anyString());
        doCallRealMethod().when(queryJson).getStart();

        ThreeDxQueryJson result = threeDxService.prepareQueryJson("", nResults);

        assertEquals("0", result.getStart());
        verify(queryJson, times(1)).setStart(anyString());
        verify(queryJson, times(1)).setNextStart(null);
        verify(threeDxClientManager, times(1)).setNewRun(anyBoolean());
    }

    @Test
    public void prepareQueryJson_nextStart_notEmpty_notNewRun() {
        List<String> predicate = new ArrayList<>();
        String predicateString = "predicate";
        predicate.add(predicateString);
        List<String> selectFile = new ArrayList<>();
        String selectFileString = "selectFile";
        predicate.add(selectFileString);
        List<String> snippets = new ArrayList<>();
        String snippetsString = "snippets";
        predicate.add(snippetsString);
        int nResults = PluginConstant.MAX_RESULTS_DEFAULT_VALUE;
        clientMap.put(connectionInfo, client);
        ThreeDxQueryJson queryJson = mock(ThreeDxQueryJson.class);

        doReturn(threeDxClientManager).when(threeDxService).getThreeDxClientManager();
        when(threeDxClientManager.isNewRun()).thenReturn(false);
        doReturn(predicate).when(threeDxService).preparePredicate();
        doReturn(selectFile).when(threeDxService).prepareSelectFile();
        doReturn(snippets).when(threeDxService).prepareSnippets();
        doReturn(queryJson).when(threeDxService).createQuery(predicate, selectFile, snippets);
        doCallRealMethod().when(queryJson).setStart(anyString());
        doCallRealMethod().when(queryJson).getStart();

        ThreeDxQueryJson result = threeDxService.prepareQueryJson("test", nResults);

        verify(queryJson, times(1)).setNextStart(anyString());
        verify(queryJson, never()).setStart(anyString());
    }

    @Test
    public void prepareQueryJson_nextStartSet() {
        List<String> predicate = new ArrayList<>();
        String predicateString = "predicate";
        predicate.add(predicateString);
        List<String> selectFile = new ArrayList<>();
        String selectFileString = "selectFile";
        predicate.add(selectFileString);
        List<String> snippets = new ArrayList<>();
        String snippetsString = "snippets";
        predicate.add(snippetsString);
        String nextStart = "10";
        int nResults = 100;
        clientMap.put(connectionInfo, client);
        ThreeDxQueryJson queryJson = mock(ThreeDxQueryJson.class);

        doReturn(threeDxClientManager).when(threeDxService).getThreeDxClientManager();
        when(threeDxClientManager.isNewRun()).thenReturn(false);
        doReturn(predicate).when(threeDxService).preparePredicate();
        doReturn(selectFile).when(threeDxService).prepareSelectFile();
        doReturn(snippets).when(threeDxService).prepareSnippets();
        doReturn(queryJson).when(threeDxService).createQuery(predicate, selectFile, snippets);
        doCallRealMethod().when(queryJson).setNextStart(anyString());
        doCallRealMethod().when(queryJson).getNextStart();
        doCallRealMethod().when(queryJson).setNresults(anyInt());
        doCallRealMethod().when(queryJson).getNresults();

        ThreeDxQueryJson result = threeDxService.prepareQueryJson(nextStart, nResults);

        assertEquals(nextStart, result.getNextStart());
        assertEquals(nResults, result.getNresults());
        verify(queryJson, never()).setStart(anyString());
        verify(queryJson).setNresults(nResults);
    }

    @Test
    public void preparePredicate() {
        assertSame("ds6w:label", threeDxService.preparePredicate().get(0));
    }

    @Test
    public void prepareSelectFile() {
        assertSame("icon", threeDxService.prepareSelectFile().get(0));
    }

    @Test
    public void prepareSnippets() {
        assertSame("ds6w:snippet", threeDxService.prepareSnippets().get(0));
    }

    @Test
    public void extractChangeActions_nullDeserialize() {
        assertTrue(threeDxService.extractChangeActions(null).isEmpty());
        verify(uiDomain, never()).logError(String.format(ExceptionConstants.THREEDX_ERROR_GETTING_CHANGE_ACTIONS, anyString()));
    }

    @Test
    public void extractChangeActions_changeActionsHaveError() {
        ThreeDxQueryResultsJson queryResultsJson = mock(ThreeDxQueryResultsJson.class);
        JsonElement errorJson = mock(JsonElement.class);
        String description = "description";

        when(queryResultsJson.getError()).thenReturn(errorJson);
        when(queryResultsJson.getErrorDescription()).thenReturn(description);

        assertTrue(threeDxService.extractChangeActions(queryResultsJson).isEmpty());
        verify(uiDomain).logError(String.format(ExceptionConstants.THREEDX_ERROR_GETTING_CHANGE_ACTIONS, description));
    }

    @Test
    public void extractChangeActions_noResults_errorPresentButNoDescription() {
        ThreeDxQueryResultsJson queryResultsJson = mock(ThreeDxQueryResultsJson.class);
        JsonElement errorJson = mock(JsonElement.class);
        ThreeDxQueryResultsInfoJson infoJson = mock(ThreeDxQueryResultsInfoJson.class);

        when(queryResultsJson.getError()).thenReturn(errorJson);
        when(queryResultsJson.getErrorDescription()).thenReturn(null);
        when(queryResultsJson.getInfos()).thenReturn(infoJson);
        when(infoJson.getNresults()).thenReturn(0);

        assertTrue(threeDxService.extractChangeActions(queryResultsJson).isEmpty());
        verify(uiDomain, never()).logError(String.format(ExceptionConstants.THREEDX_ERROR_GETTING_CHANGE_ACTIONS, anyString()));
    }

    @Test
    public void extractChangeActions_noResults_errorNotPresent() {
        ThreeDxQueryResultsJson queryResultsJson = mock(ThreeDxQueryResultsJson.class);
        ThreeDxQueryResultsInfoJson infoJson = mock(ThreeDxQueryResultsInfoJson.class);

        when(queryResultsJson.getError()).thenReturn(null);
        when(queryResultsJson.getInfos()).thenReturn(infoJson);
        when(infoJson.getNresults()).thenReturn(0);

        assertTrue(threeDxService.extractChangeActions(queryResultsJson).isEmpty());
        verify(uiDomain, never()).logError(String.format(ExceptionConstants.THREEDX_ERROR_GETTING_CHANGE_ACTIONS, anyString()));
    }

    @Test
    public void extractChangeActions() {
        ThreeDxQueryResultsJson queryResultsJson = mock(ThreeDxQueryResultsJson.class);
        JsonElement errorJson = mock(JsonElement.class);
        ThreeDxQueryResultsInfoJson infoJson = mock(ThreeDxQueryResultsInfoJson.class);
        List<ThreeDxQueryResultJson> resultJsonList = new ArrayList<>();
        ThreeDxQueryResultJson resultJson = mock(ThreeDxQueryResultJson.class);
        resultJsonList.add(resultJson);
        String name = "name";
        String value = "value";
        List<Map<String, String>> expected = new ArrayList<>();
        expected.add(Map.of(name, value));

        when(queryResultsJson.getError()).thenReturn(errorJson);
        when(queryResultsJson.getErrorDescription()).thenReturn(null);
        when(queryResultsJson.getInfos()).thenReturn(infoJson);
        when(infoJson.getNresults()).thenReturn(1);
        doReturn(resultJsonList).when(queryResultsJson).getResults();
        doReturn(expected).when(threeDxService).collectResults(resultJsonList);

        List<Map<String, String>> results = threeDxService.extractChangeActions(queryResultsJson);

        assertTrue(results.get(0).containsKey(name));
        assertSame(value, results.get(0).get(name));
    }

    @Test
    public void collectResults_nullAttributes() {
        List<ThreeDxQueryResultJson> resultJsonList = new ArrayList<>();
        ThreeDxQueryResultJson resultJson = mock(ThreeDxQueryResultJson.class);
        resultJsonList.add(resultJson);

        when(resultJson.getAttributes()).thenReturn(null);
        assertTrue(threeDxService.collectResults(resultJsonList).isEmpty());
    }

    @Test
    public void collectResults_emptyAttributes() {
        List<ThreeDxQueryResultJson> resultJsonList = new ArrayList<>();
        ThreeDxQueryResultJson resultJson = mock(ThreeDxQueryResultJson.class);
        resultJsonList.add(resultJson);

        when(resultJson.getAttributes()).thenReturn(List.of());
        assertTrue(threeDxService.collectResults(resultJsonList).isEmpty());
    }

    @Test
    public void collectResults_duplicateDetected() {
        List<ThreeDxQueryResultJson> resultJsonList = new ArrayList<>();
        ThreeDxQueryResultJson resultJson = mock(ThreeDxQueryResultJson.class);
        resultJsonList.add(resultJson);
        List<ThreeDxQueryResultAttributeJson> attributes = new ArrayList<>();
        ThreeDxQueryResultAttributeJson attribute = mock(ThreeDxQueryResultAttributeJson.class);
        ThreeDxQueryResultAttributeJson dupe1 = mock(ThreeDxQueryResultAttributeJson.class);
        ThreeDxQueryResultAttributeJson dupe2 = mock(ThreeDxQueryResultAttributeJson.class);
        String name = "name";
        String value = "value";
        String dupeName = "dupeName";
        String value2 = "value2";
        String value3 = "value3";
        attributes.add(attribute);
        attributes.add(dupe1);
        attributes.add(dupe2);
        String collisionPairCombo1 = String.format(ExceptionConstants.THREEDX_KEY_COLLISION_PAIR, dupeName, value2) +
                String.format(ExceptionConstants.THREEDX_KEY_COLLISION_PAIR, dupeName, value3);
        String collisionPairCombo2 = String.format(ExceptionConstants.THREEDX_KEY_COLLISION_PAIR, dupeName, value3) +
                String.format(ExceptionConstants.THREEDX_KEY_COLLISION_PAIR, dupeName, value2);
        String possibleErrorMessage1 = String.format(ExceptionConstants.THREEDX_KEY_COLLISION, collisionPairCombo1);
        String possibleErrorMessage2 = String.format(ExceptionConstants.THREEDX_KEY_COLLISION, collisionPairCombo2);

        when(resultJson.getAttributes()).thenReturn(attributes);
        when(attribute.getName()).thenReturn(name);
        when(attribute.getValue()).thenReturn(value);
        when(dupe1.getName()).thenReturn(dupeName);
        when(dupe1.getValue()).thenReturn(value2);
        when(dupe2.getName()).thenReturn(dupeName);
        when(dupe2.getValue()).thenReturn(value3);

        List<Map<String, String>> results = threeDxService.collectResults(resultJsonList);
        assertTrue(results.get(0).containsKey(name));
        assertSame(value, results.get(0).get(name));
        // duplicates can be unordered, so in this example we verify if one of two possibilities happened
        verify(uiDomain).showErrorMessage(or(eq(possibleErrorMessage1), eq(possibleErrorMessage2)), eq(ExceptionConstants.THREEDX_KEY_COLLISION_TITLE));
    }
}
