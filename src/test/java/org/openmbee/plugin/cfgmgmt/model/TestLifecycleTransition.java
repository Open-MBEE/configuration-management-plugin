package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Transition;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestLifecycleTransition {
    private LifecycleTransition lifecycleTransition;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private Transition transition;
    private Vertex vertex;

    @Before
    public void setup() {
        transition = mock(Transition.class);
        apiDomain = mock(ApiDomain.class);
        configurationManagementService = mock(ConfigurationManagementService.class);
        lifecycleTransition = spy(new LifecycleTransition(configurationManagementService, transition));
        vertex = mock(Vertex.class);

        when(lifecycleTransition.getApiDomain()).thenReturn(apiDomain);
        doReturn(vertex).when(transition).getTarget();
    }

    @Test
    public void getRoles_nullFromApi() {
        doReturn(null).when(apiDomain).getStereotypePropertyValue(transition,
            PluginConstant.LIFECYCLE_TRANSITION_STEREOTYPE, PluginConstant.POLICY);

        assertTrue(lifecycleTransition.getRoles().isEmpty());
    }

    @Test
    public void getRoles_emptyListFromApi() {
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValue(transition,
            PluginConstant.LIFECYCLE_TRANSITION_STEREOTYPE, PluginConstant.POLICY);

        assertTrue(lifecycleTransition.getRoles().isEmpty());
    }

    @Test
    public void getRoles_wrongTypeFromApi() {
        List<Object> policies = new ArrayList<>();
        Object badObject = mock(Object.class);
        policies.add(badObject);

        doReturn(policies).when(apiDomain).getStereotypePropertyValue(transition,
            PluginConstant.LIFECYCLE_TRANSITION_STEREOTYPE, PluginConstant.POLICY);

        assertTrue(lifecycleTransition.getRoles().isEmpty());
    }

    @Test
    public void getRoles_policyFoundWithRoles() {
        List<Object> policies = new ArrayList<>();
        Class policy = mock(Class.class);
        policies.add(policy);
        List<String> roles = new ArrayList<>();
        String role = "role";
        roles.add(role);

        doReturn(policies).when(apiDomain).getStereotypePropertyValue(transition,
            PluginConstant.LIFECYCLE_TRANSITION_STEREOTYPE, PluginConstant.POLICY);
        doReturn(roles).when(apiDomain).getStereotypePropertyValueAsString(policy, PluginConstant.POLICY_STEREOTYPE, PluginConstant.ROLE);

        List<String> results = lifecycleTransition.getRoles();

        assertFalse(results.isEmpty());
        assertTrue(results.contains(role));
    }

    @Test
    public void getRoles_policyFoundWithRolesNull() {
        List<Object> policies = new ArrayList<>();
        Class policy = mock(Class.class);
        policies.add(policy);

        doReturn(policies).when(apiDomain).getStereotypePropertyValue(transition,
            PluginConstant.LIFECYCLE_TRANSITION_STEREOTYPE, PluginConstant.POLICY);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(policy, PluginConstant.POLICY_STEREOTYPE, PluginConstant.ROLE);

        List<String> results = lifecycleTransition.getRoles();
        assertTrue(results.isEmpty());
    }

    @Test
    public void isReleasingTransition_nullFromApi() {
        doReturn(null).when(apiDomain).getStereotypePropertyValue(vertex, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE,
            PluginConstant.IS_RELEASED_STATUS);

        assertFalse(lifecycleTransition.isReleasingTransition());
    }

    @Test
    public void isReleasingTransition_emptyListFromApi() {
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValue(vertex, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE,
            PluginConstant.IS_RELEASED_STATUS);

        assertFalse(lifecycleTransition.isReleasingTransition());
    }

    @Test
    public void isReleasingTransition_badStatus() {
        List<Object> statuses = new ArrayList<>();
        Object badObject = mock(Object.class);
        statuses.add(badObject);

        doReturn(statuses).when(apiDomain).getStereotypePropertyValue(vertex, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE,
            PluginConstant.IS_RELEASED_STATUS);

        assertFalse(lifecycleTransition.isReleasingTransition());
    }

    @Test
    public void isReleasingTransition_goodStatus() {
        List<Object> statuses = new ArrayList<>();
        statuses.add(true);

        doReturn(statuses).when(apiDomain).getStereotypePropertyValue(vertex, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE,
            PluginConstant.IS_RELEASED_STATUS);

        assertTrue(lifecycleTransition.isReleasingTransition());
    }

    @Test
    public void getTargetStatusName_nullTransition() {
        lifecycleTransition = spy(new LifecycleTransition(configurationManagementService, null));

        assertNull(lifecycleTransition.getTargetStatusName());
    }

    @Test
    public void getTargetStatusName_nullTarget() {
        doReturn(null).when(transition).getTarget();

        assertNull(lifecycleTransition.getTargetStatusName());
    }

    @Test
    public void getTargetStatusName_hasName() {
        String name = "name";

        doReturn(name).when(vertex).getName();

        String result = lifecycleTransition.getTargetStatusName();

        assertNotNull(result);
        assertEquals(name, result);
    }

    @Test
    public void getTargetStatus() {
        LifecycleObjectFactory factory = mock(LifecycleObjectFactory.class);
        State state = mock(State.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);

        doReturn(factory).when(configurationManagementService).getLifecycleObjectFactory();
        doReturn(lifecycleStatus).when(factory).getLifecycleStatus(configurationManagementService, state);
        doReturn(state).when(transition).getTarget();

        LifecycleStatus result = lifecycleTransition.getTargetStatus();

        assertNotNull(result);
        assertEquals(lifecycleStatus, result);
    }

    @Test
    public void getNameTest() {
        when(transition.getName()).thenReturn("name");
        lifecycleTransition.getName();
        verify(transition).getName();
    }
}