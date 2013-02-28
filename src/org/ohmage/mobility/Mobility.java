
package org.ohmage.mobility;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.IBinder;
import android.os.RemoteException;

import org.ohmage.accelservice.IAccelService;
import org.ohmage.logprobe.Log;
import org.ohmage.logprobe.LogProbe;
import org.ohmage.mobility.blackout.Blackout;
import org.ohmage.mobility.blackout.BlackoutDesc;
import org.ohmage.mobility.blackout.base.TriggerDB;
import org.ohmage.mobility.blackout.base.TriggerInit;
import org.ohmage.mobility.blackout.utils.SimpleTime;
import org.ohmage.probemanager.ProbeBuilder;
import org.ohmage.wifigpslocation.IWiFiGPSLocationService;

//import android.widget.Toast;

public class Mobility {
    public static final int NOTIF_ERROR_ID = 124;

    static final boolean debugMode = false;
    static Location globalLoc;
    static boolean setInterval = false;
    private static PendingIntent startPI = null;
    static boolean initialized = false;
    // private static PendingIntent stopPI = null;
    // static NotificationManager nm;
    // static Notification notification;
    public static final String TAG = "Mobility";
    public static final String SERVICE_TAG = "Mobility";
    public static final String MOBILITY = "mobility";
    public static final String SAMPLE_RATE = "sample rate";
    public static final String LAST_INSERT = "last_insert";
    public static final String ACC_START = "org.ohmage.mobility.record";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_OK = "mobility";
    public static final String STATUS_ERROR = "error";
    public static final String STATUS_BLACKOUT = "blackout";
    public static final String KEY_USERNAME = "key_username";

    // public static Context appContext = null;
    static AlarmManager mgr;

