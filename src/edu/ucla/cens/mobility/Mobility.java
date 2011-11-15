package edu.ucla.cens.mobility;

import java.util.HashMap;

import edu.ucla.cens.accelservice.IAccelService;
import edu.ucla.cens.mobility.R;
import edu.ucla.cens.mobility.blackout.Blackout;
import edu.ucla.cens.mobility.blackout.BlackoutDesc;
import edu.ucla.cens.mobility.blackout.base.TriggerBase;
import edu.ucla.cens.mobility.blackout.base.TriggerDB;
import edu.ucla.cens.mobility.blackout.utils.SimpleTime;
import edu.ucla.cens.systemlog.ISystemLog;
import edu.ucla.cens.systemlog.Log;
import edu.ucla.cens.wifigpslocation.IWiFiGPSLocationService;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import edu.ucla.cens.mobility.blackout.base.TriggerInit;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
//import android.util.Log;
import android.widget.SimpleCursorAdapter;
//import android.widget.Toast;

public class Mobility
{
	
	static Location globalLoc;
	static boolean setInterval = false;
	private static PendingIntent startPI = null;
	static boolean initialized = false;
//	private static PendingIntent stopPI = null;
//	static NotificationManager nm;
//	static Notification notification;
	public static final String TAG = "Mobility";
	public static final String SERVICE_TAG = "Mobility";
	public static final String MOBILITY = "mobility";
	public static final String SAMPLE_RATE = "sample rate";
	public static final String ACC_START = "edu.ucla.cens.mobility.record";
	public static final String STATUS_PENDING = "pending";
	public static final String STATUS_OK = "mobility";
	public static final String STATUS_ERROR = "error";
	public static final String STATUS_BLACKOUT = "blackout";
	
//	public static Context appContext = null;
	static AlarmManager mgr;
	
	static long sampleRate = 60000;
	static boolean intervalSet = false;
	static boolean accelConnected = false;
	static boolean gpsConnected = false;
//	public static boolean mConnected = false;
	public static ServiceConnection accelServiceConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			setmAccel(IAccelService.Stub.asInterface(service));
//			mConnected = true;
			try
			{
				getmAccel().start(SERVICE_TAG);
				getmAccel().suggestRate(SERVICE_TAG, SensorManager.SENSOR_DELAY_GAME);
				getmAccel().suggestInterval(SERVICE_TAG, (int) sampleRate);
				
				Log.d(TAG, "START WAS CALLED ON ACCEL");
				accelConnected = true;
			} catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i(TAG, "Connected to accel service");
		}

		public void onServiceDisconnected(ComponentName className)
		{
			Log.d(TAG, "onServiceDisconnected was called!");
			try
			{
				getmAccel().stop(SERVICE_TAG);
				Log.i(TAG, "Successfully stopped service!");
				accelConnected = false;
			} catch (RemoteException e)
			{
				Log.e(TAG, "Failed to stop service!");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setmAccel(null);
//			mConnected = false;
		}
	};
	private static IAccelService mAccel;
