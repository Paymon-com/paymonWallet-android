package ru.paymon.android.filepicker.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.components.SmoothCheckBox;
import ru.paymon.android.filepicker.PickerManager;
import ru.paymon.android.filepicker.models.BaseFile;
import ru.paymon.android.filepicker.models.Media;
import ru.paymon.android.filepicker.utils.AndroidLifecycleUtils;
import ru.paymon.android.filepicker.utils.FilePickerConst;

public class PhotoGridAdapter extends SelectableAdapter<PhotoGridAdapter.PhotoViewHolder, BaseFile> {

    private final Context context;
    private final RequestManager glide;
    private final boolean showCamera;
    private int imageSize;
    private View.OnClickListener cameraOnClickListener;

    private final static int ITEM_TYPE_CAMERA = 100;
    private final static int ITEM_TYPE_PHOTO = 101;

    public PhotoGridAdapter(Context context, RequestManager requestManager, ArrayList<BaseFile> medias, ArrayList<String> selectedPaths, boolean showCamera) {
        super(medias, selectedPaths);
        this.context = context;
        this.glide = requestManager;
        this.showCamera = showCamera;
        setColumnNumber(context);
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_holder_attachment_photo_item, viewGroup, false);

        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_TYPE_PHOTO) {

            final Media media = (Media) getItems().get(showCamera ? position - 1 : position);

            if (AndroidLifecycleUtils.canLoadImage(holder.imageView.getContext())) {
                glide.load(new File(media.getPath()))
                        .apply(RequestOptions
                                .centerCropTransform()
                                .override(imageSize, imageSize)
                                .placeholder(R.drawable.image_placeholder))
                        .thumbnail(0.5f)
                        .into(holder.imageView);
            }


            if (media.getMediaType() == FilePickerConst.MEDIA_TYPE_VIDEO)
                holder.videoIcon.setVisibility(View.VISIBLE);
            else
                holder.videoIcon.setVisibility(View.GONE);

            holder.itemView.setOnClickListener(v -> onItemClicked(holder, media));

            holder.checkBox.setVisibility(View.GONE);
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setOnClickListener(view -> onItemClicked(holder, media));

            holder.checkBox.setChecked(isSelected(media));

            holder.selectBg.setVisibility(isSelected(media) ? View.VISIBLE : View.GONE);
            holder.checkBox.setVisibility(isSelected(media) ? View.VISIBLE : View.GONE);

            holder.checkBox.setOnCheckedChangeListener((checkBox, isChecked) -> {
                toggleSelection(media);
                holder.selectBg.setVisibility(isChecked ? View.VISIBLE : View.GONE);

                if (isChecked) {
                    holder.checkBox.setVisibility(View.VISIBLE);
                    PickerManager.getInstance().add(media.getPath(), FilePickerConst.FILE_TYPE_MEDIA);
                } else {
                    holder.checkBox.setVisibility(View.GONE);
                    PickerManager.getInstance().remove(media.getPath(), FilePickerConst.FILE_TYPE_MEDIA);
                }
            });

        } else {
            holder.imageView.setImageResource(PickerManager.getInstance().getCameraDrawable());
            holder.checkBox.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(cameraOnClickListener);
            holder.videoIcon.setVisibility(View.GONE);
        }
    }

    private void onItemClicked(PhotoViewHolder holder, Media media) {
        if (PickerManager.getInstance().getMaxCount() == 1) {
            PickerManager.getInstance().add(media.getPath(), FilePickerConst.FILE_TYPE_MEDIA);
        } else if (holder.checkBox.isChecked() || PickerManager.getInstance().shouldAdd()) {
            holder.checkBox.setChecked(!holder.checkBox.isChecked(), true);
        }
    }

    private void setColumnNumber(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        imageSize = widthPixels / 3;
    }

    @Override
    public int getItemCount() {
        if (showCamera)
            return getItems().size() + 1;
        return getItems().size();
    }

    @Override
    public int getItemViewType(int position) {
        if (showCamera)
            return (position == 0) ? ITEM_TYPE_CAMERA : ITEM_TYPE_PHOTO;
        else
            return ITEM_TYPE_PHOTO;
    }

    public void setCameraListener(View.OnClickListener onClickListener) {
        this.cameraOnClickListener = onClickListener;
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        SmoothCheckBox checkBox;
        ImageView imageView;
        ImageView videoIcon;
        View selectBg;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = (SmoothCheckBox) itemView.findViewById(R.id.checkbox);
            imageView = (ImageView) itemView.findViewById(R.id.iv_photo);
            videoIcon = (ImageView) itemView.findViewById(R.id.video_icon);
            selectBg = itemView.findViewById(R.id.transparent_bg);
        }
    }
}
