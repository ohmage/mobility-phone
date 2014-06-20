package org.ohmage.mobility;

import android.content.Context;

import com.google.android.gms.location.LocationRequest;

/**
 * Constants for the mobility app
 */
public final class ActivityUtils {

    // Used to track what type of request is in process
    public enum REQUEST_TYPE {
        ADD, REMOVE
    }

    public static final String APPTAG = "Mobility";

    public static long getIntervalMillis(Context context, int interval) {
        return context.getResources().getIntArray(R.array.interval_millis)[interval];
    }

    public static int getPriority(int priority) {
        switch(priority) {
            case 0: return LocationRequest.PRIORITY_HIGH_ACCURACY;
            case 1: return LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
            case 2: return LocationRequest.PRIORITY_LOW_POWER;
            case 3: return LocationRequest.PRIORITY_NO_POWER;
        }
        return LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    }

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Shared Preferences repository name
    public static final String SHARED_PREFERENCES =
            "org.ohmage.mobility.activityrecognition.SHARED_PREFERENCES";

    // Key in the repository to show if intent service is registered
    public static final String KEY_ACTIVITY_RUNNING =
            "org.ohmage.mobility.activityrecognition.KEY_ACTIVITY_RUNNING";

    public static final String KEY_LOCATION_RUNNING =
            "org.ohmage.mobility.activityrecognition.KEY_LOCATION_RUNNING";

    // Key in the repository to store the interval
    public static final String KEY_ACTIVITY_INTERVAL =
            "org.ohmage.mobility.activityrecognition.KEY_ACTIVITY_INTERVAL";

    public static final String KEY_LOCATION_INTERVAL =
            "org.ohmage.mobility.activityrecognition.KEY_LOCATION_INTERVAL";

    // Ket in the repository to store the location priority
    public static final String KEY_LOCATION_PRIORITY =
            "org.ohmage.mobility.activityrecognition.KEY_LOCATION_PRIORITY";

    public static final String ACTIVITY_STREAM_ID = "ba902741-3f4b-4909-a15a-f799ba36469b";

    public static final int ACTIVITY_STREAM_VERSION = 0;

    public static final String LOCATION_STREAM_ID = "8131a709-9342-47f8-b893-dcf9c824342c";

    public static final int LOCATION_STREAM_VERSION = 0;
}
