package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.LifecycleObjectDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.listeners.LifecycleObjectPropertyChangeListener;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.swing.event.ChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestConfiguredElement {
    private ConfiguredElement configuredElement;
    private ConfigurationManagementService configurationManagementService;
    private Element element;
    private ConfiguredElementDomain configuredElementDomain;
    private ChangeRecord changeRecord;
    private LifecycleStatus lifecycleStatus;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private Stereotype baseStereotype;
    private LifecycleObjectDomain lifecycleObjectDomain;
    private LifecycleTransition lifecycleTransition;
    private Logger logger;

    @Before
    public void setup() {
        element = mock(Element.class);
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        baseStereotype = mock(Stereotype.class);
        configuredElementDomain = mock(ConfiguredElementDomain.class);
        lifecycleObjectDomain = mock(LifecycleObjectDomain.class);
        configurationManagementService = mock(ConfigurationManagementService.class);
        configurationManagementService.setLifecycleObjectDomain(lifecycleObjectDomain);
        configuredElement = spy(new ConfiguredElement(configurationManagementService, element));
        changeRecord = mock(ChangeRecord.class);
        lifecycleStatus = mock(LifecycleStatus.class);
        lifecycleTransition = mock(LifecycleTransition.class);
        logger = mock(Logger.class);

        doReturn(baseStereotype).when(configurationManagementService).getBaseCEStereotype();
        doReturn(configuredElementDomain).when(configuredElement).getConfiguredElementDomain();
        doReturn(apiDomain).when(configuredElement).getApiDomain();
        doReturn(uiDomain).when(configuredElement).getUIDomain();
        doReturn(logger).when(configuredElement).getLogger();
    }

    @Test
    public void changeStatus_nullStatusName() {
        doReturn(null).when(configuredElement).getStatusName();

        assertFalse(configuredElement.changeStatus(lifecycleStatus, changeRecord));
        verify(uiDomain).logDebug(PluginConstant.INVALID_STATUS_NAME);
    }

    @Test
    public void changeStatus_exceptionGettingStatusName() {
        Exception exception = mock(NullPointerException.class);

        doThrow(exception).when(configuredElement).getStatusName();
        doNothing().when(uiDomain).logError(logger, ExceptionConstants.ERROR_WHILE_GETTING_STATUS_NAME);

        assertFalse(configuredElement.changeStatus(lifecycleStatus, changeRecord));

        verify(uiDomain).logError(logger, ExceptionConstants.ERROR_WHILE_GETTING_STATUS_NAME);
        verify(uiDomain, never()).logDebug(PluginConstant.INVALID_STATUS_NAME);
    }

    @Test
    public void changeStatus_Released() {
        String currentStatusName = "status";
        String futureStatusName = "futureState";
        String expectedException = String.format(ExceptionConstants.STATUS_CHANGE, currentStatusName, futureStatusName);
        Optional<LifecycleStatus> optional = spy(Optional.of(lifecycleStatus));

        doReturn(currentStatusName).when(configuredElement).getStatusName();
        doReturn(optional).when(configuredElement).getStatus();
        when(optional.isPresent()).thenReturn(true);
        when(lifecycleStatus.isReleased()).thenReturn(true);
        when(lifecycleStatus.getName()).thenReturn(futureStatusName);
        doNothing().when(configuredElementDomain).setReleaseAttributes(configuredElement);
        doNothing().when(apiDomain).unlock(element);

        assertTrue(configuredElement.changeStatus(lifecycleStatus, changeRecord));

        verify(configuredElementDomain).setReleaseAttributes(configuredElement);
        verify(apiDomain).unlock(element);
        verify(changeRecord).addAffectedElement(any(), eq(expectedException));
    }

    @Test
    public void changeStatus_NotReleased() {
        String currentStatusName = "status";
        String futureStatusName = "futureState";
        String expectedException = String.format(ExceptionConstants.STATUS_CHANGE, currentStatusName, futureStatusName);

        doReturn(currentStatusName).when(configuredElement).getStatusName();
        doReturn(false).when(configuredElement).isReleased();
        when(lifecycleStatus.getName()).thenReturn(futureStatusName);

        assertTrue(configuredElement.changeStatus(lifecycleStatus, changeRecord));
        verify(configuredElementDomain, never()).setReleaseAttributes(configuredElement);
        verify(apiDomain, never()).unlock(element);
        verify(changeRecord).addAffectedElement(any(), eq(expectedException));
    }

    @Test
    public void getRevisionCreationDate() {
        String date = "date";
        doReturn(date).when(configuredElementDomain).getRevisionCreationDate(configuredElement);

        String actualDate = configuredElement.getRevisionCreationDate();
        assertEquals(actualDate, date);

        verify(configuredElementDomain).getRevisionCreationDate(configuredElement);
    }

    @Test
    public void getRevisionReleaseDate() {
        String date = "date";
        doReturn(date).when(configuredElementDomain).getRevisionReleaseDate(configuredElement);

        String actualDate = configuredElement.getRevisionReleaseDate();
        assertEquals(actualDate, date);

        verify(configuredElementDomain).getRevisionReleaseDate(configuredElement);
    }

    @Test
    public void canBeRevised_userCannotPerformAction() {
        doReturn(baseStereotype).when(configuredElement).getAppliedStereotype();
        when(configuredElementDomain.canUserPerformAction(configurationManagementService, baseStereotype, "revision")).thenReturn(false);

        assertFalse(configuredElement.canBeRevised());
        verify(uiDomain).logError(PluginConstant.INSUFFICIENT_PERMISSIONS);
    }

    @Test
    public void canBeRevised_userCanPerformActionButNoInitialStatus() {
        doReturn(baseStereotype).when(configuredElement).getAppliedStereotype();
        when(configuredElementDomain.canUserPerformAction(configurationManagementService, baseStereotype, "revision")).thenReturn(true);
        doReturn(null).when(configuredElement).getInitialStatus();

        assertFalse(configuredElement.canBeRevised());
        verify(uiDomain).logError(PluginConstant.INVALID_MATURITY_RATING);
    }

    @Test
    public void canBeRevised_userCanPerformActionButCczOwnerIncompatible() {
        int value = 1;

        doReturn(baseStereotype).when(configuredElement).getAppliedStereotype();
        when(configuredElementDomain.canUserPerformAction(configurationManagementService, baseStereotype, "revision")).thenReturn(true);
        doReturn(lifecycleStatus).when(configuredElement).getInitialStatus();
        when(lifecycleStatus.getMaturityRating()).thenReturn(value);
        doReturn(false).when(configuredElementDomain).validateProposedMaturityRatingWRTCczOwner(configuredElement, value);

        assertFalse(configuredElement.canBeRevised());
        verify(uiDomain).logError(PluginConstant.INVALID_MATURITY_RATING);
    }

    @Test
    public void canBeRevised_userCanPerformActionAndCczOwnerCompatible() {
        int value = 1;

        doReturn(baseStereotype).when(configuredElement).getAppliedStereotype();
        when(configuredElementDomain.canUserPerformAction(configurationManagementService, baseStereotype, "revision")).thenReturn(true);
        doReturn(lifecycleStatus).when(configuredElement).getInitialStatus();
        when(lifecycleStatus.getMaturityRating()).thenReturn(value);
        doReturn(true).when(configuredElementDomain).validateProposedMaturityRatingWRTCczOwner(configuredElement, value);

        assertTrue(configuredElement.canBeRevised());
    }

    @Test
    public void canBePromoted_convenienceMethod() {
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElementDomain.canBePromoted(configurationManagementService, element)).thenReturn(true);

        assertTrue(configuredElement.canBePromoted());
    }

    @Test
    public void canBePromoted_invalidComparedToOwner() {
        int crMaturity = 1;
        int current = 1;
        int future = 2;

        when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
        when(changeRecord.getStatusMaturityRating()).thenReturn(crMaturity);
        doReturn(current).when(configuredElement).getStatusMaturityRating();
        when(lifecycleStatus.getMaturityRating()).thenReturn(future);
        doReturn(false).when(configuredElementDomain).validateProposedMaturityRatingWRTOwned(configuredElement, future);

        assertFalse(configuredElement.canBePromoted(lifecycleTransition, changeRecord));
    }

    @Test
    public void canBePromoted_invalidComparedToCczOwner() {
        int crMaturity = 1;
        int current = 2;
        int future = 1;

        when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
        when(changeRecord.getStatusMaturityRating()).thenReturn(crMaturity);
        doReturn(current).when(configuredElement).getStatusMaturityRating();
        when(lifecycleStatus.getMaturityRating()).thenReturn(future);
        doReturn(false).when(configuredElementDomain).validateProposedMaturityRatingWRTCczOwner(configuredElement, future);

        assertFalse(configuredElement.canBePromoted(lifecycleTransition, changeRecord));
    }

    @Test
    public void canBePromoted_invalidComparedToCczOwner1() {
        int crMaturity = 1;
        int current = 2;
        int future = 1;

        when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
        when(changeRecord.getStatusMaturityRating()).thenReturn(crMaturity);
        doReturn(current).when(configuredElement).getStatusMaturityRating();
        when(lifecycleStatus.getMaturityRating()).thenReturn(future);
        doReturn(true).when(configuredElementDomain).validateProposedMaturityRatingWRTCczOwner(configuredElement, future);

        assertFalse(configuredElement.canBePromoted(lifecycleTransition, changeRecord));
    }

    @Test
    public void canBePromoted_changeRecordMaturityTooLarge() {
        int crMaturity = 3;
        int current = 1;
        int future = 2;
        String qualifiedName = "qualifiedName";

        when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
        when(changeRecord.getStatusMaturityRating()).thenReturn(crMaturity);
        doReturn(current).when(configuredElement).getStatusMaturityRating();
        when(lifecycleStatus.getMaturityRating()).thenReturn(future);
        doReturn(true).when(configuredElementDomain).validateProposedMaturityRatingWRTOwned(configuredElement, future);
        when(changeRecord.getQualifiedName()).thenReturn(qualifiedName);

        assertFalse(configuredElement.canBePromoted(lifecycleTransition, changeRecord));
        verify(uiDomain).logError(String.format(ExceptionConstants.UNACCEPTABLE_STATUS_COMPARED_TO_CR, qualifiedName));
    }

    @Test
    public void canBePromoted_validatedButUserLacksPermission() {
        int crMaturity = 1;
        int current = 1;
        int future = 2;
        List<String> roles = new ArrayList<>();
        roles.add("roleOne");

        when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
        when(changeRecord.getStatusMaturityRating()).thenReturn(crMaturity);
        doReturn(current).when(configuredElement).getStatusMaturityRating();
        when(lifecycleStatus.getMaturityRating()).thenReturn(future);
        doReturn(true).when(configuredElementDomain).validateProposedMaturityRatingWRTOwned(configuredElement, future);
        when(lifecycleTransition.getRoles()).thenReturn(roles);
        when(configurationManagementService.userHasPrivileges(roles)).thenReturn(false);

        assertFalse(configuredElement.canBePromoted(lifecycleTransition, changeRecord));
        verify(uiDomain).logError(String.format(ExceptionConstants.INSUFFICIENT_PRIVILEGES, String.join(PluginConstant.COMMA, roles)));
    }

    @Test
    public void canBePromoted_validatedAndUserHasPermission() {
        int crMaturity = 1;
        int current = 1;
        int future = 2;
        List<String> roles = new ArrayList<>();
        roles.add("roleOne");

        when(lifecycleTransition.getTargetStatus()).thenReturn(lifecycleStatus);
        when(changeRecord.getStatusMaturityRating()).thenReturn(crMaturity);
        doReturn(current).when(configuredElement).getStatusMaturityRating();
        when(lifecycleStatus.getMaturityRating()).thenReturn(future);
        doReturn(true).when(configuredElementDomain).validateProposedMaturityRatingWRTOwned(configuredElement, future);
        when(lifecycleTransition.getRoles()).thenReturn(roles);
        when(configurationManagementService.userHasPrivileges(roles)).thenReturn(true);

        assertTrue(configuredElement.canBePromoted(lifecycleTransition, changeRecord));
    }

    @Test
    public void revise_cannotChangeStatus() {
        doReturn(false).when(configuredElementDomain).setStatusToInWork(configuredElement);

        assertFalse(configuredElement.revise());
        verify(uiDomain).logError(ExceptionConstants.CANNOT_REVISE_DUE_TO_STATUS_ISSUE);
    }

    @Test
    public void revise_revisionAttempted() {
        LifecycleObjectPropertyChangeListener listener1 = spy(new LifecycleObjectPropertyChangeListener(LifecycleObjectPropertyChangeListener.Property.STATUS) {
            @Override
            public void stateChanged(ChangeEvent e) {
            }
        });
        LifecycleObjectPropertyChangeListener listener2 = spy(new LifecycleObjectPropertyChangeListener(LifecycleObjectPropertyChangeListener.Property.REVISION) {
            @Override
            public void stateChanged(ChangeEvent e) {
            }
        });
        Class clazz = mock(Class.class);
        String newRev = "newRevision";
        ChangeEvent changeEvent = mock(ChangeEvent.class);
        configuredElement.propertyChangeListeners.add(listener1);
        configuredElement.propertyChangeListeners.add(listener2);

        doReturn(true).when(configuredElementDomain).setStatusToInWork(configuredElement);
        when(configuredElementDomain.createRevisionHistoryRecord(configuredElement)).thenReturn(clazz);
        when(configuredElementDomain.iterateRevision(configuredElement)).thenReturn(newRev);
        doReturn(LifecycleObjectPropertyChangeListener.Property.STATUS).when(listener1).getProp();
        doReturn(LifecycleObjectPropertyChangeListener.Property.REVISION).when(listener2).getProp();
        when(configuredElement.makeEvent()).thenReturn(changeEvent);

        assertTrue(configuredElement.revise());

        verify(listener1, never()).stateChanged(changeEvent);
        verify(listener2).stateChanged(changeEvent);
    }

    @Test
    public void getCCZOwner() {
        when(configurationManagementService.getCCZOwner(element)).thenReturn(configuredElement);

        assertNotNull(configuredElement.getCCZOwner());
    }

    @Test
    public void getOwnedConfiguredElements() {
        List<ConfiguredElement> configuredElements = new ArrayList<>();
        configuredElements.add(configuredElement);
        when(configurationManagementService.getOwnedConfiguredElements(element)).thenReturn(configuredElements);

        assertNotNull(configuredElement.getOwnedConfiguredElements());
    }

    @Test
    public void isInReadOnlyCCZ() {
        when(configurationManagementService.isInReadOnlyCCZ(element)).thenReturn(true);

        assertTrue(configuredElement.isInReadOnlyCCZ());
    }

    @Test
    public void isInReleasedCCZ() {
        when(configurationManagementService.isInReleasedCCZ(element)).thenReturn(true);

        assertTrue(configuredElement.isInReleasedCCZ());
    }

    @Test
    public void getDisplayName() {
        when(configuredElementDomain.getDisplayName(configuredElement, "originalName")).thenReturn("name");

        assertEquals("name", configuredElement.getDisplayName("originalName"));
    }

    @Test
    public void isReadyForRelease_statusUnavailable() {
        doReturn(Optional.empty()).when(configuredElement).getStatus();

        assertFalse(configuredElement.isReadyForRelease());
    }

    @Test
    public void isReadyForRelease_statusAvailableButNotReady() {
        doReturn(Optional.of(lifecycleStatus)).when(configuredElement).getStatus();
        when(lifecycleStatus.isReadyForRelease()).thenReturn(false);

        assertFalse(configuredElement.isReadyForRelease());
    }

    @Test
    public void isReadyForRelease_statusAvailableAndReady() {
        doReturn(Optional.of(lifecycleStatus)).when(configuredElement).getStatus();
        when(lifecycleStatus.isReadyForRelease()).thenReturn(true);

        assertTrue(configuredElement.isReadyForRelease());
    }
}