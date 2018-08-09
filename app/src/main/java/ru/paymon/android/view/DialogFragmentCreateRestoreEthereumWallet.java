package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import ru.paymon.android.R;

public class DialogFragmentCreateRestoreEthereumWallet extends DialogFragment {
    private static DialogFragmentCreateRestoreEthereumWallet instance;

    public static DialogFragmentCreateRestoreEthereumWallet newInstance(){
        instance = new DialogFragmentCreateRestoreEthereumWallet();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_create_restore_eth_wallet, null);

        ConstraintLayout constraintLayoutCreate = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_create_restore_eth_wallet_clCreate);
        ConstraintLayout constraintLayoutRestore = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_create_restore_eth_wallet_clRestore);

        constraintLayoutCreate.setOnClickListener((view1 -> {
            DialogFragmentCreateEthereumWallet.newInstance().show(getActivity().getSupportFragmentManager(), null);
            getDialog().cancel();
        }));

        constraintLayoutRestore.setOnClickListener((view1 -> {
            DialogFragmentRestoreEthereumWallet.newInstance().show(getActivity().getSupportFragmentManager(), null);
            getDialog().cancel();
        }));

        return view;
    }
}
