//package ru.paymon.android.components;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.util.AttributeSet;
//
//import ru.paymon.android.components.CircularImageView;
//
//import ru.paymon.android.ApplicationLoader;
//import ru.paymon.android.MediaManager;
//import ru.paymon.android.R;
//import ru.paymon.android.net.RPC;
//import ru.paymon.android.utils.Utils;
//import ru.paymon.android.utils.cache.lrudiskcache.DiskLruImageCache;
//
//public class CircleImageView2 extends CircularImageView{
//    public CircleImageView2(Context context) {
//        super(context);
//    }
//
//    public CircleImageView2(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public CircleImageView2(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    public void setPhoto(RPC.PM_photo photo) {
//        if (photo != null)
//            this.photoURL = photoURL;
//
//        if (photoURL != null && photoURL.gid > 0) {
//            Utils.stageQueue.postRunnable(() -> tryLoadBitmap());
//        } else {
//            setImageBitmap(DiskLruImageCache.getInstance().getBitmap(String.valueOf(R.drawable.profile_photo_none)));
//        }
//    }
//
//    private void tryLoadBitmap() {
//        Bitmap bitmap = MediaManager.getInstance().loadPhotoBitmap(photoURL.user_id, photoURL.gid);
//        if (bitmap != null) {
//            ApplicationLoader.applicationHandler.post(()->setImageBitmap(bitmap));
//        } else {
//            Utils.netQueue.postRunnable(() -> MediaManager.getInstance().requestPhoto(photoURL.user_id, photoURL.gid));
//        }
//        ApplicationLoader.applicationHandler.post(()->invalidate());
//    }
//}
