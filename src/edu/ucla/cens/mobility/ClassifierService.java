package edu.ucla.cens.mobility;

import java.util.ArrayList;
import java.util.TimeZone;
import java.util.Vector;

import edu.ucla.cens.accelservice.IAccelService;
import edu.ucla.cens.systemlog.Log;
import edu.ucla.cens.wifigpslocation.IWiFiGPSLocationService;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
//import android.util.Log;

public class ClassifierService extends WakefulIntentService
{
	public ClassifierService()
	{
		super("ClassifierService");
		// TODO Auto-generated constructor stub
	}

	// private IWiFiGPSLocationService mWiFiGPS;

	private static final String TAG = "ClassifierService";
	private static MobilityDbAdapter tmdb;

	@Override
	public void onCreate()
	{
		// TODO Auto-generated method stub
		super.onCreate();
//		if (!Mobility.initialized)
//			Mobility.initialize(this.getApplicationContext());
	}

	@Override
	public void onDestroy()
	{
		// TODO Auto-generated method stub
		super.onDestroy();
//		unbindService(AccelServiceConnection);
//		unbindService(mConnection);
	}

//	private IWiFiGPSLocationService mWiFiGPS;
//	private ServiceConnection mConnection = new ServiceConnection()
//	{
//		public void onServiceConnected(ComponentName className, IBinder service)
//		{
//			// This is called when the connection with the service has been
//			// established, giving us the service object we can use to
//			// interact with the service. We are communicating with our
//			// service through an IDL interface, so get a client-side
//			// representation of that from the raw service object.
//			mWiFiGPS = IWiFiGPSLocationService.Stub.asInterface(service);
//			Log.i(TAG, "Connected to WiFiGPSLocation Service");
//
//			Log.i(TAG, "Connected");
//			ready(gpsCode);
//		}
//
//		public void onServiceDisconnected(ComponentName className)
//		{
//			// This is called when the connection with the service has been
//			// unexpectedly disconnected -- that is, its process crashed.
//			mWiFiGPS = null;
//
//			Log.i(TAG, "Disconnected from WiFiGPSLocation Service");
//
//		}
//	};

//	public ServiceConnection AccelServiceConnection = new ServiceConnection()
//	{
//		public void onServiceConnected(ComponentName className, IBinder service)
//		{
//			mAccel = IAccelService.Stub.asInterface(service);
//			ClassifierService.this.ready(ClassifierService.this.accelCode);
//			Log.i(TAG, "Connected to accel service");
//		}
//
//		public void onServiceDisconnected(ComponentName className)
//		{
//			Log.d(TAG, "onServiceDisconnected was called!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//			mAccel = null;
//		}
//	};
//	private IAccelService mAccel;
	private int accelCode = 0;
	private int gpsCode = 1;
	boolean[] readySvc = { false, false };

	private synchronized void ready(int serviceCode)
	{
		readySvc[serviceCode] = true;
		if (readySvc[gpsCode] && readySvc[accelCode])
		{
			getTransportMode();
			stopSelf();
		}
	}

	@SuppressWarnings("unchecked")
	private Vector<ArrayList<Double>> getAccSamples()
	{
		try
		{
			if (Mobility.getmAccel() == null)
			{

				Log.e(TAG, "mAccel fails to not be null.");
				return null;
			}
			// ArrayList<Double[]> forceWithTimes =
			// (ArrayList<Double[]>)mAccel.getLastForce();
			ArrayList<Double> force = new ArrayList<Double>();
			// if (forceWithTimes != null)
			// for (int i = 0; i < forceWithTimes.size(); i++)
			// {
			// System.err.println(forceWithTimes.get(i) +"");
			// force.add((Double)(forceWithTimes.get(i))[0]);
			// Log.d(TAG, (forceWithTimes.get(i))[1] + "  " + i);
			// }
			Log.d(TAG, (System.currentTimeMillis() - Mobility.getmAccel().getLastTimeStamp()) / 1000 + " is how old this sample is!!!!!!");
			Vector<ArrayList<Double>> curList = new Vector<ArrayList<Double>>();
			curList.add((ArrayList<Double>) Mobility.getmAccel().getLastXValues());
			curList.add((ArrayList<Double>) Mobility.getmAccel().getLastYValues());
			curList.add((ArrayList<Double>) Mobility.getmAccel().getLastZValues());
			curList.add((ArrayList<Double>) Mobility.getmAccel().getLastForce());
			if (curList.get(0) != null)
			{
				Log.i(TAG, "Here is the force vector: \n" + curList.toString());
				return curList;
			}
			else
				Log.i(TAG, "List was null, try later.");
		}
		catch (RemoteException re)
		{
			Log.e(TAG, "Remote Ex", re);

		}
		return null;
	}

