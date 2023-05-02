package org.openmbee.plugin.cfgmgmt.constants;

public class ExceptionConstants {
    private ExceptionConstants() {} // used as constants class, disallow constructor

    // Generic
    public static final String GUI_LOG_MESSAGE = "[CM] Error: %s";
    public static final String INSUFFICIENT_PRIVILEGES = "Insufficient privileges. One of the following roles is required: [%s]";
    public static final String STATUS_CHANGE = "Status changed from %s to %s";
    public static final String LIFECYCLE_PROPERTY_NOT_ON_STATE_MACHINE = "The lifecycle property on the stereotype is not a state machine";
    public static final String PREPARE_STRING_LIST_FOR_EXCEPTION = "%n%s [%s]";
    public static final String PACKAGE_NOT_FOUND = "Package not found";
    public static final String CERTIFICATE_ERROR_MSG = "Catching certificate errors.";
    public static final int MESSAGE_CHARACTER_LIMIT = 2000;
    public static final int MESSAGE_LINE_LIMIT = 50;
    public static final String TRUNCATED_MESSAGE_APPEND = "...";
    public static final String FILE_NOT_DELETED = "File %s could not be deleted.";

    // Custom Settings
    public static final String MULTIPLE_CUSTOM_SETTINGS_FOUND_WARNING = "More than one custom CM settings objects have been found! Using: [%s]";

    // Rest
    public static final String POSTDATA_MISSING = "PostData Missing";
    public static final String HEADERS_MISSING = "Headers Missing";
    public static final String NULL_URL = "Url cannot be formed";

    // API Domain
    public static final String MERGE_UTIL_ERROR_TITLE = "Exception during Merge Attempt";

    // ChangeRecord Domain
    public static final String PROJECT_COMPARISON_IMPROPER_VERSION = "Invalid baseline or release version, " +
            "SelectedCR: %s, baseline-version: %s,  release-version: %s";
    public static final String VERSIONS_ARE_IDENTICAL = "Both baseline & release version are identical, " +
        "SelectedCR: %s, baseline-version: %s,  release-version: %s";
    public static final String MODEL_INCONSISTENT_WITH_UI = "Selected ChangeRecord with \"%s\" local id not found in " +
            "model, unable to display differences.";
    public static final String COMPARISON_REQUIRES_COMMIT_MESSAGE = "Possible uncommitted changes detected, in order" +
            " to get accurate results,\nmake sure there are no outstanding changes. If there are, please commit or" +
            " revert your changes.\n\nChanges can be checked in MSOSA from the Change Sets option from the Window" +
            " dropdown of the main toolbar.\n\nClick OK to continue anyway.";
    public static final String COMPARISON_REQUIRES_COMMIT_TITLE = "Comparison Requires Commit";

    // Configuration Management Service
    public static final String NO_ADMIN_POLICY = "No admin policy detected, it is required to enable admin mode.";

    // LifecycleObject Domain
    public static final String CANNOT_BE_PROMOTED = "Cannot be promoted since neither derived nor stereotype - baseCEStereotype or baseCRStereotype";
    public static final String ELEMENT_CANNOT_BE_EDITED_LOCKED = "This disables the action when the element is not editable, not locked and not new";
    public static final String NOT_AN_ELEMENT_INSTANCE = "Object not an instance of Element";
    public static final String CHANGERECORD_ERROR_MESSAGE = "Configuration Element does not have Available Transitions or ChangeRecord is not Released or Improper ChangeRecord";
    public static final String THROW_CONFIGURATION_MANAGEMENT_EXCEPTION = "Throwing ConfigurationManagementException with error :- ";

    // Configured Element Domain
    public static final String INCOMPATIBLE_CCZ_OWNER_STATUS = "Cannot configure element due to the status of the CCZ owner: %s[%s] (%s)";
    public static final String INVALID_MATURITY_RATING = "Cannot revise element due to the status of the CCZ owner: %s[%s], element status cannot be lower than its CCZ owner";
    public static final String PROMOTIONS_REQUIRED_FIRST = "There are owned configured elements that need to be promoted first:%s";
    public static final String NOT_NAMED_ELEMENT = "element object is not Named element";
    public static final String ELEMENT_ALREADY_PRESENT = "Element object is in change management package";
    public static final String ELEMENT_NOT_EDITABLE = "Element object is not editable";
    public static final String CM_PROFILE_NOT_ACTIVE = "CM Profile is not active";
    public static final String CR_NOT_SELECTED = "Change record is not selected";
    public static final String CR_NOT_EXPENDABLE = "Selected CR is not an expendable status";
    public static final String NO_AVAILABLE_STEREOTYPES = "No available stereotypes for this element type";
    public static final String CE_CONFIGURED = "Element is configured already";
    public static final String MULTIPLE_VALUES_PRESENT = "More than one values present for the revisionReleaseAuthority property";
    public static final String CE_NOT_CLASS_OBJECT = "configured Element is not a class object";
    public static final String PROBLEMS_WHILE_CREATING_HISTORY_RECORD_PREFIX = "Null or bad information found during attempt to create a revision history record.\nIssues found: ";
    public static final String NULL_STEREOTYPE_WHILE_CREATING_HISTORY_RECORD = "history stereotype, ";
    public static final String NULL_PACKAGE_WHILE_CREATING_HISTORY_RECORD = "revision history package, ";
    public static final String NULL_ID_WHILE_CREATING_HISTORY_RECORD = "id, ";
    public static final String NULL_REVISION_WHILE_CREATING_HISTORY_RECORD = "revision, ";
    public static final String NULL_CREATION_DATE_WHILE_CREATING_HISTORY_RECORD = "creation date, ";
    public static final String NULL_CREATOR_ID_WHILE_CREATING_HISTORY_RECORD = "creator id, ";
    public static final String NULL_DESCRIPTION_WHILE_CREATING_HISTORY_RECORD = "description, ";
    public static final String NULL_RELEASE_DATE_WHILE_CREATING_HISTORY_RECORD = "release date, ";
    public static final String NULL_RELEASER_ID_WHILE_CREATING_HISTORY_RECORD = "releaser id, ";
    public static final String NULL_RELEASE_AUTHORITY_WHILE_CREATING_HISTORY_RECORD = "release authority, ";
    public static final String NULL_BRANCH_NAME_WHILE_CREATING_HISTORY_RECORD = "branch name, ";
    public static final String BAD_LATEST_REVISION_WHILE_CREATING_HISTORY_RECORD = "latest revision is not a positive integer.";

