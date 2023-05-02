package org.openmbee.plugin.cfgmgmt.utils;

import org.openmbee.plugin.cfgmgmt.model.RevisionHistoryRecord;

import java.util.Comparator;

public class RevisionRecordComparatorForRevision implements Comparator<RevisionHistoryRecord> {
    @Override
    public int compare(RevisionHistoryRecord r1, RevisionHistoryRecord r2) {
        // handle nulls first
        if(r1.getRevision() == null && r2.getRevision() == null) {
            return 0;
        } else if(r1.getRevision() == null) {
            return 1;
        } else if(r2.getRevision() == null) {
            return -1;
        }

        // sort by length first then by lexicographic order
        return r1.getRevision().compareTo(r2.getRevision());
    }
}
