package ru.paymon.android.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.util.ArrayList;

import androidx.navigation.Navigation;
import androidx.recyclerview.selection.SelectionTracker;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.components.CircularImageView;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.ChatViewModel;

import static ru.paymon.android.net.RPC.Message.MESSAGE_FLAG_FROM_ID;

//import ru.paymon.android.utils.ImagePicker;

public abstract class AbsFragmentChat extends Fragment {
    public static final String CHAT_ID_KEY = "CHAT_ID_KEY";
    public static final String CHAT_GROUP_USERS = "CHAT_USERS";
    public final int PICK_IMAGE_ID = 100;
    public final int PICK_VIDEO_ID = 99;
    public final int PICK_DOCUMENT_ID = 98;

    public RecyclerView messagesRecyclerView;
    public EmojiEditText messageInput;
    public ImageButton sendButton;
    public ImageView emoticonsButton;
//    public ConstraintLayout includeAttachment;
//    public ImageButton buttonAttachment;
//    public ImageButton buttonDocumentAttachment;
//    public ImageButton buttonImageAttachment;
//    public ImageButton buttonVideoAttachment;
    public LinearLayout toolbarContainer;
    public View toolbarView;
    public View toolbarViewSelected;
    public TextView chatTitleTextView;
    public CircularImageView toolbarAvatar;
    public ImageView backToolbar;
    public TextView participantsCountTextView;
    public TextView selectedItemCount;
    public TextView delete;
    public TextView copy;
    public SelectionTracker selectionTracker;
    public ChatViewModel chatViewModel;
    public ArrayList<RPC.UserObject> groupUsers;
    public int chatID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        if (bundle != null) {
            if (bundle.containsKey(CHAT_ID_KEY))
                chatID = bundle.getInt(CHAT_ID_KEY);
//            if (bundle.containsKey(CHAT_GROUP_USERS))
//                groupUsers = bundle.getParcelableArrayList(CHAT_GROUP_USERS);
        }

        if (User.currentUser == null || chatID == 0) return;

        RPC.PM_getChatMessages packet = new RPC.PM_getChatMessages();

        packet.chatID = this instanceof FragmentChat ? new RPC.PM_peerUser(chatID) : new RPC.PM_peerGroup(chatID);
        packet.offset = 0;
        packet.count = 15;

        NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
            if (response == null) return;
            final RPC.PM_chat_messages receivedMessages = (RPC.PM_chat_messages) response;
            if (receivedMessages.messages.size() == 0) return;
            MessagesManager.getInstance().putMessages(receivedMessages.messages);
//            ApplicationLoader.db.chatMessageDao().insertList(receivedMessages.messages);
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageInput = (EmojiEditText) view.findViewById(R.id.input_edit_text);
        messagesRecyclerView = (RecyclerView) view.findViewById(R.id.chat_recview);
        sendButton = (ImageButton) view.findViewById(R.id.sendButton);
        emoticonsButton = (ImageView) view.findViewById(R.id.smilesButton);
//        includeAttachment = (ConstraintLayout) view.findViewById(R.id.fragment_chat_attachment_include);
//        buttonAttachment = (ImageButton) view.findViewById(R.id.attach_button);
//        buttonDocumentAttachment = (ImageButton) view.findViewById(R.id.document_chat_attachment);
//        buttonImageAttachment = (ImageButton) view.findViewById(R.id.image_chat_attachment);
//        buttonVideoAttachment = (ImageButton) view.findViewById(R.id.video_chat_attachment);
        toolbarContainer = (LinearLayout) view.findViewById(R.id.toolbar_container);

        final Bundle bundle = new Bundle();
        bundle.putInt(CHAT_ID_KEY, chatID);
        if (this instanceof FragmentChat) {
            toolbarView = getLayoutInflater().inflate(R.layout.toolbar_chat, null);
            chatTitleTextView = (TextView) toolbarView.findViewById(R.id.toolbar_title);
            toolbarAvatar = (CircularImageView) toolbarView.findViewById(R.id.toolbar_avatar);
            backToolbar = (ImageView) toolbarView.findViewById(R.id.toolbar_back_btn);
            final RPC.UserObject user = UsersManager.getInstance().getUser(chatID);
            if (user != null) {
                chatTitleTextView.setText(Utils.formatUserName(user));
                if (!user.photoURL.url.isEmpty())
                    Utils.loadPhoto(user.photoURL.url, toolbarAvatar);
            }
            toolbarView.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentFriendProfile, bundle));
        } else if (this instanceof FragmentGroupChat) {
            toolbarView = getLayoutInflater().inflate(R.layout.toolbar_chat_group, null);
            participantsCountTextView = (TextView) toolbarView.findViewById(R.id.participants_count);
            chatTitleTextView = (TextView) toolbarView.findViewById(R.id.toolbar_title);
            toolbarAvatar = (CircularImageView) toolbarView.findViewById(R.id.chat_group_avatar);
            backToolbar = (ImageView) toolbarView.findViewById(R.id.toolbar_back_btn);
            final RPC.Group group = GroupsManager.getInstance().getGroup(chatID);
            groupUsers = GroupsManager.getInstance().getGroupUsers(chatID);
            if (group != null) {
                chatTitleTextView.setText(group.title);
                participantsCountTextView.setText(String.format("%s: %d", getString(R.string.participants), ((FragmentGroupChat) this).groupUsers.size()));
                if (!group.photoURL.url.isEmpty())
                    Utils.loadPhoto(group.photoURL.url, toolbarAvatar);
            }
            toolbarView.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentGroupSettings, bundle));
        }

        toolbarViewSelected = getLayoutInflater().inflate(R.layout.toolbar_chat_selected, null);
        selectedItemCount = toolbarViewSelected.findViewById(R.id.toolbar_chat_selected_count);
        delete = toolbarViewSelected.findViewById(R.id.toolbar_chat_selected_delete);
        delete = toolbarViewSelected.findViewById(R.id.toolbar_chat_selected_copy);
        toolbarViewSelected.setVisibility(View.GONE);

        toolbarContainer.addView(toolbarView);
        toolbarContainer.addView(toolbarViewSelected);
        backToolbar.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

