package org.openmbee.plugin.cfgmgmt.model;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

public class TestRevisionHistoryRecord {
    private RevisionHistoryRecord revisionHistoryRecord;
    private ConfigurationManagementService configurationManagementService;
    private Class classObject;
    private Element element;

    @Before
    public void setup() {
        configurationManagementService = mock(ConfigurationManagementService.class);
        classObject = mock(Class.class);
        revisionHistoryRecord = Mockito.spy(new RevisionHistoryRecord(configurationManagementService, classObject));
        element = mock(Element.class);

        doReturn(configurationManagementService).when(revisionHistoryRecord).getConfigurationManagementService();
        doReturn(classObject).when(revisionHistoryRecord).getElement();
    }

    @Test
    public void getConfiguredElement_noConfiguredElementFound() {
        when(configurationManagementService.getRevisionHistoryPropertyFirstValueGivenType(classObject, PluginConstant.CONFIGURED_ELEMENT_PROPERTY_NAME, Element.class)).thenReturn(null);

        assertNull(revisionHistoryRecord.getConfiguredElement());

        verify(configurationManagementService, never()).getConfiguredElement(any());
    }

    @Test
    public void getConfiguredElement_configuredElementAlreadySet() {
        Element configuredElementObject = mock(Element.class);
        ConfiguredElement configuredElement = Mockito.spy(new ConfiguredElement(configurationManagementService, configuredElementObject));
        revisionHistoryRecord.configuredElement = configuredElementObject;

        when(configurationManagementService.getConfiguredElement(configuredElementObject)).thenReturn(configuredElement);

        assertEquals(configuredElement, revisionHistoryRecord.getConfiguredElement());

        verify(configurationManagementService).getConfiguredElement(configuredElementObject);
        verify(configurationManagementService, never()).getRevisionHistoryPropertyFirstValueGivenType(any(), anyString(), any());
    }

    @Test
    public void getConfiguredElement_configuredElementFound() {
        Element element = mock(Element.class);
        ConfiguredElement configuredElement = Mockito.spy(new ConfiguredElement(configurationManagementService, element));

        when(configurationManagementService.getRevisionHistoryPropertyFirstValueGivenType(classObject, PluginConstant.CONFIGURED_ELEMENT_PROPERTY_NAME, Element.class)).thenReturn(element);
        when(configurationManagementService.getConfiguredElement(element)).thenReturn(configuredElement);

        assertEquals(configuredElement, revisionHistoryRecord.getConfiguredElement());

        verify(configurationManagementService).getConfiguredElement(element);
    }

    @Test
    public void getRevisionReleaseAuthority_noAuthorityFound() {
        when(configurationManagementService.getRevisionHistoryPropertyFirstValueGivenType(classObject,
            PluginConstant.REVISION_RELEASE_AUTHORITY, Class.class)).thenReturn(null);

        assertNull(revisionHistoryRecord.getRevisionReleaseAuthority());

        verify(configurationManagementService, never()).getChangeRecord(any());
    }

    @Test
    public void getRevisionReleaseAuthority_authorityAlreadySet() {
        Class releaseAuthority = mock(Class.class);
        ChangeRecord changeRecord = spy(new ChangeRecord(configurationManagementService, releaseAuthority));
        revisionHistoryRecord.releaseAuthority = releaseAuthority;

        when(configurationManagementService.getChangeRecord(releaseAuthority)).thenReturn(changeRecord);

        assertEquals(changeRecord, revisionHistoryRecord.getRevisionReleaseAuthority());

        verify(configurationManagementService).getChangeRecord(releaseAuthority);
        verify(configurationManagementService, never()).getRevisionHistoryPropertyFirstValueGivenType(classObject,
            PluginConstant.REVISION_RELEASE_AUTHORITY, Class.class);
    }

    @Test
    public void getRevisionReleaseAuthority_authorityFound() {
        Class releaseAuthority = mock(Class.class);
        ChangeRecord changeRecord = spy(new ChangeRecord(configurationManagementService, releaseAuthority));

        when(configurationManagementService.getRevisionHistoryPropertyFirstValueGivenType(classObject,
            PluginConstant.REVISION_RELEASE_AUTHORITY, Class.class)).thenReturn(releaseAuthority);
        when(configurationManagementService.getChangeRecord(releaseAuthority)).thenReturn(changeRecord);

        assertEquals(changeRecord, revisionHistoryRecord.getRevisionReleaseAuthority());

        verify(configurationManagementService).getChangeRecord(releaseAuthority);
        verify(configurationManagementService).getRevisionHistoryPropertyFirstValueGivenType(classObject,
            PluginConstant.REVISION_RELEASE_AUTHORITY, Class.class);
    }

