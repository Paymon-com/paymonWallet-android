package ru.paymon.android.gateway.bitcoin.data;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.Date;

import ru.paymon.android.WalletApplication;

/**
 * @author Andreas Schildbach
 */
public class TimeLiveData extends LiveData<Date> {
    private final WalletApplication application;

    public TimeLiveData(final WalletApplication application) {
        this.application = application;
    }

    @Override
    protected void onActive() {
        application.registerReceiver(tickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        setValue(new Date());
    }

    @Override
    protected void onInactive() {
        application.unregisterReceiver(tickReceiver);
    }

    private final BroadcastReceiver tickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            setValue(new Date());
        }
    };
}