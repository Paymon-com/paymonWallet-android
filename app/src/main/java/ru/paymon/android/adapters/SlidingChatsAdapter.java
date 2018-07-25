package ru.paymon.android.adapters;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import java.util.List;

public class SlidingChatsAdapter extends PagerAdapter {
    List<View> pages;

    public SlidingChatsAdapter(List<View> pages) {
        this.pages = pages;
    }

    @Override
    public Object instantiateItem(View collection, int position) {
        View v = pages.get(position);
        ((ViewPager) collection).addView(v, 0);
        return v;
    }

    @Override
    public void destroyItem(View collection, int position, Object view) {
        ((ViewPager) collection).removeView((View) view);
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Parcelable saveState() {
        return null;
    }


}

