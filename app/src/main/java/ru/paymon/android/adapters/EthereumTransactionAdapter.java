package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.models.TransactionItem;

public class EthereumTransactionAdapter extends RecyclerView.Adapter<EthereumTransactionAdapter.ViewHolder> {
    public ArrayList<TransactionItem> transactionItems;

    public EthereumTransactionAdapter(ArrayList<TransactionItem> transactionItems){
        this.transactionItems = transactionItems;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_history_transaction_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        TransactionItem transactionItem = transactionItems.get(position);
        viewHolder.hash.setText(transactionItem.hash);
        viewHolder.status.setText(transactionItem.status);
        viewHolder.time.setText(transactionItem.time);
        viewHolder.value.setText(transactionItem.value);
        viewHolder.to.setText(transactionItem.to);
    }

    @Override
    public int getItemCount() {
        return transactionItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView hash;
        private TextView status;
        private TextView time;
        private TextView value;
        private TextView to;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            hash = (TextView) itemView.findViewById(R.id.ethereum_transaction_hash);
            status = (TextView) itemView.findViewById(R.id.ethereum_transaction_status);
            time = (TextView) itemView.findViewById(R.id.ethereum_transaction_time);
            value = (TextView) itemView.findViewById(R.id.ethereum_transaction_value);
            to = (TextView) itemView.findViewById(R.id.ethereum_transaction_to);
        }
    }
}
