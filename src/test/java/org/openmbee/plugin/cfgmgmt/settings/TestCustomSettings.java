package org.openmbee.plugin.cfgmgmt.settings;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestCustomSettings {

    private CustomSettings customSettings;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private Stereotype stereotype;
    boolean diagramAdornment;
    boolean enforceActiveCR;
    boolean automateRelease;
    String cmPackagePath;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        stereotype = mock(Stereotype.class);
        cmPackagePath = "cm package path";
        diagramAdornment = true;
        enforceActiveCR = true;
        automateRelease = true;

        customSettings = spy(new CustomSettings(configurationManagementService));

        doReturn(apiDomain).when(customSettings).getApiDomain();
        doReturn(uiDomain).when(configurationManagementService).getUIDomain();
        doReturn(configurationManagementService).when(customSettings).getConfigurationManagementService();
    }

    @Test
    public void setToDefault() {
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.CM_PACKAGE_PATH)).thenReturn(cmPackagePath);
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.CM_DIAGRAM_ADORNMENT)).thenReturn(diagramAdornment);
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.ENFORCE_ACTIVE_CR)).thenReturn(enforceActiveCR);
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.AUTOMATE_RELEASE)).thenReturn(automateRelease);

        customSettings.setToDefault(stereotype);

        assertEquals(cmPackagePath, customSettings.get(PluginConstant.CM_PACKAGE_PATH));
        assertTrue((boolean)customSettings.get(PluginConstant.CM_DIAGRAM_ADORNMENT));
        assertTrue((boolean)customSettings.get(PluginConstant.ENFORCE_ACTIVE_CR));
        assertTrue((boolean)customSettings.get(PluginConstant.CM_DIAGRAM_ADORNMENT));
    }

    @Test
    public void clear() {
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.CM_PACKAGE_PATH)).thenReturn(cmPackagePath);
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.CM_DIAGRAM_ADORNMENT)).thenReturn(diagramAdornment);
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.ENFORCE_ACTIVE_CR)).thenReturn(enforceActiveCR);
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.AUTOMATE_RELEASE)).thenReturn(automateRelease);

        customSettings.setToDefault(stereotype);

        assertEquals(cmPackagePath, customSettings.get(PluginConstant.CM_PACKAGE_PATH));
        assertTrue((boolean)customSettings.get(PluginConstant.CM_DIAGRAM_ADORNMENT));
        assertTrue((boolean)customSettings.get(PluginConstant.ENFORCE_ACTIVE_CR));
        assertTrue((boolean)customSettings.get(PluginConstant.CM_DIAGRAM_ADORNMENT));

        customSettings.clear();

        assertNull(customSettings.get(PluginConstant.CM_PACKAGE_PATH));
    }

    @Test
    public void update_nullCmcsStereotype() {
        when(configurationManagementService.getCmcsStereotype()).thenReturn(null);

        customSettings.update();

        verify(apiDomain, never()).getStereotypedElements(any());
    }

    @Test
    public void update_noStereotypedElements() {
        List<Element> mockList = new ArrayList<>();
        when(configurationManagementService.getCmcsStereotype()).thenReturn(stereotype);
        when(apiDomain.getStereotypedElements(stereotype)).thenReturn(mockList);
        doNothing().when(customSettings).setToDefault(stereotype);

        customSettings.update();

        verify(customSettings).setToDefault(stereotype);
    }

    @Test
    public void update_multipleStereotypedElements_notNamed_noSettings() {
        Element e = mock(Element.class);
        Element e2 = mock(Element.class);
        List<Element> mockList = List.of(e, e2);
        Class clazz = mock(Class.class);
        String message = String.format(ExceptionConstants.MULTIPLE_CUSTOM_SETTINGS_FOUND_WARNING, "");
        when(configurationManagementService.getCmcsStereotype()).thenReturn(stereotype);
        when(apiDomain.getStereotypedElements(stereotype)).thenReturn(mockList);
        when(apiDomain.getStereotypePropertyValue(e, stereotype, PluginConstant.CM_DIAGRAM_ADORNMENT)).thenReturn(List.of());
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.CM_DIAGRAM_ADORNMENT)).thenReturn(false);
        when(apiDomain.getStereotypePropertyValue(e, stereotype, PluginConstant.CM_PACKAGE_PATH)).thenReturn(List.of());
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.CM_PACKAGE_PATH)).thenReturn("cm path");
        when(apiDomain.getStereotypePropertyValue(e, stereotype, PluginConstant.ENFORCE_ACTIVE_CR)).thenReturn(List.of());
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.ENFORCE_ACTIVE_CR)).thenReturn(false);
        when(apiDomain.getStereotypePropertyValue(e, stereotype, PluginConstant.AUTOMATE_RELEASE)).thenReturn(List.of());
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.AUTOMATE_RELEASE)).thenReturn(false);
        when(apiDomain.getStereotypePropertyValue(e, stereotype, PluginConstant.ADMIN_MODE)).thenReturn(List.of());
        when(apiDomain.getDefaultValue(stereotype, PluginConstant.ADMIN_MODE)).thenReturn(clazz);

        customSettings.update();

        assertEquals("cm path", customSettings.get(PluginConstant.CM_PACKAGE_PATH));

        verify(customSettings, never()).setToDefault(stereotype);
        verify(uiDomain).log(message);
        verify(apiDomain).getStereotypePropertyValue(e, stereotype, PluginConstant.CM_DIAGRAM_ADORNMENT);
        verify(apiDomain).getStereotypePropertyValue(e, stereotype, PluginConstant.CM_PACKAGE_PATH);
        verify(apiDomain).getStereotypePropertyValue(e, stereotype, PluginConstant.ENFORCE_ACTIVE_CR);
        verify(apiDomain).getStereotypePropertyValue(e, stereotype, PluginConstant.AUTOMATE_RELEASE);
        verify(apiDomain).getStereotypePropertyValue(e, stereotype, PluginConstant.ADMIN_MODE);
    }

    @Test
    public void update_oneStereotypedElements_named_settings() {
        NamedElement e = mock(NamedElement.class);
        List<Element> mockList = List.of(e);
        Class clazz = mock(Class.class);
        when(configurationManagementService.getCmcsStereotype()).thenReturn(stereotype);
        when(apiDomain.getStereotypedElements(stereotype)).thenReturn(mockList);
        when(apiDomain.getStereotypePropertyValue(e, stereotype, PluginConstant.CM_DIAGRAM_ADORNMENT)).thenReturn(List.of(true));
        when(apiDomain.getStereotypePropertyValue(e, stereotype, PluginConstant.CM_PACKAGE_PATH)).thenReturn(List.of("cm path"));
        when(apiDomain.getStereotypePropertyValue(e, stereotype, PluginConstant.ENFORCE_ACTIVE_CR)).thenReturn(List.of(true));
        when(apiDomain.getStereotypePropertyValue(e, stereotype, PluginConstant.AUTOMATE_RELEASE)).thenReturn(List.of(true));
        when(apiDomain.getStereotypePropertyValue(e, stereotype, PluginConstant.ADMIN_MODE)).thenReturn(List.of(clazz));

        customSettings.update();

        assertEquals("cm path", customSettings.get(PluginConstant.CM_PACKAGE_PATH));
        assertTrue((boolean) customSettings.get(PluginConstant.CM_DIAGRAM_ADORNMENT));
        assertTrue((boolean) customSettings.get(PluginConstant.ENFORCE_ACTIVE_CR));
        assertTrue((boolean) customSettings.get(PluginConstant.AUTOMATE_RELEASE));

        verify(customSettings, never()).setToDefault(stereotype);
        verify(uiDomain, never()).log(any());
        verify(apiDomain, never()).getDefaultValue(any(), any());
        verify(apiDomain).getStereotypePropertyValue(e, stereotype, PluginConstant.CM_DIAGRAM_ADORNMENT);
        verify(apiDomain).getStereotypePropertyValue(e, stereotype, PluginConstant.CM_PACKAGE_PATH);
        verify(apiDomain).getStereotypePropertyValue(e, stereotype, PluginConstant.ENFORCE_ACTIVE_CR);
        verify(apiDomain).getStereotypePropertyValue(e, stereotype, PluginConstant.AUTOMATE_RELEASE);
        verify(apiDomain).getStereotypePropertyValue(e, stereotype, PluginConstant.ADMIN_MODE);
    }
}