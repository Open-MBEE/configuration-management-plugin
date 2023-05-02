package org.openmbee.plugin.cfgmgmt.domain;

import org.openmbee.plugin.cfgmgmt.application.BaseModule;
import org.openmbee.plugin.cfgmgmt.application.ElementHistoryMatrixModule;
import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.ui.dialogs.MDDialogParentProvider;
import com.nomagic.magicdraw.ui.dialogs.SelectElementInfo;
import com.nomagic.magicdraw.ui.dialogs.SelectElementTypes;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlg;
import com.nomagic.magicdraw.ui.dialogs.selection.ElementSelectionDlgFactory;
import com.nomagic.magicdraw.ui.dialogs.selection.TypeFilter;
import com.nomagic.magicdraw.ui.dialogs.selection.TypeFilterImpl;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants.GUI_LOG_MESSAGE;

public class UIDomain {
    private static final Logger logger = LoggerFactory.getLogger(UIDomain.class);

    public boolean isOkOption(int option) {
        return option == JOptionPane.OK_OPTION;
    }

    public int promptForSelection(String promptMessage, String promptTitle, Object[] options) {
        return JOptionPane.showOptionDialog(null,
                promptMessage,
                promptTitle,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, 0);
    }

    public String askForInput(Object message) {
        return JOptionPane.showInputDialog(message);
    }

    public int askForConfirmation(Object message, String title) {
        return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    public void showPlainMessage(Object message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public void showWarningMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public void showErrorMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public void log(String message) {
        logger.info(message);
        Application.getInstance().getGUILog().log(message);
    }

    public void logError(String errorMessage) {
        logError(logger, errorMessage);
    }

    public void logError(Logger logger, String errorMessage) {
        String message = String.format(GUI_LOG_MESSAGE, errorMessage);
        logger.error(message);
        Application.getInstance().getGUILog().log(message);
    }

    public void logError(Logger logger, String errorMessage, Exception e) {
        String message = String.format(GUI_LOG_MESSAGE, errorMessage);
        logger.error(message, e);
        Application.getInstance().getGUILog().log(message);
    }

    public void logErrorAndShowMessage(Logger logger, String errorMessage, String title) {
        String message = String.format(GUI_LOG_MESSAGE, errorMessage);
        logger.error(message);
        Application.getInstance().getGUILog().log(message);
        showErrorMessage(errorMessage, title);
    }

    public void logErrorAndShowMessage(Logger logger, String errorMessage, String title, Exception e) {
        String message = String.format(GUI_LOG_MESSAGE, errorMessage);
        logger.error(message, e);
        Application.getInstance().getGUILog().log(message);
        showErrorMessage(errorMessage, title);
    }

    public void logDebug(String debugMessage) {
        logDebug(logger, debugMessage);
    }

    public void logDebug(Logger logger, String debugMessage) {
        String message = String.format(GUI_LOG_MESSAGE, debugMessage);
        logger.debug(message);
    }

    public void logDebug(Logger logger, String debugMessage, Exception e) {
        String message = String.format(GUI_LOG_MESSAGE, debugMessage);
        logger.debug(message, e);
    }

    protected String prepareMessageForDisplay(String message) {
        if(message.lines().count() > ExceptionConstants.MESSAGE_LINE_LIMIT) {
            List<String> lines = message.lines().limit(ExceptionConstants.MESSAGE_LINE_LIMIT).collect(Collectors.toList());
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < lines.size(); i++) {
                if(i == lines.size() - 1) {
                    builder.append(lines.get(i)).append(ExceptionConstants.TRUNCATED_MESSAGE_APPEND);
                } else {
                    builder.append(lines.get(i)).append('\n');
                }
            }
            return prepareMessageForDisplay(builder.toString());
        }
        if(message.length() > ExceptionConstants.MESSAGE_CHARACTER_LIMIT) {
            return message.substring(0, ExceptionConstants.MESSAGE_CHARACTER_LIMIT) + ExceptionConstants.TRUNCATED_MESSAGE_APPEND;
        }

        return message;
    }

    //*** Element Selection Dialog ***

    public ElementSelectionDlg createElementSelectionDialog(ConfigurationManagementService configurationManagementService) {
        // intended for use with ReviseBulkAction
        return createElementSelectionDialogHelper(null, configurationManagementService);
    }

    public ElementSelectionDlg createElementSelectionDialog(ConfiguredElementDomain configuredElementDomain,
                                                            ConfigurationManagementService configurationManagementService) {
        // intended for use with ConfigureBulkAction
        return createElementSelectionDialogHelper(configuredElementDomain, configurationManagementService);
    }

    protected ElementSelectionDlg createElementSelectionDialogHelper(ConfiguredElementDomain configuredElementDomain,
                                                                     ConfigurationManagementService configurationManagementService) {
        // Only properties and their subtypes are offered to select.
        List<Class> types = configurationManagementService.getSubtypesFromNamedElement();
        SelectElementTypes selectElementTypes = createSelectElementTypes(types);

        // Available properties are filtered so that only the ones which start with 'p' are selected.
        Collection<NamedElement> candidates;
        if(configuredElementDomain == null) {
            candidates = configurationManagementService.getSelectionCandidatesForRevision();
        } else {
            candidates = configurationManagementService.getSelectionCandidatesForConfiguration();
        }

        TypeFilter selectableFilter = createTypeFilter(selectElementTypes.select, candidates);
        TypeFilter visibleFilter = createTypeFilter(selectElementTypes.display, candidates);

        ElementSelectionDlg selectionDlg = createSelectionDialog();
        SelectElementInfo selectElementInfo = createSelectElementInfo();
        // Gets elements which are initially selected in the dialog.
        initializeDialog(selectElementTypes, selectableFilter, visibleFilter, selectionDlg, selectElementInfo);

        return selectionDlg;
    }

    protected SelectElementTypes createSelectElementTypes(List<java.lang.Class> types) {
        return new SelectElementTypes(types, types, null, types);
    }

    protected TypeFilter createTypeFilter(Collection<?> itemTypes, Collection<NamedElement> candidates) {
        return new TypeFilterImpl(itemTypes) {
            @Override
            public boolean accept(BaseElement baseElement, boolean checkType) {
                return super.accept(baseElement, checkType) && candidates.contains(baseElement);
            }
        };
    }

    protected ElementSelectionDlg createSelectionDialog() {
        return ElementSelectionDlgFactory
                .create(MDDialogParentProvider.getProvider().getDialogOwner(), "Select elements to configure", null);
    }

    protected SelectElementInfo createSelectElementInfo() {
        return new SelectElementInfo(true, false, null, true);
    }

    protected void initializeDialog(SelectElementTypes selectElementTypes, TypeFilter selectableFilter, TypeFilter visibleFilter,
                                    ElementSelectionDlg selectionDlg, SelectElementInfo selectElementInfo) {
        ElementSelectionDlgFactory.initMultiple(selectionDlg, selectElementInfo, visibleFilter, selectableFilter,
                selectElementTypes.usedAsTypes, selectElementTypes.create, new ArrayList<>());
    }

    public void initializeJavafxThread() {
        if (!Platform.isFxApplicationThread()) {
            new JFXPanel();
            Platform.setImplicitExit(false);
        }
    }

    public BaseModule getElementHistoryMatrixModule() {
        return new ElementHistoryMatrixModule();
    }
}
