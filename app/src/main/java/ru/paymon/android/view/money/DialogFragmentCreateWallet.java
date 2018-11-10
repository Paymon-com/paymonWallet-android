package ru.paymon.android.view.money;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.money.FragmentMoney.CURRENCY_KEY;
import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class DialogFragmentCreateWallet extends DialogFragment {
    private String currency;
    private WalletApplication application;

    public DialogFragmentCreateWallet setArgs(Bundle bundle) {
        this.setArguments(bundle);
        return this;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (!bundle.containsKey(CURRENCY_KEY)) return;
            currency = bundle.getString(CURRENCY_KEY);
        }
        application = ((WalletApplication) getActivity().getApplication());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_create_wallet, null);


        EditText passwordEditText = (EditText) view.findViewById(R.id.dialog_fragment_create_wallet_pass);
        ConstraintLayout createButton = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_create_wallet_create_button);

        createButton.setOnClickListener((view1) -> {
            final String password = passwordEditText.getText().toString();

            switch (currency) {
                case BTC_CURRENCY_VALUE:
                    createBitcoinWallet(password);
                    break;
                case ETH_CURRENCY_VALUE:
                    createEthereumWallet(password);
                    break;
                case PMNT_CURRENCY_VALUE:
                    createPaymonWallet(password);
                    break;
            }
        });

        return view;
    }

    private void createEthereumWallet(final String password) {
        if (!password.isEmpty() && password.length() >= 8) {
            getDialog().cancel();
            Toast.makeText(getActivity(), R.string.wallet_create_hint, Toast.LENGTH_SHORT).show();
            Utils.stageQueue.postRunnable(() -> {
                boolean isCreated = application.createEthereumWallet(password);
                if (!isCreated) {
                    ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, R.string.other_fail, Toast.LENGTH_LONG).show());
                } else {
                    User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD = password;
                    User.saveConfig();
                    ApplicationLoader.applicationHandler.post(() -> {
                        Toast.makeText(ApplicationLoader.applicationContext, R.string.other_success, Toast.LENGTH_LONG).show();
                        NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.ETHEREUM_WALLET_CREATED);
                    });
                }
            });
        } else {
            Toast.makeText(ApplicationLoader.applicationContext, R.string.sign_up_password_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void createBitcoinWallet(final String password) {
        if (!password.isEmpty() && password.length() >= 8) {
            getDialog().cancel();
            Toast.makeText(getActivity(), R.string.wallet_create_hint, Toast.LENGTH_SHORT).show();
            application.startBitcoinKit();
            User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD = password;
            User.saveConfig();
            Toast.makeText(ApplicationLoader.applicationContext, R.string.other_success, Toast.LENGTH_LONG).show();
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.BITCOIN_WALLET_CREATED);
        } else {
            Toast.makeText(ApplicationLoader.applicationContext, R.string.sign_up_password_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void createPaymonWallet(final String password) {
        if (!password.isEmpty() && password.length() >= 8) {
            getDialog().cancel();
            Toast.makeText(getActivity(), R.string.wallet_create_hint, Toast.LENGTH_SHORT).show();
            Utils.stageQueue.postRunnable(() -> {
                boolean isCreated = application.createPaymonWallet(password);
                if (!isCreated) {
                    ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, R.string.other_fail, Toast.LENGTH_LONG).show()); //TODO: fix that string
                } else {
                    User.CLIENT_MONEY_PAYMON_WALLET_PASSWORD = password;
                    User.saveConfig();
                    ApplicationLoader.applicationHandler.post(() -> {
                        Toast.makeText(ApplicationLoader.applicationContext, R.string.other_success, Toast.LENGTH_LONG).show();
                        NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.PAYMON_WALLET_CREATED);
                    });
                }
            });
        } else {
            Toast.makeText(ApplicationLoader.applicationContext, R.string.sign_up_password_error, Toast.LENGTH_SHORT).show();
        }
    }
}
