package ru.paymon.android.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.User.CLIENT_BASIC_DATE_FORMAT_LIST;


public class FragmentSettingsBasic extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static FragmentSettingsBasic instance;

    public static synchronized FragmentSettingsBasic newInstance() {
        instance = new FragmentSettingsBasic();
        return instance;
    }

    public static synchronized FragmentSettingsBasic getInstance() {
        if (instance == null)
            instance = new FragmentSettingsBasic();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference_settings_basic);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPreferences, CLIENT_BASIC_DATE_FORMAT_LIST);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setActionBarWithTitle(getActivity(), "Основные");//TODO:string
        Utils.setArrowBackInToolbar(getActivity());
        Utils.hideBottomBar(getActivity());
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }
}
