package ru.paymon.android.adapters;

import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.selection.SelectionTracker;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.net.RPC;
import ru.paymon.android.selection.MessageItemDetail;
import ru.paymon.android.selection.MessageItemLookup;
import ru.paymon.android.selection.ViewHolderWithDetails;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.Utils;


public class MessagesAdapter extends PagedListAdapter<RPC.Message, MessagesAdapter.BaseMessageViewHolder> {
    private SelectionTracker selectionTracker;
    public List<RPC.Message> items = new ArrayList<>();

    public MessagesAdapter(@NonNull DiffUtil.ItemCallback<RPC.Message> diffCallback) {
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

        Context context = viewGroup.getContext();
        ViewTypes viewTypes = ViewTypes.values()[viewType];
        switch (viewTypes) {
            case SENT_MESSAGE:
                View view = LayoutInflater.from(context).inflate(R.layout.view_holder_sent_message, viewGroup, false);
                vh = new SentMessageViewHolder(view);
                break;
            case RECEIVED_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_received_message, viewGroup, false);
                vh = new ReceiveMessageViewHolder(view);
                break;
            case ACTION:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_action, viewGroup, false);
                vh = new ActionMessageViewHolder(view);
                break;
        }
        return vh;
    }

    @Override
    public void submitList(PagedList<RPC.Message> pagedList) {
        super.submitList(pagedList);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseMessageViewHolder holder, int position) {
        RPC.Message message = getItem(position);

        if (message == null)
            return;

        holder.bind(message);

        if (selectionTracker != null)
            holder.setActivatedState(selectionTracker.isSelected(message));
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
        public MessageItemLookup.ItemDetails<RPC.Message> getItemDetails() {
            return new MessageItemDetail(getAdapterPosition(), getCurrentList().get(getAdapterPosition()));
        }

        public void setActivatedState(boolean isActivated) {
            itemView.setActivated(isActivated);
        }

        abstract void bind(RPC.Message message);
    }

    public class SentMessageViewHolder extends BaseMessageViewHolder {
        public final EmojiTextView msg;
        public final TextView time;

        SentMessageViewHolder(View itemView) {
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

    public class ReceiveMessageViewHolder extends BaseMessageViewHolder {
        public final EmojiTextView msg;
        public final TextView time;

        ReceiveMessageViewHolder(View view) {
            super(view);
            msg = (EmojiTextView) view.findViewById(R.id.message_text_view);
            time = (TextView) view.findViewById(R.id.timestamp_text_view);
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
            msg = (TextView) view.findViewById(R.id.message_text_view);
        }

        @Override
        void bind(RPC.Message message) {

        }
    }
}
