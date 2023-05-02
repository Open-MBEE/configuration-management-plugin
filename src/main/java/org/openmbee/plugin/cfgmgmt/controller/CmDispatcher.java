package org.openmbee.plugin.cfgmgmt.controller;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.ui.IConfigurationManagementUI;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;

public class CmDispatcher extends AbstractCmDispatcher {
    protected FXMLLoader currentLoader;

    public CmDispatcher(CmControllerFactory cmControllerFactory) {
        this.cmControllerFactory = cmControllerFactory;
    }

    protected Scene loadSceneBySourceType(IConfigurationManagementUI.ConfigurationManagementUiType sourceType, CmControllerSettings cmControllerSettings) {
        Scene scene = null;

        // Loading here instead of BaseJavaFxModule lets us use a controller factory to dependency inject into our
        // controllers and also lets us use the controller factory in a non-static way
        try {
            currentLoader = getLoader(sourceType);
            if (currentLoader != null) {
                setCmControllerFactory(sourceType, cmControllerSettings);
                Parent root = currentLoader.load();
                setController(sourceType);
                scene = getScene(root);
            }
        } catch (IOException e) {
            getLogger().error(String.format(ExceptionConstants.ERROR_DURING_FXML_CONSTRUCTOR_INSTANTIATION, e.getMessage()));
        }

        return scene;
    }

    protected void setCmControllerFactory(IConfigurationManagementUI.ConfigurationManagementUiType sourceType, CmControllerSettings cmControllerSettings) {
        if (sourceType == IConfigurationManagementUI.ConfigurationManagementUiType.ELEMENT_CHANGE_HISTORY_MATRIX) {
            cmControllerFactory.setExpectedParameters(cmControllerSettings);
            getCurrentLoader().setControllerFactory(cmControllerFactory);
        }
    }

    @Override
    protected Object getController() {
        return getCurrentLoader().getController();
    }

    protected Scene getScene(Parent root) {
        return new Scene(root);
    }

    protected FXMLLoader getLoader(IConfigurationManagementUI.ConfigurationManagementUiType sourceType) {
        return new FXMLLoader(getClass().getResource(sourceType.getLocation()));
    }

    protected FXMLLoader getCurrentLoader() {
        return currentLoader;
    }

    @Override
    protected void initializeAndStartController(CmControllerSettings cmControllerSettings) {
        Scene scene = loadSceneBySourceType(cmControllerSettings.getSourceType(), cmControllerSettings);
        if (getCurrentController() != null && scene != null) {
            setCurrentControllerModule();
            getCurrentController().setMainScene(scene);
            getCurrentController().startCmModule();
        }
    }

    @Override
    protected void setCurrentControllerModule() {
        getCurrentController().setModule(currentControllerModule);
    }
}
