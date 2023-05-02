package org.openmbee.plugin.cfgmgmt.managers;

import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.exception.NoIconException;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.IconAdornment;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.ui.ScalableImageIcon;
import com.nomagic.ui.SquareIcon;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class IconAdornmentManager implements IconAdornment {

    private final ConfigurationManagementService configurationManagementService;

    public IconAdornmentManager(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    @CheckForNull
    @Override
    public Icon adorn(Component component, @CheckForNull Icon icon, BaseElement baseElement) {
        if(icon != null) {
            if(baseElement instanceof Element) {
                try {
                    ConfiguredElement configuredElement = getConfigurationManagementService().getConfiguredElement((Element) baseElement);
                    if (configuredElement != null) {
                        Image img = ((SquareIcon) icon).getImage();

                        BufferedImage newImg = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

                        Graphics graphics = newImg.getGraphics();
                        graphics.drawImage(img, 0, 0, null);

                        int iconSize = icon.getIconHeight();
                        try {
                            graphics.setColor(configuredElement.getStatusColor());
                        } catch (Exception e) {
                            getUIDomain().logError(e.getMessage());
                        }
                        graphics.setFont(new Font("Candara", Font.BOLD, iconSize / 2));
                        graphics.drawString("C", iconSize - (iconSize / 3), (iconSize / 2) - 1);
                        return new ScalableImageIcon(newImg);
                    }
                } catch (Exception e) {
                    getUIDomain().logError(e.getMessage());
                }
            }
            return new ScalableImageIcon(((SquareIcon) icon).getImage());
        }
        throw new NoIconException("Cannot adorn because a null icon was provided."); // runtime issue at this point
    }
}
