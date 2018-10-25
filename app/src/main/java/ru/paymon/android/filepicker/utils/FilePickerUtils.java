package ru.paymon.android.filepicker.utils;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Created by droidNinja on 29/07/16.
 */
public class FilePickerUtils {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <T> Collection<T> filter(Collection<T> target, Predicate<T> predicate) {
        Collection<T> result = new ArrayList<T>();
        for (T element : target) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result;
    }

    public static boolean contains(String[] types, String path) {
        for (String string : types) {
            if (path.toLowerCase().endsWith(string)) return true;
        }
        return false;
    }
}

