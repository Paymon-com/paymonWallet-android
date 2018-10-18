//package ru.paymon.android.gateway.bitcoin.data;
//
//
//import android.arch.lifecycle.LiveData;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
//import android.database.Cursor;
//import android.support.v4.content.CursorLoader;
//
//import ru.paymon.android.WalletApplication;
//import ru.paymon.android.gateway.bitcoin.Configuration;
//
///**
// * @author Andreas Schildbach
// */
//public class SelectedExchangeRateLiveData extends LiveData<ExchangeRate> implements OnSharedPreferenceChangeListener {
//    private final Configuration config;
//    private final CursorLoader loader;
//
//    public SelectedExchangeRateLiveData(final WalletApplication application) {
//        this.config = application.getConfiguration();
//        this.loader = new CursorLoader(application,
//                ExchangeRatesProvider.contentUri(application.getPackageName(), false), null,
//                ExchangeRatesProvider.KEY_CURRENCY_CODE, new String[] { null }, null) {
//            @Override
//            public void deliverResult(final Cursor cursor) {
//                if (cursor != null && cursor.getCount() > 0) {
//                    cursor.moveToFirst();
//                    setValue(ExchangeRatesProvider.getExchangeRate(cursor));
//                }
//            }
//        };
//    }
//
//    @Override
//    protected void onActive() {
//        loader.startLoading();
//        config.registerOnSharedPreferenceChangeListener(this);
//        onCurrencyChange();
//    }
//
//    @Override
//    protected void onInactive() {
//        config.unregisterOnSharedPreferenceChangeListener(this);
//        loader.stopLoading();
//    }
//
//    @Override
//    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
//        if (Configuration.PREFS_KEY_EXCHANGE_CURRENCY.equals(key))
//            onCurrencyChange();
//    }
//
//    private void onCurrencyChange() {
//        final String exchangeCurrency = config.getExchangeCurrencyCode();
//        loader.setSelectionArgs(new String[] { exchangeCurrency });
//        loader.forceLoad();
//    }
//}
