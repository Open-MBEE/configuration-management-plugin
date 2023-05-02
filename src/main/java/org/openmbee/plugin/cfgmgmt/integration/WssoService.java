package org.openmbee.plugin.cfgmgmt.integration;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.settings.ConfigurationManagementSystemProperties;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.webbrowser.EngineParameters;
import com.nomagic.magicdraw.webbrowser.WebBrowserFactory;
import com.teamdev.jxbrowser.browser.Browser;
import com.teamdev.jxbrowser.cookie.Cookie;
import com.teamdev.jxbrowser.engine.Engine;
import com.teamdev.jxbrowser.engine.RenderingMode;
import com.teamdev.jxbrowser.engine.internal.EngineImpl;
import com.teamdev.jxbrowser.frame.WebStorage;
import com.teamdev.jxbrowser.internal.rpc.EngineId;
import com.teamdev.jxbrowser.navigation.event.FrameLoadFinished;
import com.teamdev.jxbrowser.view.swing.BrowserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants.URL_HAS_CHANGED;
import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.*;

public class WssoService {
    private final Logger logger = LoggerFactory.getLogger(WssoService.class);
    private ConfigurationManagementService configurationManagementService;
    private JFrame loginFrame = null;
    private EngineId engineId = null;
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 840;

    public WssoService(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    public void acquireToken(IConnectionInfo connInfo, IClient client, boolean redirectThroughPSAML, String cookieName) {
        new Thread(() -> asyncLoginAttempt(connInfo, client, redirectThroughPSAML, cookieName)).start();
    }

    protected void asyncLoginAttempt(IConnectionInfo connInfo, IClient client, boolean redirectThroughPSAML, String cookieName) {
        // only use this in conjunction with a Thread or some other async method
        try {
            getLoginToken(connInfo, client, redirectThroughPSAML, cookieName);

            long expireTime = currentTimeMilliseconds() + EXPIRY_TIME_MILLISECONDS;
            while (!client.isLoggedIn() && (currentTimeMilliseconds() < expireTime)) {
                Thread.sleep(SLEEP_TIME_MILLISECONDS);
            }
        } catch (Exception e) {
            getConfigurationManagementService().getUIDomain().logError(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
        }
    }

    protected long currentTimeMilliseconds() {
        return System.currentTimeMillis();
    }

    protected void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files != null) {
            for(File f : files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    handleDelete(f.toPath());
                }
            }
        }

