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

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import edu.cornell.tech.smalldata.omhclientlib.schema.MobilitySchema;
import edu.cornell.tech.smalldata.omhclientlib.schema.ProbableActivitySchema;
import edu.cornell.tech.smalldata.omhclientlib.services.OmhDsuWriter;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.mobility.ActivityUtils;
import org.ohmage.mobility.MobilityContentProvider;
import org.ohmage.streams.StreamPointBuilder;

import java.util.Date;
import java.util.TimeZone;

/**
 * Service that receives ActivityRecognition updates. It receives updates
 * in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {

    private StreamPointBuilder mPointBuilder = new StreamPointBuilder();

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

            // Write the result to the stream
            writeResultStream(result);

            // Write the result to the DSU
            writeResultToDsu(result);

            // Log the update
            logActivityRecognitionResult(result);
        }
    }

    private void writeResultToDsu(ActivityRecognitionResult result) {

        if (result != null) {

            ProbableActivitySchema[] probableActivitySchemas = new ProbableActivitySchema[result.getProbableActivities().size()];

            int i = 0;
            for (DetectedActivity detectedActivity : result.getProbableActivities()) {

                // Get the activity type, confidence level, and human-readable name
                int activityType = detectedActivity.getType();
                int confidence = detectedActivity.getConfidence();
                String activityName = getNameFromType(activityType);

                probableActivitySchemas[i++] = new ProbableActivitySchema(activityName, confidence);
            }

            MobilitySchema mobilitySchema = new MobilitySchema(probableActivitySchemas);

            OmhDsuWriter.writeDataPoint(getApplicationContext(), mobilitySchema);
        }

    }

    /**
     * Write the activity recognition update to the ohmage stream
     *
     * @param result The result extracted from the incoming Intent
     */
    private void writeResultStream(ActivityRecognitionResult result) {

        mPointBuilder.clear().setStream(ActivityUtils.ACTIVITY_STREAM_ID,
                ActivityUtils.ACTIVITY_STREAM_VERSION).now().withId();

        JSONArray json = new JSONArray();

        // Get all the probable activities from the updated result
        for (DetectedActivity detectedActivity : result.getProbableActivities()) {

            // Get the activity type, confidence level, and human-readable name
            int activityType = detectedActivity.getType();
            int confidence = detectedActivity.getConfidence();
            String activityName = getNameFromType(activityType);

            JSONObject activity = new JSONObject();
            try {
                activity.put("activity", activityName);
                activity.put("confidence", confidence);
                json.put(activity);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        mPointBuilder.setData(json.toString());
        mPointBuilder.write(getContentResolver());
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
