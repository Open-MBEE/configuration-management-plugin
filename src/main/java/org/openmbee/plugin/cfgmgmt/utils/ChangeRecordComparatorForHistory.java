package org.openmbee.plugin.cfgmgmt.utils;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.domain.ChangeRecordDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.RevisionHistoryRecord;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;

public class ChangeRecordComparatorForHistory implements Comparator<ChangeRecord> {
    private final List<RevisionHistoryRecord> revisionHistoryRecords;
    private final ChangeRecordDomain changeRecordDomain;
    private boolean possibleIncompleteData = false;

    public ChangeRecordComparatorForHistory(List<RevisionHistoryRecord> revisionHistoryRecords, ChangeRecordDomain changeRecordDomain) {
        this.revisionHistoryRecords = revisionHistoryRecords;
        this.changeRecordDomain = changeRecordDomain;
    }

    protected ChangeRecordDomain getChangeRecordDomain() {
        return changeRecordDomain;
    }

    public boolean potentiallyHasIncompleteData() {
        return possibleIncompleteData;
    }

    @Override
    public int compare(ChangeRecord left, ChangeRecord right) {
        if (left.isReleased() && !right.isReleased()) {
            return -1;
        } else if (!left.isReleased() && right.isReleased()) {
            return 1;
        } else if (!left.isReleased() && !right.isReleased()) {
            return left.getName().compareTo(right.getName()); // sort by change record name for stability of results
        }
        // if both isReleased flags are true, try to compare appropriate timestamps instead
        RevisionHistoryRecord leftHistory = getAssociatedRevisionHistoryRecord(left);
        RevisionHistoryRecord rightHistory = getAssociatedRevisionHistoryRecord(right);
        if (leftHistory == null && rightHistory == null) {
            return left.getName().compareTo(right.getName()); // sort by change record name for stability of results
        } else if (leftHistory == null) {
            return 1;
        } else if (rightHistory == null) {
            return -1;
        }

        return isLeftRevisionHistoryBeforeRightRevisionHistoryGivenCriteria(leftHistory, rightHistory);
    }

    protected RevisionHistoryRecord getAssociatedRevisionHistoryRecord(ChangeRecord changeRecord) {
        for (RevisionHistoryRecord revisionHistoryRecord : revisionHistoryRecords) {
            if (revisionHistoryRecord.getRevisionReleaseAuthority() != null && revisionHistoryRecord.getRevisionReleaseAuthority().equals(changeRecord)) {
                return revisionHistoryRecord;
            }
        }
        possibleIncompleteData = true;
        return null;
    }

    protected int isLeftRevisionHistoryBeforeRightRevisionHistoryGivenCriteria(RevisionHistoryRecord left, RevisionHistoryRecord right) {
        String potentialErrorSuffix = ExceptionConstants.REVISION_HISTORY_RELEASE_DATE;
        return isLeftTimeBeforeRightTime(left.getReleaseDate(), right.getReleaseDate(),
            String.format(potentialErrorSuffix, left.getName()), String.format(potentialErrorSuffix, right.getName()));
    }

    protected int isLeftTimeBeforeRightTime(String leftTime, String rightTime, String leftErrorMessage, String rightErrorMessage) {
        ZonedDateTime leftParsed = getChangeRecordDomain().tryToParseTimeFromString(leftTime, leftErrorMessage);
        ZonedDateTime rightParsed = getChangeRecordDomain().tryToParseTimeFromString(rightTime, rightErrorMessage);
        if (leftParsed != null && rightParsed != null) {
            if (leftParsed.isBefore(rightParsed)) {
                return -1;
            } else if (leftParsed.isAfter(rightParsed)) {
                return 1;
            }
            return 0;
        } else if (leftParsed != null) {
            return -1;
        } else if (rightParsed != null) {
            return 1;
        }
        return 0;
    }
}
