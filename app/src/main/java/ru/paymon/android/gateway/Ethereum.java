package ru.paymon.android.gateway;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.concurrent.ExecutionException;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.BuildConfig;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.utils.Utils;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static ru.paymon.android.gateway.Ethereum.TX_STATUS.DONE;

//import ru.paymon.android.utils.cache.lruramcache.LruRamCache;

public class Ethereum {
    private static final String TAG = "Ethereum";
    private static volatile Ethereum instance;
    private static final String FILE_NAME = "paymon-eth-wallet";
    private static final String FILE_PATH = ApplicationLoader.applicationContext.getFilesDir().getAbsolutePath();
    private static final File BACKUP_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static final boolean IS_TEST = true;
    private static final String INFURA_LINK = IS_TEST ? "https://ropsten.infura.io/BAWTZQzsbBDZG6g9D0IP" : "https://mainnet.infura.io/BAWTZQzsbBDZG6g9D0IP";
    private static final BigDecimal TEN_POW_18 = new BigDecimal("1000000000000000000");
    private static final BigDecimal TEN_POW_9 = new BigDecimal("1000000000");

    private Web3j web3j;
    private RequestQueue requestQueue;
    private Credentials walletCredentials;

    public enum TX_STATUS {
        DONE,
        NOT_HAVE_MONEY,
        UNEXPECTED_ERROR,
        CREDENTIALS_NULL,
        ERROR_GET_NONCE,
        MB_NOT_HAVE_MONEY
    }

    public enum RestoreStatus {
        NO_USER_ID,
        ERROR_CREATE_FILE,
        ERROR_DECRYPTING_WRONG_PASS,
        DONE
    }

    private Ethereum() {
        web3j = Web3jFactory.build(new HttpService(INFURA_LINK));
        requestQueue = Volley.newRequestQueue(ApplicationLoader.applicationContext);
    }

    public static Ethereum getInstance() {
        if (instance == null) {
            synchronized (Ethereum.class) {
                if (instance == null)
                    instance = new Ethereum();
            }
        }
        return instance;
    }

