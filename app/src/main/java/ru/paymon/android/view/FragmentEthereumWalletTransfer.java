package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

import ru.paymon.android.R;
import ru.paymon.android.models.ExchangeRatesItem;
import ru.paymon.android.utils.ExchangeRates;

public class FragmentEthereumWalletTransfer extends Fragment {
    private static FragmentEthereumWalletTransfer instance;

    public static FragmentEthereumWalletTransfer newInstance() {
        instance = new FragmentEthereumWalletTransfer();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ethereum_wallet_transfer, container, false);

        EditText receiverAddress = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_receiver_address);
        EditText cryptoAmount = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_amount);
        EditText fiatEquivalent = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_fiat_equivalent);
        EditText gas = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas);
        TextView gasRec = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_rec);
        TextView titleFrom = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_title_from);
        TextView idFrom = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_id_from);
        TextView balance = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_balance);
        FloatingActionButton qr = (FloatingActionButton) view.findViewById(R.id.fragment_ethereum_wallet_transfer_qr);

        DialogProgress dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void loadExRatesAndWalletBalance(){
        final HashMap<String, HashMap<String, ExchangeRatesItem>> exchangeRates = ExchangeRates.getInstance().parseExchangeRates();


    }
}
