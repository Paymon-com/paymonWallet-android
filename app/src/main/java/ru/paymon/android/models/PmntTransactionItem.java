package ru.paymon.android.models;

public class PmntTransactionItem extends EthTransactionItem {
    public String dataMethod;
    public String dataTo;
    public String dataValue;

    public PmntTransactionItem(String hash, String status, String time, String value, String to, String from, String gasLimit, String gasUsed, String gasPrice, String dataMethod, String dataTo, String dataValue) {
        super(hash, status, time, value, to, from, gasLimit, gasUsed, gasPrice);
        this.dataMethod = dataMethod;
        this.dataTo = dataTo;
        this.dataValue = dataValue;
    }
}
