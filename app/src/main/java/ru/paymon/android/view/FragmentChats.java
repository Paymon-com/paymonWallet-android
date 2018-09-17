package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
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

public class FragmentChats extends Fragment implements NotificationManager.IListener, SwipeRefreshLayout.OnRefreshListener {
    private static FragmentChats instance;
    private ChatsAdapter chatsAdapter, dialogsAdapter, groupsAdapter;
    private boolean isLoading;
    private TextView toolbarTitle;
    private RecyclerView chatsRecyclerView, chatsAllRecyclerView, groupsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static synchronized FragmentChats getInstance() {
        if (instance == null)
            instance = new FragmentChats();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatsAdapter = new ChatsAdapter(iChatsClickListener);
        dialogsAdapter = new ChatsAdapter(iChatsClickListener);
        groupsAdapter = new ChatsAdapter(iChatsClickListener);
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
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        ImageButton toolbarCreateGroup = toolbar.findViewById(R.id.toolbar_create_group_btn);
        ImageButton toolbarSearch = toolbar.findViewById(R.id.toolbar_search_btn);
        chatsRecyclerView = pageDialogs.findViewById(R.id.fragment_dialog_recycler_view);
        chatsAllRecyclerView = pageAll.findViewById(R.id.fragment_dialog_recycler_view);
        groupsRecyclerView = pageGroups.findViewById(R.id.fragment_dialog_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.fragment_chats_swipe_layout);

        swipeRefreshLayout.setOnRefreshListener(this);

        toolbarCreateGroup.setOnClickListener(view1 -> Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentCreateGroup.newInstance(), null));

        toolbarSearch.setOnClickListener(view12 -> {
            Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentSearch.newInstance(), null);
        });

        List<View> pages = Arrays.asList(pageDialogs, pageAll, pageGroups);

        SlidingChatsAdapter pagerAdapter = new SlidingChatsAdapter(pages);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private int mCurrentPosition;
            private int mScrollState;

            @Override
            public void onPageSelected(final int position) {
                if (position == 0) {
                    toolbarTitle.setText(R.string.twosome);
                } else if (position == 1) {
                    toolbarTitle.setText(R.string.title_chats);
                } else {
                    toolbarTitle.setText(R.string.groups);
                }
                mCurrentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                handleScrollState(state);
                mScrollState = state;
            }

            private void handleScrollState(final int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    setNextItemIfNeeded();
                }
            }

            private void setNextItemIfNeeded() {
                if (!isScrollStateSettling()) {
                    handleSetNextItem();
                }
            }

            private boolean isScrollStateSettling() {
                return mScrollState == ViewPager.SCROLL_STATE_SETTLING;
            }

            private void handleSetNextItem() {
                final int lastPosition = viewPager.getAdapter().getCount() - 1;
                if (mCurrentPosition == 0) {
                    viewPager.setCurrentItem(lastPosition, false);
                } else if (mCurrentPosition == lastPosition) {
                    viewPager.setCurrentItem(0, false);
                }
            }

            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            }
        });

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);

        chatsAllRecyclerView.setHasFixedSize(true);
        chatsAllRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsAllRecyclerView.setAdapter(chatsAdapter);

        chatsRecyclerView.setHasFixedSize(true);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsRecyclerView.setAdapter(dialogsAdapter);

        groupsRecyclerView.setHasFixedSize(true);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsRecyclerView.setAdapter(groupsAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.showBottomBar(getActivity());
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.dialogsNeedReload);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didDisconnectedFromTheServer);
        MessagesManager.getInstance().currentChatID = 0;
        if (NetworkManager.getInstance().isAuthorized()) {
            if (!isLoading) {
                isLoading = true;
                MessagesManager.getInstance().loadChats(!NetworkManager.getInstance().isConnected());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        chatsAllRecyclerView.setAdapter(null);
//        chatsRecyclerView.setAdapter(null);
//        groupsRecyclerView.setAdapter(null);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.dialogsNeedReload);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didDisconnectedFromTheServer);
    }

    private ChatsAdapter.IChatsClickListener iChatsClickListener = (chatID, isGroup) -> {
        final Bundle bundle = new Bundle();

        if (isGroup)
            bundle.putParcelableArrayList("users", GroupsManager.getInstance().groupsUsers.get(chatID));

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
                        dialogsItems.add(new ChatsItem(user.id, user.photoURL, username, lastMessageText, lastMsg.date, lastMsg.itemType));
                    }
                }


                LinkedList<ChatsItem> groupItems = new LinkedList<>();
                for (int i = 0; i < GroupsManager.getInstance().groups.size(); i++) {
                    RPC.Group group = GroupsManager.getInstance().groups.get(GroupsManager.getInstance().groups.keyAt(i));
                    String title = group.title;
                    String lastMessageText = "";

                    Long lastMsgId = MessagesManager.getInstance().lastGroupMessages.get(group.id);
                    if (lastMsgId != null) {
                        RPC.PM_photoURL lastMsgPhoto = null;
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
                            }

                            RPC.UserObject user = UsersManager.getInstance().users.get(lastMessage.from_id);
                            if (user != null)
                                lastMsgPhoto = user.photoURL;

                            groupItems.add(new ChatsGroupItem(group.id, group.photoURL, lastMsgPhoto, title, lastMessageText, lastMessage.date, lastMessage.itemType));
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
//                    ApplicationLoader.applicationHandler.post(() -> {
//                        if (hintView != null) hintView.setVisibility(View.VISIBLE);
//                    });
                }
                isLoading = false;

                ApplicationLoader.applicationHandler.post(()->swipeRefreshLayout.setRefreshing(false));
            } else if (id == NotificationManager.NotificationEvent.didDisconnectedFromTheServer) {
                View connectingView = createConnectingCustomView();//TODO:выключение такого тулбара, когда сного появиться связь с сервером
//                ApplicationLoader.applicationHandler.post(() ->  Utils.setActionBarWithCustomView(getActivity(), connectingView)  textToolAll.setText("Connection")  toolbar.setText("Connecting"));
            } else if (id == NotificationManager.NotificationEvent.didEstablishedSecuredConnection) {
                ApplicationLoader.applicationHandler.post(() -> toolbarTitle.setText(R.string.title_chats));
            }
        });
    }

    private View createConnectingCustomView() {
        final View customView = getLayoutInflater().inflate(R.layout.toolbar_connecting, null);
        final ConstraintLayout pointLayout = (ConstraintLayout) customView.findViewById(R.id.connecting_anim_layout);

        Animation rotateAnimation = new RotateAnimation(0, 360, 50f, 50f);
        rotateAnimation.setDuration(3000);

        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rotateAnimation.reset();
                pointLayout.startAnimation(rotateAnimation);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        pointLayout.startAnimation(rotateAnimation);

        return customView;
    }

    @Override
    public void onRefresh() {
        if (NetworkManager.getInstance().isAuthorized()) {
            if (!isLoading) {
                isLoading = true;
                MessagesManager.getInstance().loadChats(!NetworkManager.getInstance().isConnected());
            }
        }
    }
}
