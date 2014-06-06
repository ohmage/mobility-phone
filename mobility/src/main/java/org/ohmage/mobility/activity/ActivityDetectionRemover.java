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

package org.ohmage.mobility.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;

import org.ohmage.mobility.ActivityUtils;
import org.ohmage.mobility.DetectionRemover;
import org.ohmage.mobility.R;

/**
 * Class for connecting to Location Services and removing activity recognition updates.
 * <b>
 * Note: Clients must ensure that Google Play services is available before removing activity
 * recognition updates.
 * </b> Use GooglePlayServicesUtil.isGooglePlayServicesAvailable() to check.
 * <p/>
 * <p/>
 * To use a DetectionRemover, instantiate it, then call removeUpdates().
 */
public class ActivityDetectionRemover extends DetectionRemover<ActivityRecognitionClient> {

    public ActivityDetectionRemover(Context context) {
        super(context);
    }

    @Override
    protected void removeUpdatesFromClient(Context context, ActivityRecognitionClient client, PendingIntent intent) {
        client.removeActivityUpdates(intent);

        // Save request state
        context.getSharedPreferences(ActivityUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit()
                .putBoolean(ActivityUtils.KEY_ACTIVITY_RUNNING, false).commit();
    }

    @Override
    protected ActivityRecognitionClient createGooglePlayServicesClient(Context context) {
        return new ActivityRecognitionClient(context, this, this);
    }
}
