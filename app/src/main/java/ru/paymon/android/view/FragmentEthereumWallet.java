package ru.paymon.android.view;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidviewhover.tools.Util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.adapters.EthereumTransactionAdapter;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.models.TransactionItem;
import ru.paymon.android.utils.RecyclerItemClickListener;
import ru.paymon.android.utils.Utils;

public class FragmentEthereumWallet extends Fragment {
    public LinkedList<TransactionItem> list = new LinkedList<>();
    private static FragmentEthereumWallet instance;
    private TextView balance;

    public static FragmentEthereumWallet newInstance() {
        instance = new FragmentEthereumWallet();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ethereum_wallet, container, false);

        ImageButton deposit = (ImageButton) view.findViewById(R.id.fragment_ethereum_wallet_deposit_button);
        ImageButton transfer = (ImageButton) view.findViewById(R.id.fragment_ethereum_wallet_transfer_button);
        ImageButton withdraw = (ImageButton) view.findViewById(R.id.fragment_ethereum_wallet_withdraw_button);
        Button privateKey = (Button) view.findViewById(R.id.fragment_ethereum_wallet_private_key_button);
        Button publicKey = (Button) view.findViewById(R.id.fragment_ethereum_wallet_public_key_button);
        TextView historyText = (TextView) view.findViewById(R.id.history_transaction_is_empty);
        balance = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_balance);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.history_transaction_recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        EthereumTransactionAdapter ethereumTransactionAdapter = new EthereumTransactionAdapter(list);
        recyclerView.setAdapter(ethereumTransactionAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TransactionItem transactionItem = list.get(position);

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
                gasLimit.setText(String.valueOf(transactionItem.gasLimit));
                gasUsed.setText(String.valueOf(transactionItem.gasUsed));
                gasPrice.setText(transactionItem.gasPrice);

                adb.setView(view).create().show();
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        if (ethereumTransactionAdapter.list.size() >0) {
            historyText.setVisibility(View.INVISIBLE);
        }

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
//        Utils.setActionBarWithTitle(getActivity(), "");
//        Utils.setArrowBackInToolbar(getActivity());
        Utils.hideBottomBar(getActivity());
        getBalance();
    }

    private void getBalance() {
        Utils.stageQueue.postRunnable(() -> {
            BigDecimal walletBalance = Ethereum.getInstance().getBalance();
            if (walletBalance != null) {
                User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE = walletBalance.toString();
                User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS = Ethereum.getInstance().getAddress();
                User.CLIENT_MONEY_ETHEREUM_WALLET_PRIVATE_ADDRESS = Ethereum.getInstance().getPrivateKey();
                User.saveConfig();
            } else {
                ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, R.string.the_balance_of_the_ethereum_wallet_could_not_be_updated, Toast.LENGTH_LONG).show());
            }
            ApplicationLoader.applicationHandler.post(() -> balance.setText(User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE));
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.wallet_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.restore_backup_wallet:
                DialogFragmentRestoreEthereumWallet.newInstance().show(getActivity().getSupportFragmentManager(), null);
                break;
            case R.id.save_backup_wallet:
                Ethereum.getInstance().backupWallet();
                break;
            case R.id.delete_wallet:
                DialogFragmentDeleteWallet.newInstance("ETH").show(getActivity().getSupportFragmentManager(), null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}
