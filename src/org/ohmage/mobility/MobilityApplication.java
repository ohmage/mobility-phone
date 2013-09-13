
package org.ohmage.mobility;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.RemoteException;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.logprobe.LogProbe;
import org.ohmage.logprobe.LogProbe.Loglevel;
import org.ohmage.mobility.glue.MobilityInterface;
import org.ohmage.probemanager.MobilityProbeWriter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MobilityApplication extends Application {

    private static final String KEY_OHMAGE_SERVER = "key_ohmage_server";
    private static final String MOBILITY_AGGREGATE_READ_PATH = "app/mobility/aggregate/read";

    public static MobilityProbeWriter probeWriter;

    public static MobilityApplication self;

    @Override
    public void onCreate() {
        super.onCreate();

        self = this;

        ensureLogProbe(this);

        probeWriter = new MobilityProbeWriter(this);
        probeWriter.connect();

        SharedPreferences settings = getSharedPreferences(Mobility.MOBILITY, Context.MODE_PRIVATE);
        if (settings.getBoolean(MobilityControl.MOBILITY_ON, false)) {
            Mobility.start(this);
        }

        if (settings.getBoolean("first_run", true)) {
            settings.edit().putBoolean("first_run", true).commit();
            // Only try to download aggregate data once.
//            aggregateRead(); // commented out for testing without ohmage dev key
        }
    }

    public static void ensureLogProbe(Context context) {
        LogProbe.setLevel(true, Loglevel.VERBOSE);
        LogProbe.get(context);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        LogProbe.close(this);

        probeWriter.close();
    }

    public static void aggregateRead() {

        AccountManager accountManager = AccountManager.get(self);
        final Account[] accounts = accountManager.getAccountsByType("org.ohmage");
        // Continue only if an ohmage account exists
        if (accounts.length < 1)
            return;

        String serverUrl = accountManager.getUserData(accounts[0], KEY_OHMAGE_SERVER);
        String password = accountManager.peekAuthToken(accounts[0], "org.ohmage");

        // If we don't have a password, we can't make the request
        if (password == null)
            return;

        // Download aggregate data for the last ten days. If it exists.
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setLenient(false);
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DATE, -10);
        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, 1);

        RequestParams params = new RequestParams();
        params.put("user", accounts[0].name);
        params.put("password", password);
        params.put("client", "mobilityPhone");
        params.put("start_date", sdf.format(start.getTime()));
        params.put("end_date", sdf.format(now.getTime()));
        params.put("duration", String.valueOf(1));

        // Start request
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(60000);
        AsyncHttpResponseHandler aggregateHandler = new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if ("success".equals(response.get("result"))) {
                        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

                        JSONArray data = response.getJSONArray("data");
                        JSONObject obj, leaf;
                        JSONArray node;
                        for (int i = 0; i < data.length(); i++) {
                            obj = data.getJSONObject(i);
                            node = obj.getJSONArray("data");
                            for (int j = 0; j < node.length(); j++) {
                                leaf = node.getJSONObject(j);
                                ContentValues values = new ContentValues();
                                values.put(MobilityInterface.KEY_DURATION, leaf.getLong("duration"));
                                values.put(MobilityInterface.KEY_MODE, leaf.getString("mode"));
                                values.put(MobilityInterface.KEY_DAY, obj.getString("timestamp"));
                                values.put(MobilityInterface.KEY_USERNAME, accounts[0].name);
                                operations.add(ContentProviderOperation
                                        .newInsert(MobilityInterface.AGGREGATES_URI)
                                        .withValues(values).build());
                            }
                        }

                        try {
                            self.getContentResolver().applyBatch(MobilityInterface.AUTHORITY,
                                    operations);
                            self.getContentResolver().notifyChange(
                                    MobilityInterface.AGGREGATES_URI, null, false);
                        } catch (OperationApplicationException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                // We don't care if it fails for now
            }
        };

        client.post(serverUrl + MOBILITY_AGGREGATE_READ_PATH, params, aggregateHandler);
    }
}
