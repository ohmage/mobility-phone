package edu.ucla.cens.mobility;

import android.app.AlarmManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.StateSet;
import edu.ucla.cens.accelservice.IAccelService;
import edu.ucla.cens.wifigpslocation.IWiFiGPSLocationService;

public class ServiceState
{
	public static boolean initialized = false;
	private static final String TAG = "ServiceState";
//	public static final String SERVICE_TAG = "Mobility";
//	public static final String MOBILITY = "mobility";
//	public static final String SAMPLE_RATE = "sample rate";
//	public static final String ACC_START = "edu.ucla.cens.mobility.record";
	static AlarmManager mgr;
	static long sampleRate;
	public static ServiceConnection AccelServiceConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			setmAccel(IAccelService.Stub.asInterface(service));
			mConnected = true;
			try
			{
				getmAccel().start(Mobility.SERVICE_TAG);
				getmAccel().suggestRate(Mobility.SERVICE_TAG, SensorManager.SENSOR_DELAY_GAME);
				getmAccel().suggestInterval(Mobility.SERVICE_TAG, (int) sampleRate);

				Log.d(TAG, "START WAS CALLED ON ACCEL!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(TAG, "Connected to accel service");
		}

		public void onServiceDisconnected(ComponentName className)
		{
			Log.d(TAG, "onServiceDisconnected was called!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			try
			{
				getmAccel().stop(Mobility.SERVICE_TAG);
				Log.i(TAG, "Successfully stopped service!");
			}
			catch (RemoteException e)
			{
				Log.e(TAG, "Failed to stop service!");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setmAccel(null);
			mConnected = false;
		}
	};
	private static IAccelService mAccel;
	public static boolean mConnected = false;

	public static void setmAccel(IAccelService mAccel)
	{
		ServiceState.mAccel = mAccel;
	}

	public static IAccelService getmAccel()
	{
		return mAccel;
	}

	private static IWiFiGPSLocationService mWiFiGPS;
	private static ServiceConnection mConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. We are communicating with our
			// service through an IDL interface, so get a client-side
			// representation of that from the raw service object.
			setmWiFiGPS(IWiFiGPSLocationService.Stub.asInterface(service));
			Log.i(TAG, "Connected to WiFiGPSLocation Service");
			try
			{
				mWiFiGPS.start(Mobility.SERVICE_TAG);
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// As part of the sample, tell the user what happened.
			Log.i(TAG, "Connected");

		}

		public void onServiceDisconnected(ComponentName className)
		{
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			setmWiFiGPS(null);

			Log.i(TAG, "Disconnected from WiFiGPSLocation Service");

			// As part of the sample, tell the user what happened.

		}
	};

	public static void setmWiFiGPS(IWiFiGPSLocationService mWiFiGPS)
	{
		ServiceState.mWiFiGPS = mWiFiGPS;
	}

	public static IWiFiGPSLocationService getmWiFiGPS()
	{
		return mWiFiGPS;
	}

	public static void initialize(Context context, long sampleRate)
	{
		ServiceState.sampleRate = sampleRate;
		context.bindService(new Intent(edu.ucla.cens.wifigpslocation.IWiFiGPSLocationService.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
		context.bindService(new Intent(IAccelService.class.getName()), AccelServiceConnection, Context.BIND_AUTO_CREATE);
		initialized = true;
	}

	public static void exterminate(Context context)
	{
		try
		{
			context.unbindService(mConnection);
			context.unbindService(AccelServiceConnection);
			initialized = false;
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

}
