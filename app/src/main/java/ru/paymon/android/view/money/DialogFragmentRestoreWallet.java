package ru.paymon.android.view.money;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import ru.paymon.android.AbsWalletApplication;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.AlertDialogOpenFile;

import static ru.paymon.android.view.money.FragmentMoney.CURRENCY_KEY;
import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class DialogFragmentRestoreWallet extends DialogFragment {
    private String currency;

    public DialogFragmentRestoreWallet setArgs(Bundle bundle) {
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
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_restore_wallet, null);

        switch (currency) {
            case BTC_CURRENCY_VALUE:
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.btc_color));
                break;
            case ETH_CURRENCY_VALUE:
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.eth_color));
                break;
            case PMNT_CURRENCY_VALUE:
                view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.pmnt_color));
                break;
        }

        final EditText passwordEditText = (EditText) view.findViewById(R.id.dialog_fragment_restore_wallet_pass);
        final Button openExplorerButton = (Button) view.findViewById(R.id.dialog_fragment_restore_wallet_from_explorer_button);
        final Button restoreButton = (Button) view.findViewById(R.id.dialog_fragment_restore_wallet_import_button);
        final TextView path = (TextView) view.findViewById(R.id.dialog_fragment_restore_wallet_path);
        final CheckBox showPass = (CheckBox) view.findViewById(R.id.dialog_fragment_restore_wallet_check);

        showPass.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            final TransformationMethod transformationMethod = isChecked ? null : PasswordTransformationMethod.getInstance();
            passwordEditText.setTransformationMethod(transformationMethod);
        });

        final String[] filePath = new String[1];
        path.setVisibility(View.GONE);

        openExplorerButton.setOnClickListener((view1) -> {
            AlertDialogOpenFile fileDialog = new AlertDialogOpenFile(getContext())
//                    .setFilter(".*\\.json")
                    .setOpenDialogListener((fileName) -> {
                        filePath[0] = fileName;
                        path.setVisibility(View.VISIBLE);
                        path.setText(fileName);
                    });
            fileDialog.show();
        });

        restoreButton.setOnClickListener((view1 -> {
            String password = passwordEditText.getText().toString().trim();
            if (password.isEmpty() || password.length() < 8) {
                Toast.makeText(getContext(), R.string.reg_check_password_length, Toast.LENGTH_LONG).show();
                return;
            }

            if (filePath[0] == null) {
                Toast.makeText(getContext(), R.string.backup_file_not_selected, Toast.LENGTH_LONG).show();
                return;
            }

            Utils.hideKeyboard(view);

            final File file = new File(filePath[0]);

            Toast.makeText(ApplicationLoader.applicationContext, R.string.ethereum_wallet_is_loaded_hint, Toast.LENGTH_SHORT).show();
            passwordEditText.setText(null);

            getDialog().cancel();
            switch (currency) {
                case BTC_CURRENCY_VALUE:
                    restoreBitcoinWallet(file, password);
                    break;
                case ETH_CURRENCY_VALUE:
                    restoreEthereumWallet(file, password);
                    break;
                case PMNT_CURRENCY_VALUE:
                    restorePaymonWallet(file, password);
                    break;

            }
        }));

        return view;
    }

    private void restoreEthereumWallet(final File file, final String password) {
        Utils.stageQueue.postRunnable(() -> {
            AbsWalletApplication.RestoreStatus restoreStatus = ((WalletApplication) getActivity().getApplication()).restoreEthereumWallet(file, password);
            ApplicationLoader.applicationHandler.post(() -> {
                switch (restoreStatus) {
                    case DONE:
                        User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD = password;
                        User.saveConfig();
                        NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.ETHEREUM_WALLET_CREATED);
//                        getDialog().dismiss();
                        break;
                    case NO_USER_ID:
                        break;
                    case ERROR_CREATE_FILE:
                        break;
                    case ERROR_DECRYPTING_WRONG_PASS:
                        break;
                }
            });
        });
    }

    private void restoreBitcoinWallet(final File file, final String password) {
        Utils.stageQueue.postRunnable(() -> {
            AbsWalletApplication.RestoreStatus restoreStatus = ((WalletApplication) getActivity().getApplication()).restoreBitcoinWallet(file, password);
            ApplicationLoader.applicationHandler.post(() -> {
                switch (restoreStatus) {
                    case DONE:
                        User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD = password;
                        User.saveConfig();
                        NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.BITCOIN_WALLET_CREATED);
                        Toast.makeText(ApplicationLoader.applicationContext, R.string.ok, Toast.LENGTH_LONG).show();
//                        getDialog().dismiss();
                        break;
                    case NO_USER_ID:
                        Toast.makeText(ApplicationLoader.applicationContext, R.string.fail, Toast.LENGTH_LONG).show();
                        break;
                    case ERROR_CREATE_FILE:
                        Toast.makeText(ApplicationLoader.applicationContext, R.string.fail, Toast.LENGTH_LONG).show();
                        break;
                    case ERROR_DECRYPTING_WRONG_PASS:
                        Toast.makeText(ApplicationLoader.applicationContext, R.string.fail, Toast.LENGTH_LONG).show();
                        break;
                }
            });
        });
    }

    private void restorePaymonWallet(final File file, final String password) {
        Utils.stageQueue.postRunnable(() -> {
            AbsWalletApplication.RestoreStatus restoreStatus = ((WalletApplication) getActivity().getApplication()).restorePaymonWallet(file, password);
            ApplicationLoader.applicationHandler.post(() -> {
                switch (restoreStatus) {
                    case DONE:
                        User.CLIENT_MONEY_PAYMON_WALLET_PASSWORD = password;
                        User.saveConfig();
                        NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.PAYMON_WALLET_CREATED);
//                        getDialog().dismiss();
                        break;
                    case NO_USER_ID:
                        break;
                    case ERROR_CREATE_FILE:
                        break;
                    case ERROR_DECRYPTING_WRONG_PASS:
                        break;
                }
            });
        });
    }
}
