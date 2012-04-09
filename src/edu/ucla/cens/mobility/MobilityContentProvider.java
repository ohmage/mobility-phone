package edu.ucla.cens.mobility;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

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
	
	/**
	 * The Authority that should be used whenever you are querying this
	 * Content Provider. 
	 * 
	 * <p>Value: <code>{@value AUTHORITY}</code></p>
	 * @
	 */
	public static final String AUTHORITY = "edu.ucla.cens.mobility.MobilityContentProvider";
	
	/**
	 * A path value that will give information about the applications that
	 * have been running.
	 * 
	 * <p>Value: <code>{@value PATH_MOBILITY}</code></p>
	 * 
	 * @see #query(Uri, String[], String, String[], String)
	 */
	public static final String PATH_MOBILITY = "mobility";
	
	private static final int URI_CODE_MOBILITY = 1;
	
	private static final UriMatcher mUriMatcher;
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY + "/" + PATH_MOBILITY);
	/**
	 * It doesn't make any sense for a user to be adding data to this
	 * ContentProvider. Therefore, nothing happens when this is called.
	 * 
	 * @param uri Unused
	 * 
	 * @param values Unused
	 * 
	 * @return Always returns null.
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		return null;
	}
	
	/**
	 * Does nothing
	 */
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs)  throws NullPointerException
	{
		return 0;
	}
	
	/**
	 * There is no reason an external class should be deleting information
	 * from the database, so this is unused.
	 * 
	 * @param uri Unused.
	 * 
	 * @param where Unused.
	 * 
	 * @param whereArgs Unused.
	 * 
	 * @return Always returns 0.
	 */
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) 
	{
		return 0;
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
	public Cursor query(Uri uri, String[] columns, String selection, String[] selectionArgs, String sortOrder) 
	{
		Log.i(TAG, (new StringBuilder()).append("Query: ").append(uri.toString()).toString());
		
		if(mUriMatcher.match(uri) == URI_CODE_MOBILITY)
		{
			Log.i(TAG, "Querying mobility.");
			MobilityDbAdapter mda = new MobilityDbAdapter(getContext());
			
			return mda.getMobilityCursor(columns, selection, selectionArgs, sortOrder);
		}
		
		return null;
	}
//	@Override
//	protected void finalize() throws Throwable
//	{
//		mda.close();
//		super.finalize();
//		
//	}

	/**
	 * I have no idea what this means yet, so I will ignore it for now.
	 */
	@Override
	public String getType(Uri uri) 
	{
		// TODO Auto-generated method stub
		return null;
	}
	
//	/**
//	 * Creates a new Cursor object with the Application and Activities tied
//	 * together and has the amount of time that that combination ran in 
//	 * nanoseconds. There will be duplicates of these Application and Activity
//	 * names because that Activity may start and stop multiple times.
//	 * 
//	 * The Cursor format is:
//	 * 		"_id", "app_and_activity", "length_in_nanos"
//	 * 
//	 * @return A Cursor with the Application and Activity names tied together
//	 * 		   and their running time in nanoseconds.
//	 */
//	private Cursor getEventTimes()
//	{
//		String[] columns = { LogDbAdapter.KEY_EVENTS_ID, LogDbAdapter.KEY_EVENTS_TIME, LogDbAdapter.KEY_EVENTS_APPLICATION, LogDbAdapter.KEY_EVENTS_ACTIVITY, LogDbAdapter.KEY_EVENTS_ACTION };
//		Cursor c = LogDbAdapter.getEventEntries(columns, null, null, LogDbAdapter.KEY_EVENTS_TIME + " ASC");
//		
//		if(c == null)
//			return null;
//		c.moveToFirst();
//		int cLen = c.getCount();
//		
//		String[] newColumns = { "_id", "app_and_activity", "length_in_nanos" };
//		MatrixCursor result = new MatrixCursor(newColumns);
//		
//		int currId = 1;
//		
//		Long lastTime;
//		String currAppAndActivity;
//		int currAction;
//		HashMap<String, Long> appRunning = new HashMap<String, Long>();
//		Object[] newVals = new Object[3];
//		
//		for(int i = 0; i < cLen; i++)
//		{
//			currAppAndActivity = c.getString(2) + c.getString(3);
//			currAction = c.getInt(4);
//			
//			if((currAction == LogDbAdapter.ACTION_TYPES.CREATE.ordinal()) || 
//			   (currAction == LogDbAdapter.ACTION_TYPES.RESTART.ordinal()) ||
//			   (currAction == LogDbAdapter.ACTION_TYPES.RESUME.ordinal()))
//			{
//				appRunning.put(currAppAndActivity, c.getLong(1));
//			}
//			else if(currAction == LogDbAdapter.ACTION_TYPES.PAUSE.ordinal())
//			{
//				if(appRunning.containsKey(currAppAndActivity))
//				{
//					lastTime = appRunning.get(currAppAndActivity);
//					
//					if(lastTime > 0)
//					{
//						appRunning.put(currAppAndActivity, -lastTime);
//						
//						newVals[0] = currId;
//						newVals[1] = currAppAndActivity;
//						newVals[2] = c.getLong(1) - lastTime;
//						
//						try
//						{
//							result.addRow(newVals);
//							currId++;
//						}
//						catch(IllegalArgumentException e)
//						{
//							Log.e(TAG, "The number of arguments for the new row doesn't exactly match the number of rows that are available.");
//						}
//					}
//				}
//			}
//		}
//
//		return result;
//	}
	
	// This is called when the class is created.
	static
	{
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AUTHORITY, PATH_MOBILITY, URI_CODE_MOBILITY);
	}

	@Override
	public boolean onCreate()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
	
}
