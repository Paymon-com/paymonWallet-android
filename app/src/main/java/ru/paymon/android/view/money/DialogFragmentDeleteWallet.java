package ru.paymon.android.view.money;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.money.FragmentMoney.CURRENCY_KEY;
import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class DialogFragmentDeleteWallet extends DialogFragment {
    private String currency;

    public DialogFragmentDeleteWallet setArgs(Bundle bundle) {
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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_delete_wallet, null);

        TextView hint = (TextView) view.findViewById(R.id.dialog_fragment_with_edit_title);
        ConstraintLayout okButton = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_with_edit_ok);
        ConstraintLayout cancelButton = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_with_edit_cancel);
        EditText editText = (EditText) view.findViewById(R.id.dialog_fragment_with_edit_edit_text);

        switch (currency) {
            case ETH_CURRENCY_VALUE:
                okButton.setOnClickListener(view1 -> {
                    final String password = editText.getText().toString().trim();

                    if (password.equals(User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD)) {
                        boolean isDeleted = ((WalletApplication) getActivity().getApplication()).deleteEthereumWallet();
                        if (isDeleted) {
                            User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD = null;
                            User.saveConfig();
                            Toast.makeText(getContext(), R.string.other_success, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), R.string.other_fail, Toast.LENGTH_LONG).show();
                        }

                        Utils.hideKeyboard(view);
                        getDialog().dismiss();
                        getDialog().cancel();
                        getActivity().onBackPressed();
                    } else {
                        Toast.makeText(getActivity(), R.string.keyguard_wrong_password, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case BTC_CURRENCY_VALUE:
                okButton.setOnClickListener(view1 -> {
                    final String password = editText.getText().toString().trim();

                    if (password.equals(User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD)) {
                        boolean isDeleted = ((WalletApplication) getActivity().getApplication()).deleteBitcoinWallet();
                        if (isDeleted) {
                            User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD = null;
                            User.saveConfig();
                            Toast.makeText(getContext(), R.string.other_success, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), R.string.other_fail, Toast.LENGTH_LONG).show();
                        }
                        Utils.hideKeyboard(view);
                        getDialog().dismiss();
                        getDialog().cancel();
                        getActivity().onBackPressed();
                    } else {
                        Toast.makeText(getActivity(), R.string.keyguard_wrong_password, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case PMNT_CURRENCY_VALUE:
                okButton.setOnClickListener(view1 -> {
                    final String password = editText.getText().toString().trim();

                    if (password.equals(User.CLIENT_MONEY_PAYMON_WALLET_PASSWORD)) {
                        boolean isDeleted = ((WalletApplication) getActivity().getApplication()).deletePaymonWallet();
                        if (isDeleted) {
                            User.CLIENT_MONEY_PAYMON_WALLET_PASSWORD = null;
                            User.saveConfig();
                            Toast.makeText(getContext(), R.string.other_success, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), R.string.other_fail, Toast.LENGTH_LONG).show();
                        }

                        Utils.hideKeyboard(view);
                        getDialog().dismiss();
                        getDialog().cancel();
                        getActivity().onBackPressed();
                    } else {
                        Toast.makeText(getActivity(), R.string.keyguard_wrong_password, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }

        cancelButton.setOnClickListener(view1 -> getDialog().dismiss());

        return view;
    }
}
