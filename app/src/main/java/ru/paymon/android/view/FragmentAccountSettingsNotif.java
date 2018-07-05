package ru.paymon.android.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.paymon.android.R;

public class FragmentAccountSettingsNotif extends PreferenceFragmentCompat {
    private static FragmentAccountSettingsNotif instance;

    public static synchronized FragmentAccountSettingsNotif newInstance(){
        instance = new FragmentAccountSettingsNotif();
        return instance;
    }

    public static synchronized FragmentAccountSettingsNotif getInstance(){
        if (instance == null)
            instance = new FragmentAccountSettingsNotif();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_account_settings_notif);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }
}
