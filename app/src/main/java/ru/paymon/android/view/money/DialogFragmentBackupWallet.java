package ru.paymon.android.view.money;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.paymon.android.R;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.AlertDialogOpenFile;

import static ru.paymon.android.view.money.FragmentMoney.CURRENCY_KEY;
import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class DialogFragmentBackupWallet extends DialogFragment {
    private String currency;
    private WalletApplication application;

    public DialogFragmentBackupWallet setArgs(Bundle bundle) {
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
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_backup_wallet, null);

        final ConstraintLayout openExplorerButton = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_backup_wallet_from_explorer_button);
        final ConstraintLayout backupButton = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_backup_wallet_import_button);
        final EditText passwordEditText = (EditText) view.findViewById(R.id.dialog_fragment_backup_wallet_pass);
        final TextView path = (TextView) view.findViewById(R.id.dialog_fragment_backup_wallet_path);
        final CheckBox showPass = (CheckBox) view.findViewById(R.id.dialog_fragment_backup_wallet_check);

        showPass.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            final TransformationMethod transformationMethod = isChecked ? null : PasswordTransformationMethod.getInstance();
            passwordEditText.setTransformationMethod(transformationMethod);
        });

        final String[] filePath = new String[1];
        path.setVisibility(View.GONE);

        openExplorerButton.setOnClickListener((view1) -> {
            AlertDialogOpenFile fileDialog = new AlertDialogOpenFile(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom))
                    .setOnlyFoldersFilter()
                    .setOpenDialogListener((fileName) -> {
                        filePath[0] = fileName;
                        path.setVisibility(View.VISIBLE);
                        path.setText(fileName);
                    });
            fileDialog.show();
        });

        backupButton.setOnClickListener((view1 -> {
            if (filePath[0] == null) {
                Toast.makeText(getContext(), R.string.backup_file_not_selected, Toast.LENGTH_LONG).show();
                return;
            }

            Utils.hideKeyboard(view);
            getDialog().dismiss();
            getDialog().cancel();
            final String password = passwordEditText.getText().toString();

            switch (currency) {
                case BTC_CURRENCY_VALUE:
                    backupBitcoinWallet(filePath[0], password);
                    break;
                case ETH_CURRENCY_VALUE:
                    backupEthereumWallet(filePath[0], password);
                    break;
                case PMNT_CURRENCY_VALUE:
                    backupPaymonWallet(filePath[0], password);
                    break;
            }
        }));

        return view;
    }

    private void backupBitcoinWallet(final String path, final String password) {
        boolean isBackuped = application.backupBitcoinWallet(path, password);
        if (isBackuped)
            Toast.makeText(getContext(), R.string.backup_created, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getContext(), R.string.backup_failed, Toast.LENGTH_LONG).show();
    }

    private void backupEthereumWallet(final String path, final String password) {
        boolean isBackuped = application.backupEthereumWallet(path, password);
        if (isBackuped)
            Toast.makeText(getContext(), R.string.backup_created, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getContext(), R.string.backup_failed, Toast.LENGTH_LONG).show();
    }

    private void backupPaymonWallet(final String path, final String password) {
        boolean isBackuped = application.backupPaymonWallet(path, password);
        if (isBackuped)
            Toast.makeText(getContext(), R.string.backup_created, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getContext(), R.string.backup_failed, Toast.LENGTH_LONG).show();
    }
}
