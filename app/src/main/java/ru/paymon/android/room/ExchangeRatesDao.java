package ru.paymon.android.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import ru.paymon.android.models.ExchangeRate;

@Dao
public interface ExchangeRatesDao {

    @Query("SELECT * FROM exchangerate")
    List<ExchangeRate> getExchangeRates();

    @Query("SELECT * FROM exchangerate WHERE fiatCurrency = :fiatCurrency")
    List<ExchangeRate> getExchangeRatesByFiatCurrecy(final String fiatCurrency);

    @Query("SELECT * FROM exchangerate WHERE cryptoCurrency = :cryptoCurrency")
    List<ExchangeRate> getExchangeRatesByCryptoCurrecy(final String cryptoCurrency);

    @Query("SELECT * FROM exchangerate WHERE fiatCurrency = :fiatCurrency AND cryptoCurrency = :cryptoCurrency")
    ExchangeRate getExchangeRatesByFiatAndCryptoCurrecy(final String fiatCurrency, final String cryptoCurrency);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertList(final List<ExchangeRate> exchangeRates);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(final ExchangeRate exchangeRate);

    @Delete
    void delete(final ExchangeRate exchangerate);

    @Query("DELETE FROM exchangerate")
    void deleteAll();
}
