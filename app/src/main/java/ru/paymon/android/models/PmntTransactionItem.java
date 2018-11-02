package ru.paymon.android.models;

public class PmntTransactionItem extends TransactionItem {
    public String gasLimit;
    public String gasUsed;
    public String gasPrice;

    public PmntTransactionItem(String hash, String time, String value, String to, String from, String gasLimit, String gasUsed, String gasPrice) {
        super(hash, "", time, value, to, from);
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.gasPrice = gasPrice;
    }
}
