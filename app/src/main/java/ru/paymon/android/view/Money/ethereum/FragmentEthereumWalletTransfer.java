package ru.paymon.android.view.Money.ethereum;

import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModelProviders;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.gateway.Ethereum;
import ru.paymon.android.models.ExchangeRatesItem;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.MoneyViewModel;

public class FragmentEthereumWalletTransfer extends Fragment {
    private static FragmentEthereumWalletTransfer instance;

    private EditText cryptoAmount;
    private TextView fiatEquivalent;
    private TextView titleFrom;
    private TextView idFrom;
    private TextView balance;
    private TextView networkFeeValueView;
    private TextView totalValue;
    private IndicatorSeekBar gasPriceBar;
    private IndicatorSeekBar gasLimitBar;
//    private DialogProgress dialogProgress;
    private EditText receiverAddress;

    private HashMap<String, HashMap<String, ExchangeRatesItem>> exchangeRates = new HashMap<>();
    private String currentFiatCurrency = "USD";//TODO:
    private final String cryptoCurrency = "ETH";
    private MoneyViewModel moneyViewModel;
    private LiveData<String> ethereumBalanceData;
    private LiveData<Integer> midGasPriceData;
    private LiveData<Integer> maxGasPriceData;
    private LiveData<ArrayList<ExchangeRatesItem>> exchangeRatesData;
    private MutableLiveData<Integer> gasPriceValue = new MutableLiveData<>();
    private MutableLiveData<Integer> gasLimitValue = new MutableLiveData<>();
    private MutableLiveData<BigDecimal> cryptoAmountValue = new MutableLiveData<>();
    private MutableLiveData<BigDecimal> totalAmountValue = new MutableLiveData<>();
    private MutableLiveData<BigDecimal> networkFeeValue = new MutableLiveData<>();

    public static FragmentEthereumWalletTransfer newInstance() {
        if (instance == null)
            instance = new FragmentEthereumWalletTransfer();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moneyViewModel = ViewModelProviders.of(getActivity()).get(MoneyViewModel.class);
        ethereumBalanceData = moneyViewModel.getEthereumBalanceData();
        exchangeRatesData = moneyViewModel.getExchangeRatesData();
        moneyViewModel.updateMidAndMaxGasPriceData();
        midGasPriceData = moneyViewModel.getMidGasPriceData();
        maxGasPriceData = moneyViewModel.getMaxGasPriceData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ethereum_wallet_transfer, container, false);

        receiverAddress = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_receiver_address);
        cryptoAmount = (EditText) view.findViewById(R.id.fragment_ethereum_wallet_transfer_amount);
        TextView cryptoAmountTitle = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_amount_title);
        fiatEquivalent = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_fiat_equivalent);
        TextView fiatEquivalentTitle = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_fiat_equivalent_title);
        titleFrom = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_title_from);
        idFrom = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_id_from);
        balance = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_balance);
//        FloatingActionButton qr = (FloatingActionButton) view.findViewById(R.gid.fragment_ethereum_wallet_transfer_qr);
        gasPriceBar = (IndicatorSeekBar) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_price_slider);
        gasLimitBar = (IndicatorSeekBar) view.findViewById(R.id.fragment_ethereum_wallet_transfer_gas_limit_slider);
        networkFeeValueView = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_network_fee_value);
        totalValue = (TextView) view.findViewById(R.id.fragment_ethereum_wallet_transfer_total_value);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.toolbar_eth_wallet_transf_back_image_button);
        TextView nextButton = (TextView) view.findViewById(R.id.toolbar_eth_wallet_transf_next_text_view);

        gasPriceBar.setIndicatorTextFormat("Current gas price: ${PROGRESS} GWEI");
        gasLimitBar.setIndicatorTextFormat("Current gas limit: ${PROGRESS}");
        cryptoAmountTitle.setText(cryptoCurrency);
        fiatEquivalentTitle.setText(currentFiatCurrency);
        if (User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS != null)
            idFrom.setText(User.CLIENT_MONEY_ETHEREUM_WALLET_PUBLIC_ADDRESS);
        backButton.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        nextButton.setOnClickListener(v -> openNextFragment());
        cryptoAmount.addTextChangedListener(cryptoAmountTextWatcher);
        gasPriceBar.setOnSeekChangeListener(gasPriceListener);
        gasLimitBar.setOnSeekChangeListener(gasLimitListener);
