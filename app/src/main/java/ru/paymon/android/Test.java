package ru.paymon.android;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletExtension;
import org.bitcoinj.wallet.WalletProtobufSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import ru.paymon.android.utils.Utils;

public class Test extends WalletAppKit {
    public Test(NetworkParameters params, File directory, String filePrefix) {
        super(params, directory, filePrefix);
    }

    public Test(Context context, File directory, String filePrefix) {
        super(context, directory, filePrefix);
    }

    public Wallet restoreWallet(final Wallet wallet) throws Exception {
        wallet.saveToFile(vWalletFile);
        startUp();
        return vWallet;
    }
}
