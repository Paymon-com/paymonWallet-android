package ru.paymon.android.models;

public class EthTransactionItem extends TransactionItem {
    public String gasLimit;
    public String gasUsed;
    public String gasPrice;

    public EthTransactionItem(String hash, String status, String time, String value, String to, String from, String gasLimit, String gasUsed, String gasPrice) {
        super(hash, status, time, value, to, from);
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.gasPrice = gasPrice;
    }
}
