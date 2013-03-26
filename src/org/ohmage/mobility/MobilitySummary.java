
package org.ohmage.mobility;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ohmage.mobility.glue.MobilityInterface;

public class MobilitySummary extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobility_summary);
        // Show the Up button in the action bar.
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SummaryList extends ListFragment implements LoaderCallbacks<Cursor> {

        public static class ProbeListCursorAdapter extends CursorAdapter {

            public ProbeListCursorAdapter(Context context) {
                super(context, null, 0);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                ((TextView) view).setText(cursor.getString(0) + ": " + cursor.getString(1));
            }

            @Override
            public View newView(Context context, Cursor c, ViewGroup parent) {
                return new TextView(context);
            }
        }

        private static final String[] PROJECTION = new String[] {
                "date(time/1000, 'unixepoch', 'localtime') as _id", "count(*)"
        };

        private ProbeListCursorAdapter mAdapter;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mAdapter = new ProbeListCursorAdapter(getActivity());
            setListAdapter(mAdapter);

            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getActivity(), MobilityInterface.CONTENT_URI.buildUpon()
                    .appendQueryParameter("group_by", "day").build(), PROJECTION,
                    MobilityInterface.KEY_USERNAME + "=?", new String[] {
                        Utilities.getUserName(getActivity())
                    }, "time");
        }

        @Override
        public void onLoadFinished(Loader loader, Cursor data) {
            mAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader loader) {
            mAdapter.swapCursor(null);
        }
    }
}
