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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestTwcRevisionService {
    private TwcRevisionService twcRevisionService;
    private ConfigurationManagementService configurationManagementService;
    private TeamworkCloudService teamworkCloudService;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private Logger logger;
    private String sourceRevision;
    private String targetRevision;
    private String projectId;
    private TwcElementJson elementJson;
    private Map<String, TwcElementJson> map;
    private TwcAddedChangedRemovedResults addedChangedRemovedResults;
    private TwcIndexedChangesAndDetails indexedChangesAndDetails;
    private Set<String> stringSet;
    private TwcIdJson twcIdJson;
    private StringBuilder stringBuilder;

    @Before
    public void setup() {
        twcRevisionService = Mockito.spy(new TwcRevisionService());
        configurationManagementService = mock(ConfigurationManagementService.class);
        teamworkCloudService = mock(TeamworkCloudService.class);
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        logger = mock(Logger.class);
        sourceRevision = "sourceRevision";
        targetRevision = "targetRevision";
        projectId = "projectId";
        elementJson = mock(TwcElementJson.class);
        map = new HashMap<>();
        addedChangedRemovedResults = mock(TwcAddedChangedRemovedResults.class);
        indexedChangesAndDetails = spy(new TwcIndexedChangesAndDetails());
        stringSet = new HashSet<>();
        twcIdJson = mock(TwcIdJson.class);
        stringBuilder = spy(new StringBuilder());

        twcRevisionService.setConfigurationManagementService(configurationManagementService);

        doReturn(logger).when(twcRevisionService).getLogger();

        doReturn(teamworkCloudService).when(twcRevisionService).getTeamworkCloudService();
        doReturn(apiDomain).when(twcRevisionService).getApiDomain();
        doReturn(uiDomain).when(twcRevisionService).getUiDomain();
        doReturn(stringSet).when(twcRevisionService).getImportantStereotypeIds();
        when(teamworkCloudService.getProjectIdFromCurrentUri(apiDomain)).thenReturn(projectId);
        doReturn(stringBuilder).when(twcRevisionService).createStringBuilder();
        doReturn(indexedChangesAndDetails).when(twcRevisionService).createdIndexedChangesAndDetailsObject();
    }

    @Test
    public void compareRevisionsAndGatherData_nullRevisionDifferenceResults() throws TWCIntegrationException {
            doReturn(null).when(twcRevisionService).getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision);

            assertTrue(twcRevisionService.compareRevisionsAndGatherData(sourceRevision, targetRevision).isEmpty());
            verify(twcRevisionService, never()).collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);
    }

    @Test
    public void compareRevisionsAndGatherData_exceptionGettingRevisionDifferenceResults() {
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));
        try {
            doThrow(integrationException).when(twcRevisionService).getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision);
            doNothing().when(uiDomain).logErrorAndShowMessage(logger, error, ExceptionConstants.EXCEPTION_WHILE_GATHERING_DATA_TITLE);

            assertTrue(twcRevisionService.compareRevisionsAndGatherData(sourceRevision, targetRevision).isEmpty());

            verify(twcRevisionService, never()).collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception occurred");
        }
    }

    @Test
    public void compareRevisionsAndGatherData_exceptionCollectingImportantStereotypes() {
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));
        try {
            doReturn(addedChangedRemovedResults).when(twcRevisionService).getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision);
            doThrow(integrationException).when(twcRevisionService).collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);
            doNothing().when(uiDomain).logErrorAndShowMessage(logger, error, ExceptionConstants.EXCEPTION_WHILE_GATHERING_DATA_TITLE);

            assertTrue(twcRevisionService.compareRevisionsAndGatherData(sourceRevision, targetRevision).isEmpty());

            verify(twcRevisionService, never()).obtainAllChangeInitialElementsAndDetails(sourceRevision, addedChangedRemovedResults, indexedChangesAndDetails);
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception occurred");
        }
    }

    @Test
    public void compareRevisionsAndGatherData_failedToCollectImportantStereotypes() throws TWCIntegrationException {
        doReturn(addedChangedRemovedResults).when(twcRevisionService).getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision);
        doReturn(false).when(twcRevisionService).collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);

        assertTrue(twcRevisionService.compareRevisionsAndGatherData(sourceRevision, targetRevision).isEmpty());
    }

    @Test
    public void compareRevisionsAndGatherData_exceptionObtainingInitialChanges() {
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));
        try {
            doReturn(addedChangedRemovedResults).when(twcRevisionService).getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision);
            doReturn(true).when(twcRevisionService).collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);
            doThrow(integrationException).when(twcRevisionService).obtainAllChangeInitialElementsAndDetails(sourceRevision, addedChangedRemovedResults, indexedChangesAndDetails);
            doNothing().when(uiDomain).logErrorAndShowMessage(logger, error, ExceptionConstants.EXCEPTION_WHILE_GATHERING_DATA_TITLE);

            assertTrue(twcRevisionService.compareRevisionsAndGatherData(sourceRevision, targetRevision).isEmpty());

            verify(twcRevisionService, never()).obtainAllAddedRemovedAndChangeFinalElementsAndDetails(anyString(), any(), any());
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception occurred");
        }
    }

    @Test
    public void compareRevisionsAndGatherData_exceptionObtainingEverythingElse() {
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));
        TwcAddedChangedRemovedResults addedChangedRemovedResults = mock(TwcAddedChangedRemovedResults.class);
        try {
            doReturn(addedChangedRemovedResults).when(twcRevisionService).getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision);
            doReturn(true).when(twcRevisionService).collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);
            doNothing().when(twcRevisionService).obtainAllChangeInitialElementsAndDetails(sourceRevision, addedChangedRemovedResults, indexedChangesAndDetails);
            doThrow(integrationException).when(twcRevisionService).obtainAllAddedRemovedAndChangeFinalElementsAndDetails(targetRevision, addedChangedRemovedResults, indexedChangesAndDetails);
            doNothing().when(uiDomain).logErrorAndShowMessage(logger, error, ExceptionConstants.EXCEPTION_WHILE_GATHERING_DATA_TITLE);

            assertTrue(twcRevisionService.compareRevisionsAndGatherData(sourceRevision, targetRevision).isEmpty());
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception occurred");
        }
    }

    @Test
    public void compareRevisionsAndGatherData_everythingObtainedAndPackaged() {
        try {
            doReturn(addedChangedRemovedResults).when(twcRevisionService).getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision);
            doReturn(true).when(twcRevisionService).collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);
            doNothing().when(twcRevisionService).obtainAllChangeInitialElementsAndDetails(sourceRevision, addedChangedRemovedResults, indexedChangesAndDetails);
            doNothing().when(twcRevisionService).obtainAllAddedRemovedAndChangeFinalElementsAndDetails(targetRevision, addedChangedRemovedResults, indexedChangesAndDetails);

            twcRevisionService.compareRevisionsAndGatherData(sourceRevision, targetRevision);

            verify(twcRevisionService).obtainAllAddedRemovedAndChangeFinalElementsAndDetails(targetRevision, addedChangedRemovedResults, indexedChangesAndDetails);
            verify(uiDomain, never()).logErrorAndShowMessage(any(), anyString(), anyString());
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception occurred");
        }
    }

    @Test
    public void collectingImportantStereotypesForRevisionComparison_exceptionGettingInitialSourceStereotypes() throws TWCIntegrationException {
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

        try {
            doThrow(integrationException).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(sourceRevision);

            twcRevisionService.collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);

            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertSame(error, e.getMessage());
            verify(twcRevisionService, never()).determineImportantStereotypesUsingIds(anyString(), anyMap(), anyMap());
        }
    }

    @Test
    public void collectingImportantStereotypesForRevisionComparison_exceptionDeterminingRemainingSourceStereotypes() throws TWCIntegrationException {
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

        try {
            doReturn(map).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(sourceRevision);
            doThrow(integrationException).when(twcRevisionService).determineImportantStereotypesUsingIds(sourceRevision, map, map);

            twcRevisionService.collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);

            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertSame(error, e.getMessage());
            verify(twcRevisionService, never()).obtainInitialImportantStereotypesUsingIds(targetRevision);
        }
    }

    @Test
    public void collectingImportantStereotypesForRevisionComparison_exceptionObtainingInitialTargetStereotypes() throws TWCIntegrationException {
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

        try {
            doReturn(map).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(sourceRevision);
            doNothing().when(twcRevisionService).determineImportantStereotypesUsingIds(sourceRevision, map, map);
            doThrow(integrationException).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(targetRevision);


            twcRevisionService.collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);

            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertSame(error, e.getMessage());
            verify(twcRevisionService, atMostOnce()).determineImportantStereotypesUsingIds(anyString(), anyMap(), anyMap());
        }
    }

    @Test
    public void collectingImportantStereotypesForRevisionComparison_exceptionObtainingRemainingTargetStereotypes() throws TWCIntegrationException {
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));
        Map<String, TwcElementJson> targetStereotypes = new HashMap<>();
        stringSet = spy(new HashSet<>());

        try {
            doReturn(map).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(sourceRevision);
            doNothing().when(twcRevisionService).determineImportantStereotypesUsingIds(sourceRevision, map, map);
            doReturn(targetStereotypes).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(targetRevision);
            doThrow(integrationException).when(twcRevisionService).determineImportantStereotypesUsingIds(targetRevision, targetStereotypes, targetStereotypes);


            twcRevisionService.collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision);

            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertSame(error, e.getMessage());
            verify(stringSet, never()).clear();
        }
    }

    @Test
    public void collectingImportantStereotypesForRevisionComparison_emptySourceStereotypes() {
        Map<String, TwcElementJson> targetStereotypes = new HashMap<>();

        try {
            doReturn(map).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(sourceRevision);
            doNothing().when(twcRevisionService).determineImportantStereotypesUsingIds(sourceRevision, map, map);
            doReturn(targetStereotypes).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(targetRevision);
            doNothing().when(twcRevisionService).determineImportantStereotypesUsingIds(targetRevision, targetStereotypes, targetStereotypes);
            doNothing().when(uiDomain).logErrorAndShowMessage(logger, String.format(ExceptionConstants.NO_STEREOTYPES_FOUND, sourceRevision, targetRevision), ExceptionConstants.NO_STEREOTYPES_FOUND_TITLE);

            assertFalse(twcRevisionService.collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision));
            verify(uiDomain).logErrorAndShowMessage(logger, String.format(ExceptionConstants.NO_STEREOTYPES_FOUND, sourceRevision, targetRevision), ExceptionConstants.NO_STEREOTYPES_FOUND_TITLE);
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception occurred");
        }
    }

    @Test
    public void collectingImportantStereotypesForRevisionComparison_emptyTargetStereotypes() {
        map.put("id", elementJson);
        Map<String, TwcElementJson> targetStereotypes = new HashMap<>();

        try {
            doReturn(map).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(sourceRevision);
            doNothing().when(twcRevisionService).determineImportantStereotypesUsingIds(sourceRevision, map, map);
            doReturn(targetStereotypes).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(targetRevision);
            doNothing().when(twcRevisionService).determineImportantStereotypesUsingIds(targetRevision, targetStereotypes, targetStereotypes);
            doNothing().when(uiDomain).logErrorAndShowMessage(logger, String.format(ExceptionConstants.NO_STEREOTYPES_FOUND, sourceRevision, targetRevision), ExceptionConstants.NO_STEREOTYPES_FOUND_TITLE);

            assertFalse(twcRevisionService.collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision));
            verify(uiDomain).logErrorAndShowMessage(logger, String.format(ExceptionConstants.NO_STEREOTYPES_FOUND, sourceRevision, targetRevision), ExceptionConstants.NO_STEREOTYPES_FOUND_TITLE);
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception occurred");
        }
    }

    @Test
    public void collectingImportantStereotypesForRevisionComparison_stereotypesAvailable() {
        String id = "id";
        String id2 = "id2";
        map.put(id, elementJson);
        Map<String, TwcElementJson> targetStereotypes = new HashMap<>();
        TwcElementJson elementJson2 = mock(TwcElementJson.class);
        targetStereotypes.put(id2, elementJson2);

        try {
            doReturn(map).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(sourceRevision);
            doNothing().when(twcRevisionService).determineImportantStereotypesUsingIds(sourceRevision, map, map);
            doReturn(targetStereotypes).when(twcRevisionService).obtainInitialImportantStereotypesUsingIds(targetRevision);
            doNothing().when(twcRevisionService).determineImportantStereotypesUsingIds(targetRevision, targetStereotypes, targetStereotypes);
            doNothing().when(uiDomain).logErrorAndShowMessage(logger, String.format(ExceptionConstants.NO_STEREOTYPES_FOUND, sourceRevision, targetRevision), ExceptionConstants.NO_STEREOTYPES_FOUND_TITLE);

            assertTrue(twcRevisionService.collectingImportantStereotypesForRevisionComparison(sourceRevision, targetRevision));
            verify(uiDomain, never()).logErrorAndShowMessage(logger, String.format(ExceptionConstants.NO_STEREOTYPES_FOUND, sourceRevision, targetRevision), ExceptionConstants.NO_STEREOTYPES_FOUND_TITLE);
            assertTrue(stringSet.contains(id));
            assertTrue(stringSet.contains(id2));
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception occurred");
        }
    }

    @Test
    public void obtainAllChangeInitialElementsAndDetails_exceptionGettingInitialDetails() {
        String uuid = "uuid";
        TwcElementJson twcElementJson = mock(TwcElementJson.class);
        map.put(uuid, twcElementJson);
        String changedUUIDs = "changedUUID";
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        twcElementJsonList.add(twcElementJson);
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

        doReturn(changedUUIDs).when(addedChangedRemovedResults).getChangedString();
        doReturn(twcElementJsonList).when(addedChangedRemovedResults).getChangedInitial();
        doReturn(map).when(twcRevisionService).mapTwcElementJsonDataById(changedUUIDs, twcElementJsonList);
        doNothing().when(indexedChangesAndDetails).setChangedInitial(map);
        doReturn(map).when(indexedChangesAndDetails).getChangedInitial();
        doNothing().when(twcRevisionService).filterUnimportantElements(map);
        try {
            doThrow(integrationException).when(twcRevisionService).initialMissingDetailsForChangedElements(sourceRevision, indexedChangesAndDetails);

            twcRevisionService.obtainAllChangeInitialElementsAndDetails(sourceRevision, addedChangedRemovedResults, indexedChangesAndDetails);
            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertSame(error, e.getMessage());
            verify(twcRevisionService, never()).copyElementJsonMapping(anyMap());
        }
    }

    @Test
    public void obtainAllChangeInitialElementsAndDetails_exceptionGettingFurtherDetails() {
        String uuid = "uuid";
        TwcElementJson twcElementJson = mock(TwcElementJson.class);
        map.put(uuid, twcElementJson);
        String changedUUIDs = "changedUUID";
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        twcElementJsonList.add(twcElementJson);
        Map<String, TwcElementJson> changedInitialMissingDetails = new HashMap<>();
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

        doReturn(changedUUIDs).when(addedChangedRemovedResults).getChangedString();
        doReturn(twcElementJsonList).when(addedChangedRemovedResults).getChangedInitial();
        doReturn(map).when(twcRevisionService).mapTwcElementJsonDataById(changedUUIDs, twcElementJsonList);
        doNothing().when(indexedChangesAndDetails).setChangedInitial(map);
        doReturn(map).when(indexedChangesAndDetails).getChangedInitial();
        doNothing().when(twcRevisionService).filterUnimportantElements(map);
        Map<String, TwcElementJson> changedInitialMissingDetailsCopied = new HashMap<>(changedInitialMissingDetails);
        try {
            doReturn(changedInitialMissingDetails).when(twcRevisionService).initialMissingDetailsForChangedElements(sourceRevision, indexedChangesAndDetails);
            doNothing().when(indexedChangesAndDetails).setDetails(changedInitialMissingDetails);
            doReturn(changedInitialMissingDetails).when(indexedChangesAndDetails).getDetails();
            doReturn(changedInitialMissingDetailsCopied).when(twcRevisionService).copyElementJsonMapping(changedInitialMissingDetails);
            doThrow(integrationException).when(twcRevisionService).getFurtherMissingDetails(sourceRevision, changedInitialMissingDetailsCopied, indexedChangesAndDetails);

            twcRevisionService.obtainAllChangeInitialElementsAndDetails(sourceRevision, addedChangedRemovedResults, indexedChangesAndDetails);
            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertSame(error, e.getMessage());
            verify(twcRevisionService).copyElementJsonMapping(changedInitialMissingDetails);
        }
    }

    @Test
    public void obtainAllChangeInitialElementsAndDetails_elementsAndDetailsObtained() {
        String changedUUIDs = "changedUUID";
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        Map<String, TwcElementJson> changedInitialMissingDetails = new HashMap<>();
        Map<String, TwcElementJson> changedInitialMissingDetailsCopied = new HashMap<>(changedInitialMissingDetails);

        doReturn(changedUUIDs).when(addedChangedRemovedResults).getChangedString();
        doReturn(twcElementJsonList).when(addedChangedRemovedResults).getChangedInitial();
        doReturn(map).when(twcRevisionService).mapTwcElementJsonDataById(changedUUIDs, twcElementJsonList);
        doNothing().when(indexedChangesAndDetails).setChangedInitial(map);
        doReturn(map).when(indexedChangesAndDetails).getChangedInitial();
        doNothing().when(twcRevisionService).filterUnimportantElements(map);
        try {
            doReturn(changedInitialMissingDetails).when(twcRevisionService).initialMissingDetailsForChangedElements(sourceRevision, indexedChangesAndDetails);
            doNothing().when(indexedChangesAndDetails).setDetails(changedInitialMissingDetails);
            doReturn(changedInitialMissingDetails).when(indexedChangesAndDetails).getDetails();
            doReturn(changedInitialMissingDetailsCopied).when(twcRevisionService).copyElementJsonMapping(changedInitialMissingDetails);
            doNothing().when(twcRevisionService).getFurtherMissingDetails(sourceRevision, changedInitialMissingDetailsCopied, indexedChangesAndDetails);

            twcRevisionService.obtainAllChangeInitialElementsAndDetails(sourceRevision, addedChangedRemovedResults, indexedChangesAndDetails);
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception occurred");
        }
    }

    @Test
    public void obtainAllAddedRemovedAndChangeFinalElementsAndDetails_exceptionGettingInitialMissingDetails() {
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        String addedIds = "addedIds";
        String removedIds = "removedIds";
        String changedIds = "changedIds";
        Map<String, TwcElementJson> added = new HashMap<>();
        Map<String, TwcElementJson> removed = new HashMap<>();
        Map<String, TwcElementJson> changedFinal = new HashMap<>();
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

        when(addedChangedRemovedResults.getAddedString()).thenReturn(addedIds);
        when(addedChangedRemovedResults.getRemovedString()).thenReturn(removedIds);
        when(addedChangedRemovedResults.getChangedString()).thenReturn(changedIds);
        when(addedChangedRemovedResults.getAddedRemovedOrChangedFinal()).thenReturn(twcElementJsonList);
        doReturn(added).when(twcRevisionService).mapTwcElementJsonDataById(addedIds, twcElementJsonList);
        doReturn(removed).when(twcRevisionService).mapTwcElementJsonDataById(removedIds, twcElementJsonList);
        doReturn(changedFinal).when(twcRevisionService).mapTwcElementJsonDataById(changedIds, twcElementJsonList);
        doNothing().when(twcRevisionService).filterUnimportantElements(added);
        doNothing().when(twcRevisionService).filterUnimportantElements(removed);
        doNothing().when(twcRevisionService).filterUnimportantElements(changedFinal);
        try {
            doThrow(integrationException).when(twcRevisionService).initialMissingDetailsForAddedAndRemovedElements(targetRevision,
                    indexedChangesAndDetails);

            twcRevisionService.obtainAllAddedRemovedAndChangeFinalElementsAndDetails(targetRevision,
                    addedChangedRemovedResults, indexedChangesAndDetails);

            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertEquals(error, e.getMessage());
        }
    }

    @Test
    public void obtainAllAddedRemovedAndChangeFinalElementsAndDetails_exceptionGettingFurtherMissingDetails() {
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        String addedIds = "addedIds";
        String removedIds = "removedIds";
        String changedIds = "changedIds";
        Map<String, TwcElementJson> added = new HashMap<>();
        Map<String, TwcElementJson> removed = new HashMap<>();
        Map<String, TwcElementJson> changedFinal = new HashMap<>();
        Map<String, TwcElementJson> missingDetails = new HashMap<>();
        String key = "key";
        TwcElementJson value = mock(TwcElementJson.class);
        missingDetails.put(key, value);
        Map<String, TwcElementJson> missingDetailsCopy = new HashMap<>(missingDetails);
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

        when(addedChangedRemovedResults.getAddedString()).thenReturn(addedIds);
        when(addedChangedRemovedResults.getRemovedString()).thenReturn(removedIds);
        when(addedChangedRemovedResults.getChangedString()).thenReturn(changedIds);
        when(addedChangedRemovedResults.getAddedRemovedOrChangedFinal()).thenReturn(twcElementJsonList);
        doReturn(added).when(twcRevisionService).mapTwcElementJsonDataById(addedIds, twcElementJsonList);
        doReturn(removed).when(twcRevisionService).mapTwcElementJsonDataById(removedIds, twcElementJsonList);
        doReturn(changedFinal).when(twcRevisionService).mapTwcElementJsonDataById(changedIds, twcElementJsonList);
        doNothing().when(twcRevisionService).filterUnimportantElements(added);
        doNothing().when(twcRevisionService).filterUnimportantElements(removed);
        doNothing().when(twcRevisionService).filterUnimportantElements(changedFinal);
        try {
            doReturn(missingDetails).when(twcRevisionService).initialMissingDetailsForAddedAndRemovedElements(targetRevision,
                    indexedChangesAndDetails);
            when(indexedChangesAndDetails.isInAGivenMap(key)).thenReturn(false);
            doReturn(missingDetailsCopy).when(twcRevisionService).copyElementJsonMapping(missingDetails);
            doThrow(integrationException).when(twcRevisionService).getFurtherMissingDetails(targetRevision, missingDetailsCopy,
                    indexedChangesAndDetails);

            twcRevisionService.obtainAllAddedRemovedAndChangeFinalElementsAndDetails(targetRevision,
                    addedChangedRemovedResults, indexedChangesAndDetails);

            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertEquals(error, e.getMessage());
            assertTrue(missingDetails.containsKey(key));
            assertTrue(missingDetails.containsValue(value));
        }
    }

    @Test
    public void obtainAllAddedRemovedAndChangeFinalElementsAndDetails_allElementsAndDetailsObtained() {
        String key = "key";
        TwcElementJson value = mock(TwcElementJson.class);
        TwcElementJson twcElementJson = mock(TwcElementJson.class);
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        twcElementJsonList.add(twcElementJson);
        String addedIds = "addedIds";
        String removedIds = "removedIds";
        String changedIds = "changedIds";
        Map<String, TwcElementJson> added = new HashMap<>();
        Map<String, TwcElementJson> removed = new HashMap<>();
        Map<String, TwcElementJson> changedFinal = new HashMap<>();
        Map<String, TwcElementJson> missingDetails = new HashMap<>();
        missingDetails.put(key, value);
        Map<String, TwcElementJson> missingDetailsCopy = new HashMap<>(missingDetails);

        when(addedChangedRemovedResults.getAddedString()).thenReturn(addedIds);
        when(addedChangedRemovedResults.getRemovedString()).thenReturn(removedIds);
        when(addedChangedRemovedResults.getChangedString()).thenReturn(changedIds);
        when(addedChangedRemovedResults.getAddedRemovedOrChangedFinal()).thenReturn(twcElementJsonList);
        doReturn(added).when(twcRevisionService).mapTwcElementJsonDataById(addedIds, twcElementJsonList);
        doReturn(removed).when(twcRevisionService).mapTwcElementJsonDataById(removedIds, twcElementJsonList);
        doReturn(changedFinal).when(twcRevisionService).mapTwcElementJsonDataById(changedIds, twcElementJsonList);
        doNothing().when(twcRevisionService).filterUnimportantElements(added);
        doNothing().when(twcRevisionService).filterUnimportantElements(removed);
        doNothing().when(twcRevisionService).filterUnimportantElements(changedFinal);
        try {
            doReturn(missingDetails).when(twcRevisionService).initialMissingDetailsForAddedAndRemovedElements(targetRevision,
                    indexedChangesAndDetails);
            when(indexedChangesAndDetails.isInAGivenMap(key)).thenReturn(true);
            doReturn(missingDetailsCopy).when(twcRevisionService).copyElementJsonMapping(missingDetails);
            doNothing().when(twcRevisionService).getFurtherMissingDetails(targetRevision, missingDetailsCopy,
                    indexedChangesAndDetails);

            twcRevisionService.obtainAllAddedRemovedAndChangeFinalElementsAndDetails(targetRevision,
                    addedChangedRemovedResults, indexedChangesAndDetails);

            // assert content of missing details
            assertFalse(missingDetails.containsKey(key));
            assertFalse(missingDetails.containsValue(value));
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception occurred");
        }
    }

    @Test
    public void getElementChangesBetweenTwoRevisions_nullRevisionDifference() throws TWCIntegrationException {
        doReturn(null).when(twcRevisionService).getTwcRevisionDifference(sourceRevision, targetRevision);

        assertNull(twcRevisionService.getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision));
    }

    @Test
    public void getElementChangesBetweenTwoRevisions_emptyRevisionDifference() throws TWCIntegrationException {
        TwcRevisionDifferenceJson twcRevisionDifferenceJson = mock(TwcRevisionDifferenceJson.class);

        doReturn(twcRevisionDifferenceJson).when(twcRevisionService).getTwcRevisionDifference(sourceRevision, targetRevision);
        when(twcRevisionDifferenceJson.isEmpty()).thenReturn(true);

        assertNull(twcRevisionService.getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision));
    }

    @Test
    public void getElementChangesBetweenTwoRevisions_blankIds() throws TWCIntegrationException {
        String initialIds = "";
        String finalIds = "";
        TwcRevisionDifferenceJson twcRevisionDifferenceJson = mock(TwcRevisionDifferenceJson.class);

        doReturn(twcRevisionDifferenceJson).when(twcRevisionService).getTwcRevisionDifference(sourceRevision, targetRevision);
        when(twcRevisionDifferenceJson.isEmpty()).thenReturn(false);
        doReturn(addedChangedRemovedResults).when(twcRevisionService).getTwcAddedChangedRemovedResults(twcRevisionDifferenceJson);
        when(addedChangedRemovedResults.getChangedString()).thenReturn(initialIds);
        when(addedChangedRemovedResults.getAddedRemovedOrChangedFinalString()).thenReturn(finalIds);

        assertEquals(addedChangedRemovedResults, twcRevisionService.getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision));
        verify(addedChangedRemovedResults, never()).setChangedInitial(any());
        verify(addedChangedRemovedResults, never()).setAddedRemovedOrChangedFinal(any());
    }

    @Test
    public void getElementChangesBetweenTwoRevisions_nonBlankIds() throws TWCIntegrationException {
        String initialIds = "initialIds";
        String finalIds = "finalIds";
        TwcRevisionDifferenceJson twcRevisionDifferenceJson = mock(TwcRevisionDifferenceJson.class);
        List<TwcElementJson> changedInitial = List.of();
        List<TwcElementJson> changedFinal = List.of();

        doReturn(twcRevisionDifferenceJson).when(twcRevisionService).getTwcRevisionDifference(sourceRevision, targetRevision);
        when(twcRevisionDifferenceJson.isEmpty()).thenReturn(false);
        doReturn(addedChangedRemovedResults).when(twcRevisionService).getTwcAddedChangedRemovedResults(twcRevisionDifferenceJson);
        when(addedChangedRemovedResults.getChangedString()).thenReturn(initialIds);
        when(addedChangedRemovedResults.getAddedRemovedOrChangedFinalString()).thenReturn(finalIds);
        doReturn(changedInitial).when(twcRevisionService).getElementsUsingIds(initialIds, sourceRevision);
        doNothing().when(addedChangedRemovedResults).setChangedInitial(changedInitial);
        doReturn(changedFinal).when(twcRevisionService).getElementsUsingIds(finalIds, targetRevision);
        doNothing().when(addedChangedRemovedResults).setAddedRemovedOrChangedFinal(changedFinal);

        assertEquals(addedChangedRemovedResults, twcRevisionService.getElementChangesBetweenTwoRevisions(sourceRevision, targetRevision));
        verify(addedChangedRemovedResults).setChangedInitial(changedInitial);
        verify(addedChangedRemovedResults).setAddedRemovedOrChangedFinal(changedFinal);
    }

    @Test
    public void getTwcRevisionDifference_nullProjectUri() {
        when(teamworkCloudService.getProjectIdFromCurrentUri(apiDomain)).thenReturn(null);

        assertNull(twcRevisionService.getTwcRevisionDifference(sourceRevision, targetRevision));
    }

    @Test
    public void getTwcRevisionDifference_exceptionThrown() {
        String error = "error";
        TWCIntegrationException twcIntegrationException = spy(new TWCIntegrationException(error));

        try {
            doThrow(twcIntegrationException).when(teamworkCloudService).getRevisionDifference(apiDomain, projectId, sourceRevision, targetRevision);

            twcRevisionService.getTwcRevisionDifference(sourceRevision, targetRevision);

            verify(uiDomain).logError(logger, ExceptionConstants.UNABLE_TO_GET_TWC_REVISION, twcIntegrationException);
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getTwcRevisionDifference_differenceObtained() {
        TwcRevisionDifferenceJson twcRevisionDifferenceJson = mock(TwcRevisionDifferenceJson.class);

        try {
            when(teamworkCloudService.getRevisionDifference(apiDomain, projectId, sourceRevision, targetRevision)).thenReturn(twcRevisionDifferenceJson);

            assertEquals(twcRevisionDifferenceJson, twcRevisionService.getTwcRevisionDifference(sourceRevision, targetRevision));
            verify(uiDomain, never()).logError(any(), anyString(), any());
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getElementsUsingIds_nullProjectUri() throws TWCIntegrationException {
        String elementIds = "elementIds";
        when(teamworkCloudService.getProjectIdFromCurrentUri(apiDomain)).thenReturn(null);

        assertTrue(twcRevisionService.getElementsUsingIds(elementIds, sourceRevision).isEmpty());
        verify(teamworkCloudService, never()).getElementsAtRevision(any(), anyString(), anyString(), anyString());
    }

    @Test
    public void getElementsUsingIds_blankElementId() throws TWCIntegrationException {
        String elementIds = " ";

        assertTrue(twcRevisionService.getElementsUsingIds(elementIds, sourceRevision).isEmpty());
        verify(teamworkCloudService, never()).getElementsAtRevision(any(), anyString(), anyString(), anyString());
    }

    @Test
    public void getElementsUsingIds_elementsObtained() {
        String elementIds = "elementIds";
        TwcElementJson twcElementJson = mock(TwcElementJson.class);
        List<TwcElementJson> elements = List.of(twcElementJson);

        try {
            when(teamworkCloudService.getElementsAtRevision(apiDomain, projectId, sourceRevision, elementIds)).thenReturn(elements);

            assertTrue(twcRevisionService.getElementsUsingIds(elementIds, sourceRevision).contains(twcElementJson));
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getElementsUsingIds_exceptionThrown() {
        String elementIds = "elementIds";
        String error = "error";
        TWCIntegrationException exception = spy(new TWCIntegrationException(error));
        try {
            doThrow(exception).when(teamworkCloudService).getElementsAtRevision(apiDomain, projectId, sourceRevision, elementIds);

            twcRevisionService.getElementsUsingIds(elementIds, sourceRevision);

            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertSame(error, e.getMessage());
            verify(uiDomain).logError(logger, String.format(ExceptionConstants.UNABLE_TO_RETRIEVE_ELEMENTS_AT_REVISION, sourceRevision));
        }
    }

    @Test
    public void getSpecificForGivenGeneralizations_specificNull() {
        JsonObject esi = mock(JsonObject.class);

        when(elementJson.getEsiData()).thenReturn(esi);
        when(esi.getAsJsonObject()).thenReturn(esi);
        when(esi.get(JsonConstants.SPECIFIC_KEY)).thenReturn(null);

        assertNull(twcRevisionService.getSpecificForGivenGeneralizations(elementJson));
        verify(twcRevisionService, never()).getGson();
    }

    @Test
    public void getSpecificForGivenGeneralizations_specificNotJsonObject() {
        JsonObject esi = mock(JsonObject.class);

        when(elementJson.getEsiData()).thenReturn(esi);
        when(esi.getAsJsonObject()).thenReturn(esi);
        when(esi.get(JsonConstants.SPECIFIC_KEY)).thenReturn(mock(JsonElement.class));

        assertNull(twcRevisionService.getSpecificForGivenGeneralizations(elementJson));
        verify(twcRevisionService, never()).getGson();
    }

    @Test
    public void getSpecificForGivenGeneralizations_specificHasNoIdJson() {
        JsonObject esi = mock(JsonObject.class);
        JsonObject specific = mock(JsonObject.class);
        Gson gson = mock(Gson.class);

        when(elementJson.getEsiData()).thenReturn(esi);
        when(esi.getAsJsonObject()).thenReturn(esi);
        when(esi.get(JsonConstants.SPECIFIC_KEY)).thenReturn(specific);
        when(specific.isJsonObject()).thenReturn(true);
        doReturn(gson).when(twcRevisionService).getGson();
        when(gson.fromJson(specific, TwcIdJson.class)).thenReturn(null);

        assertNull(twcRevisionService.getSpecificForGivenGeneralizations(elementJson));
        verify(twcRevisionService).getGson();
    }

    @Test
    public void getSpecificForGivenGeneralizations_specificIdJsonHasNoId() {
        JsonObject esi = mock(JsonObject.class);
        JsonObject specific = mock(JsonObject.class);
        Gson gson = mock(Gson.class);
        TwcIdJson twcIdJson = mock(TwcIdJson.class);

        when(elementJson.getEsiData()).thenReturn(esi);
        when(esi.getAsJsonObject()).thenReturn(esi);
        when(esi.get(JsonConstants.SPECIFIC_KEY)).thenReturn(specific);
        doReturn(gson).when(twcRevisionService).getGson();
        when(gson.fromJson(specific, TwcIdJson.class)).thenReturn(twcIdJson);
        when(twcIdJson.getId()).thenReturn(null);

        assertNull(twcRevisionService.getSpecificForGivenGeneralizations(elementJson));
        verify(twcIdJson, atMostOnce()).getId();
    }

    @Test
    public void getSpecificForGivenGeneralizations_specificIdJsonHasBlankId() {
        JsonObject esi = mock(JsonObject.class);
        JsonObject specific = mock(JsonObject.class);
        Gson gson = mock(Gson.class);
        TwcIdJson twcIdJson = mock(TwcIdJson.class);

        when(elementJson.getEsiData()).thenReturn(esi);
        when(esi.getAsJsonObject()).thenReturn(esi);
        when(esi.get(JsonConstants.SPECIFIC_KEY)).thenReturn(specific);
        when(specific.isJsonObject()).thenReturn(true);
        doReturn(gson).when(twcRevisionService).getGson();
        when(gson.fromJson(specific, TwcIdJson.class)).thenReturn(twcIdJson);
        when(twcIdJson.getId()).thenReturn(" ");

        assertNull(twcRevisionService.getSpecificForGivenGeneralizations(elementJson));
        verify(twcIdJson, times(2)).getId();
    }

    @Test
    public void getSpecificForGivenGeneralizations_validSpecific() {
        JsonObject esi = mock(JsonObject.class);
        JsonObject specific = mock(JsonObject.class);
        Gson gson = mock(Gson.class);
        TwcIdJson twcIdJson = mock(TwcIdJson.class);
        String id = "id";

        when(elementJson.getEsiData()).thenReturn(esi);
        when(esi.getAsJsonObject()).thenReturn(esi);
        when(esi.get(JsonConstants.SPECIFIC_KEY)).thenReturn(specific);
        when(specific.isJsonObject()).thenReturn(true);
        doReturn(gson).when(twcRevisionService).getGson();
        when(gson.fromJson(specific, TwcIdJson.class)).thenReturn(twcIdJson);
        when(twcIdJson.getId()).thenReturn(id);

        assertEquals(twcIdJson, twcRevisionService.getSpecificForGivenGeneralizations(elementJson));
        assertEquals(id, twcIdJson.getId());
    }

    @Test
    public void determineImportantStereotypesUsingIds_noRelations() {
        String directedRelations = " ";

        doReturn(directedRelations).when(twcRevisionService).getAllDirectedRelationshipOfTargetForGivenStereotypes(map);
        try {
            twcRevisionService.determineImportantStereotypesUsingIds(sourceRevision, map, map);

            verify(twcRevisionService, never()).getElementsUsingIds(directedRelations, sourceRevision);
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void determineImportantStereotypesUsingIds_exceptionGettingRelations() {
        String directedRelations = "directedRelations";
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

        doReturn(directedRelations).when(twcRevisionService).getAllDirectedRelationshipOfTargetForGivenStereotypes(map);
        try {
            doThrow(integrationException).when(twcRevisionService).getElementsUsingIds(directedRelations, sourceRevision);

            twcRevisionService.determineImportantStereotypesUsingIds(sourceRevision, map, map);

            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertEquals(error, e.getMessage());
            verify(twcRevisionService, never()).createTwcIdJsonSet();
        }
    }

    @Test
    public void determineImportantStereotypesUsingIds_emptyTwcIdJson() {
        String id = "id";
        map.put(id, elementJson);
        String directedRelations = "relation";
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        twcElementJsonList.add(elementJson);
        Set<TwcIdJson> specifics = new HashSet<>();

        doReturn(directedRelations).when(twcRevisionService).getAllDirectedRelationshipOfTargetForGivenStereotypes(map);
        try {
            doReturn(twcElementJsonList).when(twcRevisionService).getElementsUsingIds(directedRelations, sourceRevision);
            doReturn(specifics).when(twcRevisionService).createTwcIdJsonSet();
            doReturn(true).when(twcRevisionService).isGeneralization(elementJson);
            doReturn(null).when(twcRevisionService).getSpecificForGivenGeneralizations(elementJson);

            twcRevisionService.determineImportantStereotypesUsingIds(sourceRevision, map, map);

            verify(twcRevisionService).createTwcIdJsonSet();
            verify(twcRevisionService, never()).createStringBuilder();
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void determineImportantStereotypesUsingIds_exceptionGettingStereotypesFromRelations() throws TWCIntegrationException {
        String id = "id";
        map.put(id, elementJson);
        String directedRelations = "relation";
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        twcElementJsonList.add(elementJson);
        Set<TwcIdJson> specifics = new HashSet<>();
        String builtString = "builtString";
        stringBuilder.append(builtString);
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

        doReturn(directedRelations).when(twcRevisionService).getAllDirectedRelationshipOfTargetForGivenStereotypes(map);
        try {
            doReturn(twcElementJsonList).when(twcRevisionService).getElementsUsingIds(directedRelations, sourceRevision);
            doReturn(specifics).when(twcRevisionService).createTwcIdJsonSet();
            doReturn(true).when(twcRevisionService).isGeneralization(elementJson);
            doReturn(twcIdJson).when(twcRevisionService).getSpecificForGivenGeneralizations(elementJson);
            doNothing().when(twcRevisionService).appendIdJsonCollection(specifics, stringBuilder);
            doThrow(integrationException).when(twcRevisionService).getElementsFromIdAndMapThemById(sourceRevision, builtString);

            twcRevisionService.determineImportantStereotypesUsingIds(sourceRevision, map, map);

            fail("Expected exception did not occur");
        } catch (TWCIntegrationException e) {
            assertEquals(error, e.getMessage());
            verify(twcRevisionService).createStringBuilder();
            // verify we do not recurse
            verify(twcRevisionService, atMostOnce()).determineImportantStereotypesUsingIds(anyString(), anyMap(), anyMap());
        }
    }

    @Test
    public void determineImportantStereotypesUsingIds_nullStereotypesFromRelations() {
        String id = "id";
        map.put(id, elementJson);
        String directedRelations = "relation";
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        twcElementJsonList.add(elementJson);
        Set<TwcIdJson> specifics = new HashSet<>();
        String builtString = "builtString";
        stringBuilder.append(builtString);

        doReturn(directedRelations).when(twcRevisionService).getAllDirectedRelationshipOfTargetForGivenStereotypes(map);
        try {
            doReturn(twcElementJsonList).when(twcRevisionService).getElementsUsingIds(directedRelations, sourceRevision);
            doReturn(specifics).when(twcRevisionService).createTwcIdJsonSet();
            doReturn(true).when(twcRevisionService).isGeneralization(elementJson);
            doReturn(twcIdJson).when(twcRevisionService).getSpecificForGivenGeneralizations(elementJson);
            doNothing().when(twcRevisionService).appendIdJsonCollection(specifics, stringBuilder);
            doReturn(null).when(twcRevisionService).getElementsFromIdAndMapThemById(sourceRevision, builtString);

            twcRevisionService.determineImportantStereotypesUsingIds(sourceRevision, map, map);

            verify(twcRevisionService).createStringBuilder();
            // verify we do not recurse
            verify(twcRevisionService, atMostOnce()).determineImportantStereotypesUsingIds(anyString(), anyMap(), anyMap());
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void determineImportantStereotypesUsingIds_emptyStereotypesFromRelations() {
        String id = "id";
        map.put(id, elementJson);
        String directedRelations = "relation";
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        twcElementJsonList.add(elementJson);
        Set<TwcIdJson> specifics = new HashSet<>();
        String builtString = "builtString";
        stringBuilder.append(builtString);

        doReturn(directedRelations).when(twcRevisionService).getAllDirectedRelationshipOfTargetForGivenStereotypes(map);
        try {
            doReturn(twcElementJsonList).when(twcRevisionService).getElementsUsingIds(directedRelations, sourceRevision);
            doReturn(specifics).when(twcRevisionService).createTwcIdJsonSet();
            doReturn(true).when(twcRevisionService).isGeneralization(elementJson);
            doReturn(twcIdJson).when(twcRevisionService).getSpecificForGivenGeneralizations(elementJson);
            doNothing().when(twcRevisionService).appendIdJsonCollection(specifics, stringBuilder);
            doReturn(new HashMap<>()).when(twcRevisionService).getElementsFromIdAndMapThemById(sourceRevision, builtString);

            twcRevisionService.determineImportantStereotypesUsingIds(sourceRevision, map, map);

            verify(twcRevisionService).createStringBuilder();
            // verify we do not recurse
            verify(twcRevisionService, atMostOnce()).determineImportantStereotypesUsingIds(anyString(), anyMap(), anyMap());
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void determineImportantStereotypesUsingIds_recurseOnceThenNoMoreRelations() {
        String id = "id";
        Map<String, TwcElementJson> currentStereotypes = new HashMap<>();
        Map<String, TwcElementJson> moreStereotypes = new HashMap<>();
        moreStereotypes.put(id, elementJson);
        String directedRelations = "relation";
        List<TwcElementJson> twcElementJsonList = new ArrayList<>();
        twcElementJsonList.add(elementJson);
        Set<TwcIdJson> specifics = new HashSet<>();
        String builtString = "builtString";
        stringBuilder.append(builtString);

        doReturn(directedRelations).when(twcRevisionService).getAllDirectedRelationshipOfTargetForGivenStereotypes(currentStereotypes);
        try {
            doReturn(twcElementJsonList).when(twcRevisionService).getElementsUsingIds(directedRelations, sourceRevision);
            doReturn(specifics).when(twcRevisionService).createTwcIdJsonSet();
            doReturn(true).when(twcRevisionService).isGeneralization(elementJson);
            doReturn(twcIdJson).when(twcRevisionService).getSpecificForGivenGeneralizations(elementJson);
            doNothing().when(twcRevisionService).appendIdJsonCollection(specifics, stringBuilder);
            doReturn(moreStereotypes).when(twcRevisionService).getElementsFromIdAndMapThemById(sourceRevision, builtString);
            doReturn(PluginConstant.EMPTY_STRING).when(twcRevisionService).getAllDirectedRelationshipOfTargetForGivenStereotypes(moreStereotypes);

            assertFalse(map.containsKey(id));
            assertFalse(map.containsValue(elementJson));
            twcRevisionService.determineImportantStereotypesUsingIds(sourceRevision, currentStereotypes, map);
            assertTrue(map.containsKey(id));
            assertTrue(map.containsValue(elementJson));

            // verify we do recurse, but only one time
            verify(twcRevisionService).determineImportantStereotypesUsingIds(sourceRevision, moreStereotypes, map);
            verify(twcRevisionService, atMostOnce()).createTwcIdJsonSet();
        } catch (TWCIntegrationException e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void mapTwcElementJsonDataById_nullValidIds() {
        assertTrue(twcRevisionService.mapTwcElementJsonDataById(null, null).isEmpty());
    }

    @Test
    public void mapTwcElementJsonDataById_blankValidIds() {
        String ids = " ";

        assertTrue(twcRevisionService.mapTwcElementJsonDataById(ids, null).isEmpty());
    }

    @Test
    public void mapTwcElementJsonDataById_nullItem() {
        String validIds = "validIds";
        List<TwcElementJson> data = new ArrayList<>();
        data.add(null);

        assertTrue(twcRevisionService.mapTwcElementJsonDataById(validIds, data).isEmpty());
    }

    @Test
    public void mapTwcElementJsonDataById_nullId() {
        String validIds = "validIds";
        List<TwcElementJson> data = new ArrayList<>();

        when(elementJson.getEsiId()).thenReturn(null);
        assertTrue(twcRevisionService.mapTwcElementJsonDataById(validIds, data).isEmpty());
    }

    @Test
    public void mapTwcElementJsonDataById_blankId() {
        String validIds = "validIds";
        List<TwcElementJson> data = new ArrayList<>();
        data.add(elementJson);

        when(elementJson.getEsiId()).thenReturn(" ");
        assertTrue(twcRevisionService.mapTwcElementJsonDataById(validIds, data).isEmpty());
    }

    @Test
    public void mapTwcElementJsonDataById_notAValidId() {
        String validIds = "validIds";
        List<TwcElementJson> data = new ArrayList<>();
        data.add(elementJson);

        when(elementJson.getEsiId()).thenReturn("z");
        assertTrue(twcRevisionService.mapTwcElementJsonDataById(validIds, data).isEmpty());
    }

    @Test
    public void mapTwcElementJsonDataById_success() {
        String validIds = "validIds";
        List<TwcElementJson> data = new ArrayList<>();
        data.add(elementJson);
        when(elementJson.getEsiId()).thenReturn(validIds);
        Map<String, TwcElementJson> result = twcRevisionService.mapTwcElementJsonDataById(validIds, data);

        assertTrue(result.containsKey(validIds));
        assertTrue(result.containsValue(elementJson));
    }

    @Test
    public void appendIdJsonCollection_nullIdJson() {
        StringBuilder idsToGrab = spy(new StringBuilder());
        List<TwcIdJson> ids = new ArrayList<>();
        ids.add(null);

        twcRevisionService.appendIdJsonCollection(ids, idsToGrab);
        verify(idsToGrab, never()).append(anyString());
    }

    @Test
    public void appendIdJsonCollection_nullId() {
        StringBuilder idsToGrab = spy(new StringBuilder());
        TwcIdJson twcIdJson = mock(TwcIdJson.class);
        List<TwcIdJson> ids = new ArrayList<>();
        ids.add(twcIdJson);

        when(twcIdJson.getId()).thenReturn(null);
        twcRevisionService.appendIdJsonCollection(ids, idsToGrab);
        verify(idsToGrab, never()).append(anyString());
    }

    @Test
    public void appendIdJsonCollection_() {
        StringBuilder idsToGrab = spy(new StringBuilder());
        TwcIdJson twcIdJson = mock(TwcIdJson.class);
        List<TwcIdJson> ids = new ArrayList<>();
        String id = "id";
        ids.add(twcIdJson);

        when(twcIdJson.getId()).thenReturn(id);
        twcRevisionService.appendIdJsonCollection(ids, idsToGrab);
        assertEquals(id + PluginConstant.COMMA, idsToGrab.toString());
    }

    @Test
    public void prepareStringForBatchCall_zeroLengthString() {
        StringBuilder ids = new StringBuilder();

        assertTrue(twcRevisionService.prepareStringForBatchCall(ids).isEmpty());
    }

    @Test
    public void prepareStringForBatchCall_noCommaAtEnd() {
        StringBuilder ids = new StringBuilder();
        String id = "id";
        ids.append(id);

        assertEquals(id, twcRevisionService.prepareStringForBatchCall(ids));
    }

    @Test
    public void prepareStringForBatchCall_commaAtEnd() {
        StringBuilder ids = new StringBuilder();
        String id = "id";
        ids.append(id).append(PluginConstant.COMMA_CHAR);

        assertEquals(id, twcRevisionService.prepareStringForBatchCall(ids));
    }

    @Test
    public void iterateMapForDetailsToGrabLater_allImmediateFailureStateEntries() {
        TwcElementJson nullType = mock(TwcElementJson.class);
        TwcElementJson blankType = mock(TwcElementJson.class);
        TwcElementJson nullEsiData = mock(TwcElementJson.class);
        TwcElementJson esiLacksId = mock(TwcElementJson.class);
        String type = "type";
        JsonObject esi = mock(JsonObject.class);
        map.put("nullJson", null);
        map.put("nullType", nullType);
        map.put("blankType", blankType);
        map.put("nullEsiData", nullEsiData);
        map.put("esiLacksId", esiLacksId);

        when(nullType.getType()).thenReturn(null);
        when(blankType.getType()).thenReturn(" ");
        when(nullEsiData.getType()).thenReturn(type);
        when(esiLacksId.getType()).thenReturn(type);
        when(nullEsiData.getEsiData()).thenReturn(null);
        when(esiLacksId.getEsiData()).thenReturn(esi);
        when(esi.getAsJsonObject()).thenReturn(esi);
        when(esi.get(JsonConstants.LOCAL_ID_KEY)).thenReturn(null);

        twcRevisionService.iterateMapForDetailsToGrabLater(map, new StringBuilder(), indexedChangesAndDetails);

        verify(twcRevisionService, never()).areElementDetailsUnimportant(anyString());
    }

    @Test
    public void iterateMapForDetailsToGrabLater_elementDetailsUnimportant() {
        String type = "type";
        String id = "id";
        JsonObject esi = mock(JsonObject.class);
        map.put(id, elementJson);

        when(elementJson.getType()).thenReturn(type);
        when(elementJson.getEsiData()).thenReturn(esi);
        when(esi.getAsJsonObject()).thenReturn(esi);
        when(esi.get(JsonConstants.LOCAL_ID_KEY)).thenReturn(mock(JsonElement.class));
        doReturn(true).when(twcRevisionService).areElementDetailsUnimportant(type);

        twcRevisionService.iterateMapForDetailsToGrabLater(map, new StringBuilder(), indexedChangesAndDetails);

        verify(twcRevisionService).areElementDetailsUnimportant(type);
        verify(twcRevisionService, never()).createTwcIdJsonSet();
    }

    @Test
    public void iterateMapForDetailsToGrabLater_elementDetailsImportantButNotTaggedValue() {
        String type = "type";
        String id = "id";
        JsonObject esi = mock(JsonObject.class);
        map.put(id, elementJson);
        Set<TwcIdJson> idJsonSet = new HashSet<>();

        when(elementJson.getType()).thenReturn(type);
        when(elementJson.getEsiData()).thenReturn(esi);
        when(esi.getAsJsonObject()).thenReturn(esi);
        when(esi.get(JsonConstants.LOCAL_ID_KEY)).thenReturn(mock(JsonElement.class));
        doReturn(false).when(twcRevisionService).areElementDetailsUnimportant(type);
        doReturn(idJsonSet).when(twcRevisionService).createTwcIdJsonSet();
        doNothing().when(twcRevisionService).getTypicalDetails(elementJson, idJsonSet);
        doNothing().when(twcRevisionService).filterDetailsAlreadyObtained(idJsonSet, indexedChangesAndDetails);
        doNothing().when(twcRevisionService).appendIdJsonCollection(idJsonSet, stringBuilder);

        twcRevisionService.iterateMapForDetailsToGrabLater(map, stringBuilder, indexedChangesAndDetails);

        verify(twcRevisionService).appendIdJsonCollection(idJsonSet, stringBuilder);
        verify(twcRevisionService, never()).getElementTagValueIds(elementJson, idJsonSet);
    }

    @Test
    public void iterateMapForDetailsToGrabLater_elementDetailsImportantAndTaggedValue() {
        String type = JsonConstants.ELEMENT_TAGGED_VALUE;
        String id = "id";
        JsonObject esi = mock(JsonObject.class);
        map.put(id, elementJson);
        Set<TwcIdJson> idJsonSet = new HashSet<>();

        when(elementJson.getType()).thenReturn(type);
        when(elementJson.getEsiData()).thenReturn(esi);
        when(esi.getAsJsonObject()).thenReturn(esi);
        when(esi.get(JsonConstants.LOCAL_ID_KEY)).thenReturn(mock(JsonElement.class));
        doReturn(false).when(twcRevisionService).areElementDetailsUnimportant(type);
        doReturn(idJsonSet).when(twcRevisionService).createTwcIdJsonSet();
        doNothing().when(twcRevisionService).getElementTagValueIds(elementJson, idJsonSet);
        doNothing().when(twcRevisionService).getTypicalDetails(elementJson, idJsonSet);
        doNothing().when(twcRevisionService).filterDetailsAlreadyObtained(idJsonSet, indexedChangesAndDetails);
        doNothing().when(twcRevisionService).appendIdJsonCollection(idJsonSet, stringBuilder);

        twcRevisionService.iterateMapForDetailsToGrabLater(map, stringBuilder, indexedChangesAndDetails);

        verify(twcRevisionService).appendIdJsonCollection(idJsonSet, stringBuilder);
        verify(twcRevisionService).getElementTagValueIds(elementJson, idJsonSet);
    }

    @Test
    public void getFurtherMissingDetails_noIdsToGrab() throws TWCIntegrationException {
        Map<String, TwcElementJson> newDetails = new HashMap<>();
        String id = "id1";

        doReturn(stringBuilder).when(twcRevisionService).createStringBuilder();
        doNothing().when(twcRevisionService).iterateMapForDetailsToGrabLater(newDetails, stringBuilder, indexedChangesAndDetails);
        doReturn(id).when(twcRevisionService).prepareStringForBatchCall(stringBuilder);

        twcRevisionService.getFurtherMissingDetails(sourceRevision, newDetails, indexedChangesAndDetails);

        verify(twcRevisionService, never()).getElementsFromIdAndMapThemById(sourceRevision, id);
    }

    @Test
    public void getFurtherMissingDetails_exceptionGettingDetails() throws TWCIntegrationException {
        Map<String, TwcElementJson> newDetails = new HashMap<>();
        String id = "id1";
        stringBuilder.append(id);
        String error = "error";
        TWCIntegrationException integrationException = spy(new TWCIntegrationException(error));

        doReturn(stringBuilder).when(twcRevisionService).createStringBuilder();
        doNothing().when(twcRevisionService).iterateMapForDetailsToGrabLater(newDetails, stringBuilder, indexedChangesAndDetails);
        doReturn(id).when(twcRevisionService).prepareStringForBatchCall(stringBuilder);
        try {
            doThrow(integrationException).when(twcRevisionService).getElementsFromIdAndMapThemById(sourceRevision, id);

            twcRevisionService.getFurtherMissingDetails(sourceRevision, newDetails, indexedChangesAndDetails);
        } catch (TWCIntegrationException e) {
            assertEquals(error, e.getMessage());
            // verify we do not recurse
            verify(twcRevisionService, atMostOnce()).getFurtherMissingDetails(anyString(), anyMap(), any());
        }
    }

    @Test
    public void getFurtherMissingDetails_recurseOnceThenNoMoreDetails() throws TWCIntegrationException {
        Map<String, TwcElementJson> newDetails = new HashMap<>();
        String id = "id1";
        stringBuilder.append(id);
        Map<String, TwcElementJson> newlyFoundDetails = new HashMap<>();
        newlyFoundDetails.put(id, elementJson);
        StringBuilder emptyBuilder = new StringBuilder();
        indexedChangesAndDetails.setDetails(map);

        doReturn(stringBuilder).doReturn(emptyBuilder).when(twcRevisionService).createStringBuilder();
        doNothing().when(twcRevisionService).iterateMapForDetailsToGrabLater(newDetails, stringBuilder, indexedChangesAndDetails);
        doNothing().when(twcRevisionService).iterateMapForDetailsToGrabLater(newlyFoundDetails, emptyBuilder, indexedChangesAndDetails);
        doReturn(id).when(twcRevisionService).prepareStringForBatchCall(stringBuilder);
        doReturn(newlyFoundDetails).when(twcRevisionService).getElementsFromIdAndMapThemById(sourceRevision, id);

        assertFalse(map.containsKey(id));
        assertFalse(map.containsValue(elementJson));
        twcRevisionService.getFurtherMissingDetails(sourceRevision, newDetails, indexedChangesAndDetails);
        assertTrue(map.containsKey(id));
        assertTrue(map.containsValue(elementJson));
    }

    @Test
    public void getTypicalDetails() {
        JsonObject jsonObject = mock(JsonObject.class);
        JsonElement jsonElement = mock(JsonElement.class);
        JsonElement appliedStereotypeArray = mock(JsonElement.class);
        JsonElement ownerArray = mock(JsonElement.class);
        JsonElement ownedElementArray = mock(JsonElement.class);
        JsonElement taggedValueArray = mock(JsonElement.class);
        Set<TwcIdJson> appliedStereotypeIds = mock(Set.class);
        Set<TwcIdJson> ids = mock(Set.class);

        doReturn(jsonElement).when(elementJson).getEsiData();
        doReturn(jsonObject).when(jsonElement).getAsJsonObject();
        doReturn(appliedStereotypeArray).when(jsonObject).get(JsonConstants.APPLIED_STEREOTYPE);
        doReturn(appliedStereotypeIds).when(twcRevisionService).createTwcIdJsonSet();
        doNothing().when(twcRevisionService).getIdJsonFromArray(appliedStereotypeIds, appliedStereotypeArray);
        doReturn(ownerArray).when(twcRevisionService).selectivelyGetOwnerArrayBasedOnAppliedStereotypes(elementJson, appliedStereotypeIds);
        doNothing().when(twcRevisionService).getIdJsonFromArray(ids, ownerArray);
        doReturn(ownedElementArray).when(jsonObject).get(JsonConstants.OWNED_ELEMENT);
        doReturn(taggedValueArray).when(jsonObject).get(JsonConstants.TAGGED_VALUE_ARRAY_KEY);
        doNothing().when(twcRevisionService).getIdJsonFromArray(ids, ownedElementArray);
        doNothing().when(twcRevisionService).getIdJsonFromArray(ids, taggedValueArray);

        twcRevisionService.getTypicalDetails(elementJson, ids);
        verify(twcRevisionService).getIdJsonFromArray(appliedStereotypeIds, appliedStereotypeArray);
        verify(twcRevisionService).getIdJsonFromArray(ids, ownerArray);
        verify(twcRevisionService).getIdJsonFromArray(ids, ownedElementArray);
        verify(twcRevisionService).getIdJsonFromArray(ids, taggedValueArray);
    }

    @Test
    public void getTypicalDetails_ownerArrayNull() {
        JsonObject jsonObject = mock(JsonObject.class);
        JsonElement jsonElement = mock(JsonElement.class);
        JsonElement appliedStereotypeArray = mock(JsonElement.class);
        JsonElement ownerArray = mock(JsonElement.class);
        JsonElement ownedElementArray = mock(JsonElement.class);
        JsonElement taggedValueArray = mock(JsonElement.class);
        Set<TwcIdJson> appliedStereotypeIds = mock(Set.class);
        Set<TwcIdJson> ids = mock(Set.class);

        doReturn(jsonElement).when(elementJson).getEsiData();
        doReturn(jsonObject).when(jsonElement).getAsJsonObject();
        doReturn(appliedStereotypeArray).when(jsonObject).get(JsonConstants.APPLIED_STEREOTYPE);
        doReturn(appliedStereotypeIds).when(twcRevisionService).createTwcIdJsonSet();
        doNothing().when(twcRevisionService).getIdJsonFromArray(appliedStereotypeIds, appliedStereotypeArray);
        doReturn(null).when(twcRevisionService).selectivelyGetOwnerArrayBasedOnAppliedStereotypes(elementJson, appliedStereotypeIds);
        doReturn(ownedElementArray).when(jsonObject).get(JsonConstants.OWNED_ELEMENT);
        doReturn(taggedValueArray).when(jsonObject).get(JsonConstants.TAGGED_VALUE_ARRAY_KEY);
        doNothing().when(twcRevisionService).getIdJsonFromArray(ids, ownedElementArray);
        doNothing().when(twcRevisionService).getIdJsonFromArray(ids, taggedValueArray);

        twcRevisionService.getTypicalDetails(elementJson, ids);
        verify(twcRevisionService).getIdJsonFromArray(appliedStereotypeIds, appliedStereotypeArray);
        verify(twcRevisionService, never()).getIdJsonFromArray(ids, ownerArray);
        verify(twcRevisionService).getIdJsonFromArray(ids, ownedElementArray);
        verify(twcRevisionService).getIdJsonFromArray(ids, taggedValueArray);
    }

    @Test
    public void getIdJsonFromArray_notAnArray() {
        JsonElement jsonElement = mock(JsonElement.class);
        Set<TwcIdJson> ids = new HashSet<>();

        when(jsonElement.isJsonArray()).thenReturn(false);

        twcRevisionService.getIdJsonFromArray(ids, jsonElement);

        assertTrue(ids.isEmpty());
    }

    @Test
    public void getIdJsonFromArray_noValidItemsInArray() {
        JsonArray jsonArray = spy(new JsonArray());
        JsonElement nullItem = mock(JsonElement.class);
        JsonElement nullId = mock(JsonElement.class);
        JsonElement blankId = mock(JsonElement.class);
        jsonArray.add(nullItem);
        jsonArray.add(nullId);
        jsonArray.add(blankId);
        Gson gson = mock(Gson.class);
        TwcIdJson jsonWithNullId = mock(TwcIdJson.class);
        TwcIdJson jsonWithBlankId = mock(TwcIdJson.class);
        Set<TwcIdJson> ids = new HashSet<>();

        doReturn(gson).when(twcRevisionService).getGson();
        when(gson.fromJson(nullItem, TwcIdJson.class)).thenReturn(null);
        when(gson.fromJson(nullId, TwcIdJson.class)).thenReturn(jsonWithNullId);
        when(jsonWithNullId.getId()).thenReturn(null);
        when(gson.fromJson(blankId, TwcIdJson.class)).thenReturn(jsonWithBlankId);
        when(jsonWithBlankId.getId()).thenReturn(" ");

        twcRevisionService.getIdJsonFromArray(ids, jsonArray);

        assertTrue(ids.isEmpty());
    }

    @Test
    public void getIdJsonFromArray_hasValidItem() {
        JsonArray jsonArray = spy(new JsonArray());
        JsonElement validItem = mock(JsonElement.class);
        jsonArray.add(validItem);
        Gson gson = mock(Gson.class);
        Set<TwcIdJson> ids = new HashSet<>();
        String id = "id";

        doReturn(gson).when(twcRevisionService).getGson();
        when(gson.fromJson(validItem, TwcIdJson.class)).thenReturn(twcIdJson);
        when(twcIdJson.getId()).thenReturn(id);

        twcRevisionService.getIdJsonFromArray(ids, jsonArray);

        assertTrue(ids.contains(twcIdJson));
    }

    @Test
    public void selectivelyGetOwnerArrayBasedOnAppliedStereotypes_importantStereotype() {
        String id = "id";
        Set<String> importantStereotypeIds = new HashSet<>();
        importantStereotypeIds.add(id);
        Set<TwcIdJson> appliedStereotypesIds = new HashSet<>();
        appliedStereotypesIds.add(twcIdJson);

        doReturn(importantStereotypeIds).when(twcRevisionService).getImportantStereotypeIds();
        when(twcIdJson.getId()).thenReturn(id);

        assertNull(twcRevisionService.selectivelyGetOwnerArrayBasedOnAppliedStereotypes(elementJson, appliedStereotypesIds));
    }

    @Test
    public void selectivelyGetOwnerArrayBasedOnAppliedStereotypes_unimportantStereotype() {
        String id = "id";
        Set<String> importantStereotypeIds = new HashSet<>();
        importantStereotypeIds.add(id);
        Set<TwcIdJson> appliedStereotypesIds = new HashSet<>();
        appliedStereotypesIds.add(twcIdJson);
        JsonObject esiData = mock(JsonObject.class);
        JsonElement owner = mock(JsonElement.class);

        doReturn(importantStereotypeIds).when(twcRevisionService).getImportantStereotypeIds();
        when(twcIdJson.getId()).thenReturn("");
        when(elementJson.getEsiData()).thenReturn(esiData);
        when(esiData.getAsJsonObject()).thenReturn(esiData);
        when(esiData.get(JsonConstants.OWNER)).thenReturn(owner);

        assertEquals(owner, twcRevisionService.selectivelyGetOwnerArrayBasedOnAppliedStereotypes(elementJson, appliedStereotypesIds));
    }

    @Test
    public void filterDetailsAlreadyObtained_nullId() {
        twcIdJson = spy(new TwcIdJson());
        Set<TwcIdJson> set = new HashSet<>();
        set.add(twcIdJson);

        twcRevisionService.filterDetailsAlreadyObtained(set, indexedChangesAndDetails);
        assertTrue(set.contains(twcIdJson));
    }

    @Test
    public void filterDetailsAlreadyObtained_blankId() {
        twcIdJson = spy(new TwcIdJson());
        twcIdJson.setId(" ");
        Set<TwcIdJson> set = new HashSet<>();
        set.add(twcIdJson);

        twcRevisionService.filterDetailsAlreadyObtained(set, indexedChangesAndDetails);
        assertTrue(set.contains(twcIdJson));
    }

    @Test
    public void filterDetailsAlreadyObtained_notInGivenMap() {
        twcIdJson = spy(new TwcIdJson());
        Set<TwcIdJson> set = new HashSet<>();
        String id = "id";
        twcIdJson.setId(id);
        set.add(twcIdJson);

        when(indexedChangesAndDetails.isInAGivenMap(id)).thenReturn(false);
        twcRevisionService.filterDetailsAlreadyObtained(set, indexedChangesAndDetails);
        assertTrue(set.contains(twcIdJson));
    }

    @Test
    public void filterDetailsAlreadyObtained_idJsonFiltered() {
        twcIdJson = spy(new TwcIdJson());
        Set<TwcIdJson> set = new HashSet<>();
        String id = "id";
        twcIdJson.setId(id);
        set.add(twcIdJson);

        when(indexedChangesAndDetails.isInAGivenMap(id)).thenReturn(true);
        twcRevisionService.filterDetailsAlreadyObtained(set, indexedChangesAndDetails);
        assertFalse(set.contains(twcIdJson));
    }

    @Test
    public void filterUnimportantElements_nullType() {
        map.put("id", elementJson);

        when(elementJson.getType()).thenReturn(null);

        assertTrue(map.containsValue(elementJson));
        twcRevisionService.filterUnimportantElements(map);
        assertFalse(map.containsValue(elementJson));
    }

    @Test
    public void filterUnimportantElements_blankType() {
        map.put("id", elementJson);

        when(elementJson.getType()).thenReturn(" ");

        assertTrue(map.containsValue(elementJson));
        twcRevisionService.filterUnimportantElements(map);
        assertFalse(map.containsValue(elementJson));
    }

    @Test
    public void filterUnimportantElements_validIdButUnimportant() {
        map.put("id", elementJson);
        String type = "type";

        when(elementJson.getType()).thenReturn(type);
        doReturn(true).when(twcRevisionService).areElementDetailsUnimportant(type);

        assertTrue(map.containsValue(elementJson));
        twcRevisionService.filterUnimportantElements(map);
        assertFalse(map.containsValue(elementJson));
    }

    @Test
    public void filterUnimportantElements_importantElement() {
        map.put("id", elementJson);
        String type = "type";

        when(elementJson.getType()).thenReturn(type);
        doReturn(false).when(twcRevisionService).areElementDetailsUnimportant(type);

        assertTrue(map.containsValue(elementJson));
        twcRevisionService.filterUnimportantElements(map);
        assertTrue(map.containsValue(elementJson));
    }

    @Test
    public void areElementDetailsUnimportant_packageType() {
        assertTrue(twcRevisionService.areElementDetailsUnimportant(JsonConstants.PACKAGE_TYPE));
    }

    @Test
    public void areElementDetailsUnimportant_stereotypeType() {
        assertTrue(twcRevisionService.areElementDetailsUnimportant(JsonConstants.STEREOTYPE_TYPE));
    }

    @Test
    public void areElementDetailsUnimportant_diagramType() {
        assertTrue(twcRevisionService.areElementDetailsUnimportant(JsonConstants.DIAGRAM_TYPE));
    }

    @Test
    public void areElementDetailsUnimportant_profileType() {
        assertTrue(twcRevisionService.areElementDetailsUnimportant(JsonConstants.PROFILE_TYPE));
    }

    @Test
    public void areElementDetailsUnimportant_esiProjectTypePrefix() {
        assertTrue(twcRevisionService.areElementDetailsUnimportant(JsonConstants.ESI_PROJECT_TYPE_PREFIX));
    }

    @Test
    public void areElementDetailsUnimportant_noTypeEquals() {
        String wrongType = "wrongType";

        assertFalse(twcRevisionService.areElementDetailsUnimportant(wrongType));
    }
}
