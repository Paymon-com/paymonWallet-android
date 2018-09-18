package ru.paymon.android.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.util.ArrayList;
import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.MessagesAdapter;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.Packet;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.ImagePicker;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.net.RPC.Message.MESSAGE_FLAG_FROM_ID;


public class FragmentChat extends Fragment implements NotificationManager.IListener {
    private static final int PICK_IMAGE_ID = 100;
    private static final int PICK_VIDEO_ID = 99;
    private static final int PICK_DOCUMENT_ID = 98;
    private static FragmentChat instance;
    public static final String CHAT_ID_KEY = "CHAT_ID_KEY";
    private int chatID;
    private RecyclerView messagesRecyclerView;
    private MessagesAdapter messagesAdapter;
    private Button sendButton;
    private EmojiEditText messageInput;
    private ArrayList<RPC.UserObject> groupUsers;
    private boolean isGroup;
    private boolean loadingMessages;
//    private DialogProgress dialogProgress;
//    private EmojIconActions emojIcon;

    public static synchronized FragmentChat newInstance() {
        if (instance == null)
            instance = new FragmentChat();
        return instance;
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
            }else{
                isGroup = false;
            }
        }
        MessagesManager.getInstance().currentChatID = chatID;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageInput = (EmojiEditText) view.findViewById(R.id.input_edit_text);
        messagesRecyclerView = (RecyclerView) view.findViewById(R.id.chat_recview);
        sendButton = (Button) view.findViewById(R.id.sendButton);
        LinearLayout toolbarContainer = (LinearLayout) view.findViewById(R.id.toolbar_container);
        ImageView emoticonsButton = (ImageView) view.findViewById(R.id.smilesButton);
        ConstraintLayout includeAttachment = (ConstraintLayout) view.findViewById(R.id.fragment_chat_attachment_include);

        ImageButton buttonAttachment = (ImageButton) view.findViewById(R.id.attach_button);
        ImageButton buttonDocumentAttachment = (ImageButton) view.findViewById(R.id.document_chat_attachment);
        ImageButton buttonImageAttachment = (ImageButton) view.findViewById(R.id.image_chat_attachment);
        ImageButton buttonVideoAttachment = (ImageButton) view.findViewById(R.id.video_chat_attachment);

        buttonDocumentAttachment.setOnClickListener(view13 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/myFolder/");
            intent.setDataAndType(uri, "*/*");
            startActivityForResult(Intent.createChooser(intent, "Open folder"), PICK_DOCUMENT_ID);
            includeAttachment.setVisibility(View.GONE);
        });

        buttonImageAttachment.setOnClickListener(view12 -> {
            Intent chooseImageIntent = ImagePicker.getPickImageIntent(ApplicationLoader.applicationContext, "Выберите");//TODO:string
            startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            includeAttachment.setVisibility(View.GONE);
        });

        buttonVideoAttachment.setOnClickListener(view14 -> {
            Intent chooseVideoIntent = new Intent(Intent.ACTION_PICK);
            chooseVideoIntent.setType("video/*");
            startActivityForResult(chooseVideoIntent, PICK_VIDEO_ID);
            includeAttachment.setVisibility(View.GONE);
        });

        buttonAttachment.setOnClickListener((view1) -> {
            if (includeAttachment.getVisibility() == View.GONE) {
                includeAttachment.setVisibility(View.VISIBLE);
            } else {
                includeAttachment.setVisibility(View.GONE);
            }
        });

        View defaultChatToolbarView;
        if (isGroup)
            defaultChatToolbarView = createChatGroupCustomView();
        else
            defaultChatToolbarView = createChatCustomView();

        toolbarContainer.addView(defaultChatToolbarView);

        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(emoticonsButton).build(messageInput);

        emoticonsButton.setOnClickListener((view1)->{
            emojiPopup.toggle();
        });

        initChat();


        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.chatAddMessages);
        MessagesManager.getInstance().currentChatID = chatID;
        Utils.hideBottomBar(getActivity());
        MessagesManager.getInstance().loadMessages(chatID, 15, 0, isGroup);
    }

    @Override
    public void onPause() {
        super.onPause();
//        messagesRecyclerView.setAdapter(null);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.chatAddMessages);
        MessagesManager.getInstance().currentChatID = 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_ID:
                    Bitmap bitmap = ImagePicker.getImageFromResult(ApplicationLoader.applicationContext, requestCode, resultCode, data);
                    //TODO:Работа с картинками
                    break;
                case PICK_DOCUMENT_ID:
                    //TODO:Работа с документами
                    break;
                case PICK_VIDEO_ID:
                    //TODO:Работа с видео
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initChat() {
        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        messagesAdapter = new MessagesAdapter(iMessageClickListener, isGroup, createSelectedCustomView());
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
                messageRequest.to_id = isGroup ? new RPC.PM_peerGroup(chatID) : new RPC.PM_peerUser(chatID);
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
                    ApplicationLoader.applicationHandler.post(() -> {
                        messagesAdapter.notifyDataSetChanged();
                        messagesRecyclerView.smoothScrollToPosition(messagesRecyclerView.getAdapter().getItemCount() - 1);
                    });

                    //TODO:сделать, чтобы если сообщение не дошло, предлагало переотправить
                    NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.dialogsNeedReload, chatID);
                });
            });

            messageInput.setText("");
        });
    }

    private View createSelectedCustomView() {
        return getActivity().getLayoutInflater().inflate(R.layout.toolbar_selected_messages, null);
    }

    private View createChatCustomView() {
        final View customView = getLayoutInflater().inflate(R.layout.toolbar_chat, null);
        final TextView chatTitleTextView = (TextView) customView.findViewById(R.id.toolbar_title);
        final CircularImageView toolbarAvatar = (CircularImageView) customView.findViewById(R.id.toolbar_avatar);
        final ImageView backToolbar = (ImageView) customView.findViewById(R.id.toolbar_back_btn);

        backToolbar.setOnClickListener(view -> getActivity().getSupportFragmentManager().popBackStack());

        final RPC.UserObject user = UsersManager.getInstance().users.get(chatID);
        if (user != null) {
            chatTitleTextView.setText(Utils.formatUserName(user));
            if (!user.photoURL.url.isEmpty())
                Utils.loadPhoto(user.photoURL.url, toolbarAvatar);
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

    @SuppressLint("DefaultLocale")
    private View createChatGroupCustomView() {
        final View customView = getLayoutInflater().inflate(R.layout.toolbar_chat_group, null);
        final TextView chatTitleTextView = (TextView) customView.findViewById(R.id.toolbar_title);
        final TextView participantsCountTextView = (TextView) customView.findViewById(R.id.participants_count);
        final CircularImageView toolbarAvatar = (CircularImageView) customView.findViewById(R.id.chat_group_avatar);
        final ImageView backToolbar = (ImageView) customView.findViewById(R.id.toolbar_back_btn);

        backToolbar.setOnClickListener(view -> getActivity().getSupportFragmentManager().popBackStack());

        final RPC.Group group = GroupsManager.getInstance().groups.get(chatID);
        if (group != null) {
            chatTitleTextView.setText(group.title);
            participantsCountTextView.setText(String.format("%s: %d", getString(R.string.participants), groupUsers.size()));
            if (!group.photoURL.url.isEmpty())
                Utils.loadPhoto(group.photoURL.url, toolbarAvatar);
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

            messagesAdapter.messageIDs.addAll( messages);

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

    private MessagesAdapter.IMessageClickListener iMessageClickListener = new MessagesAdapter.IMessageClickListener() {
        @Override
        public void longClick() {

        }

        @Override
        public void forward(LinkedList<Long> checkedMessageIDs) {

        }

        @Override
        public void delete(LinkedList<Long> checkedMessageIDs) {
            for (long msgid : checkedMessageIDs) {
                if (MessagesManager.getInstance().messages.get(msgid).from_id != User.currentUser.id) {
                    Toast.makeText(getContext(), R.string.you_can_not_delete_someone_messages, Toast.LENGTH_SHORT).show();
                    messagesAdapter.deselectAll();
                    return;
                }
            }

            final boolean[] checkPermission = {false};
            final String[] text = {getString(R.string.delete_for_everyone)};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMultiChoiceItems(text, checkPermission, (dialogInterface, which, isChecked) -> checkPermission[which] = isChecked)
                    .setTitle(ApplicationLoader.applicationContext.getString(R.string.want_delete_message))
                    .setCancelable(false)
                    .setNegativeButton(getContext().getString(R.string.button_cancel), (dialogInterface, i) -> {
                    })
                    .setPositiveButton(getContext().getString(R.string.button_ok), (dialogInterface, i) -> {
                        if (checkPermission[0]) {
                            Packet request;
                            if (!isGroup) {
                                request = new RPC.PM_deleteDialogMessages();
                                ((RPC.PM_deleteDialogMessages) request).messageIDs.addAll(checkedMessageIDs);
                            } else {
                                request = new RPC.PM_deleteGroupMessages();
                                ((RPC.PM_deleteGroupMessages) request).messageIDs.addAll(checkedMessageIDs);
                            }

                            NetworkManager.getInstance().sendRequest(request, (response, error) -> {
                                if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                                    ApplicationLoader.applicationHandler.post(() -> Toast.makeText(getContext(), R.string.unable_to_delete_messages, Toast.LENGTH_SHORT).show());
                                    return;
                                }

                                if (response instanceof RPC.PM_boolTrue) {
                                    ApplicationLoader.applicationHandler.post(() -> {
                                        for (Long msgID : checkedMessageIDs) {
                                            RPC.Message msg = MessagesManager.getInstance().messages.get(msgID);
                                            MessagesManager.getInstance().deleteMessage(msg);
                                            messagesAdapter.messageIDs.remove(msgID);
                                        }
                                        messagesAdapter.notifyDataSetChanged();
                                    });
                                }
                            });

                            messagesAdapter.deselectAll();
                        } else {
                            //TODO:delete message for user
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };
}
