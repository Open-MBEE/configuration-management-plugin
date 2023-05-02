package org.openmbee.plugin.cfgmgmt.listeners;

import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestCMSaveParticipant {
    @Mock
    private Project project;
    @Mock
    private ProjectDescriptor projectDescriptor;
    @Mock
    private ConfigurationManagementService configurationManagementService;
    @Mock
    private ApiDomain apiDomain;
    @Spy
    @InjectMocks
    private CMSaveParticipant cmSaveParticipant;

    @Test
    public void isReadyForSave() {
        assertTrue(cmSaveParticipant.isReadyForSave(project, projectDescriptor));
    }

    @Test
    public void doBeforeSave_notActive() {
        when(cmSaveParticipant.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(configurationManagementService.isCmActive()).thenReturn(false);

        cmSaveParticipant.doBeforeSave(project, projectDescriptor);

        verify(cmSaveParticipant, never()).getApiDomain();
    }

    @Test
    public void doBeforeSave_elementNotConfigured() {
        doReturn(configurationManagementService).when(cmSaveParticipant).getConfigurationManagementService();
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(cmSaveParticipant.getApiDomain()).thenReturn(apiDomain);
        Element e = mock(Element.class);
        Stereotype ce = mock(Stereotype.class);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(ce);
        when(apiDomain.getExtendedElementsIncludingDerived(ce)).thenReturn(List.of(e));
        when(configurationManagementService.getConfiguredElement(e)).thenReturn(null);

        cmSaveParticipant.doBeforeSave(project, projectDescriptor);

        verify(configurationManagementService).getConfiguredElement(e);
        verifyNoInteractions(e);
        verify(cmSaveParticipant, never()).getUIDomain();
    }

    @Test
    public void doBeforeSave_elementCommitted() {
        doReturn(configurationManagementService).when(cmSaveParticipant).getConfigurationManagementService();
        when(configurationManagementService.isCmActive()).thenReturn(true);
        doReturn(apiDomain).when(cmSaveParticipant).getApiDomain();
        Element e = mock(Element.class);
        Stereotype ce = mock(Stereotype.class);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(ce);
        when(apiDomain.getExtendedElementsIncludingDerived(ce)).thenReturn(List.of(e));
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);
        when(configurationManagementService.getConfiguredElement(e)).thenReturn(configuredElement);
        when(configuredElement.isCommitted()).thenReturn(true);

        cmSaveParticipant.doBeforeSave(project, projectDescriptor);

        verify(configurationManagementService).getConfiguredElement(e);
        verify(configuredElement).isCommitted();
        verify(cmSaveParticipant, never()).getUIDomain();
        verify(configuredElement, never()).setIsCommitted(true);
    }

    @Test
    public void doBeforeSave_elementNotReleased() {
        doReturn(configurationManagementService).when(cmSaveParticipant).getConfigurationManagementService();
        when(configurationManagementService.isCmActive()).thenReturn(true);
        doReturn(apiDomain).when(cmSaveParticipant).getApiDomain();
        Element e = mock(Element.class);
        Stereotype ce = mock(Stereotype.class);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(ce);
        when(apiDomain.getExtendedElementsIncludingDerived(ce)).thenReturn(List.of(e));
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);
        when(configurationManagementService.getConfiguredElement(e)).thenReturn(configuredElement);
        when(configuredElement.isCommitted()).thenReturn(false);
        when(configuredElement.isReleased()).thenReturn(false);

        cmSaveParticipant.doBeforeSave(project, projectDescriptor);

        verify(configurationManagementService).getConfiguredElement(e);
        verify(configuredElement).isReleased();
        verify(cmSaveParticipant, never()).getUIDomain();
        verify(configuredElement, never()).setIsCommitted(true);
    }

    @Test
    public void doBeforeSave_elementNotCommittedAndReleased() {
        doReturn(configurationManagementService).when(cmSaveParticipant).getConfigurationManagementService();
        when(configurationManagementService.isCmActive()).thenReturn(true);
        doReturn(apiDomain).when(cmSaveParticipant).getApiDomain();
        Element e = mock(Element.class);
        Stereotype ce = mock(Stereotype.class);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(ce);
        when(apiDomain.getExtendedElementsIncludingDerived(ce)).thenReturn(List.of(e));
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);
        when(configurationManagementService.getConfiguredElement(e)).thenReturn(configuredElement);
        when(configuredElement.isCommitted()).thenReturn(false);
        when(configuredElement.isReleased()).thenReturn(true);

        cmSaveParticipant.doBeforeSave(project, projectDescriptor);

        verify(configurationManagementService).getConfiguredElement(e);
        verify(configuredElement).isReleased();
        verify(cmSaveParticipant, never()).getUIDomain();
        verify(configuredElement).setIsCommitted(true);
    }
}