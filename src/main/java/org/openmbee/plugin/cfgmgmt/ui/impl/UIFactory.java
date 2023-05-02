package org.openmbee.plugin.cfgmgmt.ui.impl;

import org.openmbee.plugin.cfgmgmt.application.*;
import org.openmbee.plugin.cfgmgmt.ui.*;
import org.openmbee.plugin.cfgmgmt.application.BaseModule;
import org.openmbee.plugin.cfgmgmt.application.ElementHistoryMatrixModule;
import org.openmbee.plugin.cfgmgmt.ui.IUIFactory;

public class UIFactory implements IUIFactory {

    @Override
    public BaseModule getElementChangeHistoryMatrixUI() {
        return new ElementHistoryMatrixModule();
    }
}
