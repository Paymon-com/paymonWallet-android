package ru.paymon.android.view.Money;

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
import ru.paymon.android.User;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.Money.FragmentMoney.CURRENCY_KEY;
import static ru.paymon.android.view.Money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.Money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;

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

            switch (currency){
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
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_private_key, null);

        TextView privateKey = (TextView) view.findViewById(R.id.dialog_fragment_private_key_address);
        Button button = (Button) view.findViewById(R.id.dialog_fragment_private_key_button);

        String privateKeyStr = "";
        switch (currency) {
            case "ETH":
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.eth_color));
                 privateKeyStr = ((WalletApplication) getActivity().getApplication()).getEthereumWallet(User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD).privateAddress;
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
