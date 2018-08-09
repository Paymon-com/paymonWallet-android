package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import ru.paymon.android.R;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.utils.Utils;

public class DialogFragmentPrivateKey extends DialogFragment {
    private static DialogFragmentPrivateKey instance;
    private static String currency;

    public static DialogFragmentPrivateKey newInstance(String cur) {
        instance = new DialogFragmentPrivateKey();
        currency = cur;
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_private_key, null);

        TextView privateKey = (TextView) view.findViewById(R.id.dialog_fragment_private_key_address);
        Button button = (Button) view.findViewById(R.id.dialog_fragment_private_key_button);

        String privateKeyStr = "";
        switch (currency) {
            case "ETH":
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.eth_color));
                 privateKeyStr = Ethereum.getInstance().getPrivateKey();
                break;
            case "BTC":
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.btc_color));
//                privateKeyStr = Bitcoin.getInstance().getPrivateKey();//TODO:
                break;
            case "PMNT":
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.pmnc_color));
//                privateKeyStr = Paymon.getInstance().getPrivateKey();//TODO:
                break;
        }

        final String prKey = privateKeyStr;
        privateKey.setText(prKey);
        privateKey.setOnClickListener((view1) -> Utils.copyText(prKey, getActivity()));

        button.setOnClickListener(view1 -> dismiss());

        return view;
    }
}
