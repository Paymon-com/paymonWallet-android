package ru.paymon.android.view;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.daimajia.androidviewhover.tools.Util;

import org.json.JSONObject;
import org.web3j.utils.Convert;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.models.ExchangeRatesItem;
import ru.paymon.android.utils.ExchangeRates;
import ru.paymon.android.utils.Utils;

public class FragmentEthereumWalletTransfer extends Fragment {
    private static FragmentEthereumWalletTransfer instance;

    private EditText receiverAddress;
    private EditText cryptoAmount;
    private TextView fiatEquivalent;
    private TextView gasRec;
    private TextView titleFrom;
    private TextView idFrom;
    private TextView balance;
    private TextView balanceFiat;
    private TextView cryptoAmountTitle;
    private TextView fiatEquivalentTitle;
    private TextView gasPriceValue;
    private TextView gasLimitValue;
    private TextView networkFeeValue;
    private TextView totalValue;
    private CrystalSeekbar gasPriceBar;
    private CrystalSeekbar gasLimitBar;
    private DialogProgress dialogProgress;

    private HashMap<String, HashMap<String, ExchangeRatesItem>> exchangeRates = new HashMap<>();
    private String currentFiatCurrency = "USD";//TODO:
    private final String cryptoCurrency = "ETH";


    public static FragmentEthereumWalletTransfer newInstance() {
        instance = new FragmentEthereumWalletTransfer();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ethereum_wallet_transfer, container, false);

        receiverAddress = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_receiver_address);
        cryptoAmount = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_amount);
        cryptoAmountTitle = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_amount_title);
        fiatEquivalent = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_fiat_equivalent);
        fiatEquivalentTitle = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_fiat_equivalent_title);
        gasRec = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_price_rec);
        titleFrom = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_title_from);
        idFrom = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_id_from);
        balance = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_balance);
        balanceFiat = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_balance_fiat);
        FloatingActionButton qr = (FloatingActionButton) view.findViewById(R.id.fragment_ethereum_wallet_transfer_qr);
        gasPriceBar = (CrystalSeekbar) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_price_slider);
        gasLimitBar = (CrystalSeekbar) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_limit_slider);
        gasPriceValue = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_price_value);
        gasLimitValue = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_limit_value);
        networkFeeValue = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_network_fee_value);
        totalValue = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_total_value);

        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(false);

        cryptoAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String ethAmount = editable.toString();
                if (ethAmount.isEmpty()) {
                    fiatEquivalent.setText(null);
                    updateTotalValue();
                    return;
                }

                if (ethAmount.startsWith(".")) {
                    cryptoAmount.setText(null);
                    return;
                }

                final String exchangeRate = exchangeRates.get(cryptoCurrency).get(currentFiatCurrency).value;
                final String fiatEquivalentStr = Ethereum.getInstance().convertEthToFiat(ethAmount, exchangeRate);
                fiatEquivalent.setText(fiatEquivalentStr);
                updateTotalValue();
            }
        });

        cryptoAmountTitle.setText(cryptoCurrency);
        fiatEquivalentTitle.setText(currentFiatCurrency);
        idFrom.setText(User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void init() {
        loadWalletBalance();
        loadGasPrice();
    }

    private void loadWalletBalance() {
        dialogProgress.show();
        Utils.stageQueue.postRunnable(() -> {
            exchangeRates = ExchangeRates.getInstance().parseExchangeRates();
            final String exchangeRate = exchangeRates.get(cryptoCurrency).get(currentFiatCurrency).value;
            Ethereum.getInstance().getBalance(
                    (responseBalance) -> {
                        final BigInteger bigInteger = Ethereum.getInstance().jsonToWei(responseBalance);
                        if (bigInteger != null) {
                            User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE = Ethereum.getInstance().weiToFriendlyString(bigInteger);
                            User.saveConfig();
                            final String fiatBalanceStr = Ethereum.getInstance().calculateFiatBalance(exchangeRate);//TODO:
                            ApplicationLoader.applicationHandler.post(() -> {
                                balance.setText(User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE);
                                balanceFiat.setText(fiatBalanceStr);
                            });
                        }
                    },
                    (error) -> {
                        ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, "Баланс Ethereum кошелька получить не удалось!", Toast.LENGTH_LONG).show());
                    });
        });
    }

    private void loadGasPrice() {
        Utils.stageQueue.postRunnable(() -> {
            try {
                final HttpsURLConnection httpsURLConnection = (HttpsURLConnection) ((new URL("https://www.etherchain.org/api/gasPriceOracle").openConnection()));
                httpsURLConnection.setConnectTimeout(20000);
                httpsURLConnection.connect();
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                final StringBuilder stringBuilder = new StringBuilder();
                String response;
                while ((response = bufferedReader.readLine()) != null) {
                    stringBuilder.append(response);
                }
                bufferedReader.close();

                final JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                final int maxGasPrice = Integer.parseInt(jsonObject.getString("fastest"));
                final float midGasPrice = Float.parseFloat(jsonObject.getString("standard"));

                ApplicationLoader.applicationHandler.post(() -> {
                    gasRec.setText(String.format("В данный момент рекомендуемый gas price: %s GWEI", midGasPrice));
                    gasPriceBar.setMinValue(Config.GAS_PRICE_MIN);
                    gasPriceBar.setMaxValue(maxGasPrice);
                    gasLimitBar.setMinValue(Config.GAS_LIMIT_MIN);
                    gasLimitBar.setMaxValue(Config.GAS_LIMIT_MAX);
                    gasPriceBar.setOnSeekbarChangeListener((minValue) -> {
                        gasPriceValue.setText(String.format("%s GWEI", minValue));
                        updateFeeValue();
                    });
                    gasLimitBar.setOnSeekbarChangeListener((minValue) -> {
                        gasLimitValue.setText(String.format("%s GWEI", minValue));
                        updateFeeValue();
                    });
                });
                ApplicationLoader.applicationHandler.post(() -> dialogProgress.dismiss());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateFeeValue() {
        try {
            String gasPrice = gasPriceValue.getText().toString().replaceAll("\\D+", "");
            String gasLimit = gasLimitValue.getText().toString().replaceAll("\\D+", "");
            BigDecimal bigDecimal = new BigDecimal(gasPrice).divide(new BigDecimal("1000000000")).multiply(new BigDecimal(gasLimit));
            networkFeeValue.setText(String.format("%s ETH", bigDecimal));
            updateTotalValue();
        } catch (Exception e) {

        }
    }

    private void updateTotalValue() {
        String cryptoAmountStr = cryptoAmount.getText().toString().trim().isEmpty() ? "0" : cryptoAmount.getText().toString().trim();
        BigDecimal bigDecimal = new BigDecimal(networkFeeValue.getText().toString().replace(" ", "").replace("ETH", "")).add(new BigDecimal(cryptoAmountStr));
        totalValue.setText(String.format("%s ETH", bigDecimal));
    }
}