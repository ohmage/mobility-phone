package org.ohmage.mobility;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.logprobe.Log;
import org.ohmage.mobility.glue.MobilityInterface;
import org.ohmage.probemanager.ProbeBuilder;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

public class MobilityDbAdapter {
	public static final String TAG = "MobilityDbAdapter";

	public static final String MOBILITY_TABLE = "mobility";
	public static final String AGGREGATE_TABLE = "aggregate";
	public static final String DEFAULT_SERVER_DB = "mobility";
	public static final String DEFAULT_TYPE = "mobility";

	public static final String KEY_ID = "id";
	public static final String KEY_MODE = "mode";
	public static final String KEY_SPEED = "speed";
	public static final String KEY_STATUS = "status";
	public static final String KEY_LOC_TIMESTAMP = "location_timestamp";
	public static final String KEY_ACCURACY = "accuracy";
	public static final String KEY_PROVIDER = "provider";
	public static final String KEY_WIFIDATA = "wifi_data";
	public static final String KEY_ACCELDATA = "accel_data";
	public static final String KEY_TIMEZONE = "timezone";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TIME = "time";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	private static final String KEY_DURATION = "duration";
	public static final String KEY_USERNAME = "username";
	public static final String DEFAULT_USERNAME = "default_user";

	private final Context mCtx;

	private static final String DATABASE_NAME = "mobility";
	private static final int DATABASE_VERSION = 3;


	private static final String DATABASE_CREATE = "create table if not exists %s ("
			+ KEY_ROWID + " integer primary key autoincrement," 
			+ KEY_ID + " text not null,"
			+ KEY_MODE + " text not null," 
			+ KEY_SPEED + " text not null," 
			+ KEY_STATUS + " text not null," 
			+ KEY_LOC_TIMESTAMP + " text not null," 
			+ KEY_ACCURACY + " text not null," 
			+ KEY_PROVIDER + " text not null," 
			+ KEY_WIFIDATA + " text not null," 
			+ KEY_ACCELDATA + " text not null," 
			+ KEY_TIME + " text not null," 
			+ KEY_TIMEZONE + " text not null," 
			+ KEY_LATITUDE + " text," 
			+ KEY_LONGITUDE + " text,"
			+ KEY_USERNAME + " text default " + DEFAULT_USERNAME + ");";

	private static final String KEY_DAY = "day";
	private static final String SQL_TODAY_LOCAL = "date('now', 'localtime')";

	private static final String AGGREGATE_TABLE_CREATE = "create table if not exists %s ("
			+ KEY_DAY + " text default (" + SQL_TODAY_LOCAL + "),"
			+ KEY_MODE + " text not null,"
			+ KEY_DURATION + " integer default 0,"
			+ KEY_USERNAME + " text default " + DEFAULT_USERNAME + "," +
			" primary key ( " + KEY_DAY + " , " + KEY_MODE + " , " + KEY_USERNAME + " ));";

	public class DBRow extends Object
	{
		public long rowValue;
		public String idValue;
		public String statusValue;
		public String locTimeValue;
		public String accuracyValue;
		public String providerValue;
		public String wifiDataValue;
		public String accelDataValue;
		public String modeValue;
		public String speedValue;
//		public String varianceValue;
//		public String averageValue;
//		public String fftValue;
		public String timeValue;
		public String timezoneValue;
		public String latitudeValue;
		public String longitudeValue;
	}

	public static class DatabaseHelper extends SQLiteOpenHelper {
		private final Context mContext;

		DatabaseHelper(Context ctx) {
			super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = ctx;
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(TAG, "onCreate: Creating database table: " + MOBILITY_TABLE);
			db.execSQL(String.format(DATABASE_CREATE, MOBILITY_TABLE));

			Log.v(TAG, "onCreate: Creating database table: " + AGGREGATE_TABLE);
			db.execSQL(String.format(AGGREGATE_TABLE_CREATE, AGGREGATE_TABLE));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if(oldVersion < 3) {
				// Add the username column
				db.execSQL("ALTER TABLE " + MOBILITY_TABLE + " ADD COLUMN " +  KEY_USERNAME + " text default "+ DEFAULT_USERNAME);
				// We need to drop the aggregate table so we can add the column as primary key
				db.execSQL("DROP TABLE IF EXISTS " + AGGREGATE_TABLE);
			}

			// In all other cases we don't have to do anything to the database
			// to upgrade it the correct tables will be created in onCreate
			onCreate(db);
		}

