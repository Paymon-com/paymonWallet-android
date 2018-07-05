package hani.momanii.supernova_emoji_library.helper;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.StickerPack;
import ru.paymon.android.components.StickerItemView;
import ru.paymon.android.utils.FileManager;

public class StickerGridAdapter extends ArrayAdapter<StickerPack.Sticker> {
    private LinkedList<StickerPack.Sticker> stickers;
    private EmojiconsPopup.StickersListener stickerOnClickListener;

    public StickerGridAdapter(@NonNull Context context) {
        super(context, R.layout.sticker_item);
        stickers = new LinkedList<>();
    }

    @Nullable
    @Override
    public StickerPack.Sticker getItem(int position) {
        return stickers.get(position);
    }

    @Override
    public int getCount() {
        return stickers.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder;

        if (v == null) {
            v = View.inflate(getContext(), R.layout.sticker_item, null);

            holder = new ViewHolder();
            holder.view = (StickerItemView) v.findViewById(R.id.sticker_icon);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (stickerOnClickListener != null) {
                        stickerOnClickListener.onStickerSelected(1, position + 1);
                    }
                }
            });
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        StickerPack.Sticker sticker = getItem(position);
        if (sticker != null && sticker.image != null) {
//            holder.view.setImageDrawable(sticker.image);
            holder.view.setSticker(FileManager.FileType.STICKER, sticker.id);
        }
        return v;
    }

    public void addStickers(StickerPack.Sticker... stickers) {
        this.stickers.addAll(Arrays.asList(stickers));
        Collections.sort(this.stickers, new Comparator<StickerPack.Sticker>() {
            @Override
            public int compare(StickerPack.Sticker o1, StickerPack.Sticker o2) {
                return ((Long) o1.id).compareTo(o2.id);
            }
        });
    }

    public void addStickers(Collection<? extends StickerPack.Sticker> stickers) {
        this.stickers.addAll(stickers);
        Collections.sort(this.stickers, new Comparator<StickerPack.Sticker>() {
            @Override
            public int compare(StickerPack.Sticker o1, StickerPack.Sticker o2) {
                return ((Long) o1.id).compareTo(o2.id);
            }
        });
    }

    public void setOnStickerClickListener(EmojiconsPopup.StickersListener stickerOnClickListener) {
        this.stickerOnClickListener = stickerOnClickListener;
    }

    private static class ViewHolder {
        StickerItemView view;
    }
}
