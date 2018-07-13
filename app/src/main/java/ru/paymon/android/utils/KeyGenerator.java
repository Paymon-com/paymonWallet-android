package ru.paymon.android.utils;

import android.util.Log;


import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

import ru.paymon.android.Config;
import ru.paymon.android.net.RPC;


public class KeyGenerator {
    private static volatile KeyGenerator Instance = null;

    public static KeyGenerator getInstance() {
        KeyGenerator localInstance = Instance;
        if (localInstance == null) {
            synchronized (KeyGenerator.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new KeyGenerator();
                }
            }
        }
        return localInstance;
    }

    private KeyGenerator() {

    }

    public void dispose(){
        Instance= null;
    }

    private DHParameterSpec dh;
    private KeyPair keyPairA;
    private KeyFactory keyFactory;
    private byte sharedKey[];
    private byte publicKeyBytes[];
    private long authKeyID;
    private long salt;

    private native int wrapData(long messageID, byte[] authKey, long authKeyID, int buffer);
    public native int wrapDataToSend(int buffer);
    private native boolean decryptMessage(long messageID, byte[] authKey, long authKeyID, int buffer, int length, int mark);

    public int wrapData(long messageID, SerializedBuffer buffer) {
        return wrapData(messageID, sharedKey, authKeyID, buffer.getAddress());
    }

    public boolean decryptMessage(long messageID, int buffer, int length, int mark) {
        if (sharedKey != null) {
            return decryptMessage(messageID, sharedKey, this.authKeyID, buffer, length, mark);
        } else {
            return false;
        }
    }

    public boolean init(byte[] p, byte[] g) {
        BigInteger bip = KeyGenerator.bytesToBigInteger(p);
        BigInteger big = KeyGenerator.bytesToBigInteger(g);

        dh = new DHParameterSpec(bip, big);

        return generatePair();
    }

    public boolean generatePair() {
        KeyPairGenerator keyGen;

        try {
            keyGen = KeyPairGenerator.getInstance("DH");
            keyGen.initialize(dh);
            keyPairA = keyGen.generateKeyPair();

            PublicKey pk = keyPairA.getPublic();

            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pk.getEncoded());
            keyFactory = KeyFactory.getInstance("DH");
            BigInteger x = ((DHPublicKey) keyPairA.getPublic()).getY();
            publicKeyBytes = Utils.hexStringToBytes(x.toString(16));

        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean createShared(byte publicKeyBytes[]) {
        DHPublicKey key = bytesToPublicKey(publicKeyBytes);
        return createShared(key);
    }

    private boolean createShared(PublicKey publicKeyB) {
        try {
            KeyAgreement kaA = KeyAgreement.getInstance("DH");
            kaA.init(keyPairA.getPrivate());
            kaA.doPhase(publicKeyB, true);
            sharedKey = kaA.generateSecret();
            Log.d(Config.TAG, "Shared key generated");
        } catch(NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            Log.d(Config.TAG, "Failed to create shared key");
            return false;
        }
        return true;
    }

    public DHPublicKey bytesToPublicKey(byte[] bytes){
		/* Set Y (public key), P and G values. */
        KeySpec keySpec = new DHPublicKeySpec(
                bytesToBigInteger(bytes),
                dh.getP(),
                dh.getG()
        );

        try{
            return (DHPublicKey)keyFactory.generatePublic(keySpec);
        }
        catch(InvalidKeySpecException e){
            throw new RuntimeException(e);
        }
    }

    public static BigInteger bytesToBigInteger(byte[] bytes){
		/* Pad with 0x00 so we don't get a negative BigInteger!!! */
        ByteBuffer key = ByteBuffer.allocate(bytes.length + 1);

        key.put((byte)0x00);
        key.put(bytes);

        return new BigInteger(key.array());
    }

    public byte[] getPublicKey() {
        return publicKeyBytes;
    }

    public byte[] getSharedKey() {
        return sharedKey;
    }

    public void setPostConnectionData(final RPC.PM_postConnectionData data) {
//        authKeyID = data.keyID;
        salt = data.salt;
    }

    public void reset() {
        dh = null;
        keyPairA = null;
        keyFactory = null;
        sharedKey = null;
        publicKeyBytes = null;
        authKeyID = 0;
        salt = 0;
    }

    public void setKeyID(long keyID) {
        this.authKeyID = keyID;
    }
}
