package ru.paymon.android.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.vanniktech.emoji.EmojiTextView;

import java.util.LinkedList;

import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class GroupMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public LinkedList<Long> messageIDs = new LinkedList<>();
    private MessagesAdapter.IMessageClickListener iMessageClickListener;

    public interface IMessageClickListener {
        void longClick();

        void delete(LinkedList<Long> checkedMessageIDs);

        void forward(LinkedList<Long> checkedMessageIDs);
    }

    enum ViewTypes {
        SENT_MESSAGE,
        RECEIVED_MESSAGE,
    }

    public GroupMessagesAdapter(MessagesAdapter.IMessageClickListener iMessageClickListener) {
        this.iMessageClickListener = iMessageClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder vh = null;

        Context context = viewGroup.getContext();
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
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        RPC.Message message = getMessage(position);

        if (holder instanceof GroupSentMessageViewHolder) {
            final GroupSentMessageViewHolder sentMessageViewHolder = (GroupSentMessageViewHolder) holder;
            sentMessageViewHolder.msg.setText(message.text);
            sentMessageViewHolder.time.setText(Utils.formatDateTime(message.date, true));
        } else if (holder instanceof GroupReceiveMessageViewHolder) {
            final GroupReceiveMessageViewHolder groupReceiveMessageViewHolder = (GroupReceiveMessageViewHolder) holder;
            groupReceiveMessageViewHolder.msg.setText(message.text);
            groupReceiveMessageViewHolder.time.setText(Utils.formatDateTime(message.date, true));
            RPC.UserObject user = UsersManager.getInstance().users.get(message.from_id);
            if (user != null && position != 0 && position < messageIDs.size()) {
                RPC.Message previousMessage = getMessage(position - 1);
                RPC.UserObject previousUser = UsersManager.getInstance().users.get(previousMessage.from_id);
                if (previousUser != null && user.id == previousUser.id) {
                    groupReceiveMessageViewHolder.avatar.setVisibility(View.INVISIBLE);
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
    public int getItemCount() {
        return messageIDs.size();
    }

    @Override
    public long getItemId(int position) {
        return messageIDs.get(position);
    }

    private RPC.Message getMessage(int position) {
        return MessagesManager.getInstance().messages.get(getItemId(position));
    }

    @Override
    public int getItemViewType(int position) {
        RPC.Message msg = getMessage(position);

        boolean isSent = msg.from_id == User.currentUser.id;

        if (isSent)
            return ViewTypes.SENT_MESSAGE.ordinal();
        else
            return ViewTypes.RECEIVED_MESSAGE.ordinal();
    }

//    public void deselectAll() {
//        checkedMessageIDs.clear();
//        notifyDataSetChanged();
//    }

//    private void init() {
//        final TextView textViewSelectedTitle = (TextView) selectedCustomView.findViewById(R.id.selected_title);
//        final ImageView imageViewSelectedDelete = (ImageView) selectedCustomView.findViewById(R.id.selected_delete);
//        final ImageView imageViewSelectedCopy = (ImageView) selectedCustomView.findViewById(R.id.selected_copy);
//        final ImageView imageViewSelectedForward = (ImageView) selectedCustomView.findViewById(R.id.selected_forward);
//        final ImageView backToolbar = (ImageView) selectedCustomView.findViewById(R.id.selected_back);
//
//        backToolbar.setOnClickListener(view -> deselectAll());
//
//        imageViewSelectedForward.setOnClickListener((view -> {
//            iMessageClickListener.forward(checkedMessageIDs);
//        }));
//
//        imageViewSelectedDelete.setOnClickListener((view) -> {
//            iMessageClickListener.delete(checkedMessageIDs);
//        });
//
//        imageViewSelectedCopy.setOnClickListener((view) -> {
//            StringBuilder copiedMessages = new StringBuilder();
//            for (int i = 0; i < checkedMessageIDs.size(); i++) {
//                String text = i < checkedMessageIDs.size() - 1 ? MessagesManager.getInstance().messages.get(checkedMessageIDs.get(i)).text : MessagesManager.getInstance().messages.get(checkedMessageIDs.get(i)).text + "\n";
//                copiedMessages.append(text);
//            }
//
//            ClipboardManager clipboard = (ClipboardManager) ApplicationLoader.applicationContext.getSystemService(CLIPBOARD_SERVICE);
//            ClipData clip = ClipData.newPlainText("Copied messages", copiedMessages.toString());
//            clipboard.setPrimaryClip(clip);
//            deselectAll();
//        });
//    }


    public static class GroupSentMessageViewHolder extends RecyclerView.ViewHolder {
        public final EmojiTextView msg;
        public final TextView time;

        GroupSentMessageViewHolder(View itemView) {
            super(itemView);
            msg = (EmojiTextView) itemView.findViewById(R.id.message_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class GroupReceiveMessageViewHolder extends RecyclerView.ViewHolder {
        public final EmojiTextView msg;
        public final TextView time;
        public final CircularImageView avatar;

        GroupReceiveMessageViewHolder(View view) {
            super(view);
            msg = (EmojiTextView) view.findViewById(R.id.message_text_view);
            time = (TextView) view.findViewById(R.id.timestamp_text_view);
            avatar = (CircularImageView) view.findViewById(R.id.photo);
        }
    }
}
