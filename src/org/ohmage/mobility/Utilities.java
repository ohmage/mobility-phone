
package org.ohmage.mobility;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;

public class Utilities {
    public static String getUserName(Context context) {
        String username = MobilityDbAdapter.DEFAULT_USERNAME;

        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType("org.ohmage");
        if (accounts.length > 0) {
            username = accounts[0].name;
        } else {
            // Maybe ohmage is old and told us the username
            SharedPreferences settings = context.getSharedPreferences(Mobility.MOBILITY,
                    Context.MODE_PRIVATE);
            username = settings
                    .getString(Mobility.KEY_USERNAME, MobilityDbAdapter.DEFAULT_USERNAME);
        }
        return username;
    }
}
