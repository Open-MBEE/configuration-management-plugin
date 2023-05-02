package org.openmbee.plugin.cfgmgmt.integration.twc;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.JsonConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.exception.HTTPException;
import org.openmbee.plugin.cfgmgmt.integration.ModalRestHandler;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.TwcRevisionDifferenceJson;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.TwcRoleJson;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.TwcRoleProtectedObjectJson;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.element.TwcElementJson;
import com.google.gson.*;
import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import org.mockito.Mockito;

import java.net.ConnectException;
import java.net.HttpCookie;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestTeamworkCloudService.TestTeamworkCloudServiceSingle.class,
		TestTeamworkCloudService.TestTeamworkCloudServiceRevisionDifference.class,
		TestTeamworkCloudService.TestTeamworkCloudServiceElementsAtRevision.class})
public class TestTeamworkCloudService {
	public static class TestTeamworkCloudServiceBase {
		protected TeamworkCloudService teamworkCloudService;
		protected TeamworkCloudConnectionInfo connInfo;
		protected ApiDomain apiDomain;
		protected UIDomain uiDomain;
		protected Project currentProject;
		protected IPrimaryProject primaryProject;
		protected URI uri;
		protected ModalRestHandler modalRestHandler;
		protected Map<String, List> osmcUsers;
		protected String protocol;
		protected String hostPort;
		protected String user;
		protected String password;
		protected String token;

		@Before
		public void setup() {
			teamworkCloudService = Mockito.spy(new TeamworkCloudService());
			connInfo = mock(TeamworkCloudConnectionInfo.class);
			apiDomain = mock(ApiDomain.class);
			uiDomain = mock(UIDomain.class);
			currentProject = mock(Project.class);
			primaryProject = mock(IPrimaryProject.class);
			osmcUsers = mock(HashMap.class);
			uri = mock(URI.class);
			modalRestHandler = mock(ModalRestHandler.class);

			protocol = "protocol";
			hostPort = "hostPort";
			user = "user";
			password = "password";
			token = "token";

			doReturn(currentProject).when(apiDomain).getCurrentProject();
			doReturn(primaryProject).when(currentProject).getPrimaryProject();
			doReturn(uri).when(primaryProject).getLocationURI();
			when(teamworkCloudService.getModalRestHandler()).thenReturn(modalRestHandler);
			doReturn(uiDomain).when(teamworkCloudService).getUiDomain();
		}
	}

	public static class TestTeamworkCloudServiceSingle extends TestTeamworkCloudServiceBase {
		@Test
		public void getCloudInfoFromServerLogin_nullProject() {
			doReturn(null).when(apiDomain).getCurrentProject();

			assertNull(teamworkCloudService.getCloudInfoFromServerLogin(apiDomain));
		}

		@Test
		public void getCloudInfoFromServerLogin() {
			String key = "key";
			teamworkCloudService.connections.put(key, connInfo);

			when(currentProject.getID()).thenReturn(key);

			assertEquals(connInfo, teamworkCloudService.getCloudInfoFromServerLogin(apiDomain));
		}

		@Test
		public void putEntryForConnections() {
			String key = "key";

			assertTrue(teamworkCloudService.connections.isEmpty());

			teamworkCloudService.putEntryForConnections(key, connInfo);

			assertEquals(connInfo, teamworkCloudService.connections.get(key));
		}

		@Test
		public void getConnectionInfo_noConnectionInfo() throws TWCIntegrationException {
			doReturn(null).when(teamworkCloudService).getCloudInfoFromServerLogin(apiDomain);
			doReturn(null).when(teamworkCloudService).getServerInfo(apiDomain);

			assertNull(teamworkCloudService.getConnectionInfo(apiDomain));
			verify(teamworkCloudService, never()).obtainAuthTokenWithStatusRunner(apiDomain);
		}

		@Test
		public void getConnectionInfo_bothConnectionsAvailableAndStoredInfoIsValid() throws TWCIntegrationException {
			doReturn(connInfo).when(teamworkCloudService).getCloudInfoFromServerLogin(apiDomain);
			doReturn(connInfo).when(teamworkCloudService).getServerInfo(apiDomain);
			when(connInfo.isConnectionSimilar(connInfo)).thenReturn(true);

			assertEquals(connInfo, teamworkCloudService.getConnectionInfo(apiDomain));
			verify(teamworkCloudService, never()).obtainAuthTokenWithStatusRunner(apiDomain);
		}

		@Test
		public void getConnectionInfo_bothConnectionsAvailableButStoredInvalidAndUsernameNull() throws TWCIntegrationException {
			TeamworkCloudConnectionInfo storedInfo = mock(TeamworkCloudConnectionInfo.class);
			String formattedTwc = "formattedTwc";

			doReturn(storedInfo).when(teamworkCloudService).getCloudInfoFromServerLogin(apiDomain);
			doReturn(connInfo).when(teamworkCloudService).getServerInfo(apiDomain);
			when(storedInfo.isConnectionSimilar(connInfo)).thenReturn(false);
			doReturn(token).when(teamworkCloudService).obtainAuthTokenWithStatusRunner(apiDomain);
			doNothing().when(connInfo).setToken(token);
			when(connInfo.getFormattedTwcUrl()).thenReturn(formattedTwc);
			when(connInfo.getToken()).thenReturn(token);
			doReturn(null).when(teamworkCloudService).loginToTwcAndGetUsername(formattedTwc, token);
			doNothing().when(uiDomain).logError(ExceptionConstants.TWC_USERNAME_MISMATCH);

			assertNull(teamworkCloudService.getConnectionInfo(apiDomain));
			verify(teamworkCloudService).obtainAuthTokenWithStatusRunner(apiDomain);
			verify(teamworkCloudService, never()).putEntryForConnections(anyString(), any());
		}

		@Test
		public void getConnectionInfo_storedNullAndUsernameMismatch() throws TWCIntegrationException {
			String formattedTwc = "formattedTwc";
			String user2 = "user2";

			doReturn(null).when(teamworkCloudService).getCloudInfoFromServerLogin(apiDomain);
			doReturn(connInfo).when(teamworkCloudService).getServerInfo(apiDomain);
			doReturn(token).when(teamworkCloudService).obtainAuthTokenWithStatusRunner(apiDomain);
			doNothing().when(connInfo).setToken(token);
			when(connInfo.getFormattedTwcUrl()).thenReturn(formattedTwc);
			when(connInfo.getToken()).thenReturn(token);
			doReturn(user2).when(teamworkCloudService).loginToTwcAndGetUsername(formattedTwc, token);
			doNothing().when(uiDomain).logError(ExceptionConstants.TWC_USERNAME_MISMATCH);

			assertNull(teamworkCloudService.getConnectionInfo(apiDomain));
			verify(teamworkCloudService).obtainAuthTokenWithStatusRunner(apiDomain);
			verify(teamworkCloudService, never()).putEntryForConnections(anyString(), any());
		}

