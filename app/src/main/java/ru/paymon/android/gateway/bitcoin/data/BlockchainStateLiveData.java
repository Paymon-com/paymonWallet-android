package ru.paymon.android.gateway.bitcoin.data;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import ru.paymon.android.WalletApplication;
import ru.paymon.android.gateway.bitcoin.service.BlockchainService;
import ru.paymon.android.gateway.bitcoin.service.BlockchainState;

/**
 * @author Andreas Schildbach
 */
public class BlockchainStateLiveData extends LiveData<BlockchainState> implements ServiceConnection {
    private final WalletApplication application;
    private final LocalBroadcastManager broadcastManager;

    public BlockchainStateLiveData(final WalletApplication application) {
        this.application = application;
        this.broadcastManager = LocalBroadcastManager.getInstance(application);
    }

    @Override
    protected void onActive() {
        broadcastManager.registerReceiver(receiver, new IntentFilter(BlockchainService.ACTION_BLOCKCHAIN_STATE));
        application.bindService(new Intent(application, BlockchainService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onInactive() {
        application.unbindService(this);
        broadcastManager.unregisterReceiver(receiver);
    }

    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        final BlockchainService blockchainService = ((BlockchainService.LocalBinder) service).getService();
        setValue(blockchainService.getBlockchainState());
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent broadcast) {
            setValue(BlockchainState.fromIntent(broadcast));
        }
    };
}
