package org.openmbee.plugin.cfgmgmt.listeners;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxService;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.settings.CustomSettings;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.impl.PropertyNames;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.beans.PropertyChangeEvent;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestCMPropertyListener {
    @Mock
    private PropertyChangeEvent event;
    @Mock
    private ConfigurationManagementService configurationManagementService;
    @Mock
    private ThreeDxService threeDxService;
    @InjectMocks
    @Spy
    private CMPropertyListener cmPropertyListener;

    @Before
    public void setup() {
        when(cmPropertyListener.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(configurationManagementService.getThreeDxService()).thenReturn(threeDxService);
    }

    @Test
    public void propertyChange_cmNotActive() {
        when(configurationManagementService.isCmActive()).thenReturn(false);

        cmPropertyListener.propertyChange(event);

        verifyNoInteractions(event);
    }

    @Test
    public void propertyChange_Element_wrongProperty() {
        ElementTaggedValue source = mock(ElementTaggedValue.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn("something");

        cmPropertyListener.propertyChange(event);

        verifyNoInteractions(source);
        verify(configurationManagementService, never()).setChangeRecordSelections();
    }

    @Test
    public void propertyChange_Element_NoTagDefinition() {
        ElementTaggedValue source = mock(ElementTaggedValue.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn(PropertyNames.VALUE);

        cmPropertyListener.propertyChange(event);

        verify(source).getTagDefinition();
        verify(configurationManagementService, never()).setChangeRecordSelections();
    }

    @Test
    public void propertyChange_Element_wrongChangeRecordStatusId() {
        ElementTaggedValue source = mock(ElementTaggedValue.class);
        Property property = mock(Property.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn(PropertyNames.VALUE);
        when(source.getTagDefinition()).thenReturn(property);
        when(property.getLocalID()).thenReturn("something");

        cmPropertyListener.propertyChange(event);

        verify(configurationManagementService, never()).setChangeRecordSelections();
    }

    @Test
    public void propertyChange_Element() {
        ElementTaggedValue source = mock(ElementTaggedValue.class);
        Property property = mock(Property.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn(PropertyNames.VALUE);
        when(source.getTagDefinition()).thenReturn(property);
        when(property.getLocalID()).thenReturn(PluginConstant.CHANGE_RECORD_STATUS_ID);

        cmPropertyListener.propertyChange(event);

        verify(configurationManagementService).setChangeRecordSelections();
    }

    @Test
    public void propertyChange_String_wrongProperty() {
        StringTaggedValue source = mock(StringTaggedValue.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn("something");

        cmPropertyListener.propertyChange(event);

        verifyNoInteractions(source);
        verify(configurationManagementService, never()).getCustomSettings();
    }

    @Test
    public void propertyChange_String_NoTagDefinition() {
        StringTaggedValue source = mock(StringTaggedValue.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn(PropertyNames.VALUE);

        cmPropertyListener.propertyChange(event);

        verify(source, times(3)).getTagDefinition();
        verify(configurationManagementService, never()).getCustomSettings();
    }

    @Test
    public void propertyChange_String_noOwner() {
        StringTaggedValue source = mock(StringTaggedValue.class);
        Property property = mock(Property.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn(PropertyNames.VALUE);
        when(source.getTagDefinition()).thenReturn(property);

        cmPropertyListener.propertyChange(event);

        verify(configurationManagementService, never()).getCustomSettings();
    }

    @Test
    public void propertyChange_String_wrongChangeRecordStatusId() {
        StringTaggedValue source = mock(StringTaggedValue.class);
        Element owner = mock(Element.class);
        Property property = mock(Property.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn(PropertyNames.VALUE);
        when(source.getTagDefinition()).thenReturn(property);
        when(property.getOwner()).thenReturn(owner);
        when(owner.getLocalID()).thenReturn("something");

        cmPropertyListener.propertyChange(event);

        verify(configurationManagementService, never()).getCustomSettings();
    }

    @Test
    public void propertyChange_String() {
        StringTaggedValue source = mock(StringTaggedValue.class);
        Element owner = mock(Element.class);
        Property property = mock(Property.class);
        CustomSettings customSettings = mock(CustomSettings.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn(PropertyNames.VALUE);
        when(source.getTagDefinition()).thenReturn(property);
        when(property.getOwner()).thenReturn(owner);
        when(owner.getLocalID()).thenReturn(PluginConstant.CM_PLUGIN_SETTINGS_STEREOTYPE_ID);
        when(configurationManagementService.getCustomSettings()).thenReturn(customSettings);
        cmPropertyListener.propertyChange(event);
        verify(customSettings).update();
        verify(owner).getLocalID();
        verify(configurationManagementService).getCustomSettings();
    }

    @Test
    public void propertyChange_Boolean() {
        BooleanTaggedValue source = mock(BooleanTaggedValue.class);
        Element owner = mock(Element.class);
        Property property = mock(Property.class);
        CustomSettings customSettings = mock(CustomSettings.class);

        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn(PropertyNames.VALUE);
        when(source.getTagDefinition()).thenReturn(property);
        when(property.getOwner()).thenReturn(owner);
        when(owner.getLocalID()).thenReturn(PluginConstant.CM_PLUGIN_SETTINGS_STEREOTYPE_ID);
        when(configurationManagementService.getCustomSettings()).thenReturn(customSettings);

        cmPropertyListener.propertyChange(event);

        verify(customSettings).update();
        verify(owner).getLocalID();
        verify(configurationManagementService).getCustomSettings();
    }

    @Test
    public void propertyChange_set3DxConnectionInfo() {
        StringTaggedValue source = mock(StringTaggedValue.class);
        Element owner = mock(Element.class);
        Property property = mock(Property.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn(PropertyNames.VALUE);
        when(source.getTagDefinition()).thenReturn(property);
        when(property.getOwner()).thenReturn(owner);
        when(owner.getLocalID()).thenReturn(PluginConstant.THREEDX_CONNECTION_SETTINGS_STEREOTYPE_ID);
        doNothing().when(threeDxService).update3DxConnectionInfo();
        cmPropertyListener.propertyChange(event);

        verify(threeDxService).update3DxConnectionInfo();
        verify(configurationManagementService, never()).getCustomSettings();
        verify(owner, times(3)).getLocalID();
    }

    @Test
    public void propertyChange_setJIRAConnectionInfo() {
        StringTaggedValue source = mock(StringTaggedValue.class);
        Element owner = mock(Element.class);
        Property property = mock(Property.class);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        when(event.getSource()).thenReturn(source);
        when(event.getPropertyName()).thenReturn(PropertyNames.VALUE);
        when(source.getTagDefinition()).thenReturn(property);
        when(property.getOwner()).thenReturn(owner);
        when(owner.getLocalID()).thenReturn(PluginConstant.JIRA_CONNECTION_SETTINGS_STEREOTYPE_ID);
        JiraService jiraService = mock(JiraService.class);
        when(configurationManagementService.getJiraService()).thenReturn(jiraService);

        cmPropertyListener.propertyChange(event);

        verify(configurationManagementService, never()).getCustomSettings();
        verify(owner, times(3)).getLocalID();
        verify(jiraService).updateJIRAConnectionInfo();

    }
}