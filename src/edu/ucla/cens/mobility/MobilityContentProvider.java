package edu.ucla.cens.mobility;

import edu.ucla.cens.mobility.MobilityDbAdapter.DatabaseHelper;
import edu.ucla.cens.mobility.glue.MobilityInterface;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

/**
 * This is the ContentProvider for the System Sens Lite Service that provides
 * all the information it has collected to other processes. Given that it
 * operates independently of all other projects, there is little expectation
 * that anyone will know anything about the types of information that it
 * stores. Therefore, external documentation will be available to show exactly
 * what types of information are available, what URI should be used to access
 * it, and the actual data types of that information, so it can correctly be
 * read.
 * 
 * <p>An example usage is as follows:</p>
 * <pre>
 *	Uri.Builder builder = new Uri.Builder();
 *	builder.scheme("content");
 *	builder.authority("edu.ucla.cens.systemsenslite.contentprovider");
 *	builder.path("event");
 *	Uri uri = builder.build();
 *	
 *	getContentResolver().update(uri, new ContentValues(), null, null);
 *	
 *	String[] columns = { "_id", "app", "activity", "time" };
 *	Cursor c = managedQuery(uri, columns, null, null, "time ASC");
 *	
 *	c.moveToFirst();
 * </pre>
 * <p>The URI Builder calls can be chained together, but they are separated
 * here for clarity. The anonymous ContentValues object is needed even though
 * it isn't used or else {@link #update(Uri, ContentValues, String, String[])}
 * will throw a NullPointerException exception. Also, remember that your
 * Cursor point to nothing until it is explicitly moved to a row such as
 * first.</p>
 * 
 * @author John Jenkins
 * @version 1.0
 */
public class MobilityContentProvider extends ContentProvider
{
	private static final String TAG = "MobilityContentProvider";

	private static final int URI_CODE_MOBILITY = 1;
	private static final int URI_CODE_AGGREGATES = 2;
	private static final int URI_CODE_AGGREGATES_ADD = 3;

	private static final UriMatcher mUriMatcher;

