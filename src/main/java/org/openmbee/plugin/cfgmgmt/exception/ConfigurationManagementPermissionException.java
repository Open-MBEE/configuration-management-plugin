package org.openmbee.plugin.cfgmgmt.exception;

public class ConfigurationManagementPermissionException extends Exception {
    public ConfigurationManagementPermissionException(String msg) {
        super(msg);
    }

    public ConfigurationManagementPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
