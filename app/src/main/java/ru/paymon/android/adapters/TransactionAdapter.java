package ru.paymon.android.adapters;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.List;

import ru.paymon.android.R;
import ru.paymon.android.models.BtcTransactionItem;
import ru.paymon.android.models.EthTransactionItem;
import ru.paymon.android.models.PmntTransactionItem;
import ru.paymon.android.models.TransactionItem;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public List<TransactionItem> transactionItems;
    private String pubAddress;

    public TransactionAdapter(List<TransactionItem> transactionItems) {
        this.transactionItems = transactionItems;
    }

    public TransactionAdapter(List<TransactionItem> transactionItems, String pubAddress) {
        this.transactionItems = transactionItems;
        this.pubAddress = pubAddress;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        RecyclerView.ViewHolder viewHolder = null;
        switch (i) {
            case 0:
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_bitcoin_transaction_item, viewGroup, false);
                viewHolder = new BitcoinTransactionViewHolder(view);
                break;
            case 1:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_ethereum_transaction_item, viewGroup, false);
                viewHolder = new EthereumTransactionViewHolder(view);
                break;
            case 2:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_paymon_transaction_item, viewGroup, false);
                viewHolder = new PaymonTransactionViewHolder(view);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof EthereumTransactionViewHolder) {
            TransactionItem transactionItem = (TransactionItem) transactionItems.get(position);
            ((EthereumTransactionViewHolder) viewHolder).bind(transactionItem);
        } else if (viewHolder instanceof BitcoinTransactionViewHolder) {
            BtcTransactionItem transaction = (BtcTransactionItem) transactionItems.get(position);
            ((BitcoinTransactionViewHolder) viewHolder).bind(transaction);
        } else {
            PmntTransactionItem transaction = (PmntTransactionItem) transactionItems.get(position);
            ((PaymonTransactionViewHolder) viewHolder).bind(transaction);
        }
    }

    @Override
    public int getItemCount() {
        return transactionItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        TransactionItem item = transactionItems.get(position);
        if (item instanceof BtcTransactionItem)
            return 0;
        else if (item instanceof EthTransactionItem)
            return 1;
        else
            return 2;
    }

    public class EthereumTransactionViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout main;
        private TextView hash;
        private TextView status;
        private TextView time;
        private TextView value;
        private TextView to;
        private TextView from;

        public EthereumTransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            main = (ConstraintLayout) itemView.findViewById(R.id.main_layout);
            hash = (TextView) itemView.findViewById(R.id.ethereum_transaction_hash);
            status = (TextView) itemView.findViewById(R.id.ethereum_transaction_status);
            time = (TextView) itemView.findViewById(R.id.ethereum_transaction_time);
            value = (TextView) itemView.findViewById(R.id.ethereum_transaction_value);
            from = (TextView) itemView.findViewById(R.id.ethereum_transaction_from);
            to = (TextView) itemView.findViewById(R.id.ethereum_transaction_to);
        }

        public void bind(final TransactionItem transactionItem) {
            hash.setText(transactionItem.hash);
            status.setText(transactionItem.status);
            time.setText(transactionItem.time);
            value.setText(transactionItem.value);
            from.setText(transactionItem.from);
            final String toStr = transactionItem.to;
            to.setText(toStr);
            main.setBackgroundColor(toStr.equals(pubAddress) ? Color.parseColor("#86DA52") : Color.parseColor("#DA5752"));
        }
    }

    public class PaymonTransactionViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout main;
        private TextView hash;
        private TextView time;
        private TextView value;
        private TextView to;
        private TextView from;

        public PaymonTransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            main = (ConstraintLayout) itemView.findViewById(R.id.main_layout);
            hash = (TextView) itemView.findViewById(R.id.paymon_transaction_hash);
            time = (TextView) itemView.findViewById(R.id.paymon_transaction_time);
            value = (TextView) itemView.findViewById(R.id.paymon_transaction_value);
            from = (TextView) itemView.findViewById(R.id.paymon_transaction_from);
            to = (TextView) itemView.findViewById(R.id.paymon_transaction_to);
        }

        public void bind(final TransactionItem transactionItem) {
            hash.setText(transactionItem.hash);
            time.setText(transactionItem.time);
            value.setText(transactionItem.value);
            from.setText(transactionItem.from);
            final String toStr = transactionItem.to;
            to.setText(toStr);
            main.setBackgroundColor(toStr.equals(pubAddress) ? Color.parseColor("#86DA52") : Color.parseColor("#DA5752"));
        }
    }

    public class BitcoinTransactionViewHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout main;
        private TextView hash;
        private TextView status;
        private TextView time;
        private TextView value;

        public BitcoinTransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            main = (ConstraintLayout) itemView.findViewById(R.id.main_layout);
            hash = (TextView) itemView.findViewById(R.id.bitcoin_transaction_hash);
            status = (TextView) itemView.findViewById(R.id.bitcoin_transaction_status);
            time = (TextView) itemView.findViewById(R.id.bitcoin_transaction_time);
            value = (TextView) itemView.findViewById(R.id.bitcoin_transaction_value);
        }

        public void bind(final BtcTransactionItem transactionItem) {
            hash.setText(transactionItem.hash);
            status.setText(transactionItem.status);
            time.setText(transactionItem.time);
            final long val = Long.parseLong(transactionItem.value);
            value.setText(String.valueOf(val / Math.pow(10, 8)) + " BTC");
            main.setBackgroundColor(val > 0 ? Color.parseColor("#86DA52") : Color.parseColor("#DA5752"));
        }
    }
}
