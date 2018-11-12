package ru.paymon.android.components;

import android.content.Context;
import android.os.Build;
import android.preference.ListPreference;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

public class CustomListPreference extends ListPreference {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListPreference(Context context) {
        super(context);
    }

    public void setBackground(){

    }
}
