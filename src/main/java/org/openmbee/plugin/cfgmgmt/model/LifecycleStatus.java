package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Region;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.MISSING_MATURITY_RATING;

public class LifecycleStatus {
    private static final Logger logger = LoggerFactory.getLogger(LifecycleStatus.class);

    private final ConfigurationManagementService configurationManagementService;
    private State state;

    public LifecycleStatus(ConfigurationManagementService configurationManagementService, State state) {
        this.configurationManagementService = configurationManagementService;
        this.state = state;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LifecycleStatus that = (LifecycleStatus) o;
        return Objects.equals(getState(), that.getState());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getState());
    }

    public String getName() {
        return getState().getName();
    }

    public State getState() {
        return state;
    }

    public List<LifecycleTransition> getTransitions() {
        if (state != null && state.getOwner() != null) {
            Collection<Transition> transitions = ((Region) state.getOwner()).getTransition();
            if(transitions == null) {
                return new ArrayList<>();
            }
            return transitions.stream()
                    .filter(tr -> tr.getSource() != null && tr.getSource().equals(this.state))
                    .flatMap(tr -> Stream.of(getConfigurationManagementService().getLifecycleTransition(tr)))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public int getMaturityRating() {
        List<Object> list = getApiDomain().getStereotypePropertyValue(getState(), PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.MATURITY_RATING);

        if(list != null && list.size() == 1) {
            try {
                return (int) list.get(0);
            } catch(ClassCastException ex) {
                getUIDomain().logError(PluginConstant.INVALID_TYPE + PluginConstant.MATURITY_RATING);
            }
        }

        return MISSING_MATURITY_RATING;
    }

    public Color getColor() {
        List<String> colorList = getApiDomain().getStereotypePropertyValueAsString(getState(), PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, PluginConstant.ICON_ADORNMENT_COLOR);

        if(colorList == null || colorList.size() != 1) {
            return null;
        }

        try {
            return getColorFromList(colorList);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            getUIDomain().logError(getLogger(), e.getMessage());
            return null;
        }
    }

    protected Color getColorFromList(List<String> colorList) throws IllegalAccessException, NoSuchFieldException, ClassNotFoundException {
        return (Color) Class.forName("java.awt.Color").getDeclaredField(colorList.get(0)).get(null);
    }

    protected boolean getBooleanProperty(String prop) {
        List<Object> statusList = getApiDomain().getStereotypePropertyValue(getState(), PluginConstant.LIFECYCLE_STATUS_STEREOTYPE, prop);
        if(statusList == null || statusList.size() != 1) {
            getUIDomain().logError(PluginConstant.LIFECYCLE_PROPERTY_RETRIEVAL_FAILED);
            return false;
        }

        try {
            return (boolean) statusList.get(0);
        } catch(ClassCastException ex) {
            getUIDomain().logError(PluginConstant.INVALID_TYPE + prop);
        }
        return false;
    }

    public boolean isReadOnly() {
        return getBooleanProperty(PluginConstant.IS_READ_ONLY_STATUS);
    }

    public boolean isReleased() {
        return getBooleanProperty(PluginConstant.IS_RELEASED_STATUS);
    }

    public boolean isReadyForRelease() {
        return getBooleanProperty(PluginConstant.IS_READY_FOR_RELEASE);
    }

    public boolean isExpandable() {
        return getBooleanProperty(PluginConstant.IS_EXPANDABLE_STATUS);
    }
}
