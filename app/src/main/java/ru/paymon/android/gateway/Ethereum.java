package ru.paymon.android.gateway;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

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
import java.util.concurrent.ExecutionException;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.BuildConfig;
import ru.paymon.android.Config;
import ru.paymon.android.MainActivity;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.utils.cache.lruramcache.LruRamCache;

import static java.math.BigDecimal.ROUND_HALF_UP;

public class Ethereum {
    private static final String TAG = "Ethereum";
    private static volatile Ethereum instance;
    private static final String FILE_PATH = ApplicationLoader.applicationContext.getFilesDir().getAbsolutePath();
    private static final File BACKUP_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static final String FILE_NAME = "paymon-eth-wallet";
    private static final boolean IS_TEST = true;
    private static final String INFURA_LINK = IS_TEST ? "https://ropsten.infura.io/BAWTZQzsbBDZG6g9D0IP" : "https://mainnet.infura.io/BAWTZQzsbBDZG6g9D0IP";
    private static final int NOTIFY_ID = 102;
    private static final BigDecimal TEN_POW_18 = new BigDecimal("1000000000000000000");
    private static final BigDecimal TEN_POW_9 = new BigDecimal("1000000000");

    private Web3j web3j;
    private RequestQueue requestQueue;
    private Credentials walletCredentials;

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
                if (instance == null) {
                    instance = new Ethereum();
                }
            }
        }
        return instance;
    }

