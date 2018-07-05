package ru.paymon.android.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.models.ChatsGroupItem;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.utils.cache.lruramcache.LruRamCache;

public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public LinkedList<ChatsItem> chatsItemsList;

    enum ViewTypes {
        ITEM,
        GROUP_ITEM
    }

    public ChatsAdapter(LinkedList<ChatsItem> chatsItemsList) {
        this.chatsItemsList = chatsItemsList;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder vh = null;
        ViewTypes viewTypes = ViewTypes.values()[viewType];
        switch (viewTypes) {
            case ITEM:
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chats_item, viewGroup, false);
                vh = new ChatsViewHolder(view);
                break;
            case GROUP_ITEM:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.chats_group_item, viewGroup, false);
                vh = new GroupChatsViewHolder(view);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatsItem chatsItem = getItem(position);

        final RPC.PM_photo photo = chatsItem.photo;
        final String name = chatsItem.name;
        final String lastMessageText = chatsItem.lastMessageText;

        ViewTypes viewTypes = ViewTypes.values()[this.getItemViewType(position)];
        switch (viewTypes) {
            case ITEM:
                final ChatsViewHolder chatsViewHolder = (ChatsViewHolder) holder;

                if (photo != null && photo.id <= 0)
                    chatsViewHolder.avatar.setImageBitmap(LruRamCache.getInstance().getBitmap(R.drawable.profile_photo_none));
                else //TODO:change to origin bmp
                    chatsViewHolder.avatar.setImageBitmap(LruRamCache.getInstance().getBitmap(R.drawable.profile_photo_none));

                if (name != null && !name.isEmpty())
                    chatsViewHolder.name.setText(name.substring(0, Utils.getAvailableTextLength(chatsViewHolder.name.getPaint(), name)));
                else
                    chatsViewHolder.name.setText("");

                if (lastMessageText != null && !lastMessageText.isEmpty())
                    chatsViewHolder.msg.setText(lastMessageText.substring(0, Utils.getAvailableTextLength(chatsViewHolder.msg.getPaint(), lastMessageText)));
                else
                    chatsViewHolder.msg.setText("");

                chatsViewHolder.time.setText(chatsItem.time != 0 ? Utils.formatDateTime(chatsItem.time, true, false) : "");
                break;
            case GROUP_ITEM:
                final GroupChatsViewHolder groupChatsViewHolder = (GroupChatsViewHolder) holder;

                if (photo != null && photo.id <= 0)
                    groupChatsViewHolder.avatar.setImageBitmap(LruRamCache.getInstance().getBitmap(R.drawable.profile_photo_none));
                else //TODO:change to origin bmp
                    groupChatsViewHolder.avatar.setImageBitmap(LruRamCache.getInstance().getBitmap(R.drawable.profile_photo_none));

                if (name != null && !name.isEmpty())
                    groupChatsViewHolder.name.setText(name.substring(0, Utils.getAvailableTextLength(groupChatsViewHolder.name.getPaint(), name)));
                else
                    groupChatsViewHolder.name.setText("");

                if (lastMessageText != null && !lastMessageText.isEmpty())
                    groupChatsViewHolder.msg.setText(lastMessageText.substring(0, Utils.getAvailableTextLength(groupChatsViewHolder.msg.getPaint(), lastMessageText)));
                else
                    groupChatsViewHolder.msg.setText("");

                if (chatsItem.fileType != FileManager.FileType.ACTION) {
                    RPC.PM_photo lastMsgPhoto = ((ChatsGroupItem) chatsItem).lastMsgPhoto;
                    if (lastMsgPhoto != null && lastMsgPhoto.id <= 0)
                        groupChatsViewHolder.lastMshPhoto.setImageBitmap(LruRamCache.getInstance().getBitmap(R.drawable.profile_photo_none));
                    else //TODO:change to origin bmp
                        groupChatsViewHolder.lastMshPhoto.setImageBitmap(LruRamCache.getInstance().getBitmap(R.drawable.profile_photo_none));
                }

                groupChatsViewHolder.time.setText(chatsItem.time != 0 ? Utils.formatDateTime(chatsItem.time, true, false) : "");
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return chatsItemsList.get(position) instanceof ChatsGroupItem ? ViewTypes.GROUP_ITEM.ordinal() : ViewTypes.ITEM.ordinal();
    }

    @Override
    public int getItemCount() {
        return chatsItemsList.size();
    }

    public ChatsItem getItem(int position) {
        return chatsItemsList.get(position);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        TextView time;
        TextView msg;
        TextView name;

        ChatsViewHolder(View itemView) {
            super(itemView);
            avatar = (CircleImageView) itemView.findViewById(R.id.chats_item_avatar);
            time = (TextView) itemView.findViewById(R.id.chats_item_time);
            msg = (TextView) itemView.findViewById(R.id.chats_item_msg);
            name = (TextView) itemView.findViewById(R.id.chats_item_name);
        }
    }

    public static class GroupChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatar;
        CircleImageView lastMshPhoto;
        TextView time;
        TextView msg;
        TextView name;

        GroupChatsViewHolder(View itemView) {
            super(itemView);
            avatar = (CircleImageView) itemView.findViewById(R.id.chats_item_avatar);
            lastMshPhoto = (CircleImageView) itemView.findViewById(R.id.chats_item_last_msg_avatar);
            time = (TextView) itemView.findViewById(R.id.chats_item_time);
            msg = (TextView) itemView.findViewById(R.id.chats_item_msg);
            name = (TextView) itemView.findViewById(R.id.chats_item_name);
        }
    }
}

