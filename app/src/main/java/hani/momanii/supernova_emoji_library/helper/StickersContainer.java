package hani.momanii.supernova_emoji_library.helper;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.GridView;

import ru.paymon.android.R;
import ru.paymon.android.StickerPack;

public class StickersContainer extends Container {
    Context context;
    EmojiconsPopup emojiconPopup;
    StickerGridAdapter adapter;

    public StickersContainer(Context context, final EmojiconsPopup emojiconPopup) {
        this.context = context;
        this.emojiconPopup = emojiconPopup;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.sticker_grid, null);
        GridView gridView = (GridView) rootView.findViewById(R.id.sticker_gridview);

        adapter = new StickerGridAdapter(rootView.getContext());
//        adapter.setEmojiClickListener(new EmojiconContainer.OnEmojiconClickedListener() {
//            @Override
//            public void onEmojiconClicked(Emojicon emojicon) {
//                if (emojiconPopup.stickersListener != null) {
//                    emojiconPopup.stickersListener.onStickerSelected(emojicon);
//                }
//            }
//        });
        gridView.setAdapter(adapter);
    }

    public void addStickers(StickerPack sp) {
        adapter.addStickers(sp.stickers.values());
        adapter.notifyDataSetChanged();
    }

    public void setOnStickerClickListener(EmojiconsPopup.StickersListener stickerOnClickListener) {
        adapter.setOnStickerClickListener(stickerOnClickListener);
    }
}
