package ru.paymon.android.utils;

import android.util.Log;


import java.util.ArrayList;

import ru.paymon.android.Config;


public class BuffersStorage {
    private final ArrayList<SerializedBuffer> freeBuffers128;
    private final ArrayList<SerializedBuffer> freeBuffers1024;
    private final ArrayList<SerializedBuffer> freeBuffers4096;
    private final ArrayList<SerializedBuffer> freeBuffers16384;
    private final ArrayList<SerializedBuffer> freeBuffers32768;
    private final ArrayList<SerializedBuffer> freeBuffersBig;
    private boolean isThreadSafe;
    private final static Object sync = new Object();

    private static volatile BuffersStorage Instance = null;
    public static BuffersStorage getInstance() {
        BuffersStorage localInstance = Instance;
        if (localInstance == null) {
            synchronized (BuffersStorage.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new BuffersStorage(true);
                }
            }
        }
        return localInstance;
    }

    public BuffersStorage(boolean threadSafe) {
        isThreadSafe = threadSafe;
        freeBuffers128 = new ArrayList<>();
        freeBuffers1024 = new ArrayList<>();
        freeBuffers4096 = new ArrayList<>();
        freeBuffers16384 = new ArrayList<>();
        freeBuffers32768 = new ArrayList<>();
        freeBuffersBig = new ArrayList<>();

        for (int a = 0; a < 5; a++) {
            try {
                freeBuffers128.add(new SerializedBuffer(128));
            } catch (Exception e) {
                Log.e(Config.TAG, "create new buffer failed");
            }
        }
    }

    public SerializedBuffer getFreeBuffer(int size) {
        if (size <= 0) {
            return null;
        }
        int byteCount = 0;
        ArrayList<SerializedBuffer> arrayToGetFrom = null;
        SerializedBuffer buffer = null;
        if (size <= 128) {
            arrayToGetFrom = freeBuffers128;
            byteCount = 128;
        } else if (size <= 1024 + 200) {
            arrayToGetFrom = freeBuffers1024;
            byteCount = 1024 + 200;
        } else if (size <= 4096 + 200) {
            arrayToGetFrom = freeBuffers4096;
            byteCount = 4096 + 200;
        } else if (size <= 16384 + 200) {
            arrayToGetFrom = freeBuffers16384;
            byteCount = 16384 + 200;
        } else if (size <= 40000) {
            arrayToGetFrom = freeBuffers32768;
            byteCount = 40000;
        } else if (size <= 280000) {
            arrayToGetFrom = freeBuffersBig;
            byteCount = 280000;
        } else {
            try {
                buffer = new SerializedBuffer(size);
            } catch (Exception e) {
                Log.e(Config.TAG, "create new buffer failed");
            }
        }

        if (arrayToGetFrom != null) {
            if (isThreadSafe) {
                synchronized (sync) {
                    if (arrayToGetFrom.size() > 0) {
                        buffer = arrayToGetFrom.get(0);
                        arrayToGetFrom.remove(0);
                    }
                }
            } else {
                if (arrayToGetFrom.size() > 0) {
                    buffer = arrayToGetFrom.get(0);
                    arrayToGetFrom.remove(0);
                }
            }

            if (buffer == null) {
                try {
                    buffer = new SerializedBuffer(byteCount);
                    Log.d(Config.TAG, "create new " + byteCount + " buffer");
                } catch (Exception e) {
                    Log.e(Config.TAG, "create new " + byteCount + " buffer failed");
                }
            }
        }

        buffer.buffer.limit(size).rewind();
        return buffer;
    }

    public void reuseFreeBuffer(SerializedBuffer buffer) {
        if (buffer == null) {
            return;
        }
        int maxCount = 10;
        ArrayList<SerializedBuffer> arrayToReuse = null;
        if (buffer.buffer.capacity() == 128) {
            arrayToReuse = freeBuffers128;
        } else if (buffer.buffer.capacity() == 1024 + 200) {
            arrayToReuse = freeBuffers1024;
        } if (buffer.buffer.capacity() == 4096 + 200) {
            arrayToReuse = freeBuffers4096;
        } else if (buffer.buffer.capacity() == 16384 + 200) {
            arrayToReuse = freeBuffers16384;
        } else if (buffer.buffer.capacity() == 40000) {
            arrayToReuse = freeBuffers32768;
        } else if (buffer.buffer.capacity() == 280000) {
            arrayToReuse = freeBuffersBig;
            maxCount = 10;
        }
        if (arrayToReuse != null) {
            if (isThreadSafe) {
                synchronized (sync) {
                    if (arrayToReuse.size() < maxCount) {
                        arrayToReuse.add(buffer);
                    } else {
                        Log.e(Config.TAG, "too many");
                    }
                }
            } else {
                if (arrayToReuse.size() < maxCount) {
                    arrayToReuse.add(buffer);
                }
            }
        }
    }
}
