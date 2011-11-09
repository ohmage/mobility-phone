package edu.ucla.cens.mobility.blackout.base;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

/*
 * The class which can parse and store the JSON string of the run time
 * info of each trigger.
 * 
 * An example run time description:
 * 
 * {
 * 		"trigger_timestamp": 12336673
 * 		"trigger_time_zone": ""
 * 	    
 * 		"trigger_location": {
 * 			"latitude": ...
 * 			"longitude": ...
 * 		    "accuracy": ...
 * 			"provider": ...
 * 			"time": ...	
 * 		}
 * }	
 */
public class TriggerRunTimeDesc {
	
	public static final long INVALID_TIMESTAMP = -1;
	
	private static final String TIME_STAMP_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private static final String KEY_TRIG_TIMESTAMP = "trigger_timestamp";
	private static final String KEY_TRIGGER_TIMEZONE = "trigger_time_zone";
	
	private static final String KEY_TRIGGER_LOCATION= "trigger_location";
	private static final String KEY_TRIGGER_LOC_LAT = "latitude";
	private static final String KEY_TRIGGER_LOC_LONG = "longitude";
	private static final String KEY_TRIGGER_LOC_ACC = "accuracy";
	private static final String KEY_TRIGGER_LOC_PROVIDER = "provider";
	private static final String KEY_TRIGGER_LOC_TIME = "time";
	
	private long mTrigTimeStamp = INVALID_TIMESTAMP;
	
	private double mTrigLocLat;
	private double mTrigLocLong;
	private float mTrigLocAccuracy;
	private String mTrigLocProvider;
	private long mTrigLocTime;
	private String mTrigTimeZone = "";

	private void initialize() {
		mTrigTimeStamp = INVALID_TIMESTAMP;
		mTrigTimeZone = "";
		
		mTrigLocLat = -1;
		mTrigLocLong = -1;
		mTrigLocAccuracy = -1;
		mTrigLocProvider = "";
		mTrigLocTime = INVALID_TIMESTAMP;
		
	}
	
	/*
	 * Parse a run time info JSON string and load into this
	 * object
	 */
	public boolean loadString(String desc) {
		
		initialize();
		
		if(desc == null) {
			return false;
		}
		
		try {
			JSONObject jDesc = new JSONObject(desc);
						
			if(jDesc.has(KEY_TRIG_TIMESTAMP)) {
				mTrigTimeStamp = jDesc.getLong(KEY_TRIG_TIMESTAMP);
				mTrigTimeZone = jDesc.getString(KEY_TRIGGER_TIMEZONE);
			}
			
			if(jDesc.has(KEY_TRIGGER_LOCATION)) {
				JSONObject jTrigLoc = jDesc.getJSONObject(KEY_TRIGGER_LOCATION);
				
				mTrigLocLat = jTrigLoc.getDouble(KEY_TRIGGER_LOC_LAT);
				mTrigLocLong = jTrigLoc.getDouble(KEY_TRIGGER_LOC_LONG);
				mTrigLocAccuracy = (float) jTrigLoc.getDouble(KEY_TRIGGER_LOC_ACC);
				mTrigLocProvider = jTrigLoc.getString(KEY_TRIGGER_LOC_PROVIDER);
				mTrigLocTime = jTrigLoc.getLong(KEY_TRIGGER_LOC_TIME);
			}
			
		} catch (JSONException e) {
			return false;
		}
		
		return true;
	}
	
	/*
	 * Check if there is a valid trigger time stamp
	 */
	public boolean hasTriggerTimeStamp() {
		return (mTrigTimeStamp != INVALID_TIMESTAMP);
	}
	
	/*
	 * Get the trigger time stamp
	 */
	public long getTriggerTimeStamp() {
		return mTrigTimeStamp;
	}
	
	/*
	 * Set the trigger time stamp
	 */
	public void setTriggerTimeStamp(long timeStamp) {
		mTrigTimeStamp = timeStamp;
		mTrigTimeZone = TimeZone.getDefault().getID();
	}
	
	/*
	 * Get the trigger location
	 */
	public Location getTriggerLocation() {
		Location loc = new Location(mTrigLocProvider);
		
		loc.setLatitude(mTrigLocLat);
		loc.setLongitude(mTrigLocLong);
		loc.setAccuracy(mTrigLocAccuracy);
		loc.setTime(mTrigLocTime);
		
		return loc;
	}
	
	/*
	 * Set the trigger location 
	 */
	public void setTriggerLocation(Location loc) {
		if(loc == null) {
			return;
		}

		mTrigLocLat = loc.getLatitude();
		mTrigLocLong = loc.getLongitude();
		mTrigLocAccuracy = loc.getAccuracy();
		mTrigLocProvider = loc.getProvider();
		mTrigLocTime = loc.getTime();
	}
	
	/*
	 * Format millisecond time stamp in human readable form
	 */
	private String millisToFormatedTimeStamp(long millis) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_STAMP_FORMAT);
		return  dateFormat.format(new Date(millis));
	}
	
	/*
	 * Convert this object into a JSON string. If humanReadable
	 * is true, the time stamp will be formatted in 
	 * yyyy-MM-dd HH:mm:ss form
	 */
	private String convertToString(boolean humanReadable) {
		JSONObject jDesc = new JSONObject();
		
		try {
			if(mTrigTimeStamp != INVALID_TIMESTAMP) {
				if(!humanReadable) {
					jDesc.put(KEY_TRIG_TIMESTAMP, mTrigTimeStamp);
				}
				else {
					jDesc.put(KEY_TRIG_TIMESTAMP, 
							  millisToFormatedTimeStamp(mTrigTimeStamp));
				}
				
				jDesc.put(KEY_TRIGGER_TIMEZONE, mTrigTimeZone);
			}
			if(mTrigLocTime != INVALID_TIMESTAMP) {
				JSONObject jTrigLoc = new JSONObject();
				
				jTrigLoc.put(KEY_TRIGGER_LOC_LAT, mTrigLocLat);
				jTrigLoc.put(KEY_TRIGGER_LOC_LONG, mTrigLocLong);
				jTrigLoc.put(KEY_TRIGGER_LOC_ACC, mTrigLocAccuracy);
				jTrigLoc.put(KEY_TRIGGER_LOC_PROVIDER, mTrigLocProvider);
				
				if(!humanReadable) {
					jTrigLoc.put(KEY_TRIGGER_LOC_TIME, mTrigLocTime);
				}
				else {
					jTrigLoc.put(KEY_TRIGGER_LOC_TIME,
								 millisToFormatedTimeStamp(mTrigLocTime));
				}
				
				jDesc.put(KEY_TRIGGER_LOCATION, jTrigLoc);
			}
			
		} catch (JSONException e) {
			return null;
		}
		
		return jDesc.toString();
	}
	
	/*
	 * Convert this object to a JSON string. 
	 */
	public String toString() {
		return convertToString(false);
	}
	
	/*
	 * Convert this object to a JSON string where the time stamps
	 * are in human readable format
	 * 
	 * Note: the loadString() cannot parse the output of this
	 * function
	 */
	public String toHumanReadableString() {
		return convertToString(true);
	}
	
	/*
	 * Get the default run time description for a trigger. 
	 * This can be used while adding a new trigger to the 
	 * database.
	 */
	public static String getDefaultDesc() {
		return new JSONObject().toString();
	}
}
