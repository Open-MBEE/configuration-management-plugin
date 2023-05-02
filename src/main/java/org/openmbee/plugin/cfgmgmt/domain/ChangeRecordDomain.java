package org.openmbee.plugin.cfgmgmt.domain;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.utils.ChangeRecordComparatorForHistory;
import org.openmbee.plugin.cfgmgmt.utils.RevisionRecordComparatorForRevision;
import org.openmbee.plugin.cfgmgmt.view.ElementHistoryRowView;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChangeRecordDomain extends LifecycleObjectDomain {
    private static final Logger logger = LoggerFactory.getLogger(ChangeRecordDomain.class);

    public ChangeRecordDomain(LifecycleObjectFactory lifecycleObjectFactory, ApiDomain apiDomain, UIDomain uiDomain) {
        super(lifecycleObjectFactory, apiDomain, uiDomain);
    }

    protected Logger getLogger() {
        return logger;
    }

    public List<ConfiguredElement> getAffectedElements(Element element, Stereotype stereotype,
            ConfigurationManagementService configurationManagementService) {
        List<Element> affectedElements = (List) getApiDomain().getStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS);
        if (affectedElements != null) {
            return affectedElements.stream()
                    .flatMap(el -> Stream.of(configurationManagementService.getLifecycleObjectFactory().getConfiguredElement(
                            configurationManagementService, el))).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public Map<ConfiguredElement, LifecycleStatus> getReleaseTransitionsForElements(ChangeRecord changeRecord,
            List<ConfiguredElement> elements) {
        Map<ConfiguredElement, LifecycleStatus> transitionMap = new HashMap<>();
        if(changeRecord != null && elements != null) {
            for (ConfiguredElement element : elements) {
                List<LifecycleTransition> transitions = element.getTransitions();
                LifecycleTransition releasingTransition = transitions.stream()
                        .filter(t -> t.getTargetStatus().isReleased())
                        .findAny()
                        .orElse(null);
                if (releasingTransition == null) {
                    // If there is no releasing transition, it is an invalid ConfiguredElement Lifecycle
                    getUIDomain().logError(String.format(ExceptionConstants.NO_RELEASING_TRANSITION_FOUND,
                            element.getQualifiedName(), element.getID()));
                    return Map.of();
                }

                if(!element.canBePromoted(releasingTransition, changeRecord)) {
                    return Map.of();// if the canBePromoted method returns false, an error is logged there
                }

                transitionMap.put(element, releasingTransition.getTargetStatus());
            }
        }

        return transitionMap;
    }

    public boolean transitionElements(ChangeRecord changeRecord, Map<ConfiguredElement, LifecycleStatus> transitionMap,
            ConfigurationManagementService configurationManagementService) {
        AtomicBoolean allTransitioned = new AtomicBoolean(true);
        for (Map.Entry<ConfiguredElement, LifecycleStatus> entry : transitionMap.entrySet()) {
            ConfiguredElement element = entry.getKey();
            LifecycleStatus status = entry.getValue();
            try {
                configurationManagementService.setLifecycleStatusChanging(false);
                Optional<LifecycleStatus> oldStatus = element.getStatus();
                if (oldStatus.isEmpty() || !element.changeStatus(status, changeRecord)) {
                    allTransitioned.set(false);
                    break;
                }
                addAffectedElement(changeRecord, element, String.format(ExceptionConstants.STATUS_CHANGE, oldStatus.get().getName(), status.getName()));
            } finally {
                configurationManagementService.setLifecycleStatusChanging(true);
            }
        }
        if (!allTransitioned.get()) {
            getUIDomain().logError(ExceptionConstants.CHANGE_IN_INCONSISTENT_STATE);
        }
        return allTransitioned.get();
    }

    public void addAffectedElement(ChangeRecord changeRecord, ConfiguredElement configuredElement, String changeText) {
        String timeStamp = currentTime();
        String userName = getApiDomain().getLoggedOnUser();

        String changeDescription = String.format(PluginConstant.CHANGE_DESCRIPTION_FORMAT, timeStamp, userName, configuredElement.getID(), changeText);

        List<Object> affectedElements = getApiDomain().getStereotypePropertyValue(changeRecord.getElement(), changeRecord.getBaseStereotype(),
                PluginConstant.AFFECTED_ELEMENTS);
        if (affectedElements == null || !affectedElements.contains(configuredElement.getElement())) {
            getApiDomain().setStereotypePropertyValue(changeRecord.getElement(), changeRecord.getBaseStereotype(),
                    PluginConstant.AFFECTED_ELEMENTS, configuredElement.getElement(), true);
        }

        String doc = getApiDomain().getComment(changeRecord.getElement());
        if (doc != null && !doc.isEmpty()) {
            doc += "\n" + changeDescription;
        } else {
            doc = changeDescription;
        }

        getApiDomain().setComment(changeRecord.getElement(), doc);
    }

    protected String currentTime() {
        return ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString(); // used for unit testing
    }

    public boolean areAllElementsReadyForRelease(List<ConfiguredElement> elements) {
        return reportOffendingElements(elements.stream().filter(e -> !e.isReadyForRelease()).collect(Collectors.toList()),
                ExceptionConstants.AFFECTED_NOT_READY_FOR_RELEASE_PREFIX);
    }

    public boolean areAllElementsProperlyMature(List<ConfiguredElement> elements, int futureMaturityRating,
            ChangeRecord changeRecord) {
        return reportOffendingElements(elements.stream().filter(e -> e.hasStatus()
                && e.getStatusMaturityRating() < futureMaturityRating && changeRecord.equals(e.getReleaseAuthority()))
                .collect(Collectors.toList()), ExceptionConstants.AFFECTED_NEED_PROMOTION_PREFIX);
    }

    public boolean reportOffendingElements(List<ConfiguredElement> offendingElements, String errorMessage) {
        if (!offendingElements.isEmpty()) {
            String strList = offendingElements.stream()
                    .flatMap(e -> Stream.of(String.format(ExceptionConstants.PREPARE_STRING_LIST_FOR_EXCEPTION, e.getQualifiedName(), e.getID())))
                    .collect(Collectors.joining());
            getUIDomain().logError(getUIDomain().prepareMessageForDisplay(errorMessage + strList));
            return false;
        }
        return true;
    }

    public List<RevisionHistoryRecord> getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
            List<ChangeRecord> changeRecords, ConfiguredElement configuredElement, ConfigurationManagementService configurationManagementService) {
        List<RevisionHistoryRecord> revisionHistoryRecords = configurationManagementService.getAllRevisionHistoryRecords();
        if(revisionHistoryRecords == null) {
            return List.of();
        }

        // we only want revision history records with a release authority that is in the list of change records
        return revisionHistoryRecords.stream().filter(revisionHistoryRecord -> {
            ChangeRecord releaseAuthority = revisionHistoryRecord.getRevisionReleaseAuthority();
            if (releaseAuthority != null && changeRecords.contains(releaseAuthority) &&
                    releaseAuthority.affectsGivenConfiguredElement(configuredElement)) {
                // enables backwards compatibility if configured element property is null
                if (revisionHistoryRecord.getConfiguredElement() == null) {
                    revisionHistoryRecord.setConfiguredElement(configuredElement);
                }
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    public void sortChangeRecordsByReleaseStatusAndTime(List<ChangeRecord> changeRecords,
            List<RevisionHistoryRecord> revisionHistoryRecords) {
        ChangeRecordComparatorForHistory comparatorForHistory = getChangeRecordComparator(revisionHistoryRecords);
        changeRecords.sort(comparatorForHistory);
        if (comparatorForHistory.potentiallyHasIncompleteData()) {
            getUIDomain().log(ExceptionConstants.REVISION_HISTORY_RECORD_NOT_FOUND);
        }
    }

    protected ChangeRecordComparatorForHistory getChangeRecordComparator(List<RevisionHistoryRecord> revisionHistoryRecords) {
        return new ChangeRecordComparatorForHistory(revisionHistoryRecords, this);
    }

    public Integer determinePreviousRevisionModelVersion(ConfiguredElement selectedCe, ChangeRecord selectedCr,
            List<ChangeRecord> allRelatedCrs, String revision, ConfigurationManagementService configurationManagementService) {
        List<RevisionHistoryRecord> selectedRevisionRecord = getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
            List.of(selectedCr), selectedCe, configurationManagementService)
                .stream().filter(r -> r.getConfiguredElement().equals(selectedCe) && r.getRevision().equals(revision)).collect(Collectors.toList());
        List<RevisionHistoryRecord> revisionRecords = getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
            allRelatedCrs, selectedCe, configurationManagementService);
        if (revisionRecords != null && !revisionRecords.isEmpty()) {
            revisionRecords.sort(getRevisionComparator()); // sort by the Revision field
            List<RevisionHistoryRecord> revisionRecordsForSelectedCe = revisionRecords
                .stream().filter(r -> r.getConfiguredElement().equals(selectedCe)).collect(Collectors.toList());
            if (!selectedRevisionRecord.isEmpty()) {
                // case where the selected CR has a revision history record
                return getPreviousModelVersionUsingHistoryRecord(selectedRevisionRecord.get(0), revisionRecordsForSelectedCe);
            } else {
                // case where an unreleased selected CR does not have a revision history record, we assume
                // the most recent one is the baseline
                return traverseRecordsForPrevious(revisionRecordsForSelectedCe.size(), selectedCe, revisionRecordsForSelectedCe);
            }
        }
        return 1; // if there are no RHRs, assume 1 is baseline revision
    }

    protected Integer getPreviousModelVersionUsingHistoryRecord(RevisionHistoryRecord selectedRevisionRecord,
            List<RevisionHistoryRecord> revisionRecords) {
        Integer selectedModelVersion = selectedRevisionRecord.getModelVersion();
        if(selectedModelVersion != null) {
            return traverseRecordsForPrevious(revisionRecords.indexOf(selectedRevisionRecord),
                selectedRevisionRecord.getConfiguredElement(), revisionRecords);
        }
        return 1; // no current version found, therefore assume 1 is baseline revision
    }

    protected Integer traverseRecordsForPrevious(int startingIndex, ConfiguredElement configuredElement,
            List<RevisionHistoryRecord> revisionRecords) {
        if (startingIndex > 0) {
            for (int i = startingIndex - 1; i > -1; i--) {
                RevisionHistoryRecord revisionHistoryRecord = revisionRecords.get(i);
                if (revisionHistoryRecord.getConfiguredElement().equals(configuredElement) &&
                        revisionHistoryRecord.getModelVersion() != null) {
                    return revisionHistoryRecord.getModelVersion();
                }
            }
        }
        return 1; // no revision found, assume the first revision is the baseline
    }

    public Integer determineCurrentRevisionModelVersion(ChangeRecord selectedCr, ConfiguredElement configuredElement,
            String revision, ConfigurationManagementService configurationManagementService) {
        if (configurationManagementService.getAutomateReleaseSwitch() && !selectedCr.isReleased()) {
            return getLatestRevisionOnCurrentBranch(configuredElement);
        }
        List<RevisionHistoryRecord> selectedRevisionRecord = getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
            List.of(selectedCr), configuredElement, configurationManagementService).stream()
                .filter(revisionRecordsItem -> revisionRecordsItem.getConfiguredElement().equals(configuredElement)
            && revisionRecordsItem.getRevision().equals(revision)).collect(Collectors.toList());
        if (!selectedRevisionRecord.isEmpty() && selectedRevisionRecord.get(0) != null &&
                selectedRevisionRecord.get(0).getModelVersion() != null) {
            return selectedRevisionRecord.get(0).getModelVersion();
        }
        return getLatestRevisionOnCurrentBranch(configuredElement);
    }

    protected int getLatestRevisionOnCurrentBranch(ConfiguredElement configuredElement) {
        return Math.toIntExact(getApiDomain().getCurrentBranchLatestRevision(getApiDomain().getIProject(configuredElement.getElement())));
    }

    protected RevisionRecordComparatorForRevision getRevisionComparator() {
        return new RevisionRecordComparatorForRevision();
    }

    public ZonedDateTime getCreationTimeFromElementComments(ChangeRecord changeRecord, ConfiguredElement configuredElement) throws DateTimeParseException {
        String comments = getApiDomain().getComment(changeRecord.getElement());
        if(comments != null && !comments.isBlank()) {
            String[] lines = comments.split("\n");
            String expectedLine = null;
            for (String line : lines) {
                if (line.contains(configuredElement.getName()) && line.contains(PluginConstant.CONFIGURING_ACTION)) {
                    expectedLine = line;
                    break;
                }
            }
            return parseTimeFromToken(expectedLine, String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, changeRecord.getName()));
        }
        return null;
    }

    public String trimTimestamp(String timestamp, String potentialErrorSuffix) {
        ZonedDateTime time = tryToParseTimeFromString(timestamp, potentialErrorSuffix);
        return time != null ? time.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT) : null;
    }

    protected ZonedDateTime parseTimeFromToken(String line, String potentialErrorSuffix) {
        if(line != null) {
            String[] tokens = line.split(" ");
            if(!tokens[0].isBlank()) { // expect datetime token first
                return tryToParseTimeFromString(tokens[0], potentialErrorSuffix);
            }
        }
        return null;
    }

    public ZonedDateTime tryToParseTimeFromString(String token, String potentialErrorSuffix) {
        try {
            if(token != null) {
                return ZonedDateTime.parse(token).withZoneSameInstant(ZoneOffset.UTC);
            }
        } catch (DateTimeParseException e) {
            getUIDomain().logErrorAndShowMessage(getLogger(),
                    String.format(ExceptionConstants.DATE_TIME_PARSING_ISSUE, potentialErrorSuffix),
                    ExceptionConstants.DATE_TIME_PARSING_ISSUE_TITLE);
        }
        return null;
    }

    public void determineRevisionHistoryRecordInterleaving(List<RevisionHistoryRecord> revisionHistoryRecords) {
        for(RevisionHistoryRecord revisionHistoryRecord : revisionHistoryRecords) {
            if(!revisionHistoryRecord.isInterleavedWithAnotherRevision() && revisionHistoryRecord.getRevision() != null) {
                ZonedDateTime creationTime = tryToParseTimeFromString(revisionHistoryRecord.getCreationDate(),
                        String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, revisionHistoryRecord.getName()));
                ZonedDateTime releaseTime = tryToParseTimeFromString(revisionHistoryRecord.getReleaseDate(),
                        String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, revisionHistoryRecord.getName()));
                if(creationTime != null && releaseTime != null) {
                    compareWithRemainingHistoryRecords(revisionHistoryRecords, revisionHistoryRecord, creationTime, releaseTime);
                }
            }
        }
    }

    protected void compareWithRemainingHistoryRecords(List<RevisionHistoryRecord> revisionHistoryRecords,
            RevisionHistoryRecord recordToCheck, ZonedDateTime creationTimeToCheck, ZonedDateTime releaseTimeToCheck) {
        for(RevisionHistoryRecord revisionHistoryRecord : revisionHistoryRecords) {
            if(revisionHistoryRecord.getRevision() != null && !revisionHistoryRecord.equals(recordToCheck) &&
                    isRecordInterleavedWithinAnother(creationTimeToCheck, releaseTimeToCheck, revisionHistoryRecord)) {
                // if the record we're checking was made before this record but released after this record, it is interleaved
                recordToCheck.setInterleavedWithAnotherRevision(true);
                break;
            }
        }
    }

    protected boolean isRecordInterleavedWithinAnother(ZonedDateTime creationTimeToCheck, ZonedDateTime releaseTimeToCheck,
            RevisionHistoryRecord revisionHistoryRecord) {
        ZonedDateTime creationTime = tryToParseTimeFromString(revisionHistoryRecord.getCreationDate(),
                String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, revisionHistoryRecord.getName()));
        ZonedDateTime releaseTime = tryToParseTimeFromString(revisionHistoryRecord.getReleaseDate(),
                String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, revisionHistoryRecord.getName()));

        if(creationTime != null && releaseTime != null) {
            return creationTimeToCheck.isBefore(creationTime) && releaseTimeToCheck.isAfter(releaseTime);
        }
        return false;
    }

    public void displayDifferenceViewer(ElementHistoryRowView selectedRow, String configuredElementLocalId,
            List<ChangeRecord> relevantChangeRecords, boolean setMemoryOptimization, ConfigurationManagementService configurationManagementService) {
        if (selectedRow != null && configuredElementLocalId != null && !configuredElementLocalId.isBlank() &&
                relevantChangeRecords != null && !relevantChangeRecords.isEmpty() && configurationManagementService != null) {
            ConfiguredElement configuredElement = configurationManagementService.getConfiguredElementDomain().getConfiguredElementUsingId(configuredElementLocalId, configurationManagementService);
            if (configuredElement != null) {
                ChangeRecord selectedCR = getChangeRecordUsingId(selectedRow.getChangeRecordLocalId(), configurationManagementService);
                if(selectedCR == null) {
                    getUIDomain().logError(getLogger(), String.format(ExceptionConstants.MODEL_INCONSISTENT_WITH_UI,
                            selectedRow.getChangeRecordLocalId()));
                    return;
                }

                Integer baselineVersion = determinePreviousRevisionModelVersion(configuredElement, selectedCR,
                        relevantChangeRecords, selectedRow.getRevisionColumn(), configurationManagementService);
                Integer releaseVersion = determineCurrentRevisionModelVersion(selectedCR, configuredElement,
                        selectedRow.getRevisionColumn(), configurationManagementService);
                String selectedCRName = selectedRow.getChangeRecordNameColumn();
                if (releaseVersion > 1 && baselineVersion < releaseVersion) {
                    if (!getApiDomain().isRevisionMostRecentAndProjectDirty(configuredElement, releaseVersion) ||
                            getUIDomain().isOkOption(
                            getUIDomain().askForConfirmation(ExceptionConstants.COMPARISON_REQUIRES_COMMIT_MESSAGE, ExceptionConstants.COMPARISON_REQUIRES_COMMIT_TITLE))) {
                        getApiDomain().performProjectVersionComparison(configuredElement, baselineVersion, releaseVersion,
                                setMemoryOptimization);
                    }
                } else if (baselineVersion.equals(releaseVersion)) {
                    getUIDomain().logError(getLogger(), String.format(ExceptionConstants.VERSIONS_ARE_IDENTICAL,
                            selectedCRName, baselineVersion, releaseVersion));
                } else {
                    getUIDomain().logError(getLogger(), String.format(ExceptionConstants.PROJECT_COMPARISON_IMPROPER_VERSION,
                            selectedCRName, baselineVersion, releaseVersion));
                }
            }
        }
    }

    protected ChangeRecord getChangeRecordUsingId(String id, ConfigurationManagementService configurationManagementService) {
        if (id != null) {
            Element element = getApiDomain().getElementUsingId(id);
            if (element != null) {
                return configurationManagementService.getChangeRecord(element);
            }
        }
        return null;
    }
}
