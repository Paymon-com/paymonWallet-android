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
import android.widget.FrameLayout;
import android.widget.TextView;

import ru.paymon.android.R;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.utils.Utils;

public class DialogFragmentPublicKey extends DialogFragment {
    private static DialogFragmentPublicKey instance;
    private static String currency;

    public static DialogFragmentPublicKey newInstance(String cur) {
        instance = new DialogFragmentPublicKey();
        currency = cur;
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_public_key, null);

        TextView publicKey = (TextView) view.findViewById(R.id.dialog_fragment_public_key_address);
        Button button = (Button) view.findViewById(R.id.dialog_fragment_public_key_button);

        String publicKeyStr = "";
        switch (currency) {
            case "ETH":
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.eth_color));
                publicKeyStr = Ethereum.getInstance().getAddress();
                break;
            case "BTC":
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.btc_color));
//                publicKeyStr = Bitcoin.getInstance().getAddress();//TODO:
                break;
            case "PMNT":
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.pmnc_color));
//                publicKeyStr = Paymon.getInstance().getAddress();//TODO:
                break;
        }

        final String prKey = publicKeyStr;
        publicKey.setText(prKey);
        publicKey.setOnClickListener((view1) -> Utils.copyText(prKey, getActivity()));

        button.setOnClickListener(view1 -> dismiss());

        return view;
    }
}