		public long insertMobilityPoint(ContentValues values) {
			long ret = getWritableDatabase().insert(MOBILITY_TABLE, null, values);

			SharedPreferences settings = mContext.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
			long time = values.getAsLong(KEY_TIME);
			String mode = values.getAsString(KEY_MODE);
			String username = values.getAsString(KEY_USERNAME);

			// Update the time so far for today for that mode
			getWritableDatabase().beginTransaction();
			try {
				// First create the mode if it doesn't exist
				long duration = Math.min(time - settings.getLong(Mobility.LAST_INSERT, time), DateUtils.MINUTE_IN_MILLIS * 5);
				addAggregate(mode, username, duration, null);

				// Save the time of this insert
				settings.edit().putLong(Mobility.LAST_INSERT, time).commit();
				getWritableDatabase().setTransactionSuccessful();
			} finally {
				getWritableDatabase().endTransaction();
			}
			return ret;
		}
		
		public int deleteMobiltyPoint(String where, String[] whereArgs) {
			return getWritableDatabase().delete(MOBILITY_TABLE, where, whereArgs);
		}

		public int updateMobilityPoint(ContentValues values, String where, String[] whereArgs) {
			return getWritableDatabase().update(MOBILITY_TABLE, values, where, whereArgs);
		}

		/**
		 * Adds the duration to aggregate specified by the mode, username, duration key
		 * @param mode
		 * @param username
		 * @param duration
		 * @param day optional
		 */
		private int addAggregate(String mode, String username, long duration, String day) {
			ContentValues countValues = new ContentValues();
			countValues.put(KEY_MODE, mode);
			countValues.put(KEY_USERNAME, username);
			if(!TextUtils.isEmpty(day)) {
				countValues.put(KEY_DAY, day);
			}

			try {
				getWritableDatabase().insertWithOnConflict(AGGREGATE_TABLE, KEY_MODE, countValues, SQLiteDatabase.CONFLICT_IGNORE);
				getWritableDatabase().execSQL("UPDATE " + AGGREGATE_TABLE + " SET " + KEY_DURATION + "=" + KEY_DURATION + "+"
						+ duration + " WHERE " + KEY_MODE + "=? AND "
						+ KEY_DAY + "=" + ((TextUtils.isEmpty(day)) ? SQL_TODAY_LOCAL : "'"+day+"'") + " AND " + KEY_USERNAME + "=?", new String[] { mode, username });
			} catch (SQLiteException e) {
				Log.e(TAG, "error adding aggregate value ", e);
				return 0;
			}
			return 1;
		}

		public long insertMobilityAggregate(ContentValues values) {
			return getWritableDatabase().insertWithOnConflict(AGGREGATE_TABLE, KEY_DAY, values, SQLiteDatabase.CONFLICT_REPLACE);
		}

		public int deleteMobilityAggregate(String where, String[] whereArgs) {
			return getWritableDatabase().delete(AGGREGATE_TABLE, where, whereArgs);
		}

		public int addToAggregate(ContentValues values) {
			String mode = values.getAsString(KEY_MODE);
			String username = values.getAsString(KEY_USERNAME);
			String day = values.getAsString(KEY_DAY);
			long duration = values.getAsLong(KEY_DURATION);

			return addAggregate(mode, username, duration, day);
		}

		/**
		 * Connects to the database, gets the information requested based on the
		 * parameters, closes the connection to the database, and returns the Cursor
		 * object that the query generated.
		 * 
		 * @param columns
		 *            A String array representing the columns that are being
		 *            requested.
		 * 
		 * @param selection
		 *            A SQLite WHERE clause without the "WHERE" keyword.
		 * 
		 * @param selectionArgs
		 *            A String array used for replacing "?"s in the 'selection'
		 *            String with values. The number of items in the array should be
		 *            equal to the number of "?"s in the 'selection' String. 'null'
		 *            is an acceptable value if there are no "?"s in the 'selection'
		 *            String.
		 * 
		 * @param orderBy
		 *            A SQLite "ORDER BY" clause without the "ORDER BY" keyword.
		 * 
		 * @return A Cursor object pointing to all the elements that satisfy the the
		 *         parameters. If a SQLite exception occurs, null is returned.
		 */
		public Cursor getMobilityCursor(String[] columns, String selection, String[] selectionArgs, String orderBy) {
			Cursor c;
			try {
				c = getReadableDatabase().query("mobility", columns, selection, selectionArgs, null, null, orderBy);
				c.setNotificationUri(mContext.getContentResolver(), MobilityInterface.CONTENT_URI);
			} catch (SQLiteException e) {
				Log.e(TAG, "Error getting mobility cursor", e);
				c = null;
			}

			return c;
		}

