package org.openmbee.plugin.cfgmgmt.constants;

public class PluginConstant {
    private PluginConstant() {} // used as constants class, disallow constructor

    //TODO cleanup unused constants
    // Profiles
    public static final String CONFIGURATION_MANAGEMENT_PROJECTID = "PROJECT-44c8e19c-68b6-4612-b148-927c557a90cc";
    public static final String CONFIGURATION_MANAGEMENT_PROFILE = "Configuration Management Profile";

    // Stereotypes
    public static final String LIFECYCLE_STATUS_STEREOTYPE = "LifecycleStatus";
    public static final String CONFIGURED_ELEMENT_STEREOTYPE_PATH = "Configuration Management Profile::Stereotypes::ConfiguredElement";
    public static final String REVISION_HISTORY_STEREOTYPE = "RevisionHistoryRecord";
    public static final String REVISION_HISTORY_STEREOTYPE_PATH = "Configuration Management Profile::Stereotypes::RevisionHistoryRecord";
    public static final String CHANGE_RECORD_STEREOTYPE_PATH = "Configuration Management Profile::Stereotypes::ChangeRecord";
    public static final String LIFECYCLE_TRANSITION_STEREOTYPE = "LifecycleTransition";
    public static final String POLICY_STEREOTYPE = "Policy";
    public static final String THREEDX_CONNECTION_SETTINGS_STEREOTYPE = "3DxConnectionSettings";
    public static final String JIRA_CONNECTION_SETTINGS_STEREOTYPE = "JIRAConnectionSettings";
    public static final String CM_CUSTOM_SETTINGS_STEREOTYPE = "ConfigurationManagementPluginSettings";

    // Configured Element Properties
    public static final String ID = "id";
    public static final String STATUS = "status";
    public static final String REVISION = "revision";
    public static final String REVISION_CREATION_DATE = "revisionCreationDate";
    public static final String REVISION_CREATOR_ID = "revisionCreatorId";
    public static final String REVISION_RELEASE_DATE = "revisionReleaseDate";
    public static final String REVISION_RELEASER_ID = "revisionReleaserId";
    public static final String REVISION_RELEASE_AUTHORITY = "revisionReleaseAuthority";
    public static final String REVISION_HISTORY = "revisionHistory";
    public static final String DESCRIPTION = "description";
    public static final String DESCRIPTION_NEW_REVISION = "New revision";
    public static final String IS_COMMITTED = "isCommitted";
    public static final String LIFECYCLE = "lifecycle";
    public static final String NAME_PATTERN = "namePattern";
    public static final String COMMENTS = "comments";
    public static final String USE_CUSTOM_IDS = "useCustomIds";
    public static final String TICKET_FOUND = "ticketfound";

    // Configured Element Error Messages
    public static final String INVALID_STATUS_NAME = "Could not get current status name for Configured Element";
    public static final String INSUFFICIENT_PERMISSIONS = "Insufficient permissions.";
    public static final String INVALID_MATURITY_RATING = "Maturity Rating is invalid with respect to Ccz Owner";

    // General Properties
    public static final String EMPTY_STRING = "";
    public static final String LOCALE = "en";
    public static final String RELEVANCE = "relevance";
    public static final int ZERO = 0;
    public static final String COLON = ":";
    public static final String SLASH = "/";
    public static final String PACKAGE_DELIM = "::";
    public static final String COMMA = ",";
    public static final char COMMA_CHAR = ',';
    public static final String LOCALHOST_STRING = "localhost";
    public static final String PROTOCOL_HTTP = "http";
    public static final String PROTOCOL_HTTPS = "https";
    public static final String PORT = "8111";
    public static final String AUTHORIZATION = "Authorization";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_POST = "POST";
    public static final String HTTP_AGENT = "http.agent";
    public static final String JSON_CONTENT_TYPE = "application/json";
    public static final String PLAIN_TEXT_CONTENT_TYPE = "text/plain";
    public static final String AT_THE_RATE_CHARACTER = "@";
    public static final String ROLE_NAME = "name";
    public static final String ROLE_ID = "ID";
    public static final String USER = "user";
    public static final String TWC_AUTHORIZATION_TOKEN_PREFIX = "Token ";
    public static final String CAA_URL_LOADER = "CAA URL Loader";
    public static final String TLS = "TLS";
    public static final String ACCEPT = "Accept";
    public static final String PATCH = "PATCH";
    public static final String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36 Edg/79.0.100.0";

