package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.models.MessagesSearchItem;

public class MessagesSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LinkedList<MessagesSearchItem> messagesSearchItems;

    public MessagesSearchAdapter(LinkedList<MessagesSearchItem> messagesSearchItems){
        this.messagesSearchItems = messagesSearchItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_messages_search_item, parent, false);
        MessagesSearchItemViewHolder holder = new MessagesSearchItemViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessagesSearchItem messagesSearchItem = messagesSearchItems.get(position);
        MessagesSearchAdapter.MessagesSearchItemViewHolder messagesSearchItemViewHolder = (MessagesSearchAdapter.MessagesSearchItemViewHolder) holder;
        messagesSearchItemViewHolder.photo.setPhoto(messagesSearchItem.photo);
        messagesSearchItemViewHolder.name.setText(messagesSearchItem.name);
        messagesSearchItemViewHolder.message.setText(messagesSearchItem.message);

    }

    @Override
    public int getItemCount() {
        return messagesSearchItems.size();
    }

    private class MessagesSearchItemViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView photo;
        public TextView name;
        public TextView message;

        public MessagesSearchItemViewHolder(View itemView) {
            super(itemView);
            photo = (CircleImageView) itemView.findViewById(R.id.message_search_photo);
            name = (TextView) itemView.findViewById(R.id.message_search_name);
            message = (TextView) itemView.findViewById(R.id.message_search);
        }
    }
}
