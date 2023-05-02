package org.openmbee.plugin.cfgmgmt.application;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;

public class ElementHistoryMatrixModule extends BaseModule {

    @Override
    protected FXMLLoader getLoader(String location) {
        return new FXMLLoader(ElementHistoryMatrixModule.class.getResource(location));
    }

    @Override
    public Stage getPrimaryStage() throws IOException {
        setTitle(PluginConstant.ELEMENT_CHANGE_HISTORY_MATRIX_TITLE);
        setUiType(ConfigurationManagementUiType.ELEMENT_CHANGE_HISTORY_MATRIX);
        return super.getPrimaryStage();
    }
}
