package org.openmbee.plugin.cfgmgmt.application;

import org.openmbee.plugin.cfgmgmt.ConfigurationManagementPlugin;
import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseModuleStop implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(BaseModuleStop.class);
    private final BaseModule module;

    public BaseModuleStop(BaseModule module) {
        this.module = module;
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        ClassLoader currentClassLoader = module.getClassLoader(currentThread);
        try {
            currentThread.setContextClassLoader(ConfigurationManagementPlugin.class.getClassLoader());
            module.stop(true, module.getPrimaryStage());
        } catch (Exception e) {
            getLogger().error(ExceptionConstants.IO_EXCEPTION_ERROR_MESSAGE, e.getMessage());
        } finally {
            currentThread.setContextClassLoader(currentClassLoader);
        }
    }

    protected Logger getLogger() {
        return logger;
    }
}