    // Change History Visibility Columns
    public static final String START_TIME_COLUMN = "startTimeColumn";
    public static final String COMPLETION_TIME_COLUMN = "completionTimeColumn";
    public static final String REVISION_COLUMN = "revisionColumn";
    public static final String CHANGE_RECORD_NAME_COLUMN = "changeRecordNameColumn";

    // Patterns
    public static final String TWC_ID_PATTERN = "(twcloud:\\/)(([a-z]|[0-9]|-)*)(\\/([a-z]|[0-9]|-)*)";
    public static final String TWC_URL_PATTERN = "%s://%s";
    public static final String OSMC_ROLES_PATH = "/osmc.admin.roles.json";
    public static final String OSMC_ROLES_URL_FROM_ADMIN = "/osmc/admin/users/%s/roles";
    public static final String OSMC_REVISION_DIFF = "/osmc/resources/%s/revisiondiff";
    public static final String OSMC_BATCH_ELEMENTS_AT_REVISION = "/osmc/resources/%s/revisions/%s/elements";
    public static final String OPEN_SQUARE_BRACE = "[";
    public static final String CLOSE_SQUARE_BRACE = "]";
    public static final String DISPLAY_NAME_PAIR_SPLIT = "\\.";
    public static final String DISPLAY_NAME_STRING_PATTERN = "%s";
    public static final String INTEGER_PATTERN = "^\\d+$";
    public static final String CE_NAME_AND_ID = "%s ( ID: %s )";
    public static final String TIME_DISPLAY_FORMAT = "%s %s";

    // TWC Messages
    public static final String FETCH_TWC_TOKEN = "Fetching TWC token";
    public static final String FETCH_TWC_USERNAME = "Fetching TWC username";
    public static final String LOGOUT_FROM_TWC = "Logging out from TWC";
    public static final String CHECK_PERMISSIONS = "Checking permissions";
    public static final String GETTING_REVISION_DIFF = "Getting revision difference";
    public static final String GETTING_ELEMENT_BATCH_AT_REVISION = "Getting batch of elements at requested revision";

    // TWC Integration
    public static final String LOGIN_ENDPOINT = "/osmc/login";
    public static final String LOGOUT_ENDPOINT = "/osmc/logout";
    public static final String TWC_USER_COOKIE = "twc-rest-current-user";
    public static final String PATH_ROLES = "/osmc/resources/%s/roles/%s";
    public static final String TWC_ROLE_ID = "roleID";
    public static final String PROTECTED_OBJECTS = "protectedObjects";
    public static final String RESOURCE_NAME = "resourceName";
    public static final String FILTER = "filter";
    public static final String RESOURCE_ID = "ID";
    public static final String SOURCE_REVISION = "source";
    public static final String TARGET_REVISION = "target";

    // Lifecycle Properties
    public static final String IS_READ_ONLY_STATUS = "isReadOnlyStatus";
    public static final String IS_RELEASED_STATUS = "isReleasedStatus";
    public static final String IS_EXPANDABLE_STATUS = "isExpandableStatus";
    public static final String IS_READY_FOR_RELEASE = "isReadyForRelease";
    public static final String ICON_ADORNMENT_COLOR = "iconAdornmentColor";
    public static final String MATURITY_RATING = "maturityRating";
    public static final String POLICY = "policy";
    public static final int MISSING_MATURITY_RATING = -99;

    // Lifecycle Properties Error Messages
    public static final String LIFECYCLE_PROPERTY_RETRIEVAL_FAILED = "Error retrieving lifecycle properties";
    public static final String INVALID_TYPE = "Invalid type in ";

    // Revision History Record Properties
    public static final String CONFIGURED_ELEMENT_PROPERTY_NAME = "configuredElement";
    public static final String MODEL_VERSION = "modelVersion";
    public static final String MODEL_BRANCH = "modelBranch";
    public static final String CONFIGURED_ELEMENT = "configuredElement";

    // Change Record Properties
    public static final String AFFECTED_ELEMENTS = "affectedElement";
    public static final String SOURCE = "source";
    public static final String SOURCE_ID = "sourceId";
    public static final String MANAGED_BY = "managedBy";
    public static final String CHANGE_DESCRIPTION_FORMAT = "%s - [%s] - %s - %s";

    // Element History Constants
    public static final String NO_TIME = "-";
    public static final String NO_REVISION_BUT_COMPLETED = "-";
    public static final String NO_REVISION_AND_NOT_COMPLETED = "*";
    public static final String REVISION_STATE_UNCLEAR = "~";
    public static final String ELEMENT_CHANGE_HISTORY_MATRIX_TITLE = "Change History Matrix";

    // Policy Properties
    public static final String ROLE = "role";

