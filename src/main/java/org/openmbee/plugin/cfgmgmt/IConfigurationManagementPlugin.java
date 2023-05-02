package org.openmbee.plugin.cfgmgmt;

import org.openmbee.plugin.cfgmgmt.actions.toolbar.ChangeRecordExpAction;
import org.openmbee.plugin.cfgmgmt.actions.toolbar.ChangeRecordIWAction;
import org.openmbee.plugin.cfgmgmt.actions.toolbar.ChangeRecordStatusAction;
import org.openmbee.plugin.cfgmgmt.actions.toolbar.SelectChangeRecordAction;

public interface IConfigurationManagementPlugin {
    void updateCRStatus();
    SelectChangeRecordAction getSelectChangeRecordAction();
    ChangeRecordExpAction getChangeRecordExpAction();
    ChangeRecordIWAction getChangeRecordIWAction();
    ChangeRecordStatusAction getChangeRecordStatusAction();
}
