package org.ohmage.mobility;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public class MobilityContentProvider extends ContentProvider {
    private static final String CONTENT_AUTHORITY = "org.ohmage.mobility";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final long MAX_POINTS = 50;

    public static class MobilityPoint {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath("points")
                .build();

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.ohmage.mobility.point";

        public static final String DATA = "data";
    }

    public static class MobilityDbHelper extends SQLiteOpenHelper {

        private static final String DB_NAME = "mobility.db";

        private static final int DB_VERSION = 1;

        public interface Tables {
            static final String Points = "points";
        }

        public MobilityDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + Tables.Points + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MobilityPoint.DATA + " TEXT NOT NULL);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Tables.Points);
            onCreate(db);
        }
    }

    private MobilityDbHelper dbHelper;

    // enum of the URIs we can match using sUriMatcher
    private interface MatcherTypes {
        int POINT = 0;
    }

    private static UriMatcher sUriMatcher;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        return MobilityPoint.CONTENT_ITEM_TYPE;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long result = -1;
        String id = null;

        ContentResolver cr = getContext().getContentResolver();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case MatcherTypes.POINT:
                result = db.insert(MobilityDbHelper.Tables.Points, null, values);
                // Only keep MAX_POINTS
                db.delete(MobilityDbHelper.Tables.Points, BaseColumns._ID + "<" +
                        Math.max(result - MAX_POINTS, 0), null);
                break;
            default:
                throw new UnsupportedOperationException("insert(): Unknown URI: " + uri);
        }
        if (result != -1) {
            if (id != null) {
                uri = uri.buildUpon().appendPath(id).build();
            }
            cr.notifyChange(uri, null, false);
        }
        return uri;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new MobilityDbHelper(getContext());
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(CONTENT_AUTHORITY, "points", MatcherTypes.POINT);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case MatcherTypes.POINT:
                cursor = dbHelper.getReadableDatabase().query(MobilityDbHelper.Tables.Points,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("query(): Unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
