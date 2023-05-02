package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.application.BaseModule;
import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.controller.AbstractCmDispatcher;
import org.openmbee.plugin.cfgmgmt.controller.CmDispatcher;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ChangeRecordDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.RevisionHistoryRecord;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.view.ElementHistoryRowView;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestElementHistoryAction {
    private ElementHistoryAction elementHistoryAction;
    private ConfigurationManagementService configurationManagementService;
    private ApiDomain apiDomain;
    private LifecycleObjectFactory lifecycleObjectFactory;
    private ConfiguredElementDomain configuredElementDomain;
    private ChangeRecordDomain changeRecordDomain;
    private UIDomain uiDomain;
    private Logger logger;
    private ActionEvent evt;
    private AbstractCmDispatcher cmDispatcher;

    private Element element;
    private Stereotype stereotype;
    private ConfiguredElement configuredElement;
    private RevisionHistoryRecord revisionHistoryRecord;
    private ChangeRecord changeRecord;
    private CmControllerSettings cmControllerSettings;
    private BaseModule baseModule;

    @Before
    public void setup() {
        baseModule = mock(BaseModule.class);
        cmControllerSettings = mock(CmControllerSettings.class);
        configurationManagementService = mock(ConfigurationManagementService.class);
        cmDispatcher = mock(CmDispatcher.class);
        elementHistoryAction = spy(new ElementHistoryAction(configurationManagementService, cmDispatcher));
        apiDomain = mock(ApiDomain.class);
        lifecycleObjectFactory = mock(LifecycleObjectFactory.class);
        configuredElementDomain = mock(ConfiguredElementDomain.class);
        changeRecordDomain = mock(ChangeRecordDomain.class);
        uiDomain = mock(UIDomain.class);
        logger = mock(Logger.class);
        evt = mock(ActionEvent.class);

        element = mock(Element.class);
        stereotype = mock(Stereotype.class);
        configuredElement = mock(ConfiguredElement.class);
        changeRecord = mock(ChangeRecord.class);
        revisionHistoryRecord = mock(RevisionHistoryRecord.class);

        when(configurationManagementService.getApiDomain()).thenReturn(apiDomain);
        when(configurationManagementService.getLifecycleObjectFactory()).thenReturn(lifecycleObjectFactory);
        when(configurationManagementService.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
        when(configurationManagementService.getChangeRecordDomain()).thenReturn(changeRecordDomain);
        when(configurationManagementService.getUIDomain()).thenReturn(uiDomain);
        doReturn(logger).when(elementHistoryAction).getLogger();
    }

    @Test
    public void actionPerformed_selectedElementNotConfigured() {
        doReturn(null).when(elementHistoryAction).getConfiguredElement();

        elementHistoryAction.actionPerformed(evt);

        verify(configurationManagementService, never()).getAllRevisionHistoryRecords();
    }

    @Test
    public void actionPerformed_sortedProperly() {
        RevisionHistoryRecord unrelatedRevisionRecord = mock(RevisionHistoryRecord.class);
        RevisionHistoryRecord relatedButWithoutCe = mock(RevisionHistoryRecord.class);
        List<RevisionHistoryRecord> relatedRevisionRecords = new ArrayList<>();
        relatedRevisionRecords.add(revisionHistoryRecord);
        relatedRevisionRecords.add(unrelatedRevisionRecord);
        relatedRevisionRecords.add(relatedButWithoutCe);
        List<RevisionHistoryRecord> filteredRevisionRecords = new ArrayList<>();
        filteredRevisionRecords.add(revisionHistoryRecord);
        filteredRevisionRecords.add(relatedButWithoutCe);
        List<ChangeRecord> affectingChangeRecords = new ArrayList<>();
        affectingChangeRecords.add(changeRecord);
        List<ElementHistoryRowView> rowEntries = new ArrayList<>();
        ElementHistoryRowView row = mock(ElementHistoryRowView.class);
        rowEntries.add(row);

        doReturn(configuredElement).when(elementHistoryAction).getConfiguredElement();
        when(configurationManagementService.getAllRevisionHistoryRecords()).thenReturn(relatedRevisionRecords);
        when(revisionHistoryRecord.getConfiguredElement()).thenReturn(configuredElement);
        when(unrelatedRevisionRecord.getConfiguredElement()).thenReturn(mock(ConfiguredElement.class));
        when(relatedButWithoutCe.getConfiguredElement()).thenReturn(null);
        doNothing().when(relatedButWithoutCe).setConfiguredElement(configuredElement);
        when(configuredElementDomain.getRelevantChangeRecords(configuredElement,
                configurationManagementService)).thenReturn(affectingChangeRecords);
        doNothing().when(changeRecordDomain).sortChangeRecordsByReleaseStatusAndTime(affectingChangeRecords, filteredRevisionRecords);
        doNothing().when(changeRecordDomain).determineRevisionHistoryRecordInterleaving(filteredRevisionRecords);
        doReturn(cmDispatcher).when(elementHistoryAction).getCmDispatcher();
        doReturn(cmControllerSettings).when(elementHistoryAction).getCmControllerSettings(configuredElement, rowEntries,
            affectingChangeRecords);
        doReturn(rowEntries).when(elementHistoryAction).generateElementHistoryRows(configuredElement, filteredRevisionRecords, affectingChangeRecords);
        when(uiDomain.getElementHistoryMatrixModule()).thenReturn(baseModule);

        elementHistoryAction.actionPerformed(evt);

        verify(uiDomain).getElementHistoryMatrixModule();
    }

    @Test
    public void getConfiguredElement_nullObject() {
        doReturn(null).when(elementHistoryAction).getSelectedObjectOverride();

        assertNull(elementHistoryAction.getConfiguredElement());
    }

    @Test
    public void getConfiguredElement_wrongType() {
        doReturn("wrongType").when(elementHistoryAction).getSelectedObjectOverride();

        assertNull(elementHistoryAction.getConfiguredElement());
    }

    @Test
    public void getConfiguredElement_selectObjectIsACe() {
        doReturn(element).when(elementHistoryAction).getSelectedObjectOverride();
        when(configurationManagementService.getConfiguredElement(element)).thenReturn(configuredElement);

        assertEquals(configuredElement, elementHistoryAction.getConfiguredElement());
    }

    @Test
    public void generateElementHistoryRows() {
        ChangeRecord changeRecord2 = mock(ChangeRecord.class);
        ChangeRecord changeRecordWithoutRevisionRecord = mock(ChangeRecord.class);
        RevisionHistoryRecord interleavedRevisionRecord = mock(RevisionHistoryRecord.class);
        RevisionHistoryRecord revisionWithoutReleaseAuthority = mock(RevisionHistoryRecord.class);
        List<RevisionHistoryRecord> interleavedRevisionRecordList = new ArrayList<>();
        interleavedRevisionRecordList.add(interleavedRevisionRecord);
        List<ChangeRecord> relevantChangeRecords = new ArrayList<>();
        relevantChangeRecords.add(changeRecord);
        relevantChangeRecords.add(changeRecord2);
        relevantChangeRecords.add(changeRecordWithoutRevisionRecord);
        List<RevisionHistoryRecord> relatedRevisionRecords = new ArrayList<>();
        ElementHistoryRowView rowView = mock(ElementHistoryRowView.class);
        ElementHistoryRowView rowView2 = mock(ElementHistoryRowView.class);
        ElementHistoryRowView rowView3 = mock(ElementHistoryRowView.class);
        List<RevisionHistoryRecord> revisionHistoryRecordList = new ArrayList<>();
        revisionHistoryRecordList.add(revisionHistoryRecord);
        revisionHistoryRecordList.add(revisionWithoutReleaseAuthority);

        doReturn(revisionHistoryRecordList).when(elementHistoryAction).getRevisionHistoryRecordFromChangeRecord(relatedRevisionRecords, changeRecord);
        doReturn(interleavedRevisionRecordList).when(elementHistoryAction).getRevisionHistoryRecordFromChangeRecord(relatedRevisionRecords, changeRecord2);
        doReturn(rowView).when(elementHistoryAction).setupRowView(revisionHistoryRecord);
        when(revisionHistoryRecord.isInterleavedWithAnotherRevision()).thenReturn(false);
        doReturn(null).when(elementHistoryAction).setupRowView(revisionWithoutReleaseAuthority);
        doReturn(rowView2).when(elementHistoryAction).setupRowView(interleavedRevisionRecord);
        when(interleavedRevisionRecord.isInterleavedWithAnotherRevision()).thenReturn(true);
        doNothing().when(rowView2).showRevisionStateIsUnclear();
        doReturn(rowView3).when(elementHistoryAction).setupRowView(changeRecordWithoutRevisionRecord, configuredElement);

        List<ElementHistoryRowView> results = elementHistoryAction.generateElementHistoryRows(configuredElement, relatedRevisionRecords, relevantChangeRecords);

        assertEquals(3, results.size()); // confirms null row view not added
        assertTrue(results.contains(rowView));
        assertTrue(results.contains(rowView2));
        assertTrue(results.contains(rowView3));

        verify(elementHistoryAction).getRevisionHistoryRecordFromChangeRecord(relatedRevisionRecords, changeRecord);
        verify(elementHistoryAction).getRevisionHistoryRecordFromChangeRecord(relatedRevisionRecords, changeRecord2);
        verify(elementHistoryAction).getRevisionHistoryRecordFromChangeRecord(relatedRevisionRecords, changeRecordWithoutRevisionRecord);
        verify(elementHistoryAction).setupRowView(revisionHistoryRecord);
        verify(revisionHistoryRecord).isInterleavedWithAnotherRevision();
        verify(elementHistoryAction).setupRowView(interleavedRevisionRecord);
        verify(interleavedRevisionRecord).isInterleavedWithAnotherRevision();
        verify(elementHistoryAction).setupRowView(interleavedRevisionRecord);
        verify(interleavedRevisionRecord).isInterleavedWithAnotherRevision();
        verify(rowView2).showRevisionStateIsUnclear();
        verify(elementHistoryAction).setupRowView(changeRecordWithoutRevisionRecord, configuredElement);
    }

    @Test
    public void getRevisionHistoryRecordFromChangeRecord() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
        revisionHistoryRecords.add(revisionHistoryRecord);

        List<RevisionHistoryRecord> revisionHistoryRecordsFromChangeRecord = new ArrayList<>();
        revisionHistoryRecordsFromChangeRecord.add(revisionHistoryRecord);
        doReturn(changeRecord).when(revisionHistoryRecord).getRevisionReleaseAuthority();

        assertEquals(revisionHistoryRecordsFromChangeRecord, elementHistoryAction.getRevisionHistoryRecordFromChangeRecord(revisionHistoryRecords, changeRecord));
        verify(revisionHistoryRecord, times(2)).getRevisionReleaseAuthority();
    }

    @Test
    public void getRevisionHistoryRecordFromChangeRecord_GetRevisionReleaseAuthority_Null() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
        revisionHistoryRecords.add(revisionHistoryRecord);

        List<RevisionHistoryRecord> revisionHistoryRecordsFromChangeRecord = new ArrayList<>();
        revisionHistoryRecordsFromChangeRecord.add(revisionHistoryRecord);
        doReturn(null).when(revisionHistoryRecord).getRevisionReleaseAuthority();

        assertEquals(0, elementHistoryAction.getRevisionHistoryRecordFromChangeRecord(revisionHistoryRecords, changeRecord).size());
        verify(revisionHistoryRecord).getRevisionReleaseAuthority();
    }

    @Test
    public void getRevisionHistoryRecordFromChangeRecord_noMatches() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
        revisionHistoryRecords.add(revisionHistoryRecord);

        doReturn(mock(ChangeRecord.class)).when(revisionHistoryRecord).getRevisionReleaseAuthority();

        assertTrue(elementHistoryAction.getRevisionHistoryRecordFromChangeRecord(revisionHistoryRecords, changeRecord).isEmpty());
        verify(revisionHistoryRecord, times(2)).getRevisionReleaseAuthority();
    }

    @Test
    public void getRevisionHistoryRecordFromChangeRecord_ifNoRevisionHistoryRecord() {
        assertTrue(elementHistoryAction.getRevisionHistoryRecordFromChangeRecord(List.of(), mock(ChangeRecord.class)).isEmpty());
        verify(revisionHistoryRecord, never()).getRevisionReleaseAuthority();
    }

    @Test
    public void setupRowView_revisionHistoryRecordNull() {
        assertNull(elementHistoryAction.setupRowView(null));
    }

    @Test
    public void setupRowView_revisionHistoryRecordReleaseAuthorityNull() {
        when(revisionHistoryRecord.getRevisionReleaseAuthority()).thenReturn(null);

        assertNull(elementHistoryAction.setupRowView(revisionHistoryRecord));
    }

    @Test
    public void setupRowView_allRevisionHistoryRecordFieldsAvailable() {
        String changeRecordName = "changeRecordName";
        String revisionHistoryRecordName = "revisionHistoryRecordName";
        String creationDate = "creationDate";
        String releaseDate = "releaseDate";
        String trimmedCreationDate = "trimmedCreationDate";
        String trimmedReleaseDate = "trimmedReleaseDate";
        String potentialErrorSuffixCreation = String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, revisionHistoryRecordName);
        String potentialErrorSuffixRelease = String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, revisionHistoryRecordName);
        String revision = "revision";
        String elementId = "elementId";

        when(revisionHistoryRecord.getRevisionReleaseAuthority()).thenReturn(changeRecord);
        when(changeRecord.getName()).thenReturn(changeRecordName);
        when(revisionHistoryRecord.getName()).thenReturn(revisionHistoryRecordName);
        when(revisionHistoryRecord.getCreationDate()).thenReturn(creationDate);
        when(changeRecordDomain.trimTimestamp(creationDate, potentialErrorSuffixCreation)).thenReturn(trimmedCreationDate);
        when(revisionHistoryRecord.getReleaseDate()).thenReturn(releaseDate);
        when(changeRecordDomain.trimTimestamp(releaseDate, potentialErrorSuffixRelease)).thenReturn(trimmedReleaseDate);
        when(revisionHistoryRecord.getRevision()).thenReturn(revision);
        when(changeRecord.getElement()).thenReturn(element);
        when(element.getLocalID()).thenReturn(elementId);

        ElementHistoryRowView result = elementHistoryAction.setupRowView(revisionHistoryRecord);
        assertEquals(changeRecordName, result.getChangeRecordNameColumn());
        assertEquals(trimmedCreationDate, result.getStartTimeColumn());
        assertEquals(trimmedReleaseDate, result.getCompletionTimeColumn());
        assertEquals(revision, result.getRevisionColumn());
    }

    @Test
    public void setupRowView_changeRecordDoesNotAffectCe() {
        String name = "name";
        String creationDate = "creationDate";
        String localId = "localId";

        when(changeRecord.getAffectedElements()).thenReturn(List.of());
        when(changeRecord.getName()).thenReturn(name);
        when(configuredElement.getRevisionCreationDate()).thenReturn(creationDate);
        when(changeRecordDomain.trimTimestamp(creationDate,
            String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, name))).thenReturn(creationDate);
        when(configuredElement.getName()).thenReturn(name);
        when(changeRecord.getElement()).thenReturn(element);
        when(element.getLocalID()).thenReturn(localId);
        
        ElementHistoryRowView result = elementHistoryAction.setupRowView(changeRecord, configuredElement);
        assertEquals(name, result.getChangeRecordNameColumn());
        assertEquals(creationDate, result.getStartTimeColumn());
        assertEquals(PluginConstant.NO_TIME, result.getCompletionTimeColumn());
        assertEquals(PluginConstant.NO_REVISION_AND_NOT_COMPLETED, result.getRevisionColumn());
    }

    @Test
    public void setupRowView_changeRecordNotReleased() {
        String name = "name";
        String creationDate = "creationDate";
        String localId = "localId";
        List<ConfiguredElement> affected = new ArrayList<>();
        affected.add(configuredElement);

        when(changeRecord.getAffectedElements()).thenReturn(affected);
        when(changeRecord.isReleased()).thenReturn(false);
        when(changeRecord.getName()).thenReturn(name);
        when(configuredElement.getRevisionCreationDate()).thenReturn(creationDate);
        when(changeRecordDomain.trimTimestamp(creationDate,
                String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, name))).thenReturn(creationDate);
        when(configuredElement.getName()).thenReturn(name);
        when(changeRecord.getElement()).thenReturn(element);
        when(element.getLocalID()).thenReturn(localId);

        ElementHistoryRowView result = elementHistoryAction.setupRowView(changeRecord, configuredElement);
        assertEquals(name, result.getChangeRecordNameColumn());
        assertEquals(creationDate, result.getStartTimeColumn());
        assertEquals(PluginConstant.NO_TIME, result.getCompletionTimeColumn());
        assertEquals(PluginConstant.NO_REVISION_AND_NOT_COMPLETED, result.getRevisionColumn());
    }

    @Test
    public void setupRowView_changeRecordAffectsCeAndIsReleased() {
        String changeRecordName = "changeRecordName";
        String creationDate = "creationDate";
        String trimmedCreate = "trimmedCreate";
        String releaseDate = "releaseDate";
        String trimmedRelease = "trimmedRelease";
        String configuredName = "configuredName";
        String formattedSuffixCreate = String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, configuredName);
        String formattedSuffixRelease = String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, configuredName);
        String localId = "localId";
        List<ConfiguredElement> affected = new ArrayList<>();
        affected.add(configuredElement);
        String revision = "A";

        when(changeRecord.getAffectedElements()).thenReturn(affected);
        when(changeRecord.isReleased()).thenReturn(true);
        when(changeRecord.getName()).thenReturn(changeRecordName);
        when(configuredElement.getRevisionCreationDate()).thenReturn(creationDate);
        when(configuredElement.getRevisionReleaseDate()).thenReturn(releaseDate);
        when(configuredElement.getName()).thenReturn(configuredName);
        when(changeRecord.getElement()).thenReturn(element);
        when(element.getLocalID()).thenReturn(localId);
        when(changeRecordDomain.trimTimestamp(creationDate, formattedSuffixCreate)).thenReturn(trimmedCreate);
        when(changeRecordDomain.trimTimestamp(releaseDate, formattedSuffixRelease)).thenReturn(trimmedRelease);
        when(configuredElement.getRevision()).thenReturn(revision);

        ElementHistoryRowView result = elementHistoryAction.setupRowView(changeRecord, configuredElement);
        assertEquals(changeRecordName, result.getChangeRecordNameColumn());
        assertEquals(trimmedCreate, result.getStartTimeColumn());
        assertEquals(trimmedRelease, result.getCompletionTimeColumn());
        assertEquals(revision, result.getRevisionColumn());
    }

    @Test
    public void updateState() {
        doReturn(configuredElement).when(elementHistoryAction).getConfiguredElement();
        doNothing().when(elementHistoryAction).setEnabled(true);

        elementHistoryAction.updateState();

        verify(elementHistoryAction).setEnabled(true);
        verify(elementHistoryAction, never()).setEnabled(false);
    }
}
