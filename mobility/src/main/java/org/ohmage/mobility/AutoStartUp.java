package org.ohmage.mobility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import org.ohmage.mobility.activity.ActivityDetectionRequester;
import org.ohmage.mobility.location.LocationDetectionRequester;

/**
 * Start detection when the phone boots up if it was running
 */
public class AutoStartUp extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences prefs = context.getSharedPreferences(ActivityUtils.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);

        Log.i("AutoStart", intent.toString());
        if (prefs.getBoolean(ActivityUtils.KEY_ACTIVITY_RUNNING, DefaultPreferences.ACTIVITY_RUNNING)) {

            int intervalPref = prefs.getInt(ActivityUtils.KEY_ACTIVITY_INTERVAL, DefaultPreferences.ACTIVITY_INTERVAL);
            long interval = ActivityUtils.getIntervalMillis(context, intervalPref);
            ActivityDetectionRequester ad = new ActivityDetectionRequester(context);
            ad.requestUpdates(interval);
        }

        if (prefs.getBoolean(ActivityUtils.KEY_LOCATION_RUNNING, DefaultPreferences.LOCATION_RUNNING)) {

            int intervalPref = prefs.getInt(ActivityUtils.KEY_LOCATION_INTERVAL, DefaultPreferences.LOCATION_INTERVAL);
            long interval = ActivityUtils.getIntervalMillis(context, intervalPref);
            int priorityPref = prefs.getInt(ActivityUtils.KEY_LOCATION_PRIORITY, DefaultPreferences.LOCATION_PRIORITY);
            int priority = ActivityUtils.getPriority(priorityPref);
            LocationDetectionRequester ld = new LocationDetectionRequester(context);
            ld.requestUpdates(interval, priority);
        }
        if(intent.getAction()!=null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            repeatingAutoStart(context);
        }
    }
    public static void repeatingAutoStart(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, AutoStartUp.class), PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }
}
