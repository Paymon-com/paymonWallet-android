package ru.paymon.android;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.toolbox.Volley;

import org.apache.commons.io.IOUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.crypto.LinuxSecureRandom;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import ru.paymon.android.models.EthereumWallet;
import ru.paymon.android.models.PaymonWallet;
import ru.paymon.android.utils.Constants;
import ru.paymon.android.utils.Utils;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;


public class WalletApplication extends AbsWalletApplication {
    public static final int btcTxSize = (148 * 1) + (34 * 2) + 10;
    private static final boolean IS_TEST = true;
    private static final String INFURA_LINK = IS_TEST ? "https://ropsten.infura.io/BAWTZQzsbBDZG6g9D0IP" : "https://mainnet.infura.io/BAWTZQzsbBDZG6g9D0IP";
    private EthereumWallet ethereumWallet;
    private PaymonWallet paymonWallet;
    private String ethereumWalletPath;
    private String paymonWalletPath;
    private WalletAppKit kit;

    @Override
    public void onCreate() {
        new LinuxSecureRandom();

        Threading.throwOnLockCycles();
        org.bitcoinj.core.Context.enableStrictMode();
        org.bitcoinj.core.Context.propagate(Constants.CONTEXT);

        Executors.newSingleThreadExecutor().submit(() -> startBitcoinKit());

        super.onCreate();

        ethereumWalletPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-eth-wallet.json";
        paymonWalletPath = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-pmnt-wallet.json";

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        ethereumWeb3j = Web3jFactory.build(new HttpService(INFURA_LINK));
        ethereumRequestQueue = Volley.newRequestQueue(getApplicationContext());

        paymonmWeb3j = Web3jFactory.build(new HttpService(INFURA_LINK));
        paymonRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public void startBitcoinKit() {
        kit = new WalletAppKit(Constants.NETWORK_PARAMETERS, new File(getCacheDir().getPath()), "walletappkit1-example");

        InputStream checkpoint = CheckpointManager.openStream(Constants.NETWORK_PARAMETERS);
        kit.setCheckpoints(checkpoint);
        kit.setAutoSave(true);
        kit.setBlockingStartup(false);

        Log.e("AAA", "before start");
        kit.startAsync();
        Log.e("AAA", "after start");
        kit.awaitRunning();
        Log.e("AAA", "after await");

        kit.wallet().addCoinsReceivedEventListener((Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) -> {
            Log.e("AAA", "-----> coins resceived: " + tx.getHashAsString());
            Log.e("AAA", "received: " + tx.getValue(wallet));
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED, BTC_CURRENCY_VALUE, kit.wallet().getBalance().toPlainString());
        });

        kit.wallet().addCoinsSentEventListener((Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) -> {
            Log.e("AAA", "coins sent");
            NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED, BTC_CURRENCY_VALUE, kit.wallet().getBalance().toPlainString());
        });

        kit.wallet().addKeyChainEventListener((List<ECKey> keys) -> {
            Log.e("AAA", "new key added");
        });

        kit.wallet().addScriptsChangeEventListener((Wallet wallet, List<Script> scripts, boolean isAddingScripts) -> {
            Log.e("AAA", "new script added");
        });

        kit.wallet().addTransactionConfidenceEventListener((Wallet wallet, Transaction tx) -> {
            Log.e("AAA", "-----> confidence changed: " + tx.getHashAsString());
            TransactionConfidence confidence = tx.getConfidence();
            Log.e("AAA", "new block depth: " + confidence.getDepthInBlocks());
        });

