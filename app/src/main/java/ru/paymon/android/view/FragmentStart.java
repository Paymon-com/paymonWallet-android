package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.navigation.Navigation;
import ru.paymon.android.R;


public class FragmentStart extends Fragment {
    private static FragmentStart instance;

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

        registrationButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentRegistrationLogin));
        authButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentAuthorization));

        return view;
    }
}
