package org.ohmage.mobility.blackout.base;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.text.format.DateUtils;

import org.json.JSONObject;
import android.util.Log;
import org.ohmage.mobility.blackout.MobilityBlackoutManager;
import org.ohmage.mobility.blackout.notif.Notifier;

import java.util.LinkedList;

/*
 * The abstract class which must be extended by all the triggers
 * implemented in the ..\types\ directory.Each trigger type such
 * as time trigger must extend this class and register that concrete
 * class with the trigger framework by adding and entry into the class
 * TriggerTypeMap.
 * 
 * The framework communicates with the trigger types using the concrete
 * class registered in the trigger type map. This class also provides some
 * APIs using which the trigger types can communicate with the framework.
 */
public abstract class TriggerBase {

	private static final String TAG = "TriggerBase";
	
	/*
	 * Function to be called by the trigger types to notify the user 
	 * when a trigger of that types goes off. 
	 */
	public void notifyTrigger(Context context, int trigId) {
		Log.v(TAG, "notifyTrigger(" + trigId + ")");
		
		TriggerDB db = new TriggerDB(context);
		db.open();
		
		String rtDesc = db.getRunTimeDescription(trigId);
		TriggerRunTimeDesc desc = new TriggerRunTimeDesc();
		
		desc.loadString(rtDesc);
			
		//Save trigger time stamp in the run time description 
		desc.setTriggerTimeStamp(System.currentTimeMillis());		
		//Save trigger current loc in the run time description 
		LocationManager locMan = (LocationManager)
								context.getSystemService(Context.LOCATION_SERVICE);
		Location loc = locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		desc.setTriggerLocation(loc);

		//Save the run time desc in the database
		db.updateRunTimeDescription(trigId, desc.toString());

		//Call the notifier to display the notification
		//Pass the notification description corresponding to this trigger
		Notifier.notifyNewTrigger(context, trigId, db.getRunTimeDescription(trigId));
		MobilityBlackoutManager.toggleMobility(context);
		db.close();
	}
	
	/*
	 * Get the ids of all the triggers of this type
	 */
	public LinkedList<Integer> getAllTriggerIds(Context context) {
	
		Log.v(TAG, "getAllTriggerIds()");
		
		TriggerDB db = new TriggerDB(context);
		db.open();
		
		//Query the triggers of this type
		Cursor c = db.getTriggers();
		LinkedList<Integer> ids = new LinkedList<Integer>();
		
		//Populate the list
		if(c.moveToFirst()) {
			do {
				ids.add(c.getInt(
				 		c.getColumnIndexOrThrow(TriggerDB.KEY_ID)));	
				
			} while(c.moveToNext());
		}
			
				
		c.close();
		db.close();
		
		return ids;
	}
	
	/*
	 * Get the list of ids of all the active triggers. A trigger
	 * is active if it has at least one survey associated with it.
	 * 
	 * Note: This function can be optimized by storing the action
	 * count separately in the db.
	 */
	public LinkedList<Integer> getAllActiveTriggerIds(Context context) {
		Log.v(TAG, "getAllActiveTriggerIds()");
		
		TriggerDB db = new TriggerDB(context);
		db.open();
		
		//Get the triggers of this type
		Cursor c = db.getTriggers();
		LinkedList<Integer> ids =  new LinkedList<Integer>();
		
		//Iterate through the list of all triggers of this type
		if(c.moveToFirst()) {
			do {
				boolean actDesc = c.getString(
								 c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_ACTIVE_DESCRIPT)) == TriggerDB.ON;
				
				TriggerActionDesc desc = new TriggerActionDesc();
				if(!/*desc.loadString(*/actDesc/*)*/) {
					continue;
				}
				
				//Add the trigger id to the list if there is at least
				//one survey associated with this trigger
				if(desc.getCount() > 0) {
				
					ids.add(c.getInt(
					 		c.getColumnIndexOrThrow(TriggerDB.KEY_ID)));	
				}
				
			} while(c.moveToNext());
		}
			
				
		c.close();
		db.close();
	
