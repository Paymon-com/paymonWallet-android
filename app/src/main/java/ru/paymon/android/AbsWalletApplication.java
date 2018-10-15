package ru.paymon.android;

import android.app.ActivityManager;
import android.app.Application;
import android.support.annotation.NonNull;

import com.android.volley.RequestQueue;

import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;

import ru.paymon.android.gateway.ethereum.EthereumWallet;

public abstract class AbsWalletApplication extends Application {
    protected ActivityManager activityManager;
    protected File walletFile;
    protected WalletFiles walletFiles;
    protected Web3j web3j;
    protected RequestQueue requestQueue;
    protected Credentials walletCredentials;
    protected static final BigDecimal TEN_POW_18 = new BigDecimal("1000000000000000000");
    protected static final BigDecimal TEN_POW_9 = new BigDecimal("1000000000");

    public abstract boolean createBitcoinWallet();
    public abstract Wallet getBitcoinWallet();
    public abstract Wallet backupBitcoinWallet();
    public abstract Wallet restoreBitcoinWallet();
    public abstract boolean deleteBitcoinWallet();

    public abstract boolean createEthereumWallet(final String password);
    public abstract EthereumWallet getEthereumWallet(final String password);
    public abstract RestoreStatus restoreEthereumWallet(final File file, final String password);
    public abstract boolean backupEthereumWallet(final String path);
    public abstract BigInteger getEthereumBalance();
    public abstract boolean deleteEthereumWallet();
    public abstract String convertEthereumToFiat(final String ethAmount,final float fiatExRate);

    public abstract Wallet createPaymonWallet();
    public abstract Wallet getPaymonWallet();
    public abstract Wallet restorePaymonWallet();
    public abstract Wallet backupPaymonWallet();
    public abstract boolean deletePaymonWallet();

    protected abstract void setBitcoinWalletListeners();

    public abstract EthSendTransaction sendRawEthereumTx(final @NonNull String recipientAddress, final  @NonNull BigInteger ethAmount, final @NonNull BigInteger gasPrise, final @NonNull BigInteger gasLimit);

    public enum RestoreStatus {
        NO_USER_ID,
        ERROR_CREATE_FILE,
        ERROR_DECRYPTING_WRONG_PASS,
        DONE
    }

    public enum TX_STATUS {
        DONE,
        NOT_HAVE_MONEY,
        UNEXPECTED_ERROR,
        CREDENTIALS_NULL,
        ERROR_GET_NONCE,
        MB_NOT_HAVE_MONEY
    }
}
