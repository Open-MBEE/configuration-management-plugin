package org.openmbee.plugin.cfgmgmt.listeners;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxService;
import org.openmbee.plugin.cfgmgmt.permissions.CMElementPermissions;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.ui.ConfiguredElementSurfacePainter;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.ui.DiagramSurfacePainter;
import com.nomagic.magicdraw.ui.DiagramWindow;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.impl.PropertyNames;

import java.beans.PropertyChangeEvent;

import static org.openmbee.plugin.cfgmgmt.constants.PluginConstant.CONFIGURATION_MANAGEMENT_PROFILE;

public class ProjectListener extends ProjectEventListenerAdapter {
    private final ConfigurationManagementService configurationManagementService;

    private CMPropertyListener propertyListener;
    private CMElementPermissions cmElementPermissions;
    private final DiagramSurfacePainter painter;

    public ProjectListener(ConfigurationManagementService configurationManagementService) {
        this.configurationManagementService = configurationManagementService;
        this.painter = new ConfiguredElementSurfacePainter(configurationManagementService);
    }

    protected ConfigurationManagementService getConfigurationManagementService() {
        return configurationManagementService;
    }

    protected DiagramSurfacePainter getPainter() {
        return painter;
    }

    protected JiraService getJiraService() {
        return getConfigurationManagementService().getJiraService();
    }

    protected ThreeDxService getThreeDxService() {
        return getConfigurationManagementService().getThreeDxService();
    }

    protected ApiDomain getApiDomain() {
        return getConfigurationManagementService().getApiDomain();
    }

    public CMPropertyListener getPropertyListener() {
        return propertyListener;
    }

    public void setPropertyListener(CMPropertyListener propertyListener) {
        this.propertyListener = propertyListener;
    }

    public CMElementPermissions getCmElementPermissions() {
        if (cmElementPermissions == null) {
            cmElementPermissions = new CMElementPermissions(getConfigurationManagementService());
        }
        return cmElementPermissions;
    }

    @Override
    public void projectActivated(Project project) {
        projectStartup(project);
    }

    @Override
    public void projectOpened(Project project) {
        projectStartup(project);
    }

    @Override
    public void projectDeActivated(Project project) {
        // change ordering of currently active/open projects list
        projectCleanup(project);
    }

    @Override
    public void projectClosed(Project project) {
        // remove from our currently active/open projects list
        if (project != null) {
            projectCleanup(project);
        }
    }

    protected void projectStartup(Project project) {
        addPainterForOpenedDiagrams(project);
        project.addPropertyChangeListener(this::addPainterForOpenedDiagram);

        getConfigurationManagementService().disableAdminMode();
        getConfigurationManagementService().updateCmActive();

        if (getConfigurationManagementService().isCmActive()) {
            if (getPropertyListener() == null) {
                setPropertyListener(getConfigurationManagementService().getPropertyListener());
                // element creation: UML2MetamodelConstants.INSTANCE_CREATED
                project.getRepositoryListenerRegistry().addPropertyChangeListener(getPropertyListener(), PropertyNames.VALUE);
                project.getRepositoryListenerRegistry().addPropertyChangeListener(getPropertyListener(), PropertyNames.ELEMENT);
            }

            Profile cmProfile = getApiDomain().getProfile(project, CONFIGURATION_MANAGEMENT_PROFILE);
            getConfigurationManagementService().setCmProfile(cmProfile);
            getConfigurationManagementService().setRhStereotype(getApiDomain().getStereotype(project,
                PluginConstant.REVISION_HISTORY_STEREOTYPE, cmProfile));
            getConfigurationManagementService().setCmcsStereotype(getApiDomain().getStereotype(project,
                PluginConstant.CM_CUSTOM_SETTINGS_STEREOTYPE, cmProfile));
            getConfigurationManagementService().setTdxcsStereotype(getApiDomain().getStereotype(project,
                PluginConstant.THREEDX_CONNECTION_SETTINGS_STEREOTYPE, cmProfile));
            getConfigurationManagementService().setJcsStereotype(getApiDomain().getStereotype(project,
                PluginConstant.JIRA_CONNECTION_SETTINGS_STEREOTYPE, cmProfile));

            getConfigurationManagementService().setBaseCEStereotype(getApiDomain()
                .findInProject(PluginConstant.CONFIGURED_ELEMENT_STEREOTYPE_PATH));
            getConfigurationManagementService().setBaseCRStereotype(getApiDomain()
                .findInProject(PluginConstant.CHANGE_RECORD_STEREOTYPE_PATH));
            getConfigurationManagementService().setBaseCrStatusProperty(getApiDomain().getPropertyUsingId(PluginConstant.CHANGE_RECORD_STATUS_ID));
            getConfigurationManagementService().setBaseIsReleasedProperty(getApiDomain().getPropertyUsingId(PluginConstant.IS_RELEASED_STATUS_ID));

            getConfigurationManagementService().getCustomSettings().update();
            getConfigurationManagementService().setChangeRecordSelections();
            getThreeDxService().update3DxConnectionInfo();
            getJiraService().updateJIRAConnectionInfo();

            getApiDomain().addPermissionsHandler(getCmElementPermissions());
        }
    }

    protected void addPainterForOpenedDiagrams(Project project) {
        for (DiagramPresentationElement diagram : project.getDiagrams()) {
            getApiDomain().addPainter(diagram, getPainter());
        }
    }

    protected void addPainterForOpenedDiagram(PropertyChangeEvent evt) {
        if (Project.DIAGRAM_OPENED.equals(evt.getPropertyName())) {
            Object newValue = evt.getNewValue();
            if (newValue instanceof DiagramWindow) {
                DiagramPresentationElement diagram = ((DiagramWindow) newValue).getDiagramPresentationElement();
                if (diagram != null) {
                    getApiDomain().addPainter(diagram, getPainter());
                }
            }
        }
    }

    protected void projectCleanup(Project project) {
        getConfigurationManagementService().disableAdminMode();

        if (getConfigurationManagementService().isCmActive()) {
            if (getPropertyListener() != null) {
                project.getRepositoryListenerRegistry().removeListener(getPropertyListener());
                setPropertyListener(null);
            }

            getThreeDxService().getThreeDxClientManager().clean3DxConnectionInfo();
            getJiraService().getJiraClientManager().cleanJIRAConnectionInfo();
            getConfigurationManagementService().clearChangeRecordSelectionList();
            if(getApiDomain() != null) {
                getConfigurationManagementService().getTeamworkCloudService().logout(getApiDomain());
                getApiDomain().removePermissionsHandler(getCmElementPermissions());
            }
        }

        getConfigurationManagementService().updateCmActive();
        getConfigurationManagementService().getCustomSettings().clear();
    }
}
