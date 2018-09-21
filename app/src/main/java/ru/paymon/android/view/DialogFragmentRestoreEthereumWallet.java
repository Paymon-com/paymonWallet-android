package ru.paymon.android.view;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.utils.Utils;

public class DialogFragmentRestoreEthereumWallet extends DialogFragment {
    private static DialogFragmentRestoreEthereumWallet instance;

    public static DialogFragmentRestoreEthereumWallet newInstance() {
        instance = new DialogFragmentRestoreEthereumWallet();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_restore_eth_wallet, null);
        view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.eth_color));

        final EditText passwordEditText = (EditText) view.findViewById(R.id.dialog_fragment_restore_eth_wallet_pass);
        final Button openExplorerButton = (Button) view.findViewById(R.id.dialog_fragment_restore_eth_wallet_from_explorer_button);
        final Button restoreButton = (Button) view.findViewById(R.id.dialog_fragment_restore_eth_wallet_import_button);
        final TextView path = (TextView) view.findViewById(R.id.dialog_fragment_restore_eth_wallet_path);
        final CheckBox showPass = (CheckBox) view.findViewById(R.id.dialog_fragment_restore_eth_wallet_check);

        showPass.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            final TransformationMethod transformationMethod = isChecked ? null : PasswordTransformationMethod.getInstance();
            passwordEditText.setTransformationMethod(transformationMethod);
        });

        final String[] filePath = new String[1];
        path.setVisibility(View.GONE);

        openExplorerButton.setOnClickListener((view1) -> {
            AlertDialogOpenFile fileDialog = new AlertDialogOpenFile(getContext())
                    .setFilter(".*\\.json")
                    .setOpenDialogListener((fileName) -> {
                        path.setVisibility(View.VISIBLE);
                        path.setText(fileName);
                        filePath[0] = fileName;
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
            getDialog().dismiss();

            final File file = new File(filePath[0]);

            Toast.makeText(ApplicationLoader.applicationContext, R.string.ethereum_wallet_is_loaded_hint, Toast.LENGTH_SHORT).show();
            passwordEditText.setText(null);
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            final InputStream finalInputStream = inputStream;

            getDialog().cancel();
            Utils.stageQueue.postRunnable(() -> {
                Ethereum.RestoreStatus restoreStatus = Ethereum.getInstance().restoreWallet(finalInputStream, password);
                ApplicationLoader.applicationHandler.post(() -> {
                    switch (restoreStatus) {
                        case DONE:
                            User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD = password;
                            User.saveConfig();
//                            Ethereum.getInstance().ethereumWalletNotification(getContext(), ApplicationLoader.applicationContext.getString(R.string.restore_wallet_success), ApplicationLoader.applicationContext.getString(R.string.title_ethereum_wallet));
                            break;
                        case NO_USER_ID:
//                            Ethereum.getInstance().ethereumWalletNotification(getContext(), ApplicationLoader.applicationContext.getString(R.string.restore_wallet_not_success_error_file), ApplicationLoader.applicationContext.getString(R.string.title_ethereum_wallet));
                            break;
                        case ERROR_CREATE_FILE:
//                            Ethereum.getInstance().ethereumWalletNotification(getContext(), ApplicationLoader.applicationContext.getString(R.string.wrong_password), ApplicationLoader.applicationContext.getString(R.string.title_ethereum_wallet));
                            break;
                        case ERROR_DECRYPTING_WRONG_PASS:
//                            Ethereum.getInstance().ethereumWalletNotification(getContext(), ApplicationLoader.applicationContext.getString(R.string.restore_wallet_not_success), ApplicationLoader.applicationContext.getString(R.string.title_ethereum_wallet));
                            break;
                    }
                });
            });
        }));

        return view;
    }
}