//    public boolean createOrLoadWallet(final int userID, final String password) {
//        if (userID <= 0 && password.isEmpty())
//            return false;
//
//        try {
//            if (isWalletExist(userID)) {
//                Log.d(TAG, "createOrLoadWallet: wallet exist");
//                return loadWallet(userID, password);
//            } else {
//                Log.d(TAG, "createOrLoadWallet: start create eth");
//                createWallet(userID, password);
//                return loadWallet(userID, password);
//            }
//        } catch (Exception e) {
//            return false;
//        }
//    }

    public boolean createWallet(final int userID, final String password) {
        Log.d(TAG, "createWallet: start creating new wallet");
        deleteWallet();
        String fileName;
        try {
            fileName = WalletUtils.generateNewWalletFile(password, new File(FILE_PATH), false);
        } catch (CipherException | IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            e.printStackTrace();
            return false;
        }

        Log.d(TAG, "createWallet: temp file create " + fileName);
        boolean isRenamed = renameFile(FILE_PATH, fileName, FILE_NAME + userID + ".json");
        if (isRenamed) {
            Log.d(TAG, "createWallet: success rename to " + FILE_NAME + userID + ".json");
            return true;
        } else {
            Log.d(TAG, "createWallet: error rename file");
            return false;
        }
    }

    public boolean deleteWallet() {
        return new File(FILE_PATH, FILE_NAME + User.currentUser.id + ".json").delete();
    }

    public boolean isWalletExist(final int userID) {
        return (new File(FILE_PATH, FILE_NAME + userID + ".json")).exists();
    }

    public boolean loadWallet(final String password) throws IOException, CipherException {
        Log.d(Config.TAG, "loadWallet: start load wallet " + " user id: " + User.currentUser.id + " password: " + password);

        walletCredentials = WalletUtils.loadCredentials(password, FILE_PATH + "/" + FILE_NAME + User.currentUser.id + ".json");
        Log.d(Config.TAG, "loadWallet: " + walletCredentials);

        return walletCredentials != null;
    }

    private boolean renameFile(final File from, final File to) {
        return from.renameTo(to);
    }

    private boolean renameFile(final String fromAbsolutePAth, final String toAbsolutePath) {
        return renameFile(new File(fromAbsolutePAth), new File(toAbsolutePath));
    }

    private boolean renameFile(final String directory, final String fromFileName, final String toFileName) {
        return renameFile(directory + "/" + fromFileName, directory + "/" + toFileName);
    }

    public String getPrivateKey() {
        return walletCredentials == null ? null : walletCredentials.getEcKeyPair().getPrivateKey().toString(16);
    }

    public RestoreStatus restoreWallet(final InputStream inputStream, final String password) {
        Log.d(Config.TAG, "restore start. Password: " + password + "Input stream: " + (inputStream != null));

        deleteWallet();
        File walletFileForRestore = new File(FILE_PATH, FILE_NAME + User.currentUser.id + ".json");

        Log.d(TAG, "restoreWallet: " + walletFileForRestore.getPath());

        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(walletFileForRestore);
            IOUtils.copy(inputStream, outputStream);
            Log.d(Config.TAG, "restoreWallet: wallet successfully copied");

            try {
                Log.d(Config.TAG, "restoreWallet: trying to load credentials");
                walletCredentials = null;
                loadWallet(password);
                Log.d(Config.TAG, "restoreWallet: restore is success");
                return RestoreStatus.DONE;
            } catch (Exception e) {
                Log.d(Config.TAG, "restoreWallet: trying to load credentials failed");
                e.printStackTrace();
                return RestoreStatus.ERROR_DECRYPTING_WRONG_PASS;
            }
        } catch (IOException ioe) {
            Log.d(Config.TAG, "restoreWallet: trying to copy wallet failed");
            ioe.printStackTrace();
            return RestoreStatus.ERROR_CREATE_FILE;
        }
    }

    public void ethereumWalletNotification(Context context, String text, String title) {

//            final Ringtone ringtone = RingtoneManager.getRingtone(context, User.notificationRingtone);
        final Bitmap bitmap = LruRamCache.getInstance().getBitmap(R.drawable.ic_ethereum);

        final Intent intent = new Intent(ApplicationLoader.applicationContext, MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final PendingIntent pendingIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Notification.Builder nbuilder = new Notification.Builder(context)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(bitmap)
                .setTicker(text)
                .setContentTitle(title)
                .setContentText(text)
                /*.setSound(User.notificationRingtone)*/;
//            if (User.notificationCheckVibration) {
//                nbuilder.setVibrate(new long[]{100, 200, 100, 300});
//                if (ringtone != null) {
//                    ringtone.play();
//                }
        Notification notification = nbuilder.build();
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFY_ID, notification);
//            }
    }

    public String getAddress() {
        if (walletCredentials == null) return null;
        else return walletCredentials.getAddress();
    }

    public void getBalance(final Response.Listener<JSONObject> responseListener, final Response.ErrorListener errorListener) {
        JSONObject requestObject;

        try {
            requestObject = new JSONObject("{\"jsonrpc\":\"2.0\",\"method\":\"eth_getBalance\",\"params\":[\"" + getAddress() + "\", \"latest\"],\"id\":1}");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

//        final Response.Listener<JSONObject> responseListener = (response) -> {
//            final BigInteger bigInteger = Ethereum.getInstance().jsonToWei(response);
//            if (bigInteger != null) {
//                User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE = Ethereum.getInstance().weiToFriendlyString(bigInteger);
//                User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS = Ethereum.getInstance().getAddress();
//                User.CLIENT_MONEY_ETHEREUM_WALLET_PRIVATE_ADDRESS = Ethereum.getInstance().getPrivateKey();
//                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.didLoadEthereumWallet);
//            }
//        };
//
//        final Response.ErrorListener errorListener = (error) -> {
//            Log.d(TAG, "onErrorResponse: " + error.getMessage());
//        };

        JsonObjectRequest request = new JsonObjectRequest(JsonObjectRequest.Method.POST, INFURA_LINK, requestObject, responseListener, errorListener);
        requestQueue.add(request);
    }

    public String calculateFiatBalance(String course){
        BigDecimal courseBigDecimal = (new BigDecimal(course)).setScale(2, ROUND_HALF_UP);
//        BigDecimal preOutput = courseBigDecimal.divide(TEN_POW_18, 20, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(CRYPTO_BALANCE));
        BigDecimal preOutput = courseBigDecimal.multiply(new BigDecimal(User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE));
        return preOutput.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }
//    private class WeiToFiatStringStruct {
//        public BigInteger wei;
//        public String coursePrefix;
//
//        public WeiToFiatStringStruct(BigInteger wei, String coursePrefix) {
//            this.wei = wei;
//            this.coursePrefix = coursePrefix;
//        }
//    }
//
//    private static class WeiToFiatString extends AsyncTask<WeiToFiatStringStruct, Object, String> {
//        @Override
//        protected String doInBackground(WeiToFiatStringStruct... params) {
//            Double course = parseDouble(getCourseFromSite(params[0].coursePrefix));
//            if (course == null) return null;
//            BigDecimal courseBigDecimal = (new BigDecimal(course)).setScale(2, ROUND_HALF_UP);
//            BigDecimal preOutput = courseBigDecimal.divide(TEN_POW_18, 20, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(params[0].wei));
//            return preOutput.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
//        }
//    }
//
//    private static Double parseDouble(final String course) {
//        if (course == null) return null;
//        return Double.parseDouble(course.replace(".", "").replace(",", "."));
//    }

    public BigInteger jsonToWei(@NonNull final JSONObject balanceResponse) {
        try {
            return new BigInteger(balanceResponse.getString("result").replace("0x", ""), 16);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String weiToFriendlyString(@NonNull final BigInteger wei) {
        String result;

        if (User.CLIENT_MONEY_ETHEREUM_DENOMINATION.equals(ApplicationLoader.applicationContext.getString(R.string.money_denomination_auto))) {
            result = Convert.fromWei(String.valueOf(wei), Convert.Unit.ETHER).setScale(8, ROUND_HALF_UP).toString();
        } else if (User.CLIENT_MONEY_ETHEREUM_DENOMINATION.equals(ApplicationLoader.applicationContext.getString(R.string.money_denomination_8))) {
            result = Convert.fromWei(String.valueOf(wei), Convert.Unit.ETHER).setScale(8, ROUND_HALF_UP).toString();
        } else if (User.CLIENT_MONEY_ETHEREUM_DENOMINATION.equals(ApplicationLoader.applicationContext.getString(R.string.money_denomination_6))) {
            result = Convert.fromWei(String.valueOf(wei), Convert.Unit.ETHER).setScale(6, ROUND_HALF_UP).toString();
        } else if (User.CLIENT_MONEY_ETHEREUM_DENOMINATION.equals(ApplicationLoader.applicationContext.getString(R.string.money_denomination_4))) {
            result = Convert.fromWei(String.valueOf(wei), Convert.Unit.ETHER).setScale(4, ROUND_HALF_UP).toString();
        } else if (User.CLIENT_MONEY_ETHEREUM_DENOMINATION.equals(ApplicationLoader.applicationContext.getString(R.string.money_denomination_2))) {
            result = Convert.fromWei(String.valueOf(wei), Convert.Unit.ETHER).setScale(2, ROUND_HALF_UP).toString();
        } else {
            result = Convert.fromWei(String.valueOf(wei), Convert.Unit.ETHER).setScale(8, ROUND_HALF_UP).toString();
        }

        return result;
    }

    public void backupWallet() {
        File walletFile = new File(FILE_PATH + "/" + FILE_NAME + User.currentUser.id + ".json");
        File walletInDownload = new File(BACKUP_DIR.getAbsolutePath() + "/" + FILE_NAME + User.currentUser.id + ".json");

        if( !copyFile(walletFile, walletInDownload)) {
            Toast.makeText(ApplicationLoader.applicationContext, "Скопировать файл кошелька не удалось", Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(ApplicationLoader.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("application/json");

            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(ApplicationLoader.applicationContext,  BuildConfig.APPLICATION_ID +".provider", walletInDownload));

            try {
                ApplicationLoader.applicationContext.startActivity(Intent.createChooser(intent, ""));
            } catch (Exception e) {
                Toast.makeText(ApplicationLoader.applicationContext, R.string.export_keys_dialog_mail_intent_failed, Toast.LENGTH_LONG).show();
            }

        } else
            Toast.makeText(ApplicationLoader.applicationContext, R.string.right_file_system, Toast.LENGTH_SHORT).show();
    }

    private boolean copyFile(File source, File dest)  {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
                os.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public BigDecimal getNormalGasPrice() {
        EthGasPrice ethGasPrice = null;
        try {
            ethGasPrice = web3j.ethGasPrice().sendAsync().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new BigDecimal("41");
        }
        return Convert.fromWei(new BigDecimal(ethGasPrice.getGasPrice(), 0), Convert.Unit.GWEI);
    }
}
