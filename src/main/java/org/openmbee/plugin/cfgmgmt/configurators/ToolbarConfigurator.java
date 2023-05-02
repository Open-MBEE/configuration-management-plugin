package org.openmbee.plugin.cfgmgmt.configurators;

import org.openmbee.plugin.cfgmgmt.actions.toolbar.DeactivateChangeRecordAction;
import org.openmbee.plugin.cfgmgmt.actions.toolbar.ChangeRecordExpAction;
import org.openmbee.plugin.cfgmgmt.actions.toolbar.ChangeRecordIWAction;
import org.openmbee.plugin.cfgmgmt.actions.toolbar.ChangeRecordStatusAction;
import com.nomagic.actions.*;
import com.nomagic.magicdraw.actions.MDActionsCategory;

//public class ToolbarConfigurator implements BrowserToolbarAMConfigurator
public class ToolbarConfigurator implements AMConfigurator {
    private SelectItemAction selectChangeRecordAction;
    private ChangeRecordStatusAction changeRecordStatusAction;
    private ChangeRecordIWAction changeRecordIWAction;
    private ChangeRecordExpAction changeRecordExpAction;
    private DeactivateChangeRecordAction deactivateChangeRecordAction;

    public ToolbarConfigurator(SelectItemAction selectChangeRecordAction,
                               ChangeRecordStatusAction changeRecordStatusAction,
                               ChangeRecordIWAction changeRecordIWAction,
                               ChangeRecordExpAction changeRecordExpAction,
                               DeactivateChangeRecordAction deactivateChangeRecordAction) {
        this.selectChangeRecordAction = selectChangeRecordAction;
        this.changeRecordStatusAction = changeRecordStatusAction;
        this.changeRecordIWAction = changeRecordIWAction;
        this.changeRecordExpAction = changeRecordExpAction;
        this.deactivateChangeRecordAction = deactivateChangeRecordAction;
    }

    @Override
    public void configure(ActionsManager actionsManager) {
        MDActionsCategory category = new MDActionsCategory("Configuration Management", "Configuration Management");
        category.addAction(selectChangeRecordAction);
        category.addAction(deactivateChangeRecordAction);
        category.addAction(changeRecordStatusAction);
        category.addAction(changeRecordIWAction);
        category.addAction(changeRecordExpAction);
        actionsManager.addCategory(category);
    }

    @Override
    public int getPriority() {
        return AMConfigurator.MEDIUM_PRIORITY;
    }

}
