package ru.paymon.android.view.money.bitcoin;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.shawnlin.numberpicker.NumberPicker;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.bitcoinj.core.Transaction;

import java.util.List;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.WalletApplication;
import ru.paymon.android.gateway.exchangerates.ExchangeRate;

import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;

public class FragmentBitcoinWalletTransfer extends Fragment implements NotificationManager.IListener {
    private WalletApplication application;
    private String currentFiatCurrency;
    private String currentExchangeRate;
    private NumberPicker fiatCurrencyPicker;
    private TextView fiatEqualTextView;
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

        EditText amountEditText = (EditText) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_amount);
        fiatCurrencyPicker = (NumberPicker) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_fiat_currency);
        IndicatorSeekBar feeSeekBar = (IndicatorSeekBar) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_gas_limit_slider);
        TextView totalTextView = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_total_value);
        EditText receiverAddressEditText = (EditText) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_receiver_address);
        FloatingActionButton qrScannerButton = (FloatingActionButton) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_qr);
        TextView fromAddressTextView = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_id_from);
        TextView balanceTextView = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_balance);
        TextView nextButtonTextView = (TextView) view.findViewById(R.id.toolbar_btc_wallet_transf_next_text_view);
        fiatEqualTextView = (TextView) view.findViewById(R.id.fragment_bitcoin_wallet_transfer_fiat_eq);
        TextInputLayout textInputLayout = (TextInputLayout) view.findViewById(R.id.textInputLayout);

        fiatCurrencyPicker.setMinValue(1);
        fiatCurrencyPicker.setMaxValue(Config.fiatCurrencies.length);
        fiatCurrencyPicker.setDisplayedValues(Config.fiatCurrencies);
        fiatCurrencyPicker.setOnValueChangedListener((NumberPicker picker, int oldVal, int newVal) -> changeCurrency());
        fiatCurrencyPicker.setValue(2);

        final String fromAddress = application.getBitcoinPublicAddress();
        final String balance = application.getBitcoinBalance().toPlainString();

        fromAddressTextView.setText(fromAddress);
        balanceTextView.setText(balance);

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

                if (value.isEmpty()) {
                    textInputLayout.setError("Обязательное поле для заполнения!");
                    fiatEqualTextView.setVisibility(View.GONE);
                    return;
                }

                btcAmount = Double.parseDouble(value);

                if (btcAmount <= 0.0001) {
                    textInputLayout.setError("Не допустимое значение!");
                    fiatEqualTextView.setVisibility(View.GONE);
                    return;
                }

                textInputLayout.setError(null);

                changeCurrency();
                fiatEqualTextView.setVisibility(View.VISIBLE);
                totalValueBtc = feeBtc +  btcAmount;
                totalTextView.setText(String.format("%.8f BTC", totalValueBtc));
            }
        });

        feeSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                feeSatoshis = seekParams.progress * WalletApplication.btcTxSize;
                feeBtc = feeSatoshis / Math.pow(10, 8);
                feeSeekBar.setIndicatorTextFormat(String.format("Fee: %.8f", feeBtc) + "BTC (${PROGRESS} satoshi per byte)");
                totalValueBtc = feeBtc +  btcAmount;
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
                final String destinationAddress = s.toString();

                if (destinationAddress.isEmpty()) //TODO: проверка на валидность
                    return;
            }
        });

        nextButtonTextView.setOnClickListener(v -> {
            final String toAddress = receiverAddressEditText.getText().toString();

            if (btcAmount <= 0.0001 || toAddress.isEmpty()) {
                return;
            }

            if(totalValueBtc > application.getBitcoinBalance().value){
                //TODO:алерт что недостаточно средств
                return;
            }

            if(feeSeekBar.getProgress() < 10){
                //TODO: алерт с текстом (чтобы юзер был вкурсе, что транзация будет идти долго)
            }

            final long btcAmountToSatoshi = (long) (btcAmount * Math.pow(10, 8));
            Transaction transaction = application.sendBitcoinTx(toAddress, btcAmountToSatoshi, feeSeekBar.getProgress());
            if(transaction != null){
                //TODO: выдача алерта о подтверждении, результат (Хэш)
                String hash = transaction.getHashAsString();
            }
        });

        qrScannerButton.setOnClickListener(v->{
            //TODO:вызов QR сканнера и занос резульата в строку получателя
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
            //TODO:обновление баланса
        }
    }

    private void changeCurrency() {
        currentFiatCurrency = fiatCurrencyPicker.getDisplayedValues()[fiatCurrencyPicker.getValue() - 1];
        List<ExchangeRate> exchangeRates = ApplicationLoader.db.exchangeRatesDao().getExchangeRatesByCryptoCurrecy(BTC_CURRENCY_VALUE);
        for (ExchangeRate exchangeRate : exchangeRates) {
            if (exchangeRate.fiatCurrency.equals(currentFiatCurrency))
                currentExchangeRate = exchangeRate.value;
        }
        final String fiatEqual = WalletApplication.convertBitcoinToFiat(String.valueOf(btcAmount), currentExchangeRate);
        fiatEqualTextView.setText(String.format("%s %s", fiatEqual, currentFiatCurrency));
    }
}
