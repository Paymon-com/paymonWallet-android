package ru.paymon.android.gateway.bitcoin.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity(tableName = "address_book")
public class AddressBookEntry {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "label")
    private String label;

    public AddressBookEntry(final String address, final String label) {
        this.address = address;
        this.label = label;
    }

    public String getAddress() {
        return address;
    }

    public String getLabel() {
        return label;
    }

    public static Map<String, AddressBookEntry> asMap(final List<AddressBookEntry> entries) {
        if (entries == null)
            return null;
        final Map<String, AddressBookEntry> addressBook = new HashMap<>();
        for (final AddressBookEntry entry : entries)
            addressBook.put(entry.getAddress(), entry);
        return addressBook;
    }
}