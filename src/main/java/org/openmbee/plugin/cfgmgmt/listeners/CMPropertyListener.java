package org.openmbee.plugin.cfgmgmt.listeners;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.impl.PropertyNames;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class CMPropertyListener implements PropertyChangeListener {

    private final ConfigurationManagementService configurationManagementService;

    public CMPropertyListener(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!getConfigurationManagementService().isCmActive()) {
            return;
        }

        if (evt.getSource() instanceof ElementTaggedValue
                && evt.getPropertyName().equals(PropertyNames.VALUE)
                && tagDefinitionMatchesChangeRecordStatus(((ElementTaggedValue) evt.getSource()))) {
            getConfigurationManagementService().setChangeRecordSelections();
            return;
        }

        // custom setting changed that is a string tag value
        if (evt.getSource() instanceof StringTaggedValue
                && evt.getPropertyName().equals(PropertyNames.VALUE)
                && tagDefinitionOwnerMatchesId((StringTaggedValue) evt.getSource(), PluginConstant.CM_PLUGIN_SETTINGS_STEREOTYPE_ID)) {
            getConfigurationManagementService().getCustomSettings().update();
            return;
        }

        // custom setting changed that is a boolean tag value
        if(evt.getSource() instanceof BooleanTaggedValue
                && evt.getPropertyName().equals(PropertyNames.VALUE)
                && tagDefinitionOwnerMatchesId((BooleanTaggedValue) evt.getSource(), PluginConstant.CM_PLUGIN_SETTINGS_STEREOTYPE_ID)) {
            getConfigurationManagementService().getCustomSettings().update();
            return;
        }

        if (evt.getSource() instanceof StringTaggedValue
                && evt.getPropertyName().equals(PropertyNames.VALUE)
                && tagDefinitionOwnerMatchesId((StringTaggedValue) evt.getSource(), PluginConstant.THREEDX_CONNECTION_SETTINGS_STEREOTYPE_ID)) {
            getConfigurationManagementService().getThreeDxService().update3DxConnectionInfo();
        }

        if (evt.getSource() instanceof StringTaggedValue
                && evt.getPropertyName().equals(PropertyNames.VALUE)
                && tagDefinitionOwnerMatchesId((StringTaggedValue) evt.getSource(), PluginConstant.JIRA_CONNECTION_SETTINGS_STEREOTYPE_ID)) {
            getConfigurationManagementService().getJiraService().updateJIRAConnectionInfo();
        }
    }

    protected boolean tagDefinitionMatchesChangeRecordStatus(TaggedValue taggedValue) {
        Property tagDefinition = taggedValue.getTagDefinition();
        if(tagDefinition != null) {
            String tagDefinitionLocalID = tagDefinition.getLocalID();
            return tagDefinitionLocalID != null && tagDefinitionLocalID.equals(PluginConstant.CHANGE_RECORD_STATUS_ID);
        }
        return false;
    }

    protected boolean tagDefinitionOwnerMatchesId(TaggedValue taggedValue, String id) {
        Property tagDefinition = taggedValue.getTagDefinition();
        if(tagDefinition != null) {
            Element owner = tagDefinition.getOwner();
            if(owner != null) {
                String ownerId = owner.getLocalID();
                return ownerId != null && ownerId.equals(id);
            }
        }
        return false;
    }
}