		@Test
		public void getConnectionInfo_storedNullButUserMatch() throws TWCIntegrationException {
			String formattedTwc = "formattedTwc";
			String id = "id";

			doReturn(null).when(teamworkCloudService).getCloudInfoFromServerLogin(apiDomain);
			doReturn(connInfo).when(teamworkCloudService).getServerInfo(apiDomain);
			doReturn(token).when(teamworkCloudService).obtainAuthTokenWithStatusRunner(apiDomain);
			doNothing().when(connInfo).setToken(token);
			when(connInfo.getFormattedTwcUrl()).thenReturn(formattedTwc);
			when(connInfo.getToken()).thenReturn(token);
			doReturn(user).when(teamworkCloudService).loginToTwcAndGetUsername(formattedTwc, token);
			when(connInfo.getUsername()).thenReturn(user);
			when(currentProject.getID()).thenReturn(id);
			doNothing().when(teamworkCloudService).putEntryForConnections(id, connInfo);

			assertEquals(connInfo, teamworkCloudService.getConnectionInfo(apiDomain));
			verify(teamworkCloudService).obtainAuthTokenWithStatusRunner(apiDomain);
			verify(uiDomain, never()).logError(ExceptionConstants.TWC_USERNAME_MISMATCH);
			verify(teamworkCloudService).putEntryForConnections(id, connInfo);
		}

		@Test
		public void getConnectionInfo_storedNullButExceptionGettingAuthToken() throws TWCIntegrationException {
			String error = "error";
			TWCIntegrationException exception = spy(new TWCIntegrationException(error));

			doReturn(null).when(teamworkCloudService).getCloudInfoFromServerLogin(apiDomain);
			doReturn(connInfo).when(teamworkCloudService).getServerInfo(apiDomain);
			doThrow(exception).when(teamworkCloudService).obtainAuthTokenWithStatusRunner(apiDomain);

			assertNull(teamworkCloudService.getConnectionInfo(apiDomain));
			verify(uiDomain).logError(ExceptionConstants.INDETERMINATE_TWC_ROLES);
		}

		@Test
		public void getConnectionInfo_storedNullButExceptionLoggingIn() throws TWCIntegrationException {
			String formattedTwc = "formattedTwc";
			String error = "error";
			TWCIntegrationException exception = spy(new TWCIntegrationException(error));

			doReturn(null).when(teamworkCloudService).getCloudInfoFromServerLogin(apiDomain);
			doReturn(connInfo).when(teamworkCloudService).getServerInfo(apiDomain);
			doReturn(token).when(teamworkCloudService).obtainAuthTokenWithStatusRunner(apiDomain);
			doNothing().when(connInfo).setToken(token);
			when(connInfo.getFormattedTwcUrl()).thenReturn(formattedTwc);
			when(connInfo.getToken()).thenReturn(token);
			doThrow(exception).when(teamworkCloudService).loginToTwcAndGetUsername(formattedTwc, token);

			assertNull(teamworkCloudService.getConnectionInfo(apiDomain));
			verify(uiDomain).logError(ExceptionConstants.INDETERMINATE_TWC_ROLES);
		}

		@Test
		public void loginToTwcAndGetUsername_exceptionLoadingUrl() throws Exception {
			String url = "url";
			Map<String, String> authHeaders = new HashMap<>();
			String error = "error";
			Exception exception = spy(new HTTPException(error));

			doReturn(authHeaders).when(teamworkCloudService).setupTwcAuthHeaders(token);
			doThrow(exception).when(modalRestHandler).loadUrlWithStatusRunner(url, HTTP_GET, LOGIN_ENDPOINT, null,
					null, null, authHeaders, FETCH_TWC_USERNAME, true, 0);
			doNothing().when(uiDomain).logError(ExceptionConstants.ERROR_LOGGING_INTO_TWC + "\n" + error);

			try {
				teamworkCloudService.loginToTwcAndGetUsername(url, token);

				fail("Expected exception did not occur.");
			} catch (TWCIntegrationException e) {
				assertSame(error, e.getMessage());
			}
		}

		@Test
		public void loginToTwcAndGetUsername_usernameObtainedFromCookies() throws Exception {
			String url = "url";
			Map<String, String> authHeaders = new HashMap<>();
			List<HttpCookie> cookies = new ArrayList<>();
			HttpCookie cookie = mock(HttpCookie.class);
			cookies.add(cookie);
			String value = "value";

			doReturn(authHeaders).when(teamworkCloudService).setupTwcAuthHeaders(token);
			doReturn(new ArrayList<>()).when(modalRestHandler).loadUrlWithStatusRunner(url, HTTP_GET, LOGIN_ENDPOINT, null,
					null, null, authHeaders, FETCH_TWC_USERNAME, true, 0);
			when(modalRestHandler.getCookies()).thenReturn(cookies);
			when(cookie.getName()).thenReturn(TWC_USER_COOKIE);
			when(cookie.getValue()).thenReturn(value);
			try {
				assertSame(value, teamworkCloudService.loginToTwcAndGetUsername(url, token));
			} catch (TWCIntegrationException e) {
				fail("Unexpected Exception.");
			}
		}

		@Test
		public void loginToTwcAndGetUsername_usernameNotObtainedFromCookies() throws Exception {
			String url = "url";
			Map<String, String> authHeaders = new HashMap<>();
			List<HttpCookie> cookies = new ArrayList<>();
			HttpCookie cookie = mock(HttpCookie.class);
			cookies.add(cookie);

			doReturn(authHeaders).when(teamworkCloudService).setupTwcAuthHeaders(token);
			doReturn(new ArrayList<>()).when(modalRestHandler).loadUrlWithStatusRunner(url, HTTP_GET, LOGIN_ENDPOINT, null,
					null, null, authHeaders, FETCH_TWC_USERNAME, true, 0);
			when(modalRestHandler.getCookies()).thenReturn(cookies);
			when(cookie.getName()).thenReturn("name");
			doNothing().when(uiDomain).logError(ExceptionConstants.UNABLE_TO_DETERMINE_USER);
			try {
				teamworkCloudService.loginToTwcAndGetUsername(url, token);
				fail("Expected exception did not occur.");
			} catch (TWCIntegrationException e) {
				assertSame(ExceptionConstants.UNABLE_TO_DETERMINE_USER, e.getMessage());
			}
		}

		@Test
		public void logout_nullConnectionInformation() {
			doReturn(null).when(teamworkCloudService).getCloudInfoFromServerLogin(any());

			teamworkCloudService.logout(apiDomain);

			verify(teamworkCloudService, never()).logoutFromTwc(any());
		}

		@Test
		public void logout_properConnection_butTokenNull() {
			doReturn(connInfo).when(teamworkCloudService).getCloudInfoFromServerLogin(any());

			teamworkCloudService.logout(apiDomain);

			verify(teamworkCloudService, never()).logoutFromTwc(connInfo);
		}

		@Test
		public void logout_successfulLogout() {
			TeamworkCloudConnectionInfo connectionInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			connectionInfo.setToken("token");
			doReturn(connectionInfo).when(teamworkCloudService).getCloudInfoFromServerLogin(any());

			teamworkCloudService.logout(apiDomain);

			verify(teamworkCloudService).logoutFromTwc(connectionInfo);
		}

