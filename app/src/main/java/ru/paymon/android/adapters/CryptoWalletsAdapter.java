package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.models.NonEmptyWalletItem;
import ru.paymon.android.models.WalletItem;

public class CryptoWalletsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<WalletItem> walletItems;
    private IOnItemClickListener iOnItemClickListener;

    public interface IOnItemClickListener{
        void onClick(String cryptoCurrency);
        void onCreateClick(String cryptoCurrency);
    }

    public enum WalletTypes {
        BTC,
        ETH,
        PMNT,
        EMPTY_BTC,
        EMPTY_ETH,
        EMPTY_PMNT
    }

    public CryptoWalletsAdapter(ArrayList<WalletItem> walletItems, IOnItemClickListener iOnItemClickListener) {
        this.walletItems = walletItems;
        this.iOnItemClickListener=iOnItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder vh = null;
        WalletTypes viewTypes = WalletTypes.values()[viewType];
        switch (viewTypes) {
            case BTC:
            case ETH:
            case PMNT:
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_wallet, viewGroup, false);
                vh = new WalletViewHolder(view);
                break;
            case EMPTY_BTC:
            case EMPTY_ETH:
            case EMPTY_PMNT:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_wallet_empty, viewGroup, false);
                vh = new EmptyWalletViewHolder(view);
                break;
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        WalletItem walletItem = walletItems.get(position);

        WalletTypes viewTypes = WalletTypes.values()[holder.getItemViewType()];
        switch (viewTypes) {
            case BTC:
                NonEmptyWalletItem btcWalletItem = (NonEmptyWalletItem) walletItem;
                WalletViewHolder walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_bitcoin);
                walletViewHolder.publicAddress.setText(btcWalletItem.publicAddress);
                walletViewHolder.cryptoCurrency.setText(btcWalletItem.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(btcWalletItem.cryptoBalance);
                walletViewHolder.itemView.setOnClickListener(view -> iOnItemClickListener.onClick("BTC"));
                break;
            case ETH:
                NonEmptyWalletItem ethWalletItem = (NonEmptyWalletItem) walletItem;
                walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_ethereum);
                walletViewHolder.publicAddress.setText(ethWalletItem.publicAddress);
                walletViewHolder.cryptoCurrency.setText(ethWalletItem.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(ethWalletItem.cryptoBalance);
                walletViewHolder.itemView.setOnClickListener(view -> iOnItemClickListener.onClick("ETH"));
                break;
            case PMNT:
                NonEmptyWalletItem pmntWalletItem = (NonEmptyWalletItem) walletItem;
                walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_pmnt);
                walletViewHolder.publicAddress.setText(pmntWalletItem.publicAddress);
                walletViewHolder.cryptoCurrency.setText(pmntWalletItem.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(pmntWalletItem.cryptoBalance);
                walletViewHolder.itemView.setOnClickListener(view -> iOnItemClickListener.onClick("PMNT"));
                break;
            case EMPTY_BTC:
                EmptyWalletViewHolder emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_bitcoin);
                emptyWallet.create.setOnClickListener(view -> iOnItemClickListener.onCreateClick("BTC"));
                break;
            case EMPTY_ETH:
                emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_ethereum);
                emptyWallet.create.setOnClickListener(view -> iOnItemClickListener.onCreateClick("ETH"));
                break;
            case EMPTY_PMNT:
                emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_pmnt);
                emptyWallet.create.setOnClickListener(view -> iOnItemClickListener.onCreateClick("PMNT"));
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        String crpytoCurrency = walletItems.get(position).cryptoCurrency;
        boolean isEmpty = !(walletItems.get(position) instanceof NonEmptyWalletItem);
        if (!isEmpty) {
            switch (crpytoCurrency) {
                case "BTC":
                    return WalletTypes.BTC.ordinal();
                case "ETH":
                    return WalletTypes.ETH.ordinal();
                case "PMNT":
                    return WalletTypes.PMNT.ordinal();
            }
        } else {
            switch (crpytoCurrency) {
                case "BTC":
                    return WalletTypes.EMPTY_BTC.ordinal();
                case "ETH":
                    return WalletTypes.EMPTY_ETH.ordinal();
                case "PMNT":
                    return WalletTypes.EMPTY_PMNT.ordinal();
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return walletItems.size();
    }

    class WalletViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView publicAddress;
        final TextView cryptoCurrency;
        final TextView cryptoBalance;

        WalletViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.wallet_icon);
            publicAddress = itemView.findViewById(R.id.wallet_address);
            cryptoCurrency = itemView.findViewById(R.id.wallet_crypto_currency);
            cryptoBalance = itemView.findViewById(R.id.wallet_crypto_balance);
        }
    }

    class EmptyWalletViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView hint;
        final Button create;

        EmptyWalletViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.wallet_icon);
            hint = itemView.findViewById(R.id.wallet_hint);
            create = itemView.findViewById(R.id.wallet_create);
        }
    }
}
