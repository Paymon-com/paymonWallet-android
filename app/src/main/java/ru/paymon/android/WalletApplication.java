package ru.paymon.android;

import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;

import com.android.volley.toolbox.Volley;
import com.google.common.io.BaseEncoding;

import org.apache.commons.io.IOUtils;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.LinuxSecureRandom;
import org.bitcoinj.script.Script;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletProtobufSerializer;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.DataLengthException;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.PBEParametersGenerator;
import org.spongycastle.crypto.engines.AESFastEngine;
import org.spongycastle.crypto.generators.OpenSSLPBEParametersGenerator;
import org.spongycastle.crypto.modes.CBCBlockCipher;
import org.spongycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.spongycastle.crypto.params.ParametersWithIV;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainId;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.response.NoOpProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import ru.paymon.android.models.EthereumWallet;
import ru.paymon.android.models.PaymonTokenContract;
import ru.paymon.android.models.PaymonWallet;
import ru.paymon.android.utils.Constants;
import ru.paymon.android.utils.Iso8601Format;
import ru.paymon.android.utils.Utils;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static org.bitcoinj.crypto.KeyCrypterScrypt.SALT_LENGTH;
import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;


public class WalletApplication extends AbsWalletApplication {
    public static final int btcTxSize = (148 * 1) + (34 * 2) + 10;
    private static final boolean IS_TEST = true;
    private static final String INFURA_LINK = IS_TEST ? "https://ropsten.infura.io/BAWTZQzsbBDZG6g9D0IP" : "https://mainnet.infura.io/BAWTZQzsbBDZG6g9D0IP";
    private EthereumWallet ethereumWallet;
    private PaymonWallet paymonWallet;
    private String ethereumWalletPath;
    private String paymonWalletPath;
    private WalletKit kit;

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

