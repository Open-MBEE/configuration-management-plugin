package org.openmbee.plugin.cfgmgmt.integration;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.exception.HTTPException;
import com.google.gson.JsonElement;
import org.junit.Before;
import org.junit.Test;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestModalRestHandler {
    private ModalRestHandler modalRestHandler;
    private RestClient restClient;
    private String url;
    private String httpType;
    private String endpoint;
    private String contentType;
    private byte[] data;
    private Map<String, String> params;
    private Map<String, String> headers;

    @Before
    public void setup() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        modalRestHandler = spy(new ModalRestHandler());
        restClient = mock(RestClient.class);

        url = "https://url/stuff";
        httpType = PluginConstant.HTTP_GET;
        endpoint = "endpoint/end";
        contentType = "json";
        data = new byte[32];
        params = new HashMap<>();
        headers = new HashMap<>();

        doReturn(restClient).when(modalRestHandler).getRestClient();
    }

    @Test
    public void loadUrlAsJson_jsonReceived() {
        JsonElement jsonElement = mock(JsonElement.class);

        try {
            doReturn(jsonElement).when(restClient).loadUrlAsJson(url, httpType, endpoint, contentType, data, params, headers);

            JsonElement result = modalRestHandler.loadUrlAsJson(url, httpType, endpoint, contentType, data, params, headers);

            assertNotNull(result);
            assertEquals(jsonElement, result);
        } catch(Exception e) {
            fail("No exception expected");
        }
    }

    @Test
    public void loadUrlAsJson_errorWhileLoadingUrl() {
        String error = "error";
        HTTPException exception = spy(new HTTPException(error));

        try {
            doThrow(exception).when(restClient).loadUrlAsJson(url, httpType, endpoint, contentType, data, params, headers);

            modalRestHandler.loadUrlAsJson(url, httpType, endpoint, contentType, data, params, headers);

            fail("Expected exception did not occur");
        } catch(Exception e) {
            assertEquals(error, e.getMessage());
        }
    }
}
