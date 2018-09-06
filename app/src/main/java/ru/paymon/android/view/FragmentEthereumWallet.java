package ru.paymon.android.view;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.utils.Utils;

public class FragmentEthereumWallet extends Fragment {
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
        balance = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_balance);


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
            BigInteger walletBalance = Ethereum.getInstance().getBalance();
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
