package ru.paymon.android.view.money.bitcoin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.navigation.Navigation;
import ru.paymon.android.R;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.utils.ItemClickSupport;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.money.DialogFragmentBackupWallet;
import ru.paymon.android.view.money.DialogFragmentPrivateKey;
import ru.paymon.android.view.money.DialogFragmentPublicKey;
import ru.paymon.android.view.money.DialogFragmentRestoreWallet;

import static ru.paymon.android.view.money.FragmentMoney.CURRENCY_KEY;

public class FragmentBitcoinWallet extends Fragment {
    public static final String BTC_CURRENCY_VALUE = "BTC";
    //    private BitcoinTransactionAdapter bitcoinTransactionAdapter;
    private TextView balance;
    private WalletApplication application;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = ((WalletApplication) getActivity().getApplication());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bitcoin_wallet, container, false);

        ImageButton deposit = (ImageButton) view.findViewById(R.id.fragment_bitcoin_wallet_deposit_button);
        ImageButton transfer = (ImageButton) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_button);
        ImageButton withdraw = (ImageButton) view.findViewById(R.id.fragment_bitcoin_wallet_withdraw_button);
        ImageButton backBtn = (ImageButton) view.findViewById(R.id.toolbar_bitcoin_wallet_back_btn);
        ImageButton restoreBtn = (ImageButton) view.findViewById(R.id.toolbar_bitcoin_wallet_restore_btn);
        ImageButton backupBtn = (ImageButton) view.findViewById(R.id.toolbar_bitcoin_wallet_backup_btn);
//        ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.toolbar_bitcoin_wallet_delete_btn);
        Button privateKey = (Button) view.findViewById(R.id.fragment_bitcoin_wallet_private_key_button);
        Button publicKey = (Button) view.findViewById(R.id.fragment_bitcoin_wallet_public_key_button);
        TextView historyText = (TextView) view.findViewById(R.id.history_transaction_is_empty);
        RecyclerView transactionsRecView = (RecyclerView) view.findViewById(R.id.history_transaction_recycler_view);
        balance = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_balance);

        Bundle bundle = new Bundle();
        bundle.putString(CURRENCY_KEY, BTC_CURRENCY_VALUE);

        backBtn.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        restoreBtn.setOnClickListener(v -> new DialogFragmentRestoreWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
        backupBtn.setOnClickListener(v -> new DialogFragmentBackupWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
//        deleteBtn.setOnClickListener(v -> new DialogFragmentDeleteWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
//        deposit.setOnClickListener(view1 -> Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentEthereumDeposit.newInstance(), null));
        transfer.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentBitcoinWalletTransfer));
//        withdraw.setOnClickListener(view1 -> Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentEthereumWidthdraw.newInstance(), null));
        privateKey.setOnClickListener(view1 -> new DialogFragmentPrivateKey().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
        publicKey.setOnClickListener(view1 -> new DialogFragmentPublicKey().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        transactionsRecView.setLayoutManager(linearLayoutManager);
        transactionsRecView.setHasFixedSize(true);

        ItemClickSupport.addTo(transactionsRecView).setOnItemClickListener((recyclerView, position, v) -> {
//                TransactionItem transactionItem = ethereumTransactionAdapter.transactionItems.get(position);
//
//                AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
//
//                view = (ConstraintLayout) getLayoutInflater().inflate(R.layout.alert_dialog_custom_transaction_info, null);
//
//                TextView hash = (TextView) view.findViewById(R.id.ethereum_transaction_hash_alert);
//                TextView status = (TextView) view.findViewById(R.id.ethereum_transaction_status_alert);
//                TextView time = (TextView) view.findViewById(R.id.ethereum_transaction_time_alert);
//                TextView value = (TextView) view.findViewById(R.id.ethereum_transaction_value_alert);
//                TextView to = (TextView) view.findViewById(R.id.ethereum_transaction_to_alert);
//                TextView from = (TextView) view.findViewById(R.id.ethereum_transaction_from_alert);
//                TextView gasLimit = (TextView) view.findViewById(R.id.ethereum_transaction_gas_limit_alert);
//                TextView gasUsed = (TextView) view.findViewById(R.id.ethereum_transaction_gas_used_alert);
//                TextView gasPrice = (TextView) view.findViewById(R.id.ethereum_transaction_gas_price_alert);
//
//                hash.setText(transactionItem.hash);
//                status.setText(transactionItem.status);
//                time.setText(transactionItem.time);
//                value.setText(transactionItem.value);
//                to.setText(transactionItem.to);
//                from.setText(transactionItem.from);
//                gasLimit.setText(transactionItem.gasLimit);
//                gasUsed.setText(transactionItem.gasUsed);
//                gasPrice.setText(transactionItem.gasPrice);
//
//                adb.setView(view).create().show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
        balance.setText(application.getBitcoinBalance().toFriendlyString());
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
        animation.setDuration(700);
        balance.startAnimation(animation);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
