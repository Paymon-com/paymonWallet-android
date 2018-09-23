package ru.paymon.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.adapters.EthereumTransactionAdapter;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.models.TransactionItem;
import ru.paymon.android.utils.RecyclerItemClickListener;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.MoneyViewModel;

public class FragmentEthereumWallet extends Fragment {
    private static FragmentEthereumWallet instance;
    private MoneyViewModel moneyViewModel;
    private LiveData<String> ethereumBalanceData;
    private LiveData<ArrayList<TransactionItem>> transactionsData;
    private EthereumTransactionAdapter ethereumTransactionAdapter;

    public static FragmentEthereumWallet newInstance() {
        if (instance == null)
            instance = new FragmentEthereumWallet();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moneyViewModel = ViewModelProviders.of(getActivity()).get(MoneyViewModel.class);
        ethereumBalanceData = moneyViewModel.getEthereumBalanceData();
        transactionsData = moneyViewModel.getTranscationsData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ethereum_wallet, container, false);

        ImageButton deposit = (ImageButton) view.findViewById(R.id.fragment_ethereum_wallet_deposit_button);
        ImageButton transfer = (ImageButton) view.findViewById(R.id.fragment_ethereum_wallet_transfer_button);
        ImageButton withdraw = (ImageButton) view.findViewById(R.id.fragment_ethereum_wallet_withdraw_button);
        ImageButton backBtn = (ImageButton) view.findViewById(R.id.toolbar_ethereum_wallet_back_btn);
        ImageButton restoreBtn = (ImageButton) view.findViewById(R.id.toolbar_ethereum_wallet_restore_btn);
        ImageButton backupBtn = (ImageButton) view.findViewById(R.id.toolbar_ethereum_wallet_backup_btn);
        ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.toolbar_ethereum_wallet_delete_btn);
        Button privateKey = (Button) view.findViewById(R.id.fragment_ethereum_wallet_private_key_button);
        Button publicKey = (Button) view.findViewById(R.id.fragment_ethereum_wallet_public_key_button);
        TextView historyText = (TextView) view.findViewById(R.id.history_transaction_is_empty);
        TextView balance = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_balance);
        RecyclerView transactionsRecView = (RecyclerView) view.findViewById(R.id.history_transaction_recycler_view);


        backBtn.setOnClickListener(v -> getActivity().getSupportFragmentManager().popBackStack());

        restoreBtn.setOnClickListener(v -> {
            DialogFragmentRestoreEthereumWallet.newInstance().show(getActivity().getSupportFragmentManager(), null);
        });

        backupBtn.setOnClickListener(v -> {
            Ethereum.getInstance().backupWallet();
        });

        deleteBtn.setOnClickListener(v -> {
            DialogFragmentDeleteWallet.newInstance("ETH").show(getActivity().getSupportFragmentManager(), null);
        });


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        transactionsRecView.setLayoutManager(linearLayoutManager);

        transactionsRecView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), transactionsRecView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TransactionItem transactionItem = ethereumTransactionAdapter.transactionItems.get(position);

                AlertDialog.Builder adb = new AlertDialog.Builder(getContext());

                view = (ConstraintLayout) getLayoutInflater().inflate(R.layout.alert_dialog_custom_transaction_info, null);

                TextView hash = (TextView) view.findViewById(R.id.ethereum_transaction_hash_alert);
                TextView status = (TextView) view.findViewById(R.id.ethereum_transaction_status_alert);
                TextView time = (TextView) view.findViewById(R.id.ethereum_transaction_time_alert);
                TextView value = (TextView) view.findViewById(R.id.ethereum_transaction_value_alert);
                TextView to = (TextView) view.findViewById(R.id.ethereum_transaction_to_alert);
                TextView from = (TextView) view.findViewById(R.id.ethereum_transaction_from_alert);
                TextView gasLimit = (TextView) view.findViewById(R.id.ethereum_transaction_gas_limit_alert);
                TextView gasUsed = (TextView) view.findViewById(R.id.ethereum_transaction_gas_used_alert);
                TextView gasPrice = (TextView) view.findViewById(R.id.ethereum_transaction_gas_price_alert);

                hash.setText(transactionItem.hash);
                status.setText(transactionItem.status);
                time.setText(transactionItem.time);
                value.setText(transactionItem.value);
                to.setText(transactionItem.to);
                from.setText(transactionItem.from);
                gasLimit.setText(transactionItem.gasLimit);
                gasUsed.setText(transactionItem.gasUsed);
                gasPrice.setText(transactionItem.gasPrice);

                adb.setView(view).create().show();
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        ethereumBalanceData.observe(getActivity(), (balanceData) -> balance.setText(balanceData));

        transactionsData.observe(getActivity(), (transactionItems) -> {
            ethereumTransactionAdapter = new EthereumTransactionAdapter(transactionItems);
            transactionsRecView.setAdapter(ethereumTransactionAdapter);
            if (ethereumTransactionAdapter.transactionItems.size() > 0) {
                historyText.setVisibility(View.INVISIBLE);
            }
        });


//        deposit.setOnClickListener(view1 -> Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentEthereumDeposit.newInstance(), null));
        transfer.setOnClickListener(view1 -> Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentEthereumWalletTransfer.newInstance(), null));
//        withdraw.setOnClickListener(view1 -> Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentEthereumWidthdraw.newInstance(), null));
        privateKey.setOnClickListener(view1 -> DialogFragmentPrivateKey.newInstance("ETH").show(getActivity().getSupportFragmentManager(), null));
        publicKey.setOnClickListener(view1 -> DialogFragmentPublicKey.newInstance("ETH").show(getActivity().getSupportFragmentManager(), null));

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
