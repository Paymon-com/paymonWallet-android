package ru.paymon.android.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.filepicker.FileDirectory;
import ru.paymon.android.filepicker.ImageCaptureManager;
import ru.paymon.android.filepicker.MediaStoreHelper;
import ru.paymon.android.filepicker.PickerManager;
import ru.paymon.android.filepicker.adapters.PhotoGridAdapter;
import ru.paymon.android.filepicker.models.BaseFile;
import ru.paymon.android.filepicker.models.Media;
import ru.paymon.android.filepicker.utils.FilePickerConst;

import static ru.paymon.android.filepicker.utils.FilePickerConst.FILE_TYPE_MEDIA;

public class FragmentAttachmentImage extends Fragment {
    private PhotoGridAdapter photoGridAdapter;
    private ImageCaptureManager imageCaptureManager;
    private ArrayList<BaseFile> medias = new ArrayList<>();
    private RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_attachment_image, container, false);

         recyclerView = (RecyclerView) view.findViewById(R.id.attachment_image_recycler_view);

        imageCaptureManager = new ImageCaptureManager(getActivity());

        Bundle mediaStoreArgs = new Bundle();
        mediaStoreArgs.putBoolean(FilePickerConst.EXTRA_SHOW_GIF, false);
        mediaStoreArgs.putString(FilePickerConst.EXTRA_BUCKET_ID, FilePickerConst.ALL_PHOTOS_BUCKET_ID);

        mediaStoreArgs.putInt(FilePickerConst.EXTRA_FILE_TYPE, FILE_TYPE_MEDIA);

        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        photoGridAdapter = new PhotoGridAdapter(getContext(), Glide.with(getActivity()), medias, PickerManager.getInstance().getSelectedPhotos(), true);
        recyclerView.setAdapter(photoGridAdapter);
        loadData();

        return view;
    }

    private void loadData() {
        MediaStoreHelper.getPhotosDir(getActivity().getContentResolver(), dirs -> {
            medias.clear();
            for (FileDirectory photoDir : dirs)
                medias.addAll(photoDir.getFiles());

            photoGridAdapter.notifyDataSetChanged();

            photoGridAdapter.setCameraListener(v -> {
                try {
                    Intent intent = imageCaptureManager.dispatchTakePictureIntent(getActivity());
                    if (intent != null)
                        startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
                    else
                        Toast.makeText(getActivity(), R.string.qr_scanner_no_access_to_camera, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ImageCaptureManager.REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    medias.add(0, new Media(0, "photo", imageCaptureManager.notifyMediaStoreDatabase(), FILE_TYPE_MEDIA));
                    photoGridAdapter.notifyItemInserted(1);
                }
                break;
        }
    }
}
