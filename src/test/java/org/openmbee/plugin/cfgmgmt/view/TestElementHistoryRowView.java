package org.openmbee.plugin.cfgmgmt.view;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestElementHistoryRowView {
    private ElementHistoryRowView elementHistoryRowView;
    private String changeRecordName;
    private String startTime;
    private String completionTime;
    private String revision;
    private String localId;

    @Before
    public void setup() {
        changeRecordName = "changeRecordName";
        startTime = "startTime";
        completionTime = "completionTime";
        revision = "revision";
        localId = "localId";

        elementHistoryRowView = spy(new ElementHistoryRowView(changeRecordName, startTime, completionTime, revision, localId));
    }

    @Test
    public void setValues() {
        String crn = "crn";
        String st = "st";
        String ct = "ct";
        String r = "r";

        doNothing().when(elementHistoryRowView).massageCompletionTimeAndRevisionNulls();

        assertEquals(changeRecordName, elementHistoryRowView.getChangeRecordNameColumn());
        assertEquals(startTime, elementHistoryRowView.getStartTimeColumn());
        assertEquals(completionTime, elementHistoryRowView.getCompletionTimeColumn());
        assertEquals(revision, elementHistoryRowView.getRevisionColumn());

        elementHistoryRowView.setValues(crn, st, ct, r);

        assertEquals(crn, elementHistoryRowView.getChangeRecordNameColumn());
        assertEquals(st, elementHistoryRowView.getStartTimeColumn());
        assertEquals(ct, elementHistoryRowView.getCompletionTimeColumn());
        assertEquals(r, elementHistoryRowView.getRevisionColumn());

        verify(elementHistoryRowView).massageCompletionTimeAndRevisionNulls();
    }

    @Test
    public void massageCompletionTImeAndRevisionNulls_nullCompletionTime() {
        elementHistoryRowView = spy(new ElementHistoryRowView(changeRecordName, startTime, null, revision, localId));

        assertEquals(PluginConstant.NO_TIME, elementHistoryRowView.getCompletionTimeColumn());
    }

    @Test
    public void massageCompletionTimeAndRevisionNulls_nullRevision() {
        elementHistoryRowView = spy(new ElementHistoryRowView(changeRecordName, startTime, completionTime, null, localId));

        assertEquals(PluginConstant.NO_REVISION_BUT_COMPLETED, elementHistoryRowView.getRevisionColumn());
    }

    @Test
    public void massageCompletionTimeAndRevisionNulls_nullRevisionAndCompletionTime() {
        elementHistoryRowView = spy(new ElementHistoryRowView(changeRecordName, startTime, null, null, localId));

        assertEquals(PluginConstant.NO_TIME, elementHistoryRowView.getCompletionTimeColumn());
        assertEquals(PluginConstant.NO_REVISION_AND_NOT_COMPLETED, elementHistoryRowView.getRevisionColumn());
    }

    @Test
    public void showRevisionStateIsUnclear_revisionIsStar() {
        elementHistoryRowView = spy(new ElementHistoryRowView(changeRecordName, startTime, completionTime, PluginConstant.NO_REVISION_AND_NOT_COMPLETED, localId));
        elementHistoryRowView.showRevisionStateIsUnclear();

        assertEquals(PluginConstant.NO_REVISION_AND_NOT_COMPLETED, elementHistoryRowView.getRevisionColumn());
    }

    @Test
    public void showRevisionStateIsUnclear_revisionIsDash() {
        elementHistoryRowView = spy(new ElementHistoryRowView(changeRecordName, startTime, completionTime, PluginConstant.NO_REVISION_BUT_COMPLETED, localId));
        elementHistoryRowView.showRevisionStateIsUnclear();

        assertEquals(PluginConstant.NO_REVISION_BUT_COMPLETED, elementHistoryRowView.getRevisionColumn());
    }

    @Test
    public void showRevisionStateIsUnclear_revisionIsNormal() {
        String expected = PluginConstant.REVISION_STATE_UNCLEAR + revision;

        elementHistoryRowView = spy(new ElementHistoryRowView(changeRecordName, startTime, completionTime, revision, localId));

        assertEquals(revision, elementHistoryRowView.getRevisionColumn());

        elementHistoryRowView.showRevisionStateIsUnclear();

        assertEquals(expected, elementHistoryRowView.getRevisionColumn());
    }

    @Test
    public void getChangeRecordNameColumn_nullString() {
        elementHistoryRowView.changeRecordNameColumn = null;

        assertEquals(PluginConstant.EMPTY_STRING, elementHistoryRowView.getChangeRecordNameColumn());
    }

    @Test
    public void getChangeRecordNameColumn_hasString() {
        String changeRecordName = "changeRecordName";
        elementHistoryRowView.changeRecordNameColumn = changeRecordName;
        
        assertEquals(changeRecordName, elementHistoryRowView.getChangeRecordNameColumn());
    }
}
