
package org.ohmage.probemanager;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.mobility.glue.MobilityInterface;

public class MobilityProbeWriter extends ProbeWriter {

    private static final String TAG = "MobilityProbeWriter";

    private static final String OBSERVER_ID = "edu.ucla.cens.Mobility";
    private static final int OBSERVER_VERSION = 2012061300;

    private static final String STREAM_EXTENDED = "extended";
    private static final int STREAM_EXTENDED_VERSION = 2012050700;

    private static final String STREAM_ERROR = "error";
    private static final int STREAM_ERROR_VERSION = 2012061300;

    public MobilityProbeWriter(Context context) {
        super(context);
    }

    public void write(ProbeBuilder probe, String mode, Float speed, String accel, String wifi) {
        probe.setObserver(OBSERVER_ID, OBSERVER_VERSION);

        try {
            JSONObject data = new JSONObject();
            data.put("mode", mode);

            if (MobilityInterface.ERROR.equals(mode)) {
                probe.setStream(STREAM_ERROR, STREAM_ERROR_VERSION);
            } else {
                probe.setStream(STREAM_EXTENDED, STREAM_EXTENDED_VERSION);
                data.put("speed", speed);
                data.put("accel_data", new JSONArray(accel));
                if(!TextUtils.isEmpty(wifi))
                    data.put("wifi_data", new JSONObject(wifi));
            }

            probe.setData(data.toString()).write(this);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
