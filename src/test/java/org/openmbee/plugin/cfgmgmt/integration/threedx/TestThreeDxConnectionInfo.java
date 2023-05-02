package org.openmbee.plugin.cfgmgmt.integration.threedx;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

public class TestThreeDxConnectionInfo {
    private ThreeDxConnectionInfo threeDxConnectionInfo;
    private String pass3dsURL;
    private String space3dsURL;
    private String search3dsURL;
    private String changeActionQuery;

    @Before
    public void setup() {
        pass3dsURL = "pass3dsURL";
        space3dsURL = "space3dsURL";
        search3dsURL = "search3dsURL";
        changeActionQuery = "changeActionQuery";
        threeDxConnectionInfo = spy(new ThreeDxConnectionInfo(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery));
    }

    @Test
    public void getLoginURL() {
        assertNotNull(threeDxConnectionInfo.getUrl());
    }

    @Test
    public void getPass3dsURL() {
        assertNotNull(threeDxConnectionInfo.getPass3dsURL());
    }

    @Test
    public void getSpace3dsURL() {
        assertNotNull(threeDxConnectionInfo.getSpace3dsURL());
    }

    @Test
    public void getSearch3dsURL() {
        assertNotNull(threeDxConnectionInfo.getSearch3dsURL());
    }

    @Test
    public void getChangeActionQuery() {
        assertNotNull(threeDxConnectionInfo.getChangeActionQuery());
    }

