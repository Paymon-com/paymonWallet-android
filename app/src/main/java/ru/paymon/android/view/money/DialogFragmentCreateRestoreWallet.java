package ru.paymon.android.view.money;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.paymon.android.R;

import static ru.paymon.android.view.money.FragmentMoney.CURRENCY_KEY;

public class DialogFragmentCreateRestoreWallet extends DialogFragment {
    private String currency;

    public DialogFragmentCreateRestoreWallet setArgs(Bundle bundle) {
        this.setArguments(bundle);
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (!bundle.containsKey(CURRENCY_KEY)) return;
            currency = bundle.getString(CURRENCY_KEY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_create_restore_wallet, null);

        ConstraintLayout constraintLayoutCreate = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_create_restore_eth_wallet_clCreate);
        ConstraintLayout constraintLayoutRestore = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_create_restore_eth_wallet_clRestore);

        constraintLayoutCreate.setOnClickListener((v -> {
            Bundle bundle = new Bundle();
            bundle.putString(CURRENCY_KEY, currency);
            new DialogFragmentCreateWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null);
            getDialog().cancel();
        }));

        constraintLayoutRestore.setOnClickListener((v -> {
            Bundle bundle = new Bundle();
            bundle.putString(CURRENCY_KEY, currency);
            new DialogFragmentRestoreWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null);
            getDialog().cancel();
        }));

        return view;
    }
}
