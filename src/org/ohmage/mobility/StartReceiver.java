package org.ohmage.mobility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.ohmage.logprobe.Log;

public class StartReceiver extends BroadcastReceiver
{

	public static final String TAG = "StartReceiver";

	@Override
	public void onReceive(final Context context, Intent intent)
	{
		Log.v(TAG, "Mobility updated");
		
		// start components
		SharedPreferences settings = context.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
		if (!settings.getBoolean(MobilityControl.MOBILITY_ON, false))
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
