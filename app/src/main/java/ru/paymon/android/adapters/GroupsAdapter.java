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
import ru.paymon.android.models.ChatsGroupItem;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.Utils;

public class GroupsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public LinkedList<ChatsGroupItem> groupsItemsList;

    public GroupsAdapter(LinkedList<ChatsGroupItem> groupsItemsList){
        this.groupsItemsList = groupsItemsList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_group_item, parent, false);
        RecyclerView.ViewHolder vh = new GroupViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatsGroupItem chatsGroupItem = getItem(position);
        final RPC.PM_photo photo = chatsGroupItem.photo;
        final String name = chatsGroupItem.name;
        final String lastMessageText = chatsGroupItem.lastMessageText;

        final GroupViewHolder groupViewHolder = (GroupViewHolder) holder;
        groupViewHolder.avatar.setPhoto(photo);
        groupViewHolder.name.setText(name.substring(0, Utils.getAvailableTextLength(groupViewHolder.name.getPaint(), name)));
        groupViewHolder.msg.setText(lastMessageText.substring(0, Utils.getAvailableTextLength(groupViewHolder.msg.getPaint(), lastMessageText)));

        if (chatsGroupItem.fileType != FileManager.FileType.ACTION){
            RPC.PM_photo lastMsgPhoto = ((ChatsGroupItem) chatsGroupItem).lastMsgPhoto;

            groupViewHolder.lastMsgPhoto.setPhoto(lastMsgPhoto);
        }

        groupViewHolder.time.setText(chatsGroupItem.time != 0 ? Utils.formatDateTime(chatsGroupItem.time, false) : "");
    }

    @Override
    public int getItemCount() {
        return groupsItemsList.size();
    }

    public ChatsGroupItem getItem(int position) {
        return groupsItemsList.get(position);
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        CircleImageView lastMsgPhoto;
        TextView time;
        TextView msg;
        TextView name;

        GroupViewHolder(View itemView) {
            super(itemView);
            avatar = (CircleImageView) itemView.findViewById(R.id.chats_item_avatar);
            lastMsgPhoto = (CircleImageView) itemView.findViewById(R.id.chats_item_last_msg_avatar);
            time = (TextView) itemView.findViewById(R.id.chats_item_time);
            msg = (TextView) itemView.findViewById(R.id.chats_item_msg);
            name = (TextView) itemView.findViewById(R.id.chats_item_name);
        }
    }
}
