package edu.ucla.cens.mobility;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

public class UpdateReceiver extends BroadcastReceiver
{

	public static final String TAG = "UPDATE_RECEIVER";

	@Override
	public void onReceive(final Context context, Intent intent)
	{
		Log.i(TAG, "Mobility updated");
		
		// start components
		SharedPreferences settings = context.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
		if (settings.getBoolean(MobilityControl.MOBILITY_ON, false))
		{
//			Intent MobilityServiceIntent = new Intent(context, MobilityService.class);
//			context.stopService(MobilityServiceIntent);
//			context.startService(MobilityServiceIntent);
//			context.startService(new Intent(context, Mobility.class));
//			Mobility.stop(context.getApplicationContext());
			Mobility.start(context.getApplicationContext());
			
		}
		
		
	}

}
