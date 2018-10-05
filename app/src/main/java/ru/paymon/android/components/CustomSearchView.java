package ru.paymon.android.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.SearchView;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.TextView;

import ru.paymon.android.R;

public class CustomSearchView extends SearchView {
    public CustomSearchView(Context context) {
        super(context);
        setRobotoFont();
    }

    public CustomSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setRobotoFont();
    }

    public CustomSearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRobotoFont();
    }

    private void setRobotoFont(){
        TextView searchText = (TextView) findViewById(android.support.v7.appcompat.R.id.search_src_text);
        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/roboto_thin.ttf");
        searchText.setTextColor(getResources().getColor(R.color.gray_dim));
        searchText.setHintTextColor(getResources().getColor(R.color.gray_dim));
        searchText.setTypeface(typeface);
    }
}
