package org.openmbee.plugin.cfgmgmt.controller;

import org.openmbee.plugin.cfgmgmt.application.BaseModule;
import org.openmbee.plugin.cfgmgmt.application.BaseModuleStart;
import org.openmbee.plugin.cfgmgmt.application.BaseModuleStop;
import javafx.application.Platform;

public class BaseFxController {

    protected void executeJavaFxRunnable(Runnable runnable) {
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            Platform.runLater(runnable);
        }
    }

    public void startModule(BaseModule module) {
        executeJavaFxRunnable(getBaseModuleStart(module));
    }

    protected BaseModuleStart getBaseModuleStart(BaseModule module) {
        return new BaseModuleStart(module); // allows unit testing
    }

    public void stopModule(BaseModule module) {
        executeJavaFxRunnable(getBaseModuleStop(module));
    }

    protected BaseModuleStop getBaseModuleStop(BaseModule module) {
        return new BaseModuleStop(module); // allows unit testing
    }
}
