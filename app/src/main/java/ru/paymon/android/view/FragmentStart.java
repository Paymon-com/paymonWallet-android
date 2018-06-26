package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
        view.setBackgroundResource(R.drawable.backgroundt);

        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        Button authButton = (Button) view.findViewById(R.id.fragment_start_auth_button);
        Button registrationButton = (Button) view.findViewById(R.id.fragment_start_registration_button);
        TextView hintTextView = (TextView) view.findViewById(R.id.fragment_start_already_have_button);
        ImageView logoImageView = (ImageView) view.findViewById(R.id.fragment_start_logo_image_view);

        registrationButton.setOnClickListener((v) -> {
//            final FragmentRegistrationLogin fragmentRegistrationLogin = FragmentRegistrationLogin.newInstance();
//            Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), fragmentRegistrationLogin, "registr");
        });

        authButton.setOnClickListener((v) -> {
            final FragmentAuthorization fragmentAuthorization = FragmentAuthorization.newInstance();
            Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), fragmentAuthorization, "auth");
        });

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
