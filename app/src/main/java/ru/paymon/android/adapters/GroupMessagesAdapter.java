package ru.paymon.android.adapters;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.paymon.android.components.CircularImageView;
import com.vanniktech.emoji.EmojiTextView;

import androidx.recyclerview.selection.SelectionTracker;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.selection.MessageItemDetail;
import ru.paymon.android.selection.MessageItemLookup;
import ru.paymon.android.selection.ViewHolderWithDetails;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.Utils;

public class GroupMessagesAdapter extends PagedListAdapter<RPC.Message, GroupMessagesAdapter.BaseMessageViewHolder> {
    private SelectionTracker selectionTracker;
    Context context;

    public GroupMessagesAdapter(@NonNull DiffUtil.ItemCallback<RPC.Message> diffCallback) {
        super(diffCallback);
    }

    enum ViewTypes {
        SENT_MESSAGE,
        RECEIVED_MESSAGE,
        ACTION,
    }

    public SelectionTracker getSelectionTracker() {
        return selectionTracker;
    }

    public void setSelectionTracker(SelectionTracker selectionTracker) {
        this.selectionTracker = selectionTracker;
    }

    @NonNull
    @Override
    public BaseMessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        BaseMessageViewHolder vh = null;

        context = viewGroup.getContext();
        MessagesAdapter.ViewTypes viewTypes = MessagesAdapter.ViewTypes.values()[viewType];
        switch (viewTypes) {
            case SENT_MESSAGE:
                View view = LayoutInflater.from(context).inflate(R.layout.view_holder_sent_message, viewGroup, false);
                vh = new GroupSentMessageViewHolder(view);
                break;
            case RECEIVED_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_group_received_message, viewGroup, false);
                vh = new GroupReceiveMessageViewHolder(view);
                break;
            case ACTION:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_action, viewGroup, false);
                vh = new ActionMessageViewHolder(view);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseMessageViewHolder holder, int position) {
        RPC.Message message = getItem(position);

        if (message == null)
            return;

        holder.bind(message);

        if (holder instanceof GroupReceiveMessageViewHolder) {
            final GroupReceiveMessageViewHolder groupReceiveMessageViewHolder = (GroupReceiveMessageViewHolder) holder;
            RPC.UserObject user = UsersManager.getInstance().getUser(message.from_id);
            if (user != null) {
                if (position != 0) {
                    RPC.Message previousMessage = getItem(position - 1);
                    if (previousMessage == null) return;
                    RPC.UserObject previousUser = UsersManager.getInstance().getUser(previousMessage.from_id);
                    if (previousUser != null && user.id == previousUser.id) {
                        groupReceiveMessageViewHolder.avatar.setVisibility(View.INVISIBLE);
                    } else {
                        groupReceiveMessageViewHolder.avatar.setVisibility(View.VISIBLE);
                        if (!user.photoURL.url.isEmpty()) {
                            Utils.loadPhoto(user.photoURL.url, groupReceiveMessageViewHolder.avatar);
                        }
                    }
                } else {
                    groupReceiveMessageViewHolder.avatar.setVisibility(View.VISIBLE);
                    if (!user.photoURL.url.isEmpty()) {
                        Utils.loadPhoto(user.photoURL.url, groupReceiveMessageViewHolder.avatar);
                    }
                }
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        RPC.Message msg = getItem(position);

        if (msg == null) return 0;

        boolean isSent = msg.from_id == User.currentUser.id;
        boolean isAction = msg.itemType == FileManager.FileType.ACTION;

        if (isAction)
            return ViewTypes.ACTION.ordinal();

        if (isSent)
            return ViewTypes.SENT_MESSAGE.ordinal();
        else
            return ViewTypes.RECEIVED_MESSAGE.ordinal();
    }

    public abstract class BaseMessageViewHolder extends RecyclerView.ViewHolder implements ViewHolderWithDetails {

        private BaseMessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public MessageItemLookup.ItemDetails getItemDetails() {
            return new MessageItemDetail(getAdapterPosition(), getCurrentList().get(getAdapterPosition()));
        }

        abstract void bind(RPC.Message message);
    }

    public class GroupSentMessageViewHolder extends BaseMessageViewHolder {
        public final EmojiTextView msg;
        public final TextView time;

        GroupSentMessageViewHolder(View itemView) {
            super(itemView);
            msg = (EmojiTextView) itemView.findViewById(R.id.message_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }

        @Override
        void bind(RPC.Message message) {
            msg.setText(message.text);
            time.setText(Utils.formatDateTime(message.date, true));
        }
    }

    public class GroupReceiveMessageViewHolder extends BaseMessageViewHolder {
        public final EmojiTextView msg;
        public final TextView time;
        public final CircularImageView avatar;

        GroupReceiveMessageViewHolder(View view) {
            super(view);
            msg = (EmojiTextView) view.findViewById(R.id.message_text_view);
            time = (TextView) view.findViewById(R.id.timestamp_text_view);
            avatar = (CircularImageView) view.findViewById(R.id.photo);
        }

        @Override
        void bind(RPC.Message message) {
            msg.setText(message.text);
            time.setText(Utils.formatDateTime(message.date, true));
        }
    }

    public class ActionMessageViewHolder extends BaseMessageViewHolder {
        public final TextView msg;

        public ActionMessageViewHolder(View view) {
            super(view);
            msg = (TextView) view.findViewById(R.id.message);
        }

        @Override
        void bind(RPC.Message message) {
            if (message.action instanceof RPC.PM_messageActionGroupCreate) {
//                final RPC.PM_messageActionGroupCreate actionGroupCreate = (RPC.PM_messageActionGroupCreate) message.action;
                final int groupID = message.to_peer.group_id;
                final RPC.Group group = GroupsManager.getInstance().getGroup(groupID);
                final RPC.UserObject creator = UsersManager.getInstance().getUser(group.creatorID);
                if(creator == null) return;
                String createGroupString = String.format("%s %s \"%s\"", Utils.formatUserName(creator), context.getString(R.string.created_group), group.title);
                msg.setText(createGroupString);
            }
        }
    }
}
