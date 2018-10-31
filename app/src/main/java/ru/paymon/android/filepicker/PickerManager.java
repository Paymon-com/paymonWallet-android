package ru.paymon.android.filepicker;

import android.arch.lifecycle.MutableLiveData;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.filepicker.utils.FilePickerConst;

/**
 * Created by droidNinja on 29/07/16.
 */
public class PickerManager {
    private static PickerManager ourInstance = new PickerManager();
    private int maxCount = FilePickerConst.DEFAULT_MAX_COUNT;
    private ArrayList<String> docFiles;
    private ArrayList<String> mediaFiles;
    public MutableLiveData<ArrayList<String>> mediasLiveData = new MutableLiveData<>();
    public MutableLiveData<ArrayList<String>> filesLiveData = new MutableLiveData<>();

    public static PickerManager getInstance() {
        return ourInstance;
    }

    private PickerManager() {
        mediaFiles = new ArrayList<>();
        docFiles = new ArrayList<>();
        mediasLiveData.postValue(mediaFiles);
        filesLiveData.postValue(docFiles);
    }

    public int getMaxCount() {
        return maxCount;
    }

    private int getCurrentCount() {
        return mediaFiles.size() + docFiles.size();
    }

    public void add(String path, int type) {
        if (path != null && shouldAdd()) {
            if (!mediaFiles.contains(path) && type == FilePickerConst.FILE_TYPE_MEDIA) {
                mediaFiles.add(path);
                mediasLiveData.postValue(mediaFiles);
            } else if (!docFiles.contains(path) && type == FilePickerConst.FILE_TYPE_DOCUMENT) {
                docFiles.add(path);
                filesLiveData.postValue(docFiles);
            }
        }
    }

    public void remove(String path, int type) {
        if ((type == FilePickerConst.FILE_TYPE_MEDIA) && mediaFiles.contains(path)) {
            mediaFiles.remove(path);
            mediasLiveData.postValue(mediaFiles);
        } else if (type == FilePickerConst.FILE_TYPE_DOCUMENT) {
            docFiles.remove(path);
            filesLiveData.postValue(docFiles);
        }
    }

    public void clearSelections() {
        mediaFiles.clear();
        docFiles.clear();
        mediasLiveData.postValue(mediaFiles);
        filesLiveData.postValue(docFiles);
    }

    public boolean shouldAdd() {
        if (maxCount == -1) return true;
        return getCurrentCount() < maxCount;
    }

    public ArrayList<String> getSelectedPhotos() {
        return mediaFiles;
    }

    public ArrayList<String> getSelectedFiles() {
        return docFiles;
    }

    public int getCameraDrawable() {
        return R.drawable.ic_camera;
    }
}

