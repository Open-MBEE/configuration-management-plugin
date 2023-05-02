package org.openmbee.plugin.cfgmgmt.listeners;

import javax.swing.event.ChangeListener;

public abstract class LifecycleObjectPropertyChangeListener implements ChangeListener {
    public enum Property {STATUS, REVISION};

    public Property getProp() {
        return prop;
    }

    protected Property prop;

    public LifecycleObjectPropertyChangeListener(Property prop) {
        this.prop = prop;
    }

}
