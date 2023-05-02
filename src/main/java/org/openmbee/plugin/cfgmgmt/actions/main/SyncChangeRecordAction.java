package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.integration.jira.json.JiraIssueJson;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxIntegrationException;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.Lifecycle;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.google.gson.JsonObject;
import com.nomagic.magicdraw.actions.MDAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Optional;

public class SyncChangeRecordAction extends MDAction {
    private static final Logger logger = LoggerFactory.getLogger(SyncChangeRecordAction.class);
    private final transient ConfigurationManagementService configurationManagementService;

    public SyncChangeRecordAction(ConfigurationManagementService configurationManagementService) {
        super("Sync_CHANGE_RECORD_ACTION", "Sync change records", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected JiraService getJiraService() {
        return getConfigurationManagementService().getJiraService();
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    protected UIDomain getUiDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        configurationManagementService.getChangeRecords().forEach(changeRecord -> {
            List<String> sourceIds = getApiDomain().getStereotypePropertyValueAsString(changeRecord.getElement(),
                    changeRecord.getBaseStereotype(), PluginConstant.SOURCE_ID);
            List<String> sources = getApiDomain().getStereotypePropertyValueAsString(changeRecord.getElement(),
                    changeRecord.getBaseStereotype(), PluginConstant.SOURCE);
            String newStatusName = null;
            if(sourceIds != null && !sourceIds.isEmpty() && sources != null && !sources.isEmpty() && sources.get(0) != null) {
                newStatusName = getStatusNameFromSource(sources.get(0), sourceIds);
            }
            if(newStatusName == null) {
                return;
            }

            applyStatusToChangeRecord(changeRecord, newStatusName);
        });
    }

    protected String getStatusNameFromSource(String source, List<String> sourceIds) {
        JsonObject json;
        if (source.equals(PluginConstant.THREEDX_SOURCE)) {
            try {
                json = getChangeActionFrom3DX(sourceIds);
            } catch (Exception e) {
                getUiDomain().logErrorAndShowMessage(getLogger(),
                        String.format(ExceptionConstants.THREEDX_ERROR_GETTING_CHANGE_ACTION, e.getMessage()),
                        "3Dx Communication Error", e);
                return null;
            }
            return json.get("state").getAsString();
        } else if (source.equals(PluginConstant.JIRA_SOURCE)) {
            try {
                return getIssueFromJIRA(sourceIds).getFields().getStatus().getName();
            } catch (Exception e) {
                getUiDomain().logErrorAndShowMessage(getLogger(),
                        String.format(ExceptionConstants.JIRA_ERROR_GETTING_ISSUE, e.getMessage()),
                        "Jira Communication Error", e);
                return null;
            }
        }
        return null;
    }

    public JiraIssueJson getIssueFromJIRA(List<String> issueIds) throws JiraIntegrationException {
        return getJiraService().getIssue(issueIds.get(0));
    }

    public JsonObject getChangeActionFrom3DX(List<String> changeActions) throws ThreeDxIntegrationException {
        return getConfigurationManagementService().getThreeDxService().getChangeAction(changeActions.get(0));
    }

    protected void applyStatusToChangeRecord(ChangeRecord changeRecord, String newStatusName) {
        if (!newStatusName.equals(changeRecord.getStatusName())) {
            Optional<Lifecycle> lifecycle = changeRecord.getLifecycle();
            LifecycleStatus newStatus = lifecycle.map(v -> v.getStatusByName(newStatusName)).orElse(null);
            if (newStatus == null) {
                getUiDomain().showErrorMessage(String.format("Status [%s] is incompatible with Change Record lifecycle [%s]",
                        newStatusName, lifecycle.map(Lifecycle::getName).orElse("<missing lifecycle>")),
                        "Lifecycle incompatibility");
                return;
            }

            boolean configuredElementsReady = changeRecord.checkCEsForReadiness(newStatus);
            if(configuredElementsReady) {
                try {
                    getConfigurationManagementService().setLifecycleStatusChanging(true);
                    changeRecord.changeStatus(newStatus, null);
                } catch(Exception e) {
                    getUiDomain().logErrorAndShowMessage(getLogger(),
                            String.format(ExceptionConstants.ERROR_WHILE_CHANGING_STATUS_SINGLE_OBJECT, changeRecord.getName()),
                            ExceptionConstants.SYNCHRONIZATION_ERROR);
                } finally {
                    getConfigurationManagementService().setLifecycleStatusChanging(false);
                }
                getConfigurationManagementService().resetChangeRecordSelectionAfterStatusChange(changeRecord, newStatus);
            } else {
                getUiDomain().logErrorAndShowMessage(getLogger(),
                        ExceptionConstants.SYNC_ACTION_CONFIGURED_ELEMENTS_NOT_READY,
                        ExceptionConstants.SYNCHRONIZATION_ERROR);
            }
        }
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        setEnabled(configurationManagementService.isCmActive());
    }
}
