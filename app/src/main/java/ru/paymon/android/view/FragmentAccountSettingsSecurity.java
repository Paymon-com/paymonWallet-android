package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.paymon.android.R;

public class FragmentAccountSettingsSecurity extends PreferenceFragmentCompat {
    private static FragmentAccountSettingsSecurity instance;

    public static synchronized FragmentAccountSettingsSecurity newInstance(){
        instance = new FragmentAccountSettingsSecurity();
        return instance;
    }

    public static synchronized FragmentAccountSettingsSecurity getInstance(){
        if (instance == null)
            instance = new FragmentAccountSettingsSecurity();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_account_settings_security);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }
}
