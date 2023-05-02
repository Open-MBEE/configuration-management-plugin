package org.openmbee.plugin.cfgmgmt.application;

import org.openmbee.plugin.cfgmgmt.ConfigurationManagementPlugin;
import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseModuleStart implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(BaseModuleStart.class);
    private final BaseModule module;

    public BaseModuleStart(BaseModule module) {
        this.module = module;
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        ClassLoader currentClassLoader = module.getClassLoader(currentThread);
        try {
            currentThread.setContextClassLoader(ConfigurationManagementPlugin.class.getClassLoader());
            module.moduleStartRoutine();
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
