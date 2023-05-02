package org.openmbee.plugin.cfgmgmt.actions.toolbar;

import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserStateAction;

import java.awt.event.ActionEvent;

public class ChangeRecordStatusAction extends DefaultBrowserStateAction {
    private final transient ConfigurationManagementService configurationManagementService;
    public ChangeRecordStatusAction(ConfigurationManagementService configurationManagementService) {
        super("RECORD_STATUS_ACTION", "Status", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    public void clearState() {
        setName("Status: -");
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
    }

    @Override
    public void updateState() {
        setEnabled(false);
        ChangeRecord changeRecord = getConfigurationManagementService().getSelectedChangeRecord();
        String status = "-";
        if (changeRecord != null) {
            status = changeRecord.getStatusName();
            if(status == null) {
                status = "-";
            }
        }
        setName(String.format("Status: %s", status));
    }
}
