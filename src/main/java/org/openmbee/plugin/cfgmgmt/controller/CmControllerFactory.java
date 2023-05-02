package org.openmbee.plugin.cfgmgmt.controller;

import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.ui.impl.AbstractBaseControllerFactory;
import javafx.util.Callback;

import java.util.List;

public class CmControllerFactory extends AbstractBaseControllerFactory implements Callback<Class<?>, Object> {
    @Override
    public Object call(Class<?> classType) {
        return handleCall(classType);
    }

    public void setExpectedParameters(CmControllerSettings cmControllerSettings) {
        setExpectedParameters(List.of(cmControllerSettings));
    }
}
