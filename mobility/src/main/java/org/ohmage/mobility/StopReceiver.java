package org.ohmage.mobility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.util.Log;

public class StopReceiver extends BroadcastReceiver
{

	public static final String TAG = "StopReceiver";

	@Override
	public void onReceive(final Context context, Intent intent)
	{
		Log.v(TAG, "Mobility updated");
		
		// start components
		SharedPreferences settings = context.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
		if (settings.getBoolean(MobilityControl.MOBILITY_ON, false))
		{
			Mobility.stop(context.getApplicationContext());
		}
		
		
	}

}