    // LifecycleObject
    public static final String ELEMENT_LACKS_STATUS = "The element [%s] has no status applied";
    public static final String ERROR_DURING_SELF_CLEANING_STATUS_CANNOT_BE_FOUND_IN_LIFECYCLE = "[CM] Error while self cleaning status properties on element [%s]: Status [%s] cannot be found in the element's lifecycle";
    public static final String UNABLE_TO_IDENTIFY_STEREOTYPE = "Unable to identify applied stereotype on element [%s]";
    public static final String INVALID_STATE_MACHINE_AMOUNT = "Invalid amount of state machines found: ";
    public static final String NO_STATE_MACHINES = "No state machines found";
    public static final String ERROR_WHILE_CHANGING_STATUS = "Error while attempting to change the status of a lifecycle object";
    public static final String ERROR_WHILE_CHANGING_STATUS_SINGLE_OBJECT = "Error while changing the status of \"%s\", check the notification window for more details.";
    public static final String ERROR_DURING_BULK_CHANGE_STATUS = "Potential error during bulk change status, check all elements and/or records involved.";
    public static final String PROMOTION_FAILURE_CHANGE_STATUS_ACTION = "LifecycleObject Promotion Failure, check Notification Window for more details.";

    // LifecycleStatus
    public static final String ERROR_WHILE_GETTING_STATUS_NAME = "Error while attempting to get the name of the lifecycle status";
    public static final String LIFECYCLE_STATUS_ERROR_TITLE = "Lifecycle status error";

    // ConfiguredElement
    public static final String UNACCEPTABLE_STATUS_COMPARED_TO_CR = "An element cannot be at a status lower than the affecting Change Record (%s)";
    public static final String CANNOT_REVISE_DUE_TO_STATUS_ISSUE = "The configured element's status could not be changed, unable to revise";
    public static final String CONFIGURED_ELEMENT_PERMISSIONS_FAILURE = "For element \"%s\" there is either a lack of permissions or a bad maturity level compared to the owner of this element. Check the notification window for more details.";
    public static final String CONFIGURED_ELEMENT_PERMISSIONS_FAILURE_TITLE = "ConfiguredElement Permissions Failure";
    public static final String BULK_CONFIGURED_ELEMENT_REVISE_FAILURE = "Configured element revision failure";
    public static final String ERROR_WHILE_CONFIGURING_ELEMENT = "Error while attempting to configure element";
    public static final String ERROR_DURING_BULK_CONFIGURE = "Potential error during bulk configure, check the notification window and check all elements involved.";
    public static final String ERROR_WHILE_REVISING_ELEMENT = "Error occurred while revising element \"%s\".";
    public static final String ERROR_DURING_SINGLE_REVISE_SUFFIX = " Check the notification window for more information";
    public static final String ERROR_DURING_BULK_REVISE = "Potential error during bulk revision, check the notification window and check all elements involved.";

    // ChangeRecord
    public static final String NO_RELEASING_TRANSITION_FOUND = "Invalid Configured Element Lifecycle. No releasing transition found for: %s (%s)";
    public static final String CHANGE_IN_INCONSISTENT_STATE = "Error occurred while releasing the configured elements. The change is now in an inconsistent state.";
    public static final String AFFECTED_NOT_READY_FOR_RELEASE_PREFIX = "There are affected elements that are not ready for release:";
    public static final String AFFECTED_NEED_PROMOTION_PREFIX = "There are affected elements that need to be promoted first:";
    public static final String CHANGE_RECORDS_PACKAGE_MISSING = "Change management package could not be found";
    public static final String ACTIVATE_CHANGE_RECORD_BEFORE_CHANGING_STATUS = "Activate a change record before changing status.";

