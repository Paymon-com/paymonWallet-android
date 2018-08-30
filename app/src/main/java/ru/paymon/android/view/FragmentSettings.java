package ru.paymon.android.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.User;

public class FragmentSettings extends Fragment implements NavigationView.OnNavigationItemSelectedListener {
    private static FragmentSettings instance;
    private TextView toolbarTitle;

    public static FragmentSettings newInstance() {
        instance = new FragmentSettings();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        final NavigationView settingsMenu = (NavigationView) view.findViewById(R.id.fragment_settings_navigation_view);
        toolbarTitle = getActivity().findViewById(R.id.toolbar_title);

        settingsMenu.setNavigationItemSelectedListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbarTitle.setText(R.string.title_settings);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();

        switch (itemId) {
            case R.id.settings_notifications:
                toolbarTitle.setText(R.string.settings_notifications);
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.animator.fade_to_back, R.animator.fade_to_up, R.animator.fade_to_back);
                fragmentTransaction.replace(R.id.settings_container, FragmentSettingsNotif.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.settings_basic:
                toolbarTitle.setText(R.string.settings_basic);
                fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.animator.fade_to_back, R.animator.fade_to_up, R.animator.fade_to_back);
                fragmentTransaction.replace(R.id.settings_container, FragmentSettingsBasic.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.settings_security:
                toolbarTitle.setText(R.string.settings_security);
                fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.animator.fade_to_back, R.animator.fade_to_up, R.animator.fade_to_back);
                fragmentTransaction.replace(R.id.settings_container, FragmentSettingsSecurity.newInstance());
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;
            case R.id.settings_reset_settings:
                User.setDefaultConfig();
                Toast toast = Toast.makeText(getContext(), R.string.settings_reset, Toast.LENGTH_SHORT);
                toast.show();
                break;
            case R.id.settings_about_programm:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

                final View aboutProgramDialog = layoutInflater.inflate(R.layout.about_program_dialog, null);
                ((TextView) aboutProgramDialog.findViewById(R.id.about_version)).setText(String.format(getString(R.string.about_program_text_version), Config.VERSION_STRING).replace('"', ' '));
                builder.setView(aboutProgramDialog);

                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.settings_exit:
                User.clearConfig();
                PackageManager packageManager = ApplicationLoader.applicationContext.getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage(ApplicationLoader.applicationContext.getPackageName());
                ComponentName componentName = intent.getComponent();
                Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                ApplicationLoader.applicationContext.startActivity(mainIntent);
                break;
        }

        return true;
    }
}

