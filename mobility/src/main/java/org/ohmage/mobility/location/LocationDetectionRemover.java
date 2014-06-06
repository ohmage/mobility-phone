package org.ohmage.mobility.location;

import android.app.PendingIntent;
import android.content.Context;

import com.google.android.gms.location.LocationClient;

import org.ohmage.mobility.ActivityUtils;
import org.ohmage.mobility.DetectionRemover;

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
public class LocationDetectionRemover extends DetectionRemover<LocationClient> {

    public LocationDetectionRemover(Context context) {
        super(context);
    }

    @Override
    protected void removeUpdatesFromClient(Context context, LocationClient client, PendingIntent intent) {
        client.removeLocationUpdates(intent);

        // Save request state
        context.getSharedPreferences(ActivityUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE).edit()
                .putBoolean(ActivityUtils.KEY_LOCATION_RUNNING, false).commit();
    }

    @Override
    protected LocationClient createGooglePlayServicesClient(Context context) {
        return new LocationClient(context, this, this);
    }
}
