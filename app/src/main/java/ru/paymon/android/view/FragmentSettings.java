package ru.paymon.android.view;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Executors;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.room.AppDatabase;

public class FragmentSettings extends Fragment implements NavigationView.OnNavigationItemSelectedListener {
    private TextView toolbarTitle;

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
        toolbarTitle.setText(R.string.more_menu_settings);
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
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentSettingsNotif);
                break;
            case R.id.settings_basic:
                toolbarTitle.setText(R.string.settings_basic);
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentSettingsBasic);
                break;
            case R.id.settings_security:
                toolbarTitle.setText(R.string.settings_security);
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentSettingsSecurity);
                break;
            case R.id.settings_reset_settings:
                User.setDefaultConfig();
                Toast.makeText(getContext(), R.string.settings_reset, Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings_about_programm:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                final View aboutProgramDialog = layoutInflater.inflate(R.layout.about_program_dialog, null);
                ((TextView) aboutProgramDialog.findViewById(R.id.about_version)).setText(getString(R.string.about_program_version) + Config.VERSION_STRING);
                builder.setView(aboutProgramDialog);
                AlertDialog alert = builder.create();
                alert.show();
                break;
            case R.id.settings_exit:
                User.clearConfig();
                Executors.newSingleThreadExecutor().submit(() -> AppDatabase.getDatabase().clearAllTables());
                NetworkManager.getInstance().reconnect();
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

