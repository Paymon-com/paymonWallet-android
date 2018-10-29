package ru.paymon.android;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Wallet;

import java.io.File;

public class WalletKit extends WalletAppKit {
    public WalletKit(NetworkParameters params, File directory, String filePrefix) {
        super(params, directory, filePrefix);
    }

    public WalletKit(Context context, File directory, String filePrefix) {
        super(context, directory, filePrefix);
    }

    public Wallet restoreWallet(final Wallet wallet) throws Exception {
        shutDown();
        wallet.cleanup();
        wallet.saveToFile(vWalletFile);
        startUp();
        return vWallet;
    }
}
