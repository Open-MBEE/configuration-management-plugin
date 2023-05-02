package org.openmbee.plugin.cfgmgmt.domain;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.utils.ChangeRecordComparatorForHistory;
import org.openmbee.plugin.cfgmgmt.utils.RevisionRecordComparatorForRevision;
import org.openmbee.plugin.cfgmgmt.view.ElementHistoryRowView;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestChangeRecordDomain {
	private ConfiguredElement configuredElement;
	private LifecycleObjectFactory lifecycleObjectFactory;
	private ApiDomain apiDomain;
	private UIDomain uiDomain;
	private Element element;
	private Stereotype stereotype;
	private ConfigurationManagementService configurationManagementService;
	private ChangeRecord changeRecord;
	private LifecycleStatus lifecycleStatus;
	private LifecycleStatus oldLifecycleStatus;
	private LifecycleTransition lifecycleTransition;
	private ChangeRecordDomain changeRecordDomain;
	private Logger logger;
	private RevisionHistoryRecord revisionHistoryRecord;
	private ZonedDateTime createTime1;
	private ZonedDateTime releaseTime1;
	private ZonedDateTime createTime2;
	private ZonedDateTime releaseTime2;
	private RevisionRecordComparatorForRevision revisionRecordComparatorForRevision;
	private String revision;

	@Before
	public void setUp() {
		lifecycleTransition = mock(LifecycleTransition.class);
		lifecycleStatus = mock(LifecycleStatus.class);
		oldLifecycleStatus = mock(LifecycleStatus.class);
		changeRecord = mock(ChangeRecord.class);
		configuredElement = mock(ConfiguredElement.class);
		lifecycleObjectFactory = mock(LifecycleObjectFactory.class);
		revisionHistoryRecord = mock(RevisionHistoryRecord.class);
		apiDomain = mock(ApiDomain.class);
		uiDomain = mock(UIDomain.class);
		element = mock(Element.class);
		stereotype = mock(Stereotype.class);
		configurationManagementService = mock(ConfigurationManagementService.class);
		changeRecordDomain = Mockito.spy(new ChangeRecordDomain(lifecycleObjectFactory, apiDomain, uiDomain));
		revision = "-";
		createTime1 = spy(ZonedDateTime.now());
		releaseTime1 = spy(createTime1.plusSeconds(5L));
		createTime2 = spy(releaseTime1.plusSeconds(5L));
		releaseTime2 = spy(createTime2.plusSeconds(5L));

		revisionRecordComparatorForRevision = mock(RevisionRecordComparatorForRevision.class);

		doReturn(logger).when(changeRecordDomain).getLogger();
		doReturn(revisionRecordComparatorForRevision).when(changeRecordDomain).getRevisionComparator();
	}

	@Test
	public void getAffectedElements() {
		configuredElement = setupSpiedConfiguredElement();
		List<Element> elementList = new ArrayList<>();
		elementList.add(element);
		when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS)).thenReturn((List) elementList);
		when(configurationManagementService.getLifecycleObjectFactory()).thenReturn(lifecycleObjectFactory);
		when(lifecycleObjectFactory.getConfiguredElement(configurationManagementService, element)).thenReturn(configuredElement);

		List<ConfiguredElement> results = changeRecordDomain.getAffectedElements(element, stereotype, configurationManagementService);

		assertFalse(results.isEmpty());
		assertEquals(configuredElement, results.get(0));
		assertEquals(configuredElement.getElement(), results.get(0).getElement());
	}

	private ConfiguredElement setupSpiedConfiguredElement() {
		LifecycleObjectDomain lifecycleObjectDomain = mock(LifecycleObjectDomain.class);

		doReturn(lifecycleObjectDomain).when(configurationManagementService).getLifecycleObjectDomain();
		doReturn(stereotype).when(configurationManagementService).getBaseCEStereotype();

		return spy(new ConfiguredElement(configurationManagementService, element));
	}

	@Test
	public void getAffectedElements_EmptyElementList() {
		when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS)).thenReturn(null);
		assertTrue(changeRecordDomain.getAffectedElements(element, stereotype, configurationManagementService).isEmpty());
	}

	@Test
	public void getReleaseTransitionsForElements_NullElements() {
		Map<ConfiguredElement, LifecycleStatus> results = changeRecordDomain.getReleaseTransitionsForElements(
				changeRecord, null);
		assertTrue(results.isEmpty());
		verify(configuredElement, never()).getTransitions();
	}

	@Test
	public void getReleaseTransitionsForElements_ChangeRecordNull() {
		Map<ConfiguredElement, LifecycleStatus> results = changeRecordDomain.getReleaseTransitionsForElements(
			null, new ArrayList<>());
		assertTrue(results.isEmpty());
		verify(configuredElement, never()).getTransitions();
	}

	@Test
	public void getReleaseTransitionsForElements_noReleasingTransition() {
		List<LifecycleTransition> transitions = new ArrayList<>();
		transitions.add(lifecycleTransition);
		List<ConfiguredElement> elements = new ArrayList<>();
		elements.add(configuredElement);
		String id = "id";
		String qualifiedName = "qualifiedName";
		String message = String.format(ExceptionConstants.NO_RELEASING_TRANSITION_FOUND, qualifiedName, id);

		when(configuredElement.getTransitions()).thenReturn(transitions);
		when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
		when(lifecycleStatus.isReleased()).thenReturn(false);
		when(configuredElement.getQualifiedName()).thenReturn(qualifiedName);
		when(configuredElement.getID()).thenReturn(id);

		assertTrue(changeRecordDomain.getReleaseTransitionsForElements(changeRecord, elements).isEmpty());
		verify(uiDomain).logError(message);
		verify(configuredElement, never()).canBePromoted(any(), any());
	}

	@Test
	public void getReleaseTransitionsForElements_elementCannotBePromoted() {
		List<LifecycleTransition> transitions = new ArrayList<>();
		transitions.add(lifecycleTransition);
		List<ConfiguredElement> elements = new ArrayList<>();
		elements.add(configuredElement);

		when(configuredElement.getTransitions()).thenReturn(transitions);
		when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
		when(lifecycleStatus.isReleased()).thenReturn(true);
		when(configuredElement.canBePromoted(lifecycleTransition, changeRecord)).thenReturn(false);

		assertTrue(changeRecordDomain.getReleaseTransitionsForElements(changeRecord, elements).isEmpty());
		verify(configuredElement).canBePromoted(lifecycleTransition, changeRecord);
	}

	@Test
	public void getReleaseTransitionsForElements_transitionObtained() {
		List<LifecycleTransition> transitions = new ArrayList<>();
		transitions.add(lifecycleTransition);
		List<ConfiguredElement> elements = new ArrayList<>();
		elements.add(configuredElement);

		when(configuredElement.getTransitions()).thenReturn(transitions);
		when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
		when(lifecycleStatus.isReleased()).thenReturn(true);
		when(configuredElement.canBePromoted(lifecycleTransition, changeRecord)).thenReturn(true);

		Map<ConfiguredElement, LifecycleStatus> results = changeRecordDomain.getReleaseTransitionsForElements(changeRecord, elements);

		assertFalse(results.isEmpty());
		assertTrue(results.containsKey(configuredElement));
		assertEquals(lifecycleStatus, results.get(configuredElement));
	}

	@Test
	public void transitionElements_emptyOldStatus() {
		Map<ConfiguredElement, LifecycleStatus> transitionMap = new HashMap<>();
		transitionMap.put(configuredElement, lifecycleStatus);

		when(configuredElement.getStatus()).thenReturn(Optional.empty());

		assertFalse(changeRecordDomain.transitionElements(changeRecord, transitionMap, configurationManagementService));
	}

	@Test
	public void transitionElements_changeStatusFailure() {
		Map<ConfiguredElement, LifecycleStatus> transitionMap = new HashMap<>();
		transitionMap.put(configuredElement, lifecycleStatus);
		Optional<LifecycleStatus> oldStatus = Optional.of(oldLifecycleStatus);

		when(configuredElement.getStatus()).thenReturn(oldStatus);
		when(configuredElement.changeStatus(lifecycleStatus, changeRecord)).thenReturn(false);

		assertFalse(changeRecordDomain.transitionElements(changeRecord, transitionMap, configurationManagementService));
		verify(uiDomain).logError(ExceptionConstants.CHANGE_IN_INCONSISTENT_STATE);
	}

	@Test
	public void transitionElements_transitionSuccessful() {
		Map<ConfiguredElement, LifecycleStatus> transitionMap = new HashMap<>();
		transitionMap.put(configuredElement, lifecycleStatus);
		Optional<LifecycleStatus> oldStatus = Optional.of(oldLifecycleStatus);
		String changeText = String.format(ExceptionConstants.STATUS_CHANGE, oldStatus, lifecycleStatus);

		when(configuredElement.getStatus()).thenReturn(oldStatus);
		when(configuredElement.changeStatus(lifecycleStatus, changeRecord)).thenReturn(true);
		doNothing().when(changeRecordDomain).addAffectedElement(changeRecord, configuredElement, changeText);

		assertTrue(changeRecordDomain.transitionElements(changeRecord, transitionMap, configurationManagementService));
	}

	@Test
	public void addAffectedElement_nullAffectedElementsButCommented() {
		String changeText = "changeText";
		String id = "id";
		String username = "username";
		String currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString();
		String changeDescription = String.format(PluginConstant.CHANGE_DESCRIPTION_FORMAT, currentTime, username, id, changeText);
		String elementComment = "elementComment";
		String finalComment = elementComment + "\n" + changeDescription;

		when(changeRecordDomain.currentTime()).thenReturn(currentTime);
		doReturn(username).when(apiDomain).getLoggedOnUser();
		doReturn(id).when(configuredElement).getID();
		doReturn(element).when(configuredElement).getElement();
		doReturn(stereotype).when(changeRecord).getBaseStereotype();
		doReturn(null).when(apiDomain).getStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS);
		doReturn(elementComment).when(apiDomain).getComment(element);
		doReturn(element).when(changeRecord).getElement();
		doNothing().when(apiDomain).setComment(element, finalComment);

		changeRecordDomain.addAffectedElement(changeRecord, configuredElement, changeText);

		verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS, element, true);
		verify(apiDomain).setComment(element, finalComment);
	}

	@Test
	public void addAffectedElement_elementNotAffectedAndNullComment() {
		String changeText = "changeText";
		String id = "id";
		String username = "username";
		String currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString();
		String changeDescription = String.format(PluginConstant.CHANGE_DESCRIPTION_FORMAT, currentTime, username, id, changeText);
		List affectedElements = new ArrayList();
		Element affected = mock(Element.class);
		affectedElements.add(affected);

		doReturn(currentTime).when(changeRecordDomain).currentTime();
		doReturn(username).when(apiDomain).getLoggedOnUser();
		doReturn(id).when(configuredElement).getID();
		doReturn(element).when(configuredElement).getElement();
		doReturn(stereotype).when(changeRecord).getBaseStereotype();
		doReturn(affectedElements).when(apiDomain).getStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS);
		doReturn(null).when(apiDomain).getComment(element);
		doReturn(element).when(changeRecord).getElement();
		doNothing().when(apiDomain).setComment(element, changeDescription);

		changeRecordDomain.addAffectedElement(changeRecord, configuredElement, changeText);

		verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS, element, true);
		verify(apiDomain).setComment(element, changeDescription);
	}

	@Test
	public void addAffectedElement_elementNotAffectedAndEmptyComment() {
		String changeText = "changeText";
		String id = "id";
		String username = "username";
		String currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString();
		String changeDescription = String.format(PluginConstant.CHANGE_DESCRIPTION_FORMAT, currentTime, username, id, changeText);
		List affectedElements = new ArrayList();
		Element affected = mock(Element.class);
		affectedElements.add(affected);

		doReturn(currentTime).when(changeRecordDomain).currentTime();
		doReturn(username).when(apiDomain).getLoggedOnUser();
		doReturn(id).when(configuredElement).getID();
		doReturn(element).when(configuredElement).getElement();
		doReturn(stereotype).when(changeRecord).getBaseStereotype();
		doReturn(affectedElements).when(apiDomain).getStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS);
		doReturn("").when(apiDomain).getComment(element);
		doReturn(element).when(changeRecord).getElement();
		doNothing().when(apiDomain).setComment(element, changeDescription);

		changeRecordDomain.addAffectedElement(changeRecord, configuredElement, changeText);

		verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS, element, true);
		verify(apiDomain).setComment(element, changeDescription);
	}

	@Test
	public void addAffectedElement_configuredElementAlreadyAffectedAndCommented() {
		String changeText = "changeText";
		String id = "id";
		String username = "username";
		String currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString();
		String changeDescription = String.format(PluginConstant.CHANGE_DESCRIPTION_FORMAT, currentTime, username, id, changeText);
		String elementComment = "elementComment";
		String finalComment = elementComment + "\n" + changeDescription;
		List affectedElements = new ArrayList();
		Element affected = mock(Element.class);
		affectedElements.add(affected);
		affectedElements.add(element);

		doReturn(currentTime).when(changeRecordDomain).currentTime();
		doReturn(username).when(apiDomain).getLoggedOnUser();
		doReturn(id).when(configuredElement).getID();
		doReturn(element).when(configuredElement).getElement();
		doReturn(stereotype).when(changeRecord).getBaseStereotype();
		doReturn(affectedElements).when(apiDomain).getStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS);
		doReturn(elementComment).when(apiDomain).getComment(element);
		doReturn(element).when(changeRecord).getElement();
		doNothing().when(apiDomain).setComment(element, finalComment);

		changeRecordDomain.addAffectedElement(changeRecord, configuredElement, changeText);

		verify(apiDomain, never()).setStereotypePropertyValue(element, stereotype, PluginConstant.AFFECTED_ELEMENTS, element, true);
		verify(apiDomain).setComment(element, finalComment);
	}

	@Test
	public void areAllElementsReadyForRelease_OneElementNotReady() {
		List<ConfiguredElement> elements = new ArrayList<>();
		ConfiguredElement configuredElement2 = mock(ConfiguredElement.class);
		elements.add(configuredElement);
		elements.add(configuredElement2);

		when(configuredElement.isReadyForRelease()).thenReturn(false);
		when(configuredElement2.isReadyForRelease()).thenReturn(true);
		doReturn(false).when(changeRecordDomain).reportOffendingElements(Collections.singletonList(configuredElement),
				ExceptionConstants.AFFECTED_NOT_READY_FOR_RELEASE_PREFIX);

		assertFalse(changeRecordDomain.areAllElementsReadyForRelease(elements));
	}

	@Test
	public void areAllElementsProperlyMature_OneElementNotMature() {
		List<ConfiguredElement> elements = new ArrayList<>();
		ConfiguredElement configuredElement2 = mock(ConfiguredElement.class);
		elements.add(configuredElement);
		elements.add(configuredElement2);
		int futureMaturityRating = 1;

		when(configuredElement.hasStatus()).thenReturn(true);
		when(configuredElement2.hasStatus()).thenReturn(true);
		when(configuredElement.getStatusMaturityRating()).thenReturn(0);
		when(configuredElement2.getStatusMaturityRating()).thenReturn(futureMaturityRating);
		when(configuredElement.getReleaseAuthority()).thenReturn(changeRecord);
		when(configuredElement2.getReleaseAuthority()).thenReturn(changeRecord);
		doReturn(false).when(changeRecordDomain).reportOffendingElements(elements,
			ExceptionConstants.AFFECTED_NEED_PROMOTION_PREFIX);

		assertFalse(changeRecordDomain.areAllElementsProperlyMature(elements, futureMaturityRating, changeRecord));
	}

	@Test
	public void reportOffendingElements_emptyList() {
		assertTrue(changeRecordDomain.reportOffendingElements(new ArrayList<>(), ""));
	}

	@Test
	public void reportOffendingElements_listHasElements() {
		String error = "error";

		when(configuredElement.getQualifiedName()).thenReturn("qualifiedName");
		when(configuredElement.getID()).thenReturn("id");

		assertFalse(changeRecordDomain.reportOffendingElements(Collections.singletonList(configuredElement), error));
	}

	@Test
	public void getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords_ifRevisionHistoryRecordsAreNull() {
		List<ChangeRecord> changeRecords = new ArrayList<>();
		changeRecords.add(changeRecord);

		when(configurationManagementService.getAllRevisionHistoryRecords()).thenReturn(null);

		assertTrue(changeRecordDomain.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
				changeRecords, configuredElement, configurationManagementService).isEmpty());
		verify(configurationManagementService).getAllRevisionHistoryRecords();
		verify(revisionHistoryRecord, never()).getRevisionReleaseAuthority();
	}

	@Test
	public void getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords_ifReleaseAuthorityOfRHIsNULL() {
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		revisionHistoryRecords.add(revisionHistoryRecord);
		List<ChangeRecord> changeRecords = new ArrayList<>();
		changeRecords.add(changeRecord);

		when(configurationManagementService.getAllRevisionHistoryRecords()).thenReturn(revisionHistoryRecords);
		when(revisionHistoryRecord.getRevisionReleaseAuthority()).thenReturn(null);

		assertTrue(changeRecordDomain.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
				changeRecords, configuredElement, configurationManagementService).isEmpty());
		verify(configurationManagementService).getAllRevisionHistoryRecords();
		verify(revisionHistoryRecord).getRevisionReleaseAuthority();
		verify(revisionHistoryRecord, never()).getConfiguredElement();
	}

	@Test
	public void getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords_ifSelectedCRIsNotMatching() {
		ChangeRecord changeRecord1 = mock(ChangeRecord.class);
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		revisionHistoryRecords.add(revisionHistoryRecord);
		List<ChangeRecord> changeRecords = new ArrayList<>();
		changeRecords.add(changeRecord);

		when(configurationManagementService.getAllRevisionHistoryRecords()).thenReturn(revisionHistoryRecords);
		when(revisionHistoryRecord.getRevisionReleaseAuthority()).thenReturn(changeRecord1);

		assertTrue(changeRecordDomain.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
				changeRecords, configuredElement, configurationManagementService).isEmpty());
		verify(configurationManagementService).getAllRevisionHistoryRecords();
		verify(revisionHistoryRecord).getRevisionReleaseAuthority();
		verify(revisionHistoryRecord, never()).getConfiguredElement();
	}

	@Test
	public void getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords_ifReleaseAuthorityDoesNotAffectCe() {
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		revisionHistoryRecords.add(revisionHistoryRecord);
		List<ChangeRecord> changeRecords = new ArrayList<>();
		changeRecords.add(changeRecord);

		when(configurationManagementService.getAllRevisionHistoryRecords()).thenReturn(revisionHistoryRecords);
		when(revisionHistoryRecord.getRevisionReleaseAuthority()).thenReturn(changeRecord);
		when(changeRecord.affectsGivenConfiguredElement(configuredElement)).thenReturn(false);

		assertTrue(changeRecordDomain.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
				changeRecords, configuredElement, configurationManagementService).isEmpty());
		verify(configurationManagementService).getAllRevisionHistoryRecords();
		verify(revisionHistoryRecord).getRevisionReleaseAuthority();
		verify(revisionHistoryRecord, never()).getConfiguredElement();
	}

	@Test
	public void getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords_ifReleaseAuthorityAffectsAndRhHasCe() {
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		revisionHistoryRecords.add(revisionHistoryRecord);
		List<ChangeRecord> changeRecords = new ArrayList<>();
		changeRecords.add(changeRecord);

		when(configurationManagementService.getAllRevisionHistoryRecords()).thenReturn(revisionHistoryRecords);
		when(revisionHistoryRecord.getRevisionReleaseAuthority()).thenReturn(changeRecord);
		when(changeRecord.affectsGivenConfiguredElement(configuredElement)).thenReturn(true);
		when(revisionHistoryRecord.getConfiguredElement()).thenReturn(configuredElement);

		List<RevisionHistoryRecord> results = changeRecordDomain.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
				changeRecords, configuredElement, configurationManagementService);

		assertFalse(results.isEmpty());
		assertTrue(results.contains(revisionHistoryRecord));
		verify(configurationManagementService).getAllRevisionHistoryRecords();
		verify(revisionHistoryRecord).getRevisionReleaseAuthority();
		verify(revisionHistoryRecord).getConfiguredElement();
	}

	@Test
	public void getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords_ifReleaseAuthorityAffectsButRhLacksCe() {
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		revisionHistoryRecords.add(revisionHistoryRecord);
		List<ChangeRecord> changeRecords = new ArrayList<>();
		changeRecords.add(changeRecord);

		when(configurationManagementService.getAllRevisionHistoryRecords()).thenReturn(revisionHistoryRecords);
		when(revisionHistoryRecord.getRevisionReleaseAuthority()).thenReturn(changeRecord);
		when(changeRecord.affectsGivenConfiguredElement(configuredElement)).thenReturn(true);
		when(revisionHistoryRecord.getConfiguredElement()).thenReturn(null);
		doNothing().when(revisionHistoryRecord).setConfiguredElement(configuredElement);

		List<RevisionHistoryRecord> results = changeRecordDomain.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
				changeRecords, configuredElement, configurationManagementService);

		assertFalse(results.isEmpty());
		assertTrue(results.contains(revisionHistoryRecord));
		verify(configurationManagementService).getAllRevisionHistoryRecords();
		verify(revisionHistoryRecord).getRevisionReleaseAuthority();
		verify(revisionHistoryRecord).getConfiguredElement();
	}

	@Test
	public void getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords_ifRevisionHistoryRecordsAreEmpty() {
		List<ChangeRecord> changeRecords = new ArrayList<>();
		changeRecords.add(changeRecord);
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		doReturn(revisionHistoryRecords).when(configurationManagementService).getAllRevisionHistoryRecords();

		assertTrue(changeRecordDomain.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
				changeRecords, configuredElement, configurationManagementService).isEmpty());
		verify(configurationManagementService).getAllRevisionHistoryRecords();
		verify(revisionHistoryRecord, never()).getRevisionReleaseAuthority();
		verify(revisionHistoryRecord, never()).getRevisionReleaseAuthority();
		verify(revisionHistoryRecord, never()).getConfiguredElement();
	}

	@Test
	public void sortChangeRecordsByReleaseStatusAndTime_sortedProperly() {
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		ChangeRecordComparatorForHistory comparatorForHistory = mock(ChangeRecordComparatorForHistory.class);
		List<ChangeRecord> changeRecords = spy(new ArrayList<>());

		doReturn(comparatorForHistory).when(changeRecordDomain).getChangeRecordComparator(revisionHistoryRecords);
		doNothing().when(changeRecords).sort(comparatorForHistory);
		when(comparatorForHistory.potentiallyHasIncompleteData()).thenReturn(false);

		changeRecordDomain.sortChangeRecordsByReleaseStatusAndTime(changeRecords, revisionHistoryRecords);
		verify(uiDomain, never()).log(anyString());
	}

	@Test
	public void sortChangeRecordsByReleaseStatusAndTime_exceptionDuringSort() {
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		ChangeRecordComparatorForHistory comparatorForHistory = mock(ChangeRecordComparatorForHistory.class);
		List<ChangeRecord> changeRecords = spy(new ArrayList<>());

		doReturn(comparatorForHistory).when(changeRecordDomain).getChangeRecordComparator(revisionHistoryRecords);
		doNothing().when(changeRecords).sort(comparatorForHistory);
		when(comparatorForHistory.potentiallyHasIncompleteData()).thenReturn(true);
		doNothing().when(uiDomain).log(ExceptionConstants.REVISION_HISTORY_RECORD_NOT_FOUND);

		changeRecordDomain.sortChangeRecordsByReleaseStatusAndTime(changeRecords, revisionHistoryRecords);

		verify(uiDomain).log(ExceptionConstants.REVISION_HISTORY_RECORD_NOT_FOUND);
	}

	@Test
	public void determinePreviousRevisionModelVersion_emptyRevisionRecords() {
		int expected = 1;
		String revision = "-";
		List<ChangeRecord> allRelatedCrs = new ArrayList<>();
		ChangeRecord changeRecord2 = mock(ChangeRecord.class);
		allRelatedCrs.add(changeRecord);
		allRelatedCrs.add(changeRecord2);
		List<ChangeRecord> singletonSelectedList = List.of(changeRecord);

		doReturn(List.of()).when(changeRecordDomain).getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
			singletonSelectedList, configuredElement, configurationManagementService);
		doReturn(List.of()).when(changeRecordDomain).getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
			allRelatedCrs, configuredElement, configurationManagementService);

		assertEquals(expected, changeRecordDomain.determinePreviousRevisionModelVersion(configuredElement, changeRecord,
			allRelatedCrs, revision, configurationManagementService).intValue());
		verify(changeRecordDomain, never()).getRevisionComparator();
	}

	@Test
	public void determinePreviousRevisionModelVersion_nullSelectedRevisionRecordButCrReleased() {
		int expected = 1;
		List<ChangeRecord> allRelatedCrs = new ArrayList<>();
		ChangeRecord changeRecord2 = mock(ChangeRecord.class);
		allRelatedCrs.add(changeRecord);
		allRelatedCrs.add(changeRecord2);
		List<ChangeRecord> singletonSelectedList = List.of(changeRecord);
		List<RevisionHistoryRecord> revisionRecords = spy(new ArrayList<>());
		revisionRecords.add(revisionHistoryRecord);

		doReturn(List.of()).when(changeRecordDomain).getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
			singletonSelectedList, configuredElement, configurationManagementService);
		doReturn(revisionRecords).when(changeRecordDomain).getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
			allRelatedCrs, configuredElement, configurationManagementService);
		doNothing().when(revisionRecords).sort(revisionRecordComparatorForRevision);
		when(changeRecord.isReleased()).thenReturn(true);
		doReturn(configuredElement).when(revisionHistoryRecord).getConfiguredElement();
		when(changeRecordDomain.traverseRecordsForPrevious(1, configuredElement, revisionRecords)).thenReturn(expected);

		assertEquals(expected, changeRecordDomain.determinePreviousRevisionModelVersion(configuredElement, changeRecord,
			allRelatedCrs, revision, configurationManagementService).intValue());

		verify(changeRecordDomain).getRevisionComparator();
		verify(changeRecordDomain)
			.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(allRelatedCrs, configuredElement,
				configurationManagementService);
		verify(changeRecordDomain)
			.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(singletonSelectedList, configuredElement,
				configurationManagementService);
		verify(revisionHistoryRecord, times(3)).getConfiguredElement();
		verify(changeRecordDomain, never()).getPreviousModelVersionUsingHistoryRecord(
			any(), anyList());
		verify(changeRecordDomain).traverseRecordsForPrevious(1, configuredElement, revisionRecords);
	}

	@Test
	public void determinePreviousRevisionModelVersion_emptySelectedRevisionRecordAndCrUnreleased() {
		int expected = 5;
		List<ChangeRecord> allRelatedCrs = new ArrayList<>();
		ChangeRecord changeRecord2 = mock(ChangeRecord.class);
		allRelatedCrs.add(changeRecord);
		allRelatedCrs.add(changeRecord2);
		List<ChangeRecord> singletonSelectedList = List.of(changeRecord);
		List<RevisionHistoryRecord> revisionRecords = spy(new ArrayList<>());
		revisionRecords.add(revisionHistoryRecord);

		doReturn(List.of()).when(changeRecordDomain).getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
			singletonSelectedList, configuredElement, configurationManagementService);
		doReturn(revisionRecords).when(changeRecordDomain).getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
			allRelatedCrs, configuredElement, configurationManagementService);
		doNothing().when(revisionRecords).sort(revisionRecordComparatorForRevision);
		doReturn(configuredElement).when(revisionHistoryRecord).getConfiguredElement();
		when(changeRecordDomain.traverseRecordsForPrevious(1, configuredElement, revisionRecords)).thenReturn(expected);

		assertEquals(expected, changeRecordDomain.determinePreviousRevisionModelVersion(configuredElement, changeRecord,
			allRelatedCrs, revision, configurationManagementService).intValue());

		verify(changeRecordDomain).getRevisionComparator();
		verify(changeRecordDomain)
			.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(allRelatedCrs, configuredElement,
				configurationManagementService);
		verify(changeRecordDomain)
			.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(singletonSelectedList, configuredElement,
				configurationManagementService);
		verify(revisionHistoryRecord, times(3)).getConfiguredElement();
		verify(changeRecordDomain, never()).getPreviousModelVersionUsingHistoryRecord(
			any(), anyList());
		verify(changeRecordDomain).traverseRecordsForPrevious(1, configuredElement, revisionRecords);
	}

	@Test
	public void determinePreviousRevisionModelVersion_getPreviousModelVersion() {
		int expectedResult = 3;
		List<ChangeRecord> allRelatedCrs = new ArrayList<>();
		allRelatedCrs.add(changeRecord);
		List<RevisionHistoryRecord> selectedRevisionRecord = new ArrayList<>();
		selectedRevisionRecord.add(revisionHistoryRecord);
		List<RevisionHistoryRecord> revisionRecords = new ArrayList<>();
		revisionRecords.add(revisionHistoryRecord);
		doReturn(selectedRevisionRecord).when(changeRecordDomain).getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
			List.of(changeRecord), configuredElement, configurationManagementService);
		doReturn(revisionRecords).when(changeRecordDomain).getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
			allRelatedCrs, configuredElement, configurationManagementService);
		RevisionRecordComparatorForRevision revisionRecordComparatorForRevision = mock(RevisionRecordComparatorForRevision.class);
		doReturn(revisionRecordComparatorForRevision).when(changeRecordDomain).getRevisionComparator();
		List<RevisionHistoryRecord> revisionRecordsForSelectedCe = new ArrayList<>();
		revisionRecordsForSelectedCe.add(revisionHistoryRecord);
		doReturn(configuredElement).when(revisionHistoryRecord).getConfiguredElement();
		doReturn(revision).when(revisionHistoryRecord).getRevision();
		doReturn(expectedResult).when(changeRecordDomain).getPreviousModelVersionUsingHistoryRecord(
			selectedRevisionRecord.get(0), revisionRecordsForSelectedCe);

		int actualResult = changeRecordDomain.determinePreviousRevisionModelVersion(configuredElement, changeRecord,
			allRelatedCrs, revision, configurationManagementService).intValue();

		assertEquals(expectedResult, actualResult);

		verify(changeRecordDomain, times(2))
			.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(List.of(changeRecord), configuredElement,
				configurationManagementService);
		verify(changeRecordDomain).getRevisionComparator();
		verify(revisionHistoryRecord, times(2)).getConfiguredElement();
		verify(changeRecordDomain).getPreviousModelVersionUsingHistoryRecord(
			selectedRevisionRecord.get(0), revisionRecordsForSelectedCe);
		verify(changeRecordDomain, never()).traverseRecordsForPrevious(anyInt(), any(), anyList());
	}

	@Test
	public void getPreviousModelVersionUsingHistoryRecord_selectedRevisionModelVersionNull() {
		int expected = 1;
		RevisionHistoryRecord revisionHistoryRecord2 = mock(RevisionHistoryRecord.class);
		List<RevisionHistoryRecord> revisionRecords = spy(new ArrayList<>());
		revisionRecords.add(revisionHistoryRecord);
		revisionRecords.add(revisionHistoryRecord2);

		when(revisionHistoryRecord.getModelVersion()).thenReturn(null);

		assertEquals(expected, changeRecordDomain.getPreviousModelVersionUsingHistoryRecord(revisionHistoryRecord, revisionRecords).intValue());
		verify(changeRecordDomain, never()).traverseRecordsForPrevious(anyInt(), any(), anyList());
	}

	@Test
	public void getPreviousModelVersionUsingHistoryRecord_selectedRevisionModelVersionExists() {
		int expected = 2;
		RevisionHistoryRecord revisionHistoryRecord2 = mock(RevisionHistoryRecord.class);
		List<RevisionHistoryRecord> revisionRecords = spy(new ArrayList<>());
		revisionRecords.add(revisionHistoryRecord);
		revisionRecords.add(revisionHistoryRecord2);
		int index = revisionRecords.indexOf(revisionHistoryRecord);

		when(revisionHistoryRecord.getModelVersion()).thenReturn(5);
		when(revisionHistoryRecord.getConfiguredElement()).thenReturn(configuredElement);
		doReturn(expected).when(changeRecordDomain).traverseRecordsForPrevious(index, configuredElement, revisionRecords);

		assertEquals(expected, changeRecordDomain.getPreviousModelVersionUsingHistoryRecord(revisionHistoryRecord, revisionRecords).intValue());
		verify(changeRecordDomain).traverseRecordsForPrevious(index, configuredElement, revisionRecords);
	}

	@Test
	public void traverseRecordsForPrevious_startingIndexInvalid() {
		int expected = 1;

		assertEquals(expected, changeRecordDomain.traverseRecordsForPrevious(-1, configuredElement, List.of()).intValue());
	}

	@Test
	public void traverseRecordsForPrevious_startingIndexValidButNoMatch() {
		int expected = 1;
		List<RevisionHistoryRecord> revisionRecords = new ArrayList<>();
		revisionRecords.add(revisionHistoryRecord);
		ConfiguredElement configuredElement2 = mock(ConfiguredElement.class);

		when(revisionHistoryRecord.getConfiguredElement()).thenReturn(configuredElement2);

		assertEquals(expected, changeRecordDomain.traverseRecordsForPrevious(1, configuredElement, revisionRecords).intValue());
	}

	@Test
	public void traverseRecordsForPrevious_previousFound() {
		int expected = 3;
		List<RevisionHistoryRecord> revisionRecords = new ArrayList<>();
		revisionRecords.add(revisionHistoryRecord);

		when(revisionHistoryRecord.getConfiguredElement()).thenReturn(configuredElement);
		when(revisionHistoryRecord.getModelVersion()).thenReturn(expected);

		assertEquals(expected, changeRecordDomain.traverseRecordsForPrevious(1, configuredElement, revisionRecords).intValue());
	}

	@Test
	public void determineCurrentRevisionModelVersion_selectedCrNotReleasedAndAutomatedReleaseIsOn() {
		int expected = 3;

		when(configurationManagementService.getAutomateReleaseSwitch()).thenReturn(true);
		when(changeRecord.isReleased()).thenReturn(false);
		doReturn(expected).when(changeRecordDomain).getLatestRevisionOnCurrentBranch(configuredElement);

		assertEquals(expected, changeRecordDomain.determineCurrentRevisionModelVersion(changeRecord, configuredElement,
				revision, configurationManagementService).intValue());
	}

	@Test
	public void determineCurrentRevisionModelVersion_automateReleaseOffAndNoRevisionHistoryRecord() {
		int expected = 3;

		when(configurationManagementService.getAutomateReleaseSwitch()).thenReturn(false);
		doReturn(List.of()).when(changeRecordDomain).getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
				List.of(changeRecord), configuredElement, configurationManagementService);
		doReturn(expected).when(changeRecordDomain).getLatestRevisionOnCurrentBranch(configuredElement);

		assertEquals(expected, changeRecordDomain.determineCurrentRevisionModelVersion(changeRecord,
				configuredElement, revision, configurationManagementService).intValue());
	}

	@Test
	public void determineCurrentRevisionModelVersion_noRevisionHistoryRecord() {
		int expected = 3;

		when(configurationManagementService.getAutomateReleaseSwitch()).thenReturn(true);
		when(changeRecord.isReleased()).thenReturn(true);
		doReturn(List.of()).when(changeRecordDomain).getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(
			List.of(changeRecord), configuredElement, configurationManagementService);
		doReturn(expected).when(changeRecordDomain).getLatestRevisionOnCurrentBranch(configuredElement);

		assertEquals(expected, changeRecordDomain.determineCurrentRevisionModelVersion(changeRecord,
			configuredElement, revision, configurationManagementService).intValue());
	}

	@Test
	public void determineCurrentRevisionModelVersion_nullModelVersionFromEntry() {
		int expected = 3;
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		revisionHistoryRecords.add(revisionHistoryRecord);

		when(configurationManagementService.getAutomateReleaseSwitch()).thenReturn(true);
		when(changeRecord.isReleased()).thenReturn(true);
		doReturn(revisionHistoryRecords).when(changeRecordDomain).
			getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(List.of(changeRecord), configuredElement,
				configurationManagementService);
		doReturn(configuredElement).when(revisionHistoryRecord).getConfiguredElement();
		when(revisionHistoryRecord.getModelVersion()).thenReturn(null);
		doReturn(expected).when(changeRecordDomain).getLatestRevisionOnCurrentBranch(configuredElement);
		doReturn(revision).when(revisionHistoryRecord).getRevision();

		assertEquals(expected, changeRecordDomain.determineCurrentRevisionModelVersion(changeRecord, configuredElement,
			revision, configurationManagementService).intValue());
	}

	@Test
	public void determineCurrentRevisionModelVersion_validModelVersion() {
		int expected = 3;
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		revisionHistoryRecords.add(revisionHistoryRecord);

		when(configurationManagementService.getAutomateReleaseSwitch()).thenReturn(true);
		when(changeRecord.isReleased()).thenReturn(true);
		doReturn(revisionHistoryRecords).when(changeRecordDomain)
			.getAllRevisionHistoryRecordsAssociatedWithGivenChangeRecords(List.of(changeRecord), configuredElement,
				configurationManagementService);
		doReturn(configuredElement).when(revisionHistoryRecord).getConfiguredElement();
		when(revisionHistoryRecord.getModelVersion()).thenReturn(expected);
		doReturn(revision).when(revisionHistoryRecord).getRevision();

		assertEquals(expected, changeRecordDomain.determineCurrentRevisionModelVersion(changeRecord, configuredElement,
			revision, configurationManagementService).intValue());
	}

	@Test
	public void getCreationTimeFromElementComments() {
		ZonedDateTime expectedTime = mock(ZonedDateTime.class);
		String comments = "name Configuring element..";
		doReturn(element).when(changeRecord).getElement();
		doReturn(comments).when(apiDomain).getComment(element);
		doReturn("name").when(configuredElement).getName();
		doReturn(expectedTime).when(changeRecordDomain).parseTimeFromToken(anyString(), anyString());

		ZonedDateTime actualCreationTime = changeRecordDomain.getCreationTimeFromElementComments(changeRecord, configuredElement);

		assertEquals(actualCreationTime, expectedTime);
		verify(changeRecord).getElement();
		verify(apiDomain).getComment(element);
		verify(configuredElement).getName();
	}

	@Test
	public void getCreationTimeFromElementComments_ifCommentisNull() {
		doReturn(element).when(changeRecord).getElement();
		doReturn(null).when(apiDomain).getComment(element);
		doReturn("name").when(configuredElement).getName();

		ZonedDateTime actualCreationTime = changeRecordDomain.getCreationTimeFromElementComments(changeRecord, configuredElement);

		assertNull(actualCreationTime);
		verify(changeRecord).getElement();
		verify(apiDomain).getComment(element);
		verify(configuredElement, never()).getName();
	}

	@Test
	public void trimTimestamp_nullFromParseAttempt() {
		String time = "time";
		String errorSuffix = "errorSuffix";

		doReturn(null).when(changeRecordDomain).tryToParseTimeFromString(time, errorSuffix);

		assertNull(changeRecordDomain.trimTimestamp(time, errorSuffix));
	}

	@Test
	public void trimTimestamp_parseAttemptSuccessful() {
		String time = "time";
		String errorSuffix = "errorSuffix";
		ZonedDateTime realTimestamp = spy(ZonedDateTime.now());
		String expected = realTimestamp.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT);

		doReturn(realTimestamp).when(changeRecordDomain).tryToParseTimeFromString(time, errorSuffix);

		assertEquals(expected, changeRecordDomain.trimTimestamp(time, errorSuffix));
	}

	@Test
	public void parseTimeFromToken_nullParameter() {
		assertNull(changeRecordDomain.parseTimeFromToken(null, null));
	}

	@Test
	public void parseTimeFromToken_emptyString() {
		String line = "";
		String errorSuffix = "errorSuffix";

		assertNull(changeRecordDomain.parseTimeFromToken(line, errorSuffix));
		verify(changeRecordDomain, never()).tryToParseTimeFromString(anyString(), anyString());
	}

	@Test
	public void parseTimeFromToken_validString() {
		String line = "li ne";
		String token = "li";
		String errorSuffix = "errorSuffix";
		ZonedDateTime time = mock(ZonedDateTime.class);

		doReturn(time).when(changeRecordDomain).tryToParseTimeFromString(token, errorSuffix);

		assertEquals(time, changeRecordDomain.parseTimeFromToken(line, errorSuffix));
		verify(changeRecordDomain).tryToParseTimeFromString(token, errorSuffix);
	}

	@Test
	public void tryToParseTimeFromString_nullToken() {
		assertNull(changeRecordDomain.tryToParseTimeFromString(null, ""));
		verify(uiDomain, never()).logErrorAndShowMessage(any(), anyString(), anyString());
	}

	@Test
	public void tryToParseTimeFromString_validTime() {
		ZonedDateTime time = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
		String timeString = time.toString();

		assertEquals(time, changeRecordDomain.tryToParseTimeFromString(timeString, ""));
		verify(uiDomain, never()).logErrorAndShowMessage(any(), anyString(), anyString());
	}

	@Test
	public void tryToParseTimeFromString_invalidTime() {
		String nowString = "This isn't a time at all";
		String potentialErrorSuffix = "suffix";
		String expectedError = String.format(ExceptionConstants.DATE_TIME_PARSING_ISSUE, potentialErrorSuffix);

		doNothing().when(uiDomain).logErrorAndShowMessage(logger, expectedError, ExceptionConstants.DATE_TIME_PARSING_ISSUE_TITLE);

		assertNull(changeRecordDomain.tryToParseTimeFromString(nowString, potentialErrorSuffix));
		verify(uiDomain).logErrorAndShowMessage(logger, expectedError, ExceptionConstants.DATE_TIME_PARSING_ISSUE_TITLE);
	}

	@Test
	public void determineRevisionHistoryRecordInterleaving_recordAlreadyKnownToBeInterleaved() {
		doReturn(true).when(revisionHistoryRecord).isInterleavedWithAnotherRevision();

		changeRecordDomain.determineRevisionHistoryRecordInterleaving(List.of(revisionHistoryRecord));

		verify(revisionHistoryRecord, never()).getRevision();
	}

	@Test
	public void determineRevisionHistoryRecordInterleaving_nullRevision() {
		doReturn(false).when(revisionHistoryRecord).isInterleavedWithAnotherRevision();
		doReturn(null).when(revisionHistoryRecord).getRevision();

		changeRecordDomain.determineRevisionHistoryRecordInterleaving(List.of(revisionHistoryRecord));

		verify(changeRecordDomain, never()).tryToParseTimeFromString(anyString(), anyString());
	}

	@Test
	public void determineRevisionHistoryRecordInterleaving_nullCreationTime() {
		String revision = "revision";
		String createString = "createString";
		String releaseString = "releaseString";
		String name = "name";
		String createFormatted = String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, name);
		String releaseFormatted = String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, name);

		doReturn(false).when(revisionHistoryRecord).isInterleavedWithAnotherRevision();
		doReturn(revision).when(revisionHistoryRecord).getRevision();
		setupRecordCreationReleaseAndName(createString, releaseString, name, revisionHistoryRecord);
		doReturn(null).when(changeRecordDomain).tryToParseTimeFromString(createString, createFormatted);
		doReturn(mock(ZonedDateTime.class)).when(changeRecordDomain).tryToParseTimeFromString(releaseString, releaseFormatted);

		changeRecordDomain.determineRevisionHistoryRecordInterleaving(List.of(revisionHistoryRecord));

		verify(changeRecordDomain, never()).compareWithRemainingHistoryRecords(any(), any(), any(), any());
	}

	@Test
	public void determineRevisionHistoryRecordInterleaving_nullReleaseTime() {
		String revision = "revision";
		String createString = "createString";
		String releaseString = "releaseString";
		String name = "name";
		String createFormatted = String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, name);
		String releaseFormatted = String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, name);
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		revisionHistoryRecords.add(revisionHistoryRecord);
		ZonedDateTime time = mock(ZonedDateTime.class);

		doReturn(false).when(revisionHistoryRecord).isInterleavedWithAnotherRevision();
		doReturn(revision).when(revisionHistoryRecord).getRevision();
		setupRecordCreationReleaseAndName(createString, releaseString, name, revisionHistoryRecord);
		doReturn(time).when(changeRecordDomain).tryToParseTimeFromString(createString, createFormatted);
		doReturn(null).when(changeRecordDomain).tryToParseTimeFromString(releaseString, releaseFormatted);

		changeRecordDomain.determineRevisionHistoryRecordInterleaving(revisionHistoryRecords);

		verify(changeRecordDomain, never()).compareWithRemainingHistoryRecords(revisionHistoryRecords, revisionHistoryRecord, time, null);
	}

	@Test
	public void determineRevisionHistoryRecordInterleaving_bothTimesExist() {
		String revision = "revision";
		String createString = "createString";
		String releaseString = "releaseString";
		String name = "name";
		String createFormatted = String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, name);
		String releaseFormatted = String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, name);
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		revisionHistoryRecords.add(revisionHistoryRecord);
		ZonedDateTime time = mock(ZonedDateTime.class);

		doReturn(false).when(revisionHistoryRecord).isInterleavedWithAnotherRevision();
		doReturn(revision).when(revisionHistoryRecord).getRevision();
		setupRecordCreationReleaseAndName(createString, releaseString, name, revisionHistoryRecord);
		doReturn(time).when(changeRecordDomain).tryToParseTimeFromString(createString, createFormatted);
		doReturn(time).when(changeRecordDomain).tryToParseTimeFromString(releaseString, releaseFormatted);
		doNothing().when(changeRecordDomain).compareWithRemainingHistoryRecords(revisionHistoryRecords, revisionHistoryRecord, time, time);

		changeRecordDomain.determineRevisionHistoryRecordInterleaving(revisionHistoryRecords);

		verify(changeRecordDomain).compareWithRemainingHistoryRecords(revisionHistoryRecords, revisionHistoryRecord, time, time);
	}

	@Test
	public void compareWithRemainingHistoryRecords_nullRevision() {
		ZonedDateTime time = mock(ZonedDateTime.class);

		doReturn(null).when(revisionHistoryRecord).getRevision();

		changeRecordDomain.compareWithRemainingHistoryRecords(List.of(revisionHistoryRecord), revisionHistoryRecord, time, time);

		verify(revisionHistoryRecord, never()).isInterleavedWithAnotherRevision();
	}

	@Test
	public void compareWithRemainingHistoryRecords_singletonList() {
		ZonedDateTime time = mock(ZonedDateTime.class);
		String revision = "revision";

		doReturn(revision).when(revisionHistoryRecord).getRevision();

		changeRecordDomain.compareWithRemainingHistoryRecords(List.of(revisionHistoryRecord), revisionHistoryRecord, time, time);

		verify(revisionHistoryRecord, never()).isInterleavedWithAnotherRevision();
	}

	@Test
	public void compareWithRemainingHistoryRecords_isInterleaved() {
		RevisionHistoryRecord revisionHistoryRecord2 = mock(RevisionHistoryRecord.class);
		List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
		revisionHistoryRecords.add(revisionHistoryRecord);
		revisionHistoryRecords.add(revisionHistoryRecord2);
		String revision = "revision";
		ZonedDateTime time = mock(ZonedDateTime.class);

		doReturn(revision).when(revisionHistoryRecord).getRevision();
		doReturn(true).when(changeRecordDomain).isRecordInterleavedWithinAnother(time, time, revisionHistoryRecord);
		doNothing().when(revisionHistoryRecord).setInterleavedWithAnotherRevision(true);

		changeRecordDomain.compareWithRemainingHistoryRecords(revisionHistoryRecords, revisionHistoryRecord2, time, time);

		verify(changeRecordDomain).isRecordInterleavedWithinAnother(time, time, revisionHistoryRecord);
		verify(revisionHistoryRecord2).setInterleavedWithAnotherRevision(true);
		verify(revisionHistoryRecord2, never()).getRevision();
	}

	private void setupRecordCreationReleaseAndName(String createTime, String releaseTime, String name, RevisionHistoryRecord revisionHistoryRecord) {
		// convenience method to decrease lines
		when(revisionHistoryRecord.getCreationDate()).thenReturn(createTime);
		when(revisionHistoryRecord.getName()).thenReturn(name);
		when(revisionHistoryRecord.getReleaseDate()).thenReturn(releaseTime);
	}

	@Test
	public void isRecordInterleavedWithAnother_creationTimeNull() {
		String createString1 = "createString1";
		String releaseString1 = "releaseString1";
		String name1 = "name1";
		String createFormatted1 = String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, name1);
		String releaseFormatted1 = String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, name1);

		setupRecordCreationReleaseAndName(createString1, releaseString1, name1, revisionHistoryRecord);

		doReturn(null).when(changeRecordDomain).tryToParseTimeFromString(createString1, createFormatted1);
		doReturn(releaseTime1).when(changeRecordDomain).tryToParseTimeFromString(releaseString1, releaseFormatted1);

		assertFalse(changeRecordDomain.isRecordInterleavedWithinAnother(createTime1, releaseTime1, revisionHistoryRecord));
	}

	@Test
	public void isRecordInterleavedWithAnother_releaseTimeNull() {
		String createString1 = "createString1";
		String releaseString1 = "releaseString1";
		String name1 = "name1";
		String createFormatted1 = String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, name1);
		String releaseFormatted1 = String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, name1);

		setupRecordCreationReleaseAndName(createString1, releaseString1, name1, revisionHistoryRecord);

		doReturn(createTime1).when(changeRecordDomain).tryToParseTimeFromString(createString1, createFormatted1);
		doReturn(null).when(changeRecordDomain).tryToParseTimeFromString(releaseString1, releaseFormatted1);

		assertFalse(changeRecordDomain.isRecordInterleavedWithinAnother(createTime1, releaseTime1, revisionHistoryRecord));
	}

	@Test
	public void isRecordInterleavedWithAnother_givenReleaseIsBeforeTheOther() {
		String createString1 = "createString1";
		String releaseString1 = "releaseString1";
		String name1 = "name1";
		String createFormatted1 = String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, name1);
		String releaseFormatted1 = String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, name1);

		setupRecordCreationReleaseAndName(createString1, releaseString1, name1, revisionHistoryRecord);

		doReturn(createTime2).when(changeRecordDomain).tryToParseTimeFromString(createString1, createFormatted1);
		doReturn(releaseTime2).when(changeRecordDomain).tryToParseTimeFromString(releaseString1, releaseFormatted1);
		
		assertFalse(changeRecordDomain.isRecordInterleavedWithinAnother(createTime1, releaseTime1, revisionHistoryRecord));
	}

	@Test
	public void isRecordInterleavedWithAnother_givenCreationIsAfterTheOther() {
		String createString1 = "createString1";
		String releaseString1 = "releaseString1";
		String name1 = "name1";
		String createFormatted1 = String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, name1);
		String releaseFormatted1 = String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, name1);

		setupRecordCreationReleaseAndName(createString1, releaseString1, name1, revisionHistoryRecord);

		doReturn(createTime1).when(changeRecordDomain).tryToParseTimeFromString(createString1, createFormatted1);
		doReturn(releaseTime1).when(changeRecordDomain).tryToParseTimeFromString(releaseString1, releaseFormatted1);

		assertFalse(changeRecordDomain.isRecordInterleavedWithinAnother(createTime2, releaseTime2, revisionHistoryRecord));
	}

	@Test
	public void isRecordInterleavedWithAnother_recordInterleaved() {
		String createString1 = "createString1";
		String releaseString1 = "releaseString1";
		String name1 = "name1";
		String createFormatted1 = String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, name1);
		String releaseFormatted1 = String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, name1);

		setupRecordCreationReleaseAndName(createString1, releaseString1, name1, revisionHistoryRecord);

		doReturn(createTime2).when(changeRecordDomain).tryToParseTimeFromString(createString1, createFormatted1);
		doReturn(releaseTime1).when(changeRecordDomain).tryToParseTimeFromString(releaseString1, releaseFormatted1);

		assertTrue(changeRecordDomain.isRecordInterleavedWithinAnother(createTime1, releaseTime2, revisionHistoryRecord));
	}

	@Test
	public void displayDifferenceViewer_nullSelectedRow() {
		changeRecordDomain.displayDifferenceViewer(null, "", List.of(), false, configurationManagementService);

		verify(configurationManagementService, never()).getConfiguredElementDomain();
	}

	@Test
	public void displayDifferenceViewer_nullConfiguredElementId() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);

		changeRecordDomain.displayDifferenceViewer(rowView, null, List.of(), false, configurationManagementService);

		verify(configurationManagementService, never()).getConfiguredElementDomain();
	}

	@Test
	public void displayDifferenceViewer_blankConfiguredElementId() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);

		changeRecordDomain.displayDifferenceViewer(rowView, " ", List.of(), false, configurationManagementService);

		verify(configurationManagementService, never()).getConfiguredElementDomain();
	}

	@Test
	public void displayDifferenceViewer_nullChangeRecords() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String id = "id";

		changeRecordDomain.displayDifferenceViewer(rowView, id, null, false, configurationManagementService);

		verify(configurationManagementService, never()).getConfiguredElementDomain();
	}

	@Test
	public void displayDifferenceViewer_emptyChangeRecords() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String id = "id";

		changeRecordDomain.displayDifferenceViewer(rowView, id, List.of(), false, configurationManagementService);

		verify(configurationManagementService, never()).getConfiguredElementDomain();
	}

	@Test
	public void displayDifferenceViewer_nullConfigurationManagementService() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String id = "id";
		List<ChangeRecord> changeRecords = List.of(changeRecord);

		changeRecordDomain.displayDifferenceViewer(rowView, id, changeRecords, false, null);

		verify(configurationManagementService, never()).getConfiguredElementDomain();
	}

	@Test
	public void displayDifferenceViewer_cannotGetConfiguredElementFromId() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String id = "id";
		List<ChangeRecord> changeRecords = List.of(changeRecord);
		ConfiguredElementDomain configuredElementDomain = mock(ConfiguredElementDomain.class);

		when(configurationManagementService.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
		when(configuredElementDomain.getConfiguredElementUsingId(id, configurationManagementService)).thenReturn(null);

		changeRecordDomain.displayDifferenceViewer(rowView, id, changeRecords, false, configurationManagementService);

		verify(configurationManagementService).getConfiguredElementDomain();
		verify(rowView, never()).getChangeRecordNameColumn();
	}

	@Test
	public void displayDifferenceViewer_cannotGetChangeRecordFromId() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String ceId = "ceId";
		String crId = "crId";
		List<ChangeRecord> changeRecords = List.of(changeRecord);
		ConfiguredElementDomain configuredElementDomain = mock(ConfiguredElementDomain.class);

		when(configurationManagementService.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
		when(configuredElementDomain.getConfiguredElementUsingId(ceId, configurationManagementService)).thenReturn(configuredElement);
		when(rowView.getChangeRecordLocalId()).thenReturn(crId);
		doReturn(null).when(changeRecordDomain).getChangeRecordUsingId(crId, configurationManagementService);
		doNothing().when(uiDomain).logError(logger, String.format(ExceptionConstants.MODEL_INCONSISTENT_WITH_UI, crId));

		changeRecordDomain.displayDifferenceViewer(rowView, ceId, changeRecords, false, configurationManagementService);

		verify(configurationManagementService).getConfiguredElementDomain();
		verify(uiDomain).logError(logger, String.format(ExceptionConstants.MODEL_INCONSISTENT_WITH_UI, crId));
		verify(rowView, never()).getChangeRecordNameColumn();
	}

	@Test
	public void displayDifferenceViewer_baselineVersionLargerThanReleaseVersion() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String ceId = "ceId";
		String crId = "crId";
		List<ChangeRecord> changeRecords = List.of(changeRecord);
		ConfiguredElementDomain configuredElementDomain = mock(ConfiguredElementDomain.class);
		String selectedName = "selectedName";
		Integer baselineVersion = 2;
		Integer releaseVersion = 1;

		when(configurationManagementService.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
		when(configuredElementDomain.getConfiguredElementUsingId(ceId, configurationManagementService)).thenReturn(configuredElement);
		when(rowView.getChangeRecordLocalId()).thenReturn(crId);
		doReturn(changeRecord).when(changeRecordDomain).getChangeRecordUsingId(crId, configurationManagementService);
		when(rowView.getChangeRecordNameColumn()).thenReturn(selectedName);
		when(changeRecord.getName()).thenReturn(selectedName);
		doReturn(baselineVersion).when(changeRecordDomain).determinePreviousRevisionModelVersion(configuredElement,
				changeRecord, changeRecords, revision, configurationManagementService);
		doReturn(releaseVersion).when(changeRecordDomain).determineCurrentRevisionModelVersion(changeRecord,
				configuredElement, revision, configurationManagementService);
		doNothing().when(apiDomain).performProjectVersionComparison(configuredElement, baselineVersion, releaseVersion,
				false);
		doReturn(revision).when(rowView).getRevisionColumn();

		changeRecordDomain.displayDifferenceViewer(rowView, ceId, changeRecords, false, configurationManagementService);

		verify(uiDomain, never()).logError(logger, String.format(ExceptionConstants.MODEL_INCONSISTENT_WITH_UI, crId));
		verify(apiDomain, never()).performProjectVersionComparison(configuredElement, baselineVersion, releaseVersion, false);
		verify(uiDomain).logError(logger, String.format(ExceptionConstants.PROJECT_COMPARISON_IMPROPER_VERSION,
				selectedName, baselineVersion, releaseVersion));
	}

	@Test
	public void displayDifferenceViewer_releaseVersionInvalid() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String ceId = "ceId";
		String crId = "crId";
		List<ChangeRecord> changeRecords = List.of(changeRecord);
		ConfiguredElementDomain configuredElementDomain = mock(ConfiguredElementDomain.class);
		String selectedName = "selectedName";
		Integer baselineVersion = 0;
		Integer releaseVersion = 1;

		when(configurationManagementService.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
		when(configuredElementDomain.getConfiguredElementUsingId(ceId, configurationManagementService)).thenReturn(configuredElement);
		when(rowView.getChangeRecordLocalId()).thenReturn(crId);
		doReturn(changeRecord).when(changeRecordDomain).getChangeRecordUsingId(crId, configurationManagementService);
		when(rowView.getChangeRecordNameColumn()).thenReturn(selectedName);
		when(changeRecord.getName()).thenReturn(selectedName);
		doReturn(baselineVersion).when(changeRecordDomain).determinePreviousRevisionModelVersion(configuredElement,
			changeRecord, changeRecords, revision, configurationManagementService);
		doReturn(releaseVersion).when(changeRecordDomain).determineCurrentRevisionModelVersion(changeRecord,
			configuredElement, revision, configurationManagementService);
		doNothing().when(uiDomain).logError(logger, String.format(ExceptionConstants.PROJECT_COMPARISON_IMPROPER_VERSION,
			selectedName, baselineVersion, releaseVersion));
		doReturn(revision).when(rowView).getRevisionColumn();

		changeRecordDomain.displayDifferenceViewer(rowView, ceId, changeRecords, false, configurationManagementService);

		verify(uiDomain, never()).logError(logger, String.format(ExceptionConstants.MODEL_INCONSISTENT_WITH_UI, crId));
		verify(apiDomain, never()).performProjectVersionComparison(configuredElement, baselineVersion, releaseVersion,
			false);
		verify(uiDomain).logError(logger, String.format(ExceptionConstants.PROJECT_COMPARISON_IMPROPER_VERSION,
			selectedName, baselineVersion, releaseVersion));
	}

	@Test
	public void displayDifferenceViewer_versionsImproperForComparison() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String ceId = "ceId";
		String crId = "crId";
		List<ChangeRecord> changeRecords = List.of(changeRecord);
		ConfiguredElementDomain configuredElementDomain = mock(ConfiguredElementDomain.class);
		String selectedName = "selectedName";
		Integer baselineVersion = 1;
		Integer releaseVersion = 1;

		when(configurationManagementService.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
		when(configuredElementDomain.getConfiguredElementUsingId(ceId, configurationManagementService)).thenReturn(configuredElement);
		when(rowView.getChangeRecordLocalId()).thenReturn(crId);
		doReturn(changeRecord).when(changeRecordDomain).getChangeRecordUsingId(crId, configurationManagementService);
		when(rowView.getChangeRecordNameColumn()).thenReturn(selectedName);
		when(changeRecord.getName()).thenReturn(selectedName);
		doReturn(baselineVersion).when(changeRecordDomain).determinePreviousRevisionModelVersion(configuredElement,
			changeRecord, changeRecords, revision, configurationManagementService);
		doReturn(releaseVersion).when(changeRecordDomain).determineCurrentRevisionModelVersion(changeRecord,
			configuredElement, revision, configurationManagementService);
		doNothing().when(uiDomain).logError(logger, String.format(ExceptionConstants.PROJECT_COMPARISON_IMPROPER_VERSION,
			selectedName, baselineVersion, releaseVersion));
		doReturn(revision).when(rowView).getRevisionColumn();

		changeRecordDomain.displayDifferenceViewer(rowView, ceId, changeRecords, false, configurationManagementService);

		verify(uiDomain, never()).logError(logger, String.format(ExceptionConstants.MODEL_INCONSISTENT_WITH_UI, crId));
		verify(apiDomain, never()).performProjectVersionComparison(configuredElement, baselineVersion, releaseVersion, false);
		verify(uiDomain).logError(logger, String.format(ExceptionConstants.VERSIONS_ARE_IDENTICAL,
			selectedName, baselineVersion, releaseVersion));
	}

	@Test
	public void displayDifferenceViewer_comparisonAttemptedOnMostRecentCommitWithDirtyProjectAndUserClickedCancel() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String ceId = "ceId";
		String crId = "crId";
		List<ChangeRecord> changeRecords = List.of(changeRecord);
		ConfiguredElementDomain configuredElementDomain = mock(ConfiguredElementDomain.class);
		String selectedName = "selectedName";
		Integer baselineVersion = 1;
		Integer releaseVersion = 2;
		int selection = -1;

		when(configurationManagementService.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
		when(configuredElementDomain.getConfiguredElementUsingId(ceId, configurationManagementService)).thenReturn(configuredElement);
		when(rowView.getChangeRecordLocalId()).thenReturn(crId);
		doReturn(changeRecord).when(changeRecordDomain).getChangeRecordUsingId(crId, configurationManagementService);
		when(rowView.getChangeRecordNameColumn()).thenReturn(selectedName);
		when(changeRecord.getName()).thenReturn(selectedName);
		doReturn(baselineVersion).when(changeRecordDomain).determinePreviousRevisionModelVersion(configuredElement,
				changeRecord, changeRecords, revision, configurationManagementService);
		doReturn(releaseVersion).when(changeRecordDomain).determineCurrentRevisionModelVersion(changeRecord,
				configuredElement, revision, configurationManagementService);
		doReturn(revision).when(rowView).getRevisionColumn();
		when(apiDomain.isRevisionMostRecentAndProjectDirty(configuredElement, releaseVersion)).thenReturn(true);
		when(uiDomain.askForConfirmation(ExceptionConstants.COMPARISON_REQUIRES_COMMIT_MESSAGE,
				ExceptionConstants.COMPARISON_REQUIRES_COMMIT_TITLE)).thenReturn(selection);
		when(uiDomain.isOkOption(selection)).thenReturn(false);

		changeRecordDomain.displayDifferenceViewer(rowView, ceId, changeRecords, false, configurationManagementService);

		verify(uiDomain, never()).logError(logger, String.format(ExceptionConstants.MODEL_INCONSISTENT_WITH_UI, crId));
		verify(apiDomain, never()).performProjectVersionComparison(configuredElement, baselineVersion, releaseVersion, false);
		verify(uiDomain, never()).logError(logger, String.format(ExceptionConstants.PROJECT_COMPARISON_IMPROPER_VERSION,
				selectedName, baselineVersion, releaseVersion));
		verify(uiDomain).askForConfirmation(ExceptionConstants.COMPARISON_REQUIRES_COMMIT_MESSAGE,
				ExceptionConstants.COMPARISON_REQUIRES_COMMIT_TITLE);
	}

	@Test
	public void displayDifferenceViewer_comparisonAttemptedOnMostRecentCommitWithDirtyProjectButUserClickedOk() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String ceId = "ceId";
		String crId = "crId";
		List<ChangeRecord> changeRecords = List.of(changeRecord);
		ConfiguredElementDomain configuredElementDomain = mock(ConfiguredElementDomain.class);
		String selectedName = "selectedName";
		Integer baselineVersion = 1;
		Integer releaseVersion = 2;
		int selection = 0;

		when(configurationManagementService.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
		when(configuredElementDomain.getConfiguredElementUsingId(ceId, configurationManagementService)).thenReturn(configuredElement);
		when(rowView.getChangeRecordLocalId()).thenReturn(crId);
		doReturn(changeRecord).when(changeRecordDomain).getChangeRecordUsingId(crId, configurationManagementService);
		when(rowView.getChangeRecordNameColumn()).thenReturn(selectedName);
		when(changeRecord.getName()).thenReturn(selectedName);
		doReturn(baselineVersion).when(changeRecordDomain).determinePreviousRevisionModelVersion(configuredElement,
				changeRecord, changeRecords, revision, configurationManagementService);
		doReturn(releaseVersion).when(changeRecordDomain).determineCurrentRevisionModelVersion(changeRecord,
				configuredElement, revision, configurationManagementService);
		doReturn(revision).when(rowView).getRevisionColumn();
		when(apiDomain.isRevisionMostRecentAndProjectDirty(configuredElement, releaseVersion)).thenReturn(true);
		when(uiDomain.askForConfirmation(ExceptionConstants.COMPARISON_REQUIRES_COMMIT_MESSAGE,
				ExceptionConstants.COMPARISON_REQUIRES_COMMIT_TITLE)).thenReturn(selection);
		when(uiDomain.isOkOption(selection)).thenReturn(true);
		doNothing().when(apiDomain).performProjectVersionComparison(configuredElement, baselineVersion, releaseVersion, false);

		changeRecordDomain.displayDifferenceViewer(rowView, ceId, changeRecords, false, configurationManagementService);

		verify(uiDomain, never()).logError(logger, String.format(ExceptionConstants.MODEL_INCONSISTENT_WITH_UI, crId));
		verify(apiDomain).performProjectVersionComparison(configuredElement, baselineVersion, releaseVersion, false);
		verify(uiDomain, never()).logError(logger, String.format(ExceptionConstants.PROJECT_COMPARISON_IMPROPER_VERSION,
				selectedName, baselineVersion, releaseVersion));
		verify(uiDomain).askForConfirmation(ExceptionConstants.COMPARISON_REQUIRES_COMMIT_MESSAGE,
				ExceptionConstants.COMPARISON_REQUIRES_COMMIT_TITLE);
	}

	@Test
	public void displayDifferenceViewer_comparisonPerformed() {
		ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
		String ceId = "ceId";
		String crId = "crId";
		List<ChangeRecord> changeRecords = List.of(changeRecord);
		ConfiguredElementDomain configuredElementDomain = mock(ConfiguredElementDomain.class);
		String selectedName = "selectedName";
		Integer baselineVersion = 1;
		Integer releaseVersion = 2;

		when(configurationManagementService.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
		when(configuredElementDomain.getConfiguredElementUsingId(ceId, configurationManagementService)).thenReturn(configuredElement);
		when(rowView.getChangeRecordLocalId()).thenReturn(crId);
		doReturn(changeRecord).when(changeRecordDomain).getChangeRecordUsingId(crId, configurationManagementService);
		when(rowView.getChangeRecordNameColumn()).thenReturn(selectedName);
		when(changeRecord.getName()).thenReturn(selectedName);
		doReturn(baselineVersion).when(changeRecordDomain).determinePreviousRevisionModelVersion(configuredElement,
				changeRecord, changeRecords, revision, configurationManagementService);
		doReturn(releaseVersion).when(changeRecordDomain).determineCurrentRevisionModelVersion(changeRecord,
				configuredElement, revision, configurationManagementService);
		doReturn(revision).when(rowView).getRevisionColumn();
		when(apiDomain.isRevisionMostRecentAndProjectDirty(configuredElement, releaseVersion)).thenReturn(false);
		doNothing().when(apiDomain).performProjectVersionComparison(configuredElement, baselineVersion, releaseVersion, false);

		changeRecordDomain.displayDifferenceViewer(rowView, ceId, changeRecords, false, configurationManagementService);

		verify(uiDomain, never()).logError(logger, String.format(ExceptionConstants.MODEL_INCONSISTENT_WITH_UI, crId));
		verify(apiDomain).performProjectVersionComparison(configuredElement, baselineVersion, releaseVersion, false);
		verify(uiDomain, never()).logError(logger, String.format(ExceptionConstants.PROJECT_COMPARISON_IMPROPER_VERSION,
				selectedName, baselineVersion, releaseVersion));
		verify(uiDomain, never()).askForConfirmation(ExceptionConstants.COMPARISON_REQUIRES_COMMIT_MESSAGE,
				ExceptionConstants.COMPARISON_REQUIRES_COMMIT_TITLE);
	}

	@Test
	public void getChangeRecordUsingId_nullId() {
		assertNull(changeRecordDomain.getChangeRecordUsingId(null, configurationManagementService));
		verify(apiDomain, never()).getElementUsingId(anyString());
	}

	@Test
	public void getChangeRecordUsingId_nullElementFromApi() {
		String id = "id";

		when(apiDomain.getElementUsingId(id)).thenReturn(null);

		assertNull(changeRecordDomain.getChangeRecordUsingId(id, configurationManagementService));
		verify(apiDomain).getElementUsingId(id);
		verify(configurationManagementService, never()).getChangeRecord(any());
	}

	@Test
	public void getChangeRecordUsingId_elementRetrieved() {
		String id = "id";

		when(apiDomain.getElementUsingId(id)).thenReturn(element);
		when(configurationManagementService.getChangeRecord(element)).thenReturn(changeRecord);

		assertEquals(changeRecord, changeRecordDomain.getChangeRecordUsingId(id, configurationManagementService));
		verify(configurationManagementService).getChangeRecord(element);
	}
}
