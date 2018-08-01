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
import ru.paymon.android.models.Wallet;

public class CryptoWalletsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<Wallet> wallets;

    public enum WalletTypes {
        BTC,
        ETH,
        PMNT,
        EMPTY_BTC,
        EMPTY_ETH,
        EMPTY_PMNT
    }

    public CryptoWalletsAdapter(ArrayList<Wallet> wallets){
        this.wallets =wallets;
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
        Wallet wallet = wallets.get(position);

        WalletTypes viewTypes = WalletTypes.values()[holder.getItemViewType()];
        switch (viewTypes) {
            case BTC:
                WalletViewHolder walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_bitcoin);
                walletViewHolder.cryptoCurrency.setText(wallet.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(wallet.cryptoBalance);
                walletViewHolder.fiatCurrency.setText(wallet.fiatCurrency);
                walletViewHolder.fiatBalance.setText(wallet.fiatBalance);
                break;
            case ETH:
                walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_ethereum);
                walletViewHolder.cryptoCurrency.setText(wallet.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(wallet.cryptoBalance);
                walletViewHolder.fiatCurrency.setText(wallet.fiatCurrency);
                walletViewHolder.fiatBalance.setText(wallet.fiatBalance);
                break;
            case PMNT:
                walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_pmnt);
                walletViewHolder.cryptoCurrency.setText(wallet.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(wallet.cryptoBalance);
                walletViewHolder.fiatCurrency.setText(wallet.fiatCurrency);
                walletViewHolder.fiatBalance.setText(wallet.fiatBalance);
                break;
            case EMPTY_BTC:
                EmptyWalletViewHolder emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_bitcoin);
                emptyWallet.create.setOnClickListener(view -> {

                });
                break;
            case EMPTY_ETH:
                emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_ethereum);
                emptyWallet.create.setOnClickListener(view -> {

                });
                break;
            case EMPTY_PMNT:
                emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_pmnt);
                emptyWallet.create.setOnClickListener(view -> {

                });
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        String crpytoCurrency = wallets.get(position).cryptoCurrency;
        boolean isEmpty = wallets.get(position).isEmpty;
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
        return wallets.size();
    }

    class WalletViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView fiatCurrency;
        final TextView cryptoCurrency;
        final TextView fiatBalance;
        final TextView cryptoBalance;

        WalletViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.wallet_icon);
            fiatCurrency = itemView.findViewById(R.id.wallet_fiat_currency);
            cryptoCurrency = itemView.findViewById(R.id.wallet_crypto_currency);
            fiatBalance = itemView.findViewById(R.id.wallet_fiat_balance);
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
