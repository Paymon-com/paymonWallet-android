package ru.paymon.android.utils.cache.lruramcache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;


import ru.paymon.android.ApplicationLoader;

public class LruRamCache {
    private static LruRamCache instance;
    public LruCache<Integer, Bitmap> lruCache;

    public LruRamCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        lruCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public static LruRamCache getInstance() {
        if (instance == null) {
            instance = new LruRamCache();
            return instance;
        } else {
            return instance;
        }
    }


    public void addBitmapToMemoryCache(Integer key, Bitmap bitmap) {
        if (getBitmap(key) == null) {
            lruCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmap(Integer key) {
        Bitmap bitmap = lruCache.get(key);
        if (bitmap == null && key != null) {
            bitmap = BitmapFactory.decodeResource(ApplicationLoader.applicationContext.getResources(), key);
            if (bitmap == null) {
                return null;
            }
            lruCache.put(key, bitmap);
        }
        return bitmap;
    }

    private class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Integer... params) {
            final Bitmap bitmap = BitmapFactory.decodeResource(ApplicationLoader.applicationContext.getResources(), params[0]); //decodeSampledBitmapFromResource(getResources(), params[0], 100, 100));
            if (bitmap != null) {
                addBitmapToMemoryCache(params[0], bitmap);
            }
            return bitmap;
        }
    }
}
