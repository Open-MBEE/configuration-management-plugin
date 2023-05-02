package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestLifecycle {
    private Lifecycle lifecycle;
    private ConfigurationManagementService configurationManagementService;
    private StateMachine stateMachine;
    private Region region;
    private Collection<Region> regions;
    private Vertex vertex;
    private Collection<Vertex> vertices;
    private Transition transition;
    private Collection<Transition> transitions;
    private LifecycleStatus lifecycleStatus;
    private State state;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        stateMachine = mock(StateMachine.class);
        lifecycle = spy(new Lifecycle(configurationManagementService, stateMachine));

        region = mock(Region.class);
        regions = new ArrayList<>();
        regions.add(region);
        vertex = mock(State.class);
        vertices = new ArrayList<>();
        vertices.add(vertex);
        transition = mock(Transition.class);
        transitions = new ArrayList<>();
        transitions.add(transition);
        lifecycleStatus = mock(LifecycleStatus.class);
        state = mock(State.class);

        doReturn(regions).when(stateMachine).getRegion();
        doReturn(vertices).when(region).getSubvertex();
        doReturn(transitions).when(region).getTransition();
        when(lifecycle.getConfigurationManagementService()).thenReturn(configurationManagementService);
        doReturn(lifecycleStatus).when(lifecycle).createLifecycleStatus(state);
    }

    @Test
    public void getInitialStatus_noRegions() {
        doReturn(null).when(stateMachine).getRegion();

        assertNull(lifecycle.getInitialStatus());
        verify(lifecycle, never()).findInitialVertex(regions);
    }

    @Test
    public void getInitialStatus_initialVertexNotFound() {
        doReturn(regions).when(stateMachine).getRegion();
        doReturn(null).when(lifecycle).findInitialVertex(regions);

        assertNull(lifecycle.getInitialStatus());
        verify(lifecycle, never()).getLifecycleStatusUsingTransitionTarget(regions, vertex);
    }

    @Test
    public void getInitialStatus_initialVertexFoundAndInTransitionTarget() {
        doReturn(regions).when(stateMachine).getRegion();
        doReturn(vertex).when(lifecycle).findInitialVertex(regions);
        doReturn(lifecycleStatus).when(lifecycle).getLifecycleStatusUsingTransitionTarget(regions, vertex);

        assertNotNull(lifecycle.getInitialStatus());
    }

    @Test
    public void findInitialVertex_noVertices() {
        PseudostateKind pseudostateKind = mock(PseudostateKind.class);
        doReturn(null).when(region).getSubvertex();

        assertNull(lifecycle.findInitialVertex(regions));
        verify(lifecycle, never()).isVertexKindInitial(pseudostateKind);
    }

    @Test
    public void findInitialVertex_vertexNotFound_NotInstance() {
        vertex = mock(State.class);
        vertices.clear();
        vertices.add(vertex);

        doReturn(vertices).when(region).getSubvertex();

        assertNull(lifecycle.findInitialVertex(regions));
    }

    @Test
    public void findInitialVertex_vertexNotFound_PseudostateNull() {
        vertex = mock(Pseudostate.class);
        vertices.clear();
        vertices.add(vertex);

        doReturn(null).when((Pseudostate) vertex).getKind();

        assertNull(lifecycle.findInitialVertex(regions));
    }

    @Test
    public void findInitialVertex_vertexNotFound() {
        PseudostateKind pseudostateKind = mock(PseudostateKind.class);
        vertex = mock(Pseudostate.class);
        vertices.clear();
        vertices.add(vertex);

        doReturn(pseudostateKind).when((Pseudostate) vertex).getKind();
        doReturn(false).when(lifecycle).isVertexKindInitial(pseudostateKind);

        assertNull(lifecycle.findInitialVertex(regions));
    }

    @Test
    public void findInitialVertex_vertexFound() {
        PseudostateKind pseudostateKind = mock(PseudostateKind.class);
        vertex = mock(Pseudostate.class);
        vertices.clear();
        vertices.add(vertex);

        doReturn(pseudostateKind).when((Pseudostate) vertex).getKind();
        doReturn(true).when(lifecycle).isVertexKindInitial(pseudostateKind);

        Vertex result = lifecycle.findInitialVertex(regions);

        assertNotNull(result);
        assertEquals(vertex, result);
    }

    @Test
    public void getLifecycleStatusUsingTransitionTarget_noTransitions() {
        doReturn(null).when(region).getTransition();

        assertNull(lifecycle.getLifecycleStatusUsingTransitionTarget(regions, vertex));
        verify(transition,never()).getSource();
    }

    @Test
    public void getLifecycleStatusUsingTransitionTarget_nullTransition() {
        transitions.clear();
        transitions.add(null);

        assertNull(lifecycle.getLifecycleStatusUsingTransitionTarget(regions, vertex));
        verify(lifecycle, never()).createLifecycleStatus(state);
    }

    @Test
    public void getLifecycleStatusUsingTransitionTarget_noSource() {
        Vertex target = mock(State.class);

        doReturn(null).when(transition).getSource();
        doReturn(target).when(transition).getTarget();

        assertNull(lifecycle.getLifecycleStatusUsingTransitionTarget(regions, vertex));
        verify(lifecycle, never()).createLifecycleStatus(state);
    }

    @Test
    public void getLifecycleStatusUsingTransitionTarget_Source() {
        Vertex target = mock(Pseudostate.class);

        doReturn(vertex).when(transition).getSource();
        doReturn(target).when(transition).getTarget();

        assertNull(lifecycle.getLifecycleStatusUsingTransitionTarget(regions, vertex));
        verify(lifecycle, never()).createLifecycleStatus(state);
    }

    @Test
    public void getLifecycleStatusUsingTransitionTarget_noMatch() {
        Vertex target = mock(State.class);

        doReturn(mock(State.class)).when(transition).getSource();
        doReturn(target).when(transition).getTarget();

        assertNull(lifecycle.getLifecycleStatusUsingTransitionTarget(regions, vertex));
        verify(lifecycle, never()).createLifecycleStatus(state);
    }

    @Test
    public void getLifecycleStatusUsingTransitionTarget_matchFound() {
        Vertex target = mock(State.class);

        doReturn(vertex).when(transition).getSource();
        doReturn(target).when(transition).getTarget();
        doReturn(lifecycleStatus).when(lifecycle).createLifecycleStatus((State) target);

        LifecycleStatus result = lifecycle.getLifecycleStatusUsingTransitionTarget(regions, vertex);

        assertNotNull(result);
        assertSame(lifecycleStatus, result);
    }

    @Test
    public void getStatusByName_noRegions() {
        String statusName = "statusName";

        doReturn(null).when(stateMachine).getRegion();

        assertNull(lifecycle.getStatusByName(statusName));
        verify(lifecycle, never()).createLifecycleStatusForVertex(null, statusName);
    }

    @Test
    public void getStatusByName_noVertices() {
        String statusName = "statusName";
        doReturn(null).when(region).getSubvertex();
        doReturn(null).when(lifecycle).createLifecycleStatusForVertex(null, statusName);

        assertNull(lifecycle.getStatusByName(statusName));

    }

    @Test
    public void getStatusByName_validVertices() {
        String statusName = "statusName";
        Vertex vertex = mock(Pseudostate.class);
        vertices.clear();
        vertices.add(vertex);
        doReturn(vertices).when(region).getSubvertex();
        doReturn(lifecycleStatus).when(lifecycle).createLifecycleStatusForVertex(vertices, statusName);

        assertEquals(lifecycleStatus, lifecycle.getStatusByName(statusName));
    }

    @Test
    public void createLifecycleStatusForVertex_nullVertices() {
        String stausName = "statusName";
        assertNull(lifecycle.createLifecycleStatusForVertex(null, stausName));
        verify(lifecycle, never()).createLifecycleStatus(any());
    }

    @Test
    public void createLifecycleStatusForVertex_vertexWithNoMatchingName() {
        String statusName = "statusName";
        Vertex vertex = mock(State.class);
        vertices.clear();
        vertices.add(vertex);

        doReturn("").when(vertex).getName();
        doReturn(vertices).when(region).getSubvertex();

        assertNull(lifecycle.createLifecycleStatusForVertex(vertices, statusName));
        verify(lifecycle, never()).createLifecycleStatus(any());
    }

    @Test
    public void createLifecycleStatusForVertex_vertexNotInstanceOfState() {
        String statusName = "statusName";
        vertices.clear();
        vertices.add(null);
        doReturn(lifecycleStatus).when(lifecycle).createLifecycleStatus((State) vertex);

        assertNull(lifecycle.getStatusByName(statusName));
        verify(lifecycle, never()).createLifecycleStatus(any());
    }

    @Test
    public void createLifecycleStatusForVertexe_vertexWithMatchingNameFound() {
        String statusName = "statusName";
        Vertex vertex = mock(State.class);
        vertices.clear();
        vertices.add(vertex);

        doReturn(statusName).when(vertex).getName();
        doReturn(lifecycleStatus).when(lifecycle).createLifecycleStatus((State) vertex);

        LifecycleStatus result = lifecycle.createLifecycleStatusForVertex(vertices, statusName);

        assertNotNull(result);
        assertSame(lifecycleStatus, result);
    }

    @Test
    public void isVertexKindInitial_True() {
        boolean booleanValue = lifecycle.isVertexKindInitial(PseudostateKindEnum.INITIAL);
        assertTrue(booleanValue);
    }

    @Test
    public void isVertexKindInitial_False() {
        boolean booleanValue = lifecycle.isVertexKindInitial(PseudostateKindEnum.CHOICE);
        assertFalse(booleanValue);
    }

    @Test
    public void getName() {
        String name = "name";
        doReturn(name).when(stateMachine).getName();
        assertSame(name, lifecycle.getName());
    }

    @Test
    public void createLifecycleStatus() {
        State state1 = mock(State.class);
        when(lifecycle.getConfigurationManagementService()).thenReturn(configurationManagementService);
        LifecycleObjectFactory lifecycleObjectFactory = mock(LifecycleObjectFactory.class);
        when(configurationManagementService.getLifecycleObjectFactory()).thenReturn(lifecycleObjectFactory);
        when(lifecycleObjectFactory.getLifecycleStatus(configurationManagementService, state1)).thenReturn(lifecycleStatus);
        assertNotNull(lifecycle.createLifecycleStatus(state1));
        verify(lifecycleObjectFactory, times(1)).getLifecycleStatus(configurationManagementService, state1);
    }
}