        // Make sure to properly shut down all the running services when you manually want to stop the kit. The WalletAppKit registers a runtime ShutdownHook so we actually do not need to worry about that when our application is stopping.
        //Log.e("AAA", "shutting down again");
        //kit.stopAsync();
        //kit.awaitTerminated();
    }

    @Override
    public Wallet getBitcoinWallet() {
        try {
            kit.startAsync();
            kit.awaitRunning();
        } catch (Exception e) {

        }
        if (kit.wallet().isEncrypted())
            kit.wallet().decrypt(User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD);
        return kit.wallet();
    }

    public EthereumWallet getEthereumWallet() {
        if (ethereumWallet == null) {
            getEthereumWallet(User.CLIENT_MONEY_ETHEREUM_WALLET_PASSWORD);
        }
        return ethereumWallet;
    }

    public PaymonWallet getPaymonWallet() {
        if (paymonWallet == null) {
            getPaymonWallet(User.CLIENT_MONEY_PAYMON_WALLET_PASSWORD);
        }
        return paymonWallet;
    }

    @Override
    protected EthereumWallet getEthereumWallet(final String password) {
        try {
            ethereumWalletCredentials = org.web3j.crypto.WalletUtils.loadCredentials(password, ethereumWalletPath);
            if (ethereumWalletCredentials != null)
                ethereumWallet = new EthereumWallet(ethereumWalletCredentials, password, "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ethereumWallet;
    }

    @Override
    protected PaymonWallet getPaymonWallet(final String password) {
        try {
            paymonWalletCredentials = org.web3j.crypto.WalletUtils.loadCredentials(password, paymonWalletPath);
            if (paymonWalletCredentials != null)
                paymonWallet = new PaymonWallet(paymonWalletCredentials, password, "0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paymonWallet;
    }

    @Override
    public boolean createEthereumWallet(final String password) {
        final String FILE_FOLDER = getApplicationContext().getFilesDir().getAbsolutePath();
        deleteEthereumWallet();
        try {
            String fileName = org.web3j.crypto.WalletUtils.generateNewWalletFile(password, new File(FILE_FOLDER), false);
            if (fileName != null) {
                if (!Utils.copyFile(new File(FILE_FOLDER + "/" + fileName), new File(ethereumWalletPath)))
                    return false;
                new File(FILE_FOLDER + "/" + fileName).delete();
                ethereumWallet = getEthereumWallet(password);
                return ethereumWallet != null;
            }
            return false;
        } catch (CipherException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createPaymonWallet(final String password) {
        final String FILE_FOLDER = getApplicationContext().getFilesDir().getAbsolutePath();
        deleteEthereumWallet();
        try {
            String fileName = org.web3j.crypto.WalletUtils.generateNewWalletFile(password, new File(FILE_FOLDER), false);
            if (fileName != null) {
                if (!Utils.copyFile(new File(FILE_FOLDER + "/" + fileName), new File(paymonWalletPath)))
                    return false;
                new File(FILE_FOLDER + "/" + fileName).delete();
                paymonWallet = getPaymonWallet(password);
                return paymonWallet != null;
            }
            return false;
        } catch (CipherException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean backupBitcoinWallet(final String path) {
        try {
            kit.wallet().saveToFile(new File(path));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Coin getBitcoinBalance() {
        return getBitcoinWallet().getBalance();
    }

    @Override
    public boolean backupEthereumWallet(final String path) {
        final String BACKUP_FILE_PATH = path + "/" + "paymon-eth-wallet_backup_" + System.currentTimeMillis() + ".json";
        File walletFile = new File(ethereumWalletPath);
        File backupFile = new File(BACKUP_FILE_PATH);

        if (!Utils.copyFile(walletFile, backupFile)) {
            android.widget.Toast.makeText(ApplicationLoader.applicationContext, "Скопировать файл кошелька не удалось", android.widget.Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    public BigInteger getEthereumBalance() {
        BigInteger balance = null;
        try {
            balance = ethereumWeb3j.ethGetBalance(ethereumWalletCredentials.getAddress(), DefaultBlockParameterName.fromString("latest")).send().getBalance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return balance;
    }

    @Override
    public boolean deleteEthereumWallet() {
        final String FILE_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-eth-wallet.json";
        return new File(FILE_PATH).delete();
    }

    public static String convertEthereumToFiat(final BigInteger ethAmount, final String fiatExRate) {
        return Convert.fromWei(new BigDecimal(ethAmount), Convert.Unit.ETHER).multiply(new BigDecimal(fiatExRate)).setScale(2, ROUND_HALF_UP).toString();
    }

    public static String convertEthereumToFiat(final String ethAmount, final String fiatExRate) {
        return new BigDecimal(Double.parseDouble(ethAmount) * Double.parseDouble(fiatExRate)).setScale(2, ROUND_HALF_UP).toString();
    }

    @Override
    public boolean backupPaymonWallet(final String path) {
        final String BACKUP_FILE_PATH = path + "/" + "paymon-pmnt-wallet_backup_" + System.currentTimeMillis() + ".json";
        File walletFile = new File(ethereumWalletPath);
        File backupFile = new File(BACKUP_FILE_PATH);

        if (!Utils.copyFile(walletFile, backupFile)) {
            android.widget.Toast.makeText(ApplicationLoader.applicationContext, "Скопировать файл кошелька не удалось", android.widget.Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    public BigInteger getPaymonBalance() {
        BigInteger balance = null;
        try {
            balance = ethereumWeb3j.ethGetBalance(ethereumWalletCredentials.getAddress(), DefaultBlockParameterName.fromString("latest")).send().getBalance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return balance;
    }

    @Override
    public boolean deletePaymonWallet() {
        final String FILE_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-eth-wallet.json";
        return new File(FILE_PATH).delete();
    }


    public static String convertPaymonToFiat(final BigInteger pmntAmount, final String fiatExRate) {
        return Convert.fromWei(new BigDecimal(pmntAmount), Convert.Unit.GWEI).multiply(new BigDecimal(fiatExRate)).setScale(2, ROUND_HALF_UP).toString();
    }

    @Override
    public RestoreStatus restoreBitcoinWallet(final File file) {
        try {
            kit.wallet().loadFromFile(file);
            return RestoreStatus.DONE;
        } catch (Exception e) {
            return RestoreStatus.ERROR_DECRYPTING_WRONG_PASS;
        }
    }

    @Override
    public boolean deleteBitcoinWallet() {
        return false;
    }

    public static String convertBitcoinToFiat(String btcAmount, String fiatExRate) {
        return new BigDecimal(btcAmount).multiply(new BigDecimal(fiatExRate)).setScale(2, ROUND_HALF_UP).toString();
    }

    @Override
    public String getBitcoinPublicAddress() {
        return getBitcoinWallet().currentReceiveAddress().toBase58();
    }

    @Override
    public String getBitcoinPrivateAddress() {
        return getBitcoinWallet().getActiveKeyChain().getWatchingKey().getPrivateKeyAsWiF(Constants.NETWORK_PARAMETERS);
    }

    @Override
    public List<String> getAllBitcoinPublicAdresses() {
        List<ECKey> keys = getBitcoinWallet().getActiveKeyChain().getIssuedReceiveKeys();
        List<String> keysList = new ArrayList<>();
        for (ECKey key : keys) {
            keysList.add(new Address(Constants.NETWORK_PARAMETERS, key.getPubKeyHash()).toBase58());
        }
        return keysList;
    }

    @Override
    public RestoreStatus restoreEthereumWallet(final File file, final String password) {
        final File walletFileForRestore = new File(ethereumWalletPath);
        deleteEthereumWallet();

        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = new FileInputStream(file);
            outputStream = new FileOutputStream(walletFileForRestore);
            IOUtils.copy(inputStream, outputStream);
            try {
                ethereumWalletCredentials = null;
                ethereumWallet = getEthereumWallet(password);
                if (ethereumWallet != null)
                    return RestoreStatus.DONE;
                else
                    return RestoreStatus.NO_USER_ID;
            } catch (Exception e) {
                e.printStackTrace();
                return RestoreStatus.ERROR_DECRYPTING_WRONG_PASS;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return RestoreStatus.ERROR_CREATE_FILE;
        }
    }

    @Override
    public RestoreStatus restorePaymonWallet(final File file, final String password) {
        final File walletFileForRestore = new File(paymonWalletPath);
        deleteEthereumWallet();

        InputStream inputStream;
        OutputStream outputStream;
        try {
            inputStream = new FileInputStream(file);
            outputStream = new FileOutputStream(walletFileForRestore);
            IOUtils.copy(inputStream, outputStream);
            try {
                paymonWalletCredentials = null;
                paymonWallet = getPaymonWallet(password);
                if (paymonWallet != null)
                    return RestoreStatus.DONE;
                else
                    return RestoreStatus.NO_USER_ID;
            } catch (Exception e) {
                e.printStackTrace();
                return RestoreStatus.ERROR_DECRYPTING_WRONG_PASS;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return RestoreStatus.ERROR_CREATE_FILE;
        }
    }

    @Override
    public EthSendTransaction sendRawEthereumTx(@NonNull String recipientAddress, @NonNull BigInteger ethAmount, @NonNull BigInteger gasPrice, @NonNull BigInteger gasLimit) {
        EthSendTransaction ethSendTransaction = null;
        try {
            BigInteger nonce = ethereumWeb3j.ethGetTransactionCount(ethereumWalletCredentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, recipientAddress, ethAmount);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, ethereumWalletCredentials);
            String hexValue = Numeric.toHexString(signedMessage);
            ethSendTransaction = ethereumWeb3j.ethSendRawTransaction(hexValue).send();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ethSendTransaction;
    }

    @Override
    public EthSendTransaction sendPmntContract(@NonNull String recipientAddress, @NonNull BigInteger pmntAmount, @NonNull BigInteger gasPrice, @NonNull BigInteger gasLimit) {
//        final RawTransactionManager manager = new RawTransactionManager(paymonmWeb3j, paymonWalletCredentials);
//        final String contractAddress = "0x81b4d08645da11374a03749ab170836e4e539767";
//        String data = encodeTransferData(toAddress, sum);
//        EthSendTransaction transaction = manager.sendTransaction(gasPrice, gasLimit, contractAddress, data, null);

//        EthSendTransaction ethSendTransaction = null;
//        try {
//            BigInteger nonce = ethereumWeb3j.ethGetTransactionCount(ethereumWalletCredentials.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
//            RawTransaction rawTransaction = RawTransaction.createContractTransaction(nonce, gasPrice, gasLimit, recipientAddress, pmntAmount);
//            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, ethereumWalletCredentials);
//            String hexValue = Numeric.toHexString(signedMessage);
//            ethSendTransaction = ethereumWeb3j.ethSendRawTransaction(hexValue).send();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        return ethSendTransaction;
        return null;
    }

    @Override
    public Transaction sendBitcoinTx(final String destinationAddress, final long satoshis, final long feePerB) {
        Coin value = Coin.valueOf(satoshis);
        Address to = Address.fromBase58(Constants.NETWORK_PARAMETERS, destinationAddress);

        Transaction transaction = new Transaction(Constants.NETWORK_PARAMETERS);
        transaction.addInput(getBitcoinWallet().getUnspents().get(0));// important to add proper input
        transaction.addOutput(value, to);

        SendRequest request1 = SendRequest.forTx(transaction);
        request1.feePerKb = Coin.valueOf(feePerB * 1000);

        Wallet.SendResult sendResult = null;
        try {
            sendResult = getBitcoinWallet().sendCoins(kit.peerGroup(), request1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sendResult != null ? sendResult.tx : null;
    }
}
