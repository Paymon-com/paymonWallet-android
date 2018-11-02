package ru.paymon.android;

import android.app.ActivityManager;
import android.app.Application;
import android.support.annotation.NonNull;

import com.android.volley.RequestQueue;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import ru.paymon.android.models.BtcTransactionItem;
import ru.paymon.android.models.EthereumWallet;
import ru.paymon.android.models.PaymonWallet;

public abstract class AbsWalletApplication extends Application {
    protected ActivityManager activityManager;
    protected Web3j ethereumWeb3j;
    protected Web3j paymonmWeb3j;
    protected RequestQueue ethereumRequestQueue;
    protected RequestQueue paymonRequestQueue;
    protected Credentials ethereumWalletCredentials;
    protected Credentials paymonWalletCredentials;

    public abstract Wallet getBitcoinWallet();
    public abstract RestoreStatus restoreBitcoinWallet(final File file, final String password);
    public abstract boolean backupBitcoinWallet(final String path);
    public abstract Coin getBitcoinBalance(final Wallet.BalanceType balanceType);
    public abstract boolean deleteBitcoinWallet();
    public abstract String getBitcoinPublicAddress();
    public abstract String getBitcoinPrivateAddress();
    public abstract List<String> getAllBitcoinPublicAdresses();
    public abstract List<BtcTransactionItem> getBitcoinTransactionHistory();

    public abstract boolean createEthereumWallet(final String password);
    protected abstract EthereumWallet getEthereumWallet(final String password);
    public abstract RestoreStatus restoreEthereumWallet(final File file, final String password);
    public abstract boolean backupEthereumWallet(final String path);
    public abstract BigInteger getEthereumBalance();
    public abstract boolean deleteEthereumWallet();

    public abstract boolean createPaymonWallet(final String password);
    protected abstract PaymonWallet getPaymonWallet(final String password);
    public abstract RestoreStatus restorePaymonWallet(final File file, final String password);
    public abstract boolean backupPaymonWallet(final String path);
    public abstract BigInteger getPaymonBalance();
    public abstract BigInteger getPaymonEthBalance();
    public abstract boolean deletePaymonWallet();


    public abstract EthSendTransaction sendRawEthereumTx(final @NonNull String recipientAddress, final  @NonNull BigInteger ethAmount, final @NonNull BigInteger gasPrice, final @NonNull BigInteger gasLimit);
    public abstract TransactionReceipt sendPmntContract(final @NonNull String recipientAddress, final  @NonNull BigInteger pmntAmount, final @NonNull BigInteger gasPrice, final @NonNull BigInteger gasLimit);
    public abstract Transaction sendBitcoinTx(final String destinationAddress, final long satoshis, final long feePerB);


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
