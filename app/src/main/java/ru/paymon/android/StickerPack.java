package ru.paymon.android;

import android.graphics.drawable.BitmapDrawable;

import java.util.HashMap;

public class StickerPack {
    public int id;
    public String title;
    public String author;
    public HashMap<Long, Sticker> stickers;

    public static class Sticker {
        public BitmapDrawable image;
        public long id;
    }

    public StickerPack(int id) {
        stickers = new HashMap<>();
        this.id = id;
        this.title = "";
        this.author = "";
    }

    public StickerPack(int id, int stickersCount, String title, String author) {
        stickers = new HashMap<>(stickersCount);
        this.id = id;
        this.title = title;
        this.author = author;
    }
}
