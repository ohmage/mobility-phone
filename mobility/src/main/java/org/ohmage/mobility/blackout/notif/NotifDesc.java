package org.ohmage.mobility.blackout.notif;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.mobility.blackout.config.NotifConfig;
import org.ohmage.mobility.blackout.utils.TrigPrefManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/*
 * The class which can parse and represent the trigger 
 * notification description in JSON.
 * 
 * An example notification description:
 * 
 * {
 * 		"duration": 60 //minutes
 * 		"suppression": 30 //minutes
 * 		"repeat": [5, 10, 30] //array of minutes
 * }
 */
public class NotifDesc {

	private static final String PREF_FILE_NAME = 
				"edu.ucla.cens.triggers.notif.NotifDesc";
	private static final String PREF_KEY_GLOBAL_NOTIF_DESC = "notif_desc";

	//The minimum value allowed for a repeat reminder
	private static final int REPEAT_MIN = 1;
	
	private static final String KEY_DURATION = "duration";
	private static final String KEY_SUPPRESSION = "suppression";
	private static final String KEY_REPEAT = "repeat";
	
	private int mDuration;
	private int mSuppress;
	private LinkedList<Integer> mRepeatList = new LinkedList<Integer>();
	
	private void initialze() {
		mDuration = 0;
		mSuppress = 0;
		mRepeatList.clear();
	}
	
	/*
	 * Parse a notification description in JSON format and 
	 * load the parameters into this object.
	 */
	public boolean loadString(String desc) {
		
		initialze();
		
		if(desc == null) {
			return false;
		}
		
		try {
			//Load the string
			JSONObject jDesc = new JSONObject(desc);
			
			mDuration = jDesc.getInt(KEY_DURATION);
			mSuppress = jDesc.getInt(KEY_SUPPRESSION);
			
			if(jDesc.has(KEY_REPEAT)) {
			
				mRepeatList.clear();
				
				JSONArray repeats = jDesc.getJSONArray(KEY_REPEAT);
				
				for(int i = 0; i < repeats.length(); i++) {
					mRepeatList.add(repeats.getInt(i));
				}
			}
			
		} catch (JSONException e) {
			return false;
		}
		
		return true;
	}
	
	/*
	 * Get the notification duration in minutes
	 */
	public int getDuration() {
		return mDuration;
	}
	
	/*
	 * Set the notification duration in minutes
	 */
	public void setDuration(int duration) {
		mDuration = duration;
	}
	
	/*
	 * Get the suppression window in minutes. 
	 */
	public int getSuppression() {
		return mSuppress;
	}
	
	/*
	 * Set the suppression window in minutes
	 */
	public void setSuppression(int suppress) {
		mSuppress = suppress;
	}
	
	/*
	 * Get the list (sorted) of reminders associated 
	 * with this notification description. Each item 
	 * is in minutes.  
	 */
	public List<Integer> getSortedRepeats() {
		LinkedList<Integer> ret = new LinkedList<Integer>(mRepeatList);
		
		Collections.sort(ret, new Comparator<Integer>() {

			public int compare(Integer a, Integer b) {
				
				return (a - b);
			}
		});
		
		return ret;
	}
	
	/*
	 * Set the list of repeat reminders for this description. 
	 * The items in the list must be represented in minutes.
	 * This function will replace the previous set of repeat
	 * reminders in this description with the given list. 
	 */
	public void setRepeats(List<Integer> repeats) {
		mRepeatList.clear();
		
		for(Integer repeat : repeats) {
			if(!mRepeatList.contains(repeat)) {
				mRepeatList.add(repeat);
			}
		}
	}
	
	/*
	 * Utility function to support the maintenance of a global
	 * notification description (as opposed to different description
	 * for different triggers). This function will return the string
	 * representation of the global notification description stored in
	 * the shared preferences. 
	 */
	public static String getGlobalDesc(Context context) {
		
		SharedPreferences prefs = context.getSharedPreferences(
								  PREF_FILE_NAME, Context.MODE_PRIVATE);
		
		String notifDesc = prefs.getString(PREF_KEY_GLOBAL_NOTIF_DESC, 
											NotifConfig.defaultConfig);
		
		return notifDesc;
	}
	
	/*
	 * Utility function to support the maintenance of a global
	 * notification description (as opposed to different description
	 * for different triggers). This function can be used to set the 
	 * global description and it will be saved in shared preferences.
	 */
	public static void setGlobalDesc(Context context, String desc) {
	
		SharedPreferences prefs = context.getSharedPreferences(
				  					PREF_FILE_NAME, Context.MODE_PRIVATE);
		
		Editor editor = prefs.edit();
		editor.putString(PREF_KEY_GLOBAL_NOTIF_DESC, desc);
		editor.commit();
		
		TrigPrefManager.registerPreferenceFile(context, PREF_FILE_NAME);
	}
	
	/*
	 * Get the default notification description for a trigger. 
	 * This function can be used while adding a new trigger entry
	 * to the database. 
	 * 
	 *  Currently returns the global notification description.
	 */
	public static String getDefaultDesc(Context context) {
		return getGlobalDesc(context);
	}
	
	/*
	 * Get the minimum allowed value (in minutes) for a
	 * repeat reminder. 
	 */
	public int getMinAllowedRepeat() {
		return REPEAT_MIN;
	}
	
	/*
	 * Convert the notification description represented by 
	 * this object to a JSON string. 
	 */
	public String toString() {
		
		JSONObject jDesc = new JSONObject();
		
		try {
			jDesc.put(KEY_DURATION, mDuration);
			jDesc.put(KEY_SUPPRESSION, mSuppress);
			
			JSONArray repeats = new JSONArray();
			for(int repeat : mRepeatList) {
				repeats.put(repeat);
			}
			
			if(repeats.length() > 0) {
				jDesc.put(KEY_REPEAT, repeats);
			}
			
		} catch (JSONException e) {
			return null;
		}
		
		return jDesc.toString();
	}
	
}
