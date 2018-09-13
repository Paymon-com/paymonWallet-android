package ru.paymon.android.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import br.com.sapereaude.maskedEditText.MaskedEditText;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.models.ExchangeRatesItem;
import ru.paymon.android.utils.ExchangeRates;
import ru.paymon.android.utils.Utils;

//import android.support.design.widget.FloatingActionButton;
//import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;

public class FragmentEthereumWalletTransfer extends Fragment {
    private static FragmentEthereumWalletTransfer instance;

    private EditText cryptoAmount;
    private TextView fiatEquivalent;
    private TextView gasRec;
    private TextView titleFrom;
    private TextView idFrom;
    private TextView balance;
    private TextView gasPriceValue;
    private TextView gasLimitValue;
    private TextView networkFeeValue;
    private TextView totalValue;
    private IndicatorSeekBar gasPriceBar;
    private IndicatorSeekBar gasLimitBar;
    private DialogProgress dialogProgress;
    private MaskedEditText receiverAddress;

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

        receiverAddress = (MaskedEditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_receiver_address);
        cryptoAmount = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_amount);
        TextView cryptoAmountTitle = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_amount_title);
        fiatEquivalent = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_fiat_equivalent);
        TextView fiatEquivalentTitle = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_fiat_equivalent_title);
        gasRec = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_price_rec);
        titleFrom = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_title_from);
        idFrom = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_id_from);
        balance = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_balance);
