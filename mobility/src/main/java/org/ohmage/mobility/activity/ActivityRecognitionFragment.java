package org.ohmage.mobility.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.ohmage.mobility.ActivityUtils;
import org.ohmage.mobility.MobilityContentProvider;
import org.ohmage.mobility.R;


/**
 * A fragment representing a list of Activity points.
 */
public class ActivityRecognitionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter mAdapter;

    // Store the current request type (ADD or REMOVE)
    private ActivityUtils.REQUEST_TYPE mRequestType;

    // The activity recognition update request object
    private ActivityDetectionRequester mActivityDetectionRequester;

    // The activity recognition update removal object
    private ActivityDetectionRemover mActivityDetectionRemover;

    // Spinner to choose the rate
    private Spinner mIntervalSpinner;

    // Preferences for state
    private SharedPreferences mPrefs;

    // Interval
    private int mInterval;

    // Is the classifier running?
    private boolean mRunning;

    public static ActivityRecognitionFragment newInstance() {
        ActivityRecognitionFragment fragment = new ActivityRecognitionFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ActivityRecognitionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get detection requester and remover objects
        mActivityDetectionRequester = new ActivityDetectionRequester(getActivity());
        mActivityDetectionRemover = new ActivityDetectionRemover(getActivity());

        mPrefs = getActivity().getSharedPreferences(ActivityUtils.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);

        // Get the selected interval
        mInterval = mPrefs.getInt(ActivityUtils.KEY_ACTIVITY_INTERVAL, 1);

        // Get the state of the detector
        mRunning = mPrefs.getBoolean(ActivityUtils.KEY_ACTIVITY_RUNNING, false);

        mAdapter = new ArrayAdapter<Spanned>(getActivity(), R.layout.item_layout, R.id.log_text);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_fragment, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set up the interval spinner
        mIntervalSpinner = (Spinner) view.findViewById(R.id.interval);
        mIntervalSpinner.setSelection(mInterval);
        mIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mInterval != position) {
                    mInterval = position;
                    mPrefs.edit().putInt(ActivityUtils.KEY_ACTIVITY_INTERVAL, mInterval).commit();
                    if (mRunning)
                        mActivityDetectionRequester.requestUpdates(getIntervalMillis());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button startButton = (Button) view.findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleUpdates(v);
            }
        });

        // Update button state
        if (mRunning) {
            mActivityDetectionRequester.requestUpdates(getIntervalMillis());
            startButton.setText(R.string.stop_updates);
        } else {
            startButton.setText(R.string.start_updates);
        }

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MobilityContentProvider.ActivityPoint.CONTENT_URI,
                new String[]{MobilityContentProvider.ActivityPoint.DATA}, null, null,
                BaseColumns._ID + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();

        // Clear the adapter of existing data
        mAdapter.clear();

        // Add all points
        while (data.moveToNext()) {
            String activity = data.getString(0);
            int first = activity.toString().indexOf("|");
            SpannableString item = new SpannableString(activity.toString().replace("|", "\n"));
            item.setSpan(new StyleSpan(Typeface.BOLD), 0, first, 0);
            mAdapter.add(item);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.clear();
    }

    private long getIntervalMillis() {
        return ActivityUtils.getIntervalMillis(getActivity(), mInterval);
    }

    /*
     * Handle mResults returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * DetectionRemover and DetectionRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST:

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // If the request was to start activity recognition updates
                        if (ActivityUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Restart the process of requesting activity recognition updates
                            mActivityDetectionRequester.requestUpdates(getIntervalMillis());

                            // If the request was to remove activity recognition updates
                        } else if (ActivityUtils.REQUEST_TYPE.REMOVE == mRequestType) {

                                /*
                                 * Restart the removal of all activity recognition updates for the
                                 * PendingIntent.
                                 */
                            mActivityDetectionRemover.removeUpdates(
                                    mActivityDetectionRequester.getRequestPendingIntent());

                        }
                        break;

                    // If any other result was returned by Google Play services
                    default:

                        // Report that Google Play services was unable to resolve the problem.
                        Log.d(ActivityUtils.APPTAG, getString(R.string.no_resolution));
                }

                // If any other request code was received
            default:
                // Report that this Activity received an unknown requestCode
                Log.d(ActivityUtils.APPTAG,
                        getString(R.string.unknown_activity_request_code, requestCode));

                break;
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());

        // If Google Play services is available
        if (ConnectionResult.SUCCESS != resultCode) {

            // Display an error dialog
            GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), 0).show();
            return false;
        }
        return true;
    }

    /**
     * Check to see if updates are happening or not and toggle the state
     *
     * @param view The view that triggered this method.
     */
    public void onToggleUpdates(View view) {

        if (mRunning) {
            onStopUpdates(view);
        } else {
            onStartUpdates(view);
        }
    }

    /**
     * Respond to "Start" button by requesting activity recognition
     * updates.
     *
     * @param view The view that triggered this method.
     */
    public void onStartUpdates(View view) {

        // Check for Google Play services
        if (!servicesConnected()) {
            return;
        }

        mRunning = true;

        /*
         * Set the request type. If a connection error occurs, and Google Play services can
         * handle it, then onActivityResult will use the request type to retry the request
         */
        mRequestType = ActivityUtils.REQUEST_TYPE.ADD;

        // Pass the update request to the requester object
        mActivityDetectionRequester.requestUpdates(getIntervalMillis());

        // Set correct text on button
        ((TextView) view).setText(R.string.stop_updates);
    }

    /**
     * Respond to "Stop" button by canceling updates.
     *
     * @param view The view that triggered this method.
     */
    public void onStopUpdates(View view) {

        // Check for Google Play services
        if (!servicesConnected()) {
            return;
        }

        mRunning = false;

        /*
         * Set the request type. If a connection error occurs, and Google Play services can
         * handle it, then onActivityResult will use the request type to retry the request
         */
        mRequestType = ActivityUtils.REQUEST_TYPE.REMOVE;

        // Pass the remove request to the remover object
        mActivityDetectionRemover.removeUpdates(mActivityDetectionRequester.getRequestPendingIntent());

        /*
         * Cancel the PendingIntent. Even if the removal request fails, canceling the PendingIntent
         * will stop the updates.
         */
        mActivityDetectionRequester.getRequestPendingIntent().cancel();

        // Set correct text on button
        ((TextView) view).setText(R.string.start_updates);
    }
}
