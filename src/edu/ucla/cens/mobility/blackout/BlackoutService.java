package edu.ucla.cens.mobility.blackout;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import edu.ucla.cens.mobility.Mobility;
import edu.ucla.cens.mobility.blackout.utils.SimpleTime;

public class BlackoutService extends Service
{

	private static final String TAG = "BlackoutService";

	public static final String WAKE_LOCK_NAME = "edu.ucla.cens.mobility.blackout.BlackoutService.wake_lock";

	public static final String ACTION_HANDLE_TRIGGER = "handle_alarm";
	public static final String ACTION_SET_TRIGGER = "set_trigger";
	public static final String ACTION_REMOVE_TRIGGER = "remove_trigger";
	public static final String ACTION_RESET_TRIGGER = "reset_trigger";
	public static final String KEY_TRIG_ID = "trigger_id";
	public static final String KEY_TRIG_DESC = "trigger_desc";

	private static final String ACTION_TRIG_ALM = "edu.ucla.cens.mobility.blackout.BlackoutAlarm";
	private static final String DATA_PREFIX_TRIG_ALM = "blackout://edu.ucla.cens.mobility.blackout/";

	private AlarmManager mAlarmMan = null;
	private static PowerManager.WakeLock mWakeLock = null;

	@Override
	public void onCreate()
	{
		super.onCreate();

		Log.i(TAG, "BlackoutService: onCreate");

		mAlarmMan = (AlarmManager) getSystemService(ALARM_SERVICE);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);

		Log.i(TAG, "BlackoutService: onStart");

		String action = intent.getAction();
		if (action == null || !intent.hasExtra(KEY_TRIG_ID) || !intent.hasExtra(KEY_TRIG_DESC))
		{

			Log.w(TAG, "BlackoutService: Started with invalid intent");

			releaseWakeLock();
			return;
		}

		int trigId = intent.getIntExtra(KEY_TRIG_ID, -1);
		String trigDesc = intent.getStringExtra(KEY_TRIG_DESC);

		if (action.equals(ACTION_HANDLE_TRIGGER))
		{
			Log.i(TAG, "BlackoutService: Handling trigger " + trigId);

			// Notify user
			// new Blackout().notifyTrigger(this, trigId);

			if (intent.hasExtra("start_end"))
			{
				int startEnd = intent.getIntExtra("start_end", -1);
				if (startEnd < 0)
				{
					Log.e(TAG, "Something is wrong with this trigger");
					return;
				}
				Log.d(TAG, "Time to " + (startEnd == 0 ? "start" : "end") + " this blackout!");

				// repeat the alarm
				if (startEnd == 0)
				{
					Log.d(TAG, "Blackout!");
					Mobility.stopMobility(this, true);
				}
				else
				{
					Log.d(TAG, "All clear!");
					Mobility.startMobility(this);
				}
			}
			else
			{
				Log.e(TAG, "Blackout intent is missing whether this is the end or the beginning");
			}
			setTrigger(trigId, trigDesc);

		}
		else if (action.equals(ACTION_SET_TRIGGER))
		{
			Log.i(TAG, "BlackoutService: Setting trigger " + trigId);

			setTrigger(trigId, trigDesc);
		}
		else if (action.equals(ACTION_REMOVE_TRIGGER))
		{
			Log.i(TAG, "BlackoutService: Removing trigger " + trigId);

			removeTrigger(trigId, trigDesc);
		}
		else if (action.equals(ACTION_RESET_TRIGGER))
		{
			Log.i(TAG, "BlackoutService: Resetting trigger " + trigId);

			removeTrigger(trigId, trigDesc);
			setTrigger(trigId, trigDesc);
		}

