package ru.paymon.android.room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;
import java.util.Set;

import ru.paymon.android.gateway.bitcoin.data.AddressBookEntry;

@Dao
public interface AddressBookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(AddressBookEntry addressBookEntry);

    @Query("DELETE FROM address_book WHERE address = :address")
    void delete(String address);

    @Query("SELECT label FROM address_book WHERE address = :address")
    String resolveLabel(String address);

    @Query("SELECT * FROM address_book WHERE address LIKE '%' || :constraint || '%' OR label LIKE '%' || :constraint || '%' ORDER BY label COLLATE LOCALIZED ASC")
    List<AddressBookEntry> get(String constraint);

    @Query("SELECT * FROM address_book ORDER BY label COLLATE LOCALIZED ASC")
    LiveData<List<AddressBookEntry>> getAll();

    @Query("SELECT * FROM address_book WHERE address NOT IN (:except) ORDER BY label COLLATE LOCALIZED ASC")
    LiveData<List<AddressBookEntry>> getAllExcept(Set<String> except);
}
