package org.ohmage.mobility;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.ohmage.logprobe.LogProbe;
import org.ohmage.logprobe.LogProbe.Loglevel;

public class MobilityApplication extends Application
{

	@Override
	public void onCreate()
	{
		super.onCreate();
		
        LogProbe.setLevel(true, Loglevel.VERBOSE);
        LogProbe.get(this);
        
		SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
		if (settings.getBoolean(MobilityControl.MOBILITY_ON, false))
		{
			Mobility.start(this);
		}
		
	}

	@Override
	public void onTerminate() {
	    super.onTerminate();
	    LogProbe.close(this);
	}
}
