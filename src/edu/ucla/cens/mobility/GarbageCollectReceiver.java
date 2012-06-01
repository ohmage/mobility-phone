package edu.ucla.cens.mobility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;



public class GarbageCollectReceiver extends BroadcastReceiver
{
	
	private static final String GC_START = "edu.ucla.cens.mobility.garbagecollect";
	private final static String TAG = "GarbageCollection";
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		Log.d(TAG, "Collect garbage");
		// Start service
		// For NW, fix this!
//		if (true)
//			return;
		WakefulIntentService.sendWakefulWork(context, GarbageCollectService.class);
	}

	
	public static void scheduleGC(Context context, AlarmManager mgr)
	{
		Intent gci = new Intent(GC_START);
		PendingIntent GCPI = PendingIntent.getBroadcast(context, 0, gci, PendingIntent.FLAG_CANCEL_CURRENT);
//		context.registerReceiver(GCReceiver, new IntentFilter(GC_START));
		mgr.setRepeating(AlarmManager.RTC_WAKEUP, 0, 24 * 3600 * 1000, GCPI);
	}
}
