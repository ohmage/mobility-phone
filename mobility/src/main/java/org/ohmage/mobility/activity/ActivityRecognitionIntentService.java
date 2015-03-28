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

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ohmage.mobility.MobilityContentProvider;
import org.ohmage.mobility.R;

import io.smalldatalab.omhclient.DSUDataPoint;
import io.smalldatalab.omhclient.DSUDataPointBuilder;


/**
 * Service that receives ActivityRecognition updates. It receives updates
 * in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {


    public ActivityRecognitionIntentService() {
        // Set the label for the service's background thread
        super("ActivityRecognitionIntentService");
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // If the intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {

            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Write the result to the DSU
            writeResultToDsu(result);

            // Log the update
            logActivityRecognitionResult(result);
        }
    }

    private void writeResultToDsu(ActivityRecognitionResult result) {

        if (result != null) {
            try {
                JSONArray json = new JSONArray();

                // Get all the probable activities from the updated result
                for (DetectedActivity detectedActivity : result.getProbableActivities()) {

                    // Get the activity type, confidence level, and human-readable name
                    int activityType = detectedActivity.getType();
                    int confidence = detectedActivity.getConfidence();
                    String activityName = getNameFromType(activityType);

                    JSONObject activity = new JSONObject();

                    activity.put("activity", activityName);
                    activity.put("confidence", confidence);
                    json.put(activity);

                }
                JSONObject body = new JSONObject();
                body.put("activities", json);
                DSUDataPoint datapoint = new DSUDataPointBuilder()
                        .setSchemaNamespace(getString(R.string.schema_namespace))
                        .setSchemaName(getString(R.string.activity_schema_name))
                        .setSchemaVersion(getString(R.string.schema_version))
                        .setAcquisitionModality(getString(R.string.acquisition_modality))
                        .setAcquisitionSource(getString(R.string.acquisition_source_name))
                        .setCreationDateTime(new DateTime(result.getTime()))
                        .setBody(body).createDSUDataPoint();
                datapoint.save();
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Create datapoint failed", e);
            }

        }

    }


    /**
     * Write the activity recognition update to the log file
     *
     * @param result The result extracted from the incoming Intent
     */
    private void logActivityRecognitionResult(ActivityRecognitionResult result) {

        StringBuilder msg = new StringBuilder(DateTimeFormat.mediumDateTime()
                .print(new LocalDateTime()));

        // Get all the probably activities from the updated result
        for (DetectedActivity detectedActivity : result.getProbableActivities()) {

            // Get the activity type, confidence level, and human-readable name
            int activityType = detectedActivity.getType();
            int confidence = detectedActivity.getConfidence();
            String activityName = getNameFromType(activityType);
            msg.append("|").append(activityName).append(": ").append(confidence);
        }

        ContentValues values = new ContentValues();
        values.put(MobilityContentProvider.ActivityPoint.DATA, msg.toString());
        getContentResolver().insert(MobilityContentProvider.ActivityPoint.CONTENT_URI, values);
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.RUNNING:
                return "running";
        }
        return "unknown";
    }
}
