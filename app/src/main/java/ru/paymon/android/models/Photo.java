package ru.paymon.android.models;

import android.graphics.Bitmap;

public class Photo {
    public long id;
    public int ownerID;
    public Bitmap bitmap;

    public Photo(long id, int ownerID, Bitmap bitmap) {
        this.id = id;
        this.ownerID = ownerID;
        this.bitmap = bitmap;
    }
}
