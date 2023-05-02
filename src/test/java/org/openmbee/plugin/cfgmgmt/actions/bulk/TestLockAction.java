package org.openmbee.plugin.cfgmgmt.actions.bulk;

import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class TestLockAction {
    private ApiDomain apiDomain;
    private LockAction lockAction;
    private ConfiguredElement configuredElement;
    private ActionEvent actionEvent;

    @Before
    public void setup() {
        apiDomain = mock(ApiDomain.class);
        lockAction = Mockito.spy(new LockAction(apiDomain));
        configuredElement = mock(ConfiguredElement.class);
        actionEvent = mock(ActionEvent.class);

        when(lockAction.getApiDomain()).thenReturn(apiDomain);
    }

    @Test
    public void setEnabledTest_nullParameter() {
        doNothing().when(lockAction).setEnabled(false);
        lockAction.setEnabled(null);
        verify(lockAction).setEnabled(false);
    }

    @Test
    public void setEnableTest_noConfiguredElements() {
        List<ConfiguredElement> configuredElements = new ArrayList<>();
        doNothing().when(lockAction).setEnabled(false);
        lockAction.setEnabled(configuredElements);
        verify(apiDomain, never()).isAnyLocked(configuredElements);
    }

    @Test
    public void setEnabled_somethingAlreadyLocked() {
        List<ConfiguredElement> configuredElements = new ArrayList<>();
        configuredElements.add(configuredElement);
        doReturn(true).when(apiDomain).isAnyLocked(configuredElements);
        doNothing().when(lockAction).setEnabled(false);
        lockAction.setEnabled(configuredElements);
        verify(lockAction).setEnabled(false);
    }

    @Test
    public void setEnabled_nothingAlreadyLocked() {
        List<ConfiguredElement> configuredElements = new ArrayList<>();
        configuredElements.add(configuredElement);
        doReturn(false).when(apiDomain).isAnyLocked(configuredElements);
        doNothing().when(lockAction).setEnabled(true);
        lockAction.setEnabled(configuredElements);
        verify(lockAction).setEnabled(true);
    }

    @Test
    public void actionPerformed() {
        List<ConfiguredElement> configuredElements = new ArrayList<>();
        ConfiguredElement configuredElement1 = mock(ConfiguredElement.class);
        ConfiguredElement configuredElement2 = mock(ConfiguredElement.class);
        configuredElements.add(configuredElement1);
        configuredElements.add(configuredElement2);
        doNothing().when(apiDomain).lock(configuredElements);
        lockAction.setEnabled(configuredElements);
        lockAction.actionPerformed(actionEvent);
        verify(apiDomain).lock(configuredElements);
    }
}
