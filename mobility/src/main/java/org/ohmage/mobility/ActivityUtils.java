package org.ohmage.mobility;

/**
 * Constants for the mobility app
 */
public final class ActivityUtils {

    // Used to track what type of request is in process
    public enum REQUEST_TYPE {
        ADD, REMOVE
    }

    public static final String APPTAG = "Mobility";

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Intent actions and extras for sending information from the IntentService to the Activity
    public static final String ACTION_CONNECTION_ERROR =
            "org.ohmage.mobility.activityrecognition.ACTION_CONNECTION_ERROR";

    public static final String ACTION_REFRESH_STATUS_LIST =
            "org.ohmage.mobility.activityrecognition.ACTION_REFRESH_STATUS_LIST";

    public static final String CATEGORY_LOCATION_SERVICES =
            "org.ohmage.mobility.activityrecognition.CATEGORY_LOCATION_SERVICES";

    public static final String EXTRA_CONNECTION_ERROR_CODE =
            "org.ohmage.mobility.activityrecognition.EXTRA_CONNECTION_ERROR_CODE";

    public static final String EXTRA_CONNECTION_ERROR_MESSAGE =
            "org.ohmage.mobility.activityrecognition.EXTRA_CONNECTION_ERROR_MESSAGE";

    // Shared Preferences repository name
    public static final String SHARED_PREFERENCES =
            "org.ohmage.mobility.activityrecognition.SHARED_PREFERENCES";

    // Key in the repository to show if intent service is registered
    public static final String KEY_RUNNING =
            "org.ohmage.mobility.activityrecognition.KEY_RUNNING";

    // Key in the repository to store the interval
    public static final String KEY_INTERVAL =
            "org.ohmage.mobility.activityrecognition.KEY_INTERVAL";

    // Constants for constructing the log file name
    public static final String LOG_FILE_NAME_PREFIX = "activityrecognition";
    public static final String LOG_FILE_NAME_SUFFIX = ".log";

    // Keys in the repository for storing the log file info
    public static final String KEY_LOG_FILE_NUMBER =
            "org.ohmage.mobility.activityrecognition.KEY_LOG_FILE_NUMBER";
    public static final String KEY_LOG_FILE_NAME =
            "org.ohmage.mobility.activityrecognition.KEY_LOG_FILE_NAME";

    public static final String STREAM_ID = "ba902741-3f4b-4909-a15a-f799ba36469b";

    public static final int STREAM_VERSION = 0;
}
