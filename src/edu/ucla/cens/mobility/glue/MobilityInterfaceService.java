package edu.ucla.cens.mobility.glue;

import edu.ucla.cens.mobility.Mobility;
import edu.ucla.cens.mobility.MobilityControl;
import edu.ucla.cens.systemlog.Log;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;

public class MobilityInterfaceService extends Service
{

	protected static final String TAG = "Mobility";

	@Override
	  public IBinder onBind(Intent intent) {

		Mobility.initSystemLog(getApplicationContext());

	    return new IMobility.Stub() {
	    	public void stopMobility()
	    	{
	    		SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
	    		if (settings.getBoolean(MobilityControl.MOBILITY_ON, false))
	    		{
	    			Editor editor = settings.edit();
					editor.putBoolean(MobilityControl.MOBILITY_ON, false);
					editor.commit();
					Log.e(TAG, "MOBILITY_ON was just totally set to false");
	    			Mobility.stop(getApplicationContext());
	    			
	    		}
	    		else
	    		{
	    			Log.e(TAG, "Mobility is already off according to settings");
	    		}
	    	}
	    	
	    	public void startMobility()
	    	{
	    		Log.e(TAG, "I got called!");
	    		SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
	    		if (!settings.getBoolean(MobilityControl.MOBILITY_ON, false))
	    		{
	    			Editor editor = settings.edit();
					editor.putBoolean(MobilityControl.MOBILITY_ON, true);
					editor.commit();
	    			Log.e(TAG, "MOBILITY_ON was just totally set to true");
	    			Mobility.start(getApplicationContext());
	    			
	    		}
	    		else
	    		{
	    			Log.e(TAG, "Mobility is already on according to settings");
	    		}
	    		Log.e(TAG, "MOBILITY_ON was just totally set to true unless it was already");
	    	}
	    	
	    	/**
	    	 * Set the rate in seconds to 300, 60, 30, or 15.
	    	 * @param context
	    	 * @param intervalInSeconds This must be 300, 60, 30, or 15.
	    	 * @return true if successful, false otherwise
	    	 */
	    	public boolean changeMobilityRate(int intervalInSeconds)
	    	{
	    		for (int rate : new int [] {300, 60, 30, 15})
	    			if (intervalInSeconds == rate)
	    			{
	    				SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
	    				Editor editor = settings.edit();
	    				editor.putInt(Mobility.SAMPLE_RATE, intervalInSeconds);
	    				editor.commit();
	    				if (settings.getBoolean(MobilityControl.MOBILITY_ON, false))
	    				{
	    					stopMobility();
	    					startMobility();
	    				}
	    				return true;
	    			}
	    		return false;
	    	}
	    	
	    	public boolean isMobilityOn()
	    	{
	    				SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
	    				
	    				return settings.getBoolean(MobilityControl.MOBILITY_ON, false);
	    				
	    	}
	    	
	    	public int getMobilityInterval()
	    	{
	    				SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
	    				
	    				return settings.getInt(Mobility.SAMPLE_RATE, 60);
	    				
	    	}

	    };
	
	}	

	
	
	
}
