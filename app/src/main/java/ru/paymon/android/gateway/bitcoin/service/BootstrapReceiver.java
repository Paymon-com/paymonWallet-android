package ru.paymon.android.gateway.bitcoin.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.paymon.android.WalletApplication;
import ru.paymon.android.gateway.bitcoin.Configuration;
import ru.paymon.android.gateway.bitcoin.Constants;

/**
 * @author Andreas Schildbach
 */
public class BootstrapReceiver extends BroadcastReceiver {
    private static final Logger log = LoggerFactory.getLogger(BootstrapReceiver.class);

    @Override
    public void onReceive(final Context context, final Intent intent) {
        log.info("got broadcast: " + intent);

        final WalletApplication application = (WalletApplication) context.getApplicationContext();

        final boolean bootCompleted = Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction());
        final boolean packageReplaced = Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction());

        if (packageReplaced || bootCompleted) {
            // make sure wallet is upgraded to HD
            if (packageReplaced)
                UpgradeWalletService.startUpgrade(context);

            // make sure there is always an alarm scheduled
            BlockchainService.scheduleStart(application);

            // if the app hasn't been used for a while and contains coins, maybe show reminder
            final Configuration config = application.getConfiguration();
            if (config.remindBalance() && config.hasBeenUsed()
                    && config.getLastUsedAgo() > Constants.LAST_USAGE_THRESHOLD_INACTIVE_MS)
                InactivityNotificationService.startMaybeShowNotification(context);
        }
    }
}
