package org.openmbee.plugin.cfgmgmt.ui;

import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

public class TestConfiguredElementSurfacePainter {
    private ConfiguredElementSurfacePainter configuredElementSurfacePainter;
    private ConfigurationManagementService configurationManagementService;
    private PresentationElement presentationElement;
    private Graphics graphics;
    private DiagramPresentationElement diagram;
    private Element element;
    private ConfiguredElement configuredElement;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        configuredElementSurfacePainter = spy(new ConfiguredElementSurfacePainter(configurationManagementService));
        presentationElement = mock(PresentationElement.class);
        graphics = mock(Graphics.class);
        diagram = mock(DiagramPresentationElement.class);
        element = mock(Element.class);
        configuredElement = mock(ConfiguredElement.class);

        when(configuredElementSurfacePainter.getConfigurationManagementService()).thenReturn(configurationManagementService);
    }

    @Test
    public void paint_cmInactiveTest() {
        doReturn(false).when(configurationManagementService).isCmActive();

        configuredElementSurfacePainter.paint(graphics, diagram);

        verify(graphics, never()).setColor(any());
    }

    @Test
    public void paint_cmInactiveTest2() {
        doReturn(false).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).getDiagramAdornmentSwitch();

        configuredElementSurfacePainter.paint(graphics, diagram);

        verify(graphics, never()).setColor(any());
    }

    @Test
    public void paint_adornmentSwitchTest() {
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(false).when(configurationManagementService).getDiagramAdornmentSwitch();

        configuredElementSurfacePainter.paint(graphics, diagram);

        verify(graphics, never()).setColor(any());
    }

    @Test
    public void paint_nullPresentationElementList() {
        ArrayList<PresentationElement> list = new ArrayList<>();
        list.add(null);

        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).getDiagramAdornmentSwitch();
        doReturn(list).when(diagram).getPresentationElements();

        configuredElementSurfacePainter.paint(graphics, diagram);

        verify(graphics, never()).setColor(any());
    }

    @Test
    public void paint_presentationElementHasNoElement() {
        ArrayList<PresentationElement> list = new ArrayList<>();
        list.add(presentationElement);

        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).getDiagramAdornmentSwitch();
        doReturn(list).when(diagram).getPresentationElements();
        doReturn(null).when(presentationElement).getElement();

        configuredElementSurfacePainter.paint(graphics, diagram);

        verify(configuredElementSurfacePainter, never()).handleElementBasicColorCases(any());
    }

    @Test
    public void paint_presentationElementHasElement() {
        ArrayList<PresentationElement> list = new ArrayList<>();
        list.add(presentationElement);

        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).getDiagramAdornmentSwitch();
        doReturn(list).when(diagram).getPresentationElements();
        doReturn(element).when(presentationElement).getElement();

        configuredElementSurfacePainter.paint(graphics, diagram);

        verify(configuredElementSurfacePainter).draw2DBoxAroundElement(presentationElement, graphics);
    }

    @Test
    public void paint_shapeElementIsConfigured() {
        Graphics2D graphics = mock(Graphics2D.class);
        Rectangle bounds = mock(Rectangle.class);
        ArrayList<PresentationElement> list = new ArrayList<>();
        ShapeElement presentationElement = mock(ShapeElement.class);
        list.add(presentationElement);

        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).getDiagramAdornmentSwitch();
        doReturn(list).when(diagram).getPresentationElements();
        doReturn(element).when(presentationElement).getElement();
        doReturn(true).when(configurationManagementService).isConfigured(element);
        doReturn(bounds).when(presentationElement).getBounds();
        doNothing().when(graphics).draw(bounds);

        configuredElementSurfacePainter.paint(graphics, diagram);

        verify(presentationElement).getBounds();
    }

    @Test
    public void paint_shapeElementNotConfigured() {
        Graphics2D graphics = mock(Graphics2D.class);
        Rectangle bounds = mock(Rectangle.class);
        ArrayList<PresentationElement> list = new ArrayList<>();
        ShapeElement presentationElement = mock(ShapeElement.class);
        list.add(presentationElement);

        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).getDiagramAdornmentSwitch();
        doReturn(list).when(diagram).getPresentationElements();
        doReturn(element).when(presentationElement).getElement();
        doReturn(false).when(configurationManagementService).isConfigured(element);
        doReturn(null).when(configurationManagementService).getCCZOwner(element);
        doReturn(bounds).when(presentationElement).getBounds();

        configuredElementSurfacePainter.paint(graphics, diagram);

        verify(presentationElement, never()).getBounds();
    }

    @Test
    public void paint_shapeElementHasConfiguredCCZOwner() {
        Graphics2D graphics = mock(Graphics2D.class);
        Rectangle bounds = mock(Rectangle.class);
        ArrayList<PresentationElement> list = new ArrayList<>();
        ShapeElement presentationElement = mock(ShapeElement.class);
        list.add(presentationElement);

        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).getDiagramAdornmentSwitch();
        doReturn(list).when(diagram).getPresentationElements();
        doReturn(element).when(presentationElement).getElement();
        doReturn(false).when(configurationManagementService).isConfigured(element);
        doReturn(configuredElement).when(configurationManagementService).getCCZOwner(element);
        doReturn(bounds).when(presentationElement).getBounds();
        doNothing().when(graphics).draw(bounds);

        configuredElementSurfacePainter.paint(graphics, diagram);
        verify(presentationElement).getBounds();
    }

    @Test
    public void paint_lifecycleChangingTest() {
        doReturn(true).when(configurationManagementService).isLifecycleStatusChanging();

        configuredElementSurfacePainter.handleGettingColor(configuredElement);

        verify(configurationManagementService).setLifecycleStatusChanging(false);
        verify(configurationManagementService).setLifecycleStatusChanging(true);
    }

    @Test
    public void paint_getColorFromDiagramOrItsCczOwnerTest() {
        DiagramPresentationElement diagramPresentationElement = mock(DiagramPresentationElement.class);
        doReturn(null).when(diagramPresentationElement).getElement();

        assertNull(configuredElementSurfacePainter.getColorFromDiagramOrItsCczOwner(diagramPresentationElement));
    }

    @Test
    public void paint_getColorFromDiagramOrItsCczOwnerTest2() {
        DiagramPresentationElement diagramPresentationElement = mock(DiagramPresentationElement.class);
        Diagram diagram = mock(Diagram.class);

        doReturn(diagram).when(diagramPresentationElement).getElement();

        configuredElementSurfacePainter.getColorFromDiagramOrItsCczOwner(diagramPresentationElement);

        verify(configuredElementSurfacePainter).handleElementBasicColorCases(diagram);
    }

    @Test
    public void paint_handleElementBasicColorCases() {
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(element);

        configuredElementSurfacePainter.handleElementBasicColorCases(element);

        verify(configuredElementSurfacePainter).handleGettingColor(configuredElement);
    }

    @Test
    public void draw2DBoxAroundElement_configuredElement() {
        doReturn(Color.RED).when(configuredElementSurfacePainter).handleElementBasicColorCases(element);

        configuredElementSurfacePainter.draw2DBoxAroundElement(presentationElement, graphics);

        verify(configuredElementSurfacePainter, never()).getColorFromDiagramOrItsCczOwner(any());
    }
}
