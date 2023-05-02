package org.openmbee.plugin.cfgmgmt.integration;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.exception.HTTPException;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.UriBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants.CERTIFICATE_ERROR_MSG;
import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;

public class RestClient {
    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);
    private static int responseCode = 0;// - Response code last call
    private static URL lastRedirectUrl = null;// - On redirection
    private static RestClient instance = null;
    private static CookieManager cookieManager;

    public static RestClient getInstance() {
        if (instance == null) {
            instance = new RestClient();
            cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);
        }
        return instance;
    }

    private RestClient() {
        // Sets up a UNIQUE User Agent. java/_version will be appended at the end of the String
        System.setProperty(HTTP_AGENT, CAA_URL_LOADER);
    }

    /**
     * default getter
     *
     * @return content type of last URL loaded, null if none has been load
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * default getter
     *
     * @return Last known redirect URL, null if none has been set
     */
    public URL getLastRedirectUrl() {
        return lastRedirectUrl;
    }

    public List<HttpCookie> getCookies() {
        return cookieManager != null && cookieManager.getCookieStore() != null ?
                cookieManager.getCookieStore().getCookies() : List.of();
    }

    public JsonElement loadUrlAsJson(String strURL, String method, String endpoint, String contentType, byte[] postData,
            Map<String, String> params, Map<String, String> headers) throws IOException, HTTPException {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(ACCEPT, JSON_CONTENT_TYPE);

        byte[] bytes = loadUrl(strURL, method, endpoint, contentType, postData, params, headers);
        String encodedBytes = new String(bytes, StandardCharsets.UTF_8);
        return new JsonParser().parse(encodedBytes);
    }

    public List<Byte> loadUrlAsByteList(String url, String method, String endpoint, String contentType, byte[] postData,
            Map<String, String> params, Map<String, String> headers) throws IOException, HTTPException {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(ACCEPT, JSON_CONTENT_TYPE);
        byte[] bytes = loadUrl(url, method, endpoint, contentType, postData, params, headers);
        List<Byte> wrappedBytes = new ArrayList<>();
        for (byte aByte : bytes) {
            wrappedBytes.add(aByte); // can't use a lambda here :(
        }
        return wrappedBytes;
    }

    /**
     * Load an URL and catch the response
     *
     * @param strURL       the URL to load.
     * @param method       GET or POST
     * @param contentType mandatory when method is POST
     * @param postData    the data to send when using POST method
     * @return response body
     * @throws IOException throws IOException
     */
    protected byte[] loadUrl(String strURL, String method, String endpoint, String contentType, byte[] postData,
            Map<String, String> params, Map<String, String> headers) throws IOException, HTTPException {
        URL url = generateUrl(strURL, endpoint, params);

        // OPEN THE CONNECTION
        HttpsURLConnection connection = getHttpsUrlConnection(url, method);
        if (headers != null) {
            headers.forEach(connection::setRequestProperty);
        } else {
            throw new UnknownHostException(ExceptionConstants.HEADERS_MISSING);
        }

        // POST DATA
        if(method.equalsIgnoreCase(HTTP_POST)) {
            doGenericPost(contentType, postData, connection);
        }

        // CATCHING RESPONSE
        handlePostResponse(endpoint, url, connection);

        // RESPONSE BODY
        // Read response body content
        InputStream stream = getInputStream(connection);
        return stream != null ? getBytes(stream) : new byte[0];
    }

    /**
     * Generate an Url from strUrl endpoint and param
     */
    protected URL generateUrl(String strURL, String endpoint, Map<String, String> params) throws IOException {
        if (strURL == null) {
            throw new IOException(ExceptionConstants.NULL_URL);
        }

        if (!strURL.endsWith(SLASH) && !endpoint.startsWith(SLASH)) {
            strURL = strURL + SLASH;
        } else if (strURL.endsWith(SLASH) && endpoint.startsWith(SLASH)) {
            strURL = strURL.substring(0, strURL.length() - 1);
        }

        UriBuilder uriBuilder = UriBuilder.fromUri(strURL + endpoint);
        if(params != null && !params.isEmpty()) {
            for(Map.Entry<String, String> entry : params.entrySet()) {
                // handle case of space being converted into + sign instead of %20
                uriBuilder = uriBuilder.queryParam(entry.getKey().replace(" ", "%20"),
                        entry.getValue().replace(" ", "%20"));
            }
        }
        return new URL(uriBuilder.build().toString());
    }

    protected HttpsURLConnection getHttpsUrlConnection(URL url, String method) throws IOException {
        // Set up connection
        HttpsURLConnection connection = getConnection(url);
        // 3DSwym support automatic redirect
        connection.setInstanceFollowRedirects(true);
        // Abort the request after 15 seconds
        connection.setConnectTimeout(15000);
        if (method.equals(PATCH)) {
            connection.setRequestProperty(X_HTTP_METHOD_OVERRIDE, PATCH);
            connection.setRequestMethod(HTTP_POST);
        } else {
            // Add information to request header
            connection.setRequestMethod(method);
        }

        return connection;
    }

    protected HttpsURLConnection getConnection(URL url) throws IOException {
        return (HttpsURLConnection) url.openConnection();
    }

    protected void doGenericPost(String contentType, byte[] postData, HttpsURLConnection connection) throws IOException {
        // If there is data to send, send it!
        if (postData != null) {
            // Tell connection we are going to send data
            connection.setDoOutput(true);
            // Add POST information to request header
            connection.setRequestProperty(CONTENT_LENGTH, Integer.toString(postData.length));
            connection.setRequestProperty(CONTENT_TYPE, contentType);
            // Send data
            OutputStream output = connection.getOutputStream();
            output.write(postData);
            output.flush();
            output.close();
        } else {
            throw new UnknownHostException(ExceptionConstants.POSTDATA_MISSING);
        }
    }

    protected void handlePostResponse(String endpoint, URL url, HttpsURLConnection connection) throws IOException, HTTPException {
        responseCode = connection.getResponseCode();
        if (responseCode > 299 || responseCode < 200) {
            throw new HTTPException(String.format("%s returned %d", endpoint, responseCode));
        }

        lastRedirectUrl = null;
        if (!connection.getURL().toString().equals(url.toString())) {
            lastRedirectUrl = connection.getURL();
        }

        // RESPONSE
        // TODO do we need to manipulate header fields here? Previous code did nothing
    }

    /**
     * Get bytes from the input and return.
     */
    protected byte[] getBytes(InputStream input) throws IOException {
        byte[] ioBuffer = null;
        if (input != null) {
            int read;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ioBuffer = new byte[0x10000];
            while ((read = input.read(ioBuffer)) >= 0) {
                if (read == 0) {
                    break;
                }
                byteArrayOutputStream.write(ioBuffer, 0, read);
            }
            ioBuffer = byteArrayOutputStream.toByteArray();
        }
        return ioBuffer;
    }

    /**
     * Get the InputStream based on Connection information.
     */
    protected InputStream getInputStream(HttpsURLConnection connection) throws IOException {
        InputStream input = null;
        if (connection.getResponseCode() != 200 && connection.getResponseCode() != 201 &&
                connection.getResponseCode() != 204) {
            input = connection.getErrorStream();
        } else if (connection.getResponseCode() != 204) {
            input = connection.getInputStream(); // DELETE no content
        }
        return input;
    }

    /**
     * Wrapper used to intercept the server certificate chain even in case of error
     */
    private static class CheckingTrustManager implements X509TrustManager {
        private final X509TrustManager trustManager;

        CheckingTrustManager(X509TrustManager tm) {
            trustManager = tm;
        }

        public X509Certificate[] getAcceptedIssuers() {
            return trustManager.getAcceptedIssuers();
        }

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                trustManager.checkClientTrusted(chain, authType);
            } catch (CertificateException certificateException) {
                logger.debug(CERTIFICATE_ERROR_MSG);
                throw certificateException;
            }
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                trustManager.checkServerTrusted(chain, authType);
            } catch (CertificateException certificateException) {
                logger.debug(CERTIFICATE_ERROR_MSG);
                throw certificateException;
            }
        }
    }
}
