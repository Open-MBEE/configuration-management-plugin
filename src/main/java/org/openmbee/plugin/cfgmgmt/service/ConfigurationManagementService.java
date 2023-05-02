package org.openmbee.plugin.cfgmgmt.service;

import org.openmbee.plugin.cfgmgmt.IConfigurationManagementPlugin;
import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.*;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.integration.WssoService;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxService;
import org.openmbee.plugin.cfgmgmt.integration.twc.TWCIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.twc.TeamworkCloudService;
import org.openmbee.plugin.cfgmgmt.integration.twc.TwcRevisionService;
import org.openmbee.plugin.cfgmgmt.listeners.CMPropertyListener;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.openmbee.plugin.cfgmgmt.policy.Policies;
import org.openmbee.plugin.cfgmgmt.settings.CustomSettings;
import org.openmbee.plugin.cfgmgmt.utils.Policy;
import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.ClassTypes;
import com.nomagic.uml2.ext.jmi.helpers.CoreHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.StateMachine;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Transition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.CM_PROFILE_ID;

public class ConfigurationManagementService {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementService.class);

    private final IConfigurationManagementPlugin parentPlugin;
    private final CustomSettings customSettings = new CustomSettings(this);

    private boolean cmActive = false;
    private boolean adminMode = false;
    private boolean lifecycleStatusChanging = false;
    private String changeRecordName;

    private UIDomain uiDomain;
    private ApiDomain apiDomain;
    private LifecycleObjectDomain lifecycleObjectDomain;
    private ConfiguredElementDomain configuredElementDomain;
    private ChangeRecordDomain changeRecordDomain;

    private LifecycleObjectFactory lifecycleObjectFactory;

    private TeamworkCloudService teamworkCloudService;
    private TwcRevisionService twcRevisionService;
    private ThreeDxService threeDxService;
    private JiraService jiraService;
    private WssoService wssoService;

    private Stereotype baseCEStereotype;
    private Stereotype baseCRStereotype;
    private Stereotype cmcsStereotype;
    private Stereotype rhStereotype;
    private Stereotype tdxcsStereotype;
    private Stereotype jcsStereotype;

    private Profile cmProfile;

    private CMPropertyListener cmPropertyListener = null;

    public ConfigurationManagementService(IConfigurationManagementPlugin parentPlugin) {
        this.parentPlugin = parentPlugin;
    }

    public UIDomain getUIDomain() {
        return uiDomain;
    }

    public void setUIDomain(UIDomain uiDomain) {
        this.uiDomain = uiDomain;
    }

    public void setApiDomain(ApiDomain apiDomain) {
        this.apiDomain = apiDomain;
    }

    public ApiDomain getApiDomain() {
        return apiDomain;
    }

    protected Logger getLogger() {
        return logger; // used for unit testing
    }

    public void setLifecycleObjectDomain(LifecycleObjectDomain lifecycleObjectDomain) {
        this.lifecycleObjectDomain = lifecycleObjectDomain;
    }

    public LifecycleObjectDomain getLifecycleObjectDomain() {
        return lifecycleObjectDomain;
    }

    public void setLifecycleObjectFactory(LifecycleObjectFactory lifecycleObjectFactory) {
        this.lifecycleObjectFactory = lifecycleObjectFactory;
    }

    public LifecycleObjectFactory getLifecycleObjectFactory() {
        return lifecycleObjectFactory;
    }

    public ConfiguredElementDomain getConfiguredElementDomain() {
        return configuredElementDomain;
    }

    public void setConfiguredElementDomain(ConfiguredElementDomain configuredElementDomain) {
        this.configuredElementDomain = configuredElementDomain;
    }

    public ChangeRecordDomain getChangeRecordDomain() {
        return changeRecordDomain;
    }

    public void setChangeRecordDomain(ChangeRecordDomain changeRecordDomain) {
        this.changeRecordDomain = changeRecordDomain;
    }

    public TeamworkCloudService getTeamworkCloudService() {
        return teamworkCloudService;
    }

    public void setTeamworkCloudService(TeamworkCloudService teamworkCloudService) {
        this.teamworkCloudService = teamworkCloudService;
    }

    public TwcRevisionService getTwcRevisionService() {
        return twcRevisionService;
    }

    public void setTwcRevisionService(TwcRevisionService twcRevisionService) {
        this.twcRevisionService = twcRevisionService;
    }

    public ThreeDxService getThreeDxService() {
        return threeDxService;
    }

    public void setThreeDxService(ThreeDxService threeDxService) {
        this.threeDxService = threeDxService;
    }
    public JiraService getJiraService() {
        return jiraService;
    }

    public void setJiraService(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    public WssoService getWssoService() {
        return wssoService;
    }

    public void setWssoService(WssoService wssoService) {
        this.wssoService = wssoService;
    }

    public boolean isCmActive() {
        return cmActive;
    }

    public void updateCmActive() {
        this.cmActive = isCMProfilePresent();
    }

    public Stereotype getBaseCEStereotype() {
        return baseCEStereotype;
    }

    public void setBaseCEStereotype(Stereotype baseCEStereotype) {
        this.baseCEStereotype = baseCEStereotype;
    }

    public Stereotype getBaseCRStereotype() {
        return baseCRStereotype;
    }

    public void setBaseCRStereotype(Stereotype baseCRStereotype) {
        this.baseCRStereotype = baseCRStereotype;
    }

    public Stereotype getCmcsStereotype() {
        return cmcsStereotype;
    }

    public void setCmcsStereotype(Stereotype cmcsStereotype) {
        this.cmcsStereotype = cmcsStereotype;
    }

    public Stereotype getRhStereotype() {
        return rhStereotype;
    }

    public void setRhStereotype(Stereotype rhStereotype) {
        this.rhStereotype = rhStereotype;
    }

    public Stereotype getTdxcsStereotype() {
        return tdxcsStereotype;
    }

    public void setTdxcsStereotype(Stereotype tdxcsStereotype) {
        this.tdxcsStereotype = tdxcsStereotype;
    }

    public Stereotype getJcsStereotype() {
        return jcsStereotype;
    }

    public void setJcsStereotype(Stereotype jcsStereotype) {
        this.jcsStereotype = jcsStereotype;
    }

    public Profile getCmProfile() {
        return cmProfile;
    }

    public void setCmProfile(Profile cmProfile) {
        this.cmProfile = cmProfile;
    }

    public Property getBaseCrStatusProperty() {
        return getTwcRevisionService().getBaseCrStatusProperty();
    }

    public void setBaseCrStatusProperty(Property baseCrStatusProperty) {
        getTwcRevisionService().setBaseCrStatusProperty(baseCrStatusProperty);
    }

    public Property getBaseIsReleasedProperty() {
        return getTwcRevisionService().getBaseIsReleasedProperty();
    }

    public void setBaseIsReleasedProperty(Property baseIsReleasedProperty) {
        getTwcRevisionService().setBaseIsReleasedProperty(baseIsReleasedProperty);
    }

    public void setChangeRecordName(String changeRecordName) {
        this.changeRecordName = changeRecordName;
    }

    public String getChangeRecordName() {
        return this.changeRecordName;
    }

    protected void setAdminMode(boolean adminMode) { this.adminMode = adminMode; }

    public CustomSettings getCustomSettings() {
        return customSettings;
    }

    public CMPropertyListener getPropertyListener() {
        if (cmPropertyListener == null) {
            cmPropertyListener = new CMPropertyListener(this);
        }
        return cmPropertyListener;
    }

    public Collection<Element> getElementRelations(Element element) {
        return CoreHelper.collectRelationships(element);
    }

    //********** Stereotypes and Profiles **********

    public List<Stereotype> getAvailableCEStereotypes() {
        return getAvailableStereotypesUsingBaseStereotype(baseCEStereotype);
    }

    public List<Stereotype> getAvailableCRStereotypes() {
        return getAvailableStereotypesUsingBaseStereotype(baseCRStereotype);
    }

    protected List<Stereotype> getAvailableStereotypesUsingBaseStereotype(Stereotype baseStereotype) {
        List<Stereotype> list = new ArrayList<>();
        if (baseStereotype != null) {
            list = apiDomain.getDerivedStereotypesRecursively(baseStereotype);
            if (list.isEmpty()) {
                list.add(baseStereotype);
            }
        }
        return list;
    }

    public List<Stereotype> getApplicableStereotypes(Element element) {
        List<Stereotype> stereotypes = getAvailableCEStereotypes();
        List<Stereotype> applicableStereotypes = stereotypes.stream()
            .filter(s -> getApiDomain().canAssignStereotype(element, s))
            .collect(Collectors.toList());

        return trimDefaultStereotypes(applicableStereotypes);
    }

    public boolean isCMProfilePresent() {
        Collection<IAttachedProject> projectUsages = getApiDomain().getAllAttachedProjectsForCurrentProject();
        return projectUsages != null && projectUsages.stream()
            .anyMatch(p -> p.getProjectID().equals(PluginConstant.CONFIGURATION_MANAGEMENT_PROJECTID));
    }

    //********** Packages **********

    public boolean isElementInChangeManagementPackageRoot(NamedElement element) {
        return element.getQualifiedName().startsWith(getChangeManagementPackagePath() + PluginConstant.PACKAGE_DELIM)
            || element.getQualifiedName().equals(getChangeManagementPackagePath());
    }

    public Package getChangeManagementPackage(boolean create) {
        Project project = getApiDomain().getCurrentProject();

        if (project != null) {
            Package primaryModel = project.getPrimaryModel();
            if (primaryModel != null && isCMProfilePresent()) {
                String changeManagementPath = getChangeManagementPackagePath();
                if (changeManagementPath == null) {
                    getCustomSettings().update();
                    changeManagementPath = getChangeManagementPackagePath();
                }

                Package packageObj = getApiDomain().findRelativePackage(primaryModel, changeManagementPath);
                if (packageObj == null && create) {
                    packageObj = getApiDomain().createPackageStructureGivenPath(project, primaryModel, changeManagementPath);
                }

                return packageObj;
            }
        }
        return null;
    }

    public String getChangeManagementPackagePath() {
        return (String) getCustomSettings().get(PluginConstant.CM_PACKAGE_PATH);
    }

    public String getRevisionHistoryPackagePath() {
        return getCustomSettings().get(PluginConstant.CM_PACKAGE_PATH) + PluginConstant.PACKAGE_DELIM +
            PluginConstant.REVISION_HISTORY_PACKAGE;
    }

    public Package getRevisionHistoryPackage(boolean create) {
        return getSpecificPackageFromChangeManagement(getRevisionHistoryPackagePath(), PluginConstant.REVISION_HISTORY_PACKAGE, create);
    }

    public String getChangeRecordsPackagePath() {
        return getCustomSettings().get(PluginConstant.CM_PACKAGE_PATH) + PluginConstant.PACKAGE_DELIM + PluginConstant.CHANGE_RECORD_PACKAGE;
    }

    public Package getChangeRecordsPackage(boolean create) {
        return getSpecificPackageFromChangeManagement(getChangeRecordsPackagePath(), PluginConstant.CHANGE_RECORD_PACKAGE, create);
    }

    protected Package getSpecificPackageFromChangeManagement(String path, String name, boolean create) {
        Package changeManagementPackage = getChangeManagementPackage(create);
        if (changeManagementPackage == null) {
            return null;
        }

        Project project = getApiDomain().getCurrentProject();
        return project != null ? getApiDomain().createPackageIfNotFound(project, path, project.getPrimaryModel(), changeManagementPackage, name) : null;
    }

    //********** Change Records **********

    public boolean isChangeRecordSelected() {
        return changeRecordName != null;
    }

    public List<ChangeRecord> getChangeRecords() {
        List<ChangeRecord> changeRecords = new ArrayList<>();
        Package changeRecordsPackage = getChangeRecordsPackage(false);
        if (changeRecordsPackage == null) {
            return changeRecords;
        }

        Collection<PackageableElement> pkgContents = changeRecordsPackage.getPackagedElement();

        pkgContents.forEach(el -> {
            if (el instanceof Class) {
                changeRecords.add(getLifecycleObjectFactory().getChangeRecord(this, (Class) el));
            }
        });

        return changeRecords;
    }

    public ChangeRecord initializeChangeRecord(Class element, String id, Stereotype stereotype, Package changeRecordsPackage) {
        LifecycleStatus status = lifecycleObjectDomain.getInitialStatus(this, stereotype);
        if (status != null) {
            return initializeChangeRecord(element, id, stereotype, changeRecordsPackage, status);
        }
        return null;
    }

    public ChangeRecord initializeChangeRecord(Class element, String id, Stereotype stereotype, Package changeRecordsPackage, LifecycleStatus status) {
        getApiDomain().addStereotypeToElement(element, stereotype);

        element.setOwner(changeRecordsPackage);
        element.setName(id);
        getApiDomain().setStereotypePropertyValue(element, getBaseCRStereotype(), PluginConstant.STATUS, status.getState());

        return getLifecycleObjectFactory().getChangeRecord(this, element);
    }

    public void setChangeRecordParametersForDataSource(Element element, String source, String sourceId, String description) {
        getApiDomain().setStereotypePropertyValue(element, getBaseCRStereotype(), PluginConstant.SOURCE, source);
        getApiDomain().setStereotypePropertyValue(element, getBaseCRStereotype(), PluginConstant.SOURCE_ID, sourceId);
        getApiDomain().setStereotypePropertyValue(element, getBaseCRStereotype(), PluginConstant.DESCRIPTION, description);
    }

    public ChangeRecord getSelectedChangeRecord() {
        if (changeRecordName == null) {
            return null;
        }

        Project project = getApiDomain().getCurrentProject();
        if (project != null) {
            Class foundRecord = findRelatedChangeRecord();
            if (foundRecord != null) {
                return getChangeRecord(foundRecord);
            }
        }
        return null;
    }

    public ChangeRecord getChangeRecord(Element element) {
        if (!(element instanceof Class)) {
            getUIDomain().logDebug("Cannot cast to Class for element type " + element.getClass().getName());
            return null;
        }

        if (getApiDomain().hasStereotypeOrDerived(element, getBaseCRStereotype())) {
            return getLifecycleObjectFactory().getChangeRecord(this, (Class) element);
        }
        return null;
    }

    protected Class findRelatedChangeRecord() {
        return getApiDomain().findClassRelativeToCurrentPrimary(getChangeRecordsPackagePath() + PluginConstant.PACKAGE_DELIM + getChangeRecordName());
    }

    public void resetChangeRecordSelectionAfterStatusChange(LifecycleObject lifecycleObject, LifecycleStatus newStatus) {
        if (lifecycleObject instanceof ChangeRecord && newStatus != null && !newStatus.isReleased()) {
            // solves issue with async events happening during lifecycle status change for a change record
            setChangeRecordSelections();
            ChangeRecord currentChangeRecord = getSelectedChangeRecord();
            if (currentChangeRecord != null && !currentChangeRecord.getName().isEmpty()) {
                setCurrentChangeRecordSelection(currentChangeRecord.getName());
            }
        }
    }

    //********** Revision History **********

    public List<RevisionHistoryRecord> getAllRevisionHistoryRecords() {
        List<RevisionHistoryRecord> revisionHistoryRecords = getRevisionRecordList();
        Package revisionHistoryPackage = getRevisionHistoryPackage(false);
        if (revisionHistoryPackage == null) {
            return revisionHistoryRecords;
        }

        revisionHistoryPackage.getPackagedElement().forEach(el -> {
            if (el instanceof Class) {
                revisionHistoryRecords.add(getLifecycleObjectFactory().getRevisionHistoryRecord(this, (Class) el));
            }
        });

        return revisionHistoryRecords;
    }

    protected List<RevisionHistoryRecord> getRevisionRecordList() {
        return new ArrayList<>(); // convenience method to support unit testing
    }

    public <T> T getRevisionHistoryPropertyFirstValueGivenType(Element element, String property, java.lang.Class<T> type) {
        return getApiDomain().getStereotypePropertyFirstValueGivenType(element, getRhStereotype(), property, type);
    }

    public String getRevisionHistoryPropertyFirstValueAsString(Element element, String property) {
        return getApiDomain().getStereotypePropertyFirstValueAsString(element, getRhStereotype(), property);
    }

    //********** Switches **********

    public boolean getAutomateReleaseSwitch() {
        return getCustomSettings().get(PluginConstant.AUTOMATE_RELEASE) != null && (Boolean) getCustomSettings().get(PluginConstant.AUTOMATE_RELEASE);
    }

    public boolean getEnforceActiveCRSwitch() {
        return getCustomSettings().get(PluginConstant.ENFORCE_ACTIVE_CR) != null && (Boolean) getCustomSettings().get(PluginConstant.ENFORCE_ACTIVE_CR);
    }

    public boolean getDiagramAdornmentSwitch() {
        return getCustomSettings().get(PluginConstant.CM_DIAGRAM_ADORNMENT) != null && (Boolean) getCustomSettings().get(PluginConstant.CM_DIAGRAM_ADORNMENT);
    }

    //********** Policies **********

    public Policy getAdminModePolicy() {
        Class o = (Class) getCustomSettings().get(PluginConstant.ADMIN_MODE);
        if (o == null) {
            o = findTripleAPolicy(); // used to enable unit testing
        }

        return getLifecycleObjectFactory().getPolicy(this, o);
    }

    protected Class findTripleAPolicy() {
        return apiDomain.findInProject(apiDomain.getCurrentProject(), Policies.MBSE_AAA_POLICY_PATH);
    }

    //********** Admin mode **********

    public void disableAdminMode() {
        setAdminMode(false);
    }

    public boolean enableAdminMode() {
        Policy policy = getAdminModePolicy();
        if (policy == null) {
            return false;
        }

        if (!userHasPrivileges(policy.getRoles())) {
            return false;
        }
        setAdminMode(true);
        return true;
    }

    public boolean getAdminMode() {
        return adminMode;
    }

    //********** Roles **********

    public boolean hasAnyRole(List<String> roles) throws TWCIntegrationException {
        return getTeamworkCloudService().hasRole(getApiDomain(), roles); // used to allow unit testing
    }

    public boolean userHasPrivileges(List<String> roles) {
        if (roles != null && !roles.isEmpty()) {
            boolean hasRole;
            try {
                hasRole = hasAnyRole(roles);
            } catch (TWCIntegrationException e) {
                getUIDomain().logError(getLogger(), ExceptionConstants.INDETERMINATE_TWC_ROLES, e);
                return false;
            }
            if (!hasRole) {
                getUIDomain().logError(String.format(ExceptionConstants.INSUFFICIENT_PRIVILEGES, String.join(PluginConstant.COMMA, roles)));
            }
            return hasRole;
        }
        return true;
    }

    //********** ConfiguredElements **********

    public ConfiguredElement getCCZOwner(Element element) {
        Element parent = element.getOwner();
        if (parent == null) {
            return null;
        }

        if (isConfigured(parent)) {
            return getLifecycleObjectFactory().getConfiguredElement(this, parent);
        }

        return getCCZOwner(parent);
    }

    public ConfiguredElement configureElement(Element element, String id, Stereotype stereotype) {
        return getConfiguredElementDomain().configure(this, element, id, stereotype);
    }

    public ConfiguredElement getConfiguredElement(Element element) {
        if (isConfigured(element)) {
            return getLifecycleObjectFactory().getConfiguredElement(this, element);
        }
        return null;
    }

    public boolean isConfigured(Element element) {
        return getConfiguredElementDomain().isConfigured(element, this);
    }


    public boolean isInReadOnlyCCZ(Element element) {
        ConfiguredElement cczOwner = getCCZOwner(element);
        return (cczOwner != null && cczOwner.isReadOnly());
    }

    public boolean isInReleasedCCZ(Element element) {
        ConfiguredElement cczOwner = getCCZOwner(element);
        return (cczOwner != null && cczOwner.isReleased());
    }

    public List<ConfiguredElement> getOwnedConfiguredElements(Element element) {
        List<ConfiguredElement> configuredElements = new ArrayList<>();
        Collection<Element> ownedElement = element.getOwnedElement();

        ownedElement.forEach(e -> {
            ConfiguredElement configuredElement = getConfiguredElement(e);
            if (configuredElement != null) {
                configuredElements.add(configuredElement);
            } else {
                configuredElements.addAll(getOwnedConfiguredElements(e));
            }
        });

        return configuredElements;
    }

    public Collection<NamedElement> getSelectionCandidatesForConfiguration() {
        final Collection<NamedElement> elements = new ArrayList<>();

        if (getApiDomain().getCurrentProject() != null) {
            final Collection<Element> candidates = getApiDomain().getAllElementsInCurrentProject();
            if (candidates != null) {
                candidates.forEach(element -> {
                    if (configuredElementDomain.canBeConfigured(element, this)) {
                        elements.add((NamedElement) element);
                    }
                });
            }
        }
        return elements;
    }

    public Collection<NamedElement> getSelectionCandidatesForRevision() {
        final Collection<NamedElement> elements = new ArrayList<>();

        if (getApiDomain().getCurrentProject() != null) {
            final Collection<Element> candidates = getApiDomain().getAllElementsInCurrentProject();
            if (candidates != null) {
                candidates.forEach(element -> {
                    if (getApiDomain().isElementInEditableState(element)) {
                        ConfiguredElement configured = getConfiguredElement(element);
                        if (configured != null && configured.hasStatus() && configured.isReleased() && configured.isCommitted()) {
                            elements.add((NamedElement) element);
                        }
                    }
                });
            }
        }
        return elements;
    }

    public List<java.lang.Class> getSubtypesFromNamedElement() {
        return ClassTypes.getSubtypes(NamedElement.class);
    }

    //********** Lifecycles **********

    public Lifecycle getLifecycle(Stereotype stereotype) {
        return getLifecycleObjectDomain().getLifecycle(this, stereotype);
    }

    public Lifecycle getLifecycle(StateMachine stateMachine) {
        return getLifecycleObjectFactory().getLifecycle(this, stateMachine);
    }

    public List<Stereotype> getCustomChangeRecordStereotypes(String managedBy) {
        List<Stereotype> stereotypes = getAvailableCRStereotypes().stream().filter(s -> {
            Object value = getApiDomain().getDefaultValue(s, PluginConstant.MANAGED_BY);
            return value instanceof EnumerationLiteral && ((EnumerationLiteral) value).getName().equals(managedBy);
        }).collect(Collectors.toList());

        return trimDefaultStereotypes(stereotypes);
    }

    protected List<Stereotype> trimDefaultStereotypes(List<Stereotype> applicableStereos) {
        // IF MORE THAN ONE LEFT, ELIMINATE DEFAULT ONES
        if (applicableStereos.size() > 1) {
            applicableStereos = applicableStereos.stream()
                    .filter(s -> s.getProfile() != null && !s.getProfile().getLocalID().equals(CM_PROFILE_ID))
                    .collect(Collectors.toList());
        }
        return applicableStereos;
    }

    public Stereotype userChoosesDesiredStereotype(List<Stereotype> stereotypes, String prompt, String promptTitle) {
        if (stereotypes == null || stereotypes.isEmpty()) {
            getUIDomain().log("Relevant stereotype not found");
            return null;
        }

        if (stereotypes.size() == 1) {
            return stereotypes.get(0);
        } else {
            Object[] options = stereotypes.stream().map(NamedElement::getName).toArray();
            int selection = getUIDomain().promptForSelection(prompt, promptTitle, options);
            if (selection == JOptionPane.CLOSED_OPTION) {
                getUIDomain().log("User cancelled action");
                return null;
            }
            return stereotypes.get(selection);
        }
    }

    public LifecycleTransition getLifecycleTransition(Transition transition) {
        return getLifecycleObjectFactory().getLifecycleTransition(this, transition);
    }

    public LifecycleStatus getLifecycleStatus(State status) {
        return getLifecycleObjectFactory().getLifecycleStatus(this, status);
    }

    public boolean isLifecycleStatusChanging() {
        return lifecycleStatusChanging;
    }

    public void setLifecycleStatusChanging(boolean lifecycleStatusChanging) {
        this.lifecycleStatusChanging = lifecycleStatusChanging;
    }

    //********** Parent plugin actions **********

    public void updateCRStatus() {
        parentPlugin.updateCRStatus();
    }

    public void setChangeRecordSelections() {
        parentPlugin.getSelectChangeRecordAction().setChangeRecordSelections();
    }

    public void clearChangeRecordSelectionList() {
        parentPlugin.getSelectChangeRecordAction().clearList();
    }

    public void setCurrentChangeRecordSelection(String changeRecordName) {
        // only use for manual reset of selected active change record
        parentPlugin.getSelectChangeRecordAction().setSelectedChangeRecord(changeRecordName);
    }
}
