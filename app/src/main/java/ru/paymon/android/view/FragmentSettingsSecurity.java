package ru.paymon.android.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import ru.paymon.android.KeyGuardActivity;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.User.CLIENT_SECURITY_PASSWORD_ENABLED_CHECK;
import static ru.paymon.android.User.CLIENT_SECURITY_PASSWORD_PREFERENCE;

public class FragmentSettingsSecurity extends PreferenceFragmentCompat {
    private static FragmentSettingsSecurity instance;

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

        CheckBoxPreference passwordEnabledPreference = (CheckBoxPreference) findPreference(CLIENT_SECURITY_PASSWORD_ENABLED_CHECK);
        Preference passwordPreference = findPreference(CLIENT_SECURITY_PASSWORD_PREFERENCE);

        passwordEnabledPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            passwordPreference.setEnabled((boolean) newValue);
            return true;
        });

        if(User.CLIENT_SECURITY_PASSWORD_VALUE != null)
            passwordPreference.setEnabled(User.CLIENT_SECURITY_PASSWORD_IS_ENABLED);
        else
            passwordEnabledPreference.setChecked(false);

        passwordPreference.setOnPreferenceClickListener((preference) ->{
            Intent intent = new Intent(getActivity(), KeyGuardActivity.class);
            startActivity(intent);
//            Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentKeyGuard.newInstance(),null);
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
        Utils.setActionBarWithTitle(getActivity(), "Безопасность");
        Utils.setArrowBackInToolbar(getActivity());
    }
}
