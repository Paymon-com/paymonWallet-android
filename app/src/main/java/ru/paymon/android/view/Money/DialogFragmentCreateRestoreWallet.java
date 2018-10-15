package ru.paymon.android.view.Money;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.paymon.android.R;
import ru.paymon.android.view.Money.ethereum.DialogFragmentCreateEthereumWallet;
import ru.paymon.android.view.Money.ethereum.DialogFragmentRestoreEthereumWallet;

import static ru.paymon.android.view.Money.FragmentMoney.CURRENCY_KEY;
import static ru.paymon.android.view.Money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.Money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;

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
            if(!bundle.containsKey(CURRENCY_KEY)) return;

            currency = bundle.getString(CURRENCY_KEY);

            switch (currency) {
                case BTC_CURRENCY_VALUE:
                    break;
                case ETH_CURRENCY_VALUE:
                    break;
//                case PMNT_CURRENCY_VALUE:
//                    break;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_create_restore_eth_wallet, null);

        ConstraintLayout constraintLayoutCreate = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_create_restore_eth_wallet_clCreate);
        ConstraintLayout constraintLayoutRestore = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_create_restore_eth_wallet_clRestore);

        switch (currency) {
            case BTC_CURRENCY_VALUE:
//                constraintLayoutCreate.setOnClickListener((v -> {
//                    new DialogFragmentCreateEthereumWallet().show(getActivity().getSupportFragmentManager(), null);
//                    getDialog().cancel();
//                }));
//
//                constraintLayoutRestore.setOnClickListener((v -> {
//                    new DialogFragmentRestoreEthereumWallet().show(getActivity().getSupportFragmentManager(), null);
//                    getDialog().cancel();
//                }));
                break;
            case ETH_CURRENCY_VALUE:
                constraintLayoutCreate.setOnClickListener((v -> {
                    new DialogFragmentCreateEthereumWallet().show(getActivity().getSupportFragmentManager(), null);
                    getDialog().cancel();
                }));

                constraintLayoutRestore.setOnClickListener((v -> {
                    new DialogFragmentRestoreEthereumWallet().show(getActivity().getSupportFragmentManager(), null);
                    getDialog().cancel();
                }));
                break;
//                case PMNT_CURRENCY_VALUE:
//                    break;

        }

        return view;
    }
}
