package org.ohmage.mobility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Start detection when the phone boots up if it was running
 */
public class AutoStartUp extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences(ActivityUtils.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);

        if (prefs.getBoolean(ActivityUtils.KEY_RUNNING, false)) {
            DetectionRequester detectionRequester = new DetectionRequester(context);
            detectionRequester.requestUpdates(prefs.getInt(ActivityUtils.KEY_INTERVAL, 1));
        }
    }
}
