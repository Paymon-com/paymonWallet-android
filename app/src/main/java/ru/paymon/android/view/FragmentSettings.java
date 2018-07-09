package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class FragmentSettings extends Fragment implements NavigationView.OnNavigationItemSelectedListener {
    private static FragmentSettings instance;
    private FragmentManager fragmentManager;

    public static synchronized FragmentSettings newInstance() {
        instance = new FragmentSettings();
        return instance;
    }

    public static synchronized FragmentSettings getInstance() {
        if (instance == null)
            instance = new FragmentSettings();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getActivity().getSupportFragmentManager();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        final NavigationView settingsMenu = (NavigationView) view.findViewById(R.id.fragment_settings_navigation_view);
        final View headerView = (View) settingsMenu.getHeaderView(0);
        final TextView name = (TextView) headerView.findViewById(R.id.fragment_settings_header_profile_name_text_view);
        final CircleImageView avatar = (CircleImageView) headerView.findViewById(R.id.fragment_settings_header_profile_avatar_image_view);

        settingsMenu.setNavigationItemSelectedListener(this);

        headerView.setOnClickListener(view1 ->
                Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentProfile.newInstance()));

        name.setText(Utils.formatUserName(User.currentUser));

        RPC.PM_photo photo = new RPC.PM_photo();
        photo.user_id = User.currentUser.id;
        photo.id = User.currentUser.photoID;
        avatar.setPhoto(photo);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_settings));
        Utils.setArrowBackInToolbar(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();

        switch (itemId) {
            case R.id.settings_notifications:
                final FragmentAccountSettingsNotif fragmentAccountSettingsNotif = FragmentAccountSettingsNotif.newInstance();
                Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentAccountSettingsNotif, null);
                break;
            case R.id.settings_basic:
                final FragmentAccountSettingsBasic fragmentAccountSettingsBasic = FragmentAccountSettingsBasic.newInstance();
                Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentAccountSettingsBasic, null);
                break;
            case R.id.settings_security:
                final FragmentAccountSettingsSecurity fragmentAccountSettingsSecurity = FragmentAccountSettingsSecurity.newInstance();
                Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentAccountSettingsSecurity, null);
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
                //TODO: Выход из учетной записи
                break;
        }

        return true;
    }
}
