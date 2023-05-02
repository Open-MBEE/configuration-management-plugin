package org.openmbee.plugin.cfgmgmt.integration;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.webbrowser.EngineParameters;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.cache.HttpCache;
import com.teamdev.jxbrowser.cookie.Cookie;
import com.teamdev.jxbrowser.cookie.CookieStore;
import com.teamdev.jxbrowser.engine.RenderingMode;
import com.teamdev.jxbrowser.engine.internal.EngineImpl;
import com.teamdev.jxbrowser.frame.Frame;
import com.teamdev.jxbrowser.frame.WebStorage;
import com.teamdev.jxbrowser.internal.rpc.EngineId;
import com.teamdev.jxbrowser.navigation.Navigation;
import com.teamdev.jxbrowser.navigation.event.FrameLoadFinished;
import com.teamdev.jxbrowser.spellcheck.SpellChecker;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestWssoService {
    private WssoService wssoService;
    private ConfigurationManagementService configurationManagementService;
    private UIDomain uiDomain;
    private IConnectionInfo connectionInfo;
    private IClient client;
    private Logger logger;
    private Browser browser;
    private JComponent jComponent;
    private JFrame jFrame;
    private Path path;
    private File file;
    private EngineImpl engine;
    private EngineId engineId;
    private WebStorage webStorage;
    private FrameLoadFinished frameLoadFinished;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        wssoService = spy(new WssoService(configurationManagementService));

        uiDomain = mock(UIDomain.class);
        connectionInfo = mock(IConnectionInfo.class);
        client = mock(IClient.class);
        logger = mock(Logger.class);
        browser = mock(Browser.class);
        jComponent = mock(JComponent.class);
        jFrame = mock(JFrame.class);
        path = mock(Path.class);
        file = mock(File.class);
        engine = mock(EngineImpl.class);
        engineId = mock(EngineId.class);
        webStorage = mock(WebStorage.class);
        frameLoadFinished = mock(FrameLoadFinished.class);

        when(configurationManagementService.getUIDomain()).thenReturn(uiDomain);
        doReturn(path).when(wssoService).getUserChromiumCachePath();
        when(path.toFile()).thenReturn(file);
    }

    @Test
    public void asyncLoginAttempt_exceptionGettingTokenAndExceptionHasMessage() {
        String error = "error";
        Exception exception = spy(new MalformedURLException(error));

        try {
            doThrow(exception).when(wssoService).getLoginToken(connectionInfo, client, true, "");
            doNothing().when(uiDomain).logError(error);

            wssoService.asyncLoginAttempt(connectionInfo, client, true, "");

            verify(uiDomain).logError(error);
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void asyncLoginAttempt_exceptionGettingTokenButExceptionLacksMessage() {
        Exception exception = spy(new MalformedURLException());

        try {
            doThrow(exception).when(wssoService).getLoginToken(connectionInfo, client, true, "");
            doReturn(null).when(exception).getMessage();
            doNothing().when(uiDomain).logError(exception.getClass().getName());

            wssoService.asyncLoginAttempt(connectionInfo, client, true, "");

            verify(uiDomain).logError(exception.getClass().getName());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void asyncLoginAttempt_clientLoggedInRightAway() throws Exception {
        long time = System.currentTimeMillis();

        doNothing().when(wssoService).getLoginToken(connectionInfo, client, true, "");
        doReturn(time).when(wssoService).currentTimeMilliseconds();
        when(client.isLoggedIn()).thenReturn(true);

        wssoService.asyncLoginAttempt(connectionInfo, client, true, "");

        verify(wssoService).currentTimeMilliseconds();
        verify(uiDomain, never()).logError(anyString());
    }

    @Test
    public void asyncLoginAttempt_clientLoggedInAfterSingleLoop() throws Exception {
        long time = System.currentTimeMillis();

        doNothing().when(wssoService).getLoginToken(connectionInfo, client, true, "");
        doReturn(time).when(wssoService).currentTimeMilliseconds();
        when(client.isLoggedIn()).thenReturn(false).thenReturn(true);

        wssoService.asyncLoginAttempt(connectionInfo, client, true, "");

        verify(wssoService, times(2)).currentTimeMilliseconds();
        verify(uiDomain, never()).logError(anyString());
    }

    @Test
    public void asyncLoginAttempt_timeExpires() throws Exception {
        long time = System.currentTimeMillis();
        long expireTime = time + PluginConstant.EXPIRY_TIME_MILLISECONDS;

        doNothing().when(wssoService).getLoginToken(connectionInfo, client, true, "");
        doReturn(time).doReturn(time).doReturn(expireTime).when(wssoService).currentTimeMilliseconds();
        when(client.isLoggedIn()).thenReturn(false);

        wssoService.asyncLoginAttempt(connectionInfo, client, true, "");

        verify(wssoService, times(3)).currentTimeMilliseconds();
        verify(uiDomain, never()).logError(anyString());
    }

    @Test
    public void deleteFolder_folder_With_NoFiles() {
        File emptyFolder = new File("./src/test/resources/emptyFolder");
        emptyFolder.mkdir();

        wssoService.deleteFolder(emptyFolder);

        verify(wssoService, times(1)).handleDelete(any());
    }

    @Test
    public void deleteFolder_with_OneFolder() {
        File testFolder = new File("./src/test/resources/testFolder");
        testFolder.mkdir();
        File innerFolder = new File("./src/test/resources/testFolder/innerFolder");
        innerFolder.mkdir();

        wssoService.deleteFolder(testFolder);

        verify(wssoService, times(2)).deleteFolder(any());
    }

    @Test
    public void deleteFolder_with_OneFile() throws IOException {
        File testFolder = new File("./src/test/resources/testFolder");
        testFolder.mkdir();
        File filewithAbsolutePath = new File("./src/test/resources/testFolder/testFile.txt");
        filewithAbsolutePath.createNewFile();

        wssoService.deleteFolder(testFolder);

        verify(wssoService, times(2)).handleDelete(any());
    }

    @Test
    public void  handleDelete_nullPath() {
        wssoService.handleDelete(null);

        verify(logger, never()).debug(anyString());
    }

    @Test
    public void generateNewEngine_cacheIsFolder() {
        EngineParameters engineParameters = mock(EngineParameters.class);
        EngineId engineId = mock(EngineId.class);

        when(file.isDirectory()).thenReturn(true);
        doReturn(engineParameters).when(wssoService).createEngineParameters();
        doReturn(engineParameters).when(engineParameters).browserDir(file);
        doReturn(engineParameters).when(engineParameters).renderingMode(RenderingMode.HARDWARE_ACCELERATED);
        doReturn(engineParameters).when(engineParameters).userAgent(PluginConstant.USER_AGENT);
        doReturn(engine).when(wssoService).createEngine(engineParameters);
        when(engine.id()).thenReturn(engineId);

        assertSame(engine, wssoService.generateNewEngine());
        verify(wssoService).deleteFolder(file);
        verify(wssoService, never()).handleDelete(path);
    }

    @Test
    public void generateNewEngine_cacheHasStuff() {
        EngineParameters engineParameters = mock(EngineParameters.class);

        when(file.isDirectory()).thenReturn(false);
        when(file.toPath()).thenReturn(path);
        doNothing().when(wssoService).handleDelete(path);
        doReturn(engineParameters).when(wssoService).createEngineParameters();
        doReturn(engineParameters).when(engineParameters).browserDir(file);
        doReturn(engineParameters).when(engineParameters).renderingMode(RenderingMode.HARDWARE_ACCELERATED);
        doReturn(engineParameters).when(engineParameters).userAgent(PluginConstant.USER_AGENT);
        doReturn(engine).when(wssoService).createEngine(engineParameters);
        when(engine.id()).thenReturn(engineId);

        assertSame(engine, wssoService.generateNewEngine());
        verify(wssoService, never()).deleteFolder(file);
        verify(wssoService).handleDelete(path);
    }

    @Test
    public void getEngine_nullId() {
        doReturn(null).when(wssoService).getEngineId();
        doReturn(engine).when(wssoService).generateNewEngine();

        assertSame(engine, wssoService.getEngine());
    }

    @Test
    public void getEngine_idExistsButNoEngineFromIt() {
        doReturn(engineId).when(wssoService).getEngineId();
        doReturn(null).when(wssoService).getEngineFromId();
        doReturn(engine).when(wssoService).generateNewEngine();

        assertSame(engine, wssoService.getEngine());
    }

    @Test
    public void getEngine_idExistsWithEngine() {
        doReturn(engineId).when(wssoService).getEngineId();
        doReturn(engine).when(wssoService).getEngineFromId();

        assertSame(engine, wssoService.getEngine());
    }

    @Test
    public void getLoginToken_noRedirect() throws MalformedURLException {
        String cookieName = "cookieName";
        String url = "url";
        Browser browser = mock(Browser.class);
        HttpCache httpCache = mock(HttpCache.class);
        SpellChecker spellChecker = mock(SpellChecker.class);
        Navigation navigation = mock(Navigation.class);

        when(connectionInfo.getUrl()).thenReturn(url);
        doReturn(engine).when(wssoService).getEngine();
        when(engine.newBrowser()).thenReturn(browser);
        when(engine.httpCache()).thenReturn(httpCache);
        doReturn(mock(CompletableFuture.class)).when(httpCache).clear();
        when(engine.spellChecker()).thenReturn(spellChecker);
        doNothing().when(spellChecker).disable();

        doNothing().when(wssoService).cleanCookies(url, engine);
        doNothing().when(wssoService).setupAsyncTokenAcquire(client, cookieName, url, engine, browser);
        doReturn(jFrame).when(wssoService).getLoginFrame(browser);
        when(browser.navigation()).thenReturn(navigation);
        doNothing().when(navigation).loadUrl(url);

        wssoService.getLoginToken(connectionInfo, client, false, cookieName);

        verify(wssoService, never()).getRedirectString(url);
    }

    @Test
    public void getLoginToken_redirect() throws MalformedURLException {
        String cookieName = "cookieName";
        String url = "url";
        String redirect = "redirect";
        Browser browser = mock(Browser.class);
        HttpCache httpCache = mock(HttpCache.class);
        SpellChecker spellChecker = mock(SpellChecker.class);
        Navigation navigation = mock(Navigation.class);

        when(connectionInfo.getUrl()).thenReturn(url);
        doReturn(redirect).when(wssoService).getRedirectString(url);
        doReturn(engine).when(wssoService).getEngine();
        when(engine.newBrowser()).thenReturn(browser);
        when(engine.httpCache()).thenReturn(httpCache);
        doReturn(mock(CompletableFuture.class)).when(httpCache).clear();
        when(engine.spellChecker()).thenReturn(spellChecker);
        doNothing().when(spellChecker).disable();

        doNothing().when(wssoService).cleanCookies(url, engine);
        doNothing().when(wssoService).setupAsyncTokenAcquire(client, cookieName, url, engine, browser);
        doReturn(jFrame).when(wssoService).getLoginFrame(browser);
        when(browser.navigation()).thenReturn(navigation);
        doNothing().when(navigation).loadUrl(redirect);

        wssoService.getLoginToken(connectionInfo, client, true, cookieName);

        verify(wssoService).getRedirectString(url);
    }

    @Test
    public void getLoginFrame() {
        doReturn(jComponent).when(wssoService).getBrowserView(browser);
        doReturn(jFrame).when(wssoService).getNewFrame("Login");

        assertSame(jFrame, wssoService.getLoginFrame(browser));
        verify(jFrame).setAlwaysOnTop(true);
    }

    @Test
    public void cleanCookies_urlNotEmpty() throws MalformedURLException {
        String host = "www.host.com";
        String url = "https://" + host;

        CookieStore cookieStore = mock(CookieStore.class);
        List<Cookie> cookies = new ArrayList<>();
        Cookie cookie = mock(Cookie.class);
        cookies.add(cookie);

        when(engine.cookieStore()).thenReturn(cookieStore);
        when(cookieStore.cookies()).thenReturn(cookies);
        when(cookie.domain()).thenReturn(host);
        doReturn(true).when(cookieStore).delete(cookie);

        wssoService.cleanCookies(url, engine);

        verify(cookieStore).delete(cookie);
    }

    @Test
    public void cleanCookies_urlEmpty() throws MalformedURLException {
        CookieStore cookieStore = mock(CookieStore.class);
        List<Cookie> cookies = new ArrayList<>();
        Cookie cookie = mock(Cookie.class);
        cookies.add(cookie);

        when(engine.cookieStore()).thenReturn(cookieStore);
        when(cookieStore.cookies()).thenReturn(cookies);
        when(cookie.domain()).thenReturn(PluginConstant.JIRA);
        doReturn(true).when(cookieStore).delete(cookie);

        wssoService.cleanCookies("", engine);

        verify(cookieStore).delete(cookie);
    }

    @Test
    public void acquireTokenAfterFrameLoad_malformedUrl() throws MalformedURLException {
        String url = "bad<Url>";
        String error = "error";
        Exception exception = spy(new MalformedURLException(error));

        when(frameLoadFinished.url()).thenReturn(url);
        doThrow(exception).when(wssoService).createUrl(url);
        doNothing().when(uiDomain).logError(error);

        wssoService.acquireTokenAfterFrameLoad(client, "", "", engine, frameLoadFinished);

        verify(uiDomain).logError(error);
        verify(frameLoadFinished, never()).frame();
    }

    @Test
    public void acquireTokenAfterFrameLoad_ticketNotFound() throws MalformedURLException {
        String host = "www.host.com";
        String url = "https://" + host;
        String error = "error";
        URL currentUrl = spy(new URL(url));
        Frame frame = mock(Frame.class);

        when(frameLoadFinished.url()).thenReturn(url);
        doReturn(currentUrl).when(wssoService).createUrl(url);
        when(frameLoadFinished.frame()).thenReturn(frame);
        when(frame.sessionStorage()).thenReturn(webStorage);
        doReturn(false).when(wssoService).shouldFindTicket(webStorage);

        wssoService.acquireTokenAfterFrameLoad(client, "", "", engine, frameLoadFinished);

        verify(uiDomain, never()).logError(error);
        verify(engine, never()).cookieStore();
    }

    @Test
    public void acquireTokenAfterFrameLoad_ticketFoundButEitherMismatchesOrException() throws Exception {
        String cookieName = "cookieName";
        String targetUrl = "targetUrl";
        String host = "www.host.com";
        String url = "https://" + host;
        String error = "error";
        String formattedError = "error: error";
        Exception exception = spy(new URISyntaxException(error, error));
        URL currentUrl = spy(new URL(url));
        Frame frame = mock(Frame.class);
        CookieStore cookieStore = mock(CookieStore.class);
        List<Cookie> cookies = new ArrayList<>();
        Cookie domainMismatch = mock(Cookie.class);
        Cookie nameMismatch = mock(Cookie.class);
        Cookie uriSyntaxException = mock(Cookie.class);
        cookies.add(domainMismatch);
        cookies.add(nameMismatch);
        cookies.add(uriSyntaxException);

        when(frameLoadFinished.url()).thenReturn(url);
        doReturn(currentUrl).when(wssoService).createUrl(url);
        when(frameLoadFinished.frame()).thenReturn(frame);
        when(frame.sessionStorage()).thenReturn(webStorage);
        doReturn(true).when(wssoService).shouldFindTicket(webStorage);
        when(engine.cookieStore()).thenReturn(cookieStore);
        when(cookieStore.cookies()).thenReturn(cookies);
        when(domainMismatch.domain()).thenReturn("");
        when(nameMismatch.domain()).thenReturn(host);
        when(nameMismatch.name()).thenReturn("");
        when(uriSyntaxException.domain()).thenReturn(host);
        when(uriSyntaxException.name()).thenReturn(cookieName);
        doNothing().when(webStorage).putItem(PluginConstant.TICKET_FOUND, PluginConstant.TRUE);
        doThrow(exception).when(client).setToken(targetUrl, cookies);

        wssoService.acquireTokenAfterFrameLoad(client, cookieName, targetUrl, engine, frameLoadFinished);

        verify(uiDomain).logError(formattedError);
        verify(uiDomain, never()).log(PluginConstant.LOGIN_TOKEN_ACQUIRED);
        verify(uiDomain, never()).logError(ExceptionConstants.URL_HAS_CHANGED);
    }

    @Test
    public void acquireTokenAfterFrameLoad_ticketFoundButUrlChanged() throws Exception {
        String cookieName = "cookieName";
        String targetUrl = "targetUrl";
        String host = "www.host.com";
        String url = "https://" + host;
        String error = "error";
        URL currentUrl = spy(new URL(url));
        Frame frame = mock(Frame.class);
        CookieStore cookieStore = mock(CookieStore.class);
        List<Cookie> cookies = new ArrayList<>();
        Cookie urlChanged = mock(Cookie.class);
        cookies.add(urlChanged);
        WindowEvent windowEvent = mock(WindowEvent.class);

        when(frameLoadFinished.url()).thenReturn(url);
        doReturn(currentUrl).when(wssoService).createUrl(url);
        when(frameLoadFinished.frame()).thenReturn(frame);
        when(frame.sessionStorage()).thenReturn(webStorage);
        doReturn(true).when(wssoService).shouldFindTicket(webStorage);
        when(engine.cookieStore()).thenReturn(cookieStore);
        when(cookieStore.cookies()).thenReturn(cookies);
        when(urlChanged.domain()).thenReturn(host);
        when(urlChanged.name()).thenReturn(cookieName);
        doNothing().when(webStorage).putItem(PluginConstant.TICKET_FOUND, PluginConstant.TRUE);
        doReturn(false).when(client).setToken(targetUrl, cookies);
        doNothing().when(uiDomain).logError(ExceptionConstants.URL_HAS_CHANGED);
        doReturn(jFrame).when(wssoService).getLoginFrame();
        doReturn(windowEvent).when(wssoService).createWindowClosingEvent();
        doNothing().when(jFrame).dispatchEvent(windowEvent);
        doNothing().when(jFrame).dispose();

        wssoService.acquireTokenAfterFrameLoad(client, cookieName, targetUrl, engine, frameLoadFinished);

        verify(uiDomain, never()).logError(error);
        verify(uiDomain, never()).log(PluginConstant.LOGIN_TOKEN_ACQUIRED);
        verify(uiDomain).logError(ExceptionConstants.URL_HAS_CHANGED);
    }

    @Test
    public void acquireTokenAfterFrameLoad_ticketFoundAndTokenAcquired() throws Exception {
        String cookieName = "cookieName";
        String targetUrl = "targetUrl";
        String host = "www.host.com";
        String url = "https://" + host;
        String error = "error";
        URL currentUrl = spy(new URL(url));
        Frame frame = mock(Frame.class);
        CookieStore cookieStore = mock(CookieStore.class);
        List<Cookie> cookies = new ArrayList<>();
        Cookie urlChanged = mock(Cookie.class);
        cookies.add(urlChanged);
        WindowEvent windowEvent = mock(WindowEvent.class);

        when(frameLoadFinished.url()).thenReturn(url);
        doReturn(currentUrl).when(wssoService).createUrl(url);
        when(frameLoadFinished.frame()).thenReturn(frame);
        when(frame.sessionStorage()).thenReturn(webStorage);
        doReturn(true).when(wssoService).shouldFindTicket(webStorage);
        when(engine.cookieStore()).thenReturn(cookieStore);
        when(cookieStore.cookies()).thenReturn(cookies);
        when(urlChanged.domain()).thenReturn(host);
        when(urlChanged.name()).thenReturn(cookieName);
        doNothing().when(webStorage).putItem(PluginConstant.TICKET_FOUND, PluginConstant.TRUE);
        doReturn(true).when(client).setToken(targetUrl, cookies);
        doNothing().when(uiDomain).log(PluginConstant.LOGIN_TOKEN_ACQUIRED);
        doReturn(jFrame).when(wssoService).getLoginFrame();
        doReturn(windowEvent).when(wssoService).createWindowClosingEvent();
        doNothing().when(jFrame).dispatchEvent(windowEvent);
        doNothing().when(jFrame).dispose();

        wssoService.acquireTokenAfterFrameLoad(client, cookieName, targetUrl, engine, frameLoadFinished);

        verify(uiDomain, never()).logError(error);
        verify(uiDomain).log(PluginConstant.LOGIN_TOKEN_ACQUIRED);
        verify(uiDomain, never()).logError(ExceptionConstants.URL_HAS_CHANGED);
    }

    @Test
    public void shouldFindTicket_notFound() {
        when(webStorage.contains(PluginConstant.TICKET_FOUND)).thenReturn(false);

        assertTrue(wssoService.shouldFindTicket(webStorage));
    }

    @Test
    public void shouldFindTicket_foundInStorageButItemNotPresent() {
        when(webStorage.contains(PluginConstant.TICKET_FOUND)).thenReturn(true);
        when(webStorage.item(PluginConstant.TICKET_FOUND)).thenReturn(Optional.empty());

        assertTrue(wssoService.shouldFindTicket(webStorage));
    }

    @Test
    public void shouldFindTicket_foundInStorageAndItemPresentButNotTrue() {
        Optional<String> item = Optional.of("item");

        when(webStorage.contains(PluginConstant.TICKET_FOUND)).thenReturn(true);
        when(webStorage.item(PluginConstant.TICKET_FOUND)).thenReturn(item);

        assertTrue(wssoService.shouldFindTicket(webStorage));
    }

    @Test
    public void shouldFindTicket_foundInStorageAndItemPresentAndTrue() {
        Optional<String> item = Optional.of(PluginConstant.TRUE);

        when(webStorage.contains(PluginConstant.TICKET_FOUND)).thenReturn(true);
        when(webStorage.item(PluginConstant.TICKET_FOUND)).thenReturn(item);

        assertFalse(wssoService.shouldFindTicket(webStorage));
    }
}