package org.openmbee.plugin.cfgmgmt.ui;

import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.awt.*;


public class ConfiguredElementSurfacePainter extends com.nomagic.magicdraw.ui.DiagramSurfacePainter {

    private final ConfigurationManagementService configurationManagementService;

    public ConfiguredElementSurfacePainter(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    @Override
    public void paint(Graphics graphics, DiagramPresentationElement diagram) {
        if (getConfigurationManagementService().isCmActive() &&
                getConfigurationManagementService().getDiagramAdornmentSwitch()) {
            //traverse all symbols in the diagram
            for (PresentationElement presentationElement : diagram.getPresentationElements()) {
                if (presentationElement != null) {
                    draw2DBoxAroundElement(presentationElement, graphics);
                }
            }
        }
    }

    protected void draw2DBoxAroundElement(PresentationElement presentationElement, Graphics graphics) {
        Element element = presentationElement.getElement();
        if (element != null) {
            Color color = handleElementBasicColorCases(element);
            if (color == null) {
                // otherwise, attempt to get the color from the diagram the element is in or the diagram's ccz
                color = getColorFromDiagramOrItsCczOwner(presentationElement.getDiagramPresentationElement());
            }

            if (presentationElement instanceof ShapeElement &&
                    (getConfigurationManagementService().isConfigured(element) ||
                            getConfigurationManagementService().getCCZOwner(element) != null)) {
                Rectangle bounds = presentationElement.getBounds();
                graphics.setColor(color);
                bounds.grow(0, 0);
                if (graphics instanceof Graphics2D) {
                    ((Graphics2D) graphics).draw(bounds);
                }
            }
        }
    }

    protected Color handleGettingColor(ConfiguredElement configuredElement) {
        boolean lifecycleStatusChanging = getConfigurationManagementService().isLifecycleStatusChanging();
        if (lifecycleStatusChanging) {
            getConfigurationManagementService().setLifecycleStatusChanging(false);
        }
        Color color = configuredElement.getStatusColor();
        if (lifecycleStatusChanging) {
            getConfigurationManagementService().setLifecycleStatusChanging(true);
        }
        return color;
    }

    protected Color getColorFromCczOwner(Element element) {
        ConfiguredElement configuredOwner = getConfigurationManagementService().getCCZOwner(element);
        if (configuredOwner != null) {
            return handleGettingColor(configuredOwner);
        }
        return null;
    }

    protected Color getColorFromDiagramOrItsCczOwner(DiagramPresentationElement diagramPresentationElement) {
        return diagramPresentationElement != null && diagramPresentationElement.getElement() != null ?
                handleElementBasicColorCases(diagramPresentationElement.getElement()) : null;
    }

    protected Color handleElementBasicColorCases(Element element) {
        ConfiguredElement configuredElement = getConfigurationManagementService().getConfiguredElement(element);
        if (configuredElement != null) {
            return handleGettingColor(configuredElement);
        } else {
            return getColorFromCczOwner(element);
        }
    }
}
