package org.openmbee.plugin.cfgmgmt.configurators;

import org.openmbee.plugin.cfgmgmt.actions.context.*;
import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.ui.browser.Tree;
import org.openmbee.plugin.cfgmgmt.actions.context.*;

public class ContextualMenuConfigurator implements BrowserContextAMConfigurator {
    private ConfigureAction configureAction;
    private ReviseAction reviseAction;
    private ChangeStatusAction changeStatusAction;
    private SetDescriptionAction setDescriptionAction;
    private SetCommentsAction setCommentsAction;
    private ManageAffectedElementsAction manageAffectedElementsAction;
    private ElementHistoryAction elementHistoryAction;

    public ContextualMenuConfigurator(ConfigureAction configureAction,
                                      ReviseAction reviseAction,
                                      ChangeStatusAction changeStatusAction,
                                      SetDescriptionAction setDescriptionAction,
                                      SetCommentsAction setCommentsAction,
                                      ManageAffectedElementsAction manageAffectedElementsAction,
                                      ElementHistoryAction elementHistoryAction) {
        this.configureAction = configureAction;
        this.reviseAction = reviseAction;
        this.changeStatusAction = changeStatusAction;
        this.setDescriptionAction = setDescriptionAction;
        this.setCommentsAction = setCommentsAction;
        this.manageAffectedElementsAction = manageAffectedElementsAction;
        this.elementHistoryAction = elementHistoryAction;
    }

    @Override
    public void configure(ActionsManager actionsManager, Tree tree) {
        MDActionsCategory category = new MDActionsCategory("Configuration Management", "Configuration Management");
        category.setNested(true);
        category.addAction(configureAction);
        category.addAction(reviseAction);
        category.addAction(changeStatusAction);
        category.addAction(setDescriptionAction);
        category.addAction(setCommentsAction);
        category.addAction(manageAffectedElementsAction);
        category.addAction(elementHistoryAction);

        actionsManager.addCategory(category);
    }

    @Override
    public int getPriority() {
        return AMConfigurator.HIGH_PRIORITY;
    }
}
