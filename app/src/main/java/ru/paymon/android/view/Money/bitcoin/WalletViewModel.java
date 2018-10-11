/*
 * Copyright the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.paymon.android.view.Money.bitcoin;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import org.bitcoinj.wallet.Wallet;

import ru.paymon.android.WalletApplication;
import ru.paymon.android.gateway.bitcoin.Constants;
import ru.paymon.android.gateway.bitcoin.data.AbstractWalletLiveData;
import ru.paymon.android.gateway.bitcoin.util.OnFirstPreDraw;


/**
 * @author Andreas Schildbach
 */
public class WalletViewModel extends AndroidViewModel implements OnFirstPreDraw.Callback {
    public static enum EnterAnimationState {
        WAITING, ANIMATING, FINISHED
    }

    private final WalletApplication application;
    public final WalletEncryptedLiveData walletEncrypted;
    public final MutableLiveData<EnterAnimationState> enterAnimation = new MutableLiveData<>();
    private boolean doAnimation, globalLayoutFinished, balanceLoadingFinished, addressLoadingFinished,
            transactionsLoadingFinished;

    public WalletViewModel(final Application application) {
        super(application);
        this.application = (WalletApplication) application;
        this.walletEncrypted = new WalletEncryptedLiveData(this.application);
    }

    public void animateWhenLoadingFinished() {
        doAnimation = true;
        maybeToggleState();
    }

    @Override
    public boolean onFirstPreDraw() {
        globalLayoutFinished = true;
        maybeToggleState();
        return true;
    }

    public void balanceLoadingFinished() {
        balanceLoadingFinished = true;
        maybeToggleState();
    }

    public void addressLoadingFinished() {
        addressLoadingFinished = true;
        maybeToggleState();
    }

    public void transactionsLoadingFinished() {
        transactionsLoadingFinished = true;
        maybeToggleState();
    }

    public void animationFinished() {
        enterAnimation.setValue(EnterAnimationState.FINISHED);
    }

    private void maybeToggleState() {
        if (enterAnimation.getValue() == null) {
            if (doAnimation && globalLayoutFinished)
                enterAnimation.setValue(EnterAnimationState.WAITING);
        } else if (enterAnimation.getValue() == EnterAnimationState.WAITING) {
            if (balanceLoadingFinished && addressLoadingFinished && transactionsLoadingFinished)
                enterAnimation.setValue(EnterAnimationState.ANIMATING);
        }
    }

    public static class WalletEncryptedLiveData extends AbstractWalletLiveData<Boolean> {
        public WalletEncryptedLiveData(final WalletApplication application) {
            super(application);
        }

        @Override
        protected void onWalletActive(final Wallet wallet) {
            load();
        }

        @Override
        protected void load() {
            final Wallet wallet = getWallet();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    org.bitcoinj.core.Context.propagate(Constants.CONTEXT);
                    postValue(wallet.isEncrypted());
                }
            });
        }
    }
}
