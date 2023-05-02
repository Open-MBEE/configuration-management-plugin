package org.openmbee.plugin.cfgmgmt.factory;

import org.openmbee.plugin.cfgmgmt.model.*;
import org.openmbee.plugin.cfgmgmt.model.LifecycleTransition;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.utils.Policy;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Transition;
import org.openmbee.plugin.cfgmgmt.model.*;

public class LifecycleObjectFactory {

    public ConfiguredElement getConfiguredElement(ConfigurationManagementService configurationManagementService, Element element) {
        return new ConfiguredElement(configurationManagementService, element);
    }

    public ChangeRecord getChangeRecord(ConfigurationManagementService configurationManagementService, Class aClass) {
        return new ChangeRecord(configurationManagementService, aClass);
    }

    public Lifecycle getLifecycle(ConfigurationManagementService configurationManagementService, StateMachine stateMachine) {
        return new Lifecycle(configurationManagementService, stateMachine);
    }

    public LifecycleTransition getLifecycleTransition(ConfigurationManagementService configurationManagementService, Transition transition) {
        return new LifecycleTransition(configurationManagementService, transition);
    }

    public LifecycleStatus getLifecycleStatus(ConfigurationManagementService configurationManagementService, State state) {
        return new LifecycleStatus(configurationManagementService, state);
    }

    public Policy getPolicy(ConfigurationManagementService configurationManagementService, Class adminModeElement) {
        return adminModeElement != null ? new Policy(configurationManagementService.getApiDomain(), adminModeElement) : null;
    }

    public RevisionHistoryRecord getRevisionHistoryRecord(ConfigurationManagementService configurationManagementService,
                                         Class revisionHistoryRecordObject) {
        return new RevisionHistoryRecord(configurationManagementService, revisionHistoryRecordObject);
    }
}
