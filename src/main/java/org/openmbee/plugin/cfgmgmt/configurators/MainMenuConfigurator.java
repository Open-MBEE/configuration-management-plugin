package org.openmbee.plugin.cfgmgmt.configurators;

import org.openmbee.plugin.cfgmgmt.actions.main.*;
import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import org.openmbee.plugin.cfgmgmt.actions.main.*;

public class MainMenuConfigurator implements AMConfigurator {
    private CreateChangeRecordAction createChangeRecordAction;
    private PullChangeRecordAction3Dx pullChangeRecordAction3Dx;
    private PullChangeRecordActionJIRA pullChangeRecordActionJIRA;
    private SyncChangeRecordAction syncChangeRecordAction;
    private JIRALoginAction jiraLoginAction;
    private ThreeDXLoginAction threeDxLoginAction;
    private AdminModeAction adminModeAction;

    public MainMenuConfigurator(CreateChangeRecordAction createChangeRecordAction,
                                PullChangeRecordAction3Dx pullChangeRecordAction3Dx,
                                PullChangeRecordActionJIRA pullChangeRecordActionJIRA,
                                SyncChangeRecordAction syncChangeRecordAction,
                                ThreeDXLoginAction threeDxLoginAction,
                                JIRALoginAction jiraLoginAction,
                                AdminModeAction adminModeAction) {
        this.createChangeRecordAction = createChangeRecordAction;
        this.pullChangeRecordAction3Dx = pullChangeRecordAction3Dx;
        this.pullChangeRecordActionJIRA = pullChangeRecordActionJIRA;
        this.syncChangeRecordAction = syncChangeRecordAction;
        this.threeDxLoginAction = threeDxLoginAction;
        this.jiraLoginAction = jiraLoginAction;
        this.adminModeAction = adminModeAction;
    }

    @Override
    public void configure(ActionsManager actionsManager) {
        MDActionsCategory category = new MDActionsCategory("Configuration Management", "Configuration Management");
        category.setNested(true);
        category.addAction(createChangeRecordAction);
        category.addAction(pullChangeRecordAction3Dx);
        category.addAction(pullChangeRecordActionJIRA);
        category.addAction(threeDxLoginAction);
        category.addAction(jiraLoginAction);
        category.addAction(syncChangeRecordAction);
        category.addAction(adminModeAction);
        actionsManager.addCategory(category);
    }

    @Override
    public int getPriority() {
        return AMConfigurator.MEDIUM_PRIORITY;
    }
}