		/**
		 * Returns the aggregates for today
		 * @param sortOrder 
		 * @param selectionArgs 
		 * @param selection 
		 * @param columns 
		 * @return
		 */
		public Cursor getMobilityAggregatesCursor(String[] columns, String selection, String[] selectionArgs, String sortOrder) {
			Cursor c;
			try {
				if(selection == null)
					selection = KEY_DAY + "=" + SQL_TODAY_LOCAL;

				// If we are querying for the average duration, we assume we want to group by mode
				String groupby = null;
				if(columns != null) {
					for(String column : columns)
						if(column.toLowerCase().equals("avg(" + MobilityInterface.KEY_DURATION.toLowerCase() + ")"))
							groupby = KEY_MODE;
				}

				c = getReadableDatabase().query(AGGREGATE_TABLE, columns, selection, selectionArgs, groupby, null, sortOrder);
				c.setNotificationUri(mContext.getContentResolver(), MobilityInterface.CONTENT_URI);
			} catch (SQLiteException e) {
				Log.e(TAG, "Error getting mobility aggregates cursor", e);
				c = null;
			}

			return c;
		}
	}

	public MobilityDbAdapter(Context ctx) {
		mCtx = ctx;
	}

	public long createRow(String mode, long time, String status, Float speed, long timestamp, Float accuracy, String provider, String wifiData, Vector<ArrayList<Double>> samples, Double latitude, Double longitude) {
		ContentValues vals = new ContentValues();

		String username = DEFAULT_USERNAME;

		AccountManager am = AccountManager.get(mCtx);
		Account[] accounts = am.getAccountsByType("org.ohmage");
		if(accounts.length > 0) {
		    username = accounts[0].name;
		} else {
			// Maybe ohmage is old and told us the username
			SharedPreferences settings = mCtx.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
			username = settings.getString(Mobility.KEY_USERNAME, DEFAULT_USERNAME);
		}

		if (wifiData.equals(""))
			wifiData = "{}";

		UUID id = UUID.randomUUID();
		
		String timezone = DateTimeZone.getDefault().getID();
		vals.put(KEY_ID, id.toString());
		Log.v(TAG, id.toString());
		vals.put(KEY_MODE, mode);
		vals.put(KEY_SPEED, speed.toString());
		vals.put(KEY_STATUS, status);
		vals.put(KEY_LOC_TIMESTAMP, timestamp);
		vals.put(KEY_ACCURACY, accuracy.toString());
		vals.put(KEY_PROVIDER, provider);
		vals.put(KEY_WIFIDATA, wifiData);
		vals.put(KEY_ACCELDATA, formatAccelData(samples));
		vals.put(KEY_TIME, time + "");
		vals.put(KEY_TIMEZONE, timezone);
		vals.put(KEY_LATITUDE, latitude.toString());
		vals.put(KEY_LONGITUDE, longitude.toString());
		vals.put(KEY_USERNAME, username);
		Log.v(TAG, "createRow: adding to table: " + MOBILITY_TABLE + ": " + mode);

		long rowid = -1;
		Uri row = mCtx.getContentResolver().insert(MobilityInterface.CONTENT_URI, vals);

        ProbeBuilder probe = new ProbeBuilder();
        probe.withId(id.toString()).withTime(time, timezone);
        if(!"unavailable".equals(status))
            probe.withLocation(time, timezone, latitude, longitude, accuracy, provider);

        // We can't send NaN or Inf, so we set those values to null
        if(Float.isInfinite(speed) || Float.isNaN(speed))
            speed = null;

        Mobility.writeProbe(mCtx, probe, mode, speed, formatAccelData(samples), wifiData);

		if(row != null) {
			rowid = ContentUris.parseId(row);
			ContentResolver r = mCtx.getContentResolver();
			r.notifyChange(MobilityInterface.CONTENT_URI, null, false);
		}
		return rowid;
	}

	private String formatAccelData(Vector<ArrayList<Double>> samples)
	{
		if (samples == null)
			return "[]";
		JSONArray ja = new JSONArray();
		// Sometime there are more x values for some strange reason. We don't want to upload incomplete x,y,z sets.
		int minSize = 100; // max allowed
		if (samples.get(0).size() < minSize)
			minSize = samples.get(0).size(); 
		if (samples.get(1).size() < minSize)
			minSize = samples.get(1).size();
		if (samples.get(2).size() < minSize)
			minSize = samples.get(2).size();
		for (int i = 0; i < minSize; i++)
		{
			JSONObject jo = new JSONObject();
			try
			{
				jo.put("x", samples.get(0).get(i));
				jo.put("y", samples.get(1).get(i));
				jo.put("z", samples.get(2).get(i));
			} catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(IndexOutOfBoundsException ie)
			{
				Log.e(TAG, "X has " + samples.get(0).size() + ", Y has " + samples.get(1).size() + ", Z has " + samples.get(2).size(), ie);
				
//				throw ie; // want crash to alert me
			}
			catch(NullPointerException e)
			{
				Log.e(TAG, "Null pointer somehow", e);
			}
			ja.put(jo);
		}
		
		return ja.toString();
	}

