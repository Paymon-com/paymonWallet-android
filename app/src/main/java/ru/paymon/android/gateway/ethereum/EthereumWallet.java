package ru.paymon.android.gateway.ethereum;

import org.web3j.crypto.Credentials;

public class EthereumWallet {
    public Credentials credentials;
    public String publicAddress;
    public String privateAddress;
    public String password;
    public String balance;

    public EthereumWallet(Credentials credentials, String password, String balance) {
        this.credentials = credentials;
        this.publicAddress = credentials.getAddress();
        this.privateAddress = credentials.getEcKeyPair().getPrivateKey().toString(16);
        this.password = password;
        this.balance = balance;
    }
}
