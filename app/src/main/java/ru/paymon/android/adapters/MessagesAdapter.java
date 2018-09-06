package ru.paymon.android.adapters;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

//import com.davidecirillo.multichoicerecyclerview.MultiChoiceAdapter;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.LinkedList;

import hani.momanii.supernova_emoji_library.helper.EmojiconTextView;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MediaManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.components.ItemView;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.Packet;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.MultiChoiceHelper;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.FragmentChat;
import ru.paymon.android.utils.cache.lrudiskcache.DiskLruImageCache;
import ru.paymon.android.view.FragmentChats;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static String WALLET_PUBLIC_KEY = "PUBLIC_WALLET_KEY";
    public static String FORWARD_MESSAGES_KEY = "forward_messages";
    public LinkedList<Long> messageIDs;
    private IMessageClickListener iMessageClickListener;
    private LinkedList<Long> checkedMessageIDs;
    private boolean isGroup;
    private View defaultCustomView;
    private View selectedCustomView;
    private LinearLayout toolbarContainer;

    public interface IMessageClickListener {
        void longClick();

        void delete(LinkedList<Long> checkedMessageIDs);

        void forward(LinkedList<Long> checkedMessageIDs);
    }

    enum ViewTypes {
        SENT_MESSAGE,
        RECEIVED_MESSAGE,
        SENT_MESSAGE_ITEM,
        RECEIVED_MESSAGE_ITEM,
        GROUP_SENT_MESSAGE,
        GROUP_RECEIVED_MESSAGE,
        ITEM_ACTION_MESSAGE,
        SENT_MESSAGE_WALLET,
        RECEIVED_MESSAGE_WALLET,
        GROUP_RECEIVED_MESSAGE_WALLET,
        SENT_MESSAGE_PICTURE,
        RECEIVED_MESSAGE_PICTURE,
        GROUP_RECEIVED_MESSAGE_PICTURE,
        SENT_MESSAGE_DOCUMENT,
        RECEIVED_MESSAGE_DOCUMENT,
        GROUP_RECEIVED_MESSAGE_DOCUMENT,
        SENT_MESSAGE_VIDEO,
        RECEIVED_MESSAGE_VIDEO,
        GROUP_RECEIVED_MESSAGE_VIDEO
    }

    public MessagesAdapter(IMessageClickListener iMessageClickListener, boolean isGroup, View defaultCustomView, View selectedCustomView, LinearLayout toolbarContainer) {
        this.isGroup = isGroup;
        this.defaultCustomView = defaultCustomView;
        this.selectedCustomView = selectedCustomView;
        this.toolbarContainer = toolbarContainer;
        this.iMessageClickListener = iMessageClickListener;
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
            case GROUP_SENT_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_sent_message, viewGroup, false);
                vh = new SentMessageViewHolder(view);
                break;
            case GROUP_RECEIVED_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_group_received_message, viewGroup, false);
                vh = new GroupReceiveMessageViewHolder(view);
                break;
            case ITEM_ACTION_MESSAGE:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_action_item, viewGroup, false);
                vh = new ActionMessageViewHolder(view);
                break;
            case SENT_MESSAGE_WALLET:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_sent_message_wallet, viewGroup, false);
                vh = new SentMessageWalletViewHolder(view);
                break;
            case RECEIVED_MESSAGE_WALLET:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_received_message_wallet, viewGroup, false);
                vh = new ReceivedMessageWalletViewHolder(view);
                break;
            case GROUP_RECEIVED_MESSAGE_WALLET:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_group_received_message_wallet, viewGroup, false);
                vh = new GroupReceivedMessageWalletViewHolder(view);
                break;
            case SENT_MESSAGE_PICTURE:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_sent_message_picture, viewGroup, false);
                vh = new SentMessagePictureViewHolder(view);
                break;
            case RECEIVED_MESSAGE_PICTURE:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_received_message_picture, viewGroup, false);
                vh = new ReceivedMessagePictureViewHolder(view);
                break;
            case GROUP_RECEIVED_MESSAGE_PICTURE:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_group_received_message_picture, viewGroup, false);
                vh = new GroupReceivedMessagePictureViewHolder(view);
                break;
            case SENT_MESSAGE_DOCUMENT:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_sent_message_document, viewGroup, false);
                vh = new SentMessageDocumentViewHolder(view);
                break;
            case RECEIVED_MESSAGE_DOCUMENT:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_received_message_document, viewGroup, false);
                vh = new ReceivedMessageDocumentViewHolder(view);
                break;
            case GROUP_RECEIVED_MESSAGE_DOCUMENT:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_group_received_message_document, viewGroup, false);
                vh = new GroupReceivedMessageDocumentViewHolder(view);
                break;
            case SENT_MESSAGE_VIDEO:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_sent_message_video, viewGroup, false);
                vh = new SentMessageVideoViewHolder(view);
                break;
            case RECEIVED_MESSAGE_VIDEO:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_received_message_video, viewGroup, false);
                vh = new ReceivedMessageVideoViewHolder(view);
                break;
            case GROUP_RECEIVED_MESSAGE_VIDEO:
                view = LayoutInflater.from(context).inflate(R.layout.view_holder_group_received_message_video, viewGroup, false);
                vh = new GroupReceivedMessageVideoViewHolder(view);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((BaseViewHolder) holder).message = getMessage(position);

        if (holder instanceof SentMessageViewHolder) {
            final SentMessageViewHolder sentMessageViewHolder = (SentMessageViewHolder) holder;
            sentMessageViewHolder.msg.setText(sentMessageViewHolder.message.text);
            sentMessageViewHolder.time.setText(Utils.formatDateTime(sentMessageViewHolder.message.date, true));
        } else if (holder instanceof ReceiveMessageViewHolder) {
            final ReceiveMessageViewHolder receiveMessageViewHolder = (ReceiveMessageViewHolder) holder;
            receiveMessageViewHolder.msg.setText(receiveMessageViewHolder.message.text);
            receiveMessageViewHolder.time.setText(Utils.formatDateTime(receiveMessageViewHolder.message.date, true));
        } else if (holder instanceof SentMessageItemViewHolder) {
            final SentMessageItemViewHolder sentMessageItemViewHolder = (SentMessageItemViewHolder) holder;
            sentMessageItemViewHolder.item.setSticker(sentMessageItemViewHolder.message.itemType, sentMessageItemViewHolder.message.itemID);
            sentMessageItemViewHolder.time.setText(Utils.formatDateTime(sentMessageItemViewHolder.message.date, true));
        } else if (holder instanceof ReceiveMessageItemViewHolder) {
            final ReceiveMessageItemViewHolder receiveMessageItemViewHolder = (ReceiveMessageItemViewHolder) holder;
            receiveMessageItemViewHolder.item.setSticker(receiveMessageItemViewHolder.message.itemType, receiveMessageItemViewHolder.message.itemID);
            receiveMessageItemViewHolder.time.setText(Utils.formatDateTime(receiveMessageItemViewHolder.message.date, true));
        } else if (holder instanceof GroupReceiveMessageViewHolder) {
            final GroupReceiveMessageViewHolder groupReceiveMessageViewHolder = (GroupReceiveMessageViewHolder) holder;
            groupReceiveMessageViewHolder.msg.setText(groupReceiveMessageViewHolder.message.text);
            groupReceiveMessageViewHolder.time.setText(Utils.formatDateTime(groupReceiveMessageViewHolder.message.date, true));
            RPC.UserObject user = UsersManager.getInstance().users.get(groupReceiveMessageViewHolder.message.from_id);
            if (user != null)
                if (!user.photoURL.isEmpty())
                    Utils.loadPhoto(user.photoURL, groupReceiveMessageViewHolder.avatar);

            if(user != null && position != 0 && position < messageIDs.size()){
                RPC.Message previousMessage = getMessage(position-1);
                RPC.UserObject previousUser = UsersManager.getInstance().users.get(previousMessage.from_id);
                if(previousUser != null && user.id == previousUser.id){
                    groupReceiveMessageViewHolder.avatar.setVisibility(View.INVISIBLE);
                } else {
                    groupReceiveMessageViewHolder.avatar.setVisibility(View.VISIBLE);
                }
            }
        } else if (holder instanceof ActionMessageViewHolder) {
            final ActionMessageViewHolder actionMessageViewHolder = (ActionMessageViewHolder) holder;
            actionMessageViewHolder.msg.setText(actionMessageViewHolder.message.text);
            actionMessageViewHolder.time.setText(Utils.formatDateTime(actionMessageViewHolder.message.date, true));
        } else if (holder instanceof SentMessagePictureViewHolder) {
            final SentMessagePictureViewHolder sentMessagePictureViewHolder = (SentMessagePictureViewHolder) holder;
            sentMessagePictureViewHolder.text.setText(sentMessagePictureViewHolder.message.text);
            sentMessagePictureViewHolder.time.setText(Utils.formatDateTime(sentMessagePictureViewHolder.message.date, true));
            //TODO:Прикреп картинки
        } else if (holder instanceof ReceivedMessagePictureViewHolder) {
            final ReceivedMessagePictureViewHolder receivedMessagePictureViewHolder = (ReceivedMessagePictureViewHolder) holder;
            receivedMessagePictureViewHolder.text.setText(receivedMessagePictureViewHolder.message.text);
            receivedMessagePictureViewHolder.time.setText(receivedMessagePictureViewHolder.message.date);
            //TODO:Прикреп картинки
        } else if (holder instanceof GroupReceivedMessagePictureViewHolder) {
            final GroupReceivedMessagePictureViewHolder groupReceivedMessagePictureViewHolder = (GroupReceivedMessagePictureViewHolder) holder;
            groupReceivedMessagePictureViewHolder.text.setText(groupReceivedMessagePictureViewHolder.message.text);
            groupReceivedMessagePictureViewHolder.time.setText(groupReceivedMessagePictureViewHolder.message.date);
            int uid = groupReceivedMessagePictureViewHolder.message.from_id;
            Long pid = MediaManager.getInstance().userProfilePhotoIDs.get(uid);
            if (pid != null) {
                RPC.PM_photo photo = new RPC.PM_photo();
                photo.user_id = uid;
                photo.id = pid;
//                groupReceivedMessagePictureViewHolder.avatar.setPhoto(photo);
            }
            //TODO:Прикреп картинки
        } else if (holder instanceof SentMessageDocumentViewHolder) {
            final SentMessageDocumentViewHolder sentMessageDocumentViewHolder = (SentMessageDocumentViewHolder) holder;
            sentMessageDocumentViewHolder.text.setText(sentMessageDocumentViewHolder.message.text);
            sentMessageDocumentViewHolder.time.setText(sentMessageDocumentViewHolder.message.date);
            //TODO:Часть с файлом
        } else if (holder instanceof ReceivedMessageDocumentViewHolder) {
            final ReceivedMessageDocumentViewHolder receivedMessageDocumentViewHolder = (ReceivedMessageDocumentViewHolder) holder;
            receivedMessageDocumentViewHolder.text.setText(receivedMessageDocumentViewHolder.message.text);
            receivedMessageDocumentViewHolder.time.setText(receivedMessageDocumentViewHolder.message.date);
            //TODO:Часть с файлом
        } else if (holder instanceof GroupReceivedMessageDocumentViewHolder) {
            final GroupReceivedMessageDocumentViewHolder groupReceivedMessageDocumentViewHolder = (GroupReceivedMessageDocumentViewHolder) holder;
            groupReceivedMessageDocumentViewHolder.text.setText(groupReceivedMessageDocumentViewHolder.message.text);
            groupReceivedMessageDocumentViewHolder.time.setText(groupReceivedMessageDocumentViewHolder.message.date);
            int uid = groupReceivedMessageDocumentViewHolder.message.from_id;
            Long pid = MediaManager.getInstance().userProfilePhotoIDs.get(uid);
            if (pid != null) {
                RPC.PM_photo photo = new RPC.PM_photo();
                photo.user_id = uid;
                photo.id = pid;
//                groupReceivedMessageDocumentViewHolder.avatar.setPhoto(photo);
            }
            //TODO:Часть с файлом
        } else if (holder instanceof SentMessageVideoViewHolder) {
            final SentMessageVideoViewHolder sentMessageVideoViewHolder = (SentMessageVideoViewHolder) holder;
            sentMessageVideoViewHolder.text.setText(sentMessageVideoViewHolder.message.text);
            sentMessageVideoViewHolder.time.setText(Utils.formatDateTime(sentMessageVideoViewHolder.message.date, true));
            //TODO:Прикреп видео
        } else if (holder instanceof ReceivedMessageVideoViewHolder) {
            final ReceivedMessageVideoViewHolder receivedMessageVideoViewHolder = (ReceivedMessageVideoViewHolder) holder;
            receivedMessageVideoViewHolder.text.setText(receivedMessageVideoViewHolder.message.text);
            receivedMessageVideoViewHolder.time.setText(receivedMessageVideoViewHolder.message.date);
            //TODO:Прикреп видео
        } else if (holder instanceof GroupReceivedMessageVideoViewHolder) {
            final GroupReceivedMessageVideoViewHolder groupReceivedMessageVideoViewHolder = (GroupReceivedMessageVideoViewHolder) holder;
            groupReceivedMessageVideoViewHolder.text.setText(groupReceivedMessageVideoViewHolder.message.text);
            groupReceivedMessageVideoViewHolder.time.setText(groupReceivedMessageVideoViewHolder.message.date);
            int uid = groupReceivedMessageVideoViewHolder.message.from_id;
            Long pid = MediaManager.getInstance().userProfilePhotoIDs.get(uid);
            if (pid != null) {
                RPC.PM_photo photo = new RPC.PM_photo();
                photo.user_id = uid;
                photo.id = pid;
//                groupReceivedMessageVideoViewHolder.avatar.setPhoto(photo);
            }
            //TODO:Прикреп видео
        } else if (holder instanceof SentMessageWalletViewHolder) {
            final SentMessageWalletViewHolder sentMessageWalletViewHolder = (SentMessageWalletViewHolder) holder;
            final String publicKey = Utils.getETHorBTCpubKeyFromText(sentMessageWalletViewHolder.message.text);
            final int keyType = Utils.WTF(publicKey);

            sentMessageWalletViewHolder.publicKey.setText(publicKey);

            switch (keyType) {
                case 1:
                    sentMessageWalletViewHolder.imageWallet.setImageResource(R.drawable.ic_bitcoin);
                    break;
                case 2:
                    sentMessageWalletViewHolder.imageWallet.setImageResource(R.drawable.ic_ethereum);
                    break;
            }

            sentMessageWalletViewHolder.time.setText(Utils.formatDateTime(sentMessageWalletViewHolder.message.date, true));
            sentMessageWalletViewHolder.text.setText(sentMessageWalletViewHolder.message.text);

            sentMessageWalletViewHolder.buttonWallet.setOnClickListener((view -> {
                Bundle bundle = new Bundle();
                bundle.putString(WALLET_PUBLIC_KEY, publicKey);
                //TODO:открытие вью работы с кошельком
            }));
        } else if (holder instanceof GroupReceivedMessageWalletViewHolder) {
            final GroupReceivedMessageWalletViewHolder groupReceivedMessageWalletViewHolder = (GroupReceivedMessageWalletViewHolder) holder;
            final String publicKey = Utils.getETHorBTCpubKeyFromText(groupReceivedMessageWalletViewHolder.message.text);
            final int keyType = Utils.WTF(publicKey);

            groupReceivedMessageWalletViewHolder.publicKey.setText(publicKey);

            switch (keyType) {
                case 1:
                    groupReceivedMessageWalletViewHolder.imageWallet.setImageResource(R.drawable.ic_bitcoin);
                    break;
                case 2:
                    groupReceivedMessageWalletViewHolder.imageWallet.setImageResource(R.drawable.ic_ethereum);
                    break;
            }

            groupReceivedMessageWalletViewHolder.time.setText(Utils.formatDateTime(groupReceivedMessageWalletViewHolder.message.date, true));
            groupReceivedMessageWalletViewHolder.text.setText(groupReceivedMessageWalletViewHolder.message.text);

            groupReceivedMessageWalletViewHolder.buttonWallet.setOnClickListener((view -> {
                Bundle bundle = new Bundle();
                bundle.putString(WALLET_PUBLIC_KEY, publicKey);
                //TODO:открытие вью работы с кошельком
            }));

            int uid = groupReceivedMessageWalletViewHolder.message.from_id;
            Long pid = MediaManager.getInstance().userProfilePhotoIDs.get(uid);
            if (pid != null) {
                RPC.PM_photo photo = new RPC.PM_photo();
                photo.user_id = uid;
                photo.id = pid;
//                groupReceivedMessageWalletViewHolder.avatar.setPhoto(photo);
            }
        } else if (holder instanceof ReceivedMessageWalletViewHolder) {
            final ReceivedMessageWalletViewHolder receivedMessageWalletViewHolder = (ReceivedMessageWalletViewHolder) holder;
            final String publicKey = Utils.getETHorBTCpubKeyFromText(receivedMessageWalletViewHolder.message.text);
            final int keyType = Utils.WTF(publicKey);

            receivedMessageWalletViewHolder.publicKey.setText(publicKey);

            switch (keyType) {
                case 1:
                    receivedMessageWalletViewHolder.imageWallet.setImageResource(R.drawable.ic_bitcoin);
                    break;
                case 2:
                    receivedMessageWalletViewHolder.imageWallet.setImageResource(R.drawable.ic_ethereum);
                    break;
            }

            receivedMessageWalletViewHolder.time.setText(Utils.formatDateTime(receivedMessageWalletViewHolder.message.date, true));
            receivedMessageWalletViewHolder.text.setText(receivedMessageWalletViewHolder.message.text);

            receivedMessageWalletViewHolder.buttonWallet.setOnClickListener((view -> {
                Bundle bundle = new Bundle();
                bundle.putString(WALLET_PUBLIC_KEY, publicKey);
                //TODO:открытие вью работы с кошельком
            }));
        }
    }