    static long sampleRate = 60000;
    static boolean intervalSet = false;
    static boolean accelConnected = false;
    static boolean gpsConnected = false;
    // public static boolean mConnected = false;
    public static ServiceConnection accelServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            setmAccel(IAccelService.Stub.asInterface(service));
            // mConnected = true;
            try {
                getmAccel().start(SERVICE_TAG);
                getmAccel().suggestRate(SERVICE_TAG, SensorManager.SENSOR_DELAY_GAME);
                getmAccel().suggestInterval(SERVICE_TAG, (int) sampleRate);

                Log.v(TAG, "START WAS CALLED ON ACCEL");
                accelConnected = true;
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // accelConnected = true;
            Log.v(TAG, "Connected to accel service");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.v(TAG, "onServiceDisconnected was called!");
            // try
            // {
            // getmAccel().stop(SERVICE_TAG);
            // Log.i(TAG, "Successfully stopped service!");
            //
            // } catch (RemoteException e)
            // {
            // Log.e(TAG, "Failed to stop service!");
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            accelConnected = false;
            setmAccel(null);
            // mConnected = false;
        }
    };
    private static IAccelService mAccel;

    // private static Service bootService = null;
    // public static void start(Context context, Service boot)
    // {
    // bootService = boot;
    // start(context);
    // }

    public static void start(Context context) {
        TriggerDB db = new TriggerDB(context);
        db.open();
        boolean canRunNow = true;
        Cursor c = db.getAllTriggers();
        if (c.moveToFirst()) {
            do {
                int trigId = c.getInt(c.getColumnIndexOrThrow(TriggerDB.KEY_ID));

                String trigDesc = db.getTriggerDescription(trigId);
                BlackoutDesc conf = new BlackoutDesc();

                if (!conf.loadString(trigDesc)) {
                    continue;
                }
                SimpleTime start = conf.getRangeStart();
                SimpleTime end = conf.getRangeEnd();
                SimpleTime now = new SimpleTime();
                Log.i(TAG,
                        start.getHour() + ":" + start.getMinute() + " until " + end.getHour() + ":"
                                + end.getMinute() + " is blackout and now is " + now.getHour()
                                + now.getMinute());
                if (!start.isAfter(now) && end.isAfter(now)) {
                    canRunNow = false;
                }

            } while (c.moveToNext());
        }
        c.close();
        db.close();
        TriggerInit.initTriggers(context);
        // if (!initialized)
        initialize(context);
        if (canRunNow) {
            Log.v(TAG, "Starting mobility!");
            startMobility(context);

        }
        GarbageCollectReceiver.scheduleGC(context, mgr);
    }

    public static void stop(Context context) {

        TriggerDB db = new TriggerDB(context);
        db.open();
        boolean runningNow = true;
        Cursor c = db.getAllTriggers();
        if (c.moveToFirst()) {
            do {
                int trigId = c.getInt(c.getColumnIndexOrThrow(TriggerDB.KEY_ID));

                String trigDesc = db.getTriggerDescription(trigId);
                BlackoutDesc conf = new BlackoutDesc();

                if (!conf.loadString(trigDesc)) {
                    continue;
                }
                SimpleTime start = conf.getRangeStart();
                SimpleTime end = conf.getRangeEnd();
                SimpleTime now = new SimpleTime();
                if (!start.isAfter(now) && !end.isBefore(now)) {
                    runningNow = false;
                }
                new Blackout().stopTrigger(context, trigId, db.getTriggerDescription(trigId));

            } while (c.moveToNext());
        }
        c.close();
        db.close();
        // TriggerInit.initTriggers(context);
        if (runningNow) {
            Log.v(TAG, "Stopping mobility!");
            stopMobility(context, false);
        } else
            Log.v(TAG, "Not running, so ignoring stop command");
        LogProbe.close(context);
    }

    static int failCount = 0;

    public static void setNotification(Context context, String status, String message) {
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        // int icon = R.drawable.pending; // for debug
        int icon = R.drawable.mobility;
        if (status.equals(STATUS_OK))
            icon = R.drawable.mobility;
        else if (status.equals(STATUS_ERROR)) {
            // icon = R.drawable.error;
            setDebugNotification(context, message);
        } else if (status.equals(STATUS_BLACKOUT))
            icon = R.drawable.blackout;
        Notification notification = new Notification(icon, null, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_NO_CLEAR;
        Intent i = new Intent("org.ohmage.mobility.control");
        // i.

        // appContext = context;
        PendingIntent pi = PendingIntent.getActivity(context.getApplicationContext(), 1, i, 1);
        if (!debugMode && !(status.equals(STATUS_BLACKOUT) || status.equals(STATUS_OK)))
            message = "Click for Mobility options";
        notification.setLatestEventInfo(context.getApplicationContext(), "Mobility", message, pi);
        nm.notify(123, notification);
    }

    public static void setDebugNotification(Context context, String message) {
        // if (!debugMode)
        // return;
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        int icon = android.R.drawable.stat_notify_error;

        Notification notification = new Notification(icon, null, System.currentTimeMillis());
        // notification.flags |= Notification.FLAG_NO_CLEAR;
        // Intent i = new Intent("org.ohmage.mobility.control");
        // i.

        // appContext = context;
        PendingIntent pi = PendingIntent.getActivity(context.getApplicationContext(), 1,
                new Intent("org.ohmage.mobility.control"), 1);
        notification.setLatestEventInfo(context.getApplicationContext(), "MobilityError", message,
                pi);
        nm.notify(NOTIF_ERROR_ID, notification);
    }

    public static void startMobility(Context context) {
        Log.v(TAG, "Starting mobility service, no blackout!");
        setNotification(context, STATUS_PENDING, "Waiting for the first sensor sample");
        // nm = (NotificationManager)
        // context.getSystemService(Context.NOTIFICATION_SERVICE);
        // notification = new Notification(R.drawable.pending, null,
        // System.currentTimeMillis());
        // notification.flags |= Notification.FLAG_NO_CLEAR;
        //
        //
        // // appContext = context;
        // PendingIntent pi =
        // PendingIntent.getActivity(context.getApplicationContext(), 1, new
        // Intent(context, MobilityControl.class), 1);
        // notification.setLatestEventInfo(context.getApplicationContext(),
        // "Mobility", "Service Running", pi);
        // nm.notify(123, notification);

        SharedPreferences settings = context.getSharedPreferences(MOBILITY, Context.MODE_PRIVATE);
        sampleRate = (long) settings.getInt(SAMPLE_RATE, 60) * 1000;

        mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // context.registerReceiver(accReceiver, new IntentFilter(ACC_START));

        Log.i(TAG, "Sample rate is: " + sampleRate);
        startGPS(context, sampleRate);
        startAcc(context, sampleRate);
        // Toast.makeText(context, R.string.mobilityservicestarted,
        // Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Starting transport mode service with sampleRate: " + sampleRate);

    }

    public static void stopMobility(Context context, boolean blackout) {
        Log.v(TAG, "Stopping mobility service!");
        if (mgr == null) // just in case
            mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            Intent i = new Intent(ACC_START);
            startPI = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            mgr.cancel(startPI);
        } else {
            Log.w(TAG, "AlarmManager was null so it wasn't cancelled!");
        }
        stopAcc(context);
        stopGPS(context);
        // try
        // {
        // context.unregisterReceiver(accReceiver);
        // } catch (Exception e1)
        // {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
        // unregisterReceiver(stopAccReceiver);
        // lManager.removeUpdates(lListener);
        // try
        // {
        // // if (getmWiFiGPS() != null)
        // // getmWiFiGPS().stop(SERVICE_TAG);
        // // if (getmAccel() != null)
        // // getmAccel().stop(SERVICE_TAG);
        //
        // // try
        // // {
        // // context.unbindService(mConnection);
        // // } catch (Exception e)
        // // {
        // // // TODO Auto-generated catch block
        // //// e.printStackTrace();
        // // }
        //
        // // unbindService(Log.SystemLogConnection);
        // } catch (Exception e)
        // {
        // // If it's not running then we don't care if this can't be unbound.
        // // Why does it want to crash?
        // e.printStackTrace();
        // }
        exterminate(context.getApplicationContext());
        // if (nm != null)
        {
            if (blackout) {
                setNotification(context, STATUS_BLACKOUT, "Mobility paused during blackout time");
            } else {
                Log.v(TAG, "Canceling notification!");
                NotificationManager nm = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                nm.cancel(123);
            }
        }
        // else
        // Log.d(TAG, "nm is null!");
    }

    private static void startAcc(Context context, long milliseconds) {
        // start accel
        if (accelConnected) {
            try {

                getmAccel().start(SERVICE_TAG);
                getmAccel().suggestRate(SERVICE_TAG, SensorManager.SENSOR_DELAY_GAME);
                getmAccel().suggestInterval(SERVICE_TAG, (int) sampleRate);

                Log.v(TAG, "START WAS CALLED ON ACCEL");
            } catch (RemoteException e) {
                Log.e(TAG, "Error starting accel", e);
            }
        } else
            initialize(context);

        Intent i = new Intent(ACC_START);
        startPI = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        mgr.setRepeating(AlarmManager.RTC_WAKEUP, 0, milliseconds, startPI);

        // context.bindService(new Intent(IAccelService.class.getName()),
        // AccelServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private static void stopAcc(Context context) {
        if (startPI != null)
            mgr.cancel(startPI);
        // if (stopPI != null)
        // mgr.cancel(stopPI);

        try {
            if (accelConnected && getmAccel() != null) {
                getmAccel().stop(SERVICE_TAG);
                Log.v(TAG, "Successfully stopped service!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop service!", e);
        }
        // try
        // {
        // // getmAccel().stop(SERVICE_TAG);
        // // context.unbindService(getmAccel());
        //
        //
        // Log.i(TAG, "Successfully unbound accel service");
        // }
        // catch (Exception e)
        // {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
    }

    private static void startGPS(Context context, long milliseconds) {
        if (gpsConnected && mWiFiGPS != null) {
            try {
                mWiFiGPS.start(SERVICE_TAG);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else
            initialize(context); // bind again just to be safe
        // Log.d(TAG, String.format("Sampling GPS at %d.", milliseconds /
        // 1000));
        // context.bindService(new
        // Intent(edu.ucla.cens.wifigpslocation.IWiFiGPSLocationService.class.getName()),
        // mConnection, Context.BIND_AUTO_CREATE);

    }

    private static void stopGPS(Context context) {
        try {
            getmWiFiGPS().stop(SERVICE_TAG);
            // context.unbindService(mConnection);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }

    }

    public static void setmWiFiGPS(IWiFiGPSLocationService mWiFiGPS) {
        Mobility.mWiFiGPS = mWiFiGPS;
    }

    public static IWiFiGPSLocationService getmWiFiGPS() {
        return mWiFiGPS;
    }

    public static void setmAccel(IAccelService mAccel) {
        Mobility.mAccel = mAccel;
    }

    public static IAccelService getmAccel() {
        return mAccel;
    }

    private static IWiFiGPSLocationService mWiFiGPS;
    public static ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service. We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            setmWiFiGPS(IWiFiGPSLocationService.Stub.asInterface(service));
            Log.v(TAG, "Connected to WiFiGPSLocation Service");
            try {
                mWiFiGPS.start(SERVICE_TAG);
                gpsConnected = true;
            } catch (RemoteException e) {
                Log.e(TAG, "Error starting wifigps", e);
            }
            // As part of the sample, tell the user what happened.
            Log.v(TAG, "Connected");

        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            setmWiFiGPS(null);
            gpsConnected = false;
            Log.v(TAG, "Disconnected from WiFiGPSLocation Service");

            // As part of the sample, tell the user what happened.

        }
    };

    public static void initialize(Context context) {
        Log.v(TAG, "Initializing");
        // ServiceState.sampleRate = sampleRate;
        context.bindService(
                new Intent(IWiFiGPSLocationService.class.getName()),
                mConnection, Context.BIND_AUTO_CREATE);
        context.bindService(new Intent(IAccelService.class.getName()), accelServiceConnection,
                Context.BIND_AUTO_CREATE);

        initialized = true;
    }

    public static void exterminate(Context context) {
        try {
            context.unbindService(mConnection);
            context.unbindService(accelServiceConnection);

            initialized = false;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
    }

    public static void restartAccelService(Context context) {
        Log.w(TAG,
                "There is an accel object that didn't start when I told it to. Accel connected: "
                        + accelConnected);
        try {
            if (mgr == null)
                mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE); // so
                                                                                      // start
                                                                                      // doesn't
                                                                                      // crash,
                                                                                      // move
                                                                                      // to
                                                                                      // start()
            stopAcc(context);
            startAcc(context, sampleRate);
            getmAccel().start(SERVICE_TAG);
            getmAccel().suggestRate(SERVICE_TAG, SensorManager.SENSOR_DELAY_GAME);
            getmAccel().suggestInterval(SERVICE_TAG, (int) sampleRate);

        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void writeProbe(Context context, ProbeBuilder probe, String mode, Float speed,
            String accel, String wifi) {
        if(MobilityApplication.probeWriter != null) {
            MobilityApplication.probeWriter.write(probe, mode, speed, accel, wifi);
        } else {
            Log.e(TAG, "Probewriter is null");
        }
    }

    // public static void unbindServices(Context context)
    // {
    // ServiceState.exterminate(context);
    // // try
    // // {
    // // context.unbindService(mConnection);
    // // context.unbindService(AccelServiceConnection);
    // // try
    // // {
    // // if (bootService != null)
    // // bootService.stopSelf();
    // // }
    // // catch (Exception e)
    // // {
    // // // If this breaks, then we didn't want it to happen anyway
    // // Log.d(TAG, "Failed stopping boot service");
    // // }
    // //// Intent i = new Intent(BootService.bound);
    // //// PendingIntent boundPI = PendingIntent.getBroadcast(context, 0, i,
    // PendingIntent.FLAG_CANCEL_CURRENT);
    // //
    // // } catch (Exception e)
    // // {
    // // // TODO Auto-generated catch block
    // //// e.printStackTrace();
    // // }
    // }

    // static BroadcastReceiver accReceiver = new BroadcastReceiver() // put
    // this sucker in its own file and register in manifest
    // {
    //
    // @Override
    // public void onReceive(Context context, Intent intent)
    // {
    // Log.e(TAG, "Start mobility service");
    // // Old way
    // // context.startService(new Intent(context, ClassifierService.class));
    // // wakeful intent service
    // WakefulIntentService.sendWakefulWork(context, ClassifierService.class);
    // }
    // };
    // static BroadcastReceiver GCReceiver = new BroadcastReceiver() // this one
    // too
    // {
    //
    // @Override
    // public void onReceive(Context context, Intent intent)
    // {
    // Log.d(TAG, "Collect garbage");
    // // Need context!
    // MobilityDbAdapter mda = new MobilityDbAdapter(context, "mobility",
    // "mobility", "mobility");
    // mda.open();
    // mda.deleteSomeRows(System.currentTimeMillis() - gctime * 24 * 3600 *
    // 1000);
    // mda.close();
    // }
    // };
}