	/**
	 * Delete rows that are older than the timestamp. Used for garbage
	 * collection.
	 */
	public int deleteSomeRows(long timestamp) {
		int dels = 0;

		Log.v(TAG, "fetchSomeRows from table: " + MOBILITY_TABLE);
		ContentResolver cr = mCtx.getContentResolver();
		dels = cr.delete(MobilityInterface.CONTENT_URI, KEY_TIME + "<= ?", new String[] { String.valueOf(timestamp) });
		cr.notifyChange(MobilityInterface.CONTENT_URI, null, false);
		return dels;
	}

	public boolean deleteRow(long rowId) {
		int count = 0;
		ContentResolver cr = mCtx.getContentResolver();
		Log.v(TAG, "deleteRow: deleting row: " + rowId + "from table: " + MOBILITY_TABLE);
		count = cr.delete(MobilityInterface.CONTENT_URI, KEY_ROWID + "=" + rowId, null);
		if (count > 0) {
			cr.notifyChange(MobilityInterface.CONTENT_URI, null, false);
			return true;
		}

		return false;
	}

	public ArrayList<DBRow> fetchSomeRows(Integer numLines, String selection, String[] selectionArgs) {
		ArrayList<DBRow> ret = new ArrayList<DBRow>();

		try
		{
			Log.v(TAG, "fetchSomeRows from table: " + MOBILITY_TABLE);
			ContentResolver cr = mCtx.getContentResolver();
			Cursor c = cr.query(MobilityInterface.CONTENT_URI, new String[] { KEY_ROWID, KEY_ID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE }, selection, selectionArgs, null);
			c.moveToFirst();
			for (int i = 0; i < c.getCount() && i < numLines; ++i)
			{
				DBRow row = new DBRow();
				row.rowValue = c.getLong(0);
				row.idValue = c.getString(1);
				row.modeValue = c.getString(2);
				row.speedValue = c.getString(3);
				row.statusValue = c.getString(4);
				row.locTimeValue = c.getString(5);
				row.accuracyValue = c.getString(6);
				row.providerValue = c.getString(7);
				row.wifiDataValue = c.getString(8);
				row.accelDataValue = c.getString(9);
				row.timeValue = c.getString(10);
				row.timezoneValue = c.getString(11);
				if (c.isNull(12))
					row.latitudeValue = null;
				else
					row.latitudeValue = c.getString(12);
				if (c.isNull(13))
					row.longitudeValue = null;
				else
					row.longitudeValue = c.getString(13);
				ret.add(row);
				c.moveToNext();
			}
			c.close();
		} catch (Exception e)
		{
			Log.e(TAG, "Error reading rows from mobility table", e);
		}
		return ret;
	}

	public ArrayList<DBRow> fetchSomeRows(Integer numLines) {
		return fetchSomeRows(numLines, null, null);
	}
	
	public ArrayList<DBRow> fetchSomeRows(Integer numLines, long last) {
		return fetchSomeRows(numLines, KEY_ROWID + " > " + last, null);
	}
	
	public ArrayList<DBRow> fetchSomeRows(long timeLimit) {
		return fetchSomeRows(Integer.MAX_VALUE, KEY_TIME + " < " + timeLimit, null);
	}

	public ArrayList<DBRow> fetchAllRows() {
		return fetchSomeRows(Integer.MAX_VALUE, null, null);
	}

	public DBRow fetchRow(long rowId) {
		ArrayList<DBRow> rows = fetchSomeRows(1, KEY_ROWID + "=" + rowId, null);
		if(!rows.isEmpty()) {
			return rows.get(0);
		} else {
			DBRow ret = new DBRow();
			ret.rowValue = -1;
			ret.idValue = null;
			ret.modeValue = null;
			ret.statusValue = null;
			ret.locTimeValue = null;
			ret.accuracyValue = null;
			ret.providerValue = null;
			ret.wifiDataValue = null;
			ret.accelDataValue = null;
			ret.timeValue = null;
			ret.timezoneValue = null;
			ret.latitudeValue = null;
			ret.longitudeValue = null;
			return ret;
		}
	}

	/**
	 * Updates the username for mobility points after a certain time
	 * @param username
	 * @param backdate
	 */
	public void updateUsername(String username, long backdate) {
		ContentValues vals = new ContentValues();
		vals.put(KEY_USERNAME, username);
		mCtx.getContentResolver().update(MobilityInterface.CONTENT_URI, vals, KEY_USERNAME + "=? AND " + KEY_TIME + ">=" + backdate, new String[] { DEFAULT_USERNAME });
	}
}
