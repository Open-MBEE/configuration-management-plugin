package org.openmbee.plugin.cfgmgmt.integration.jira;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.junit.Assert.*;

public class TestJiraConnectionInfo {
    private String jiraURL;
    private String jiraRestPath;
    private String issueQuery;
    private JiraConnectionInfo jiraConnectionInfo;

    @Before
    public void setup() {
        jiraURL = "jiraURL";
        jiraRestPath = "jiraRestPath";
        issueQuery = "issueQuery";
        jiraConnectionInfo = spy(new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery));
    }

    private static class JiraConnectionInfoDummy extends JiraConnectionInfo {
        public JiraConnectionInfoDummy(String jiraURLDummy, String jiraRESTPathDummy,String issueQueryDummy) {
            super(jiraURLDummy, jiraRESTPathDummy, issueQueryDummy);
        }
    }

    @Test
    public void getJiraRestPath() {
        assertNotNull(jiraConnectionInfo.getJiraRestPath());
    }

    @Test
    public void getIssueQuery() {
        assertNotNull(jiraConnectionInfo.getIssueQuery());
    }

    @Test
    public void getLoginUrl() {
        assertNotNull(jiraConnectionInfo.getUrl());
    }

    @Test
    public void clear(){
        assertNotNull(jiraConnectionInfo.getUrl());
        assertNotNull(jiraConnectionInfo.getJiraRestPath());
        assertNotNull(jiraConnectionInfo.getIssueQuery());
        jiraConnectionInfo.clear();
        assertNull(jiraConnectionInfo.getUrl());
        assertNull(jiraConnectionInfo.getJiraRestPath());
        assertNull(jiraConnectionInfo.getIssueQuery());
    }

    @Test
    public void hasInfo(){
        assertNotNull(jiraConnectionInfo.getUrl());
        assertNotNull(jiraConnectionInfo.getJiraRestPath());
        assertNotNull(jiraConnectionInfo.getIssueQuery());
        assertFalse(jiraConnectionInfo.getUrl().isEmpty());
        assertFalse(jiraConnectionInfo.getJiraRestPath().isEmpty());
        assertFalse(jiraConnectionInfo.getIssueQuery().isEmpty());

        assertTrue(jiraConnectionInfo.hasInfo());
    }

    @Test
    public void hasInfo_NullJiraUrl_NullJiraRestPath_NullIssueQuery(){
        jiraConnectionInfo.setInfo(null , null , null);
        assertNull(jiraConnectionInfo.getUrl());
        assertNull(jiraConnectionInfo.getJiraRestPath());
        assertNull(jiraConnectionInfo.getIssueQuery());

        assertFalse(jiraConnectionInfo.hasInfo());
    }

    @Test
    public void hasInfo_NullJiraUrl_ProperJiraRestPath_ProperIssueQuery(){

        jiraConnectionInfo.setInfo(null , "jiraRestPath" , "issueQuery");
        assertNull(jiraConnectionInfo.getUrl());
        assertNotNull(jiraConnectionInfo.getJiraRestPath());
        assertNotNull(jiraConnectionInfo.getIssueQuery());

        boolean hasMandatoryInfo = jiraConnectionInfo.hasInfo();

        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_ProperJiraUrl_NullJiraRestPath_ProperIssueQuery(){

        jiraConnectionInfo.setInfo("jiraURL" , null , "issueQuery");
        assertNotNull(jiraConnectionInfo.getUrl());
        assertNull(jiraConnectionInfo.getJiraRestPath());
        assertNotNull(jiraConnectionInfo.getIssueQuery());

        boolean hasMandatoryInfo = jiraConnectionInfo.hasInfo();

        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_ProperJiraUrl_ProperJiraRestPath_NullIssueQuery(){

        jiraConnectionInfo.setInfo("jiraURL" , "jiraRestPath" , null);
        assertNotNull(jiraConnectionInfo.getUrl());
        assertNotNull(jiraConnectionInfo.getJiraRestPath());
        assertNull(jiraConnectionInfo.getIssueQuery());

        boolean hasMandatoryInfo = jiraConnectionInfo.hasInfo();

        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_NullJiraUrl_NullJiraRestPath_ProperIssueQuery(){

        jiraConnectionInfo.setInfo(null , null, "issueQuery");
        assertNull(jiraConnectionInfo.getUrl());
        assertNull(jiraConnectionInfo.getJiraRestPath());
        assertNotNull(jiraConnectionInfo.getIssueQuery());

        boolean hasMandatoryInfo = jiraConnectionInfo.hasInfo();

        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_NullJiraUrl_ProperJiraRestPath_NullIssueQuery(){

        jiraConnectionInfo.setInfo(null , "jiraRestPath", null);
        assertNull(jiraConnectionInfo.getUrl());
        assertNotNull(jiraConnectionInfo.getJiraRestPath());
        assertNull(jiraConnectionInfo.getIssueQuery());

        boolean hasMandatoryInfo = jiraConnectionInfo.hasInfo();

        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_ProperJiraUrl_NullJiraRestPath_NullIssueQuery(){

        jiraConnectionInfo.setInfo("jiraUR0" , null, null);
        assertNotNull(jiraConnectionInfo.getUrl());
        assertNull(jiraConnectionInfo.getJiraRestPath());
        assertNull(jiraConnectionInfo.getIssueQuery());

        boolean hasMandatoryInfo = jiraConnectionInfo.hasInfo();

        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_EmptyJiraUrl_EmptyJiraRestPath_EmptyIssueQuery(){
        jiraConnectionInfo.setInfo("" , "" , "");
        assertTrue(jiraConnectionInfo.getUrl().isEmpty());
        assertTrue(jiraConnectionInfo.getJiraRestPath().isEmpty());
        assertTrue(jiraConnectionInfo.getIssueQuery().isEmpty());

        assertFalse(jiraConnectionInfo.hasInfo());
    }

    @Test
    public void hasInfo_EmptyJiraUrl_ProperJiraRestPath_ProperIssueQuery(){
        jiraConnectionInfo.setInfo("" , "jiraRestPath" , "issueQuery");
        assertTrue(jiraConnectionInfo.getUrl().isEmpty());
        assertFalse(jiraConnectionInfo.getJiraRestPath().isEmpty());
        assertFalse(jiraConnectionInfo.getIssueQuery().isEmpty());

        assertFalse(jiraConnectionInfo.hasInfo());
    }

    @Test
    public void hasInfo_ProperJiraUrl_EmptyJiraRestPath_ProperIssueQuery(){
        jiraConnectionInfo.setInfo("jiraUrl" , "" , "issueQuery");
        assertFalse(jiraConnectionInfo.getUrl().isEmpty());
        assertTrue(jiraConnectionInfo.getJiraRestPath().isEmpty());
        assertFalse(jiraConnectionInfo.getIssueQuery().isEmpty());

        assertFalse(jiraConnectionInfo.hasInfo());
    }

    @Test
    public void hasInfo_EmptyJiraUrl_ProperJiraRestPath_EmptyIssueQuery(){

        jiraConnectionInfo.setInfo("" , "jiraRestPath" , "");
        assertTrue(jiraConnectionInfo.getUrl().isEmpty());
        assertFalse(jiraConnectionInfo.getJiraRestPath().isEmpty());
        assertTrue(jiraConnectionInfo.getIssueQuery().isEmpty());

        boolean hasMandatoryInfo = jiraConnectionInfo.hasInfo();

        assertFalse(hasMandatoryInfo);
    }


    @Test
    public void hasInfo_EmptyJiraUrl_EmptyJiraRestPath_ProperIssueQuery(){

        jiraConnectionInfo.setInfo("" , "" , "issueQuery");
        assertTrue(jiraConnectionInfo.getUrl().isEmpty());
        assertTrue(jiraConnectionInfo.getJiraRestPath().isEmpty());
        assertFalse(jiraConnectionInfo.getIssueQuery().isEmpty());

        boolean hasMandatoryInfo = jiraConnectionInfo.hasInfo();

        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void hasInfo_ProperJiraUrl_ProperJiraRestPath_EmptyIssueQuery(){
        jiraConnectionInfo.setInfo("jiraUrl" , "jiraRestPath" , "");
        assertFalse(jiraConnectionInfo.getUrl().isEmpty());
        assertFalse(jiraConnectionInfo.getJiraRestPath().isEmpty());
        assertTrue(jiraConnectionInfo.getIssueQuery().isEmpty());

        assertFalse(jiraConnectionInfo.hasInfo());
    }

    @Test
    public void hasInfo_ProperJiraUrl_EmptyJiraRestPath_EmptyIssueQuery(){

        jiraConnectionInfo.setInfo("jiraUrl" , "" , "");
        assertFalse(jiraConnectionInfo.getUrl().isEmpty());
        assertTrue(jiraConnectionInfo.getJiraRestPath().isEmpty());
        assertTrue(jiraConnectionInfo.getIssueQuery().isEmpty());

        boolean hasMandatoryInfo = jiraConnectionInfo.hasInfo();

        assertFalse(hasMandatoryInfo);
    }

    @Test
    public void equals_Test1() {
        JiraConnectionInfo ob1 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);

        assertTrue(ob1.equals(ob1));
    }

    @Test
    public void equals_Test2() {
        JiraConnectionInfo ob1 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);
        JiraConnectionInfo ob2 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);

        assertTrue(ob1.equals(ob2));
    }

    @Test
    public void equals_Test3() {
        JiraConnectionInfo ob1 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);
        JiraConnectionInfo ob2 = null;

        assertFalse(ob1.equals(ob2));
    }

    @Test
    public void equals_Test4() {
        JiraConnectionInfo ob1 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);
        List<Object> ob2 = new ArrayList<>();

        assertFalse(ob1.equals(ob2));
    }

    @Test
    public void equals_Test5() {
        JiraConnectionInfo ob1 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);
        JiraConnectionInfo ob2 = new JiraConnectionInfo(null, null, null);

        assertFalse(ob1.equals(ob2));
    }

    @Test
    public void equals_Test6() {
        JiraConnectionInfo ob1 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);
        JiraConnectionInfo ob2 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);

        assertTrue(ob1.equals(ob2));
    }

    @Test
    public void equals_Test7() {
        JiraConnectionInfo ob1 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);
        JiraConnectionInfo ob2 = new JiraConnectionInfo(null, jiraRestPath, issueQuery);

        assertFalse(ob1.equals(ob2));
    }

    @Test
    public void equals_Test8() {
        JiraConnectionInfo ob1 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);
        JiraConnectionInfo ob2 = new JiraConnectionInfo(jiraURL, null, issueQuery);

        assertFalse(ob1.equals(ob2));
    }

    @Test
    public void equals_Test9() {
        JiraConnectionInfo ob1 = new JiraConnectionInfo(jiraURL, jiraRestPath, issueQuery);
        JiraConnectionInfo ob2 = new JiraConnectionInfo(jiraURL, jiraRestPath, null);

        assertFalse(ob1.equals(ob2));
    }

    @Test
    public void hashCodeTest() {
        JiraConnectionInfo these = new TestJiraConnectionInfo.JiraConnectionInfoDummy(jiraURL, jiraRestPath, issueQuery);
        JiraConnectionInfo other = new TestJiraConnectionInfo.JiraConnectionInfoDummy(jiraURL, jiraRestPath, issueQuery);

        assertEquals(these.hashCode(), other.hashCode());
    }
}