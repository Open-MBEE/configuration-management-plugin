package org.openmbee.plugin.cfgmgmt.integration.twc;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.TwcRevisionDifferenceJson;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.element.TwcElementJson;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TwcAddedChangedRemovedResults {
    protected List<TwcElementJson> changedInitial;
    protected List<TwcElementJson> addedRemovedOrChangedFinal;
    protected String addedString = PluginConstant.EMPTY_STRING;
    protected String changedString = PluginConstant.EMPTY_STRING;
    protected String removedString = PluginConstant.EMPTY_STRING;
    protected String addedRemovedOrChangedFinalString = PluginConstant.EMPTY_STRING;

    public TwcAddedChangedRemovedResults(TwcRevisionDifferenceJson revisionDifferenceJson) {
        if(revisionDifferenceJson != null && !revisionDifferenceJson.isEmpty()) {
            // determine string delimited ids
            addedString = joinSingleStringArray(revisionDifferenceJson.getAdded());
            changedString = joinSingleStringArray(revisionDifferenceJson.getChanged());
            removedString = joinSingleStringArray(revisionDifferenceJson.getRemoved());
            addedRemovedOrChangedFinalString = joinAllStringArrays(revisionDifferenceJson.getAdded(),
                    revisionDifferenceJson.getChanged(), revisionDifferenceJson.getRemoved());
        }
    }

    protected String joinSingleStringArray(String[] array) {
        return Stream.of(array).filter(Objects::nonNull).filter(v -> !v.isBlank()).collect(Collectors.joining(PluginConstant.COMMA));
    }

    protected String joinAllStringArrays(String[] added, String[] changed, String[] removed) {
        return Stream.of(added, changed, removed).filter(Objects::nonNull).flatMap(Stream::of)
                .filter(v -> !v.isBlank()).collect(Collectors.joining(PluginConstant.COMMA));
    }

    public String getChangedString() {
        return changedString;
    }

    public String getAddedString() {
        return addedString;
    }

    public String getRemovedString() {
        return removedString;
    }

    public String getAddedRemovedOrChangedFinalString() {
        return addedRemovedOrChangedFinalString;
    }

    public List<TwcElementJson> getChangedInitial() {
        return changedInitial;
    }

    public void setChangedInitial(List<TwcElementJson> changedInitial) {
        this.changedInitial = changedInitial;
    }

    public List<TwcElementJson> getAddedRemovedOrChangedFinal() {
        return addedRemovedOrChangedFinal;
    }

    public void setAddedRemovedOrChangedFinal(List<TwcElementJson> addedRemovedOrChangedFinal) {
        this.addedRemovedOrChangedFinal = addedRemovedOrChangedFinal;
    }
}
