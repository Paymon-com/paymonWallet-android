package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.models.ChatsGroupItem;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.utils.Utils;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.CommonChatsViewHolder> {
    public LinkedList<ChatsItem> chatsItemsList = new LinkedList<>();
    private IChatsClickListener iChatsClickListener;

    public interface IChatsClickListener {
        void openChat(int chatID, boolean isGroup);
    }

    enum ViewTypes {
        ITEM,
        GROUP_ITEM
    }

    public ChatsAdapter(IChatsClickListener iChatsClickListener) {
        this.iChatsClickListener = iChatsClickListener;
    }

    @Override
    public CommonChatsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        CommonChatsViewHolder vh = null;
        ViewTypes viewTypes = ViewTypes.values()[viewType];
        switch (viewTypes) {
            case ITEM:
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_chats_item, viewGroup, false);
                vh = new ChatsViewHolder(view);
                break;
            case GROUP_ITEM:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_chats_group_item, viewGroup, false);
                vh = new GroupChatsViewHolder(view);
                break;
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull CommonChatsViewHolder holder, int position) {
        final ChatsItem chatsItem = getItem(position);

        final String name = chatsItem.name;
        final String lastMessageText = chatsItem.lastMessageText;
        final int chatID = chatsItem.chatID;
        final String avatarPhotoURL = chatsItem.photoURL.url;

        ViewTypes viewTypes = ViewTypes.values()[this.getItemViewType(position)];
        switch (viewTypes) {
            case ITEM:
                final ChatsViewHolder chatsViewHolder = (ChatsViewHolder) holder;

                if (!avatarPhotoURL.isEmpty())
                    Utils.loadPhoto(avatarPhotoURL, chatsViewHolder.avatar);

                if (name != null && !name.isEmpty())
                    chatsViewHolder.name.setText(name.substring(0, Utils.getAvailableTextLength(chatsViewHolder.name.getPaint(), name)));
                else
                    chatsViewHolder.name.setText("");

                if (lastMessageText != null && !lastMessageText.isEmpty())
                    chatsViewHolder.msg.setText(lastMessageText.substring(0, Utils.getAvailableTextLength(chatsViewHolder.msg.getPaint(), lastMessageText)));
                else
                    chatsViewHolder.msg.setText("");

                chatsViewHolder.time.setText(chatsItem.time != 0 ? Utils.formatDateTime(chatsItem.time, false) : "");

                chatsViewHolder.mainLayout.setOnClickListener((view -> iChatsClickListener.openChat(chatID, false)));

                break;
            case GROUP_ITEM:
                final GroupChatsViewHolder groupChatsViewHolder = (GroupChatsViewHolder) holder;
                final String lastMessagePhotoURL = ((ChatsGroupItem) chatsItem).lastMsgPhotoURL == null ? "" : ((ChatsGroupItem) chatsItem).lastMsgPhotoURL.url;

                if (!avatarPhotoURL.isEmpty())
                    Utils.loadPhoto(avatarPhotoURL, groupChatsViewHolder.avatar);

                if (!lastMessagePhotoURL.isEmpty())
                    Utils.loadPhoto(lastMessagePhotoURL, groupChatsViewHolder.lastMshPhoto);

                if (name != null && !name.isEmpty())
                    groupChatsViewHolder.name.setText(name.substring(0, Utils.getAvailableTextLength(groupChatsViewHolder.name.getPaint(), name)));
                else
                    groupChatsViewHolder.name.setText("");

                if (lastMessageText != null && !lastMessageText.isEmpty())
                    groupChatsViewHolder.msg.setText(lastMessageText.substring(0, Utils.getAvailableTextLength(groupChatsViewHolder.msg.getPaint(), lastMessageText)));
                else
                    groupChatsViewHolder.msg.setText("");

                groupChatsViewHolder.time.setText(chatsItem.time != 0 ? Utils.formatDateTime(chatsItem.time, false) : "");

                groupChatsViewHolder.mainLayout.setOnClickListener((view -> iChatsClickListener.openChat(chatID, true)));
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

    private ChatsItem getItem(int position) {
        return chatsItemsList.get(position);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull CommonChatsViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    public static class CommonChatsViewHolder extends RecyclerView.ViewHolder {
        public final ConstraintLayout mainLayout;

        public CommonChatsViewHolder(View itemView) {
            super(itemView);
            mainLayout = (ConstraintLayout) itemView.findViewById(R.id.main_layout);
        }
    }

    public static class ChatsViewHolder extends CommonChatsViewHolder {
        CircularImageView avatar;
        TextView time;
        TextView msg;
        TextView name;

        ChatsViewHolder(View itemView) {
            super(itemView);
            avatar = (CircularImageView) itemView.findViewById(R.id.chats_item_avatar);
            time = (TextView) itemView.findViewById(R.id.chats_item_time);
            msg = (TextView) itemView.findViewById(R.id.chats_item_msg);
            name = (TextView) itemView.findViewById(R.id.chats_item_name);
        }
    }

    public static class GroupChatsViewHolder extends CommonChatsViewHolder {
        CircularImageView avatar;
        CircularImageView lastMshPhoto;
        TextView time;
        TextView msg;
        TextView name;

        GroupChatsViewHolder(View itemView) {
            super(itemView);
            avatar = (CircularImageView) itemView.findViewById(R.id.chats_item_avatar);
            lastMshPhoto = (CircularImageView) itemView.findViewById(R.id.chats_item_last_msg_avatar);
            time = (TextView) itemView.findViewById(R.id.chats_item_time);
            msg = (TextView) itemView.findViewById(R.id.chats_item_msg);
            name = (TextView) itemView.findViewById(R.id.chats_item_name);
        }
    }
}

