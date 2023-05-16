package org.openmbee.plugin.cfgmgmt.ui.impl;

import org.openmbee.plugin.cfgmgmt.application.*;
import org.openmbee.plugin.cfgmgmt.ui.*;

public class UIFactory implements IUIFactory {

    @Override
    public BaseModule getElementChangeHistoryMatrixUI() {
        return new ElementHistoryMatrixModule();
    }
}
