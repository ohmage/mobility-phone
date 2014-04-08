package org.ohmage.mobility.blackout.config;

/*
 * Class containing the compile time constants which define the 
 * behavior of the user interface in non-admin mode
 */
public class TrigUserConfig {
	
	/*
	 * The admin password. Currently, the admin mode
	 * is a mechanism to prevent the user from accidentally
	 * changing the trigger settings.  
	 */
	public static final String adminPass = "0000";
	
	/* UI options in trigger list affected by admin mode */
	public static boolean addTrigers = false;
	public static boolean removeTrigers = false;
	public static boolean editTriggerActions = false;	
	public static boolean editNotificationSettings = false;
	public static boolean editTriggerSettings = true;
	
	/* UI options in location triggers affected by admin mode */
	public static boolean editLocationTrigger = false;
	public static boolean editLocationTriggerSettings = true;
	public static boolean editLocationTriggerPlace = false;
	
	/* UI options in time triggers affected by admin mode */
	public static boolean editTimeTrigger = true;
	public static boolean editTimeTriggerTime = true;
	public static boolean editTimeTriggerRepeat = false;
	public static boolean editTimeTriggerRange = false;
}
