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

import static ru.paymon.android.view.money.bitcoin.FragmentBitcoinWallet.BTC_CURRENCY_VALUE;
import static ru.paymon.android.view.money.ethereum.FragmentEthereumWallet.ETH_CURRENCY_VALUE;
import static ru.paymon.android.view.money.pmnt.FragmentPaymonWallet.PMNT_CURRENCY_VALUE;

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
                walletViewHolder.fiatCurrency.setText(btcWalletItem.fiatCurrency);
                walletViewHolder.fiatBalance.setText(btcWalletItem.fiatBalance);
                walletViewHolder.cryptoCurrency.setText(btcWalletItem.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(btcWalletItem.cryptoBalance);
                walletViewHolder.itemView.setOnClickListener(view -> iOnItemClickListener.onClick(BTC_CURRENCY_VALUE));
                break;
            case ETH:
                NonEmptyWalletItem ethWalletItem = (NonEmptyWalletItem) walletItem;
                walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_ethereum);
                walletViewHolder.fiatCurrency.setText(ethWalletItem.fiatCurrency);
                walletViewHolder.fiatBalance.setText(ethWalletItem.fiatBalance);
                walletViewHolder.cryptoCurrency.setText(ethWalletItem.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(ethWalletItem.cryptoBalance);
                walletViewHolder.itemView.setOnClickListener(view -> iOnItemClickListener.onClick(ETH_CURRENCY_VALUE));
                break;
            case PMNT:
                NonEmptyWalletItem pmntWalletItem = (NonEmptyWalletItem) walletItem;
                walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_pmnt);
                walletViewHolder.fiatCurrency.setText(pmntWalletItem.fiatCurrency);
                walletViewHolder.fiatBalance.setText(pmntWalletItem.fiatBalance);
                walletViewHolder.cryptoCurrency.setText(pmntWalletItem.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(pmntWalletItem.cryptoBalance);
                walletViewHolder.itemView.setOnClickListener(view -> iOnItemClickListener.onClick(PMNT_CURRENCY_VALUE));
                break;
            case EMPTY_BTC:
                EmptyWalletViewHolder emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_bitcoin);
                emptyWallet.create.setOnClickListener(view -> iOnItemClickListener.onCreateClick(BTC_CURRENCY_VALUE));
                break;
            case EMPTY_ETH:
                emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_ethereum);
                emptyWallet.create.setOnClickListener(view -> iOnItemClickListener.onCreateClick(ETH_CURRENCY_VALUE));
                break;
            case EMPTY_PMNT:
                emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_pmnt);
                emptyWallet.create.setOnClickListener(view -> iOnItemClickListener.onCreateClick(PMNT_CURRENCY_VALUE));
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        String crpytoCurrency = walletItems.get(position).cryptoCurrency;
        boolean isEmpty = !(walletItems.get(position) instanceof NonEmptyWalletItem);
        if (!isEmpty) {
            switch (crpytoCurrency) {
                case BTC_CURRENCY_VALUE:
                    return WalletTypes.BTC.ordinal();
                case ETH_CURRENCY_VALUE:
                    return WalletTypes.ETH.ordinal();
                case PMNT_CURRENCY_VALUE:
                    return WalletTypes.PMNT.ordinal();
            }
        } else {
            switch (crpytoCurrency) {
                case BTC_CURRENCY_VALUE:
                    return WalletTypes.EMPTY_BTC.ordinal();
                case ETH_CURRENCY_VALUE:
                    return WalletTypes.EMPTY_ETH.ordinal();
                case PMNT_CURRENCY_VALUE:
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
        final TextView fiatBalance;
        final TextView fiatCurrency;
        final TextView cryptoCurrency;
        final TextView cryptoBalance;

        WalletViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.wallet_icon);
            fiatBalance = itemView.findViewById(R.id.wallet_fiat_balance);
            fiatCurrency = itemView.findViewById(R.id.wallet_fiat_currency);
            cryptoCurrency = itemView.findViewById(R.id.wallet_crypto_currency);
            cryptoBalance = itemView.findViewById(R.id.wallet_crypto_balance);
        }
    }

    class EmptyWalletViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final Button create;

        EmptyWalletViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.wallet_icon);
            create = itemView.findViewById(R.id.wallet_create);
        }
    }
}
