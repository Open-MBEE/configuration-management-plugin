package org.openmbee.plugin.cfgmgmt.integration.twc.json;

public class TwcRevisionDifferenceJson {
    private String[] added;
    private String[] changed;
    private boolean empty;
    private String[] removed;

    public String[] getAdded() {
        return added;
    }

    public void setAdded(String[] added) {
        this.added = added;
    }

    public String[] getChanged() {
        return changed;
    }

    public void setChanged(String[] changed) {
        this.changed = changed;
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    public String[] getRemoved() {
        return removed;
    }

    public void setRemoved(String[] removed) {
        this.removed = removed;
    }
}
