package org.openmbee.plugin.cfgmgmt.permissions;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestCMElementPermissions {
    @Mock
    private ConfigurationManagementService configurationManagementService;
    @Spy
    @InjectMocks
    private CMElementPermissions cmElementPermissions;
    @Mock
    private BaseElement baseElement;
    @Mock
    private Element element;
    @Mock
    private Package pkg;
    @Mock
    private ConfiguredElement configuredElement;
    @Mock
    private Diagram diagram;
    @Mock
    private DiagramPresentationElement diagramPresentationElement;
    @Mock
    private PresentationElement presentationElement;

    @Test
    public void isElementEditable_cmNotActive() {
        when(configurationManagementService.isCmActive()).thenReturn(false);

        boolean result = cmElementPermissions.isElementEditable(baseElement);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verifyNoMoreInteractions(configurationManagementService, baseElement);
    }

    @Test
    public void isElementEditable_cmAdmin() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(true);

        boolean result = cmElementPermissions.isElementEditable(baseElement);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verify(configurationManagementService).getAdminMode();
        verifyNoMoreInteractions(configurationManagementService, baseElement);
    }

    @Test
    public void isElementEditable_baseElement() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);

        boolean result = cmElementPermissions.isElementEditable(baseElement);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verify(configurationManagementService).getAdminMode();
        verifyNoMoreInteractions(configurationManagementService, baseElement);
    }

    @Test
    public void isElementEditable_presentationElement_notConfigured() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);
        when(presentationElement.getDiagramPresentationElement()).thenReturn(diagramPresentationElement);
        when(diagramPresentationElement.getDiagram()).thenReturn(diagram);
        doReturn(null).when(cmElementPermissions).getConfiguredElement(diagram);

        boolean result = cmElementPermissions.isElementEditable(presentationElement);

        assertTrue(result);

        verify(cmElementPermissions).getConfiguredElement(diagram);
    }

    @Test
    public void isElementEditable_presentationElement_readOnly() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);
        when(presentationElement.getDiagramPresentationElement()).thenReturn(diagramPresentationElement);
        when(diagramPresentationElement.getDiagram()).thenReturn(diagram);
        doReturn(configuredElement).when(cmElementPermissions).getConfiguredElement(diagram);
        when(configuredElement.isReadOnly()).thenReturn(true);

        boolean result = cmElementPermissions.isElementEditable(presentationElement);

        assertFalse(result);

        verify(configuredElement).isReadOnly();
    }

    @Test
    public void isElementEditable_presentationElement_changeRecordInactive() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);
        when(presentationElement.getDiagramPresentationElement()).thenReturn(diagramPresentationElement);
        when(diagramPresentationElement.getDiagram()).thenReturn(diagram);
        doReturn(configuredElement).when(cmElementPermissions).getConfiguredElement(diagram);
        when(configuredElement.isReadOnly()).thenReturn(false);
        doReturn(false).when(cmElementPermissions).isChangeRecordActive(configuredElement);

        boolean result = cmElementPermissions.isElementEditable(presentationElement);

        assertFalse(result);

        verify(cmElementPermissions).isChangeRecordActive(configuredElement);
    }

    @Test
    public void isElementEditable_presentationElement_changeRecordActive() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);
        when(presentationElement.getDiagramPresentationElement()).thenReturn(diagramPresentationElement);
        when(diagramPresentationElement.getDiagram()).thenReturn(diagram);
        doReturn(configuredElement).when(cmElementPermissions).getConfiguredElement(diagram);
        when(configuredElement.isReadOnly()).thenReturn(false);
        doReturn(true).when(cmElementPermissions).isChangeRecordActive(configuredElement);

        boolean result = cmElementPermissions.isElementEditable(presentationElement);

        assertTrue(result);

        verify(cmElementPermissions).isChangeRecordActive(configuredElement);
    }

    @Test
    public void isElementEditable_element_hasPermissions() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);
        doReturn(true).when(cmElementPermissions).checkElementPermissions(element, true);

        assertTrue(cmElementPermissions.isElementEditable(element));
        verify(cmElementPermissions).checkElementPermissions(element, true);
    }

    @Test
    public void canCreateChildIn_cmNotActive() {
        when(configurationManagementService.isCmActive()).thenReturn(false);

        boolean result = cmElementPermissions.canCreateChildIn(baseElement);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verifyNoMoreInteractions(configurationManagementService, baseElement);
    }

    @Test
    public void canCreateChildIn_cmAdmin() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(true);

        boolean result = cmElementPermissions.canCreateChildIn(baseElement);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verify(configurationManagementService).getAdminMode();
        verifyNoMoreInteractions(configurationManagementService, baseElement);
    }

    @Test
    public void canCreateChildIn_baseElement() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);

        boolean result = cmElementPermissions.canCreateChildIn(baseElement);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verify(configurationManagementService).getAdminMode();
        verifyNoMoreInteractions(configurationManagementService, baseElement);
    }

    @Test
    public void canCreateChildIn_element_hasPermissions() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);
        doReturn(true).when(cmElementPermissions).checkElementPermissions(element, false);

        assertTrue(cmElementPermissions.canCreateChildIn(element));
        verify(cmElementPermissions).checkElementPermissions(element, false);
    }

    @Test
    public void canAddChild_cmNotActive() {
        BaseElement baseElement2 = mock(BaseElement.class);
        when(configurationManagementService.isCmActive()).thenReturn(false);

        boolean result = cmElementPermissions.canAddChild(baseElement, baseElement2);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verifyNoMoreInteractions(configurationManagementService, baseElement, baseElement2);
    }

    @Test
    public void canAddChild_cmAdmin() {
        BaseElement baseElement2 = mock(BaseElement.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(true);

        boolean result = cmElementPermissions.canAddChild(baseElement, baseElement2);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verify(configurationManagementService).getAdminMode();
        verifyNoMoreInteractions(configurationManagementService, baseElement, baseElement2);
    }

    @Test
    public void canAddChild_baseElements() {
        BaseElement baseElement2 = mock(BaseElement.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);

        boolean result = cmElementPermissions.canAddChild(baseElement, baseElement2);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verify(configurationManagementService).getAdminMode();
        verifyNoMoreInteractions(configurationManagementService, baseElement, baseElement2);
    }

    @Test
    public void canAddChild_element_parent_lacksPermissions() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);
        doReturn(false).when(cmElementPermissions).checkElementPermissions(element, false);

        assertFalse(cmElementPermissions.canAddChild(element, baseElement));
    }

    @Test
    public void canAddChild_element_parentAndChildHavePermissions() {
        Element child = mock(Element.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);
        doReturn(true).when(cmElementPermissions).checkElementPermissions(element, false);
        doReturn(false).when(cmElementPermissions).checkElementPermissions(child, true);

        assertFalse(cmElementPermissions.canAddChild(element, child));
        verify(cmElementPermissions).checkElementPermissions(element, false);
    }


    @Test
    public void canDelete_cmAdmin() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(true);

        boolean result = cmElementPermissions.canDelete(baseElement);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verify(configurationManagementService).getAdminMode();
        verifyNoMoreInteractions(configurationManagementService, baseElement);
    }

    @Test
    public void canDelete_baseElement() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);

        boolean result = cmElementPermissions.canDelete(baseElement);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verify(configurationManagementService).getAdminMode();
        verifyNoMoreInteractions(configurationManagementService, baseElement);
    }

    @Test
    public void canDelete_element_hasPermissions() {
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(configurationManagementService.getAdminMode()).thenReturn(false);
        doReturn(true).when(cmElementPermissions).checkElementPermissions(element, true);

        assertTrue(cmElementPermissions.canDelete(element));
    }

    @Test
    public void canDelete_cmNotActive() {
        when(configurationManagementService.isCmActive()).thenReturn(false);

        boolean result = cmElementPermissions.canDelete(baseElement);

        assertTrue(result);

        verify(configurationManagementService).isCmActive();
        verifyNoMoreInteractions(configurationManagementService, baseElement);
    }

    @Test
    public void checkElementPermissions_element_readOnly() {
        doReturn(configuredElement).when(cmElementPermissions).getConfiguredElement(element);
        when(configuredElement.isReadOnly()).thenReturn(true);

        assertFalse(cmElementPermissions.checkElementPermissions(element, false));

        verify(cmElementPermissions, never()).isElementARevisionHistoryOrChangeRecord(element);
    }

    @Test
    public void checkElementPermissions_nullElement1() {
        doReturn(null).when(cmElementPermissions).getConfiguredElement(element);
        doReturn(false).when(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(element);
        doReturn(false).when(cmElementPermissions).isPackageChangeManagementChangeRecordsOrRevisionHistory(element, false);

        assertTrue(cmElementPermissions.checkElementPermissions(element, false));

        verify(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(element);
    }

    @Test
    public void checkElementPermissions_nullElement() {
        doReturn(null).when(cmElementPermissions).getConfiguredElement(element);
        doReturn(true).when(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(element);

        assertFalse(cmElementPermissions.checkElementPermissions(element, false));

        verify(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(element);
    }

    @Test
    public void checkElementPermissions_element_isCR() {
        doReturn(configuredElement).when(cmElementPermissions).getConfiguredElement(element);
        when(configuredElement.isReadOnly()).thenReturn(false);
        doReturn(true).when(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(element);

        assertFalse(cmElementPermissions.checkElementPermissions(element, false));

        verify(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(element);
        verify(cmElementPermissions, never()).isPackageChangeManagementChangeRecordsOrRevisionHistory(element, false);
    }

    @Test
    public void checkElementPermissions_element_isCMpackage() {
        doReturn(configuredElement).when(cmElementPermissions).getConfiguredElement(element);
        when(configuredElement.isReadOnly()).thenReturn(false);
        doReturn(false).when(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(element);
        doReturn(true).when(cmElementPermissions).isPackageChangeManagementChangeRecordsOrRevisionHistory(element, false);

        assertFalse(cmElementPermissions.checkElementPermissions(element, false));

        verify(cmElementPermissions).isPackageChangeManagementChangeRecordsOrRevisionHistory(element, false);
        verify(cmElementPermissions, never()).isChangeRecordActive(configuredElement);
    }

    @Test
    public void checkElementPermissions_element_inInactive() {
        doReturn(configuredElement).when(cmElementPermissions).getConfiguredElement(element);
        when(configuredElement.isReadOnly()).thenReturn(false);
        doReturn(false).when(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(element);
        doReturn(false).when(cmElementPermissions).isPackageChangeManagementChangeRecordsOrRevisionHistory(element, false);
        doReturn(false).when(cmElementPermissions).isChangeRecordActive(configuredElement);

        assertFalse(cmElementPermissions.checkElementPermissions(element, false));

        verify(cmElementPermissions).isChangeRecordActive(configuredElement);
        verify(cmElementPermissions, never()).taggedValuePermissionCheck(any());
    }

    @Test
    public void checkElementPermissions_taggedValue_nullCheck() {
        TaggedValue taggedValue = mock(TaggedValue.class);
        doReturn(null).when(cmElementPermissions).getConfiguredElement(null);
        doReturn(false).when(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(null);
        doReturn(false).when(cmElementPermissions).isPackageChangeManagementChangeRecordsOrRevisionHistory(null, false);

        assertTrue(cmElementPermissions.checkElementPermissions(null, false));
        verify(cmElementPermissions, never()).taggedValuePermissionCheck(taggedValue);
    }

    @Test
    public void checkElementPermissions_taggedValue_falseCheck() {
        TaggedValue taggedValue = mock(TaggedValue.class);

        doReturn(null).when(cmElementPermissions).getConfiguredElement(taggedValue);
        doReturn(false).when(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(taggedValue);
        doReturn(false).when(cmElementPermissions).isPackageChangeManagementChangeRecordsOrRevisionHistory(taggedValue, false);

        assertTrue(cmElementPermissions.checkElementPermissions(taggedValue, false));
        verify(cmElementPermissions).taggedValuePermissionCheck(taggedValue);
    }

    @Test
    public void checkElementPermissions_taggedValueNoPermission() {
        TaggedValue taggedValue = mock(TaggedValue.class);

        doReturn(null).when(cmElementPermissions).getConfiguredElement(taggedValue);
        doReturn(false).when(cmElementPermissions).isElementARevisionHistoryOrChangeRecord(taggedValue);
        doReturn(false).when(cmElementPermissions).isPackageChangeManagementChangeRecordsOrRevisionHistory(taggedValue, false);
        doReturn(false).when(cmElementPermissions).taggedValuePermissionCheck(taggedValue);

        assertFalse(cmElementPermissions.checkElementPermissions(taggedValue, false));
        verify(cmElementPermissions).taggedValuePermissionCheck(taggedValue);
    }

    @Test
    public void checkElementPermissions_commentPermission() {
        Comment comment = mock(Comment.class);

        doReturn(true).when(cmElementPermissions).commentPermissionCheck(comment);

        assertTrue(cmElementPermissions.checkElementPermissions(comment, true));
        verify(cmElementPermissions).commentPermissionCheck(comment);
    }

    @Test
    public void isPackageChangeManagementChangeRecordsOrRevisionHistory_nullParameter() {
        assertFalse(cmElementPermissions.isPackageChangeManagementChangeRecordsOrRevisionHistory(null, false));
    }

    @Test
    public void isPackageChangeManagementChangeRecordsOrRevisionHistory_notAPackage() {
        assertFalse(cmElementPermissions.isPackageChangeManagementChangeRecordsOrRevisionHistory(element, false));
    }

    @Test
    public void isPackageChangeManagementChangeRecordsOrRevisionHistory_checkOnlyChangeRecordsAndRevisionHistory() {
        doReturn(true).when(cmElementPermissions).isPackageChangeRecordsOrRevisionHistory(pkg);

        assertTrue(cmElementPermissions.isPackageChangeManagementChangeRecordsOrRevisionHistory(pkg, false));
    }

    @Test
    public void isPackageChangeManagementChangeRecordsOrRevisionHistory_checkForAllThreePackages() {
        doReturn(true).when(cmElementPermissions).isPackageChangeManagementChangeRecordsOrRevisionHistory(pkg);

        assertTrue(cmElementPermissions.isPackageChangeManagementChangeRecordsOrRevisionHistory(pkg, true));
    }

    @Test
    public void isPackageChangeManagementChangeRecordsOrRevisionHistory_notMatchingPackage() {
        when(configurationManagementService.getChangeManagementPackage(false)).thenReturn(mock(Package.class));
        when(configurationManagementService.getChangeRecordsPackage(false)).thenReturn(mock(Package.class));
        when(configurationManagementService.getRevisionHistoryPackage(false)).thenReturn(mock(Package.class));

        assertFalse(cmElementPermissions.isPackageChangeManagementChangeRecordsOrRevisionHistory(pkg));

        verify(configurationManagementService).getChangeManagementPackage(anyBoolean());
        verify(configurationManagementService).getChangeRecordsPackage(anyBoolean());
        verify(configurationManagementService).getRevisionHistoryPackage(anyBoolean());
    }

    @Test
    public void isPackageChangeManagementChangeRecordsOrRevisionHistory_cmPackage() {
        when(configurationManagementService.getChangeManagementPackage(false)).thenReturn(pkg);

        assertTrue(cmElementPermissions.isPackageChangeManagementChangeRecordsOrRevisionHistory(pkg));

        verify(configurationManagementService).getChangeManagementPackage(anyBoolean());
        verify(configurationManagementService, never()).getChangeRecordsPackage(anyBoolean());
        verify(configurationManagementService, never()).getRevisionHistoryPackage(anyBoolean());
    }

    @Test
    public void isPackageChangeManagementChangeRecordsOrRevisionHistory_crPackage() {
        when(configurationManagementService.getChangeManagementPackage(false)).thenReturn(mock(Package.class));
        when(configurationManagementService.getChangeRecordsPackage(false)).thenReturn(pkg);

        assertTrue(cmElementPermissions.isPackageChangeManagementChangeRecordsOrRevisionHistory(pkg));

        verify(configurationManagementService).getChangeManagementPackage(anyBoolean());
        verify(configurationManagementService).getChangeRecordsPackage(anyBoolean());
        verify(configurationManagementService, never()).getRevisionHistoryPackage(anyBoolean());
    }

    @Test
    public void isPackageChangeManagementChangeRecordsOrRevisionHistory_rhPackage() {
        when(configurationManagementService.getChangeManagementPackage(false)).thenReturn(mock(Package.class));
        when(configurationManagementService.getChangeRecordsPackage(false)).thenReturn(mock(Package.class));
        when(configurationManagementService.getRevisionHistoryPackage(false)).thenReturn(pkg);

        assertTrue(cmElementPermissions.isPackageChangeManagementChangeRecordsOrRevisionHistory(pkg));

        verify(configurationManagementService).getChangeManagementPackage(anyBoolean());
        verify(configurationManagementService).getChangeRecordsPackage(anyBoolean());
        verify(configurationManagementService).getRevisionHistoryPackage(anyBoolean());
    }

    @Test
    public void isPackageChangeRecordsOrRevisionHistory_notMatchingPackage() {
        when(configurationManagementService.getChangeRecordsPackage(false)).thenReturn(mock(Package.class));
        when(configurationManagementService.getRevisionHistoryPackage(false)).thenReturn(mock(Package.class));

        assertFalse(cmElementPermissions.isPackageChangeRecordsOrRevisionHistory(pkg));

        verify(configurationManagementService).getChangeRecordsPackage(anyBoolean());
        verify(configurationManagementService).getRevisionHistoryPackage(anyBoolean());
    }

    @Test
    public void isPackageChangeRecordsOrRevisionHistory_crPackage() {
        when(configurationManagementService.getChangeRecordsPackage(false)).thenReturn(pkg);

        assertTrue(cmElementPermissions.isPackageChangeRecordsOrRevisionHistory(pkg));

        verify(configurationManagementService).getChangeRecordsPackage(anyBoolean());
        verify(configurationManagementService, never()).getRevisionHistoryPackage(anyBoolean());
    }

    @Test
    public void isPackageChangeRecordsOrRevisionHistory_rhPackage() {
        when(configurationManagementService.getChangeRecordsPackage(false)).thenReturn(mock(Package.class));
        when(configurationManagementService.getRevisionHistoryPackage(false)).thenReturn(pkg);

        assertTrue(cmElementPermissions.isPackageChangeRecordsOrRevisionHistory(pkg));

        verify(configurationManagementService).getChangeRecordsPackage(anyBoolean());
        verify(configurationManagementService).getRevisionHistoryPackage(anyBoolean());
    }

    @Test
    public void isElementARevisionHistoryOrChangeRecord_isRH() {
        ApiDomain apiDomain = mock(ApiDomain.class);
        Stereotype rh = mock(Stereotype.class);
        when(cmElementPermissions.getApiDomain()).thenReturn(apiDomain);
        when(configurationManagementService.getRhStereotype()).thenReturn(rh);
        when(apiDomain.hasStereotype(element, rh)).thenReturn(true);

        assertTrue(cmElementPermissions.isElementARevisionHistoryOrChangeRecord(element));

        verify(apiDomain).hasStereotype(element, rh);
    }

    @Test
    public void isElementARevisionHistoryOrChangeRecord_isCR() {
        ApiDomain apiDomain = mock(ApiDomain.class);
        Stereotype rh = mock(Stereotype.class);
        Stereotype cr = mock(Stereotype.class);
        doReturn(apiDomain).when(cmElementPermissions).getApiDomain();
        when(configurationManagementService.getRhStereotype()).thenReturn(rh);
        when(apiDomain.hasStereotype(element, rh)).thenReturn(false);
        when(configurationManagementService.getBaseCRStereotype()).thenReturn(cr);
        when(apiDomain.hasStereotypeOrDerived(element, cr)).thenReturn(true);

        assertTrue(cmElementPermissions.isElementARevisionHistoryOrChangeRecord(element));

        verify(apiDomain).hasStereotype(element, rh);
        verify(apiDomain).hasStereotypeOrDerived(element, cr);
    }

    @Test
    public void isElementARevisionHistoryOrChangeRecord_false() {
        ApiDomain apiDomain = mock(ApiDomain.class);
        Stereotype rh = mock(Stereotype.class);
        Stereotype cr = mock(Stereotype.class);
        doReturn(apiDomain).when(cmElementPermissions).getApiDomain();
        when(configurationManagementService.getRhStereotype()).thenReturn(rh);
        when(apiDomain.hasStereotype(element, rh)).thenReturn(false);
        when(configurationManagementService.getBaseCRStereotype()).thenReturn(cr);
        when(apiDomain.hasStereotypeOrDerived(element, cr)).thenReturn(false);

        assertFalse(cmElementPermissions.isElementARevisionHistoryOrChangeRecord(element));

        verify(apiDomain).hasStereotype(element, rh);
        verify(apiDomain).hasStereotypeOrDerived(element, cr);
    }

    @Test
    public void isChangeRecordActive_nullInput() {
        assertTrue(cmElementPermissions.isChangeRecordActive(null));
    }

    @Test
    public void isChangeRecordActive_notEnforceActiveCRSwitch() {
        when(configurationManagementService.getEnforceActiveCRSwitch()).thenReturn(false);

        assertTrue(cmElementPermissions.isChangeRecordActive(configuredElement));
    }

    @Test
    public void isChangeRecordActive_crNull() {
        when(configurationManagementService.getEnforceActiveCRSwitch()).thenReturn(true);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(null);

        assertFalse(cmElementPermissions.isChangeRecordActive(configuredElement));
    }

    @Test
    public void isChangeRecordActive_crReadOnly() {
        when(configurationManagementService.getEnforceActiveCRSwitch()).thenReturn(true);
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(changeRecord.isReadOnly()).thenReturn(true);

        assertFalse(cmElementPermissions.isChangeRecordActive(configuredElement));
    }

    @Test
    public void isChangeRecordActive_notReleaseAuthority() {
        when(configurationManagementService.getEnforceActiveCRSwitch()).thenReturn(true);
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(changeRecord.isReadOnly()).thenReturn(false);
        when(configuredElement.getReleaseAuthority()).thenReturn(mock(ChangeRecord.class));

        assertFalse(cmElementPermissions.isChangeRecordActive(configuredElement));

        verify(configuredElement).getReleaseAuthority();
    }

    @Test
    public void isChangeRecordActive_releaseAuthority() {
        when(configurationManagementService.getEnforceActiveCRSwitch()).thenReturn(true);
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(changeRecord.isReadOnly()).thenReturn(false);
        when(configuredElement.getReleaseAuthority()).thenReturn(changeRecord);

        assertTrue(cmElementPermissions.isChangeRecordActive(configuredElement));

        verify(configuredElement).getReleaseAuthority();
    }

    @Test
    public void taggedValuePermissionCheck_nullProperty() {
        TaggedValue taggedValue = mock(TaggedValue.class);
        when(taggedValue.getTagDefinition()).thenReturn(null);

        assertTrue(cmElementPermissions.taggedValuePermissionCheck(taggedValue));
    }

    @Test
    public void taggedValuePermissionCheck_nullOwner() {
        TaggedValue taggedValue = mock(TaggedValue.class);
        Property property = mock(Property.class);
        when(taggedValue.getTagDefinition()).thenReturn(property);
        when(property.getOwner()).thenReturn(null);

        assertTrue(cmElementPermissions.taggedValuePermissionCheck(taggedValue));
        verify(property).getOwner();
    }

    @Test
    public void taggedValuePermissionCheck_propertyOwnerIsProtectedStereotype() {
        TaggedValue taggedValue = mock(TaggedValue.class);
        Property property = mock(Property.class);
        when(taggedValue.getTagDefinition()).thenReturn(property);
        when(property.getOwner()).thenReturn(element);
        doReturn(false).when(cmElementPermissions).checkPropertyOwnerIsNotAProtectedStereotype(element);

        assertFalse(cmElementPermissions.taggedValuePermissionCheck(taggedValue));
        verify(property, times(2)).getOwner();
    }

    @Test
    public void taggedValuePermissionCheck_propertyOwnerNotProtectedDirectlyButIsViaGeneralizationLink() {
        TaggedValue taggedValue = mock(TaggedValue.class);
        Property property = mock(Property.class);
        when(taggedValue.getTagDefinition()).thenReturn(property);
        doReturn(false).when(cmElementPermissions).propertyPermissionCheck(property);

        assertFalse(cmElementPermissions.taggedValuePermissionCheck(taggedValue));

        verify(taggedValue).getTagDefinition();
        verify(cmElementPermissions).propertyPermissionCheck(property);
    }

    @Test
    public void propertyPermissionCheckTest() {
        Property property = mock(Property.class);
        when(property.getOwner()).thenReturn(element);
        Collection<Property> propertyCollection  = new ArrayList<>();
        propertyCollection.add(property);
        when(property.getRedefinedProperty()).thenReturn(propertyCollection);
        doReturn(true).when(cmElementPermissions).checkPropertyOwnerIsNotAProtectedStereotype(element);
        doReturn(false).when(cmElementPermissions).checkRedefinedProperties(propertyCollection);

        assertFalse(cmElementPermissions.propertyPermissionCheck(property));

        verify(cmElementPermissions, times(1)).checkPropertyOwnerIsNotAProtectedStereotype(element);
        verify(cmElementPermissions, times(1)).checkRedefinedProperties(propertyCollection);
    }

    @Test
    public void taggedValuePermissionCheck_notProtectedDirectlyOrOtherwise() {
        TaggedValue taggedValue = mock(TaggedValue.class);
        Property property = mock(Property.class);
        when(taggedValue.getTagDefinition()).thenReturn(property);
        when(property.getOwner()).thenReturn(element);
        doReturn(true).when(cmElementPermissions).checkPropertyOwnerIsNotAProtectedStereotype(element);

        assertTrue(cmElementPermissions.taggedValuePermissionCheck(taggedValue));

        verify(cmElementPermissions).checkPropertyOwnerIsNotAProtectedStereotype(element);
    }

    @Test
    public void checkPropertyOwnerIsNotAProtectedStereotype_nullId() {
        when(element.getLocalID()).thenReturn(null);

        assertTrue(cmElementPermissions.checkPropertyOwnerIsNotAProtectedStereotype(element));
    }

    @Test
    public void checkPropertyOwnerIsNotAProtectedStereotype_idMismatch() {
        String id = "id";
        when(element.getLocalID()).thenReturn(id);

        assertTrue(cmElementPermissions.checkPropertyOwnerIsNotAProtectedStereotype(element));
    }

    @Test
    public void checkPropertyOwnerIsNotAProtectedStereotype_idMatchesCe() {
        when(element.getLocalID()).thenReturn(PluginConstant.CONFIGURED_ELEMENT_STEREOTYPE_ID);

        assertFalse(cmElementPermissions.checkPropertyOwnerIsNotAProtectedStereotype(element));
    }

    @Test
    public void checkPropertyOwnerIsNotAProtectedStereotype_idMatchesCr() {
        when(element.getLocalID()).thenReturn(PluginConstant.CHANGE_RECORD_STEREOTYPE_ID);

        assertFalse(cmElementPermissions.checkPropertyOwnerIsNotAProtectedStereotype(element));
    }

    @Test
    public void checkPropertyOwnerIsNotAProtectedStereotype_idMatchesRh() {
        when(element.getLocalID()).thenReturn(PluginConstant.REVISION_HISTORY_STEREOTYPE_ID);

        assertFalse(cmElementPermissions.checkPropertyOwnerIsNotAProtectedStereotype(element));
    }

    @Test
    public void getConfiguredElement_null() {
        when(configurationManagementService.getConfiguredElement(element)).thenReturn(null);
        when(configurationManagementService.getCCZOwner(element)).thenReturn(null);

        assertNull(cmElementPermissions.getConfiguredElement(element));
    }

    @Test
    public void getConfiguredElement_elementConfigured() {
        when(configurationManagementService.getConfiguredElement(element)).thenReturn(configuredElement);

        assertSame(configuredElement, cmElementPermissions.getConfiguredElement(element));
    }

    @Test
    public void getConfiguredElement_cczownerConfigured() {
        when(configurationManagementService.getConfiguredElement(element)).thenReturn(null);
        when(configurationManagementService.getCCZOwner(element)).thenReturn(configuredElement);

        assertSame(configuredElement, cmElementPermissions.getConfiguredElement(element));
    }

    @Test
    public void commentPermissionCheck_noOwner() {
        Comment comment = mock(Comment.class);

        doReturn(null).when(comment).getOwner();

        assertTrue(cmElementPermissions.commentPermissionCheck(comment));
        verify(configurationManagementService, never()).getBaseCRStereotype();
    }

    @Test
    public void commentPermissionCheck_ownerStereotypeNotChangeRecord() {
        Comment comment = mock(Comment.class);
        ApiDomain apiDomain = mock(ApiDomain.class);
        Stereotype stereotype = mock(Stereotype.class);

        doReturn(element).when(comment).getOwner();
        doReturn(apiDomain).when(cmElementPermissions).getApiDomain();
        when(configurationManagementService.getBaseCRStereotype()).thenReturn(stereotype);
        doReturn(false).when(apiDomain).hasStereotypeOrDerived(element, stereotype);

        assertTrue(cmElementPermissions.commentPermissionCheck(comment));
        verify(configurationManagementService).getBaseCRStereotype();
    }

    @Test
    public void commentPermissionCheck_ownerStereotypeIsChangeRecord() {
        Comment comment = mock(Comment.class);
        ApiDomain apiDomain = mock(ApiDomain.class);
        Stereotype stereotype = mock(Stereotype.class);

        doReturn(element).when(comment).getOwner();
        doReturn(apiDomain).when(cmElementPermissions).getApiDomain();
        when(configurationManagementService.getBaseCRStereotype()).thenReturn(stereotype);
        doReturn(true).when(apiDomain).hasStereotypeOrDerived(element, stereotype);

        assertFalse(cmElementPermissions.commentPermissionCheck(comment));
        verify(configurationManagementService).getBaseCRStereotype();
    }

    @Test
    public void checkRedefinedProperties_nullParameter() {
        assertTrue(cmElementPermissions.checkRedefinedProperties(null));
    }

    @Test
    public void checkRedefinedProperties_nullOwnerAndChildHasNoRedefinedProperties() {
        Property property = mock(Property.class);
        Collection<Property> redefined = new ArrayList<>();
        redefined.add(property);

        when(property.getOwner()).thenReturn(null);
        when(property.getRedefinedProperty()).thenReturn(new ArrayList<>());

        assertTrue(cmElementPermissions.checkRedefinedProperties(redefined));
    }

    @Test
    public void checkRedefinedProperties_ownerIsProtected() {
        Property property = mock(Property.class);
        Collection<Property> redefined = new ArrayList<>();
        redefined.add(property);

        when(property.getOwner()).thenReturn(element);
        doReturn(false).when(cmElementPermissions).checkPropertyOwnerIsNotAProtectedStereotype(element);

        assertFalse(cmElementPermissions.checkRedefinedProperties(redefined));
    }

    @Test
    public void checkRedefinedProperties_ownerIsNotProtectedButRecursionIsProtected() {
        Property property = mock(Property.class);
        Collection<Property> redefined = new ArrayList<>();
        redefined.add(property);
        Property recurseProperty = mock(Property.class);
        Collection<Property> recurseRedefined = new ArrayList<>();
        recurseRedefined.add(recurseProperty);
        Element recurseOwner = mock(Element.class);

        when(property.getOwner()).thenReturn(element);
        doReturn(true).when(cmElementPermissions).checkPropertyOwnerIsNotAProtectedStereotype(element);
        when(property.getRedefinedProperty()).thenReturn(recurseRedefined);
        when(recurseProperty.getOwner()).thenReturn(recurseOwner);
        doReturn(false).when(cmElementPermissions).checkPropertyOwnerIsNotAProtectedStereotype(recurseOwner);

        assertFalse(cmElementPermissions.checkRedefinedProperties(redefined));
    }
}