//        FloatingActionButton qr = (FloatingActionButton) view.findViewById(R.gid.fragment_ethereum_wallet_transfer_qr);
        gasPriceBar = (IndicatorSeekBar) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_price_slider);
        gasLimitBar = (IndicatorSeekBar) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_limit_slider);
        gasPriceValue = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_price_value);
        gasLimitValue = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_limit_value);
        networkFeeValue = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_network_fee_value);
        totalValue = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_total_value);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.toolbar_eth_wallet_transf_back_image_button);
        TextView nextButton = (TextView) view.findViewById(R.id.toolbar_eth_wallet_transf_next_text_view);


        backButton.setOnClickListener(view1 -> getActivity().getSupportFragmentManager().popBackStack());

        nextButton.setOnClickListener(view12 -> {
            next();
        });

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

                if (exchangeRates != null) {
                    final float exchangeRate = exchangeRates.get(cryptoCurrency).get(currentFiatCurrency).value;
                    final String fiatEquivalentStr = Ethereum.getInstance().convertEthToFiat(ethAmount, exchangeRate);
                    fiatEquivalent.setText(fiatEquivalentStr);
                } else {
                    fiatEquivalent.setText("Курс получить не удалось");
                }

                updateTotalValue();
            }
        });

        receiverAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //TODO:проверка на ввод ETH адреса
            }
        });

        cryptoAmountTitle.setText(cryptoCurrency);
        fiatEquivalentTitle.setText(currentFiatCurrency);
        if (User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS != null)
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
        Utils.stageQueue.postRunnable(() -> {
            BigDecimal gasprice = Ethereum.getInstance().getNormalGasPrice();
            ApplicationLoader.applicationHandler.post(() -> dialogProgress.show());
            loadWalletBalance();
            loadGasPrice();
            ApplicationLoader.applicationHandler.post(() -> dialogProgress.dismiss());
        });
    }

    private void loadWalletBalance() {
        exchangeRates = ExchangeRates.getInstance().parseExchangeRates();
        BigDecimal walletBalance = Ethereum.getInstance().getBalance();
        ApplicationLoader.applicationHandler.post(() -> {
            if (walletBalance != null)
                balance.setText(String.format("%s ETH", walletBalance));
        });
//        Ethereum.getInstance().getBalance(
//                (responseBalance) -> {
//                    final BigInteger bigInteger = Ethereum.getInstance().jsonToWei(responseBalance);
//                    if (bigInteger != null) {
//                        User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE = Ethereum.getInstance().weiToFriendlyString(bigInteger);
//                        User.saveConfig();
//                        balance.setText(User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE);
//                    }
//                },
//                (error) -> {
//                    ApplicationLoader.applicationHandler.post(() -> Toast.makeText(ApplicationLoader.applicationContext, "Баланс Ethereum кошелька получить не удалось!", Toast.LENGTH_LONG).show());
//                    getActivity().onBackPressed();
//                });
    }

    private void loadGasPrice() {
        ApplicationLoader.applicationHandler.post(() -> {
            gasPriceBar.setOnSeekChangeListener(new OnSeekChangeListener() {
                @Override
                public void onSeeking(SeekParams seekParams) {
                    gasPriceValue.setText(String.format("%s GWEI", seekParams.progress));
                    updateFeeValue();
                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

                }
            });

            gasLimitBar.setOnSeekChangeListener(new OnSeekChangeListener() {
                @Override
                public void onSeeking(SeekParams seekParams) {
                    gasLimitValue.setText(String.format("%s", seekParams.progress));
                    updateFeeValue();
                }

                @Override
                public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

                }
            });
        });

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
            final int maxGasPrice = (int) Float.parseFloat(jsonObject.getString("fastest"));
            final int midGasPrice = (int) Float.parseFloat(jsonObject.getString("standard"));

            ApplicationLoader.applicationHandler.post(() -> {
                gasRec.setText(String.format("В данный момент рекомендуемый gas price: %s GWEI", midGasPrice));
                gasPriceBar.setMin(Config.GAS_PRICE_MIN);
                gasPriceBar.setMax(maxGasPrice);
                gasPriceBar.setProgress(midGasPrice);
                gasPriceBar.setTickCount(maxGasPrice);
            });
        } catch (Exception e) {
            ApplicationLoader.applicationHandler.post(() -> {
                gasRec.setText("Рекомендуемый GAS price получить не удалось");
                gasPriceBar.setMin(Config.GAS_PRICE_MIN);
                gasPriceBar.setMax(100);
                gasPriceBar.setProgress(1);
                gasPriceBar.setTickCount(50);
            });
            e.printStackTrace();
        }
    }

    private void updateFeeValue() {
        try {
            String gasPrice = gasPriceValue.getText().toString().replaceAll("\\D+", "");
            String gasLimit = gasLimitValue.getText().toString().replaceAll("\\D+", "");
            BigDecimal bigDecimal = new BigDecimal(gasPrice).divide(new BigDecimal("1000000000")).multiply(new BigDecimal(gasLimit));
            networkFeeValue.setText(String.format("%s ETH", bigDecimal));
            updateTotalValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTotalValue() {
        String cryptoAmountStr = cryptoAmount.getText().toString().trim().isEmpty() ? "0" : cryptoAmount.getText().toString().trim();
        BigDecimal bigDecimal = new BigDecimal(networkFeeValue.getText().toString().replace(" ", "").replace("ETH", "")).add(new BigDecimal(cryptoAmountStr));
        totalValue.setText(String.format("%s ETH", bigDecimal));
    }

    private void next() {
        //TODO:double поля переписать на BigDecimal
        if (cryptoAmount.getText().toString().isEmpty() || receiverAddress.getText().toString().length() < 41) {
            Toast.makeText(ApplicationLoader.applicationContext, "Заполнены не все поля!", Toast.LENGTH_SHORT).show();
            return;
        }
        BigDecimal total = new BigDecimal(totalValue.getText().toString().replaceAll("ETH", "").trim());
        if (total.compareTo(new BigDecimal(User.CLIENT_MONEY_ETHEREUM_WALLET_BALANCE)) == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("У вас недостаточно средств")
                    .setCancelable(false)
                    .setNegativeButton(R.string.ok, (dialog, which) -> dialog.cancel()).show();
        } else {
            FragmentEthereumWalletTransferInfo fragmentEthereumWalletTransferInfo = FragmentEthereumWalletTransferInfo.newInstance();
            Bundle bundle = new Bundle();
            bundle.putString("TO_ADDRESS", receiverAddress.getText().toString().trim());
            bundle.putString("AMOUNT", new BigDecimal(cryptoAmount.getText().toString()).toString());
            bundle.putString("FEE", new BigDecimal(networkFeeValue.getText().toString().replaceAll("ETH", "").trim()).toString());
            bundle.putString("TOTAL", total.toString());
            bundle.putString("GAS_PRICE", String.valueOf(gasPriceBar.getProgress()));
            bundle.putString("GAS_LIMIT",String.valueOf(gasLimitBar.getProgress()));
            fragmentEthereumWalletTransferInfo.setArguments(bundle);
            Utils.replaceFragmentWithAnimationFade(getActivity().getSupportFragmentManager(), fragmentEthereumWalletTransferInfo, null);
        }
    }
}
