package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.utils.Utils;

public class DialogFragmentDeleteWallet extends DialogFragment {
    private static DialogFragmentDeleteWallet instance;
    private static String currency;

    public static DialogFragmentDeleteWallet newInstance(String cur) {
        instance = new DialogFragmentDeleteWallet();
        currency = cur;
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_with_edit, null);

        TextView hint = (TextView) view.findViewById(R.id.dialog_fragment_with_edit_title);
        Button okButton = (Button) view.findViewById(R.id.dialog_fragment_with_edit_ok);
        Button cancelButton = (Button) view.findViewById(R.id.dialog_fragment_with_edit_cancel);
        EditText editText = (EditText) view.findViewById(R.id.dialog_fragment_with_edit_edit_text);

        hint.setText(R.string.delete_wallet_dialog_hint);

        switch (currency) {
            case "ETH":
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.eth_color));

                okButton.setOnClickListener(view1 -> {
                    final String password = editText.getText().toString().trim();

                    if (password.equals(User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD)) {
                        Ethereum.getInstance().deleteWallet();
                        User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD = null;
                        User.saveConfig();

                        Utils.hideKeyboard(view);
                        getDialog().dismiss();
                    } else {
                        getDialog().dismiss();
                        Toast.makeText(getActivity(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case "BTC":
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.btc_color));

//                okButton.setOnClickListener(view1 -> { //TODO:
//                    final String password = editText.getText().toString().trim();
//
//                    if (password.equals(User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD)) {
//                        Ethereum.getInstance().deleteWallet();
//                        User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD = null;
//                        User.saveConfig();
//
//                        Utils.hideKeyboard(view);
//                        getDialog().dismiss();
//                    } else {
//                        getDialog().dismiss();
//                        Toast.makeText(getActivity(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
//                    }
//                });
                break;
            case "PMNT":
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.pmnc_color));

//                okButton.setOnClickListener(view1 -> {//TODO:
//                    final String password = editText.getText().toString().trim();
//
//                    if (password.equals(User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD)) {
//                        Ethereum.getInstance().deleteWallet();
//                        User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD = null;
//                        User.saveConfig();
//
//                        Utils.hideKeyboard(view);
//                        getDialog().dismiss();
//                    } else {
//                        getDialog().dismiss();
//                        Toast.makeText(getActivity(), R.string.wrong_password, Toast.LENGTH_SHORT).show();
//                    }
//                });
                break;
        }

        cancelButton.setOnClickListener(view1 -> getDialog().dismiss());

        return view;
    }
}
