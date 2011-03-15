package edu.ucla.cens.mobility;

import edu.ucla.cens.accelservice.IAccelService;
import edu.ucla.cens.wifigpslocation.IWiFiGPSLocationService;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class BootService extends Service
{
	
	private static final String TAG = "BootService";
	public static final String bound = "edu.ucla.cens.mobility.bound";
	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
		registerReceiver(bindingReceiver, new IntentFilter(bound));
//		Mobility.start(this, this);
//		stopSelf(); // want to stop service after start command received by wifigps/accel
	}
	
	
	
	BroadcastReceiver bindingReceiver = new BroadcastReceiver() // put this sucker in its own file and register in manifest
	{

		@Override
		public void onReceive(Context context, Intent intent)
		{
			stopSelf();
		}
	};
	

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		// TODO Auto-generated method stub
		return null;
	}


}
