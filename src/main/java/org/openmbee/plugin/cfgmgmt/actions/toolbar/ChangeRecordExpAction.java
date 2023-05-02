package org.openmbee.plugin.cfgmgmt.actions.toolbar;

import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserStateAction;

import java.awt.event.ActionEvent;
import java.util.Optional;

public class ChangeRecordExpAction extends DefaultBrowserStateAction {
    private final transient ConfigurationManagementService configurationManagementService;
    public ChangeRecordExpAction(ConfigurationManagementService configurationManagementService) {
        super("RECORD_EXP_ACTION", "EX", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    public void clearState() {
        setState(false);
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
    }

    @Override
    public void updateState() {
        setEnabled(false);
        ChangeRecord changeRecord = getConfigurationManagementService().getSelectedChangeRecord();
        if(changeRecord != null) {
            Optional<LifecycleStatus> status = changeRecord.getStatus();
            setState(status.isPresent() && status.get().isExpandable());
        } else {
            setState(false);
        }
    }
}
