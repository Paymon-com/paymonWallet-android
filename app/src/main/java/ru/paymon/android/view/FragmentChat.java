package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedList;

import hani.momanii.supernova_emoji_library.helper.EmojiconEditText;
import ru.paymon.android.Config;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.MessagesAdapter;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;


public class FragmentChat extends Fragment implements NotificationManager.IListener {
    private int chatID;
    private RecyclerView messageRecyclerView;
    private MessagesAdapter messagesAdapter;
    private EmojiconEditText messageInput;
    private ArrayList<RPC.UserObject> groupUsers;
    private boolean isGroup;
    private boolean loadingMessages;

    public static synchronized FragmentChat newInstance() {
        return new FragmentChat();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        if (bundle != null) {
            chatID = bundle.getInt("chat_id");
            if (bundle.containsKey("groupUsers")) {
                isGroup = true;
                groupUsers = bundle.getParcelableArrayList("groupUsers");
            }
        }
        MessagesManager.getInstance().currentChatID = chatID;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageInput = (EmojiconEditText) view.findViewById(R.id.input_edit_text);
        messageRecyclerView = (RecyclerView) view.findViewById(R.id.chat_recview);

        View defaultCustomView;
        if (isGroup)
            defaultCustomView = createChatGroupCustomView();
        else
            defaultCustomView = createChatCustomView();

        final ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowCustomEnabled(false);
            supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
            supportActionBar.setCustomView(defaultCustomView);
            supportActionBar.setDisplayShowCustomEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        initChat(defaultCustomView);

        MessagesManager.getInstance().loadMessages(chatID, 15, 0, isGroup);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.chatAddMessages);
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.chatAddMessages);
    }

    private void initChat(View defaultCustomView) {
        messageRecyclerView.setHasFixedSize(true);
        messageRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        messagesAdapter = new MessagesAdapter((AppCompatActivity) getActivity(), isGroup, defaultCustomView);
        messageRecyclerView.setAdapter(messagesAdapter);

        messageRecyclerView
                .addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        if (!loadingMessages && (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) && messagesAdapter.getItemCount() != 0) {
                            loadingMessages = true;
                            Utils.netQueue.postRunnable(() -> MessagesManager.getInstance().loadMessages(chatID, 15, messagesAdapter.messageIDs.size(), isGroup));
                        }
                    }
                });
    }

    private View createChatCustomView() {
        final View customView = getLayoutInflater().inflate(R.layout.chat_action_bar, null);
        final TextView chatTitleTextView = (TextView) customView.findViewById(R.id.chat_title);
        final CircleImageView toolbarAvatar = (CircleImageView) customView.findViewById(R.id.chat_avatar);

        final RPC.UserObject user = UsersManager.getInstance().users.get(chatID);
        if (user != null) {
            chatTitleTextView.setText(Utils.formatUserName(user));
            RPC.PM_photo photo = new RPC.PM_photo();
            photo.id = user.photoID;
            photo.user_id = user.id;
            toolbarAvatar.setPhoto(photo);
        }

        customView.setOnClickListener(v -> {
//            final Bundle bundle = new Bundle();
//            bundle.putInt(Config.KEY_USER_ID, chatID);
//            bundle.putBoolean(Config.KEY_OPEN_FRIEND_PROFILE_FROM_CHAT, true);
//            final FragmentFriendProfile fragmentFriendProfile = FragmentFriendProfile.newInstance();
//            fragmentFriendProfile.setArguments(bundle);
//            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//            Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentFriendProfile, null);
        });

        return customView;
    }

    private View createChatGroupCustomView() {
        final View customView = getLayoutInflater().inflate(R.layout.chat_group_action_bar, null);
        final TextView chatTitleTextView = (TextView) customView.findViewById(R.id.chat_title);
        final TextView participantsCountTextView = (TextView) customView.findViewById(R.id.participants_count);
        final CircleImageView toolbarAvatar = (CircleImageView) customView.findViewById(R.id.chat_group_avatar);

        final RPC.Group group = GroupsManager.getInstance().groups.get(chatID);
        if (group != null) {
            chatTitleTextView.setText(group.title);
            toolbarAvatar.setPhoto(group.photo);
            participantsCountTextView.setText(getString(R.string.participants) + ": " + groupUsers.size());
        }

        customView.setOnClickListener(v -> {
//            final Bundle bundle = new Bundle();
//            bundle.putInt("chat_id", chatID);
//            final FragmentGroupSettings fragmentGroupSettings = FragmentGroupSettings.newInstance();
//            fragmentGroupSettings.setArguments(bundle);
//            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//            Utils.replaceFragmentWithAnimationSlideFade(fragmentManager, fragmentGroupSettings, null);
        });

        return customView;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationManager.chatAddMessages) {
            if (args.length < 1) return;

            LinkedList<Long> messages = (LinkedList<Long>) args[0];
            Boolean onScroll = (Boolean) args[1];

            if (messagesAdapter == null || messagesAdapter.messageIDs == null) return;

            if (messagesAdapter.messageIDs.size() < 1)
                messagesAdapter.messageIDs.addAll(0, messages);
            else
                messagesAdapter.messageIDs.addAll(messages);

            messagesAdapter.notifyDataSetChanged();
            if (!onScroll) {
                if (((LinearLayoutManager) messageRecyclerView.getLayoutManager()).findLastVisibleItemPosition() >= messageRecyclerView.getAdapter().getItemCount() - 2)
                    messageRecyclerView.smoothScrollToPosition(messageRecyclerView.getAdapter().getItemCount() - 1);
            } else {
                if (messagesAdapter.getItemCount() > 0) {
                    int scrolledCount = (int) args[2];
                    messageRecyclerView.scrollToPosition(scrolledCount + ((LinearLayoutManager) messageRecyclerView.getLayoutManager()).findLastVisibleItemPosition());
                }
            }
            loadingMessages = false;
        }
    }
}
