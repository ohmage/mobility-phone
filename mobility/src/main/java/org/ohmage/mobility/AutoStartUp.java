package org.ohmage.mobility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.ohmage.mobility.activity.ActivityDetectionRequester;

/**
 * Start detection when the phone boots up if it was running
 */
public class AutoStartUp extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences(ActivityUtils.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);

        if (prefs.getBoolean(ActivityUtils.KEY_ACTIVITY_RUNNING, false)) {
            ActivityDetectionRequester activityDetectionRequester = new ActivityDetectionRequester(context);
            activityDetectionRequester.requestUpdates(prefs.getInt(ActivityUtils.KEY_ACTIVITY_INTERVAL, 1));
        }
    }
}
