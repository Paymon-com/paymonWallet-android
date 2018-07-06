package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.utils.Utils;

public class FragmentSettings extends Fragment {
    private static FragmentSettings instance;

    public static synchronized FragmentSettings newInstance(){
        instance = new FragmentSettings();
        return instance;
    }

    public static synchronized FragmentSettings getInstance(){
        if (instance == null)
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

        TextView settProfileName = view.findViewById(R.id.settings_name_text_view);
        settProfileName.setText(Utils.formatUserName(User.currentUser));

        CircleImageView profilePhoto = view.findViewById(R.id.settings_avatar_image_view);

        TextView updateProfile = view.findViewById(R.id.settings_update_profile_button);
        ImageView notifClick = view.findViewById(R.id.notif_click_image_view);
        ImageView basicClick = view.findViewById(R.id.basic_click_image_view);
        ImageView securityClick = view.findViewById(R.id.security_click_image_view);
        ImageView aboutClick = view.findViewById(R.id.about_the_program_click_image_view);

        FragmentManager fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();

        updateProfile.setOnClickListener(v -> {
            final FragmentAccountSettingsUpdateProfile fragmentAccountSettingsUpdateProfile = FragmentAccountSettingsUpdateProfile.newInstance();
            Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentAccountSettingsUpdateProfile, null);
        });

        notifClick.setOnClickListener(v -> {
            final FragmentAccountSettingsNotif fragmentAccountSettingsNotif = FragmentAccountSettingsNotif.newInstance();
            Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentAccountSettingsNotif, null);
        });

//        basicClick.setOnClickListener(v -> {
//            final FragmentAccountSettingsBasic fragmentAccountSettingsBasic = FragmentAccountSettingsBasic.newInstance();
//            Utils.replaceFragmentWithAnimationSlideFade(fragmentManager, fragmentAccountSettingsBasic, null);
//        });

        securityClick.setOnClickListener(v -> {
            final FragmentAccountSettingsSecurity fragmentAccountSettingsSecurity = FragmentAccountSettingsSecurity.newInstance();
            Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentAccountSettingsSecurity, null);
        });

        aboutClick.setOnClickListener(v -> {
            aboutClick.setBackgroundColor(ContextCompat.getColor(getActivity().getApplication(),
                    R.color.gray));
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater image = LayoutInflater.from(getActivity());
            final View view1 = image.inflate(R.layout.about_program_dialog, null);
            ((TextView) view1.findViewById(R.id.about_version)).setText(String.format(getString(R.string.about_program_text_version), Config.VERSION).replace('"', ' '));
            builder.setView(view1);

            AlertDialog alert = builder.create();
            alert.show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_settings));
        setHasOptionsMenu(true);
    }
}