		return ids;
	}
	
	/*
	 * Get the description for a specific trigger
	 * 
	 * TODO: check if trigId corresponds to a trigger of this type
	 */
	public String getTrigger(Context context, int trigId) {
		Log.v(TAG, "getTrigger(" + trigId + ")");
		
		TriggerDB db = new TriggerDB(context);
		db.open();
	
		String ret = null;
		Cursor c = db.getTrigger(trigId);
		if(c.moveToFirst()) {
			
			ret = c.getString(
				  c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_DESCRIPT));
		}
		
		c.close();
		db.close();
		return ret;
	}
	
	/*
	 * Get the latest time stamp (the time when the trigger went 
	 * off the last time) of a specific trigger. 
	 * 
	 * Returns -1 if there is no time stamp
	 * 
	 * TODO: check if trigId corresponds to a trigger of this type
	 */
	public long getTriggerLatestTimeStamp(Context context, int trigId) {
		Log.v(TAG, "getTriggerLatestTimeStamp(" + trigId + ")");
		
		TriggerDB db = new TriggerDB(context);
		db.open();
	
		long ret = -1;
		Cursor c = db.getTrigger(trigId);
		if(c.moveToFirst()) {
			
			String rtDesc = c.getString(
							c.getColumnIndexOrThrow(TriggerDB.KEY_RUNTIME_DESCRIPT));
			TriggerRunTimeDesc desc = new TriggerRunTimeDesc();
			if(desc.loadString(rtDesc)) {
				ret = desc.getTriggerTimeStamp();
			}
		}
		
		c.close();
		db.close();
		return ret;
	}
	
	/*
	 * Delete a trigger from the db given its id.
	 * The trigger can be deleted only if its type matches
	 * this type
	 */
	public void deleteTrigger(Context context, int trigId) {
		Log.v(TAG, "deleteTrigger(" + trigId + ")");
		
		TriggerDB db = new TriggerDB(context);
		db.open();
		
		Cursor c = db.getTrigger(trigId);
		if(c.moveToFirst()) {
//			String trigType = c.getString(
//					          c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_TYPE));
			
//			if(trigType.equals(this.getTriggerType())) {
				
				//Stop trigger first
				stopTrigger(context, trigId, db.getTriggerDescription(trigId));
				//Delete from database
				db.deleteTrigger(trigId);
				//Now refresh the notification display
				Notifier.removeTriggerNotification(context, trigId);
//			}
		}
		
		c.close();
		db.close();
	}
	
	/*
	 * Save a new trigger description to the database.
	 */
	public void addNewTrigger(Context context, String trigDesc) {
		Log.v(TAG, "getTriggerLatestTimeStamp(" + trigDesc + ")");
		
		TriggerDB db = new TriggerDB(context);
		db.open();
		
		//Save the trigger desc. Use default desc for notification, action
		// and run time
		int trigId = (int) db.addTrigger(/*this.getTriggerType(), */trigDesc,
					  			   		TriggerActionDesc.getDefaultDesc(),
					  			   		/*NotifDesc.getDefaultDesc(context),*/
					  			   		TriggerRunTimeDesc.getDefaultDesc());
		
		boolean actDesc = db.getActionDescription(trigId); // TODO make default!
		db.close();
	
		//If the action has a positive number of surveys, 
		//start the trigger. 
//		TriggerActionDesc desc = new TriggerActionDesc();
		if(/*desc.loadBoolean(*/actDesc/*) && desc.getCount() > 0*/) {
			startTrigger(context, trigId, trigDesc);
		}	
	}
	
	/*
	 * Update the description of an existing trigger
	 */
	public void updateTrigger(Context context, int trigId, String trigDesc) {
		Log.v(TAG, "getTriggerLatestTimeStamp(" + trigId 
										+ ", " + trigDesc + ")");
		
		TriggerDB db = new TriggerDB(context);
		db.open();
		db.updateTriggerDescription(trigId, trigDesc);
		
		boolean actDesc = db.getActionDescription(trigId);
		db.close();
		
		//If the action has a positive number of surveys, 
		//restart the trigger. 
		TriggerActionDesc desc = new TriggerActionDesc();
		if(/*desc.loadString(*/actDesc/*) && desc.getCount() > 0*/) {
			resetTrigger(context, trigId, trigDesc);
		}
	}	
	
	/*
	 * Utility function to check if the trigger has already gone
	 * off today. Useful for any time based triggers which are
	 * supposed to go off only once per day at most.
	 */
	public boolean hasTriggeredToday(Context context, int trigId) {
		Log.v(TAG, "hasTriggeredToday(" + trigId + ")");
		
		long trigTS = getTriggerLatestTimeStamp(context, trigId);
		
		if(trigTS != TriggerRunTimeDesc.INVALID_TIMESTAMP) {
			return DateUtils.isToday(trigTS);
		}
		
		return false;
	}
	
	
	/* Abstract functions to be implemented by all the concrete trigger types */
	
	/*
	 * Get the string which represents the type of this trigger.
	 * This string must be unique and no other trigger type must
	 * use it. The list of registered types can be found in the class
	 * TriggerTypeMap.
	 */
