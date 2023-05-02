package org.openmbee.plugin.cfgmgmt.integration;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxClientManager;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxService;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TestLiaison {
    private Liaison jiraIssueLiaison;
    private Liaison threeDxChangeActionsLiaison;
    private JiraService jiraService;
    private ThreeDxService threeDxService;

    @Before
    public void setup() {
        jiraService = mock(JiraService.class);
        jiraIssueLiaison = spy(new Liaison(jiraService));
        threeDxService = mock(ThreeDxService.class);
        ThreeDxClientManager threeDxClientManager = mock(ThreeDxClientManager.class);

        when(threeDxService.getThreeDxClientManager()).thenReturn(threeDxClientManager);
        doNothing().when(threeDxClientManager).setNewRun(true);
        
        threeDxChangeActionsLiaison = spy(new Liaison(threeDxService));
    }

    @Test
    public void getIssues_Empty() throws JiraIntegrationException {
        when(jiraService.getIssues(0, PluginConstant.MAX_RESULTS_DEFAULT_VALUE)).thenReturn(new ArrayList<>());

        assertTrue(jiraIssueLiaison.getIssues().isEmpty());
        verify(jiraIssueLiaison, never()).giveNextSlice();
    }

    @Test
    public void getIssues_singleSlice() throws JiraIntegrationException {
        List<Map<String, String>> slice = new ArrayList<>();
        Map<String, String> data = new HashMap<>();
        slice.add(data);

        when(jiraService.getIssues(0, PluginConstant.MAX_RESULTS_DEFAULT_VALUE)).thenReturn(slice);

        List<Map<String, String>> results = jiraIssueLiaison.getIssues();

        assertFalse(results.isEmpty());
        verify(jiraIssueLiaison).giveNextSlice();
    }

    @Test
    public void getIssues_twoSlices() throws JiraIntegrationException {
        jiraIssueLiaison = spy(new Liaison(jiraService, 2));
        List<Map<String, String>> slice = new ArrayList<>();
        Map<String, String> data = new HashMap<>();
        Map<String, String> data2 = new HashMap<>();
        List<Map<String, String>> slice2 = new ArrayList<>();
        Map<String, String> data3 = new HashMap<>();
        slice.add(data);
        slice.add(data2);
        slice2.add(data3);

        when(jiraService.getIssues(0, 2)).thenReturn(slice);
        when(jiraService.getIssues(2, 2)).thenReturn(slice2);
        when(jiraService.getIssues(3, 2)).thenReturn(new ArrayList<>());

        List<Map<String, String>> resultSlice1 = jiraIssueLiaison.getIssues();
        List<Map<String, String>> resultSlice2 = jiraIssueLiaison.getIssues();
        List<Map<String, String>> resultSlice3 = jiraIssueLiaison.getIssues();

        assertFalse(resultSlice1.isEmpty());
        assertFalse(resultSlice2.isEmpty());
        assertTrue(resultSlice3.isEmpty());
        verify(jiraIssueLiaison, times(2)).giveNextSlice();
    }

    @Test
    public void getIssues_exceptionGettingIssuesFromJiraService() {
        String error = "error";
        JiraIntegrationException jiraIntegrationException = spy(new JiraIntegrationException(error));
        try {
            doThrow(jiraIntegrationException).when(jiraService).getIssues(0, PluginConstant.MAX_RESULTS_DEFAULT_VALUE);

            jiraIssueLiaison.getIssues();

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertEquals(error, e.getMessage());
        }
    }

    @Test
    public void getChangeActions_Empty() throws ThreeDxIntegrationException {
        when(threeDxService.getChangeActions(PluginConstant.MAX_RESULTS_DEFAULT_VALUE)).thenReturn(new ArrayList<>());

        assertTrue(threeDxChangeActionsLiaison.getChangeActions().isEmpty());
        verify(threeDxChangeActionsLiaison, never()).giveNextSlice();
    }

    @Test
    public void getChangeActions_singleSlice() throws ThreeDxIntegrationException {
        List<Map<String, String>> slice = new ArrayList<>();
        Map<String, String> data = new HashMap<>();
        slice.add(data);

        when(threeDxService.getChangeActions(PluginConstant.MAX_RESULTS_DEFAULT_VALUE)).thenReturn(slice);

        List<Map<String, String>> results = threeDxChangeActionsLiaison.getChangeActions();

        assertFalse(results.isEmpty());
        verify(threeDxChangeActionsLiaison).giveNextSlice();
    }

    @Test
    public void getChangeActions_twoSlices() throws ThreeDxIntegrationException {
        threeDxChangeActionsLiaison = spy(new Liaison(threeDxService, 2));
        List<Map<String, String>> slice = new ArrayList<>();
        Map<String, String> data = new HashMap<>();
        Map<String, String> data2 = new HashMap<>();
        List<Map<String, String>> slice2 = new ArrayList<>();
        Map<String, String> data3 = new HashMap<>();
        slice.add(data);
        slice.add(data2);
        slice2.add(data3);

        when(threeDxService.getChangeActions(2)).thenReturn(slice).thenReturn(slice2).thenReturn(new ArrayList<>());

        List<Map<String, String>> resultSlice1 = threeDxChangeActionsLiaison.getChangeActions();
        List<Map<String, String>> resultSlice2 = threeDxChangeActionsLiaison.getChangeActions();
        List<Map<String, String>> resultSlice3 = threeDxChangeActionsLiaison.getChangeActions();

        assertFalse(resultSlice1.isEmpty());
        assertFalse(resultSlice2.isEmpty());
        assertTrue(resultSlice3.isEmpty());
        verify(threeDxChangeActionsLiaison, times(2)).giveNextSlice();
    }

    @Test
    public void getChangeActions_exceptionGettingChangeActionsFromThreeDxService() {
        String error = "error";
        ThreeDxIntegrationException threeDxIntegrationException = spy(new ThreeDxIntegrationException(error));
        try {
            doThrow(threeDxIntegrationException).when(threeDxService).getChangeActions(PluginConstant.MAX_RESULTS_DEFAULT_VALUE);

            threeDxChangeActionsLiaison.getChangeActions();

            fail("Expected exception did not occur");
        } catch (Exception e) {
            assertSame(error, e.getMessage());
        }
    }
}