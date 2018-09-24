package ru.paymon.android.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.activities.KeyGuardActivity;

import static ru.paymon.android.User.CLIENT_SECURITY_PASSWORD_ENABLED_CHECK;
import static ru.paymon.android.User.CLIENT_SECURITY_PASSWORD_PREFERENCE;

public class FragmentSettingsSecurity extends PreferenceFragmentCompat {
    private static FragmentSettingsSecurity instance;
    private CheckBoxPreference passwordEnabledPreference;
    private Preference passwordPreference;

    public static synchronized FragmentSettingsSecurity newInstance() {
        instance = new FragmentSettingsSecurity();
        return instance;
    }

    public static synchronized FragmentSettingsSecurity getInstance() {
        if (instance == null)
            instance = new FragmentSettingsSecurity();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preference_settings_security);

        passwordEnabledPreference = (CheckBoxPreference) findPreference(CLIENT_SECURITY_PASSWORD_ENABLED_CHECK);
        passwordPreference = findPreference(CLIENT_SECURITY_PASSWORD_PREFERENCE);

        passwordEnabledPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if (User.CLIENT_SECURITY_PASSWORD_VALUE != null) {
                Intent intent = new Intent(ApplicationLoader.applicationContext, KeyGuardActivity.class);
                getActivity().startActivityForResult(intent, 20);
                return false;
            }
            passwordPreference.setEnabled(User.CLIENT_SECURITY_PASSWORD_VALUE == null && (boolean) newValue);
            return true;
        });

        passwordPreference.setOnPreferenceClickListener((preference) -> {
            Intent intent = new Intent(getActivity(), KeyGuardActivity.class);
            startActivity(intent);
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        passwordEnabledPreference.setChecked(User.CLIENT_SECURITY_PASSWORD_VALUE != null);
        passwordPreference.setEnabled(User.CLIENT_SECURITY_PASSWORD_VALUE == null && passwordEnabledPreference.isChecked());
    }
}
