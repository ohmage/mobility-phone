package edu.ucla.cens.mobility.blackout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import edu.ucla.cens.mobility.Mobility;
import edu.ucla.cens.mobility.MobilityControl;
import edu.ucla.cens.mobility.glue.MobilityInterface;

public class MobilityBlackoutManager
{
	private static boolean running = false;
	private static boolean initialized = false;
	private static SharedPreferences settings;
//	private static Editor editor;
	public static void initializeMobilityBlackouts(Context context)
	{
		settings = context.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
		running = settings.getBoolean(MobilityControl.MOBILITY_ON, false); 
//		editor = settings.edit();
		
	}	
	
	public static void toggleMobility(Context context)
	{
		if (!initialized)
			initializeMobilityBlackouts(context);
		if (running)
			Mobility.stop(context);
//			MobilityInterface.stopMobility(context);
		else
			Mobility.start(context);
//			MobilityInterface.startMobility(context);
		running = !running;
	}
	
//	public static void setupMobility(Context context)
//	{
//		// Read blackouts
//		
//		// Set toggle triggers for today
//		
//		// Set reset for tomorrow
//		
//		//Start Mobility if not in an off period
//	}
	
	
	
	
//	public static void startMobility()
//	{
//		
//	}
//	
//	public static void stopMobility()
//	{
//		
//	}
}
