package org.openmbee.plugin.cfgmgmt.actions.main;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.List;

public class CreateChangeRecordAction extends MDAction {
    private static final Logger logger = LoggerFactory.getLogger(CreateChangeRecordAction.class);
    private final transient ConfigurationManagementService configurationManagementService;

    public CreateChangeRecordAction(ConfigurationManagementService configurationManagementService) {
        super("CREATE_CHANGE_RECORD_ACTION", "Create change record", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected ConfiguredElementDomain getConfiguredElementDomain() {
        return getConfigurationManagementService().getConfiguredElementDomain();
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected Logger getLogger() {
        return logger;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        Project project = getApiDomain().getCurrentProject();
        String id = getUIDomain().askForInput("Please enter the Change Record ID");
        if (id == null || id.isEmpty()) {
            return;
        }

        List<ChangeRecord> changeRecords = getConfigurationManagementService().getChangeRecords();
        ChangeRecord duplicate = changeRecords.stream().filter(cr -> cr.getName().equals(id)).findFirst().orElse(null);
        if (duplicate != null) {
            getUIDomain().showErrorMessage(String.format("A change record with ID [%s] already exists", id),
                "Change Record Creation failure");
            return;
        }

        List<Stereotype> stereotypes = getConfigurationManagementService().getCustomChangeRecordStereotypes(PluginConstant.CAMEO);
        Stereotype stereotype = getConfigurationManagementService().userChoosesDesiredStereotype(stereotypes,
            "Select change record type", "Change record type selection");
        if (stereotype == null) {
            return;
        }

        // Checking if the user has the required permission
        String action = "creation";
        if (!getConfiguredElementDomain().canUserPerformAction(getConfigurationManagementService(), stereotype, action)) {
            getUIDomain().logErrorAndShowMessage(getLogger(), "Configured element failure for " + action,
                "Permissions error");
            return;
        }

        // get the change management package
        Package chgRecs = getConfigurationManagementService().getChangeRecordsPackage(true);
        if (chgRecs == null) {
            getUIDomain().logErrorAndShowMessage(getLogger(), ExceptionConstants.CHANGE_RECORDS_PACKAGE_MISSING,
                    ExceptionConstants.PACKAGE_NOT_FOUND);
            return;
        }

        // Create Change Record
        Class clazz = project.getElementsFactory().createClassInstance();
        getConfigurationManagementService().initializeChangeRecord(clazz, id, stereotype, chgRecs);

        getApiDomain().setCurrentProjectHardDirty();
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        setEnabled(getConfigurationManagementService().isCmActive());
    }
}
