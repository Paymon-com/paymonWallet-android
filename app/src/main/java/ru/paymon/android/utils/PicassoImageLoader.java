package ru.paymon.android.utils;

import android.widget.ImageView;

import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import ru.paymon.android.R;

public class PicassoImageLoader implements ImageLoader {

    @Override
    public void loadImage(String path, ImageView imageView, ImageType imageType) {
        Picasso.get().load(path)
                .resize(300, 300)
                .centerCrop()
                .placeholder(R.drawable.profile_photo_none)
                .error(R.drawable.profile_photo_none)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView);
    }
}
