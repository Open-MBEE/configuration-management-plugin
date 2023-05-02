package org.openmbee.plugin.cfgmgmt.utils;

import javafx.scene.image.*;

import java.net.*;

public class ConfigurationManagementUtil {

    public static Image getImageFromResources(Class requestingClass, String location) {
        if (requestingClass != null && location != null) {
            URL url = requestingClass.getResource(location);
            return url != null ? new Image(url.toString()) : null;
        }
        return null;
    }
}
