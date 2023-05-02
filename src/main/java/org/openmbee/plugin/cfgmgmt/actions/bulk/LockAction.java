package org.openmbee.plugin.cfgmgmt.actions.bulk;

import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class LockAction implements Action {
    private final ApiDomain apiDomain;
    private List<ConfiguredElement> configuredElements;
    private boolean isEnabled;

    public LockAction(ApiDomain apiDomain) {
        this.apiDomain = apiDomain;
        this.isEnabled = false;
    }

    protected ApiDomain getApiDomain() {
        return apiDomain;
    }

    @Override
    public Object getValue(String key) {
        return null;
    }

    @Override
    public void putValue(String key, Object value) {
    }

    public void setEnabled(List<ConfiguredElement> configuredElements) {
        this.configuredElements = configuredElements;
        if (configuredElements == null || configuredElements.isEmpty()) {
            setEnabled(false);
            return;
        }

        if (getApiDomain().isAnyLocked(configuredElements)) {
            setEnabled(false);
            return;
        }
        setEnabled(true);
    }

    @Override
    public void setEnabled(boolean b) {
        isEnabled = b;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getApiDomain().lock(configuredElements);
    }
}
