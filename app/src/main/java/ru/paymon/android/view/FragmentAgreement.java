package ru.paymon.android.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;

public class FragmentAgreement extends Fragment {
    private static FragmentAgreement instance;

    public static synchronized FragmentAgreement newInstance() {
        instance = new FragmentAgreement();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.agreement_layout, container, false);
        Utils.setActionBarWithTitle(getActivity(), getString(R.string.agreement));

        final Button backToRegistration = view.findViewById(R.id.agreement_back_to_registration_button);

        backToRegistration.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

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
