package ru.paymon.android.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.models.WalletItem;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.DialogFragmentCreateRestoreEthereumWallet;
import ru.paymon.android.view.FragmentEthereumWallet;

public class CryptoWalletsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<WalletItem> walletItems;
    private AppCompatActivity activity;

    public enum WalletTypes {
        BTC,
        ETH,
        PMNT,
        EMPTY_BTC,
        EMPTY_ETH,
        EMPTY_PMNT
    }

    public CryptoWalletsAdapter(ArrayList<WalletItem> walletItems, Activity activity) {
        this.walletItems = walletItems;
        this.activity = (AppCompatActivity) activity;
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
                WalletViewHolder walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_bitcoin);
                walletViewHolder.publicAddress.setText(walletItem.publicAddress);
                walletViewHolder.cryptoCurrency.setText(walletItem.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(walletItem.cryptoBalance);
                walletViewHolder.fiatCurrency.setText(walletItem.fiatCurrency);
                walletViewHolder.fiatBalance.setText(walletItem.fiatBalance);
                walletViewHolder.itemView.setOnClickListener((view -> {
//                    Utils.replaceFragmentWithAnimationSlideFade(activity.getSupportFragmentManager(), FragmentBitcoinWallet.newInstance(), null);
                }));
                break;
            case ETH:
                walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_ethereum);
                walletViewHolder.publicAddress.setText(walletItem.publicAddress);
                walletViewHolder.cryptoCurrency.setText(walletItem.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(walletItem.cryptoBalance);
                walletViewHolder.fiatCurrency.setText(walletItem.fiatCurrency);
                walletViewHolder.fiatBalance.setText(walletItem.fiatBalance);
                walletViewHolder.itemView.setOnClickListener((view -> {
                    Utils.replaceFragmentWithAnimationSlideFade(activity.getSupportFragmentManager(), FragmentEthereumWallet.newInstance(), null);
                }));
                break;
            case PMNT:
                walletViewHolder = (WalletViewHolder) holder;
                walletViewHolder.icon.setImageResource(R.drawable.ic_pmnt);
                walletViewHolder.publicAddress.setText(walletItem.publicAddress);
                walletViewHolder.cryptoCurrency.setText(walletItem.cryptoCurrency);
                walletViewHolder.cryptoBalance.setText(walletItem.cryptoBalance);
                walletViewHolder.fiatCurrency.setText(walletItem.fiatCurrency);
                walletViewHolder.fiatBalance.setText(walletItem.fiatBalance);
                walletViewHolder.itemView.setOnClickListener((view -> {
//                    Utils.replaceFragmentWithAnimationSlideFade(activity.getSupportFragmentManager(), FragmentPaymonWallet.newInstance(), null);
                }));
                break;
            case EMPTY_BTC:
                EmptyWalletViewHolder emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_bitcoin);
                emptyWallet.create.setOnClickListener(view -> {
//                    DialogFragmentCreateRestoreBitcoinWallet.newInstance().show(activity.getSupportFragmentManager(), null);
                });
                break;
            case EMPTY_ETH:
                emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_ethereum);
                emptyWallet.create.setOnClickListener(view -> {
                    DialogFragmentCreateRestoreEthereumWallet.newInstance().show(activity.getSupportFragmentManager(), null);
                });
                break;
            case EMPTY_PMNT:
                emptyWallet = (EmptyWalletViewHolder) holder;
                emptyWallet.icon.setImageResource(R.drawable.ic_pmnt);
                emptyWallet.create.setOnClickListener(view -> {
//                    DialogFragmentCreateRestorePaymonWallet.newInstance().show(activity.getSupportFragmentManager(), null);
                });
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        String crpytoCurrency = walletItems.get(position).cryptoCurrency;
        boolean isEmpty = walletItems.get(position).isEmpty;
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
        final TextView fiatCurrency;
        final TextView cryptoCurrency;
        final TextView fiatBalance;
        final TextView cryptoBalance;

        WalletViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.wallet_icon);
            publicAddress = itemView.findViewById(R.id.wallet_address);
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
