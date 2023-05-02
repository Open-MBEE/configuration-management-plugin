package org.openmbee.plugin.cfgmgmt.domain;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.LifecycleStatus;
import org.openmbee.plugin.cfgmgmt.model.RevisionHistoryRecord;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils.EsiBranchInfo;
import com.nomagic.magicdraw.teamwork2.locks.ILockProjectService;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.statemachines.mdbehaviorstatemachines.State;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestConfiguredElementDomain {
    private ConfigurationManagementService configurationManagementService;
    private ConfiguredElementDomain configuredElementDomain;
    private LifecycleObjectFactory lifecycleObjectFactory;
    private ApiDomain apiDomain;
    private UIDomain uiDomain;
    private NamedElement element;
    private Stereotype stereotype;
    private ConfiguredElement configuredElement;
    private LifecycleStatus lifecycleStatus;
    private ChangeRecord changeRecord;
    private Element releaseAuthorityElement;
    private State state;
    private Logger logger;

    @Before
    public void setup() {
        lifecycleObjectFactory = mock(LifecycleObjectFactory.class);
        apiDomain = mock(ApiDomain.class);
        uiDomain = mock(UIDomain.class);
        configuredElementDomain = spy(new ConfiguredElementDomain(lifecycleObjectFactory, apiDomain, uiDomain));
        configurationManagementService = mock(ConfigurationManagementService.class);
        stereotype = mock(Stereotype.class);
        configuredElement = mock(ConfiguredElement.class);
        element = mock(NamedElement.class);
        lifecycleStatus = mock(LifecycleStatus.class);
        changeRecord = mock(ChangeRecord.class);
        releaseAuthorityElement = mock(Element.class);
        state = mock(State.class);
        logger = mock(Logger.class);

        doReturn(configurationManagementService).when(configuredElement).getConfigurationManagementService();
        doReturn(logger).when(configuredElementDomain).getLogger();
        when(changeRecord.getElement()).thenReturn(releaseAuthorityElement);
    }

    @Test
    public void configure_nullElement() {
        String id = "someId";
        assertNull(configuredElementDomain.configure(configurationManagementService, null, id, stereotype));
    }

    @Test
    public void configure_nullStereotype() {
        String id = "someId";
        assertNull(configuredElementDomain.configure(configurationManagementService, element, id, null));
    }

    @Test
    public void configure_nullId() {
        assertNull(configuredElementDomain.configure(configurationManagementService, element, null, stereotype));
    }

    @Test
    public void configure_nullService() {
        String id = "someId";
        assertNull(configuredElementDomain.configure(null, element, id, stereotype));
    }

    @Test
    public void configure_noInitialStatus() {
        String id = "someId";
        String timestamp = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString();
        String username = "someUser";
        LifecycleStatus status = mock(LifecycleStatus.class);
        configuredElement = spy(new ConfiguredElement(configurationManagementService, element));

        doReturn(stereotype).when(configurationManagementService).getBaseCEStereotype();
        doReturn(timestamp).when(configuredElementDomain).currentTime();
        doReturn(username).when(apiDomain).getLoggedOnUser();
        doReturn(null).when(configuredElementDomain).getInitialStatus(configurationManagementService, stereotype);

        assertNull(configuredElementDomain.configure(configurationManagementService, element, id, stereotype));
        verify(status, never()).getState();
    }

    @Test
    public void configure_noStateFromInitialStatus() {
        String id = "someId";
        String timestamp = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString();
        String username = "someUser";
        LifecycleStatus status = mock(LifecycleStatus.class);
        configuredElement = spy(new ConfiguredElement(configurationManagementService, element));

        doReturn(stereotype).when(configurationManagementService).getBaseCEStereotype();
        doReturn(timestamp).when(configuredElementDomain).currentTime();
        doReturn(username).when(apiDomain).getLoggedOnUser();
        doReturn(status).when(configuredElementDomain).getInitialStatus(configurationManagementService, stereotype);
        doReturn(null).when(status).getState();

        assertNull(configuredElementDomain.configure(configurationManagementService, element, id, stereotype));
        verify(configuredElementDomain, never()).currentTime();
    }

    @Test
    public void configure_NoChangeRecord() {
        String id = "someId";
        String timestamp = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString();
        String username = "someUser";
        LifecycleStatus status = mock(LifecycleStatus.class);
        State state = mock(State.class);
        configuredElement = spy(new ConfiguredElement(configurationManagementService, element));

        doReturn(stereotype).when(configurationManagementService).getBaseCEStereotype();
        doReturn(timestamp).when(configuredElementDomain).currentTime();
        doReturn(username).when(apiDomain).getLoggedOnUser();
        doReturn(status).when(configuredElementDomain).getInitialStatus(configurationManagementService, stereotype);
        doReturn(state).when(status).getState();
        doReturn(null).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(configuredElement).when(lifecycleObjectFactory).getConfiguredElement(configurationManagementService, element);

        assertNotNull(configuredElementDomain.configure(configurationManagementService, element, id, stereotype));
        verify(apiDomain, never()).setStereotypePropertyValue(element, stereotype, PluginConstant.REVISION_RELEASE_AUTHORITY, element);
        verify(changeRecord, never()).addAffectedElement(configuredElement, PluginConstant.CONFIGURING_ACTION);
    }

    @Test
    public void configure_elementConfigured() {
        String id = "someId";
        String timestamp = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString();
        String username = "someUser";
        LifecycleStatus status = mock(LifecycleStatus.class);
        State state = mock(State.class);
        Element crElement = mock(Element.class);
        configuredElement = spy(new ConfiguredElement(configurationManagementService, element));

        doReturn(stereotype).when(configurationManagementService).getBaseCEStereotype();
        doReturn(timestamp).when(configuredElementDomain).currentTime();
        doReturn(username).when(apiDomain).getLoggedOnUser();
        doReturn(status).when(configuredElementDomain).getInitialStatus(configurationManagementService, stereotype);
        doReturn(state).when(status).getState();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(crElement).when(changeRecord).getElement();
        doReturn(configuredElement).when(lifecycleObjectFactory).getConfiguredElement(configurationManagementService, element);
        doNothing().when(changeRecord).addAffectedElement(configuredElement, PluginConstant.CONFIGURING_ACTION);

        assertNotNull(configuredElementDomain.configure(configurationManagementService, element, id, stereotype));
    }

    @Test
    public void checkConfiguredElementPermissions_Exception() {
        String action = "creation";
        doThrow(new RuntimeException()).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        assertFalse(configuredElementDomain.checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement));
}

    @Test
    public void checkConfiguredElementPermissions_NullCczOwner() {
        String action = "creation";

        doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        assertTrue(configuredElementDomain.checkConfiguredElementPermissions(configurationManagementService, stereotype, null));
        verify(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
    }

    @Test
    public void checkConfiguredElementPermissions_nullInitialStatus() {
        String action = "creation";
        LifecycleStatus status = mock(LifecycleStatus.class);
        String getQualifiedName = "name";
        String getID = "ID";

        int r1 = 10;

        doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(r1).when(configuredElement).getStatusMaturityRating();
        doReturn(null).when(configuredElementDomain).getInitialStatus(configurationManagementService, stereotype);
        doReturn(getQualifiedName).when(configuredElement).getQualifiedName();
        doReturn(getID).when(configuredElement).getID();
        doReturn(Optional.of(status)).when(configuredElement).getStatus();

        assertFalse(configuredElementDomain.checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement));
        verify(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
    }

    @Test
    public void checkConfiguredElementPermissions_BadCczStatus() {
        String action = "creation";
        LifecycleStatus status = mock(LifecycleStatus.class);
        String getQualifiedName = "name";
        String getID = "ID";

        int r1 = 10;
        int r2 = 5;

        doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(r1).when(configuredElement).getStatusMaturityRating();
        doReturn(status).when(configuredElementDomain).getInitialStatus(configurationManagementService, stereotype);
        doReturn(r2).when(status).getMaturityRating();
        doReturn(getQualifiedName).when(configuredElement).getQualifiedName();
        doReturn(getID).when(configuredElement).getID();
        doReturn(Optional.of(status)).when(configuredElement).getStatus();

        assertFalse(configuredElementDomain.checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement));
        verify(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
    }

    @Test
    public void checkConfiguredElementPermissions_OkStatus() {
        String action = "creation";
        LifecycleStatus status = mock(LifecycleStatus.class);
        String getQualifiedName = "name";
        String getID = "ID";

        int r1 = 1;
        int r2 = 5;

        doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(r1).when(configuredElement).getStatusMaturityRating();
        doReturn(status).when(configuredElementDomain).getInitialStatus(configurationManagementService, stereotype);
        doReturn(r2).when(status).getMaturityRating();
        doReturn(getQualifiedName).when(configuredElement).getQualifiedName();
        doReturn(getID).when(configuredElement).getID();
        doReturn(Optional.of(status)).when(configuredElement).getStatus();

        assertTrue(configuredElementDomain.checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement));

        verify(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        verify(configuredElementDomain).getInitialStatus(configurationManagementService, stereotype);
        verify(configuredElement).getStatusMaturityRating();
    }

    @Test
    public void checkConfiguredElementPermissions_EqualStatus() {
        String action = "creation";
        LifecycleStatus status = mock(LifecycleStatus.class);
        String getQualifiedName = "name";
        String getID = "ID";

        int r1 = 5;
        int r2 = 5;

        doReturn(true).when(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        doReturn(r1).when(configuredElement).getStatusMaturityRating();
        doReturn(status).when(configuredElementDomain).getInitialStatus(configurationManagementService, stereotype);
        doReturn(r2).when(status).getMaturityRating();
        doReturn(getQualifiedName).when(configuredElement).getQualifiedName();
        doReturn(getID).when(configuredElement).getID();
        doReturn(Optional.of(status)).when(configuredElement).getStatus();

        assertTrue(configuredElementDomain.checkConfiguredElementPermissions(configurationManagementService, stereotype, configuredElement));
        verify(configuredElementDomain).canUserPerformAction(configurationManagementService, stereotype, action);
        verify(configuredElementDomain).getInitialStatus(configurationManagementService, stereotype);
        verify(configuredElement).getStatusMaturityRating();
    }

    @Test
    public void canBeConfigured_WrongParameterType() {
        Element element = mock(Element.class);

        assertFalse(configuredElementDomain.canBeConfigured(element, configurationManagementService));
    }

    @Test
    public void canBeConfigured_ElementInChangeManagePackage() {
        String qualifiedName = PluginConstant.CM_PACKAGE_PATH;

        doReturn(true).when(configurationManagementService).isElementInChangeManagementPackageRoot(element);
        doReturn(qualifiedName).when(element).getQualifiedName();
        doReturn(qualifiedName).when(configurationManagementService).getChangeManagementPackagePath();

        assertFalse(configuredElementDomain.canBeConfigured(element, configurationManagementService));
    }

    @Test
    public void canBeConfigured_ElementNotEditable() {
        String qualifiedName = "Test";
        String packagePath = PluginConstant.CM_PACKAGE_PATH;
        ILockProjectService lockProjectService = mock(ILockProjectService.class);

        doReturn(false).when(configurationManagementService).isElementInChangeManagementPackageRoot(element);
        doReturn(qualifiedName).when(element).getQualifiedName();
        doReturn(packagePath).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(false).when(apiDomain).isElementInEditableState(element);
        doReturn(lockProjectService).when(apiDomain).getLockService(element);
        doReturn(true).when(lockProjectService).isLockedByMe(element);

        assertFalse(configuredElementDomain.canBeConfigured(element, configurationManagementService));
    }

    @Test
    public void canBeConfigured_CmProfileInactive() {
        String qualifiedName = "Test";
        String packagePath = PluginConstant.CM_PACKAGE_PATH;

        doReturn(false).when(configurationManagementService).isElementInChangeManagementPackageRoot(element);
        doReturn(qualifiedName).when(element).getQualifiedName();
        doReturn(packagePath).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(false).when(configurationManagementService).isCmActive();

        assertFalse(configuredElementDomain.canBeConfigured(element, configurationManagementService));
    }

    @Test
    public void canBeConfigured_NoChangeRecordSelected() {
        String qualifiedName = "Test";
        String packagePath = PluginConstant.CM_PACKAGE_PATH;

        doReturn(false).when(configurationManagementService).isElementInChangeManagementPackageRoot(element);
        doReturn(qualifiedName).when(element).getQualifiedName();
        doReturn(packagePath).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(false).when(configurationManagementService).isChangeRecordSelected();

        assertFalse(configuredElementDomain.canBeConfigured(element, configurationManagementService));
    }

    @Test
    public void canBeConfigured_changeRecordNull() {
        String qualifiedName = "Test";
        String packagePath = PluginConstant.CM_PACKAGE_PATH;

        doReturn(false).when(configurationManagementService).isElementInChangeManagementPackageRoot(element);
        doReturn(qualifiedName).when(element).getQualifiedName();
        doReturn(packagePath).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(null);

        assertFalse(configuredElementDomain.canBeConfigured(element, configurationManagementService));
    }

    @Test
    public void canBeConfigured_changeRecordHasStatusButNotExpendable() {
        String qualifiedName = "Test";
        String packagePath = PluginConstant.CM_PACKAGE_PATH;
        String formattedError = String.format(ExceptionConstants.GUI_LOG_MESSAGE, ExceptionConstants.CR_NOT_EXPENDABLE);

        doReturn(false).when(configurationManagementService).isElementInChangeManagementPackageRoot(element);
        doReturn(qualifiedName).when(element).getQualifiedName();
        doReturn(packagePath).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(true).when(changeRecord).hasStatus();
        doReturn(false).when(changeRecord).isExpandable();

        assertFalse(configuredElementDomain.canBeConfigured(element, configurationManagementService));
    }

    @Test
    public void canBeConfigured_NoAssignableCeStereotypes() {
        String qualifiedName = "Test";
        String packagePath = PluginConstant.CM_PACKAGE_PATH;
        List<Stereotype> ceStereotypes = new ArrayList<>();
        Stereotype ce1 = mock(Stereotype.class);
        Stereotype ce2 = mock(Stereotype.class);
        ceStereotypes.add(ce1);
        ceStereotypes.add(ce2);

        doReturn(false).when(configurationManagementService).isElementInChangeManagementPackageRoot(element);
        doReturn(qualifiedName).when(element).getQualifiedName();
        doReturn(packagePath).when(configurationManagementService).getChangeManagementPackagePath();
        doReturn(true).when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).hasStatus();
        doReturn(true).when(changeRecord).isExpandable();
        doReturn(ceStereotypes).when(configurationManagementService).getAvailableCEStereotypes();
        doReturn(false).when(apiDomain).canAssignStereotype(element, ce1);
        doReturn(false).when(apiDomain).canAssignStereotype(element, ce2);

        assertFalse(configuredElementDomain.canBeConfigured(element, configurationManagementService));
    }

    @Test
    public void canBeConfigured_ElementAlreadyConfigured() {
        String qualifiedName = "Test";
        String packagePath = PluginConstant.CM_PACKAGE_PATH;
        List<Stereotype> ceStereotypes = new ArrayList<>();
        Stereotype ce1 = mock(Stereotype.class);
        Stereotype ce2 = mock(Stereotype.class);
        ceStereotypes.add(ce1);
        ceStereotypes.add(ce2);

        doCallRealMethod().when(configurationManagementService).isElementInChangeManagementPackageRoot(element);
        doReturn(qualifiedName).when(element).getQualifiedName();
        doReturn(packagePath).when(configurationManagementService).getChangeManagementPackagePath();
        doCallRealMethod().when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(element).isEditable();
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(true).when(changeRecord).hasStatus();
        doReturn(true).when(changeRecord).isExpandable();
        doReturn(ceStereotypes).when(configurationManagementService).getAvailableCEStereotypes();
        doReturn(false).when(apiDomain).canAssignStereotype(element, ce1);
        doReturn(true).when(apiDomain).canAssignStereotype(element, ce2);
        doReturn(true).when(configuredElementDomain).isConfigured(element, configurationManagementService);

        assertFalse(configuredElementDomain.canBeConfigured(element, configurationManagementService));
    }

    @Test
    public void canBeConfigured_ElementCanBeConfigured() {
        String qualifiedName = "Test";
        String packagePath = PluginConstant.CM_PACKAGE_PATH;
        List<Stereotype> ceStereotypes = new ArrayList<>();
        Stereotype ce1 = mock(Stereotype.class);
        Stereotype ce2 = mock(Stereotype.class);
        ceStereotypes.add(ce1);
        ceStereotypes.add(ce2);

        doCallRealMethod().when(configurationManagementService).isElementInChangeManagementPackageRoot(element);
        doReturn(qualifiedName).when(element).getQualifiedName();
        doReturn(packagePath).when(configurationManagementService).getChangeManagementPackagePath();
        doCallRealMethod().when(apiDomain).isElementInEditableState(element);
        doReturn(true).when(element).isEditable();
        doReturn(true).when(configurationManagementService).isCmActive();
        doReturn(true).when(configurationManagementService).isChangeRecordSelected();
        doReturn(changeRecord).when(configurationManagementService).getSelectedChangeRecord();
        doReturn(false).when(changeRecord).hasStatus();
        doReturn(false).when(changeRecord).isExpandable();
        doReturn(ceStereotypes).when(configurationManagementService).getAvailableCEStereotypes();
        doReturn(false).when(apiDomain).canAssignStereotype(element, ce1);
        doReturn(true).when(apiDomain).canAssignStereotype(element, ce2);
        doReturn(false).when(configuredElementDomain).isConfigured(element, configurationManagementService);

        assertTrue(configuredElementDomain.canBeConfigured(element, configurationManagementService));
    }

    @Test
    public void setReleaseAttributes_attributesSet() {
        Element crElement = mock(Element.class);
        ConfigurationManagementService cnfService = mock(ConfigurationManagementService.class);
        String username = "SomeUser";
        String timestamp = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toString();

        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        doReturn(username).when(apiDomain).getLoggedOnUser();
        doReturn(timestamp).when(configuredElementDomain).currentTime();
        doReturn(cnfService).when(configuredElement).getConfigurationManagementService();
        doReturn(changeRecord).when(cnfService).getSelectedChangeRecord();
        doReturn(crElement).when(changeRecord).getElement();

        configuredElementDomain.setReleaseAttributes(configuredElement);
        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.REVISION_RELEASER_ID, username);
        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.REVISION_RELEASE_DATE, timestamp);
        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.IS_COMMITTED, false);
    }

    @Test
    public void getReleaseAuthority_NullData() {
        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.REVISION_RELEASE_AUTHORITY)).thenReturn(null);

        assertNull(configuredElementDomain.getReleaseAuthority(configuredElement));
    }

    @Test
    public void getReleaseAuthority_hasReleaseAuthority() {
        List<Object> list = new ArrayList<>();
        Class releaseAuthority = mock(Class.class);
        list.add(releaseAuthority);
        Element element = mock(Element.class);

        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.REVISION_RELEASE_AUTHORITY)).thenReturn(list);
        doReturn(changeRecord).when(lifecycleObjectFactory).getChangeRecord(configurationManagementService, releaseAuthority);

        ChangeRecord result = configuredElementDomain.getReleaseAuthority(configuredElement);

        assertNotNull(result);
        assertEquals(changeRecord, result);
    }

    @Test
    public void getReleaseAuthority_changeRecordAffectedElementsListEmpty() {
        List<Object> list = new ArrayList<>();
        Element element = mock(Element.class);

        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.REVISION_RELEASE_AUTHORITY)).thenReturn(list);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(changeRecord.getAffectedElements()).thenReturn(new ArrayList<>());

        assertNull(configuredElementDomain.getReleaseAuthority(configuredElement));
    }

    @Test
    public void getReleaseAuthority_changeRecordAffectedElementsListTooLarge() {
        List<Object> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        Element element = mock(Element.class);

        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.REVISION_RELEASE_AUTHORITY)).thenReturn(list);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(null);

        assertNull(configuredElementDomain.getReleaseAuthority(configuredElement));
        verify(uiDomain).logDebug(ExceptionConstants.MULTIPLE_VALUES_PRESENT);
    }

    @Test
    public void getReleaseAuthority_changeRecordAffectedElementsListHasWrongType() {
        List<Object> list = new ArrayList<>();
        list.add("item");
        Element element = mock(Element.class);

        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.REVISION_RELEASE_AUTHORITY)).thenReturn(list);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(null);

        assertNull(configuredElementDomain.getReleaseAuthority(configuredElement));
        verify(uiDomain).logDebug(ExceptionConstants.CE_NOT_CLASS_OBJECT);
    }

    @Test
    public void getReleaseAuthority_changeRecordAffectedElementsListHasConfiguredElement() {
        List<Object> list = new ArrayList<>();
        List<ConfiguredElement> configuredElementList = new ArrayList<>();
        configuredElementList.add(configuredElement);
        Element element = mock(Element.class);

        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        when(apiDomain.getStereotypePropertyValue(element, stereotype, PluginConstant.REVISION_RELEASE_AUTHORITY)).thenReturn(list);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(changeRecord.getAffectedElements()).thenReturn(configuredElementList);
        doReturn(element).when(changeRecord).getElement();

        ChangeRecord result = configuredElementDomain.getReleaseAuthority(configuredElement);
        assertNotNull(result);
        assertEquals(changeRecord, result);
    }

    @Test
    public void setIsCommitted_propertySet() {
        boolean isCommitted = true;

        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);

        configuredElementDomain.setIsCommitted(configuredElement, isCommitted);
        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.IS_COMMITTED, isCommitted);
    }

    @Test
    public void getID_NoIdPresent() {
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.ID);

        assertNull(configuredElementDomain.getID(configuredElement));
    }

    @Test
    public void getID_IdPresent() {
        String id = "someID";
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        doReturn(id).when(apiDomain).getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.ID);

        assertEquals(id, configuredElementDomain.getID(configuredElement));
    }

    @Test
    public void validateProposedMaturityRatingWRTCczOwner_validated() {
        int cczMaturityRating = 10;
        int proposedRating = 11;

        doReturn(configuredElement).when(configuredElement).getCCZOwner();
        doReturn(cczMaturityRating).when(configuredElement).getStatusMaturityRating();

        assertTrue(configuredElementDomain.validateProposedMaturityRatingWRTCczOwner(configuredElement, proposedRating));
    }

    @Test
    public void validateProposedMaturityRatingWRTCczOwner_InvalidMaturity() {
        String qualifiedName = "Test";
        String getID = "ID";
        int cczMaturityRating = 10;
        int proposedRating = 9;

        doReturn(configuredElement).when(configuredElement).getCCZOwner();
        doReturn(cczMaturityRating).when(configuredElement).getStatusMaturityRating();
        doReturn(qualifiedName).when(configuredElement).getQualifiedName();
        doReturn(getID).when(configuredElement).getID();

        assertFalse(configuredElementDomain.validateProposedMaturityRatingWRTCczOwner(configuredElement, proposedRating));
        verify(uiDomain).logError(String.format(ExceptionConstants.INVALID_MATURITY_RATING, qualifiedName, getID));
    }

    @Test
    public void validateProposedMaturityRatingWRTCczOwner_NoOwner() {
        int proposedRating = 9;

        doReturn(null).when(configuredElement).getCCZOwner();

        assertTrue(configuredElementDomain.validateProposedMaturityRatingWRTCczOwner(configuredElement, proposedRating));
    }


    @Test
    public void validateProposedMaturityRatingWRTOwned_NullCeList() {
        int future = 15;

        doReturn(null).when(configuredElement).getOwnedConfiguredElements();

        configuredElementDomain.validateProposedMaturityRatingWRTOwned(configuredElement, future);
        verify(configuredElement).getOwnedConfiguredElements();
    }

    @Test
    public void validateProposedMaturityRatingWRTOwned_EmptyCe() {
        int future = 15;

        doReturn(new ArrayList<>()).when(configuredElement).getOwnedConfiguredElements();

        configuredElementDomain.validateProposedMaturityRatingWRTOwned(configuredElement, future);
        verify(configuredElement).getOwnedConfiguredElements();
    }

    @Test
    public void validateProposedMaturityRatingWRTOwned_BadMaturityRatingAndNoSelectedCr() {
        int future = 15;
        ConfiguredElement offendingCe = mock(ConfiguredElement.class);
        List<ConfiguredElement> cmList = List.of(offendingCe);
        String name = "name";
        String id = "id";
        String formattedError = String.format(ExceptionConstants.PROMOTIONS_REQUIRED_FIRST,
                String.format(ExceptionConstants.PREPARE_STRING_LIST_FOR_EXCEPTION, name, id));

        when(configuredElement.getOwnedConfiguredElements()).thenReturn(cmList);
        when(configuredElement.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(null);
        when(offendingCe.getStatusMaturityRating()).thenReturn(future - 1);
        when(offendingCe.getQualifiedName()).thenReturn(name);
        when(offendingCe.getID()).thenReturn(id);

        doNothing().when(uiDomain).logError(logger, formattedError);

        assertFalse(configuredElementDomain.validateProposedMaturityRatingWRTOwned(configuredElement, future));
        verify(uiDomain).logError(logger, formattedError);
    }

    @Test
    public void validateProposedMaturityRatingWRTOwned_BadMaturityRatingAndReleaseAuthorityMismatch() {
        int future = 15;
        ConfiguredElement offendingCe = mock(ConfiguredElement.class);
        ChangeRecord releaseAuthority = mock(ChangeRecord.class);
        List<ConfiguredElement> cmList = List.of(offendingCe);
        String name = "name";
        String id = "id";
        String formattedError = String.format(ExceptionConstants.PROMOTIONS_REQUIRED_FIRST,
                String.format(ExceptionConstants.PREPARE_STRING_LIST_FOR_EXCEPTION, name, id));

        when(configuredElement.getOwnedConfiguredElements()).thenReturn(cmList);
        when(configuredElement.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(offendingCe.getStatusMaturityRating()).thenReturn(future - 1);
        when(offendingCe.getReleaseAuthority()).thenReturn(releaseAuthority);
        when(offendingCe.getQualifiedName()).thenReturn(name);
        when(offendingCe.getID()).thenReturn(id);

        doNothing().when(uiDomain).logError(logger, formattedError);

        assertFalse(configuredElementDomain.validateProposedMaturityRatingWRTOwned(configuredElement, future));
        verify(uiDomain).logError(logger, formattedError);
    }

    @Test
    public void validateProposedMaturityRatingWRTOwned_BadMaturityRatingAndCrIsReleased() {
        int future = 15;
        ConfiguredElement offendingCe = mock(ConfiguredElement.class);
        List<ConfiguredElement> cmList = List.of(offendingCe);
        String name = "name";
        String id = "id";
        String formattedError = String.format(ExceptionConstants.PROMOTIONS_REQUIRED_FIRST,
                String.format(ExceptionConstants.PREPARE_STRING_LIST_FOR_EXCEPTION, name, id));

        when(configuredElement.getOwnedConfiguredElements()).thenReturn(cmList);
        when(configuredElement.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(offendingCe.getStatusMaturityRating()).thenReturn(future - 1);
        when(offendingCe.getReleaseAuthority()).thenReturn(changeRecord);
        when(changeRecord.isReleased()).thenReturn(true);
        when(offendingCe.getQualifiedName()).thenReturn(name);
        when(offendingCe.getID()).thenReturn(id);

        doNothing().when(uiDomain).logError(logger, formattedError);

        assertFalse(configuredElementDomain.validateProposedMaturityRatingWRTOwned(configuredElement, future));
        verify(uiDomain).logError(logger, formattedError);
    }

    @Test
    public void validateProposedMaturityRatingWRTOwned_BadMaturityRatingAndNoAutomatedRelease() {
        int future = 15;
        ConfiguredElement offendingCe = mock(ConfiguredElement.class);
        List<ConfiguredElement> cmList = List.of(offendingCe);
        String name = "name";
        String id = "id";
        String formattedError = String.format(ExceptionConstants.PROMOTIONS_REQUIRED_FIRST,
                String.format(ExceptionConstants.PREPARE_STRING_LIST_FOR_EXCEPTION, name, id));

        when(configuredElement.getOwnedConfiguredElements()).thenReturn(cmList);
        when(configuredElement.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(offendingCe.getStatusMaturityRating()).thenReturn(future - 1);
        when(offendingCe.getReleaseAuthority()).thenReturn(changeRecord);
        when(changeRecord.isReleased()).thenReturn(false);
        when(configurationManagementService.getAutomateReleaseSwitch()).thenReturn(false);
        when(offendingCe.getQualifiedName()).thenReturn(name);
        when(offendingCe.getID()).thenReturn(id);

        doNothing().when(uiDomain).logError(logger, formattedError);

        assertFalse(configuredElementDomain.validateProposedMaturityRatingWRTOwned(configuredElement, future));
        verify(uiDomain).logError(logger, formattedError);
    }

    @Test
    public void validateProposedMaturityRatingWRTOwned_BadMaturityRatingButItsAnAutomatedReleaseScenario() {
        int future = 15;
        ConfiguredElement offendingCe = mock(ConfiguredElement.class);
        List<ConfiguredElement> cmList = List.of(offendingCe);
        String name = "name";
        String id = "id";
        String formattedError = String.format(ExceptionConstants.PROMOTIONS_REQUIRED_FIRST,
                String.format(ExceptionConstants.PREPARE_STRING_LIST_FOR_EXCEPTION, name, id));

        when(configuredElement.getOwnedConfiguredElements()).thenReturn(cmList);
        when(configuredElement.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(offendingCe.getStatusMaturityRating()).thenReturn(future - 1);
        when(offendingCe.getReleaseAuthority()).thenReturn(changeRecord);
        when(changeRecord.isReleased()).thenReturn(false);
        when(configurationManagementService.getAutomateReleaseSwitch()).thenReturn(true);
        when(offendingCe.getQualifiedName()).thenReturn(name);
        when(offendingCe.getID()).thenReturn(id);

        assertTrue(configuredElementDomain.validateProposedMaturityRatingWRTOwned(configuredElement, future));
        verify(uiDomain, never()).logError(logger, formattedError);
    }

    @Test
    public void validateProposedMaturityRatingWRTOwned_GoodMaturityRating() {
        int future = 15;
        ConfiguredElement nonOffendingCe = mock(ConfiguredElement.class);
        List<ConfiguredElement> cmList = List.of(nonOffendingCe);

        doReturn(cmList).when(configuredElement).getOwnedConfiguredElements();
        doReturn(16).when(nonOffendingCe).getStatusMaturityRating();

        assertTrue(configuredElementDomain.validateProposedMaturityRatingWRTOwned(configuredElement, future));
    }

    @Test
    public void getDisplayName_EmptyListFromConfiguredElement() {
        String name = "someName";
        Element element = mock(Element.class);

        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.NAME_PATTERN);

        assertEquals(name, configuredElementDomain.getDisplayName(configuredElement, name));
    }

    @Test
    public void getDisplayName_NullListFromConfiguredElement() {
        String name = "someName";
        Element element = mock(Element.class);

        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        doReturn(null).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.NAME_PATTERN);

        assertEquals(name, configuredElementDomain.getDisplayName(configuredElement, name));
    }

    @Test
    public void getDisplayName_ListWithNullValueFromConfiguredElement() {
        String name = "someName";
        Element element = mock(Element.class);
        List<String> list = new ArrayList<>();
        list.add(null);
        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        doReturn(list).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.NAME_PATTERN);

        assertEquals(name, configuredElementDomain.getDisplayName(configuredElement, name));
    }

    @Test
    public void getDisplayName_ListWithEmptyStringFromConfiguredElement() {
        String name = "someName";
        Element element = mock(Element.class);
        List<String> list = new ArrayList<>();
        list.add("");
        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        doReturn(list).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.NAME_PATTERN);

        assertEquals(name, configuredElementDomain.getDisplayName(configuredElement, name));
    }

    @Test
    public void getDisplayName_ValidListFromConfiguredElement() {
        String name = "someName";
        Element element = mock(Element.class);
        String displayName = "displayName";
        List<String> list = new ArrayList<>();
        list.add(displayName);
        String formatted = "formatted";

        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        doReturn(list).when(apiDomain).getStereotypePropertyValueAsString(element, stereotype, PluginConstant.NAME_PATTERN);
        doReturn(formatted).when(configuredElementDomain).createFormattedDisplayName(element, displayName, name);

        assertEquals(formatted, configuredElementDomain.getDisplayName(configuredElement, name));
    }

    @Test
    public void createFormattedDisplayName_nullDisplayName() {
        String originalName = "originalName";

        String result = configuredElementDomain.createFormattedDisplayName(element, null, originalName);

        assertNotNull(result);
        assertEquals(PluginConstant.EMPTY_STRING, result);
    }

    @Test
    public void createFormattedDisplayName_noSubstitutions() {
        String displayName = "displayName";
        String originalName = "originalName";

        String result = configuredElementDomain.createFormattedDisplayName(element, displayName, originalName);

        assertNotNull(result);
        assertEquals(displayName, result);
    }

    @Test
    public void createFormattedDisplayName_singleSubstitution() {
        String originalName = "originalName";
        String stereotypeName = "s1";
        String propertyName = "p1";
        String displayName = "display ${" + stereotypeName + "." + propertyName + "} Name";
        String value = "value1";
        List<String> values = new ArrayList<>();
        values.add(value);
        String expected = "display " + value + " Name";

        doReturn(values).when(apiDomain).getStereotypePropertyValueAsString(element, stereotypeName, propertyName);

        String result = configuredElementDomain.createFormattedDisplayName(element, displayName, originalName);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void createFormattedDisplayName_multipleSubstitutionWithStringFormat() {
        String originalName = "originalName";
        String stereotypeName = "s1";
        String propertyName = "p1";
        String displayName = "${" + PluginConstant.DISPLAY_NAME_STRING_PATTERN + "}" + " display ${" + stereotypeName + "." + propertyName + "} Name";
        String value = "value1";
        List<String> values = new ArrayList<>();
        values.add(value);
        String expected = originalName + " display " + value + " Name";

        doReturn(values).when(apiDomain).getStereotypePropertyValueAsString(element, stereotypeName, propertyName);

        String result = configuredElementDomain.createFormattedDisplayName(element, displayName, originalName);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void iterateRevision_LcH() {
        String rev = "-";
        int index = 0;
        String ans = "A";
        assertEquals(ans, configuredElementDomain.iterateRevision(rev, index));
    }

    @Test
    public void iterateRevision_LcZ() {
        String rev = "Z";
        int index = 0;
        String ans = "AA";
        assertEquals(ans, configuredElementDomain.iterateRevision(rev, index));
    }

    @Test
    public void iterateRevision_LcZ_IndexNonZero() {
        String rev = "ZZ";
        int index = 1;
        String ans = "AAA";
        assertEquals(ans, configuredElementDomain.iterateRevision(rev, index));
    }

    @Test
    public void iterateRevision_NoExceptions() {
        String rev = "TEST";
        int index = 1;
        String ans = "TFST";
        assertEquals(ans, configuredElementDomain.iterateRevision(rev, index));
    }

    @Test
    public void iterateRevision_InputError1() {
        String rev = "TEST";
        int index = -1;
        assertNull(configuredElementDomain.iterateRevision(rev, index));
    }

    @Test
    public void iterateRevision_InputError2() {
        String rev = "";
        int index = 1;
        assertNull(configuredElementDomain.iterateRevision(rev, index));
    }

    @Test
    public void iterateRevision_InputError3() {
        String rev = null;
        int index = 1;
        assertNull(configuredElementDomain.iterateRevision(rev, index));
    }

    @Test
    public void iterateRevision_ConfiguredElement_NullRevision() {
        doReturn(null).when(configuredElementDomain).getRevision(configuredElement);

        assertNull(configuredElementDomain.iterateRevision(configuredElement));
    }

    protected void setupForIterateRevision_ConfiguredElement(String revision) {
        doReturn(revision).when(configuredElementDomain).getRevision(configuredElement);
        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
    }

    @Test
    public void iterateRevision_ConfiguredElement_NoRevisionYet() {
        String revision = "-";
        String expected = "A";
        setupForIterateRevision_ConfiguredElement(revision);

        String result = configuredElementDomain.iterateRevision(configuredElement);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void iterateRevision_ConfiguredElement_UnexpectedCharacterBeforeA() {
        String revision = "#";
        setupForIterateRevision_ConfiguredElement(revision);

        assertNull(configuredElementDomain.iterateRevision(configuredElement));
    }

    @Test
    public void iterateRevision_ConfiguredElement_UnexpectedCharacterAfterZ() {
        String revision = "|";
        setupForIterateRevision_ConfiguredElement(revision);

        assertNull(configuredElementDomain.iterateRevision(configuredElement));
    }

    @Test
    public void iterateRevision_ConfiguredElement_A() {
        String revision = "A";
        String expected = "B";
        setupForIterateRevision_ConfiguredElement(revision);

        String result = configuredElementDomain.iterateRevision(configuredElement);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void iterateRevision_ConfiguredElement_Z() {
        String revision = "Z";
        String expected = "AA";
        setupForIterateRevision_ConfiguredElement(revision);

        String result = configuredElementDomain.iterateRevision(configuredElement);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void iterateRevision_ConfiguredElement_CZ() {
        String revision = "CZ";
        String expected = "DA";
        setupForIterateRevision_ConfiguredElement(revision);

        String result = configuredElementDomain.iterateRevision(configuredElement);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void iterateRevision_ConfiguredElement_ZZ() {
        String revision = "ZZ";
        String expected = "AAA";
        setupForIterateRevision_ConfiguredElement(revision);

        String result = configuredElementDomain.iterateRevision(configuredElement);

        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void createRevisionHistoryRecord() {
        Project project = mock(Project.class);
        IProject iProject = mock(IProject.class);
        Class clazz = mock(Class.class);
        Package revHistory = mock(Package.class);
        String name = "name";
        String path = "somePath::" + name;
        String value = "value";
        List<String> propValuesList = new ArrayList<>();
        propValuesList.add(value);
        EsiBranchInfo branch = mock(EsiBranchInfo.class);
        long latestRev = 9999999;
        String user1 = "user1";
        String timestamp1 = "timestamp1";
        String user2 = "user2";
        String timestamp2 = "timestamp2";
        String description = "description";
        Package chgMgmt = mock(Package.class);
        Package root = mock(Package.class);
        String id = "someID";

        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        doReturn(id).when(apiDomain).getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.ID);
        doReturn(path).when(configurationManagementService).getRevisionHistoryPackagePath();
        doReturn(chgMgmt).when(configurationManagementService).getChangeManagementPackage(false);
        doReturn(root).when(project).getPrimaryModel();
        when(configuredElementDomain.getApiDomain()).thenReturn(apiDomain);
        when(configuredElement.getElement()).thenReturn(element);
        when(apiDomain.getProject(element)).thenReturn(project);
        when(apiDomain.getIProject(element)).thenReturn(iProject);
        when(apiDomain.createClassInstance(project)).thenReturn(clazz);
        doReturn(stereotype).when(apiDomain).findInProject(project, PluginConstant.REVISION_HISTORY_STEREOTYPE_PATH);
        when(configuredElement.getConfigurationManagementService()).thenReturn(configurationManagementService);
        doReturn(revHistory).when(configurationManagementService).getRevisionHistoryPackage(true);
        doReturn(null).when(configurationManagementService).getChangeManagementPackage(false);
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_CREATION_DATE)).thenReturn(timestamp1);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_CREATOR_ID)).thenReturn(user1);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_RELEASE_DATE)).thenReturn(timestamp2);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_RELEASER_ID)).thenReturn(user2);
        doReturn(changeRecord).when(configuredElementDomain).getReleaseAuthority(configuredElement);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.DESCRIPTION)).thenReturn(description);
        when(apiDomain.getStereotypePropertyValueAsString(element, stereotype, PluginConstant.ID)).thenReturn(propValuesList);
        doReturn("revision").when(configuredElementDomain).getRevision(configuredElement);
        when(apiDomain.getCurrentBranch(iProject)).thenReturn(branch);
        when(branch.getName()).thenReturn(name);
        when(apiDomain.getCurrentBranchLatestRevision(iProject)).thenReturn(latestRev);
        doReturn("").when(configuredElementDomain).revisionHistoryRecordCreationSanityCheck(
            stereotype, revHistory, id, "revision", timestamp1, user1, description, timestamp2, user2, releaseAuthorityElement, name, latestRev);

        assertNotNull(configuredElementDomain.createRevisionHistoryRecord(configuredElement));
        verify(configuredElementDomain).revisionHistoryRecordCreationSanityCheck(
            stereotype, revHistory, id, "revision", timestamp1, user1, description, timestamp2, user2, releaseAuthorityElement, name, latestRev);
    }

    @Test
    public void createRevisionHistoryRecord_Null() {
        Project project = mock(Project.class);
        IProject iProject = mock(IProject.class);
        Class clazz = mock(Class.class);
        Package revHistory = mock(Package.class);
        String name = "name";
        String path = "somePath::" + name;
        String value = "value";
        List<String> propValuesList = new ArrayList<>();
        propValuesList.add(value);
        EsiBranchInfo branch = mock(EsiBranchInfo.class);
        long latestRev = 9999999;
        String user1 = "user1";
        String timestamp1 = "timestamp1";
        String user2 = "user2";
        String timestamp2 = "timestamp2";
        String description = "description";
        Package chgMgmt = mock(Package.class);
        Package root = mock(Package.class);
        String id = "someID";

        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        doReturn(id).when(apiDomain).getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.ID);
        doReturn(path).when(configurationManagementService).getRevisionHistoryPackagePath();
        doReturn(chgMgmt).when(configurationManagementService).getChangeManagementPackage(false);
        doReturn(root).when(project).getPrimaryModel();
        when(configuredElementDomain.getApiDomain()).thenReturn(apiDomain);
        when(configuredElement.getElement()).thenReturn(element);
        when(apiDomain.getProject(element)).thenReturn(project);
        when(apiDomain.getIProject(element)).thenReturn(iProject);
        when(apiDomain.createClassInstance(project)).thenReturn(clazz);
        doReturn(stereotype).when(apiDomain).findInProject(project, PluginConstant.REVISION_HISTORY_STEREOTYPE_PATH);
        when(configuredElement.getConfigurationManagementService()).thenReturn(configurationManagementService);
        doReturn(revHistory).when(configurationManagementService).getRevisionHistoryPackage(true);
        doReturn(null).when(configurationManagementService).getChangeManagementPackage(false);
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_CREATION_DATE)).thenReturn(timestamp1);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_CREATOR_ID)).thenReturn(user1);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_RELEASE_DATE)).thenReturn(timestamp2);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_RELEASER_ID)).thenReturn(user2);
        doReturn(changeRecord).when(configuredElementDomain).getReleaseAuthority(configuredElement);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.DESCRIPTION)).thenReturn(description);
        when(apiDomain.getStereotypePropertyValueAsString(element, stereotype, PluginConstant.ID)).thenReturn(propValuesList);
        when(apiDomain.getCurrentBranch(iProject)).thenReturn(branch);
        doReturn("revision").when(configuredElementDomain).getRevision(configuredElement);
        when(branch.getName()).thenReturn(name);
        when(apiDomain.getCurrentBranchLatestRevision(iProject)).thenReturn(latestRev);
        doReturn("notEmpty").when(configuredElementDomain).revisionHistoryRecordCreationSanityCheck(
            stereotype, revHistory, id, "revision", timestamp1, user1, description, timestamp2, user2, releaseAuthorityElement, name, latestRev);

        assertNull(configuredElementDomain.createRevisionHistoryRecord(configuredElement));
        verify(configuredElementDomain).revisionHistoryRecordCreationSanityCheck(
            stereotype, revHistory, id, "revision", timestamp1, user1, description, timestamp2, user2, releaseAuthorityElement, name, latestRev);
    }

    @Test
    public void createRevisionHistoryRecord_NullBranch() {
        Project project = mock(Project.class);
        IProject iProject = mock(IProject.class);
        Class clazz = mock(Class.class);
        Package revHistory = mock(Package.class);
        String name = "name";
        String path = "somePath::" + name;
        String value = "value";
        List<String> propValuesList = new ArrayList<>();
        propValuesList.add(value);
        long latestRev = 9999999;
        String user1 = "user1";
        String timestamp1 = "timestamp1";
        String user2 = "user2";
        String timestamp2 = "timestamp2";
        String description = "description";
        Package chgMgmt = mock(Package.class);
        Package root = mock(Package.class);
        String id = "someID";

        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        doReturn(id).when(apiDomain).getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.ID);
        doReturn(path).when(configurationManagementService).getRevisionHistoryPackagePath();
        doReturn(chgMgmt).when(configurationManagementService).getChangeManagementPackage(false);
        doReturn(root).when(project).getPrimaryModel();
        when(configuredElementDomain.getApiDomain()).thenReturn(apiDomain);
        when(configuredElement.getElement()).thenReturn(element);
        when(apiDomain.getProject(element)).thenReturn(project);
        when(apiDomain.getIProject(element)).thenReturn(iProject);
        when(apiDomain.createClassInstance(project)).thenReturn(clazz);
        doReturn(stereotype).when(apiDomain).findInProject(project, PluginConstant.REVISION_HISTORY_STEREOTYPE_PATH);
        when(configuredElement.getConfigurationManagementService()).thenReturn(configurationManagementService);
        doReturn(revHistory).when(configurationManagementService).getRevisionHistoryPackage(true);
        doReturn(null).when(configurationManagementService).getChangeManagementPackage(false);
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_CREATION_DATE)).thenReturn(timestamp1);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_CREATOR_ID)).thenReturn(user1);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_RELEASE_DATE)).thenReturn(timestamp2);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_RELEASER_ID)).thenReturn(user2);
        doReturn(changeRecord).when(configuredElementDomain).getReleaseAuthority(configuredElement);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.DESCRIPTION)).thenReturn(description);
        when(apiDomain.getStereotypePropertyValueAsString(element, stereotype, PluginConstant.ID)).thenReturn(propValuesList);
        when(apiDomain.getCurrentBranch(iProject)).thenReturn(null);
        doReturn("revision").when(configuredElementDomain).getRevision(configuredElement);
        when(apiDomain.getCurrentBranchLatestRevision(iProject)).thenReturn(latestRev);
        doReturn("notEmpty").when(configuredElementDomain).revisionHistoryRecordCreationSanityCheck(
            stereotype, revHistory, id, "revision", timestamp1, user1, description, timestamp2, user2, releaseAuthorityElement, null, latestRev);

        assertNull(configuredElementDomain.createRevisionHistoryRecord(configuredElement));
        verify(configuredElementDomain).revisionHistoryRecordCreationSanityCheck(
            stereotype, revHistory, id, "revision", timestamp1, user1, description, timestamp2, user2, releaseAuthorityElement, null, latestRev);
    }

    @Test
    public void isCommitted_containsTrue() {
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        doReturn(List.of(true)).when(apiDomain).getStereotypePropertyValue(element, stereotype, PluginConstant.IS_COMMITTED);

        assertTrue(configuredElementDomain.isCommitted(configuredElement));
    }

    @Test
    public void isCommitted_doesNotContainTrue() {
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        doReturn(List.of("")).when(apiDomain).getStereotypePropertyValue(element, stereotype, PluginConstant.IS_COMMITTED);

        assertFalse(configuredElementDomain.isCommitted(configuredElement));
    }

    @Test
    public void isCommitted_emptyValue() {
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        doReturn(apiDomain).when(configuredElementDomain).getApiDomain();
        doReturn(new ArrayList<>()).when(apiDomain).getStereotypePropertyValue(element, stereotype, PluginConstant.IS_COMMITTED);

        assertFalse(configuredElementDomain.isCommitted(configuredElement));
    }

    @Test
    public void isCommitted_nullValue() {
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        doReturn(null).when(apiDomain).getStereotypePropertyValue(element, stereotype, PluginConstant.IS_COMMITTED);

        assertFalse(configuredElementDomain.isCommitted(configuredElement));
    }

    @Test
    public void isConfigured_True() {
        when(apiDomain.hasStereotypeOrDerived(element, configurationManagementService.getBaseCEStereotype())).thenReturn(true);
        assertTrue(configuredElementDomain.isConfigured(element, configurationManagementService));
    }

    @Test
    public void isConfigured_False() {
        when(apiDomain.hasStereotypeOrDerived(element, configurationManagementService.getBaseCEStereotype())).thenReturn(false);
        assertFalse(configuredElementDomain.isConfigured(element, configurationManagementService));
    }

    @Test
    public void resetRevisionAttributes() {
        String username = "user";
        doReturn(apiDomain).when(configuredElementDomain).getApiDomain();
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        when(apiDomain.getLoggedOnUser()).thenReturn(username);
        when(configuredElement.getConfigurationManagementService()).thenReturn(configurationManagementService);
        when(configurationManagementService.getSelectedChangeRecord()).thenReturn(changeRecord);
        when(changeRecord.getElement()).thenReturn(element);

        configuredElementDomain.resetRevisionAttributes(configuredElement);

        verify(apiDomain).clearStereotypeProperty(element, stereotype, PluginConstant.IS_COMMITTED);
    }

    @Test
    public void getRelevantChangeRecords() {
        List<RevisionHistoryRecord> relatedRevisionRecords = new ArrayList<>();
        RevisionHistoryRecord revisionHistoryRecord = mock(RevisionHistoryRecord.class);
        RevisionHistoryRecord revisionHistoryRecord2 = mock(RevisionHistoryRecord.class);
        relatedRevisionRecords.add(revisionHistoryRecord);
        relatedRevisionRecords.add(revisionHistoryRecord2);
        List<ChangeRecord> affectingChangeRecords = new ArrayList<>();
        ChangeRecord changeRecordWithoutRevisionRecord = mock(ChangeRecord.class);
        affectingChangeRecords.add(changeRecord);
        affectingChangeRecords.add(changeRecordWithoutRevisionRecord);
        doReturn(affectingChangeRecords).when(configuredElementDomain).getChangeRecordsAffectingConfiguredElement(configuredElement, configurationManagementService);

        List<ChangeRecord> results = configuredElementDomain.getRelevantChangeRecords(configuredElement, configurationManagementService);

        assertTrue(results.contains(changeRecord));
        assertTrue(results.contains(changeRecordWithoutRevisionRecord));

        verify(configuredElementDomain).getChangeRecordsAffectingConfiguredElement(configuredElement, configurationManagementService);
    }

    @Test
    public void getChangeRecordsAffectingConfiguredElement_changeRecordsFound() {
        ChangeRecord changeRecord = mock(ChangeRecord.class);
        List<ChangeRecord> history = List.of(changeRecord);

        doReturn(history).when(configuredElementDomain).getChangeRecordsAffectingConfiguredElement(configuredElement, configurationManagementService);

        assertFalse(configuredElementDomain.getChangeRecordsAffectingConfiguredElement(configuredElement,
            configurationManagementService).isEmpty());
        verify(uiDomain, never()).logErrorAndShowMessage(any(), anyString(), anyString());
    }

    @Test
    public void getChangeRecordsAffectingConfiguredElement_noChangeRecordsFound() {
        String name = "name";
        String error = String.format(ExceptionConstants.NO_CHANGE_RECORDS_FOUND, name);

        when(configuredElementDomain.getAllRecordsAffectingConfiguredElement(configuredElement,
            configurationManagementService)).thenReturn(List.of());
        when(configuredElement.getName()).thenReturn(name);
        doNothing().when(uiDomain).logErrorAndShowMessage(logger, error, ExceptionConstants.NO_CHANGE_RECORDS_FOUND_TITLE);

        assertTrue(configuredElementDomain.getChangeRecordsAffectingConfiguredElement(configuredElement,
            configurationManagementService).isEmpty());
        verify(uiDomain).logErrorAndShowMessage(logger, error, ExceptionConstants.NO_CHANGE_RECORDS_FOUND_TITLE);
    }

    @Test
    public void attachRevisionHistoryRecord() {
        Class clazz = mock(Class.class);
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        configuredElementDomain.attachRevisionHistoryRecord(configuredElement, clazz);
        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.REVISION_HISTORY, clazz, true);
    }

    @Test
    public void setStatusToInWork() {
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        when(configuredElement.getInitialStatus()).thenReturn(lifecycleStatus);
        when(lifecycleStatus.getState()).thenReturn(state);

        assertTrue(configuredElementDomain.setStatusToInWork(configuredElement));

        verify(apiDomain).setStereotypePropertyValue(element, stereotype, PluginConstant.STATUS, state);
    }

    @Test
    public void setStatusToInWork_NullInitialStatus() {
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        when(configuredElement.getInitialStatus()).thenReturn(null);
        when(lifecycleStatus.getState()).thenReturn(state);

        assertFalse(configuredElementDomain.setStatusToInWork(configuredElement));
    }

    @Test
    public void getRevision() {
        String revision = "revision";
        when(configuredElement.getElement()).thenReturn(element);
        when(configuredElement.getBaseStereotype()).thenReturn(stereotype);
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION)).thenReturn(revision);

        assertNotNull(configuredElementDomain.getRevision(configuredElement));
        assertEquals(revision, configuredElementDomain.getRevision(configuredElement));
    }

    @Test
    public void getRevision_Null() {
        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        when(apiDomain.getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION)).thenReturn(null);
        assertNull(configuredElementDomain.getRevision(configuredElement));
    }

    @Test
    public void getRevisionCreationDate() {
        String value = "value";
        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        doReturn(value).when(apiDomain).getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_CREATION_DATE);

        String actualValue = configuredElementDomain.getRevisionCreationDate(configuredElement);
        assertEquals(actualValue, value);

        verify(configuredElement).getElement();
        verify(apiDomain).getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_CREATION_DATE);
    }

    @Test
    public void getRevisionReleaseDate() {
        String value = "value";
        doReturn(element).when(configuredElement).getElement();
        doReturn(stereotype).when(configuredElement).getBaseStereotype();
        doReturn(value).when(apiDomain).getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_RELEASE_DATE);

        String actualValue = configuredElementDomain.getRevisionReleaseDate(configuredElement);
        assertEquals(actualValue, value);

        verify(configuredElement).getElement();
        verify(apiDomain).getStereotypePropertyFirstValueAsString(element, stereotype, PluginConstant.REVISION_RELEASE_DATE);
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, "id", "rev",
            "created", "creator", "description", "released", "releaser",
                element, "branch", 1);

        assertTrue(message.isEmpty());
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail1() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(null, pkg, "id", "rev",
            "created", "creator", "description", "released", "releaser",
                element, "branch", 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_STEREOTYPE_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_STEREOTYPE_WHILE_CREATING_HISTORY_RECORD
            .substring(0, ExceptionConstants.NULL_STEREOTYPE_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail2() {
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, null, "id", "rev",
            "created", "creator", "description", "released", "releaser",
                element, "branch", 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_PACKAGE_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_PACKAGE_WHILE_CREATING_HISTORY_RECORD
            .substring(0, ExceptionConstants.NULL_PACKAGE_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail3() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, null, "rev",
            "created", "creator", "description", "released", "releaser",
                element, "branch", 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_ID_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_ID_WHILE_CREATING_HISTORY_RECORD
            .substring(0, ExceptionConstants.NULL_ID_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail4() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, "id", null,
            "created", "creator", "description", "released", "releaser",
                element, "branch", 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_REVISION_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_REVISION_WHILE_CREATING_HISTORY_RECORD
            .substring(0, ExceptionConstants.NULL_REVISION_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail5() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, "id", "rev",
            null, "creator", "description", "released", "releaser",
                element, "branch", 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_CREATION_DATE_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_CREATION_DATE_WHILE_CREATING_HISTORY_RECORD
            .substring(0, ExceptionConstants.NULL_CREATION_DATE_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail6() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, "id", "rev",
            "created", null, "description", "released", "releaser",
                element, "branch", 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_CREATOR_ID_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_CREATOR_ID_WHILE_CREATING_HISTORY_RECORD
            .substring(0, ExceptionConstants.NULL_CREATOR_ID_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail7() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, "id", "rev",
            "created", "creator", null, "released", "releaser",
                element, "branch", 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_DESCRIPTION_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_DESCRIPTION_WHILE_CREATING_HISTORY_RECORD
            .substring(0, ExceptionConstants.NULL_DESCRIPTION_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail8() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, "id", "rev",
            "created", "creator", "description", null, "releaser",
                element, "branch", 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_RELEASE_DATE_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_RELEASE_DATE_WHILE_CREATING_HISTORY_RECORD
            .substring(0, ExceptionConstants.NULL_RELEASE_DATE_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail9() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, "id", "rev",
            "created", "creator", "description", "released", null,
                element, "branch", 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_RELEASER_ID_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_RELEASER_ID_WHILE_CREATING_HISTORY_RECORD
            .substring(0, ExceptionConstants.NULL_RELEASER_ID_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail10() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, "id", "rev",
                "created", "creator", "description", "released", null,
                null, "branch", 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_RELEASE_AUTHORITY_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_RELEASE_AUTHORITY_WHILE_CREATING_HISTORY_RECORD
                .substring(0, ExceptionConstants.NULL_RELEASE_AUTHORITY_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail11() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, "id", "rev",
            "created", "creator", "description", "released", "releaser",
                element, null, 1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertFalse(message.contains(ExceptionConstants.NULL_BRANCH_NAME_WHILE_CREATING_HISTORY_RECORD)); //Should get trimmed
        assertTrue(message.contains(ExceptionConstants.NULL_BRANCH_NAME_WHILE_CREATING_HISTORY_RECORD
            .substring(0, ExceptionConstants.NULL_BRANCH_NAME_WHILE_CREATING_HISTORY_RECORD.length() - 2)));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail12() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(stereotype, pkg, "id", "rev",
            "created", "creator", "description", "released", "releaser",
                element, "branch", -1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertTrue(message.contains(ExceptionConstants.BAD_LATEST_REVISION_WHILE_CREATING_HISTORY_RECORD));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void revisionHistoryRecordCreationSanityCheck_Fail_multi() {
        Package pkg = mock(Package.class);
        String message = configuredElementDomain.revisionHistoryRecordCreationSanityCheck(null, pkg, "id", "rev",
            "created", "creator", "description", "released", "releaser",
                element, "branch", -1);

        assertFalse(message.isEmpty());
        assertTrue(message.startsWith(ExceptionConstants.PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX));
        assertTrue(message.contains(ExceptionConstants.NULL_STEREOTYPE_WHILE_CREATING_HISTORY_RECORD));
        assertTrue(message.contains(ExceptionConstants.BAD_LATEST_REVISION_WHILE_CREATING_HISTORY_RECORD));
        assertTrue(message.endsWith("."));
    }

    @Test
    public void getAllRecordsAffectingConfiguredElement() {
        ChangeRecord mismatch = mock(ChangeRecord.class);
        List<ChangeRecord> changeRecords = List.of(changeRecord, mismatch);

        when(configurationManagementService.getChangeRecords()).thenReturn(changeRecords);
        when(changeRecord.affectsGivenConfiguredElement(configuredElement)).thenReturn(true);
        when(mismatch.affectsGivenConfiguredElement(configuredElement)).thenReturn(false);

        List<ChangeRecord> results = configuredElementDomain.getAllRecordsAffectingConfiguredElement(configuredElement, configurationManagementService);
        assertNotNull(results);
        assertTrue(results.contains(changeRecord));
        assertFalse(results.contains(mismatch));
    }

    @Test
    public void getConfiguredElementUsingId_nullId() {
        assertNull(configuredElementDomain.getConfiguredElementUsingId(null, configurationManagementService));
    }

    @Test
    public void getConfiguredElementUsingId_nullElement() {
        String id = "id";

        when(apiDomain.getElementUsingId(id)).thenReturn(null);
        assertNull(configuredElementDomain.getConfiguredElementUsingId(id, configurationManagementService));
    }

    @Test
    public void getConfiguredElementUsingId_hasElement() {
        String id = "id";
        
        when(apiDomain.getElementUsingId(id)).thenReturn(element);
        when(configurationManagementService.getConfiguredElement(element)).thenReturn(configuredElement);
        Assert.assertEquals(configuredElement, configuredElementDomain.getConfiguredElementUsingId(id, configurationManagementService));
    }
}