		@Test
		public void logoutFromTwc_exceptionDuringLogoutAttempt() {
			TeamworkCloudConnectionInfo connectionInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			Map<String, String> authHeaders = new HashMap<>();
			String error = "error";
			Exception exception = spy(new HTTPException(error));

			doReturn(authHeaders).when(teamworkCloudService).setupTwcAuthHeaders(token);

			try {
				doThrow(exception).when(modalRestHandler).loadUrlWithStatusRunner(anyString(), anyString(), anyString(),any(),
						any(), any(), any(), anyString(), anyBoolean(), anyInt());
				doNothing().when(uiDomain).logError(ExceptionConstants.ERROR_LOGGING_OUT_OF_TWC + "\n" + error);

				teamworkCloudService.logoutFromTwc(connectionInfo);

				verify(modalRestHandler).loadUrlWithStatusRunner(anyString(), anyString(), anyString(),any(),
						any(), any(), any(), anyString(), anyBoolean(), anyInt());
			} catch (Exception e) {
				fail("Unexpected Exception.");
			}
		}

		@Test
		public void logoutFromTwc_noIssues() {
			TeamworkCloudConnectionInfo connectionInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			Map<String, String> authHeaders = new HashMap<>();

			doReturn(authHeaders).when(teamworkCloudService).setupTwcAuthHeaders(token);
			try {
				doReturn(new ArrayList<>()).when(modalRestHandler).loadUrlWithStatusRunner(anyString(), anyString(), anyString(),any(),
						any(), any(), any(), anyString(), anyBoolean(), anyInt());

				teamworkCloudService.logoutFromTwc(connectionInfo);

				verify(modalRestHandler).loadUrlWithStatusRunner(anyString(), anyString(), anyString(),any(),
						any(), any(), any(), anyString(), anyBoolean(), anyInt());
			} catch (Exception e) {
				fail("Unexpected Exception.");
			}
		}

		@Test
		public void obtainAuthTokenWithStatusRunner_noException() throws TWCIntegrationException {
			AtomicReference<TWCIntegrationException> exceptionReference = spy(new AtomicReference<>());
			AtomicReference<String> tokenReference = spy(new AtomicReference<>());
			tokenReference.set(token);
			RunnableWithProgress runnableWithProgress = mock(RunnableWithProgress.class);

			doReturn(exceptionReference).when(teamworkCloudService).getAtomicReferenceForTwcException();
			doReturn(tokenReference).when(teamworkCloudService).getAtomicReferenceForToken();
			doReturn(runnableWithProgress).when(teamworkCloudService).setupRunnable(apiDomain, exceptionReference, tokenReference);
			doNothing().when(teamworkCloudService).runWithProgressStatus(runnableWithProgress);
			assertEquals(token, teamworkCloudService.obtainAuthTokenWithStatusRunner(apiDomain));
		}

		@Test
		public void obtainAuthTokenWithStatusRunner_exceptionOccurredDuringRun() {
			AtomicReference<TWCIntegrationException> exceptionReference = spy(new AtomicReference<>());
			AtomicReference<String> tokenReference = spy(new AtomicReference<>());
			RunnableWithProgress runnableWithProgress = mock(RunnableWithProgress.class);
			String error = "error";
			TWCIntegrationException twcIntegrationException = spy(new TWCIntegrationException(error));
			exceptionReference.set(twcIntegrationException);

			doReturn(exceptionReference).when(teamworkCloudService).getAtomicReferenceForTwcException();
			doReturn(tokenReference).when(teamworkCloudService).getAtomicReferenceForToken();
			try {
				doReturn(runnableWithProgress).when(teamworkCloudService).setupRunnable(apiDomain, exceptionReference, tokenReference);
				doNothing().when(teamworkCloudService).runWithProgressStatus(runnableWithProgress);

				teamworkCloudService.obtainAuthTokenWithStatusRunner(apiDomain);

				fail("Expected exception did not occur");
			} catch (Exception e) {
				assertEquals(error, e.getMessage());
			}
		}

		@Test
		public void setupRunnable_noException() throws Exception {
			AtomicReference<TWCIntegrationException> exceptionReference = spy(new AtomicReference<>());
			AtomicReference<String> tokenReference = spy(new AtomicReference<>());
			ProgressStatus progressStatus = spy(ProgressStatus.class);


			doReturn(token).when(teamworkCloudService).obtainAuthToken(apiDomain);

			teamworkCloudService.setupRunnable(apiDomain, exceptionReference, tokenReference).run(progressStatus);

			assertEquals(token, tokenReference.get());
		}

		@Test
		public void setupRunnable_exceptionWouldHappen() throws Exception {
			AtomicReference<TWCIntegrationException> exceptionReference = spy(new AtomicReference<>());
			AtomicReference<String> tokenReference = spy(new AtomicReference<>());
			ProgressStatus progressStatus = spy(ProgressStatus.class);
			String error = "error";
			TWCIntegrationException twcIntegrationException = spy(new TWCIntegrationException(error));


			doThrow(twcIntegrationException).when(teamworkCloudService).obtainAuthToken(apiDomain);

			teamworkCloudService.setupRunnable(apiDomain, exceptionReference, tokenReference).run(progressStatus);

			assertEquals(twcIntegrationException, exceptionReference.get());
			assertEquals(error, exceptionReference.get().getMessage());
		}

		@Test
		public void obtainAuthToken() {
			String auth = "token";

			try{
				doReturn(auth).when(apiDomain).getSecondaryAuthToken(MAGICDRAW);
				String result = teamworkCloudService.obtainAuthToken(apiDomain);
				assertEquals(auth, result);
			} catch (Exception e) {
				fail("Unexpected Exception.");
			}
		}

		@Test
		public void obtainAuthToken_ExceptionMessageNotNull() {
			String error = "error";
			ConnectException exception = spy(new ConnectException(error));

			try{
				doThrow(exception).when(apiDomain).getSecondaryAuthToken(MAGICDRAW);
				doNothing().when(uiDomain).logError(ExceptionConstants.ERROR_WHILE_CONNECTING_TO_TWC + "\n" + error);

				teamworkCloudService.obtainAuthToken(apiDomain);

				fail("Expected exception did not occur");
			} catch (Exception e) {
				assertEquals(error, e.getMessage());
			}
		}

		@Test
		public void obtainAuthToken_ExceptionMessageNull() {
			ConnectException exception = spy(new ConnectException(null));

			try{
				doThrow(exception).when(apiDomain).getSecondaryAuthToken(MAGICDRAW);
				doNothing().when(uiDomain).logError(ExceptionConstants.ERROR_WHILE_CONNECTING_TO_TWC);

				teamworkCloudService.obtainAuthToken(apiDomain);

				fail("Expected exception did not occur");
			} catch (Exception e) {
				assertEquals(exception.getClass().getName(), e.getMessage());
			}
		}

		@Test
		public void getRoleId() {
			assertEquals("a020ca32-22a2-4c90-8513-f6b9c4ea8513",
					teamworkCloudService.getRoleId("Resource Locks Administrator"));
		}

		@Test
		public void getRoleId_noOsmcAdminRoles() {
			doReturn(null).when(teamworkCloudService).getOsmcAdminRoles();

			assertNull(teamworkCloudService.getRoleId("roleName"));
		}

		@Test
		public void getRoleId_InvalidRole() {
			assertNull(teamworkCloudService.getRoleId("Invalid Role"));
		}

