package ru.paymon.android.gateway.bitcoin.data;

import android.arch.lifecycle.LiveData;
import android.content.SharedPreferences;

import org.bitcoinj.utils.MonetaryFormat;

import ru.paymon.android.WalletApplication;
import ru.paymon.android.gateway.bitcoin.Configuration;

/**
 * @author Andreas Schildbach
 */
public class ConfigFormatLiveData extends LiveData<MonetaryFormat>
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Configuration config;

    public ConfigFormatLiveData(final WalletApplication application) {
        this.config = application.getConfiguration();
    }

    @Override
    protected void onActive() {
        config.registerOnSharedPreferenceChangeListener(this);
        setValue(config.getFormat());
    }

    @Override
    protected void onInactive() {
        config.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (Configuration.PREFS_KEY_BTC_PRECISION.equals(key))
            setValue(config.getFormat());
    }
}
