package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.adapters.MessagesAdapter;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.net.RPC;


public class FragmentChat extends Fragment {
    private int chatID;
    private RecyclerView messageRecyclerView;
    private MessagesAdapter messagesAdapter;
//    private EmojiconEditText messageInput;
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

//        messageInput = (EmojiconEditText) view.findViewById(R.id.input_edit_text);
        messageRecyclerView = view.findViewById(R.id.chat_recview);

        final ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowCustomEnabled(false);
            supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);

            View defaultCustomView;
            if (isGroup)
                defaultCustomView = createChatGroupCustomView();
            else
                defaultCustomView = createChatCustomView();

            supportActionBar.setCustomView(defaultCustomView);
            supportActionBar.setDisplayShowCustomEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setDisplayHomeAsUpEnabled(true);

            initChat(defaultCustomView);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void initChat(View defaultCustomView) {
        messageRecyclerView.setHasFixedSize(true);
        messageRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        messageRecyclerView.setLayoutManager(linearLayoutManager);

        messagesAdapter = new MessagesAdapter((AppCompatActivity) getActivity(), isGroup, chatID, defaultCustomView);
        messageRecyclerView.setAdapter(messagesAdapter);

        messageRecyclerView
                .addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        if (!loadingMessages && (linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) && messagesAdapter.getItemCount() != 0) {
                            loadingMessages = true;
//                            MessageManager.getInstance().loadMessages(chatID, 15, messagesAdapter.messageIDs.size(), isGroup);
                        }
                    }
                });
    }

    private View createChatCustomView() {
        final View customView = getLayoutInflater().inflate(R.layout.chat_action_bar, null);
        final TextView chatTitleTextView = customView.findViewById(R.id.chat_title);
        final CircleImageView toolbarAvatar = customView.findViewById(R.id.chat_avatar);

//        final RPC.UserObject user = MessageManager.getInstance().users.get(chatID);//TODO: отделить действия с юзерами в отдельный менеджер
//        if (user != null) {
//            chatTitleTextView.setText(Utils.formatUserName(user));
//            toolbarAvatar.setPhoto(user.id, user.photoID);
//        }

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
        final TextView chatTitleTextView = customView.findViewById(R.id.chat_title);
        final TextView participantsCountTextView = customView.findViewById(R.id.participants_count);
        final CircleImageView toolbarAvatar = customView.findViewById(R.id.chat_group_avatar);

//        final RPC.Group group = MessageManager.getInstance().groups.get(chatID);//TODO: отделить действия с группами в отдельный менеджер
//        if (group != null) {
//            chatTitleTextView.setText(group.title);
//            toolbarAvatar.setPhoto(group.id, group.photo.id);
//            participantsCountTextView.setText(getString(R.string.participants) + ": " + groupUsers.size());
//        }

        customView.setOnClickListener(v -> {
//            final Bundle bundle = new Bundle();
//            bundle.putInt("chat_id", chatID);
//            final FragmentGroupSettings fragmentGroupSettings = FragmentGroupSettings.newInstance();
//            fragmentGroupSettings.setArguments(bundle);
//            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//            Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentGroupSettings, null);
        });

        return customView;
    }
}
