package ru.paymon.android.utils;

public interface SerializableData {
    void writeInt32(int x);

    void writeInt64(long x);

    void writeBool(boolean value);

    void writeBytes(byte[] b);

    void writeBytes(byte[] b, int offset, int count);

    void writeByte(int i);

    void writeByte(byte b);

    void writeString(String s);

    void writeByteArray(byte[] b, int offset, int count);

    void writeByteArray(byte[] b);

    void writeDouble(double d);

    void writeByteBuffer(SerializedBuffer buffer);

    int readInt32(boolean exception);

    boolean readBool(boolean exception);

    long readInt64(boolean exception);

    void readBytes(byte[] b, boolean exception);

    byte[] readData(int count, boolean exception);

    String readString(boolean exception);

    byte[] readByteArray(boolean exception);

    SerializedBuffer readByteBuffer(boolean exception);

    double readDouble(boolean exception);

    int length();

    void skip(int count);

    int getPosition();
}