        paymonmWeb3j = Web3jFactory.build(new HttpService("https://mainnet.infura.io/BAWTZQzsbBDZG6g9D0IP"));
        paymonRequestQueue = Volley.newRequestQueue(getApplicationContext());
    }

    public void startBitcoinKit() {
//        if (User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD == null) return;
        kit = new WalletKit(Constants.NETWORK_PARAMETERS, new File(getCacheDir().getPath()), "walletappkit1-example");

        InputStream checkpoint = CheckpointManager.openStream(Constants.NETWORK_PARAMETERS);
        kit.setCheckpoints(checkpoint);
        kit.setAutoSave(true);
        kit.setBlockingStartup(false);

        kit.setDownloadListener(new DownloadProgressTracker() {
            @Override
            public void onChainDownloadStarted(Peer peer, int blocksLeft) {
                super.onChainDownloadStarted(peer, blocksLeft);
            }

            @Override
            public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
                super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);
            }

            @Override
            protected void progress(double pct, int blocksSoFar, Date date) {
                super.progress(pct, blocksSoFar, date);
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_DOWNLOAD_PROGRESS);
                Log.e("AAA", "QQ " + pct);
            }

            @Override
            protected void startDownload(int blocks) {
                super.startDownload(blocks);
            }

            @Override
            protected void doneDownload() {
                super.doneDownload();
                NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.BTC_BLOCKCHAIN_DOWNLOAD_FINISHED);
                Log.e("AAA", "QQ fin");
            }
        });

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
        if (User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD == null) return null;

        try {
            kit.startAsync();
            kit.awaitRunning();
        } catch (Exception e) {
            e.printStackTrace();
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

    private static byte[] concat(final byte[] arrayA, final byte[] arrayB) {
        final byte[] result = new byte[arrayA.length + arrayB.length];
        System.arraycopy(arrayA, 0, result, 0, arrayA.length);
        System.arraycopy(arrayB, 0, result, arrayA.length, arrayB.length);

        return result;
    }

    private static final SecureRandom secureRandom = new SecureRandom();

    private static byte[] encryptRaw(final byte[] plainTextAsBytes, final char[] password) throws IOException {
        try {
            // Generate salt - each encryption call has a different salt.
            final byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);

            final ParametersWithIV key = (ParametersWithIV) getAESPasswordKey(password, salt);

            // The following code uses an AES cipher to encrypt the message.
            final BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(true, key);
            final byte[] encryptedBytes = new byte[cipher.getOutputSize(plainTextAsBytes.length)];
            final int processLen = cipher.processBytes(plainTextAsBytes, 0, plainTextAsBytes.length, encryptedBytes, 0);
            final int doFinalLen = cipher.doFinal(encryptedBytes, processLen);

            // The result bytes are the SALT_LENGTH bytes followed by the encrypted bytes.
            return concat(salt, Arrays.copyOf(encryptedBytes, processLen + doFinalLen));
        } catch (final InvalidCipherTextException | DataLengthException x) {
            throw new IOException("Could not encrypt bytes", x);
        }
    }

    public static String encrypt(final byte[] plainTextAsBytes, final char[] password) throws IOException {
        final byte[] encryptedBytes = encryptRaw(plainTextAsBytes, password);

        // OpenSSL prefixes the salt bytes + encryptedBytes with Salted___ and then base64 encodes it
        final byte[] encryptedBytesPlusSaltedText = concat(OPENSSL_SALTED_BYTES, encryptedBytes);

        return BASE64_ENCRYPT.encode(encryptedBytesPlusSaltedText);
    }

    @Override
    public boolean backupBitcoinWallet(final String path) {
        final Wallet wallet = kit.wallet();
        final Protos.Wallet walletProto = new WalletProtobufSerializer().walletToProto(wallet);
//        Uri targetUri = Uri.parse(path + "/" + "paymon-btc-wallet_backup_" + System.currentTimeMillis());

        try (final Writer cipherOut = new OutputStreamWriter(new FileOutputStream(new File(path + "/" + "paymon-btc-wallet_backup_" + System.currentTimeMillis())), StandardCharsets.UTF_8)) {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            walletProto.writeTo(baos);
            baos.close();
            final byte[] plainBytes = baos.toByteArray();

            cipherOut.write(encrypt(plainBytes, User.CLIENT_MONEY_BITCOIN_WALLET_PASSWORD.toCharArray()));
            cipherOut.flush();

//            final String target = uriToTarget(targetUri) != null ? uriToTarget(targetUri) : targetUri.toString();
            return true;
        } catch (final IOException x) {
            x.printStackTrace();
            return false;
        }
//        try {
//            kit.wallet().saveToFile(new File(path + "/" + "paymon-btc-wallet_backup_" + System.currentTimeMillis() + ".wallet"));
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
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

    public static String convertEthereumToFiat(final BigInteger weiAmount, final String fiatExRate) {
        BigDecimal bigDecimalEthAmount = Convert.fromWei(new BigDecimal(weiAmount), Convert.Unit.ETHER);
        BigDecimal bigDecimalExRate = new BigDecimal(fiatExRate);
        return bigDecimalEthAmount.multiply(bigDecimalExRate).setScale(2, ROUND_HALF_UP).toString();
    }

    public static String convertEthereumToFiat(final String ethAmount, final String fiatExRate) {
        return new BigDecimal(Double.parseDouble(ethAmount) * Double.parseDouble(fiatExRate)).setScale(2, ROUND_HALF_UP).toString();
    }

    @Override
    public boolean backupPaymonWallet(final String path) {
        final String BACKUP_FILE_PATH = path + "/" + "paymon-pmnt-wallet_backup_" + System.currentTimeMillis() + ".json";
        File walletFile = new File(paymonWalletPath);
        File backupFile = new File(BACKUP_FILE_PATH);

        if (!Utils.copyFile(walletFile, backupFile)) {
            android.widget.Toast.makeText(ApplicationLoader.applicationContext, "Скопировать файл кошелька не удалось", android.widget.Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    public BigInteger getPaymonBalance() {
        TransactionReceiptProcessor transactionReceiptProcessor = new NoOpProcessor(paymonmWeb3j);
        TransactionManager transactionManager = new RawTransactionManager(paymonmWeb3j, paymonWalletCredentials, ChainId.MAINNET, transactionReceiptProcessor);
        PaymonTokenContract paymonTokenContract = PaymonTokenContract.load("0x81b4d08645da11374a03749ab170836e4e539767", paymonmWeb3j, transactionManager, new BigInteger("0"), new BigInteger("0"));
        BigInteger balance = null;
        try {
            balance = paymonTokenContract.balanceOf(getPaymonWallet().publicAddress).sendAsync().get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return balance;
    }

    @Override
    public BigInteger getPaymonEthBalance() {
        BigInteger balance = null;
        try {
            balance = paymonmWeb3j.ethGetBalance(paymonWalletCredentials.getAddress(), DefaultBlockParameterName.fromString("latest")).send().getBalance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return balance;
    }

    @Override
    public boolean deletePaymonWallet() {
        final String FILE_PATH = getApplicationContext().getFilesDir().getAbsolutePath() + "/" + "paymon-pmnt-wallet.json";
        return new File(FILE_PATH).delete();
    }


    public static String convertPaymonToFiat(final BigInteger pmntAmount, final String fiatExRate) {
        return Convert.fromWei(new BigDecimal(pmntAmount), Convert.Unit.ETHER).multiply(new BigDecimal(fiatExRate)).setScale(2, ROUND_HALF_UP).toString();
    }

    public static String convertPaymonToFiat(final String pmntAmount, final String fiatExRate) {
        return new BigDecimal(Double.parseDouble(pmntAmount) * Double.parseDouble(fiatExRate)).setScale(2, ROUND_HALF_UP).toString();
    }

    private static final BaseEncoding BASE64_ENCRYPT = BaseEncoding.base64().withSeparator("\n", 76);
    private static final BaseEncoding BASE64_DECRYPT = BaseEncoding.base64().withSeparator("\r\n", 76);
    private static final String OPENSSL_SALTED_TEXT = "Salted__";
    private static final byte[] OPENSSL_SALTED_BYTES = OPENSSL_SALTED_TEXT.getBytes(StandardCharsets.UTF_8);
    private static final int KEY_LENGTH = 256;
    private static final int NUMBER_OF_ITERATIONS = 1024;
    private static final int IV_LENGTH = 128;
    private static final int NUMBER_OF_CHARACTERS_TO_MATCH_IN_OPENSSL_MAGIC_TEXT = 10;
    private static final String OPENSSL_MAGIC_TEXT = BASE64_ENCRYPT.encode(OPENSSL_SALTED_BYTES).substring(0, NUMBER_OF_CHARACTERS_TO_MATCH_IN_OPENSSL_MAGIC_TEXT);


    private static CipherParameters getAESPasswordKey(final char[] password, final byte[] salt) {
        final PBEParametersGenerator generator = new OpenSSLPBEParametersGenerator();
        generator.init(PBEParametersGenerator.PKCS5PasswordToBytes(password), salt, NUMBER_OF_ITERATIONS);

        final ParametersWithIV key = (ParametersWithIV) generator.generateDerivedParameters(KEY_LENGTH, IV_LENGTH);

        return key;
    }

    private static byte[] decryptRaw(final byte[] bytesToDecode, final char[] password) throws IOException {
        try {
            // separate the salt and bytes to decrypt
            final byte[] salt = new byte[SALT_LENGTH];

            System.arraycopy(bytesToDecode, 0, salt, 0, SALT_LENGTH);

            final byte[] cipherBytes = new byte[bytesToDecode.length - SALT_LENGTH];
            System.arraycopy(bytesToDecode, SALT_LENGTH, cipherBytes, 0, bytesToDecode.length - SALT_LENGTH);

            final ParametersWithIV key = (ParametersWithIV) getAESPasswordKey(password, salt);

            // decrypt the message
            final BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
            cipher.init(false, key);

            final byte[] decryptedBytes = new byte[cipher.getOutputSize(cipherBytes.length)];
            final int processLen = cipher.processBytes(cipherBytes, 0, cipherBytes.length, decryptedBytes, 0);
            final int doFinalLen = cipher.doFinal(decryptedBytes, processLen);

            return Arrays.copyOf(decryptedBytes, processLen + doFinalLen);
        } catch (final InvalidCipherTextException | DataLengthException x) {
            throw new IOException("Could not decrypt bytes", x);
        }
    }

    public static byte[] decryptBytes(final String textToDecode, final char[] password) throws IOException {
        final byte[] decodeTextAsBytes;
        try {
            decodeTextAsBytes = BASE64_DECRYPT.decode(textToDecode);
        } catch (final IllegalArgumentException x) {
            throw new IOException("invalid base64 encoding");
        }

        if (decodeTextAsBytes.length < OPENSSL_SALTED_BYTES.length)
            throw new IOException("out of salt");

        final byte[] cipherBytes = new byte[decodeTextAsBytes.length - OPENSSL_SALTED_BYTES.length];
        System.arraycopy(decodeTextAsBytes, OPENSSL_SALTED_BYTES.length, cipherBytes, 0,
                decodeTextAsBytes.length - OPENSSL_SALTED_BYTES.length);

        final byte[] decryptedBytes = decryptRaw(cipherBytes, password);

        return decryptedBytes;
    }

    public static final long copy(final Reader reader, final StringBuilder builder, final long maxChars)
            throws IOException {
        final char[] buffer = new char[256];
        long count = 0;
        int n = 0;
        while (-1 != (n = reader.read(buffer))) {
            builder.append(buffer, 0, n);
            count += n;

            if (maxChars != 0 && count > maxChars)
                throw new IOException("Read more than the limit of " + maxChars + " characters");
        }
        return count;
    }

    public static Wallet restoreWalletFromProtobuf(final InputStream is, final NetworkParameters expectedNetworkParameters) throws IOException {
        try {
            final Wallet wallet = new WalletProtobufSerializer().readWallet(is, true, null);

            if (!wallet.getParams().equals(expectedNetworkParameters))
                throw new IOException("bad wallet backup network parameters: " + wallet.getParams().getId());
            if (!wallet.isConsistent())
                throw new IOException("inconsistent wallet backup");

            return wallet;
        } catch (final UnreadableWalletException x) {
            throw new IOException("unreadable wallet", x);
        }
    }

    public static Wallet restoreWalletFromProtobufOrBase58(final InputStream is, final NetworkParameters expectedNetworkParameters) throws IOException {
        is.mark((int) Constants.BACKUP_MAX_CHARS);

        try {
            return restoreWalletFromProtobuf(is, expectedNetworkParameters);
        } catch (final IOException x) {
            try {
                is.reset();
                return restorePrivateKeysFromBase58(is, expectedNetworkParameters);
            } catch (final IOException x2) {
                throw new IOException("cannot read protobuf (" + x.getMessage() + ") or base58 (" + x2.getMessage() + ")", x);
            }
        }
    }

    public static List<ECKey> readKeys(final BufferedReader in, final NetworkParameters expectedNetworkParameters)
            throws IOException {
        try {
            final DateFormat format = Iso8601Format.newDateTimeFormatT();

            final List<ECKey> keys = new LinkedList<ECKey>();

            long charCount = 0;
            while (true) {
                final String line = in.readLine();
                if (line == null)
                    break; // eof
                charCount += line.length();
                if (charCount > Constants.BACKUP_MAX_CHARS)
                    throw new IOException("read more than the limit of " + Constants.BACKUP_MAX_CHARS + " characters");
                if (line.trim().isEmpty() || line.charAt(0) == '#')
                    continue; // skip comment

                final String[] parts = line.split(" ");

                final ECKey key = DumpedPrivateKey.fromBase58(expectedNetworkParameters, parts[0]).getKey();
                key.setCreationTimeSeconds(
                        parts.length >= 2 ? format.parse(parts[1]).getTime() / DateUtils.SECOND_IN_MILLIS : 0);

                keys.add(key);
            }

            return keys;
        } catch (final AddressFormatException | ParseException x) {
            throw new IOException("cannot read keys", x);
        }
    }

    public static Wallet restorePrivateKeysFromBase58(final InputStream is, final NetworkParameters expectedNetworkParameters) throws IOException {
        final BufferedReader keyReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

        // create non-HD wallet
        final KeyChainGroup group = new KeyChainGroup(expectedNetworkParameters);
        group.importKeys(readKeys(keyReader, expectedNetworkParameters));
        return new Wallet(expectedNetworkParameters, group);
    }


    public static final FileFilter KEYS_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File file) {
            try (final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                readKeys(reader, Constants.NETWORK_PARAMETERS);

                return true;
            } catch (final IOException x) {
                return false;
            }
        }
    };

    public static final FileFilter BACKUP_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File file) {
            try (final InputStream is = new FileInputStream(file)) {
                return WalletProtobufSerializer.isWallet(is);
            } catch (final IOException x) {
                return false;
            }
        }
    };

    public final static FileFilter OPENSSL_FILE_FILTER = new FileFilter() {
        private final char[] buf = new char[OPENSSL_MAGIC_TEXT.length()];

        @Override
        public boolean accept(final File file) {
            try (final Reader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                if (in.read(buf) == -1)
                    return false;
                final String str = new String(buf);
                if (!str.toString().equals(OPENSSL_MAGIC_TEXT))
                    return false;
                return true;
            } catch (final IOException x) {
                return false;
            }
        }
    };

    private Wallet restoreWalletFromEncrypted(final File file, final String password) {
        try {
            final BufferedReader cipherIn = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            final StringBuilder cipherText = new StringBuilder();
            copy(cipherIn, cipherText, Constants.BACKUP_MAX_CHARS);
            cipherIn.close();

            final byte[] plainText = decryptBytes(cipherText.toString(), password.toCharArray());
            final InputStream is = new ByteArrayInputStream(plainText);

            return restoreWalletFromProtobufOrBase58(is, Constants.NETWORK_PARAMETERS);
        } catch (final IOException x) {
            x.printStackTrace();
        }
        return null;
    }

    @Override
    public RestoreStatus restoreBitcoinWallet(final File file, final String password) {
        Wallet wallet = null;

        if (BACKUP_FILE_FILTER.accept(file)) {
            try (final FileInputStream is1 = new FileInputStream(file)) {
                wallet = restoreWalletFromProtobuf(is1, Constants.NETWORK_PARAMETERS);
            } catch (final IOException x) {
                x.printStackTrace();
            }
        } else if (KEYS_FILE_FILTER.accept(file)) {
            try (final FileInputStream is1 = new FileInputStream(file)) {
                wallet = restorePrivateKeysFromBase58(is1, Constants.NETWORK_PARAMETERS);
            } catch (final IOException x) {
                x.printStackTrace();
            }
        } else if (OPENSSL_FILE_FILTER.accept(file)) {
           wallet =  restoreWalletFromEncrypted(file, password);
        }

        Wallet resultWallet = null;
        try {
            resultWallet = kit.restoreWallet(wallet);
        }catch (Exception e){
            e.printStackTrace();
        }

        return resultWallet != null ? RestoreStatus.DONE : RestoreStatus.ERROR_DECRYPTING_WRONG_PASS;
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
    public TransactionReceipt sendPmntContract(@NonNull String recipientAddress, @NonNull BigInteger pmntAmount, @NonNull BigInteger gasPrice, @NonNull BigInteger gasLimit) {
        TransactionReceiptProcessor transactionReceiptProcessor = new NoOpProcessor(paymonmWeb3j);
        TransactionManager transactionManager = new RawTransactionManager(paymonmWeb3j, paymonWalletCredentials, ChainId.MAINNET, transactionReceiptProcessor);
        PaymonTokenContract paymonTokenContract = PaymonTokenContract.load("0x81b4d08645da11374a03749ab170836e4e539767", paymonmWeb3j, transactionManager, gasPrice, gasLimit);
        TransactionReceipt transactionReceipt = null;
        try {
            transactionReceipt = paymonTokenContract.transfer(recipientAddress, pmntAmount).send();
            String sTransHash = transactionReceipt.getTransactionHash();
            Log.e("AAA", "toAccount: " + recipientAddress + " coinAmount: " + pmntAmount + " transactionhash: " + sTransHash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return transactionReceipt;
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
