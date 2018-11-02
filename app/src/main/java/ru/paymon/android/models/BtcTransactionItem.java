package ru.paymon.android.models;

public class BtcTransactionItem extends TransactionItem {
    public String fee;

    public BtcTransactionItem(String hash, String status, String time, String value, String fee) {
        super(hash, status, time, value, "", "");
        this.fee = fee;
    }
}
