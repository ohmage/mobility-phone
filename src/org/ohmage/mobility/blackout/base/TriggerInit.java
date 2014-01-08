package org.ohmage.mobility.blackout.base;

import android.content.Context;
import android.database.Cursor;

import android.util.Log;
import org.ohmage.mobility.blackout.Blackout;

/*
 * Boot listener. Starts all the active triggers. 
 * Also restores the pending notifications if any
 */
public class TriggerInit/* extends BroadcastReceiver */{
	
	private static final String TAG = "TriggerInit";
	
	public static void initTriggers(Context context) {
		
		Log.v(TAG, "TriggerInit: Initializing triggers");
		
//		TriggerTypeMap trigMap = new TriggerTypeMap();
		
		TriggerDB db = new TriggerDB(context);
		db.open();
		
		Cursor c = db.getAllTriggers();
		if(c.moveToFirst()) {
			do {
				int trigId = c.getInt(
							 c.getColumnIndexOrThrow(TriggerDB.KEY_ID));
				
				String trigDesc = c.getString(
								  c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_DESCRIPT));
			
//				String notifDesc = c.getString(
//						 		   c.getColumnIndexOrThrow(TriggerDB.KEY_NOTIF_DESCRIPT));
				
//				String trigType = c.getString(
//				 		   		  c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_TYPE));
				
				String rtDesc = c.getString(
		 		   		  		c.getColumnIndexOrThrow(TriggerDB.KEY_RUNTIME_DESCRIPT));
				
				String actDesc = c.getString(
 		   		  				 c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_ACTIVE_DESCRIPT));
				
				Log.v(TAG, "TriggerInit: Read from db: " + trigId +
								 ", " + trigDesc + ", " + actDesc);	
				
				TriggerBase trig = new Blackout();//trigMap.getTrigger(trigType);
				if(trig != null) {
	
					//Start the trigger
					TriggerActionDesc aDesc = new TriggerActionDesc();
					//Start only if it has a positive number of surveys
					if(aDesc.loadBoolean(actDesc)/* && aDesc.getCount() > 0*/) {
						Log.v(TAG, "TriggerInit: Starting trigger: " + trigId + 
										 ", " + trigDesc);
						
						trig.startTrigger(context, trigId, trigDesc);
					}
					
					//Restore the notification states for this trigger
//					TriggerRunTimeDesc desc = new TriggerRunTimeDesc();
//					if(desc.loadString(rtDesc) && desc.hasTriggerTimeStamp()) {
//						Log.v(TAG, "TriggerInit: Restoring notifications for " + trigId);
//						
////						Notifier.restorePastNotificationStates(context, trigId, /*notifDesc,*/ 
////														desc.getTriggerTimeStamp());
//					}
				}
			
			} while(c.moveToNext());
		}
		c.close();
		db.close();
		
		//Refresh the notification display
//		Notifier.refreshNotification(context, true);
	}
	
//	@Override
//	public void onReceive(Context context, Intent intent) {
//		
//		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//			Log.v(DEBUG_TAG, "TriggerInit: Received boot completed intent");
//			
//			initTriggers(context);
//		}
//	}
	
	/*
	 * Resets all triggers, settings and preferences to its default.
	 * Removes all triggers from the database after stopping them.
	 */
//	public static boolean resetAllTriggersAndSettings(Context context) {
//		Log.v(DEBUG_TAG, "TriggerInit: Resetting all triggers");
//		
////		TriggerTypeMap trigMap = new TriggerTypeMap();
//		
//		TriggerDB db = new TriggerDB(context);
//		db.open();
//		
//		Cursor c = db.getAllTriggers();
//		
//		//Stop and delete all triggers
//		if(c.moveToFirst()) {
//			do {
//				int trigId = c.getInt(
//							 c.getColumnIndexOrThrow(TriggerDB.KEY_ID));
//				
//				TriggerBase trig = new Blackout();// trigMap.getTrigger(db.getTriggerType(trigId));
//				if(trig != null) {
//					//delete the trigger 
//					trig.deleteTrigger(context, trigId);
//				}
//			} while(c.moveToNext());
//		}
//		
//		c.close();
//		db.close();
//		
//		//Reset the settings of all trigger types
//		for(TriggerBase trig : trigMap.getAllTriggers()) {
//			
//			if(trig.hasSettings()) {
//				trig.resetSettings(context);
//			}
//		}
//		
//		//Refresh the notification display
//		Notifier.refreshNotification(context, true);
//		
//		//Clear all preference files registered with the preference manager
//		TrigPrefManager.clearAllPreferenceFiles(context);
//		
//		return true;
//	}
}
