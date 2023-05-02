package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.listeners.LifecycleObjectPropertyChangeListener;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import javax.swing.event.ChangeEvent;
import java.util.List;
import java.util.Optional;

public class ConfiguredElement extends LifecycleObject {
    public ConfiguredElement(ConfigurationManagementService configurationManagementService, Element element) {
        super(configurationManagementService, element, configurationManagementService.getBaseCEStereotype());
    }

    public ConfiguredElementDomain getConfiguredElementDomain() {
        return getConfigurationManagementService().getConfiguredElementDomain();
    }

    @Override
    public Stereotype getBaseStereotype() {
        return baseStereotype;
    }

    @Override
    public boolean changeStatus(LifecycleStatus status, ChangeRecord changeRecord) {
        try {
            getConfigurationManagementService().setLifecycleStatusChanging(false); // doing this solves an interleaving problem
            String currentStatusName = getStatusName();
            if(currentStatusName == null) {
                getUIDomain().logDebug(PluginConstant.INVALID_STATUS_NAME);
                return false;
            }

            if(!super.changeStatus(status, changeRecord)) {
                return false;
            }

            // if going to a released state, set all the release attributes
            Optional<LifecycleStatus> changedStatus = getStatus();
            if (changedStatus.isPresent() && changedStatus.get().isReleased()) {
                getConfiguredElementDomain().setReleaseAttributes(this);
                //unlocking the element tree after release
                getApiDomain().unlock(getElement());
            }

            changeRecord.addAffectedElement(this, String.format(ExceptionConstants.STATUS_CHANGE, currentStatusName, status.getName()));
        } catch(Exception e) {
            getUIDomain().logError(getLogger(), ExceptionConstants.ERROR_WHILE_GETTING_STATUS_NAME);
            return false;
        } finally {
            getConfigurationManagementService().setLifecycleStatusChanging(true);
        }
        return true;
    }

    public ChangeRecord getReleaseAuthority() {
       return getConfiguredElementDomain().getReleaseAuthority(this);
    }

    public void setIsCommitted(boolean newVal) {
        getConfiguredElementDomain().setIsCommitted(this, newVal);
    }

    public boolean isCommitted() {
        return getConfiguredElementDomain().isCommitted(this);
    }

    public String getID() {
        return getConfiguredElementDomain().getID(this);
    }

    public String getRevision() {
        return getConfiguredElementDomain().getRevision(this);
    }

    public String getRevisionCreationDate() {
        return getConfiguredElementDomain().getRevisionCreationDate(this);
    }

    public String getRevisionReleaseDate() {
        return getConfiguredElementDomain().getRevisionReleaseDate(this);
    }

    public boolean canBeRevised() {
        if(!getConfiguredElementDomain().canUserPerformAction(getConfigurationManagementService(), getAppliedStereotype(), "revision")) {
            getUIDomain().logError(PluginConstant.INSUFFICIENT_PERMISSIONS);
            return false;
        }

        // Checking if the status of the CCZ owner is compatible
        LifecycleStatus initialStatus = getInitialStatus();
        if(initialStatus == null || !getConfiguredElementDomain().validateProposedMaturityRatingWRTCczOwner(this, initialStatus.getMaturityRating())) {
            getUIDomain().logError(PluginConstant.INVALID_MATURITY_RATING);
            return false;
        }

        return true;
    }

    public boolean canBePromoted() {
        return getConfiguredElementDomain().canBePromoted(getConfigurationManagementService(), getElement());
    }

    @Override
    public boolean canBePromoted(LifecycleTransition transition, ChangeRecord changeRecord) {
        LifecycleStatus newStatus = transition.getTargetStatus();
        int crMaturity = changeRecord.getStatusMaturityRating();
        int current = getStatusMaturityRating();
        int future = newStatus.getMaturityRating();
        if (future > current && !getConfiguredElementDomain().validateProposedMaturityRatingWRTOwned(this, future)) {
            return false; // if the validate method returns false, an error is logged there
        } else if (future < current && !getConfiguredElementDomain().validateProposedMaturityRatingWRTCczOwner(this, future)) {
            return false; // if the validate method returns false, an error is logged there
        }

        if (future < crMaturity) {
            getUIDomain().logError(String.format(ExceptionConstants.UNACCEPTABLE_STATUS_COMPARED_TO_CR, changeRecord.getQualifiedName()));
            return false;
        }

        if(!getConfigurationManagementService().userHasPrivileges(transition.getRoles())) {
            getUIDomain().logError(String.format(ExceptionConstants.INSUFFICIENT_PRIVILEGES, String.join(PluginConstant.COMMA, transition.getRoles())));
            return false;
        }

        return true;
    }

    public boolean revise() {
        // Change status to in Work
        if(!getConfiguredElementDomain().setStatusToInWork(this)) {
            getUIDomain().logError(ExceptionConstants.CANNOT_REVISE_DUE_TO_STATUS_ISSUE);
            return false;
        }

        // Create Revision History Record
        Class clazz = getConfiguredElementDomain().createRevisionHistoryRecord(this);

        // Iterate revision
        getConfiguredElementDomain().iterateRevision(this);

        // attach the rev history to the element
        getConfiguredElementDomain().attachRevisionHistoryRecord(this, clazz);

        // Reset creation/release attributes
        getConfiguredElementDomain().resetRevisionAttributes(this);

        propertyChangeListeners.stream()
                .filter(l -> l.getProp().equals(LifecycleObjectPropertyChangeListener.Property.REVISION))
                .forEach(l -> l.stateChanged(makeEvent()));

        return true;
    }

    @Override
    protected ChangeEvent makeEvent() {
        return new ChangeEvent(this);
    }

    public ConfiguredElement getCCZOwner() {
        return getConfigurationManagementService().getCCZOwner(element);
    }

    public List<ConfiguredElement> getOwnedConfiguredElements() {
        return getConfigurationManagementService().getOwnedConfiguredElements(element);
    }

    public boolean isInReadOnlyCCZ() {
        return getConfigurationManagementService().isInReadOnlyCCZ(element);
    }

    public boolean isInReleasedCCZ() {
        return getConfigurationManagementService().isInReleasedCCZ(element);
    }

    public String getDisplayName(String originalName) {
        return getConfiguredElementDomain().getDisplayName(this, originalName);
    }

    public boolean isReadyForRelease() {
        Optional<LifecycleStatus> status = getStatus();
        return status.isPresent() && status.get().isReadyForRelease();
    }
}
