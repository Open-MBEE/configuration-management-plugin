package org.openmbee.plugin.cfgmgmt.listeners;

import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraClientManager;
import org.openmbee.plugin.cfgmgmt.integration.jira.JiraService;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxClientManager;
import org.openmbee.plugin.cfgmgmt.integration.threedx.ThreeDxService;
import org.openmbee.plugin.cfgmgmt.integration.twc.TeamworkCloudService;
import org.openmbee.plugin.cfgmgmt.permissions.CMElementPermissions;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.settings.CustomSettings;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.ui.DiagramSurfacePainter;
import com.nomagic.magicdraw.ui.DiagramWindow;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.DiagramSurface;
import com.nomagic.uml2.ext.jmi.RepositoryListenerRegistry;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TestProjectListener {
    @Mock
    private Project project;
    @Mock
    private ConfigurationManagementService configurationManagementService;
    @Spy
    @InjectMocks
    private ProjectListener projectListener;
    @Mock
    private ApiDomain apiDomain;
    @Mock
    private ThreeDxService threeDxService;
    @Mock
    private ThreeDxClientManager threeDxClientManager;
    @Mock
    private JiraService jiraService;
    @Mock
    private JiraClientManager jiraClientManager;
    @Mock
    private DiagramSurfacePainter painter;
    @Mock
    private CMPropertyListener propertyListener;
    @Mock
    private RepositoryListenerRegistry repositoryListenerRegistry;
    @Mock
    private TeamworkCloudService teamworkCloudService;
    @Mock
    private CustomSettings customSettings;
    @Mock
    private Profile profile;
    @Mock
    private Stereotype revisionHistory;
    @Mock
    private Stereotype cmCustomSettings;
    @Mock
    private Stereotype threeDxSettings;
    @Mock
    private Stereotype jiraSettings;
    @Mock
    private Stereotype configuredElement;
    @Mock
    private Stereotype changeRecord;


    @Before
    public void setup() {
        doReturn(apiDomain).when(projectListener).getApiDomain();
        doReturn(threeDxService).when(projectListener).getThreeDxService();
        when(threeDxService.getThreeDxClientManager()).thenReturn(threeDxClientManager);
        when(jiraService.getJiraClientManager()).thenReturn(jiraClientManager);
        doReturn(configurationManagementService).when(projectListener).getConfigurationManagementService();
        when(configurationManagementService.getTeamworkCloudService()).thenReturn(teamworkCloudService);
        when(projectListener.getPainter()).thenReturn(painter);
    }

    @Test
    public void projectActivated() {
        doNothing().when(projectListener).projectStartup(project);

        projectListener.projectActivated(project);

        verify(projectListener).projectStartup(project);
    }

    @Test
    public void projectOpened() {
        doNothing().when(projectListener).projectStartup(project);

        projectListener.projectOpened(project);

        verify(projectListener).projectStartup(project);
    }

    @Test
    public void projectDeActivated() {
        doNothing().when(projectListener).projectCleanup(project);

        projectListener.projectDeActivated(project);

        verify(projectListener).projectCleanup(project);
    }

    @Test
    public void projectClosed() {
        doNothing().when(projectListener).projectCleanup(project);

        projectListener.projectClosed(project);

        verify(projectListener).projectCleanup(project);
    }

    @Test
    public void projectClosed_null() {
        projectListener.projectClosed(null);

        verify(projectListener, never()).projectCleanup(any());
    }

    @Test
    public void projectStartup_nullListener_notActive() {
        doReturn(false).when(configurationManagementService).isCmActive();

        projectListener.projectStartup(project);

        verify(configurationManagementService).disableAdminMode();
        verify(configurationManagementService).updateCmActive();
        verify(apiDomain, never()).getProfile(any(), any());
    }

    @Test
    public void projectStartup_notNullListener_active() {
        doReturn(jiraService).when(projectListener).getJiraService();
        doReturn(propertyListener).when(projectListener).getPropertyListener();
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(apiDomain).when(projectListener).getApiDomain();
        when(apiDomain.getProfile(project, "Configuration Management Profile")).thenReturn(profile);
        when(apiDomain.getStereotype(project, PluginConstant.REVISION_HISTORY_STEREOTYPE, profile)).thenReturn(revisionHistory);
        when(apiDomain.getStereotype(project, PluginConstant.CM_CUSTOM_SETTINGS_STEREOTYPE, profile)).thenReturn(cmCustomSettings);
        when(apiDomain.getStereotype(project, PluginConstant.THREEDX_CONNECTION_SETTINGS_STEREOTYPE, profile)).thenReturn(threeDxSettings);
        when(apiDomain.getStereotype(project, PluginConstant.JIRA_CONNECTION_SETTINGS_STEREOTYPE, profile)).thenReturn(jiraSettings);
        when(apiDomain.findInProject(PluginConstant.CONFIGURED_ELEMENT_STEREOTYPE_PATH)).thenReturn(configuredElement);
        when(apiDomain.findInProject(PluginConstant.CHANGE_RECORD_STEREOTYPE_PATH)).thenReturn(changeRecord);
        when(configurationManagementService.getCustomSettings()).thenReturn(customSettings);

        projectListener.projectStartup(project);

        verify(configurationManagementService).disableAdminMode();
        verify(projectListener, never()).setPropertyListener(propertyListener);
        verify(configurationManagementService).updateCmActive();
        verify(configurationManagementService).setChangeRecordSelections();
        verify(apiDomain).addPermissionsHandler(any(CMElementPermissions.class));
        verify(customSettings).update();
        verify(jiraService).updateJIRAConnectionInfo();
    }

    @Test
    public void projectStartup_NullListener_active() {
        doReturn(jiraService).when(projectListener).getJiraService();
        doReturn(null).when(projectListener).getPropertyListener();
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(propertyListener).when(configurationManagementService).getPropertyListener();
        doReturn(repositoryListenerRegistry).when(project).getRepositoryListenerRegistry();
        doReturn(apiDomain).when(projectListener).getApiDomain();
        when(apiDomain.getProfile(project, "Configuration Management Profile")).thenReturn(profile);
        when(apiDomain.getStereotype(project, PluginConstant.REVISION_HISTORY_STEREOTYPE, profile)).thenReturn(revisionHistory);
        when(apiDomain.getStereotype(project, PluginConstant.CM_CUSTOM_SETTINGS_STEREOTYPE, profile)).thenReturn(cmCustomSettings);
        when(apiDomain.getStereotype(project, PluginConstant.THREEDX_CONNECTION_SETTINGS_STEREOTYPE, profile)).thenReturn(threeDxSettings);
        when(apiDomain.getStereotype(project, PluginConstant.JIRA_CONNECTION_SETTINGS_STEREOTYPE, profile)).thenReturn(jiraSettings);
        when(apiDomain.findInProject(PluginConstant.CONFIGURED_ELEMENT_STEREOTYPE_PATH)).thenReturn(configuredElement);
        when(apiDomain.findInProject(PluginConstant.CHANGE_RECORD_STEREOTYPE_PATH)).thenReturn(changeRecord);
        when(configurationManagementService.getCustomSettings()).thenReturn(customSettings);

        projectListener.projectStartup(project);

        verify(configurationManagementService).disableAdminMode();
        verify(projectListener).setPropertyListener(propertyListener);
        verify(configurationManagementService).updateCmActive();
        verify(configurationManagementService).setChangeRecordSelections();
        verify(apiDomain).addPermissionsHandler(any(CMElementPermissions.class));
        verify(customSettings).update();
        verify(jiraService).updateJIRAConnectionInfo();
    }

    @Test
    public void addPainterForOpenedDiagrams() {
        DiagramPresentationElement diagram = mock(DiagramPresentationElement.class);
        ArrayList<DiagramPresentationElement> diagramList = new ArrayList<>();
        diagramList.add(diagram);

        doReturn(diagramList).when(project).getDiagrams();

        projectListener.addPainterForOpenedDiagrams(project);
        verify(apiDomain).addPainter(diagram, painter);
    }

    @Test
    public void addPainterForOpenedDiagram_notOpened() {
        PropertyChangeEvent event = mock(PropertyChangeEvent.class);

        doReturn(Project.DIAGRAM_CLOSED).when(event).getPropertyName();

        projectListener.addPainterForOpenedDiagram(event);
        verify(event, never()).getNewValue();
        verify(apiDomain, never()).addPainter(any(), any());
    }

    @Test
    public void addPainterForOpenedDiagram_evtNotDiagram() {
        PropertyChangeEvent event = mock(PropertyChangeEvent.class);
        Object newValue = mock(DiagramSurface.class);

        doReturn(Project.DIAGRAM_OPENED).when(event).getPropertyName();
        doReturn(newValue).when(event).getNewValue();

        projectListener.addPainterForOpenedDiagram(event);

        verify(apiDomain, never()).addPainter(any(), any());
    }

    @Test
    public void addPainterForOpenedDiagram_NullDiagram() {
        PropertyChangeEvent event = mock(PropertyChangeEvent.class);
        Object newValue = mock(DiagramWindow.class);

        doReturn(Project.DIAGRAM_OPENED).when(event).getPropertyName();
        doReturn(newValue).when(event).getNewValue();
        doReturn(null).when((DiagramWindow) newValue).getDiagramPresentationElement();

        projectListener.addPainterForOpenedDiagram(event);

        verify(apiDomain, never()).addPainter(any(), any());
    }

    @Test
    public void addPainterForOpenedDiagram() {
        DiagramPresentationElement diagram = mock(DiagramPresentationElement.class);
        PropertyChangeEvent event = mock(PropertyChangeEvent.class);
        Object newValue = mock(DiagramWindow.class);

        doReturn(Project.DIAGRAM_OPENED).when(event).getPropertyName();
        doReturn(newValue).when(event).getNewValue();
        doReturn(diagram).when((DiagramWindow) newValue).getDiagramPresentationElement();
        projectListener.addPainterForOpenedDiagram(event);

        verify(apiDomain).addPainter(diagram, painter);
    }

    @Test
    public void projectCleanup_nullListener_notActive() {
        when(configurationManagementService.isCmActive()).thenReturn(false);
        when(configurationManagementService.getCustomSettings()).thenReturn(customSettings);

        projectListener.projectCleanup(project);

        verify(configurationManagementService).disableAdminMode();
        verify(configurationManagementService).updateCmActive();
        verify(customSettings).clear();
    }

    @Test
    public void projectCleanup_notNullListener_active() {
        doReturn(jiraService).when(projectListener).getJiraService();
        doReturn(propertyListener).when(projectListener).getPropertyListener();
        when(project.getRepositoryListenerRegistry()).thenReturn(repositoryListenerRegistry);
        when(configurationManagementService.isCmActive()).thenReturn(true);
        doNothing().when(teamworkCloudService).logout(apiDomain);
        when(configurationManagementService.getCustomSettings()).thenReturn(customSettings);

        projectListener.projectCleanup(project);

        verify(configurationManagementService).disableAdminMode();
        verify(projectListener).setPropertyListener(null);
        verify(configurationManagementService).updateCmActive();
        verify(configurationManagementService).clearChangeRecordSelectionList();
        verify(customSettings).clear();
        verify(jiraClientManager).cleanJIRAConnectionInfo();
    }

    @Test
    public void projectCleanup_nullListenerAndNullApiDomain_active() {
        doReturn(jiraService).when(projectListener).getJiraService();
        doReturn(null).when(projectListener).getPropertyListener();
        when(configurationManagementService.isCmActive()).thenReturn(true);
        doReturn(null).when(projectListener).getApiDomain();
        when(configurationManagementService.getCustomSettings()).thenReturn(customSettings);

        projectListener.projectCleanup(project);

        verify(configurationManagementService).disableAdminMode();
        verify(projectListener, never()).setPropertyListener(null);
        verify(project, never()).getRepositoryListenerRegistry();
        verify(configurationManagementService).updateCmActive();
        verify(configurationManagementService).clearChangeRecordSelectionList();
        verify(customSettings).clear();
        verify(jiraClientManager).cleanJIRAConnectionInfo();
        verify(teamworkCloudService, never()).logout(any());
    }

    @Test
    public void projectCleanup_NullListener_active() {
        doReturn(jiraService).when(projectListener).getJiraService();
        doReturn(null).when(projectListener).getPropertyListener();
        when(configurationManagementService.isCmActive()).thenReturn(true);
        doNothing().when(teamworkCloudService).logout(apiDomain);
        when(configurationManagementService.getCustomSettings()).thenReturn(customSettings);

        projectListener.projectCleanup(project);

        verify(configurationManagementService).disableAdminMode();
        verify(projectListener, never()).setPropertyListener(null);
        verify(project, never()).getRepositoryListenerRegistry();
        verify(configurationManagementService).updateCmActive();
        verify(configurationManagementService).clearChangeRecordSelectionList();
        verify(customSettings).clear();
        verify(jiraClientManager).cleanJIRAConnectionInfo();
    }
}