package ru.paymon.android.filepicker.models;

import java.io.File;

/**
 * Created by droidNinja on 29/07/16.
 */
public class Document extends BaseFile {
    private String size;

    public Document(int id, String title, String path) {
        super(id,title,path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Document)) return false;

        Document document = (Document) o;

        return id == document.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTitle() {
        return new File(this.path).getName();
    }

    public void setTitle(String title) {
        this.name = title;
    }
}

