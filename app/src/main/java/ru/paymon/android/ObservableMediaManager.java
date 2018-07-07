package ru.paymon.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.LongSparseArray;
import android.util.SparseArray;


import java.util.ArrayList;

import ru.paymon.android.models.Photo;
import ru.paymon.android.net.RPC;

public class ObservableMediaManager {
    private LongSparseArray<ArrayList<Object>> photoObservers = new LongSparseArray<>();
    private LongSparseArray<ArrayList<Object>> removePhotoAfterBroadcast = new LongSparseArray<>();
    private LongSparseArray<ArrayList<Object>> addPhotoAfterBroadcast = new LongSparseArray<>();
    private LongSparseArray<Long> updatedPhotoIDs = new LongSparseArray<>();
    private ArrayList<DelayedPhotoPost> delayedPhotoPosts = new ArrayList<>(10);
    private ArrayList<DelayedPhotoUpdateIDPost> delayedUpdatePhotoIDPosts = new ArrayList<>(10);
    private LongSparseArray<BitmapDrawable> photoIDsBitmaps = new LongSparseArray<>(100);
    private LongSparseArray<ArrayList<Object>> stickerObservers = new LongSparseArray<>();
    private LongSparseArray<ArrayList<Object>> removeStickerAfterBroadcast = new LongSparseArray<>();
    private LongSparseArray<ArrayList<Object>> addStickerAfterBroadcast = new LongSparseArray<>();
    private ArrayList<DelayedStickerPost> delayedStickerPosts = new ArrayList<>(10);
    private LongSparseArray<BitmapDrawable> stickerIDsBitmaps = new LongSparseArray<>(100);
    private int broadcasting = 0;
    private boolean animationInProgress;

    private int[] allowedNotifications;

    private static volatile ObservableMediaManager Instance = null;

