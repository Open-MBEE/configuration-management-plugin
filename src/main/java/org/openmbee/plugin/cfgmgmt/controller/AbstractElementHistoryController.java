package org.openmbee.plugin.cfgmgmt.controller;

import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.view.ElementHistoryRowView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractElementHistoryController {
    protected Logger logger = LoggerFactory.getLogger(AbstractElementHistoryController.class);

    protected CmControllerSettings cmControllerSettings;

    public void startCmModule() {
        startModule();
        initializeColumns();
        updateElementHistoryUI(cmControllerSettings.getRowViews(), cmControllerSettings.getConfiguredElementId(), cmControllerSettings.getConfiguredElementName());
        populateElementHistoryTable();
    }

    protected Logger getLogger() {
        return logger;
    }

    public abstract void startModule();
    public abstract void stopModule();

    protected abstract void resetForNextStart();

    protected abstract void initializeColumns();
    protected abstract void updateElementHistoryUI(List<ElementHistoryRowView> rows, String id, String name);
    protected abstract void populateElementHistoryTable();
}
