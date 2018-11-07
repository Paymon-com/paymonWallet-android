package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.paymon.android.R;
import ru.paymon.android.models.ExchangeRate;


public class ExchangeRatesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public List<ExchangeRate> exchangeRatesItems;

    public ExchangeRatesAdapter(List<ExchangeRate> exchangeRatesItems) {
        this.exchangeRatesItems = exchangeRatesItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_exchange_rate, viewGroup, false);
        return new ExchangeRatesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ExchangeRate exchangeRatesItem = exchangeRatesItems.get(position);
        ExchangeRatesViewHolder exchangeRatesViewHolder = (ExchangeRatesViewHolder) holder;
        exchangeRatesViewHolder.cryptoCurrency.setText(exchangeRatesItem.cryptoCurrency);
        exchangeRatesViewHolder.fiatAmount.setText(String.format("%s %s", exchangeRatesItem.value, exchangeRatesItem.fiatCurrency));
        switch (exchangeRatesItem.cryptoCurrency){
            case "BTC":
                exchangeRatesViewHolder.icon.setImageResource(R.drawable.bitcoin_chart);
                break;
            case "ETH":
                exchangeRatesViewHolder.icon.setImageResource(R.drawable.ethereum_chart);
                break;
            case "PMNT":
                exchangeRatesViewHolder.icon.setImageResource(R.drawable.paymon_chart);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return exchangeRatesItems.size();
    }

    class ExchangeRatesViewHolder extends RecyclerView.ViewHolder {
        public final ImageView icon;
        public final TextView cryptoCurrency;
        public final TextView fiatAmount;

        public ExchangeRatesViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.currency_rate_image_view);
            cryptoCurrency = itemView.findViewById(R.id.crypto_currency);
            fiatAmount = itemView.findViewById(R.id.fiat_amount);
        }
    }
}
