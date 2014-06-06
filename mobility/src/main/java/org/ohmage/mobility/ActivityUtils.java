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

    // Shared Preferences repository name
    public static final String SHARED_PREFERENCES =
            "org.ohmage.mobility.activityrecognition.SHARED_PREFERENCES";

    // Key in the repository to show if intent service is registered
    public static final String KEY_RUNNING =
            "org.ohmage.mobility.activityrecognition.KEY_ACTIVITY_RUNNING";

    // Key in the repository to store the interval
    public static final String KEY_INTERVAL =
            "org.ohmage.mobility.activityrecognition.KEY_ACTIVITY_INTERVAL";

    public static final String ACTIVITY_STREAM_ID = "ba902741-3f4b-4909-a15a-f799ba36469b";

    public static final int ACTIVITY_STREAM_VERSION = 0;
}
