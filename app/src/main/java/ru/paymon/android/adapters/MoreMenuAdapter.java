package ru.paymon.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.models.MoreMenuItem;

public class MoreMenuAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<MoreMenuItem> moreMenuItemArrayList;

    public MoreMenuAdapter(Context context, ArrayList<MoreMenuItem> moreMenuItemArrayList) {
        this.moreMenuItemArrayList = moreMenuItemArrayList;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.moreMenuItemArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return this.moreMenuItemArrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {

        View view = convertView;
        if (view == null) {
            view = this.inflater.inflate(R.layout.more_menu_item, viewGroup, false);
        }

        MoreMenuItem moreMenuItem = (MoreMenuItem) getItem(position);

        ((ImageView) view.findViewById(R.id.more_menu_item_image_view)).setImageResource(moreMenuItem.icon);

        ((TextView) view.findViewById(R.id.more_menu_item_text_view)).setText(moreMenuItem.text);

        return view;
    }
}
