package org.openmbee.plugin.cfgmgmt.ui;

import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.properties.PropertyID;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElementColorEnum;
import com.nomagic.magicdraw.uml.symbols.paths.ControlFlowView;
import com.nomagic.magicdraw.uml.symbols.paths.PathElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.magicdraw.uml.symbols.paths.PathElementRenderer;

import java.awt.*;

public class ConfiguredPathRenderer extends PathElementRenderer {

    private final ConfigurationManagementService configurationManagementService;

    public ConfiguredPathRenderer(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
    }

    public ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    @Override
    public int getLineWidth(PathElement pathElement) {
        return (int) pathElement.getProperty(PropertyID.LINE_WIDTH).getValue();
    }

    @Override
    public BasicStroke getPathStroke(PathElement pathElement)
    {
        if (pathElement instanceof ControlFlowView) {
            return new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {8f}, 0f);
        }
        else return null;
    }

    @Override
    public Color getColor(PresentationElement presentationElement, PresentationElementColorEnum colorEnum) {
        try {
            if (colorEnum.equals(PresentationElementColorEnum.LINE)) {
                Element element = presentationElement.getElement();
                ConfiguredElement configuredElement = getConfigurationManagementService().getConfiguredElement(element);
                if (configuredElement != null) {
                    return configuredElement.getStatusColor();
                } else {
                    ConfiguredElement ce = getConfigurationManagementService().getCCZOwner(element);
                    if (ce != null) {
                        return ce.getStatusColor();
                    }
                }
            }
        } catch (Exception e) {
            return super.getColor(presentationElement, colorEnum);
        }
        return super.getColor(presentationElement, colorEnum);
    }
}