    @Test
    public void equals_TestSame() {
        ThreeDxConnectionInfo threeDxConnectionInfo1 = new ThreeDxConnectionInfo(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        assertTrue(threeDxConnectionInfo1.equals(threeDxConnectionInfo1));
    }

    @Test
    public void equals_TestWithNull() {
        ThreeDxConnectionInfo threeDxConnectionInfo1 = new ThreeDxConnectionInfo(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        assertFalse(threeDxConnectionInfo1.equals(null));
    }

    @Test
    public void equals_Test_False1() {
        ThreeDxConnectionInfo threeDxConnectionInfo1 = new ThreeDxConnectionInfo(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        List<Object> listObject = new ArrayList<>();

        assertFalse(threeDxConnectionInfo1.equals(listObject));
    }


    private static class ThreeDxConnectionInfoDummy extends ThreeDxConnectionInfo {
        public ThreeDxConnectionInfoDummy(String pass3dsURL, String space3dsURL, String search3dsURL, String changeActionQuery) {
            super(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        }
    }

    @Test
    public void equalsTest_False2() {
        String pass3dsURLDummy = "pass3dsURLDummy";
        ThreeDxConnectionInfo thisN = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        ThreeDxConnectionInfo other = new ThreeDxConnectionInfoDummy(pass3dsURLDummy, space3dsURL, search3dsURL, changeActionQuery);
        assertFalse(thisN.equals(other));
    }

    @Test
    public void equalsTest_False3() {
        String space3dsURLDummy = "space3dsURLDummy";
        ThreeDxConnectionInfo thisN = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        ThreeDxConnectionInfo other = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURLDummy, search3dsURL, changeActionQuery);
        assertFalse(thisN.equals(other));
    }

    @Test
    public void equalsTest_False4() {
        String search3dsURLDummy = "search3dsURLDummy";
        ThreeDxConnectionInfo thisN = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        ThreeDxConnectionInfo other = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURL, search3dsURLDummy, changeActionQuery);
        assertFalse(thisN.equals(other));
    }

    @Test
    public void equalsTest_False5() {
        String changeActionQueryDummy = "changeActionQueryDummy";
        ThreeDxConnectionInfo thisN = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        ThreeDxConnectionInfo other = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURL, search3dsURL, changeActionQueryDummy);
        assertFalse(thisN.equals(other));
    }

    @Test
    public void equalsTestTrue() {
        ThreeDxConnectionInfo thisN = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        ThreeDxConnectionInfo other = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        assertTrue(thisN.equals(other));
    }

    @Test
    public void hashCodeTest() {
        ThreeDxConnectionInfo thisN = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        ThreeDxConnectionInfo other = new ThreeDxConnectionInfoDummy(pass3dsURL, space3dsURL, search3dsURL, changeActionQuery);
        assertEquals(thisN.hashCode(), other.hashCode());
    }

    @Test
    public void hasInfo() {
        assertNotNull(threeDxConnectionInfo.getPass3dsURL());
        assertNotNull(threeDxConnectionInfo.getSpace3dsURL());
        assertNotNull(threeDxConnectionInfo.getSearch3dsURL());
        assertNotNull(threeDxConnectionInfo.getChangeActionQuery());
        assertFalse(threeDxConnectionInfo.getPass3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getSpace3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getSearch3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getChangeActionQuery().isEmpty());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertTrue(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_AllNullValues() {
        threeDxConnectionInfo.setInfo(null, null, null, null);
        assertNull(threeDxConnectionInfo.getPass3dsURL());
        assertNull(threeDxConnectionInfo.getSpace3dsURL());
        assertNull(threeDxConnectionInfo.getSearch3dsURL());
        assertNull(threeDxConnectionInfo.getChangeActionQuery());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_NullPass3dsURL() {
        threeDxConnectionInfo.setInfo(null, space3dsURL, search3dsURL, changeActionQuery);
        assertNull(threeDxConnectionInfo.getPass3dsURL());
        assertNotNull(threeDxConnectionInfo.getSpace3dsURL());
        assertNotNull(threeDxConnectionInfo.getSearch3dsURL());
        assertNotNull(threeDxConnectionInfo.getChangeActionQuery());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_NullSpace3dsURL() {
        threeDxConnectionInfo.setInfo(pass3dsURL, null, search3dsURL, changeActionQuery);
        assertNotNull(threeDxConnectionInfo.getPass3dsURL());
        assertNull(threeDxConnectionInfo.getSpace3dsURL());
        assertNotNull(threeDxConnectionInfo.getSearch3dsURL());
        assertNotNull(threeDxConnectionInfo.getChangeActionQuery());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_NullSearch3dsURL() {
        threeDxConnectionInfo.setInfo(pass3dsURL, space3dsURL, null, changeActionQuery);
        assertNotNull(threeDxConnectionInfo.getPass3dsURL());
        assertNotNull(threeDxConnectionInfo.getSpace3dsURL());
        assertNull(threeDxConnectionInfo.getSearch3dsURL());
        assertNotNull(threeDxConnectionInfo.getChangeActionQuery());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_NullChangeActionQuery() {
        threeDxConnectionInfo.setInfo(pass3dsURL, space3dsURL, search3dsURL, null);
        assertNotNull(threeDxConnectionInfo.getPass3dsURL());
        assertNotNull(threeDxConnectionInfo.getSpace3dsURL());
        assertNotNull(threeDxConnectionInfo.getSearch3dsURL());
        assertNull(threeDxConnectionInfo.getChangeActionQuery());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_AllEmptyValues() {
        threeDxConnectionInfo.setInfo("", "", "", "");
        assertTrue(threeDxConnectionInfo.getPass3dsURL().isEmpty());
        assertTrue(threeDxConnectionInfo.getSpace3dsURL().isEmpty());
        assertTrue(threeDxConnectionInfo.getSearch3dsURL().isEmpty());
        assertTrue(threeDxConnectionInfo.getChangeActionQuery().isEmpty());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_EmptyPass3dsURL() {
        threeDxConnectionInfo.setInfo("", space3dsURL, search3dsURL, changeActionQuery);
        assertTrue(threeDxConnectionInfo.getPass3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getSpace3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getSearch3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getChangeActionQuery().isEmpty());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_EmptySpace3dsURL() {
        threeDxConnectionInfo.setInfo(pass3dsURL, "", search3dsURL, changeActionQuery);
        assertFalse(threeDxConnectionInfo.getPass3dsURL().isEmpty());
        assertTrue(threeDxConnectionInfo.getSpace3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getSearch3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getChangeActionQuery().isEmpty());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_EmptySearch3dsURL() {
        threeDxConnectionInfo.setInfo(pass3dsURL, space3dsURL, "", changeActionQuery);
        assertFalse(threeDxConnectionInfo.getPass3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getSpace3dsURL().isEmpty());
        assertTrue(threeDxConnectionInfo.getSearch3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getChangeActionQuery().isEmpty());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_EmptyChangeActionQuery() {
        threeDxConnectionInfo.setInfo(pass3dsURL, space3dsURL, search3dsURL, "");
        assertFalse(threeDxConnectionInfo.getPass3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getSpace3dsURL().isEmpty());
        assertFalse(threeDxConnectionInfo.getSearch3dsURL().isEmpty());
        assertTrue(threeDxConnectionInfo.getChangeActionQuery().isEmpty());

        boolean hasMandatoryInfo = threeDxConnectionInfo.hasInfo();
        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void clear() {
        assertNotNull(threeDxConnectionInfo.getPass3dsURL());
        assertNotNull(threeDxConnectionInfo.getSpace3dsURL());
        assertNotNull(threeDxConnectionInfo.getSearch3dsURL());
        assertNotNull(threeDxConnectionInfo.getChangeActionQuery());
        threeDxConnectionInfo.clear();
        assertNull(threeDxConnectionInfo.getPass3dsURL());
        assertNull(threeDxConnectionInfo.getSpace3dsURL());
        assertNull(threeDxConnectionInfo.getSearch3dsURL());
        assertNull(threeDxConnectionInfo.getChangeActionQuery());
    }
}
