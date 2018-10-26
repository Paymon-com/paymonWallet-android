package ru.paymon.android.models;

public class TransactionItem {
    public String hash;
    public String status;
    public String time;
    public String value;
    public String to;
    public String from;


    public TransactionItem (String hash, String status, String time, String value, String to, String from){
        this.hash = hash;
        this.status = status;
        this.time = time;
        this.value = value;
        this.to = to;
        this.from = from;
    }

}
