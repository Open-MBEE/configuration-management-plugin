package org.openmbee.plugin.cfgmgmt.domain;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.TypeFilter;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TestUIDomain {
    private UIDomain uiDomain;
    private NamedElement namedElement;
    private ConfiguredElementDomain configuredElementDomain;
    private ConfigurationManagementService configurationManagementService;

    @Before
    public void setup() {
        uiDomain = Mockito.spy(new UIDomain());
        namedElement = mock(NamedElement.class);
        configuredElementDomain = mock(ConfiguredElementDomain.class);
        configurationManagementService = mock(ConfigurationManagementService.class);
    }

    @Test
    public void prepareMessageForDisplay_noLimitReached() {
        String message = "message";

        assertEquals(message, uiDomain.prepareMessageForDisplay(message));
    }

    @Test
    public void prepareMessageForDisplay_lineLimitReached() {
        StringBuilder message = new StringBuilder();

        for(int i = 0; i < ExceptionConstants.MESSAGE_LINE_LIMIT; i++) {
            if(i == ExceptionConstants.MESSAGE_LINE_LIMIT - 1) {
                message.append(i);
            } else {
                message.append(i).append('\n');
            }
        }
        String expected = message + ExceptionConstants.TRUNCATED_MESSAGE_APPEND;
        message.append('\n').append("extra");

        assertEquals(expected, uiDomain.prepareMessageForDisplay(message.toString()));
    }

    @Test
    public void prepareMessageForDisplay_characterLimitReached() {
        StringBuilder message = new StringBuilder();

        for(int i = 0; i < ExceptionConstants.MESSAGE_CHARACTER_LIMIT; i++) {
            if(i == ExceptionConstants.MESSAGE_CHARACTER_LIMIT - 1) {
                message.append(i);
            } else {
                message.append(i).append(',');
            }
        }
        String expected = message.substring(0, ExceptionConstants.MESSAGE_CHARACTER_LIMIT) + ExceptionConstants.TRUNCATED_MESSAGE_APPEND;

        assertEquals(expected, uiDomain.prepareMessageForDisplay(message.toString()));
    }

    @Test
    public void prepareMessageForDisplay_lineLimitReachedThenCharacterLimitReached() {
        StringBuilder message = new StringBuilder();

        for(int i = 0; i < ExceptionConstants.MESSAGE_CHARACTER_LIMIT; i++) {
            if(i == ExceptionConstants.MESSAGE_CHARACTER_LIMIT - 1) {
                message.append(i);
            } else {
                message.append(i).append(',');
            }
        }

        String expected = message.substring(0, ExceptionConstants.MESSAGE_CHARACTER_LIMIT) + ExceptionConstants.TRUNCATED_MESSAGE_APPEND;

        for(int i = 0; i < ExceptionConstants.MESSAGE_LINE_LIMIT; i++) {
            if(i == ExceptionConstants.MESSAGE_LINE_LIMIT - 1) {
                message.append(i);
            } else {
                message.append(i).append('\n');
            }
        }
        message.append("extra").append('\n');

        assertEquals(expected, uiDomain.prepareMessageForDisplay(message.toString()));
    }

    @Test
    public void createElementSelectionDialog_FromReviseBulkAction() {
        List<Class> subtypes = new ArrayList<>();
        subtypes.add(NamedElement.class);
        SelectElementTypes selectElementTypes = spy(new SelectElementTypes(subtypes, subtypes, null, subtypes));
        Collection<NamedElement> candidates = new ArrayList<>();
        candidates.add(namedElement);
        TypeFilter filter = mock(TypeFilter.class);
        ElementSelectionDlg selectionDlg = mock(ElementSelectionDlg.class);
        SelectElementInfo selectElementInfo = mock(SelectElementInfo.class);

        doReturn(subtypes).when(configurationManagementService).getSubtypesFromNamedElement();
        doReturn(selectElementTypes).when(uiDomain).createSelectElementTypes(subtypes);
        doReturn(candidates).when(configurationManagementService).getSelectionCandidatesForRevision();
        doReturn(filter).when(uiDomain).createTypeFilter(subtypes, candidates);
        doReturn(selectionDlg).when(uiDomain).createSelectionDialog();
        doReturn(selectElementInfo).when(uiDomain).createSelectElementInfo();
        doNothing().when(uiDomain).initializeDialog(selectElementTypes, filter, filter, selectionDlg, selectElementInfo);

        ElementSelectionDlg result = uiDomain.createElementSelectionDialog(configurationManagementService);

        assertNotNull(result);
        assertEquals(selectionDlg, result);
    }

    @Test
    public void createElementSelectionDialog_FromConfigureBulkAction() {
        List<Class> subtypes = new ArrayList<>();
        subtypes.add(NamedElement.class);
        SelectElementTypes selectElementTypes = spy(new SelectElementTypes(subtypes, subtypes, null, subtypes));
        Collection<NamedElement> candidates = new ArrayList<>();
        candidates.add(namedElement);
        TypeFilter filter = mock(TypeFilter.class);
        ElementSelectionDlg selectionDlg = mock(ElementSelectionDlg.class);
        SelectElementInfo selectElementInfo = mock(SelectElementInfo.class);

        doReturn(subtypes).when(configurationManagementService).getSubtypesFromNamedElement();
        doReturn(selectElementTypes).when(uiDomain).createSelectElementTypes(subtypes);
        doReturn(candidates).when(configurationManagementService).getSelectionCandidatesForConfiguration();
        doReturn(filter).when(uiDomain).createTypeFilter(subtypes, candidates);
        doReturn(selectionDlg).when(uiDomain).createSelectionDialog();
        doReturn(selectElementInfo).when(uiDomain).createSelectElementInfo();
        doNothing().when(uiDomain).initializeDialog(selectElementTypes, filter, filter, selectionDlg, selectElementInfo);

        ElementSelectionDlg result = uiDomain.createElementSelectionDialog(configuredElementDomain, configurationManagementService);

        assertNotNull(result);
        assertEquals(selectionDlg, result);
    }
}
