package edu.ucla.cens.systemlog;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;



public class Log
{
    private static final String DEFAULT_APP_NAME = "default";
    private static final String TAG = "CENS.SystemLog";

	private static ISystemLog sLogger;
	
	private static boolean sConnected = false;

    private static String sAppName = DEFAULT_APP_NAME;

    public static void setAppName(String name)
    {
        sAppName = name;
    }
		
    public static ServiceConnection SystemLogConnection 
        = new ServiceConnection() 
    {
        public void onServiceConnected(ComponentName className, 
                IBinder service) 
        {
            sLogger = ISystemLog.Stub.asInterface(service);
            sConnected = true;
        }

        public void onServiceDisconnected(ComponentName className) 
        {
            sLogger = null;
            sConnected = false;
        }
    };
    
    public static void register(String tag) 
    {
    	if (sConnected)
    	{
	    	try
	    	{
	    		sLogger.registerLogger(tag, sAppName);
	    	} 
	    	catch (RemoteException re)
	    	{
	    		android.util.Log.e(TAG, 
                        "Remote Exception when trying to register tag"
                        + tag, re);
	    	}
    	}
		else
		{
			android.util.Log.i(TAG, 
                    "Not connected to SystemLog. Could not register "
                    + tag);
		}    	
    }

    public static boolean isRegistered(String tag)
    {
    	boolean res = false;
    	if (sConnected)
    	{
	    	try
	    	{
	    		res =  sLogger.isRegistered(tag);
	    	}
	    	catch (RemoteException re)
	    	{
	    		android.util.Log.e(TAG, "Remote Exception", re);
	    		res =  false;
	    	}
    	}
    	else
    	{
    		android.util.Log.e(tag, "Not connected");
    		res = false;
    	}
    	return res;
    }

	
    public static void i (String tag, String message)
    {
    	if (sConnected)
    	{
	    	try
	    	{
                if (!sLogger.isRegistered(tag))
                    register(tag);

	    		sLogger.info(tag, message);
	    	}
	    	catch (RemoteException re)
	    	{
	    		android.util.Log.e(TAG, "Remote Exception", re);
	    	}
    	}
		else
		{
			android.util.Log.i(tag, message);
		}
    }
    
    public static void d (String tag, String message)
    {
    	if (sConnected)
    	{
	    	try
	    	{
                if (!sLogger.isRegistered(tag))
                    register(tag);

	    		sLogger.debug(tag, message);
	    	}
	    	catch (RemoteException re)
	    	{
	    		android.util.Log.e(TAG, "Remote Exception", re);
	    	}
    	}
		else
		{
			android.util.Log.d(tag, message);
		}
    }
    

    
    public static void e (String tag, String message, Exception e)
    {
    	if (sConnected)
    	{
	    	try
	    	{
                if (!sLogger.isRegistered(tag))
                    register(tag);

	    		sLogger.error(tag, message + e.getMessage());
	    	}
	    	catch (RemoteException re)
	    	{
	    		android.util.Log.e(TAG, "Remote Exception", re);
	    	}
    	}
    	else
    	{
    		android.util.Log.e(tag, message, e);
    	}
    }


    public static void e (String tag, String message)
    {
    	if (sConnected)
    	{
	    	try
	    	{
                if (!sLogger.isRegistered(tag))
                    register(tag);

	    		sLogger.error(tag, message);
	    	}
	    	catch (RemoteException re)
	    	{
	    		android.util.Log.e(TAG, "Remote Exception", re);
	    	}
    	}
    	else
    	{
    		android.util.Log.e(tag, message);
    	}
    }



    public static void v (String tag, String message)
    {
    	if (sConnected)
    	{
	    	try
	    	{
                if (!sLogger.isRegistered(tag))
                    register(tag);

	    		sLogger.verbose(tag, message);
	    	}
	    	catch (RemoteException re)
	    	{
	    		android.util.Log.e(TAG, "Remote Exception", re);
	    	}
    	}
    	else
    	{
    		android.util.Log.v(tag, message);
    	}
    }


    public static void w (String tag, String message)
    {
    	if (sConnected)
    	{
	    	try
	    	{
                if (!sLogger.isRegistered(tag))
                    register(tag);

	    		sLogger.warning(tag, message);
	    	}
	    	catch (RemoteException re)
	    	{
	    		android.util.Log.e(TAG, "Remote Exception", re);
	    	}
    	}
    	else
    	{
    		android.util.Log.w(tag, message);
    	}
    }


}
