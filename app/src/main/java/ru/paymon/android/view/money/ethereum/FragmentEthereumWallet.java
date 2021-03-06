package ru.paymon.android.view.money.ethereum;

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
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import androidx.navigation.Navigation;
import ru.paymon.android.R;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.adapters.TransactionAdapter;
import ru.paymon.android.models.EthTransactionItem;
import ru.paymon.android.utils.ItemClickSupport;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.money.DialogFragmentBackupWallet;
import ru.paymon.android.view.money.DialogFragmentDeleteWallet;
import ru.paymon.android.view.money.DialogFragmentPrivateKey;
import ru.paymon.android.view.money.DialogFragmentPublicKey;
import ru.paymon.android.view.money.DialogFragmentRestoreWallet;
import ru.paymon.android.viewmodels.MoneyViewModel;

import static ru.paymon.android.view.money.FragmentMoney.CURRENCY_KEY;

public class FragmentEthereumWallet extends Fragment {
    public static final String ETH_CURRENCY_VALUE = "ETH";
    private MoneyViewModel moneyViewModel;
    private LiveData<BigInteger> balanceLiveData;
    private LiveData<ArrayList<EthTransactionItem>> transactionsData;
    private TransactionAdapter transactionAdapter;
    private TextView balanceTextView;
    private TextView historyText;
    private RecyclerView transactionsRecView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moneyViewModel = ViewModelProviders.of(getActivity()).get(MoneyViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ethereum_wallet, container, false);

        balanceTextView = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_balance);
        historyText = (TextView) view.findViewById(R.id.history_transaction_is_empty);
        transactionsRecView = (RecyclerView) view.findViewById(R.id.history_transaction_recycler_view);
//        ImageButton deposit = (ImageButton) view.findViewById(R.id.fragment_ethereum_wallet_deposit_button);
        ImageButton transfer = (ImageButton) view.findViewById(R.id.fragment_ethereum_wallet_transfer_button);
//        ImageButton withdraw = (ImageButton) view.findViewById(R.id.fragment_ethereum_wallet_withdraw_button);
        ImageButton backBtn = (ImageButton) view.findViewById(R.id.toolbar_ethereum_wallet_back_btn);
        ImageButton restoreBtn = (ImageButton) view.findViewById(R.id.toolbar_ethereum_wallet_restore_btn);
        ImageButton backupBtn = (ImageButton) view.findViewById(R.id.toolbar_ethereum_wallet_backup_btn);
        ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.toolbar_ethereum_wallet_delete_btn);
        Button privateKey = (Button) view.findViewById(R.id.fragment_ethereum_wallet_private_key_button);
        Button publicKey = (Button) view.findViewById(R.id.fragment_ethereum_wallet_public_key_button);

        Bundle bundle = new Bundle();
        bundle.putString(CURRENCY_KEY, ETH_CURRENCY_VALUE);

        backBtn.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        restoreBtn.setOnClickListener(v -> new DialogFragmentRestoreWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
        backupBtn.setOnClickListener(v -> new DialogFragmentBackupWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
        deleteBtn.setOnClickListener(v -> new DialogFragmentDeleteWallet().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
//        deposit.setOnClickListener(view1 -> Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentEthereumDeposit.newInstance(), null));
        transfer.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentEthereumWalletTransfer));
//        withdraw.setOnClickListener(view1 -> Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentEthereumWidthdraw.newInstance(), null));
        privateKey.setOnClickListener(view1 -> new DialogFragmentPrivateKey().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));
        publicKey.setOnClickListener(view1 -> new DialogFragmentPublicKey().setArgs(bundle).show(getActivity().getSupportFragmentManager(), null));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        transactionsRecView.setLayoutManager(linearLayoutManager);
        transactionsRecView.setHasFixedSize(true);

        ItemClickSupport.addTo(transactionsRecView).setOnItemClickListener((recyclerView, position, v) -> {
            EthTransactionItem transactionItem = (EthTransactionItem) transactionAdapter.transactionItems.get(position);

            AlertDialog.Builder adb = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom));

            View dialogView = (ConstraintLayout) getLayoutInflater().inflate(R.layout.alert_dialog_custom_transaction_info, null);

            TextView hash = (TextView) dialogView.findViewById(R.id.ethereum_transaction_hash_alert);
            TextView status = (TextView) dialogView.findViewById(R.id.ethereum_transaction_status_alert);
            TextView time = (TextView) dialogView.findViewById(R.id.ethereum_transaction_time_alert);
            TextView value = (TextView) dialogView.findViewById(R.id.ethereum_transaction_value_alert);
            TextView to = (TextView) dialogView.findViewById(R.id.ethereum_transaction_to_alert);
            TextView from = (TextView) dialogView.findViewById(R.id.ethereum_transaction_from_alert);
            TextView gasLimit = (TextView) dialogView.findViewById(R.id.ethereum_transaction_gas_limit_alert);
            TextView gasUsed = (TextView) dialogView.findViewById(R.id.ethereum_transaction_gas_used_alert);
            TextView gasPrice = (TextView) dialogView.findViewById(R.id.ethereum_transaction_gas_price_alert);

            hash.setText(transactionItem.hash);
            status.setText(transactionItem.status);
            time.setText(transactionItem.time);
            value.setText(transactionItem.value);
            to.setText(transactionItem.to);
            from.setText(transactionItem.from);
            gasLimit.setText(transactionItem.gasLimit);
            gasUsed.setText(transactionItem.gasUsed);
            gasPrice.setText(transactionItem.gasPrice);

            adb.setView(dialogView).create().show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
        balanceLiveData = moneyViewModel.getEthereumBalanceData();
        transactionsData = moneyViewModel.getEthereumTranscationsData();
        balanceLiveData.observe(this, value -> {
            if (value != null) {
                final String balance = Convert.fromWei(new BigDecimal(value), Convert.Unit.ETHER).toString();
                balanceTextView.setText(String.format("%s ETH", balance));
            } else {
                balanceTextView.setText("0 ETH");
            }
        });

        transactionsData.observe(this, (transactionItems) -> {
            if (transactionItems == null) return;
            transactionAdapter = new TransactionAdapter(new ArrayList<>(transactionItems), ((WalletApplication) getActivity().getApplication()).getEthereumWallet().publicAddress);
            transactionsRecView.setAdapter(transactionAdapter);
            if (transactionAdapter.transactionItems.size() > 0)
                historyText.setVisibility(View.INVISIBLE);
        });
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left);
        animation.setDuration(700);
        balanceTextView.startAnimation(animation);
    }

    @Override
    public void onPause() {
        super.onPause();
        balanceLiveData.removeObservers(this);
        transactionsData.removeObservers(this);
    }
}
