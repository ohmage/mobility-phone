package edu.ucla.cens.mobility.glue;

import java.net.URI;

import edu.ucla.cens.mobility.Mobility;
import edu.ucla.cens.mobility.MobilityControl;


import android.R;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.widget.Toast;

public class MobilityInterface
{
	public static final String KEY_MODE = "mode";
	public static final String KEY_SPEED = "speed";
	public static final String KEY_STATUS = "status";
	public static final String KEY_LOC_TIMESTAMP = "location_timestamp";
	public static final String KEY_ACCURACY = "accuracy";
	public static final String KEY_PROVIDER = "provider";
	public static final String KEY_WIFIDATA = "wifi_data";
	public static final String KEY_ACCELDATA = "accel_data";
	public static final String KEY_TIMEZONE = "timezone";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TIME = "time";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	private static String [] columns = {KEY_ROWID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE};
	// Content provider strings
	public static final String AUTHORITY = "edu.ucla.cens.mobility.MobilityContentProvider";
	public static final String PATH_MOBILITY = "mobility";
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY + "/" + PATH_MOBILITY);
	/**
	 * Helper function to get cursor to data with only the last retrieved timestamp.
	 * @param timestamp
	 * @return cursor to the data after the timestamp
	 */
	public static Cursor getMobilityCursor(Context context, Long timestamp)
	{
		ContentResolver r = context.getContentResolver();
		
		return r.query(CONTENT_URI, columns, KEY_TIME + " > ?", new String[] {String.valueOf(timestamp)}, KEY_TIME);
	}
	
	
	public static void showMobilityOptions(Context context)
	{
//		context.startActivity(new Intent(context, MobilityControl.class));
		try
		{
			final Intent intentDeviceTest = new Intent("android.intent.action.MAIN");                
			intentDeviceTest.setComponent(new ComponentName("edu.ucla.cens.mobility","edu.ucla.cens.mobility.MobilityControl"));
			context.startActivity(intentDeviceTest);
		}
		catch(Exception e)
		{
			Toast.makeText(context, "There was an error. Please verify that Mobility has been installed.", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	
}