//	public abstract String getTriggerType();
	
	/*
	 * Get the resource id of the icon which represents this trigger
	 * type. This icon will be used by the main trigger list.
	 */
	public abstract int getIcon();
	
	/*
	 * Get the display name for this trigger type. This name is used to
	 * create the 'trigger type selector' dialog which is displayed to 
	 * the user when a new trigger is to be created or when the settings
	 * are to be modified. 
	 */
	public abstract String getTriggerTypeDisplayName();
	
	/*
	 * Get the title of a specific trigger description of this type. 
	 * This title will be used in main trigger list when the list item 
	 * corresponding to this description is displayed.
	 */
	public abstract String getDisplayTitle(Context context, String trigDesc);
	
	/*
	 * Get the summary of a specific trigger description of this type. 
	 * This summary will be used in main trigger list when the list item 
	 * corresponding to this description is displayed.
	 */
	public abstract String getDisplaySummary(Context context, String trigDesc);
	
	/*
	 * Get the preferences for this trigger in JSON format.
	 * Used while uploading a survey response
	 */
	public abstract JSONObject getPreferences(Context context);

	/*
	 * Start a specific trigger 
	 */
	public abstract void startTrigger(Context context, int trigId, String trigDesc);
	
	/*
	 * Reset a specific trigger 
	 */
	public abstract void resetTrigger(Context context, int trigId, String desc);
	
	/*
	 * Stop a specific trigger 
	 */
	public abstract void stopTrigger(Context context, int trigId, String trigDesc);
	
	/*
	 * Launch the activity to create a new trigger of this type. The activity
	 * can save the trigger description to the db using the API addNewTrigger() 
	 * provided by this class.
	 */
	public abstract void launchTriggerCreateActivity(Context context, boolean adminMode);
	
	/*
	 * Launch the activity to edit an existing trigger of this type. The activity
	 * can save the edited trigger description to the db using the API updateTrigger() 
	 * provided by this class.
	 */
	public abstract void launchTriggerEditActivity(Context context, int trigId, 
													String trigDesc, boolean adminMode);
	
	/*
	 * Check if this trigger type has its own settings.
	 */
	public abstract boolean hasSettings();
	
	/*
	 * Launch the activity to edit the settings if this trigger type 
	 * has its own settings. 
	 */
	public abstract void launchSettingsEditActivity(Context context, boolean adminMode);
	
	/*
	 * Reset all settings to default
	 */
	public abstract void resetSettings(Context context);
}
