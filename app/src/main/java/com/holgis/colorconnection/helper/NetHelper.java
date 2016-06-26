package com.holgis.colorconnection.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Holger on 21.02.2016.
 */
public class NetHelper {

    static public boolean isConnected(Context context) {

        final int[] NETWORK_TYPES = {ConnectivityManager.TYPE_WIFI,
                ConnectivityManager.TYPE_ETHERNET};

        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        for (int networkType : NETWORK_TYPES) {
            NetworkInfo info = connManager.getNetworkInfo(networkType);
            if (info != null && info.isConnectedOrConnecting()) {
                return true;
            }
        }
        return false;
    }

}
