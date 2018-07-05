package ru.paymon.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseArray;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import ru.paymon.android.models.Photo;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.Packet;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.cache.lrudiskcache.DiskLruImageCache;

public class MediaManager {
    public static final String PHOTOS_DIR = "/Paymon/photos/";
    public static final String STICKERS_DIR = "/Paymon/stickers/";
    public LongSparseArray<RPC.PM_photo> waitingPhotosList = new LongSparseArray<>();
    public LongSparseArray<StickerPack> stickerPacks = new LongSparseArray<>();
    public HashMap<Integer, Long> userProfilePhotoIDs = new HashMap<>();
    public SparseArray<Long> groupPhotoIDs = new SparseArray<>();
    public ArrayList<Integer> waitingStickerPacks = new ArrayList<>();

    private static AtomicLong lastPhotoID = new AtomicLong(0);
    private static volatile MediaManager Instance = null;

    public static MediaManager getInstance() {
        MediaManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (MediaManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new MediaManager();
                }
            }
        }
        return localInstance;
    }

    public void prepare() {
        try {
            File path = new File(Environment.getExternalStorageDirectory(), "Paymon");
            path.mkdir();
            File imagePath = new File(path, "photos");
            imagePath.mkdir();
            File videoPath = new File(path, "video");
            videoPath.mkdir();
            File stickersPath = new File(path, "stickers");
            stickersPath.mkdir();
        } catch (Exception e) {
            Log.e(Config.TAG, e.getMessage());
        }
    }

    private MediaManager() {
//        NotificationManager.getInstance().addObserver(this, NotificationManager.userAuthorized);
    }

    public void dispose() {
//        NotificationManager.getInstance().removeObserver(this, NotificationManager.userAuthorized);
        Instance = null;
    }

    public long generatePhotoID() {
        return lastPhotoID.decrementAndGet();
    }

    public RPC.PM_photo savePhoto(Bitmap bitmap, RPC.UserObject user) {
        final long photoID = generatePhotoID();
        DiskLruImageCache.getInstance().put(Integer.toString(user.id) + "_" + Long.toString(photoID), bitmap);
        RPC.PM_photo photo = new RPC.PM_photo();
        photo.id = photoID;
        photo.user_id = user.id;
        return photo;
    }

    public RPC.PM_photo savePhoto(FileManager.DownloadingFile downloadedFile) {
        try {
            RPC.PM_photo photo = waitingPhotosList.get(downloadedFile.id);
            if (photo == null) {
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(downloadedFile.buffer.buffer.array(), downloadedFile.buffer.buffer.arrayOffset(), downloadedFile.buffer.buffer.limit());

            if (bitmap != null)
                DiskLruImageCache.getInstance().put(Integer.toString(photo.user_id) + "_" + photo.id, bitmap);

            return photo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void updatePhotoID(long oldID, long newID) {
        try {
            if (DiskLruImageCache.getInstance().getBitmap(Integer.toString(User.currentUser.id) + "_" + Long.toString(oldID)) == null)
                return;

            DiskLruImageCache.getInstance().renameBitmap(Integer.toString(User.currentUser.id) + "_" + Long.toString(oldID),
                    Integer.toString(User.currentUser.id) + "_" + Long.toString(newID));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getFile(int ownerID, long fileID) {
        try {
            File file = new File(DiskLruImageCache.getInstance().getCacheFolder().getPath() + "/" + ownerID + "_" + fileID);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            DiskLruImageCache.getInstance().getBitmap(Integer.toString(ownerID) + "_" + Long.toString(fileID))
                    .compress(Bitmap.CompressFormat.JPEG, 90, os);
            os.close();
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public int getStickerPackIDByStickerID(long stickerID) {
        int stickerPackID = 0;
        for (int i = 0; i < stickerPacks.size(); i++) {
            StickerPack sp = stickerPacks.get(stickerPacks.keyAt(i));
            if (sp.stickers.containsKey(stickerID)) {
                return sp.id;
            }
        }
        return stickerPackID;
    }

    public StickerPack.Sticker saveSticker(Context context, FileManager.DownloadingFile downloadedFile) {
        long stickerID = downloadedFile.id;
        int stickerPackID = getStickerPackIDByStickerID(stickerID);
        if (stickerPackID == 0) return null;
        StickerPack sp = stickerPacks.get(stickerPackID);
        if (sp == null) return null;
        StickerPack.Sticker sticker = sp.stickers.get(stickerID);
        if (sticker == null) return null;

        String path = Environment.getExternalStorageDirectory().toString();
        File file = new File(path + STICKERS_DIR + "/" + stickerPackID + "/");
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            file = new File(path + STICKERS_DIR + "/" + stickerPackID + "/", stickerID + ".png");
            if (!file.createNewFile()) {
                new PrintWriter(file).close();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rws");
            raf.write(downloadedFile.buffer.buffer.array(), downloadedFile.buffer.buffer.arrayOffset(), downloadedFile.buffer.buffer.limit());
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return sticker;
    }

    public boolean checkStickerPackExists(int id) {
        String path = Environment.getExternalStorageDirectory().toString();
        File fileSrc = new File(path + STICKERS_DIR + "/" + id);
        return fileSrc.exists();
    }

    public boolean requestPhoto(int userID, final long photoID) {
        if (waitingPhotosList.get(photoID) != null) return false;

        RPC.PM_photo photo = new RPC.PM_photo();
        photo.id = photoID;
        photo.user_id = userID;
        waitingPhotosList.put(photoID, photo);

        if (NetworkManager.getInstance().isAuthorized()) {
            RPC.PM_requestPhoto request = new RPC.PM_requestPhoto();
            request.id = photoID;
            request.userID = userID;

            NetworkManager.getInstance().sendRequest(request, new Packet.OnResponseListener() {
                @Override
                public void onResponse(Packet response, RPC.PM_error error) {
                    if (response != null) {
                        if (response instanceof RPC.PM_boolFalse) {
                            waitingPhotosList.remove(photoID);
                        }
                    }
                }
            });
        }
        return true;
    }

    public boolean requestStickerPack(final int stickerPackID) {
        if (waitingStickerPacks.contains(stickerPackID) || checkStickerPackExists(stickerPackID))
            return false;
        RPC.PM_getStickerPack request = new RPC.PM_getStickerPack();
        request.id = stickerPackID;
        NetworkManager.getInstance().sendRequest(request, new Packet.OnResponseListener() {
            @Override
            public void onResponse(Packet response, RPC.PM_error error) {
                if (response != null) {
                    RPC.PM_stickerPack stickerPackResponse = (RPC.PM_stickerPack) response;
                    StickerPack stickerPack = new StickerPack(stickerPackResponse.id, stickerPackResponse.count, stickerPackResponse.title, stickerPackResponse.author);
                    for (RPC.PM_sticker s : stickerPackResponse.stickers) {
                        StickerPack.Sticker sticker = new StickerPack.Sticker();
                        sticker.id = s.id;
                        stickerPack.stickers.put(s.id, sticker);
                    }
                    stickerPacks.put(stickerPackResponse.id, stickerPack);

                    ApplicationLoader.applicationHandler.post(() -> NotificationManager.getInstance().postNotificationName(NotificationManager.didLoadedStickerPack, stickerPackID));
                }
            }
        });
        waitingStickerPacks.add(stickerPackID);
        return true;
    }

    public StickerPack loadStickerPack(int spid) {
        if (checkStickerPackExists(spid)) {
            StickerPack sp = stickerPacks.get(spid);
            if (sp != null) {
                return sp;
            }

            sp = new StickerPack(spid, 14, "Playman", "Sergey Pomelov");
            long id = 1;
            Bitmap bitmap;
            while ((bitmap = loadStickerBitmap(spid, id)) != null) {
                StickerPack.Sticker sticker = new StickerPack.Sticker();
                sticker.image = new BitmapDrawable(ApplicationLoader.applicationContext.getResources(), bitmap);
                sticker.id = id;
                sp.stickers.put(id, sticker);
                id++;
            }
            stickerPacks.put(spid, sp);
            return sp;
        } else {
            requestStickerPack(spid);
            return null;
        }
    }

    public void saveAndUpdatePhoto(FileManager.DownloadingFile downloadingFile) {
        RPC.PM_photo photo = savePhoto(downloadingFile);
        if (photo != null) {
            final Bitmap bitmap = loadPhotoBitmap(photo.user_id, photo.id);
            if (bitmap != null) {
                final Photo newPhoto = new Photo(photo.id, photo.user_id, bitmap);
                ApplicationLoader.applicationHandler.post(() -> ObservableMediaManager.getInstance().postPhotoNotification(newPhoto));
            }
        }
    }

    public void saveAndUpdateSticker(FileManager.DownloadingFile downloadingFile) {
        final StickerPack.Sticker sticker = saveSticker(ApplicationLoader.applicationContext, downloadingFile);
        if (sticker != null) {
            final Bitmap bitmap = loadStickerBitmap(getStickerPackIDByStickerID(sticker.id), sticker.id);
            sticker.image = new BitmapDrawable(ApplicationLoader.applicationContext.getResources(), bitmap);
            if (bitmap != null) {
                ApplicationLoader.applicationHandler.post(() -> ObservableMediaManager.getInstance().postStickerNotification(sticker));
            }
        }
    }

    public Bitmap loadPhotoBitmap(int userID, long photoID) {
        return DiskLruImageCache.getInstance().getBitmap(Integer.toString(userID) + "_" + Long.toString(photoID));
    }

    public Bitmap loadStickerBitmap(int stickerPackID, long stickerID) {
        String path = Environment.getExternalStorageDirectory().toString();
        File file = new File(path + STICKERS_DIR + "/" + stickerPackID + "/", stickerID + ".png");
        if (!file.exists()) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(file.getPath(), options);
    }

    public void processPhotoRequest() {
        if (waitingPhotosList.size() == 0) return;
        long key = waitingPhotosList.keyAt(waitingPhotosList.size() - 1);
        RPC.PM_photo photo = waitingPhotosList.get(key);
        if (photo == null) return;

        RPC.PM_requestPhoto request = new RPC.PM_requestPhoto();
        final long photoID = photo.id;
        request.id = photoID;
        request.userID = photo.user_id;

        NetworkManager.getInstance().sendRequest(request, new Packet.OnResponseListener() {
            @Override
            public void onResponse(Packet response, RPC.PM_error error) {
                if (response != null) {
                    if (response instanceof RPC.PM_boolFalse) {
                        waitingPhotosList.remove(photoID);
                    }
                    processPhotoRequest();
                }
            }
        });

        waitingPhotosList.remove(key);
    }
}
