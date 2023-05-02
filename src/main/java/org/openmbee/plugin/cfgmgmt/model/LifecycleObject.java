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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.util.*;
import java.util.List;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.MISSING_MATURITY_RATING;

public abstract class LifecycleObject {
    private final Logger logger = LoggerFactory.getLogger(LifecycleObject.class);

    private final ConfigurationManagementService configurationManagementService;
    protected Element element;
    protected List<LifecycleObjectPropertyChangeListener> propertyChangeListeners;
    protected Stereotype baseStereotype;

    public LifecycleObject(ConfigurationManagementService configurationManagementService, Element element, @Nullable Stereotype baseStereotype) {
        this.configurationManagementService = configurationManagementService;
        this.element = element;
        this.baseStereotype = baseStereotype;
        propertyChangeListeners = new ArrayList<>();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LifecycleObject lifecycleObject = (LifecycleObject) object;
        return Objects.equals(element.getLocalID(), lifecycleObject.element.getLocalID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    protected Logger getLogger() {
        return logger;
    }

    public abstract boolean canBePromoted(LifecycleTransition tr, ChangeRecord cr);

    public Element getElement() {
        return element;
    }

    public Stereotype getBaseStereotype() {
        return baseStereotype;
    }

    public ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    public ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    public UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    public LifecycleObjectFactory getLifecycleObjectFactory() {
        return getConfigurationManagementService().getLifecycleObjectFactory();
    }

    public void addPropertyChangeListener(LifecycleObjectPropertyChangeListener lifecycleObjectPropertyChangeListener) {
        propertyChangeListeners.add(lifecycleObjectPropertyChangeListener);
    }

    protected List<LifecycleObjectPropertyChangeListener> getPropertyChangeListeners() {
        return propertyChangeListeners; // used for unit testing
    }

    //Returns if element is read only.  Default return for elements without status is true
    public boolean isReadOnly() {
        Optional<LifecycleStatus> status = getStatus();
        return status.isEmpty() || status.get().isReadOnly();
    }

    //Returns if element is released.  Default return for elements without status is true
    public boolean isReleased() {
        Optional<LifecycleStatus> status = getStatus();
        return status.isEmpty() || status.get().isReleased();
    }

    public String getQualifiedName() {
        NamedElement namedElement = getApiDomain().getNamedElement(element);
        if (namedElement != null) {
            return namedElement.getQualifiedName();
        } else {
            return PluginConstant.EMPTY_STRING;
        }
    }

    public String getName() {
        NamedElement namedElement = getApiDomain().getNamedElement(element);
        if (namedElement != null) {
            return namedElement.getName();
        } else {
            return PluginConstant.EMPTY_STRING;
        }
    }

    public void setDescription(String description) {
        getApiDomain().setStereotypePropertyValue(element, baseStereotype, PluginConstant.DESCRIPTION, description);
    }

    public String getDescription() {
        //TODO better type management
        List<String> value = (List) getApiDomain().getStereotypePropertyValue(element, baseStereotype, PluginConstant.DESCRIPTION);
        if (value == null || value.isEmpty()) {
            return PluginConstant.EMPTY_STRING;
        }

        return value.get(0);
    }

    public void setComments(String comments) {
        getApiDomain().setStereotypePropertyValue(element, baseStereotype, PluginConstant.COMMENTS, comments);
    }

    public String getComments() {
        //TODO better type management
        List<String> value =(List) getApiDomain().getStereotypePropertyValue(element, baseStereotype, PluginConstant.COMMENTS);
        if (value == null || value.isEmpty()) {
            return PluginConstant.EMPTY_STRING;
        }

        return value.get(0);
    }

    public int getStatusMaturityRating() {
        Optional<LifecycleStatus> status = getStatus();
        return status.map(LifecycleStatus::getMaturityRating).orElse(MISSING_MATURITY_RATING);
    }

    public Color getStatusColor() {
        Optional<LifecycleStatus> status = getStatus();
        return status.map(LifecycleStatus::getColor).orElse(null);
    }

    public LifecycleStatus getInitialStatus() {
        Optional<Lifecycle> lifecycle = getLifecycle();
        return lifecycle.map(Lifecycle::getInitialStatus).orElse(null);
    }

    public Optional<LifecycleStatus> getStatus() {
        if(getConfigurationManagementService().isLifecycleStatusChanging()) {
            return Optional.empty(); // handles edge case where status can be empty while it is changing
        }

        List<Object> statusList = getApiDomain().getStereotypePropertyValue(element, baseStereotype, PluginConstant.STATUS);
        if (statusList == null || statusList.isEmpty()) {
            String errorMessage = String.format(ExceptionConstants.ELEMENT_LACKS_STATUS, getQualifiedName());
            getUIDomain().logError(errorMessage);
            return Optional.empty();
        }

        Object status = statusList.get(statusList.size() - 1);

        if (!(status instanceof State)) {
            if (status instanceof String) {
                // previous versions of this plugin relied on String tag values (they are now objects), this code is
                // a compatibility bridge between that old version and the current implementation
                LifecycleStatus lifecycleStatus = getLifecycle().map(v -> v.getStatusByName((String) status)).orElse(null);
                if (lifecycleStatus != null) {
                    getApiDomain().setStereotypePropertyValue(element, baseStereotype, PluginConstant.STATUS, lifecycleStatus.getState());
                    return Optional.of(lifecycleStatus);
                } else {
                    getUIDomain().logError(String.format(ExceptionConstants.ERROR_DURING_SELF_CLEANING_STATUS_CANNOT_BE_FOUND_IN_LIFECYCLE, getQualifiedName(), status));
                    return Optional.empty();
                }
            } else {
                String errorMessage = String.format(ExceptionConstants.ELEMENT_LACKS_STATUS, getQualifiedName());
                getUIDomain().logError(getLogger(), errorMessage);
                return Optional.empty();
            }
        }

        return Optional.of(getConfigurationManagementService().getLifecycleStatus((State) status));
    }

    public boolean hasStatus() {
        return getStatus().isPresent();
    }

    public String getStatusName() {
        Optional<LifecycleStatus> status = getStatus();
        return status.map(LifecycleStatus::getName).orElse(null);
    }

    public List<LifecycleTransition> getTransitions() {
        Optional<LifecycleStatus> status = getStatus();
        return status.map(LifecycleStatus::getTransitions).orElse(List.of());
    }

    public boolean hasAvailableTransitions() {
        List<LifecycleTransition> lifecycleTransitionList = getTransitions();
        return (lifecycleTransitionList != null && !lifecycleTransitionList.isEmpty());
    }

    public Stereotype getAppliedStereotype() {
        Collection<Stereotype> listAppliedStereos = getApiDomain().getDerivedStereotypes(element, baseStereotype, true);
        if (listAppliedStereos == null || listAppliedStereos.size() != 1) {
            getUIDomain().logError(String.format(ExceptionConstants.UNABLE_TO_IDENTIFY_STEREOTYPE, getQualifiedName()));
            return null;
        }

        return listAppliedStereos.iterator().next();
    }

    public Optional<Lifecycle> getLifecycle() {
        Stereotype stereotype = getAppliedStereotype();
        if(stereotype == null) {
            return Optional.empty();
        }
        List<Object> objectList = getApiDomain().getStereotypePropertyValue(element, stereotype, PluginConstant.LIFECYCLE);
        if(objectList == null) {
            getUIDomain().logDebug(ExceptionConstants.NO_STATE_MACHINES);
            return Optional.empty();
        } else if(objectList.size() == 1 && objectList.get(0) instanceof StateMachine) {
            return Optional.of(getConfigurationManagementService().getLifecycle((StateMachine) objectList.get(0)));
        } else if (objectList.size() != 1) {
            getUIDomain().logError(ExceptionConstants.INVALID_STATE_MACHINE_AMOUNT + objectList.size());
            return Optional.empty();
        } else {
            getUIDomain().logError(ExceptionConstants.LIFECYCLE_PROPERTY_NOT_ON_STATE_MACHINE);
            return Optional.empty();
        }
    }

    public boolean changeStatus(LifecycleStatus newStatus, ChangeRecord cr) {
        getApiDomain().setStereotypePropertyValue(getElement(), getBaseStereotype(), PluginConstant.STATUS, newStatus.getState());

        getPropertyChangeListeners().stream()
            .filter(l -> l.getProp().equals(LifecycleObjectPropertyChangeListener.Property.STATUS))
            .forEach(l -> l.stateChanged(makeEvent()));

        return true;
    }

    protected ChangeEvent makeEvent() {
        return new ChangeEvent(this); // used for unit testing
    }
}
