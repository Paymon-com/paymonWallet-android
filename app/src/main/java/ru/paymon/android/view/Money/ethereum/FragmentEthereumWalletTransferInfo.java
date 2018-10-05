package ru.paymon.android.view.Money.ethereum;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

import ru.paymon.android.R;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.utils.Utils;

public class FragmentEthereumWalletTransferInfo extends Fragment {
    private static FragmentEthereumWalletTransferInfo instance;

    public static FragmentEthereumWalletTransferInfo newInstance() {
        instance = new FragmentEthereumWalletTransferInfo();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ethereum_wallet_transfer_info, container, false);

        TextView fromAddress = (TextView) view.findViewById(R.id.fragment_payment_info_id_from_text_view);
        TextView toAddress = (TextView) view.findViewById(R.id.fragment_payment_info_id_to_text_view);
        TextView fee = (TextView) view.findViewById(R.id.fragment_payment_info_commision_text_view);
        TextView amount = (TextView) view.findViewById(R.id.fragment_payment_info_amount_text_view);
        TextView total = (TextView) view.findViewById(R.id.fragment_payment_info_total);
        Button pay = (Button) view.findViewById(R.id.fragment_payment_info_pay_button);

        Bundle bundle = getArguments();
        if (bundle != null) {
            final String toAddr = bundle.getString("TO_ADDRESS");
            final BigDecimal amountValue = new BigDecimal(bundle.getString("AMOUNT"));
            final BigInteger gasPrice = new BigInteger(bundle.getString("GAS_PRICE"));
            final BigInteger gasLimit = new BigInteger(bundle.getString("GAS_LIMIT"));
            final BigDecimal feeVal = new BigDecimal(bundle.getString("FEE"));
            final BigDecimal totalVal = new BigDecimal(bundle.getString("TOTAL"));

            fromAddress.setText(Ethereum.getInstance().getAddress());
            toAddress.setText(toAddr);
            fee.setText(String.valueOf(feeVal));
            amount.setText(String.valueOf(amountValue));
            total.setText(String.valueOf(totalVal));

            pay.setOnClickListener((view1) -> {
                Utils.netQueue.postRunnable(() -> {
                    EthSendTransaction ethSendTransaction = Ethereum.getInstance().send(gasPrice, gasLimit, toAddr, Convert.toWei(amountValue, Convert.Unit.ETHER).toBigInteger());
                    if (ethSendTransaction != null) {
                        Log.e("AAA", ethSendTransaction.getTransactionHash());
                        Log.e("AAA", ethSendTransaction.getResult());
                    } else {
                        Log.e("AAA", "null");
                    }
                });
            });
        }

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
}
