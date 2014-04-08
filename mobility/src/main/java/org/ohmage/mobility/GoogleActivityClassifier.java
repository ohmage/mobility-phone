package org.ohmage.mobility;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;

import com.google.android.gms.location.ActivityRecognitionClient;

public class GoogleActivityClassifier 
implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener 
{
	
	private static final String TAG = "GoogleActivityClassifier";
	Context mContext;
	ActivityRecognitionClient mActivityRecognitionClient;
	long mSampleRate;
	
	private static String googlemode = ClassifierService.UNKNOWN;
	private static Object googlesemaphore = new Object();
	private static long googletime = System.currentTimeMillis();
	private static String secondmode = ClassifierService.UNKNOWN;
	
	
	public static void setGooglemode(String activity)
	{
		synchronized (googlesemaphore)
        {
			googlemode = activity;
			googletime = System.currentTimeMillis();
        }
	}
	
	public static String getGooglemode()
	{
		synchronized (googlesemaphore)
        {
			if (System.currentTimeMillis() - googletime <= 1.5 * Mobility.sampleRate)
				return googlemode;
			else
				return ClassifierService.ERROR;
        }
	}
	
	public static List<String> getGooglemodes()
	{
		synchronized (googlesemaphore)
        {
			List<String> list = new ArrayList<String>();
			if (System.currentTimeMillis() - googletime <= 1.5 * Mobility.sampleRate)
			{
				list.add(googlemode);
				list.add(secondmode);
				return list;
			}
			else
			{
				list.add(ClassifierService.ERROR);
				list.add(ClassifierService.ERROR);
				return list;
			}
        }
	}
	
	public static void setGooglemodes(String activity, String second)
	{
		synchronized (googlesemaphore)
        {
			googlemode = activity;
			secondmode = second;
			googletime = System.currentTimeMillis();
        }
	}
	
	
	public GoogleActivityClassifier(Context context, long milliseconds)
	{
		mContext = context;
		// Connect to the ActivityRecognitionService
		Log.d(TAG, "Starting GAC");
		mSampleRate = milliseconds;
		mActivityRecognitionClient =
		    new ActivityRecognitionClient(context, this, this);
		mActivityRecognitionClient.connect();
		

		
	}

	
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
		Log.e(TAG, "Connection to GAC failed");
	}
	PendingIntent callbackIntent = null;
	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "GAC Connection happened at " + mSampleRate);
		Intent intent = new Intent(mContext, GoogleActivityClassificationReceiver.class);
	    callbackIntent = PendingIntent.getService(mContext, 0, intent,
	            PendingIntent.FLAG_UPDATE_CURRENT);
	    mActivityRecognitionClient.requestActivityUpdates(mSampleRate, callbackIntent);
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}

	public void stop() 
	{
		if (mActivityRecognitionClient != null)
		{
			if (callbackIntent != null)
				mActivityRecognitionClient.removeActivityUpdates(callbackIntent);
			callbackIntent.cancel();
			mActivityRecognitionClient.disconnect();
		}
	}



}
