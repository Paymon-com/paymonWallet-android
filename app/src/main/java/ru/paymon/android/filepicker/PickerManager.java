package ru.paymon.android.filepicker;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import ru.paymon.android.R;
import ru.paymon.android.filepicker.models.FileType;
import ru.paymon.android.filepicker.utils.FilePickerConst;
import ru.paymon.android.filepicker.utils.Orientation;

/**
 * Created by droidNinja on 29/07/16.
 */
public class PickerManager {
    private static PickerManager ourInstance = new PickerManager();
    private int maxCount = FilePickerConst.DEFAULT_MAX_COUNT;

    public static PickerManager getInstance() {
        return ourInstance;
    }

    private ArrayList<String> mediaFiles;
    private ArrayList<String> docFiles;

    private LinkedHashSet<FileType> fileTypes;

    private int theme = R.style.AppTheme;

    private String title = null;

    private Orientation orientation = Orientation.UNSPECIFIED;

    private PickerManager() {
        mediaFiles = new ArrayList<>();
        docFiles = new ArrayList<>();
        fileTypes = new LinkedHashSet<>();
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int getCurrentCount() {
        return mediaFiles.size() + docFiles.size();
    }

    public void add(String path, int type) {
        if (path != null && shouldAdd()) {
            if (!mediaFiles.contains(path) && type == FilePickerConst.FILE_TYPE_MEDIA) {
                mediaFiles.add(path);
            } else if (!docFiles.contains(path) && type == FilePickerConst.FILE_TYPE_DOCUMENT) {
                docFiles.add(path);
            } else {
                return;
            }
        }
    }

    public void add(ArrayList<String> paths, int type) {
        for (int index = 0; index < paths.size(); index++) {
            add(paths.get(index), type);
        }
    }

    public void remove(String path, int type) {
        if ((type == FilePickerConst.FILE_TYPE_MEDIA) && mediaFiles.contains(path)) {
            mediaFiles.remove(path);
        } else if (type == FilePickerConst.FILE_TYPE_DOCUMENT) {
            docFiles.remove(path);
        }
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

    public void reset() {
        docFiles.clear();
        mediaFiles.clear();
        fileTypes.clear();
        maxCount = -1;
    }

    public int getTheme() {
        return theme;
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }
}

