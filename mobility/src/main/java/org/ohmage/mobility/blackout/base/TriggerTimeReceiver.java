package org.ohmage.mobility.blackout.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import android.util.Log;
import org.ohmage.mobility.blackout.Blackout;
import org.ohmage.mobility.blackout.notif.Notifier;

/*
 * Time/Time-zone change listener. Restarts the triggers and
 * refreshes the notification 
 */
public class TriggerTimeReceiver extends BroadcastReceiver{

	private static final String DEBUG_TAG = "TriggerFramework";
	
	private static void handleTimeChange(Context context) {
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
//				String trigType = c.getString(
//				 		   		  c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_TYPE));
				String actDesc = c.getString(
 		   		  				 c.getColumnIndexOrThrow(TriggerDB.KEY_TRIG_ACTIVE_DESCRIPT));
				
				TriggerBase trig = new Blackout();// trigMap.getTrigger(trigType);
				if(trig != null) {
					TriggerActionDesc aDesc = new TriggerActionDesc();
					//Restart the trigger if it is active
					if(aDesc.loadBoolean(actDesc) && aDesc.getCount() > 0) {
						trig.resetTrigger(context, trigId, trigDesc);
					}
				}
			} while(c.moveToNext());
		}
		
		c.close();
		db.close();
		
		//Finally, quietly refresh the notification
		Notifier.refreshNotification(context,true);
	}
	
	@Override
	public void onReceive(Context context, Intent i) {
		if(i.getAction().equals(Intent.ACTION_TIME_CHANGED) || 
		   i.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
			
			Log.v(DEBUG_TAG, "TriggerTimeReceiver: " + i.getAction());
			
			handleTimeChange(context);
		}
	}
}
