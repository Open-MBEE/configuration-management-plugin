package org.openmbee.plugin.cfgmgmt.actions.toolbar;

import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.actions.SelectItemAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageableElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SelectChangeRecordAction extends SelectItemAction {
    private static final Logger logger = LoggerFactory.getLogger(SelectChangeRecordAction.class);
    private final transient ConfigurationManagementService configurationManagementService;

    public SelectChangeRecordAction(ConfigurationManagementService configurationManagementService) {
        super("ACTIVE_CHANGE_RECORD", "Active change record", null, null,
                List.of("---------------------------"), "Selection");
        this.configurationManagementService = configurationManagementService;
        this.setDisplayAsComboBox(true);
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected LifecycleObjectFactory getLifecycleObjectFactory() {
        return getConfigurationManagementService().getLifecycleObjectFactory();
    }

    protected UIDomain getUIDomain() {
        return getConfigurationManagementService().getUIDomain();
    }

    protected Logger getLogger() {
        return logger;
    }

    /**
     * Updates menu item.
     */
    @Override
    public void updateState() {
        setEnabled(getConfigurationManagementService().isCmActive());
    }

    @Override
    public void propertyChange(PropertyChangeEvent var1) {
        // this is a copy of the underlying property change method with the addition of changing the record name
        // in the configuration management service
        if (var1.getPropertyName().equals(this.getValueName())) {
            if(var1.getNewValue() instanceof String) {
                configurationManagementService.setChangeRecordName((String) var1.getNewValue());
            }
            this.updateState();
        }
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        configurationManagementService.updateCRStatus();
    }

    public String getSelectedChangeRecord() {
        return (String) this.getValue();
    }

    public void setSelectedChangeRecord(String name) {
        // only use for manual reset of selected active change record
        List<String> selections = getSelections();
        if(selections != null && !selections.isEmpty() && selections.stream().anyMatch(s -> s.equals(name))) {
            this.setValue(name);
        }
    }

    public void resetSelections() {
        this.setValue(null);
        this.setChangeRecordSelections();
    }

    public void clearList() {
        this.setValue(null);
        this.setItems(new ArrayList<>());
    }

    public List<String> getSelections() {
        return (List<String>) this.getItems();
    }

    public void setChangeRecordSelections() {
        Package chgRecordsPkg = getConfigurationManagementService().getChangeRecordsPackage(false);
        if (chgRecordsPkg == null) {
            this.setItems(new ArrayList<>());
            this.setValue(null);
            return;
        }

        Collection<PackageableElement> pkgContents = chgRecordsPkg.getPackagedElement();

        List<String> activeRecords = pkgContents.stream().filter(activeRecord -> {
            if (activeRecord instanceof Class) {
                return !getLifecycleObjectFactory().getChangeRecord(getConfigurationManagementService(), (Class) activeRecord).isReleased();
            } else {
                return false;
            }
        }).map(NamedElement::getName).collect(Collectors.toList());

        this.setItems(activeRecords);

        if (this.getSelectedChangeRecord() != null && !this.getSelections().contains(this.getSelectedChangeRecord())) {
            this.setValue(null);
        }
    }
}
