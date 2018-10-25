package ru.paymon.android.filepicker.models;

public class Media extends BaseFile {

    private int mediaType;

    public Media(int id, String name, String path, int mediaType) {
        super(id, name, path);
        this.mediaType = mediaType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Media)) return false;

        Media media = (Media) o;

        return id == media.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public String getPath() {
        return (path != null) ? path : "";
    }

    public int getMediaType() {
        return mediaType;
    }

}

