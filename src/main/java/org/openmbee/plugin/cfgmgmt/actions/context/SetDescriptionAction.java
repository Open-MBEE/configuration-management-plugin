package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleObject;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SetDescriptionAction extends DefaultBrowserAction {
    private static final Logger logger = LoggerFactory.getLogger(SetDescriptionAction.class);
    private final transient ConfigurationManagementService configurationManagementService;
    private JTextArea textArea;

    public SetDescriptionAction(ConfigurationManagementService configurationManagementService) {
        super("SET_DESCRIPTION", "Set Description", null, null);
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected LifecycleObjectFactory getLifecycleObjectFactory() {
        return getConfigurationManagementService().getLifecycleObjectFactory();
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
        try {
            LifecycleObject lifecycleObject = getConfigurationManagementService().getChangeRecord((Element) getSelectedObjectOverride());
            if(lifecycleObject == null) {
                lifecycleObject = getConfigurationManagementService().getConfiguredElement((Element) getSelectedObjectOverride());
            }

            if(lifecycleObject == null) {
                getUIDomain().showErrorMessage("Cannot set description for selected object", "Set description failure");
                return;
            }

            String currentDescription = lifecycleObject.getDescription();
            String description = getUpdatedDescriptionFromUser(currentDescription);

            if(description == null) {
                return;
            }

            lifecycleObject.setDescription(description);
            getApiDomain().setCurrentProjectHardDirty();
        } catch (Exception e) {
            getUIDomain().logErrorAndShowMessage(getLogger(), "Error occurred while setting a description.",
                    "Set description failure", e);
        }
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        try {
            // this disables the action for non-element items in the tree such as "Project Usages"
            if (!(getSelectedObjectOverride() instanceof NamedElement)) {
                setEnabled(false);
                return;
            }

            NamedElement element = (NamedElement) getSelectedObjectOverride();

            // this disables the action when the element is not editable, not locked and not new
            if(!getApiDomain().isElementInEditableState(element)) {
                setEnabled(false);
                return;
            }

            // the following checks do not apply to Change Records
            if (!getApiDomain().hasStereotypeOrDerived(element, getConfigurationManagementService().getBaseCRStereotype())) {
                // this disables the contextual menu when the CM profile is not present or when no change record is selected
                if (!getConfigurationManagementService().isCmActive()
                        || !getConfigurationManagementService().isChangeRecordSelected()) {
                    setEnabled(false);
                    return;
                }

                // this disables the contextual menu when the selected CR is not at an expendable status
                ChangeRecord cr = getConfigurationManagementService().getSelectedChangeRecord();
                if (cr == null || (cr.hasStatus() && !cr.isExpandable() && cr.isReadOnly())) {
                    setEnabled(false);
                    return;
                }

                // this disables the contextual menu if the element is not configured
                if (!getConfigurationManagementService().isConfigured(element)) {
                    setEnabled(false);
                    return;
                }

                // this disables the contextual menu if the element is not editable
                ConfiguredElement configuredElement = getConfigurationManagementService().getConfiguredElement(element);
                setEnabled(configuredElement != null && !configuredElement.isReadOnly());
            } else {
                // this enables the contextual menu if the element is editable, which means we need to get a change
                // record from it and determine it is either *not* read only or if it is expendable
                ChangeRecord changeRecord = getConfigurationManagementService().getChangeRecord(element);
                setEnabled(changeRecord != null && (!changeRecord.isReadOnly() || changeRecord.isExpandable()));
            }
        } catch (Exception e) {
            setEnabled(false);
            getUIDomain().logErrorAndShowMessage(getLogger(), "Cannot update state for SetDescriptionAction, forcing disable",
                    ExceptionConstants.ACTION_STATE_FAILURE, e);
        }
    }

    protected String getUpdatedDescriptionFromUser(String currentDescription) {
        int res = getUIDomain().askForConfirmation(getPanel(currentDescription), "Please provide the description");
        if (res != JOptionPane.OK_OPTION) {
            return null;
        }
        return textArea.getText();
    }

    protected JPanel getPanel(String description) {
        JPanel panel = new JPanel();

        textArea = new JTextArea(10, 50);
        textArea.setText(description);
        JScrollPane scrollPane = new JScrollPane(textArea);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;

    }

    public Object getSelectedObjectOverride() {
        return getSelectedObject(); // needed for unit tests
    }
}
