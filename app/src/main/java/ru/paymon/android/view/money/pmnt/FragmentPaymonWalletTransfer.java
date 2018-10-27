package ru.paymon.android.view.money.pmnt;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.shawnlin.numberpicker.NumberPicker;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.Executors;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.activities.QrCodeScannerActivity;
import ru.paymon.android.models.ExchangeRate;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.MoneyViewModel;

import static android.app.Activity.RESULT_OK;
import static ru.paymon.android.activities.QrCodeScannerActivity.QR_SCAN_RESULT_KEY;
import static ru.paymon.android.activities.QrCodeScannerActivity.REQUEST_CODE_QR_SCANNER_START;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

public class FragmentPaymonWalletTransfer extends Fragment {
    private EditText amountEditText;
    private TextView balanceTextView;
    private TextView balanceEthTextView;
    private TextView feeTextView;
    private IndicatorSeekBar gasPriceBar;
    private IndicatorSeekBar gasLimitBar;
    private EditText receiverAddressEditText;
    private NumberPicker fiatCurrencyPicker;
    private TextView fiatEqualTextView;

    private WalletApplication application;
    private MoneyViewModel moneyViewModel;
    private LiveData<BigInteger> paymonBalanceData;
    private LiveData<BigInteger> paymonEthBalanceData;
    private LiveData<Integer> midGasPriceData;
    private LiveData<Integer> maxGasPriceData;
    private String currentExchangeRate;
    private double pmntAmount;
    private double ethFee;
    private int gasPrice;
    private int gasLimit = Config.GAS_LIMIT_CONTRACT_DEFAULT;
    private BigDecimal bigIntegerWeiFee;
    private BigDecimal bigIntegerGweiAmount;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (WalletApplication) getActivity().getApplication();
        moneyViewModel = ViewModelProviders.of(getActivity()).get(MoneyViewModel.class);
        paymonBalanceData = moneyViewModel.getPaymonBalanceData();
        paymonEthBalanceData = moneyViewModel.getPaymonEthBalanceData();
        moneyViewModel.updateMidAndMaxGasPriceData();
        midGasPriceData = moneyViewModel.getMidGasPriceData();
        maxGasPriceData = moneyViewModel.getMaxGasPriceData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paymon_wallet_transfer, container, false);

        receiverAddressEditText = (EditText) view.findViewById(R.id.fragment_paymon_wallet_transfer_receiver_address);
        amountEditText = (EditText) view.findViewById(R.id.fragment_paymon_wallet_transfer_amount);
        fiatCurrencyPicker = (NumberPicker) view.findViewById(R.id.fragment_paymon_wallet_transfer_fiat_currency);
        balanceTextView = (TextView) view.findViewById(R.id.fragment_paymon_wallet_transfer_balance);
        balanceEthTextView = (TextView) view.findViewById(R.id.fragment_paymon_wallet_transfer_eth_balance);
        gasPriceBar = (IndicatorSeekBar) view.findViewById(R.id.fragment_paymon_wallet_transfer_gas_price_slider);
        gasLimitBar = (IndicatorSeekBar) view.findViewById(R.id.fragment_paymon_wallet_transfer_gas_limit_slider);
        feeTextView = (TextView) view.findViewById(R.id.fragment_paymon_wallet_transfer_network_fee_value);
        fiatEqualTextView = (TextView) view.findViewById(R.id.fragment_paymon_wallet_transfer_fiat_eq);
        TextView fromAddressTextView = (TextView) view.findViewById(R.id.fragment_paymon_wallet_transfer_id_from);
        FloatingActionButton qrScannerButton = (FloatingActionButton) view.findViewById(R.id.fragment_paymon_wallet_transfer_qr);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.toolbar_pmnt_wallet_transf_back_image_button);
        TextView payButton = (TextView) view.findViewById(R.id.toolbar_pmnt_wallet_transf_next_text_view);
        TextInputLayout amountInputLayout = (TextInputLayout) view.findViewById(R.id.fragment_paymon_amount_input_layout);
        TextInputLayout receiverAddressInputLayout = (TextInputLayout) view.findViewById(R.id.fragment_paymon_receiver_address_input_layout);

        WalletApplication application = (WalletApplication) getActivity().getApplication();

        fiatCurrencyPicker.setMinValue(1);
        fiatCurrencyPicker.setMaxValue(Config.fiatCurrencies.length);
        fiatCurrencyPicker.setDisplayedValues(Config.fiatCurrencies);
        fiatCurrencyPicker.setOnValueChangedListener((NumberPicker picker, int oldVal, int newVal) -> changeCurrency());
        fiatCurrencyPicker.setValue(2);

        gasPriceBar.setIndicatorTextFormat("Current gas price: ${PROGRESS} GWEI");
        gasLimitBar.setIndicatorTextFormat("Current gas limit: ${PROGRESS}");
        fromAddressTextView.setText(application.getEthereumWallet().publicAddress);

        backButton.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        payButton.setOnClickListener(v -> pay());

        final String fromAddress = application.getEthereumWallet().publicAddress;
        fromAddressTextView.setText(fromAddress);

        receiverAddressEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();

                if (!Utils.verifyETHpubKey(value)) {
                    receiverAddressInputLayout.setError("Введеное значение не является ETH адресом!");
                } else {
                    receiverAddressInputLayout.setError(null);
                }
            }
        });

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                final String value = editable.toString();

                if (value.startsWith(".")) {
                    amountEditText.setText(null);
                    return;
                }

                if (value.isEmpty()) {
                    amountInputLayout.setError("Обязательное поле для заполнения!");
                    fiatEqualTextView.setVisibility(View.GONE);
                    return;
                }

                pmntAmount = Double.parseDouble(value);

                if (pmntAmount <= 0) {
                    amountInputLayout.setError("Недопустимое значение!");
                    fiatEqualTextView.setVisibility(View.GONE);
                    return;
                }

                amountInputLayout.setError(null);

                fiatEqualTextView.setVisibility(View.VISIBLE);
                calculateFees();
                changeCurrency();
            }
        });

        gasPriceBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                gasPrice = seekParams.progress;
                calculateFees();
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
                gasLimit = seekParams.progress;
                calculateFees();
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        paymonEthBalanceData.observe(this, balanceData -> {
            if (balanceData != null)
                balanceEthTextView.setText(String.format("%s ETH", Convert.fromWei(new BigDecimal(balanceData), Convert.Unit.ETHER).toString()));
        });

        paymonBalanceData.observe(this, (balanceData) -> {
            if (balanceData != null)
                balanceTextView.setText(String.format("%s PMNT", Convert.fromWei(new BigDecimal(balanceData), Convert.Unit.GWEI).toString()));
        });

        maxGasPriceData.observe(getActivity(), maxGasPrice -> {
            gasPriceBar.setMin(1);
            gasPriceBar.setMax(maxGasPrice != null ? maxGasPrice : 100);
            gasLimitBar.setMax(Config.GAS_LIMIT_MAX);
            gasLimitBar.setMin(Config.GAS_LIMIT_MIN);
            gasLimitBar.setProgress(Config.GAS_LIMIT_CONTRACT_DEFAULT);
        });

        midGasPriceData.observe(getActivity(), midGasPrice -> {
            if (midGasPrice != null) {
                gasPriceBar.setProgress(midGasPrice);
                gasPrice = midGasPrice;
            } else {
                gasPriceBar.setProgress(10);
                gasPrice = 10;
            }
        });

        qrScannerButton.setOnClickListener(v -> {
            Intent qrScannerIntent = new Intent(getContext(), QrCodeScannerActivity.class);
            startActivityForResult(qrScannerIntent, REQUEST_CODE_QR_SCANNER_START);
        });

        return view;
    }

    private void pay() {
        final String toAddress = receiverAddressEditText.getText().toString();

        if (toAddress.isEmpty() || !Utils.verifyETHpubKey(toAddress)) {
            return;
        }

        if (gasLimit < Config.GAS_LIMIT_CONTRACT_DEFAULT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMessage("Значение Gas Limit меньше 200000, это может привести к неуспешной транзакции и списанной комиссии")
                    .setCancelable(true)
                    .setNegativeButton("Отмена", (DialogInterface dialog, int which) -> {
                    })
                    .setPositiveButton("Продолжить", (DialogInterface dialog, int which) -> send());
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else {
            send();
        }
    }

    private void send() {
        final String toAddress = receiverAddressEditText.getText().toString();
        final BigInteger bigIntegerBalance = moneyViewModel.getPaymonBalanceData().getValue();
        final BigInteger bigIntegerEthBalance = moneyViewModel.getPaymonEthBalanceData().getValue();
        if (bigIntegerBalance != null && bigIntegerEthBalance != null) {
            if (bigIntegerGweiAmount.toBigInteger().compareTo(bigIntegerBalance) == 1 || bigIntegerWeiFee.toBigInteger().compareTo(bigIntegerEthBalance) == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setMessage("Не достаточно средств")
                        .setCancelable(true);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                final BigInteger bigIntegerGasPrice = new BigDecimal(gasPrice).multiply(new BigDecimal(Math.pow(10, 9))).toBigInteger();
                final BigInteger bigIntegerGasLimit = new BigDecimal(gasLimit).toBigInteger();
                Executors.newSingleThreadExecutor().submit(() -> {
                    TransactionReceipt transactionReceipt = application.sendPmntContract(toAddress, bigIntegerGweiAmount.toBigInteger(), bigIntegerGasPrice, bigIntegerGasLimit);
                    ApplicationLoader.applicationHandler.post(() -> {
                        final String text = transactionReceipt != null ? "Хэш транзакции: " + transactionReceipt.getTransactionHash() : "Транзакцию отправить не удалось";
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext())
                                .setMessage(text)
                                .setCancelable(true);
                        AlertDialog alertDialog = builder2.create();
                        alertDialog.show();
                    });
                });
            }
        }
    }

    private void calculateFees() {
        BigDecimal bigDecimalWeiGasPrice = Convert.toWei(new BigDecimal(gasPrice), Convert.Unit.GWEI);
        BigDecimal bigDecimalGasLimit = new BigDecimal(gasLimit);

        bigIntegerWeiFee = bigDecimalWeiGasPrice.multiply(bigDecimalGasLimit);
        ethFee = Double.parseDouble(Convert.fromWei(bigIntegerWeiFee, Convert.Unit.ETHER).toString());

        feeTextView.setText(String.format("%.9f ETH", ethFee));

        if (pmntAmount != 0)
            bigIntegerGweiAmount = new BigDecimal(pmntAmount).multiply(new BigDecimal(Math.pow(10, 9)));
    }

    private void changeCurrency() {
        if(bigIntegerGweiAmount == null) return;
        final String currentFiatCurrency = fiatCurrencyPicker.getDisplayedValues()[fiatCurrencyPicker.getValue() - 1];
        final List<ExchangeRate> exchangeRates = ApplicationLoader.db.exchangeRatesDao().getExchangeRatesByCryptoCurrecy(PMNT_CURRENCY_VALUE);
        for (ExchangeRate exchangeRate : exchangeRates) {
            if (exchangeRate.fiatCurrency.equals(currentFiatCurrency))
                currentExchangeRate = exchangeRate.value;
        }
        final String fiatEqual = WalletApplication.convertEthereumToFiat(bigIntegerGweiAmount.toBigInteger(), currentExchangeRate);
        fiatEqualTextView.setText(String.format("%s %s", fiatEqual, currentFiatCurrency));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getExtras() != null) {
                    if (data.getExtras().containsKey(QR_SCAN_RESULT_KEY)) {
                        String receiverAddress = data.getStringExtra(QR_SCAN_RESULT_KEY);
                        receiverAddressEditText.setText(receiverAddress);
                    }
                }
            }
        } else {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext())
                    .setMessage("Не удалсоь считать Qr код")
                    .setCancelable(true);
            android.support.v7.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
