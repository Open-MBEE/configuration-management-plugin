package org.openmbee.plugin.cfgmgmt.domain;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.Lifecycle;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.utils.Policy;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestLifecycleObjectDomain {
    private LifecycleObjectDomain lifecycleObjectDomain;
    private LifecycleObjectFactory lifecycleObjectFactory;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private Element element;
    private ConfigurationManagementService configurationManagementService;
    private Stereotype baseCEStereotype;
    private Stereotype stereotype;
    private ConfiguredElement configuredElement;
    private ChangeRecord changeRecord;

    @Before
    public void setup() {
        lifecycleObjectFactory = mock(LifecycleObjectFactory.class);
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        lifecycleObjectDomain = Mockito.spy(new LifecycleObjectDomain(lifecycleObjectFactory, apiDomain, uiDomain));
        configurationManagementService = mock(ConfigurationManagementService.class);
        element = mock(Element.class);
        baseCEStereotype = mock(Stereotype.class);
        stereotype = mock(Stereotype.class);
        configuredElement = mock(ConfiguredElement.class);
        changeRecord = mock(ChangeRecord.class);

        when(configurationManagementService.getAutomateReleaseSwitch()).thenReturn(true);
    }

    @Test
    public void getApiDomain() {
        when(lifecycleObjectDomain.getApiDomain()).thenReturn(apiDomain);
        ApiDomain apiDomainObject = lifecycleObjectDomain.getApiDomain();
        assertNotNull(apiDomainObject);
    }

    @Test
    public void getUiDomain() {
        when(lifecycleObjectDomain.getUIDomain()).thenReturn(uiDomain);
        UIDomain uiDomainObject = lifecycleObjectDomain.getUIDomain();
        assertNotNull(uiDomainObject);
    }

    @Test
    public void canUserPerformAction_NotClass() {
        String action = "action";
        String fullKey = action + "Policy";
        Object randomObject = mock(Object.class);

        doReturn(randomObject).when(lifecycleObjectDomain).getDefaultValue(stereotype, fullKey);

        try {
            lifecycleObjectDomain.canUserPerformAction(configurationManagementService, stereotype, action);

            verify(configurationManagementService, never()).hasAnyRole(any());
        } catch (Exception e) {
            fail("Unexpected exception.");
        }
    }

    @Test
    public void canUserPerformAction_Class() {
        String action = "action";
        String fullKey = action + "Policy";
        Class classifier = mock(Class.class);
        Policy policy = mock(Policy.class);
        List<String> roles = new ArrayList<>();

        doReturn(classifier).when(apiDomain).getDefaultValue(stereotype, fullKey);
        doReturn(policy).when(lifecycleObjectDomain).getPolicyFromClass(configurationManagementService, classifier);
        doReturn(roles).when(policy).getRoles();
        lifecycleObjectDomain.canUserPerformAction(configurationManagementService, stereotype, action);
        verify(configurationManagementService).userHasPrivileges(roles);
    }

    @Test
    public void getDefaultValue() {
        String key = "key";
        Object object = mock(Object.class);
        doReturn(object).when(apiDomain).getDefaultValue(stereotype, key);
        assertNotNull(lifecycleObjectDomain.getDefaultValue(stereotype, key));
    }

    @Test
    public void getPolicyFromClassTest() {
        Class clazz = mock(Class.class);
        Policy policy = mock(Policy.class);
        when(lifecycleObjectFactory.getPolicy(configurationManagementService, clazz)).thenReturn(policy);

        Policy returned = lifecycleObjectDomain.getPolicyFromClass(configurationManagementService, clazz);
        assertSame(policy, returned);
    }

    @Test
    public void getLifecycle_ObtainsLifecycle() {

        StateMachine stateMachine = mock(StateMachine.class);
        Lifecycle lifecycle = mock(Lifecycle.class);

        doReturn(stateMachine).when(apiDomain).getDefaultValue(stereotype, PluginConstant.LIFECYCLE);
        when(lifecycleObjectFactory.getLifecycle(configurationManagementService, stateMachine)).thenReturn(lifecycle);

        assertSame(lifecycle, lifecycleObjectDomain.getLifecycle(configurationManagementService, stereotype));
    }

    @Test
    public void getLifecycle_failure() {
        doReturn(null).when(lifecycleObjectDomain).getDefaultValue(stereotype, PluginConstant.LIFECYCLE);

        Lifecycle lifecycle = lifecycleObjectDomain.getLifecycle(configurationManagementService, baseCEStereotype);
        assertNull(lifecycle);
    }

    @Test
    public void getInitialStatus_LifecycleNull() {
        doReturn(null).when(lifecycleObjectDomain).getLifecycle(configurationManagementService, baseCEStereotype);

        assertNull(lifecycleObjectDomain.getInitialStatus(configurationManagementService, baseCEStereotype));
    }

    @Test
    public void getInitialStatus_ObtainsStatus() {
        Lifecycle lifecycle = mock(Lifecycle.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);

        doReturn(lifecycle).when(lifecycleObjectDomain).getLifecycle(configurationManagementService, baseCEStereotype);
        when(lifecycle.getInitialStatus()).thenReturn(lifecycleStatus);

        assertNotNull(lifecycleObjectDomain.getInitialStatus(configurationManagementService, baseCEStereotype));
    }

    @Test
    public void canBePromoted_NotAnElement() {
        Object o = mock(Object.class);

        assertFalse(lifecycleObjectDomain.canBePromoted(configurationManagementService, o));
    }

    @Test
    public void canBePromoted_ElementInNonEditableState() {
        when(apiDomain.isElementInEditableState(any())).thenReturn(false);

        assertFalse(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_WithCMCSStereo() {
        when(apiDomain.isElementInEditableState(any())).thenReturn(true);
        when(apiDomain.hasStereotypeOrDerived(any(), any())).thenReturn(false);

        assertFalse(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_WithBaseCEStereotype() {
        when(apiDomain.isElementInEditableState(any())).thenReturn(true);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(baseCEStereotype);
        when(apiDomain.hasStereotypeOrDerived(any(), any())).thenReturn(true);
        when(lifecycleObjectFactory.getConfiguredElement(configurationManagementService, element)).thenReturn(configuredElement);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(changeRecord.isReleased()).thenReturn(false);
        when(configuredElement.hasAvailableTransitions()).thenReturn(true);
        when(configuredElement.getReleaseAuthority()).thenReturn(changeRecord);

        assertTrue(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_NoSelectedChangeRecord() {
        when(apiDomain.isElementInEditableState(any())).thenReturn(true);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(baseCEStereotype);
        when(apiDomain.hasStereotypeOrDerived(any(), any())).thenReturn(true);
        when(lifecycleObjectFactory.getConfiguredElement(configurationManagementService, element)).thenReturn(configuredElement);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(null);
        when(configuredElement.hasAvailableTransitions()).thenReturn(true);
        when(configuredElement.getReleaseAuthority()).thenReturn(changeRecord);

        assertFalse(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_DifferentChangeRecords() {
        ChangeRecord changeRecord2 = mock(ChangeRecord.class);

        when(apiDomain.isElementInEditableState(any())).thenReturn(true);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(baseCEStereotype);
        when(apiDomain.hasStereotypeOrDerived(any(), any())).thenReturn(true);
        when(lifecycleObjectFactory.getConfiguredElement(configurationManagementService, element)).thenReturn(configuredElement);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord2);
        when(changeRecord2.isReleased()).thenReturn(false);
        when(configuredElement.hasAvailableTransitions()).thenReturn(true);
        when(configuredElement.getReleaseAuthority()).thenReturn(changeRecord);

        assertFalse(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_DifferentChangeRecordsButAutomateReleaseSwitchOff() {
        ChangeRecord changeRecord2 = mock(ChangeRecord.class);

        when(apiDomain.isElementInEditableState(any())).thenReturn(true);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(baseCEStereotype);
        when(apiDomain.hasStereotypeOrDerived(any(), any())).thenReturn(true);
        when(lifecycleObjectFactory.getConfiguredElement(configurationManagementService, element)).thenReturn(configuredElement);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord2);
        when(changeRecord2.isReleased()).thenReturn(false);
        when(configuredElement.hasAvailableTransitions()).thenReturn(true);
        when(configuredElement.getReleaseAuthority()).thenReturn(changeRecord);
        when(configurationManagementService.getAutomateReleaseSwitch()).thenReturn(false);
        when(changeRecord.isReleased()).thenReturn(false);

        assertTrue(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_noTransitionsAndNoReleaseAuthority() {
        when(apiDomain.isElementInEditableState(any())).thenReturn(true);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(baseCEStereotype);
        when(apiDomain.hasStereotypeOrDerived(any(), any())).thenReturn(true);
        when(lifecycleObjectFactory.getConfiguredElement(configurationManagementService, element)).thenReturn(configuredElement);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(null);
        when(configuredElement.hasAvailableTransitions()).thenReturn(true);
        when(configuredElement.getReleaseAuthority()).thenReturn(null);
        doNothing().when(uiDomain).logError(ExceptionConstants.CHANGERECORD_ERROR_MESSAGE);

        assertFalse(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_ReleasedAlready() {
        when(apiDomain.isElementInEditableState(any())).thenReturn(true);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(baseCEStereotype);
        when(apiDomain.hasStereotypeOrDerived(any(), any())).thenReturn(true);
        when(lifecycleObjectFactory.getConfiguredElement(configurationManagementService, element)).thenReturn(configuredElement);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(changeRecord.isReleased()).thenReturn(true);
        when(configuredElement.hasAvailableTransitions()).thenReturn(true);
        when(configuredElement.getReleaseAuthority()).thenReturn(changeRecord);

        assertFalse(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_WithBaseCEStereotype_NoTransitions() {
        when(apiDomain.isElementInEditableState(any())).thenReturn(true);
        when(configurationManagementService.getBaseCEStereotype()).thenReturn(baseCEStereotype);
        when(apiDomain.hasStereotypeOrDerived(any(), any())).thenReturn(true);
        when(lifecycleObjectFactory.getConfiguredElement(configurationManagementService, element)).thenReturn(configuredElement);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(changeRecord.isReleased()).thenReturn(false);
        when(configuredElement.hasAvailableTransitions()).thenReturn(false);
        when(configuredElement.getReleaseAuthority()).thenReturn(changeRecord);

        assertFalse(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_WithBaseCRStereotype() {
        Stereotype baseCRStereotype = mock(Stereotype.class);
        element = mock(Class.class);

        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(baseCEStereotype).when(configurationManagementService).getBaseCEStereotype();
        doReturn(false).when(apiDomain).hasStereotypeOrDerived(element, baseCEStereotype);
        doReturn(baseCRStereotype).when(configurationManagementService).getBaseCRStereotype();
        doReturn(true).when(apiDomain).hasStereotypeOrDerived(element, baseCRStereotype);
        doReturn(changeRecord).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, (Class) element);
        doReturn(true).when(changeRecord).hasAvailableTransitions();

        assertTrue(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_WithBaseCRStereotype_NoChangeRecord() {
        Stereotype baseCRStereotype = mock(Stereotype.class);
        element = mock(Class.class);

        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(baseCEStereotype).when(configurationManagementService).getBaseCEStereotype();
        doReturn(false).when(apiDomain).hasStereotypeOrDerived(element, baseCEStereotype);
        doReturn(baseCRStereotype).when(configurationManagementService).getBaseCRStereotype();
        doReturn(true).when(apiDomain).hasStereotypeOrDerived(element, baseCRStereotype);
        doReturn(null).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, (Class) element);

        assertFalse(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }

    @Test
    public void canBePromoted_WithBaseCRStereotype_NoTransitions() {
        Stereotype baseCRStereotype = mock(Stereotype.class);
        element = mock(Class.class);

        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(baseCEStereotype).when(configurationManagementService).getBaseCEStereotype();
        doReturn(false).when(apiDomain).hasStereotypeOrDerived(element, baseCEStereotype);
        doReturn(baseCRStereotype).when(configurationManagementService).getBaseCRStereotype();
        doReturn(true).when(apiDomain).hasStereotypeOrDerived(element, baseCRStereotype);
        doReturn(changeRecord).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, (Class) element);
        doReturn(false).when(changeRecord).hasAvailableTransitions();

        assertFalse(lifecycleObjectDomain.canBePromoted(configurationManagementService, element));
    }
}
