package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;

public class FragmentProfile extends Fragment {
    private static FragmentProfile instance;

    public static synchronized FragmentProfile newInstance(){
        instance = new FragmentProfile();
        return instance;
    }

    public static synchronized FragmentProfile getInstance(){
        if (instance == null)
            instance = new FragmentProfile();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        Utils.setActionBarWithTitle(getActivity(), "");

        ImageView updateProfile = view.findViewById(R.id.profile_update_button);

        updateProfile.setOnClickListener(v -> {
            final FragmentAccountSettingsUpdateProfile fragmentAccountSettingsUpdateProfile = FragmentAccountSettingsUpdateProfile.newInstance();
            Utils.replaceFragmentWithAnimationFade(getActivity().getSupportFragmentManager(), fragmentAccountSettingsUpdateProfile, null);
        });

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.profile_settings, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final FragmentSettings fragmentSettings = FragmentSettings.newInstance();
        Utils.replaceFragmentWithAnimationFade(getActivity().getSupportFragmentManager(), fragmentSettings, null);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
