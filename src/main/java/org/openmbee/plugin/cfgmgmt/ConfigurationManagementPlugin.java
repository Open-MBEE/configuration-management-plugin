
package org.openmbee.plugin.cfgmgmt;

import org.openmbee.plugin.cfgmgmt.configurators.ContextualMenuConfigurator;
import org.openmbee.plugin.cfgmgmt.configurators.MainMenuConfigurator;
import org.openmbee.plugin.cfgmgmt.configurators.ToolbarConfigurator;
import org.openmbee.plugin.cfgmgmt.controller.AbstractCmDispatcher;
import org.openmbee.plugin.cfgmgmt.controller.CmControllerFactory;
import org.openmbee.plugin.cfgmgmt.controller.CmDispatcher;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.integration.ModalRestHandler;
import org.openmbee.plugin.cfgmgmt.integration.WssoService;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraClientManager;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxClientManager;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxService;
import org.openmbee.plugin.cfgmgmt.integration.twc.TeamworkCloudService;
import org.openmbee.plugin.cfgmgmt.integration.twc.TwcRevisionService;
import org.openmbee.plugin.cfgmgmt.listeners.CMSaveParticipant;
import org.openmbee.plugin.cfgmgmt.listeners.ProjectListener;
import org.openmbee.plugin.cfgmgmt.managers.IconAdornmentManager;
import org.openmbee.plugin.cfgmgmt.managers.TextAdornmentManager;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.ui.browser.TreeNodeAdornmentManager;
import org.openmbee.plugin.cfgmgmt.actions.context.*;
import org.openmbee.plugin.cfgmgmt.actions.main.*;
import org.openmbee.plugin.cfgmgmt.actions.toolbar.*;
import org.openmbee.plugin.cfgmgmt.domain.*;

import java.net.CookieHandler;
import java.net.CookieManager;

public class ConfigurationManagementPlugin extends Plugin implements IConfigurationManagementPlugin {
    private ChangeRecordStatusAction changeRecordStatusAction;
    private ChangeRecordIWAction changeRecordIWAction;
    private ChangeRecordExpAction changeRecordExpAction;
    private DeactivateChangeRecordAction deactivateChangeRecordAction;
    private SelectChangeRecordAction selectChangeRecordAction;

    public void updateCRStatus() {
        changeRecordStatusAction.updateState();
        changeRecordIWAction.updateState();
        changeRecordExpAction.updateState();
        deactivateChangeRecordAction.updateState();
    }

