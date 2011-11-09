package edu.ucla.cens.mobility.blackout.base;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Vector;

import edu.ucla.cens.mobility.blackout.config.TriggerTypesConfig;
import edu.ucla.cens.mobility.blackout.Blackout;

/*
 * The map of available trigger types to their concrete class instances.
 * 
 * When a new trigger type is defined, it must be registered here. Each
 * <key, value> entry in the map has the form <String, TriggerBase> where 
 * the key corresponds to the trigger type (String) and the value corresponds
 * to the instance of the concrete trigger (which extends TriggerBase).
 */
public class BlackoutList {
	
	private static Vector<TriggerBase> mBlackoutList = null;
//	priavte static Vector<TriggerBase> mTriggerList = null;
	/*
	 * A new trigger type must be registered here. 
	 * 
	 * In order to disable a type of trigger altogether, 
	 * the corresponding boolean flag in ..\config\TriggerTypesConfig
	 * can be set to false. 
	 */
	public BlackoutList() {
		
		mBlackoutList = new Vector<TriggerBase>();
		
		//Time trigger
//			TriggerBase timeTrig = new Blackout();
//			mBlackoutList.add(timeTrig.getTriggerType(), timeTrig);
	}

	/*
	 * Get the TriggerBase instance corresponding to a 
	 * type
	 */
//	public TriggerBase getTrigger(String trigType) {
//		if(mBlackoutList == null) {
//			return null;
//		}
//		
//		return mBlackoutList.get(trigType);
//	}
	
	/*
	 * Get TriggerBase instances of all types
	 */
	public Vector<TriggerBase> getAllTriggers() {
		return mBlackoutList;
	}
	
	/*
	 * Get the list of all registered types
	 */
//	public Vector<String> getAllTriggerTypes() {
//		return mBlackoutList.keySet();
//	}
	
}
