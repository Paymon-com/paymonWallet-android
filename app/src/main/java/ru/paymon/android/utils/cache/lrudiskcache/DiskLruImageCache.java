//package ru.paymon.android.utils.cache.lrudiskcache;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.os.Environment;
//import android.util.Log;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//import ru.paymon.android.ApplicationLoader;
//import ru.paymon.android.BuildConfig;
//
//
//public class DiskLruImageCache {
//    private static DiskLruImageCache instance;
//    private static final int APP_VERSION = 1;
//    private static final int VALUE_COUNT = 1;
//    private static final String TAG = "DiskLruImageCache";
//    private final long CACHE_MAX_SIZE = 1024 * 1024 * 100; // 100MB
//    private DiskLruCache mDiskCache;
//    private Bitmap.CompressFormat mCompressFormat = Bitmap.CompressFormat.JPEG;
//    private int mCompressQuality = 70;
//
//    public static DiskLruImageCache getInstance() {
//        if (instance == null)
//            instance = new DiskLruImageCache();
//        return instance;
//    }
//
//    public DiskLruImageCache() {
//        try {
//            final File diskCacheDir = getDiskCacheDir(ApplicationLoader.applicationContext, TAG);
//            mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, CACHE_MAX_SIZE);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public DiskLruImageCache(String uniqueName, int diskCacheSize, Bitmap.CompressFormat compressFormat, int quality) {
//        try {
//            final File diskCacheDir = getDiskCacheDir(ApplicationLoader.applicationContext, uniqueName);
//            mDiskCache = DiskLruCache.open(diskCacheDir, APP_VERSION, VALUE_COUNT, diskCacheSize);
//            mCompressFormat = compressFormat;
//            mCompressQuality = quality;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private boolean writeBitmapToFile(Bitmap bitmap, DiskLruCache.Editor editor)
//            throws IOException, FileNotFoundException {
//        OutputStream out = null;
//        try {
//            out = new BufferedOutputStream(editor.newOutputStream(0), Util.IO_BUFFER_SIZE);
//            return bitmap.compress(mCompressFormat, mCompressQuality, out);
//        } catch (Exception e) {
//            Log.d(TAG, "writeBitmapToFile error " + e.getMessage());
//            return false;
//        } finally {
//            if (out != null) {
//                out.close();
//            }
//        }
//    }
//
//    private File getDiskCacheDir(Context context, String uniqueName) {
//
//        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
//        // otherwise use internal cache dir
//        final String cachePath =
//                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
//                        !Util.isExternalStorageRemovable() ?
//                        Util.getExternalCacheDir(context).getPath() :
//                        context.getCacheDir().getPath();
//
//        return new File(cachePath + File.separator + uniqueName);
//    }
//
//    public void put(String key, Bitmap data) {
//        DiskLruCache.Editor editor = null;
//        try {
//            editor = mDiskCache.edit(key);
//            if (editor == null) {
//                return;
//            }
//
//            if (writeBitmapToFile(data, editor)) {
//                mDiskCache.flush();
//                editor.commit();
//                if (BuildConfig.DEBUG) {
//                    Log.d("cache_test_DISK_", "image put on disk cache " + key);
//                }
//            } else {
//                editor.abort();
//                if (BuildConfig.DEBUG) {
//                    Log.d("cache_test_DISK_", "ERROR on: image put on disk cache " + key);
//                }
//            }
//        } catch (IOException e) {
//            if (BuildConfig.DEBUG) {
//                Log.d("cache_test_DISK_", "ERROR on: image put on disk cache " + key);
//            }
//            try {
//                if (editor != null) {
//                    editor.abort();
//                }
//            } catch (IOException ignored) {
//            }
//        }
//
//    }
//
//    public Bitmap getBitmap(String key) {
//        Bitmap bitmap = null;
//        DiskLruCache.Snapshot snapshot = null;
//        try {
//            snapshot = mDiskCache.get(key);
//            if (snapshot == null) {
//                bitmap = BitmapFactory.decodeResource(ApplicationLoader.applicationContext.getResources(), ApplicationLoader.applicationContext.getResources().getIdentifier(key, "drawable", ApplicationLoader.applicationContext.getPackageName())); // this
//                put(key, bitmap);
//                Log.d(TAG, "bitmap " + key + " not found in cache!");
//                return bitmap;
//            }
//            Log.d(TAG, "bitmap " + key + " found in cache!");
//            final InputStream in = snapshot.getInputStream(0);
//            if (in != null) {
//                final BufferedInputStream buffIn = new BufferedInputStream(in, Util.IO_BUFFER_SIZE);
//                bitmap = BitmapFactory.decodeStream(buffIn);
//                buffIn.close();
//                in.close();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (snapshot != null) {
//                snapshot.close();
//            }
//        }
//
//        if (BuildConfig.DEBUG) {
//            Log.d(TAG, bitmap == null ? "" : "image read from disk " + key);
//        }
//
//        return bitmap;
//
//    }
//
//    public boolean containsKey(String key) {
//
//        boolean contained = false;
//        DiskLruCache.Snapshot snapshot = null;
//        try {
//            snapshot = mDiskCache.get(key);
//            contained = snapshot != null;
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (snapshot != null) {
//                snapshot.close();
//            }
//        }
//
//        return contained;
//
//    }
//
//    public void clearCache() {
//        if (BuildConfig.DEBUG) {
//            Log.d("cache_test_DISK_", "disk cache CLEARED");
//        }
//        try {
//            mDiskCache.delete();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public File getCacheFolder() {
//        return mDiskCache.getDirectory();
//    }
//
//    public void deleteBitmap(String key) throws IOException {
//        mDiskCache.remove(key);
//    }
//
//    public void renameBitmap(String from, String to) throws FileNotFoundException, IOException {
//        File fileFrom = new File(getCacheFolder().getPath() + from);
//        OutputStream os = new BufferedOutputStream(new FileOutputStream(fileFrom));
//        getBitmap(from).compress(Bitmap.CompressFormat.JPEG, 90, os);
//        os.close();
//        fileFrom.renameTo(new File(getCacheFolder().getPath() + to));
//    }
//
//}