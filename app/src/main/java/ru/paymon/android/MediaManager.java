//package ru.paymon.android;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.drawable.BitmapDrawable;
//import android.os.Environment;
//import android.util.Log;
//import android.util.LongSparseArray;
//import android.util.SparseArray;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStream;
//import java.io.PrintWriter;
//import java.io.RandomAccessFile;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.concurrent.atomic.AtomicLong;
//
//import ru.paymon.android.models.Photo;
//import ru.paymon.android.net.NetworkManager;
//import ru.paymon.android.net.RPC;
//import ru.paymon.android.utils.FileManager;
//import ru.paymon.android.utils.cache.lrudiskcache.DiskLruImageCache;
//
//import static ru.paymon.android.Config.TAG;
//
//public class MediaManager {
//    public static final String PHOTOS_DIR = "/Paymon/photos/";
//    public static final String STICKERS_DIR = "/Paymon/stickers/";
//    public LongSparseArray<RPC.PM_photo> waitingPhotosList = new LongSparseArray<>();
//    public LongSparseArray<StickerPack> stickerPacks = new LongSparseArray<>();
//    public HashMap<Integer, Long> userProfilePhotoIDs = new HashMap<>();
//    public SparseArray<Long> groupPhotoIDs = new SparseArray<>();
//    public ArrayList<Integer> waitingStickerPacks = new ArrayList<>();

//    private static AtomicLong lastPhotoID = new AtomicLong(0);
//    private static volatile MediaManager Instance = null;
//
//    public static MediaManager getInstance() {
//        MediaManager localInstance = Instance;
//        if (localInstance == null) {
//            synchronized (MediaManager.class) {
//                localInstance = Instance;
//                if (localInstance == null) {
//                    Instance = localInstance = new MediaManager();
//                }
//            }
//        }
//        return localInstance;
//    }

//    public void prepare() {
//        try {
//            File path = new File(Environment.getExternalStorageDirectory(), "Paymon");
//            path.mkdir();
//            File imagePath = new File(path, "photos");
//            imagePath.mkdir();
//            File videoPath = new File(path, "video");
//            videoPath.mkdir();
//            File stickersPath = new File(path, "stickers");
//            stickersPath.mkdir();
//        } catch (Exception e) {
//            Log.e(TAG, e.getMessage());
//        }
//    }

//    private MediaManager() {
//        NotificationManager.getInstance().addObserver(this, NotificationManager.userAuthorized);
//    }

//    public void dispose() {
//        NotificationManager.getInstance().removeObserver(this, NotificationManager.userAuthorized);
//        Instance = null;
//    }

//    public long generatePhotoID() {
//        return lastPhotoID.decrementAndGet();
//    }

//    public RPC.PM_photo savePhoto(Bitmap bitmap, RPC.UserObject user) {
//        final long photoID = generatePhotoID();
//        DiskLruImageCache.getInstance().put(Integer.toString(user.gid) + "_" + Long.toString(photoID), bitmap);
//        RPC.PM_photo photo = new RPC.PM_photo();
//        photo.gid = photoID;
//        photo.user_id = user.gid;
//        return photo;
//    }
//
//    public RPC.PM_photo savePhoto(FileManager.DownloadingFile downloadedFile) {
//        try {
//            RPC.PM_photo photo = waitingPhotosList.get(downloadedFile.gid);
//
//            if (photo != null) {
//                Bitmap bitmap = BitmapFactory.decodeByteArray(downloadedFile.buffer.buffer.array(), downloadedFile.buffer.buffer.arrayOffset(), downloadedFile.buffer.buffer.limit());
//                if (bitmap != null)
//                    DiskLruImageCache.getInstance().put(Integer.toString(photoURL.user_id) + "_" + photoURL.gid, bitmap);
//            }
//
//            return photo;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

