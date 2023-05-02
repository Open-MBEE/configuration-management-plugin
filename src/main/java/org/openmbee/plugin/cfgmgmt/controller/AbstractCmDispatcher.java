package org.openmbee.plugin.cfgmgmt.controller;

import org.openmbee.plugin.cfgmgmt.ConfigurationManagementPlugin;
import org.openmbee.plugin.cfgmgmt.application.BaseModule;
import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.ui.IConfigurationManagementUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCmDispatcher {
    private final Logger logger = LoggerFactory.getLogger(AbstractCmDispatcher.class);

    protected ElementChangeHistoryController currentController;
    protected CmControllerFactory cmControllerFactory;
    protected BaseModule currentControllerModule;

    public void invokeCmController(CmControllerSettings cmControllerSettings, BaseModule module) {
        prepareCmController(module);
        Thread currentThread = Thread.currentThread();
        ClassLoader currentClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(ConfigurationManagementPlugin.class.getClassLoader());
            initializeAndStartController(cmControllerSettings);
        } finally {
            currentThread.setContextClassLoader(currentClassLoader);
        }
    }

    protected void prepareCmController(BaseModule currentControllerModule) {
        if (getCurrentController() != null) {
            getCurrentController().stopModule();
            getCurrentController().resetForNextStart();
            currentController = null;
        }
        this.currentControllerModule = currentControllerModule;
    }

    protected ElementChangeHistoryController getCurrentController() {
        return currentController;
    }

    protected abstract Object getController();

    protected void setController(IConfigurationManagementUI.ConfigurationManagementUiType sourceType) {
        Object controller = getController();
        if (sourceType == IConfigurationManagementUI.ConfigurationManagementUiType.ELEMENT_CHANGE_HISTORY_MATRIX) {
            currentController = (ElementChangeHistoryController) controller;
        }
    }

    protected Logger getLogger() {
        return logger;
    }

    protected abstract void initializeAndStartController(CmControllerSettings ftaControllerSettings);

    protected abstract void setCurrentControllerModule();
}
