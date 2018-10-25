package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.filepicker.adapters.PhotoGridAdapter;
import ru.paymon.android.filepicker.models.BaseFile;
import ru.paymon.android.filepicker.FileDirectory;
import ru.paymon.android.filepicker.utils.FilePickerConst;
import ru.paymon.android.filepicker.MediaStoreHelper;
import ru.paymon.android.filepicker.PickerManager;

public class FragmentAttachmentImage extends Fragment {
    private PhotoGridAdapter photoGridAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attachment_image, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.attachment_image_recycler_view);

        Bundle mediaStoreArgs = new Bundle();
        mediaStoreArgs.putBoolean(FilePickerConst.EXTRA_SHOW_GIF, false);
        mediaStoreArgs.putString(FilePickerConst.EXTRA_BUCKET_ID, FilePickerConst.ALL_PHOTOS_BUCKET_ID);

        mediaStoreArgs.putInt(FilePickerConst.EXTRA_FILE_TYPE, FilePickerConst.FILE_TYPE_MEDIA);

        final ArrayList<BaseFile> medias = new ArrayList<>();
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);

        MediaStoreHelper.getPhotosDir(getActivity().getContentResolver(), mediaStoreArgs, dirs -> {
            for (FileDirectory photoDir: dirs)
                medias.addAll(photoDir.getFiles());
            photoGridAdapter = new PhotoGridAdapter(getContext(), Glide.with(getActivity()), medias, PickerManager.getInstance().getSelectedPhotos(), false);
            recyclerView.setAdapter(photoGridAdapter);
        });

        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        return view;
    }
}
