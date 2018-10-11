package ru.paymon.android.view.Money.bitcoin;

import android.content.Context;

import org.bitcoinj.core.Coin;

import javax.annotation.Nullable;

import ru.paymon.android.gateway.bitcoin.data.FeeCategory;

public final class SendCoinsActivity extends AbstractWalletActivity {
//    public static final String INTENT_EXTRA_PAYMENT_INTENT = "payment_intent";
//    public static final String INTENT_EXTRA_FEE_CATEGORY = "fee_category";
//
//    public static void start(final Context context, final PaymentIntent paymentIntent,
//                             final @Nullable FeeCategory feeCategory, final int intentFlags) {
//        final Intent intent = new Intent(context, SendCoinsActivity.class);
//        intent.putExtra(INTENT_EXTRA_PAYMENT_INTENT, paymentIntent);
//        if (feeCategory != null)
//            intent.putExtra(INTENT_EXTRA_FEE_CATEGORY, feeCategory);
//        if (intentFlags != 0)
//            intent.setFlags(intentFlags);
//        context.startActivity(intent);
//    }
//
//    public static void start(final Context context, final PaymentIntent paymentIntent) {
//        start(context, paymentIntent, null, 0);
//    }
//
    public static void startDonate(final Context context, final Coin amount, final @Nullable FeeCategory feeCategory,
                                   final int intentFlags) {
//        start(context, PaymentIntent.from(Constants.DONATION_ADDRESS,
//                context.getString(R.string.wallet_donate_address_label), amount), feeCategory, intentFlags);
    }
//
//    @Override
//    protected void onCreate(final Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.send_coins_content);
//
//        BlockchainService.start(this, false);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(final Menu menu) {
//        getMenuInflater().inflate(R.menu.send_coins_activity_options, menu);
//
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(final MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.send_coins_options_help:
//                HelpDialogFragment.page(getSupportFragmentManager(), R.string.help_send_coins);
//                return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}