		@Test
		public void getRoleId_NullJsonElement(){
			try{
				JsonArray osmcAdminRoles = mock(JsonArray.class);
				Iterator<JsonElement> jsonElementIterator = mock(Iterator.class);

				doReturn(osmcAdminRoles).when(teamworkCloudService).getOsmcAdminRoles();
				doReturn(jsonElementIterator).when(osmcAdminRoles).iterator();
				doReturn(null).when(jsonElementIterator).next();

				String roleId = teamworkCloudService.getRoleId("roleName");
				assertNull(roleId);
			}catch(Exception exception){
				fail("Unexpected exception");
			}
		}

		@Test
		public void getGsonBuilder_notYetInitialized() {
			assertNull(teamworkCloudService.regularGsonBuilder);

			GsonBuilder result = teamworkCloudService.getGsonBuilder();

			assertNotNull(teamworkCloudService.regularGsonBuilder);
			assertEquals(teamworkCloudService.regularGsonBuilder, result);
		}

		@Test
		public void getGsonBuilder_alreadyInitialized() {
			teamworkCloudService.regularGsonBuilder = mock(GsonBuilder.class);

			assertNotNull(teamworkCloudService.regularGsonBuilder);
			assertEquals(teamworkCloudService.regularGsonBuilder, teamworkCloudService.getGsonBuilder());
		}

		@Test
		public void getGsonWithoutExposeAnnotations_notYetInitialized() {
			assertNull(teamworkCloudService.onlyExposedFieldsGson);

			Gson result = teamworkCloudService.getGsonWithoutExposeAnnotations();

			assertNotNull(teamworkCloudService.onlyExposedFieldsGson);
			assertEquals(teamworkCloudService.onlyExposedFieldsGson, result);
		}

		@Test
		public void getGsonWithoutExposeAnnotations_alreadyInitialized() {
			teamworkCloudService.onlyExposedFieldsGson = mock(Gson.class);

			assertNotNull(teamworkCloudService.onlyExposedFieldsGson);
			assertEquals(teamworkCloudService.onlyExposedFieldsGson,
					teamworkCloudService.getGsonWithoutExposeAnnotations());
		}

		@Test
		public void hasRole_Null() {
			List<String> roles = new ArrayList<>();
			try {
				doReturn(null).when(teamworkCloudService).getConnectionInfo(apiDomain);
				assertFalse(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected.");
			}
		}

		@Test
		public void hasRole_nullUserName() {
			List<String> roles = new ArrayList<>();
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, null, protocol));
			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doNothing().when(uiDomain).logError(ExceptionConstants.UNABLE_TO_DETERMINE_USER);

				teamworkCloudService.hasRole(apiDomain, roles);

