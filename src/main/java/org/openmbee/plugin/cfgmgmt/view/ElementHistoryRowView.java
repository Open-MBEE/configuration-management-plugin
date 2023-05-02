package org.openmbee.plugin.cfgmgmt.view;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;

public class ElementHistoryRowView {
    protected String changeRecordNameColumn;
    private String startTimeColumn;
    private String completionTimeColumn;
    private String revisionColumn;
    private final String changeRecordLocalId; // do not display

    public ElementHistoryRowView(String changeRecordName, String startTime, String completionTime, String revision, String changeRecordLocalId) {
        setValues(changeRecordName, startTime, completionTime, revision);
        this.changeRecordLocalId = changeRecordLocalId;
    }

    public void setValues(String changeRecordName, String startTime, String completionTime, String revision) {
        this.changeRecordNameColumn = changeRecordName;
        this.startTimeColumn = startTime;
        this.completionTimeColumn = completionTime;
        this.revisionColumn = revision;
        massageCompletionTimeAndRevisionNulls();
    }

    protected void massageCompletionTimeAndRevisionNulls() {
        if (revisionColumn == null) {
            if (completionTimeColumn == null) {
                revisionColumn = PluginConstant.NO_REVISION_AND_NOT_COMPLETED;
            } else {
                revisionColumn = PluginConstant.NO_REVISION_BUT_COMPLETED;
            }
        }
        if (completionTimeColumn == null) {
            completionTimeColumn = PluginConstant.NO_TIME;
        }
    }

    public void showRevisionStateIsUnclear() {
        if (!revisionColumn.equals(PluginConstant.NO_REVISION_AND_NOT_COMPLETED) && !revisionColumn.equals(PluginConstant.NO_REVISION_BUT_COMPLETED)) {
            revisionColumn = PluginConstant.REVISION_STATE_UNCLEAR + revisionColumn;
        }
    }

    public String getChangeRecordNameColumn() {
        return changeRecordNameColumn != null ? changeRecordNameColumn : PluginConstant.EMPTY_STRING;
    }

    public String getStartTimeColumn() {
        return startTimeColumn;
    }

    public String getCompletionTimeColumn() {
        return completionTimeColumn;
    }

    public String getRevisionColumn() {
        return revisionColumn;
    }

    public String getChangeRecordLocalId() {
        return changeRecordLocalId;
    }
}
