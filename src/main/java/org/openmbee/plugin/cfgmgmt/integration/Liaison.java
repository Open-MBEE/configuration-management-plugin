package org.openmbee.plugin.cfgmgmt.integration;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Liaison {
    private JiraService jiraService;
    private ThreeDxService threeDxService;
    private List<Map<String, String>> currentData;
    private int sliceSize;
    private int sliceNumber;
    private boolean queryCompleted;

    public Liaison(JiraService jiraService) {
        this.jiraService = jiraService;
        reset();
    }

    public Liaison(ThreeDxService threeDxService) {
        this.threeDxService = threeDxService;
        reset();
    }

    public Liaison(JiraService jiraService, int sliceSize) {
        this(jiraService);
        this.sliceSize = sliceSize;
    }

    public Liaison(ThreeDxService threeDxService, int sliceSize) {
        this(threeDxService);
        this.sliceSize = sliceSize;
    }

    public void reset() {
        if (currentData == null) {
            currentData = new ArrayList<>();
        }
        currentData.clear();
        if (sliceSize <= 0) {
            sliceSize = PluginConstant.MAX_RESULTS_DEFAULT_VALUE; // don't override constructed size if possible
        }
        sliceNumber = 0;
        queryCompleted = false;
        if (threeDxService != null) {
            threeDxService.getThreeDxClientManager().setNewRun(true); // due to 3Dx weirdness, we need this for a full reset
        }
    }

    public List<Map<String, String>> getIssues() throws JiraIntegrationException {
        if (!queryCompleted) {
            List<Map<String, String>> currentSlice = jiraService.getIssues(sliceNumber * sliceSize, sliceSize);
            addSlices(currentSlice);
        }

        if (areAllSlicesGiven()) {
            return new ArrayList<>();
        }
        return giveNextSlice();
    }

    public List<Map<String, String>> getChangeActions() throws ThreeDxIntegrationException {
        if (!queryCompleted) {
            List<Map<String, String>> currentSlice = threeDxService.getChangeActions(sliceSize);
            addSlices(currentSlice);
        }

        if (areAllSlicesGiven()) {
            return new ArrayList<>();
        }
        return giveNextSlice();
    }

    public void addSlices(List<Map<String, String>> currentSlice) {
        if (!currentSlice.isEmpty()) {
            currentData.addAll(currentSlice);
            if (currentSlice.size() < sliceSize) {
                queryCompleted = true;
            }
        } else {
            queryCompleted = true;
        }
    }

    protected List<Map<String, String>> giveNextSlice() {
        int from = sliceNumber * sliceSize;
        int to = (sliceNumber + 1) * sliceSize;
        if (to > currentData.size()) {
            to = currentData.size();
        }

        sliceNumber++;
        List<Map<String, String>> slice = new ArrayList<>();
        for (int i = from; i < to; i++) {
            slice.add(currentData.get(i)); // forced to iterate, using sublist causes an exception
        }
        return slice;
    }

    protected boolean areAllSlicesGiven() {
        return sliceSize <= 0 || sliceNumber * sliceSize >= currentData.size();
    }
}