//    @Override
//    public void setActive(@NonNull View view, boolean state) {
//        if (state)
//            view.setBackgroundColor(ContextCompat.getColor(ApplicationLoader.applicationContext, R.color.gray));
//        else
//            view.setBackgroundColor(ContextCompat.getColor(ApplicationLoader.applicationContext, android.R.color.transparent));
//    }

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
        int viewType = ViewTypes.SENT_MESSAGE.ordinal();

        if (isSent) {
            if (msg instanceof RPC.PM_message) {
                if (!isGroup)
                    viewType = ViewTypes.SENT_MESSAGE.ordinal();
                else
                    viewType = ViewTypes.GROUP_SENT_MESSAGE.ordinal();
            } else if (msg instanceof RPC.PM_messageItem) {
                if (((RPC.PM_messageItem) msg).itemType == FileManager.FileType.ACTION)
                    viewType = ViewTypes.ITEM_ACTION_MESSAGE.ordinal();
                else if (msg.itemType == FileManager.FileType.PHOTO)
                    viewType = ViewTypes.SENT_MESSAGE_PICTURE.ordinal();
                else if (msg.itemType == FileManager.FileType.DOCUMENT)
                    viewType = ViewTypes.SENT_MESSAGE_DOCUMENT.ordinal();
                else if (msg.itemType == FileManager.FileType.VIDEO)
                    viewType = ViewTypes.SENT_MESSAGE_VIDEO.ordinal();
                else if (msg.itemType == FileManager.FileType.WALLET)
                    viewType = ViewTypes.SENT_MESSAGE_WALLET.ordinal();
                else
                    viewType = ViewTypes.SENT_MESSAGE_ITEM.ordinal();
            }
        } else {
            if (msg instanceof RPC.PM_message) {
                if (!isGroup)
                    viewType = ViewTypes.RECEIVED_MESSAGE.ordinal();
                else
                    viewType = ViewTypes.GROUP_RECEIVED_MESSAGE.ordinal();
            } else if (msg instanceof RPC.PM_messageItem) {
                if (((RPC.PM_messageItem) msg).itemType == FileManager.FileType.ACTION)
                    viewType = ViewTypes.ITEM_ACTION_MESSAGE.ordinal();
                else if (msg.itemType == FileManager.FileType.PHOTO) {
                    if (!isGroup)
                        viewType = ViewTypes.RECEIVED_MESSAGE_PICTURE.ordinal();
                    else
                        viewType = ViewTypes.GROUP_RECEIVED_MESSAGE_PICTURE.ordinal();
                } else if (msg.itemType == FileManager.FileType.DOCUMENT) {
                    if (!isGroup)
                        viewType = ViewTypes.RECEIVED_MESSAGE_DOCUMENT.ordinal();
                    else
                        viewType = ViewTypes.GROUP_RECEIVED_MESSAGE_DOCUMENT.ordinal();
                } else if (msg.itemType == FileManager.FileType.VIDEO) {
                    if (!isGroup)
                        viewType = ViewTypes.RECEIVED_MESSAGE_VIDEO.ordinal();
                    else
                        viewType = ViewTypes.GROUP_RECEIVED_MESSAGE_VIDEO.ordinal();
                } else if (msg.itemType == FileManager.FileType.WALLET) {
                    if (!isGroup)
                        viewType = ViewTypes.RECEIVED_MESSAGE_WALLET.ordinal();
                    else
                        viewType = ViewTypes.GROUP_RECEIVED_MESSAGE_WALLET.ordinal();
                } else
                    viewType = ViewTypes.RECEIVED_MESSAGE_ITEM.ordinal();
            }
        }

        return viewType;
    }

    public void deselectAll() {
        checkedMessageIDs.clear();
        notifyDataSetChanged();
    }

    private void init() {
        final TextView textViewSelectedTitle = (TextView) selectedCustomView.findViewById(R.id.selected_title);
        final ImageView imageViewSelectedDelete = (ImageView) selectedCustomView.findViewById(R.id.selected_delete);
        final ImageView imageViewSelectedCopy = (ImageView) selectedCustomView.findViewById(R.id.selected_copy);
        final ImageView imageViewSelectedForward = (ImageView) selectedCustomView.findViewById(R.id.selected_forward);
        final ImageView backToolbar = (ImageView) selectedCustomView.findViewById(R.id.selected_back);

        backToolbar.setOnClickListener(view -> deselectAll());

        imageViewSelectedForward.setOnClickListener((view -> {
            iMessageClickListener.forward(checkedMessageIDs);
        }));

        imageViewSelectedDelete.setOnClickListener((view) -> {
            iMessageClickListener.delete(checkedMessageIDs);
        });

        imageViewSelectedCopy.setOnClickListener((view) -> {
            StringBuilder copiedMessages = new StringBuilder();
            for (int i = 0; i < checkedMessageIDs.size(); i++) {
                String text = i < checkedMessageIDs.size() - 1 ? MessagesManager.getInstance().messages.get(checkedMessageIDs.get(i)).text : MessagesManager.getInstance().messages.get(checkedMessageIDs.get(i)).text + "\n";
                copiedMessages.append(text);
            }

            ClipboardManager clipboard = (ClipboardManager) ApplicationLoader.applicationContext.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Copied messages", copiedMessages.toString());
            clipboard.setPrimaryClip(clip);
            deselectAll();
        });

//        setMultiChoiceSelectionListener(new MultiChoiceAdapter.Listener() {
//            @Override
//            public void OnItemSelected(int selectedPosition, int itemSelectedCount, int allItemCount) {
//                if (itemSelectedCount >= 1) {
//                    textViewSelectedTitle.setText(String.format("%s: %s", ApplicationLoader.applicationContext.getString(R.string.selected_messages_count), itemSelectedCount));
//                    toolbarContainer.removeAllViews();
//                    toolbarContainer.addView(selectedCustomView);
//                }
//                checkedMessageIDs.add(getItemId(selectedPosition));
//            }
//
//            @Override
//            public void OnItemDeselected(int deselectedPosition, int itemSelectedCount, int allItemCount) {
//                checkedMessageIDs.remove(getItemId(deselectedPosition));
//                if (itemSelectedCount < 1) {
//                    toolbarContainer.removeAllViews();
//                    toolbarContainer.addView(defaultCustomView);
//                } else {
//                    textViewSelectedTitle.setText(String.format("%s: %s", ApplicationLoader.applicationContext.getString(R.string.selected_messages_count), itemSelectedCount));
//                    toolbarContainer.removeAllViews();
//                    toolbarContainer.addView(selectedCustomView);
//                }
//            }
//
//            @Override
//            public void OnSelectAll(int itemSelectedCount, int allItemCount) {
//
//            }
//
//            @Override
//            public void OnDeselectAll(int itemSelectedCount, int allItemCount) {
//                checkedMessageIDs.clear();
//                if (itemSelectedCount < 1) {
//                    toolbarContainer.removeAllViews();
//                    toolbarContainer.addView(defaultCustomView);
//                }
//            }
//        });
    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder {
        public RPC.Message message;
        public MultiChoiceHelper helper;

        public BaseViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class SentMessageViewHolder extends BaseViewHolder {
        EmojiconTextView msg;
        TextView time;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            msg = (EmojiconTextView) itemView.findViewById(R.id.message_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class ReceiveMessageViewHolder extends BaseViewHolder {
        EmojiconTextView msg;
        TextView time;

        ReceiveMessageViewHolder(View view) {
            super(view);
            msg = (EmojiconTextView) view.findViewById(R.id.message_text_view);
            time = (TextView) view.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class SentMessageItemViewHolder extends BaseViewHolder {
        ItemView item;
        TextView time;

        SentMessageItemViewHolder(View view) {
            super(view);
            item = (ItemView) view.findViewById(R.id.message_item_view);
            time = (TextView) view.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class ReceiveMessageItemViewHolder extends BaseViewHolder {
        ItemView item;
        TextView time;

        ReceiveMessageItemViewHolder(View view) {
            super(view);
            item = (ItemView) view.findViewById(R.id.message_item_view);
            time = (TextView) view.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class GroupReceiveMessageViewHolder extends BaseViewHolder {
        EmojiconTextView msg;
        TextView time;
        CircularImageView avatar;

        GroupReceiveMessageViewHolder(View view) {
            super(view);
            msg = (EmojiconTextView) view.findViewById(R.id.message_text_view);
            time = (TextView) view.findViewById(R.id.timestamp_text_view);
            avatar = (CircularImageView) view.findViewById(R.id.photo);
        }
    }

    public static class ActionMessageViewHolder extends BaseViewHolder {
        TextView msg;
        TextView time;

        ActionMessageViewHolder(View view) {
            super(view);
            msg = (TextView) view.findViewById(R.id.message_text_view);
            time = (TextView) view.findViewById(R.id.timestamp_text_view);
        }
    }

    public static class SentMessageWalletViewHolder extends BaseViewHolder {
        EmojiconTextView text;
        TextView time;
        ImageButton buttonWallet;
        ImageView imageWallet;
        TextView publicKey;

        public SentMessageWalletViewHolder(View itemView) {
            super(itemView);
            text = (EmojiconTextView) itemView.findViewById(R.id.wallet_sent_message_text_view);
            time = (TextView) itemView.findViewById(R.id.wallet_sent_timestamp_text_view);
            buttonWallet = (ImageButton) itemView.findViewById(R.id.wallet_sent_image_button);
            imageWallet = (ImageView) itemView.findViewById(R.id.wallet_sent_image_view);
            publicKey = (TextView) itemView.findViewById(R.id.wallet_sent_text_view);
        }
    }

    public static class ReceivedMessageWalletViewHolder extends BaseViewHolder {
        EmojiconTextView text;
        TextView time;
        ImageButton buttonWallet;
        ImageView imageWallet;
        TextView publicKey;

        public ReceivedMessageWalletViewHolder(View itemView) {
            super(itemView);
            text = (EmojiconTextView) itemView.findViewById(R.id.wallet_received_message_text_view);
            time = (TextView) itemView.findViewById(R.id.wallet_received_timestamp_text_view);
            buttonWallet = (ImageButton) itemView.findViewById(R.id.wallet_received_image_button);
            imageWallet = (ImageView) itemView.findViewById(R.id.wallet_received_image_view);
            publicKey = (TextView) itemView.findViewById(R.id.wallet_received_text_view);
        }
    }

    public static class GroupReceivedMessageWalletViewHolder extends BaseViewHolder {
        EmojiconTextView text;
        TextView time;
        ImageButton buttonWallet;
        ImageView imageWallet;
        CircularImageView avatar;
        TextView publicKey;

        public GroupReceivedMessageWalletViewHolder(View itemView) {
            super(itemView);
            text = (EmojiconTextView) itemView.findViewById(R.id.wallet_received_group_message_text_view);
            time = (TextView) itemView.findViewById(R.id.wallet_received_group_timestamp_text_view);
            buttonWallet = (ImageButton) itemView.findViewById(R.id.wallet_received_group_image_button);
            imageWallet = (ImageView) itemView.findViewById(R.id.wallet_received_group_image_view);
            avatar = (CircularImageView) itemView.findViewById(R.id.photo_wallet_received_group);
            publicKey = (TextView) itemView.findViewById(R.id.wallet_received_group_text_view);
        }
    }

    public static class SentMessagePictureViewHolder extends BaseViewHolder {
        EmojiconTextView text;
        TextView time;
        ImageView picture;

        SentMessagePictureViewHolder(View itemView) {
            super(itemView);
            text = (EmojiconTextView) itemView.findViewById(R.id.message_picture_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_picture_text_view);
            picture = (ImageView) itemView.findViewById(R.id.message_picture_image_view);
        }
    }

    public static class ReceivedMessagePictureViewHolder extends BaseViewHolder {
        EmojiconTextView text;
        TextView time;
        ImageView picture;

        ReceivedMessagePictureViewHolder(View itemView) {
            super(itemView);
            text = (EmojiconTextView) itemView.findViewById(R.id.message_received_picture_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_received_picture_text_view);
            picture = (ImageView) itemView.findViewById(R.id.message_received_picture_image_view);
        }
    }

    public static class GroupReceivedMessagePictureViewHolder extends BaseViewHolder {
        CircularImageView avatar;
        EmojiconTextView text;
        TextView time;
        ImageView picture;

        GroupReceivedMessagePictureViewHolder(View itemView) {
            super(itemView);
            avatar = (CircularImageView) itemView.findViewById(R.id.message_group_received_picture_photo);
            text = (EmojiconTextView) itemView.findViewById(R.id.message_group_received_picture_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_group_received_picture_text_view);
            picture = (ImageView) itemView.findViewById(R.id.message_group_received_picture_image_view);
        }
    }

    public static class SentMessageDocumentViewHolder extends BaseViewHolder {
        EmojiconTextView text;
        TextView time;
        TextView title;
        TextView expansion;
        ImageView image;

        SentMessageDocumentViewHolder(View itemView) {
            super(itemView);
            text = (EmojiconTextView) itemView.findViewById(R.id.message_document_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_document_text_view);
            title = (TextView) itemView.findViewById(R.id.message_title_document_text_view);
            expansion = (TextView) itemView.findViewById(R.id.message_expansion_document_text_view);
            image = (ImageView) itemView.findViewById(R.id.message_document_image_view);
        }
    }

    public static class ReceivedMessageDocumentViewHolder extends BaseViewHolder {
        EmojiconTextView text;
        TextView time;
        TextView title;
        TextView expansion;
        ImageView image;

        ReceivedMessageDocumentViewHolder(View itemView) {
            super(itemView);
            text = (EmojiconTextView) itemView.findViewById(R.id.message_received_document_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_received_document_text_view);
            title = (TextView) itemView.findViewById(R.id.title_message_received_document_text_view);
            expansion = (TextView) itemView.findViewById(R.id.expansion_message_received_document_text_view);
            image = (ImageView) itemView.findViewById(R.id.message_received_document_image_view);
        }
    }

    public static class GroupReceivedMessageDocumentViewHolder extends BaseViewHolder {
        CircularImageView avatar;
        EmojiconTextView text;
        TextView time;
        TextView title;
        TextView expansion;
        ImageView image;

        GroupReceivedMessageDocumentViewHolder(View itemView) {
            super(itemView);
            avatar = (CircularImageView) itemView.findViewById(R.id.message_group_received_document_avatar);
            text = (EmojiconTextView) itemView.findViewById(R.id.message_group_received_document_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_group_received_document_text_view);
            title = (TextView) itemView.findViewById(R.id.title_group_received_document_text_view);
            expansion = (TextView) itemView.findViewById(R.id.expansion_group_received_document_text_view);
            image = (ImageView) itemView.findViewById(R.id.message_group_received_document_image_view);
        }
    }

    public static class SentMessageVideoViewHolder extends BaseViewHolder {
        EmojiconTextView text;
        TextView time;
        VideoView video;

        SentMessageVideoViewHolder(View itemView) {
            super(itemView);
            text = (EmojiconTextView) itemView.findViewById(R.id.message_video_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_video_text_view);
            video = (VideoView) itemView.findViewById(R.id.message_video_video_view);
        }
    }

    public static class ReceivedMessageVideoViewHolder extends BaseViewHolder {
        EmojiconTextView text;
        TextView time;
        VideoView video;

        ReceivedMessageVideoViewHolder(View itemView) {
            super(itemView);
            text = (EmojiconTextView) itemView.findViewById(R.id.message_received_video_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_received_video_text_view);
            video = (VideoView) itemView.findViewById(R.id.message_received_video_video_view);
        }
    }

    public static class GroupReceivedMessageVideoViewHolder extends BaseViewHolder {
        CircularImageView avatar;
        EmojiconTextView text;
        TextView time;
        VideoView video;

        GroupReceivedMessageVideoViewHolder(View itemView) {
            super(itemView);
            avatar = (CircularImageView) itemView.findViewById(R.id.message_group_received_video_photo);
            text = (EmojiconTextView) itemView.findViewById(R.id.message_group_received_video_text_view);
            time = (TextView) itemView.findViewById(R.id.timestamp_group_received_video_text_view);
            video = (VideoView) itemView.findViewById(R.id.message_group_received_video_video_view);
        }
    }
}
