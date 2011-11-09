package edu.ucla.cens.mobility.blackout.config;

/*
 * Class containing the compile time constants which define the 
 * behavior of trigger notification
 */
public class NotifConfig {

	//Default notification description
	public static final String defaultConfig = 
		"{\"duration\": 60, \"suppression\": 30}";
	
	//The default value of repeat reminder
	public static final int defaultRepeat = 5; //minutes
	//Maximum value of notification duration
	public static final int maxDuration = 60; //minutes
	//Maximum value of suppression window. If the survey
	//has already been taken within this window, the 
	//notification will be suppressed
	public static final int maxSuppression = 60; //minutes
}
