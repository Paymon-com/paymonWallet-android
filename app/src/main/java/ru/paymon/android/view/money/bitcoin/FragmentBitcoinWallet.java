package ru.paymon.android.view.money.bitcoin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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

import org.bitcoinj.wallet.Wallet;

import java.util.ArrayList;
import java.util.List;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.adapters.TransactionAdapter;
import ru.paymon.android.models.BtcTransactionItem;
import ru.paymon.android.utils.ItemClickSupport;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.money.DialogFragmentBackupWallet;
import ru.paymon.android.view.money.DialogFragmentDeleteWallet;
import ru.paymon.android.view.money.DialogFragmentPrivateKey;
import ru.paymon.android.view.money.DialogFragmentPublicKey;
import ru.paymon.android.view.money.DialogFragmentRestoreWallet;

import static ru.paymon.android.view.money.FragmentMoney.CURRENCY_KEY;

public class FragmentBitcoinWallet extends Fragment implements NotificationManager.IListener {
    public static final String BTC_CURRENCY_VALUE = "BTC";
    private TransactionAdapter transactionAdapter;
    private TextView balanceTextView;
    private TextView estimatedBalanceTextView;
    private WalletApplication application;
    private RecyclerView transactionsRecView;
    private TextView historyText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = ((WalletApplication) getActivity().getApplication());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bitcoin_wallet, container, false);

        balanceTextView = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_balance);
        estimatedBalanceTextView = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_balance_estimated);
        transactionsRecView = (RecyclerView) view.findViewById(R.id.history_transaction_recycler_view);
        historyText = (TextView) view.findViewById(R.id.history_transaction_is_empty);
//        ImageButton deposit = (ImageButton) view.findViewById(R.id.fragment_bitcoin_wallet_deposit_button);
        ImageButton transfer = (ImageButton) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_button);
//        ImageButton withdraw = (ImageButton) view.findViewById(R.id.fragment_bitcoin_wallet_withdraw_button);
        ImageButton backBtn = (ImageButton) view.findViewById(R.id.toolbar_bitcoin_wallet_back_btn);
        ImageButton restoreBtn = (ImageButton) view.findViewById(R.id.toolbar_bitcoin_wallet_restore_btn);
        ImageButton backupBtn = (ImageButton) view.findViewById(R.id.toolbar_bitcoin_wallet_backup_btn);
        ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.toolbar_bitcoin_wallet_delete_btn);
        Button privateKey = (Button) view.findViewById(R.id.fragment_bitcoin_wallet_private_key_button);
        Button publicKey = (Button) view.findViewById(R.id.fragment_bitcoin_wallet_public_key_button);

        Bundle bundle = new Bundle();
        bundle.putString(CURRENCY_KEY, BTC_CURRENCY_VALUE);

        backBtn.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        restoreBtn.setOnClickListener(v -> new DialogFragmentRestoreWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
        backupBtn.setOnClickListener(v -> new DialogFragmentBackupWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
        deleteBtn.setOnClickListener(v -> new DialogFragmentDeleteWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
//        deposit.setOnClickListener(view1 -> Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentEthereumDeposit.newInstance(), null));
        transfer.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentBitcoinWalletTransfer));
//        withdraw.setOnClickListener(view1 -> Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentEthereumWidthdraw.newInstance(), null));
        privateKey.setOnClickListener(view1 -> new DialogFragmentPrivateKey().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
        publicKey.setOnClickListener(view1 -> new DialogFragmentPublicKey().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        transactionsRecView.setLayoutManager(linearLayoutManager);
        transactionsRecView.setHasFixedSize(true);

        ItemClickSupport.addTo(transactionsRecView).setOnItemClickListener((recyclerView, position, v) -> {
            final BtcTransactionItem transactionItem = (BtcTransactionItem) transactionAdapter.transactionItems.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMessage("Открыть подробней?")
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.other_ok), (dialogInterface, i) -> {
                        final String url = Config.DEBUG ? "https://live.blockcypher.com/btc-testnet/tx/" + transactionItem.hash: "https://live.blockcypher.com/btc/tx/" + transactionItem.hash;
                        final Uri uri = Uri.parse(url);
                        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(browserIntent);
                    })
                    .setNegativeButton(getString(R.string.other_cancel), (dialogInterface, i) -> {
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_FINISHED);
        Utils.hideBottomBar(getActivity());
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
        final String balance = application.getBitcoinBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).toFriendlyString();
        final String balanceEstimated = application.getBitcoinBalance(Wallet.BalanceType.ESTIMATED).toFriendlyString();
        balanceTextView.setText(balance);
        estimatedBalanceTextView.setText(balanceEstimated);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
        animation.setDuration(700);
        balanceTextView.startAnimation(animation);
        estimatedBalanceTextView.startAnimation(animation);
        List<BtcTransactionItem> transactions = application.getBitcoinTransactionHistory();
        transactionAdapter = new TransactionAdapter(new ArrayList<>(transactions));
        transactionsRecView.setAdapter(transactionAdapter);
        if (transactionAdapter.transactionItems.size() > 0)
            historyText.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_FINISHED);
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_SYNC_FINISHED) {
            ApplicationLoader.applicationHandler.post(() -> {
                final String balance = application.getBitcoinBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE).toFriendlyString();
                final String balanceEstimated = application.getBitcoinBalance(Wallet.BalanceType.ESTIMATED).toFriendlyString();
                balanceTextView.setText(balance);
                estimatedBalanceTextView.setText(balanceEstimated);
            });
        }
    }
}
