package ru.paymon.android.view.money;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.paymon.android.R;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.money.FragmentMoney.CURRENCY_KEY;
import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class DialogFragmentPrivateKey extends DialogFragment {
    private String currency;

    public DialogFragmentPrivateKey setArgs(Bundle bundle){
        this.setArguments(bundle);
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle!= null){
            if(!bundle.containsKey(CURRENCY_KEY)) return;
            currency = bundle.getString(CURRENCY_KEY);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_private_key, null);

        TextView privateKey = (TextView) view.findViewById(R.id.dialog_fragment_private_key_address);
        ConstraintLayout button = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_private_key_button);

        WalletApplication application = ((WalletApplication) getActivity().getApplication());

        String privateKeyStr = "";
        switch (currency) {
            case ETH_CURRENCY_VALUE:
                 privateKeyStr = application.getEthereumWallet().privateAddress;
                break;
            case BTC_CURRENCY_VALUE:
                privateKeyStr = application.getBitcoinPrivateAddress();
                break;
            case PMNT_CURRENCY_VALUE:
                privateKeyStr = application.getPaymonWallet().privateAddress;
                break;
        }

        final String prKey = privateKeyStr;
        privateKey.setText(prKey);
        privateKey.setOnClickListener((view1) -> Utils.copyText(prKey, getActivity()));

        button.setOnClickListener(view1 -> dismiss());

        return view;
    }
}
