package edu.ucla.cens.mobility;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

public class StopReceiver extends BroadcastReceiver
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
			Mobility.stop(context.getApplicationContext());
		}
		
		
	}

}
