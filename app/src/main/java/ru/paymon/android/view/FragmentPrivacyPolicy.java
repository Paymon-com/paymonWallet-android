package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.navigation.Navigation;
import ru.paymon.android.R;

public class FragmentPrivacyPolicy extends Fragment {
    private static FragmentPrivacyPolicy instance;

    public static synchronized FragmentPrivacyPolicy newInstance() {
        instance = new FragmentPrivacyPolicy();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.privacy_polycy_layout, container, false);

        //Utils.setActionBarWithTitle(getActivity(), getString(R.string.privacy_policy2));

        final Button backToRegistration = view.findViewById(R.id.privacy_back_to_registration_button);

        backToRegistration.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
