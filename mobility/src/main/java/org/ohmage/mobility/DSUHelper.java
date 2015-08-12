package org.ohmage.mobility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jaredsieling on 8/12/15.
 */
public class DSUHelper {
    private final static String PREFERENCE_DSU_URL = "preference_dsu_url";

    public static String getUrl(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(PREFERENCE_DSU_URL, context.getString(R.string.dsu_client_url));
    }

    public static void setUrl(Context context, String url){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PREFERENCE_DSU_URL, url).commit();
    }
}
