package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class DialogsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public LinkedList<ChatsItem> dialogsItemsList;

    public DialogsAdapter(LinkedList<ChatsItem> dialogsItemsList) {
        this.dialogsItemsList = dialogsItemsList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        RecyclerView.ViewHolder vh = null;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_item, parent, false);
        RecyclerView.ViewHolder vh = new DialogsViewHolder(view);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatsItem chatsItem = getItem(position);

        final RPC.PM_photo photo = chatsItem.photo;
        final String name = chatsItem.name;
        final String lastMessageText = chatsItem.lastMessageText;

        final DialogsViewHolder dialogsViewHolder = (DialogsViewHolder) holder;
        dialogsViewHolder.avatar.setPhoto(photo);
        dialogsViewHolder.name.setText(name.substring(0, Utils.getAvailableTextLength(dialogsViewHolder.name.getPaint(), name)));
        dialogsViewHolder.msg.setText(lastMessageText.substring(0, Utils.getAvailableTextLength(dialogsViewHolder.msg.getPaint(), lastMessageText)));
        dialogsViewHolder.time.setText(chatsItem.time !=0 ? Utils.formatDateTime(chatsItem.time, false):"");
    }

    @Override
    public int getItemCount() {
        return dialogsItemsList.size();
    }

    public ChatsItem getItem(int position) {
        return dialogsItemsList.get(position);
    }

    public static class DialogsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView time;
        TextView msg;
        TextView name;

        DialogsViewHolder(View itemView) {
            super(itemView);
            avatar = (CircleImageView) itemView.findViewById(R.id.chats_item_avatar);
            time = (TextView) itemView.findViewById(R.id.chats_item_time);
            msg = (TextView) itemView.findViewById(R.id.chats_item_msg);
            name = (TextView) itemView.findViewById(R.id.chats_item_name);
        }
    }
}
