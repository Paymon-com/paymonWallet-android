package ru.paymon.android.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.User.CLIENT_MESSAGES_NOTIFY_SOUND_PREFERENCE;

public class FragmentSettingsNotif extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static FragmentSettingsNotif instance;

    public static synchronized FragmentSettingsNotif newInstance(){
        instance = new FragmentSettingsNotif();
        return instance;
    }

    public static synchronized FragmentSettingsNotif getInstance(){
        if (instance == null)
            instance = new FragmentSettingsNotif();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference_settings_notif);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPreferences, CLIENT_MESSAGES_NOTIFY_SOUND_PREFERENCE);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setActionBarWithTitle(getActivity(), "Уведомления");//TODO:string
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {//TODO:название выбранного звука
//        Preference preference = findPreference(key);
//        if (preference instanceof ListPreference) {
//            ListPreference listPreference = (ListPreference) preference;
//            int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
//            if (prefIndex >= 0) {
//                preference.setSummary(listPreference.getEntries()[prefIndex]);
//            }
//        } else if(preference instanceof SwitchPreferenceCompat) {
//            preference.setSummary(sharedPreferences.getString(key, ""));
//        } else {
//            preference.setSummary(sharedPreferences.getString(key, ""));
//        }
    }
}