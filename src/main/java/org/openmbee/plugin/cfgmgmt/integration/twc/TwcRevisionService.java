package org.openmbee.plugin.cfgmgmt.integration.twc;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.JsonConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.TwcRevisionDifferenceJson;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.element.TwcElementJson;
import org.openmbee.plugin.cfgmgmt.integration.twc.json.element.TwcIdJson;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TwcRevisionService {
    private static final Logger logger = LoggerFactory.getLogger(TwcRevisionService.class);
    private ConfigurationManagementService configurationManagementService;

    private Property baseCrStatusProperty;
    private Property baseIsReleasedProperty;
    private final Set<String> importantStereotypeIds = new HashSet<>();
    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    public void setConfigurationManagementService(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
    }

    protected TeamworkCloudService getTeamworkCloudService() {
        return getConfigurationManagementService().getTeamworkCloudService();
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

    public Property getBaseCrStatusProperty() {
        return baseCrStatusProperty;
    }

    public void setBaseCrStatusProperty(Property baseCrStatusProperty) {
        this.baseCrStatusProperty = baseCrStatusProperty;
    }

    public Property getBaseIsReleasedProperty() {
        return baseIsReleasedProperty;
    }

    public void setBaseIsReleasedProperty(Property baseIsReleasedProperty) {
        this.baseIsReleasedProperty = baseIsReleasedProperty;
    }

    protected Set<String> getImportantStereotypeIds() {
        return importantStereotypeIds; // enables unit testing
    }

    protected Gson getGson() {
        return gson; // enables unit testing
    }

    protected Map<String, TwcElementJson> copyElementJsonMapping(Map<String, TwcElementJson> mapping) {
        return new HashMap<>(mapping); // enables unit testing
    }

    protected TwcIndexedChangesAndDetails createdIndexedChangesAndDetailsObject() {
        return new TwcIndexedChangesAndDetails(); // enables unit testing
    }

    protected StringBuilder createStringBuilder() {
        return new StringBuilder(); // enables unit testing
    }

    protected Set<TwcIdJson> createTwcIdJsonSet() {
        return new HashSet<>(); // enables unit testing
    }

    /**
     * Attempts to do a comparison on Teamwork Cloud revisions based on Change Management stereotypes and their
     * associated elements. After that, this method attempts to collect all the information surrounding these elements
     * that allow someone with enough knowledge to understand the context of these changes.
     *
     * @param sourceRevision a given positive integer as a string, this should be less than the targetRevision.
     * @param targetRevision a given positive integer as a string, this should be more than the sourceRevision.
     * @return a TwcIndexedChangesAndDetails object, which has the following maps inside:
     * changedInitial, added, removed, changedFinal, details
     *
     * Note: The details map refers to the details that contextualize elements from the added or removed categories.
     */
    public TwcIndexedChangesAndDetails compareRevisionsAndGatherData(String sourceRevision, String targetRevision) {
        TwcIndexedChangesAndDetails indexedChangesAndDetails = createdIndexedChangesAndDetailsObject();
        try {
            TwcAddedChangedRemovedResults elementsBetweenRevisions = getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision);
            if(elementsBetweenRevisions != null && collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision)) {
                // obtain elements and the details that contextualize them within the model
                obtainAllChangeInitialElementsAndDetails(sourceRevision, elementsBetweenRevisions, indexedChangesAndDetails);
                obtainAllAddedRemovedAndChangeFinalElementsAndDetails(targetRevision, elementsBetweenRevisions, indexedChangesAndDetails);
            }
        } catch (TWCIntegrationException e) {
            getUiDomain().logErrorAndShowMessage(getLogger(), e.getMessage(), ExceptionConstants.EXCEPTION_WHILE_GATHERING_DATA_TITLE);
        }
        return indexedChangesAndDetails;
    }

    protected boolean collectingImportantStereotypesForRevisionComparison(String sourceRevision,
            String targetRevision) throws TWCIntegrationException {
        // get relevant stereotypes from source revision
        Map<String, TwcElementJson> sourceRevisionStereotypes = obtainInitialImportantStereotypesUsingIds(sourceRevision);
        determineImportantStereotypesUsingIds(sourceRevision, sourceRevisionStereotypes, sourceRevisionStereotypes);
        // get relevant stereotypes from target revision
        Map<String, TwcElementJson> targetRevisionStereotypes = obtainInitialImportantStereotypesUsingIds(targetRevision);
        determineImportantStereotypesUsingIds(targetRevision, targetRevisionStereotypes, targetRevisionStereotypes);
        getImportantStereotypeIds().clear();
        if(!sourceRevisionStereotypes.isEmpty() && !targetRevisionStereotypes.isEmpty()) {
            getImportantStereotypeIds().addAll(sourceRevisionStereotypes.keySet());
            getImportantStereotypeIds().addAll(targetRevisionStereotypes.keySet());
        } else {
            getUiDomain().logErrorAndShowMessage(getLogger(),
                    String.format(ExceptionConstants.NO_STEREOTYPES_FOUND, sourceRevision, targetRevision),
                    ExceptionConstants.NO_STEREOTYPES_FOUND_TITLE);
            return false;
        }
        return true;
    }

    protected void obtainAllChangeInitialElementsAndDetails(String sourceRevision,
            TwcAddedChangedRemovedResults elementsBetweenRevisions, TwcIndexedChangesAndDetails indexedChangesAndDetails) throws TWCIntegrationException {
        // map the elements
        indexedChangesAndDetails.setChangedInitial(mapTwcElementJsonDataById(elementsBetweenRevisions.getChangedString(),
                elementsBetweenRevisions.getChangedInitial()));
        // filter out "unimportant" elements such as packages, stereotypes, etc.
        filterUnimportantElements(indexedChangesAndDetails.getChangedInitial());
        // get the missing details and then recursively get more missing details
        indexedChangesAndDetails.setDetails(initialMissingDetailsForChangedElements(sourceRevision, indexedChangesAndDetails));
        getFurtherMissingDetails(sourceRevision, copyElementJsonMapping(indexedChangesAndDetails.getDetails()), indexedChangesAndDetails);
    }

    protected void obtainAllAddedRemovedAndChangeFinalElementsAndDetails(String targetRevision,
            TwcAddedChangedRemovedResults elementsBetweenRevisions, TwcIndexedChangesAndDetails indexedChangesAndDetails) throws TWCIntegrationException {
        // map the elements of each section
        indexedChangesAndDetails.setAdded(mapTwcElementJsonDataById(elementsBetweenRevisions.getAddedString(),
                elementsBetweenRevisions.getAddedRemovedOrChangedFinal()));
        indexedChangesAndDetails.setRemoved(mapTwcElementJsonDataById(elementsBetweenRevisions.getRemovedString(),
                elementsBetweenRevisions.getAddedRemovedOrChangedFinal()));
        indexedChangesAndDetails.setChangedFinal(mapTwcElementJsonDataById(elementsBetweenRevisions.getChangedString(),
                elementsBetweenRevisions.getAddedRemovedOrChangedFinal()));
        // filter out "unimportant" elements such as packages, stereotypes, etc.
        filterUnimportantElements(indexedChangesAndDetails.getAdded());
        filterUnimportantElements(indexedChangesAndDetails.getRemoved());
        filterUnimportantElements(indexedChangesAndDetails.getChangedFinal());
        // get the missing details and then recursively get more missing details
        Map<String, TwcElementJson> addedChangedFinalOrRemovedMissingDetails = initialMissingDetailsForAddedAndRemovedElements(targetRevision,
                indexedChangesAndDetails);
        // filter the details based on previous data to reduce needless future calls
        addedChangedFinalOrRemovedMissingDetails.entrySet().removeIf(entry -> indexedChangesAndDetails.isInAGivenMap(entry.getKey()));
        getFurtherMissingDetails(targetRevision, copyElementJsonMapping(addedChangedFinalOrRemovedMissingDetails),
                indexedChangesAndDetails);
        // consolidate details
        indexedChangesAndDetails.getDetails().putAll(addedChangedFinalOrRemovedMissingDetails);
    }

    public TwcAddedChangedRemovedResults getElementChangesBetweenTwoRevisions(String sourceRevision,
            String targetRevision) throws TWCIntegrationException {
        TwcRevisionDifferenceJson revisionDifferenceJson = getTwcRevisionDifference(sourceRevision, targetRevision);
        if(revisionDifferenceJson != null && !revisionDifferenceJson.isEmpty()) {
            TwcAddedChangedRemovedResults twcAddedChangedRemovedResults = getTwcAddedChangedRemovedResults(revisionDifferenceJson);
            String initialIds = twcAddedChangedRemovedResults.getChangedString();
            String finalIds = twcAddedChangedRemovedResults.getAddedRemovedOrChangedFinalString();

            if(!initialIds.isBlank()) {
                twcAddedChangedRemovedResults.setChangedInitial(getElementsUsingIds(initialIds, sourceRevision));
            }
            if(!finalIds.isBlank()) {
                twcAddedChangedRemovedResults.setAddedRemovedOrChangedFinal(getElementsUsingIds(finalIds, targetRevision));
            }

            return twcAddedChangedRemovedResults;
        }

        return null;
    }

    protected TwcAddedChangedRemovedResults getTwcAddedChangedRemovedResults(TwcRevisionDifferenceJson revisionDifferenceJson) {
        return new TwcAddedChangedRemovedResults(revisionDifferenceJson);
    }

    public TwcRevisionDifferenceJson getTwcRevisionDifference(String sourceRevision, String targetRevision) {
        String projectId = getTeamworkCloudService().getProjectIdFromCurrentUri(getApiDomain());
        TwcRevisionDifferenceJson revisionDifferenceJson = null;
        if(projectId != null) {
            try {
                revisionDifferenceJson = getTeamworkCloudService().getRevisionDifference(getApiDomain(),
                        projectId, sourceRevision, targetRevision);
            } catch (TWCIntegrationException e) {
                getUiDomain().logError(getLogger(), ExceptionConstants.UNABLE_TO_GET_TWC_REVISION, e);
            }
        }

        return revisionDifferenceJson;
    }

    protected List<TwcElementJson> getElementsUsingIds(String elementIds, String revision) throws TWCIntegrationException {
        String projectId = getTeamworkCloudService().getProjectIdFromCurrentUri(getApiDomain());
        if(projectId != null && !elementIds.isBlank()) {
            try {
                return getTeamworkCloudService().getElementsAtRevision(getApiDomain(), projectId, revision, elementIds);
            } catch (TWCIntegrationException e) {
                getUiDomain().logError(getLogger(),
                        String.format(ExceptionConstants.UNABLE_TO_RETRIEVE_ELEMENTS_AT_REVISION, revision));
                throw e;
            }
        }
        return List.of();
    }

    protected Map<String, TwcElementJson> obtainInitialImportantStereotypesUsingIds(String revision) throws TWCIntegrationException {
        return getElementsFromIdAndMapThemById(revision, String.join(PluginConstant.COMMA, configurationManagementService.getBaseCRStereotype().getID(),
                configurationManagementService.getBaseCEStereotype().getID(), configurationManagementService.getRhStereotype().getID()));
    }

    protected void determineImportantStereotypesUsingIds(String revision, Map<String, TwcElementJson> currentImportantStereotypes,
            Map<String, TwcElementJson> allImportantStereotypes) throws TWCIntegrationException {
        // get all the directed relationship targets
        String directedRelations = getAllDirectedRelationshipOfTargetForGivenStereotypes(currentImportantStereotypes);
        if(!directedRelations.isBlank()) {
            List<TwcElementJson> directedRelationsJson = getElementsUsingIds(directedRelations, revision);
            Set<TwcIdJson> specifics = createTwcIdJsonSet();
            // find the relations that are generalizations and get their specifics
            for(TwcElementJson relation : directedRelationsJson) {
                if(isGeneralization(relation)) {
                    TwcIdJson id = getSpecificForGivenGeneralizations(relation);
                    if(id != null) {
                        specifics.add(id);
                    }
                }
            }
            // get the specifics from twc and add them to our important stereotypes, then recurse
            if(!specifics.isEmpty()) {
                StringBuilder builder = createStringBuilder();
                appendIdJsonCollection(specifics, builder);
                Map<String, TwcElementJson> moreImportantStereotypes = getElementsFromIdAndMapThemById(revision, builder.toString());
                if(moreImportantStereotypes != null && !moreImportantStereotypes.isEmpty()) {
                    allImportantStereotypes.putAll(moreImportantStereotypes);
                    determineImportantStereotypesUsingIds(revision, moreImportantStereotypes, allImportantStereotypes);
                }
            }
        }
    }

    protected String getAllDirectedRelationshipOfTargetForGivenStereotypes(Map<String, TwcElementJson> stereotypes) {
        Set<TwcIdJson> directedRelations = createTwcIdJsonSet();
        StringBuilder builder = createStringBuilder();
        for(TwcElementJson stereotype : stereotypes.values()) {
            getIdJsonFromArray(directedRelations, stereotype.getEsiData().getAsJsonObject().get(JsonConstants.DIRECTED_RELATIONSHIP_OF_TARGET));
        }
        appendIdJsonCollection(directedRelations, builder);
        return builder.toString();
    }

    protected TwcIdJson getSpecificForGivenGeneralizations(TwcElementJson generalization) {
        JsonElement specific = generalization.getEsiData().getAsJsonObject().get(JsonConstants.SPECIFIC_KEY);
        if(specific != null && specific.isJsonObject()) {
            TwcIdJson idJson = getGson().fromJson(specific, TwcIdJson.class);
            if (idJson != null && idJson.getId() != null && !idJson.getId().isBlank()) {
                return idJson;
            }
        }
        return null;
    }

    protected boolean isGeneralization(TwcElementJson elementJson) {
        return elementJson.getType().equals(JsonConstants.GENERALIZATION_TYPE_VALUE);
    }

    public Map<String, TwcElementJson> mapTwcElementJsonDataById(String validIds, List<TwcElementJson> data) {
        if(validIds != null && !validIds.isBlank()) {
            Map<String, TwcElementJson> mappedSegment = new HashMap<>();
            for(TwcElementJson item : data) {
                if(item != null && item.getEsiId() != null && !item.getEsiId().isBlank() && validIds.contains(item.getEsiId())) {
                    mappedSegment.put(item.getEsiId(), item);
                }
            }
            return mappedSegment;
        }
        return Map.of();
    }

    protected Map<String, TwcElementJson> initialMissingDetailsForChangedElements(String revision,
            TwcIndexedChangesAndDetails indexedChangesAndDetails) throws TWCIntegrationException {
        StringBuilder idsToGrab = createStringBuilder();
        iterateMapForDetailsToGrabLater(indexedChangesAndDetails.getChangedInitial(), idsToGrab, indexedChangesAndDetails);
        return getElementsFromIdAndMapThemById(revision, prepareStringForBatchCall(idsToGrab));
    }

    protected Map<String, TwcElementJson> initialMissingDetailsForAddedAndRemovedElements(String revision,
            TwcIndexedChangesAndDetails indexedChangesAndDetails) throws TWCIntegrationException {
        StringBuilder idsToGrab = createStringBuilder();
        iterateMapForDetailsToGrabLater(indexedChangesAndDetails.getAdded(), idsToGrab, indexedChangesAndDetails);
        iterateMapForDetailsToGrabLater(indexedChangesAndDetails.getRemoved(), idsToGrab, indexedChangesAndDetails);
        return getElementsFromIdAndMapThemById(revision, prepareStringForBatchCall(idsToGrab));
    }

    protected void appendIdJsonCollection(Collection<TwcIdJson> ids, StringBuilder idsToGrab) {
        ids.forEach(id -> {
            if(id != null && id.getId() != null) {
                idsToGrab.append(id.getId()).append(PluginConstant.COMMA);
            }
        });
    }

    protected String prepareStringForBatchCall(StringBuilder idsToGrab) {
        if(idsToGrab.length() > 0 && idsToGrab.charAt(idsToGrab.length() - 1) == PluginConstant.COMMA_CHAR) {
            idsToGrab.deleteCharAt(idsToGrab.length() - 1);
        }
        return idsToGrab.toString();
    }

    protected void iterateMapForDetailsToGrabLater(Map<String, TwcElementJson> map, StringBuilder idsToGrab,
            TwcIndexedChangesAndDetails indexedChangesAndDetails) {
        for (Map.Entry<String, TwcElementJson> entry : map.entrySet()) {
            TwcElementJson elementJson = entry.getValue();
            if(elementJson != null && elementJson.getType() != null && !elementJson.getType().isBlank() &&
                    elementJson.getEsiData() != null && elementJson.getEsiData().getAsJsonObject().get(JsonConstants.LOCAL_ID_KEY) != null) {
                String type = elementJson.getType();
                if(!areElementDetailsUnimportant(type)) {
                    Set<TwcIdJson> possibleDetails = createTwcIdJsonSet();
                    if(type.equals(JsonConstants.ELEMENT_TAGGED_VALUE)) {
                        getElementTagValueIds(elementJson, possibleDetails);
                    }
                    // The typical details we might need are the following:
                    // owner, ownedElement, taggedValues, relations
                    // Are ports/relations part of ownedElement? Maybe. We may need more analysis to know.
                    getTypicalDetails(elementJson, possibleDetails);

                    filterDetailsAlreadyObtained(possibleDetails, indexedChangesAndDetails);
                    appendIdJsonCollection(possibleDetails, idsToGrab);
                }
            }
        }
    }

    protected Map<String, TwcElementJson> getElementsFromIdAndMapThemById(String revision, String idsToGrab) throws TWCIntegrationException {
        return mapTwcElementJsonDataById(idsToGrab, getElementsUsingIds(idsToGrab, revision));
    }

    protected void getFurtherMissingDetails(String revision, Map<String, TwcElementJson> newDetails,
            TwcIndexedChangesAndDetails indexedChangesAndDetails) throws TWCIntegrationException {
        StringBuilder idsToGrab = createStringBuilder();
        iterateMapForDetailsToGrabLater(newDetails, idsToGrab, indexedChangesAndDetails);

        if(idsToGrab.length() > 0) {
            Map<String, TwcElementJson> newlyFoundDetails = getElementsFromIdAndMapThemById(revision, prepareStringForBatchCall(idsToGrab));
            indexedChangesAndDetails.getDetails().putAll(newlyFoundDetails);

            getFurtherMissingDetails(revision, newlyFoundDetails, indexedChangesAndDetails);
        }
    }

    protected void getTypicalDetails(TwcElementJson elementJson, Set<TwcIdJson> ids) {
        JsonElement appliedStereotypeArray = elementJson.getEsiData().getAsJsonObject().get(JsonConstants.APPLIED_STEREOTYPE);
        Set<TwcIdJson> appliedStereotypeIds = createTwcIdJsonSet();
        getIdJsonFromArray(appliedStereotypeIds, appliedStereotypeArray);
        JsonElement ownerArray = selectivelyGetOwnerArrayBasedOnAppliedStereotypes(elementJson, appliedStereotypeIds);
        if(ownerArray != null) {
            getIdJsonFromArray(ids, ownerArray);
        }
        JsonElement ownedElementArray = elementJson.getEsiData().getAsJsonObject().get(JsonConstants.OWNED_ELEMENT);
        JsonElement taggedValueArray = elementJson.getEsiData().getAsJsonObject().get(JsonConstants.TAGGED_VALUE_ARRAY_KEY);
        getIdJsonFromArray(ids, ownedElementArray);
        getIdJsonFromArray(ids, taggedValueArray);
    }

    protected void getElementTagValueIds(TwcElementJson tagValue, Set<TwcIdJson> ids) {
        getIdJsonFromArray(ids, tagValue.getEsiData().getAsJsonObject().get(JsonConstants.VALUE_ARRAY_KEY));
    }

    protected void getIdJsonFromArray(Set<TwcIdJson> ids, JsonElement jsonArray) {
        if (jsonArray.isJsonArray()) {
            for (JsonElement valueContainer : jsonArray.getAsJsonArray()) {
                TwcIdJson idJson = getGson().fromJson(valueContainer, TwcIdJson.class);
                if (idJson != null && idJson.getId() != null && !idJson.getId().isBlank()) {
                    ids.add(idJson);
                }
            }
        }
    }

    protected JsonElement selectivelyGetOwnerArrayBasedOnAppliedStereotypes(TwcElementJson elementJson, Set<TwcIdJson> appliedStereotypeIds) {
        for (TwcIdJson id : appliedStereotypeIds) {
            if (getImportantStereotypeIds().contains(id.getId())) {
                return null; // current element is a "top-level" element, do not get the owner
            }
        }
        return elementJson.getEsiData().getAsJsonObject().get(JsonConstants.OWNER);
    }

    protected void filterDetailsAlreadyObtained(Set<TwcIdJson> ids, TwcIndexedChangesAndDetails indexedChangesAndDetails) {
        ids.removeIf(id -> id.getId() != null && !id.getId().isBlank() && indexedChangesAndDetails.isInAGivenMap(id.getId()));
    }

    protected void filterUnimportantElements(Map<String, TwcElementJson> map) {
        map.entrySet().removeIf(entry -> {
            String type = entry.getValue().getType();
            if(type == null || type.isBlank()) {
                return true;
            } else {
                return areElementDetailsUnimportant(type);
            }
        });
    }

    protected boolean areElementDetailsUnimportant(String type) {
        return type.equals(JsonConstants.PACKAGE_TYPE) || type.equals(JsonConstants.STEREOTYPE_TYPE) ||
                type.equals(JsonConstants.DIAGRAM_TYPE) || type.equals(JsonConstants.PROFILE_TYPE) ||
                type.startsWith(JsonConstants.ESI_PROJECT_TYPE_PREFIX);
    }
}
