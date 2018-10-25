package ru.paymon.android.filepicker;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import ru.paymon.android.filepicker.models.BaseFile;
import ru.paymon.android.filepicker.models.Media;

public class FileDirectory extends BaseFile implements Parcelable{

    private String bucketId;
    private String name;
    private List<BaseFile> files = new ArrayList<>();

    public FileDirectory()
    {
        super();
    }

    private FileDirectory(Parcel in) {
        bucketId = in.readString();
        name = in.readString();
    }

    public static final Creator<FileDirectory> CREATOR = new Creator<FileDirectory>() {
        @Override
        public FileDirectory createFromParcel(Parcel in) {
            return new FileDirectory(in);
        }

        @Override
        public FileDirectory[] newArray(int size) {
            return new FileDirectory[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileDirectory)) return false;

        FileDirectory directory = (FileDirectory) o;

        boolean hasId = !TextUtils.isEmpty(bucketId);
        boolean otherHasId = !TextUtils.isEmpty(directory.bucketId);

        if (hasId && otherHasId) {
            if (!TextUtils.equals(bucketId, directory.bucketId)) {
                return false;
            }

            return TextUtils.equals(name, directory.name);
        }

        return false;
    }

    @Override
    public int hashCode() {
        if (TextUtils.isEmpty(bucketId)) {
            if (TextUtils.isEmpty(name)) {
                return 0;
            }

            return name.hashCode();
        }

        int result = bucketId.hashCode();

        if (TextUtils.isEmpty(name)) {
            return result;
        }

        result = 31 * result + name.hashCode();
        return result;
    }

    public List<BaseFile> getFiles() {
        return files;
    }

    public void addPhoto(int id, String name, String path, int mediaType) {
        files.add(new Media(id, name, path, mediaType));
    }
}
