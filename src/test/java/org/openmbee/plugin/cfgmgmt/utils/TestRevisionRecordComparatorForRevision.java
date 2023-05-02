package org.openmbee.plugin.cfgmgmt.utils;

import org.openmbee.plugin.cfgmgmt.model.RevisionHistoryRecord;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestRevisionRecordComparatorForRevision.TestRevisionRecordComparatorForRevisionSingle.class,
        TestRevisionRecordComparatorForRevision.TestRevisionRecordComparatorForRevisionParameterized.class})
public class TestRevisionRecordComparatorForRevision {

    public static class TestRevisionRecordComparatorForRevisionBase {
        public RevisionRecordComparatorForRevision revisionRecordComparatorForRevision;
        public RevisionHistoryRecord revisionHistoryRecord1;
        public RevisionHistoryRecord revisionHistoryRecord2;

        @Before
        public void setup() {
            revisionRecordComparatorForRevision = spy(new RevisionRecordComparatorForRevision());
            revisionHistoryRecord1 = mock(RevisionHistoryRecord.class);
            revisionHistoryRecord2 = mock(RevisionHistoryRecord.class);
        }
    }

    public static class TestRevisionRecordComparatorForRevisionSingle extends TestRevisionRecordComparatorForRevisionBase {
        @Test
        public void compare_bothRevisionsNull() {
            assertEquals(0, revisionRecordComparatorForRevision.compare(revisionHistoryRecord1, revisionHistoryRecord2));
        }

        @Test
        public void compare_revision1IsNull() {
            when(revisionHistoryRecord2.getRevision()).thenReturn("revision2");

            assertEquals(1, revisionRecordComparatorForRevision.compare(revisionHistoryRecord1, revisionHistoryRecord2));
        }

        @Test
        public void compare_revision2IsNull() {
            when(revisionHistoryRecord1.getRevision()).thenReturn("revision1");

            assertEquals(-1, revisionRecordComparatorForRevision.compare(revisionHistoryRecord1, revisionHistoryRecord2));
        }
    }

    @RunWith(Parameterized.class)
    public static class TestRevisionRecordComparatorForRevisionParameterized extends TestRevisionRecordComparatorForRevisionBase {
        @Parameterized.Parameter(value = 0)
        public String revision1;
        @Parameterized.Parameter(value = 1)
        public String revision2;

        @Parameterized.Parameters(name = "{index}: revision1: {0}, revision2: {1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {"revision1", "rev2"},
                    {"rev1", "revision2"},
                    {"aaa", "aab"},
                    {"aaa", "a"}
            });
        }

        @Test
        public void compare() {
            when(revisionHistoryRecord1.getRevision()).thenReturn(revision1);
            when(revisionHistoryRecord2.getRevision()).thenReturn(revision2);

            assertEquals(revision1.compareTo(revision2),
                    revisionRecordComparatorForRevision.compare(revisionHistoryRecord1, revisionHistoryRecord2));
        }
    }
}
