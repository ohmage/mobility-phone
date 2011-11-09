package edu.ucla.cens.mobility.blackout;

import java.util.Calendar;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;
import edu.ucla.cens.mobility.blackout.base.TriggerDB;
import edu.ucla.cens.mobility.blackout.utils.SimpleTime;

/**
 * { "time": "12:45", "start": "11:45", "end": "13:45", "repeat": ["Monday",
 * "Tuesday"], }
 */
public class BlackoutDesc
{
	private static final int DAY_SHORT_LEN = 3;

	// private static final String KEY_TIME = "time";
	private static final String KEY_START = "start";
	private static final String KEY_END = "end";
	private static final String KEY_REPEAT = "repeat";

	public static final int WEEKDAY = 0;
	// private static final String VAL_RANDOM = "random";

	public static final int WEEKEND = 1;

	// private SimpleTime mTrigTime = new SimpleTime();
	private SimpleTime mRangeStart = new SimpleTime();
	private SimpleTime mRangeEnd = new SimpleTime();
	// private boolean mIsRandomized = false;
	// private boolean mIsRangeEnabled = false;
	/*
	 * TODO: store the repeat days in using the constants in Calendar and
	 * convert them to the textual representation when required.
	 */
	private LinkedHashMap<String, Boolean> mRepeatList = new LinkedHashMap<String, Boolean>();

	public BlackoutDesc()
	{
		initialize(true);
	}

	private void initialize(boolean repeatStatus)
	{
		// mIsRandomized = false;
		// mIsRangeEnabled = false;

		// mRepeatList.put(getDayOfWeekString(Calendar.SUNDAY), repeatStatus);
		// mRepeatList.put(getDayOfWeekString(Calendar.MONDAY), repeatStatus);
		// mRepeatList.put(getDayOfWeekString(Calendar.TUESDAY), repeatStatus);
		// mRepeatList.put(getDayOfWeekString(Calendar.WEDNESDAY),
		// repeatStatus);
		// mRepeatList.put(getDayOfWeekString(Calendar.THURSDAY), repeatStatus);
		// mRepeatList.put(getDayOfWeekString(Calendar.FRIDAY), repeatStatus);
		// mRepeatList.put(getDayOfWeekString(Calendar.SATURDAY), repeatStatus);

		mRepeatList.put(getDayOfWeekString(WEEKDAY), repeatStatus);
		mRepeatList.put(getDayOfWeekString(WEEKEND), repeatStatus);
	}

	public static String getDayOfWeekString(int dayType)
	{
		// return DateUtils.getDayOfWeekString(dayOfWeek,
		// DateUtils.LENGTH_LONG);
		return dayType == WEEKDAY ? "Weekdays" : "Weekend";
	}

	public boolean loadString(String desc)
	{

		initialize(false);

		if (desc == null)
		{
			return false;
		}

		try
		{
			JSONObject jDesc = new JSONObject(desc);

			// String time = jDesc.getString(KEY_TIME);
			// if(time.equalsIgnoreCase(VAL_RANDOM)) {
			// mIsRandomized = true;
			// }
			// else {
			// if(!mTrigTime.loadString(time)) {
			// return false;
			// }
			// }

			if (jDesc.has(KEY_START))
			{

				if (!jDesc.has(KEY_END))
				{
					return false;
				}

				String start = jDesc.getString(KEY_START);
				if (!mRangeStart.loadString(start))
				{
					return false;
				}

				String end = jDesc.getString(KEY_END);
				if (!mRangeEnd.loadString(end))
				{
					return false;
				}

				// mIsRangeEnabled = true;
			}
			else if (jDesc.has(KEY_END))
			{
				// "End" without start - error
				return false;
			}

			JSONArray repeats = jDesc.getJSONArray(KEY_REPEAT);
			if (repeats.length() == 0)
			{
				return false;
			}

			for (int i = 0; i < repeats.length(); i++)
			{
				String day = repeats.getString(i);

				if (!mRepeatList.containsKey(day))
				{
					return false;
				}

				mRepeatList.put(day, true);
			}

		}
		catch (JSONException e)
		{
			return false;
		}

		return true;
	}

