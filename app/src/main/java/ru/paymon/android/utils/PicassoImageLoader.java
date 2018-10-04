package ru.paymon.android.utils;

import android.widget.ImageView;

import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;
import com.squareup.picasso.Picasso;

import java.io.File;

public class PicassoImageLoader implements ImageLoader {

    @Override
    public void loadImage(String path, ImageView imageView, ImageType imageType) {
        Picasso.get().load(new File(path))
                .resize(300, 0)
                .into(imageView);
    }
}