	private DatabaseHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(getContext());
		return true;
	}

	/**
	 * We want to download aggregate data from the server. Since ohmage handles
	 * interaction with the server we will have to trust it to insert the data
	 * correctly
	 * 
	 * @param uri
	 * @param values
	 * @return returns aggregate uri if insert was successful, null otherwise
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long id = -1;
		switch (mUriMatcher.match(uri)) {
			case URI_CODE_AGGREGATES: {
				id = dbHelper.insertMobilityAggregate(values);
				break;
			}
			case URI_CODE_MOBILITY: {
				id = dbHelper.insertMobilityPoint(values);
				break;
			}
		}

		if (!dbHelper.getWritableDatabase().inTransaction())
			getContext().getContentResolver().notifyChange(uri, null);

		if(id == -1)
			return null;
	
		return ContentUris.appendId(uri.buildUpon(), id).build();
	}

	/**
	 * Does nothing
	 */
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		int ret = 0;
		switch (mUriMatcher.match(uri)) {
			case URI_CODE_MOBILITY: {
				ret = dbHelper.updateMobilityPoint(values, where, whereArgs);
				break;
			}
			case URI_CODE_AGGREGATES_ADD: {
				ret = dbHelper.addToAggregate(values);
				break;
			}
		}
		return ret;
	}

	/**
	 * Delete data from the content provider
	 * 
	 * @param uri
	 * @param where
	 * @param whereArgs
	 * @return returns number of rows deleted
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int res = 0;
		switch (mUriMatcher.match(uri)) {
			case URI_CODE_AGGREGATES: {
				res = dbHelper.deleteMobilityAggregate(where, whereArgs);
				break;
			}
			case URI_CODE_MOBILITY: {
				res = dbHelper.deleteMobiltyPoint(where, whereArgs);
				break;
			}
		}
		if (!dbHelper.getWritableDatabase().inTransaction())
			getContext().getContentResolver().notifyChange(uri, null);

		return res;
	}
	
	/**
	 * Queries the database for the given information. The URI will determine
	 * which table will be queried.
	 * 
	 * <p>All URIs should be of the format:</p>
	 * 
	 * <pre>	content://{@link #AUTHORITY}/&#60path&#62</pre>
	 * 
	 * <p>The different path values will result in the type of data that is
	 * returned. The following is the description of what data each path will
	 * provide:<p>
	 * <table border=1 cellspacing=0 cellpadding=3>
	 *  <tr bgcolor="#CCCCFF"><th align=center>Path</td>
	 *      <th colspan=3 align=center>Description</td></tr>
	 * 	<tr bgcolor="#CCFFCC"><td>event</td><td colspan=3>The time that each
	 * 		application had an event (create, restart, etc.) took place. The
	 * 		format of the returned Cursor is based on the provided columns. 
	 * 		The available columns are:</td></tr>
	 * 		<tr><td></td><td>_id</td><td>Integer</td><td>A unique identifier
	 * 			for this record.</td></tr>
	 * 		<tr><td></td><td>action</td><td>Integer</td><td>0 = Create, 
	 * 			1 = Restart, 2 = Resume, 3 = Pause</td></tr>
	 * 		<tr><td></td><td>time</td><td>Long</td><td>The time in nanoseconds
	 * 			that the action took place. This has no relation to the  wall
	 * 			time.</td></tr>
	 * 		<tr><td></td><td>app</td><td>String</td><td>The fully qualified
	 * 			name of the application that this action refers to.</td></tr>
	 * 		<tr><td></td><td>activity</td><td>String</td><td>The Activity
	 * 			within the "app" that took this action.</td></tr>
	 * 
	 * 	<tr bgcolor="#CCFFCC"><td>event/duration</td><td colspan=3>A list of
	 * 		applications and the amount of time that they were running. The
	 * 		columns returned are static and will follow this format:</td></tr>
	 * 		<tr><td></td><td>_id</td><td>Integer</td><td>A unique identifier
	 * 			for this event.</td></tr>
	 * 		<tr><td></td><td>app_and_activity</td><td>String</td><td>A String
	 * 			that is the concatenation of the Application's name the
	 * 			Activity that it ran.</td></tr>
	 * 		<tr><td></td><td>length_in_nanos</td><td>Long</td><td>The number
	 * 			of nanometers that this Activity ran.</td></tr>
	 * 
	 * 	<tr bgcolor="#CCFFCC"><td>network</td><td colspan=3>A list of the
	 * 		network usage for each Application. The format of the returned
	 * 		Cursor is based on the provided columns. The available columns 
	 * 		are:</td></tr>
	 * 		<tr><td></td><td>_id</td><td>Integer</td><td>A unique identifier
	 * 			for this record.</td></tr>
	 * 		<tr><td></td><td>app</td><td>String</td><td>The Application that
	 * 			the record pertains to.</td></tr>
	 * 		<tr><td></td><td>sent</td><td>Long</td><td>The number of units
	 * 			that was sent from this interface. See "type" and 
	 * 			"interface".</td></tr>
	 * 		<tr><td></td><td>received</td><td>Long</td><td>The number of units
	 * 			that was received on this interface. See "type" and 
	 * 			"interface".</td></tr>
	 * 		<tr><td></td><td>type</td><td>Integer</td><td>0 = Bytes, 
	 * 			1 = Packets</td></tr>
	 * 		<tr><td></td><td>interface</td><td>String</td><td>A String 
	 * 			representing the name of the interface through which this
	 * 			information was transmitted.</td></tr></table>
	 * 
	 * @param uri The URI describing which type of information is being
	 * 			  requested.
	 * 
	 * @param columns A String array of all columns being requested from the
	 * 				  applicable table denoted by the URI.
	 * 
	 * @param selection A SQLite WHERE clause without the "WHERE" keyword.
	 * 
	 * @param selectionArgs The Strings that should replace the "?"s in the
	 * 						'selection' parameter.
	 * 
	 * @param sortOrder A SQLite "ORDER BY" keyword without the "ORDER BY"
	 * 					keyword.
	 * 
	 * @return Returns a Cursor generated by the SQLite query based on these
	 * 		   parameters. If an error occurs, null is returned.
	 * 
	 * @see #AUTHORITY
	 */
	@Override
	public Cursor query(Uri uri, String[] columns, String selection, String[] selectionArgs, String sortOrder) {
		Log.i(TAG, (new StringBuilder()).append("Query: ").append(uri.toString()).toString());
		
		switch(mUriMatcher.match(uri)) {
			
			case URI_CODE_AGGREGATES: {
				return dbHelper.getMobilityAggregatesCursor(columns, selection, selectionArgs, sortOrder);
			} case URI_CODE_MOBILITY: {
				Log.i(TAG, "Querying mobility.");
				return dbHelper.getMobilityCursor(columns, selection, selectionArgs, sortOrder);
			}
		}
		
		return null;
	}

	/**
	 * I have no idea what this means yet, so I will ignore it for now.
	 */
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	// This is called when the class is created.
	static
	{
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(MobilityInterface.AUTHORITY, MobilityInterface.PATH_MOBILITY, URI_CODE_MOBILITY);
		mUriMatcher.addURI(MobilityInterface.AUTHORITY, MobilityInterface.PATH_AGGREGATES, URI_CODE_AGGREGATES);
		mUriMatcher.addURI(MobilityInterface.AUTHORITY, MobilityInterface.PATH_AGGREGATES_ADD, URI_CODE_AGGREGATES_ADD);
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		int numValues;

		dbHelper.getWritableDatabase().beginTransaction();
		try {
			numValues = super.bulkInsert(uri, values);
			dbHelper.getWritableDatabase().setTransactionSuccessful();
		} finally {
			dbHelper.getWritableDatabase().endTransaction();
		}

		getContext().getContentResolver().notifyChange(uri, null);

		return numValues;
	}

	@Override
	public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		dbHelper.getWritableDatabase().beginTransaction();
		ContentProviderResult[] results = null;
		try {
			results  = super.applyBatch(operations);
			dbHelper.getWritableDatabase().setTransactionSuccessful();
		} finally {
			dbHelper.getWritableDatabase().endTransaction();
		}
		return results;
	}
}
