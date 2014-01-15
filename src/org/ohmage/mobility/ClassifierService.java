
package org.ohmage.mobility;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.accelservice.IAccelService;
import org.ohmage.logprobe.Log;
import org.ohmage.logprobe.LogProbe;
import org.ohmage.wifigpslocation.IWiFiGPSLocationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;



public class ClassifierService extends WakefulIntentService {
    private LogProbe logger;

    public ClassifierService() {
        super("ClassifierService");
        // TODO Auto-generated constructor stub
    }

    // private IWiFiGPSLocationService mWiFiGPS;

    private static final String TAG = "ClassifierService";
    private static MobilityDbAdapter tmdb;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        // if (!Mobility.initialized)
        // Mobility.initialize(this.getApplicationContext());
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        // unbindService(AccelServiceConnection);
        // unbindService(mConnection);
    }

    // private IWiFiGPSLocationService mWiFiGPS;
    // private ServiceConnection mConnection = new ServiceConnection()
    // {
    // public void onServiceConnected(ComponentName className, IBinder service)
    // {
    // // This is called when the connection with the service has been
    // // established, giving us the service object we can use to
    // // interact with the service. We are communicating with our
    // // service through an IDL interface, so get a client-side
    // // representation of that from the raw service object.
    // mWiFiGPS = IWiFiGPSLocationService.Stub.asInterface(service);
    // Log.i(TAG, "Connected to WiFiGPSLocation Service");
    //
    // Log.i(TAG, "Connected");
    // ready(gpsCode);
    // }
    //
    // public void onServiceDisconnected(ComponentName className)
    // {
    // // This is called when the connection with the service has been
    // // unexpectedly disconnected -- that is, its process crashed.
    // mWiFiGPS = null;
    //
    // Log.i(TAG, "Disconnected from WiFiGPSLocation Service");
    //
    // }
    // };

    // public ServiceConnection AccelServiceConnection = new ServiceConnection()
    // {
    // public void onServiceConnected(ComponentName className, IBinder service)
    // {
    // mAccel = IAccelService.Stub.asInterface(service);
    // ClassifierService.this.ready(ClassifierService.this.accelCode);
    // Log.i(TAG, "Connected to accel service");
    // }
    //
    // public void onServiceDisconnected(ComponentName className)
    // {
    // Log.d(TAG,
    // "onServiceDisconnected was called!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    // mAccel = null;
    // }
    // };
    // private IAccelService mAccel;
    private final int accelCode = 0;
    private final int gpsCode = 1;
    boolean[] readySvc = {
            false, false
    };
    private final boolean wifiChecking = true;

    private synchronized void ready(int serviceCode) {
        readySvc[serviceCode] = true;
        if (readySvc[gpsCode] && readySvc[accelCode]) {
            getTransportMode();
            stopSelf();
        }
    }

    @SuppressWarnings("unchecked")
    private Vector<ArrayList<Double>> getAccSamples() {
        try {
            if (Mobility.getmAccel() == null
                    || System.currentTimeMillis() - Mobility.getmAccel().getLastTimeStamp() > 10000 + Mobility.sampleRate) {
                Log.v(TAG, "mAccel fails to not be null or old.");
                if (Mobility.failCount++ > 2) {
                    if (Mobility.getmAccel() == null)
                        Mobility.setNotification(this, Mobility.STATUS_ERROR,
                                "Please verify that AccelService is installed");
                    else {
                        Log.w(TAG, "Last accelerometer sample is "
                                + (System.currentTimeMillis() - Mobility.getmAccel()
                                        .getLastTimeStamp()) / 1000 + " seconds old");
                        Mobility.setNotification(this, Mobility.STATUS_ERROR,
                                "Mobility is waiting for new accelerometer data");
                        // A workaround for when AccelService failed to start
                        // all the way. The bug was fixed so the workaround is
                        // gone.
                        // Mobility.getWithTheProgram(this.getApplicationContext());
                        // // workaround for when fast restart on MyTouch 4G
                        // breaks AccelService
                    }
                } else
                    Mobility.setNotification(this, Mobility.STATUS_PENDING,
                            "Waiting for the first sensor sample");
                return null;
            }
            Mobility.failCount = 0;
            // Remove the accelservice error notification
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(Mobility.NOTIF_ERROR_ID);

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
            Log.i(TAG, (System.currentTimeMillis() - Mobility.getmAccel().getLastTimeStamp())
                    / 1000 + " is how old this sample is!!!!!!");
            Vector<ArrayList<Double>> curList = new Vector<ArrayList<Double>>();
            curList.add((ArrayList<Double>) Mobility.getmAccel().getLastXValues());
            curList.add((ArrayList<Double>) Mobility.getmAccel().getLastYValues());
            curList.add((ArrayList<Double>) Mobility.getmAccel().getLastZValues());
            curList.add((ArrayList<Double>) Mobility.getmAccel().getLastForce());
            if (curList.get(0) != null) {
                // Log.i(TAG, "Here is the force vector: \n" +
                // curList.toString());
                return curList;
            } else
                Log.w(TAG, "List was null, try later.");
        } catch (RemoteException re) {
            Log.e(TAG, "Remote Ex", re);

        }
        return null;
    }

    private String printTriaxialData(Vector<ArrayList<Double>> triax) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < triax.get(0).size(); i++) {
            sb.append("x:" + triax.get(0).get(i) + ",");
            sb.append("y:" + triax.get(1).get(i) + ",");
            sb.append("z:" + triax.get(2).get(i) + ";");
        }
        return sb.toString();
    }

    /** Provider strings */
    private static final String WIFIGPS_PROVIDER = "WiFiGPSLocation:GPS";
    private static final String WIFIGPSCACHED_PROVIDER = "WiFiGPSLocation:Cached";
    private static final String FAKE_PROVIDER = "WiFiGPSLocation:Fake";
    private static final String APPROX_PROVIDER = "WiFiGPSLocation:Approx";
    private static final String NET_PROVIDER = "WiFiGPSLocation:Network";
    private static final String UNAVAILABLE = "unavailable";
    private static final String VALID = "valid";
    private static final String INACCURATE = "inaccurate";
    private static final String STALE = "stale";
    private static final String NETWORK = "network";
    private static final int INACCURACY_THRESHOLD = 30;
    private static final int STALENESS_THRESHOLD = 3 * 60 * 1000;
    public static final String WALK = "walk";
    public static final String RUN = "run";
    public static final String STILL = "still";
    public static final String DRIVE = "drive";
    public static final String BIKE = "bike";
    public static final String ERROR = "error";
    public static final String UNKNOWN = "unknown";
    public static final String TILT = "tilt";
    private static final String WIFI_HISTORY = "wifi_history";
    private static final String LOC_HISTORY = "loc_history";

    private void getTransportMode() {
        Vector<ArrayList<Double>> samples = getAccSamples();

        boolean gpsFail = false;

        // Log.i(TAG, samples.size() + " is the sample size"); // Always 4 now
        double lat = Double.NaN;
        double lon = Double.NaN;
        float speed = 0;
        int cachedCount = 0;
        boolean driveCheat = true;
        float acc = Float.NaN;
        long timestamp = 0;
        String provider = "None";
        // boolean setInterval = false;
        String status = UNAVAILABLE;
        // double acc = 99999;
        Location loc;
        String wifiData = "";
        Classification wifiClass = null;
        Classification locClass = null;
        try {
            // while (mWiFiGPS == null) Log.e(TAG,
            // "wifigps is null even though android returned from onbind. Fantastic.");
            // // make this better
            if (Mobility.getmWiFiGPS() != null) {
                loc = Mobility.getmWiFiGPS().getLocation();
                wifiData = Mobility.getmWiFiGPS().getWiFiScan();
                // Log.d(TAG, wifiData);

                try {
                	if(wifiData != null)
                		wifiClass = checkWifi(new JSONObject(wifiData));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.e(TAG, "Error running checkWifi :" + wifiData, e);
                }
                
                try {
                	if (loc != null)
                	{
	                	LocationPoint lp = new LocationPoint(loc.getLatitude(), loc.getLongitude(), loc.getTime());
	                    locClass = checkLocation(lp);
                	}
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.e(TAG, "Error running checkLocation :" + loc.toString(), e);
                }
                
                // globalLoc = mWiFiGPS.getLocation();
                // if (!setInterval)
                // {
                // Mobility.getmWiFiGPS().suggestInterval(Mobility.SERVICE_TAG,
                // (int) Mobility.sampleRate);
                // setInterval = true;
                // }
                Log.v(TAG, "mWiFiGPS is not null!");
                if (loc != null) {
                    lat = loc.getLatitude();
                    lon = loc.getLongitude();
                    speed = loc.getSpeed();
                    acc = loc.getAccuracy();
                    provider = loc.getProvider();
                    timestamp = loc.getTime();
                    if (provider.equals(FAKE_PROVIDER)) {
                        Log.v(TAG, "Fake provider");
                        status = UNAVAILABLE;
                        speed = Float.NaN;
                        cachedCount = 0;
                    } else if (provider.equals(WIFIGPSCACHED_PROVIDER)) {
                        cachedCount++;
                        Log.v(TAG, "Cached provider");
                        if (acc > INACCURACY_THRESHOLD) {
                            Log.v(TAG, "Inaccurate");
                            status = INACCURATE;
                            if (!driveCheat)
                                speed = Float.NaN;
                        } else {
                            Log.v(TAG, "Valid");
                            status = VALID;
                        }
                    } else if (provider.equals(WIFIGPS_PROVIDER)) {
                        Log.v(TAG, "GPS provider");
                        cachedCount = 0;
                        if (timestamp > System.currentTimeMillis() - STALENESS_THRESHOLD) {
                            if (acc > INACCURACY_THRESHOLD) {
                                Log.v(TAG, "Inaccurate");
                                status = INACCURATE;
                                if (!driveCheat)
                                    speed = Float.NaN;
                            } else {
                                Log.v(TAG, "Valid");
                                status = VALID;
                            }
                        } else {
                            Log.v(TAG, "Stale");
                            status = STALE;
                            speed = Float.NaN;
                        }
                    } else if (provider.equals(APPROX_PROVIDER)) {
                        cachedCount = 0;
                        Log.v(TAG, "Stale");
                        status = STALE;
                        speed = 0;
                    } else if (provider.equals(NET_PROVIDER)) {
                        cachedCount = 0;
                        Log.v(TAG, "Network");
                        status = NETWORK;
                        speed = 0;
                    } else {
                        cachedCount = 0;
                        Log.e(TAG, "Invalid provider code received: " + provider);
                        Mobility.setDebugNotification(this, "Invalid WiFiGPS code: " + provider);
                    }
                } else {
                    Log.e(TAG, "mWiFiGPS.getLocation() is null, losing sample");
                }
                // Mobility.gpsFailCount = 0;
            } else {
                loc = null;
                // Mobility.gpsFailCount++;
                gpsFail = true;
                Log.w(TAG, "mWiFiGPS is null, no GPS data");
                status = UNAVAILABLE;
                speed = Float.NaN;
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            loc = null;
            status = UNAVAILABLE;
            speed = Float.NaN;
            Log.e(TAG, "Exception happened with connection to WiFiGPS service", e);
            e.printStackTrace();
        }
        String activity = UNKNOWN;
        if (samples == null) {
            Log.w(TAG, "Null object from AccelService");

            activity = ERROR;
            addTransportMode(activity, samples, speed, acc, provider, status, timestamp, wifiData,
                    lat, lon);
            return;
        }
        ArrayList<Double> accData = samples.get(3);

        if (samples.get(0).size() < 10) {
            Log.w(TAG, "Too few samples to do accelerometer feature analysis");
            addTransportMode(STILL, samples, speed, acc, provider, status, timestamp, wifiData,
                    lat, lon);
            return;
        }
        // sample *= 310;

        // Thread thread = new Thread(new Classify(samples));
        // Log.d(TAG, "Starting thread");
        // thread.start();
        // int newVal = 0;
        double dataSize = accData.size();

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

        for (int i = 0; i < dataSize; i++) {
            s += accData.get(i);
        }
        a = s / dataSize;
        // Log.d(TAG, "s is " + s);
        s = 0.0;
        for (int i = 0; i < dataSize; i++) {
            s += Math.pow((accData.get(i) - a), 2.0);
        }

        v = s / dataSize;

        for (int i = 0; i < dataSize; i++) {

            accData.set(i, accData.get(i) * 310.); // restore to android
            // measurement

        }

        for (int i = 0; i < dataSize; i++) {
            sum += accData.get(i);
        }

        avg = sum / dataSize;
        sum = 0.0;
        for (int i = 0; i < dataSize; i++) {
            sum += Math.pow((accData.get(i) - avg), 2.0);
        }
        var = sum / dataSize;

        accFft1 = goertzel(accData, 1., dataSize);
        accFft2 = goertzel(accData, 2., dataSize);
        accFft3 = goertzel(accData, 3., dataSize);
        accFft4 = goertzel(accData, 4., dataSize);
        accFft5 = goertzel(accData, 5., dataSize);
        // Log.d(TAG, String.format("Samples = %4.0f", dataSize));

        // if (loc != null)
        // {
        // speed = loc.getSpeed();
        // //acc = loc.getAccuracy();
        // }
        Log.i(TAG, speed + " is the speed");
        String features = String
                .format("%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f,%2.4f",
                        var, accFft1, accFft2, accFft3, speed, v, a, a1, a2, a3, a4, a5, a6, a7,
                        a8, a9, a0, 0., 0., 0.);
        // Log.d(TAG, speed +
        // " is the speed and the features are " + features);
        
        int totalAPs = 0;
        int matchedAPs = 0;
        if (wifiClass != null)
        {
        	totalAPs = wifiClass.getWifiTotal();
        	matchedAPs = wifiClass.getWifiRecogTotal();
        }
        double radius = 0;
        double travelled = 0;
        if (locClass != null)
        {
	        radius = locClass.getRadius();
	        travelled = locClass.getTravelled();
        }
        activity = activity(speed, a, v, a1, a2, a3, a4, a5, a6, a7, a8, a9, a0, totalAPs, matchedAPs, radius, travelled);
//        activity = GoogleActivityClassifier.getGooglemode(); // TODO get rid of this!
//        if (wifiChecking && !wifiActivity.equals(UNKNOWN)) {
//            if (activity.equals(DRIVE) || activity.equals(STILL))
//                activity = wifiActivity; // The other classifier is rubbish for
//                                         // still/drive, just use WiFi result
//        } else
//            Log.i(TAG, "wifi not used (turned off or unknown for this sample)");
        if (gpsFail && Mobility.debugMode)
            Mobility.setNotification(this, Mobility.STATUS_OK, activity + " (Warning: No GPS)");
        else
            Mobility.setNotification(this, Mobility.STATUS_OK, activity);
        // var, accFft1, accFft2, accFft3, speed, v, a, a1, a2, a3, a4, a5, a6,
        // a7, a8, a9, a0);
        // double [] fft = {a1, a2, a3, a4, a5, a6, a7, a8, a9, a0}; // real
        // features
        double[] fft = {
                a1, a2, a3, a5, a8, accFft1, accFft2, accFft3, var, acc
        }; // wrong
           // order
           // hack
           // to
           // get
           // all
           // used
           // features
        addTransportMode(activity, samples, speed, acc, provider, status, timestamp, wifiData, lat,
                lon);
    }

    // private static String indoorActivity(double var, double avg, double a1,
    // double a2, double a3, double a4, double a5, double a6, double a7, double
    // a8, double a9, double a0)
    // {
    // Log.d(TAG, "Features: var=" + var + " avg=" + avg + " a=" + a1 + "," + a2
    // + "," + a3 + "," + a4 + "," + a5 + "," + a6 + "," + a7 + "," + a8 + "," +
    // a9 + "," + a0);
    // if (var <= 0.0047)
    // {
    // if (var <= 0.0016)
    // return "still";
    // else
    // {
    // if (a5 <= 0.1532)
    // {
    // if (a1 <= 0.5045)
    // return "still";
    // else
    // return "walk";
    // }
    // else
    // return "still";
    // }
    // }
    // else
    // {
    // if (a3 <= 60.3539)
    // {
    // if (var <= 0.0085)
    // {
    // if (a8 <= 0.0506)
    // return "walk";
    // else
    // {
    // if (a2 <= 2.8607)
    // return "still";
    // else
    // return "walk";
    // }
    // }
    // else
    // {
    // if (a2 <= 2.7725)
    // {
    // if (a1 <= 13.0396)
    // return "walk";
    // else
    // return "still";
    // }
    // else
    // return "walk";
    // }
    // }
    // else
    // return "run";
    // }
    //
    // }

    // private static String activity(double acc_var, double accgz1, double
    // accgz2, double accgz3, float gps_speed, double var, double avg, double
    // a1, double a2, double a3, double a4, double a5,
    // double a6, double a7, double a8, double a9, double a0)
    // {
    // String output = "still";
    // Log.d(TAG, "Features: speed=" + gps_speed + " var=" + acc_var + " gz1=" +
    // accgz1 + " gz2=" + accgz2 + " gz3=" + accgz3);
    // if (gps_speed <= 0.29)
    // output = indoorActivity(var, avg, a1, a2, a3, a4, a5, a6, a7, a8, a9,
    // a0);// "still";
    // else if (accgz3 <= 2663606.69633)
    // if (gps_speed <= 6.37)
    // if (accgz2 <= 463400.011249)
    // if (acc_var <= 205.972492)
    // if (acc_var <= 13.084102)
    // if (gps_speed <= 0.8)
    // output = "still";
    // else
    // output = "drive";// "bike";
    // else if (gps_speed <= 1.33)
    // output = "still";// "bike";
    // else
    // output = "drive";
    // else if (gps_speed <= 1.84)
    // if (accgz1 <= 125502.942136)
    // output = "walk";// "bike";
    // else
    // output = "walk";
    // else
    // output = "bike";// "bike";
    //
    // else if (acc_var <= 41153.783729)
    // if (gps_speed <= 2.12)
    // output = "walk";
    // else
    // output = "bike";
    // else
    // output = "run";
    // else
    // output = "drive";
    //
    // else if (accgz3 <= 5132319.94693)
    // if (gps_speed <= 1.86)
    // output = "walk";// bike
    // else
    // output = "run";
    // else
    // output = "run";
    // Log.d(TAG, output);
    // return output;
    // }

    // private double historySize = 5.;
    private final double wifiCheckLength = 10 * 60 * 1000; // 11 minutes
    private final double locCheckLength = 6 * 60 * 1000;
    /**
     * Gets distance as a naïve Euclidean distance between lat/lon points.
     * @param loc1
     * @param loc2
     * @return
     */
    private double NaïveDistance(Location loc1, Location loc2) {
		return Math.sqrt(Math.pow(loc1.getLatitude() - loc2.getLatitude(), 2) + Math.pow(loc1.getLongitude() - loc2.getLongitude(), 2));
	}
    
    private double Distance(LocationPoint loc1, LocationPoint loc2) {
    	
	    double pk = (float) (180/3.14169);

	    double a1 = loc1.getLatitude() / pk;
	    double a2 = loc1.getLongitude() / pk;
	    double b1 = loc2.getLatitude() / pk;
	    double b2 = loc2.getLongitude() / pk;

	    double t1 = Math.cos(a1)*Math.cos(a2)*Math.cos(b1)*Math.cos(b2);
	    double t2 = Math.cos(a1)*Math.sin(a2)*Math.cos(b1)*Math.sin(b2);
	    double t3 = Math.sin(a1)*Math.sin(b1);
	    double tt = Math.acos(t1 + t2 + t3);
	   
	    return 6366000*tt;
    	
    }
    
    private Classification checkWifi(JSONObject jsonObject) throws JSONException {
        // load previous
        SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
        String APsFromLastTimeStr = settings.getString(WIFI_HISTORY, null); // compare
                                                                            // with
                                                                            // previous
                                                                            // sample
        Classification wifiClass = new Classification();
        wifiClass.setWifiMode(UNKNOWN);
        if(jsonObject.length() == 0) {
            // WiFi wasn't able to give us any information.
            return wifiClass; // UNKNOWN
        }

        long time = jsonObject.getLong("time");

        if (APsFromLastTimeStr != null) {
            HashMap<Long, Vector<String>> lastAPs = new HashMap<Long, Vector<String>>();
            String[] lines = APsFromLastTimeStr.split("\n");
            long lastTime = Long.parseLong(lines[0]);
            String lastClassificationString = lines[1];
//            String [] lastFeatures = lines[1].split(";");
//            String lastMode = UNKNOWN;
	            
	        int lastTotal = Integer.parseInt(lines[1]);
	        int lastMatching = Integer.parseInt(lines[2]);
            
            Vector<String> APsFromLastTimes = new Vector<String>();
            // Log.d(TAG, "aps from last time object: " + APsFromLastTimeStr);
            // Log.d(TAG, APsFromLastTimes.size() + " previous scans");
            int count = 0;
            for (int l = 3; l < lines.length; l++) {
                try {
                    Vector<String> prev = new Vector<String>();
                    String[] APStrs = lines[l].split(",");
                    long prevTimestamp = Long.parseLong(APStrs[0]);
                    for (int m = 1; m < APStrs.length; m++) {
                        String str = APStrs[m];
                        APsFromLastTimes.add(str);
                        prev.add(str);
                    }
                    lastAPs.put(prevTimestamp, prev);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Malformed timestamp in line " + l + " of previous strings: \""
                            + lines[1] + "\"", e);
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            // Log.d(TAG, "AP from last time exists and is well-formed:\n"
            // + APsFromLastTimeStr);
            Vector<String> APs = JSONToList(jsonObject);

            // compare to APsFromLastTime
            int same = 0;
            int total = 0;
            if (lastTime == time) // no new wifi data
            {
                Log.v(TAG, "Returning previous value since there has been no new WiFi data");
//                wifiClass.setWifiMode(lastMode);
                wifiClass.setWifiTotal(lastTotal);
                wifiClass.setWifiRecogTotal(lastMatching);
                return wifiClass;
            }
            Log.i(TAG, "Current wifi is " + (System.currentTimeMillis() - time) / 60000
                    + " minutes old.");
            if (lastTime <= System.currentTimeMillis() - 1000 * 60 * wifiCheckLength) // if no
                                                                       // recent
                                                                       // wifi
                                                                       // for
                                                                       // comparison
            {
                Log.i(TAG, "Last stored wifi was ages (" + (System.currentTimeMillis() - lastTime)
                        / 60000 + " minutes) ago .");
                writeWifi(settings, time, APs, null, 0, 0);
                return wifiClass; // UNKNOWN
            } else
                Log.i(TAG, "Last stored wifi is " + (System.currentTimeMillis() - lastTime) / 60000
                        + " minutes old.");
            // Now we can do the comparison
            for (String AP : APs) {
                if (APsFromLastTimes.contains(AP))
                    same++;
                total++;
            }
            // for (String AP : APsFromLastTime)
            // {
            // if (APs.contains(AP)) // only count others that don't match. We
            // don't count the same ones again.
            // same++;
            // total++;
            // }
            Log.i(TAG, "There were " + same + " matches out of " + total
                    + " APs were in this sample. current:" + APs + " previous:" + APsFromLastTimes);
            writeWifi(settings, time, APs, lastAPs, total, same);
            wifiClass.setWifiRecogTotal(same);
            wifiClass.setWifiTotal(total);
            
            if (total > 0) {
//                int threshold = 2;
//                if (total <= 3)
//                    threshold = 1;
//                if (total == 1)
//                    threshold = 0;
//                if (same <= threshold) {
//                    Log.v(TAG, "Wifi chooses drive!");
//                    writeWifi(settings, time, DRIVE, APs, lastAPs, total, same);
//                    return DRIVE;// + " " + same / total;
//                } else {
//                    Log.v(TAG, "Wifi chooses still!");
//                    writeWifi(settings, time, STILL, APs, lastAPs, total, same);
//                    return STILL;// + " " + same / total;
//                }

            } else {
                Log.v(TAG, "No wifi detected in new sample; it's up to the GPS.");
                writeWifi(settings, time, APs, lastAPs, total, same);
                return wifiClass; // UNKNOWN
            }
            return wifiClass;
        } else {
            Log.v(TAG, "No previous AP!");
            // no history
            Vector<String> APs = JSONToList(jsonObject);
            writeWifi(settings, time, APs, null, 0, 0);
            return wifiClass;
        }

    }

    private Classification checkLocation(LocationPoint loc) throws JSONException {
        // load previous
        SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
        String locsFromLastTimeStr = settings.getString(LOC_HISTORY, null); // compare
                                                                            // with
                                                                            // previous
                                                                            // samples
        Classification locClass = new Classification();
//        locClass.setiMode(UNKNOWN);
        if (Double.isNaN(loc.getLatitude()) || Double.isNaN(loc.getLongitude()))
        {
            // WiFi wasn't able to give us any information.
            return locClass; // UNKNOWN
        }

        long time = loc.getTime();

        if (locsFromLastTimeStr != null) {
//            HashMap<Long, LocationPoint> lastLocations = new HashMap<Long, LocationPoint>();
//            ArrayList<Long> times = new ArrayList<Long>();
            ArrayList<LocationPoint> lastLocations = new ArrayList<LocationPoint>();
            String[] lines = locsFromLastTimeStr.split("\n");
            long lastTime = Long.parseLong(lines[0]);
//            String lastClassificationString = lines[1];
//            String [] lastFeatures = lines[1].split(";");
//            String lastMode = UNKNOWN;
	            
	        double lastRadius = Double.parseDouble(lines[1]);
	        double lastTravelled = Double.parseDouble(lines[2]);
	        
            
            Vector<String> locsFromLastTimes = new Vector<String>();
            // Log.d(TAG, "aps from last time object: " + APsFromLastTimeStr);
            // Log.d(TAG, APsFromLastTimes.size() + " previous scans");
            int count = 0;
            for (int l = 3; l < lines.length; l++) {
                try {
//                    Vector<String> prev = new Vector<String>();
                    String[] locStrs = lines[l].split(",");
                    long prevTimestamp = Long.parseLong(locStrs[0]);
                    if (prevTimestamp <= System.currentTimeMillis() - 1000 * 60 * locCheckLength)
                    	continue;
                    LocationPoint prev = new LocationPoint(Double.parseDouble(locStrs[1]), Double.parseDouble(locStrs[2]), prevTimestamp);
//                    times.add(prevTimestamp);
                    lastLocations.add(prev);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Malformed timestamp in line " + l + " of previous strings: \""
                            + lines[1] + "\"", e);
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            

            // Log.d(TAG, "AP from last time exists and is well-formed:\n"
            // + APsFromLastTimeStr);
//            Vector<String> locs = JSONToList(jsonObject);

            // compare to locsFromLastTime
            double radius = 0;
            double travelled = 0;
            if (lastTime == time) // no new wifi data
            {
                Log.v(TAG, "Returning previous value since there has been no new location data");
//                wifiClass.setWifiMode(lastMode);
                locClass.setRadius(lastRadius);
                locClass.setTravelled(lastTravelled);
                return locClass;
            }
            
         // Add current to same list if it's new
//            times.add(time);
            lastLocations.add(loc);
            
            Log.i(TAG, "Current location is " + (System.currentTimeMillis() - time) / 60000
                    + " minutes old.");
            if (lastTime <= System.currentTimeMillis() - 1000 * 60 * locCheckLength) // if no
                                                                       // recent
                                                                       // location
                                                                       // for
                                                                       // comparison
            {
                Log.i(TAG, "Last stored wifi was ages (" + (System.currentTimeMillis() - lastTime)
                        / 60000 + " minutes) ago .");
                writeLocations(settings, time, loc, null, radius, travelled);
                return locClass; // UNKNOWN
            } else
                Log.i(TAG, "Last stored wifi is " + (System.currentTimeMillis() - lastTime) / 60000
                        + " minutes old.");
            // Now we can do the comparison
//            for (String AP : APs) {
//                if (locsFromLastTimes.contains(AP))
//                    same++;
//                total++;
//            }
            // for (String AP : APsFromLastTime)
            // {
            // if (APs.contains(AP)) // only count others that don't match. We
            // don't count the same ones again.
            // same++;
            // total++;
            // }
//            Log.i(TAG, "There were " + same + " matches out of " + total
//                    + " APs were in this sample. current:" + APs + " previous:" + APsFromLastTimes);
            
//            ArrayList<Long> timestamps = new ArrayList<Long>();
//            for ()
//            {
//            	
//            }
            double maxDist = 0;
			travelled = Distance(loc, lastLocations.get(0));
			
            for (LocationPoint lp : lastLocations)
            {
            	for (LocationPoint lp2 : lastLocations)
				{
					double dist = Distance(lp, lp2);
					if (dist > maxDist)
						maxDist = dist;
				}
            }
            //radius = maxDist / 2; // to laggy to detect stops
            
            	
            if (lastLocations.size() >= 2)
            {
            	int index = lastLocations.size() - 2;
                while (index >= 0)
                {
                	if (lastLocations.get(lastLocations.size() - 1).getTime() > 60  * 1000 + lastLocations.get(index).getTime())
                	{
                		radius = Distance(lastLocations.get(lastLocations.size() - 1), lastLocations.get(index)) / 2;
                		break;
                	}
                	index--;
                }
            }            	
            
            writeLocations(settings, time, loc, lastLocations, radius, travelled);
            
            
            
            
            locClass.setRadius(radius);
            locClass.setTravelled(travelled);
            
//            if (total > 0) {
////                int threshold = 2;
////                if (total <= 3)
////                    threshold = 1;
////                if (total == 1)
////                    threshold = 0;
////                if (same <= threshold) {
////                    Log.v(TAG, "Wifi chooses drive!");
////                    writeWifi(settings, time, DRIVE, APs, lastAPs, total, same);
////                    return DRIVE;// + " " + same / total;
////                } else {
////                    Log.v(TAG, "Wifi chooses still!");
////                    writeWifi(settings, time, STILL, APs, lastAPs, total, same);
////                    return STILL;// + " " + same / total;
////                }
//
//            } else {
////                Log.v(TAG, "No wifi detected in new sample; it's up to the GPS.");
//                writeWifi(settings, time, APs, lastAPs, total, same);
//                return locClass; // UNKNOWN
//            }
            return locClass;
        } else {
            Log.v(TAG, "No previous Location!");
            // no history
//            LocationPoint loc = loc;
            writeLocations(settings, time, loc, null, 0, 0);
            return locClass;
        }

    }
    
    
    
    private void writeWifi(SharedPreferences settings, long time, Vector<String> APs,
            HashMap<Long, Vector<String>> lastAPs, int total, int same) {
        StringBuilder store = new StringBuilder(time + "\n" + total + "\n" + same + "\n" + time);
        for (String s : APs)
            store.append(",").append(s);
        long now = System.currentTimeMillis();
        if (lastAPs != null) {

            for (Long ts : lastAPs.keySet()) {
                if (ts >= now - wifiCheckLength) {
                    store.append("\n").append(ts);
                    for (String ap : lastAPs.get(ts))
                        store.append(",").append(ap);
                }
            }

        }

        Editor editor = settings.edit();
        editor.putString(WIFI_HISTORY, store.toString());
        editor.commit();
    }
    
    private void writeLocations(SharedPreferences settings, long time, LocationPoint location,
            ArrayList<LocationPoint> lastLocs, double radius, double travelled) {
        StringBuilder store = new StringBuilder(time + "\n" + radius + "\n" + travelled + "\n");
        store.append(location);
        long now = System.currentTimeMillis();
        if (lastLocs != null) {

            for (LocationPoint lp : lastLocs) {
                if (lp.getTime() >= now - locCheckLength) {
                    store.append("\n").append(lp);
//                    store.append(",").append(lp.getLatitude());
//                    store.append(",").append(lp.getLongitude());
                }
            }

        }
        Log.d(TAG, "Writing location: " + store.toString());
        Editor editor = settings.edit();
        editor.putString(LOC_HISTORY, store.toString());
        editor.commit();
    }

    private Vector<String> JSONToList(JSONObject jsonObject) throws JSONException {
        Vector<String> list = new Vector<String>();
        int strsum = 0, strcount = 0;
        JSONObject ap;
        JSONArray array = jsonObject.getJSONArray("scan");
        for (int i = 0; i < array.length(); i++) {
            ap = array.getJSONObject(i);
            strsum += ap.getInt("strength");
            strcount++;
            // if (ap.getInt("strength") > -50)
            // {
            // list.add(ap.getString("ssid"));
            // // Log.d(TAG, "Adding \"" + ap.getString("ssid") + "\" to APs");
            // }
        }
        if (list.size() == 0 && strcount > 0) {
            double avg = strsum / strcount;
            for (int i = 0; i < array.length(); i++) {
                ap = array.getJSONObject(i);
                // strsum += ap.getInt("strength");
                // strcount++;
                if (ap.getInt("strength") >= avg) {
                    list.add(ap.getString("ssid"));
                }
            }
        }

        return list;
    }

    /**
     * This is the main classification method. Updated code after retraining 11/28/2013
     * 
     * @param acc_var
     * @param accgz1
     * @param accgz2
     * @param accgz3
     * @param gps_speed
     * @param avg
     * @param var
     * @param a1
     * @param a2
     * @param a3
     * @param a4
     * @param a5
     * @param a6
     * @param a7
     * @param a8
     * @param a9
     * @param a0
     * @return Classification object with the mode
     */
    private String oldactivity(Float gps_speed, double avg, double var, double a1, double a2,
            double a3, double a4, double a5, double a6, double a7, double a8, double a9, double a0) {
        String output = STILL;

        if (var <= 0.016791) {
            if (a6 <= 0.002427) {
                /*
                 * if(a7 <= 0.001608) {
                 */
                if (gps_speed <= 0.791462 || gps_speed.isNaN())// || gps_speed
                                                               // !=
                                                               // Double.NaN)
                {

                    // if(avg <= 0.963016)
                    // {
                    // output = STILL;
                    // }
                    // else if(avg <= 0.98282)
                    // {
                    // output = DRIVE;Log.d(TAG, "Drive 0 because gps speed is "
                    // + gps_speed + " and avg is " + avg);
                    // }
                    // else if(avg <= 1.042821)
                    // {
                    // if(avg <= 1.040987)
                    // {
                    // if(avg <= 1.037199)
                    // {
                    // if(avg <= 1.03592)
                    // {
                    // output = STILL;
                    // }
                    // else
                    // {
                    // output = DRIVE;Log.d(TAG, "Drive 1");
                    // }
                    // }
                    // else
                    // {
                    // output = STILL;
                    // }
                    // }
                    // else
                    // {
                    // output = DRIVE;Log.d(TAG, "Drive 2");
                    // }
                    // }
                    // else
                    {
                        output = STILL;
                    }
                } else {
                    output = DRIVE;
                    Log.v(TAG, "Drive 3");
                }
                /*
                 * } else { output = DRIVE;Log.d(TAG, "Drive 4"); }
                 */
            } else if (gps_speed <= 0.791462 || gps_speed.isNaN())// &&
                                                                  // gps_speed
                                                                  // !=
                                                                  // Double.NaN)
            {
                output = STILL;
            } else {
                output = DRIVE;
                Log.v(TAG, "Drive 5");
            }
        } else {
            if (a3 <= 16.840921) {
                output = WALK;
            } else {
                output = RUN;
            }
        }

        return output;

    }
    
    private String activity(Float gps_speed, double avg, double var, double a1, double a2,
            double a3, double a4, double a5, double a6, double a7, double a8, double a9, double a0, 
            int totalWiFi, int matchingWifi, double radius, double travelled) {
//        String output = STILL;
        double ratio = 0;
    	if (totalWiFi > 0)
        	ratio = (double)matchingWifi / totalWiFi;
    	if (var <= 0.038625)
			if ((matchingWifi <= 3 && ratio <= .380952 && totalWiFi > 0) || radius > 108)
				return DRIVE;
			else
				return STILL;
		else return WALK;
    	
    	
//		if (matchingWifi <= 3)
//		{
//			if (var <= 0.038625)
//			{
//				if (radius <= 238.44889)//0.002696)
//				{
////					if (radius <= 0) // TODO more robust 
////						return DRIVE;
////					else
////					{
////						if (avg <= 1.002447)
//							return STILL;
////						else
////							return WALK;
////					}
//				}
//				else
//					return DRIVE;
//			}
//			else
//				return WALK;
//		}
//		else
//		{
//			if (a2 <= .314018)
//				return STILL;
//			else
//				return WALK;
//			
//		}
    	
        

//        return output;

    }
    
    
    private String activityBefore1_4_7(Float gps_speed, double avg, double var, double a1, double a2,
            double a3, double a4, double a5, double a6, double a7, double a8, double a9, double a0) {
        String output = STILL;

        if (var <= 0.016791) {
            if (a6 <= 0.002427) {
                /*
                 * if(a7 <= 0.001608) {
                 */
                if (gps_speed <= 0.791462 || gps_speed.isNaN())// || gps_speed
                                                               // !=
                                                               // Double.NaN)
                {

                    // if(avg <= 0.963016)
                    // {
                    // output = STILL;
                    // }
                    // else if(avg <= 0.98282)
                    // {
                    // output = DRIVE;Log.d(TAG, "Drive 0 because gps speed is "
                    // + gps_speed + " and avg is " + avg);
                    // }
                    // else if(avg <= 1.042821)
                    // {
                    // if(avg <= 1.040987)
                    // {
                    // if(avg <= 1.037199)
                    // {
                    // if(avg <= 1.03592)
                    // {
                    // output = STILL;
                    // }
                    // else
                    // {
                    // output = DRIVE;Log.d(TAG, "Drive 1");
                    // }
                    // }
                    // else
                    // {
                    // output = STILL;
                    // }
                    // }
                    // else
                    // {
                    // output = DRIVE;Log.d(TAG, "Drive 2");
                    // }
                    // }
                    // else
                    {
                        output = STILL;
                    }
                } else {
                    output = DRIVE;
                    Log.v(TAG, "Drive 3");
                }
                /*
                 * } else { output = DRIVE;Log.d(TAG, "Drive 4"); }
                 */
            } else if (gps_speed <= 0.791462 || gps_speed.isNaN())// &&
                                                                  // gps_speed
                                                                  // !=
                                                                  // Double.NaN)
            {
                output = STILL;
            } else {
                output = DRIVE;
                Log.v(TAG, "Drive 5");
            }
        } else {
            if (a3 <= 16.840921) {
                output = WALK;
            } else {
                output = RUN;
            }
        }

        return output;

    }

    public String activity1(double gps_speed, double avg, double var, double a1, double a2,
            double a3, double a4, double a5, double a6, double a7, double a8, double a9, double a0) {
        if (a3 <= 0.096835) {
            if (a6 <= 0.002421) {
                if (a5 <= 0.002208) {
                    return STILL;// (15611.0/4731.0)
                } else {
                    return DRIVE;// a5 > 0.002208: drive (2238.0/841.0)
                }
            } else
            // if (a6 > 0.002421)
            {
                if (avg <= 1.066716) {
                    if (gps_speed <= 5) {
                        return BIKE;// (2723.27/1784.81)
                    } else {
                        return DRIVE;// speed > 5: drive (5528.73/350.99)
                    }
                } else {
                    return BIKE;// average > 1.066716: bike (2358.0/423.0)
                }
            }
        } else {// a3 > 0.096835
            if (a3 <= 21.993029) {
                if (a2 <= 6.32047) {
                    return BIKE;// : bike (8924.0/3077.0)
                } else
                // a2 > 6.32047
                {
                    if (var <= 0.139588) {
                        return WALK;// : walk (7759.0/323.0)
                    } else
                    // (var > 0.139588)
                    {
                        return BIKE;// : bike (2251.0/1265.0)
                    }
                }
            } else {
                return RUN;// a3 > 21.993029: run (9217.0/90.0)
            }
        }
    }

    private static double goertzel(ArrayList<Double> accData, double freq, double sr) {
        double s_prev = 0;
        double s_prev2 = 0;
        double coeff = 2 * Math.cos((2 * Math.PI * freq) / sr);
        double s;
        for (int i = 0; i < accData.size(); i++) {
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

    public void addTransportMode(String mode, Vector<ArrayList<Double>> samples, float speed,
            float accuracy, String provider, String status, long timestamp, String wifiData,
            double lat, double lon) {
        long time = System.currentTimeMillis();// resJson.setAndReturnTime();

        // Open the database, and store the response
        
        tmdb.createRow(mode, time, status, speed, timestamp, accuracy, provider, wifiData, samples,
                lat, lon);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        tmdb = new MobilityDbAdapter(this);
        if (!Mobility.gpsConnected)
            this.getApplicationContext().bindService(
                    new Intent(IWiFiGPSLocationService.class.getName()),
                    Mobility.mConnection, Context.BIND_AUTO_CREATE);
        if (!Mobility.accelConnected)
            this.getApplicationContext().bindService(new Intent(IAccelService.class.getName()),
                    Mobility.accelServiceConnection, Context.BIND_AUTO_CREATE);
        getTransportMode();
        stopSelf();
    }

}