    // 3Dx Connection Settings
    public static final String URL_3DSPACE = "3DSpaceURL";
    public static final String ON_PREMISE = "OnPremise";
    public static final String THREEDSPACE = "3dspace";
    public static final String THREED_DESC = "desc";
    public static final String URL_3DSEARCH = "3DSearchURL";
    public static final String URL_3DPASSPORT = "3DPassportURL";
    public static final String CHANGE_ACTION_QUERY = "ChangeActionQuery";
    public static final String MISSING_3DX_CONNECTION_SETTING = "Missing 3Dx connection settings.";
    public static final String MULTIPLE_THREEDX_CONFIGURATIONS_WARNING = "More than one 3Dx configuration objects have been found! Using: [%s]";
    public static final String THREEDX_CONNECTION_SETTINGS_ERROR = "3Dx Connection Settings error";
    public static final String THREEDX_CONNECTION_SETTINGS_NOT_SET = "The 3Dx Connection settings element [%s] does not have all required properties set";
    public static final String NO_3DX_LOGIN_ESTABLISHED_OR_EXPIRED = "No 3Dx login established or login expired";
    public static final String FETCHING_CHANGE_ACTION = "Fetching Change Action";
    public static final String FETCHING_CHANGE_ACTIONS = "Fetching Change Actions";
    public static final String SECURITY_CONTEXT = "SecurityContext";
    public static final String THREEDX_SEARCH = "3DSearch-Cameo";
    public static final String THREEDX_CHANGE_ACTION_ENDPOINT = "/resources/v1/modeler/dslc/changeaction/%s";
    public static final String THREEDX_FEDERATED_SEARCH_ENDPOINT = "/federated/search";
    public static final String ALL_THREEDX_CHANGE_ACTIONS_PULLED_TITLE = "All Change Actions Pulled";
    public static final String ALL_THREEDX_CHANGE_ACTIONS_PULLED_MESSAGE = "All current change actions matching 3Dx " +
            "settings criteria have been pulled.\nIf you would like to reset, close this and the table and use the context menu again.";

    // JIRA Connection Settings
    public static final String JIRA_URL = "jiraURL";
    public static final String WSSO_URL = "WSSOURL";
    public static final String JIRA_REST_PATH = "jiraRESTPath";
    public static final String ISSUE_QUERY = "issueQuery";
    public static final String FIRST_CONFIG_OBJECT_FOUND = "The first configuration object found";
    public static final String MULTIPLE_JIRA_CONFIGURATIONS_WARNING = "More than one JIRA configuration objects have been found! Using: %s";
    public static final String JIRA_CONNECTION_SETTINGS_ERROR = "JIRA Connection Settings error";
    public static final String JIRA_CONNECTION_SETTINGS_NOT_SET = "%s does not have all required properties set";
    public static final String MISSING_JIRA_CONNECTION_SETTING = "Missing JIRA connection settings.";
    public static final String NO_JIRA_LOGIN_ESTABLISHED_OR_EXPIRED = "No JIRA login established or login expired";
    public static final String JIRA_CONNECTION_NOT_INITIALIZED = "JIRA connection settings not initialized";
    public static final String NO_JIRA_CONNECTION = "No JIRA connection";
    public static final String FETCHING_JIRA_ISSUES = "Fetching JIRA issues";
    public static final String FETCHING_JIRA_ISSUE = "Fetching JIRA issue";
    public static final String JIRA_SEARCH_ENDPOINT = "/search";
    public static final String JIRA_ISSUE_ENDPOINT = "/issue/";
    public static final String MULTIPLE_CONFIGURATIONS_WARNING = "Multiple configurations warning";
    public static final String ATLASSIAN_XSRF_TOKEN = "atlassian.xsrf.token";
    public static final String START_AT = "startAt";
    public static final String JQL = "jql";
    public static final String COOKIE = "Cookie";
    public static final String ISSUES = "issues";
    public static final String MAX_RESULTS = "maxResults";
    public static final int MAX_RESULTS_DEFAULT_VALUE = 50;
    public static final String TOTAL = "total";
    public static final String FIELDS = "fields";
    public static final String SUMMARY = "summary";
    public static final String CREATOR = "creator";
    public static final String TYPE = "type";
    public static final String ISSUETYPE = "issuetype";
    public static final String NAME = "name";
    public static final String DISPLAY_NAME = "displayName";
    public static final String ALL_JIRA_ISSUES_PULLED_TITLE = "All Issues Pulled";
    public static final String ALL_JIRA_ISSUES_PULLED_MESSAGE = "All current issues matching Jira " +
            "settings criteria have been pulled.\nIf you would like to reset, close this and the table and use the context menu again.";

