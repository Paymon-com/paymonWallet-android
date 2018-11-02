package ru.paymon.android.filepicker.models;

import java.io.File;

public class Document extends BaseFile {

    public Document(int id, String title, String path) {
        super(id, title, path);
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

    public String getTitle() {
        return new File(this.path).getName();
    }
}

