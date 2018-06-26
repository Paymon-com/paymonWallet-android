package ru.paymon.android.adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;


import java.util.LinkedList;


import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.net.RPC;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MessagesAdapter extends MultiChoiceAdapter<RecyclerView.ViewHolder> {
    public LinkedList<Long> messageIDs;
    private LinkedList<Long> checkedMessageIDs;
    private boolean isGroup;
    private AppCompatActivity activity;
    private View defaultCustomView;

    enum ViewTypes {
        SENT_MESSAGE,
        RECEIVED_MESSAGE,
        SENT_MESSAGE_ITEM,
        RECEIVED_MESSAGE_ITEM,
        GROUP_SENT_MESSAGE,
        GROUP_RECEIVED_MESSAGE,
        ITEM_ACTION_MESSAGE
    }

    public MessagesAdapter(AppCompatActivity activity, boolean isGroup, int chatID, View defaultCustomView) {
        this.isGroup = isGroup;
        this.activity = activity;
        this.defaultCustomView = defaultCustomView;
        messageIDs = new LinkedList<>();
        checkedMessageIDs = new LinkedList<>();

        init();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder vh = null;

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
            case SENT_MESSAGE_ITEM:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_sent_message_item, viewGroup, false);
                vh = new SentMessageItemViewHolder(view);
                break;
            case RECEIVED_MESSAGE_ITEM:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_received_message_item, viewGroup, false);
                vh = new ReceiveMessageItemViewHolder(view);
                break;
            case GROUP_RECEIVED_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_group_received_message, viewGroup, false);
                vh = new GroupReceiveMessageViewHolder(view);
                break;
            case ITEM_ACTION_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_action_item, viewGroup, false);
                vh = new ActionMessageViewHolder(view);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
