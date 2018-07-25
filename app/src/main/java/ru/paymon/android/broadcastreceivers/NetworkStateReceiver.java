package ru.paymon.android.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.net.NetworkManager;

public class NetworkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NetworkManager.getInstance().networkState = isNetworkOnline();
        if (NetworkManager.getInstance().networkState == NetworkManager.NetworkState.CONNECTED) {
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        } else {
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);
        }
    }

    private NetworkManager.NetworkState isNetworkOnline() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if (netInfo != null && (netInfo.isConnectedOrConnecting() || netInfo.isAvailable())) {
                return NetworkManager.NetworkState.CONNECTED;
            }

            netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return NetworkManager.NetworkState.CONNECTED;
            } else {
                netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return NetworkManager.NetworkState.CONNECTED;
                }
            }
        } catch (Exception e) {
            Log.e("ConnectorService:", e.getMessage());
            return NetworkManager.NetworkState.CONNECTED;
        }
        return NetworkManager.NetworkState.DISCONNECTED;
    }
}
