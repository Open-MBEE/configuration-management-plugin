package org.openmbee.plugin.cfgmgmt.integration.twc;

import org.openmbee.plugin.cfgmgmt.integration.twc.json.element.TwcElementJson;

import java.util.HashMap;
import java.util.Map;

public class TwcIndexedChangesAndDetails {
    private Map<String, TwcElementJson> changedInitial;
    private Map<String, TwcElementJson> added;
    private Map<String, TwcElementJson> removed;
    private Map<String, TwcElementJson> changedFinal;
    private Map<String, TwcElementJson> details;

    public TwcIndexedChangesAndDetails() {
        changedInitial = new HashMap<>();
        added = new HashMap<>();
        removed = new HashMap<>();
        changedFinal = new HashMap<>();
        details = new HashMap<>();
    }

    public Map<String, TwcElementJson> getChangedInitial() {
        return changedInitial;
    }

    public void setChangedInitial(Map<String, TwcElementJson> changedInitial) {
        this.changedInitial = changedInitial;
    }

    public Map<String, TwcElementJson> getAdded() {
        return added;
    }

    public void setAdded(Map<String, TwcElementJson> added) {
        this.added = added;
    }

    public Map<String, TwcElementJson> getRemoved() {
        return removed;
    }

    public void setRemoved(Map<String, TwcElementJson> removed) {
        this.removed = removed;
    }

    public Map<String, TwcElementJson> getChangedFinal() {
        return changedFinal;
    }

    public void setChangedFinal(Map<String, TwcElementJson> changedFinal) {
        this.changedFinal = changedFinal;
    }

    public Map<String, TwcElementJson> getDetails() {
        return details;
    }

    public void setDetails(Map<String, TwcElementJson> details) {
        this.details = details;
    }

    public boolean isInAGivenMap(String id) {
        return changedInitial.containsKey(id) || added.containsKey(id) || removed.containsKey(id) ||
                changedFinal.containsKey(id) || details.containsKey(id);
    }

    public TwcElementJson getElementFromAnyMapUsingId(String id) {
        if(details.containsKey(id)) {
            return details.get(id);
        } else if(changedInitial.containsKey(id)) {
            return changedInitial.get(id);
        } else if(added.containsKey(id)) {
            return added.get(id);
        } else if(removed.containsKey(id)) {
            return removed.get(id);
        } else if(changedFinal.containsKey(id)) {
            return changedFinal.get(id);
        }
        return null;
    }

    public boolean isEmpty() {
        return changedInitial.isEmpty() && added.isEmpty() && removed.isEmpty() && changedFinal.isEmpty() && details.isEmpty();
    }
}
