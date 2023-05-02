package org.openmbee.plugin.cfgmgmt.domain;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.utils.DisplayNameLookup;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfiguredElementDomain extends LifecycleObjectDomain {
    private static final Logger logger = LoggerFactory.getLogger(ConfiguredElementDomain.class);
    private DisplayNameLookup displayNameLookup;

    public ConfiguredElementDomain(LifecycleObjectFactory lifeCycleObjectFactory, ApiDomain apiDomain, UIDomain uiDomain) {
        super(lifeCycleObjectFactory, apiDomain, uiDomain);
        displayNameLookup = new DisplayNameLookup(apiDomain);
    }

    protected Logger getLogger() {
        return logger;
    }

    public ConfiguredElement configure(ConfigurationManagementService configurationManagementService, Element element,
            String id, Stereotype stereo) {
        if(stereo == null) {
            return null;
        }

        ConfiguredElement configuredElement = null;
        State initialState = null;
        LifecycleStatus initialStatus = getInitialStatus(configurationManagementService, stereo);
        if(initialStatus != null) {
            initialState = initialStatus.getState();
        }

        if (element != null && id != null && configurationManagementService != null && initialState != null) {
            Stereotype baseStereo = configurationManagementService.getBaseCEStereotype();
            String timeStamp = currentTime();
            String userName = getApiDomain().getLoggedOnUser();
            getApiDomain().addStereotypeToElement(element, stereo);
            getApiDomain().setStereotypePropertyValue(element, baseStereo, PluginConstant.REVISION_CREATION_DATE, timeStamp);
            getApiDomain().setStereotypePropertyValue(element, baseStereo, PluginConstant.REVISION_CREATOR_ID, userName);
            getApiDomain().setStereotypePropertyValue(element, baseStereo, PluginConstant.REVISION, "-");
            getApiDomain().setStereotypePropertyValue(element, baseStereo, PluginConstant.STATUS, initialState);
            getApiDomain().setStereotypePropertyValue(element, baseStereo, PluginConstant.DESCRIPTION, PluginConstant.DESCRIPTION_NEW_REVISION);
            getApiDomain().setStereotypePropertyValue(element, baseStereo, PluginConstant.ID, id);
            ChangeRecord changeRecord = configurationManagementService.getSelectedChangeRecord();
            if (changeRecord != null) {
                getApiDomain().setStereotypePropertyValue(element, baseStereo, PluginConstant.REVISION_RELEASE_AUTHORITY,
                    configurationManagementService.getSelectedChangeRecord().getElement());
            }
            configuredElement = getLifeCycleObjectFactory().getConfiguredElement(configurationManagementService, element);
            if (changeRecord != null) {
                changeRecord.addAffectedElement(configuredElement, PluginConstant.CONFIGURING_ACTION);
            }
        }
        return configuredElement;
    }

    protected String currentTime() {
        return ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString(); // used for unit testing
    }

    public boolean isConfigured(Element element, ConfigurationManagementService configurationManagementService) {
        return getApiDomain().hasStereotypeOrDerived(element, configurationManagementService.getBaseCEStereotype());
    }

    public boolean checkConfiguredElementPermissions(ConfigurationManagementService configurationManagementService,
            Stereotype stereo, ConfiguredElement cczOwner) {
        String action = "creation";

        try {
            canUserPerformAction(configurationManagementService, stereo, action);
        } catch (Exception e) {
            getUIDomain().logError(e.getMessage());
            return false;
        }

        // Checking if the status of the CCZ owner is compatible
        if (cczOwner != null) {
            int cczOwnerMaturityRating = cczOwner.getStatusMaturityRating();
            LifecycleStatus initialStatus = getInitialStatus(configurationManagementService, stereo);
            int initialMaturityRating = Integer.MIN_VALUE;
            if(initialStatus != null) {
                initialMaturityRating = initialStatus.getMaturityRating();
            }
            if (initialMaturityRating < cczOwnerMaturityRating) {
                getUIDomain().logError(String.format(ExceptionConstants.INCOMPATIBLE_CCZ_OWNER_STATUS, cczOwner.getQualifiedName(),
                    cczOwner.getID(), cczOwner.getStatus()));
                return false;
            }
        }
        return true;
    }

    public boolean canBeConfigured(Object elementObject, ConfigurationManagementService configurationManagementService) {
        if (!(elementObject instanceof NamedElement)) {
            getUIDomain().logDebug(ExceptionConstants.NOT_NAMED_ELEMENT);
            return false;
        }

        NamedElement element = (NamedElement) elementObject;

        // this disables the configure option when the element is in the change management package
        if (configurationManagementService.isElementInChangeManagementPackageRoot(element)) {
            getUIDomain().logDebug(ExceptionConstants.ELEMENT_ALREADY_PRESENT);
            return false;
        }

        // this disables the action when the element is not editable, not locked and not new
        if (!getApiDomain().isElementInEditableState(element)) {
            getUIDomain().logDebug(ExceptionConstants.ELEMENT_NOT_EDITABLE);
            return false;
        }

        // this disables the contextual menu when the CM profile is not present
        if (!configurationManagementService.isCmActive()) {
            getUIDomain().logDebug(ExceptionConstants.CM_PROFILE_NOT_ACTIVE);
            return false;
        }

        // this disables the contextual menu when no change record is selected
        if (!configurationManagementService.isChangeRecordSelected()) {
            getUIDomain().logDebug(ExceptionConstants.CR_NOT_SELECTED);
            return false;
        }

        ChangeRecord changeRecord = configurationManagementService.getSelectedChangeRecord();
        // this disables the contextual menu when the selected CR is not at an expendable status
        if(changeRecord == null) {
            return false;
        } else if (changeRecord.hasStatus() && !changeRecord.isExpandable()) {
            getUIDomain().logDebug(ExceptionConstants.CR_NOT_EXPENDABLE);
            return false;
        }


        // this disables the contextual menu if there are no available stereotypes for this element type
        List<Stereotype> stereos = configurationManagementService.getAvailableCEStereotypes();
        if (stereos.stream().noneMatch(s -> getApiDomain().canAssignStereotype(element, s))) {
            getUIDomain().logDebug(ExceptionConstants.NO_AVAILABLE_STEREOTYPES);
            return false;
        }

        // this disables the contextual menu if the element is already configured
        boolean isAlreadyConfigured = isConfigured(element, configurationManagementService);
        if (isAlreadyConfigured) {
            getUIDomain().logDebug(ExceptionConstants.CE_CONFIGURED);
        }
        return !isAlreadyConfigured;
    }

    public void setReleaseAttributes(ConfiguredElement configuredElement) {
        String userName = getApiDomain().getLoggedOnUser();
        getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.REVISION_RELEASER_ID, userName);
        String timeStamp = currentTime();
        getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.REVISION_RELEASE_DATE, timeStamp);
        // set isCommitted to false
        getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.IS_COMMITTED, false);
        getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.REVISION_RELEASE_AUTHORITY,
            configuredElement.getConfigurationManagementService().getSelectedChangeRecord().getElement());

    }

    public ChangeRecord getReleaseAuthority(ConfiguredElement configuredElement) {
        List<Object> list = getApiDomain().getStereotypePropertyValue(configuredElement.getElement(),
            configuredElement.getBaseStereotype(), PluginConstant.REVISION_RELEASE_AUTHORITY);
        if (list == null) {
            return null;
        }

        if (list.size() != 1 || !(list.get(0) instanceof Class)) {
            // this code is a safeguard for cases where the tag is not set because the element was created using an older version of the plugin
            // it applies the tag to the selected cr if that cr contains the element in the affected elements tag
            ChangeRecord changeRecord = configuredElement.getConfigurationManagementService().getSelectedChangeRecord();
            if (changeRecord != null && changeRecord.getAffectedElements().contains(configuredElement)) {
                getApiDomain().setStereotypePropertyValue(configuredElement.getElement(),
                    configuredElement.getBaseStereotype(), PluginConstant.REVISION_RELEASE_AUTHORITY, changeRecord.getElement());
                return changeRecord;
            }

            if (list.size() > 1) {
                getUIDomain().logDebug(ExceptionConstants.MULTIPLE_VALUES_PRESENT);
            }

            if (!list.isEmpty() && !(list.get(0) instanceof Class)) {
                getUIDomain().logDebug(ExceptionConstants.CE_NOT_CLASS_OBJECT);
            }

            return null;
        }

        return getLifeCycleObjectFactory().getChangeRecord(configuredElement.getConfigurationManagementService(),
            (Class) list.get(0));
    }

    public void setIsCommitted(ConfiguredElement configuredElement, boolean isCommitted) {
        getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.IS_COMMITTED, isCommitted);
    }

    public boolean isCommitted(ConfiguredElement configuredElement) {
        List<Object> value = getApiDomain().getStereotypePropertyValue(configuredElement.getElement(),
            configuredElement.getBaseStereotype(), PluginConstant.IS_COMMITTED);
        return value != null && !value.isEmpty() && value.contains(true);
    }

    public String getID(ConfiguredElement configuredElement) {
        return getApiDomain().getStereotypePropertyFirstValueAsString(configuredElement.getElement(),
            configuredElement.getBaseStereotype(), PluginConstant.ID);
    }

    public boolean validateProposedMaturityRatingWRTCczOwner(ConfiguredElement configuredElement, int proposedMaturityRating) {
        ConfiguredElement cczOwner = configuredElement.getCCZOwner();
        if (cczOwner != null && proposedMaturityRating < cczOwner.getStatusMaturityRating()) {
            getUIDomain().logError(String.format(ExceptionConstants.INVALID_MATURITY_RATING, cczOwner.getQualifiedName(), cczOwner.getID()));
            return false;
        }
        return true;
    }

    public boolean validateProposedMaturityRatingWRTOwned(ConfiguredElement configuredElement, int proposedMaturityRating) {
        List<ConfiguredElement> ownedElements = configuredElement.getOwnedConfiguredElements();
        if (ownedElements != null && !ownedElements.isEmpty()) {
            List<ConfiguredElement> elementsWithInvalidStatus = new ArrayList<>();
            String errorMessage;
            ChangeRecord changeRecord = configuredElement.getConfigurationManagementService().getSelectedChangeRecord();
            for (ConfiguredElement configuredElement1 : ownedElements) {
                if (proposedMaturityRating > configuredElement1.getStatusMaturityRating()) {
                    ChangeRecord releaseAuthority = configuredElement1.getReleaseAuthority();
                    if(changeRecord == null || !changeRecord.equals(releaseAuthority) || changeRecord.isReleased() ||
                            !configuredElement.getConfigurationManagementService().getAutomateReleaseSwitch()) {
                        elementsWithInvalidStatus.add(configuredElement1);
                    }
                }
            }
            if (!elementsWithInvalidStatus.isEmpty()) {
                errorMessage = String.format(ExceptionConstants.PROMOTIONS_REQUIRED_FIRST,
                    elementsWithInvalidStatus.stream()
                        .flatMap(e -> Stream.of(String.format(ExceptionConstants.PREPARE_STRING_LIST_FOR_EXCEPTION, e.getQualifiedName(), e.getID())))
                        .collect(Collectors.joining()));
                getUIDomain().logError(getLogger(), errorMessage);
                return false;
            }
        }
        return true;
    }

    public String getDisplayName(ConfiguredElement configuredElement, String originalName) {
        List<String> value = getApiDomain().getStereotypePropertyValueAsString(configuredElement.getElement(),
            configuredElement.getBaseStereotype(), PluginConstant.NAME_PATTERN);
        if (value == null || value.isEmpty()) {
            return originalName;
        }

        String displayedName = value.get(0);
        if (displayedName != null && !displayedName.isEmpty()) {
            return createFormattedDisplayName(configuredElement.getElement(), displayedName, originalName);
        } else {
            return originalName;
        }
    }

    public String createFormattedDisplayName(Element element, String displayName, String originalName) {
        displayNameLookup.setElement(element);
        displayNameLookup.setOriginalName(originalName);
        String formattedName = new StringSubstitutor(displayNameLookup).replace(displayName);
        return formattedName != null ? formattedName : PluginConstant.EMPTY_STRING;
    }

    public boolean setStatusToInWork(ConfiguredElement configuredElement) {
        LifecycleStatus lifecycleStatus = configuredElement.getInitialStatus();
        if(lifecycleStatus == null) {
            return false;
        }
        getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.STATUS, lifecycleStatus.getState());
        return true;
    }

    public String getRevision(ConfiguredElement configuredElement) {
        return getApiDomain().getStereotypePropertyFirstValueAsString(configuredElement.getElement(),
            configuredElement.getBaseStereotype(), PluginConstant.REVISION);
    }

    public String getRevisionCreationDate(ConfiguredElement configuredElement) {
        return getApiDomain().getStereotypePropertyFirstValueAsString(configuredElement.getElement(),
                configuredElement.getBaseStereotype(), PluginConstant.REVISION_CREATION_DATE);
    }

    public String getRevisionReleaseDate(ConfiguredElement configuredElement) {
        return getApiDomain().getStereotypePropertyFirstValueAsString(configuredElement.getElement(),
                configuredElement.getBaseStereotype(), PluginConstant.REVISION_RELEASE_DATE);
    }

    public String iterateRevision(String rev, int index) {
        if (rev != null && !rev.isEmpty() && index >= 0) {
            char lastChar = rev.charAt(index);
            StringBuilder next;

            if (lastChar == '-' || (lastChar >= 'A' && lastChar <= 'Z')) {
                if (lastChar == '-') {
                    next = new StringBuilder(rev);
                    next.setCharAt(index, 'A');
                } else if (lastChar == 'Z') {
                    if (index == 0) {
                        next = new StringBuilder("A" + rev);
                        next.setCharAt(index + 1, 'A');
                    } else {
                        next = new StringBuilder(rev);
                        next.setCharAt(index, 'A');
                        return iterateRevision(next.toString(), index - 1);
                    }
                } else {
                    next = new StringBuilder(rev);
                    next.setCharAt(index, (char) (lastChar + 1));
                }
                return next.toString();
            }
        }
        return null;
    }

    public String iterateRevision(ConfiguredElement configuredElement) {
        String rev = getRevision(configuredElement);
        if (rev != null) {
            String newRev = iterateRevision(rev, rev.length() - 1);
            getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
                PluginConstant.REVISION, newRev);
            return newRev;
        }
        return null;
    }

    public Class createRevisionHistoryRecord(ConfiguredElement configuredElement) {
        Project project = getApiDomain().getProject(configuredElement.getElement());
        IProject iProject = getApiDomain().getIProject(configuredElement.getElement());
        // get history stereotype
        Stereotype historyStereo = getApiDomain().findInProject(project, PluginConstant.REVISION_HISTORY_STEREOTYPE_PATH);
        // get history record owner
        Package revHist = configuredElement.getConfigurationManagementService().getRevisionHistoryPackage(true);
        // get the Id and revision
        String id = getID(configuredElement);
        String rev = getRevision(configuredElement);
        // get the model branch and version
        EsiUtils.EsiBranchInfo branch = getApiDomain().getCurrentBranch(iProject);
        String branchName = null; // TODO is it a problem if this remains null later?
        if (branch != null) {
            branchName = branch.getName();
        }
        long latestRev = getApiDomain().getCurrentBranchLatestRevision(iProject);
        // Retrieve the history data from current revision
        String creationDate = getApiDomain().getStereotypePropertyFirstValueAsString(configuredElement.getElement(),
            configuredElement.getBaseStereotype(), PluginConstant.REVISION_CREATION_DATE);
        String creatorId = getApiDomain().getStereotypePropertyFirstValueAsString(configuredElement.getElement(),
            configuredElement.getBaseStereotype(), PluginConstant.REVISION_CREATOR_ID);
        String releaseDate = getApiDomain().getStereotypePropertyFirstValueAsString(configuredElement.getElement(),
            configuredElement.getBaseStereotype(), PluginConstant.REVISION_RELEASE_DATE);
        String releaserId = getApiDomain().getStereotypePropertyFirstValueAsString(configuredElement.getElement(),
            configuredElement.getBaseStereotype(), PluginConstant.REVISION_RELEASER_ID);
        ChangeRecord releaseAuthority = getReleaseAuthority(configuredElement);
        Element releaseAuthorityElement = releaseAuthority != null ? releaseAuthority.getElement() : null;
        String description = getApiDomain().getStereotypePropertyFirstValueAsString(configuredElement.getElement(),
            configuredElement.getBaseStereotype(), PluginConstant.DESCRIPTION);

        // perform a simple sanity check on all the variables we'll need, log any errors that are found, only attempt
        // to create a new Class when there are no errors found
        String errorMessage = revisionHistoryRecordCreationSanityCheck(historyStereo, revHist, id, rev, creationDate, creatorId, description,
            releaseDate, releaserId, releaseAuthorityElement, branchName, latestRev);
        if (errorMessage.isEmpty()) {
            Class clazz = getApiDomain().createClassInstance(project);
            // add history stereotype, record owner, id, and revision
            getApiDomain().addStereotypeToElement(clazz, historyStereo);
            clazz.setOwner(revHist);
            clazz.setName(id + " " + rev);
            // set various values for class's tags
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.REVISION_CREATION_DATE, creationDate);
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.REVISION_CREATOR_ID, creatorId);
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.REVISION, rev);
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.DESCRIPTION, description);
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.REVISION_RELEASE_DATE, releaseDate);
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.REVISION_RELEASER_ID, releaserId);
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.REVISION_RELEASE_AUTHORITY, releaseAuthorityElement);
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.MODEL_VERSION, latestRev);
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.MODEL_BRANCH, branchName);
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.ID, id);
            getApiDomain().setStereotypePropertyValue(clazz, historyStereo, PluginConstant.CONFIGURED_ELEMENT, configuredElement.getElement());
            return clazz;
        } else {
            getUIDomain().logError(errorMessage);
            return null;
        }
    }

    protected String revisionHistoryRecordCreationSanityCheck(Stereotype historyStereo, Package revHist, String id,
                      String rev, String creationDate, String creatorId, String description, String releaseDate,
                      String releaserId, Element releaseAuthority, String branchName, long latestRev) {
        StringBuilder messageBuilder = new StringBuilder();
        if (historyStereo == null) {
            messageBuilder.append(ExceptionConstants.NULL_STEREOTYPE_WHILE_CREATING_HISTORY_RECORD);
        }
        if (revHist == null) {
            messageBuilder.append(ExceptionConstants.NULL_PACKAGE_WHILE_CREATING_HISTORY_RECORD);
        }
        if (id == null) {
            messageBuilder.append(ExceptionConstants.NULL_ID_WHILE_CREATING_HISTORY_RECORD);
        }
        if (rev == null) {
            messageBuilder.append(ExceptionConstants.NULL_REVISION_WHILE_CREATING_HISTORY_RECORD);
        }
        if (creationDate == null) {
            messageBuilder.append(ExceptionConstants.NULL_CREATION_DATE_WHILE_CREATING_HISTORY_RECORD);
        }
        if (creatorId == null) {
            messageBuilder.append(ExceptionConstants.NULL_CREATOR_ID_WHILE_CREATING_HISTORY_RECORD);
        }
        if (description == null) {
            messageBuilder.append(ExceptionConstants.NULL_DESCRIPTION_WHILE_CREATING_HISTORY_RECORD);
        }
        if (releaseDate == null) {
            messageBuilder.append(ExceptionConstants.NULL_RELEASE_DATE_WHILE_CREATING_HISTORY_RECORD);
        }
        if (releaserId == null) {
            messageBuilder.append(ExceptionConstants.NULL_RELEASER_ID_WHILE_CREATING_HISTORY_RECORD);
        }
        if(releaseAuthority == null) {
            messageBuilder.append(ExceptionConstants.NULL_RELEASE_AUTHORITY_WHILE_CREATING_HISTORY_RECORD);
        }
        if (branchName == null) {
            messageBuilder.append(ExceptionConstants.NULL_BRANCH_NAME_WHILE_CREATING_HISTORY_RECORD);
        }
        if (latestRev < 0) {
            messageBuilder.append(ExceptionConstants.BAD_LATEST_REVISION_WHILE_CREATING_HISTORY_RECORD);
        }
        if (messageBuilder.length() > 0) {
            messageBuilder.insert(0, ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX);
        }
        if (messageBuilder.toString().endsWith(", ")) {
            return messageBuilder.substring(0, messageBuilder.length() - 2) + ".";
        }
        return messageBuilder.toString();
    }

    public void attachRevisionHistoryRecord(ConfiguredElement configuredElement, Class clazz) {
        getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.REVISION_HISTORY, clazz, true);
    }

    public void resetRevisionAttributes(ConfiguredElement configuredElement) {
        String newRevTimeStamp = currentTime();
        getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.REVISION_CREATION_DATE, newRevTimeStamp);
        String userName = getApiDomain().getLoggedOnUser();
        getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.REVISION_CREATOR_ID, userName);
        getApiDomain().clearStereotypeProperty(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.REVISION_RELEASE_DATE);
        getApiDomain().clearStereotypeProperty(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.REVISION_RELEASER_ID);
        getApiDomain().setStereotypePropertyValue(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.REVISION_RELEASE_AUTHORITY,
            configuredElement.getConfigurationManagementService().getSelectedChangeRecord().getElement());
        // remove isCommitted value
        getApiDomain().clearStereotypeProperty(configuredElement.getElement(), configuredElement.getBaseStereotype(),
            PluginConstant.IS_COMMITTED);
    }

    protected List<ChangeRecord> getAllRecordsAffectingConfiguredElement(ConfiguredElement configuredElement,
            ConfigurationManagementService configurationManagementService) {
        return configurationManagementService.getChangeRecords().stream()
                .filter(cr -> cr.affectsGivenConfiguredElement(configuredElement)).collect(Collectors.toList());
    }

    public List<ChangeRecord> getChangeRecordsAffectingConfiguredElement(ConfiguredElement configuredElement,
            ConfigurationManagementService configurationManagementService) {
        List<ChangeRecord> affectingChangeRecords = getAllRecordsAffectingConfiguredElement(configuredElement, configurationManagementService);
        if (affectingChangeRecords.isEmpty()) { // this is a significant history error
            getUIDomain().logErrorAndShowMessage(getLogger(),
                    String.format(ExceptionConstants.NO_CHANGE_RECORDS_FOUND, configuredElement.getName()),
                    ExceptionConstants.NO_CHANGE_RECORDS_FOUND_TITLE);
        }
        return affectingChangeRecords;
    }

    public List<ChangeRecord> getRelevantChangeRecords(ConfiguredElement configuredElement,
            ConfigurationManagementService configurationManagementService) {
        List<ChangeRecord> relevantChangeRecords = new ArrayList<>();
        getChangeRecordsAffectingConfiguredElement(configuredElement, configurationManagementService).forEach(cr -> {
            if (!relevantChangeRecords.contains(cr)) {
                relevantChangeRecords.add(cr);
            }
        });
        return relevantChangeRecords;
    }

    public ConfiguredElement getConfiguredElementUsingId(String id, ConfigurationManagementService configurationManagementService) {
        if (id != null) {
            Element element = getApiDomain().getElementUsingId(id);
            if (element != null) {
                return configurationManagementService.getConfiguredElement(element);
            }
        }
        return null;
    }
}
