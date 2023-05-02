package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ChangeRecordDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestChangeRecord {
	private ConfigurationManagementService configurationManagementService;
	private ChangeRecord changeRecord;
	private ChangeRecordDomain changeRecordDomain;
	private Stereotype stereotype;
	private Element element;
	private LifecycleStatus lifecycleStatus;
	private LifecycleTransition lifecycleTransition;
	private ConfiguredElement configuredElement;
	private ApiDomain apiDomain;
	private UIDomain uiDomain;

	@Before
	public void setUp() {
		configurationManagementService = mock(ConfigurationManagementService.class);
		changeRecord = spy(new ChangeRecord(configurationManagementService, mock(Class.class)));
		changeRecordDomain = mock(ChangeRecordDomain.class);
		stereotype = mock(Stereotype.class);
		configuredElement = mock(ConfiguredElement.class);
		element = mock(Element.class);
		lifecycleStatus = mock(LifecycleStatus.class);
		lifecycleTransition = mock(LifecycleTransition.class);
		apiDomain = mock(ApiDomain.class);
		uiDomain = mock(UIDomain.class);

		doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
		when(changeRecord.getChangeRecordDomain()).thenReturn(changeRecordDomain);
		doReturn(apiDomain).when(changeRecord).getApiDomain();
		doReturn(uiDomain).when(changeRecord).getUIDomain();
	}

	@Test
	public void getChangeRecordDomain() {
		assertNotNull(changeRecord.getChangeRecordDomain());
	}

	@Test
	public void changeStatus_released() {
		doReturn(true).when(lifecycleStatus).isReleased();
		doReturn(true).when(configurationManagementService).getAutomateReleaseSwitch();
		doReturn(true).when(changeRecord).releaseAffectedElements();
		changeRecord.changeStatus(lifecycleStatus, changeRecord);

		verify(changeRecord).releaseAffectedElements();
	}

	@Test
	public void changeStatus_notReleased() {
		State state = setupForChangeStatus(false, true);

		changeRecord.changeStatus(lifecycleStatus, changeRecord);

		verify(changeRecord, never()).releaseAffectedElements();
		verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.STATUS, state);
		verify(configurationManagementService).updateCRStatus();
	}

	@Test
	public void changeStatus_releasedButNotAutomated() {
		State state = setupForChangeStatus(true, false);

		changeRecord.changeStatus(lifecycleStatus, changeRecord);

		verify(changeRecord, never()).releaseAffectedElements();
		verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.STATUS, state);
		verify(configurationManagementService).updateCRStatus();
	}

	protected State setupForChangeStatus(boolean isReleased, boolean automateReleaseSwitch) {
		State state = mock(State.class);

		doReturn(isReleased).when(lifecycleStatus).isReleased();
		doReturn(stereotype).when(changeRecord).getBaseStereotype();
		doReturn(automateReleaseSwitch).when(configurationManagementService).getAutomateReleaseSwitch();
		doReturn(true).when(changeRecord).releaseAffectedElements();
		doReturn(state).when(lifecycleStatus).getState();
		doReturn(element).when(changeRecord).getElement();

		return state;
	}

	@Test
	public void changeStatus_nullStatus() {
		changeRecord.changeStatus(null, changeRecord);

		verify(changeRecord, never()).releaseAffectedElements();
	}

	@Test
	public void changeStatus_nullChangeRecord() {
		changeRecord.changeStatus(lifecycleStatus, null);

		verify(changeRecord, never()).releaseAffectedElements();
	}

	@Test
	public void changeStatus_affectedElementsNotReleased() {
		when(lifecycleStatus.isReleased()).thenReturn(true);
		doReturn(stereotype).when(changeRecord).getBaseStereotype();
		when(configurationManagementService.getAutomateReleaseSwitch()).thenReturn(true);
		doReturn(false).when(changeRecord).releaseAffectedElements();

		assertFalse(changeRecord.changeStatus(lifecycleStatus, null));
	}

	@Test
	public void releaseAffectedElements_noReleasedTransitions() {
		List<ConfiguredElement> configuredElements = new ArrayList<>();
		configuredElements.add(configuredElement);

		doReturn(configuredElements).when(changeRecord).getAffectedElements();
		when(changeRecordDomain.getReleaseTransitionsForElements(changeRecord, configuredElements)).thenReturn(Map.of());

		assertFalse(changeRecord.releaseAffectedElements());
	}

	@Test
	public void releaseAffectedElements_transitionElements() {
		Map<ConfiguredElement, LifecycleStatus> releaseTransitionsMap = new HashMap<>();
		releaseTransitionsMap.put(configuredElement, lifecycleStatus);
		List<ConfiguredElement> configuredElements = new ArrayList<>();
		configuredElements.add(configuredElement);

		doReturn(configuredElements).when(changeRecord).getAffectedElements();
		when(changeRecordDomain.getReleaseTransitionsForElements(changeRecord, configuredElements)).thenReturn(releaseTransitionsMap);
		doReturn(true).when(changeRecordDomain).transitionElements(changeRecord, releaseTransitionsMap, configurationManagementService);

		assertTrue(changeRecord.releaseAffectedElements());
	}

	@Test
	public void releaseAffectedElements_handlingUnexpectedException() {
		Map<ConfiguredElement, LifecycleStatus> releaseTransitionsMap = spy(new HashMap<>());
		List<ConfiguredElement> configuredElements = new ArrayList<>();
		configuredElements.add(configuredElement);
		Exception exception = mock(NullPointerException.class);
		Logger logger = mock(Logger.class);

		try {
			doReturn(configuredElements).when(changeRecord).getAffectedElements();
			when(changeRecordDomain.getReleaseTransitionsForElements(changeRecord, configuredElements)).thenReturn(releaseTransitionsMap);
			doThrow(exception).when(releaseTransitionsMap).isEmpty();
			doReturn(logger).when(changeRecord).getLogger();
			doNothing().when(uiDomain).logError(logger, ExceptionConstants.ERROR_WHILE_CHANGING_STATUS);

			assertFalse(changeRecord.releaseAffectedElements());
			verify(uiDomain).logError(logger, ExceptionConstants.ERROR_WHILE_CHANGING_STATUS);
			verify(configurationManagementService).setLifecycleStatusChanging(true);
		} catch(Exception e) {
			fail("Unexpected exception");
		}
	}

	@Test
	public void checkCEsForReadiness_releasedAndAutomated() {
		List<ConfiguredElement> configuredElements = new ArrayList<>();

		doReturn(configuredElements).when(changeRecord).getAffectedElements();
		when(lifecycleStatus.isReleased()).thenReturn(true);
		doReturn(true).when(configurationManagementService).getAutomateReleaseSwitch();
		doReturn(true).when(changeRecordDomain).areAllElementsReadyForRelease(configuredElements);

		assertTrue(changeRecord.checkCEsForReadiness(lifecycleStatus));
	}

	@Test
	public void checkCEsForReadiness_automatedButNotReleased() {
		List<ConfiguredElement> configuredElements = new ArrayList<>();
		int future = 12;

		doReturn(configuredElements).when(changeRecord).getAffectedElements();
		when(lifecycleStatus.isReleased()).thenReturn(false);
		doReturn(true).when(configurationManagementService).getAutomateReleaseSwitch();
		when(lifecycleStatus.getMaturityRating()).thenReturn(future);
		doReturn(true).when(changeRecordDomain).areAllElementsProperlyMature(configuredElements, future, changeRecord);

		assertTrue(changeRecord.checkCEsForReadiness(lifecycleStatus));
	}

	@Test
	public void checkCEsForReadiness_releasedButNotAutomated() {
		List<ConfiguredElement> configuredElements = new ArrayList<>();
		int future = 12;

		doReturn(configuredElements).when(changeRecord).getAffectedElements();
		when(lifecycleStatus.isReleased()).thenReturn(true);
		doReturn(false).when(configurationManagementService).getAutomateReleaseSwitch();
		when(lifecycleStatus.getMaturityRating()).thenReturn(future);
		doReturn(true).when(changeRecordDomain).areAllElementsProperlyMature(configuredElements, future, changeRecord);

		assertTrue(changeRecord.checkCEsForReadiness(lifecycleStatus));
	}

	@Test
	public void checkCEsForReadiness_notReleasedNorAutomated() {
		List<ConfiguredElement> configuredElements = new ArrayList<>();
		int future = 12;

		doReturn(configuredElements).when(changeRecord).getAffectedElements();
		when(lifecycleStatus.isReleased()).thenReturn(false);
		doReturn(false).when(configurationManagementService).getAutomateReleaseSwitch();
		when(lifecycleStatus.getMaturityRating()).thenReturn(future);
		doReturn(true).when(changeRecordDomain).areAllElementsProperlyMature(configuredElements, future, changeRecord);

		assertTrue(changeRecord.checkCEsForReadiness(lifecycleStatus));
	}

	@Test
	public void canBePromoted_configuredElementsNotReady() {
		when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
		doReturn(false).when(changeRecord).checkCEsForReadiness(lifecycleStatus);

		assertFalse(changeRecord.canBePromoted(lifecycleTransition, changeRecord));
	}

	@Test
	public void canBePromoted_userHasPrivileges() {
		List<String> roles = Arrays.asList("admin", "user", "super admin");

		when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
		doReturn(true).when(changeRecord).checkCEsForReadiness(lifecycleStatus);
		when(lifecycleTransition.getRoles()).thenReturn(roles);
		when(configurationManagementService.userHasPrivileges(roles)).thenReturn(true);

		assertTrue(changeRecord.canBePromoted(lifecycleTransition, changeRecord));
	}

	@Test
	public void addAffectedElement() {
		String changeText = "changeText";

		changeRecord.addAffectedElement(configuredElement, changeText);

		verify(changeRecordDomain).addAffectedElement(changeRecord, configuredElement, changeText);
	}

	@Test
	public void getAffectedElements() {
		List<ConfiguredElement> configuredElements = new ArrayList<>();
		configuredElements.add(configuredElement);

		doReturn(element).when(changeRecord).getElement();
		doReturn(stereotype).when(changeRecord).getBaseStereotype();
		doReturn(configuredElements).when(changeRecordDomain).getAffectedElements(element, stereotype, configurationManagementService);

		List<ConfiguredElement> affectedElements = changeRecord.getAffectedElements();
		assertNotNull(affectedElements);
		assertSame(configuredElements, affectedElements);
	}

	@Test
	public void affectsGivenConfiguredElement_doesNotAffectElement() {
		List<ConfiguredElement> affected = List.of(configuredElement);

		doReturn(affected).when(changeRecord).getAffectedElements();

		assertFalse(changeRecord.affectsGivenConfiguredElement(mock(ConfiguredElement.class)));
	}

	@Test
	public void affectsGivenConfiguredElement_affectsElement() {
		List<ConfiguredElement> affected = List.of(configuredElement);

		doReturn(affected).when(changeRecord).getAffectedElements();

		assertTrue(changeRecord.affectsGivenConfiguredElement(configuredElement));
	}

	@Test
	public void isExpandable() {
		doReturn(Optional.of(lifecycleStatus)).when(changeRecord).getStatus();
		when(lifecycleStatus.isExpandable()).thenReturn(true);
		assertTrue(changeRecord.isExpandable());
	}

	@Test
	public void isExpandable_emptyStatus() {
		doReturn(Optional.empty()).when(changeRecord).getStatus();
		when(lifecycleStatus.isExpandable()).thenReturn(true);
		assertFalse(changeRecord.isExpandable());
	}

	@Test
	public void isExpandable_returnsFalse() {
		doReturn(Optional.of(lifecycleStatus)).when(changeRecord).getStatus();
		when(lifecycleStatus.isExpandable()).thenReturn(false);
		assertFalse(changeRecord.isExpandable());
	}

	@Test
	public void getConfigureTimeFromElementComments() {
		ZonedDateTime time = mock(ZonedDateTime.class);
		doReturn(changeRecordDomain).when(changeRecord).getChangeRecordDomain();
		doReturn(time).when(changeRecordDomain).getCreationTimeFromElementComments(changeRecord, configuredElement);

		ZonedDateTime actualTime = changeRecord.getConfigureTimeFromElementComments(configuredElement);
		assertEquals(actualTime, time);

		verify(changeRecordDomain).getCreationTimeFromElementComments(changeRecord, configuredElement);
	}
}
