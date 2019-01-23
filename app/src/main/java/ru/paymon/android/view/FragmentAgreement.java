package ru.paymon.android.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.navigation.Navigation;
import ru.paymon.android.R;

public class FragmentAgreement extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.agreement_layout, container, false);
        TextView textAgreement = view.findViewById(R.id.agreement_text_view);

        String firstPart = getString(R.string.other_agreements_part1);
        String secondPart = getString(R.string.other_agreements_part2);

        textAgreement.setText(firstPart + secondPart);

        final Button backToRegistration = view.findViewById(R.id.agreement_back_to_registration_button);

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
