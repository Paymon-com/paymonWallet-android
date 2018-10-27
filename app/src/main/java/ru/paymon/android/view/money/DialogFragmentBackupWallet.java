package ru.paymon.android.view.money;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fragment_backup_wallet, null);

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

        final Button openExplorerButton = (Button) view.findViewById(R.id.dialog_fragment_backup_wallet_from_explorer_button);
        final Button backupButton = (Button) view.findViewById(R.id.dialog_fragment_backup_wallet_import_button);

        final String[] paths = new String[1];

        openExplorerButton.setOnClickListener((view1) -> {
            AlertDialogOpenFile fileDialog = new AlertDialogOpenFile(getContext())
                    .setOnlyFoldersFilter()
                    .setOpenDialogListener((fileName) -> {
                        paths[0] = fileName;
                    });
            fileDialog.show();
        });

        backupButton.setOnClickListener((view1 -> {
            final String path = paths[0];

            if (path == null) {
                Toast.makeText(getContext(), R.string.backup_file_not_selected, Toast.LENGTH_LONG).show();
                return;
            }

            Utils.hideKeyboard(view);
            getDialog().dismiss();
            getDialog().cancel();

            switch (currency) {
                case BTC_CURRENCY_VALUE:
                    backupBitcoinWallet(path);
                    break;
                case ETH_CURRENCY_VALUE:
                    backupEthereumWallet(path);
                    break;
                case PMNT_CURRENCY_VALUE:
                    backupPaymonWallet(path);
                    break;
            }
        }));

        return view;
    }

    private void backupBitcoinWallet(final String path) {
        WalletApplication application = ((WalletApplication) getActivity().getApplication());
        boolean isBackuped = application.backupBitcoinWallet(path);
        if (isBackuped)
            Toast.makeText(getContext(), "Резервная копия создана", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getContext(), "Резервную копию создать не удалось", Toast.LENGTH_LONG).show();
    }

    private void backupEthereumWallet(final String path) {
        WalletApplication application = ((WalletApplication) getActivity().getApplication());
        boolean isBackuped = application.backupEthereumWallet(path);
        if (isBackuped)
            Toast.makeText(getContext(), "Резервная копия создана", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getContext(), "Резервную копию создать не удалось", Toast.LENGTH_LONG).show();
    }

    private void backupPaymonWallet(final String path) {
        WalletApplication application = ((WalletApplication) getActivity().getApplication());
        boolean isBackuped = application.backupPaymonWallet(path);
        if (isBackuped)
            Toast.makeText(getContext(), "Резервная копия создана", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(getContext(), "Резервную копию создать не удалось", Toast.LENGTH_LONG).show();
    }
}
