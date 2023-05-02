package org.openmbee.plugin.cfgmgmt.actions.context;

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

public class SetCommentsAction extends DefaultBrowserAction {
    private static final Logger logger = LoggerFactory.getLogger(SetCommentsAction.class);
    private final transient ConfigurationManagementService configurationManagementService;
    private JTextArea textArea;

    public SetCommentsAction(ConfigurationManagementService configurationManagementService) {
        super("SET_COMMENTS", "Set Comments", null, null);
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
        LifecycleObject lifecycleObject = getConfigurationManagementService().getChangeRecord((Element) getSelectedObjectOverride());
        if(lifecycleObject == null) {
            lifecycleObject = getConfigurationManagementService().getConfiguredElement((Element) getSelectedObjectOverride());
        }

        if(lifecycleObject == null) {
            getUIDomain().showErrorMessage("Cannot set comments on selected object.", "Set comments failure");
            return;
        }

        int res = getUIDomain().askForConfirmation(getPanel(lifecycleObject.getComments()),
                "Please provide the revision comments");
        if (res != JOptionPane.OK_OPTION) {
            return;
        }
        String comments = getTextArea().getText();

        lifecycleObject.setComments(comments);
        getApiDomain().setCurrentProjectHardDirty();
    }

    public Object getSelectedObjectOverride() {
        return getSelectedObject();
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        // this disables the action for non-element items in the tree such as "Project Usages"
        Object selectedObject = getSelectedObjectOverride();
        if (!(selectedObject instanceof NamedElement)) {
            setEnabled(false);
            return;
        }
        NamedElement element = (NamedElement) selectedObject;

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

            // this disables the contextual menu if the element is not configured
            if (!getConfigurationManagementService().isConfigured(element)) {
                setEnabled(false);
                return;
            }

            // this disables the contextual menu if the element is released
            ConfiguredElement configuredElement = getConfigurationManagementService().getConfiguredElement(element);
            setEnabled(configuredElement != null && !configuredElement.isReleased());
        } else {
            // this disables the contextual menu if the element is not editable
            ChangeRecord changeRecord = getConfigurationManagementService().getChangeRecord(element);
            setEnabled(changeRecord != null && !changeRecord.isReleased());
        }
    }

    public JPanel getPanel(String comments) {
        textArea = new JTextArea(10, 50);
        textArea.setText(comments);

        JScrollPane scrollPane = new JScrollPane(textArea);
        JPanel panel = new JPanel();
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    protected JTextArea getTextArea() {
        return textArea; // used for unit testing
    }
}
