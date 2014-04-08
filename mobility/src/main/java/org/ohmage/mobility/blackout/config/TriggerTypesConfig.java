package org.ohmage.mobility.blackout.config;

/*
 * Class containing the compile time constants which decides
 * which all trigger types are to be present in a given build.
 * 
 * New trigger types can add flags here and use them in
 * TriggerTypeMap. This class is only a convenience to 
 * selectively disable and enable trigger types in various
 * releases. A trigger type can be disabled also by commenting
 * the registration in the TriggerTypeMap
 */
public class TriggerTypesConfig {

	//Flag which decides if location triggers must be 
	//present in the build
	public static final boolean locationTriggers = true;
	//Flag which decides if time triggers must be 
	//present in the build
	public static final boolean timeTriggers = true;
}
