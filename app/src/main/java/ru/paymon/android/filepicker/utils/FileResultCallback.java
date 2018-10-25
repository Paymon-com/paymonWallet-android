package ru.paymon.android.filepicker.utils;

import java.util.List;

public interface FileResultCallback<T> {
    void onResultCallback(List<T> files);
}
