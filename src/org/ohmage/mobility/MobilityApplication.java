package org.ohmage.mobility;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import org.ohmage.logprobe.LogProbe;
import org.ohmage.logprobe.LogProbe.Loglevel;
import org.ohmage.probemanager.MobilityProbeWriter;

public class MobilityApplication extends Application {

    public static MobilityProbeWriter probeWriter;

	@Override
	public void onCreate()
	{
		super.onCreate();
		
        LogProbe.setLevel(true, Loglevel.VERBOSE);
        LogProbe.get(this);
        
        probeWriter = new MobilityProbeWriter(this);
        probeWriter.connect();

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

        probeWriter.close();
	}
}