		releaseWakeLock();
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		releaseWakeLock();
	}

	private static void acquireWakeLock(Context context)
	{

		if (mWakeLock == null)
		{
			PowerManager powerMan = (PowerManager) context.getSystemService(POWER_SERVICE);

			mWakeLock = powerMan.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_NAME);
			mWakeLock.setReferenceCounted(true);
		}

		if (!mWakeLock.isHeld())
		{
			mWakeLock.acquire();
		}
	}

	private static void releaseWakeLock()
	{

		if (mWakeLock == null)
		{
			return;
		}

		if (mWakeLock.isHeld())
		{
			mWakeLock.release();
		}
	}

	private Intent createAlarmIntent(int trigId, String trigDesc, int startEnd)
	{
		Intent i = new Intent();

		i.setAction(ACTION_TRIG_ALM);
		i.setData(Uri.parse(DATA_PREFIX_TRIG_ALM + trigId + "_" + startEnd));
		i.putExtra(KEY_TRIG_ID, trigId);
		i.putExtra(KEY_TRIG_DESC, trigDesc);
		i.putExtra("start_end", startEnd);
		return i;
	}

	private Vector<Calendar> getTriggerTimeForToday(int trigId, BlackoutDesc trigDesc)
	{

		Blackout timeTrig = new Blackout();
		if (timeTrig.hasTriggeredToday(this, trigId))
		{
			return null;
		}

		Calendar now = Calendar.getInstance();

		Vector<Calendar> targets = new Vector<Calendar>();
		targets.add(Calendar.getInstance());
		targets.add(Calendar.getInstance());

		targets.get(0).set(Calendar.SECOND, 0);
		targets.get(1).set(Calendar.SECOND, 0);

		// if(!trigDesc.isRandomized()) {
		// target.set(Calendar.HOUR_OF_DAY,
		// trigDesc.getTriggerTime().getHour());
		// target.set(Calendar.MINUTE, trigDesc.getTriggerTime().getMinute());
		//
		// if(now.before(target)) {
		// return target;
		// }
		// }
		// else { //if randomized, check if there is any more time left in the
		// interval
		SimpleTime tCurr = new SimpleTime();
		SimpleTime tStart = trigDesc.getRangeStart();
		SimpleTime tEnd = trigDesc.getRangeEnd();

		if (tCurr.isBefore(tEnd))
		{

			// int eDiff;
			// if(tCurr.isAfter(tStart)) {
			// eDiff = tCurr.differenceInMinutes(tEnd);
			targets.get(1).set(Calendar.HOUR_OF_DAY, tEnd.getHour());
			targets.get(1).set(Calendar.MINUTE, tEnd.getMinute());
		}
		// else {
		if (tCurr.isBefore(tStart))
		{
			// int sDiff = tCurr.differenceInMinutes(tStart);
			targets.get(0).set(Calendar.HOUR_OF_DAY, tStart.getHour());
			targets.get(0).set(Calendar.MINUTE, tStart.getMinute());
		}

		// Random rand = new Random();
		// Generate a random number (both ranges inclusive)
		// target.add(Calendar.MINUTE, rand.nextInt(diff + 1));
		return targets;
		// }
		// }
		//
		// return null;
	}

	private Vector<Calendar> getTriggerTimeForDay(int trigId, BlackoutDesc trigDesc, int dayOffset)
	{

		Vector<Calendar> targets = new Vector<Calendar>();
		targets.add(Calendar.getInstance());
		targets.add(Calendar.getInstance());
		targets.get(0).add(Calendar.DAY_OF_YEAR, dayOffset);
		targets.get(1).add(Calendar.DAY_OF_YEAR, dayOffset);

		String dayStr = BlackoutDesc
				.getDayOfWeekString((targets.get(0).get(Calendar.DAY_OF_WEEK) >= Calendar.MONDAY && targets.get(0).get(Calendar.DAY_OF_WEEK) <= Calendar.FRIDAY) ? BlackoutDesc.WEEKDAY
						: BlackoutDesc.WEEKEND);

		if (!trigDesc.doesRepeatOnDay(dayStr))
		{
			return null;
		}

		if (dayOffset == 0)
		{
			return getTriggerTimeForToday(trigId, trigDesc);
		}

		targets.get(0).set(Calendar.SECOND, 0);
		targets.get(1).set(Calendar.SECOND, 0);

		// if(!trigDesc.isRandomized()) {
		// target.set(Calendar.HOUR_OF_DAY,
		// trigDesc.getTriggerTime().getHour());
		// target.set(Calendar.MINUTE, trigDesc.getTriggerTime().getMinute());
		// }
		// else {
		targets.get(0).set(Calendar.HOUR_OF_DAY, trigDesc.getRangeStart().getHour());
		targets.get(0).set(Calendar.MINUTE, trigDesc.getRangeStart().getMinute());
		targets.get(1).set(Calendar.HOUR_OF_DAY, trigDesc.getRangeEnd().getHour());
		targets.get(1).set(Calendar.MINUTE, trigDesc.getRangeEnd().getMinute());

		// int diff = trigDesc.getRangeStart()
		// .differenceInMinutes(trigDesc.getRangeEnd());
		// Random rand = new Random();
		// Generate a random number (both ranges inclusive)
		// target.add(Calendar.MINUTE, rand.nextInt(diff + 1));
		// }

		return targets;
	}

	private Vector<Long> getAlarmTimeInMillis(int trigId, BlackoutDesc trigDesc)
	{

		for (int i = 0; i <= 7; i++)
		{

			Vector<Calendar> targets = getTriggerTimeForDay(trigId, trigDesc, i);
			if (targets != null)
			{
				Log.i(TAG, "BlackoutService: Calculated target time: " + targets.get(0).getTime().toString() + " until " + targets.get(1).getTime().toString());
				Vector<Long> ret = new Vector<Long>();
				ret.add(targets.get(0).getTimeInMillis());
				ret.add(targets.get(1).getTimeInMillis());
				return ret;
			}
		}

		Log.w(TAG, "BlackoutService: No valid day of " + "the week found!");

		// Must not reach here
		return null;
	}

	private void cancelAlarm(int trigId, String trigDesc)
	{
		cancelAlarm(trigId, trigDesc, 0);
		cancelAlarm(trigId, trigDesc, 1);

	}

	private void cancelAlarm(int trigId, String trigDesc, int startEnd)
	{
		Intent i = createAlarmIntent(trigId, trigDesc, startEnd);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_NO_CREATE);

		if (pi != null)
		{
			// remove the pending intent
			Log.i(TAG, "BlackoutService: Canceling the pending" + " intent and alarm for id: " + trigId);

			mAlarmMan.cancel(pi);
			pi.cancel();
		}
	}

	private void setAlarm(int trigId, BlackoutDesc desc)
	{

		// Cancel the pending intent and the existing alarm first
		cancelAlarm(trigId, desc.toString());

		Log.i(TAG, "BlackoutService: Attempting to set trigger " + trigId);

		Intent i0 = createAlarmIntent(trigId, desc.toString(), 0);
		PendingIntent pi0 = PendingIntent.getBroadcast(this, 0, i0, PendingIntent.FLAG_CANCEL_CURRENT);

		Intent i1 = createAlarmIntent(trigId, desc.toString(), 1);
		PendingIntent pi1 = PendingIntent.getBroadcast(this, 0, i1, PendingIntent.FLAG_CANCEL_CURRENT);

		Vector<Long> alarmTimes = getAlarmTimeInMillis(trigId, desc);
		if (alarmTimes == null)
		{
			Log.i(TAG, "BlackoutService: No valid time found for " + trigId);
			return;
		}

		/*
		 * Convert the alarm time to elapsed real time. If we dont do this, a
		 * time change in the system might set off all the alarms and a trigger
		 * might go off before we get a chance to cancel it
		 */

		long elapsedRT0 = alarmTimes.get(0) - System.currentTimeMillis();
		if (elapsedRT0 <= 0)
		{
			Log.i(TAG, "BlackoutService: negative elapsed realtime - " + "alarm not setting: " + trigId);
//			return;
		}
		else
		{
			Log.i(TAG, "BlackoutService: Setting alarm to start blackout for " + elapsedRT0 + " millis into the future");
			//		Log.d(TAG, "Setting start alarm for " + new Date(SystemClock.elapsedRealtime() + elapsedRT0).toString());
			mAlarmMan.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + elapsedRT0, pi0);
		}
		long elapsedRT1 = alarmTimes.get(1) - System.currentTimeMillis();
