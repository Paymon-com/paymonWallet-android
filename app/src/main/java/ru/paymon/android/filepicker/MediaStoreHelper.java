package ru.paymon.android.filepicker;


import android.content.ContentResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.paymon.android.filepicker.models.Document;
import ru.paymon.android.filepicker.utils.FileResultCallback;


public class MediaStoreHelper {

    public static final ArrayList<String> extensionsList = new ArrayList<String>() {
        {
            add("TXT");
            add("PDF");
            add("WORD");
            add("EXCEL");
            add("PPT");
            add("ZIP");
        }
    };
    public static final Map<String, List<String>> extensionsMap = new HashMap<String, List<String>>() {
        {
            put("TXT", Arrays.asList("txt", "csv"));
            put("PDF", Collections.singletonList("pdf"));
            put("WORD", Arrays.asList("doc", "docm", "docx", "dot", "dotm", "dotx"));
            put("EXCEL", Arrays.asList("xlsx", "xla", "xlam", "xls", "xlsb", "xlsm", "xlt", "xltm", "xltx"));
            put("PPT", Arrays.asList("ppt", "pot", "potm", "potx", "ppa", "ppam", "ppsm", "pptm", "pptx"));
            put("ZIP", Arrays.asList("zip", "7z", "rar", "zipx"));
        }
    };

    public static void getPhotosDir(ContentResolver contentResolver, FileResultCallback<FileDirectory> resultCallback) {
        new PhotoScannerTask(contentResolver, resultCallback).execute();
    }

    public static void getDocs(ContentResolver contentResolver, FileResultCallback<Document> resultCallback) {
        new DocScannerTask(contentResolver, resultCallback).execute();
    }

}