//        buttonDocumentAttachment.setOnClickListener(view13 -> {
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/myFolder/");
//            intent.setDataAndType(uri, "*/*");
//            startActivityForResult(Intent.createChooser(intent, "Open folder"), PICK_DOCUMENT_ID);
//            includeAttachment.setVisibility(View.GONE);
//        });

//        buttonImageAttachment.setOnClickListener(view12 -> {
//            Intent chooseImageIntent = ImagePicker.getPickImageIntent(ApplicationLoader.applicationContext, "Выберите");//TODO:string
//            startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
//            includeAttachment.setVisibility(View.GONE);
//        });

//        buttonVideoAttachment.setOnClickListener(view14 -> {
//            Intent chooseVideoIntent = new Intent(Intent.ACTION_PICK);
//            chooseVideoIntent.setType("video/*");
//            startActivityForResult(chooseVideoIntent, PICK_VIDEO_ID);
//            includeAttachment.setVisibility(View.GONE);
//        });
//
//        buttonAttachment.setOnClickListener((view1) -> {
//            if (includeAttachment.getVisibility() == View.GONE) {
//                includeAttachment.setVisibility(View.VISIBLE);
//            } else {
//                includeAttachment.setVisibility(View.GONE);
//            }
//        });
        messagesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                Log.e("AAA", llm.findFirstVisibleItemPosition() + " " + llm.findLastVisibleItemPosition());
//                if(llm == null) return;
//                if(llm.findFirstVisibleItemPosition() == 0){
//                    if (User.currentUser == null || chatID == 0) return;
//
//                    RPC.PM_getChatMessages packet = new RPC.PM_getChatMessages();
//
//                    packet.chatID = AbsFragmentChat.this instanceof FragmentChat ? new RPC.PM_peerUser(chatID) : new RPC.PM_peerGroup(chatID);
//                    packet.offset = 0;
//                    packet.count = 15;
//
//                    NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
//                        if (response == null) return;
//                        final RPC.PM_chat_messages receivedMessages = (RPC.PM_chat_messages) response;
//                        if (receivedMessages.messages.size() == 0) return;
//                        MessagesManager.getInstance().putMessages(receivedMessages.messages);
////            ApplicationLoader.db.chatMessageDao().insertList(receivedMessages.messages);
//                    });
//                }
            }
        });

        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(emoticonsButton).build(messageInput);
        emoticonsButton.setOnClickListener((view1) -> emojiPopup.toggle());

        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        sendButton.setOnClickListener((view1) -> {
            Utils.netQueue.postRunnable(() -> {
                final String messageText = messageInput.getText().toString();

                if (User.currentUser == null || messageText.trim().isEmpty()) return;

                RPC.PM_message messageRequest = new RPC.PM_message();
                messageRequest.id = MessagesManager.generateMessageID();
                messageRequest.text = messageText;
                messageRequest.flags = MESSAGE_FLAG_FROM_ID;
                messageRequest.date = (int) (System.currentTimeMillis() / 1000L);
                messageRequest.from_id = User.currentUser.id;
                messageRequest.to_peer = this instanceof FragmentChat ? new RPC.PM_peerUser(chatID) : new RPC.PM_peerGroup(chatID);
                messageRequest.to_id = chatID;
                messageRequest.unread = true;

                NetworkManager.getInstance().sendRequest(messageRequest, (response, error) -> {
                    if (error != null || response == null)
                        return;     //TODO:сделать, чтобы если сообщение не дошло, предлагало переотправить

                    RPC.PM_updateMessageID updateMsgID = (RPC.PM_updateMessageID) response;
                    messageRequest.id = updateMsgID.newID;
                    MessagesManager.getInstance().putMessage(messageRequest);
                });
            });
            messageInput.setText("");
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
        MessagesManager.getInstance().currentChatID = chatID;
    }

    @Override
    public void onPause() {
        super.onPause();
        MessagesManager.getInstance().currentChatID = 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_ID:
//                    Bitmap bitmap = ImagePicker.getImageFromResult(ApplicationLoader.applicationContext, requestCode, resultCode, data);
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
}
