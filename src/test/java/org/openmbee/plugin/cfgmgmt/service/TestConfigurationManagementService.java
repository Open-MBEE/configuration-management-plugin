package org.openmbee.plugin.cfgmgmt.service;

import org.openmbee.plugin.cfgmgmt.IConfigurationManagementPlugin;
import org.openmbee.plugin.cfgmgmt.actions.toolbar.SelectChangeRecordAction;
import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.LifecycleObjectDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.integration.twc.TWCIntegrationException;
import org.openmbee.plugin.cfgmgmt.integration.twc.TeamworkCloudService;
import org.openmbee.plugin.cfgmgmt.listeners.CMPropertyListener;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.openmbee.plugin.cfgmgmt.policy.Policies;
import org.openmbee.plugin.cfgmgmt.settings.CustomSettings;
import org.openmbee.plugin.cfgmgmt.utils.Policy;
import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.Transition;
import org.junit.Before;
import org.junit.Test;
import org.openmbee.plugin.cfgmgmt.model.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestConfigurationManagementService {
    private IConfigurationManagementPlugin configurationManagementPlugin;
    private ConfigurationManagementService configurationManagementService;
    private Stereotype stereotype;
    private CustomSettings customSettings;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private Logger logger;
    private LifecycleObjectDomain lifecycleObjectDomain;
    private LifecycleObjectFactory lifecycleObjectFactory;
    private ConfiguredElementDomain configuredElementDomain;
    private Project project;
    private Package pkg;
    private String cmPath;
    private String changeRecordName;
    private SelectChangeRecordAction selectChangeRecordAction;
    private NamedElement namedElement;
    private ConfiguredElement configuredElement;

    @Before
    public void setup() {
        configurationManagementPlugin = mock(IConfigurationManagementPlugin.class);
        configurationManagementService = spy(new ConfigurationManagementService(configurationManagementPlugin));
        stereotype = mock(Stereotype.class);
        customSettings = mock(CustomSettings.class);
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        logger = mock(Logger.class);
        lifecycleObjectDomain = mock(LifecycleObjectDomain.class);
        lifecycleObjectFactory = mock(LifecycleObjectFactory.class);
        configuredElementDomain = mock(ConfiguredElementDomain.class);
        project = mock(Project.class);
        pkg = mock(Package.class);
        selectChangeRecordAction = mock(SelectChangeRecordAction.class);
        namedElement = mock(NamedElement.class);
        configuredElement = mock(ConfiguredElement.class);

        cmPath = "pathPrefix::customCmPath";
        changeRecordName = "Change Record";

        configurationManagementService.setBaseCEStereotype(stereotype);
        configurationManagementService.setBaseCRStereotype(stereotype);
        configurationManagementService.setApiDomain(apiDomain);
        configurationManagementService.setUIDomain(uiDomain);
        configurationManagementService.setLifecycleObjectDomain(lifecycleObjectDomain);
        configurationManagementService.setLifecycleObjectFactory(lifecycleObjectFactory);
        configurationManagementService.setConfiguredElementDomain(configuredElementDomain);
        configurationManagementService.setChangeRecordName(changeRecordName);

        when(configurationManagementService.getCustomSettings()).thenReturn(customSettings);
        when(configurationManagementService.getLogger()).thenReturn(logger);
        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn(pkg).when(project).getPrimaryModel();
        doReturn(cmPath).when(customSettings).get(PluginConstant.CM_PACKAGE_PATH);
    }

    @Test
    public void getAvailableCEStereotypes_OneAvailable() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype derivativeStereotype = mock(Stereotype.class);
        stereotypes.add(derivativeStereotype);

        doReturn(stereotypes).when(apiDomain).getDerivedStereotypesRecursively(stereotype);

        List<Stereotype> result = configurationManagementService.getAvailableCEStereotypes();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertSame(stereotypes.get(0), result.get(0));
    }

    @Test
    public void getPropertyListener() {
        // as of writing this test, the method uses lazy initialization, and therefore
        // testing it this way is the only way to get coverage on both cases
        CMPropertyListener initial = configurationManagementService.getPropertyListener();
        CMPropertyListener secondary = configurationManagementService.getPropertyListener();

        assertNotNull(initial);
        assertNotNull(secondary);
        assertSame(initial, secondary);
    }

    @Test
    public void getAvailableCEStereotypes_None() {
        List<Stereotype> stereotypes = new ArrayList<>();

        doReturn(stereotypes).when(apiDomain).getDerivedStereotypesRecursively(stereotype);

        List<Stereotype> result = configurationManagementService.getAvailableCEStereotypes();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertSame(stereotype, stereotypes.get(0));

        verify(apiDomain).getDerivedStereotypesRecursively(stereotype);
    }

    @Test
    public void getAvailableCRStereotypes_OneAvailable() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype derivativeStereotype = mock(Stereotype.class);
        stereotypes.add(derivativeStereotype);

        doReturn(stereotypes).when(apiDomain).getDerivedStereotypesRecursively(stereotype);

        List<Stereotype> result = configurationManagementService.getAvailableCRStereotypes();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertSame(stereotypes.get(0), result.get(0));

        verify(apiDomain).getDerivedStereotypesRecursively(stereotype);
    }

    @Test
    public void getAvailableCRStereotypes_None() {
        List<Stereotype> stereotypes = new ArrayList<>();

        doReturn(stereotypes).when(apiDomain).getDerivedStereotypesRecursively(stereotype);

        List<Stereotype> result = configurationManagementService.getAvailableCRStereotypes();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertSame(stereotype, stereotypes.get(0));

        verify(apiDomain).getDerivedStereotypesRecursively(stereotype);
    }

    @Test
    public void getAvailableStereotypesUsingBaseStereotype_nullParameter() {
        List<Stereotype> results = configurationManagementService.getAvailableStereotypesUsingBaseStereotype(null);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    public void getAvailableStereotypesUsingBaseStereotype_emptyListFromApi() {
        List<Stereotype> apiStereotypes = new ArrayList<>();

        doReturn(apiStereotypes).when(apiDomain).getDerivedStereotypesRecursively(stereotype);

        List<Stereotype> results = configurationManagementService.getAvailableStereotypesUsingBaseStereotype(stereotype);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(stereotype));

        verify(apiDomain).getDerivedStereotypesRecursively(stereotype);
    }

    @Test
    public void getAvailableStereotypesUsingBaseStereotype_derivedStereotypesObtained() {
        List<Stereotype> apiStereotypes = new ArrayList<>();
        Stereotype derived = mock(Stereotype.class);
        apiStereotypes.add(derived);

        doReturn(apiStereotypes).when(apiDomain).getDerivedStereotypesRecursively(stereotype);

        List<Stereotype> results = configurationManagementService.getAvailableStereotypesUsingBaseStereotype(stereotype);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertTrue(results.contains(derived));

        verify(apiDomain).getDerivedStereotypesRecursively(stereotype);
    }

    @Test
    public void getApplicableStereotypes_stereotypesFilteredOut() {
        Element element = mock(Element.class);
        List<Stereotype> ceStereotypes = new ArrayList<>();
        ceStereotypes.add(stereotype);

        doReturn(false).when(apiDomain).canAssignStereotype(element, stereotype);
        doReturn(new ArrayList<>()).when(configurationManagementService).trimDefaultStereotypes(ceStereotypes);

        List<Stereotype> results = configurationManagementService.getApplicableStereotypes(element);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(apiDomain).canAssignStereotype(element, stereotype);
        verify(configurationManagementService).getApplicableStereotypes(element);
    }

    @Test
    public void getApplicableStereotypes_stereotypesKept() {
        Element element = mock(Element.class);
        List<Stereotype> ceStereotypes = new ArrayList<>();
        ceStereotypes.add(stereotype);

        doReturn(true).when(apiDomain).canAssignStereotype(element, stereotype);
        doReturn(ceStereotypes).when(configurationManagementService).trimDefaultStereotypes(ceStereotypes);

        List<Stereotype> results = configurationManagementService.getApplicableStereotypes(element);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertSame(stereotype, results.get(0));

        verify(apiDomain).canAssignStereotype(element, stereotype);
        verify(configurationManagementService).trimDefaultStereotypes(ceStereotypes);
    }

    @Test
    public void isCMProfilePresent_ActuallyPresent() {
        IAttachedProject attachedProject = mock(IAttachedProject.class);
        Collection<IAttachedProject> attachedProjects = new ArrayList<>();
        attachedProjects.add(attachedProject);

        doReturn(attachedProjects).when(apiDomain).getAllAttachedProjectsForCurrentProject();
        doReturn(PluginConstant.CONFIGURATION_MANAGEMENT_PROJECTID).when(attachedProject).getProjectID();

        assertTrue(configurationManagementService.isCMProfilePresent());

        verify(apiDomain).getAllAttachedProjectsForCurrentProject();
        verify(attachedProject).getProjectID();
    }

    @Test
    public void isCMProfilePresent_NotPresent() {
        IAttachedProject attachedProject = mock(IAttachedProject.class);
        Collection<IAttachedProject> attachedProjects = new ArrayList<>();
        attachedProjects.add(attachedProject);
        String projectId = "someProjectId";

        doReturn(attachedProjects).when(apiDomain).getAllAttachedProjectsForCurrentProject();
        doReturn(projectId).when(attachedProject).getProjectID();

        assertFalse(configurationManagementService.isCMProfilePresent());

        verify(apiDomain).getAllAttachedProjectsForCurrentProject();
        verify(attachedProject).getProjectID();
    }

    @Test
    public void isCMProfilePresent_noProjects() {
        doReturn(null).when(apiDomain).getAllAttachedProjectsForCurrentProject();

        assertFalse(configurationManagementService.isCMProfilePresent());

        verify(apiDomain).getAllAttachedProjectsForCurrentProject();
    }

    @Test
    public void isCMProfilePresent_NoAttachments() {
        Collection<IAttachedProject> attachedProjects = new ArrayList<>();

        doReturn(attachedProjects).when(apiDomain).getAllAttachedProjectsForCurrentProject();

        assertFalse(configurationManagementService.isCMProfilePresent());

        verify(apiDomain).getAllAttachedProjectsForCurrentProject();
    }

    @Test
    public void isElementInChangeManagementPackageRoot_StartsWithCondition() {
        NamedElement namedElement = mock(NamedElement.class);
        String suffix = PluginConstant.PACKAGE_DELIM + "someSuffix";

        doReturn(cmPath).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(cmPath + suffix).when(namedElement).getQualifiedName();

        assertTrue(configurationManagementService.isElementInChangeManagementPackageRoot(namedElement));

        verify(configurationManagementService).getChangeManagementPackagePath();
        verify(namedElement).getQualifiedName();
    }

    @Test
    public void isElementInChangeManagementPackageRoot_EqualsCondition() {
        NamedElement namedElement = mock(NamedElement.class);

        doReturn(cmPath).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(cmPath).when(namedElement).getQualifiedName();

        assertTrue(configurationManagementService.isElementInChangeManagementPackageRoot(namedElement));

        verify(configurationManagementService, times(2)).getChangeManagementPackagePath();
        verify(namedElement, times(2)).getQualifiedName();
    }

    @Test
    public void getChangeManagementPackage_NullProject() {
        doReturn(null).when(apiDomain).getCurrentProject();

        assertNull(configurationManagementService.getChangeManagementPackage(false));

        verify(apiDomain).getCurrentProject();
    }

    @Test
    public void getChangeManagementPackage_NullPrimaryModel() {
        doReturn(null).when(project).getPrimaryModel();

        assertNull(configurationManagementService.getChangeManagementPackage(false));

        verify(project).getPrimaryModel();
    }

    @Test
    public void getChangeManagementPackage_NoCmProfile() {
        doReturn(pkg).when(project).getPrimaryModel();
        doReturn(false).when(configurationManagementService).isCMProfilePresent();

        assertNull(configurationManagementService.getChangeManagementPackage(false));

        verify(project).getPrimaryModel();
        verify(configurationManagementService).isCMProfilePresent();
    }

    @Test
    public void getChangeManagementPackage_ChangeManagementPackagePathNotNull() {
        String path = "some path";

        doReturn(pkg).when(project).getPrimaryModel();
        doReturn(true).when(configurationManagementService).isCMProfilePresent();
        doReturn(path).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(null).when(apiDomain).findRelativePackage(pkg, path);

        assertNull(configurationManagementService.getChangeManagementPackage(false));

        verify(project).getPrimaryModel();
        verify(configurationManagementService).isCMProfilePresent();
        verify(configurationManagementService).getChangeManagementPackagePath();
        verify(apiDomain).findRelativePackage(pkg, path);
    }

    @Test
    public void getChangeManagementPackage_CannotFindExpectedPackage() {
        String path = "some path";

        doReturn(pkg).when(project).getPrimaryModel();
        doReturn(true).when(configurationManagementService).isCMProfilePresent();
        doReturn(null).doReturn(path).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(null).when(apiDomain).findRelativePackage(pkg, path);

        assertNull(configurationManagementService.getChangeManagementPackage(false));

        verify(project).getPrimaryModel();
        verify(configurationManagementService).isCMProfilePresent();
        verify(configurationManagementService, times(2)).getChangeManagementPackagePath();
        verify(apiDomain).findRelativePackage(pkg, path);
    }

    @Test
    public void getChangeManagementPackage_CannotFindExpectedPackageWithCreateMode() {
        String path = "some path";

        doReturn(pkg).when(project).getPrimaryModel();
        doReturn(true).when(configurationManagementService).isCMProfilePresent();
        doReturn(null).doReturn(path).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(null).when(apiDomain).findRelativePackage(pkg, path);
        doReturn(pkg).when(apiDomain).createPackageStructureGivenPath(project, pkg, path);

        Package result = configurationManagementService.getChangeManagementPackage(true);

        assertNotNull(result);
        assertSame(pkg, result);

        verify(project).getPrimaryModel();
        verify(configurationManagementService).isCMProfilePresent();
        verify(configurationManagementService, times(2)).getChangeManagementPackagePath();
        verify(apiDomain).findRelativePackage(pkg, path);
        verify(apiDomain).createPackageStructureGivenPath(project, pkg, path);
    }

    @Test
    public void getChangeManagementPackage_FoundPackageNoCreateMode() {
        String path = "some path";

        doReturn(pkg).when(project).getPrimaryModel();
        doReturn(true).when(configurationManagementService).isCMProfilePresent();
        doReturn(null).doReturn(path).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(pkg).when(apiDomain).findRelativePackage(pkg, path);

        Package result = configurationManagementService.getChangeManagementPackage(false);

        assertNotNull(result);
        assertSame(pkg, result);

        verify(project).getPrimaryModel();
        verify(configurationManagementService).isCMProfilePresent();
        verify(configurationManagementService, times(2)).getChangeManagementPackagePath();
        verify(apiDomain).findRelativePackage(pkg, path);
    }

    @Test
    public void getChangeManagementPackage_FoundPackageWithCreateMode() {
        String path = "some::path";

        doReturn(pkg).when(project).getPrimaryModel();
        doReturn(true).when(configurationManagementService).isCMProfilePresent();
        doReturn(null).doReturn(path).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(pkg).when(apiDomain).findRelativePackage(pkg, path);

        Package result = configurationManagementService.getChangeManagementPackage(true);

        assertNotNull(result);
        assertSame(pkg, result);

        verify(project).getPrimaryModel();
        verify(configurationManagementService).isCMProfilePresent();
        verify(configurationManagementService, times(2)).getChangeManagementPackagePath();
        verify(apiDomain).findRelativePackage(pkg, path);
    }

    @Test
    public void getChangeManagementPackagePath() {
        assertNotNull(configurationManagementService.getChangeManagementPackagePath());
    }

    @Test
    public void getRevisionHistoryPackagePath() {
        assertNotNull(configurationManagementService.getRevisionHistoryPackagePath());
    }

    @Test
    public void getRevisionHistoryPackage_noPackageReturned() {
        String name = "name";
        String path = "somePath::" + name;

        doReturn(path).when(configurationManagementService).getRevisionHistoryPackagePath();
        doReturn(null).when(configurationManagementService).getSpecificPackageFromChangeManagement(path,
            PluginConstant.REVISION_HISTORY_PACKAGE, false);

        assertNull(configurationManagementService.getRevisionHistoryPackage(false));

        verify(configurationManagementService).getRevisionHistoryPackagePath();
        verify(configurationManagementService).getSpecificPackageFromChangeManagement(path,
            PluginConstant.REVISION_HISTORY_PACKAGE, false);
    }

    @Test
    public void getRevisionHistoryPackage_packageReturned() {
        Package chgMgmt = mock(Package.class);
        String name = "name";
        String path = "somePath::" + name;

        doReturn(path).when(configurationManagementService).getRevisionHistoryPackagePath();
        doReturn(chgMgmt).when(configurationManagementService).getSpecificPackageFromChangeManagement(path,
            PluginConstant.REVISION_HISTORY_PACKAGE, false);

        assertNotNull(configurationManagementService.getRevisionHistoryPackage(false));

        verify(configurationManagementService).getRevisionHistoryPackagePath();
        verify(configurationManagementService).getSpecificPackageFromChangeManagement(path,
            PluginConstant.REVISION_HISTORY_PACKAGE, false);
    }

    @Test
    public void getChangeRecordsPackagePath() {
        assertNotNull(configurationManagementService.getChangeRecordsPackagePath());
    }

    @Test
    public void getChangeRecordsPackage_NoChangeManagementPackage() {
        String name = "name";
        String path = "somePath::" + name;

        doReturn(path).when(configurationManagementService).getChangeRecordsPackagePath();
        doReturn(null).when(configurationManagementService).getSpecificPackageFromChangeManagement(path,
            "Change Records", false);

        assertNull(configurationManagementService.getChangeRecordsPackage(false));

        verify(configurationManagementService).getChangeRecordsPackagePath();
        verify(configurationManagementService).getSpecificPackageFromChangeManagement(path,
            "Change Records", false);
    }

    @Test
    public void getChangeRecordsPackage_ProjectNull() {
        Package chgMgmt = mock(Package.class);
        String name = "name";
        String path = "somePath::" + name;

        doReturn(path).when(configurationManagementService).getChangeRecordsPackagePath();
        doReturn(chgMgmt).when(configurationManagementService).getSpecificPackageFromChangeManagement(path,
            "Change Records", false);

        assertNotNull(configurationManagementService.getChangeRecordsPackage(false));

        verify(configurationManagementService).getChangeRecordsPackagePath();
        verify(configurationManagementService).getSpecificPackageFromChangeManagement(path,
            "Change Records", false);
    }

    @Test
    public void getSpecificPackageFromChangeManagement_NoChangeManagementPackage() {
        String name = "name";
        String path = "somePath::" + name;

        doReturn(null).when(configurationManagementService).getChangeManagementPackage(false);

        assertNull(configurationManagementService.getSpecificPackageFromChangeManagement(path, name, false));

        verify(configurationManagementService).getChangeManagementPackage(false);
    }

    @Test
    public void getSpecificPackageFromChangeManagement_ProjectNull() {
        Package chgMgmt = mock(Package.class);
        String name = "name";
        String path = "somePath::" + name;

        doReturn(chgMgmt).when(configurationManagementService).getChangeManagementPackage(false);
        doReturn(null).when(apiDomain).getCurrentProject();

        assertNull(configurationManagementService.getSpecificPackageFromChangeManagement(path, name, false));

        verify(configurationManagementService).getChangeManagementPackage(false);
        verify(apiDomain).getCurrentProject();
    }

    @Test
    public void getSpecificPackageFromChangeManagement_PackageFound() {
        Package chgMgmt = mock(Package.class);
        Package root = mock(Package.class);
        String name = "name";
        String path = "somePath::" + name;

        doReturn(chgMgmt).when(configurationManagementService).getChangeManagementPackage(false);
        doReturn(root).when(project).getPrimaryModel();
        doReturn(pkg).when(apiDomain).createPackageIfNotFound(project, path, root, chgMgmt, name);

        Package result = configurationManagementService.getSpecificPackageFromChangeManagement(path, name, false);

        assertNotNull(result);
        assertSame(pkg, result);

        verify(configurationManagementService).getChangeManagementPackage(false);
        verify(project).getPrimaryModel();
        verify(apiDomain).createPackageIfNotFound(project, path, root, chgMgmt, name);
    }

    @Test
    public void isChangeRecordSelected_True() {
        assertTrue(configurationManagementService.isChangeRecordSelected());
    }

    @Test
    public void isChangeRecordSelected_False() {
        configurationManagementService.setChangeRecordName(null);

        assertFalse(configurationManagementService.isChangeRecordSelected());
    }

    @Test
    public void getChangeRecords_NoChangeRecordsPackage() {
        doReturn(null).when(configurationManagementService).getChangeRecordsPackage(false);

        List<ChangeRecord> results = configurationManagementService.getChangeRecords();

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(configurationManagementService).getChangeRecordsPackage(false);
    }

    @Test
    public void getChangeRecords_NoPackagedElements() {
        Collection<PackageableElement> packedElements = new ArrayList<>();

        doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(false);
        doReturn(packedElements).when(pkg).getPackagedElement();

        List<ChangeRecord> results = configurationManagementService.getChangeRecords();

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(configurationManagementService).getChangeRecordsPackage(false);
        verify(pkg).getPackagedElement();
    }

    @Test
    public void getChangeRecords_HasContainedElements() {
        Collection<PackageableElement> packedElements = new ArrayList<>();
        PackageableElement element = mock(Class.class);
        PackageableElement element2 = mock(LiteralString.class);
        packedElements.add(element);
        packedElements.add(element2);
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(pkg).when(configurationManagementService).getChangeRecordsPackage(false);
        doReturn(packedElements).when(pkg).getPackagedElement();
        doReturn(changeRecord).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, (Class) element);

        List<ChangeRecord> results = configurationManagementService.getChangeRecords();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertSame(changeRecord, results.get(0));

        verify(configurationManagementService).getChangeRecordsPackage(false);
        verify(pkg).getPackagedElement();
        verify(lifecycleObjectFactory).getChangeRecord(configurationManagementService, (Class) element);
    }

    @Test
    public void initializeChangeRecord_statusNotGivenAndNotFound() {
        try {
            doReturn(null).when(lifecycleObjectDomain).getInitialStatus(configurationManagementService, stereotype);

            assertNull(configurationManagementService.initializeChangeRecord(mock(Class.class), "name", stereotype, pkg));

            verify(lifecycleObjectDomain).getInitialStatus(configurationManagementService, stereotype);
        } catch(Exception e) {
            fail("No exceptions expected.");
        }
    }

    @Test
    public void initializeChangeRecord_statusNotGivenButFound() {
        LifecycleStatus status = mock(LifecycleStatus.class);
        Class element = mock(Class.class);
        String name = "name";
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        try {
            doReturn(status).when(lifecycleObjectDomain).getInitialStatus(configurationManagementService, stereotype);
            when(lifecycleObjectFactory.getChangeRecord(configurationManagementService, element)).thenReturn(changeRecord);

            assertSame(changeRecord, configurationManagementService.initializeChangeRecord(element, name, stereotype, pkg));

            verify(lifecycleObjectDomain).getInitialStatus(configurationManagementService, stereotype);
            verify(lifecycleObjectFactory).getChangeRecord(configurationManagementService, element);
        } catch(Exception e) {
            fail("No exceptions expected.");
        }
    }

    @Test
    public void initializeChangeRecord_statusGiven() {
        LifecycleStatus status = mock(LifecycleStatus.class);
        Class element = mock(Class.class);
        String name = "name";
        State state = mock(State.class);
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        try {
            doReturn(state).when(status).getState();
            when(lifecycleObjectFactory.getChangeRecord(configurationManagementService, element)).thenReturn(changeRecord);

            assertSame(changeRecord, configurationManagementService.initializeChangeRecord(element, name, stereotype, pkg, status));

            verify(status).getState();
            verify(lifecycleObjectFactory).getChangeRecord(configurationManagementService, element);
        } catch(Exception e) {
            fail("No exceptions expected.");
        }
    }

    @Test
    public void setChangeRecordParametersForDataSource() {
        Element element = mock(Element.class);
        String source = "source";
        String sourceId = "sourceId";
        String description = "description";

        doNothing().when(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.SOURCE, source);
        doNothing().when(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.SOURCE_ID, sourceId);
        doNothing().when(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.DESCRIPTION, description);

        configurationManagementService.setChangeRecordParametersForDataSource(element, source, sourceId, description);

        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.SOURCE, source);
        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.SOURCE_ID, sourceId);
        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.DESCRIPTION, description);
    }

    @Test
    public void getSelectedChangeRecord_NullChangeRecordName() {
        configurationManagementService.setChangeRecordName(null);

        assertNull(configurationManagementService.getSelectedChangeRecord());
    }

    @Test
    public void getSelectedChangeRecord_NullProject() {
        String changeRecordName = "recordName";

        configurationManagementService.setChangeRecordName(changeRecordName);

        doReturn(null).when(apiDomain).getCurrentProject();

        assertNull(configurationManagementService.getSelectedChangeRecord());

        verify(apiDomain).getCurrentProject();
    }

    @Test
    public void getSelectedChangeRecord_RecordNotFound() {
        String changeRecordName = "recordName";

        configurationManagementService.setChangeRecordName(changeRecordName);

        doReturn(null).when(configurationManagementService).findRelatedChangeRecord();

        assertNull(configurationManagementService.getSelectedChangeRecord());

        verify(configurationManagementService).findRelatedChangeRecord();
    }

    @Test
    public void getSelectedChangeRecord_RecordFound() {
        String changeRecordName = "recordName";
        Class changeRecordElement = mock(Class.class);
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        configurationManagementService.setChangeRecordName(changeRecordName);
        when(apiDomain.getCurrentProject()).thenReturn(project);
        doReturn(changeRecordElement).when(configurationManagementService).findRelatedChangeRecord();
        doReturn(changeRecord).when(configurationManagementService).getChangeRecord(changeRecordElement);

        ChangeRecord result = configurationManagementService.getSelectedChangeRecord();

        assertNotNull(result);
        assertSame(changeRecord, result);

        verify(apiDomain).getCurrentProject();
        verify(configurationManagementService).findRelatedChangeRecord();
        verify(configurationManagementService).getChangeRecord(changeRecordElement);
    }

    @Test
    public void getChangeRecord_WrongType() {
        Element element = mock(Element.class);

        assertNull(configurationManagementService.getChangeRecord(element));
    }

    @Test
    public void getChangeRecord_LacksCrStereotype() {
        Element element = mock(Class.class);

        doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
        doReturn(false).when(apiDomain).hasStereotypeOrDerived(element, stereotype);

        assertNull(configurationManagementService.getChangeRecord(element));

        verify(configurationManagementService).getBaseCRStereotype();
        verify(apiDomain).hasStereotypeOrDerived(element, stereotype);
    }

    @Test
    public void getChangeRecord_HasCrStereotype() {
        Element element = mock(Class.class);
        ChangeRecord changeRecord = mock(ChangeRecord.class);

        doReturn(stereotype).when(configurationManagementService).getBaseCRStereotype();
        doReturn(true).when(apiDomain).hasStereotypeOrDerived(element, stereotype);
        doReturn(changeRecord).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, (Class) element);

        ChangeRecord result = configurationManagementService.getChangeRecord(element);

        assertNotNull(result);
        assertSame(changeRecord, result);

        verify(configurationManagementService).getBaseCRStereotype();
        verify(apiDomain).hasStereotypeOrDerived(element, stereotype);
        verify(lifecycleObjectFactory).getChangeRecord(configurationManagementService, (Class) element);
    }

    @Test
    public void findRelatedChangeRecord() {
        String changeRecordName = "recordName";
        String name = lifecycleObjectDomain + PluginConstant.PACKAGE_DELIM + changeRecordName;
        Class classObject = mock(Class.class);

        when(configurationManagementService.getChangeRecordName()).thenReturn(changeRecordName);
        doReturn(classObject).when(apiDomain).findClassRelativeToCurrentPrimary(name);

        configurationManagementService.findRelatedChangeRecord();

        verify(configurationManagementService).getChangeRecordName();
        verify(apiDomain).findClassRelativeToCurrentPrimary("pathPrefix::customCmPath::Change Records::recordName");
    }

    @Test
    public void resetChangeRecordSelectionAfterStatusChange_nullLifecycleObject() {
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);

        configurationManagementService.resetChangeRecordSelectionAfterStatusChange(null, lifecycleStatus);

        verify(configurationManagementService, never()).setChangeRecordSelections();
    }

    @Test
    public void resetChangeRecordSelectionAfterStatusChange_lifecycleObjectWrongType() {
        LifecycleObject lifecycleObject = mock(ConfiguredElement.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);

        configurationManagementService.resetChangeRecordSelectionAfterStatusChange(lifecycleObject, lifecycleStatus);

        verify(configurationManagementService, never()).setChangeRecordSelections();
    }

    @Test
    public void resetChangeRecordSelectionAfterStatusChange_nullStatus() {
        LifecycleObject lifecycleObject = mock(ChangeRecord.class);

        configurationManagementService.resetChangeRecordSelectionAfterStatusChange(lifecycleObject, null);

        verify(configurationManagementService, never()).setChangeRecordSelections();
    }

    @Test
    public void resetChangeRecordSelectionAfterStatusChange_statusIsReleased() {
        LifecycleObject lifecycleObject = mock(ChangeRecord.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);

        when(lifecycleStatus.isReleased()).thenReturn(true);

        configurationManagementService.resetChangeRecordSelectionAfterStatusChange(lifecycleObject, lifecycleStatus);

        verify(lifecycleStatus).isReleased();
        verify(configurationManagementService, never()).setChangeRecordSelections();
    }

    @Test
    public void resetChangeRecordSelectionAfterStatusChange_noSelectedChangeRecord() {
        LifecycleObject lifecycleObject = mock(ChangeRecord.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);

        when(lifecycleStatus.isReleased()).thenReturn(false);
        doNothing().when(configurationManagementService).setChangeRecordSelections();
        doReturn(null).when(configurationManagementService).getSelectedChangeRecord();

        configurationManagementService.resetChangeRecordSelectionAfterStatusChange(lifecycleObject, lifecycleStatus);

        verify(configurationManagementService).setChangeRecordSelections();
        verify(lifecycleStatus).isReleased();
        verify(configurationManagementService).getSelectedChangeRecord();
        verify(configurationManagementService, never()).setCurrentChangeRecordSelection(anyString());
    }

    @Test
    public void resetChangeRecordSelectionAfterStatusChange_selectedChangeRecordHasNoName() {
        LifecycleObject lifecycleObject = mock(ChangeRecord.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        ChangeRecord currentChangeRecord = mock(ChangeRecord.class);

        when(lifecycleStatus.isReleased()).thenReturn(false);
        doNothing().when(configurationManagementService).setChangeRecordSelections();
        doReturn(currentChangeRecord).when(configurationManagementService).getSelectedChangeRecord();
        when(currentChangeRecord.getName()).thenReturn(PluginConstant.EMPTY_STRING);

        configurationManagementService.resetChangeRecordSelectionAfterStatusChange(lifecycleObject, lifecycleStatus);

        verify(configurationManagementService).setChangeRecordSelections();
        verify(lifecycleStatus).isReleased();
        verify(configurationManagementService).getSelectedChangeRecord();
        verify(currentChangeRecord).getName();
        verify(configurationManagementService, never()).setCurrentChangeRecordSelection(anyString());
    }

    @Test
    public void resetChangeRecordSelectionAfterStatusChange_selectedChangeRecordHasName() {
        LifecycleObject lifecycleObject = mock(ChangeRecord.class);
        LifecycleStatus lifecycleStatus = mock(LifecycleStatus.class);
        ChangeRecord currentChangeRecord = mock(ChangeRecord.class);
        String changeRecordName = "changeRecordName";

        when(lifecycleStatus.isReleased()).thenReturn(false);
        doNothing().when(configurationManagementService).setChangeRecordSelections();
        doReturn(currentChangeRecord).when(configurationManagementService).getSelectedChangeRecord();
        when(currentChangeRecord.getName()).thenReturn(changeRecordName);
        doNothing().when(configurationManagementService).setCurrentChangeRecordSelection(changeRecordName);

        configurationManagementService.resetChangeRecordSelectionAfterStatusChange(lifecycleObject, lifecycleStatus);

        verify(configurationManagementService).setChangeRecordSelections();
        verify(lifecycleStatus).isReleased();
        verify(configurationManagementService).getSelectedChangeRecord();
        verify(currentChangeRecord, times(2)).getName();
        verify(configurationManagementService).setCurrentChangeRecordSelection(changeRecordName);
    }

    @Test
    public void getAllRevisionHistoryRecords_revisionHistoryPackageIsNull() {
        doReturn(null).when(configurationManagementService).getRevisionHistoryPackage(false);

        assertTrue(configurationManagementService.getAllRevisionHistoryRecords().isEmpty());

        verify(configurationManagementService).getRevisionHistoryPackage(false);
        verify(configurationManagementService, never()).getLifecycleObjectFactory();
    }

    @Test
    public void getAllRevisionHistoryRecords_mixOfRecordsWithAndWithoutConfiguredElements() {
        Package revisionHistoryPackage = mock(Package.class);
        Class packagedElement = mock(Class.class);
        Class packagedButNotConfigured = mock(Class.class);
        Collection<PackageableElement> pkgContents = new ArrayList<>();
        pkgContents.add(packagedElement);
        pkgContents.add(pkg);

        List<RevisionHistoryRecord> revisionHistoryRecords = new ArrayList<>();
        RevisionHistoryRecord revisionHistoryRecordWithoutConfiguredElement = mock(RevisionHistoryRecord.class);
        RevisionHistoryRecord revisionHistoryRecordWithConfiguredElement = mock(RevisionHistoryRecord.class);
        revisionHistoryRecords.add(revisionHistoryRecordWithoutConfiguredElement);
        revisionHistoryRecords.add(revisionHistoryRecordWithConfiguredElement);

        when(configurationManagementService.getRevisionRecordList()).thenReturn(revisionHistoryRecords);
        doReturn(revisionHistoryPackage).when(configurationManagementService).getRevisionHistoryPackage(false);
        doReturn(pkgContents).when(revisionHistoryPackage).getPackagedElement();
        when(lifecycleObjectFactory.getRevisionHistoryRecord(configurationManagementService, packagedElement)).thenReturn(revisionHistoryRecordWithConfiguredElement);
        when(lifecycleObjectFactory.getRevisionHistoryRecord(configurationManagementService, packagedButNotConfigured)).thenReturn(revisionHistoryRecordWithoutConfiguredElement);
        when(revisionHistoryRecordWithConfiguredElement.getConfiguredElement()).thenReturn(configuredElement);
        when(revisionHistoryRecordWithoutConfiguredElement.getConfiguredElement()).thenReturn(null);

        List<RevisionHistoryRecord> results = configurationManagementService.getAllRevisionHistoryRecords();

        assertTrue(results.contains(revisionHistoryRecordWithoutConfiguredElement));
        assertTrue(results.contains(revisionHistoryRecordWithConfiguredElement));
    }

    @Test
    public void getAutomateReleaseSwitch_OFF() {
        doReturn(Boolean.FALSE).when(customSettings).get(PluginConstant.AUTOMATE_RELEASE);

        assertFalse(configurationManagementService.getAutomateReleaseSwitch());

        verify(customSettings, times(2)).get(PluginConstant.AUTOMATE_RELEASE);
    }

    @Test
    public void getAutomateReleaseSwitch_ON() {
        doReturn(Boolean.TRUE).when(customSettings).get(PluginConstant.AUTOMATE_RELEASE);

        assertTrue(configurationManagementService.getAutomateReleaseSwitch());

        verify(customSettings, times(2)).get(PluginConstant.AUTOMATE_RELEASE);
    }

    @Test
    public void getAutomateReleaseSwitch_Null() {
        doReturn(null).when(customSettings).get(PluginConstant.AUTOMATE_RELEASE);

        assertFalse(configurationManagementService.getAutomateReleaseSwitch());

        verify(customSettings).get(PluginConstant.AUTOMATE_RELEASE);
    }

    @Test
    public void getEnforceActiveCRSwitch_OFF() {
        doReturn(Boolean.FALSE).when(customSettings).get(PluginConstant.ENFORCE_ACTIVE_CR);

        assertFalse(configurationManagementService.getEnforceActiveCRSwitch());

        verify(customSettings, times(2)).get(PluginConstant.ENFORCE_ACTIVE_CR);
    }

    @Test
    public void getEnforceActiveCRSwitch_ON() {
        doReturn(Boolean.TRUE).when(customSettings).get(PluginConstant.ENFORCE_ACTIVE_CR);

        assertTrue(configurationManagementService.getEnforceActiveCRSwitch());

        verify(customSettings, times(2)).get(PluginConstant.ENFORCE_ACTIVE_CR);
    }

    @Test
    public void getEnforceActiveCRSwitch_Null() {
        doReturn(null).when(customSettings).get(PluginConstant.ENFORCE_ACTIVE_CR);

        assertFalse(configurationManagementService.getEnforceActiveCRSwitch());

        verify(customSettings).get(PluginConstant.ENFORCE_ACTIVE_CR);
    }

    @Test
    public void getDiagramAdornmentSwitch_OFF() {
        doReturn(Boolean.FALSE).when(customSettings).get(PluginConstant.CM_DIAGRAM_ADORNMENT);

        assertFalse(configurationManagementService.getDiagramAdornmentSwitch());

        verify(customSettings, times(2)).get(PluginConstant.CM_DIAGRAM_ADORNMENT);
    }

    @Test
    public void getDiagramAdornmentSwitch_ON() {
        doReturn(Boolean.TRUE).when(customSettings).get(PluginConstant.CM_DIAGRAM_ADORNMENT);

        assertTrue(configurationManagementService.getDiagramAdornmentSwitch());

        verify(customSettings, times(2)).get(PluginConstant.CM_DIAGRAM_ADORNMENT);
    }

    @Test
    public void getDiagramAdornmentSwitch_Null() {
        doReturn(null).when(customSettings).get(PluginConstant.CM_DIAGRAM_ADORNMENT);

        assertFalse(configurationManagementService.getDiagramAdornmentSwitch());

        verify(customSettings).get(PluginConstant.CM_DIAGRAM_ADORNMENT);
    }

    @Test
    public void getAdminModePolicy_SettingFound() {
        Class policyClass = mock(Class.class);
        Policy policy = mock(Policy.class);
        LifecycleObjectFactory lifecycleObjectFactory = mock(LifecycleObjectFactory.class);

        doReturn(policyClass).when(customSettings).get(PluginConstant.ADMIN_MODE);
        doReturn(lifecycleObjectFactory).when(configurationManagementService).getLifecycleObjectFactory();
        when(lifecycleObjectFactory.getPolicy(configurationManagementService, policyClass)).thenReturn(policy);

        assertSame(policy, configurationManagementService.getAdminModePolicy());

        verify(customSettings).get(PluginConstant.ADMIN_MODE);
        verify(configurationManagementService).getLifecycleObjectFactory();
        verify(lifecycleObjectFactory).getPolicy(configurationManagementService, policyClass);
        verify(configurationManagementService, never()).findTripleAPolicy();
    }

    @Test
    public void getAdminModePolicy_NullSettingFoundPolicy() {
        Class policyClass = mock(Class.class);
        Policy policy = mock(Policy.class);
        LifecycleObjectFactory lifecycleObjectFactory = mock(LifecycleObjectFactory.class);

        doReturn(null).when(customSettings).get(PluginConstant.ADMIN_MODE);
        doReturn(policyClass).when(configurationManagementService).findTripleAPolicy();
        doReturn(lifecycleObjectFactory).when(configurationManagementService).getLifecycleObjectFactory();
        when(lifecycleObjectFactory.getPolicy(configurationManagementService, policyClass)).thenReturn(policy);

        assertSame(policy, configurationManagementService.getAdminModePolicy());

        verify(customSettings).get(PluginConstant.ADMIN_MODE);
        verify(configurationManagementService).findTripleAPolicy();
        verify(configurationManagementService).getLifecycleObjectFactory();
        verify(lifecycleObjectFactory).getPolicy(configurationManagementService, policyClass);
    }

    @Test
    public void getAdminModePolicy_NullSettingNullPolicy() {
        LifecycleObjectFactory lifecycleObjectFactory = mock(LifecycleObjectFactory.class);

        doReturn(null).when(customSettings).get(PluginConstant.ADMIN_MODE);
        doReturn(null).when(configurationManagementService).findTripleAPolicy();
        doReturn(lifecycleObjectFactory).when(configurationManagementService).getLifecycleObjectFactory();
        when(lifecycleObjectFactory.getPolicy(configurationManagementService, null)).thenReturn(null);

        assertNull(configurationManagementService.getAdminModePolicy());
    }

    @Test
    public void findTripleAPolicy() {
        doReturn(project).when(apiDomain).getCurrentProject();
        Class policyClass = mock(Class.class);

        doReturn(policyClass).when(apiDomain).findInProject(project, Policies.MBSE_AAA_POLICY_PATH);

        assertNotNull(configurationManagementService.findTripleAPolicy());

        verify(apiDomain).findInProject(project, Policies.MBSE_AAA_POLICY_PATH);
    }

    @Test
    public void disableAdminMode() {
        configurationManagementService.setAdminMode(true);
        assertTrue(configurationManagementService.getAdminMode());
        configurationManagementService.disableAdminMode();

        assertFalse(configurationManagementService.getAdminMode());
    }

    @Test
    public void enableAdminMode_NullPolicy() {
        doReturn(null).when(configurationManagementService).getAdminModePolicy();

        assertFalse(configurationManagementService.enableAdminMode());
    }

    @Test
    public void enableAdminMode_userLacksPrivileges() {
        Policy policy = mock(Policy.class);
        List<String> roles = new ArrayList<>();

        doReturn(policy).when(configurationManagementService).getAdminModePolicy();
        doReturn(roles).when(policy).getRoles();
        doReturn(false).when(configurationManagementService).userHasPrivileges(roles);

        assertFalse(configurationManagementService.enableAdminMode());

        verify(configurationManagementService).getAdminModePolicy();
        verify(policy).getRoles();
        verify(configurationManagementService).userHasPrivileges(roles);
    }

    @Test
    public void enableAdminMode_userHasPrivileges() {
        Policy policy = mock(Policy.class);
        List<String> roles = new ArrayList<>();

        doReturn(policy).when(configurationManagementService).getAdminModePolicy();
        doReturn(roles).when(policy).getRoles();
        doReturn(true).when(configurationManagementService).userHasPrivileges(roles);
        doNothing().when(configurationManagementService).setAdminMode(true);

        assertTrue(configurationManagementService.enableAdminMode());

        verify(configurationManagementService).getAdminModePolicy();
        verify(policy).getRoles();
        verify(configurationManagementService).userHasPrivileges(roles);
        verify(configurationManagementService).setAdminMode(true);
    }

    @Test
    public void hasAnyRole_True() throws TWCIntegrationException {
        List<String> roles = new ArrayList<>();
        String role = "someRole";
        roles.add(role);
        TeamworkCloudService teamworkCloudService = mock(TeamworkCloudService.class);

        doReturn(teamworkCloudService).when(configurationManagementService).getTeamworkCloudService();
        doReturn(true).when(teamworkCloudService).hasRole(apiDomain, roles);

        assertTrue(configurationManagementService.hasAnyRole(roles));

        verify(teamworkCloudService).hasRole(apiDomain, roles);
        verify(configurationManagementService).getTeamworkCloudService();
    }

    @Test
    public void hasAnyRole_False() throws TWCIntegrationException {
        List<String> roles = new ArrayList<>();
        String role = "someRole";
        roles.add(role);
        TeamworkCloudService teamworkCloudService = mock(TeamworkCloudService.class);

        doReturn(teamworkCloudService).when(configurationManagementService).getTeamworkCloudService();
        doReturn(false).when(teamworkCloudService).hasRole(apiDomain, roles);

        assertFalse(configurationManagementService.hasAnyRole(roles));

        verify(teamworkCloudService).hasRole(apiDomain, roles);
        verify(configurationManagementService).getTeamworkCloudService();
    }

    @Test
    public void userHasPrivileges_nullParameter() {
        try {
            assertTrue(configurationManagementService.userHasPrivileges(null));

            verify(configurationManagementService, never()).hasAnyRole(null);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void userHasPrivileges_NoRoles() {
        List<String> roles = new ArrayList<>();
        try {
            assertTrue(configurationManagementService.userHasPrivileges(roles));

            verify(configurationManagementService, never()).hasAnyRole(roles);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void userHasPrivileges_HasARole() {
        List<String> roles = new ArrayList<>();
        String role = "role";
        roles.add(role);
        String error = "error";
        TWCIntegrationException twcIntegrationException = spy(new TWCIntegrationException(error));
        try {
            doReturn(true).when(configurationManagementService).hasAnyRole(roles);

            assertTrue(configurationManagementService.userHasPrivileges(roles));

            verify(configurationManagementService).hasAnyRole(roles);
            verify(uiDomain, never()).logError(logger, ExceptionConstants.INDETERMINATE_TWC_ROLES, twcIntegrationException);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void userHasPrivileges_twcError() {
        List<String> roles = new ArrayList<>();
        String role = "role";
        roles.add(role);
        String error = "error";
        TWCIntegrationException twcIntegrationException = spy(new TWCIntegrationException(error));
        try {
            doThrow(twcIntegrationException).when(configurationManagementService).hasAnyRole(roles);
            doNothing().when(uiDomain).logError(logger, ExceptionConstants.INDETERMINATE_TWC_ROLES, twcIntegrationException);

            assertFalse(configurationManagementService.userHasPrivileges(roles));

            verify(configurationManagementService).hasAnyRole(roles);
            verify(uiDomain).logError(logger, ExceptionConstants.INDETERMINATE_TWC_ROLES, twcIntegrationException);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void userHasPrivileges_noPermission() {
        List<String> roles = new ArrayList<>();
        String role = "role";
        String role2 = "role2";
        roles.add(role);
        roles.add(role2);
        String error = "error";
        TWCIntegrationException twcIntegrationException = spy(new TWCIntegrationException(error));
        String expected = String.format(ExceptionConstants.INSUFFICIENT_PRIVILEGES, String.join(PluginConstant.COMMA, roles));
        try {
            doReturn(false).when(configurationManagementService).hasAnyRole(roles);
            doNothing().when(uiDomain).logError(expected);

            assertFalse(configurationManagementService.userHasPrivileges(roles));

            verify(configurationManagementService).hasAnyRole(roles);
            verify(uiDomain).logError(expected);
            verify(uiDomain, never()).logError(logger, ExceptionConstants.INDETERMINATE_TWC_ROLES, twcIntegrationException);
        } catch(Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    public void getCCZOwner_NoParent() {
        Element element = mock(Element.class);

        doReturn(null).when(element).getOwner();

        assertNull(configurationManagementService.getCCZOwner(element));
    }

    @Test
    public void getCCZOwner_NotConfiguredThenNoOwner() {
        Element element = mock(Element.class);
        Element parent = mock(Element.class);

        doReturn(parent).when(element).getOwner();
        doReturn(false).when(configurationManagementService).isConfigured(parent);
        doReturn(null).when(parent).getOwner();

        assertNull(configurationManagementService.getCCZOwner(element));
    }

    @Test
    public void getCCZOwner_ConfiguredElementCreated() {
        Element element = mock(Element.class);
        Element parent = mock(Element.class);
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);

        doReturn(parent).when(element).getOwner();
        doReturn(true).when(configurationManagementService).isConfigured(parent);
        doReturn(configuredElement).when(lifecycleObjectFactory).getConfiguredElement(configurationManagementService, parent);

        ConfiguredElement result = configurationManagementService.getCCZOwner(element);

        assertNotNull(result);
        assertSame(configuredElement, result);

        verify(element).getOwner();
        verify(configurationManagementService).isConfigured(parent);
        verify(lifecycleObjectFactory).getConfiguredElement(configurationManagementService, parent);
    }

    @Test
    public void configureElement() {
        Element element = mock(Element.class);
        String id = "id";
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);

        doReturn(configuredElement).when(configuredElementDomain).configure(configurationManagementService, element, id , stereotype);

        ConfiguredElement result = configurationManagementService.configureElement(element, id, stereotype);

        assertNotNull(result);
        assertSame(configuredElement, result);

        verify(configuredElementDomain).configure(configurationManagementService, element, id , stereotype);
    }

    @Test
    public void getConfiguredElement_notConfigured() {
        Element element = mock(Element.class);

        doReturn(false).when(configurationManagementService).isConfigured(element);

        assertNull(configurationManagementService.getConfiguredElement(element));

        verify(configurationManagementService).isConfigured(element);
    }

    @Test
    public void getConfiguredElement_isConfigured() {
        Element element = mock(Element.class);
        ConfiguredElement configuredElement = mock(ConfiguredElement.class);

        doReturn(true).when(configurationManagementService).isConfigured(element);
        doReturn(configuredElement).when(lifecycleObjectFactory).getConfiguredElement(configurationManagementService, element);

        ConfiguredElement result = configurationManagementService.getConfiguredElement(element);

        assertNotNull(result);
        assertSame(configuredElement, result);

        verify(configurationManagementService).isConfigured(element);
        verify(lifecycleObjectFactory).getConfiguredElement(configurationManagementService, element);
    }

    @Test
    public void isConfigured() {
        Element element = mock(Element.class);
        ConfiguredElementDomain configuredElementDomain = mock(ConfiguredElementDomain.class);

        when(configurationManagementService.getConfiguredElementDomain()).thenReturn(configuredElementDomain);
        when(configuredElementDomain.isConfigured(element, configurationManagementService)).thenReturn(false);

        assertFalse(configurationManagementService.isConfigured(element));

        verify(configurationManagementService).getConfiguredElementDomain();
        verify(configuredElementDomain).isConfigured(element, configurationManagementService);
}

    @Test
    public void isInReadOnlyCCZ_NullConfiguredElement() {
        Element element = mock(Element.class);

        try {
            doReturn(null).when(configurationManagementService).getCCZOwner(element);

            assertFalse(configurationManagementService.isInReadOnlyCCZ(element));

            verify(configurationManagementService).isInReadOnlyCCZ(element);
        } catch(Exception e) {
            fail("No exceptions expected.");
        }
    }

    @Test
    public void isInReadOnlyCCZ_NotReadOnly() {
        Element element = mock(Element.class);
        ConfiguredElement configured = mock(ConfiguredElement.class);

        try {
            doReturn(configured).when(configurationManagementService).getCCZOwner(element);
            doReturn(false).when(configured).isReadOnly();

            assertFalse(configurationManagementService.isInReadOnlyCCZ(element));

            verify(configured).isReadOnly();
            verify(configurationManagementService).getCCZOwner(element);
        } catch(Exception e) {
            fail("No exceptions expected.");
        }
    }

    @Test
    public void isInReadOnlyCCZ_ActuallyReadOnly() {
        Element element = mock(Element.class);
        ConfiguredElement configured = mock(ConfiguredElement.class);

        try {
            doReturn(configured).when(configurationManagementService).getCCZOwner(element);
            doReturn(true).when(configured).isReadOnly();

            assertTrue(configurationManagementService.isInReadOnlyCCZ(element));

            verify(configurationManagementService).getCCZOwner(element);
            verify(configured).isReadOnly();
        } catch(Exception e) {
            fail("No exceptions expected.");
        }
    }

    @Test
    public void isInReleasedCCZ_NullConfiguredElement() {
        Element element = mock(Element.class);

        try {
            doReturn(null).when(configurationManagementService).getCCZOwner(element);

            assertFalse(configurationManagementService.isInReleasedCCZ(element));

            verify(configurationManagementService).getCCZOwner(element);
        } catch(Exception e) {
            fail("No exceptions expected.");
        }
    }

    @Test
    public void isInReleasedCCZ_NotReleased() {
        Element element = mock(Element.class);
        ConfiguredElement configured = mock(ConfiguredElement.class);

        try {
            doReturn(configured).when(configurationManagementService).getCCZOwner(element);
            doReturn(false).when(configured).isReleased();

            assertFalse(configurationManagementService.isInReleasedCCZ(element));

            verify(configurationManagementService).getCCZOwner(element);
            verify(configured).isReleased();
        } catch(Exception e) {
            fail("No exceptions expected.");
        }
    }

    @Test
    public void isInReleasedCCZ_ActuallyReleased() {
        Element element = mock(Element.class);
        ConfiguredElement configured = mock(ConfiguredElement.class);

        try {
            doReturn(configured).when(configurationManagementService).getCCZOwner(element);
            doReturn(true).when(configured).isReleased();

            assertTrue(configurationManagementService.isInReleasedCCZ(element));

            verify(configurationManagementService).getCCZOwner(element);
            verify(configured).isReleased();
        } catch(Exception e) {
            fail("No exceptions expected.");
        }
    }

    @Test
    public void getOwnedConfiguredElements_noOwnedElements() {
        Element parent = mock(Element.class);

        doReturn(new ArrayList<>()).when(parent).getOwnedElement();

        List<ConfiguredElement> results = configurationManagementService.getOwnedConfiguredElements(parent);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(parent).getOwnedElement();
    }

    @Test
    public void getOwnedConfiguredElements_noRecursion() {
        Element parent = mock(Element.class);
        Collection<Element> elements = new ArrayList<>();
        Element element = mock(Element.class);
        elements.add(element);
        ConfiguredElement c1 = mock(ConfiguredElement.class);

        doReturn(elements).when(parent).getOwnedElement();
        doReturn(c1).when(configurationManagementService).getConfiguredElement(element);

        List<ConfiguredElement> results = configurationManagementService.getOwnedConfiguredElements(parent);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertSame(c1, results.get(0));

        verify(parent).getOwnedElement();
        verify(configurationManagementService).getConfiguredElement(element);
    }

    @Test
    public void getOwnedConfiguredElements_recurseWithOwnedElements() {
        Element parent = mock(Element.class);
        Collection<Element> elements = new ArrayList<>();
        Element element = mock(Element.class);
        elements.add(element);
        Collection<Element> children = new ArrayList<>();
        Element child1 = mock(Element.class);
        Element child2 = mock(Element.class);
        children.add(child1);
        children.add(child2);
        ConfiguredElement configuredChild1 = mock(ConfiguredElement.class);
        ConfiguredElement configuredChild2 = mock(ConfiguredElement.class);

        doReturn(elements).when(parent).getOwnedElement();
        doReturn(children).when(element).getOwnedElement();
        doReturn(null).when(configurationManagementService).getConfiguredElement(element);
        doReturn(configuredChild1).when(configurationManagementService).getConfiguredElement(child1);
        doReturn(configuredChild2).when(configurationManagementService).getConfiguredElement(child2);

        List<ConfiguredElement> results = configurationManagementService.getOwnedConfiguredElements(parent);

        Collection<ConfiguredElement> expected = new ArrayList<>();
        expected.add(configuredChild1);
        expected.add(configuredChild2);

        assertNotNull(results);
        assertEquals(expected.size(), results.size());
        for (ConfiguredElement c : expected) {
            assertTrue(expected.contains(c));
        }

        verify(parent).getOwnedElement();
        verify(element).getOwnedElement();
        verify(configurationManagementService).getConfiguredElement(element);
        verify(configurationManagementService).getConfiguredElement(child1);
        verify(configurationManagementService).getConfiguredElement(child2);
    }

    @Test
    public void getOwnedConfiguredElements_recurseButNoOwnedElements() {
        Element parent = mock(Element.class);
        Collection<Element> elements = new ArrayList<>();
        Element element = mock(Element.class);
        elements.add(element);
        Collection<Element> children = new ArrayList<>();

        doReturn(elements).when(parent).getOwnedElement();
        doReturn(children).when(element).getOwnedElement();
        doReturn(null).when(configurationManagementService).getConfiguredElement(element);

        List<ConfiguredElement> results = configurationManagementService.getOwnedConfiguredElements(parent);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(parent).getOwnedElement();
        verify(element).getOwnedElement();
        verify(configurationManagementService).getConfiguredElement(element);
    }

    @Test
    public void getSelectionCandidatesForConfiguration_nullProject() {
        doReturn(null).when(apiDomain).getCurrentProject();

        assertTrue(configurationManagementService.getSelectionCandidatesForConfiguration().isEmpty());

        verify(apiDomain).getCurrentProject();
    }

    @Test
    public void getSelectionCandidatesForConfiguration_nullCandidates() {
        doReturn(null).when(apiDomain).getAllElementsInCurrentProject();

        assertTrue(configurationManagementService.getSelectionCandidatesForConfiguration().isEmpty());

        verify(apiDomain).getAllElementsInCurrentProject();
        verify(configuredElementDomain, never()).canBeConfigured(namedElement, configurationManagementService);
    }

    @Test
    public void getSelectionCandidatesForConfiguration_candidateNotConfigurable() {
        Collection<Element> candidates = new ArrayList<>();
        candidates.add(namedElement);

        doReturn(candidates).when(apiDomain).getAllElementsInCurrentProject();
        doReturn(false).when(configuredElementDomain).canBeConfigured(namedElement, configurationManagementService);

        assertTrue(configurationManagementService.getSelectionCandidatesForConfiguration().isEmpty());

        verify(apiDomain).getAllElementsInCurrentProject();
        verify(configuredElementDomain).canBeConfigured(namedElement, configurationManagementService);
    }

    @Test
    public void getSelectionCandidatesForConfiguration_goodCandidate() {
        Collection<Element> candidates = new ArrayList<>();
        candidates.add(namedElement);

        doReturn(candidates).when(apiDomain).getAllElementsInCurrentProject();
        doReturn(true).when(configuredElementDomain).canBeConfigured(namedElement, configurationManagementService);

        Collection<NamedElement> results = configurationManagementService.getSelectionCandidatesForConfiguration();

        assertFalse(results.isEmpty());
        assertTrue(results.contains(namedElement));

        verify(apiDomain).getAllElementsInCurrentProject();
        verify(configuredElementDomain).canBeConfigured(namedElement, configurationManagementService);
    }

    @Test
    public void getSelectionCandidatesForRevision_nullProject() {
        doReturn(null).when(apiDomain).getCurrentProject();

        assertTrue(configurationManagementService.getSelectionCandidatesForRevision().isEmpty());

        verify(apiDomain).getCurrentProject();
    }

    @Test
    public void getSelectionCandidatesForRevision_noCandidates() {
        doReturn(null).when(apiDomain).getAllElementsInCurrentProject();

        assertTrue(configurationManagementService.getSelectionCandidatesForRevision().isEmpty());

        verify(apiDomain).getAllElementsInCurrentProject();
    }

    @Test
    public void getSelectionCandidatesForRevision_candidateNotEditable() {
        Collection<Element> candidates = new ArrayList<>();
        candidates.add(namedElement);

        doReturn(candidates).when(apiDomain).getAllElementsInCurrentProject();
        doReturn(false).when(apiDomain).isElementInEditableState(namedElement);

        assertTrue(configurationManagementService.getSelectionCandidatesForRevision().isEmpty());

        verify(apiDomain).getAllElementsInCurrentProject();
        verify(apiDomain).isElementInEditableState(namedElement);
    }

    @Test
    public void getSelectionCandidatesForRevision_candidateNotConfigurable() {
        Collection<Element> candidates = new ArrayList<>();
        candidates.add(namedElement);

        doReturn(candidates).when(apiDomain).getAllElementsInCurrentProject();
        doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
        doReturn(null).when(configurationManagementService).getConfiguredElement(namedElement);

        assertTrue(configurationManagementService.getSelectionCandidatesForRevision().isEmpty());

        verify(apiDomain).getAllElementsInCurrentProject();
        verify(apiDomain).isElementInEditableState(namedElement);
        verify(configurationManagementService).getConfiguredElement(namedElement);
    }

    @Test
    public void getSelectionCandidatesForRevision_missingStatus() {
        Collection<Element> candidates = new ArrayList<>();
        candidates.add(namedElement);

        doReturn(candidates).when(apiDomain).getAllElementsInCurrentProject();
        doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(namedElement);
        doReturn(false).when(configuredElement).hasStatus();

        assertTrue(configurationManagementService.getSelectionCandidatesForRevision().isEmpty());

        verify(apiDomain).getAllElementsInCurrentProject();
        verify(apiDomain).isElementInEditableState(namedElement);
        verify(configurationManagementService).getConfiguredElement(namedElement);
        verify(configuredElement).hasStatus();
    }

    @Test
    public void getSelectionCandidatesForRevision_candidateNotReleased() {
        Collection<Element> candidates = new ArrayList<>();
        candidates.add(namedElement);

        doReturn(candidates).when(apiDomain).getAllElementsInCurrentProject();
        doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(namedElement);
        doReturn(true).when(configuredElement).hasStatus();
        doReturn(false).when(configuredElement).isReleased();

        assertTrue(configurationManagementService.getSelectionCandidatesForRevision().isEmpty());

        verify(apiDomain).getAllElementsInCurrentProject();
        verify(apiDomain).isElementInEditableState(namedElement);
        verify(configurationManagementService).getConfiguredElement(namedElement);
        verify(configuredElement).hasStatus();
        verify(configuredElement).isReleased();
    }

    @Test
    public void getSelectionCandidatesForRevision_candidateNotCommitted() {
        Collection<Element> candidates = new ArrayList<>();
        candidates.add(namedElement);

        doReturn(candidates).when(apiDomain).getAllElementsInCurrentProject();
        doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(namedElement);
        doReturn(true).when(configuredElement).hasStatus();
        doReturn(true).when(configuredElement).isReleased();
        doReturn(false).when(configuredElement).isCommitted();

        assertTrue(configurationManagementService.getSelectionCandidatesForRevision().isEmpty());

        verify(apiDomain).getAllElementsInCurrentProject();
        verify(apiDomain).isElementInEditableState(namedElement);
        verify(configurationManagementService).getConfiguredElement(namedElement);
        verify(configuredElement).hasStatus();
        verify(configuredElement).isReleased();
        verify(configuredElement).isCommitted();
    }

    @Test
    public void getSelectionCandidatesForRevision_goodCandidate() {
        Collection<Element> candidates = new ArrayList<>();
        candidates.add(namedElement);

        doReturn(candidates).when(apiDomain).getAllElementsInCurrentProject();
        doReturn(true).when(apiDomain).isElementInEditableState(namedElement);
        doReturn(configuredElement).when(configurationManagementService).getConfiguredElement(namedElement);
        doReturn(true).when(configuredElement).hasStatus();
        doReturn(true).when(configuredElement).isReleased();
        doReturn(true).when(configuredElement).isCommitted();

        Collection<NamedElement> results = configurationManagementService.getSelectionCandidatesForRevision();

        assertFalse(results.isEmpty());
        assertTrue(results.contains(namedElement));

        verify(apiDomain).getAllElementsInCurrentProject();
        verify(apiDomain).isElementInEditableState(namedElement);
        verify(configurationManagementService).getConfiguredElement(namedElement);
        verify(configuredElement).hasStatus();
        verify(configuredElement).isReleased();
        verify(configuredElement).isCommitted();
    }

    @Test
    public void getLifecycle() {
        Lifecycle lifecycle = mock(Lifecycle.class);

        doReturn(lifecycle).when(lifecycleObjectDomain).getLifecycle(configurationManagementService, stereotype);

        configurationManagementService.getLifecycle(stereotype);

        verify(lifecycleObjectDomain).getLifecycle(configurationManagementService, stereotype);
    }

    @Test
    public void getCustomChangeRecordStereotypes_emptyList() {
        List<Stereotype> stereotypes = new ArrayList<>();

        doReturn(stereotypes).when(configurationManagementService).getAvailableCRStereotypes();

        List<Stereotype> results = configurationManagementService.getCustomChangeRecordStereotypes(PluginConstant.CAMEO);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(configurationManagementService).getAvailableCRStereotypes();
    }

    @Test
    public void getCustomChangeRecordStereotypes_filteredOut() {
        List<Stereotype> stereotypes = new ArrayList<>();
        stereotypes.add(stereotype);
        List<Stereotype> trimmedList = new ArrayList<>();

        doReturn(stereotypes).when(configurationManagementService).getAvailableCRStereotypes();
        doReturn(null).when(apiDomain).getDefaultValue(stereotype, PluginConstant.MANAGED_BY);
        doReturn(trimmedList).when(configurationManagementService).trimDefaultStereotypes(trimmedList);

        List<Stereotype> results = configurationManagementService.getCustomChangeRecordStereotypes(PluginConstant.CAMEO);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(configurationManagementService).getAvailableCRStereotypes();
        verify(apiDomain).getDefaultValue(stereotype, PluginConstant.MANAGED_BY);
        verify(configurationManagementService).trimDefaultStereotypes(trimmedList);
    }

    @Test
    public void getCustomChangeRecordStereotypes_filteredOut2() {
        List<Stereotype> stereotypes = new ArrayList<>();
        stereotypes.add(stereotype);
        EnumerationLiteral enumerationLiteral = mock(EnumerationLiteral.class);
        List<Stereotype> trimmedList = new ArrayList<>();

        doReturn(stereotypes).when(configurationManagementService).getAvailableCRStereotypes();
        doReturn(enumerationLiteral).when(apiDomain).getDefaultValue(stereotype, PluginConstant.MANAGED_BY);
        doReturn("Test").when(enumerationLiteral).getName();
        doReturn(trimmedList).when(configurationManagementService).trimDefaultStereotypes(trimmedList);

        List<Stereotype> results = configurationManagementService.getCustomChangeRecordStereotypes(PluginConstant.CAMEO);

        assertNotNull(results);
        assertTrue(results.isEmpty());

        verify(configurationManagementService).getAvailableCRStereotypes();
        verify(apiDomain).getDefaultValue(stereotype, PluginConstant.MANAGED_BY);
        verify(enumerationLiteral).getName();
        verify(configurationManagementService).trimDefaultStereotypes(trimmedList);
    }

    @Test
    public void getCustomChangeRecordStereotypes_notFiltered() {
        List<Stereotype> stereotypes = new ArrayList<>();
        stereotypes.add(stereotype);
        EnumerationLiteral enumerationLiteral = mock(EnumerationLiteral.class);
        List<Stereotype> trimmedList = new ArrayList<>();
        trimmedList.add(stereotype);

        doReturn(stereotypes).when(configurationManagementService).getAvailableCRStereotypes();
        doReturn(enumerationLiteral).when(apiDomain).getDefaultValue(stereotype, PluginConstant.MANAGED_BY);
        doReturn(PluginConstant.CAMEO).when(enumerationLiteral).getName();
        doReturn(trimmedList).when(configurationManagementService).trimDefaultStereotypes(trimmedList);

        List<Stereotype> results = configurationManagementService.getCustomChangeRecordStereotypes(PluginConstant.CAMEO);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertSame(stereotype, results.get(0));

        verify(configurationManagementService).getAvailableCRStereotypes();
        verify(apiDomain).getDefaultValue(stereotype, PluginConstant.MANAGED_BY);
        verify(enumerationLiteral).getName();
        verify(configurationManagementService).trimDefaultStereotypes(trimmedList);
    }

    @Test
    public void trimDefaultStereotypes_singletonParameter() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        stereotypes.add(stereotype);

        List<Stereotype> results = configurationManagementService.trimDefaultStereotypes(stereotypes);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertSame(stereotype, results.get(0));
    }

    @Test
    public void trimDefaultStereotypes_filtersOutDefault() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        Stereotype stereotype3 = mock(Stereotype.class);
        stereotypes.add(stereotype);
        stereotypes.add(stereotype2);
        stereotypes.add(stereotype3);
        Profile profile = mock(Profile.class);
        Profile defaultProfile = mock(Profile.class);

        doReturn(profile).when(stereotype).getProfile();
        doReturn(defaultProfile).when(stereotype2).getProfile();
        doReturn(null).when(stereotype3).getProfile();
        doReturn("id").when(profile).getLocalID();
        doReturn(PluginConstant.CM_PROFILE_ID).when(defaultProfile).getLocalID();

        List<Stereotype> results = configurationManagementService.trimDefaultStereotypes(stereotypes);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertSame(stereotype, results.get(0));

        verify(stereotype, times(2)).getProfile();
        verify(profile).getLocalID();
        verify(defaultProfile).getLocalID();
    }

    @Test
    public void userChoosesDesiredStereotype_nullParameter() {
        assertNull(configurationManagementService.userChoosesDesiredStereotype(null, "", ""));
    }

    @Test
    public void userChoosesDesiredStereotype_emptyList() {
        assertNull(configurationManagementService.userChoosesDesiredStereotype(new ArrayList<>(), "", ""));
    }

    @Test
    public void userChoosesDesiredStereotype_singletonList() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        stereotypes.add(stereotype);

        Stereotype result = configurationManagementService.userChoosesDesiredStereotype(stereotypes,
                "Select change record type", "Change record type selection");

        assertNotNull(result);
        assertSame(stereotype, result);
    }

    @Test
    public void userChoosesDesiredStereotype_userCancelsSelection() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        stereotypes.add(stereotype);
        stereotypes.add(stereotype2);
        String name = "name";
        String name2 = "name2";
        Object[] options = new String[] {name, name2};

        doReturn(name).when(stereotype).getName();
        doReturn(name2).when(stereotype2).getName();
        doReturn(-1).when(uiDomain).promptForSelection("Select change record type",
                "Change record type selection", options);

        assertNull(configurationManagementService.userChoosesDesiredStereotype(stereotypes,
                "Select change record type", "Change record type selection"));

        verify(stereotype).getName();
        verify(stereotype2).getName();
        verify(uiDomain).promptForSelection("Select change record type",
            "Change record type selection", options);
    }

    @Test
    public void userChoosesDesiredStereotype_userPicksAStereotype() {
        List<Stereotype> stereotypes = new ArrayList<>();
        Stereotype stereotype = mock(Stereotype.class);
        Stereotype stereotype2 = mock(Stereotype.class);
        stereotypes.add(stereotype);
        stereotypes.add(stereotype2);
        String name = "name";
        String name2 = "name2";
        Object[] options = new String[] {name, name2};

        doReturn(name).when(stereotype).getName();
        doReturn(name2).when(stereotype2).getName();
        doReturn(1).when(uiDomain).promptForSelection("Select change record type",
                "Change record type selection", options);

        Stereotype result = configurationManagementService.userChoosesDesiredStereotype(stereotypes,
                "Select change record type", "Change record type selection");

        assertNotNull(result);
        assertSame(stereotype2, result);

        verify(stereotype).getName();
        verify(stereotype2).getName();
        verify(uiDomain).promptForSelection("Select change record type",
            "Change record type selection", options);
    }

    @Test
    public void updateCRStatus() {
        configurationManagementService.updateCRStatus();

        verify(configurationManagementPlugin).updateCRStatus();
    }

    @Test
    public void setChangeRecordSelections() {
        doReturn(selectChangeRecordAction).when(configurationManagementPlugin).getSelectChangeRecordAction();

        configurationManagementService.setChangeRecordSelections();

        verify(configurationManagementPlugin).getSelectChangeRecordAction();
    }

    @Test
    public void clearChangeRecordSelectionList() {
        doReturn(selectChangeRecordAction).when(configurationManagementPlugin).getSelectChangeRecordAction();

        configurationManagementService.clearChangeRecordSelectionList();

        verify(configurationManagementPlugin).getSelectChangeRecordAction();
    }

    @Test
    public void getLifecycleTransition() {
        Transition transition = mock(Transition.class);
        LifecycleTransition lifecycleTransition = mock(LifecycleTransition.class);

        doReturn(lifecycleObjectFactory).when(configurationManagementService).getLifecycleObjectFactory();
        doReturn(lifecycleTransition).when(lifecycleObjectFactory).getLifecycleTransition(configurationManagementService, transition);

        configurationManagementService.getLifecycleTransition(transition);

        verify(configurationManagementService).getLifecycleObjectFactory();
        verify(lifecycleObjectFactory).getLifecycleTransition(configurationManagementService, transition);
    }
}
