package ru.paymon.android.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;

import ru.paymon.android.Config;
import ru.paymon.android.MediaManager;
import ru.paymon.android.ObservableMediaManager;
import ru.paymon.android.StickerPack;
import ru.paymon.android.User;
import ru.paymon.android.models.Photo;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.Utils;

public class ObservableImageView extends AppCompatImageView implements ObservableMediaManager.IPhotoListener, ObservableMediaManager.IStickerListener {
    public static Bitmap profilePhotoNoneBitmap;

    private long photoID;
    private int photoOwnerID;
    private int userId;
    private long itemID;
    private FileManager.FileType itemType;
    private BitmapDrawable bitmap;
    private boolean added = false;

    public ObservableImageView(Context context) {
        super(context);
        photoID = Long.MIN_VALUE;
        photoOwnerID = Integer.MIN_VALUE;
    }

    public ObservableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void subscribe(long photoID, int ownerID) {
        if (photoID == 0 || this.photoID == photoID || ownerID == 0) return;
        this.photoOwnerID = ownerID;

        ObservableMediaManager.getInstance().removePhotoObserver(this, this.photoID);
        ObservableMediaManager.getInstance().addPhotoObserver(this, photoID);
        this.photoID = photoID;

    }

    public void subscribeItem(FileManager.FileType itemType, long itemID) {
        this.itemType = itemType;

        if (added) {
            ObservableMediaManager.getInstance().removeStickerObserver(this, itemID);
        }
        ObservableMediaManager.getInstance().addStickerObserver(this, itemID);
        added = true;
        this.itemID = itemID;

        tryLoadSticker();
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setPhoto(RPC.PM_photo photo) {
        setPhoto(photo.user_id, photo.id);
    }

    public void setPhoto(int ownerID, long photoID) {
        if (this.photoID == photoID && this.photoOwnerID == ownerID) {
            return;
        } else {
            if (photoID == 0 || ownerID == 0) {
                setImageBitmap(profilePhotoNoneBitmap);
                return;
            }
            subscribeProfilePhoto(ownerID, photoID);
        }
    }

    public void setSticker(FileManager.FileType itemType, long itemID) {
        if (this.itemID == itemID && itemType == FileManager.FileType.NONE) {
            return;
        } else {
            if (itemID == 0 || itemType == FileManager.FileType.NONE) {
                return;
            }
            subscribeItem(itemType, itemID);
        }
    }

    public void subscribeProfilePhoto(int ownerID, long photoID) {
        this.photoOwnerID = ownerID;
        if (added) {
            ObservableMediaManager.getInstance().removePhotoObserver(this, this.photoID);
        }
        ObservableMediaManager.getInstance().addPhotoObserver(this, photoID);
        added = true;
        this.photoID = photoID;

        tryLoadBitmap();
    }

    @Override
    public void didLoadedSticker(StickerPack.Sticker sticker) {
        bitmap = sticker.image;
        setImageDrawable(bitmap);
        invalidate();
    }

    @Override
    public void didLoadedPhoto(final Photo photo) {
        bitmap = new BitmapDrawable(getResources(), photo.bitmap);
        setImageDrawable(bitmap);
        invalidate();
    }


    @Override
    public void loadProgress(int progress) {

    }

    @Override
    public void didUpdatedPhotoID(long newPhotoID, int userID) {
        this.photoID = newPhotoID;
        tryLoadBitmap();
    }

    private void tryLoadBitmap() {
        if (photoID <= 0 && photoOwnerID != User.currentUser.id) {
            setImageBitmap(profilePhotoNoneBitmap);
            return;
        }
        Log.d(Config.TAG, "trying to load bitmap " + photoOwnerID + "_" + photoID);

        BitmapDrawable bitmap = ObservableMediaManager.getInstance().loadPhotoBitmap(photoOwnerID, photoID);

        if (bitmap != null) {
            this.bitmap = bitmap;
            setImageDrawable(bitmap);
        } else {
            setImageBitmap(profilePhotoNoneBitmap);

            Utils.netQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    MediaManager.getInstance().requestPhoto(photoOwnerID, photoID);
                }
            });
        }
        invalidate();
    }

    private void tryLoadSticker() {
        if (itemID <= 0) {
            return;
        }
        Log.d(Config.TAG, "trying to load sticker " + itemType.toString() + "_" + itemID);

        BitmapDrawable bitmap = ObservableMediaManager.getInstance().loadStickerBitmap(itemID);

        if (bitmap != null) {
            this.bitmap = bitmap;
            setImageDrawable(bitmap);
        } else {
            setImageBitmap(profilePhotoNoneBitmap);

            Utils.netQueue.postRunnable(new Runnable() {
                @Override
                public void run() {
                    MediaManager.getInstance().requestStickerPack(1);
                }
            });
        }
        invalidate();
    }

    public void destroy() {
        ObservableMediaManager.getInstance().removePhotoObserver(this, photoID);
        added = false;
    }
}