//	private static Service bootService = null;
//	public static void start(Context context, Service boot)
//	{
//		bootService = boot;
//		start(context);
//	}
	
	public static void start(Context context)
	{
		TriggerDB db = new TriggerDB(context);
		db.open();
		boolean canRunNow = true;
		Cursor c = db.getAllTriggers();
		if (c.moveToFirst())
		{
			do
			{
				int trigId = c.getInt(c.getColumnIndexOrThrow(TriggerDB.KEY_ID));
				
				String trigDesc = db.getTriggerDescription(trigId);
				BlackoutDesc conf = new BlackoutDesc();

				if (!conf.loadString(trigDesc))
				{
					continue;
				}
				SimpleTime start = conf.getRangeStart();
				SimpleTime end = conf.getRangeEnd();
				SimpleTime now = new SimpleTime();
				Log.d(TAG, start.getHour() + ":" + start.getMinute() + " until " + end.getHour() + ":" + end.getMinute() + " is blackout and now is " + now.getHour() + now.getMinute());
				if (!start.isAfter(now) && end.isAfter(now))
				{
					canRunNow = false;
				}


			} while (c.moveToNext());
		}
		c.close();
		db.close();
		TriggerInit.initTriggers(context);
		if (canRunNow)
		{
			Log.d(TAG, "Starting mobility!");
			startMobility(context);
			
		}
	}
	
	public static void stop(Context context)
	{
		
		TriggerDB db = new TriggerDB(context);
		db.open();
		boolean runningNow = true;
		Cursor c = db.getAllTriggers();
		if (c.moveToFirst())
		{
			do
			{
				int trigId = c.getInt(c.getColumnIndexOrThrow(TriggerDB.KEY_ID));
				
				String trigDesc = db.getTriggerDescription(trigId);
				BlackoutDesc conf = new BlackoutDesc();

				if (!conf.loadString(trigDesc))
				{
					continue;
				}
				SimpleTime start = conf.getRangeStart();
				SimpleTime end = conf.getRangeEnd();
				SimpleTime now = new SimpleTime();
				if (!start.isAfter(now) && !end.isBefore(now))
				{
					runningNow = false;
				}
				new Blackout().stopTrigger(context, trigId, db.getTriggerDescription(trigId));

			} while (c.moveToNext());
		}
		c.close();
		db.close();
//		TriggerInit.initTriggers(context);
		if (runningNow)
		{
			Log.d(TAG, "Stopping mobility!");
			stopMobility(context, false);
		}
		else
			Log.d(TAG, "Not running, so ignoring stop command");
	}
	
	static int failCount = 0;
	public static void setNotification(Context context, String status, String message)
	{
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.pending;
		if (status.equals(STATUS_OK))
			icon = R.drawable.mobility;
		else if (status.equals(STATUS_ERROR))
			icon = R.drawable.error;
		else if (status.equals(STATUS_BLACKOUT))
			icon = R.drawable.blackout;
		Notification notification = new Notification(icon, null, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_NO_CLEAR;
		Intent i = new Intent("edu.ucla.cens.mobility.control");
//		i.
		
//		appContext = context;
		PendingIntent pi = PendingIntent.getActivity(context.getApplicationContext(), 1, i, 1);
		notification.setLatestEventInfo(context.getApplicationContext(), "Mobility", message, pi);
		nm.notify(123, notification);
	}
	
	
	public static void startMobility(Context context)
	{
		Log.d(TAG, "Starting mobility service, no blackout!");
		setNotification(context, STATUS_PENDING, "Waiting for the first sensor sample");
//		nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//		notification = new Notification(R.drawable.pending, null, System.currentTimeMillis());
//		notification.flags |= Notification.FLAG_NO_CLEAR;
//		
//		
////		appContext = context;
//		PendingIntent pi = PendingIntent.getActivity(context.getApplicationContext(), 1, new Intent(context, MobilityControl.class), 1);
//		notification.setLatestEventInfo(context.getApplicationContext(), "Mobility", "Service Running", pi);
//		nm.notify(123, notification);

		SharedPreferences settings = context.getSharedPreferences(MOBILITY, Context.MODE_PRIVATE);
		sampleRate = (long) settings.getInt(SAMPLE_RATE, 60) * 1000;

		mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

//		context.registerReceiver(accReceiver, new IntentFilter(ACC_START));
//		if (!initialized)
		initialize(context);
		Log.d(TAG, "Sample rate is: " + sampleRate);
		startGPS(context, sampleRate);
		startAcc(context, sampleRate);
//		Toast.makeText(context, R.string.mobilityservicestarted, Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Starting transport mode service with sampleRate: " + sampleRate);
		GarbageCollectReceiver.scheduleGC(context, mgr);
		
	}
	
	

	public static void stopMobility(Context context, boolean blackout)
	{
		Log.d(TAG, "Stopping mobility service!");
		if (mgr != null)
		{
			Intent i = new Intent(ACC_START);
			startPI = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
			mgr.cancel(startPI);
		}
		stopAcc(context, sampleRate);
		stopGPS(context);
//		try
//		{
//			context.unregisterReceiver(accReceiver);
//		} catch (Exception e1)
//		{
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		// unregisterReceiver(stopAccReceiver);
		// lManager.removeUpdates(lListener);
//		try
//		{
////			if (getmWiFiGPS() != null)
////				getmWiFiGPS().stop(SERVICE_TAG);
////			if (getmAccel() != null)
////				getmAccel().stop(SERVICE_TAG);
//			
////			try
////			{
////				context.unbindService(mConnection);
////			} catch (Exception e)
////			{
////				// TODO Auto-generated catch block
//////				e.printStackTrace();
////			}
//			
////			unbindService(Log.SystemLogConnection);
//		} catch (Exception e)
//		{
//			// If it's not running then we don't care if this can't be unbound.
//			// Why does it want to crash?
//			e.printStackTrace();
//		}
		exterminate(context.getApplicationContext());
//		if (nm != null)
		{
			if (blackout)
			{
				setNotification(context, STATUS_BLACKOUT, "Mobility paused during blackout time");
			}
			else
			{
				Log.d(TAG, "Canceling notification!");
				NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				nm.cancel(123);
			}
		}
//		else
//			Log.d(TAG, "nm is null!");
	}
	
	private static void startAcc(Context context, long milliseconds)
	{
		Intent i = new Intent(ACC_START);
		startPI = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		mgr.setRepeating(AlarmManager.RTC_WAKEUP, 0, milliseconds, startPI);
//		context.bindService(new Intent(IAccelService.class.getName()), AccelServiceConnection, Context.BIND_AUTO_CREATE);

	}

	private static void stopAcc(Context context, long milliseconds)
	{
		if (startPI != null)
			mgr.cancel(startPI);
//		if (stopPI != null)
//			mgr.cancel(stopPI);
		try
		{
			getmAccel().stop(SERVICE_TAG);
//			context.unbindService(AccelServiceConnection);
			Log.i(TAG, "Successfully unbound accel service");
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void startGPS(Context context, long milliseconds)
	{
//		Log.d(TAG, String.format("Sampling GPS at %d.", milliseconds / 1000));
//		context.bindService(new Intent(edu.ucla.cens.wifigpslocation.IWiFiGPSLocationService.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
		
	}

	private static void stopGPS(Context context)
	{
		try
		{
			getmWiFiGPS().stop(SERVICE_TAG);
//			context.unbindService(mConnection);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}

	}
	public static void setmWiFiGPS(IWiFiGPSLocationService mWiFiGPS)
	{
		Mobility.mWiFiGPS = mWiFiGPS;
	}

	public static IWiFiGPSLocationService getmWiFiGPS()
	{
		return mWiFiGPS;
	}
	public static void setmAccel(IAccelService mAccel)
	{
		Mobility.mAccel = mAccel;
	}

	public static IAccelService getmAccel()
	{
		return mAccel;
	}
	private static IWiFiGPSLocationService mWiFiGPS;
	public static ServiceConnection mConnection = new ServiceConnection()
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
				mWiFiGPS.start(SERVICE_TAG);
				gpsConnected = true;
			} catch (RemoteException e)
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
			gpsConnected = false;
			Log.i(TAG, "Disconnected from WiFiGPSLocation Service");
			
			// As part of the sample, tell the user what happened.

		}
	};
	
	public static void initialize(Context context)
	{
		
		Log.i(TAG, "Initializing");
//		ServiceState.sampleRate = sampleRate;
		context.bindService(new Intent(edu.ucla.cens.wifigpslocation.IWiFiGPSLocationService.class.getName()), mConnection, Context.BIND_AUTO_CREATE);
		context.bindService(new Intent(IAccelService.class.getName()), accelServiceConnection, Context.BIND_AUTO_CREATE);
		context.bindService(new Intent(ISystemLog.class.getName()), Log.SystemLogConnection, Context.BIND_AUTO_CREATE);
		Log.register(TAG);
		initialized = true;
	}

	public static void exterminate(Context context)
	{
		try
		{
			context.unbindService(mConnection);
			context.unbindService(accelServiceConnection);
			context.unbindService(Log.SystemLogConnection);
			initialized = false;
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}
	
	
//	public static void unbindServices(Context context)
//	{
//		ServiceState.exterminate(context);
////		try
////		{
////			context.unbindService(mConnection);
////			context.unbindService(AccelServiceConnection);
////			try
////			{
////				if (bootService != null)
////					bootService.stopSelf();
////			}
////			catch (Exception e)
////			{
////				// If this breaks, then we didn't want it to happen anyway
////				Log.d(TAG, "Failed stopping boot service");
////			}
//////			Intent i = new Intent(BootService.bound);
//////			PendingIntent boundPI = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
////			
////		} catch (Exception e)
////		{
////			// TODO Auto-generated catch block
//////			e.printStackTrace();
////		}
//	}
	
//	static BroadcastReceiver accReceiver = new BroadcastReceiver() // put this sucker in its own file and register in manifest
//	{
//
//		@Override
//		public void onReceive(Context context, Intent intent)
//		{
//			Log.e(TAG, "Start mobility service");
//			// Old way
////			context.startService(new Intent(context, ClassifierService.class));
//			// wakeful intent service
//			WakefulIntentService.sendWakefulWork(context, ClassifierService.class);
//		}
//	};
//	static BroadcastReceiver GCReceiver = new BroadcastReceiver() // this one too
//	{
//
//		@Override
//		public void onReceive(Context context, Intent intent)
//		{
//			Log.d(TAG, "Collect garbage");
//			// Need context!
//			MobilityDbAdapter mda = new MobilityDbAdapter(context, "mobility", "mobility", "mobility");
//			mda.open();
//			mda.deleteSomeRows(System.currentTimeMillis() - gctime * 24 * 3600 * 1000);
//			mda.close();
//		}
//	};
}
