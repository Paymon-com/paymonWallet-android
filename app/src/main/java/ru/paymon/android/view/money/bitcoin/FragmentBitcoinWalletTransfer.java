package ru.paymon.android.view.money.bitcoin;

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

import org.bitcoinj.core.Transaction;

import java.util.List;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.activities.QrCodeScannerActivity;
import ru.paymon.android.models.ExchangeRate;
import ru.paymon.android.utils.Utils;

import static android.app.Activity.RESULT_OK;
import static ru.paymon.android.activities.QrCodeScannerActivity.QR_SCAN_RESULT_KEY;
import static ru.paymon.android.activities.QrCodeScannerActivity.REQUEST_CODE_QR_SCANNER_START;
import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;

public class FragmentBitcoinWalletTransfer extends Fragment implements NotificationManager.IListener {
    private WalletApplication application;
    private String currentExchangeRate;
    private NumberPicker fiatCurrencyPicker;
    private TextView fiatEqualTextView;
    private TextView balanceTextView;
    private EditText receiverAddressEditText;
    private IndicatorSeekBar feeSeekBar;
    private double btcAmount;
    private long feeSatoshis;
    private Double feeBtc;
    private double totalValueBtc;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = ((WalletApplication) getActivity().getApplication());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bitcoin_wallet_transfer, container, false);

        fiatCurrencyPicker = (NumberPicker) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_fiat_currency);
        receiverAddressEditText = (EditText) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_receiver_address);
        balanceTextView = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_balance);
        fiatEqualTextView = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_fiat_eq);
        feeSeekBar = (IndicatorSeekBar) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_gas_limit_slider);
        EditText amountEditText = (EditText) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_amount);
        TextView totalTextView = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_total_value);
        FloatingActionButton qrScannerButton = (FloatingActionButton) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_qr);
        TextView fromAddressTextView = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_id_from);
        TextView payButton = (TextView) view.findViewById(R.id.toolbar_btc_wallet_transf_next_text_view);
        TextInputLayout amountInputLayout = (TextInputLayout) view.findViewById(R.id.fragment_bitcoin_amount_input_layout);
        TextInputLayout receiverAddressInputLayout = (TextInputLayout) view.findViewById(R.id.fragment_bitcoin_receiver_address_input_layout);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.toolbar_btc_wallet_transf_back_image_button);

        fiatCurrencyPicker.setMinValue(1);
        fiatCurrencyPicker.setMaxValue(Config.fiatCurrencies.length);
        fiatCurrencyPicker.setDisplayedValues(Config.fiatCurrencies);
        fiatCurrencyPicker.setOnValueChangedListener((NumberPicker picker, int oldVal, int newVal) -> changeCurrency());
        fiatCurrencyPicker.setValue(2);

        final String fromAddress = application.getBitcoinPublicAddress();
        final String balance = application.getBitcoinBalance().toPlainString();

        backButton.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        payButton.setOnClickListener(v -> pay());

        fromAddressTextView.setText(fromAddress);
        balanceTextView.setText(balance + " BTC");

        amountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                final String value = s.toString();

                if (value.startsWith(".")) {
                    amountEditText.setText(null);
                    return;
                }

                if (value.isEmpty()) {
                    amountInputLayout.setError("Обязательное поле для заполнения!");
                    fiatEqualTextView.setVisibility(View.GONE);
                    return;
                }

                btcAmount = Double.parseDouble(value);

                if (btcAmount <= 0.00000546) {
                    amountInputLayout.setError("Не допустимое значение!");
                    fiatEqualTextView.setVisibility(View.GONE);
                    return;
                }

                amountInputLayout.setError(null);

                changeCurrency();
                fiatEqualTextView.setVisibility(View.VISIBLE);
                totalValueBtc = feeBtc + btcAmount;
                totalTextView.setText(String.format("%.8f BTC", totalValueBtc));
            }
        });

        feeSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                feeSatoshis = seekParams.progress * WalletApplication.btcTxSize;
                feeBtc = feeSatoshis / Math.pow(10, 8);
                feeSeekBar.setIndicatorTextFormat(String.format("Fee: %.8f", feeBtc) + "BTC (${PROGRESS} satoshi per byte)");
                totalValueBtc = feeBtc + btcAmount;
                totalTextView.setText(String.format("%.8f BTC", totalValueBtc));
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }
        });

        feeSeekBar.setMin(1);
        feeSeekBar.setMax(100);
        feeSeekBar.setProgress(10);
        feeSeekBar.setIndicatorTextFormat(String.format("Fee: %.8f", feeBtc) + " (${PROGRESS} satoshi per byte)");

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

                if (!Utils.verifyBTCpubKey(value)) {
                    receiverAddressInputLayout.setError("Введеное значение не является BTC адресом!");
                }else{
                    receiverAddressInputLayout.setError(null);
                }
            }
        });


        qrScannerButton.setOnClickListener(v -> {
            Intent qrScannerIntent = new Intent(getContext(), QrCodeScannerActivity.class);
            startActivityForResult(qrScannerIntent, REQUEST_CODE_QR_SCANNER_START);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED);
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED);
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.MONEY_BALANCE_CHANGED) {
            String currency = (String) args[0];
            if (currency.equals(BTC_CURRENCY_VALUE)) {
                String balance = (String) args[1];
                balanceTextView.setText(balance + " BTC");
            }
        }
    }

    private void changeCurrency() {
        final String currentFiatCurrency = fiatCurrencyPicker.getDisplayedValues()[fiatCurrencyPicker.getValue() - 1];
        final List<ExchangeRate> exchangeRates = ApplicationLoader.db.exchangeRatesDao().getExchangeRatesByCryptoCurrecy(BTC_CURRENCY_VALUE);
        for (ExchangeRate exchangeRate : exchangeRates) {
            if (exchangeRate.fiatCurrency.equals(currentFiatCurrency))
                currentExchangeRate = exchangeRate.value;
        }
        final String fiatEqual = WalletApplication.convertBitcoinToFiat(String.valueOf(btcAmount), currentExchangeRate);
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
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMessage("Не удалсоь считать Qr код")
                    .setCancelable(true);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void pay() {
        //TODO:Strings
        final String toAddress = receiverAddressEditText.getText().toString();

        if (btcAmount <= 0.00000546 || toAddress.isEmpty() || !Utils.verifyBTCpubKey(toAddress)) {
            return;
        }

        if (totalValueBtc > application.getBitcoinBalance().value) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMessage("Не достаточно средств")
                    .setCancelable(true);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setMessage(feeSeekBar.getProgress() < 10 ? "Будьте аккуратны, низкое значение комиссии может привести к долгой отправке транзакции. Продолжить?" : "Продолжить?")
                .setCancelable(true)
                .setPositiveButton("Продолжить", (DialogInterface dialog, int which) -> {
                    final long btcAmountToSatoshi = (long) (btcAmount * Math.pow(10, 8));
                    Transaction transaction = application.sendBitcoinTx(toAddress, btcAmountToSatoshi, feeSeekBar.getProgress());
                    if (transaction != null) {
                        String hash = transaction.getHashAsString();
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext())
                                .setMessage("Хэш транзакции " + hash)
                                .setCancelable(true);
                        AlertDialog alertDialog = builder2.create();
                        alertDialog.show();
                    }
                })
                .setNegativeButton("Отмена", (DialogInterface dialog, int which) -> {
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