    public boolean loadWallet(final String password) {
        try {
            walletCredentials = WalletUtils.loadCredentials(password, FILE_PATH + "/" + FILE_NAME + User.currentUser.id + ".json");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return walletCredentials != null;
    }

    public boolean createWallet(final int userID, final String password) {
        deleteWallet();
        String fileName;
        try {
            fileName = WalletUtils.generateNewWalletFile(password, new File(FILE_PATH), false);
        } catch (CipherException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
            return false;
        }

        return Utils.renameFile(FILE_PATH, fileName, FILE_NAME + userID + ".json");
    }

    public void backupWallet() {
        File walletFile = new File(FILE_PATH + "/" + FILE_NAME + User.currentUser.id + ".json");
        File walletInDownload = new File(BACKUP_DIR.getAbsolutePath() + "/" + FILE_NAME + User.currentUser.id + ".json");

        if (!Utils.copyFile(walletFile, walletInDownload)) {
            Toast.makeText(ApplicationLoader.applicationContext, "Скопировать файл кошелька не удалось", Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(ApplicationLoader.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("application/json");

            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(ApplicationLoader.applicationContext, BuildConfig.APPLICATION_ID + ".provider", walletInDownload));

            try {
                ApplicationLoader.applicationContext.startActivity(Intent.createChooser(intent, ""));
            } catch (Exception e) {
                Toast.makeText(ApplicationLoader.applicationContext, R.string.export_keys_dialog_mail_intent_failed, Toast.LENGTH_LONG).show();
            }

        } else
            Toast.makeText(ApplicationLoader.applicationContext, R.string.right_file_system, Toast.LENGTH_SHORT).show();
    }

    public RestoreStatus restoreWallet(final InputStream inputStream, final String password) {
        deleteWallet();
        File walletFileForRestore = new File(FILE_PATH, FILE_NAME + User.currentUser.id + ".json");

        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(walletFileForRestore);
            IOUtils.copy(inputStream, outputStream);
            try {
                walletCredentials = null;
                loadWallet(password);
                return RestoreStatus.DONE;
            } catch (Exception e) {
                e.printStackTrace();
                return RestoreStatus.ERROR_DECRYPTING_WRONG_PASS;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return RestoreStatus.ERROR_CREATE_FILE;
        }
    }

    public boolean deleteWallet() {
        return new File(FILE_PATH, FILE_NAME + User.currentUser.id + ".json").delete();
    }

    public String getAddress() {
        if (walletCredentials == null) return null;
        else return walletCredentials.getAddress();
    }

    public String getPrivateKey() {
        return walletCredentials == null ? null : walletCredentials.getEcKeyPair().getPrivateKey().toString(16);
    }

    public BigDecimal getBalance() {
        try {
            return Convert.fromWei(new BigDecimal(web3j.ethGetBalance(getAddress(), DefaultBlockParameterName.fromString("latest")).send().getBalance()), Convert.Unit.ETHER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String convertEthToFiat(final String ethAmount, final float fiatExRate) {
//        BigDecimal courseBigDecimal = (new BigDecimal(fiatExRate)).setScale(2, ROUND_HALF_UP);
//        BigDecimal preOutput = courseBigDecimal.multiply(new BigDecimal(ethAmount));
//        return preOutput.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        return new BigDecimal(ethAmount).multiply(new BigDecimal(fiatExRate)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    public BigDecimal getNormalGasPrice() {
        try {
            EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
            return Convert.fromWei(new BigDecimal(ethGasPrice.getGasPrice(), 0), Convert.Unit.GWEI).setScale(0, ROUND_HALF_UP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public TX_STATUS send_RawTx(@NonNull String recipientAddress, @NonNull BigDecimal ethAmount, @NonNull BigDecimal gasPrise, @NonNull BigInteger gasLimit) {
        if (walletCredentials == null) return TX_STATUS.CREDENTIALS_NULL;
        Log.d(TAG, "send_RawTx: " + ethAmount + "->" + ethAmount.multiply(TEN_POW_18).setScale(0, RoundingMode.HALF_UP).toString());
        final BigInteger ethAmountBigInteger16 = new BigInteger(ethAmount.multiply(TEN_POW_18).setScale(0, RoundingMode.HALF_UP).toString(), 16);
        final BigInteger gasPriseBigInteger16 = new BigInteger(gasPrise.multiply(TEN_POW_9).setScale(0, RoundingMode.HALF_UP).toString(), 16);
        final BigInteger gasLimitBigInteger16 = new BigInteger(gasLimit.toString(), 16);
        Log.d(TAG, "sendETH: start sendETH");

        EthGetTransactionCount ethGetTransactionCount;
        try {
            ethGetTransactionCount = web3j.ethGetTransactionCount(getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Log.d(TAG, "sendETH: " + TX_STATUS.ERROR_GET_NONCE.name());
            return TX_STATUS.ERROR_GET_NONCE;
        }
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();
        Log.d(TAG, "sendETH: nonce is get");
        Log.d(TAG, "send_RawTx: create raw " + nonce + " " + gasPriseBigInteger16 + " " + gasLimitBigInteger16 + " " + recipientAddress + " " + ethAmountBigInteger16);
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPriseBigInteger16, gasLimitBigInteger16, recipientAddress, ethAmountBigInteger16, "0x");

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, walletCredentials);
        String hexValue = Numeric.toHexString(signedMessage);
        Log.d(TAG, "send_RawTx: hex " + hexValue);

        String requestStr = "{\"jsonrpc\":\"2.0\",\"method\":\"eth_sendRawTransaction\",\"params\":[\"" + hexValue + "\"],\"gid\":1}";

        JsonObjectRequest requestObject = null;
        try {
            requestObject = new JsonObjectRequest(JsonObjectRequest.Method.POST, INFURA_LINK, new JSONObject(requestStr),
                    (jsonObject) -> Log.d(TAG, "onResponse: " + jsonObject),
                    (volleyError) -> Log.d(TAG, "onErrorResponse: " + volleyError.getMessage()));
        } catch (JSONException ignored) {
            ignored.printStackTrace();
            Log.d(TAG, "sendETH: e " + TX_STATUS.MB_NOT_HAVE_MONEY.name());
            return TX_STATUS.MB_NOT_HAVE_MONEY;
        }

        assert requestObject != null;
        requestQueue.add(requestObject);

        return DONE;
    }

    private EthSendTransaction ethSendTransaction;

    public EthSendTransaction send(BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger amount) {
            try {
                BigInteger nonce = web3j.ethGetTransactionCount(getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
                RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, amount);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, walletCredentials);
                String hexValue = Numeric.toHexString(signedMessage);
                ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
            } catch (Exception e) {
                e.printStackTrace();
            }

        return ethSendTransaction;
    }
}
