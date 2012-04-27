package edu.ucla.cens.mobility;
import edu.ucla.cens.mobility.glue.MobilityInterface;

import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MobilityDbAdapter
{

	public static final String DEFAULT_TABLE = "mobility";
	public static final String DEFAULT_AGGREGATE_TABLE = "aggregate";
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

	private static boolean databaseOpen = false;
	private static Object dbLock = new Object();
	public static final String TAG = "awDB";
	private DatabaseHelper dbHelper;
	private static SQLiteDatabase db = null;

	private Context mCtx = null;

	// This variable should ONLY be set in the constructor. If it is set
	// anywhere else
	// in this class, then this could cause DatabaseHelper to be out of sync,
	// because it
	// will not necessarily get that updated name.
	private final String database_table;
	private final String aggregate_table;
	private static final int DATABASE_VERSION = 3;
	SharedPreferences settings;
	SharedPreferences.Editor editor;
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

	public class DatabaseHelper extends SQLiteOpenHelper
	{
		// Stores the name of the database table that is used in the parent
		// class
		private final String table;
		private final String aggregateTable;

		DatabaseHelper(Context ctx, String table, String aggregateTable)
		{
			super(ctx, table, null, DATABASE_VERSION);
			this.table = table;
			this.aggregateTable = aggregateTable;
			Log.d(TAG, "Calling constructor: Creating database name: " + table);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			Log.d(TAG, "onCreate: Creating database table: " + table);
			db.execSQL(String.format(DATABASE_CREATE, table));

			Log.d(TAG, "onCreate: Creating database table: " + aggregateTable);
			db.execSQL(String.format(AGGREGATE_TABLE_CREATE, aggregateTable));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			if(oldVersion < 3) {
				// Add the username column
				db.execSQL("ALTER TABLE " + table + " ADD COLUMN " +  KEY_USERNAME + " text default "+ DEFAULT_USERNAME);
				// We need to drop the aggregate table so we can add the column as primary key
				db.execSQL("DROP TABLE IF EXISTS " + aggregateTable);
			}

			// In all other cases we don't have to do anything to the database
			// to upgrade it the correct tables will be created in onCreate
			onCreate(db);
		}
	}

	private static Lock lock = new ReentrantLock();

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
	public synchronized Cursor getMobilityCursor(String[] columns, String selection, String[] selectionArgs, String orderBy)
	{
		lock.lock();

		Cursor c;
		try
		{
			Log.i(TAG, (dbHelper == null) + " that dbHelper is null");
			SQLiteDatabase sdb = new DatabaseHelper(mCtx, database_table, aggregate_table).getReadableDatabase();

			c = sdb.query("mobility", columns, selection, selectionArgs, null, null, orderBy);
			c.setNotificationUri(mCtx.getContentResolver(), MobilityInterface.CONTENT_URI);
		} catch (SQLiteException e)
		{
			Log.i(TAG, e.toString());
			c = null;
		}
		lock.unlock();
		
		return c;
	}

	public MobilityDbAdapter(Context ctx) {
		this(ctx, DEFAULT_TABLE, DEFAULT_AGGREGATE_TABLE, DEFAULT_SERVER_DB, DEFAULT_TYPE);
	}

	public MobilityDbAdapter(Context ctx, String table, String aggregateTable)
	{
		settings = ctx.getSharedPreferences(ctx.getString(R.string.prefs), 0);
		mCtx = ctx;
		database_table = table;
		aggregate_table = aggregateTable;
	}

	public MobilityDbAdapter(Context ctx, String table, String aggregateTable, String serverDB, String type)
	{
		this(ctx, table, aggregateTable);
		try
		{
			registerTable(table, serverDB, type);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private long random()
	{
		Random gen = new Random(System.currentTimeMillis());
		return gen.nextInt(Integer.MAX_VALUE / 2);

	}

	private synchronized void registerTable(String tableName, String serverDB, String type) throws Exception
	{
		editor = settings.edit();
		if (!settings.contains(tableName))
		{
			int numTables = settings.getInt("numTables", 0);
			editor.putLong(tableName, random());
			editor.putString(tableName + "_serverDB", serverDB);
			editor.putString(tableName + "_type", type);
			numTables++;
			editor.putString("Table" + numTables, tableName);
			editor.putInt("numTables", numTables);
			Log.d(TAG, "Now numTables is " + numTables);
			Log.d(TAG, "tableName is " + tableName);
			Log.d(TAG, "registerTable: registered " + tableName + "_serverDB = " + serverDB);
			Log.d(TAG, "registerTable: registered " + tableName + "_type = " + type);
			Log.d(TAG, "registerTable: registered Table" + numTables + " = " + tableName);
			editor.commit();
			dbHelper = new DatabaseHelper(mCtx, database_table, aggregate_table);
		} else if (!settings.getString(tableName + "_serverDB", "").equals(serverDB)) // update
																						// database
		{
			editor.putString(tableName + "_serverDB", serverDB);
			editor.commit();
		}
	}

	public MobilityDbAdapter open() throws SQLException
	{
		synchronized (dbLock)
		{
			while (databaseOpen)
			{
				try
				{
					dbLock.wait();
				} catch (InterruptedException e)
				{
				}

			}
			databaseOpen = true;
			dbHelper = new DatabaseHelper(mCtx, database_table, aggregate_table);
			try
			{
				db = dbHelper.getWritableDatabase();
				Log.d(TAG, "Trying to getWriteableDatabase: " + db.toString());
			} catch (SQLiteException e)
			{
				Log.e(TAG, "Could not open database: " + database_table);
				return null;
			}
			return this;
		}
	}

	public void close()
	{
		synchronized (dbLock)
		{
			// if (dbHelper != null)
			dbHelper.close();
			databaseOpen = false;
			dbLock.notify();
		}
	}

	public SQLiteDatabase getDb() {
		return db;
	}

	public long createRow(String mode, long time, String status, String speed, long timestamp, String accuracy, String provider, String wifiData, Vector<ArrayList<Double>> samples, String latitude,
			String longitude)
	{
		ContentValues vals = new ContentValues();
		if (db == null)
		{
			Log.e(TAG, "ERROR, database table: " + database_table + " was not initialized!");
			return -1;
		}
	

		else if (db.inTransaction())
		{
			Log.e(TAG, "ERROR, database in transaction, why is this happening?");
			return -1;
		}
		
		SharedPreferences settings = mCtx.getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
		String username = settings.getString(Mobility.KEY_USERNAME, DEFAULT_USERNAME);

		if (speed.equals(""))
			speed = "NaN";
		if (accuracy.equals(""))
			accuracy = "NaN";
		if (wifiData.equals(""))
			wifiData = "{}";
		if (latitude.equals(""))
			latitude = "NaN";
		if (longitude.equals(""))
			longitude = "NaN";
		
		UUID id = UUID.randomUUID();
		
		String timezone = DateTimeZone.getDefault().getID();
//		Log.i(TAG, timezone + " is the timezone, per Jovem!");
		editor.putLong(database_table, 1);
		editor.commit();
		vals.put(KEY_ID, id.toString());
		Log.d(TAG, id.toString());
		vals.put(KEY_MODE, mode);
		vals.put(KEY_SPEED, speed);
		vals.put(KEY_STATUS, status);
		vals.put(KEY_LOC_TIMESTAMP, timestamp);
		vals.put(KEY_ACCURACY, accuracy);
		vals.put(KEY_PROVIDER, provider);
		vals.put(KEY_WIFIDATA, wifiData);
		vals.put(KEY_ACCELDATA, formatAccelData(samples));
		vals.put(KEY_TIME, time + "");
		vals.put(KEY_TIMEZONE, timezone);
		vals.put(KEY_LATITUDE, latitude);
		vals.put(KEY_LONGITUDE, longitude);
		vals.put(KEY_USERNAME, username);
		Log.d(TAG, "createRow: adding to table: " + database_table + ": " + mode);
		long rowid = db.insert(database_table, null, vals);

		// Update the time so far for today for that mode
		db.beginTransaction();
		try {
			// First create the mode if it doesn't exist
			ContentValues countValues = new ContentValues();
			countValues.put(KEY_MODE, mode);
			countValues.put(KEY_USERNAME, username);
			db.insertWithOnConflict(aggregate_table, KEY_MODE, countValues, SQLiteDatabase.CONFLICT_IGNORE);

			// Amount of time that has passed since the last insert (or 5 minutes max)
			long lastRowInsert = Math.min(time - settings.getLong(Mobility.LAST_INSERT, time), DateUtils.MINUTE_IN_MILLIS * 5);
			db.execSQL("UPDATE " + aggregate_table + " SET " + KEY_DURATION + "=" + KEY_DURATION + "+"
					+ lastRowInsert + " WHERE " + KEY_MODE + "=? AND "
					+ KEY_DAY + "=" + SQL_TODAY_LOCAL + " AND " + KEY_USERNAME + "=?", new String[] { mode, username });

			// Save the time of this insert
			settings.edit().putLong(Mobility.LAST_INSERT, time).commit();
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		ContentResolver r = mCtx.getContentResolver();
		r.notifyChange(MobilityInterface.CONTENT_URI, null);
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
				Log.e(TAG, "X has " + samples.get(0).size() + ", Y has " + samples.get(1).size() + ", Z has " + samples.get(2).size());
				
//				throw ie; // want crash to alert me
			}
			catch(NullPointerException e)
			{
				Log.e(TAG, "Null pointer somehow");
			}
			ja.put(jo);
		}
		
		return ja.toString();
	}

	// public long createRow(String subtype, String mode, long time, String
	// latitude, String longitude)
	// {
	// ContentValues vals = new ContentValues();
	// if (db == null) {
	// Log.e(TAG, "ERROR, database table: " + database_table +
	// " was not initialized!");
	// return -1;
	// }
	//
	// else if (db.inTransaction()) {
	// Log.e(TAG, "ERROR, database in transaction, why is this happening?");
	// return -1;
	// }
	//
	//
	// editor.putLong(database_table, 1);
	// editor.commit();
	// vals.put(KEY_MODE, mode);
	// vals.put(KEY_TIME, time);
	// vals.put(KEY_LATITUDE, latitude);
	// vals.put(KEY_LONGITUDE, longitude);
	// Log.d(TAG, "createRow: adding to table: " + database_table + ": " +
	// mode);
	// long rowid = db.insert(database_table, null, vals);
	// return rowid;
	// }

	/**
	 * Delete rows that are older than the timestamp. Used for garbage
	 * collection.
	 */
	public int deleteSomeRows(long timestamp)
	{
		int dels = 0;

		// try
		{
			Log.d(TAG, "fetchSomeRows from table: " + database_table);
			dels = db.delete(database_table, KEY_TIME + "<= ?", new String[] { "" + timestamp });
		}
		// catch (Exception e)
		// {
		// Log.e(TAG, e.getMessage());
		// }
		ContentResolver r = mCtx.getContentResolver();
		r.notifyChange(MobilityInterface.CONTENT_URI, null);
		return dels;
	}

	public boolean deleteRow(long rowId)
	{
		int count = 0;
		count = db.delete(database_table, KEY_ROWID + "=" + rowId, null);
		Log.d(TAG, "deleteRow: deleting row: " + rowId + "from table: " + database_table);
		if (count > 0)
		{
			ContentResolver r = mCtx.getContentResolver();
			r.notifyChange(MobilityInterface.CONTENT_URI, null);
			return true;
		}
		else
			return false;
	}
	
	public ArrayList<DBRow> fetchSomeRows(Integer numLines)
	{
		ArrayList<DBRow> ret = new ArrayList<DBRow>();

		try
		{
			Log.d(TAG, "fetchSomeRows from table: " + database_table);
			Cursor c = db.query(database_table, new String[] { KEY_ROWID, KEY_ID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE }, null, null,
					null, null, null, numLines.toString());
			int numRows = c.getCount();
			Log.d(TAG, numRows + " rows in the table\n");
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i)
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
			Log.e(TAG, e.getMessage());
		}
		return ret;
	}

	public ArrayList<DBRow> fetchSomeRows(Integer numLines, long last)
	{
		ArrayList<DBRow> ret = new ArrayList<DBRow>();

		try
		{
			Log.d(TAG, "fetchSomeRows from table: " + database_table);
			Cursor c = db.query(database_table, 
					new String[] { KEY_ROWID, KEY_ID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, 
					KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE }, KEY_ROWID + " > " + last, null,
					null, null, null, numLines.toString());
			int numRows = c.getCount();
			Log.d(TAG, numRows + " rows in the table\n");
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i)
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
			Log.e(TAG, e.getMessage());
		}
		return ret;
	}
	
	public ArrayList<DBRow> fetchSomeRows(long timeLimit)
	{
		ArrayList<DBRow> ret = new ArrayList<DBRow>();

		try
		{
			Log.d(TAG, "fetchSomeRows from table: " + database_table);
			Cursor c = db.query(database_table, new String[] { KEY_ROWID, KEY_ID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE }, KEY_TIME
					+ " < " + timeLimit, null, null, null, null, null);
			int numRows = c.getCount();
			Log.d(TAG, numRows + " rows in the table\n");
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i)
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
			Log.e(TAG, e.getMessage());
		}
		return ret;
	}

	public ArrayList<DBRow> fetchAllRows()
	{
		ArrayList<DBRow> ret = new ArrayList<DBRow>();

		try
		{
			Log.d(TAG, "fetchSomeRows from table: " + database_table);
			Cursor c = db.query(database_table, new String[] { KEY_ROWID, KEY_ID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE }, null, null, null, null,
					null, null);
			int numRows = c.getCount();
			Log.d(TAG, numRows + " rows in the table\n");
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i)
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
			Log.e(TAG, e.getMessage());
		}
		return ret;
	}

	public DBRow fetchRow(long rowId) throws SQLException
	{
		Cursor c = db.query(database_table, new String[] { KEY_ROWID, KEY_ID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE }, KEY_ROWID + "=" + rowId,
				null, null, null, null);
		DBRow ret = new DBRow();

		if (c != null)
		{
			c.moveToFirst();

			ret.rowValue = c.getLong(0);
			ret.idValue = c.getString(1);
			ret.modeValue = c.getString(2);
			ret.speedValue = c.getString(3);
			ret.statusValue = c.getString(4);
			ret.locTimeValue = c.getString(5);
			ret.accuracyValue = c.getString(6);
			ret.providerValue = c.getString(7);
			ret.wifiDataValue = c.getString(8);
			ret.accelDataValue = c.getString(9);
			ret.timeValue = c.getString(10);
			ret.timezoneValue = c.getString(11);
			if (c.isNull(12))
				ret.latitudeValue = null;
			else
				ret.latitudeValue = c.getString(12);
			if (c.isNull(13))
				ret.longitudeValue = null;
			else
				ret.longitudeValue = c.getString(13);
		} else
		{
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

		}
		c.close();
		return ret;
	}

	public boolean updateRowData(long rowId, String modeValue)
	{
		ContentValues vals = new ContentValues();
		vals.put(KEY_MODE, modeValue);
		Log.d(TAG, "updateRow: Updating mode: " + modeValue + "at rowId = " + rowId);
		return db.update(database_table, vals, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean updateRowTime(long rowId, long timeValue)
	{
		ContentValues vals = new ContentValues();
		vals.put(KEY_TIME, timeValue);
		Log.d(TAG, "updateRow: Updating time " + timeValue + "at rowId = " + rowId);
		return db.update(database_table, vals, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean updateRowLocation(long rowId, String latitudeValue, String longitudeValue)
	{
		ContentValues vals = new ContentValues();
		vals.put(KEY_LATITUDE, latitudeValue);
		vals.put(KEY_LONGITUDE, longitudeValue);
		Log.d(TAG, "updateRow: Updating location " + latitudeValue + ", " + longitudeValue + "at rowId = " + rowId);
		return db.update(database_table, vals, KEY_ROWID + "=" + rowId, null) > 0;
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
		lock.lock();

		Cursor c;
		try
		{
			Log.i(TAG, (dbHelper == null) + " that dbHelper is null");
			SQLiteDatabase sdb = new DatabaseHelper(mCtx, database_table, aggregate_table).getReadableDatabase();

			if(selection == null)
				selection = KEY_DAY + "=" + SQL_TODAY_LOCAL;

			c = sdb.query(aggregate_table, columns, selection, selectionArgs, null, null, sortOrder);
			c.setNotificationUri(mCtx.getContentResolver(), MobilityInterface.CONTENT_URI);
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
			c = null;
		} finally {
			lock.unlock();
		}

		return c;
	}

	public void insertMobilityAggregate(ContentValues values) {
		db.insertWithOnConflict(aggregate_table, KEY_DAY, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	/**
	 * Updates the username for mobility points after a certain time
	 * @param username
	 * @param backdate
	 */
	public void updateUsername(String username, long backdate) {
		ContentValues vals = new ContentValues();
		vals.put(KEY_USERNAME, username);
		db.update(database_table, vals, KEY_USERNAME + "=? AND " + KEY_TIME + ">=" + backdate, new String[] { DEFAULT_USERNAME });
	}
}
