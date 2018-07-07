package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.LinkedList;

import ru.paymon.android.GroupsManager;
import ru.paymon.android.MediaManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.ChatsAdapter;
import ru.paymon.android.models.ChatsGroupItem;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.RecyclerItemClickListener;
import ru.paymon.android.utils.Utils;

public class FragmentChats extends Fragment implements NotificationManager.IListener {
    private static FragmentChats instance;
    private ChatsAdapter chatsAdapter;
    private RecyclerView chatsRecyclerView;
    private ProgressBar progressBar;
    private TextView hintView;
    private boolean isLoading;
    private LinkedList<ChatsItem> chatsItemsList = new LinkedList<>();


    public static synchronized FragmentChats newInstance() {
        instance = new FragmentChats();
        return instance;
    }

    public static synchronized FragmentChats getInstance() {
        if (instance == null)
            instance = new FragmentChats();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_dialog_recycler_view);
        progressBar = (ProgressBar) view.findViewById(R.id.chats_progress_bar);
        progressBar.setVisibility(View.GONE);

        initChats();

        getActivity().invalidateOptionsMenu();
        setHasOptionsMenu(true);

        return view;
    }

    private void initChats() {
        chatsRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getContext(), chatsRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        final ChatsItem chatsItem = chatsAdapter.getItem(position);
                        boolean isGroup = chatsItem instanceof ChatsGroupItem;
                        int chatID = chatsItem.chatID;

                        final Bundle bundle = new Bundle();
                        bundle.putInt("chat_id", chatID);

                        if (isGroup)
                            bundle.putParcelableArrayList("groupUsers", GroupsManager.getInstance().groupsUsers.get(chatID));

                        final FragmentChat fragmentChat = FragmentChat.newInstance();
                        fragmentChat.setArguments(bundle);
                        Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), fragmentChat, null);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                    }
                })
        );

        chatsAdapter = new ChatsAdapter(chatsItemsList);
        chatsRecyclerView.setHasFixedSize(true);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsRecyclerView.setAdapter(chatsAdapter);

        if (NetworkManager.getInstance().isAuthorized()) { //TODO: мб в apphandler + netQueue
            if (!isLoading) {
                isLoading = true;
                if (hintView != null)
                    hintView.setVisibility(View.GONE);

                Utils.netQueue.postRunnable(() -> MessagesManager.getInstance().loadChats(!NetworkManager.getInstance().isConnected()));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_dialog));
        NotificationManager.getInstance().addObserver(this, NotificationManager.dialogsNeedReload);
//        MessageManager.getInstance().currentChatID = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
        NotificationManager.getInstance().removeObserver(this, NotificationManager.dialogsNeedReload);
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationManager.dialogsNeedReload) {
            if (progressBar != null)
                progressBar.setVisibility(View.GONE);

            LinkedList<ChatsItem> array = new LinkedList<>();
            for (int i = 0; i < UsersManager.getInstance().userContacts.size(); i++) {
                RPC.UserObject user = UsersManager.getInstance().userContacts.get(UsersManager.getInstance().userContacts.keyAt(i));
                String username = Utils.formatUserName(user);
                String lastMessageText = "";

                RPC.Message lastMsg = MessagesManager.getInstance().messages.get(MessagesManager.getInstance().lastMessages.get(user.id));
                if (lastMsg != null) {
                    if (lastMsg instanceof RPC.PM_message) {
                        lastMessageText = lastMsg.text;
                    } else if (lastMsg instanceof RPC.PM_messageItem) {
                        switch (lastMsg.itemType) {
                            case STICKER:
                                lastMessageText = getString(R.string.sticker);
                                break;

                            case ACTION:
                                lastMessageText = lastMsg.text;
                                break;
                        }
                    }

                    RPC.PM_photo photo = new RPC.PM_photo();
                    photo.id = user.photoID;
                    photo.user_id = user.id;
                    array.add(new ChatsItem(user.id, photo, username, lastMessageText, lastMsg.date, lastMsg.itemType));
                }
            }

            for (int i = 0; i < GroupsManager.getInstance().groups.size(); i++) {
                RPC.Group group = GroupsManager.getInstance().groups.get(GroupsManager.getInstance().groups.keyAt(i));
                String title = group.title;
                String lastMessageText = "";

                Long lastMsgId = MessagesManager.getInstance().lastGroupMessages.get(group.id);
                if (lastMsgId != null) {
                    RPC.PM_photo lastMsgPhoto = null;
                    RPC.Message lastMessage = MessagesManager.getInstance().messages.get(lastMsgId);
                    if (lastMessage != null) {
                        if (lastMessage instanceof RPC.PM_message) {
                            lastMessageText = lastMessage.text;
                        } else if (lastMessage instanceof RPC.PM_messageItem) {
                            switch (lastMessage.itemType) {
                                case STICKER:
                                    lastMessageText = getString(R.string.sticker);
                                    break;

                                case ACTION:
                                    lastMessageText = lastMessage.text;
                                    break;
                            }

                            RPC.UserObject user = UsersManager.getInstance().users.get(lastMessage.from_id);
                            if (user != null) {
                                lastMsgPhoto = new RPC.PM_photo();
                                lastMsgPhoto.user_id = user.id;
                                lastMsgPhoto.id = user.photoID;
                            }
                        }

                        RPC.PM_photo photo = group.photo;
                        if (photo.id == 0) {
                            photo.id = MediaManager.getInstance().generatePhotoID();
                        }
                        if (photo.user_id == 0) {
                            photo.user_id = -group.id;
                        }
                        array.add(new ChatsGroupItem(group.id, photo, lastMsgPhoto, title, lastMessageText, lastMessage.date, lastMessage.itemType));
                    }
                }
            }

            if (!array.isEmpty()) {
                Collections.sort(array, (chatItem1, chatItem2) -> Long.compare(chatItem1.time, chatItem2.time) * -1);
                chatsAdapter.chatsItemsList.clear();
                chatsAdapter.chatsItemsList.addAll(array);
                chatsAdapter.notifyDataSetChanged();
            } else {
                if (hintView != null) {
                    hintView.setVisibility(View.VISIBLE);
                }
            }

//            if (swipeRefreshLayout != null) {
//                swipeRefreshLayout.setRefreshing(false);
//            }

            isLoading = false;
        } else if (id == NotificationManager.didDisconnectedFromTheServer) {
            isLoading = false;
//            if (swipeRefreshLayout != null) {
//                swipeRefreshLayout.setRefreshing(false);
//            }
        }
    }
}
