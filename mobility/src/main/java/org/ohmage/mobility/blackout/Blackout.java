package org.ohmage.mobility.blackout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.json.JSONObject;
import android.util.Log;
import org.ohmage.mobility.Mobility;
import org.ohmage.mobility.MobilityControl;
import org.ohmage.mobility.R;
import org.ohmage.mobility.blackout.base.TriggerBase;

public class Blackout extends TriggerBase
{
	// private static final String TRIGGER_TYPE = "TimeTrigger";
	// TODO localize
	private static final String DISP_NAME = "Time Trigger";

	private static final String TAG = "Blackout";

	@Override
	public String getTriggerTypeDisplayName()
	{

		return DISP_NAME;
	}

	// @Override
	// public String getTriggerType() {
	//
	// return TRIGGER_TYPE;
	// }

	@Override
	public boolean hasSettings()
	{

		return false;
	}

	@Override
	public void stopTrigger(Context context, int trigId, String trigDesc)
	{
		Intent i = new Intent(context, BlackoutService.class);
		i.setAction(BlackoutService.ACTION_REMOVE_TRIGGER);
		i.putExtra(BlackoutService.KEY_TRIG_ID, trigId);
		i.putExtra(BlackoutService.KEY_TRIG_DESC, trigDesc);
		context.startService(i);
		
	}

	@Override
	public void resetTrigger(Context context, int trigId, String trigDesc)
	{
		SharedPreferences settings = context.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
		if (settings.getBoolean(MobilityControl.MOBILITY_ON, false))
		{
			Intent i = new Intent(context, BlackoutService.class);
			i.setAction(BlackoutService.ACTION_RESET_TRIGGER);
			i.putExtra(BlackoutService.KEY_TRIG_ID, trigId);
			i.putExtra(BlackoutService.KEY_TRIG_DESC, trigDesc);
			context.startService(i);
		}
	}

	@Override
	public void startTrigger(Context context, int trigId, String trigDesc)
	{
		SharedPreferences settings = context.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
		if (settings.getBoolean(MobilityControl.MOBILITY_ON, false))
		{
			Log.v(TAG, "Starting trigger by starting blackoutservice!");
			Intent i = new Intent(context, BlackoutService.class);
			i.setAction(BlackoutService.ACTION_SET_TRIGGER);
			i.putExtra(BlackoutService.KEY_TRIG_ID, trigId);
			i.putExtra(BlackoutService.KEY_TRIG_DESC, trigDesc);
			context.startService(i);
		}
	}

	@Override
	public void launchTriggerCreateActivity(Context context, boolean adminMode)
	{

		BlackoutEditActivity.setOnExitListener(new BlackoutEditActivity.ExitListener()
		{

			@Override
            public void onDone(Context context, int trigId, String trigDesc)
			{

				Log.v(TAG, "TimeTrigger: Saving new trigger: " + trigDesc);
				addNewTrigger(context, trigDesc);
			}
		});

		context.startActivity(new Intent(context, BlackoutEditActivity.class));
	}

	@Override
    public void launchTriggerEditActivity(Context context, int trigId, String trigDesc, boolean adminMode)
	{
		Log.v(TAG, "Editing the blackout time!");
		BlackoutEditActivity.setOnExitListener(new BlackoutEditActivity.ExitListener()
		{

			@Override
            public void onDone(Context context, int trigId, String trigDesc)
			{

				updateTrigger(context, trigId, trigDesc);
			}
		});
//		SharedPreferences settings = context.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
//		if (settings.getBoolean(MobilityControl.MOBILITY_ON, false))
//		{
			Intent i = new Intent(context, BlackoutEditActivity.class);
			i.putExtra(BlackoutEditActivity.KEY_TRIG_ID, trigId);
			i.putExtra(BlackoutEditActivity.KEY_TRIG_DESC, trigDesc);
			i.putExtra(BlackoutEditActivity.KEY_ADMIN_MODE, adminMode);
			context.startActivity(i);
//		}
	}

	@Override
	public void launchSettingsEditActivity(Context context, boolean adminMode)
	{

	}

	@Override
	public void resetSettings(Context context)
	{

	}

	@Override
	public String getDisplaySummary(Context context, String trigDesc)
	{
		BlackoutDesc conf = new BlackoutDesc();

		if (!conf.loadString(trigDesc))
		{
			return null;
		}

		return conf.getRepeatDescription();
	}

	@Override
	public String getDisplayTitle(Context context, String trigDesc)
	{
		BlackoutDesc conf = new BlackoutDesc();

		if (!conf.loadString(trigDesc))
		{
			return null;
		}

		// String ret = conf.toString();

		// if(conf.isRandomized()) {
		String ret = conf.getRangeStart().toString() + " - " + conf.getRangeEnd().toString();
		// }

		return ret;
	}

	@Override
	public int getIcon()
	{
		return R.drawable.clock;
	}

	@Override
	public JSONObject getPreferences(Context context)
	{
		return new JSONObject();
	}
}