//		Log.d(TAG, "Setting start alarm for " + new Date(SystemClock.elapsedRealtime() + elapsedRT1).toString());
		if (elapsedRT1 <= 0)
		{
			Log.i(TAG, "BlackoutService: negative elapsed realtime - " + "alarm not setting: " + trigId);
			return;
		}
		
		if (elapsedRT0 <= 0 && elapsedRT1 > 0)
			Mobility.stopMobility(this, true); // If the phone is in a blackout now, stop it.

		Log.i(TAG, "BlackoutService: Setting stop to start blackout for " + elapsedRT1 + " millis into the future");

		mAlarmMan.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + elapsedRT1, pi1);
	}

	private void setTrigger(int trigId, String trigDesc)
	{

		Log.i(TAG, "BlackoutService: Attempting to set " + "the trigger: " + trigId);

		BlackoutDesc desc = new BlackoutDesc();
		if (desc.loadString(trigDesc))
		{
			setAlarm(trigId, desc);
		}
		else
		{
			Log.i(TAG, "BlackoutService: Failed to parse" + " trigger config: id = " + trigId);
		}
	}

	private void removeTrigger(int trigId, String trigDesc)
	{
		cancelAlarm(trigId, trigDesc);
	}

	@Override
	public IBinder onBind(Intent intent)
	{

		return null;
	}

	/* Receiver for alarms */
	public static class AlarmReceiver extends BroadcastReceiver
	{

		public void onReceive(Context context, Intent intent)
		{

			Log.e(TAG, "BlackoutService: Received broadcast");

			if (intent.getAction().equals(ACTION_TRIG_ALM))
			{

				Log.i(TAG, "BlackoutService: Handling alarm event");

				acquireWakeLock(context);

				Intent i = new Intent(context, BlackoutService.class);

				i.setAction(ACTION_HANDLE_TRIGGER);
				i.replaceExtras(intent);
				context.startService(i);
			}
		}
	}
}
