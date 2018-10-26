package ru.paymon.android.models;

public class BtcTransactionItem extends TransactionItem {
    public String sathoshiPerByte;

    public BtcTransactionItem(String hash, String status, String time, String value, String to, String from, String gasLimit, String gasUsed, String gasPrice, String sathoshiPerByte) {
        super(hash, status, time, value, to, from);
        this.sathoshiPerByte = sathoshiPerByte;
    }
}
