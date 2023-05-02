package org.openmbee.plugin.cfgmgmt.domain;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.integration.twc.TeamworkCloudConnectionInfo;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.esi.api.info.BranchInfo;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.esi.session.ConnectionInfo;
import com.nomagic.magicdraw.merge.*;
import com.nomagic.magicdraw.teamwork2.locks.ILockProjectService;
import com.nomagic.magicdraw.teamwork2.locks.LockInfo;
import com.nomagic.magicdraw.ui.DiagramSurfacePainter;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.DiagramSurface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestApiDomain {
    private ApiDomain apiDomain;
    private Project project;
    private Element element;
    private ConfigurationManagementService configurationManagementService;
    private NamedElement namedElement;
    private ConfiguredElement configuredElement;

    @Before
    public void setup() {
        apiDomain = Mockito.spy(new ApiDomain());
        project = mock(Project.class);
        element = mock(Element.class);
        namedElement = mock(NamedElement.class);
        configuredElement = mock(ConfiguredElement.class);
        configurationManagementService = mock(ConfigurationManagementService.class);

        when(configurationManagementService.getConfiguredElement(namedElement)).thenReturn(configuredElement);
    }

    @Test
    public void setCurrentProjectHardDirty() {
        doReturn(project).when(apiDomain).getCurrentProject();
        apiDomain.setCurrentProjectHardDirty();

        verify(apiDomain).setCurrentProjectHardDirty();
    }

    @Test
    public void getStereotypePropertyFirstValueGivenType() {
        Stereotype stereotype = mock(Stereotype.class);
        List<Object> values = new ArrayList<>();
        String value = "value";
        values.add(value);
        String property = "property";
        doReturn(values).when(apiDomain).getStereotypePropertyValue(element, stereotype, property);

        Object result = apiDomain.getStereotypePropertyFirstValueGivenType(element, stereotype, property, String.class);
        assertTrue(result instanceof String);
        assertEquals(value, result);
    }

    @Test
    public void getStereotypePropertyFirstValueGivenType_emptyStereotypePropertyValues() {
        Stereotype stereotype = mock(Stereotype.class);
        List<Object> values = new ArrayList<>();
        String property = "property";
        doReturn(values).when(apiDomain).getStereotypePropertyValue(element, stereotype, property);

        assertNull(apiDomain.getStereotypePropertyFirstValueGivenType(element, stereotype, property, String.class));
    }
    
    @Test
    public void getDefaultValue_hasProperty() {
        Stereotype stereo = mock(Stereotype.class);
        Property property = mock(Property.class);
        String propertyName = "propertyName";
        Collection<NamedElement> collection = new ArrayList<>();
        collection.add(property);
        collection.add(namedElement);
        doReturn(propertyName).when(property).getName();
        doReturn(collection).when(stereo).getOwnedMember();

        assertNull(apiDomain.getDefaultValue(stereo, propertyName));
        verify(apiDomain).getValueSpecificationValueFromProperty(property);
    }

    @Test
    public void getDefaultValue_hasInvalidProperty() {
        Stereotype stereo = mock(Stereotype.class);
        Property property = mock(Property.class);
        String propertyName = "propertyName";
        String propertyName2 = "propertyName2";
        Collection<NamedElement> collection = new ArrayList<>();
        collection.add(property);
        collection.add(namedElement);
        doReturn(propertyName2).when(property).getName();
        doReturn(collection).when(stereo).getOwnedMember();

        assertNull(apiDomain.getDefaultValue(stereo, propertyName));
        verify(apiDomain, never()).getValueSpecificationValueFromProperty(property);
    }

    @Test
    public void getDefaultValue_stereotypeHasInheritance() {
        Stereotype stereo = mock(Stereotype.class);
        Property property = mock(Property.class);
        String propertyName = "propertyName";
        String propertyName2 = "propertyName2";
        Collection<NamedElement> collection1 = new ArrayList<>();
        Collection<NamedElement> collection2 = new ArrayList<>();
        collection1.add(namedElement);
        collection2.add(property);
        doReturn(propertyName).when(property).getName();
        doReturn(collection1).when(stereo).getOwnedMember();
        doReturn(propertyName2).when(property).getName();
        doReturn(collection2).when(stereo).getInheritedMember();

        assertNull(apiDomain.getDefaultValue(stereo, propertyName));
        verify(apiDomain, never()).getValueSpecificationValueFromProperty(property);
    }

    @Test
    public void getDefaultValue_stereotypeHasInheritance2() {
        Stereotype stereo = mock(Stereotype.class);
        Property property = mock(Property.class);
        String propertyName = "propertyName";
        Collection<NamedElement> collection1 = new ArrayList<>();
        Collection<NamedElement> collection2 = new ArrayList<>();
        collection1.add(namedElement);
        collection2.add(property);
        doReturn(propertyName).when(property).getName();
        doReturn(collection1).when(stereo).getOwnedMember();
        doReturn("propertyName").when(property).getName();
        doReturn(collection2).when(stereo).getInheritedMember();

        assertNull(apiDomain.getDefaultValue(stereo, propertyName));
        verify(apiDomain).getValueSpecificationValueFromProperty(property);
    }

    @Test
    public void getDefaultValue_stereotypeHasInheritance3() {
        Stereotype stereo = mock(Stereotype.class);
        Property property = mock(Property.class);
        String propertyName = "propertyName";
        Collection<NamedElement> collection1 = new ArrayList<>();
        Collection<NamedElement> collection2 = new ArrayList<>();
        collection1.add(namedElement);
        collection2.add(namedElement);
        doReturn(propertyName).when(property).getName();
        doReturn(collection1).when(stereo).getOwnedMember();
        doReturn(propertyName).when(property).getName();
        doReturn(collection2).when(stereo).getInheritedMember();

        assertNull(apiDomain.getDefaultValue(stereo, propertyName));
        verify(apiDomain, never()).getValueSpecificationValueFromProperty(property);
    }

    @Test
    public void getDefaultValue_NotEmpty() {
        Stereotype stereo = mock(Stereotype.class);
        Object value = mock(Object.class);
        Property prop = mock(Property.class);
        String propertyName = "property name";
        Collection<Property> collection = new ArrayList<>();
        collection.add(prop);
        doReturn(collection).when(stereo).getOwnedMember();
        doReturn(propertyName).when(prop).getName();
        doReturn(value).when(apiDomain).getValueSpecificationValueFromProperty(prop);

        assertNotNull(apiDomain.getDefaultValue(stereo, propertyName));
    }

    @Test
    public void isElementInEditableState_editable() {
        doReturn(true).when(element).isEditable();

        assertTrue(apiDomain.isElementInEditableState(element));
        verify(element).isEditable();
        verify(apiDomain, never()).isElementFreeOrNew(element);
        verify(apiDomain, never()).isElementLockedByMe(element);
    }

    @Test
    public void isElementInEditableState_freeOrNew() {
        doReturn(false).when(element).isEditable();
        doReturn(true).when(apiDomain).isElementFreeOrNew(element);

        assertTrue(apiDomain.isElementInEditableState(element));
        verify(element).isEditable();
        verify(apiDomain).isElementFreeOrNew(element);
        verify(apiDomain, never()).isElementLockedByMe(element);
    }

    @Test
    public void isElementInEditableState_isLockedByMe() {
        doReturn(false).when(element).isEditable();
        doReturn(false).when(apiDomain).isElementFreeOrNew(element);
        doReturn(true).when(apiDomain).isElementLockedByMe(element);

        assertTrue(apiDomain.isElementInEditableState(element));
        verify(element).isEditable();
        verify(apiDomain).isElementFreeOrNew(element);
        verify(apiDomain).isElementLockedByMe(element);
    }

    @Test
    public void isElementInEditableState_notEditableAtAll() {
        doReturn(false).when(element).isEditable();
        doReturn(false).when(apiDomain).isElementFreeOrNew(element);
        doReturn(false).when(apiDomain).isElementLockedByMe(element);

        assertFalse(apiDomain.isElementInEditableState(element));
        verify(element).isEditable();
        verify(apiDomain).isElementFreeOrNew(element);
        verify(apiDomain).isElementLockedByMe(element);
    }

    @Test
    public void isElementLockedByMe_noService() {
        doReturn(null).when(apiDomain).getLockService(element);

        assertFalse(apiDomain.isElementLockedByMe(element));
    }

    @Test
    public void isElementLockedByMe_notLockedByMe() {
        ILockProjectService lockProjectService = mock(ILockProjectService.class);
        doReturn(lockProjectService).when(apiDomain).getLockService(element);
        doReturn(false).when(lockProjectService).isLockedByMe(element);

        assertFalse(apiDomain.isElementLockedByMe(element));
    }

    @Test
    public void isElementLockedByMe_lockedByMe() {
        ILockProjectService lockProjectService = mock(ILockProjectService.class);
        doReturn(lockProjectService).when(apiDomain).getLockService(element);
        doReturn(true).when(lockProjectService).isLockedByMe(element);

        assertTrue(apiDomain.isElementLockedByMe(element));
    }

    @Test
    public void getLockingUser_whenNull() {
        doReturn(null).when(apiDomain).getLockService(element);

        assertEquals(PluginConstant.NO_LOCK_SERVICE_OR_NOT_LOCKED, apiDomain.getLockingUser(element));
    }

    @Test
    public void getLockingUser_whenNotNull() {
        ILockProjectService lockService = mock(ILockProjectService.class);
        doReturn(lockService).when(apiDomain).getLockService(element);
        doReturn(true).when(lockService).isLocked(element);

        assertEquals(PluginConstant.NO_LOCK_INFORMATION, apiDomain.getLockingUser(element));
    }

    @Test
    public void getLockingUser_elementNotNullAndNotLocked() {
        ILockProjectService lockService = mock(ILockProjectService.class);
        doReturn(lockService).when(apiDomain).getLockService(element);
        doReturn(false).when(lockService).isLocked(element);

        assertEquals(PluginConstant.NO_LOCK_SERVICE_OR_NOT_LOCKED, apiDomain.getLockingUser(element));
    }

    @Test
    public void getLockingUser_elementIsLocked() {
        ILockProjectService lockService = mock(ILockProjectService.class);
        LockInfo lockInfo = mock(LockInfo.class);
        String user = "user";
        doReturn(lockService).when(apiDomain).getLockService(element);
        doReturn(true).when(lockService).isLocked(element);
        doReturn(lockInfo).when(lockService).getLockInfo(element);
        doReturn(user).when(lockInfo).getUser();

        assertNotNull(apiDomain.getLockingUser(element));
    }

    @Test
    public void isAnyLocked_whenNull() {
        ConfiguredElement c1 = mock(ConfiguredElement.class);
        ConfiguredElement c2 = mock(ConfiguredElement.class);
        List<ConfiguredElement> list = new ArrayList<>();
        list.add(c1);
        list.add(c2);

        assertTrue(apiDomain.isAnyLocked(list));
    }

    @Test
    public void isAnyLocked_unlocked() {
        Element e1 = mock(Element.class);
        Element e2 = mock(Element.class);
        ConfiguredElement c1 = mock(ConfiguredElement.class);
        ConfiguredElement c2 = mock(ConfiguredElement.class);
        ILockProjectService p1 = mock(ILockProjectService.class);
        List<ConfiguredElement> list = new ArrayList<>();
        list.add(c1);
        list.add(c2);
        doReturn(e1).when(c1).getElement();
        doReturn(e2).when(c2).getElement();
        doReturn(p1).when(apiDomain).getLockService(e1);
        doReturn(false).when(p1).isLocked(e1);
        doReturn(false).when(p1).isLocked(e2);

        assertFalse(apiDomain.isAnyLocked(list));
    }

    @Test
    public void isAnyLocked_nullLockService() {
        Element e1 = mock(Element.class);
        Element e2 = mock(Element.class);
        ConfiguredElement c1 = mock(ConfiguredElement.class);
        ConfiguredElement c2 = mock(ConfiguredElement.class);
        ILockProjectService p1 = mock(ILockProjectService.class);
        List<ConfiguredElement> list = new ArrayList<>();
        list.add(c1);
        list.add(c2);
        doReturn(e1).when(c1).getElement();
        doReturn(e2).when(c2).getElement();
        doReturn(p1).when(apiDomain).getLockService(e1);
        doReturn(true).when(p1).isLocked(e1);
        doReturn(true).when(p1).isLocked(e2);

        assertTrue(apiDomain.isAnyLocked(list));
    }

    @Test
    public void isAnyLocked_EmptyList() {
        List<ConfiguredElement> list = new ArrayList<>();
        assertFalse(apiDomain.isAnyLocked(list));
    }

    @Test
    public void isAnyLocked_nullList() {
        assertFalse(apiDomain.isAnyLocked(null));
        verify(apiDomain, never()).getLockService(element);
    }

    @Test
    public void unlock() {
        ILockProjectService lockService = mock(ILockProjectService.class);
        doReturn(lockService).when(apiDomain).getLockService(element);
        doReturn(true).when(lockService).isLockedByMe(element);

        apiDomain.unlock(element);
        verify(lockService).unlockElements(List.of(element), true, null, false);
    }

    @Test
    public void unlock_notLockedByElement() {
        ILockProjectService lockService = mock(ILockProjectService.class);
        doReturn(lockService).when(apiDomain).getLockService(element);
        doReturn(false).when(lockService).isLockedByMe(element);

        apiDomain.unlock(element);
        verify(lockService, never()).unlockElements(List.of(element), true, null, false);
    }

    @Test
    public void unlock_nullLockService() {
        ILockProjectService lockService = mock(ILockProjectService.class);
        doReturn(null).when(apiDomain).getLockService(element);

        apiDomain.unlock(element);
        verify(lockService, never()).unlockElements(List.of(element), true, null, false);
    }

    @Test
    public void lock() {
        ILockProjectService lockService = mock(ILockProjectService.class);
        ConfiguredElement c1 = mock(ConfiguredElement.class);
        ConfiguredElement c2 = mock(ConfiguredElement.class);
        List<ConfiguredElement> list = new ArrayList<>();
        list.add(c1);
        list.add(c2);
        List<Element> elements = new ArrayList<>();
        Element e1 = mock(Element.class);
        Element e2 = mock(Element.class);
        elements.add(e1);
        elements.add(e2);

        doReturn(lockService).when(apiDomain).getLockService();
        when(c1.getElement()).thenReturn(e1);
        when(c2.getElement()).thenReturn(e2);
        when(lockService.lockElements(elements, false, null)).thenReturn(true);

        apiDomain.lock(list);
        verify(lockService).lockElements(elements, false, null);
    }

    @Test
    public void getCurrentBranchLatestRevision_notEsiUri() {
        IProject iProject = mock(IProject.class);

        doReturn(false).when(apiDomain).isEsiUri(iProject);

        assertEquals(0, apiDomain.getCurrentBranchLatestRevision(iProject));
    }

    @Test
    public void getCurrentBranchLatestRevision_nullBranchInfo() {
        IProject iProject = mock(IProject.class);

        doReturn(true).when(apiDomain).isEsiUri(iProject);
        doReturn(null).when(apiDomain).getBranch(iProject);

        assertEquals(0, apiDomain.getCurrentBranchLatestRevision(iProject));
    }

    @Test
    public void getCurrentBranchLatestRevision_validBranchInfo() {
        IProject iProject = mock(IProject.class);
        BranchInfo branchInfo = mock(BranchInfo.class);
        long expected = 1L;

        doReturn(true).when(apiDomain).isEsiUri(iProject);
        doReturn(branchInfo).when(apiDomain).getBranch(iProject);
        doReturn(expected).when(branchInfo).getLatestRevision();

        assertEquals(expected, apiDomain.getCurrentBranchLatestRevision(iProject));
    }

    @Test
    public void isEsiUri_nullParameter() {
        assertFalse(apiDomain.isEsiUri(null));
    }

    @Test
    public void isEsiUri_nullLocation() {
        IProject iProject = mock(IProject.class);
        when(iProject.getLocationURI()).thenReturn(null);

        assertFalse(apiDomain.isEsiUri(iProject));
    }

    @Test
    public void isEsiUri_notUri() {
        IProject iProject = mock(IProject.class);
        URI locationUri = mock(URI.class);

        when(iProject.getLocationURI()).thenReturn(locationUri);
        doReturn(false).when(apiDomain).isEsiUriWrapped(locationUri);

        assertFalse(apiDomain.isEsiUri(iProject));
    }

    @Test
    public void isEsiUri_properUri() {
        IProject iProject = mock(IProject.class);
        URI locationUri = mock(URI.class);

        when(iProject.getLocationURI()).thenReturn(locationUri);
        doReturn(true).when(apiDomain).isEsiUriWrapped(locationUri);

        assertTrue(apiDomain.isEsiUri(iProject));
    }

    @Test
    public void getEsiUri_nullParameter() {
        assertNull(apiDomain.getEsiUri(null));
    }

    @Test
    public void getEsiUri_nullLocation() {
        IProject iProject = mock(IProject.class);
        when(iProject.getLocationURI()).thenReturn(null);

        assertNull(apiDomain.getEsiUri(iProject));
        verify(apiDomain, never()).isEsiUriWrapped(any());
    }

    @Test
    public void getEsiUri_notUri() {
        IProject iProject = mock(IProject.class);
        URI locationUri = mock(URI.class);

        when(iProject.getLocationURI()).thenReturn(locationUri);
        doReturn(false).when(apiDomain).isEsiUriWrapped(locationUri);

        assertNull(apiDomain.getEsiUri(iProject));
        verify(apiDomain).isEsiUriWrapped(locationUri);
    }

    @Test
    public void getEsiUri_properUri() {
        IProject iProject = mock(IProject.class);
        URI locationUri = mock(URI.class);
        String uri = "uri";

        when(iProject.getLocationURI()).thenReturn(locationUri);
        doReturn(true).when(apiDomain).isEsiUriWrapped(locationUri);
        when(locationUri.toString()).thenReturn(uri);

        assertEquals(uri, apiDomain.getEsiUri(iProject));
    }

    @Test
    public void getValueSpecificationValueFromProperty_null() {
        Property property = mock(Property.class);
        assertNull(apiDomain.getValueSpecificationValueFromProperty(property));
    }

    @Test
    public void getValueSpecificationValueFromProperty_notNull() {
        Property property = mock(Property.class);
        ValueSpecification valueSpecification = mock(ValueSpecification.class);
        doReturn(valueSpecification).when(property).getDefaultValue();
        apiDomain.getValueSpecificationValueFromProperty(property);

        verify(apiDomain).getValueSpecificationValueFromProperty(property);
    }

    @Test
    public void createRemoteProjectDescriptor_nullDescriptor() {
        assertNull(apiDomain.createRemoteProjectDescriptor("", null, 1));
    }

    @Test
    public void createRemoteProjectDescriptor_nullProjectName() {
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);

        assertNull(apiDomain.createRemoteProjectDescriptor(null, projectDescriptor, 1));
    }

    @Test
    public void createRemoteProjectDescriptor_invalidVersionInteger() {
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        String name = "name";

        assertNull(apiDomain.createRemoteProjectDescriptor(name, projectDescriptor, 0));
    }

    @Test
    public void createRemoteProjectDescriptor_descriptorHasNullUri() {
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        String name = "name";

        when(projectDescriptor.getURI()).thenReturn(null);

        assertNull(apiDomain.createRemoteProjectDescriptor(name, projectDescriptor, 1));
        verify(apiDomain, never()).getRemoteIdFromProjectDescriptorUri(any());
    }

    @Test
    public void createRemoteProjectDescriptor_remoteIdFromDescriptorIsNull() {
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        String name = "name";
        java.net.URI uri = mock(java.net.URI.class);

        when(projectDescriptor.getURI()).thenReturn(uri);
        doReturn(null).when(apiDomain).getRemoteIdFromProjectDescriptorUri(uri);

        assertNull(apiDomain.createRemoteProjectDescriptor(name, projectDescriptor, 1));
        verify(apiDomain, never()).createRemoteProjectDescriptor(anyString(), anyInt(), anyString());
    }

    @Test
    public void createRemoteProjectDescriptor_remoteIdFromDescriptorIsBlank() {
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        String name = "name";
        java.net.URI uri = mock(java.net.URI.class);

        when(projectDescriptor.getURI()).thenReturn(uri);
        doReturn(" ").when(apiDomain).getRemoteIdFromProjectDescriptorUri(uri);

        assertNull(apiDomain.createRemoteProjectDescriptor(name, projectDescriptor, 1));
        verify(apiDomain, never()).createRemoteProjectDescriptor(anyString(), anyInt(), anyString());
    }

    @Test
    public void createRemoteProjectDescriptor_remoteDescriptorCreated() {
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        String name = "name";
        java.net.URI uri = mock(java.net.URI.class);
        int version = 1;
        String remoteId = "remoteId";

        when(projectDescriptor.getURI()).thenReturn(uri);
        doReturn(remoteId).when(apiDomain).getRemoteIdFromProjectDescriptorUri(uri);
        doReturn(projectDescriptor).when(apiDomain).createRemoteProjectDescriptor(name, version, remoteId);

        assertEquals(projectDescriptor, apiDomain.createRemoteProjectDescriptor(name, projectDescriptor, version));
        verify(apiDomain).createRemoteProjectDescriptor(name, version, remoteId);
    }

    @Test
    public void getNamedElement_null() {
        assertNull(apiDomain.getNamedElement(element));
    }

    @Test
    public void getNamedElement_notNull() {
        assertNotNull(apiDomain.getNamedElement(namedElement));
    }

    @Test
    public void compareProjects_releaseProjectNull() {
        ProjectsManager projectsManager = mock(ProjectsManager.class);
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        UIDomain uiDomain = mock(UIDomain.class);

        doReturn(projectsManager).when(apiDomain).getProjectsManager();
        doNothing().when(projectsManager).loadProject(projectDescriptor, true);
        doReturn(null).when(projectsManager).findProject(projectDescriptor);

        apiDomain.compareProjects(projectDescriptor, null, uiDomain, false);
        verify(apiDomain, never()).compareProjectsWrapper(any(), any(), any(), anyBoolean());
    }

    @Test
    public void compareProjects_baseDescriptorNull() {
        ProjectsManager projectsManager = mock(ProjectsManager.class);
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        UIDomain uiDomain = mock(UIDomain.class);

        doReturn(projectsManager).when(apiDomain).getProjectsManager();
        doNothing().when(projectsManager).loadProject(projectDescriptor, true);
        doReturn(project).when(projectsManager).findProject(projectDescriptor);

        apiDomain.compareProjects(projectDescriptor, null, uiDomain, false);
        verify(apiDomain, never()).compareProjectsWrapper(any(), any(), any(), anyBoolean());
    }

    @Test
    public void compareProjects_projectDifferenceNull() {
        ProjectsManager projectsManager = mock(ProjectsManager.class);
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        ProjectDescriptor projectDescriptor2 = mock(ProjectDescriptor.class);
        UIDomain uiDomain = mock(UIDomain.class);
        ApiDomain.SimpleErrorHandler simpleErrorHandler = mock(ApiDomain.SimpleErrorHandler.class);

        doReturn(projectsManager).when(apiDomain).getProjectsManager();
        doNothing().when(projectsManager).loadProject(projectDescriptor, true);
        doReturn(project).when(projectsManager).findProject(projectDescriptor);
        doReturn(simpleErrorHandler).when(apiDomain).getErrorHandlerForMergeUtility(uiDomain);
        doReturn(null).when(apiDomain).compareProjectsWrapper(projectDescriptor2, project, simpleErrorHandler, false);

        apiDomain.compareProjects(projectDescriptor, projectDescriptor2, uiDomain, false);
        verify(apiDomain).compareProjectsWrapper(projectDescriptor2, project, simpleErrorHandler, false);
        verify(apiDomain, never()).mergeRestore(any());
        verify(apiDomain, never()).displayDifference(any());
    }

    @Test
    public void compareProjects_projectDifferenceChangesEmpty() {
        ProjectsManager projectsManager = mock(ProjectsManager.class);
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        ProjectDescriptor projectDescriptor2 = mock(ProjectDescriptor.class);
        UIDomain uiDomain = mock(UIDomain.class);
        ApiDomain.SimpleErrorHandler simpleErrorHandler = mock(ApiDomain.SimpleErrorHandler.class);
        ProjectDifference projectDifference = mock(ProjectDifference.class);

        doReturn(projectsManager).when(apiDomain).getProjectsManager();
        doNothing().when(projectsManager).loadProject(projectDescriptor, true);
        doReturn(project).when(projectsManager).findProject(projectDescriptor);
        doReturn(simpleErrorHandler).when(apiDomain).getErrorHandlerForMergeUtility(uiDomain);
        doReturn(projectDifference).when(apiDomain).compareProjectsWrapper(projectDescriptor2, project, simpleErrorHandler, false);
        doReturn(true).when(apiDomain).noChangesBetweenProjectsFound(projectDifference);
        doNothing().when(apiDomain).mergeRestore(projectDifference);

        apiDomain.compareProjects(projectDescriptor, projectDescriptor2, uiDomain, false);
        verify(apiDomain).mergeRestore(projectDifference);
        verify(apiDomain, never()).displayDifference(projectDifference);
    }

    @Test
    public void compareProjects_projectDifferenceChangesExist() {
        ProjectsManager projectsManager = mock(ProjectsManager.class);
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        ProjectDescriptor projectDescriptor2 = mock(ProjectDescriptor.class);
        UIDomain uiDomain = mock(UIDomain.class);
        ApiDomain.SimpleErrorHandler simpleErrorHandler = mock(ApiDomain.SimpleErrorHandler.class);
        ProjectDifference projectDifference = mock(ProjectDifference.class);

        doReturn(projectsManager).when(apiDomain).getProjectsManager();
        doNothing().when(projectsManager).loadProject(projectDescriptor, true);
        doReturn(project).when(projectsManager).findProject(projectDescriptor);
        doReturn(simpleErrorHandler).when(apiDomain).getErrorHandlerForMergeUtility(uiDomain);
        doReturn(projectDifference).when(apiDomain).compareProjectsWrapper(projectDescriptor2, project, simpleErrorHandler, false);
        doReturn(false).when(apiDomain).noChangesBetweenProjectsFound(projectDifference);
        doNothing().when(apiDomain).displayDifference(projectDifference);

        apiDomain.compareProjects(projectDescriptor, projectDescriptor2, uiDomain, false);
        verify(apiDomain, never()).mergeRestore(projectDifference);
        verify(apiDomain).displayDifference(projectDifference);
    }

    @Test
    public void compareProjects_projectDifferenceChangesExist_OptimizeForMemory() {
        ProjectsManager projectsManager = mock(ProjectsManager.class);
        ProjectDescriptor projectDescriptor = mock(ProjectDescriptor.class);
        ProjectDescriptor projectDescriptor2 = mock(ProjectDescriptor.class);
        UIDomain uiDomain = mock(UIDomain.class);
        ApiDomain.SimpleErrorHandler simpleErrorHandler = mock(ApiDomain.SimpleErrorHandler.class);
        ProjectDifference projectDifference = mock(ProjectDifference.class);

        doReturn(projectsManager).when(apiDomain).getProjectsManager();
        doNothing().when(projectsManager).loadProject(projectDescriptor, true);
        doReturn(project).when(projectsManager).findProject(projectDescriptor);
        doReturn(simpleErrorHandler).when(apiDomain).getErrorHandlerForMergeUtility(uiDomain);
        doReturn(projectDifference).when(apiDomain).compareProjectsWrapper(projectDescriptor2, project, simpleErrorHandler, true);
        doReturn(false).when(apiDomain).noChangesBetweenProjectsFound(projectDifference);
        doNothing().when(apiDomain).displayDifference(projectDifference);

        apiDomain.compareProjects(projectDescriptor, projectDescriptor2, uiDomain, true);
        verify(apiDomain, never()).mergeRestore(projectDifference);
        verify(apiDomain).displayDifference(projectDifference);
    }

    @Test
    public void createStructureGivenPath_FindsOuterPackageButCreatesInnerPackage() {
        String prefix = "some";
        String suffix = "path";
        String path = prefix + PluginConstant.PACKAGE_DELIM + suffix;
        Package pkg = mock(Package.class);
        Package innerPkg = mock(Package.class);
        ElementsFactory elementsFactory = mock(ElementsFactory.class);

        doReturn(pkg).when(apiDomain).findRelativePackage(pkg, prefix);
        doReturn(null).when(apiDomain).findRelativePackage(pkg, path);
        doReturn(elementsFactory).when(project).getElementsFactory();
        doReturn(innerPkg).when(elementsFactory).createPackageInstance();

        Package result = apiDomain.createPackageStructureGivenPath(project, pkg, path);

        assertNotNull(result);
        assertSame(innerPkg, result);
    }

    @Test
    public void createPackageIfNotFound_PackageFound() {
        Package pkg = mock(Package.class);
        Package context = mock(Package.class);
        Package owner = mock(Package.class);
        String name = "packageName";
        String path = "somePath::" + name;

        doReturn(pkg).when(apiDomain).findRelativePackage(context, path);

        Package result = apiDomain.createPackageIfNotFound(project, path, context, owner, name);

        assertNotNull(result);
        assertEquals(pkg, result);
    }

    @Test
    public void createPackageIfNotFound_PackageNotFound() {
        Package pkg = mock(Package.class);
        Package context = mock(Package.class);
        Package owner = mock(Package.class);
        String name = "packageName";
        String path = "somePath::" + name;
        ElementsFactory elementsFactory = mock(ElementsFactory.class);

        doReturn(null).when(apiDomain).findRelativePackage(context, path);
        doReturn(elementsFactory).when(project).getElementsFactory();
        doReturn(pkg).when(elementsFactory).createPackageInstance();
        doNothing().when(pkg).setName(name);
        doNothing().when(pkg).setOwner(owner);

        Package result = apiDomain.createPackageIfNotFound(project, path, context, owner, name);

        assertNotNull(result);
        assertEquals(pkg, result);
    }

    @Test
    public void addPainter_DiagramFound() {
        DiagramPresentationElement diagram = mock(DiagramPresentationElement.class);
        DiagramSurfacePainter painter = mock(DiagramSurfacePainter.class);
        DiagramSurface diagramSurface = mock(DiagramSurface.class);

        doReturn(diagramSurface).when(diagram).getDiagramSurface();

        apiDomain.addPainter(diagram, painter);

        verify(diagramSurface).addPainter(painter);
    }

    @Test
    public void addPainter_DiagramNotFound() {
        DiagramPresentationElement diagram = mock(DiagramPresentationElement.class);
        DiagramSurfacePainter painter = mock(DiagramSurfacePainter.class);
        DiagramSurface diagramSurface = mock(DiagramSurface.class);

        doReturn(null).when(diagram).getDiagramSurface();

        apiDomain.addPainter(diagram, painter);

        verify(diagramSurface, never()).addPainter(painter);
    }

    @Test
    public void getServerInfo_noneAvailable() {
        doReturn(null).when(apiDomain).getConnectionInfo();

        assertNull(apiDomain.getServerInfo());
    }

    @Test
    public void getServerInfo_availableAndSecure() {
        TeamworkCloudConnectionInfo teamworkCloudConnectionInfo = mock(TeamworkCloudConnectionInfo.class);
        ConnectionInfo connectionInfo = mock(ConnectionInfo.class);
        String hostPort = "hostPort";
        String username = "username";

        doReturn(connectionInfo).when(apiDomain).getConnectionInfo();
        when(connectionInfo.isUseSecureConnection()).thenReturn(true);
        when(connectionInfo.getHostPort()).thenReturn(hostPort);
        when(connectionInfo.getUserName()).thenReturn(username);
        doReturn(teamworkCloudConnectionInfo).when(apiDomain).createTeamworkCloudConnectionInfo(hostPort, username, PluginConstant.PROTOCOL_HTTPS);

        assertSame(teamworkCloudConnectionInfo, apiDomain.getServerInfo());
    }

    @Test
    public void getServerInfo_availableButInsecure() {
        TeamworkCloudConnectionInfo teamworkCloudConnectionInfo = mock(TeamworkCloudConnectionInfo.class);
        ConnectionInfo connectionInfo = mock(ConnectionInfo.class);
        String hostPort = "hostPort";
        String username = "username";

        doReturn(connectionInfo).when(apiDomain).getConnectionInfo();
        when(connectionInfo.isUseSecureConnection()).thenReturn(false);
        when(connectionInfo.getHostPort()).thenReturn(hostPort);
        when(connectionInfo.getUserName()).thenReturn(username);
        doReturn(teamworkCloudConnectionInfo).when(apiDomain).createTeamworkCloudConnectionInfo(hostPort, username, PluginConstant.PROTOCOL_HTTP);

        assertSame(teamworkCloudConnectionInfo, apiDomain.getServerInfo());
    }

    @Test
    public void findInProject_nullObject() {
        String stereotypeName = "stereotypeName";

        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn(null).when(apiDomain).findInProject(project, stereotypeName);

        assertNull(apiDomain.findInProject(stereotypeName));
    }

    @Test
    public void findInProject_wrongObjectType() {
        String stereotypeName = "stereotypeName";

        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn(element).when(apiDomain).findInProject(project, stereotypeName);

        assertNull(apiDomain.findInProject(stereotypeName));
    }

    @Test
    public void findInProject_stereotypeFound() {
        String stereotypeName = "stereotypeName";
        Stereotype stereotype = mock(Stereotype.class);

        doReturn(project).when(apiDomain).getCurrentProject();
        doReturn(stereotype).when(apiDomain).findInProject(project, stereotypeName);

        assertEquals(stereotype, apiDomain.findInProject(stereotypeName));
    }

    @Test
    public void getElementUsingId_nullParameter() {
        assertNull(apiDomain.getElementUsingId(null));
    }

    @Test
    public void getElementUsingId_nullReturned() {
        String id = "id";

        doReturn(project).when(apiDomain).getCurrentProject();
        when(project.getElementByID(id)).thenReturn(null);

        assertNull(apiDomain.getElementUsingId(id));
    }

    @Test
    public void getElementUsingId_nonElementReturned() {
        String id = "id";
        BaseElement baseElement = mock(BaseElement.class);

        doReturn(project).when(apiDomain).getCurrentProject();
        when(project.getElementByID(id)).thenReturn(baseElement);

        assertNull(apiDomain.getElementUsingId(id));
    }

    @Test
    public void getElementUsingId_elementReturned() {
        String id = "id";

        doReturn(project).when(apiDomain).getCurrentProject();
        when(project.getElementByID(id)).thenReturn(element);

        assertEquals(element, apiDomain.getElementUsingId(id));
    }

    @Test
    public void getPropertyUsingId_nullReturned() {
        String id = "id";

        doReturn(null).when(apiDomain).getElementUsingId(id);

        assertNull(apiDomain.getPropertyUsingId(id));
    }

    @Test
    public void getPropertyUsingId_wrongType() {
        String id = "id";

        doReturn(element).when(apiDomain).getElementUsingId(id);

        assertNull(apiDomain.getPropertyUsingId(id));
    }

    @Test
    public void getPropertyUsingId_correctType() {
        String id = "id";
        Property property = mock(Property.class);

        doReturn(property).when(apiDomain).getElementUsingId(id);

        assertEquals(property, apiDomain.getPropertyUsingId(id));
    }

}