//    public void updatePhotoID(long oldID, long newID) {
//        try {
//            if (DiskLruImageCache.getInstance().getBitmap(Integer.toString(User.currentUser.gid) + "_" + Long.toString(oldID)) == null)
//                return;
//
//            DiskLruImageCache.getInstance().renameBitmap(Integer.toString(User.currentUser.gid) + "_" + Long.toString(oldID),
//                    Integer.toString(User.currentUser.gid) + "_" + Long.toString(newID));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public File getFile(int ownerID, long fileID) {
//        try {
//            File file = new File(DiskLruImageCache.getInstance().getCacheFolder().getPath() + "/" + ownerID + "_" + fileID);
//            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
//            DiskLruImageCache.getInstance().getBitmap(Integer.toString(ownerID) + "_" + Long.toString(fileID)).compress(Bitmap.CompressFormat.JPEG, 90, os);
//            os.close();
//            return file;
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    public int getStickerPackIDByStickerID(long stickerID) {
//        int stickerPackID = 0;
//        for (int i = 0; i < stickerPacks.size(); i++) {
//            StickerPack sp = stickerPacks.get(stickerPacks.keyAt(i));
//            if (sp.stickers.containsKey(stickerID))
//                return sp.gid;
//        }
//        return stickerPackID;
//    }
//
//    public StickerPack.Sticker saveSticker(FileManager.DownloadingFile downloadedFile) {
//        long stickerID = downloadedFile.gid;
//        int stickerPackID = getStickerPackIDByStickerID(stickerID);
//        if (stickerPackID == 0) return null;
//        StickerPack sp = stickerPacks.get(stickerPackID);
//        if (sp == null) return null;
//        StickerPack.Sticker sticker = sp.stickers.get(stickerID);
//        if (sticker == null) return null;
//
//        String path = Environment.getExternalStorageDirectory().toString();
//        File file = new File(path + STICKERS_DIR + "/" + stickerPackID + "/");
//
//        if (!file.exists())
//            file.mkdirs();
//
//        try {
//            file = new File(path + STICKERS_DIR + "/" + stickerPackID + "/", stickerID + ".png");
//            if (!file.createNewFile())
//                new PrintWriter(file).close();
//            RandomAccessFile raf = new RandomAccessFile(file, "rws");
//            raf.write(downloadedFile.buffer.buffer.array(), downloadedFile.buffer.buffer.arrayOffset(), downloadedFile.buffer.buffer.limit());
//            raf.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return null;
//        }
//
//        return sticker;
//    }

//    public boolean checkStickerPackExists(int gid) {
//        return new File(Environment.getExternalStorageDirectory().toString() + STICKERS_DIR + "/" + gid).exists();
//    }

//    public boolean requestPhoto(int userID, final long photoID) {
//        if (waitingPhotosList.get(photoID) != null) return false;
//
//        RPC.PM_photo photo = new RPC.PM_photo();
//        photo.gid = photoID;
//        photo.user_id = userID;
//        waitingPhotosList.put(photoID, photo);
//
//        if (NetworkManager.getInstance().isAuthorized()) {
//            RPC.PM_requestPhoto request = new RPC.PM_requestPhoto();
//            request.gid = photoID;
//            request.userID = userID;
//
//            NetworkManager.getInstance().sendRequest(request, (response, error) -> {
//                if (response != null) {
//                    if (response instanceof RPC.PM_boolFalse)
//                        waitingPhotosList.remove(photoID);
//                }
//            });
//        }
//        return true;
//    }

//    public boolean requestStickerPack(final int stickerPackID) {
//        if (waitingStickerPacks.contains(stickerPackID) || checkStickerPackExists(stickerPackID))
//            return false;
//        RPC.PM_getStickerPack request = new RPC.PM_getStickerPack();
//        request.gid = stickerPackID;
//        NetworkManager.getInstance().sendRequest(request, (response, error) -> {
//            if (response != null) {
//                RPC.PM_stickerPack stickerPackResponse = (RPC.PM_stickerPack) response;
//                StickerPack stickerPack = new StickerPack(stickerPackResponse.gid, stickerPackResponse.count, stickerPackResponse.title, stickerPackResponse.author);
//                for (RPC.PM_sticker s : stickerPackResponse.stickers) {
//                    StickerPack.Sticker sticker = new StickerPack.Sticker();
//                    sticker.gid = s.gid;
//                    stickerPack.stickers.put(s.gid, sticker);
//                }
//                stickerPacks.put(stickerPackResponse.gid, stickerPack);
//
//                ApplicationLoader.applicationHandler.post(() -> NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.didLoadedStickerPack, stickerPackID));
//            }
//        });
//        waitingStickerPacks.add(stickerPackID);
//        return true;
//    }
//
//    public StickerPack loadStickerPack(int spid) {
//        if (checkStickerPackExists(spid)) {
//            StickerPack sp = stickerPacks.get(spid);
//
//            if (sp != null)
//                return sp;
//
//            sp = new StickerPack(spid, 14, "Playman", "Sergey Pomelov");
//            long gid = 1;
//            Bitmap bitmap;
//            while ((bitmap = loadStickerBitmap(spid, gid)) != null) {
//                StickerPack.Sticker sticker = new StickerPack.Sticker();
//                sticker.image = new BitmapDrawable(ApplicationLoader.applicationContext.getResources(), bitmap);
//                sticker.gid = gid;
//                sp.stickers.put(gid, sticker);
//                gid++;
//            }
//            stickerPacks.put(spid, sp);
//            return sp;
//        } else {
//            requestStickerPack(spid);
//            return null;
//        }
//    }

//    public void saveAndUpdatePhoto(FileManager.DownloadingFile downloadingFile) {
//        RPC.PM_photo photo = savePhoto(downloadingFile);
//        if (photo != null) {
//            final Bitmap bitmap = loadPhotoBitmap(photoURL.user_id, photoURL.gid);
//            if (bitmap != null) {
//                final Photo newPhoto = new Photo(photoURL.gid, photoURL.user_id, bitmap);
////                ApplicationLoader.applicationHandler.post(() -> ObservableMediaManager.getInstance().postPhotoNotification(newPhoto));
//            }
//        }
//    }

//    public void saveAndUpdateSticker(FileManager.DownloadingFile downloadingFile) {
//        final StickerPack.Sticker sticker = saveSticker(downloadingFile);
//        if (sticker != null) {
//            final Bitmap bitmap = loadStickerBitmap(getStickerPackIDByStickerID(sticker.gid), sticker.gid);
//            sticker.image = new BitmapDrawable(ApplicationLoader.applicationContext.getResources(), bitmap);
////            if (bitmap != null)
////                ApplicationLoader.applicationHandler.post(() -> ObservableMediaManager.getInstance().postStickerNotification(sticker));
//        }
//    }

//    public Bitmap loadPhotoBitmap(int userID, long photoID) {
//        return DiskLruImageCache.getInstance().getBitmap(Integer.toString(userID) + "_" + Long.toString(photoID));
//    }
//
//    public Bitmap loadStickerBitmap(int stickerPackID, long stickerID) {
//        File file = new File(Environment.getExternalStorageDirectory().toString() + STICKERS_DIR + "/" + stickerPackID + "/", stickerID + ".png");
//
//        if (!file.exists()) return null;
//
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        return BitmapFactory.decodeFile(file.getPath(), options);
//    }
//
//    public void processPhotoRequest() {
//        if (waitingPhotosList.size() == 0) return;
//        long key = waitingPhotosList.keyAt(waitingPhotosList.size() - 1);
//        RPC.PM_photo photo = waitingPhotosList.get(key);
//        if (photo == null) return;
//
//        RPC.PM_requestPhoto request = new RPC.PM_requestPhoto();
//        final long photoID = photo.gid;
//        request.gid = photoID;
//        request.userID = photo.user_id;
//
//        NetworkManager.getInstance().sendRequest(request, (response, error) -> {
//            if (response != null) {
//                if (response instanceof RPC.PM_boolFalse)
//                    waitingPhotosList.remove(photoID);
//                processPhotoRequest();
//            }
//        });
//
//        waitingPhotosList.remove(key);
//    }
//}
