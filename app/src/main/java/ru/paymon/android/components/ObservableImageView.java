//package ru.paymon.android.components;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.drawable.BitmapDrawable;
//import android.support.v7.widget.AppCompatImageView;
//import android.util.AttributeSet;
//import android.util.Log;
//
//import ru.paymon.android.ApplicationLoader;
//import ru.paymon.android.Config;
//import ru.paymon.android.MediaManager;
//import ru.paymon.android.ObservableMediaManager;
//import ru.paymon.android.R;
//import ru.paymon.android.StickerPack;
//import ru.paymon.android.User;
//import ru.paymon.android.models.Photo;
//import ru.paymon.android.net.RPC;
//import ru.paymon.android.utils.FileManager;
//import ru.paymon.android.utils.Utils;
//import ru.paymon.android.utils.cache.lrudiskcache.DiskLruImageCache;
//
//public class ObservableImageView extends AppCompatImageView implements ObservableMediaManager.IPhotoListener, ObservableMediaManager.IStickerListener {
//    private long photoID;
//    private int photoOwnerID;
//    private int userId;
//    private long itemID;
//    private FileManager.FileType itemType;
//    private BitmapDrawable bitmap;
//
//    public ObservableImageView(Context context) {
//        super(context);
//        photoID = Long.MIN_VALUE;
//        photoOwnerID = Integer.MIN_VALUE;
//    }
//
//    public ObservableImageView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public ObservableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    public void subscribeItem(FileManager.FileType itemType, long itemID) {
//        this.itemType = itemType;
//
//        ObservableMediaManager.getInstance().removeStickerObserver(this, this.itemID);
//        ObservableMediaManager.getInstance().addStickerObserver(this, itemID);
//
//        this.itemID = itemID;
//
//        tryLoadSticker();
//    }
//
//    public void setUserId(int userId) {
//        this.userId = userId;
//    }
//
//    public int getUserId() {
//        return this.userId;
//    }
//
//    public void setPhoto(RPC.PM_photo photo) {
//        if (photoURL != null && photoURL.gid > 0) {
//            int userID = photo.user_id;
//            long photoID = photo.gid;
//            subscribeProfilePhoto(userID, photoID);
//        } else {
//            setImageBitmap(DiskLruImageCache.getInstance().getBitmap(String.valueOf(R.drawable.profile_photo_none)));
//        }
//    }
//
//    public void setSticker(FileManager.FileType itemType, long itemID) {
//        if (!(this.itemID == itemID && itemType == FileManager.FileType.NONE)) {
//            if (!(itemID == 0 || itemType == FileManager.FileType.NONE))
//                subscribeItem(itemType, itemID);
//        }
//    }
//
//    public void subscribeProfilePhoto(int ownerID, long photoID) {
//        ObservableMediaManager.getInstance().removePhotoObserver(this, this.photoID);
//        ObservableMediaManager.getInstance().addPhotoObserver(this, photoID);
//        this.photoOwnerID = ownerID;
//        this.photoID = photoID;
//        tryLoadBitmap();
//    }
//
//    @Override
//    public void didLoadedSticker(StickerPack.Sticker sticker) {
//        bitmap = sticker.image;
//        setImageDrawable(bitmap);
//        invalidate();
//    }
//
//    @Override
//    public void didLoadedPhoto(final Photo photo) {
//        bitmap = new BitmapDrawable(getResources(), photo.bitmap);
//        setImageDrawable(bitmap);
//        invalidate();
//    }
//
//    @Override
//    public void didUpdatedPhotoID(long newPhotoID, int userID) {
//        this.photoID = newPhotoID;
//        tryLoadBitmap();
//    }
//
//    private void tryLoadBitmap() {
//        BitmapDrawable bitmap = ObservableMediaManager.getInstance().loadPhotoBitmap(photoOwnerID, photoID);
//
//        if (bitmap != null) {
//            this.bitmap = bitmap;
//            setImageDrawable(bitmap);
//        } else {
//            Utils.netQueue.postRunnable(() -> MediaManager.getInstance().requestPhoto(photoOwnerID, photoID));
//        }
//        invalidate();
//    }
//
//    private void tryLoadSticker() {
//        if (itemID <= 0)
//            return;
//
//        BitmapDrawable bitmap = ObservableMediaManager.getInstance().loadStickerBitmap(itemID);
//
//        if (bitmap != null) {
//            this.bitmap = bitmap;
//            setImageDrawable(bitmap);
//        } else {
//            Utils.netQueue.postRunnable(() -> MediaManager.getInstance().requestStickerPack(1));
//        }
//        invalidate();
//    }
//
//
//    public void destroy() { //TODO:удалять обсервер когда вью не станет!!!!
//        ObservableMediaManager.getInstance().removePhotoObserver(this, photoID);
//    }
//}