	public String toString()
	{

		JSONObject jDesc = new JSONObject();

		try
		{
			// jDesc.put(KEY_TIME, mIsRandomized ?
			// VAL_RANDOM : mTrigTime.toString(false));
			//
			// if(mIsRangeEnabled) {
			jDesc.put(KEY_START, mRangeStart.toString(false));
			jDesc.put(KEY_END, mRangeEnd.toString(false));
			// }

			JSONArray repeats = new JSONArray();
			for (String day : mRepeatList.keySet())
			{
				if (mRepeatList.get(day))
				{
					repeats.put(day);
				}
			}

			jDesc.put(KEY_REPEAT, repeats);

		}
		catch (JSONException e)
		{
			return null;
		}

		return jDesc.toString();
	}

	// public SimpleTime getTriggerTime() {
	//
	// return new SimpleTime(mTrigTime);
	// }
	//
	// public boolean setTriggerTime(SimpleTime time) {
	//
	// mTrigTime.copy(time);
	// return true;
	// }

	// public boolean isRandomized() {
	// return mIsRandomized;
	// }
	//
	// public void setRandomized(boolean randomize) {
	// mIsRandomized = randomize;
	// }
	//
	// public boolean isRangeEnabled() {
	// return mIsRangeEnabled;
	// }
	//
	// public void setRangeEnabled(boolean enable) {
	// mIsRangeEnabled = enable;
	// }

	public SimpleTime getRangeStart()
	{
		return new SimpleTime(mRangeStart);
	}

	public void setRangeStart(SimpleTime time)
	{
		mRangeStart.copy(time);
	}

	public SimpleTime getRangeEnd()
	{
		return new SimpleTime(mRangeEnd);
	}

	public void setRangeEnd(SimpleTime time)
	{
		mRangeEnd.copy(time);
	}

	public LinkedHashMap<String, Boolean> getRepeat()
	{
		LinkedHashMap<String, Boolean> ret = new LinkedHashMap<String, Boolean>();

		ret.putAll(mRepeatList);

		return ret;
	}

	public void setRepeatStatus(String day, boolean status)
	{
		mRepeatList.put(day, status);
	}

	private int getRepeatDayTypesCount()
	{
		int nRepeatDays = 0;
		for (String day : mRepeatList.keySet())
		{
			if (mRepeatList.get(day))
			{
				nRepeatDays++;
			}
		}

		return nRepeatDays;
	}

	public String getRepeatDescription()
	{
		String ret = "";

		int nRepeatDays = getRepeatDayTypesCount();

		if (nRepeatDays == 2)
		{
			ret = "Everyday";
		}
		else
		{
			int i = 0;
			for (String day : mRepeatList.keySet())
			{
				if (mRepeatList.get(day))
				{
					int strLen = day.length();

					// if(strLen > DAY_SHORT_LEN) {
					// strLen = DAY_SHORT_LEN;
					// }

					ret += day/* .substring(0, strLen) */;

					i++;
					if (i < nRepeatDays)
					{
						ret += ", ";
					}
				}
			}
		}

		return ret;
	}

	public boolean doesRepeatOnDay(String dayType)
	{

		return mRepeatList.get(dayType);
	}

	public boolean validate(Context context, int tId)
	{

		// if(mIsRangeEnabled) {

		if (!mRangeEnd.isAfter(mRangeStart))
		{
			return false;
		}

		// check other times
		TriggerDB db = new TriggerDB(context);
		db.open();

		Cursor c = db.getAllTriggers();
		if (c.moveToFirst())
		{
			do
			{
				int trigId = c.getInt(c.getColumnIndexOrThrow(TriggerDB.KEY_ID));
				if (trigId == tId)
					continue; // Don't validate against this same blackout's old settings. They can overlap.
				String trigDesc = db.getTriggerDescription(trigId);
				BlackoutDesc conf = new BlackoutDesc();

				if (!conf.loadString(trigDesc))
				{
					continue;
				}
				SimpleTime start = conf.getRangeStart();
				SimpleTime end = conf.getRangeEnd();
				if (start.isBefore(mRangeStart))
				{
					if (!end.isBefore(mRangeStart))
						return false;
					
				}
				else if (!start.isAfter(mRangeEnd))
				{
					return false;
				}


			} while (c.moveToNext());
		}
		c.close();
		db.close();

		// if(!mIsRandomized) {

		// if(mTrigTime.isAfter(mRangeEnd) ||
		// mTrigTime.isBefore(mRangeStart)) {
		// return false;
		// }
		// }
		// }

		if (getRepeatDayTypesCount() == 0)
		{
			return false;
		}

		return true;
	}
}
