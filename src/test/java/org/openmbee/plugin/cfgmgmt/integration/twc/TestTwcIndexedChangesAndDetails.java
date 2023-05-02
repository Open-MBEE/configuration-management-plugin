package org.openmbee.plugin.cfgmgmt.integration.twc;

import org.openmbee.plugin.cfgmgmt.integration.twc.json.element.TwcElementJson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class TestTwcIndexedChangesAndDetails {
    private TwcIndexedChangesAndDetails indexedChangesAndDetails;
    private Map<String, TwcElementJson> changedInitial;
    private Map<String, TwcElementJson> added;
    private Map<String, TwcElementJson> removed;
    private Map<String, TwcElementJson> changedFinal;
    private Map<String, TwcElementJson> details;
    private String id;
    private TwcElementJson value;

    @Before
    public void setup() {
        indexedChangesAndDetails = spy(new TwcIndexedChangesAndDetails());
        changedInitial = new HashMap<>();
        added = new HashMap<>();
        removed = new HashMap<>();
        changedFinal = new HashMap<>();
        details = new HashMap<>();
        id = "id";
        value = mock(TwcElementJson.class);

        indexedChangesAndDetails.setChangedInitial(changedInitial);
        indexedChangesAndDetails.setAdded(added);
        indexedChangesAndDetails.setRemoved(removed);
        indexedChangesAndDetails.setChangedFinal(changedFinal);
        indexedChangesAndDetails.setDetails(details);
    }

    @Test
    public void isInAGivenMap_notInAnything() {
        assertFalse(indexedChangesAndDetails.isInAGivenMap(id));
    }

    @Test
    public void isInAGivenMap_inChangedInitial() {
        changedInitial.put(id, value);

        assertTrue(indexedChangesAndDetails.isInAGivenMap(id));
    }

    @Test
    public void isInAGivenMap_inAdded() {
        added.put(id, value);

        assertTrue(indexedChangesAndDetails.isInAGivenMap(id));
    }

    @Test
    public void isInAGivenMap_inRemoved() {
        removed.put(id, value);

        assertTrue(indexedChangesAndDetails.isInAGivenMap(id));
    }

    @Test
    public void isInAGivenMap_inChangedFinal() {
        changedFinal.put(id, value);

        assertTrue(indexedChangesAndDetails.isInAGivenMap(id));
    }

    @Test
    public void isInAGivenMap_inDetails() {
        details.put(id, value);

        assertTrue(indexedChangesAndDetails.isInAGivenMap(id));
    }

    @Test
    public void getElementFromAnyMapUsingId_notInAnything() {
        assertNull(indexedChangesAndDetails.getElementFromAnyMapUsingId(id));
    }

    @Test
    public void getElementFromAnyMapUsingId_inDetails() {
        details.put(id, value);

        Assert.assertEquals(value, indexedChangesAndDetails.getElementFromAnyMapUsingId(id));
    }

    @Test
    public void getElementFromAnyMapUsingId_inChangedInitial() {
        changedInitial.put(id, value);

        Assert.assertEquals(value, indexedChangesAndDetails.getElementFromAnyMapUsingId(id));
    }

    @Test
    public void getElementFromAnyMapUsingId_inAdded() {
        added.put(id, value);

        Assert.assertEquals(value, indexedChangesAndDetails.getElementFromAnyMapUsingId(id));
    }

    @Test
    public void getElementFromAnyMapUsingId_inRemoved() {
        removed.put(id, value);

        Assert.assertEquals(value, indexedChangesAndDetails.getElementFromAnyMapUsingId(id));
    }

    @Test
    public void getElementFromAnyMapUsingId_inChangedFinal() {
        changedFinal.put(id, value);

        Assert.assertEquals(value, indexedChangesAndDetails.getElementFromAnyMapUsingId(id));
    }

    @Test
    public void isEmpty_allMapsEmpty() {
        assertTrue(indexedChangesAndDetails.isEmpty());
    }

    @Test
    public void isEmpty_changedInitialNotEmpty() {
        changedInitial.put(id, value);

        assertFalse(indexedChangesAndDetails.isEmpty());
    }

    @Test
    public void isEmpty_addedNotEmpty() {
        added.put(id, value);

        assertFalse(indexedChangesAndDetails.isEmpty());
    }

    @Test
    public void isEmpty_removedNotEmpty() {
        removed.put(id, value);

        assertFalse(indexedChangesAndDetails.isEmpty());
    }

    @Test
    public void isEmpty_changedFinalNotEmpty() {
        changedFinal.put(id, value);

        assertFalse(indexedChangesAndDetails.isEmpty());
    }

    @Test
    public void isEmpty_detailsNotEmpty() {
        details.put(id, value);

        assertFalse(indexedChangesAndDetails.isEmpty());
    }
}
