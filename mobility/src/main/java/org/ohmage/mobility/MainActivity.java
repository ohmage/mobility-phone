/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ohmage.mobility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.v4.app.LoaderManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.ohmage.mobility.activity.ActivityDetectionRemover;
import org.ohmage.mobility.activity.ActivityDetectionRequester;

import java.io.IOException;
import java.util.List;

/**
 * Uses an {@link com.google.android.gms.location.ActivityRecognitionClient} to track the users
 * activities.
 * <p/>
 * An IntentService receives activity detection updates in the background so that detection can
 * continue even if the Activity is not visible.
 */
public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    // Store the current request type (ADD or REMOVE)
    private ActivityUtils.REQUEST_TYPE mRequestType;

    // Holds the ListView object in the UI
    private ListView mStatusListView;

    /*
     * Holds activity recognition data, in the form of
     * strings that can contain markup
     */
    private ArrayAdapter<Spanned> mStatusAdapter;

    // The activity recognition update request object
    private ActivityDetectionRequester mDetectionRequester;

    // The activity recognition update removal object
    private ActivityDetectionRemover mDetectionRemover;

    // Spinner to choose the rate
    private Spinner mIntervalSpinner;

    // Preferences for state
    private SharedPreferences mPrefs;

    // Interval
    private int mInterval;

    // Is the classifier running?
    private boolean mRunning;

    /*
     * Set main UI layout, get a handle to the ListView for logs, and create the broadcast
     * receiver.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the main layout
        setContentView(R.layout.activity_main);

        // Get a handle to the activity update list
        mStatusListView = (ListView) findViewById(R.id.log_listview);

        // Instantiate an adapter to store update data from the log
        mStatusAdapter = new ArrayAdapter<Spanned>(
                this,
                R.layout.item_layout,
                R.id.log_text
        );

        // Bind the adapter to the status list
        mStatusListView.setAdapter(mStatusAdapter);

        // Get detection requester and remover objects
        mDetectionRequester = new ActivityDetectionRequester(this);
        mDetectionRemover = new ActivityDetectionRemover(this);

        mPrefs = getSharedPreferences(ActivityUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get the selected interval
        mInterval = mPrefs.getInt(ActivityUtils.KEY_INTERVAL, 1);

        // Get the state of the detector
        mRunning = mPrefs.getBoolean(ActivityUtils.KEY_RUNNING, false);

        // Set up the interval spinner
        mIntervalSpinner = (Spinner) findViewById(R.id.interval);
        mIntervalSpinner.setSelection(mInterval);
        mIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mInterval != position) {
                    mInterval = position;
                    mPrefs.edit().putInt(ActivityUtils.KEY_INTERVAL, mInterval).commit();
                    if (mRunning)
                        mDetectionRequester.requestUpdates(getIntervalMillis());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Update button state
        if (mRunning) {
            mDetectionRequester.requestUpdates(getIntervalMillis());
            ((TextView) findViewById(R.id.start_button)).setText(R.string.stop_updates);
        } else {
            ((TextView) findViewById(R.id.start_button)).setText(R.string.start_updates);
        }

        getSupportLoaderManager().initLoader(0, null, this);
    }

    private long getIntervalMillis() {
        return getResources().getIntArray(R.array.interval_millis)[mInterval];
    }

    /*
     * Handle mResults returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * DetectionRemover and DetectionRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

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
                            mDetectionRequester.requestUpdates(getIntervalMillis());

                            // If the request was to remove activity recognition updates
                        } else if (ActivityUtils.REQUEST_TYPE.REMOVE == mRequestType) {

                                /*
                                 * Restart the removal of all activity recognition updates for the 
                                 * PendingIntent.
                                 */
                            mDetectionRemover.removeUpdates(
                                    mDetectionRequester.getRequestPendingIntent());

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
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS != resultCode) {

            // Display an error dialog
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0).show();
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
            mRunning = false;
        } else {
            onStartUpdates(view);
            mRunning = true;
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

        /*
         * Set the request type. If a connection error occurs, and Google Play services can
         * handle it, then onActivityResult will use the request type to retry the request
         */
        mRequestType = ActivityUtils.REQUEST_TYPE.ADD;

        // Pass the update request to the requester object
        mDetectionRequester.requestUpdates(getIntervalMillis());

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

        /*
         * Set the request type. If a connection error occurs, and Google Play services can
         * handle it, then onActivityResult will use the request type to retry the request
         */
        mRequestType = ActivityUtils.REQUEST_TYPE.REMOVE;

        // Pass the remove request to the remover object
        mDetectionRemover.removeUpdates(mDetectionRequester.getRequestPendingIntent());

        /*
         * Cancel the PendingIntent. Even if the removal request fails, canceling the PendingIntent
         * will stop the updates.
         */
        mDetectionRequester.getRequestPendingIntent().cancel();

        // Set correct text on button
        ((TextView) view).setText(R.string.start_updates);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, MobilityContentProvider.MobilityPoint.CONTENT_URI,
                new String[] {MobilityContentProvider.MobilityPoint.DATA}, null, null,
                BaseColumns._ID + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        // Clear the adapter of existing data
        mStatusAdapter.clear();

        // Add all points
        while(data.moveToNext()) {
            String activity = data.getString(0);
            int first = activity.toString().indexOf("|");
            SpannableString item = new SpannableString(activity.toString().replace("|", "\n"));
            item.setSpan(new StyleSpan(Typeface.BOLD), 0, first, 0);
            mStatusAdapter.add(item);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mStatusAdapter.clear();
    }
}
