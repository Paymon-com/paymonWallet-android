package ru.paymon.android.test2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.selection.ItemDetailsLookup;
import ru.paymon.android.adapters.MessagesAdapter;

public class MessageItemLookup extends ItemDetailsLookup {

    private final RecyclerView recyclerView;

    public MessageItemLookup(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Nullable
    @Override
    public ItemDetails getItemDetails(@NonNull MotionEvent e) {
        View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
        if (view != null) {
            RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
            if (viewHolder instanceof MessagesAdapter.BaseMessageViewHolder) {
                return ((MessagesAdapter.BaseMessageViewHolder) viewHolder).getItemDetails();
            }
        }

        return null;
    }
}