//
//        ((BaseViewHolder) holder).message = getMessage(position);
//
//        if (holder instanceof SentMessageViewHolder) {
//            final SentMessageViewHolder sentMessageViewHolder = (SentMessageViewHolder) holder;
//            sentMessageViewHolder.msg.setText(sentMessageViewHolder.message.text);
//            sentMessageViewHolder.time.setText(Utils.formatDateTime(sentMessageViewHolder.message.date, true, activity.getApplicationContext(), true));
//        } else if (holder instanceof ReceiveMessageViewHolder) {
//            final ReceiveMessageViewHolder receiveMessageViewHolder = (ReceiveMessageViewHolder) holder;
//            receiveMessageViewHolder.msg.setText(receiveMessageViewHolder.message.text);
//            receiveMessageViewHolder.time.setText(Utils.formatDateTime(receiveMessageViewHolder.message.date, true, activity.getApplicationContext(), true));
//        } else if (holder instanceof SentMessageItemViewHolder) {
//            final SentMessageItemViewHolder sentMessageItemViewHolder = (SentMessageItemViewHolder) holder;
//            sentMessageItemViewHolder.item.setSticker(sentMessageItemViewHolder.message.itemType, sentMessageItemViewHolder.message.itemID);
//            sentMessageItemViewHolder.time.setText(Utils.formatDateTime(sentMessageItemViewHolder.message.date, true, activity.getApplicationContext(), true));
//        } else if (holder instanceof ReceiveMessageItemViewHolder) {
//            final ReceiveMessageItemViewHolder receiveMessageItemViewHolder = (ReceiveMessageItemViewHolder) holder;
//            receiveMessageItemViewHolder.item.setSticker(receiveMessageItemViewHolder.message.itemType, receiveMessageItemViewHolder.message.itemID);
//            receiveMessageItemViewHolder.time.setText(Utils.formatDateTime(receiveMessageItemViewHolder.message.date, true, activity.getApplicationContext(), true));
//        } else if (holder instanceof GroupReceiveMessageViewHolder) {
//            final GroupReceiveMessageViewHolder groupReceiveMessageViewHolder = (GroupReceiveMessageViewHolder) holder;
//            groupReceiveMessageViewHolder.msg.setText(groupReceiveMessageViewHolder.message.text);
//            groupReceiveMessageViewHolder.time.setText(Utils.formatDateTime(groupReceiveMessageViewHolder.message.date, true, activity.getApplicationContext(), true));
//            if (MediaManager.getInstance().userProfilePhotoIDs.get(groupReceiveMessageViewHolder.message.from_id) != null)
//                groupReceiveMessageViewHolder.avatar.setPhoto(groupReceiveMessageViewHolder.message.from_id, MediaManager.getInstance().userProfilePhotoIDs.get(groupReceiveMessageViewHolder.message.from_id));
//        } else if (holder instanceof ActionMessageViewHolder) {
//            final ActionMessageViewHolder actionMessageViewHolder = (ActionMessageViewHolder) holder;
//            actionMessageViewHolder.msg.setText(actionMessageViewHolder.message.text);
//            actionMessageViewHolder.time.setText(Utils.formatDateTime(actionMessageViewHolder.message.date, true, activity.getApplicationContext(), true));
//        }
    }

    @Override
    public void setActive(@NonNull View view, boolean state) {
        if (state) {
            view.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.gray));
        } else {
            view.setBackgroundColor(ContextCompat.getColor(activity.getApplicationContext(), android.R.color.transparent));
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

    public RPC.Message getMessage(int position) {
        return MessagesManager.getInstance().messages.get(getItemId(position));
    }

    @Override
    public int getItemViewType(int position) {
//        RPC.Message msg = getMessage(position);
//
//        boolean isSent = msg.from_id == User.currentUser.id;
//        int viewType = ViewTypes.SENT_MESSAGE.ordinal();
//
//        if (isSent) {
//            if (msg instanceof RPC.PM_message) {
//                if (isGroup)
//                    viewType = ViewTypes.SENT_MESSAGE.ordinal();
//                else
//                    viewType = ViewTypes.GROUP_SENT_MESSAGE.ordinal();
//            } else if (msg instanceof RPC.PM_messageItem) {
//                if (((RPC.PM_messageItem) msg).itemType == FileManager.FileType.ACTION)
//                    viewType = ViewTypes.ITEM_ACTION_MESSAGE.ordinal();
//                else
//                    viewType = ViewTypes.SENT_MESSAGE_ITEM.ordinal();
//            }
//        } else {
//            if (msg instanceof RPC.PM_message) {
//                if (isGroup)
//                    viewType = ViewTypes.RECEIVED_MESSAGE.ordinal();
//                else
//                    viewType = ViewTypes.GROUP_RECEIVED_MESSAGE.ordinal();
//            } else if (msg instanceof RPC.PM_messageItem) {
//                if (((RPC.PM_messageItem) msg).itemType == FileManager.FileType.ACTION)
//                    viewType = ViewTypes.ITEM_ACTION_MESSAGE.ordinal();
//                else
//                    viewType = ViewTypes.RECEIVED_MESSAGE_ITEM.ordinal();
//            }
//        }
//
//        return viewType;
        return 1;
    }

    private void init() {
        final View selectedCustomView = activity.getLayoutInflater().inflate(R.layout.selected_messages_action_bar, null);
        final TextView textViewSelectedTitle = (TextView) selectedCustomView.findViewById(R.id.selected_title);
        final ImageView imageViewSelectedDelete = (ImageView) selectedCustomView.findViewById(R.id.selected_delete);
        final ImageView imageViewSelectedCopy = (ImageView) selectedCustomView.findViewById(R.id.selected_copy);
        final ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);

        imageViewSelectedDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (long msgid : checkedMessageIDs) {
//                    if (MessageManager.getInstance().messages.get(msgid).from_id != User.currentUser.id) {
//                        Toast.makeText(activity.getApplicationContext(), "Вы не можете удалять чужие сообщения!", Toast.LENGTH_SHORT).show();
//                        deselectAll();
//                        return;
//                    }
                }

//                DialogFragmentOkCancel.show(activity.getSupportFragmentManager(), activity.getString(R.string.want_delete_message), new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (!isGroup) {
//                            RPC.PM_deleteDialogMessages deleteMessagesRequest = new RPC.PM_deleteDialogMessages();
//                            deleteMessagesRequest.messageIDs.addAll(checkedMessageIDs);

//                            NetworkManager.getInstance().sendRequest(deleteMessagesRequest, new Packet.OnResponseListener() {
//                                @Override
//                                public void onResponse(Packet response, RPC.PM_error error) {
//                                    if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
////                                        Toast.makeText(ApplicationLoader.applicationContext, "Не удалось удалить сообщения!", Toast.LENGTH_SHORT).show();
//                                        return;
//                                    }
//
//                                    if (response instanceof RPC.PM_boolTrue) {
//                                        ApplicationLoader.applicationHandler.post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                for (Long msgid : deleteMessagesRequest.messageIDs) {
//                                                    RPC.Message msg = MessageManager.getInstance().messages.get(msgid);
//                                                    MessageManager.getInstance().deleteMessage(msg, chatID);
//                                                    messageIDs.remove(msgid);
//                                                }
//                                                notifyDataSetChanged();
//                                            }
//                                        });
//                                    }
//                                }
//                            });
//                        } else {
//                            RPC.PM_deleteGroupMessages deleteMessagesRequest = new RPC.PM_deleteGroupMessages();
//                            deleteMessagesRequest.messageIDs.addAll(checkedMessageIDs);

//                            NetworkManager.getInstance().sendRequest(deleteMessagesRequest, new Packet.OnResponseListener() {
//                                @Override
//                                public void onResponse(Packet response, RPC.PM_error error) {
//                                    if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
//                                        Toast.makeText(ApplicationLoader.applicationContext, R.string.could_not_delete_message, Toast.LENGTH_SHORT).show();
//                                        return;
//                                    }
//
//                                    if (response instanceof RPC.PM_boolTrue) {
//                                        ApplicationLoader.applicationHandler.post(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                for (Long msgid : deleteMessagesRequest.messageIDs) {
//                                                    RPC.Message msg = MessageManager.getInstance().messages.get(msgid);
//                                                    MessageManager.getInstance().deleteMessage(msg, chatID);
//                                                    messageIDs.remove(msgid);
//                                                }
//                                                notifyDataSetChanged();
//                                            }
//                                        });
//                                    }
//                                }
//                            });
//                        }

//                        NotificationManager.getInstance().postNotificationName(NotificationManager.cancelDialog);
//                        deselectAll();
//                    }
//                }, null);


            }
        });

        imageViewSelectedCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String copiedMessages = "";
                for (int i = 0; i < checkedMessageIDs.size(); i++) {
//                    copiedMessages += MessageManager.getInstance().messages.get(checkedMessageIDs.get(i)).text;
                    if (i < checkedMessageIDs.size() - 1)
                        copiedMessages += "\n";
                }

                ClipboardManager clipboard = (ClipboardManager) activity.getApplicationContext().getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Copied messages", copiedMessages);
                clipboard.setPrimaryClip(clip);
                deselectAll();
            }
        });

        setMultiChoiceSelectionListener(new MultiChoiceAdapter.Listener() {
            @Override
            public void OnItemSelected(int selectedPosition, int itemSelectedCount, int allItemCount) {
                if (itemSelectedCount >= 1) {
                    textViewSelectedTitle.setText(String.format("%s: %s", activity.getApplicationContext().getString(R.string.selected_messages_count), itemSelectedCount));
                    activity.getSupportActionBar().setCustomView(selectedCustomView, params);
                }
                checkedMessageIDs.add(getItemId(selectedPosition));
            }

            @Override
            public void OnItemDeselected(int deselectedPosition, int itemSelectedCount, int allItemCount) {
                checkedMessageIDs.remove(getItemId(deselectedPosition));
                if (itemSelectedCount < 1) {
                    activity.getSupportActionBar().setCustomView(defaultCustomView);
                } else if (itemSelectedCount >= 1) {
                    textViewSelectedTitle.setText(String.format("%s: %s", activity.getApplicationContext().getString(R.string.selected_messages_count), itemSelectedCount));
                    activity.getSupportActionBar().setCustomView(selectedCustomView, params);
                }
            }

            @Override
            public void OnSelectAll(int itemSelectedCount, int allItemCount) {

            }

            @Override
            public void OnDeselectAll(int itemSelectedCount, int allItemCount) {
                checkedMessageIDs.clear();
                if (itemSelectedCount < 1)
                    activity.getSupportActionBar().setCustomView(defaultCustomView);
            }
        });
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        public RPC.Message message;
//        public MultiChoiceHelper helper;

        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class SentMessageViewHolder extends BaseViewHolder {
//        EmojiconTextView msg;
        TextView time;

        SentMessageViewHolder(View itemView) {
            super(itemView);
//            msg = (EmojiconTextView) itemView.findViewById(R.id.message_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class ReceiveMessageViewHolder extends BaseViewHolder {
//        EmojiconTextView msg;
        TextView time;

        ReceiveMessageViewHolder(View itemView) {
            super(itemView);
//            msg = (EmojiconTextView) itemView.findViewById(R.id.message_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class SentMessageItemViewHolder extends BaseViewHolder {
//        ItemView item;
        TextView time;

        SentMessageItemViewHolder(View itemView) {
            super(itemView);
//            item = (ItemView) itemView.findViewById(R.id.message_item_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class ReceiveMessageItemViewHolder extends BaseViewHolder {
//        ItemView item;
        TextView time;

        ReceiveMessageItemViewHolder(View itemView) {
            super(itemView);
//            item = (ItemView) itemView.findViewById(R.id.message_item_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class GroupReceiveMessageViewHolder extends BaseViewHolder {
//        EmojiconTextView msg;
        TextView time;
        CircleImageView avatar;

        GroupReceiveMessageViewHolder(View itemView) {
            super(itemView);
//            msg = (EmojiconTextView) itemView.findViewById(R.id.message_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_text_view);
            avatar = (CircleImageView) itemView.findViewById(R.id.photo);
        }
    }

    public static class ActionMessageViewHolder extends BaseViewHolder {
        TextView msg;
        TextView time;

        ActionMessageViewHolder(View itemView) {
            super(itemView);
            msg = (TextView) itemView.findViewById(R.id.message_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }
    }
}
