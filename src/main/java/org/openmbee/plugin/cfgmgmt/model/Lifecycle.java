package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.*;

import java.util.Collection;

public class Lifecycle {
    private final ConfigurationManagementService configurationManagementService;
    private StateMachine stateMachine;

    public Lifecycle(ConfigurationManagementService configurationManagementService, StateMachine stateMachine) {
        this.configurationManagementService = configurationManagementService;
        this.stateMachine = stateMachine;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    public LifecycleStatus getInitialStatus() {
        Collection<Region> regions = stateMachine.getRegion();
        if (regions != null) {
            Vertex initial = findInitialVertex(regions);
            return initial != null ? getLifecycleStatusUsingTransitionTarget(regions, initial) : null;
        }
        return null;
    }

    protected Vertex findInitialVertex(Collection<Region> regions) {
        for (Region r : regions) {
            Collection<Vertex> vertices = r.getSubvertex();
            if (vertices != null) {
                for (Vertex v : vertices) {
                    if (v instanceof Pseudostate && ((Pseudostate) v).getKind() != null && isVertexKindInitial(((Pseudostate) v).getKind())) {
                        return v;
                    }
                }
            }
        }
        return null;
    }

    protected boolean isVertexKindInitial(PseudostateKind kind) {
        return kind.equals(PseudostateKindEnum.INITIAL); // used for unit testing
    }

    protected LifecycleStatus getLifecycleStatusUsingTransitionTarget(Collection<Region> regions, Vertex initial) {
        for (Region r : regions) {
            Collection<Transition> transitions = r.getTransition();
            if (transitions != null) {
                for (Transition t : transitions) {
                    if (t != null && t.getSource() != null && t.getSource().equals(initial) && t.getTarget() instanceof State) {
                        return createLifecycleStatus((State) t.getTarget());
                    }
                }
            }
        }
        return null;
    }

    public LifecycleStatus getStatusByName(String statusName) {
        Collection<Region> regions = stateMachine.getRegion();
        if (regions != null) {
            for (Region r : regions) {
                Collection<Vertex> vertices = r.getSubvertex();
                LifecycleStatus initialVertex = createLifecycleStatusForVertex(vertices, statusName);
                if (initialVertex != null) {
                    return initialVertex;
                }
            }
        }
        return null;
    }

    protected LifecycleStatus createLifecycleStatusForVertex(Collection<Vertex> vertices, String statusName) {
        if (vertices != null) {
            for (Vertex v : vertices) {
                if (v instanceof State && v.getName().equals(statusName)) {
                    return createLifecycleStatus((State) v);
                }
            }
        }
        return null;
    }

    protected LifecycleStatus createLifecycleStatus(State st) {
        return getConfigurationManagementService().getLifecycleObjectFactory().getLifecycleStatus(getConfigurationManagementService(), st);
    }

    public String getName() {
        return stateMachine.getName();
    }
}
