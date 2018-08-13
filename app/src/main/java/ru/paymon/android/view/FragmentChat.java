package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

import hani.momanii.supernova_emoji_library.helper.EmojiconEditText;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.AttachmentsAdapter;
import ru.paymon.android.adapters.MessagesAdapter;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.models.attachments.AttachmentItem;
import ru.paymon.android.models.attachments.AttachmentMessages;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.adapters.MessagesAdapter.FORWARD_MESSAGES_KEY;
import static ru.paymon.android.net.RPC.Message.MESSAGE_FLAG_FROM_ID;


public class FragmentChat extends Fragment implements NotificationManager.IListener {
    public static final String CHAT_ID_KEY = "CHAT_ID_KEY";
    private int chatID;
    private RecyclerView messagesRecyclerView;
    private MessagesAdapter messagesAdapter;
    private Button sendButton;
    private EmojiconEditText messageInput;
    private ArrayList<RPC.UserObject> groupUsers;
    private boolean isGroup;
    private boolean loadingMessages;
    private boolean isForward;
    private LinkedList<Long> forwardMessages;
    private AttachmentsAdapter attachmentsAdapter;

    public static synchronized FragmentChat newInstance() {
        return new FragmentChat();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(CHAT_ID_KEY))
                chatID = bundle.getInt(CHAT_ID_KEY);
            if (bundle.containsKey("users")) {
                isGroup = true;
                groupUsers = bundle.getParcelableArrayList("users");
            }
            if (bundle.containsKey(FORWARD_MESSAGES_KEY)) {
                isForward = true;
                forwardMessages = (LinkedList<Long>) bundle.getSerializable(FORWARD_MESSAGES_KEY);
            }
        }
        MessagesManager.getInstance().currentChatID = chatID;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageInput = (EmojiconEditText) view.findViewById(R.id.input_edit_text);
        messagesRecyclerView = (RecyclerView) view.findViewById(R.id.chat_recview);
        sendButton = (Button) view.findViewById(R.id.sendButton);
        LinearLayout toolbarContainer = (LinearLayout) view.findViewById(R.id.toolbar_container);

        View defaultCustomView;
        if (isGroup)
            defaultCustomView = createChatGroupCustomView();
        else
            defaultCustomView = createChatCustomView();

        toolbarContainer.addView(defaultCustomView);

        initChat(defaultCustomView, toolbarContainer);

        if (isForward) {
            RecyclerView recyclerViewAttachments = (RecyclerView) view.findViewById(R.id.recViewAttachments);
            recyclerViewAttachments.setHasFixedSize(true);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            recyclerViewAttachments.setLayoutManager(layoutManager);
            LinkedList<AttachmentItem> attachmentItems = new LinkedList<>();
            AttachmentMessages attachmentMessages = new AttachmentMessages();
            attachmentMessages.messages = forwardMessages;
            attachmentItems.add(attachmentMessages);
            attachmentsAdapter = new AttachmentsAdapter(attachmentItems);
            recyclerViewAttachments.setAdapter(attachmentsAdapter);
        }

        MessagesManager.getInstance().loadMessages(chatID, 15, 0, isGroup);

