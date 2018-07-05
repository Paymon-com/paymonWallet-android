/*
 * Copyright 2014 Hieu Rocker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hani.momanii.supernova_emoji_library.emoji;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Hieu Rocker (rockerhieu@gmail.com)
 */
public class Emojicon implements Parcelable {
    public static Context context;
    private int icon;
    private BitmapDrawable drawable;
    private char value;
    public int codePoint;
    private String emoji;

    public Emojicon(int icon, char value, String emoji) {
        this.icon = icon;
        this.value = value;
        this.emoji = emoji;
    }

    private Emojicon() {
    }

    public Emojicon(String emoji) {
        this.emoji = emoji;
    }

    public BitmapDrawable getDrawable() {
        return drawable;
    }

    public static Emojicon fromResource(int icon, int value) {
        Emojicon emoji = new Emojicon();
        emoji.icon = icon;
        emoji.value = (char) value;
        emoji.codePoint = value;
        return emoji;
    }

    public static Emojicon fromCodePoint(int codePoint) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = newString(codePoint);
//        emoji.value = (char) codePoint;
        emoji.codePoint = codePoint;
//        Log.d("pdbg", "FCP " + codePoint + " " + (int) emoji.value);
        return emoji;
    }

    public static Emojicon fromChar(char ch) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = Character.toString(ch);
//        emoji.value = ch;
//        Log.d("pdbg", "FC " + (int)ch);
        emoji.codePoint = (int) ch;
        return emoji;
    }

    public static Emojicon fromChars(String chars) {
        Emojicon emoji = new Emojicon();
        emoji.emoji = chars;
        return emoji;
    }

    public static final String newString(int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf(codePoint);
        } else {
            return new String(Character.toChars(codePoint));
        }
    }

    public char getValue() {
        return value;
    }

    public int getIcon() {
        return icon;
    }

    public String getEmoji() {
        return emoji;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Emojicon && emoji.equals(((Emojicon) o).emoji);
    }

    @Override
    public int hashCode() {
        return emoji.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.icon);
//        dest.writeParcelable(this.drawable, flags);
        dest.writeInt(this.value);
        dest.writeString(this.emoji);
    }

    protected Emojicon(Parcel in) {
        this.icon = in.readInt();
//        this.drawable = in.readParcelable(BitmapDrawable.class.getClassLoader());
        this.value = (char) in.readInt();
        this.emoji = in.readString();
    }

    public static final Creator<Emojicon> CREATOR = new Creator<Emojicon>() {
        @Override
        public Emojicon createFromParcel(Parcel source) {
            return new Emojicon(source);
        }

        @Override
        public Emojicon[] newArray(int size) {
            return new Emojicon[size];
        }
    };
}
