package org.openmbee.plugin.cfgmgmt.application;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.ui.IConfigurationManagementUI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class BaseModule extends Application implements IConfigurationManagementUI {
    private static final Logger logger = LoggerFactory.getLogger(BaseModule.class);
    protected Stage stage;
    protected Scene scene;
    protected Stage primaryStage;
    private String title;
    private ConfigurationManagementUiType uiType;

    protected ClassLoader getClassLoader(Thread currentThread) {
        ClassLoader currentClassLoader = currentThread.getContextClassLoader();
        if (!Platform.isFxApplicationThread()) {
            try {
                throw new IllegalStateException(ExceptionConstants.JAVAFX_ILLEGAL_STATE_MESSAGE + currentThread.getName());
            } catch (IllegalStateException e) {
                getLogger().error(ExceptionConstants.JAVAFX_THREAD_EXCEPTION, e.getMessage());
            }
        }
        return currentClassLoader;
    }

    protected void moduleStartRoutine() throws IOException {
        if (stage == null) {
            stage = getPrimaryStage();
            if (stage == null) {
                return;
            }
            start(stage);
            stage.setOnCloseRequest(event -> stop(true, stage));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.show();
    }

    protected void stop(boolean exit, Stage primaryStage) {
        if (exit && primaryStage != null) {
            primaryStage.hide();
        }
        stage = null;
    }

    public Scene getScene(ConfigurationManagementUiType uiType) throws IOException {
        if (scene != null) {
            return scene;
        }
        FXMLLoader loader = getLoader(uiType.location);
        if(loader != null) {
            Parent root = loader.load();
            scene = createScene(root);
        }

        return scene;
    }

    protected Scene createScene(Parent root) {
        return new Scene(root);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    protected abstract FXMLLoader getLoader(String location);

    public Stage getPrimaryStage() throws IOException {
        if (primaryStage != null) {
            return primaryStage;
        }
        primaryStage = createStage();
        primaryStage.setResizable(false);
        primaryStage.setTitle(title);
        primaryStage.setScene(getScene(uiType));

        return primaryStage;
    }

    public void showStage(boolean showStage) {
        if(primaryStage != null) {
            if(showStage) {
                primaryStage.show();
            } else {
                primaryStage.hide();
            }
        }
    }

    protected Stage createStage() {
        return new Stage();
    }

    protected void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setUiType(ConfigurationManagementUiType uiLocation) {
        this.uiType = uiLocation;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected String getTitle() {
        return title;
    }

    protected Logger getLogger() {
        return logger;
    }
}
