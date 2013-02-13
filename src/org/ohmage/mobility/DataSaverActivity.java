
package org.ohmage.mobility;

import android.app.Activity;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.logprobe.Log;
import org.ohmage.logprobe.LogProbe;
import org.ohmage.mobility.MobilityDbAdapter.DBRow;
import org.ohmage.mobility.glue.MobilityInterface;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class DataSaverActivity extends Activity {
    private static final String TAG = "DataSaverActivity";

    /** Called when the activity is first created. */
    Button goButton;
    TextView text;

    private LogProbe logger;

    // Thread thread;
    @Override
    public void onResume(/* Bundle savedInstanceState */) {
        super.onResume(/* savedInstanceState */);

        setContentView(R.layout.datasaver);
        goButton = (Button) findViewById(R.id.button1);
        text = (TextView) findViewById(R.id.text1);

        text.setText("Press the button");
        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("Thanks! Now please wait until this message changes (it could take up to 2 minutes)");
                // thread = new Thread(new Rescue("Mobility_" +
                // System.currentTimeMillis()));
                // thread.start();
                String result = "...an error occured";
                new Rescue().execute("Mobility_" + System.currentTimeMillis(), null, result);
                // rescueData("Mobility_" + System.currentTimeMillis());
                // text.append(result);

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    Object rescueLock = new Object();

    private class Rescue extends AsyncTask<String, Void, String>// implements
                                                                // Runnable
    {
        String file;

        // public Rescue(String path)
        // {
        // file = path;
        // }
        public int rescueData(String filename) throws IOException {
            Log.v(TAG, "Starting rescueData");
            // Cursor c =
            // MobilityInterface.getMobilityCursor(DataSaverActivity.this, 0L);
            MobilityDbAdapter mdb = new MobilityDbAdapter(DataSaverActivity.this);
            Log.v(TAG, "Got cursor");
            int skipped = 0;
            HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
            int ki = 0;
            for (String s : new String[] {
                    MobilityInterface.KEY_ROWID, MobilityInterface.KEY_MODE,
                    MobilityInterface.KEY_SPEED, MobilityInterface.KEY_STATUS,
                    MobilityInterface.KEY_LOC_TIMESTAMP, MobilityInterface.KEY_ACCURACY,
                    MobilityInterface.KEY_PROVIDER, MobilityInterface.KEY_WIFIDATA,
                    MobilityInterface.KEY_ACCELDATA, MobilityInterface.KEY_TIME,
                    MobilityInterface.KEY_TIMEZONE, MobilityInterface.KEY_LATITUDE,
                    MobilityInterface.KEY_LONGITUDE
            }) {
                indexMap.put(s, ki++);
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            // Log.d(TAG, "It is " + (c==null) +" that c is null");
            // Log.d(TAG, c.getCount() + " lines to save!");
            // if (c != null)// && c.getCount() > 0) {
            boolean full = true;
            int index = 0;
            long last = -1;
            while (full) {
                // Log.i(TAG, "There are " + String.valueOf(c.getCount()) +
                // " mobility points to save.");
                // c.moveToFirst();
                Integer max = 300;
                ArrayList<DBRow> mpoints = mdb.fetchSomeRows(max, last);
                full = mpoints.size() == 300; // must match limit number in
                                              // transportmodedb.fetchSomeTransportModes's
                                              // query
                // Log.d(TAG,
                // String.format("I done fetched all the transport modes in only %d milliseconds",
                // System.currentTimeMillis() - start));
                // int remainingCount = 28800;//c.getCount();//c.getcount fails
                // int limit = 300;

                // while (remainingCount > 0)
                {

                    // if (remainingCount < limit) {
                    // limit = remainingCount;
                    // Log.e(TAG, "Remaining: " + remainingCount);
                    // }
                    BufferedWriter outWrite = new BufferedWriter(new FileWriter(
                            Environment.getExternalStorageDirectory() + "/" + filename + "_"
                                    + index++ + ".txt"));
                    // Log.i(TAG, "Attempting to write a batch with " +
                    // String.valueOf(mpoints.size()) + " mobility points.");

                    for (int i = 0; i < mpoints.size(); i++) {
                        DBRow row = mpoints.get(i);
                        JSONObject mobilityPointJson = new JSONObject();
                        try {
                            last = row.rowValue;
                            String time = row.timeValue;
                            mobilityPointJson.put("date", dateFormat.format(new Date(time)));
                            mobilityPointJson.put("time", time);
                            mobilityPointJson.put("timezone", row.timezoneValue);// c.getString(c.getColumnIndex(MobilityInterface.KEY_TIMEZONE)));
                            mobilityPointJson.put("subtype", "sensor_data");
                            JSONObject dataJson = new JSONObject();
                            dataJson.put("mode", row.modeValue);// c.getString(c.getColumnIndex(MobilityInterface.KEY_MODE)));
                            // Log.d(TAG,
                            // Float.parseFloat(c.getString(c.getColumnIndex(MobilityInterface.KEY_SPEED)))
                            // + " is the speed");
                            if (!Float.isNaN(Float.parseFloat((row.speedValue))))
                                dataJson.put("speed", Float.parseFloat(row.speedValue));// c.getString(c.getColumnIndex(MobilityInterface.KEY_SPEED))));
                            else
                                dataJson.put("speed", null);
                            dataJson.put("accel_data", new JSONArray(row.accelDataValue));// c.getString(c.getColumnIndex(MobilityInterface.KEY_ACCELDATA))));
                            dataJson.put("wifi_data", new JSONObject(row.wifiDataValue));// c.getString(c.getColumnIndex(MobilityInterface.KEY_WIFIDATA))));
                            mobilityPointJson.put("data", dataJson);
                            String locationStatus = row.statusValue;// c.getString(c.getColumnIndex(MobilityInterface.KEY_STATUS));
                            mobilityPointJson.put("location_status", locationStatus);
                            if (!locationStatus.equals("unavailable")) {
                                JSONObject locationJson = new JSONObject();
                                if (!Double.isNaN(Double.parseDouble(row.latitudeValue)))// c.getString(c.getColumnIndex(MobilityInterface.KEY_LATITUDE)))))
                                    locationJson.put("latitude",
                                            Double.parseDouble(row.latitudeValue));// c.getString(c.getColumnIndex(MobilityInterface.KEY_LATITUDE))));
                                else
                                    locationJson.put("latitude", null);

                                if (!Double.isNaN(Double.parseDouble(row.longitudeValue)))// c.getString(c.getColumnIndex(MobilityInterface.KEY_LONGITUDE)))))
                                    locationJson.put("longitude",
                                            Double.parseDouble(row.longitudeValue));// c.getString(c.getColumnIndex(MobilityInterface.KEY_LONGITUDE))));
                                else
                                    locationJson.put("longitude", null);
                                locationJson.put("provider", row.providerValue);// c.getString(c.getColumnIndex(MobilityInterface.KEY_PROVIDER)));
                                locationJson.put("accuracy", Float.parseFloat(row.accuracyValue));// c.getString(c.getColumnIndex(MobilityInterface.KEY_ACCURACY))));
                                locationJson.put("timestamp", dateFormat.format(new Date(Long
                                        .parseLong(row.locTimeValue))));// c.getString(c.getColumnIndex(MobilityInterface.KEY_LOC_TIMESTAMP))))));
                                mobilityPointJson.put("location", locationJson);
                            }

                        } catch (CursorIndexOutOfBoundsException ce) {
                            outWrite.close();
                            Log.v(TAG, "OK, done!");
                            // c.close();
                            return skipped;
                        } catch (Exception e) {
                            skipped++;
                            // throw new RuntimeException(e);
                            Log.e(TAG, "Unknown error", e);
                        }

                        outWrite.write(mobilityPointJson.toString());

                        // c.moveToNext();
                    }
                    // remainingCount -= limit;
                    outWrite.close();
                    // last = mpoints.get(mpoints.size() - 1).rowValue;
                }

                // c.close();
            }
            Log.v(TAG, "OK, done!");
            // else {
            // Log.i(TAG, "No mobility points to write.");
            // }
            return skipped;
        }

        // public String run() // use lock
        // {
        //
        // }
        @Override
        protected String doInBackground(String... params) {
            synchronized (rescueLock) {
                try {
                    int skipped = rescueData(params[0]);
                    if (skipped > 0)
                        return "...finished! Data stored on sd card. " + skipped
                                + " records were skipped.";
                    else
                        return "...finished! Data stored on sd card.";
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "IO Exception", e);
                    return "...failed! There was a writing error";
                }
            }

        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            text.append(result);
        }

    }

    public void rescueData(String filename) throws IOException {
        Cursor c = MobilityInterface.getMobilityCursor(this, 0L);
        if (c != null && c.getCount() > 0) {

            Log.i(TAG, "There are " + String.valueOf(c.getCount()) + " mobility points to upload.");

            c.moveToFirst();

            int remainingCount = c.getCount();
            int limit = 300;
            int index = 0;
            while (remainingCount > 0) {

                if (remainingCount < limit) {
                    limit = remainingCount;
                }
                BufferedWriter outWrite = new BufferedWriter(new FileWriter(
                        Environment.getExternalStorageDirectory() + "/" + filename + "_" + index++
                                + ".txt"));
                Log.i(TAG, "Attempting to write a batch with " + String.valueOf(limit)
                        + " mobility points.");

                for (int i = 0; i < limit; i++) {
                    JSONObject mobilityPointJson = new JSONObject();
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Long time = c.getLong(c.getColumnIndex(MobilityInterface.KEY_TIME));
                        mobilityPointJson.put("date", dateFormat.format(new Date(time)));
                        mobilityPointJson.put("time", time);
                        mobilityPointJson.put("timezone",
                                c.getString(c.getColumnIndex(MobilityInterface.KEY_TIMEZONE)));
                        mobilityPointJson.put("subtype", "sensor_data");
                        JSONObject dataJson = new JSONObject();
                        dataJson.put("mode",
                                c.getString(c.getColumnIndex(MobilityInterface.KEY_MODE)));
                        Log.i(TAG,
                                Float.parseFloat(c.getString(c
                                        .getColumnIndex(MobilityInterface.KEY_SPEED)))
                                        + " is the speed");
                        if (!Float.isNaN(Float.parseFloat(c.getString(c
                                .getColumnIndex(MobilityInterface.KEY_SPEED)))))
                            dataJson.put("speed", Float.parseFloat(c.getString(c
                                    .getColumnIndex(MobilityInterface.KEY_SPEED))));
                        else
                            dataJson.put("speed", null);
                        dataJson.put(
                                "accel_data",
                                new JSONArray(c.getString(c
                                        .getColumnIndex(MobilityInterface.KEY_ACCELDATA))));
                        dataJson.put(
                                "wifi_data",
                                new JSONObject(c.getString(c
                                        .getColumnIndex(MobilityInterface.KEY_WIFIDATA))));
                        mobilityPointJson.put("data", dataJson);
                        String locationStatus = c.getString(c
                                .getColumnIndex(MobilityInterface.KEY_STATUS));
                        mobilityPointJson.put("location_status", locationStatus);
                        if (!locationStatus.equals("unavailable")) {
                            JSONObject locationJson = new JSONObject();
                            if (!Double.isNaN(Double.parseDouble(c.getString(c
                                    .getColumnIndex(MobilityInterface.KEY_LATITUDE)))))
                                locationJson.put("latitude", Double.parseDouble(c.getString(c
                                        .getColumnIndex(MobilityInterface.KEY_LATITUDE))));
                            else
                                locationJson.put("latitude", null);

                            if (!Double.isNaN(Double.parseDouble(c.getString(c
                                    .getColumnIndex(MobilityInterface.KEY_LONGITUDE)))))
                                locationJson.put("longitude", Double.parseDouble(c.getString(c
                                        .getColumnIndex(MobilityInterface.KEY_LONGITUDE))));
                            else
                                locationJson.put("longitude", null);
                            locationJson.put("provider",
                                    c.getString(c.getColumnIndex(MobilityInterface.KEY_PROVIDER)));
                            locationJson.put("accuracy", Float.parseFloat(c.getString(c
                                    .getColumnIndex(MobilityInterface.KEY_ACCURACY))));
                            locationJson
                                    .put("timestamp",
                                            dateFormat.format(new Date(
                                                    Long.parseLong(c.getString(c
                                                            .getColumnIndex(MobilityInterface.KEY_LOC_TIMESTAMP))))));
                            mobilityPointJson.put("location", locationJson);
                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    outWrite.write(mobilityPointJson.toString());

                    c.moveToNext();
                }
                remainingCount -= limit;
                outWrite.close();

            }
            Log.v(TAG, "OK, done!");
            c.close();
        } else {
            Log.v(TAG, "No mobility points to write.");
        }

    }

    // private int getMobilityCount() {
    // // SharedPreferencesHelper prefHelper = new
    // SharedPreferencesHelper(this);
    // // Long lastMobilityUploadTimestamp =
    // prefHelper.getLastMobilityUploadTimestamp();
    // Cursor c = MobilityInterface.getMobilityCursor(this, 0L);
    // if (c == null) {
    // return 0;
    // } else {
    // int count = c.getCount();
    // c.close();
    // return count;
    // }
    // }
}