    // TeamworkCloudService
    public static final String INDETERMINATE_TWC_ROLES = "Could not determine TWC roles";
    public static final String TWC_USERNAME_MISMATCH = "Username mismatch between api calls, CM plugin unable to log in. This does not affect your normal TWC login.";
    public static final String ERROR_WHILE_CONNECTING_TO_TWC = "Error occurred while connected to TWC";
    public static final String ERROR_LOGGING_INTO_TWC = "Error during CM plugin's login attempt to TWC. This does not affect your normal TWC login.";
    public static final String ERROR_LOGGING_OUT_OF_TWC = "Error during CM plugin's logout from TWC. This does not affect your normal TWC login.";
    public static final String UNABLE_TO_DETERMINE_USER = "Unable to determine user name.";
    public static final String UNABLE_TO_FIND_PROJECT_URI = "Unable to find project URI";
    public static final String ERROR_WHILE_LOADING_URL = "Error occurred while attempting to load url%n%s";
    public static final String UNABLE_TO_GET_TWC_REVISION = "Error occurred while attempting to get requested revision.";

    // TwcRevisionService
    public static final String NO_STEREOTYPES_FOUND = "CM stereotypes not found at either TWC revision %s or %s, and therefore comparison cannot occur.";
    public static final String NO_STEREOTYPES_FOUND_TITLE = "Missing Stereotypes at TWC Revision";
    public static final String UNABLE_TO_RETRIEVE_ELEMENTS_AT_REVISION = "Unable to retrieve elements at revision %s";
    public static final String EXCEPTION_WHILE_GATHERING_DATA_TITLE = "Error gathering data for revision comparison";

    // Actions
    public static final String TRANSITION_FAILURE = "Lifecycle transition failure";
    public static final String ACTION_STATE_FAILURE = "Action state failure";
    public static final String CHANGE_STATUS_ACTION_UPDATE_FAILURE = "Could not update state in ChangeStatusAction, forcing disable";
    public static final String SYNCHRONIZATION_ERROR = "Synchronization Error";

    // Integration
    public static final String THREEDX_CONNECTION_ISSUE = "An issue occurred while pulling 3Dx Change Records:%n%s";
    public static final String THREEDX_ERROR_GETTING_CHANGE_ACTION = "Error getting 3Dx Change Action:%n%s";
    public static final String THREEDX_ERROR_GETTING_CHANGE_ACTIONS = "Error getting 3Dx Change Actions:%n%s";
    public static final String THREEDX_INVALID_CREDENTIALS = "Invalid 3Dx credentials. Please login to 3Dx and select a collaboration spaces first.";
    public static final String THREEDX_KEY_COLLISION_TITLE = "Key Collision Detected";
    public static final String THREEDX_KEY_COLLISION = "Key collision detected, discarding the following key-value pairs: %s";
    public static final String THREEDX_KEY_COLLISION_PAIR = "%nk: '%s' v: '%s'";
    public static final String JIRA_ERROR_GETTING_ISSUE = "Error getting JIRA Issue:%n%s";
    public static final String SYNC_ACTION_CONFIGURED_ELEMENTS_NOT_READY = "Potential error during synchronization of change records, check the notification window and check all elements involved.";
    public static final String URL_HAS_CHANGED = "URL has changed";
    public static final String NO_JIRA_ISSUES_CAN_BE_FETCHED = "No Jira Issues can be fetched";
    public static final String NO_ISSUES_FOUND_MATCHING_THE_SEARCH_CRITERIA = "No issues found matching the search criteria";
    public static final String ERROR_WHILE_PULLING_JIRA_ISSUES = "Error while pulling jira issues";

    // Element History
    public static final String NO_CHANGE_RECORDS_FOUND = "No change records found for \"%s\"";
    public static final String NO_CHANGE_RECORDS_FOUND_TITLE = "Broken Element History";
    public static final String DATE_TIME_PARSING_ISSUE = "Exception while parsing %s";
    public static final String DATE_TIME_PARSING_ISSUE_TITLE = "Date Parsing Exception";
    public static final String REVISION_HISTORY_RELEASE_DATE = "the revision release date from %s.";
    public static final String REVISION_HISTORY_CREATION_DATE = "the revision creation date from %s.";
    public static final String REVISION_HISTORY_RECORD_NOT_FOUND = "Missing Revision History Record detected, if a " +
            "ChangeRecord in this list seems to have missing data:\nCreate another CR, activate it, revise the" +
            " associated ConfiguredElement, and then look at history again.";
    public static final String REVISION_HISTORY_RECORD_NOT_FOUND_TITLE = "Revision History Record Not Found";
    public static final String IO_EXCEPTION_ERROR_MESSAGE = "IOException occurred {}";
    public static final String JAVAFX_ILLEGAL_STATE_MESSAGE = "Not on FX application thread; currentThread = ";
    public static final String JAVAFX_THREAD_EXCEPTION = "No JavaFX thread running when expected: {}";
    public static final String ERROR_DURING_FXML_CONSTRUCTOR_INSTANTIATION = "Error while instantiating CM FXML controller:  %s";

    // Json
    public static final String INAPPROPRIATE_JSON = "Json inappropriate for deserialization.";
}
