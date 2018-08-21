package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MediaManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.ChatsAdapter;
import ru.paymon.android.adapters.SlidingChatsAdapter;
import ru.paymon.android.models.ChatsGroupItem;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

public class FragmentChats extends Fragment implements NotificationManager.IListener{
    private static FragmentChats instance;
    private ChatsAdapter chatsAdapter, dialogsAdapter, groupsAdapter;
    private boolean isLoading;

    public static synchronized FragmentChats getInstance() {
        if (instance == null)
            instance = new FragmentChats();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        ViewPager viewPager = view.findViewById(R.id.fragment_chats_view_pager);
        View pageDialogs = inflater.inflate(R.layout.fragment_chats_page, container, false);
        View pageAll = inflater.inflate(R.layout.fragment_chats_page, container, false);
        View pageGroups = inflater.inflate(R.layout.fragment_chats_page, container, false);
        View toolbar = view.findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        ImageButton toolbarCreateGroup = toolbar.findViewById(R.id.toolbar_create_group_btn);
        ImageButton toolbarSearch = toolbar.findViewById(R.id.toolbar_search_btn);
        RecyclerView chatsRecyclerView = pageDialogs.findViewById(R.id.fragment_dialog_recycler_view);
        RecyclerView chatsAllRecyclerView = pageAll.findViewById(R.id.fragment_dialog_recycler_view);
        RecyclerView groupsRecyclerView = pageGroups.findViewById(R.id.fragment_dialog_recycler_view);

        List<View> pages = Arrays.asList(pageDialogs, pageAll, pageGroups);

        SlidingChatsAdapter pagerAdapter = new SlidingChatsAdapter(pages);

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);

        chatsAdapter = new ChatsAdapter(iChatsClickListener);
        chatsAllRecyclerView.setHasFixedSize(true);
        chatsAllRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsAllRecyclerView.setAdapter(chatsAdapter);

        dialogsAdapter = new ChatsAdapter(iChatsClickListener);
        chatsRecyclerView.setHasFixedSize(true);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsRecyclerView.setAdapter(dialogsAdapter);

        groupsAdapter = new ChatsAdapter(iChatsClickListener);
        groupsRecyclerView.setHasFixedSize(true);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsRecyclerView.setAdapter(groupsAdapter);

        if (NetworkManager.getInstance().isAuthorized()) {
            if (!isLoading) {
                isLoading = true;
                MessagesManager.getInstance().loadChats(!NetworkManager.getInstance().isConnected());
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.dialogsNeedReload);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didDisconnectedFromTheServer);
        MessagesManager.getInstance().currentChatID = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.dialogsNeedReload);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didDisconnectedFromTheServer);
    }

    private ChatsAdapter.IChatsClickListener iChatsClickListener = (chatID, isGroup) -> {
        final Bundle bundle = new Bundle();

        if (isGroup)
            bundle.putParcelableArrayList("users", GroupsManager.getInstance().groupsUsers.get(chatID));

//        if (forwardMessages != null && forwardMessages.size() > 0)
//            bundle.putSerializable(FORWARD_MESSAGES_KEY, forwardMessages);

        bundle.putInt(CHAT_ID_KEY, chatID);
        final FragmentChat fragmentChat = FragmentChat.newInstance();
        fragmentChat.setArguments(bundle);
        Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), fragmentChat, null);
    };

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent id, Object... args) {
        Utils.stageQueue.postRunnable(() -> {
            if (id == NotificationManager.NotificationEvent.dialogsNeedReload) {
                LinkedList<ChatsItem> dialogsItems = new LinkedList<>();
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
                        dialogsItems.add(new ChatsItem(user.id, photo, username, lastMessageText, lastMsg.date, lastMsg.itemType));
                    }
                }


                LinkedList<ChatsItem> groupItems = new LinkedList<>();
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
                            if (photo.id == 0)
                                photo.id = MediaManager.getInstance().generatePhotoID();

                            if (photo.user_id == 0)
                                photo.user_id = -group.id;
                            groupItems.add(new ChatsGroupItem(group.id, photo, lastMsgPhoto, title, lastMessageText, lastMessage.date, lastMessage.itemType));
                        }
                    }
                }

                if (!dialogsItems.isEmpty()) {
                    Collections.sort(dialogsItems, (chatItem1, chatItem2) -> Long.compare(chatItem1.time, chatItem2.time) * -1);
                    dialogsAdapter.chatsItemsList = dialogsItems;
                    ApplicationLoader.applicationHandler.post(() -> dialogsAdapter.notifyDataSetChanged());
                } else {
                }

                if (!groupItems.isEmpty()) {
                    Collections.sort(groupItems, (chatItem1, chatItem2) -> Long.compare(chatItem1.time, chatItem2.time) * -1);
                    groupsAdapter.chatsItemsList = groupItems;
                    ApplicationLoader.applicationHandler.post(() -> groupsAdapter.notifyDataSetChanged());
                } else {
                }

                if (!dialogsItems.isEmpty() || !groupItems.isEmpty()) {
                    LinkedList<ChatsItem> chatsItems = new LinkedList<>();
                    chatsItems.addAll(dialogsItems);
                    chatsItems.addAll(groupItems);
                    Collections.sort(chatsItems, (chatItem1, chatItem2) -> Long.compare(chatItem1.time, chatItem2.time) * -1);
                    chatsAdapter.chatsItemsList = chatsItems;
                    ApplicationLoader.applicationHandler.post(() -> chatsAdapter.notifyDataSetChanged());
                } else {
                }
                isLoading = false;
            } else if (id == NotificationManager.NotificationEvent.didDisconnectedFromTheServer) {
//                View connectingView = createConnectingCustomView();//TODO:выключение такого тулбара, когда сного появиться связь с сервером
//                ApplicationLoader.applicationHandler.post(() ->  Utils.setActionBarWithCustomView(getActivity(), connectingView)  textToolAll.setText("Connection")  toolbar.setText("Connecting"));
            } else if (id == NotificationManager.NotificationEvent.didEstablishedSecuredConnection) {
//                ApplicationLoader.applicationHandler.post(() -> toolbarTitle.setText("Чаты"));
            }
        });
    }
}