//        dialogProgress = new DialogProgress(getContext());
//        dialogProgress.setCancelable(false);
        ethereumBalanceData.observe(getActivity(), (balanceData) -> balance.setText(String.format("%s ETH", balanceData)));
        maxGasPriceData.observe(getActivity(), maxGasPrice -> {
            if(maxGasPrice == null) return;
            gasPriceBar.setMax(maxGasPrice);
            gasLimitBar.setMax(Config.GAS_LIMIT_MAX);
            gasLimitBar.setMin(Config.GAS_LIMIT_MIN);
            gasLimitBar.setProgress(Config.GAS_LIMIT_DEFAULT);
            gasLimitValue.postValue(Config.GAS_LIMIT_DEFAULT);
        });
        midGasPriceData.observe(getActivity(), midGasPrice -> {
            if(midGasPrice == null) return;
            gasPriceBar.setProgress(midGasPrice);
            gasPriceValue.postValue(midGasPrice);
        });
        gasLimitValue.observe(getActivity(), gasLimit -> calculateFees());
        gasPriceValue.observe(getActivity(), gasPrice -> calculateFees());
        cryptoAmountValue.observe(getActivity(), amount -> calculateFees());
        totalAmountValue.observe(getActivity(), totalVal -> totalValue.setText(String.format("%s ETH", totalVal)));
        networkFeeValue.observe(getActivity(), fee -> networkFeeValueView.setText(String.format("%s ETH", fee)));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void openNextFragment() {
        if (cryptoAmount.getText().toString().isEmpty()) {
            Toast.makeText(ApplicationLoader.applicationContext, "Заполнены не все поля!", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!Utils.verifyETHpubKey(receiverAddress.getText().toString())){
            Toast.makeText(ApplicationLoader.applicationContext, "Адрес получателя введен не верно!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (totalAmountValue.getValue() != null && totalAmountValue.getValue().compareTo(new BigDecimal(ethereumBalanceData.getValue())) == 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("У вас недостаточно средств")
                    .setCancelable(false)
                    .setNegativeButton(R.string.ok, (dialog, which) -> dialog.cancel()).show();
        } else {
            if(networkFeeValue.getValue() == null || cryptoAmountValue.getValue() == null
                    || gasLimitValue.getValue() == null || gasPriceValue.getValue() == null) return;
            Bundle bundle = new Bundle();
            bundle.putString("TO_ADDRESS", receiverAddress.getText().toString().trim());
            bundle.putString("AMOUNT", cryptoAmountValue.getValue().toString());
            bundle.putString("FEE", networkFeeValue.getValue().toString());
            bundle.putString("TOTAL", totalAmountValue.getValue().toString());
            bundle.putString("GAS_PRICE", gasPriceValue.getValue().toString());
            bundle.putString("GAS_LIMIT", gasLimitValue.getValue().toString());
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentEthereumWalletTransferInfo, bundle);
        }
    }

    private void calculateFees() {
        BigDecimal bigDecimalFee = null;
        if (gasPriceValue.getValue() != null && gasLimitValue.getValue() != null) {
            bigDecimalFee = new BigDecimal(gasPriceValue.getValue()).divide(new BigDecimal("1000000000")).multiply(new BigDecimal(gasLimitValue.getValue()));
            networkFeeValue.postValue(bigDecimalFee);
        }
        if (cryptoAmountValue.getValue() != null && bigDecimalFee != null) {
            BigDecimal cryptoAmount = cryptoAmountValue.getValue() == null ? new BigDecimal(0) : cryptoAmountValue.getValue();
            BigDecimal bigDecimalTotalFee = bigDecimalFee.add(cryptoAmount);
            totalAmountValue.postValue(bigDecimalTotalFee);
        } else {
            totalAmountValue.postValue(bigDecimalFee);
        }
    }

    private OnSeekChangeListener gasLimitListener = new OnSeekChangeListener() {
        @Override
        public void onSeeking(SeekParams seekParams) {
            gasLimitValue.postValue(seekParams.progress);
        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

        }
    };

    private OnSeekChangeListener gasPriceListener = new OnSeekChangeListener() {
        @Override
        public void onSeeking(SeekParams seekParams) {
            gasPriceValue.postValue(seekParams.progress);
        }

        @Override
        public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

        }
    };

    private TextWatcher cryptoAmountTextWatcher = new TextWatcher() {
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
                cryptoAmountValue.postValue(new BigDecimal(0));
                return;
            }

            if (ethAmount.startsWith(".")) {
                cryptoAmount.setText(null);
                return;
            }

            if (exchangeRates != null) {
                if (exchangeRatesData.getValue() != null) {
                    for (ExchangeRatesItem exRateItem : exchangeRatesData.getValue()) {
                        if (exRateItem.fiatCurrency.equals(currentFiatCurrency) && exRateItem.cryptoCurrency.equals(cryptoCurrency)) {
                            final String fiatEquivalentStr = Ethereum.getInstance().convertEthToFiat(ethAmount, exRateItem.value);
                            fiatEquivalent.setText(fiatEquivalentStr);
                        }
                    }
                }

            } else {
                fiatEquivalent.setText("Курс получить не удалось");
            }

            cryptoAmountValue.postValue(new BigDecimal(ethAmount));
        }
    };
}