				fail("Expected TWCIntegration exception");
			} catch (TWCIntegrationException e) {
				assertEquals(ExceptionConstants.UNABLE_TO_DETERMINE_USER, e.getMessage());
			}
		}

		@Test
		public void hasRole_EmptyUserName() {
			List<String> roles = new ArrayList<>();
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, "", protocol));
			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doNothing().when(uiDomain).logError(ExceptionConstants.UNABLE_TO_DETERMINE_USER);

				teamworkCloudService.hasRole(apiDomain, roles);

				fail("Expected TWCIntegration exception");
			} catch (TWCIntegrationException e) {
				assertEquals(ExceptionConstants.UNABLE_TO_DETERMINE_USER, e.getMessage());
			}
		}

		@Test
		public void hasRole_invalidURI() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			List<String> roles = new ArrayList<>();
			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				doReturn(osmcUsers).when(teamworkCloudService).getOsmcUsers();
				when(uri.toString()).thenReturn("Invalid_URI");

				assertFalse(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected.");
			}
		}

		@Test
		public void hasRole_emptyRoles() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			List<String> roles = new ArrayList<>();
			Map<String, List<String>> improperOsmcUsers = new HashMap<>();

			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				doReturn(improperOsmcUsers).when(teamworkCloudService).getOsmcUsers();
				when(uri.toString()).thenReturn("twcloud:/domain/page");
				assertFalse(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected.");
			}
		}

		@Test
		public void hasRole_nullProjectUri() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			List<String> roles = new ArrayList<>();
			Map<String, List<String>> improperOsmcUsers = new HashMap<>();

			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				doReturn(improperOsmcUsers).when(teamworkCloudService).getOsmcUsers();
				when(uri.toString()).thenReturn("twcloud:/domain/page");
				doReturn(null).when(teamworkCloudService).getCurrentProjectUri(apiDomain);
				doNothing().when(uiDomain).logError(ExceptionConstants.UNABLE_TO_FIND_PROJECT_URI);

				teamworkCloudService.hasRole(apiDomain, roles);

				fail("Expected TWCIntegration exception");
			} catch (TWCIntegrationException e) {
				assertEquals(ExceptionConstants.UNABLE_TO_FIND_PROJECT_URI, e.getMessage());
			}
		}

		@Test
		public void hasRole_cannotGetProjectIdFromUri() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			List<String> roles = new ArrayList<>();
			String projectUri = "twcloud:/domain/page";
			Map<String, List<String>> improperOsmcUsers = new HashMap<>();

			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				doReturn(improperOsmcUsers).when(teamworkCloudService).getOsmcUsers();
				when(uri.toString()).thenReturn(projectUri);
				doReturn(null).when(teamworkCloudService).getProjectIdFromUri(projectUri);

				assertFalse(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected");
			}
		}

		@Test
		public void hasRole_informationAlreadyStored() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			String role = "admin";
			List<String> roles = new ArrayList<>();
			roles.add(role);
			String projectUri = "twcloud:/domain/page";
			String roleId = "roleId";
			String key = connInfo.getUsername() + AT_THE_RATE_CHARACTER + connInfo.getUrl();
			Map<String, Boolean> userRoleMap = new HashMap<>();
			userRoleMap.put(role, true);
			Map<String, List<String>> improperOsmcUsers = new HashMap<>();
			List<String> projectList = new ArrayList<>();
			projectList.add("domain");
			improperOsmcUsers.put(roleId, projectList);

			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				doReturn(improperOsmcUsers).when(teamworkCloudService).getOsmcUsers();
				doReturn(userRoleMap).when(teamworkCloudService).getUserRoleMap(key);
				when(uri.toString()).thenReturn(projectUri);
				doReturn(roleId).when(teamworkCloudService).getRoleId("admin");

				assertTrue(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected");
			}
		}

		@Test
		public void hasRole_nullRoleID() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			List<String> roles = new ArrayList<>();
			roles.add("admin");
			String projectUri = "twcloud:/domain/page";
			Map<String, List<String>> improperOsmcUsers = new HashMap<>();

			try {
				doReturn(improperOsmcUsers).when(teamworkCloudService).getOsmcUsers();
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				when(uri.toString()).thenReturn(projectUri);
				doReturn(null).when(teamworkCloudService).getRoleId("admin");

				assertFalse(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected.");
			}
		}

		@Test
		public void hasRole_nullRoleID_differentUsername() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			List<String> roles = new ArrayList<>();
			roles.add("admin");
			String projectUri = "twcloud:/domain/page";
			Map<String, List<String>> improperOsmcUsers = new HashMap<>();

			try {
				doReturn(improperOsmcUsers).when(teamworkCloudService).getOsmcUsers();
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn("differentUsername").when(teamworkCloudService).getUsername();
				doReturn(osmcUsers).when(teamworkCloudService).getOsmcUsers(connInfo);
				when(uri.toString()).thenReturn(projectUri);
				doReturn(null).when(teamworkCloudService).getRoleId("admin");

				assertFalse(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected.");
			}
		}

		@Test
		public void hasRole_nullRoleID_nullOsmcUsers() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			List<String> roles = new ArrayList<>();
			roles.add("admin");
			String projectUri = "twcloud:/domain/page";

			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				doReturn(null).when(teamworkCloudService).getOsmcUsers();
				doReturn(osmcUsers).when(teamworkCloudService).getOsmcUsers(connInfo);
				when(uri.toString()).thenReturn(projectUri);
				doReturn(null).when(teamworkCloudService).getRoleId("admin");

				assertFalse(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected.");
			}
		}


		@Test
		public void hasRole_exceptionDuringUrlLoad() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			connInfo.setToken(token);
			List<String> roles = new ArrayList<>();
			roles.add("admin");
			String projectUri = "twcloud:/domain/page";
			String roleId = "roleId";
			String error = "error";
			TWCIntegrationException twcIntegrationException = spy(new TWCIntegrationException(error));

			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				doReturn(null).when(teamworkCloudService).getOsmcUsers();
				when(uri.toString()).thenReturn(projectUri);
				doReturn(roleId).when(teamworkCloudService).getRoleId("admin");
				doThrow(twcIntegrationException).when(teamworkCloudService).getOsmcUsers(connInfo);

				teamworkCloudService.hasRole(apiDomain, roles);

				fail("Expected exception did not occur");
			} catch (TWCIntegrationException e) {
				assertEquals(error, e.getMessage());
			}
		}

		@Test
		public void hasRole_nullJsonReceived() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			connInfo.setToken(token);
			List<String> roles = new ArrayList<>();
			roles.add("admin");
			String projectUri = "twcloud:/domain/page";
			String roleId = "roleId";
			Map<String, List<String>> improperOsmcUsers = new HashMap<>();

			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				doReturn(improperOsmcUsers).when(teamworkCloudService).getOsmcUsers();
				when(uri.toString()).thenReturn(projectUri);
				doReturn(roleId).when(teamworkCloudService).getRoleId("admin");
				doReturn(null).when(teamworkCloudService).useModalRestHandlerForOsmcRoles(connInfo);

				assertFalse(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected");
			}
		}

		@Test
		public void hasRole_jsonLacksUserArray() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			connInfo.setToken(token);
			List<String> roles = new ArrayList<>();
			roles.add("admin");
			String projectUri = "twcloud:/domain/page";
			String roleId = "roleId";
			JsonElement jsonElement = mock(JsonElement.class);
			JsonObject jsonObject = mock(JsonObject.class);
			Map<String, List<String>> improperOsmcUsers = new HashMap<>();

			try {
				doReturn(improperOsmcUsers).when(teamworkCloudService).getOsmcUsers();
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				when(uri.toString()).thenReturn(projectUri);
				doReturn(roleId).when(teamworkCloudService).getRoleId("admin");
				doReturn(jsonElement).when(teamworkCloudService).useModalRestHandlerForOsmcRoles(connInfo);
				doReturn(jsonObject).when(jsonElement).getAsJsonObject();
				doReturn(null).when(jsonObject).getAsJsonArray(USER);

				assertFalse(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected");
			}
		}

		@Test
		public void hasRole_jsonHasUserArrayButItsEmpty() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			connInfo.setToken(token);
			List<String> roles = new ArrayList<>();
			roles.add("admin");
			String projectUri = "twcloud:/domain/page";
			String roleId = "roleId";
			JsonElement jsonElement = mock(JsonElement.class);
			JsonObject jsonObject = mock(JsonObject.class);
			JsonArray jsonArray = spy(new JsonArray());
			Map<String, List<String>> improperOsmcUsers = new HashMap<>();

			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				doReturn(improperOsmcUsers).when(teamworkCloudService).getOsmcUsers();
				when(uri.toString()).thenReturn(projectUri);
				doReturn(roleId).when(teamworkCloudService).getRoleId("admin");
				doReturn(jsonElement).when(teamworkCloudService).useModalRestHandlerForOsmcRoles(connInfo);
				doReturn(jsonObject).when(jsonElement).getAsJsonObject();
				doReturn(jsonArray).when(jsonObject).getAsJsonArray(USER);

				assertFalse(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected");
			}
		}

		@Test
		public void hasRole_jsonHasUserArrayWithMatch() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			connInfo.setToken(token);
			List<String> roles = new ArrayList<>();
			roles.add("admin");
			String projectUri = "twcloud:/domain/page";
			String roleId = "roleId";
			JsonElement jsonElement = mock(JsonElement.class);
			JsonObject jsonObject = mock(JsonObject.class);
			JsonArray jsonArray = spy(new JsonArray());
			JsonElement userElement = mock(JsonElement.class);
			jsonArray.add(userElement);
			Map<String, List<String>> improperOsmcUsers = new HashMap<>();
			List<String> projectList = new ArrayList<>();
			projectList.add("domain");
			improperOsmcUsers.put(roleId, projectList);

			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn(connInfo.getUsername()).when(teamworkCloudService).getUsername();
				doReturn(improperOsmcUsers).when(teamworkCloudService).getOsmcUsers();
				when(uri.toString()).thenReturn(projectUri);
				doReturn(roleId).when(teamworkCloudService).getRoleId("admin");
				doReturn(jsonElement).when(teamworkCloudService).useModalRestHandlerForOsmcRoles(connInfo);
				doReturn(jsonObject).when(jsonElement).getAsJsonObject();
				doReturn(jsonArray).when(jsonObject).getAsJsonArray(USER);
				doReturn(user).when(userElement).getAsString();

				assertTrue(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected");
			}
		}

		@Test
		public void hasRole_getOsmcUsers_nullOsmcUsers_differentUsername() {
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, protocol));
			connInfo.setToken(token);
			List<String> roles = new ArrayList<>();
			roles.add("admin");
			String roleID = "adminID";
			String roleName = "admin";
			String projectUri = "twcloud:/domain/page";
			Map<String, List<String>> properOsmcUsers = new HashMap<>();
			List<String> projectList = new ArrayList<>();
			projectList.add("domain");
			properOsmcUsers.put(roleID, projectList);

			try {
				doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
				doReturn("differentUsername").when(teamworkCloudService).getUsername();
				doReturn(properOsmcUsers).when(teamworkCloudService).getOsmcUsers(connInfo);
				when(uri.toString()).thenReturn(projectUri);
				doReturn(roleID).when(teamworkCloudService).getRoleId(roleName);

				assertTrue(teamworkCloudService.hasRole(apiDomain, roles));
			} catch (TWCIntegrationException e) {
				fail("No exception expected.");
			}
		}

		@Test
		public void getOsmcUsers() throws TWCIntegrationException {
			JsonElement json = mock(JsonElement.class);
			GsonBuilder builder = mock(GsonBuilder.class);
			Gson gson = mock(Gson.class);
			TwcRoleJson roleJson = spy(new TwcRoleJson());
			String roleId = "roleId";
			roleJson.setRoleID(roleId);
			List<TwcRoleProtectedObjectJson> protectedObjects = new ArrayList<>();
			TwcRoleProtectedObjectJson protectedObject = spy(new TwcRoleProtectedObjectJson());
			String resourceId = "resourceId";
			protectedObject.setId(resourceId);
			protectedObjects.add(protectedObject);
			roleJson.setProtectedObjects(protectedObjects);
			TwcRoleJson[] roles = new TwcRoleJson[]{roleJson};
			Map<String, List<String>> expected = new HashMap<>();
			List<String> expectedResources = new ArrayList<>();
			expectedResources.add(resourceId);
			expected.put(roleId, expectedResources);

			doReturn(json).when(teamworkCloudService).useModalRestHandlerForOsmcRoles(connInfo);
			doReturn(builder).when(teamworkCloudService).getGsonBuilder();
			doReturn(gson).when(builder).create();
			doReturn(roles).when(gson).fromJson(json, TwcRoleJson[].class);

			Map<String, List<String>> results = teamworkCloudService.getOsmcUsers(connInfo);

			assertNotNull(results);
			expected.forEach((k, v) -> {
				assertTrue(results.containsKey(k));
				expected.get(k).forEach(r -> assertTrue(results.get(k).contains(r)));
			});
		}

		@Test
		public void getProjectIdFromUri_doesNotMatchPattern() {
			String projectUri = "fakeUri";

			assertNull(teamworkCloudService.getProjectIdFromUri(projectUri));
		}

		@Test
		public void getProjectIdFromUri_matchesPattern() {
			String projectUri = "twcloud:/a433baef-9dfa-4846-87bd-e5bbbc1a9721/7285a3cb-8842-4c9c-99aa-a8f5360e7bb2";
			String projectId = "a433baef-9dfa-4846-87bd-e5bbbc1a9721";

			assertEquals(projectId, teamworkCloudService.getProjectIdFromUri(projectUri));
		}

		@Test
		public void getProjectIdFromCurrentUri_currentUriNull() {
			when(apiDomain.getEsiUriFromCurrentProject()).thenReturn(null);

			assertNull(teamworkCloudService.getProjectIdFromCurrentUri(apiDomain));
		}

		@Test
		public void getProjectIdFromCurrentUri_currentUriValid() {
			String uri = "uri";

			when(apiDomain.getEsiUriFromCurrentProject()).thenReturn(uri);
			doReturn(uri).when(teamworkCloudService).getProjectIdFromUri(uri);

			assertEquals(uri, teamworkCloudService.getProjectIdFromCurrentUri(apiDomain));
		}

		@Test
		public void getRevisionDifference_nullConnectionInfo() {
			String projectId = "projectId";
			String sourceRevision = "1";
			String targetRevision = "2";

			doReturn(null).when(teamworkCloudService).getConnectionInfo(apiDomain);

			try {
				assertNull(teamworkCloudService.getRevisionDifference(apiDomain, projectId, sourceRevision, targetRevision));
			} catch (TWCIntegrationException e) {
				fail("Unexpected exception occurred");
			}
		}

		@Test
		public void getRevisionDifference_exceptionDuringRestCall() {
			String projectId = "projectId";
			String sourceRevision = "1";
			String targetRevision = "2";
			String error = "error";
			TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

			doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);

			try {
				doThrow(integrationException).when(teamworkCloudService).useModalRestHandlerForRevisionDifference(connInfo, projectId, sourceRevision, targetRevision);

				teamworkCloudService.getRevisionDifference(apiDomain, projectId, sourceRevision, targetRevision);
				fail("Expected exception did not occur");
			} catch (TWCIntegrationException e) {
				assertEquals(error, e.getMessage());
			}
		}

		@Test
		public void getRevisionDifference_validJson() {
			String projectId = "projectId";
			String sourceRevision = "1";
			String targetRevision = "2";
			JsonElement data = mock(JsonElement.class);
			GsonBuilder gsonBuilder = mock(GsonBuilder.class);
			Gson gson = mock(Gson.class);
			TwcRevisionDifferenceJson revisionDifferenceJson = mock(TwcRevisionDifferenceJson.class);

			doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);

			try {
				doReturn(data).when(teamworkCloudService).useModalRestHandlerForRevisionDifference(connInfo, projectId, sourceRevision, targetRevision);
				doReturn(gsonBuilder).when(teamworkCloudService).getGsonBuilder();
				when(gsonBuilder.create()).thenReturn(gson);
				when(gson.fromJson(data, TwcRevisionDifferenceJson.class)).thenReturn(revisionDifferenceJson);

				assertEquals(revisionDifferenceJson, teamworkCloudService.getRevisionDifference(apiDomain, projectId, sourceRevision, targetRevision));
			} catch (TWCIntegrationException e) {
				fail("Unexpected exception occurred");
			}
		}

		@Test
		public void getElementsAtRevision_nullConnectionInfo() throws TWCIntegrationException {
			doReturn(null).when(teamworkCloudService).getConnectionInfo(apiDomain);

			assertTrue(teamworkCloudService.getElementsAtRevision(apiDomain, "", "", "").isEmpty());
		}

		@Test
		public void getElementsAtRevision_elementsRetrieved() throws TWCIntegrationException {
			String projectId = "projectId";
			String revision = "1";
			String elementIds = "elementIds";
			JsonElement jsonElement = mock(JsonElement.class);
			List<TwcElementJson> elements = new ArrayList<>();
			TwcElementJson elementJson = mock(TwcElementJson.class);
			elements.add(elementJson);

			doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);
			doReturn(jsonElement).when(teamworkCloudService).useModalRestHandleForBatchElementsAtRevision(connInfo, projectId, revision, elementIds);
			doReturn(elements).when(teamworkCloudService).deserializeBatchElementResults(jsonElement);

			assertTrue(teamworkCloudService.getElementsAtRevision(apiDomain, projectId, revision, elementIds).contains(elementJson));
		}

		@Test
		public void deserializeBatchElements_nullParameter() {
			List<TwcElementJson> batch = new ArrayList<>();

			doReturn(batch).when(teamworkCloudService).createTwcElementJsonList();

			assertTrue(teamworkCloudService.deserializeBatchElementResults(null).isEmpty());
		}

		@Test
		public void deserializeBatchElements_wrongType() {
			List<TwcElementJson> batch = new ArrayList<>();
			JsonElement json = spy(new JsonArray());

			doReturn(batch).when(teamworkCloudService).createTwcElementJsonList();

			assertTrue(teamworkCloudService.deserializeBatchElementResults(json).isEmpty());
		}

		@Test
		public void deserializeBatchElements_objectHasNullValueAndWrongTypeValue() {
			List<TwcElementJson> batch = new ArrayList<>();
			JsonObject json = spy(new JsonObject());
			json.add("1", null);
			json.add("2", spy(new JsonArray()));

			doReturn(batch).when(teamworkCloudService).createTwcElementJsonList();

			assertTrue(teamworkCloudService.deserializeBatchElementResults(json).isEmpty());
		}

		@Test
		public void deserializeBatchElements_nestedDataObjectNullAndWrongType() {
			List<TwcElementJson> batch = new ArrayList<>();
			JsonObject json = spy(new JsonObject());
			JsonObject dataNull = spy(new JsonObject());
			dataNull.add(JsonConstants.DATA, null);
			JsonObject dataWrongType = spy(new JsonObject());
			dataNull.add(JsonConstants.DATA, spy(new JsonObject()));
			json.add("1", dataNull);
			json.add("2", dataWrongType);

			doReturn(batch).when(teamworkCloudService).createTwcElementJsonList();

			assertTrue(teamworkCloudService.deserializeBatchElementResults(json).isEmpty());
		}

		@Test
		public void deserializeBatchElements_hasDeserializableData() {
			List<TwcElementJson> batch = new ArrayList<>();
			TwcElementJson elementJson = mock(TwcElementJson.class);
			batch.add(elementJson);
			JsonObject json = spy(new JsonObject());
			JsonObject data = spy(new JsonObject());
			JsonArray array = spy(new JsonArray());
			data.add(JsonConstants.DATA, array);
			json.add("1", data);

			doReturn(batch).when(teamworkCloudService).createTwcElementJsonList();
			doNothing().when(teamworkCloudService).populateBatchElementResults(batch, array);

			assertTrue(teamworkCloudService.deserializeBatchElementResults(json).contains(elementJson));
		}

		@Test
		public void populateBatchElementResults_notTwcElementJson() {
			JsonArray array = spy(new JsonArray());
			JsonElement item = mock(JsonElement.class);
			array.add(item);

			doReturn(false).when(teamworkCloudService).isTwcElementJson(item);

			teamworkCloudService.populateBatchElementResults(null, array);

			verify(teamworkCloudService, never()).getGsonWithoutExposeAnnotations();
		}

		@Test
		public void populateBatchElementResults_gsonDeserializesNull() {
			JsonArray array = spy(new JsonArray());
			JsonElement item = mock(JsonElement.class);
			array.add(item);
			Gson gson = mock(Gson.class);
			List<TwcElementJson> batch = spy(new ArrayList<>());

			doReturn(true).when(teamworkCloudService).isTwcElementJson(item);
			doReturn(gson).when(teamworkCloudService).getGsonWithoutExposeAnnotations();
			when(gson.fromJson(item, TwcElementJson.class)).thenReturn(null);

			teamworkCloudService.populateBatchElementResults(batch, array);

			verify(teamworkCloudService).getGsonWithoutExposeAnnotations();
			assertTrue(batch.isEmpty());
		}

		@Test
		public void populateBatchElementResults_properDeserialize() {
			JsonArray array = spy(new JsonArray());
			JsonElement item = mock(JsonElement.class);
			array.add(item);
			Gson gson = mock(Gson.class);
			List<TwcElementJson> batch = spy(new ArrayList<>());
			TwcElementJson elementJson = mock(TwcElementJson.class);

			doReturn(true).when(teamworkCloudService).isTwcElementJson(item);
			doReturn(gson).when(teamworkCloudService).getGsonWithoutExposeAnnotations();
			when(gson.fromJson(item, TwcElementJson.class)).thenReturn(elementJson);

			teamworkCloudService.populateBatchElementResults(batch, array);

			assertTrue(batch.contains(elementJson));
		}

		@Test
		public void isTwcElementJson_nullParameter() {
			assertFalse(teamworkCloudService.isTwcElementJson(null));
		}

		@Test
		public void isTwcElementJson_wrongType() {
			JsonElement json = spy(new JsonArray());

			assertFalse(teamworkCloudService.isTwcElementJson(json));
		}

		@Test
		public void isTwcElementJson_lacksEsi() {
			JsonElement json = spy(new JsonObject());

			assertFalse(teamworkCloudService.isTwcElementJson(json));
		}

		@Test
		public void isTwcElementJson_lacksKermlOwner() {
			JsonObject json = spy(new JsonObject());
			json.add(JsonConstants.ESI_DATA, mock(JsonElement.class));

			assertTrue(teamworkCloudService.isTwcElementJson(json));
		}

		@Test
		public void isTwcElementJson_hasKermlOwnerButWrongType() {
			JsonObject json = spy(new JsonObject());
			json.add(JsonConstants.ESI_DATA, mock(JsonElement.class));
			json.add(JsonConstants.KERML_OWNER, spy(new JsonArray()));

			assertFalse(teamworkCloudService.isTwcElementJson(json));
		}

		@Test
		public void isTwcElementJson_hasKermlOwnerAndCorrectType() {
			JsonObject json = spy(new JsonObject());
			json.add(JsonConstants.ESI_DATA, mock(JsonElement.class));
			json.add(JsonConstants.KERML_OWNER, spy(new JsonObject()));

			assertTrue(teamworkCloudService.isTwcElementJson(json));
		}

		@Test
		public void useModalRestHandler() {
			String expectedUrl = hostPort + COLON + PORT;
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, PROTOCOL_HTTPS));
			connInfo.setToken(token);
			String formattedUrl = String.format(TWC_URL_PATTERN, PROTOCOL_HTTPS, expectedUrl);
			String projectId = "projectId";
			String roleId = "roleId";
			Map<String, String> headers = new HashMap<>();
			headers.put(AUTHORIZATION, TWC_AUTHORIZATION_TOKEN_PREFIX + connInfo.getToken());
			Map<String, String> params = new HashMap<>();
			params.put(RESOURCE_NAME, "true");
			params.put(FILTER, "false");
			String method = String.format(PATH_ROLES, projectId, roleId);
			JsonElement jsonElement = mock(JsonElement.class);
			try {
				doReturn(jsonElement).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(formattedUrl, HTTP_GET,
						method, null, null, params, headers, CHECK_PERMISSIONS, true, 0);

				JsonElement result = teamworkCloudService.useModalRestHandler(connInfo, method, params, CHECK_PERMISSIONS);

				assertNotNull(result);
				assertSame(jsonElement, result);
			} catch (Exception e) {
				fail("No exception expected");
			}
		}

		@Test
		public void useModalRestHandler_errorDuringLoadAttempt() {
			String expectedUrl = hostPort + COLON + PORT;
			connInfo = Mockito.spy(new TeamworkCloudConnectionInfo(hostPort, user, PROTOCOL_HTTPS));
			connInfo.setToken(token);
			String formattedUrl = String.format(TWC_URL_PATTERN, PROTOCOL_HTTPS, expectedUrl);
			String projectId = "projectId";
			String roleId = "roleId";
			Map<String, String> headers = new HashMap<>();
			headers.put(AUTHORIZATION, TWC_AUTHORIZATION_TOKEN_PREFIX + connInfo.getToken());
			String method = String.format(PATH_ROLES, projectId, roleId);
			Map<String, String> params = new HashMap<>();
			params.put(RESOURCE_NAME, "true");
			params.put(FILTER, "false");
			String error = "error";
			Exception exception = spy(new Exception(error));
			try {
				doThrow(exception).when(modalRestHandler).loadUrlAsJsonWithStatusRunner(formattedUrl, HTTP_GET,
						method, null, null, params, headers, CHECK_PERMISSIONS, true, 0);

				teamworkCloudService.useModalRestHandler(connInfo, method, params, CHECK_PERMISSIONS);

				fail("Expected exception did not occur");
			} catch (Exception e) {
				assertEquals(error, e.getMessage());
			}
		}

		@Test
		public void useModalRestHandlerOsmcRoles() {
			Map<String, String> params = new HashMap<>();
			params.put(RESOURCE_NAME, "true");
			params.put(FILTER, "false");
			String method = String.format(PluginConstant.OSMC_ROLES_URL_FROM_ADMIN, user);
			JsonElement jsonElement = mock(JsonElement.class);

			when(connInfo.getUsername()).thenReturn(user);
			try {
				doReturn(jsonElement).when(teamworkCloudService).useModalRestHandler(connInfo, method, params, CHECK_PERMISSIONS);

				JsonElement result = teamworkCloudService.useModalRestHandlerForOsmcRoles(connInfo);

				assertNotNull(result);
				assertSame(jsonElement, result);
			} catch (Exception e) {
				fail("No exception expected");
			}
		}

		@Test
		public void useModalRestHandlerForOsmcRoles_errorDuringLoadAttempt() {
			Map<String, String> params = new HashMap<>();
			params.put(RESOURCE_NAME, "true");
			params.put(FILTER, "false");
			String method = String.format(PluginConstant.OSMC_ROLES_URL_FROM_ADMIN, user);
			String error = "error";
			TWCIntegrationException exception = spy(new TWCIntegrationException(error));

			when(connInfo.getUsername()).thenReturn(user);
			try {
				doThrow(exception).when(teamworkCloudService).useModalRestHandler(connInfo, method, params, CHECK_PERMISSIONS);

				teamworkCloudService.useModalRestHandlerForOsmcRoles(connInfo);

				fail("Expected exception did not occur");
			} catch (Exception e) {
				assertEquals(error, e.getMessage());
			}
		}

		@Test
		public void useModalRestHandlerForRevisionDifference_noIssues() {
			String projectId = "projectId";
			String sourceRevision = "1";
			String targetRevision = "2";
			Map<String, String> params = new HashMap<>();
			params.put(SOURCE_REVISION, sourceRevision);
			params.put(TARGET_REVISION, targetRevision);
			String method = String.format(OSMC_REVISION_DIFF, projectId);
			JsonElement jsonElement = mock(JsonElement.class);

			try {
				doReturn(jsonElement).when(teamworkCloudService).useModalRestHandler(connInfo, method, params, GETTING_REVISION_DIFF);

				assertEquals(jsonElement, teamworkCloudService.useModalRestHandlerForRevisionDifference(connInfo, projectId, sourceRevision, targetRevision));
			} catch (TWCIntegrationException e) {
				fail("Unexpected exception occurred");
			}
		}

		@Test
		public void useModalRestHandlerForRevisionDifference_exceptionDuringRestCall() {
			String projectId = "projectId";
			String sourceRevision = "1";
			String targetRevision = "2";
			Map<String, String> params = new HashMap<>();
			params.put(SOURCE_REVISION, sourceRevision);
			params.put(TARGET_REVISION, targetRevision);
			String method = String.format(OSMC_REVISION_DIFF, projectId);
			String error = "error";
			TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

			try {
				doThrow(integrationException).when(teamworkCloudService).useModalRestHandler(connInfo, method, params, GETTING_REVISION_DIFF);

				teamworkCloudService.useModalRestHandlerForRevisionDifference(connInfo, projectId, sourceRevision, targetRevision);

				fail("Expected exception did no occur");
			} catch (TWCIntegrationException e) {
				assertEquals(error, e.getMessage());
			}
		}

		@Test
		public void getCurrentProjectUri() {
			String result = teamworkCloudService.getCurrentProjectUri(apiDomain);

			assertNotNull(result);
			assertEquals(uri.toString(), result);
		}

		@Test
		public void getCurrentProjectUri_nullUri() {
			doReturn(null).when(primaryProject).getLocationURI();

			assertNull(teamworkCloudService.getCurrentProjectUri(apiDomain));
		}

		@Test
		public void getCurrentProjectUri_nullPrimaryProject() {
			doReturn(null).when(currentProject).getPrimaryProject();

			assertNull(teamworkCloudService.getCurrentProjectUri(apiDomain));
		}

		@Test
		public void getCurrentProjectUri_nullProject() {
			doReturn(null).when(apiDomain).getCurrentProject();

			assertNull(teamworkCloudService.getCurrentProjectUri(apiDomain));
		}
	}

	@RunWith(Parameterized.class)
	public static class TestTeamworkCloudServiceRevisionDifference extends TestTeamworkCloudServiceBase {
		@Parameterized.Parameter(value = 0)
		public String projectId;
		@Parameterized.Parameter(value = 1)
		public String sourceRevision;
		@Parameterized.Parameter(value = 2)
		public String targetRevision;

		@Parameterized.Parameters(name = "{index}: getRevisionDifference(projectId: {0}, sourceRevision: {1}, targetRevision: {2})")
		public static Collection<Object[]> data() {
			return Arrays.asList(new Object[][] {
					{null, "1", "2"},
					{" ", "1", "2"},
					{"projectId", null, "2"},
					{"projectId", " ", "2"},
					{"projectId", "3.14", "2"},
					{"projectId", "1", null},
					{"projectId", "1", " "},
					{"projectId", "1", "a"}
			});
		}

		@Test
		public void getRevisionDifference() {
			doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);

			try {
				assertNull(teamworkCloudService.getRevisionDifference(apiDomain, projectId, sourceRevision, targetRevision));
			} catch (TWCIntegrationException e) {
				fail("Unexpected exception occurred");
			}
		}
	}

	@RunWith(Parameterized.class)
	public static class TestTeamworkCloudServiceElementsAtRevision extends TestTeamworkCloudServiceBase {
		@Parameterized.Parameter(value = 0)
		public String projectId;
		@Parameterized.Parameter(value = 1)
		public String revision;
		@Parameterized.Parameter(value = 2)
		public String elementIds;

		@Parameterized.Parameters(name = "{index}: getElementsAtRevision(projectId: {0}, revision: {1}, elementIds: {2})")
		public static Collection<Object[]> data() {
			return Arrays.asList(new Object[][] {
					{null, "1", "2"},
					{" ", "1", "2"},
					{"projectId", null, "2"},
					{"projectId", " ", "2"},
					{"projectId", "3.14", "2"},
					{"projectId", "1", null},
					{"projectId", "1", " "},
					{"projectId", "1", "a"}
			});
		}

		@Test
		public void getElementsAtRevision() {
			doReturn(connInfo).when(teamworkCloudService).getConnectionInfo(apiDomain);

			try {
				assertTrue(teamworkCloudService.getElementsAtRevision(apiDomain, projectId, revision, elementIds).isEmpty());
			} catch (TWCIntegrationException e) {
				fail("Unexpected exception occurred");
			}
		}
	}
}
