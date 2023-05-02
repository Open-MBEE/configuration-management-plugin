package org.openmbee.plugin.cfgmgmt.integration;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.exception.HTTPException;
import com.google.gson.JsonElement;
import com.nomagic.ui.ProgressStatusRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ModalRestHandler {
    private final Logger logger = LoggerFactory.getLogger(ModalRestHandler.class);

    public ModalRestHandler() {}

    /**
     * Uses RestClient to load the url as Json with MagicDraw's ProgressStatusRunner in a conveniently wrapped way.
     *
     * @param url a string url
     * @param httpType an http type such as GET
     * @param endpoint the endpoint of the url
     * @param contentType describes what the data is
     * @param data used for POST rest calls
     * @param params url parameters
     * @param headers http headers
     * @param description description for the ProgressStatusRunner's user interface
     * @param allowCancel whether the ProgressStatusRunner can be cancelled or not
     * @param milliSecondsBeforeShowing amount of milliseconds before the progress of ProgressStatusRunner can be shown
     * @return a JsonElement if there are no issues
     * @throws Exception can be one of any number of exceptions that can occur during a REST call.
     */
    public JsonElement loadUrlAsJsonWithStatusRunner(String url, String httpType, String endpoint, String contentType,
            byte[] data, Map<String, String> params, Map<String, String> headers,
            String description, boolean allowCancel, int milliSecondsBeforeShowing) throws Exception {
        AtomicReference<Exception> exception = new AtomicReference<>();
        AtomicReference<JsonElement> json = new AtomicReference<>();
        ProgressStatusRunner.runWithProgressStatus(progressStatus -> {
            try {
                json.set(loadUrlAsJson(url, httpType, endpoint, contentType, data, params, headers));
            } catch (IOException | HTTPException e) {
                logger.error(String.format(ExceptionConstants.ERROR_WHILE_LOADING_URL, e.getMessage()));
                exception.set(e);
            }
        }, description, allowCancel, milliSecondsBeforeShowing);
        if (exception.get() != null) {
            throw exception.get(); // useful for passing on an exception if it is used in specific integrations
        }
        return json.get();
    }

    protected JsonElement loadUrlAsJson(String url, String httpType, String endpoint, String contentType,
            byte[] data, Map<String, String> params, Map<String, String> headers) throws HTTPException, IOException {
        try {
            return getRestClient().loadUrlAsJson(url, httpType, endpoint, contentType, data, params, headers);
        } catch (IOException | HTTPException e) {
            logger.error(String.format(ExceptionConstants.ERROR_WHILE_LOADING_URL, e.getMessage()));
            throw e;
        }
    }

    public List<Byte> loadUrlWithStatusRunner(String url, String method, String endpoint, String contentType, byte[] data,
            Map<String, String> params, Map<String, String> headers, String description, boolean allowCancel,
            int milliSecondsBeforeShowing) throws Exception {
        AtomicReference<Exception> exception = new AtomicReference<>();
        AtomicReference<List<Byte>> bytes = new AtomicReference<>();
        ProgressStatusRunner.runWithProgressStatus(progressStatus -> {
            try {
                bytes.set(loadUrlAsByteList(url, method, endpoint, contentType, data, params, headers));
            } catch (IOException | HTTPException e) {
                logger.error(String.format(ExceptionConstants.ERROR_WHILE_LOADING_URL, e.getMessage()));
                exception.set(e);
            }
        }, description, allowCancel, milliSecondsBeforeShowing);
        if(exception.get() != null) {
            throw exception.get(); // useful for passing on an exception if it is used in specific integrations
        }

        return bytes.get();
    }

    protected List<Byte> loadUrlAsByteList(String url, String method, String endpoint, String contentType, byte[] data,
            Map<String, String> params, Map<String, String> headers) throws HTTPException, IOException {
        try {
            return getRestClient().loadUrlAsByteList(url, method, endpoint, contentType, data, params, headers);
        } catch (IOException | HTTPException e) {
            logger.error(String.format(ExceptionConstants.ERROR_WHILE_LOADING_URL, e.getMessage()));
            throw e;
        }
    }

    public URL getLastRedirectedUrl() {
        return getRestClient().getLastRedirectUrl();
    }

    public int getResponseCode() {
        return getRestClient().getResponseCode();
    }

    public List<HttpCookie> getCookies() {
        return getRestClient().getCookies();
    }

    protected RestClient getRestClient() {
        return RestClient.getInstance();
    }
}
