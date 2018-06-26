package ru.paymon.android.net;



import ru.paymon.android.utils.SerializableData;
import ru.paymon.android.utils.SerializedBuffer;

public class Packet {
    public interface OnResponseListener {
        void onResponse(Packet response, RPC.PM_error error);
    }

    boolean disableFree = false;

    private static final ThreadLocal<SerializedBuffer> sizeCalculator = new ThreadLocal<SerializedBuffer>() {
        @Override
        protected SerializedBuffer initialValue() {
            return new SerializedBuffer(true);
        }
    };

    public Packet() {

    }

    public void readParams(SerializableData stream, boolean exception) {

    }

    public void serializeToStream(SerializableData stream) {

    }

    public Packet deserializeResponse(SerializableData stream, int constructor, boolean exception) {
        return null;
    }

    public void freeResources() {

    }

    public int getSize() {
        SerializedBuffer byteBuffer = sizeCalculator.get();
        byteBuffer.rewind();
        serializeToStream(sizeCalculator.get());
        return byteBuffer.length();
    }
}
