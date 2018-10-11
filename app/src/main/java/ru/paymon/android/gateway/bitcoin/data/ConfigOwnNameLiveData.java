package ru.paymon.android.gateway.bitcoin.data;


import android.arch.lifecycle.LiveData;
import android.content.SharedPreferences;

import ru.paymon.android.WalletApplication;
import ru.paymon.android.gateway.bitcoin.Configuration;

public class ConfigOwnNameLiveData extends LiveData<String>
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final Configuration config;

    public ConfigOwnNameLiveData(final WalletApplication application) {
        this.config = application.getConfiguration();
    }

    @Override
    protected void onActive() {
        config.registerOnSharedPreferenceChangeListener(this);
        setValue(config.getOwnName());
    }

    @Override
    protected void onInactive() {
        config.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (Configuration.PREFS_KEY_OWN_NAME.equals(key))
            setValue(config.getOwnName());
    }
}
