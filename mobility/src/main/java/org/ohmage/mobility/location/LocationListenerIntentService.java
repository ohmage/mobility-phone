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

package org.ohmage.mobility.location;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationClient;

import edu.cornell.tech.smalldata.omhclientlib.schema.LocationSchema;
import edu.cornell.tech.smalldata.omhclientlib.services.OmhDsuWriter;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.mobility.ActivityUtils;
import org.ohmage.mobility.MobilityContentProvider;
import org.ohmage.streams.StreamPointBuilder;

/**
 * Service that receives location updates. It receives updates
 * in the background, even if the main Activity is not visible.
 */
public class LocationListenerIntentService extends IntentService {

    private StreamPointBuilder mPointBuilder = new StreamPointBuilder();

    public LocationListenerIntentService() {
        // Set the label for the service's background thread
        super("LocationIntentService");
    }

    /**
     * Called when a new location update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Location result = intent.getParcelableExtra(LocationClient.KEY_LOCATION_CHANGED);

        // If the intent contains an update
        if (result != null) {

            // Write the result to the stream
            writeResultStream(result);

            // Write the result to the DSU
            writeResultToDsu(result);

            // Log the update
            logLocationResult(result);
        }
    }

    private void writeResultToDsu(Location result) {

        if (result != null){

            double latitude = result.getLatitude();
            double longitude = result.getLongitude();
            double accuracy = result.getAccuracy();
            double altitude = result.getAltitude();
            double bearing = result.getBearing();
            double speed = result.getSpeed();

            LocationSchema locationSchema = new LocationSchema(latitude, longitude, accuracy, altitude, bearing, speed);

            OmhDsuWriter.writeDataPoint(getApplicationContext(), locationSchema);

        }

    }

    /**
     * Write the location update to the ohmage stream
     *
     * @param result The result extracted from the incoming Intent
     */
    private void writeResultStream(Location result) {

        mPointBuilder.clear().setStream(ActivityUtils.LOCATION_STREAM_ID,
                ActivityUtils.LOCATION_STREAM_VERSION).now().withId();

        JSONObject json = new JSONObject();

        try {
            json.put("latitude", result.getLatitude());
            json.put("longitude", result.getLongitude());
            if (result.hasAccuracy())
                json.put("accuracy", result.getAccuracy());
            if (result.hasAltitude())
                json.put("altitude", result.getAltitude());
            if (result.hasBearing())
                json.put("bearing", result.getBearing());
            if (result.hasSpeed())
                json.put("speed", result.getSpeed());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mPointBuilder.setData(json.toString());
        mPointBuilder.write(getContentResolver());
    }

    /**
     * Write the location update to the log file
     *
     * @param result The result extracted from the incoming Intent
     */
    private void logLocationResult(Location result) {

        StringBuilder msg = new StringBuilder(DateTimeFormat.mediumDateTime()
                .print(new LocalDateTime())).append("|");

        msg.append(result.getLatitude()).append(", ").append(result.getLongitude());

        ContentValues values = new ContentValues();
        values.put(MobilityContentProvider.LocationPoint.DATA, msg.toString());
        getContentResolver().insert(MobilityContentProvider.LocationPoint.CONTENT_URI, values);
    }
}
