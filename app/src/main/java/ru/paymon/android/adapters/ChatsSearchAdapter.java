package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.models.ChatsSearchItem;
import ru.paymon.android.models.Contact;

public class ChatsSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LinkedList<ChatsSearchItem> chatsSearchItems;

    public ChatsSearchAdapter(LinkedList<ChatsSearchItem> chatsSearchItems){
        this.chatsSearchItems=chatsSearchItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_chats_search_item, parent, false);
        ChatsSearchItemViewHolder holder = new ChatsSearchItemViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatsSearchItem chatsSearchItem = chatsSearchItems.get(position);
        ChatsSearchAdapter.ChatsSearchItemViewHolder chatsSearchItemViewHolder = (ChatsSearchAdapter.ChatsSearchItemViewHolder) holder;
        chatsSearchItemViewHolder.name.setText(chatsSearchItem.name);
//        chatsSearchItemViewHolder.photo.setPhoto(chatsSearchItem.photo);
    }

    @Override
    public int getItemCount() {
        return chatsSearchItems.size();
    }

    private class ChatsSearchItemViewHolder extends RecyclerView.ViewHolder{
        public CircularImageView photo;
        public TextView name;

        public ChatsSearchItemViewHolder(View itemView) {
            super(itemView);
            photo = (CircularImageView) itemView.findViewById(R.id.chats_search_photo);
            name = (TextView) itemView.findViewById(R.id.chats_search_name);
        }
    }
}
