package org.openmbee.plugin.cfgmgmt.permissions;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.permissions.ElementPermissions;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;

import java.util.Collection;

public class CMElementPermissions implements ElementPermissions {

    private final ConfigurationManagementService configurationManagementService;

    public CMElementPermissions(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    @Override
    public boolean isElementEditable(BaseElement baseElement) {
        try {
            if (getConfigurationManagementService().isCmActive() && !getConfigurationManagementService().getAdminMode()) {
                if (baseElement instanceof PresentationElement) {
                    // diagram contents
                    Diagram d = ((PresentationElement) baseElement).getDiagramPresentationElement().getDiagram();
                    ConfiguredElement configuredElement = getConfiguredElement(d);
                    if(configuredElement == null) {
                        return true;
                    } else if (configuredElement.isReadOnly()) {
                        return false;
                    } else { // if affecting CR active
                        return isChangeRecordActive(configuredElement);
                    }
                } else if (baseElement instanceof Element) {
                    return checkElementPermissions((Element) baseElement, true);
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            return true;
        }

        return true;
    }

    @Override
    public boolean canCreateChildIn(BaseElement baseElement) {
        try {
            if (getConfigurationManagementService().isCmActive() && !getConfigurationManagementService().getAdminMode()) {
                if (baseElement instanceof Element) {
                    return checkElementPermissions((Element) baseElement, false);
                } else {
                    return true;
                }
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    @Override
    public boolean canAddChild(BaseElement parent, BaseElement child) {
        try {
            if (getConfigurationManagementService().isCmActive() && !getConfigurationManagementService().getAdminMode()) {
                if (parent instanceof Element && !checkElementPermissions((Element) parent, false)) {
                    return false; // if parent isn't allowed to add a child we leave early
                }

                if (child instanceof Element) {
                    return checkElementPermissions((Element) child, true);
                }
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    @Override
    public boolean canDelete(BaseElement baseElement) {
        try {
            if (getConfigurationManagementService().isCmActive() && !getConfigurationManagementService().getAdminMode() &&
                    baseElement instanceof Element) {
                return checkElementPermissions((Element) baseElement, true);
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    protected boolean checkElementPermissions(Element element, boolean checkCmPackage) {
        // if comment owned by CR
        if(element instanceof Comment) {
            return commentPermissionCheck((Comment) element);
        }

        ConfiguredElement configuredElement = getConfiguredElement(element);
        if (configuredElement != null && configuredElement.isReadOnly()) {
            return false;
        }

        // if RH or CR
        if (isElementARevisionHistoryOrChangeRecord(element)) {
            return false;
        }

        // if CM, CR, or RH package
        if (isPackageChangeManagementChangeRecordsOrRevisionHistory(element, checkCmPackage)) {
            return false;
        }

        // if affecting CR active
        if(configuredElement != null) {
            return isChangeRecordActive(configuredElement);
        }

        if (element instanceof TaggedValue) {
            return taggedValuePermissionCheck((TaggedValue) element);
        }

        return true;
    }

    protected boolean isPackageChangeManagementChangeRecordsOrRevisionHistory(Element element, boolean checkChangeManagementPackage) {
        if(element instanceof Package) {
            if(checkChangeManagementPackage) {
                return isPackageChangeManagementChangeRecordsOrRevisionHistory((Package) element);
            }
            return isPackageChangeRecordsOrRevisionHistory((Package) element);
        }
        return false;
    }

    protected boolean isPackageChangeManagementChangeRecordsOrRevisionHistory(Package pkg) {
        return pkg.equals(getConfigurationManagementService().getChangeManagementPackage(false))
                || pkg.equals(getConfigurationManagementService().getChangeRecordsPackage(false))
                || pkg.equals(getConfigurationManagementService().getRevisionHistoryPackage(false));
    }

    protected boolean isPackageChangeRecordsOrRevisionHistory(Package pkg) {
        return pkg.equals(getConfigurationManagementService().getChangeRecordsPackage(false))
                || pkg.equals(getConfigurationManagementService().getRevisionHistoryPackage(false));
    }

    protected boolean isElementARevisionHistoryOrChangeRecord(Element element) {
        if (getApiDomain().hasStereotype(element, getConfigurationManagementService().getRhStereotype())) {
            return true; // if RH
        }

        // if CR
        return getApiDomain().hasStereotypeOrDerived(element, getConfigurationManagementService().getBaseCRStereotype());
    }

    protected boolean isChangeRecordActive(ConfiguredElement configuredElement) {
        if (getConfigurationManagementService().getEnforceActiveCRSwitch()) {
            ChangeRecord changeRecord = getConfigurationManagementService().getSelectedChangeRecord();
            if (changeRecord == null || changeRecord.isReadOnly()) {
                return false;
            }

            return changeRecord.equals(configuredElement.getReleaseAuthority());
        }
        return true;
    }

    protected boolean taggedValuePermissionCheck(TaggedValue taggedValue) {
        return propertyPermissionCheck(taggedValue.getTagDefinition());
    }

    protected boolean propertyPermissionCheck(Property property) {
        if(property != null && property.getOwner() != null) {
            boolean notProtectedDirectly = checkPropertyOwnerIsNotAProtectedStereotype(property.getOwner());
            if(notProtectedDirectly && !checkRedefinedProperties(property.getRedefinedProperty())) {
                return false;
            }
            return notProtectedDirectly;
        }
        return true;
    }

    protected boolean checkPropertyOwnerIsNotAProtectedStereotype(Element propertyOwner) {
        String stereotypeID = propertyOwner.getLocalID();
        return stereotypeID == null || (!stereotypeID.equals(PluginConstant.CONFIGURED_ELEMENT_STEREOTYPE_ID) &&
                !stereotypeID.equals(PluginConstant.CHANGE_RECORD_STEREOTYPE_ID) &&
                !stereotypeID.equals(PluginConstant.REVISION_HISTORY_STEREOTYPE_ID));
    }

    protected ConfiguredElement getConfiguredElement(Element element) {
        ConfiguredElement configuredElement = getConfigurationManagementService().getConfiguredElement(element);
        if(configuredElement == null) {
            return getConfigurationManagementService().getCCZOwner(element);
        }
        return configuredElement;
    }

    protected boolean commentPermissionCheck(Comment comment) {
        return comment.getOwner() == null || !getApiDomain().hasStereotypeOrDerived(comment.getOwner(), getConfigurationManagementService().getBaseCRStereotype());
    }

    protected boolean checkRedefinedProperties(Collection<Property> redefinedBy) {
        if(redefinedBy != null) {
            for(Property p : redefinedBy) {
                if(p.getOwner() != null && !checkPropertyOwnerIsNotAProtectedStereotype(p.getOwner())) {
                    return false;
                } else if(!checkRedefinedProperties(p.getRedefinedProperty())) {
                    return false;
                }
            }
        }
        return true;
    }
}
