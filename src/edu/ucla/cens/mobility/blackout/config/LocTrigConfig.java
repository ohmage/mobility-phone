package edu.ucla.cens.mobility.blackout.config;

/*
 * Class containing the compile time constants which define the 
 * behavior of location triggers
 */
public class LocTrigConfig {
	//Minimum allowed gap between re-entries in order to be triggered
	public static final int MIN_REENTRY = 120; //minutes
	//The default radius of any location
	public static final float LOC_RADIUS_DEFAULT = 70; //m
	//The minimum allowed radius of any location
	public static final float LOC_RADIUS_MIN = 50;//m
	//The maximum allowed radius of any location
	public static final float LOC_RADIUS_MAX = 200;//m
	//Minimum allowed gap between any two locations
	public static final float MIN_LOC_GAP = 200;//m
	//The distance factor used to adaptive duty cycle
	//GPS when location tracing is enabled and no triggers
	//are defined. The proximity to the closest location will
	//be replaced by the parameter. Reduce this parameter to 
	//make the sampling more aggressive and vice versa.
	public static final float LOC_TRACE_DISTANCE_FACTOR = 1000;
	//If 'upload always' is not enabled, a location trace is 
	//uploaded only if it is at least as far as this value
	//from the previous trace.
	public static final float LOC_TRACE_MIN_DISTANCE_FOR_UPLOAD = 50; //m
	//If 'upload always' is not enabled, a location trace is uploaded
	//every LOC_TRACE_MAX_GAP_BETWEEN_UPLOADS milliseconds even if the
	//trace is the same as that of the previous trace.
	public static final long LOC_TRACE_MAX_GAP_BETWEEN_UPLOADS = 30 * 60 * 1000; //30 mins
	
	//Whether fall back to network based provider
	//when GPS is available
	public static final boolean useNetworkLocation = true;
	//Whether to use motion detection
	public static final boolean useMotionDetection = true;
}	
