//package ru.paymon.android;
//
//import android.graphics.drawable.BitmapDrawable;
//
//import java.util.HashMap;
//
//public class StickerPack {
//    public int gid;
//    public String title;
//    public String author;
//    public HashMap<Long, Sticker> stickers;
//
//    public static class Sticker {
//        public BitmapDrawable image;
//        public long gid;
//    }
//
//    public StickerPack(int gid) {
//        stickers = new HashMap<>();
//        this.gid = gid;
//        this.title = "";
//        this.author = "";
//    }
//
//    public StickerPack(int gid, int stickersCount, String title, String author) {
//        stickers = new HashMap<>(stickersCount);
//        this.gid = gid;
//        this.title = title;
//        this.author = author;
//    }
//}
