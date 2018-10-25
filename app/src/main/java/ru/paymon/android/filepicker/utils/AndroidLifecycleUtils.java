package ru.paymon.android.filepicker.utils;

import android.app.Activity;
import android.content.Context;

public class AndroidLifecycleUtils {

    public static boolean canLoadImage(Context context) {
        if (context == null) {
            return true;
        }

        if (!(context instanceof Activity)) {
            return true;
        }

        Activity activity = (Activity) context;
        return canLoadImage(activity);
    }

    private static boolean canLoadImage(Activity activity) {
        if (activity == null) {
            return true;
        }

        boolean destroyed = activity.isDestroyed();

        return !destroyed && !activity.isFinishing();
    }
}
