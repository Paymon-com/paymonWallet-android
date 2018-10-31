package ru.paymon.android.filepicker;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

public class ImageCaptureManager {

    public static final int REQUEST_TAKE_PHOTO = 0x101;

    private String mCurrentPhotoPath;
    private Context mContext;

    public ImageCaptureManager(Context mContext) {
        this.mContext = mContext;
    }

    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        if (!storageDir.exists()) {
            if (!storageDir.mkdir()) {
                throw new IOException();
            }
        }

        File image = new File(storageDir, imageFileName);

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    public Intent dispatchTakePictureIntent(Context context) throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(mContext.getPackageManager()) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                File newFile = createImageFile();
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                String author = context.getApplicationContext().getPackageName() + ".provider";
                Uri photoURI = FileProvider.getUriForFile(context, author, newFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            } else {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(createImageFile()));
            }
            return takePictureIntent;
        }
        return null;
    }


    public String notifyMediaStoreDatabase() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        if (TextUtils.isEmpty(mCurrentPhotoPath)) {
            return null;
        }

        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        mContext.sendBroadcast(mediaScanIntent);

        return mCurrentPhotoPath;
    }

}
