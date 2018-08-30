package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.models.ExchangeRatesItem;

public class ExchangeRatesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<ExchangeRatesItem> exchangeRatesItems;

    public ExchangeRatesAdapter(ArrayList<ExchangeRatesItem> exchangeRatesItems) {
        this.exchangeRatesItems = exchangeRatesItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_exchange_rate, viewGroup, false);
        RecyclerView.ViewHolder vh = new ExchangeRatesViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ExchangeRatesItem exchangeRatesItem = exchangeRatesItems.get(position);
        ExchangeRatesViewHolder exchangeRatesViewHolder = (ExchangeRatesViewHolder) holder;
        exchangeRatesViewHolder.cryptoCurrency.setText(exchangeRatesItem.cryptoCurrency);
        exchangeRatesViewHolder.fiatCurrency.setText(exchangeRatesItem.fiatCurrency);
        exchangeRatesViewHolder.fiatAmount.setText(String.valueOf(exchangeRatesItem.value));
    }

    @Override
    public int getItemCount() {
        return exchangeRatesItems.size();
    }

    class ExchangeRatesViewHolder extends RecyclerView.ViewHolder {
        public final TextView cryptoCurrency;
        public final TextView fiatCurrency;
        public final TextView fiatAmount;

        public ExchangeRatesViewHolder(View itemView) {
            super(itemView);
            cryptoCurrency = itemView.findViewById(R.id.crypto_currency);
            fiatAmount = itemView.findViewById(R.id.fiat_amount);
            fiatCurrency = itemView.findViewById(R.id.fiat_currency);
        }
    }
}
