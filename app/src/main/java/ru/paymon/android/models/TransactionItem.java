package ru.paymon.android.models;

public class TransactionItem {
    public String hash;
    public String status;
    public String time;
    public String value;
    public String to;
    public String from;
    public String gasLimit;
    public String gasUsed;
    public String gasPrice;

    public TransactionItem (String hash, String status, String time, String value, String to, String from, String gasLimit, String gasUsed, String gasPrice){
        this.hash = hash;
        this.status = status;
        this.time = time;
        this.value = value;
        this.to = to;
        this.from = from;
        this.gasLimit = gasLimit;
        this.gasUsed = gasUsed;
        this.gasPrice = gasPrice;
    }
}
