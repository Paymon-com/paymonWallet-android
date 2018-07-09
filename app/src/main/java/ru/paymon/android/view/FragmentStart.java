package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;


public class FragmentStart extends Fragment {
    private static FragmentStart instance;

    public static synchronized FragmentStart newInstance() {
        instance = new FragmentStart();
        return instance;
    }

    public static synchronized FragmentStart getInstance() {
        if (instance == null)
            instance = new FragmentStart();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        view.setBackgroundResource(R.drawable.background);

        Button authButton = (Button) view.findViewById(R.id.fragment_start_auth_button);
        Button registrationButton = (Button) view.findViewById(R.id.fragment_start_registration_button);

        registrationButton.setOnClickListener((v) ->
                Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentRegistrationLogin.newInstance(), null)
        );

        authButton.setOnClickListener((v) ->
                Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentAuthorization.newInstance(), null)
        );

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideActionBar(getActivity());
        Utils.hideBottomBar(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
    }
}
