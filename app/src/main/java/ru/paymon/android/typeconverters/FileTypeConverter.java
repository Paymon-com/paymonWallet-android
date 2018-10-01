package ru.paymon.android.typeconverters;

import android.arch.persistence.room.TypeConverter;

import ru.paymon.android.utils.FileManager;

public class FileTypeConverter {
    @TypeConverter
    public static FileManager.FileType toFileType(String string) {
        return string.equals("") ? null : FileManager.FileType.values()[Integer.parseInt(string)];
    }

    @TypeConverter
    public static String toString(FileManager.FileType fileType) {
        return fileType == null ? "" : fileType.ordinal() + "";
    }
}