        handleDelete(folder.toPath());
    }

    protected void handleDelete(Path path) {
        try {
             if (path != null) {
                 Files.deleteIfExists(path);
             }
        } catch (Exception e) {
            logger.debug(String.format(ExceptionConstants.FILE_NOT_DELETED, path));
        }
    }

    protected Engine generateNewEngine() {
        // cleaning folder contents
        Path browserDataDirectory = getUserChromiumCachePath();
        File dataDirectory = browserDataDirectory.toFile();
        if(dataDirectory.isDirectory()) {
            deleteFolder(dataDirectory);
        } else {
            handleDelete(dataDirectory.toPath());
        }

        //TODO replace deprecated class
        EngineParameters params = createEngineParameters();
        params.browserDir(browserDataDirectory.toFile());
        params.renderingMode(RenderingMode.HARDWARE_ACCELERATED);
        params.userAgent(PluginConstant.USER_AGENT);
        Engine engine = createEngine(params);

        engineId = ((EngineImpl) engine).id();

        return engine;
    }

    protected EngineId getEngineId() {
        return engineId;
    }

    protected Path getUserChromiumCachePath() {
        return Paths.get(Application.environment().getUserDataDirectory().toString(), CHROMIUM_CACHE);
    }

    protected EngineParameters createEngineParameters() {
        return EngineParameters.create();
    }

    //TODO replace deprecated class
    protected Engine createEngine(EngineParameters engineParameters) {
        return WebBrowserFactory.createEngine(engineParameters);
    }

    protected Engine getEngine() {
        if (getEngineId() == null) {
            return generateNewEngine();
        } else {
            Engine engine = getEngineFromId();
            if (engine == null) {
                return generateNewEngine();
            } else {
                return engine;
            }
        }
    }

    protected EngineImpl getEngineFromId() {
        return EngineImpl.with(engineId).orElse(null);
    }

    protected void getLoginToken(IConnectionInfo connInfo, IClient client, boolean redirectThroughPSAML,
            String cookieName) throws MalformedURLException {
        // DEPLOYMENT ONLY : Use this function during deployment to generate the base64 encoding of the form to include
        // at the bottom of this module.
        //STPAEditorFormData.encodeSTPAEditorForm();

        String url;
        String targetUrl = connInfo.getUrl();

        if (redirectThroughPSAML) {
            url = getRedirectString(targetUrl);
        } else {
            url = targetUrl;
        }

        Engine engine = getEngine();
        Browser browser = engine.newBrowser();
        engine.httpCache().clear();
        engine.spellChecker().disable();

        // Delete cookies
        cleanCookies(targetUrl, engine);

        // Bring the browser window to the front - then set the "myJavaObject"
        // handler (created above) so that callbacks can be made from the HTML form
        setupAsyncTokenAcquire(client, cookieName, targetUrl, engine, browser);
        loginFrame = getLoginFrame(browser);
        browser.navigation().loadUrl(url);
    }

    protected String getRedirectString(String targetUrl) {
        //TODO use a library for the url cleanup
        return ConfigurationManagementSystemProperties.getPropertyValue(PSAML_URL) + "?PartnerSpId=" +
                targetUrl.replace("\\/", "%2F").replace(COLON, "%3A");
    }

    protected void setupAsyncTokenAcquire(IClient client, String cookieName, String targetUrl, Engine engine, Browser browser) {
        browser.navigation().on(FrameLoadFinished.class, loadEvent -> acquireTokenAfterFrameLoad(client, cookieName, targetUrl, engine, loadEvent));
    }

    protected JFrame getLoginFrame(Browser browser) {
        final String formName = "Login";

        JComponent view = getBrowserView(browser);
        loginFrame = getNewFrame(formName);
        loginFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        loginFrame.add(view, BorderLayout.CENTER);
        loginFrame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
        loginFrame.setAlwaysOnTop(true);

        return loginFrame;
    }

    protected void cleanCookies(String targetUrl, Engine engine) throws MalformedURLException {
        if (!targetUrl.isEmpty()) {
            URL url = createUrl(targetUrl);
            List<Cookie> cookies = engine.cookieStore().cookies();
            for (int i = cookies.size() - 1; i >= 0; i--) {
                if (cookies.get(i).domain().equalsIgnoreCase(url.getHost())) {
                    engine.cookieStore().delete(cookies.get(i));
                }
            }
        } else {
            List<Cookie> cookies = engine.cookieStore().cookies();
            for (Cookie currCookie : cookies) {
                // TODO: Fix cookie name for jira and 3Dx
                if (currCookie.domain().toUpperCase().contains(JIRA)) {
                    engine.cookieStore().delete(currCookie);
                }
            }
        }
    }

    protected URL createUrl(String targetUrl) throws MalformedURLException {
        return new URL(targetUrl);
    }

    protected void acquireTokenAfterFrameLoad(IClient client, String cookieName, String targetUrl, Engine engine, FrameLoadFinished loadEvent) {
        try {
            URL currentUrl = createUrl(loadEvent.url());
            WebStorage sessionWebStorage = loadEvent.frame().sessionStorage();

            //TODO clean this logic up
            if (shouldFindTicket(sessionWebStorage)) {
                List<Cookie> cookies = engine.cookieStore().cookies();

                for(Cookie c : cookies) {
                    if (c.domain().equalsIgnoreCase(currentUrl.getHost()) && c.name().equalsIgnoreCase(cookieName)) {
                        sessionWebStorage.putItem(PluginConstant.TICKET_FOUND, TRUE);
                        Thread.sleep(LOWEST_SLEEP_TIME_MILLISECONDS);
                        if (client.setToken(targetUrl, cookies)) {
                            getConfigurationManagementService().getUIDomain().log(LOGIN_TOKEN_ACQUIRED);
                        } else {
                            getConfigurationManagementService().getUIDomain().logError(URL_HAS_CHANGED);
                        }

                        getLoginFrame().dispatchEvent(createWindowClosingEvent());
                        getLoginFrame().dispose();
                        break;
                    }
                }
            }
        } catch (InterruptedException | MalformedURLException | URISyntaxException e) {
            getConfigurationManagementService().getUIDomain().logError(e.getMessage() == null ? e.getClass().getName() : e.getMessage());
        }
    }

    protected JFrame getLoginFrame() {
        return loginFrame; // used for unit testing
    }

    protected boolean shouldFindTicket(WebStorage sessionWebStorage) {
        if(sessionWebStorage.contains(PluginConstant.TICKET_FOUND)) {
            Optional<String> ticketOptional = sessionWebStorage.item(PluginConstant.TICKET_FOUND);
            if(ticketOptional.isPresent()) {
                return !ticketOptional.get().equals(TRUE); // if ticket is not "true" we should find a ticket
            }
        }
        return true; // if ticket isn't found
    }

    protected WindowEvent createWindowClosingEvent() {
        return new WindowEvent(loginFrame, WindowEvent.WINDOW_CLOSING);
    }

    protected JComponent getBrowserView(Browser browser) {
        return BrowserView.newInstance(browser);
    }

    protected JFrame getNewFrame(String formName) {
        return new JFrame(formName);
    }
}
