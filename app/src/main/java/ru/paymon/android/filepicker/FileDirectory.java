package ru.paymon.android.filepicker;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.List;

import ru.paymon.android.filepicker.models.BaseFile;
import ru.paymon.android.filepicker.models.Document;
import ru.paymon.android.filepicker.models.Media;
import ru.paymon.android.filepicker.utils.FilePickerConst;

public class FileDirectory extends BaseFile implements Parcelable{

    private String bucketId;
    private String coverPath;
    private String name;
    private long dateAdded;
    private List<BaseFile> files = new ArrayList<>();

    public FileDirectory()
    {
        super();
    }

    public FileDirectory(int id, String name, String path) {
        super(id, name, path);
    }

    protected FileDirectory(Parcel in) {
        bucketId = in.readString();
        coverPath = in.readString();
        name = in.readString();
        dateAdded = in.readLong();
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

    public String getCoverPath() {
        if(files !=null && files.size()>0)
            return files.get(0).getPath();
        else if(coverPath!=null)
            return coverPath;
        else
            return "";
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public List<BaseFile> getFiles() {
        return files;
    }

    public void setFiles(List<BaseFile> files) {
        this.files = files;
    }

    public List<String> getPhotoPaths() {
        List<String> paths = new ArrayList<>(files.size());
        for (BaseFile media : files) {
            paths.add(media.getPath());
        }
        return paths;
    }

    public void addPhoto(int id, String name, String path, int mediaType) {
        files.add(new Media(id, name, path, mediaType));
    }

    public void addDocument(int id, String name, String path) {
        files.add(new Document(id, name, path));
    }

    public void addPhoto(Media media) {
        files.add(media);
    }

    public void addPhotos(List<Media> photosList) {
        files.addAll(photosList);
    }

    public String getBucketId() {
        if(bucketId.equals(FilePickerConst.ALL_PHOTOS_BUCKET_ID))
            return null;
        return bucketId;
    }

    public void setBucketId(String bucketId) {
        this.bucketId = bucketId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(bucketId);
        parcel.writeString(coverPath);
        parcel.writeString(name);
        parcel.writeLong(dateAdded);
    }
}
