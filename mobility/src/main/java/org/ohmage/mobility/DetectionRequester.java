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
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Class for connecting to Location Services and activity recognition updates.
 * <b>
 * Note: Clients must ensure that Google Play services is available before requesting updates.
 * </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 * <p/>
 * <p/>
 * To use a DetectionRequester, instantiate it and call requestUpdates(). Everything else is done
 * automatically.
 */
public abstract class DetectionRequester<T extends GooglePlayServicesClient>
        implements ConnectionCallbacks, OnConnectionFailedListener {

    // Storage for a context from the calling client
    private Context mContext;

    // Stores the PendingIntent used to send activity recognition events back to the app
    private PendingIntent mRequestPendingIntent;

    // Stores the current instantiation of the activity recognition client
    private T mGooglePlayServicesClient;

    public DetectionRequester(Context context) {
        // Save the context
        mContext = context;

        // Initialize the globals to null
        mRequestPendingIntent = null;
        mGooglePlayServicesClient = null;
    }

    /**
     * Returns the current PendingIntent to the caller.
     *
     * @return The PendingIntent used to request activity recognition updates
     */
    public PendingIntent getRequestPendingIntent() {
        return mRequestPendingIntent;
    }

    /**
     * Sets the PendingIntent used to make activity recognition update requests
     *
     * @param intent The PendingIntent
     */
    public void setRequestPendingIntent(PendingIntent intent) {
        mRequestPendingIntent = intent;
    }

    /**
     * Allows callers to cancel this pending intent which will force the request to stop.
     */
    public void cancelPendingIntent() {
        if (mRequestPendingIntent != null) {
            mRequestPendingIntent.cancel();
            mRequestPendingIntent = null;
        }
    }

    /**
     * Make the actual update request. This is called from onConnected().
     */
    private void continueRequestUpdates() {
        /*
         * Request updates, using the given detection interval.
         * The PendingIntent sends updates to ActivityRecognitionIntentService
         */
        requestUpdatesFromClient(mContext, getGooglePlayServicesClient(), createRequestPendingIntent());

        // Disconnect the client
        requestDisconnection();
    }

    /**
     * Extending classes should implement this method and remove the request for updates
     */
    protected abstract void requestUpdatesFromClient(Context context, T client, PendingIntent intent);

    /**
     * Extending classes should implement this method and create the play services client they need
     */
    protected abstract T createGooglePlayServicesClient(Context context);

    /**
     * Extending classes should implement this method and return an intent to the IntentService
     * they want to receive updates with.
     *
     * @param context
     * @return
     */
    protected abstract Intent getIntentService(Context context);

    /**
     * Request a connection to Location Services. This call returns immediately,
     * but the request is not complete until onConnected() or onConnectionFailure() is called.
     */
    protected void requestConnection() {
        getGooglePlayServicesClient().connect();
    }

    /**
     * Get the current activity recognition client, or create a new one if necessary.
     * This method facilitates multiple requests for a client, even if a previous
     * request wasn't finished. Since only one client object exists while a connection
     * is underway, no memory leaks occur.
     *
     * @return An ActivityRecognitionClient object
     */
    private T getGooglePlayServicesClient() {
        if (mGooglePlayServicesClient == null) {
            mGooglePlayServicesClient = createGooglePlayServicesClient(mContext);
        }
        return mGooglePlayServicesClient;
    }

    /**
     * Get the current activity recognition client and disconnect from Location Services
     */
    private void requestDisconnection() {
        getGooglePlayServicesClient().disconnect();
    }

    /*
     * Called by Location Services once the activity recognition client is connected.
     *
     * Continue by requesting activity updates.
     */
    @Override
    public void onConnected(Bundle arg0) {
        // If debugging, log the connection
        Log.d(ActivityUtils.APPTAG, mContext.getString(R.string.connected));

        // Continue the process of requesting activity recognition updates
        continueRequestUpdates();
    }

    /*
     * Called by Location Services once the activity recognition client is disconnected.
     */
    @Override
    public void onDisconnected() {
        // In debug mode, log the disconnection
        Log.d(ActivityUtils.APPTAG, mContext.getString(R.string.disconnected));

        // Destroy the current activity recognition client
        mGooglePlayServicesClient = null;
    }

    /**
     * Get a PendingIntent to send with the request to get activity recognition updates. Location
     * Services issues the Intent inside this PendingIntent whenever a activity recognition update
     * occurs.
     *
     * @return A PendingIntent for the IntentService that handles activity recognition updates.
     */
    private PendingIntent createRequestPendingIntent() {

        // If the PendingIntent already exists
        if (null != getRequestPendingIntent()) {

            // Return the existing intent
            return mRequestPendingIntent;

            // If no PendingIntent exists
        } else {
            // Create an Intent pointing to the IntentService
            Intent intent = getIntentService(mContext);

            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
            PendingIntent pendingIntent = PendingIntent.getService(mContext, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            setRequestPendingIntent(pendingIntent);
            return pendingIntent;
        }

    }

    /*
     * Implementation of OnConnectionFailedListener.onConnectionFailed
     * If a connection or disconnection request fails, report the error
     * connectionResult is passed in from Location Services
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {

            try {
                connectionResult.startResolutionForResult((Activity) mContext,
                        ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

            /*
             * Thrown if Google Play services canceled the original
             * PendingIntent
             */
            } catch (SendIntentException e) {
                // display an error or log it here.
            }

        /*
         * If no resolution is available, display Google
         * Play service error dialog. This may direct the
         * user to Google Play Store if Google Play services
         * is out of date.
         */
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
                    connectionResult.getErrorCode(),
                    (Activity) mContext,
                    ActivityUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            if (dialog != null) {
                dialog.show();
            }
        }
    }
}
