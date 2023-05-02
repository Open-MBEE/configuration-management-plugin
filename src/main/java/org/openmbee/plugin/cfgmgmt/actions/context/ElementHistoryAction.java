package org.openmbee.plugin.cfgmgmt.actions.context;

import org.openmbee.plugin.cfgmgmt.constants.ExceptionConstants;
import org.openmbee.plugin.cfgmgmt.constants.PluginConstant;
import org.openmbee.plugin.cfgmgmt.controller.AbstractCmDispatcher;
import org.openmbee.plugin.cfgmgmt.domain.ApiDomain;
import org.openmbee.plugin.cfgmgmt.domain.ChangeRecordDomain;
import org.openmbee.plugin.cfgmgmt.domain.ConfiguredElementDomain;
import org.openmbee.plugin.cfgmgmt.domain.UIDomain;
import org.openmbee.plugin.cfgmgmt.factory.LifecycleObjectFactory;
import org.openmbee.plugin.cfgmgmt.model.ChangeRecord;
import org.openmbee.plugin.cfgmgmt.model.CmControllerSettings;
import org.openmbee.plugin.cfgmgmt.model.ConfiguredElement;
import org.openmbee.plugin.cfgmgmt.model.RevisionHistoryRecord;
import org.openmbee.plugin.cfgmgmt.service.ConfigurationManagementService;
import org.openmbee.plugin.cfgmgmt.ui.IConfigurationManagementUI;
import org.openmbee.plugin.cfgmgmt.view.ElementHistoryRowView;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ElementHistoryAction extends DefaultBrowserAction {
	private static final Logger logger = LoggerFactory.getLogger(ElementHistoryAction.class);
	private final transient ConfigurationManagementService configurationManagementService;
	private final transient AbstractCmDispatcher cmDispatcher;

	public ElementHistoryAction(ConfigurationManagementService configurationManagementService, AbstractCmDispatcher cmDispatcher) {
		super("ELEMENT_HISTORY_ACTION", "Element History", null, null);
		this.configurationManagementService = configurationManagementService;
		this.cmDispatcher = cmDispatcher;
	}

	protected Logger getLogger() {
		return logger;
	}

	protected ConfigurationManagementService getConfigurationManagementService() {
		return configurationManagementService;
	}

	protected ApiDomain getApiDomain() {
		return getConfigurationManagementService().getApiDomain();
	}

	protected LifecycleObjectFactory getLifecycleObjectFactory() {
		return getConfigurationManagementService().getLifecycleObjectFactory();
	}

	protected ConfiguredElementDomain getConfiguredElementDomain() {
		return getConfigurationManagementService().getConfiguredElementDomain();
	}

	protected ChangeRecordDomain getChangeRecordDomain() {
		return getConfigurationManagementService().getChangeRecordDomain();
	}

	protected UIDomain getUIDomain() {
		return getConfigurationManagementService().getUIDomain();
	}

	protected AbstractCmDispatcher getCmDispatcher() {
		return cmDispatcher;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		ConfiguredElement configuredElement = getConfiguredElement();
		if(configuredElement == null) {
			return;
		}

		// all revision records that have the selected configured element as their configured element
		List<RevisionHistoryRecord> relatedRevisionRecords = getConfigurationManagementService().getAllRevisionHistoryRecords();
		relatedRevisionRecords = relatedRevisionRecords.stream().filter(r -> {
			ConfiguredElement ceFromRecord = r.getConfiguredElement();
			if(ceFromRecord == null) {
				// enables backwards compatibility if configured element property is null
				r.setConfiguredElement(configuredElement);
				return true;
			}
			return ceFromRecord.equals(configuredElement);
		}).collect(Collectors.toList());
		// get each revision record's change record entry and any remaining have or are affecting the configured element
		List<ChangeRecord> relevantChangeRecords = getConfiguredElementDomain().getRelevantChangeRecords(configuredElement,
			getConfigurationManagementService());
		// sort the change records using the tools available
		getChangeRecordDomain().sortChangeRecordsByReleaseStatusAndTime(relevantChangeRecords, relatedRevisionRecords);
		// prepare results for the user interface
		getChangeRecordDomain().determineRevisionHistoryRecordInterleaving(relatedRevisionRecords);
		// populate user interface with data
		getCmDispatcher().invokeCmController(getCmControllerSettings(configuredElement,
			generateElementHistoryRows(configuredElement, relatedRevisionRecords, relevantChangeRecords),
			relevantChangeRecords), getUIDomain().getElementHistoryMatrixModule());
	}

	protected ConfiguredElement getConfiguredElement() {
		Object selected = getSelectedObjectOverride();
		if (selected instanceof Element) {
			return getConfigurationManagementService().getConfiguredElement((Element) selected);
		}
		return null;
	}

	protected List<ElementHistoryRowView> generateElementHistoryRows(ConfiguredElement configuredElement,
			List<RevisionHistoryRecord> relatedRevisionRecords, List<ChangeRecord> relevantChangeRecords) {
		List<ElementHistoryRowView> rows = new ArrayList<>();
		for (ChangeRecord changeRecord : relevantChangeRecords) {
			List<RevisionHistoryRecord> revisionHistoryRecordList = getRevisionHistoryRecordFromChangeRecord(relatedRevisionRecords, changeRecord);
			ElementHistoryRowView row;
			for (RevisionHistoryRecord revisionHistoryRecord : revisionHistoryRecordList) {
				row = setupRowView(revisionHistoryRecord);
				if(row != null) {
					if (revisionHistoryRecord.isInterleavedWithAnotherRevision()) {
						row.showRevisionStateIsUnclear();
					}
					rows.add(row);
				}
			}
			if (revisionHistoryRecordList.isEmpty()) {
				rows.add(setupRowView(changeRecord, configuredElement));
			}
		}

		return rows;
	}

	protected CmControllerSettings getCmControllerSettings(ConfiguredElement configuredElement,
			List<ElementHistoryRowView> rows, List<ChangeRecord> relevantChangeRecords) {
		return new CmControllerSettings(configuredElement.getElement().getLocalID(), configuredElement.getID(),
				configuredElement.getName(), PluginConstant.ELEMENT_CHANGE_HISTORY_MATRIX_TITLE, rows,
				relevantChangeRecords,
				getConfigurationManagementService(), IConfigurationManagementUI.ConfigurationManagementUiType.ELEMENT_CHANGE_HISTORY_MATRIX);
	}

	protected List<RevisionHistoryRecord> getRevisionHistoryRecordFromChangeRecord(List<RevisionHistoryRecord> revisionHistoryRecords, ChangeRecord changeRecord) {
		List<RevisionHistoryRecord> revisionRecordForGivenCR = new ArrayList<>();
		for(RevisionHistoryRecord revisionHistoryRecord : revisionHistoryRecords) {
			if(revisionHistoryRecord.getRevisionReleaseAuthority() != null && revisionHistoryRecord.getRevisionReleaseAuthority().equals(changeRecord)) {
				revisionRecordForGivenCR.add(revisionHistoryRecord);
			}
		}
		return revisionRecordForGivenCR;
	}

	protected ElementHistoryRowView setupRowView(RevisionHistoryRecord revisionHistoryRecord) {
		if (revisionHistoryRecord != null && revisionHistoryRecord.getRevisionReleaseAuthority() != null) {
			return new ElementHistoryRowView(revisionHistoryRecord.getRevisionReleaseAuthority().getName(),
				getChangeRecordDomain().trimTimestamp(revisionHistoryRecord.getCreationDate(),
					String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, revisionHistoryRecord.getName())),
				getChangeRecordDomain().trimTimestamp(revisionHistoryRecord.getReleaseDate(),
					String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, revisionHistoryRecord.getName())),
				revisionHistoryRecord.getRevision(), revisionHistoryRecord.getRevisionReleaseAuthority().getElement().getLocalID());
		}
		return null;
	}

	protected ElementHistoryRowView setupRowView(ChangeRecord changeRecord, ConfiguredElement configuredElement) {
		// intended for edge cases where a revision history record does not exist for a change record
		if (changeRecord.getAffectedElements().contains(configuredElement) && changeRecord.isReleased()) {
			return new ElementHistoryRowView(changeRecord.getName(),
				getChangeRecordDomain().trimTimestamp(configuredElement.getRevisionCreationDate(),
					String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, configuredElement.getName())),
				getChangeRecordDomain().trimTimestamp(configuredElement.getRevisionReleaseDate(),
					String.format(ExceptionConstants.REVISION_HISTORY_RELEASE_DATE, configuredElement.getName())),
				configuredElement.getRevision(), changeRecord.getElement().getLocalID());
		}
		return new ElementHistoryRowView(changeRecord.getName(),
			getChangeRecordDomain().trimTimestamp(configuredElement.getRevisionCreationDate(),
				String.format(ExceptionConstants.REVISION_HISTORY_CREATION_DATE, configuredElement.getName())),
			PluginConstant.NO_TIME, PluginConstant.NO_REVISION_AND_NOT_COMPLETED, changeRecord.getElement().getLocalID());
	}

	protected Object getSelectedObjectOverride() {
		return getSelectedObject();
	}

	@Override
	public void updateState() {
		setEnabled(getConfiguredElement() != null);
	}
}