    public static ObservableMediaManager getInstance() {
        ObservableMediaManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (ObservableMediaManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ObservableMediaManager();
                }
            }
        }
        return localInstance;
    }

    public BitmapDrawable loadPhotoBitmap(int photoOwnerID, long photoID) {
        BitmapDrawable bitmapDrawable = photoIDsBitmaps.get(photoID);
        if (bitmapDrawable == null) {
            Bitmap bitmap = MediaManager.getInstance().loadPhotoBitmap(photoOwnerID, photoID);
            if (bitmap != null) {
                bitmapDrawable = new BitmapDrawable(
                        ApplicationLoader.applicationContext.getResources(),
                        bitmap);
                photoIDsBitmaps.put(photoID, bitmapDrawable);
            } else {
                return null;
            }
        }
        return bitmapDrawable;
    }

    public BitmapDrawable loadStickerBitmap(long stickerID) {
        BitmapDrawable bitmapDrawable = stickerIDsBitmaps.get(stickerID);
        if (bitmapDrawable == null) {
            int spid = MediaManager.getInstance().getStickerPackIDByStickerID(stickerID);
            if (spid != 0) {
                Bitmap bitmap = MediaManager.getInstance().loadStickerBitmap(spid, stickerID);

                if (bitmap != null) {
                    bitmapDrawable = new BitmapDrawable(
                            ApplicationLoader.applicationContext.getResources(),
                            bitmap);
                    stickerIDsBitmaps.put(stickerID, bitmapDrawable);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return bitmapDrawable;
    }

    public interface IPhotoListener {
        void didLoadedPhoto(final Photo photo);

        void didUpdatedPhotoID(long newPhotoID, int ownerID);

        void loadProgress(int progress);
    }

    private class DelayedPhotoPost {
        private Photo photo;

        private DelayedPhotoPost(Photo photo) {
            this.photo = photo;
        }
    }

    public interface IStickerListener {
        void didLoadedSticker(StickerPack.Sticker sticker);
    }

    private class DelayedStickerPost {
        private StickerPack.Sticker sticker;

        private DelayedStickerPost(StickerPack.Sticker sticker) {
            this.sticker = sticker;
        }
    }

    private class DelayedPhotoUpdateIDPost {
        private long oldPhotoID;
        private long newPhotoID;

        private DelayedPhotoUpdateIDPost(long oldPhotoID, long newPhotoID) {
            this.oldPhotoID = oldPhotoID;
            this.newPhotoID = newPhotoID;
        }
    }

    public void setAllowedNotificationsDutingAnimation(int notifications[]) {
        allowedNotifications = notifications;
    }

    // !!! Изменить, если нужно сделать уведомление с задержкой
    public void setAnimationInProgress(boolean flag) {
        animationInProgress = flag;
        if (!animationInProgress && !delayedPhotoPosts.isEmpty()) {
            for (DelayedPhotoPost delayedPhotoPost : delayedPhotoPosts) {
                postPhotoNotificationInternal(true, delayedPhotoPost.photo);
            }
            delayedPhotoPosts.clear();
        }
    }

    public boolean isAnimationInProgress() {
        return animationInProgress;
    }

    public void postPhotoNotification(final Photo photo) {
        postPhotoNotificationInternal(false, photo);
    }

    public void postPhotoUpdateIDNotification(long oldPhotoID, long newPhotoID) {
        postPhotoUpdateIDNotificationInternal(false, oldPhotoID, newPhotoID);
    }

    public void postPhotoUpdateIDNotificationInternal(boolean allowDuringAnimation, long oldPhotoID, long newPhotoID) {
        if (User.currentUser.photoID == oldPhotoID) {
            User.currentUser.photoID = newPhotoID;
            NotificationManager.getInstance().postNotificationName(NotificationManager.profileUpdated);
        } else {
            SparseArray<RPC.Group> groups = GroupsManager.getInstance().groups;
            for (int i = 0; i < groups.size(); i++) {
                RPC.Group group = groups.get(groups.keyAt(i));
                if (group.photo.id == oldPhotoID) {
                    group.photo.id = newPhotoID;
                    break;
                }
            }
        }

        if (!allowDuringAnimation && animationInProgress) {
            DelayedPhotoUpdateIDPost delayedPhotoPost = new DelayedPhotoUpdateIDPost(oldPhotoID, newPhotoID);
            delayedUpdatePhotoIDPosts.add(delayedPhotoPost);
            return;
        }
        broadcasting++;
        ArrayList<Object> objects = photoObservers.get(oldPhotoID);
        if (objects != null && !objects.isEmpty()) {
            for (int a = 0; a < objects.size(); a++) {
                Object obj = objects.get(a);
                ((IPhotoListener) obj).didUpdatedPhotoID(newPhotoID, 0); // TODO: do it need ownerID?
            }
        }
        updatedPhotoIDs.put(newPhotoID, oldPhotoID);
        broadcasting--;
        if (broadcasting == 0) {
            updatePhotoBroadcasting();
        }
    }

    public void postPhotoNotificationInternal(boolean allowDuringAnimation, final Photo photo) {
        if (!allowDuringAnimation && animationInProgress) {
            DelayedPhotoPost delayedPhotoPost = new DelayedPhotoPost(photo);
            delayedPhotoPosts.add(delayedPhotoPost);
            return;
        }
        broadcasting++;
        long photoID = photo.id;
        Long oldID = updatedPhotoIDs.get(photoID);
        if (oldID != null) {
            photoID = oldID;
        }
        ArrayList<Object> objects = photoObservers.get(photoID);
        if (objects != null && !objects.isEmpty()) {
            for (int a = 0; a < objects.size(); a++) {
                Object obj = objects.get(a);
                ((IPhotoListener) obj).didLoadedPhoto(photo);
            }
        }
        broadcasting--;
        if (broadcasting == 0) {
            updatePhotoBroadcasting();
        }
    }

    private void updatePhotoBroadcasting() {
        if (updatedPhotoIDs.size() != 0) {
            for (int i = 0; i < updatedPhotoIDs.size(); i++) {
                long newID = updatedPhotoIDs.keyAt(i);
                long oldID = updatedPhotoIDs.get(newID);
                ArrayList<Object> arrayList = photoObservers.get(oldID);
                if (arrayList != null) {
                    ArrayList<Object> newArrayList = new ArrayList<>(arrayList);
                    photoObservers.delete(oldID);
                    photoObservers.put(newID, newArrayList);
                }
                arrayList = removePhotoAfterBroadcast.get(oldID);
                if (arrayList != null) {
                    ArrayList<Object> newArrayList = new ArrayList<>(arrayList);
                    removePhotoAfterBroadcast.delete(oldID);
                    removePhotoAfterBroadcast.put(newID, newArrayList);
                }
                arrayList = addPhotoAfterBroadcast.get(oldID);
                if (arrayList != null) {
                    ArrayList<Object> newArrayList = new ArrayList<>(arrayList);
                    addPhotoAfterBroadcast.delete(oldID);
                    addPhotoAfterBroadcast.put(newID, newArrayList);
                }
            }
            updatedPhotoIDs.clear();
        }
        if (removePhotoAfterBroadcast.size() != 0) {
            for (int a = 0; a < removePhotoAfterBroadcast.size(); a++) {
                long key = removePhotoAfterBroadcast.keyAt(a);
                ArrayList<Object> arrayList = removePhotoAfterBroadcast.get(key);
                for (int b = 0; b < arrayList.size(); b++) {
                    removePhotoObserver(arrayList.get(b), key);
                }
            }
            removePhotoAfterBroadcast.clear();
        }
        if (addPhotoAfterBroadcast.size() != 0) {
            for (int a = 0; a < addPhotoAfterBroadcast.size(); a++) {
                long key = addPhotoAfterBroadcast.keyAt(a);
                ArrayList<Object> arrayList = addPhotoAfterBroadcast.get(key);
                for (int b = 0; b < arrayList.size(); b++) {
                    addPhotoObserver(arrayList.get(b), key);
                }
            }
            addPhotoAfterBroadcast.clear();
        }
    }

    public void addPhotoObserver(Object observer, long photoID) {
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = addPhotoAfterBroadcast.get(photoID);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                addPhotoAfterBroadcast.put(photoID, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        ArrayList<Object> objects = photoObservers.get(photoID);
        if (objects == null)
            photoObservers.put(photoID, (objects = new ArrayList<>()));

        if (!objects.contains(observer))
            objects.add(observer);
    }

    public void removePhotoObserver(Object observer, long photoID) {
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = removePhotoAfterBroadcast.get(photoID);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                removePhotoAfterBroadcast.put(photoID, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        ArrayList<Object> objects = photoObservers.get(photoID);
        if (objects != null)
            objects.remove(observer);
    }

    public void postStickerNotification(StickerPack.Sticker sticker) {
        boolean allowDuringAnimation = false;
//        if (allowedNotifications != null) {
//            for (int a = 0; a < allowedNotifications.length; a++) {
//                if (allowedNotifications[a] == photo.getID()) {
//                    allowDuringAnimation = true;
//                    break;
//                }
//            }
//        }
        postStickerNotificationInternal(allowDuringAnimation, sticker);
    }

    public void postStickerNotificationInternal(boolean allowDuringAnimation, StickerPack.Sticker sticker) {
        if (!allowDuringAnimation && animationInProgress) {
            DelayedStickerPost delayedStickerPost = new DelayedStickerPost(sticker);
            delayedStickerPosts.add(delayedStickerPost);
            return;
        }
        broadcasting++;
        long stickerID = sticker.id;

        ArrayList<Object> objects = stickerObservers.get(stickerID);
        if (objects != null && !objects.isEmpty()) {
            for (int a = 0; a < objects.size(); a++) {
                Object obj = objects.get(a);
                ((IStickerListener) obj).didLoadedSticker(sticker);
            }
        }
        broadcasting--;
        if (broadcasting == 0) {
            updateStickerBroadcasting();
        }
    }

    private void updateStickerBroadcasting() {
        if (removeStickerAfterBroadcast.size() != 0) {
            for (int a = 0; a < removeStickerAfterBroadcast.size(); a++) {
                long key = removeStickerAfterBroadcast.keyAt(a);
                ArrayList<Object> arrayList = removeStickerAfterBroadcast.get(key);
                for (int b = 0; b < arrayList.size(); b++) {
                    removeStickerObserver(arrayList.get(b), key);
                }
            }
            removeStickerAfterBroadcast.clear();
        }
        if (addStickerAfterBroadcast.size() != 0) {
            for (int a = 0; a < addStickerAfterBroadcast.size(); a++) {
                long key = addStickerAfterBroadcast.keyAt(a);
                ArrayList<Object> arrayList = addStickerAfterBroadcast.get(key);
                for (int b = 0; b < arrayList.size(); b++) {
                    addStickerObserver(arrayList.get(b), key);
                }
            }
            addStickerAfterBroadcast.clear();
        }
    }

    public void addStickerObserver(Object observer, long stickerID) {
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = addStickerAfterBroadcast.get(stickerID);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                addStickerAfterBroadcast.put(stickerID, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        ArrayList<Object> objects = stickerObservers.get(stickerID);
        if (objects == null)
            stickerObservers.put(stickerID, (objects = new ArrayList<>()));

        if (!objects.contains(observer))
            objects.add(observer);
    }

    public void removeStickerObserver(Object observer, long stickerID) {
        if (broadcasting != 0) {
            ArrayList<Object> arrayList = removeStickerAfterBroadcast.get(stickerID);
            if (arrayList == null) {
                arrayList = new ArrayList<>();
                removeStickerAfterBroadcast.put(stickerID, arrayList);
            }
            arrayList.add(observer);
            return;
        }
        ArrayList<Object> objects = stickerObservers.get(stickerID);
        if (objects != null)
            objects.remove(observer);
    }
}
