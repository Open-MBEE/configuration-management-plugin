package org.openmbee.plugin.cfgmgmt.utils;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.domain.ChangeRecordDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.RevisionHistoryRecord;
import org.junit.Before;
import org.junit.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestChangeRecordComparatorForHistory {
    private ChangeRecordComparatorForHistory changeRecordComparatorForHistory;
    private RevisionHistoryRecord revisionHistoryRecord1;
    private RevisionHistoryRecord revisionHistoryRecord2;
    private RevisionHistoryRecord revisionHistoryRecord3;
    private ChangeRecordDomain changeRecordDomain;

    @Before
    public void setup() {
        changeRecordDomain = mock(ChangeRecordDomain.class);
        revisionHistoryRecord1 = mock(RevisionHistoryRecord.class);
        revisionHistoryRecord2 = mock(RevisionHistoryRecord.class);
        revisionHistoryRecord3 = mock(RevisionHistoryRecord.class);
        changeRecordComparatorForHistory = spy(new ChangeRecordComparatorForHistory(List.of(revisionHistoryRecord1, revisionHistoryRecord2, revisionHistoryRecord3), changeRecordDomain));

    }

    @Test
    public void compare_leftIsReleasedAndRightIsNot() {
        ChangeRecord left = mock(ChangeRecord.class);
        ChangeRecord right = mock(ChangeRecord.class);

        when(left.isReleased()).thenReturn(true);
        when(right.isReleased()).thenReturn(false);

        assertEquals(-1, changeRecordComparatorForHistory.compare(left, right));
    }

    @Test
    public void compare_leftIsNotReleasedButRightIs() {
        ChangeRecord left = mock(ChangeRecord.class);
        ChangeRecord right = mock(ChangeRecord.class);

        when(left.isReleased()).thenReturn(false);
        when(right.isReleased()).thenReturn(true);

        assertEquals(1, changeRecordComparatorForHistory.compare(left, right));
    }

    @Test
    public void compare_bothNotReleasedAndDifferentLengthNames() {
        ChangeRecord left = mock(ChangeRecord.class);
        ChangeRecord right = mock(ChangeRecord.class);
        String leftName = "left";
        String rightName = "rightName";

        when(left.isReleased()).thenReturn(false);
        when(right.isReleased()).thenReturn(false);
        when(left.getName()).thenReturn(leftName);
        when(right.getName()).thenReturn(rightName);

        assertEquals(leftName.compareTo(rightName), changeRecordComparatorForHistory.compare(left, right));
    }

    @Test
    public void compare_bothNotReleasedAndSameLengthNamesButDifferentContents() {
        ChangeRecord left = mock(ChangeRecord.class);
        ChangeRecord right = mock(ChangeRecord.class);
        String leftName = "aaa";
        String rightName = "aab";

        when(left.isReleased()).thenReturn(false);
        when(right.isReleased()).thenReturn(false);
        when(left.getName()).thenReturn(leftName);
        when(right.getName()).thenReturn(rightName);

        assertEquals(leftName.compareTo(rightName), changeRecordComparatorForHistory.compare(left, right));
    }

    @Test
    public void compare_bothReleasedButLeftHasNoHistory() {
        ChangeRecord left = mock(ChangeRecord.class);
        ChangeRecord right = mock(ChangeRecord.class);

        when(left.isReleased()).thenReturn(true);
        when(right.isReleased()).thenReturn(true);
        doReturn(null).when(changeRecordComparatorForHistory).getAssociatedRevisionHistoryRecord(left);
        doReturn(revisionHistoryRecord2).when(changeRecordComparatorForHistory).getAssociatedRevisionHistoryRecord(right);

        assertEquals(1, changeRecordComparatorForHistory.compare(left, right));
    }

    @Test
    public void compare_bothReleasedButRightHasNoHistory() {
        ChangeRecord left = mock(ChangeRecord.class);
        ChangeRecord right = mock(ChangeRecord.class);

        when(left.isReleased()).thenReturn(true);
        when(right.isReleased()).thenReturn(true);
        doReturn(revisionHistoryRecord1).when(changeRecordComparatorForHistory).getAssociatedRevisionHistoryRecord(left);
        doReturn(null).when(changeRecordComparatorForHistory).getAssociatedRevisionHistoryRecord(right);

        assertEquals(-1, changeRecordComparatorForHistory.compare(left, right));
    }

    @Test
    public void compare_bothReleasedButNoHistory() {
        ChangeRecord left = mock(ChangeRecord.class);
        ChangeRecord right = mock(ChangeRecord.class);
        String leftName = "aaa";
        String rightName = "aab";

        when(left.isReleased()).thenReturn(true);
        when(right.isReleased()).thenReturn(true);
        doReturn(null).when(changeRecordComparatorForHistory).getAssociatedRevisionHistoryRecord(left);
        doReturn(null).when(changeRecordComparatorForHistory).getAssociatedRevisionHistoryRecord(right);
        when(left.getName()).thenReturn(leftName);
        when(right.getName()).thenReturn(rightName);

        assertEquals(leftName.compareTo(rightName), changeRecordComparatorForHistory.compare(left, right));
    }

    @Test
    public void compare_bothReleasedAndHaveHistory() {
        ChangeRecord left = mock(ChangeRecord.class);
        ChangeRecord right = mock(ChangeRecord.class);

        when(left.isReleased()).thenReturn(true);
        when(right.isReleased()).thenReturn(true);
        doReturn(revisionHistoryRecord1).when(changeRecordComparatorForHistory).getAssociatedRevisionHistoryRecord(left);
        doReturn(revisionHistoryRecord2).when(changeRecordComparatorForHistory).getAssociatedRevisionHistoryRecord(right);
        doReturn(-1).when(changeRecordComparatorForHistory).isLeftRevisionHistoryBeforeRightRevisionHistoryGivenCriteria(revisionHistoryRecord1, revisionHistoryRecord2);

        assertEquals(-1, changeRecordComparatorForHistory.compare(left, right));
    }

    @Test
    public void getAssociatedRevisionHistoryRecord_authorityFound() {
        ChangeRecord expected = mock(ChangeRecord.class);
        ChangeRecord mismatch = mock(ChangeRecord.class);

        when(revisionHistoryRecord1.getRevisionReleaseAuthority()).thenReturn(mismatch);
        when(revisionHistoryRecord2.getRevisionReleaseAuthority()).thenReturn(mismatch);
        when(revisionHistoryRecord3.getRevisionReleaseAuthority()).thenReturn(expected);

        assertEquals(revisionHistoryRecord3, changeRecordComparatorForHistory.getAssociatedRevisionHistoryRecord(expected));
    }

    @Test
    public void getAssociatedRevisionHistoryRecord_authorityNotFound() {
        ChangeRecord expected = mock(ChangeRecord.class);
        ChangeRecord mismatch = mock(ChangeRecord.class);
        String name = "name";

        when(revisionHistoryRecord1.getRevisionReleaseAuthority()).thenReturn(mismatch);
        when(revisionHistoryRecord2.getRevisionReleaseAuthority()).thenReturn(mismatch);
        when(revisionHistoryRecord3.getRevisionReleaseAuthority()).thenReturn(mismatch);
        when(expected.getName()).thenReturn(name);

        assertNull(changeRecordComparatorForHistory.getAssociatedRevisionHistoryRecord(expected));
    }

    @Test
    public void getAssociatedRevisionHistoryRecord_authorityNull() {
        when(revisionHistoryRecord1.getRevisionReleaseAuthority()).thenReturn(null);

        assertNull(changeRecordComparatorForHistory.getAssociatedRevisionHistoryRecord(mock(ChangeRecord.class)));
    }

    @Test
    public void isLeftRevisionHistoryBeforeRightRevisionHistoryGivenCriteria_forReleaseDate() {
        String potentialErrorSuffix = ExceptionConstants.REVISION_HISTORY_RELEASE_DATE;
        String releaseDate = "releaseDate";
        String leftName = "leftName";
        String rightName = "rightName";
        String leftFormat = String.format(potentialErrorSuffix, leftName);
        String rightFormat = String.format(potentialErrorSuffix, rightName);

        when(revisionHistoryRecord1.getReleaseDate()).thenReturn(releaseDate);
        when(revisionHistoryRecord1.getName()).thenReturn(leftName);
        when(revisionHistoryRecord2.getReleaseDate()).thenReturn(releaseDate);
        when(revisionHistoryRecord2.getName()).thenReturn(rightName);
        doReturn(-1).when(changeRecordComparatorForHistory).isLeftTimeBeforeRightTime(releaseDate, releaseDate, leftFormat, rightFormat);

        assertEquals(-1, changeRecordComparatorForHistory.isLeftRevisionHistoryBeforeRightRevisionHistoryGivenCriteria(revisionHistoryRecord1, revisionHistoryRecord2));
        verify(changeRecordComparatorForHistory).isLeftTimeBeforeRightTime(releaseDate, releaseDate, leftFormat, rightFormat);
        verify(revisionHistoryRecord1, never()).getCreationDate();
        verify(revisionHistoryRecord2, never()).getCreationDate();
    }

    @Test
    public void isLeftTimeBeforeRightTime_leftIsNull() {
        String leftTime = "leftTime";
        String rightTime = "rightTime";
        String leftErrorMessage = "leftErrorMessage";
        String rightErrorMessage = "rightErrorMessage";
        ZonedDateTime leftParsed = ZonedDateTime.now();
        ZonedDateTime rightParsed = leftParsed.minusSeconds(5L);

        when(changeRecordDomain.tryToParseTimeFromString(leftTime, leftErrorMessage)).thenReturn(null);
        when(changeRecordDomain.tryToParseTimeFromString(rightTime, rightErrorMessage)).thenReturn(rightParsed);

        assertEquals(1, changeRecordComparatorForHistory.isLeftTimeBeforeRightTime(leftTime, rightTime, leftErrorMessage, rightErrorMessage));
    }

    @Test
    public void isLeftTimeBeforeRightTime_rightIsNull() {
        String leftTime = "leftTime";
        String rightTime = "rightTime";
        String leftErrorMessage = "leftErrorMessage";
        String rightErrorMessage = "rightErrorMessage";
        ZonedDateTime leftParsed = ZonedDateTime.now();

        when(changeRecordDomain.tryToParseTimeFromString(leftTime, leftErrorMessage)).thenReturn(leftParsed);
        when(changeRecordDomain.tryToParseTimeFromString(rightTime, rightErrorMessage)).thenReturn(null);

        assertEquals(-1, changeRecordComparatorForHistory.isLeftTimeBeforeRightTime(leftTime, rightTime, leftErrorMessage, rightErrorMessage));
    }

    @Test
    public void isLeftTimeBeforeRightTime_bothNull() {
        String leftTime = "leftTime";
        String rightTime = "rightTime";
        String leftErrorMessage = "leftErrorMessage";
        String rightErrorMessage = "rightErrorMessage";

        when(changeRecordDomain.tryToParseTimeFromString(leftTime, leftErrorMessage)).thenReturn(null);
        when(changeRecordDomain.tryToParseTimeFromString(rightTime, rightErrorMessage)).thenReturn(null);

        assertEquals(0, changeRecordComparatorForHistory.isLeftTimeBeforeRightTime(leftTime, rightTime, leftErrorMessage, rightErrorMessage));
    }

    @Test
    public void isLeftTimeBeforeRightTime_rightIsBefore() {
        String leftTime = "leftTime";
        String rightTime = "rightTime";
        String leftErrorMessage = "leftErrorMessage";
        String rightErrorMessage = "rightErrorMessage";
        ZonedDateTime leftParsed = ZonedDateTime.now();
        ZonedDateTime rightParsed = leftParsed.minusSeconds(5L);

        when(changeRecordDomain.tryToParseTimeFromString(leftTime, leftErrorMessage)).thenReturn(leftParsed);
        when(changeRecordDomain.tryToParseTimeFromString(rightTime, rightErrorMessage)).thenReturn(rightParsed);

        assertEquals(1, changeRecordComparatorForHistory.isLeftTimeBeforeRightTime(leftTime, rightTime, leftErrorMessage, rightErrorMessage));
    }

    @Test
    public void isLeftTimeBeforeRightTime_leftIsBefore() {
        String leftTime = "leftTime";
        String rightTime = "rightTime";
        String leftErrorMessage = "leftErrorMessage";
        String rightErrorMessage = "rightErrorMessage";
        ZonedDateTime leftParsed = ZonedDateTime.now();
        ZonedDateTime rightParsed = leftParsed.plusSeconds(5L);

        when(changeRecordDomain.tryToParseTimeFromString(leftTime, leftErrorMessage)).thenReturn(leftParsed);
        when(changeRecordDomain.tryToParseTimeFromString(rightTime, rightErrorMessage)).thenReturn(rightParsed);

        assertEquals(-1, changeRecordComparatorForHistory.isLeftTimeBeforeRightTime(leftTime, rightTime, leftErrorMessage, rightErrorMessage));
    }

    @Test
    public void isLeftTimeBeforeRightTime_timeEqual() {
        String leftTime = "leftTime";
        String rightTime = "rightTime";
        String leftErrorMessage = "leftErrorMessage";
        String rightErrorMessage = "rightErrorMessage";
        ZonedDateTime leftParsed = ZonedDateTime.now();

        when(changeRecordDomain.tryToParseTimeFromString(leftTime, leftErrorMessage)).thenReturn(leftParsed);
        when(changeRecordDomain.tryToParseTimeFromString(rightTime, rightErrorMessage)).thenReturn(leftParsed);

        assertEquals(0, changeRecordComparatorForHistory.isLeftTimeBeforeRightTime(leftTime, rightTime, leftErrorMessage, rightErrorMessage));
    }
}