	private String printTriaxialData(Vector<ArrayList<Double>> triax)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < triax.get(0).size(); i++)
		{
			sb.append("x:" + triax.get(0).get(i) + ",");
			sb.append("y:" + triax.get(1).get(i) + ",");
			sb.append("z:" + triax.get(2).get(i) + ";");
		}
		return sb.toString();
	}

	/** Provider strings */
	private static final String WIFIGPS_PROVIDER = "WiFiGPSLocation:GPS";
	private static final String FAKE_PROVIDER = "WiFiGPSLocation:Fake";
	private static final String APPROX_PROVIDER = "WiFiGPSLocation:Approx";
	private static final String UNAVAILABLE = "unavailable";
	private static final String VALID = "valid";
	private static final String INACCURATE = "inaccurate";
	private static final String STALE = "stale";
	private static final int INACCURACY_THRESHOLD = 30;

	private void getTransportMode()
	{
		Vector<ArrayList<Double>> samples = getAccSamples();

		if (samples == null)
		{
			return;
		}
		ArrayList<Double> accData = samples.get(3);
		Log.i(TAG, samples.size() + " is the sample size");
		double lat = Double.NaN;
		double lon = Double.NaN;
		float speed = 0;
		double acc = Double.NaN;
		long timestamp = 0;
		String provider = "None";
//		boolean setInterval = false;
		String status = UNAVAILABLE;
		// double acc = 99999;
		Location loc;
		String wifiData = "";
		try
		{
			// while (mWiFiGPS == null) Log.e(TAG,
			// "wifigps is null even though android returned from onbind. Fantastic.");
			// // make this better
			if (Mobility.getmWiFiGPS() != null)
			{
				loc = Mobility.getmWiFiGPS().getLocation();
				wifiData = Mobility.getmWiFiGPS().getWiFiScan();
				// globalLoc = mWiFiGPS.getLocation();
//				if (!setInterval)
//				{
//					Mobility.getmWiFiGPS().suggestInterval(Mobility.SERVICE_TAG, (int) Mobility.sampleRate);
//					setInterval = true;
//				}
				Log.d(TAG, "mWiFiGPS is not null!");
				if (loc != null)
				{
					lat = loc.getLatitude();
					lon = loc.getLongitude();
					speed = loc.getSpeed();
					acc = loc.getAccuracy();
					provider = loc.getProvider();
					timestamp = loc.getTime();
					if (provider.equals(FAKE_PROVIDER))
					{
						status = UNAVAILABLE;
					}
					else if (provider.equals(WIFIGPS_PROVIDER))
					{
						if (acc > INACCURACY_THRESHOLD)
						{
							status = INACCURATE;
						}
						else
							status = VALID;
					}
					else if (provider.equals(APPROX_PROVIDER))
					{
						status = STALE;
					}
				}
				else
				{
					Log.d(TAG, "mWiFiGPS.getLocation() is null, losing sample");
				}
			}
			else
			{
				loc = null;
				Log.d(TAG, "mWiFiGPS is null, losing sample");
			}
		}
		catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			loc = null;
			// e.printStackTrace();
		}

		if (samples.get(0).size() < 10)
		{

			addTransportMode("still", samples, speed, acc, provider, status, timestamp, wifiData, lat, lon);
		}
		// sample *= 310;

		// Thread thread = new Thread(new Classify(samples));
		// Log.d(TAG, "Starting thread");
		// thread.start();
		// int newVal = 0;
		double dataSize = accData.size();
		String activity = "unknown";
		double sum = 0.0, s = 0.0;
		double avg = 0.0, a = 0.0;
		double var = 0.0, v = 0.0;
		double accFft1, accFft2, accFft3, accFft4, accFft5, a1, a2, a3, a4, a5, a6, a7, a8, a9, a0;

		// float speed = 0.0f;

		a1 = goertzel(accData, 1., dataSize);
		a2 = goertzel(accData, 2., dataSize);
		a3 = goertzel(accData, 3., dataSize);
		a4 = goertzel(accData, 4., dataSize);
		a5 = goertzel(accData, 5., dataSize);
		a6 = goertzel(accData, 6., dataSize);
		a7 = goertzel(accData, 7., dataSize);
		a8 = goertzel(accData, 8., dataSize);
		a9 = goertzel(accData, 9., dataSize);
		a0 = goertzel(accData, 10., dataSize);

		for (int i = 0; i < dataSize; i++)
		{
			s += accData.get(i);
		}
		a = s / dataSize;
		Log.d(TAG, "s is " + s);
		s = 0.0;
		for (int i = 0; i < dataSize; i++)
		{
			s += Math.pow((accData.get(i) - a), 2.0);
		}

		v = s / dataSize;

		for (int i = 0; i < dataSize; i++)
		{

			accData.set(i, accData.get(i) * 310.); // restore to android
			// measurement

		}

		for (int i = 0; i < dataSize; i++)
		{
			sum += accData.get(i);
		}

		avg = sum / dataSize;
		sum = 0.0;
		for (int i = 0; i < dataSize; i++)
		{
			sum += Math.pow((accData.get(i) - avg), 2.0);
		}
		var = sum / dataSize;

		accFft1 = goertzel(accData, 1., dataSize);
		accFft2 = goertzel(accData, 2., dataSize);
		accFft3 = goertzel(accData, 3., dataSize);
		accFft4 = goertzel(accData, 4., dataSize);
		accFft5 = goertzel(accData, 5., dataSize);
		Log.d(TAG, String.format("Samples = %4.0f", dataSize));

		if (loc != null)
		{
			speed = loc.getSpeed();
			acc = loc.getAccuracy();
		}
		Log.d(TAG, speed + " is the speed");
		String features = String.format("%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f", var, accFft1, accFft2, accFft3,
				speed, v, a, a1, a2, a3, a4, a5, a6, a7, a8, a9, a0, 0., 0., 0.);
		// Log.d(TAG, speed +
		// " is the speed and the features are " + features);
		activity = activity(var, accFft1, accFft2, accFft3, speed, v, a, a1, a2, a3, a4, a5, a6, a7, a8, a9, a0);
		// double [] fft = {a1, a2, a3, a4, a5, a6, a7, a8, a9, a0}; // real
		// features
		double[] fft = { a1, a2, a3, a5, a8, accFft1, accFft2, accFft3, var, acc }; // wrong
																					// order
																					// hack
																					// to
																					// get
																					// all
																					// used
																					// features
		addTransportMode(activity, samples, speed, acc, provider, status, timestamp, wifiData, lat, lon);
	}

	private static String indoorActivity(double var, double avg, double a1, double a2, double a3, double a4, double a5, double a6, double a7, double a8, double a9, double a0)
	{
		Log.d(TAG, "Features: var=" + var + " avg=" + avg + " a=" + a1 + "," + a2 + "," + a3 + "," + a4 + "," + a5 + "," + a6 + "," + a7 + "," + a8 + "," + a9 + "," + a0);
		if (var <= 0.0047)
		{
			if (var <= 0.0016)
				return "still";
			else
			{
				if (a5 <= 0.1532)
				{
					if (a1 <= 0.5045)
						return "still";
					else
						return "walk";
				}
				else
					return "still";
			}
		}
		else
		{
			if (a3 <= 60.3539)
			{
				if (var <= 0.0085)
				{
					if (a8 <= 0.0506)
						return "walk";
					else
					{
						if (a2 <= 2.8607)
							return "still";
						else
							return "walk";
					}
				}
				else
				{
					if (a2 <= 2.7725)
					{
						if (a1 <= 13.0396)
							return "walk";
						else
							return "still";
					}
					else
						return "walk";
				}
			}
			else
				return "run";
		}

	}

	private static String activity(double acc_var, double accgz1, double accgz2, double accgz3, float gps_speed, double var, double avg, double a1, double a2, double a3, double a4, double a5,
			double a6, double a7, double a8, double a9, double a0)
	{
		String output = "still";
		Log.d(TAG, "Features: speed=" + gps_speed + " var=" + acc_var + " gz1=" + accgz1 + " gz2=" + accgz2 + " gz3=" + accgz3);
		if (gps_speed <= 0.29)
			output = indoorActivity(var, avg, a1, a2, a3, a4, a5, a6, a7, a8, a9, a0);// "still";
		else if (accgz3 <= 2663606.69633)
			if (gps_speed <= 6.37)
				if (accgz2 <= 463400.011249)
					if (acc_var <= 205.972492)
						if (acc_var <= 13.084102)
							if (gps_speed <= 0.8)
								output = "still";
							else
								output = "drive";// "bike";
						else if (gps_speed <= 1.33)
							output = "still";// "bike";
						else
							output = "drive";
					else if (gps_speed <= 1.84)
						if (accgz1 <= 125502.942136)
							output = "walk";// "bike";
						else
							output = "walk";
					else
						output = "bike";// "bike";

				else if (acc_var <= 41153.783729)
					if (gps_speed <= 2.12)
						output = "walk";
					else
						output = "bike";
				else
					output = "run";
			else
				output = "drive";

		else if (accgz3 <= 5132319.94693)
			if (gps_speed <= 1.86)
				output = "walk";// bike
			else
				output = "run";
		else
			output = "run";
		Log.d(TAG, output);
		return output;
	}

	private static double goertzel(ArrayList<Double> accData, double freq, double sr)
	{
		double s_prev = 0;
		double s_prev2 = 0;
		double coeff = 2 * Math.cos((2 * Math.PI * freq) / sr);
		double s;
		for (int i = 0; i < accData.size(); i++)
		{
			double sample = accData.get(i);
			s = sample + coeff * s_prev - s_prev2;
			s_prev2 = s_prev;
			s_prev = s;
		}
		double power = s_prev2 * s_prev2 + s_prev * s_prev - coeff * s_prev2 * s_prev;

		return power;
	}

	// public static void addTransportMode(String mode, String features, double
	// speed, double variance, double average, double [] fft, double lat, double
	// lon)
	// {
	// uploadMode(mode, speed, variance, average, fft, lat, lon);
	// }

	public static void addTransportMode(String mode, Vector<ArrayList<Double>> samples, double speed, double accuracy, String provider, String status, long timestamp, String wifiData, double lat,
			double lon)
	{
		Log.d(TAG, "GPS is " + lat + " and " + lon + "");
		long time = System.currentTimeMillis();// resJson.setAndReturnTime();

		// Open the database, and store the response
		tmdb.open();
		tmdb.createRow(mode, time, status, String.valueOf(speed), timestamp, String.valueOf(accuracy), provider, wifiData, samples, String.valueOf(lat), String.valueOf(lon));
		tmdb.close();
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doWakefulWork(Intent intent)
	{
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		tmdb = new MobilityDbAdapter(this, "mobility", "mobility", "mobility");
		if (!Mobility.gpsConnected)
			this.getApplicationContext().bindService(new Intent(edu.ucla.cens.wifigpslocation.IWiFiGPSLocationService.class.getName()), Mobility.mConnection, Context.BIND_AUTO_CREATE);
		if (!Mobility.accelConnected)
			this.getApplicationContext().bindService(new Intent(IAccelService.class.getName()), Mobility.accelServiceConnection, Context.BIND_AUTO_CREATE);
		getTransportMode();
		stopSelf();
	}

}