    @Override
    public void init() {
        // Manage Cookies
        if (CookieHandler.getDefault() == null) {
            CookieHandler.setDefault(new CookieManager());
        }

        // Services
        ConfigurationManagementService configurationManagementService = new ConfigurationManagementService(this);
        TeamworkCloudService teamworkCloudService = new TeamworkCloudService();
        TwcRevisionService twcRevisionService = new TwcRevisionService();
        ModalRestHandler modalRestHandler = new ModalRestHandler();
        ThreeDxClientManager threeDxClientManager = new ThreeDxClientManager();
        ThreeDxService threeDxService = new ThreeDxService(configurationManagementService, threeDxClientManager, modalRestHandler);
        JiraClientManager jiraClientManager = new JiraClientManager();
        JiraService jiraService = new JiraService(configurationManagementService, jiraClientManager, modalRestHandler);
        WssoService wssoService = new WssoService(configurationManagementService);

        configurationManagementService.setThreeDxService(threeDxService);
        configurationManagementService.setTeamworkCloudService(teamworkCloudService);
        configurationManagementService.setJiraService(jiraService);
        configurationManagementService.setWssoService(wssoService);

        // Factories
        LifecycleObjectFactory lifecycleObjectFactory = new LifecycleObjectFactory();
        CmControllerFactory cmControllerFactory = new CmControllerFactory();

        // Domains
        UIDomain uiDomain = new UIDomain();
        ApiDomain apiDomain = new ApiDomain();
        LifecycleObjectDomain lifecycleObjectDomain = new LifecycleObjectDomain(lifecycleObjectFactory, apiDomain, uiDomain);
        ConfiguredElementDomain configuredElementDomain = new ConfiguredElementDomain(lifecycleObjectFactory, apiDomain, uiDomain);
        ChangeRecordDomain changeRecordDomain = new ChangeRecordDomain(lifecycleObjectFactory, apiDomain, uiDomain);
        AbstractCmDispatcher cmDispatcher = new CmDispatcher(cmControllerFactory);

        uiDomain.initializeJavafxThread();
        teamworkCloudService.setUiDomain(uiDomain);
        twcRevisionService.setConfigurationManagementService(configurationManagementService);
        configurationManagementService.setUIDomain(uiDomain);
        configurationManagementService.setApiDomain(apiDomain);
        configurationManagementService.setLifecycleObjectDomain(lifecycleObjectDomain);
        configurationManagementService.setConfiguredElementDomain(configuredElementDomain);
        configurationManagementService.setChangeRecordDomain(changeRecordDomain);
        configurationManagementService.setLifecycleObjectFactory(lifecycleObjectFactory);
        configurationManagementService.setTwcRevisionService(twcRevisionService);

        // Actions
        ConfigureAction configureAction = new ConfigureAction(configurationManagementService);
        ReviseAction reviseAction = new ReviseAction(configurationManagementService);
        ChangeStatusAction changeStatusAction = new ChangeStatusAction(configurationManagementService);
        CreateChangeRecordAction createChangeRecordAction = new CreateChangeRecordAction(configurationManagementService);
        PullChangeRecordAction3Dx pullChangeRecordAction3Dx = new PullChangeRecordAction3Dx(configurationManagementService);
        PullChangeRecordActionJIRA pullChangeRecordActionJIRA = new PullChangeRecordActionJIRA(configurationManagementService);
        SyncChangeRecordAction syncChangeRecordAction = new SyncChangeRecordAction(configurationManagementService);
        SetDescriptionAction setDescAction = new SetDescriptionAction(configurationManagementService);
        SetCommentsAction setCommentsAction = new SetCommentsAction(configurationManagementService);
        JIRALoginAction jiraLoginAction = new JIRALoginAction(configurationManagementService);
        ThreeDXLoginAction threeDxLoginAction = new ThreeDXLoginAction(configurationManagementService);
        ManageAffectedElementsAction manageAffectedElementsAction = new ManageAffectedElementsAction(configurationManagementService);
        ElementHistoryAction elementHistoryAction = new ElementHistoryAction(configurationManagementService, cmDispatcher);

        selectChangeRecordAction = new SelectChangeRecordAction(configurationManagementService);

        changeRecordStatusAction = new ChangeRecordStatusAction(configurationManagementService);
        changeRecordIWAction = new ChangeRecordIWAction(configurationManagementService);
        changeRecordExpAction = new ChangeRecordExpAction(configurationManagementService);
        deactivateChangeRecordAction = new DeactivateChangeRecordAction(this, configurationManagementService);
        AdminModeAction adminModeAction = new AdminModeAction(configurationManagementService);

        // add toolbar configurator
        ActionsConfiguratorsManager.getInstance().addMainToolbarConfigurator(new ToolbarConfigurator(selectChangeRecordAction,
            changeRecordStatusAction,  changeRecordIWAction, changeRecordExpAction, deactivateChangeRecordAction));

        ActionsConfiguratorsManager.getInstance().addMainMenuConfigurator(new MainMenuConfigurator(createChangeRecordAction,
            pullChangeRecordAction3Dx, pullChangeRecordActionJIRA, syncChangeRecordAction, threeDxLoginAction,
            jiraLoginAction, adminModeAction));

        // add icon and text adornment managers
        TreeNodeAdornmentManager.getInstance().addIconAdornment(new IconAdornmentManager(configurationManagementService));
        TreeNodeAdornmentManager.getInstance().addTextAdornment(new TextAdornmentManager(configurationManagementService));

        // add contextual menu configurator
        ActionsConfiguratorsManager.getInstance().addContainmentBrowserContextConfigurator(new ContextualMenuConfigurator(
            configureAction, reviseAction, changeStatusAction, setDescAction, setCommentsAction,
            manageAffectedElementsAction, elementHistoryAction));

        Application.getInstance().addProjectEventListener(new ProjectListener(configurationManagementService));
        Application.getInstance().addSaveParticipant(new CMSaveParticipant(configurationManagementService));
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public SelectChangeRecordAction getSelectChangeRecordAction() {
        return selectChangeRecordAction;
    }

    @Override
    public ChangeRecordExpAction getChangeRecordExpAction() {
        return changeRecordExpAction;
    }

    @Override
    public ChangeRecordIWAction getChangeRecordIWAction() {
        return changeRecordIWAction;
    }

    @Override
    public ChangeRecordStatusAction getChangeRecordStatusAction() {
        return changeRecordStatusAction;
    }
}