    @Test
    public void getRevision_revisionNotFound() {
        when(configurationManagementService.getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION)).thenReturn(null);

        assertNull(revisionHistoryRecord.getRevision());

        verify(configurationManagementService).getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION);
    }

    @Test
    public void getRevision_revisionAlreadyExists() {
        String revision = "*";
        revisionHistoryRecord.revision = revision;

        assertEquals(revision, revisionHistoryRecord.getRevision());

        verify(configurationManagementService, never()).getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION);
    }

    @Test
    public void getRevision_revisionFound() {
        String revision = "*";

        when(configurationManagementService.getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION)).thenReturn(revision);

        assertEquals(revision, revisionHistoryRecord.getRevision());
    }

    @Test
    public void getModelVersion_versionNotFound() {
        when(configurationManagementService.getRevisionHistoryPropertyFirstValueGivenType(classObject,
            PluginConstant.MODEL_VERSION, Integer.class)).thenReturn(null);

        assertNull(revisionHistoryRecord.getModelVersion());

        verify(configurationManagementService).getRevisionHistoryPropertyFirstValueGivenType(classObject,
            PluginConstant.MODEL_VERSION, Integer.class);
    }

    @Test
    public void getModelVersion_versionAlreadyExists() {
        Integer mVersion = 10;
        revisionHistoryRecord.modelVersion = mVersion;

        assertEquals(mVersion, revisionHistoryRecord.getModelVersion());

        verify(configurationManagementService, never()).getRevisionHistoryPropertyFirstValueGivenType(classObject,
            PluginConstant.MODEL_VERSION, Integer.class);
    }

    @Test
    public void getModelVersion_versionFound() {
        Integer mVersion = 10;
        when(configurationManagementService.getRevisionHistoryPropertyFirstValueGivenType(classObject,
            PluginConstant.MODEL_VERSION, Integer.class)).thenReturn(mVersion);

        assertEquals(mVersion, revisionHistoryRecord.getModelVersion());

        verify(configurationManagementService).getRevisionHistoryPropertyFirstValueGivenType(classObject,
            PluginConstant.MODEL_VERSION, Integer.class);
    }

    @Test
    public void getCreationDate_dateNotFound() {
        when(configurationManagementService.getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION_CREATION_DATE)).thenReturn(null);

        assertNull(revisionHistoryRecord.getCreationDate());

        verify(configurationManagementService).getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION_CREATION_DATE);
    }

    @Test
    public void getCreationDate_dateAlreadyExists() {
        String datetime = "2023-01-09T07:47:34.145531500Z";
        revisionHistoryRecord.creationDate = datetime;

        assertEquals(datetime, revisionHistoryRecord.getCreationDate());

        verify(configurationManagementService, never()).getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION_CREATION_DATE);
    }

    @Test
    public void getCreationDate_dateFound() {
        String datetime = "2023-01-09T07:47:34.145531500Z";
        when(configurationManagementService.getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION_CREATION_DATE)).thenReturn(datetime);

        assertEquals(datetime, revisionHistoryRecord.getCreationDate());

        verify(configurationManagementService).getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION_CREATION_DATE);
    }

    @Test
    public void getReleaseDate_dateNotFound() {
        when(configurationManagementService.getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION_RELEASE_DATE)).thenReturn(null);

        assertNull(revisionHistoryRecord.getReleaseDate());

        verify(configurationManagementService).getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION_RELEASE_DATE);
    }

    @Test
    public void getReleaseDate_dateAlreadyExists() {
        String datetime = "2023-01-09T07:49:07.309616200Z";
        revisionHistoryRecord.releaseDate = datetime;

        assertEquals(datetime, revisionHistoryRecord.getReleaseDate());

        verify(configurationManagementService, never()).getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION_RELEASE_DATE);
    }

    @Test
    public void getReleaseDate_dateFound() {
        String datetime = "2023-01-09T07:49:07.309616200Z";

        when(configurationManagementService.getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION_RELEASE_DATE)).thenReturn(datetime);

        assertEquals(datetime, revisionHistoryRecord.getReleaseDate());

        verify(configurationManagementService).getRevisionHistoryPropertyFirstValueAsString(classObject,
            PluginConstant.REVISION_RELEASE_DATE);
    }
}
