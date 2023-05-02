package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Region;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Transition;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Vertex;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestLifecycleStatus {
    private LifecycleStatus lifecycleStatus;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private Logger logger;
    private State state;

    @Before
    public void setup() {
        state = mock(State.class);
        configurationManagementService = mock(ConfigurationManagementService.class);
        lifecycleStatus = Mockito.spy(new LifecycleStatus(configurationManagementService, state));
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        logger = mock(Logger.class);

        when(lifecycleStatus.getApiDomain()).thenReturn(apiDomain);
        when(lifecycleStatus.getUIDomain()).thenReturn(uiDomain);
        when(lifecycleStatus.getLogger()).thenReturn(logger);
    }

    @Test
    public void equals_SelfObject() {
        LifecycleStatus lifecycleStatus1 = new LifecycleStatus(configurationManagementService, state);
        assertTrue(lifecycleStatus1.equals(lifecycleStatus1));
    }

    @Test
    public void equals_IdenticalObjects() {
        LifecycleStatus lifecycleStatus1 = new LifecycleStatus(configurationManagementService, state);
        LifecycleStatus lifecycleStatus2 = new LifecycleStatus(configurationManagementService, state);
        assertTrue(lifecycleStatus1.equals(lifecycleStatus2));
    }

    @Test
    public void equals_NullObject() {
        LifecycleStatus lifecycleStatus1 = new LifecycleStatus(configurationManagementService, state);
        LifecycleStatus lifecycleStatus2 = null;
        assertFalse(lifecycleStatus1.equals(lifecycleStatus2));
    }

    @Test
    public void equals_NonIdenticalObjects() {
        LifecycleStatus lifecycleStatus1 = new LifecycleStatus(configurationManagementService, state);
        List<Object> list = new ArrayList<>();
        assertFalse(lifecycleStatus1.equals(list));
    }

    @Test
    public void hashCodeTest() {
        LifecycleStatus thisn = new LifecycleStatusDummy(configurationManagementService, state);
        LifecycleStatus other = new LifecycleStatusDummy(configurationManagementService, state);
        assertEquals(thisn.hashCode(), other.hashCode());
    }

    private static class LifecycleStatusDummy extends LifecycleStatus {
        public LifecycleStatusDummy(ConfigurationManagementService configurationManagementService, State state) {
            super(configurationManagementService, state);
        }
    }

    @Test
    public void getName_Test() {
        String name = "name";
        doReturn(name).when(state).getName();
        assertEquals(name, lifecycleStatus.getName());
    }

    @Test
    public void getTransitions_noState() {
        lifecycleStatus = Mockito.spy(new LifecycleStatus(configurationManagementService, null));
        assertTrue(lifecycleStatus.getTransitions().isEmpty());
    }

    @Test
    public void getTransitions_stateHasNoOwner() {
        doReturn(null).when(state).getOwner();
        assertTrue(lifecycleStatus.getTransitions().isEmpty());
        verify(state).getOwner();
    }

    @Test
    public void getTransitions_noTransitions() {
        Region owner = mock(Region.class);

        doReturn(owner).when(state).getOwner();
        doReturn(null).when(owner).getTransition();

        assertTrue(lifecycleStatus.getTransitions().isEmpty());
        verify(configurationManagementService, never()).getLifecycleTransition(any());
    }

    @Test
    public void getTransitions_foundMatch() {
        Region owner = mock(Region.class);
        Collection<Transition> transitions = new ArrayList<>();
        Transition transition = mock(Transition.class);
        Transition transition2 = mock(Transition.class);
        Transition transition3 = mock(Transition.class);
        transitions.add(transition);
        transitions.add(transition2);
        transitions.add(transition3);
        LifecycleTransition lifecycleTransition = mock(LifecycleTransition.class);
        Vertex vertex = mock(Vertex.class);

        doReturn(owner).when(state).getOwner();
        doReturn(transitions).when(owner).getTransition();
        doReturn(null).when(transition).getSource();
        doReturn(state).when(transition2).getSource();
        doReturn(vertex).when(transition3).getSource();
        doReturn(lifecycleTransition).when(configurationManagementService).getLifecycleTransition(transition2);

        List<LifecycleTransition> results = lifecycleStatus.getTransitions();

        assertFalse(results.isEmpty());
        assertEquals(lifecycleTransition, results.get(0));
        verify(configurationManagementService).getLifecycleTransition(transition2);
        verify(configurationManagementService, never()).getLifecycleTransition(transition3);
    }

    @Test
    public void getMaturityRating_nullList() {
        doReturn(null).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.MATURITY_RATING);
        assertEquals(-99, lifecycleStatus.getMaturityRating());
    }

    @Test
    public void getMaturityRating_emptyList() {
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.MATURITY_RATING);
        assertEquals(-99, lifecycleStatus.getMaturityRating());
    }

    @Test
    public void getMaturityRating_populatedList() {
        List<Object> list = new ArrayList<>();
        list.add(1);
        doReturn(list).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.MATURITY_RATING);

        assertEquals(1, lifecycleStatus.getMaturityRating());
    }

    @Test
    public void getMaturityRating_ListSizeGreaterThanOne() {
        List<Object> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        doReturn(list).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.MATURITY_RATING);

        assertEquals(-99, lifecycleStatus.getMaturityRating());
    }

    @Test
    public void getMaturityRating_populatedListInvalidType() {
        List<Object> list = new ArrayList<>();
        list.add("string");
        doReturn(list).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.MATURITY_RATING);

        assertEquals(-99, lifecycleStatus.getMaturityRating());
        verify(uiDomain).logError(PluginConstant.INVALID_TYPE + PluginConstant.MATURITY_RATING);
    }

    @Test
    public void getColor_nullTest() {
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.ICON_ADORNMENT_COLOR);
        assertNull(lifecycleStatus.getColor());
        verify(uiDomain, never()).logError(any(), anyString());
    }

    @Test
    public void getColor_populatedList() {
        String colorString = "RED";
        Color color = Color.RED;
        List<String> colorList = new ArrayList<>();
        colorList.add(colorString);
        try {
            doReturn(colorList).when(apiDomain).getStereotypePropertyValueAsString(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.ICON_ADORNMENT_COLOR);
            doReturn(color).when(lifecycleStatus).getColorFromList(colorList);

            assertEquals(color, lifecycleStatus.getColor());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getColor_ListGreaterThanOne() {
        String colorString = "RED";
        List<String> colorList = new ArrayList<>();
        colorList.add(colorString);
        colorList.add(colorString);
        doReturn(colorList).when(apiDomain).getStereotypePropertyValueAsString(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.ICON_ADORNMENT_COLOR);

        assertNull(lifecycleStatus.getColor());
        verify(uiDomain, never()).logError(any(), anyString());
    }

    @Test
    public void getColor_populatedListException() {
        String colorString = "RED";
        List<String> colorList = new ArrayList<>();
        colorList.add(colorString);
        String error = "error";
        NoSuchFieldException noSuchFieldException = spy(new NoSuchFieldException(error));

        try {
            doReturn(colorList).when(apiDomain).getStereotypePropertyValueAsString(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.ICON_ADORNMENT_COLOR);
            doThrow(noSuchFieldException).when(lifecycleStatus).getColorFromList(colorList);

            assertNull(lifecycleStatus.getColor());
            verify(uiDomain).logError(logger, error);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getColorFromList() {
        String colorString = "RED";
        List<String> colorList = new ArrayList<>();
        colorList.add(colorString);
        try {
            assertNotNull(lifecycleStatus.getColorFromList(colorList));
        } catch (Exception e) {
            fail("Unexpected Exception");
        }
    }

    @Test
    public void getBooleanProperty_nullList() {
        String property = PluginConstant.IS_READ_ONLY_STATUS;
        doReturn(null).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, property);

        assertFalse(lifecycleStatus.getBooleanProperty(PluginConstant.IS_READ_ONLY_STATUS));
        verify(uiDomain).logError(PluginConstant.LIFECYCLE_PROPERTY_RETRIEVAL_FAILED);
    }

    @Test
    public void getBooleanProperty_emptyList() {
        String property = PluginConstant.IS_READ_ONLY_STATUS;
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, property);

        assertFalse(lifecycleStatus.getBooleanProperty(PluginConstant.IS_READ_ONLY_STATUS));
        verify(uiDomain).logError(PluginConstant.LIFECYCLE_PROPERTY_RETRIEVAL_FAILED);
    }

    @Test
    public void getBooleanProperty_readOnlyStatus() {
        String property = PluginConstant.IS_READ_ONLY_STATUS;
        List<Object> statusList = new ArrayList<>();
        statusList.add(true);
        doReturn(statusList).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, property);

        assertTrue(lifecycleStatus.getBooleanProperty(property));
    }

    @Test
    public void getBooleanProperty_invalidType() {
        String property = PluginConstant.IS_READ_ONLY_STATUS;
        List<Object> statusList = new ArrayList<>();
        int propertyValue = 100;
        statusList.add(propertyValue);
        doReturn(statusList).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, property);

        assertFalse(lifecycleStatus.getBooleanProperty(property));
        verify(uiDomain).logError(PluginConstant.INVALID_TYPE + property);
    }

    @Test
    public void isReadOnly() {
        String property = PluginConstant.IS_READ_ONLY_STATUS;
        List<Object> statusList = new ArrayList<>();
        statusList.add(true);
        doReturn(statusList).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, property);

        assertTrue(lifecycleStatus.isReadOnly());
    }

    @Test
    public void isReleased() {
        String property = PluginConstant.IS_RELEASED_STATUS;
        List<Object> statusList = new ArrayList<>();
        statusList.add(true);
        doReturn(statusList).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, property);

        assertTrue(lifecycleStatus.isReleased());
    }

    @Test
    public void isReadyForRelease() {
        String property = PluginConstant.IS_READY_FOR_RELEASE;
        List<Object> statusList = new ArrayList<>();
        statusList.add(true);
        doReturn(statusList).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, property);

        assertTrue(lifecycleStatus.isReadyForRelease());
    }

    @Test
    public void isExpendable() {
        String property = PluginConstant.IS_EXPANDABLE_STATUS;
        List<Object> statusList = new ArrayList<>();
        statusList.add(true);
        doReturn(statusList).when(apiDomain).getStereotypePropertyValue(state, PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, property);

        assertTrue(lifecycleStatus.isExpandable());
    }
}
