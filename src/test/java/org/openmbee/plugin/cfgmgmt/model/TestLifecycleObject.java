package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.listeners.LifecycleObjectPropertyChangeListener;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestLifecycleObject {
    private ConfigurationManagementService configurationManagementService;
    private LifecycleObject lifecycleObject;
    private Element element;
    private String elementId;
    private Stereotype stereotype;
    private NamedElement namedElement;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private LifecycleStatus lifecycleStatus;
    private State state;
    private Lifecycle lifeCycle;
    private ChangeRecord changeRecord;
    protected List<LifecycleObjectPropertyChangeListener> propertyChangeListeners;
    private String qualifiedName;
    private Logger logger;
    private LifecycleObjectFactory lifecycleObjectFactory;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        stereotype = mock(Stereotype.class);
        element = mock(Element.class);
        elementId = "elementId";
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        namedElement = mock(NamedElement.class);
        lifecycleStatus = mock(LifecycleStatus.class);
        state = mock(State.class);
        lifeCycle = mock(Lifecycle.class);
        propertyChangeListeners = new ArrayList<>();
        changeRecord = mock(ChangeRecord.class);
        logger = mock(Logger.class);
        lifecycleObjectFactory = mock(LifecycleObjectFactory.class);
        lifecycleObject = spy(new LifecycleObject(configurationManagementService, element, stereotype) {
            @Override
            public boolean canBePromoted(LifecycleTransition lifecycleTransition, ChangeRecord changeRecord) {
                return true;
            }
        });
        qualifiedName = "qualifiedName";

        when(configurationManagementService.getApiDomain()).thenReturn(apiDomain);
        when(configurationManagementService.getUIDomain()).thenReturn(uiDomain);
        when(lifecycleObject.getLogger()).thenReturn(logger);
        when(configurationManagementService.getLifecycleObjectFactory()).thenReturn(lifecycleObjectFactory);
        when(element.getLocalID()).thenReturn(elementId);
    }

    @Test
    public void equals_identityTest() {
        LifecycleObject lifecycleObject1 = new LifecycleObjectDummy(configurationManagementService, element, stereotype);
        assertTrue(lifecycleObject1.equals(lifecycleObject1));
    }

    @Test
    public void equals_NullTest() {
        LifecycleObject lifecycleObject1 = new LifecycleObjectDummy(configurationManagementService, element, stereotype);
        assertFalse(lifecycleObject1.equals(null));
    }

    @Test
    public void equals_wrongType() {
        LifecycleObject lifecycleObject1 = new LifecycleObjectDummy(configurationManagementService, element, stereotype);
        assertFalse(lifecycleObject1.equals(0));
    }

    @Test
    public void equals_differentInternalElement() {
        Element otherElement = mock(Element.class);
        LifecycleObject thisn = new LifecycleObjectDummy(configurationManagementService, element, stereotype);
        LifecycleObject other = new LifecycleObjectDummy(configurationManagementService, otherElement, stereotype);
        when(otherElement.getLocalID()).thenReturn("otherId");
        assertFalse(thisn.equals(other));
    }

    @Test
    public void equals_sameInternalElement() {
        LifecycleObject thisn = new LifecycleObjectDummy(configurationManagementService, element, stereotype);
        LifecycleObject other = new LifecycleObjectDummy(configurationManagementService, element, stereotype);
        assertTrue(thisn.equals(other));
    }

    @Test
    public void getLifecycleObjectFactory() {
        assertSame(lifecycleObjectFactory, lifecycleObject.getLifecycleObjectFactory());
    }

    @Test
    public void hashCode_sameInternalElement() {
        LifecycleObject thisn = new LifecycleObjectDummy(configurationManagementService, element, stereotype);
        LifecycleObject other = new LifecycleObjectDummy(configurationManagementService, element, stereotype);
        assertEquals(thisn.hashCode(), other.hashCode());
    }

    private static class LifecycleObjectDummy extends LifecycleObject {

        public LifecycleObjectDummy(ConfigurationManagementService configurationManagementService, Element element, @Nullable Stereotype baseStereotype) {
            super(configurationManagementService, element, baseStereotype);
        }

        @Override
        public boolean canBePromoted(LifecycleTransition tr, ChangeRecord cr) {
            return true;
        }
    }

    @Test
    public void getElement() {
        assertNotNull(lifecycleObject.getElement());
    }

    @Test
    public void isReadOnly_valueObtained() {
        try {
            doReturn(Optional.of(lifecycleStatus)).when(lifecycleObject).getStatus();
            when(lifecycleStatus.isReadOnly()).thenReturn(true);
            assertTrue(lifecycleObject.isReadOnly());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void isReadOnly_valueObtained1() {
        try {
            doReturn(Optional.of(lifecycleStatus)).when(lifecycleObject).getStatus();
            when(lifecycleStatus.isReadOnly()).thenReturn(false);
            assertFalse(lifecycleObject.isReadOnly());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void isReadOnly_valueObtained2() {
        try {
            doReturn(Optional.empty()).when(lifecycleObject).getStatus();
            when(lifecycleStatus.isReadOnly()).thenReturn(true);
            assertTrue(lifecycleObject.isReadOnly());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }


    @Test
    public void isReleased_valueObtained() {
        try {
            doReturn(Optional.of(lifecycleStatus)).when(lifecycleObject).getStatus();
            when(lifecycleStatus.isReleased()).thenReturn(true);
            assertTrue(lifecycleObject.isReleased());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void isReleased_valueObtained1() {
        try {
            doReturn(Optional.of(lifecycleStatus)).when(lifecycleObject).getStatus();
            when(lifecycleStatus.isReleased()).thenReturn(false);
            assertFalse(lifecycleObject.isReleased());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void isReleased_valueObtained2() {
        try {
            doReturn(Optional.empty()).when(lifecycleObject).getStatus();
            when(lifecycleStatus.isReleased()).thenReturn(true);
            assertTrue(lifecycleObject.isReleased());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getQualifiedName() {
        when(apiDomain.getNamedElement(element)).thenReturn(namedElement);
        when(namedElement.getQualifiedName()).thenReturn(qualifiedName);

        String result = lifecycleObject.getQualifiedName();

        assertNotNull(result);
        assertEquals(qualifiedName, result);
    }

    @Test
    public void getQualifiedName_Null() {
        when(apiDomain.getNamedElement(element)).thenReturn(null);
        assertEquals(PluginConstant.EMPTY_STRING, lifecycleObject.getQualifiedName());
    }

    @Test
    public void getApiDomain() {
        assertNotNull(lifecycleObject.getApiDomain());
    }

    @Test
    public void getUiDomain() {
        assertSame(uiDomain, lifecycleObject.getUIDomain());
    }

    @Test
    public void getName() {
        String elementName = "name";
        when(apiDomain.getNamedElement(element)).thenReturn(namedElement);
        when(namedElement.getName()).thenReturn(elementName);

        String result = lifecycleObject.getName();

        assertNotNull(result);
        assertEquals(elementName, result);
    }

    @Test
    public void getName_Null() {
        when(apiDomain.getNamedElement(element)).thenReturn(null);
        assertEquals(PluginConstant.EMPTY_STRING, lifecycleObject.getName());
    }

    @Test
    public void setDescription() {
        lifecycleObject.setDescription("Desc");
        verify(lifecycleObject).setDescription("Desc");
        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.DESCRIPTION, "Desc");
    }

    @Test
    public void getDescription_hasDescription() {
        String description = "description";
        List<Object> list = new ArrayList<>();
        list.add(description);
        when(apiDomain.getStereotypePropertyValue(element, stereotype, description)).thenReturn(list);
        assertEquals(description, lifecycleObject.getDescription());
    }

    @Test
    public void getDescription_null() {
        String description = "description";
        when(apiDomain.getStereotypePropertyValue(element, stereotype, description)).thenReturn(null);
        assertEquals(PluginConstant.EMPTY_STRING, lifecycleObject.getDescription());
    }

    @Test
    public void getDescription_Empty() {
        String description = "description";
        when(apiDomain.getStereotypePropertyValue(element, stereotype, description)).thenReturn(new ArrayList<>());
        assertEquals(PluginConstant.EMPTY_STRING, lifecycleObject.getDescription());
    }

    @Test
    public void setComments() {
        lifecycleObject.setComments("comment");
        verify(lifecycleObject).setComments("comment");
        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.COMMENTS, "comment");
    }

    @Test
    public void getComments() {
        String comments = "comments";
        List<Object> list = new ArrayList<>();
        list.add(comments);
        when(apiDomain.getStereotypePropertyValue(element, stereotype, comments)).thenReturn(list);
        assertEquals(comments, lifecycleObject.getComments());
        verify(lifecycleObject).getComments();
    }

    @Test
    public void getCommentsTest_null() {
        String comments = "comments";
        when(apiDomain.getStereotypePropertyValue(element, stereotype, comments)).thenReturn(null);
        assertEquals(PluginConstant.EMPTY_STRING, lifecycleObject.getComments());
    }

    @Test
    public void getCommentsTest_Empty() {
        String comments = "comments";
        when(apiDomain.getStereotypePropertyValue(element, stereotype, comments)).thenReturn(new ArrayList<>());
        assertEquals(PluginConstant.EMPTY_STRING, lifecycleObject.getComments());
    }

    @Test
    public void getStatusMaturityRating_ratingObtained() {
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        Optional<LifecycleStatus> status = Optional.of(lifecycleStatus);
        int rating = 1;
        try {
            doReturn(status).when(lifecycleObject).getStatus();
            when(lifecycleStatus.getMaturityRating()).thenReturn(rating);
            assertEquals(rating, lifecycleObject.getStatusMaturityRating());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getStatusColor_colorObtained() {
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        Optional<LifecycleStatus> status = Optional.of(lifecycleStatus);
        Color color = mock(Color.class);
        try {
            doReturn(status).when(lifecycleObject).getStatus();
            when(lifecycleStatus.getColor()).thenReturn(color);
            assertEquals(color, lifecycleObject.getStatusColor());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getInitialStatus_LifecycleNull() {
        try {
            doReturn(null).when(lifecycleObject).getLifecycle();
            LifecycleStatus lifecycleStatus = lifecycleObject.getInitialStatus();
            assertNull(lifecycleStatus);
            fail("Unexpected exception");
        } catch(Exception e) {
        }
    }

    @Test
    public void getInitialStatus_Success() {
        try {
            doReturn(Optional.of(lifeCycle)).when(lifecycleObject).getLifecycle();
            doReturn(lifecycleStatus).when(lifeCycle).getInitialStatus();
            LifecycleStatus result = lifecycleObject.getInitialStatus();
            assertNotNull(result);
            assertEquals(lifecycleStatus, result);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getStatus_nullStatusList() {
        String expectedMessage = String.format(ExceptionConstants.ELEMENT_LACKS_STATUS, qualifiedName);
        try {
            when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.STATUS)).thenReturn(null);
            doReturn(qualifiedName).when(lifecycleObject).getQualifiedName();

            lifecycleObject.getStatus();
            verify(lifecycleObject).getQualifiedName();
        } catch (Exception e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void getStatus_lifecycleStatusChanging() {
        doReturn(true).when(configurationManagementService).isLifecycleStatusChanging();
        
        assertSame(Optional.empty(), lifecycleObject.getStatus());
    }

    @Test
    public void getStatus_emptyStatusList() {
        List<Object> statusList = new ArrayList<>();
        String expectedMessage = String.format(ExceptionConstants.ELEMENT_LACKS_STATUS, qualifiedName);
        try {
            when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.STATUS)).thenReturn(statusList);
            doReturn(qualifiedName).when(lifecycleObject).getQualifiedName();

            lifecycleObject.getStatus();

        } catch (Exception e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void getStatus_unexpectedTypeInStatusList() {
        List<Object> statusList = new ArrayList<>();
        statusList.add(10);
        String expectedMessage = String.format(ExceptionConstants.ELEMENT_LACKS_STATUS, qualifiedName);
        try {
            when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.STATUS)).thenReturn(statusList);
            doReturn(qualifiedName).when(lifecycleObject).getQualifiedName();

            lifecycleObject.getStatus();

        } catch(Exception e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void getStatus_LifeCycleStatusNull() {
        List<Object> statusList = new ArrayList<>();
        statusList.add(PluginConstant.LIFECYCLE_STATUS_STEREOTYPE);

        try {
            doReturn(statusList).when(apiDomain).getStereotypePropertyValue(element, stereotype, PluginConstant.STATUS);
            doReturn(Optional.of(lifeCycle)).when(lifecycleObject).getLifecycle();
            doReturn(null).when(lifeCycle).getStatusByName(PluginConstant.LIFECYCLE_STATUS_STEREOTYPE);
            doReturn(qualifiedName).when(lifecycleObject).getQualifiedName();

            lifecycleObject.getStatus();
            verify(lifecycleObject).getLifecycle();
            verify(lifecycleStatus, never()).getState();
        } catch (Exception e) {
            fail("Expected exception did not occur");
        }
    }

    @Test
    public void getStatus_successWithString() {
        List<Object> statusList = new ArrayList<>();
        statusList.add(PluginConstant.LIFECYCLE_STATUS_STEREOTYPE);
        try {
            when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.STATUS)).thenReturn(statusList);
            doReturn(Optional.of(lifeCycle)).when(lifecycleObject).getLifecycle();
            when(lifeCycle.getStatusByName(PluginConstant.LIFECYCLE_STATUS_STEREOTYPE)).thenReturn(lifecycleStatus);

            Optional<LifecycleStatus> result = lifecycleObject.getStatus();

            assertTrue(result.isPresent());
            assertEquals(lifecycleStatus, result.get());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getStatus_instanceOfState() {
        List<Object> statusList = new ArrayList<>();
        statusList.add(state);
        try {
            when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.STATUS)).thenReturn(statusList);
            doReturn(lifecycleStatus).when(configurationManagementService).getLifecycleStatus(state);

            assertEquals(Optional.of(lifecycleStatus), lifecycleObject.getStatus());
        } catch (Exception exception) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void hasStatusTest() {
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        Optional<LifecycleStatus> status = Optional.of(lifecycleStatus);
        doReturn(status).when(lifecycleObject).getStatus();
        assertTrue(lifecycleObject.hasStatus());
    }

    @Test
    public void getStatusName_statusNameObtained() {
        String name = "name";
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        Optional<LifecycleStatus> status = Optional.of(lifecycleStatus);
        try {
            doReturn(status).when(lifecycleObject).getStatus();
            when(lifecycleStatus.getName()).thenReturn(name);

            assertEquals(name, lifecycleObject.getStatusName());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getTransitions_transitionsObtained() {
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        List<LifecycleTransition> list = new ArrayList<>();
        try {
            doReturn(Optional.of(lifecycleStatus)).when(lifecycleObject).getStatus();
            when(lifecycleStatus.getTransitions()).thenReturn(list);
            assertEquals(list, lifecycleObject.getTransitions());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void hasAvailableTransitions_null() {
        try {
            doReturn(null).when(lifecycleObject).getTransitions();

            assertFalse(lifecycleObject.hasAvailableTransitions());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void hasAvailableTransitions_emptyList() {
        List<LifecycleTransition> list = new ArrayList<>();
        try {
            doReturn(list).when(lifecycleObject).getTransitions();

            assertFalse(lifecycleObject.hasAvailableTransitions());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void hasAvailableTransitions_transitionsFound() {
        List<LifecycleTransition> list = new ArrayList<>();
        LifecycleTransition lifecycleTransition = mock(LifecycleTransition.class);
        list.add(lifecycleTransition);

        try {
            doReturn(list).when(lifecycleObject).getTransitions();

            assertTrue(lifecycleObject.hasAvailableTransitions());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getAppliedStereotype_nullList() {
        String expectedMessage = String.format(ExceptionConstants.UNABLE_TO_IDENTIFY_STEREOTYPE, qualifiedName);
        try {
            when(apiDomain.getDerivedStereotypes(element, stereotype, true)).thenReturn(null);
            doReturn(qualifiedName).when(lifecycleObject).getQualifiedName();

            lifecycleObject.getAppliedStereotype();

        } catch (Exception e) {
            fail("Expected exception did not occur");
        }
    }

    @Test
    public void getAppliedStereotype_emptyList() {
        String expectedMessage = String.format(ExceptionConstants.UNABLE_TO_IDENTIFY_STEREOTYPE, qualifiedName);
        try {
            when(apiDomain.getDerivedStereotypes(element, stereotype, true)).thenReturn(new ArrayList<>());
            doReturn(qualifiedName).when(lifecycleObject).getQualifiedName();

            lifecycleObject.getAppliedStereotype();

        } catch (Exception e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void getAppliedStereotype_listTooLarge() {
        Collection<Stereotype> listAppliedStereos = new ArrayList<>();
        Stereotype stereotype1 = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        listAppliedStereos.add(stereotype1);
        listAppliedStereos.add(stereotype2);
        String expectedMessage = String.format(ExceptionConstants.UNABLE_TO_IDENTIFY_STEREOTYPE, qualifiedName);

        try {
            when(apiDomain.getDerivedStereotypes(element, stereotype, true)).thenReturn(listAppliedStereos);
            doReturn(qualifiedName).when(lifecycleObject).getQualifiedName();

            assertNull(lifecycleObject.getAppliedStereotype());
        } catch(Exception e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void getAppliedStereotype_singletonList() {
        Collection<Stereotype> listAppliedStereos = new ArrayList<>();
        listAppliedStereos.add(stereotype);
        try {
            when(apiDomain.getDerivedStereotypes(element, stereotype, true)).thenReturn(listAppliedStereos);

            assertEquals(stereotype, lifecycleObject.getAppliedStereotype());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getLifecycle_GettingNullAppliedStereotype() {
        doReturn(null).when(lifecycleObject).getAppliedStereotype();
        assertEquals(Optional.empty(), lifecycleObject.getLifecycle());
        verify(lifecycleObject).getAppliedStereotype();
    }

    @Test
    public void getLifecycle_nullLifecycleList() {
        try {
            doReturn(stereotype).when(lifecycleObject).getAppliedStereotype();
            when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.LIFECYCLE)).thenReturn(null);

            lifecycleObject.getLifecycle();

        } catch (Exception e) {
            assertEquals(ExceptionConstants.NO_STATE_MACHINES, e.getMessage());
        }
    }

    @Test
    public void getLifecycle_emptyLifecycleList() {
        List<Object> statusList = new ArrayList<>();
        String expectedMessage = ExceptionConstants.INVALID_STATE_MACHINE_AMOUNT + statusList.size();
        try {
            doReturn(stereotype).when(lifecycleObject).getAppliedStereotype();
            when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.LIFECYCLE)).thenReturn(statusList);

            lifecycleObject.getLifecycle();

        } catch (Exception e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void getLifecycle_singletonButWrongType() {
        List<Object> statusList = new ArrayList<>();
        statusList.add(state);
        try {
            doReturn(stereotype).when(lifecycleObject).getAppliedStereotype();
            when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.LIFECYCLE)).thenReturn(Collections.singletonList(statusList));

            lifecycleObject.getLifecycle();

        } catch (Exception e) {
            assertEquals(ExceptionConstants.LIFECYCLE_PROPERTY_NOT_ON_STATE_MACHINE, e.getMessage());
        }
    }

    @Test
    public void getLifecycle_lifecycleObtained() {
        List<Object> statusList = new ArrayList<>();
        StateMachine stateMachine = mock(StateMachine.class);
        statusList.add(stateMachine);
        try {
            doReturn(stereotype).when(lifecycleObject).getAppliedStereotype();
            when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.LIFECYCLE)).thenReturn(statusList);
            when(configurationManagementService.getLifecycle(stateMachine)).thenReturn(lifeCycle);

            assertEquals(Optional.of(lifeCycle), lifecycleObject.getLifecycle());
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void changeStatus_MatchingData() {
        LifecycleObjectPropertyChangeListener listener1 = spy(new LifecycleObjectPropertyChangeListener(LifecycleObjectPropertyChangeListener.Property.STATUS) {
            @Override
            public void stateChanged(ChangeEvent e) {
            }
        });
        LifecycleObjectPropertyChangeListener listener2 = spy(new LifecycleObjectPropertyChangeListener(LifecycleObjectPropertyChangeListener.Property.REVISION) {
            @Override
            public void stateChanged(ChangeEvent e) {
            }
        });
        propertyChangeListeners.add(listener1);
        propertyChangeListeners.add(listener2);
        doReturn(propertyChangeListeners).when(lifecycleObject).getPropertyChangeListeners();
        ChangeEvent changeEvent = spy(new ChangeEvent(lifecycleObject));

        when(lifecycleStatus.getState()).thenReturn(state);
        doReturn(LifecycleObjectPropertyChangeListener.Property.STATUS).when(listener1).getProp();
        doReturn(LifecycleObjectPropertyChangeListener.Property.REVISION).when(listener2).getProp();
        when(lifecycleObject.makeEvent()).thenReturn(changeEvent);

        try {
            lifecycleObject.changeStatus(lifecycleStatus, changeRecord);

            verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.STATUS, state);
            verify(listener1).stateChanged(changeEvent);
            verify(listener2, never()).stateChanged(changeEvent);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }
}