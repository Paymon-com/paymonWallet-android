package ru.paymon.android.adapters;

import android.arch.paging.PagedListAdapter;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.ChatsManager;
import ru.paymon.android.R;
import ru.paymon.android.components.CircularImageView;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class ChatsAdapter extends PagedListAdapter<ChatsItem, ChatsAdapter.BaseChatsViewHolder> {

    public ChatsAdapter(@NonNull DiffUtil.ItemCallback<ChatsItem> diffCallback) {
        super(diffCallback);
    }

    enum ViewTypes {
        ITEM,
        GROUP_ITEM
    }

    @Override
    public BaseChatsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        BaseChatsViewHolder vh = null;
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
    public void onBindViewHolder(@NonNull BaseChatsViewHolder holder, int position) {
        final ChatsItem chatsItem = getItem(position);

        if (chatsItem == null)
            return;

        holder.bind(chatsItem);
    }

    @Override
    public int getItemViewType(int position) {
        return getCurrentList().get(position).isGroup ? ViewTypes.GROUP_ITEM.ordinal() : ViewTypes.ITEM.ordinal();
    }

    public abstract static class BaseChatsViewHolder extends RecyclerView.ViewHolder {
        public final Button delete;

        private BaseChatsViewHolder(View itemView) {
            super(itemView);
            delete = (Button) itemView.findViewById(R.id.delete);
        }

        abstract void bind(ChatsItem chatsItem);
    }

    public class ChatsViewHolder extends BaseChatsViewHolder {
        private final CircularImageView avatar;
        private final TextView time;
        private final TextView msg;
        private final TextView name;
        private final SwipeLayout swipe;

        ChatsViewHolder(View itemView) {
            super(itemView);
            avatar = (CircularImageView) itemView.findViewById(R.id.chats_item_avatar);
            time = (TextView) itemView.findViewById(R.id.chats_item_time);
            msg = (TextView) itemView.findViewById(R.id.chats_item_msg);
            name = (TextView) itemView.findViewById(R.id.chats_item_name);
            swipe = (SwipeLayout) itemView.findViewById(R.id.swipe);
        }

        @Override
        void bind(ChatsItem chatsItem) {
            final String nameString = chatsItem.name != null ? chatsItem.name : "";
            final String lastMessageText = chatsItem.lastMessageText != null ? chatsItem.lastMessageText : "";
            final String avatarPhotoURL = chatsItem.photoURL.url;

            if (!avatarPhotoURL.isEmpty())
                Utils.loadPhoto(avatarPhotoURL, avatar);

            name.setText(nameString);
            msg.setText(lastMessageText);
            time.setText(chatsItem.time != 0 ? Utils.formatDateTime(chatsItem.time, false) : "");

            delete.setOnClickListener(v -> {
                Utils.netQueue.postRunnable(() -> {
                    final RPC.PM_deleteChat groupInfo = new RPC.PM_deleteChat(new RPC.PM_peerUser(chatsItem.chatID));
                    NetworkManager.getInstance().sendRequest(groupInfo, (response, error) -> {
                        if (response == null || error != null || response instanceof RPC.PM_boolFalse) {
                            ApplicationLoader.applicationHandler.post(() -> {
                                swipe.close(true);
                                Toast.makeText(ApplicationLoader.applicationContext, ApplicationLoader.applicationContext.getString(R.string.other_fail), Toast.LENGTH_LONG).show();
                            });
                            return;
                        }

                        if (response instanceof RPC.PM_boolTrue) {
                            ChatsManager.getInstance().removeChat(chatsItem);
                        }
                    });
                });
            });
        }
    }

    public class GroupChatsViewHolder extends BaseChatsViewHolder {
        private final CircularImageView avatar;
        private final CircularImageView lastMshPhoto;
        private final TextView time;
        private final TextView msg;
        private final TextView name;
        private final SwipeLayout swipe;

        GroupChatsViewHolder(View itemView) {
            super(itemView);
            avatar = (CircularImageView) itemView.findViewById(R.id.chats_item_avatar);
            lastMshPhoto = (CircularImageView) itemView.findViewById(R.id.chats_item_last_msg_avatar);
            time = (TextView) itemView.findViewById(R.id.chats_item_time);
            msg = (TextView) itemView.findViewById(R.id.chats_item_msg);
            name = (TextView) itemView.findViewById(R.id.chats_item_name);
            swipe = (SwipeLayout) itemView.findViewById(R.id.swipe);
        }

        @Override
        void bind(ChatsItem chatsItem) {
            final String nameString = chatsItem.name != null ? chatsItem.name : "";
            final String lastMessageText = chatsItem.lastMessageText != null ? chatsItem.lastMessageText : "";
            final String avatarPhotoURL = chatsItem.photoURL.url;
            final String lastMessagePhotoURL = chatsItem.lastMsgPhotoURL == null ? "" : chatsItem.lastMsgPhotoURL.url;

            if (!avatarPhotoURL.isEmpty())
                Utils.loadPhoto(avatarPhotoURL, avatar);

            if (!lastMessagePhotoURL.isEmpty())
                Utils.loadPhoto(lastMessagePhotoURL, lastMshPhoto);

            name.setText(nameString);
            msg.setText(lastMessageText);
            time.setText(chatsItem.time != 0 ? Utils.formatDateTime(chatsItem.time, false) : "");

            delete.setOnClickListener(v -> {
                Utils.netQueue.postRunnable(() -> {
                    final RPC.PM_deleteChat deleteChat = new RPC.PM_deleteChat(new RPC.PM_peerGroup(chatsItem.chatID));
                    NetworkManager.getInstance().sendRequest(deleteChat, (response, error) -> {
                        if (response == null || error != null || response instanceof RPC.PM_boolFalse) {
                            ApplicationLoader.applicationHandler.post(() -> {
                                swipe.close(true);
                            });
                            return; //TODO:не удалось и закрытие свайпа
                        }

                        if (response instanceof RPC.PM_boolTrue) {
                            ChatsManager.getInstance().removeChat(chatsItem);
                        }
                    });
                });
            });
        }
    }
}

