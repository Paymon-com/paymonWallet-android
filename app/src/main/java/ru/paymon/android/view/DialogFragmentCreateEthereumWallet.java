package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.utils.Utils;

public class DialogFragmentCreateEthereumWallet extends DialogFragment {
    private static DialogFragmentCreateEthereumWallet instance;

    public static DialogFragmentCreateEthereumWallet newInstance() {
        instance = new DialogFragmentCreateEthereumWallet();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_create_eth_wallet, null);

        EditText passwordEditText = (EditText) view.findViewById(R.id.dialog_fragment_create_eth_wallet_pass);
        Button createButton = (Button) view.findViewById(R.id.dialog_fragment_create_eth_wallet_create_button);

        createButton.setOnClickListener((view1) -> {
            String passwordString = passwordEditText.getText().toString();
            if (!passwordString.isEmpty() && passwordString.length() >= 8) {
                Utils.hideKeyboard(view);
                getDialog().cancel();
                Toast.makeText(getActivity(), R.string.create_wallet_dialog_hint, Toast.LENGTH_SHORT).show();
                Utils.stageQueue.postRunnable(() -> {
                    boolean isCreated = Ethereum.getInstance().createWallet(User.currentUser.id, passwordString);
                    if (!isCreated) {
                        ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, "Создать Ethereum кошелек не удалось", Toast.LENGTH_LONG).show());
                    } else {
                        //TODO:отправка кошеля в бд
                        User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD = passwordString;
                        User.saveConfig();
                        ApplicationLoader.applicationHandler.post(() -> {
                            Toast.makeText(ApplicationLoader.applicationContext, "Ethereum кошелек успешно создан", Toast.LENGTH_LONG).show();
                            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.didLoadEthereumWallet);
                        });
                    }
                });
            } else {
                Toast.makeText(ApplicationLoader.applicationContext, "Пароль слишком короткий!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
