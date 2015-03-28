package org.ohmage.mobility.location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.ohmage.mobility.ActivityUtils;
import org.ohmage.mobility.MobilityContentProvider;
import org.ohmage.mobility.R;

import java.util.ArrayList;


/**
 * A fragment representing a list of Location points.
 */
public class LocationFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Store the current request type (ADD or REMOVE)
    private ActivityUtils.REQUEST_TYPE mRequestType;

    // The location update request object
    private LocationDetectionRequester mLocationDetectionRequester;

    // The location update removal object
    private LocationDetectionRemover mLocationDetectionRemover;

    // Spinner to choose the rate
    private Spinner mIntervalSpinner;

    // Spinner to choose the priority
    private Spinner mPrioritySpinner;

    // Preferences for state
    private SharedPreferences mPrefs;

    // Interval
    private int mInterval;

    // Location Priority (Accurate, Block, City, No Power)
    private int mPriority;

    // Is the classifier running?
    private boolean mRunning;

    private ArrayList<LatLng> mLocations = new ArrayList<LatLng>();

    public static LocationFragment newInstance() {
        LocationFragment fragment = new LocationFragment();
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public LocationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get detection requester and remover objects
        mLocationDetectionRequester = new LocationDetectionRequester(getActivity());
        mLocationDetectionRemover = new LocationDetectionRemover(getActivity());

        mPrefs = getActivity().getSharedPreferences(ActivityUtils.SHARED_PREFERENCES,
                Context.MODE_PRIVATE);

        // Get the selected interval
        mInterval = mPrefs.getInt(ActivityUtils.KEY_LOCATION_INTERVAL, 0);

        // Get the priority
        mPriority = mPrefs.getInt(ActivityUtils.KEY_LOCATION_PRIORITY, 1);

        // Get the state of the detector
        mRunning = mPrefs.getBoolean(ActivityUtils.KEY_LOCATION_RUNNING, true);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_fragment, container, false);

        ViewGroup mapContainer = (ViewGroup) view.findViewById(R.id.map_container);
        mapContainer.addView(super.onCreateView(inflater, container, savedInstanceState));

        // Set up the interval spinner
        mIntervalSpinner = (Spinner) view.findViewById(R.id.interval);
        mIntervalSpinner.setSelection(mInterval);
        mIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mInterval != position) {
                    mInterval = position;
                    mPrefs.edit().putInt(ActivityUtils.KEY_LOCATION_INTERVAL, mInterval).commit();
                    if (mRunning)
                        mLocationDetectionRequester.requestUpdates(getIntervalMillis(), getPriority());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mPrioritySpinner = (Spinner) view.findViewById(R.id.priority);
        mPrioritySpinner.setSelection(mPriority);
        mPrioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mPriority != position) {
                    mPriority = position;
                    mPrefs.edit().putInt(ActivityUtils.KEY_LOCATION_PRIORITY, mPriority).commit();
                    if (mRunning)
                        mLocationDetectionRequester.requestUpdates(getIntervalMillis(), getPriority());
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
            mLocationDetectionRequester.requestUpdates(getIntervalMillis(), getPriority());
            startButton.setText(R.string.stop_updates);
        } else {
            startButton.setText(R.string.start_updates);
        }

        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), MobilityContentProvider.LocationPoint.CONTENT_URI,
                new String[]{MobilityContentProvider.ActivityPoint.DATA}, null, null,
                BaseColumns._ID + " desc");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final GoogleMap map = getMap();

        boolean moveMap = mLocations.isEmpty();

        // Clear the map of existing data
        mLocations.clear();
        map.clear();

        data.moveToPosition(-1);

        // Add all points
        while (data.moveToNext()) {
            String[] latlng = data.getString(0).split("\\|")[1].split(",");
            mLocations.add(new LatLng(Double.valueOf(latlng[0]), Double.valueOf(latlng[1])));
        }

        map.addPolyline(new PolylineOptions().geodesic(true).addAll(mLocations));

        // Move the map if needed
        if (moveMap && !mLocations.isEmpty()) {

            map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    LatLngBounds.Builder bounds = LatLngBounds.builder();
                    for (LatLng point : mLocations) {
                        bounds.include(point);
                    }
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),
                            getResources().getDimensionPixelSize(R.dimen.gutter)));
                    if (map.getCameraPosition().zoom > 19) {
                        map.moveCamera(CameraUpdateFactory.zoomTo(19));
                    }
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        GoogleMap map = getMap();
        if (map != null) {
            map.clear();
        }
    }

    private long getIntervalMillis() {
        return ActivityUtils.getIntervalMillis(getActivity(), mInterval);
    }

    private int getPriority() {
        return ActivityUtils.getPriority(mPriority);
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

                        // If the request was to start location updates
                        if (ActivityUtils.REQUEST_TYPE.ADD == mRequestType) {

                            // Restart the process of requesting location updates
                            mLocationDetectionRequester.requestUpdates(getIntervalMillis(),
                                    getPriority());

                            // If the request was to remove location updates
                        } else if (ActivityUtils.REQUEST_TYPE.REMOVE == mRequestType) {

                                /*
                                 * Restart the removal of all location updates for the
                                 * PendingIntent.
                                 */
                            mLocationDetectionRemover.removeUpdates(
                                    mLocationDetectionRequester.getRequestPendingIntent());

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
     * Respond to "Start" button by requesting location
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
        mLocationDetectionRequester.requestUpdates(getIntervalMillis(), getPriority());

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
        mLocationDetectionRemover.removeUpdates(mLocationDetectionRequester.getRequestPendingIntent());

        /*
         * Cancel the PendingIntent. Even if the removal request fails, canceling the PendingIntent
         * will stop the updates.
         */
        mLocationDetectionRequester.cancelPendingIntent();

        // Set correct text on button
        ((TextView) view).setText(R.string.start_updates);
    }
}
