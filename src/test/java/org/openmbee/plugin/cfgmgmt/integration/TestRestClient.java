package org.openmbee.plugin.cfgmgmt.integration;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.exception.HTTPException;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestRestClient {
	private RestClient restClient;
	private URL lastRedirectUrl;
	private String url;
	private String endpoint;
	private Map<String, String> params;
	private String paramKey;
	private String paramValue;
	private Map<String, String> headers;
	private byte[] postData;
	private InputStream inputStream;

	@Before
	public void setup() {
		restClient = spy(RestClient.class);
		url = "https://url/stuff";
		endpoint = "/resources/v1/modeler/";
		params = new HashMap<>();
		paramKey = "xrequestedwith";
		paramValue = "xmlhttprequest";
		params.put(paramKey, paramValue);
		headers = new HashMap<>();
		headers.put(ACCEPT, JSON_CONTENT_TYPE);
		postData = "post_data".getBytes();
		inputStream = mock(InputStream.class);
	}

	@Test
	public void loadUrlAsJson_hasHeaders() throws IOException, HTTPException {
		byte[] bytes = new byte[]{'a'};
		doReturn(bytes).when(restClient).loadUrl("url", "method", endpoint, "contentType",
				postData, params, headers);
		restClient.loadUrlAsJson("url", "method", endpoint, "contentType",
				postData, params, headers);
		verify(restClient).loadUrl("url", "method", endpoint, "contentType",
				postData, params, headers);
	}
	@Test
	public void loadUrlAsJson_nullHeaders() throws IOException, HTTPException {
		byte[] bytes = new byte[]{'a'};
		doReturn(bytes).when(restClient).loadUrl("url", "method", endpoint, "contentType",
				postData, params, headers);
		restClient.loadUrlAsJson("url", "method", endpoint, "contentType",
				postData, params, null);
		verify(restClient).loadUrl("url", "method", endpoint, "contentType",
				postData, params, headers);
	}

	@Test
	public void loadUrlAsByteList() throws IOException, HTTPException {
		byte[] bytes = new byte[]{'a'};
		doReturn(bytes).when(restClient).loadUrl("url", "method", endpoint, "contentType",
				postData, params, headers);

		List<Byte> result = restClient.loadUrlAsByteList("url", "method", endpoint, "contentType",
				postData, params, headers);

		assertNotNull(result);
		assertSame(bytes[0], result.get(0));
	}

	@Test
	public void loadUrl_headersNull_UnknownHostException() throws IOException, HTTPException{
		URL urlObj = mock(URL.class);
		HttpsURLConnection connection = mock(HttpsURLConnection.class);

		try {
			doReturn(urlObj).when(restClient).generateUrl(url, endpoint, params);
			doReturn(connection).when(restClient).getHttpsUrlConnection(urlObj, "POST");

			restClient.loadUrl(url, "POST", endpoint, "contentType", postData, params, null);

			fail("Expected exception did not occur");
		} catch(UnknownHostException unknownHostException) {
			verify(connection, never()).setRequestProperty(anyString(), anyString());
			assertEquals(ExceptionConstants.HEADERS_MISSING, unknownHostException.getMessage());
		}
	}

	@Test
	public void loadUrl_nullInputStream() throws IOException, HTTPException {
		URL urlObj = mock(URL.class);
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		String contentType = "contentType";
		byte[] bytes = new byte[]{'a'};

		doReturn(urlObj).when(restClient).generateUrl(url, endpoint, params);
		doReturn(connection).when(restClient).getHttpsUrlConnection(urlObj, "POST");
		doNothing().when(restClient).doGenericPost(contentType, postData, connection);
		doNothing().when(restClient).handlePostResponse(endpoint, urlObj, connection);
		doReturn(null).when(restClient).getInputStream(connection);

		byte[] result = restClient.loadUrl(url, "POST", endpoint, contentType, postData, params, headers);

		assertNotNull(result);
		assertEquals(0, result.length);
		verify(connection).setRequestProperty(ACCEPT, JSON_CONTENT_TYPE);
	}

	@Test
	public void loadUrl_urlLoaded() throws IOException, HTTPException {
		URL urlObj = mock(URL.class);
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		String contentType = "contentType";
		byte[] bytes = new byte[]{'a'};

		doReturn(urlObj).when(restClient).generateUrl(url, endpoint, params);
		doReturn(connection).when(restClient).getHttpsUrlConnection(urlObj, "POST");
		doNothing().when(restClient).doGenericPost(contentType, postData, connection);
		doNothing().when(restClient).handlePostResponse(endpoint, urlObj, connection);
		doReturn(inputStream).when(restClient).getInputStream(connection);
		doReturn(bytes).when(restClient).getBytes(inputStream);

		byte[] result = restClient.loadUrl(url, "POST", endpoint, contentType, postData, params, headers);

		assertNotNull(result);
		assertEquals(bytes[0], result[0]);
		verify(connection).setRequestProperty(ACCEPT, JSON_CONTENT_TYPE);
	}

	@Test
	public void generateUrl_nullUrl() {
		try {
			restClient.generateUrl(null, endpoint, params);

			fail("Expected exception did not occur");
		} catch(IOException e) {
			assertEquals(ExceptionConstants.NULL_URL, e.getMessage());
		}
	}

	@Test
	public void generateUrl_noParameters() throws IOException {
		URL result = restClient.generateUrl(url, endpoint, new HashMap<>());

		assertNotNull(result);
		assertEquals(url + endpoint, result.toString());
	}

	@Test
	public void generateUrl_singleParam_urlNoTrailingSlashEndpointNoStartingSlash() throws IOException {
		String paramString = "?" + paramKey + "=" + paramValue;
		endpoint = endpoint.substring(1); // removes starting slash

		URL result = restClient.generateUrl(url, endpoint, params);

		assertNotNull(result);
		assertEquals(url + SLASH + endpoint + paramString, result.toString());
	}

	@Test
	public void generateUrl_multiParam_urlHasTrailingSlashEndpointHasStartingSlash() throws IOException {
		String paramKey2 = "param Key2";
		String paramValue2 = "param\"Value2";
		String convertedParamKey2 = "param%20Key2";
		String convertedParamValue2 = "param%22Value2";
		params.put(paramKey2, paramValue2);
		String paramString = "?" + paramKey + "=" + paramValue + "&" + convertedParamKey2 + "=" + convertedParamValue2;
		url = url + SLASH;

		URL result = restClient.generateUrl(url, endpoint, params);

		assertNotNull(result);
		assertEquals(url.substring(0, url.length() - 1) + endpoint + paramString, result.toString());
	}

	@Test
	public void getHttpsUrlConnection_Non_PATCH() throws IOException {
		URL urlObject = mock(URL.class);
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		when(restClient.getConnection(urlObject)).thenReturn(connection);

		assertSame(connection, restClient.getHttpsUrlConnection(urlObject, "method"));
		verify(connection, never()).setRequestProperty(X_HTTP_METHOD_OVERRIDE, PATCH);
	}

	@Test
	public void getHttpsUrlConnection_PATCH() throws IOException {
		URL urlObject = mock(URL.class);
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		doReturn(connection).when(restClient).getConnection(urlObject);

		assertSame(connection, restClient.getHttpsUrlConnection(urlObject, PATCH));
		verify(connection).setRequestProperty(X_HTTP_METHOD_OVERRIDE, PATCH);
	}

	@Test
	public void doGenericPost_postDataNull() throws IOException {
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		String contentType = "contentType";

		try {
			restClient.doGenericPost(contentType, null, connection);
		} catch(UnknownHostException  unknownHostException) {
			assertEquals(ExceptionConstants.POSTDATA_MISSING, unknownHostException.getMessage());
		}
	}

	@Test
	public void doGenericPost_hasPostData() throws IOException {
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		String contentType = "contentType";
		OutputStream outputStream = mock(OutputStream.class);

		when(connection.getOutputStream()).thenReturn(outputStream);
		doNothing().when(outputStream).write(postData);
		doNothing().when(outputStream).flush();
		doNothing().when(outputStream).close();

		restClient.doGenericPost(contentType, postData, connection);

		verify(outputStream).write(postData);
		verify(connection).setDoOutput(true);
	}

	@Test
	public void handlePostResponse_StatusCode_307() {
		URL urlObj = mock(URL.class);
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		int responseCode = 307;
		String error = String.format("%s returned %d", endpoint, responseCode);

		try {
			when(connection.getResponseCode()).thenReturn(responseCode);

			restClient.handlePostResponse(endpoint, urlObj, connection);

			fail("Expected exception did not occur");
		} catch(Exception e) {
			assertEquals(error, e.getMessage());
		}
	}

	@Test
	public void handlePostResponse_StatusCode_101() {
		URL urlObj = mock(URL.class);
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		int responseCode = 101;
		String error = String.format("%s returned %d", endpoint, responseCode);

		try {
			when(connection.getResponseCode()).thenReturn(responseCode);

			restClient.handlePostResponse(endpoint, urlObj, connection);

			fail("Expected exception did not occur");
		} catch(Exception e) {
			assertEquals(error, e.getMessage());
		}
	}

	@Test
	public void handlePostResponse_StatusCode_200_SameUrls() throws IOException, HTTPException {
		URL urlObj = mock(URL.class);
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		int responseCode = 200;

		when(connection.getResponseCode()).thenReturn(responseCode);
		when(connection.getURL()).thenReturn(urlObj);
		when(urlObj.toString()).thenReturn(url);


		restClient.handlePostResponse(endpoint, urlObj, connection);

		verify(connection).getURL();
	}

	@Test
	public void handlePostResponse_StatusCode_200_UrlMismatch() throws IOException, HTTPException {
		URL urlObj = mock(URL.class);
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		int responseCode = 200;
		URL connectionUrl = mock(URL.class);
		String connectionUrlString = "connectionUrl";

		when(connection.getResponseCode()).thenReturn(responseCode);
		when(connection.getURL()).thenReturn(connectionUrl);
		when(urlObj.toString()).thenReturn(url);
		when(connectionUrl.toString()).thenReturn(connectionUrlString);

		restClient.handlePostResponse(endpoint, urlObj, connection);

		verify(connection, times(2)).getURL();
	}

	@Test
	public void loadUrl() throws IOException, HTTPException {
		URL urlObj = mock(URL.class);
		HttpsURLConnection connection = mock(HttpsURLConnection.class);
		doReturn(connection).when(restClient).getHttpsUrlConnection(urlObj, "method");
		doReturn(urlObj).when(restClient).generateUrl(url, endpoint, params);
		OutputStream output = mock(OutputStream.class);
		doReturn(output).when(connection).getOutputStream();
		doReturn(200).when(connection).getResponseCode();
		doReturn(urlObj).when(connection).getURL();
		doReturn(inputStream).when(restClient).getInputStream(connection);

		restClient.loadUrl(url, "method", endpoint, "contentType", postData, params, headers);

		verify(restClient).getHttpsUrlConnection(urlObj, "method");
		verify(restClient).generateUrl(url, endpoint, params);
		verify(connection).getURL();
	}

	@Test
	public void getBytes() throws IOException {
		File initialFile = new File("src/test/resources/sample.txt");
		inputStream = new FileInputStream(initialFile);
		byte[] expected = "test".getBytes();

		byte[] ioBuffer = restClient.getBytes(inputStream);

		assertNotNull(ioBuffer);
		assertTrue(ioBuffer.length > 0);
		for(int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], ioBuffer[i]);
		}
	}

	@Test
	public void getBytes_noReads() throws IOException {
		byte[] ioBuffer = restClient.getBytes(inputStream);
		assertEquals(0, ioBuffer.length);
	}

	@Test
	public void getBytes_inputStreamNull() throws IOException {
		assertNull(restClient.getBytes(null));
	}

	@Test
	public void getInputStream_response200() throws IOException {
		HttpsURLConnection connection = mock(HttpsURLConnection.class);

		when(connection.getResponseCode()).thenReturn(200);
		when(connection.getInputStream()).thenReturn(inputStream);

		InputStream result = restClient.getInputStream(connection);

		assertNotNull(result);
		assertEquals(inputStream, result);
		verify(connection, never()).getErrorStream();
	}

	@Test
	public void getInputStream_response201() throws IOException {
		HttpsURLConnection connection = mock(HttpsURLConnection.class);

		when(connection.getResponseCode()).thenReturn(201);
		when(connection.getInputStream()).thenReturn(inputStream);

		InputStream result = restClient.getInputStream(connection);

		assertNotNull(result);
		assertEquals(inputStream, result);
		verify(connection, never()).getErrorStream();
	}

	@Test
	public void getInputStream_response204() throws IOException {
		HttpsURLConnection connection = mock(HttpsURLConnection.class);

		when(connection.getResponseCode()).thenReturn(204);

		assertNull(restClient.getInputStream(connection));
		verify(connection, never()).getErrorStream();
		verify(connection, never()).getInputStream();
	}

	@Test
	public void getInputStream_response500() throws IOException {
		HttpsURLConnection connection = mock(HttpsURLConnection.class);

		when(connection.getResponseCode()).thenReturn(500);
		when(connection.getErrorStream()).thenReturn(inputStream);

		InputStream result = restClient.getInputStream(connection);

		assertNotNull(result);
		assertEquals(inputStream, result);
		verify(connection, never()).getInputStream();
	}
}