//        LongSparseArray<RPC.Message> messageLongSparseArray = DBHelper.getAllMessages();
//        for (int i =0 ; i< messageLongSparseArray.size(); i++) {
//            RPC.Message message = messageLongSparseArray.get(messageLongSparseArray.keyAt(i));
//            message.text = "HUI";
//        }
//        DBHelper.updateMessages(messageLongSparseArray);
//        LongSparseArray<RPC.Message> mss = DBHelper.getAllMessages();
//        for (int i =0 ; i< mss.size(); i++) {
//            RPC.Message message = mss.get(mss.keyAt(i));
//            Log.e("AAA", message.text);
//        }
//        DBHelper.updateMessages(messageLongSparseArray);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.chatAddMessages);
        MessagesManager.getInstance().currentChatID = chatID;
        Utils.hideBottomBar(getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.chatAddMessages);
        MessagesManager.getInstance().currentChatID = 0;
    }

    private void initChat(View defaultCustomView, LinearLayout toolbarContainer) {
        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        messagesAdapter = new MessagesAdapter((AppCompatActivity) getActivity(), isGroup, defaultCustomView, toolbarContainer);
        messagesRecyclerView.setAdapter(messagesAdapter);

        messagesRecyclerView
                .addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        if (!loadingMessages && (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) && messagesAdapter.getItemCount() != 0) {
                            loadingMessages = true;
                            //TODO:прогресс бар загрузки сообщений
                            Utils.netQueue.postRunnable(() -> MessagesManager.getInstance().loadMessages(chatID, 15, messagesAdapter.messageIDs.size(), isGroup));
                        }
                    }
                });

        sendButton.setOnClickListener((view) -> { //TODO:отправка прикрепленных файлов\пересланных сообщений
            Utils.netQueue.postRunnable(() -> {
                final String messageText = messageInput.getText().toString();

                if (User.currentUser == null || messageText.trim().isEmpty()) return;

                RPC.PM_message messageRequest = new RPC.PM_message();
                messageRequest.id = MessagesManager.generateMessageID();
                messageRequest.text = messageText;
                messageRequest.flags = MESSAGE_FLAG_FROM_ID;
                messageRequest.date = (int) (System.currentTimeMillis() / 1000L);
                messageRequest.from_id = User.currentUser.id;
                messageRequest.to_id = !isGroup ? new RPC.PM_peerUser(chatID) : new RPC.PM_peerGroup(chatID);
                messageRequest.unread = true;

                final long requestID = NetworkManager.getInstance().sendRequest(messageRequest, (response, error) -> {
                    if (error != null || response == null) return;

                    RPC.PM_updateMessageID updateMsgID = (RPC.PM_updateMessageID) response;

                    messageRequest.id = updateMsgID.newID;
                    MessagesManager.getInstance().putMessage(messageRequest);
                    if (messageRequest.to_id.user_id == User.currentUser.id)
                        MessagesManager.getInstance().lastMessages.put(messageRequest.from_id, messageRequest.id);
                    else
                        MessagesManager.getInstance().lastMessages.put(messageRequest.to_id.user_id, messageRequest.id);

                    messagesAdapter.messageIDs.add(messageRequest.id);
                    ApplicationLoader.applicationHandler.post(() -> messagesAdapter.notifyDataSetChanged());
                    messagesRecyclerView.smoothScrollToPosition(messagesRecyclerView.getAdapter().getItemCount() - 1);
                    //TODO:сделать, чтобы если сообщение не дошло, предлагало переотправить
                    NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.dialogsNeedReload, chatID);
                });
            });

            messageInput.setText("");
        });
    }

    private View createChatCustomView() {
        final View customView = getLayoutInflater().inflate(R.layout.toolbar_chat, null);
        final TextView chatTitleTextView = (TextView) customView.findViewById(R.id.toolbar_title);
        final CircleImageView toolbarAvatar = (CircleImageView) customView.findViewById(R.id.toolbar_avatar);
        final ImageView backToolbar = (ImageView) customView.findViewById(R.id.toolbar_back_btn);

        backToolbar.setOnClickListener(view -> getActivity().getSupportFragmentManager().popBackStack());

        final RPC.UserObject user = UsersManager.getInstance().users.get(chatID);
        if (user != null) {
            chatTitleTextView.setText(Utils.formatUserName(user));
            RPC.PM_photo photo = new RPC.PM_photo();
            photo.id = user.photoID;
            photo.user_id = user.id;
            toolbarAvatar.setPhoto(photo);
        }

        customView.setOnClickListener(v -> {
            final Bundle bundle = new Bundle();
            bundle.putInt(CHAT_ID_KEY, chatID);
            final FragmentFriendProfile fragmentFriendProfile = FragmentFriendProfile.newInstance();
            fragmentFriendProfile.setArguments(bundle);
            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentFriendProfile, null);
        });

        return customView;
    }

    private View createChatGroupCustomView() {
        final View customView = getLayoutInflater().inflate(R.layout.toolbar_chat_group, null);
        final TextView chatTitleTextView = (TextView) customView.findViewById(R.id.toolbar_title);
        final TextView participantsCountTextView = (TextView) customView.findViewById(R.id.participants_count);
        final CircleImageView toolbarAvatar = (CircleImageView) customView.findViewById(R.id.chat_group_avatar);
        final ImageView backToolbar = (ImageView) customView.findViewById(R.id.toolbar_back_btn);

        backToolbar.setOnClickListener(view -> getActivity().getSupportFragmentManager().popBackStack());

        final RPC.Group group = GroupsManager.getInstance().groups.get(chatID);
        if (group != null) {
            chatTitleTextView.setText(group.title);
            toolbarAvatar.setPhoto(group.photo);
            participantsCountTextView.setText(getString(R.string.participants) + ": " + groupUsers.size());
        }

        customView.setOnClickListener(v -> {
            final Bundle bundle = new Bundle();
            bundle.putInt("chat_id", chatID);
            final FragmentGroupSettings fragmentGroupSettings = FragmentGroupSettings.newInstance();
            fragmentGroupSettings.setArguments(bundle);
            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            Utils.replaceFragmentWithAnimationSlideFade(fragmentManager, fragmentGroupSettings, null);
        });

        return customView;
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent id, Object... args) {
        if (id == NotificationManager.NotificationEvent.chatAddMessages) {
            if (args.length < 1) return;

            LinkedList<Long> messages = (LinkedList<Long>) args[0];
            Boolean onScroll = (Boolean) args[1];

            if (messagesAdapter == null || messagesAdapter.messageIDs == null) return;

            messagesAdapter.messageIDs.addAll(0, messages);

            messagesAdapter.notifyDataSetChanged();
            if (!onScroll) {
                if (((LinearLayoutManager) messagesRecyclerView.getLayoutManager()).findLastVisibleItemPosition() >= messagesRecyclerView.getAdapter().getItemCount() - 2)
                    messagesRecyclerView.smoothScrollToPosition(messagesRecyclerView.getAdapter().getItemCount() - 1);
            } else {
                if (messagesAdapter.getItemCount() > 0) {
                    int scrolledCount = (int) args[2];
                    messagesRecyclerView.scrollToPosition(scrolledCount + ((LinearLayoutManager) messagesRecyclerView.getLayoutManager()).findLastVisibleItemPosition());
                }
            }
            loadingMessages = false;
        }
    }
}
