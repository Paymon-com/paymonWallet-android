package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;

import ru.paymon.android.R;


public class FragmentAccountSettingsBasic extends PreferenceFragmentCompat {
    private static FragmentAccountSettingsBasic instance;

    public static synchronized FragmentAccountSettingsBasic newInstance(){
        instance = new FragmentAccountSettingsBasic();
        return instance;
    }

    public static synchronized FragmentAccountSettingsBasic getInstance(){
        if (instance == null)
            instance = new FragmentAccountSettingsBasic();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_account_settings_basic);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

}

