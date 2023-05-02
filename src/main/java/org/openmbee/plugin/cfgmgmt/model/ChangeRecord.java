package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.domain.ChangeRecordDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ChangeRecord extends LifecycleObject {
    public ChangeRecord(ConfigurationManagementService configurationManagementService, Class changeRecord) {
        super(configurationManagementService, changeRecord, configurationManagementService.getBaseCRStereotype());
    }

    public ChangeRecordDomain getChangeRecordDomain() {
        return getConfigurationManagementService().getChangeRecordDomain();
    }

    @Override
    public boolean changeStatus(LifecycleStatus status, ChangeRecord changeRecord) {
        if (status != null) {
            if (status.isReleased() && getConfigurationManagementService().getAutomateReleaseSwitch()) {
                boolean released = releaseAffectedElements();
                if(released) {
                    super.changeStatus(status, changeRecord);
                }
                return released;
            }
            if(!super.changeStatus(status, changeRecord)) {
                return false;
            }
            getConfigurationManagementService().updateCRStatus();
            return true;
        }
        return false;
    }

    public boolean releaseAffectedElements() {
        List<ConfiguredElement> affectedElements = getAffectedElements();
        try {
            getConfigurationManagementService().setLifecycleStatusChanging(false); // doing this solves an interleaving problem
            Map<ConfiguredElement, LifecycleStatus> releaseTransitions = getChangeRecordDomain().getReleaseTransitionsForElements(this, affectedElements);
            if(!releaseTransitions.isEmpty()) {
                return getChangeRecordDomain().transitionElements(this, releaseTransitions, getConfigurationManagementService());
            }
        } catch(Exception e) {
            getUIDomain().logError(getLogger(), ExceptionConstants.ERROR_WHILE_CHANGING_STATUS);
            return false;
        } finally {
            getConfigurationManagementService().setLifecycleStatusChanging(true);
        }

        return false;
    }

    public boolean checkCEsForReadiness(LifecycleStatus futureStatus) {
        if (futureStatus.isReleased() && getConfigurationManagementService().getAutomateReleaseSwitch()) {
            return getChangeRecordDomain().areAllElementsReadyForRelease(getAffectedElements());
        } else {
            return getChangeRecordDomain().areAllElementsProperlyMature(getAffectedElements(), futureStatus.getMaturityRating(), this);
        }
    }

    @Override
    public boolean canBePromoted(LifecycleTransition transition, ChangeRecord activeCR) {
        LifecycleStatus futureStatus = transition.getTargetStatus();
        if(!checkCEsForReadiness(futureStatus)) {
            return false;
        }

        return getConfigurationManagementService().userHasPrivileges(transition.getRoles());
    }

    public void addAffectedElement(ConfiguredElement configuredElement, String changeText) {
        getChangeRecordDomain().addAffectedElement(this, configuredElement, changeText);
    }

    public List<ConfiguredElement> getAffectedElements() {
        return getChangeRecordDomain().getAffectedElements(getElement(), getBaseStereotype(), getConfigurationManagementService());
    }

    public boolean affectsGivenConfiguredElement(ConfiguredElement configuredElement) {
        return getAffectedElements().contains(configuredElement);
    }

    public boolean isExpandable() {
        Optional<LifecycleStatus> status = getStatus();
        return status.isPresent() && status.get().isExpandable();
    }

    public ZonedDateTime getConfigureTimeFromElementComments(ConfiguredElement configuredElement) {
        return getChangeRecordDomain().getCreationTimeFromElementComments(this, configuredElement);
    }
}