    // CM Custom Settings
    public static final String CM_PACKAGE_PATH = "changeManagementPackageLocation";
    public static final String CM_DIAGRAM_ADORNMENT = "diagramAdornments";
    public static final String ENFORCE_ACTIVE_CR = "enforceCRActivationForAllChanges";
    public static final String AUTOMATE_RELEASE = "automateRelease";
    public static final String ADMIN_MODE = "adminModePolicy";

    // Packages
    public static final String CHANGE_RECORD_PACKAGE = "Change Records";
    public static final String REVISION_HISTORY_PACKAGE = "Revision History";

    // IDs
    public static final String CHANGE_RECORD_STATUS_ID = "_19_0_3_74001e9_1593643793619_257619_109";
    public static final String IS_RELEASED_STATUS_ID = "_19_0_3_74001e9_1590191469592_764273_14";
    public static final String CONFIGURED_ELEMENT_STEREOTYPE_ID = "_19_0_3_74001e9_1589240184242_167345_20";
    public static final String CHANGE_RECORD_STEREOTYPE_ID = "_19_0_3_74001e9_1589240601097_622635_8364";
    public static final String REVISION_HISTORY_STEREOTYPE_ID = "_19_0_3_74001e9_1590180407823_403735_47";
    public static final String THREEDX_CONNECTION_SETTINGS_STEREOTYPE_ID = "_19_0_3_74001e9_1602362553514_594399_16";
    public static final String JIRA_CONNECTION_SETTINGS_STEREOTYPE_ID = "_19_0_3_74001e9_1614059599797_320476_4982";
    public static final String CM_PLUGIN_SETTINGS_STEREOTYPE_ID = "_19_0_3_74001e9_1622004448678_421199_13655";
    public static final String CM_PROFILE_ID = "_19_0_3_74001e9_1589240087847_594092_3";

    // VALUES
    public static final String THREEDX_SOURCE = "3DX";
    public static final String JIRA_SOURCE = "JIRA";
    public static final String CAMEO = "Cameo";
    public static final String THREEDX = "3Dx";
    public static final String JIRA = "JIRA";
    public static final String MAGICDRAW = "MAGICDRAW";

    // ACTIONS
    public static final String CONFIGURING_ACTION = "Configuring element";
    public static final String REVISING_ACTION = "Revision %s Created";
    public static final String SELECT_TRANSITION_PROMPT_MESSAGE = "Select transition";
    public static final String SELECT_TRANSITION_PROMPT_TITLE = "Transition selection";
    public static final String AN_ERROR_OCCURRED = "An error occurred: [%s]";
    public static final String SELECT_CONFIGURED_ELEMENT_TYPE = "Select configured element type";
    public static final String CONFIGURED_ELEMENT_TYPE_SELECTION = "Configure element type selection";
    public static final String PLEASE_ENTER_THE_CONFIGURED_ELEMENT_ID = "Please enter the Configured Element ID for [%s]";
    public static final String SELECT_CR_TO_COMPARE = "Select a CR to view changes";

    // Manage Affected User Interface
    public static final String NO_LOCK_SERVICE_OR_NOT_LOCKED = "-";
    public static final String NO_LOCK_INFORMATION = "-";

    // Time Units
    public static final long EXPIRY_TIME_MILLISECONDS = 120000;
    public static final long SLEEP_TIME_MILLISECONDS = 250;
    public static final long LOWEST_SLEEP_TIME_MILLISECONDS = 100;

    // WSSO GENERIC
    public static final String LOGIN_TOKEN_ACQUIRED = "[CM] Login token acquired";
    public static final String CHROMIUM_CACHE = "ChromiumCache";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    //JIRA MESSAGES
    public static final String PULL_CHANGE_RECORD_ACTION_JIRA = "PULL_CHANGE_RECORD_ACTION_JIRA";
    public static final String PULL_CHANGE_RECORD_FROM_JIRA = "Pull change record from JIRA";
    public static final String SELECT_JIRA_ISSUES_TO_PULL = "Select the JIRA issue to pull";
    public static final String NO_ISSUE_WAS_SELECTED = "No issue was selected";
    public static final String JIRA_INTEGRATION = "Jira integration";
    public static final String LOAD_MORE = "Load more";
    public static final String LOAD_MORE_EXTENDED = "Load more...";

    //3DX MESSAGES
    public static final String THREE_DX_LOGIN = "3DX_LOGIN";
    public static final String LOGIN_TO_3DX = "Login to 3Dx";
}
