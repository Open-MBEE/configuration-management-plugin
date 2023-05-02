package org.openmbee.plugin.cfgmgmt.ui;

public interface IConfigurationManagementUI {
    String ELEMENT_CHANGE_HISTORY_LOCATION = "/fxml/elementChangeHistory.fxml";

    enum ConfigurationManagementUiType {
        ELEMENT_CHANGE_HISTORY_MATRIX(ELEMENT_CHANGE_HISTORY_LOCATION);

        public final String location;

        ConfigurationManagementUiType(String location) {
            this.location = location;
        }

        public String getLocation() {
            return location;
        }
    }
}
