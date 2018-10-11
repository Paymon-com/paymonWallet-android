//package ru.paymon.android.gateway.bitcoin.offline;
//
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import org.bitcoin.protocols.payments.Protos;
//import org.bitcoin.protocols.payments.Protos.PaymentACK;
//import org.bitcoinj.core.ProtocolException;
//import org.bitcoinj.core.Transaction;
//import org.bitcoinj.protocols.payments.PaymentProtocol;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothServerSocket;
//import android.bluetooth.BluetoothSocket;
//
//import ru.paymon.android.gateway.bitcoin.Constants;
//
//
//public abstract class AcceptBluetoothThread extends Thread {
//    protected final BluetoothServerSocket listeningSocket;
//    protected final AtomicBoolean running = new AtomicBoolean(true);
//
//    protected static final Logger log = LoggerFactory.getLogger(AcceptBluetoothThread.class);
//
//    private AcceptBluetoothThread(final BluetoothServerSocket listeningSocket) {
//        this.listeningSocket = listeningSocket;
//    }
//
//    public static abstract class ClassicBluetoothThread extends AcceptBluetoothThread {
//        public ClassicBluetoothThread(final BluetoothAdapter adapter) throws IOException {
//            super(adapter.listenUsingInsecureRfcommWithServiceRecord(Bluetooth.CLASSIC_PAYMENT_PROTOCOL_NAME,
//                    Bluetooth.CLASSIC_PAYMENT_PROTOCOL_UUID));
//        }
//
//        @Override
//        public void run() {
//            org.bitcoinj.core.Context.propagate(Constants.CONTEXT);
//
//            while (running.get()) {
//                try ( // start a blocking call, and return only on success or exception
//                      final BluetoothSocket socket = listeningSocket.accept();
//                      final DataInputStream is = new DataInputStream(socket.getInputStream());
//                      final DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
//                    log.info("accepted classic bluetooth connection");
//
//                    boolean ack = true;
//
//                    final int numMessages = is.readInt();
//
//                    for (int i = 0; i < numMessages; i++) {
//                        final int msgLength = is.readInt();
//                        final byte[] msg = new byte[msgLength];
//                        is.readFully(msg);
//
//                        try {
//                            final Transaction tx = new Transaction(Constants.NETWORK_PARAMETERS, msg);
//
//                            if (!handleTx(tx))
//                                ack = false;
//                        } catch (final ProtocolException x) {
//                            log.info("cannot decode message received via bluetooth", x);
//                            ack = false;
//                        }
//                    }
//
//                    os.writeBoolean(ack);
//                } catch (final IOException x) {
//                    log.info("exception in bluetooth accept loop", x);
//                }
//            }
//        }
//    }
//
//    public static abstract class PaymentProtocolThread extends AcceptBluetoothThread {
//        public PaymentProtocolThread(final BluetoothAdapter adapter) throws IOException {
//            super(adapter.listenUsingInsecureRfcommWithServiceRecord(Bluetooth.BIP70_PAYMENT_PROTOCOL_NAME,
//                    Bluetooth.BIP70_PAYMENT_PROTOCOL_UUID));
//        }
//
//        @Override
//        public void run() {
//            org.bitcoinj.core.Context.propagate(Constants.CONTEXT);
//
//            while (running.get()) {
//                try ( // start a blocking call, and return only on success or exception
//                      final BluetoothSocket socket = listeningSocket.accept();
//                      final DataInputStream is = new DataInputStream(socket.getInputStream());
//                      final DataOutputStream os = new DataOutputStream(socket.getOutputStream())) {
//                    log.info("accepted payment protocol bluetooth connection");
//
//                    boolean ack = true;
//
//                    final Protos.Payment payment = Protos.Payment.parseDelimitedFrom(is);
//
//                    log.debug("got payment message");
//
//                    for (final Transaction tx : PaymentProtocol
//                            .parseTransactionsFromPaymentMessage(Constants.NETWORK_PARAMETERS, payment)) {
//                        if (!handleTx(tx))
//                            ack = false;
//                    }
//
//                    final String memo = ack ? "ack" : "nack";
//
//                    log.info("sending {} via bluetooth", memo);
//
//                    final PaymentACK paymentAck = PaymentProtocol.createPaymentAck(payment, memo);
//                    paymentAck.writeDelimitedTo(os);
//                } catch (final IOException x) {
//                    log.info("exception in bluetooth accept loop", x);
//                }
//            }
//        }
//    }
//
//    public void stopAccepting() {
//        running.set(false);
//
//        try {
//            listeningSocket.close();
//        } catch (final IOException x) {
//            // swallow
//        }
//    }
//
//    protected abstract boolean handleTx(Transaction tx);
//}