package org.openmbee.plugin.cfgmgmt.domain;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.integration.twc.TeamworkCloudConnectionInfo;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleObject;
import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.esi.api.config.ServerServices;
import com.nomagic.esi.api.info.BranchInfo;
import com.nomagic.esi.emf.impl.EsiObjectImpl;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.esi.EsiUtilsInternal;
import com.nomagic.magicdraw.esi.session.ConnectionInfo;
import com.nomagic.magicdraw.merge.MergeUtil;
import com.nomagic.magicdraw.merge.ProjectDifference;
import com.nomagic.magicdraw.teamwork2.esi.EsiServerActionsExecuter;
import com.nomagic.magicdraw.teamwork2.locks.ILockProjectService;
import com.nomagic.magicdraw.teamwork2.locks.LockInfo;
import com.nomagic.magicdraw.teamwork2.locks.LockService;
import com.nomagic.magicdraw.ui.DiagramSurfacePainter;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.Finder;
import com.nomagic.magicdraw.uml.permissions.ElementPermissions;
import com.nomagic.magicdraw.uml.permissions.ElementPermissionsManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.DiagramSurface;
import com.nomagic.magicdraw.utils.StateChangeHandler;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.utils.ErrorHandler;
import org.eclipse.emf.common.util.URI;

import java.net.ConnectException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ApiDomain {
    public String getLoggedOnUser() {
        return EsiUtils.getLoggedUserName();
    }

    //*** Projects and Branches ***

    public EsiUtils.EsiBranchInfo getCurrentBranch(IProject iProject) {
        return iProject != null ? EsiUtils.getCurrentBranch(iProject) : null;
    }

    public Project getCurrentProject() {
        return Application.getInstance().getProject();
    }

    public Project getProject(Element element) {
        return element != null ? Project.getProject(element) : null;
    }

    public IProject getIProject(Element element) {
        return element != null ? ProjectUtilities.getProject(element) : null;
    }

    public boolean isElementInCurrentProject(Element element) {
        // Careful when changing this. There are many  getProject methods in the API and most do not return the correct result
        return ProjectUtilities.getProjectFor(element).equals(getIProject(getCurrentProject().getPrimaryModel()));
    }

    public Collection<Element> getAllElementsInCurrentProject() {
        Project project = getCurrentProject();
        return project != null ? Finder.byScope().find(project) : null;
    }

    public long getCurrentBranchLatestRevision(IProject iProject) {
        long latestRev = 0;
        if (isEsiUri(iProject)) {
            BranchInfo branchInfo = getBranch(iProject);
            if (branchInfo != null) {
                latestRev = branchInfo.getLatestRevision();
            }
        }
        return latestRev;
    }

    public boolean isEsiUri(IProject iProject) {
        if (iProject != null) {
            URI locationUri = iProject.getLocationURI();
            return locationUri != null && isEsiUriWrapped(locationUri);
        }
        return false;
    }

    public String getEsiUri(IProject iProject) {
        if(iProject != null) {
            URI locationUri = iProject.getLocationURI();
            if(locationUri != null && isEsiUriWrapped(locationUri)) {
                return locationUri.toString();
            }
        }
        return null;
    }

    protected boolean isEsiUriWrapped(URI locationUri) {
        return ProjectUtilities.isESIUri(locationUri);
    }

    public String getEsiUriFromCurrentProject() {
        return getEsiUri(getIProject(getCurrentProject().getPrimaryModel()));
    }

    public BranchInfo getBranch(IProject iProject) {
        // TODO replace deprecated method
        return iProject != null ? EsiUtilsInternal.getCurrentBranch(iProject) : null;
    }

    public void setCurrentProjectHardDirty() {
        getCurrentProject().setDirty(true, StateChangeHandler.DirtyType.HARD_DIRTY);
    }

    public Collection<IAttachedProject> getAllAttachedProjectsForCurrentProject() {
        Project currentProject = getCurrentProject();
        return currentProject != null ? ProjectUtilities.getAllAttachedProjects(currentProject) : null;
    }

    public ProjectDescriptor createRemoteProjectDescriptor(String projectName, ProjectDescriptor projectDescriptor, int version) {
        if (projectDescriptor != null && projectName != null && version > 0) {
            java.net.URI uri = projectDescriptor.getURI();
            if (uri != null) {
                final String remoteID = getRemoteIdFromProjectDescriptorUri(uri);
                if (remoteID != null && !remoteID.isBlank()) {
                    return createRemoteProjectDescriptor(projectName, version, remoteID);
                }
            }
        }
        return null;
    }

    protected String getRemoteIdFromProjectDescriptorUri(java.net.URI uri) {
        return ProjectDescriptorsFactory.getRemoteID(uri);
    }

    protected ProjectDescriptor createRemoteProjectDescriptor(String projectName, int version, String remoteID) {
        return ProjectDescriptorsFactory.createRemoteProjectDescriptor(remoteID, projectName, version);
    }

    protected ProjectsManager getProjectsManager() {
        return Application.getInstance().getProjectsManager();
    }

    //*** Utils ***

    public <T> NamedElement getNamedElement(T element) {
        return element instanceof NamedElement ? (NamedElement) element : null;
    }

    public void addPainter(DiagramPresentationElement diagram, DiagramSurfacePainter painter) {
        DiagramSurface diagramSurface = diagram.getDiagramSurface();
        if (diagramSurface != null) {
            diagramSurface.addPainter(painter);
        }
    }

    public void performProjectVersionComparison(ConfiguredElement configuredElement, Integer baselineVersion,
            Integer releaseVersion, boolean setMemoryOptimization) {
        // get relevant project descriptors for both versions
        Project project = getProject(configuredElement.getElement());

        ProjectDescriptor baseVersionProjectDescriptor = createRemoteProjectDescriptor(project.getName(),
                project.getLoadedFrom(), baselineVersion);
        ProjectDescriptor releaseVersionProjectDescriptor = createRemoteProjectDescriptor(project.getName(),
                project.getLoadedFrom(), releaseVersion);
        // compare using project descriptors
        compareProjects(releaseVersionProjectDescriptor, baseVersionProjectDescriptor, configuredElement.getUIDomain(),
                setMemoryOptimization);
    }

    /**
     * Compare projects and display project difference GUI.
     *
     * @param releaseVersionProjectDescriptor release version of project descriptor.
     * @param baseVersionProjectDescriptor base version of project descriptor.
     * @param uiDomain used to display errors from the merge util.
     */
    protected void compareProjects(ProjectDescriptor releaseVersionProjectDescriptor,
            ProjectDescriptor baseVersionProjectDescriptor, UIDomain uiDomain, boolean setMemoryOptimization) {
        ProjectsManager projectsManager = getProjectsManager();
        projectsManager.loadProject(releaseVersionProjectDescriptor, true); // load release project version
        Project releaseProject = projectsManager.findProject(releaseVersionProjectDescriptor);

        if (releaseProject != null && baseVersionProjectDescriptor != null) {
            final ProjectDifference difference = compareProjectsWrapper(baseVersionProjectDescriptor, releaseProject,
                getErrorHandlerForMergeUtility(uiDomain), setMemoryOptimization);
            if (difference != null) {
                if (noChangesBetweenProjectsFound(difference)) {
                    mergeRestore(difference); // on diff MagicDraw may load projects, so need to restore previous state after diff
                } else {
                    displayDifference(difference);
                }
            }
        }
    }

    protected ProjectDifference compareProjectsWrapper(ProjectDescriptor baseVersionProjectDescriptor,
            Project releaseProject, SimpleErrorHandler simpleErrorHandler, boolean setMemoryOptimization) {
        return MergeUtil.compareProjects(releaseProject, baseVersionProjectDescriptor, simpleErrorHandler,
            setMemoryOptimization ? MergeUtil.Optimization.MEMORY : MergeUtil.Optimization.PERFORMANCE);
    }

    protected boolean noChangesBetweenProjectsFound(ProjectDifference difference) {
        return difference.getChanges().isEmpty(); // wrapped because getChanges() returns a type we cannot test with
    }

    protected void mergeRestore(ProjectDifference difference) {
        MergeUtil.restore(difference);
    }

    protected void displayDifference(ProjectDifference difference) {
        MergeUtil.showDifferenceGUI(difference);
    }

    protected SimpleErrorHandler getErrorHandlerForMergeUtility(UIDomain uiDomain) {
        return new SimpleErrorHandler(uiDomain);
    }

    protected static class SimpleErrorHandler implements ErrorHandler<Exception> {
        private final UIDomain uiDomain;

        public SimpleErrorHandler(UIDomain uiDomain) {
            this.uiDomain = uiDomain;
        }

        @Override
        public void error(Exception ex) throws Exception {
            uiDomain.showErrorMessage(ex.getMessage(), ExceptionConstants.MERGE_UTIL_ERROR_TITLE);
        }
    }

    public boolean isRevisionMostRecentAndProjectDirty(ConfiguredElement configuredElement, int revision) {
        return getCurrentBranchLatestRevision(getIProject(configuredElement.getElement())) == revision && getCurrentProject().isDirty();
    }

    //*** Comments ***

    public String getComment(Element element) {
        return element != null ? ModelHelper.getComment(element) : null;
    }

    public void setComment(Element element, String doc) {
        if (element != null && doc != null) {
            ModelHelper.setComment(element, doc);
        }
    }

    //*** Permissions ***

    public void addPermissionsHandler(ElementPermissions elementPermissions) {
        ElementPermissionsManager.getElementPermissionsManager().addPermissionsHandler(elementPermissions);
    }

    public void removePermissionsHandler(ElementPermissions elementPermissions) {
        ElementPermissionsManager.getElementPermissionsManager().removePermissionsHandler(elementPermissions);
    }

    //*** Teamwork Cloud ***

    public TeamworkCloudConnectionInfo getServerInfo() {
        ConnectionInfo connectionInfo = getConnectionInfo();
        if (connectionInfo != null) {
            String protocol = PluginConstant.PROTOCOL_HTTPS;
            if (!connectionInfo.isUseSecureConnection()) {
                protocol = PluginConstant.PROTOCOL_HTTP;
            }
            return createTeamworkCloudConnectionInfo(connectionInfo.getHostPort(), connectionInfo.getUserName(), protocol);
        }
        return null;
    }

    protected ConnectionInfo getConnectionInfo() {
        return EsiUtils.getServerInfo(getCurrentProject());
    }

    protected TeamworkCloudConnectionInfo createTeamworkCloudConnectionInfo(String url, String user, String protocol) {
        return new TeamworkCloudConnectionInfo(url, user, protocol); // used for unit testing purposes
    }

    public String getSecondaryAuthToken(String key) throws ConnectException {
        //TODO replace deprecated method, if this is replaced with a REST call then we need to refactor where this method
        // would be located.
        return EsiServerActionsExecuter.getSecondaryAuthToken(key);
    }

    public ServerServices getServerServices(IPrimaryProject project) {
        //TODO replace deprecated method, if this is replaced with a REST call then we need to refactor where this method
        // would be located.
        return EsiUtilsInternal.getServerServices(project);
    }

    //*** Object Creation ***

    public Class createClassInstance(Project project) {
        return project.getElementsFactory().createClassInstance();
    }

    public Package createPackageInstance(Project project) {
        return project.getElementsFactory().createPackageInstance();
    }

    //*** Element Search ***

    public <T extends Element> T findInProject(Project project, String name) {
        return project != null && name != null ? Finder.byQualifiedName().find(project, name) : null;
    }

    public <T extends Element> T findInCurrentProject(String name) {
        return findInProject(getCurrentProject(), name);
    }

    public Class findClassRelativeToCurrentPrimary(String name) {
        Project currentProject = getCurrentProject();
        if (currentProject != null) {
            Package primaryModel = currentProject.getPrimaryModel();
            return primaryModel != null ? Finder.byQualifiedName().findRelative(primaryModel, name, Class.class) : null;
        }
        return null;
    }

    public Package findPackageRelativeToCurrentPrimary(String name) {
        return name != null ? Finder.byQualifiedName().findRelative(getCurrentProject().getPrimaryModel(), name, Package.class) : null;
    }

    public Package findRelativePackage(Package context, String name) {
        return context != null && name != null ? Finder.byQualifiedName().findRelative(context, name, Package.class) : null;
    }

    public Stereotype findInProject(String stereotypeName) {
        Element found = findInProject(getCurrentProject(), stereotypeName);
        return found instanceof Stereotype ? (Stereotype) found : null;
    }

    public Element getElementUsingId(String id) {
        Element element = null;

        if(id != null) {
            BaseElement baseElement = getCurrentProject().getElementByID(id);
            if(baseElement instanceof Element) {
                element = (Element) baseElement;
            } else {
                element = findInCurrentProject(id);
            }
        }
        return element;
    }

    public Property getPropertyUsingId(String id) {
        Element property = getElementUsingId(id);
        return property instanceof Property ? (Property) property : null;
    }

    //*** Profiles and Stereotypes ***

    public Profile getProfile(Project project, String profileName) {
        return StereotypesHelper.getProfile(project, profileName);
    }

    public Stereotype getStereotype(Project project, String stereotype, Profile profile) {
        return StereotypesHelper.getStereotype(project, stereotype, profile);
    }

    public void addStereotypeToElement(Element element, Stereotype stereotype) {
        StereotypesHelper.addStereotype(element, stereotype);
        StereotypesHelper.createDefaultValues(element, stereotype, false);
    }

    public boolean hasStereotype(Element element, Stereotype stereotype) {
        return StereotypesHelper.hasStereotype(element, stereotype);
    }

    public void setStereotypePropertyValue(Element element, Stereotype stereotype, String property, Object value) {
        StereotypesHelper.setStereotypePropertyValue(element, stereotype, property, value);
    }

    public void setStereotypePropertyValue(Element element, Stereotype stereotype, String propertyName, Object value, boolean appendValues) {
        StereotypesHelper.setStereotypePropertyValue(element, stereotype, propertyName, value, appendValues);
    }

    public List<Object> getStereotypePropertyValue(Element element, Stereotype stereotype, String property) {
        return StereotypesHelper.getStereotypePropertyValue(element, stereotype, property);
    }

    public List<Object> getStereotypePropertyValue(Element element, String stereotype, String property) {
        return StereotypesHelper.getStereotypePropertyValue(element, stereotype, property);
    }

    public <T> T getStereotypePropertyFirstValueGivenType(Element element, Stereotype stereotype, String property, java.lang.Class<T> type) {
        List<Object> values = getStereotypePropertyValue(element, stereotype, property);
        if(values != null && !values.isEmpty() && type.isInstance(values.get(0))) {
            return type.cast(values.get(0));
        }
        return null;
    }

    public List<String> getStereotypePropertyValueAsString(Element element, Stereotype stereotype, String property) {
        return StereotypesHelper.getStereotypePropertyValueAsString(element, stereotype, property);
    }

    public List<String> getStereotypePropertyValueAsString(Element element, String stereotypeName, String property) {
        //TODO replace deprecated method
        return StereotypesHelper.getStereotypePropertyValueAsString(element, stereotypeName, property);
    }

    public String getStereotypePropertyFirstValueAsString(Element element, Stereotype stereotype, String property) {
        return StereotypesHelper.getStereotypePropertyValueAsString(element, stereotype, property)
                .stream().findFirst().orElse(null);
    }

    public List<Element> getStereotypedElements(Stereotype stereotype) {
        return StereotypesHelper.getStereotypedElements(stereotype);
    }

    public Collection<Stereotype> getDerivedStereotypes(Element element, Stereotype stereotype, boolean includeParent) {
        return StereotypesHelper.getDerivedStereotypes(element, stereotype, includeParent);
    }

    public Collection<Element> getExtendedElementsIncludingDerived(Stereotype stereotype) {
        //TODO remove deprecated method
        return StereotypesHelper.getExtendedElementsIncludingDerived(stereotype);
    }

    public List<Stereotype> getDerivedStereotypesRecursively(Stereotype stereotype) {
        return StereotypesHelper.getDerivedStereotypesRecursively(stereotype);
    }

    public void clearStereotypeProperty(Element element, Stereotype stereotype, String propertyName) {
        StereotypesHelper.clearStereotypeProperty(element, stereotype, propertyName);
    }

    public boolean hasStereotypeOrDerived(Element element, Stereotype stereotype) {
        return StereotypesHelper.hasStereotypeOrDerived(element, stereotype);
    }

    public boolean canAssignStereotype(Element element, Stereotype stereotype) {
        return StereotypesHelper.canAssignStereotype(element, stereotype);
    }

    //*** Values ***

    public Object getDefaultValue(Stereotype stereo, String propertyName) {
        List<NamedElement> list = stereo.getOwnedMember().stream()
                .filter(x -> x instanceof Property && x.getName().equals(propertyName))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            //TODO: Verify case when there are two levels of inheritance, each with a redefinition of creationRole
            list = stereo.getInheritedMember().stream()
                    .filter(x -> x instanceof Property && x.getName().equals(propertyName))
                    .collect(Collectors.toList());
        }

        if (!list.isEmpty()) {
            return getValueSpecificationValueFromProperty(((Property) list.get(0)));
        }

        return null;
    }

    protected Object getValueSpecificationValueFromProperty(Property property) {
        if (property.getDefaultValue() != null){
            return ModelHelper.getValueSpecificationValue(property.getDefaultValue());
        }
        return null;
    }

    //*** Locking ***

    protected ILockProjectService getLockService(Element element) {
        return LockService.getLockService(getProject(element));
    }

    protected ILockProjectService getLockService() {
        return LockService.getLockService(getCurrentProject());
    }

    public boolean isElementInEditableState(Element element) {
        // true if the element is editable, "free or new" according to esi, or is locked by the user
        return element.isEditable() || isElementFreeOrNew(element) || isElementLockedByMe(element);
    }

    protected boolean isElementFreeOrNew(Element element) {
        return (element instanceof EsiObjectImpl && ((EsiObjectImpl) element).esiState().isFreeOrNew());
    }

    protected boolean isElementLockedByMe(Element element) {
        ILockProjectService lockProjectService = getLockService(element);
        return lockProjectService != null && lockProjectService.isLockedByMe(element);
    }

    public String getLockingUser(Element element) {
        ILockProjectService lockService = getLockService(element);
        if (lockService != null && lockService.isLocked(element)) {
            LockInfo lockInfo = lockService.getLockInfo(element);
            if (lockInfo != null) {
                return lockInfo.getUser();
            }
            return PluginConstant.NO_LOCK_INFORMATION;
        }
        return PluginConstant.NO_LOCK_SERVICE_OR_NOT_LOCKED;
    }

    public boolean isAnyLocked(List<ConfiguredElement> configuredElements) {
        if (configuredElements != null && !configuredElements.isEmpty()){
            ILockProjectService lockService = getLockService(configuredElements.get(0).getElement());
            for (LifecycleObject configuredElement : configuredElements) {
                // this disables the contextual menu when the element is not locked
                if (lockService == null || lockService.isLocked(configuredElement.getElement())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void unlock(Element element) {
        ILockProjectService lockService = getLockService(element);
        if (lockService != null && lockService.isLockedByMe(element)) {
            lockService.unlockElements(List.of(element), true, null, false);
        }
    }

    public void lock(List<ConfiguredElement> configuredElements) {
        ILockProjectService lockService = getLockService();
        lockService.lockElements(configuredElements.stream().map(LifecycleObject::getElement).collect(Collectors.toList()), false, null);
    }

    //*** Packages ***

    public Package createPackageStructureGivenPath(Project project, Package primaryModel, String changeManagementPath) {
        String[] packagesInPath = changeManagementPath.split(PluginConstant.PACKAGE_DELIM);

        StringBuilder reconstructedPath = new StringBuilder();
        Package owner = primaryModel;
        Package packageObj = null;
        boolean first = true;
        for (String packageName : packagesInPath) {
            if (!first) {
                reconstructedPath.append(PluginConstant.PACKAGE_DELIM);
            } else {
                first = false;
            }
            reconstructedPath.append(packageName);
            packageObj = createPackageIfNotFound(project, reconstructedPath.toString(), primaryModel, owner, packageName);
            owner = packageObj;
        }
        return packageObj;
    }

    public Package createPackageIfNotFound(Project project, String path, Package context, Package owner, String name) {
        Package relativePackage = findRelativePackage(context, path);
        if (relativePackage == null) {
            relativePackage = project.getElementsFactory().createPackageInstance();
            relativePackage.setName(name);
            relativePackage.setOwner(owner);
        }
        return relativePackage;
    }
}
