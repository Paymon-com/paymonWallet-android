package ru.paymon.android.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

//import com.daimajia.swipe.SwipeLayout;
//import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;


import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.util.LinkedList;

import ru.paymon.android.GroupsManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.models.ChatsGroupItem;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.FragmentChat;

import static ru.paymon.android.adapters.MessagesAdapter.FORWARD_MESSAGES_KEY;
import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

public class ChatsAdapter extends RecyclerSwipeAdapter<ChatsAdapter.CommonChatsViewHolder> {
    public LinkedList<ChatsItem> chatsItemsList = new LinkedList<>();
    private AppCompatActivity activity;
    private LinkedList<Long> forwardMessages;

    enum ViewTypes {
        ITEM,
        GROUP_ITEM
    }

    public ChatsAdapter(/*LinkedList<ChatsItem> chatsItemsList,*/ Activity activity, LinkedList<Long> forwardedMessages) {
//        this.chatsItemsList = chatsItemsList;
        this.activity = (AppCompatActivity) activity;
        this.forwardMessages = forwardedMessages;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public CommonChatsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        CommonChatsViewHolder vh = null;
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
    public void onBindViewHolder(@NonNull CommonChatsViewHolder holder, int position) {
        final ChatsItem chatsItem = getItem(position);

        final RPC.PM_photo photo = chatsItem.photo;
        final String name = chatsItem.name;
        final String lastMessageText = chatsItem.lastMessageText;
        final int chatID = chatsItem.chatID;
        boolean isGroup = false;

        if (User.CLIENT_DO_NOT_DISTURB_CHATS_LIST.contains(chatID))
            holder.doNotDisturb.setImageResource(R.drawable.ic_notifications_none_white_24dp);
        else
            holder.doNotDisturb.setImageResource(R.drawable.ic_notifications_off_black_24dp);

        holder.doNotDisturb.setOnClickListener((view -> {
            if (!User.CLIENT_DO_NOT_DISTURB_CHATS_LIST.contains(chatID))
                User.CLIENT_DO_NOT_DISTURB_CHATS_LIST.add(chatID);
            else
                User.CLIENT_DO_NOT_DISTURB_CHATS_LIST.remove((Integer) chatID);

            onBindViewHolder(holder, position);
            User.saveConfig();
        }));

        holder.remove.setOnClickListener((view) -> {
            chatsItemsList.remove(position);
            mItemManger.removeShownLayouts(holder.swipeLayout);
            chatsItemsList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, chatsItemsList.size());
            mItemManger.closeAllItems();
            notifyDataSetChanged();
        });

        ViewTypes viewTypes = ViewTypes.values()[this.getItemViewType(position)];
        switch (viewTypes) {
            case ITEM:
                final ChatsViewHolder chatsViewHolder = (ChatsViewHolder) holder;

                chatsViewHolder.avatar.setPhoto(photo);

                if (name != null && !name.isEmpty())
                    chatsViewHolder.name.setText(name.substring(0, Utils.getAvailableTextLength(chatsViewHolder.name.getPaint(), name)));
                else
                    chatsViewHolder.name.setText("");

                if (lastMessageText != null && !lastMessageText.isEmpty())
                    chatsViewHolder.msg.setText(lastMessageText.substring(0, Utils.getAvailableTextLength(chatsViewHolder.msg.getPaint(), lastMessageText)));
                else
                    chatsViewHolder.msg.setText("");

                chatsViewHolder.time.setText(chatsItem.time != 0 ? Utils.formatDateTime(chatsItem.time, false) : "");
                break;
            case GROUP_ITEM:
                final GroupChatsViewHolder groupChatsViewHolder = (GroupChatsViewHolder) holder;

                groupChatsViewHolder.avatar.setPhoto(photo);

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

                    groupChatsViewHolder.lastMshPhoto.setPhoto(lastMsgPhoto);
                }

                groupChatsViewHolder.time.setText(chatsItem.time != 0 ? Utils.formatDateTime(chatsItem.time, false) : "");

                isGroup= true;
                break;
        }

        final Bundle bundle = new Bundle();

        if (isGroup)
            bundle.putParcelableArrayList("users", GroupsManager.getInstance().groupsUsers.get(chatID));

        if (forwardMessages != null && forwardMessages.size() > 0)
            bundle.putSerializable(FORWARD_MESSAGES_KEY, forwardMessages);

        holder.mainLayout.setOnClickListener((view -> {
            bundle.putInt(CHAT_ID_KEY, chatID);
            final FragmentChat fragmentChat = FragmentChat.newInstance();
            fragmentChat.setArguments(bundle);
            Utils.replaceFragmentWithAnimationSlideFade(activity.getSupportFragmentManager(), fragmentChat, null);
        }));

        mItemManger.bindView(holder.itemView, position);
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

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe_layout;
    }

    public static class CommonChatsViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout mainLayout;
        SwipeLayout swipeLayout;
        ImageView remove;
        ImageView doNotDisturb;

        public CommonChatsViewHolder(View itemView) {
            super(itemView);
            mainLayout = (ConstraintLayout) itemView.findViewById(R.id.main_layout);
            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe_layout);
            View swipeView = (View) itemView.findViewById(R.id.swipe_view);
            remove = (ImageView) swipeView.findViewById(R.id.delete_item);
            doNotDisturb = (ImageView) swipeView.findViewById(R.id.disable_notifications);

            View.OnLongClickListener onLongClickListener = ((view) -> {
                swipeLayout.toggle();
                return true;
            });

            swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
            swipeView.setOnLongClickListener(onLongClickListener);
            mainLayout.setOnLongClickListener(onLongClickListener);
        }
    }

    public static class ChatsViewHolder extends CommonChatsViewHolder {
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

    public static class GroupChatsViewHolder extends CommonChatsViewHolder {
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

