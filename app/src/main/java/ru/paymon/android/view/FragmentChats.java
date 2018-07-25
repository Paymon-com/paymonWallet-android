package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import com.daimajia.androidanimations.library.fading_entrances.FadeInLeftAnimator;
import com.daimajia.androidviewhover.tools.Util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.DBHelper;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MediaManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.ChatsAdapter;
import ru.paymon.android.adapters.DialogsAdapter;
import ru.paymon.android.adapters.GroupsAdapter;
import ru.paymon.android.adapters.SlidingChatsAdapter;
import ru.paymon.android.models.ChatsGroupItem;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.RecyclerItemClickListener;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

import static ru.paymon.android.adapters.MessagesAdapter.FORWARD_MESSAGES_KEY;

public class FragmentChats extends Fragment implements NotificationManager.IListener {
    private static FragmentChats instance;
    private TextView hintView;
    private boolean isLoading;
    private LinkedList<ChatsItem> chatsItemsList = new LinkedList<>();
    private boolean isForward;
    private LinkedList<Long> forwardMessages;

    private LinkedList<ChatsItem> dialogItemsList = new LinkedList<>();
    private LinkedList<ChatsGroupItem> groupItemsList = new LinkedList<>();
    private ProgressBar progressBarAllChats;
//    private ProgressBar progressBarChats;
//    private ProgressBar progressBarGroups;
    private RecyclerView chatsAllRecyclerView;
    private RecyclerView chatsRecyclerView;
    private RecyclerView groupsRecyclerView;
    private ChatsAdapter chatsAdapter;
    private DialogsAdapter dialogsAdapter;
    private GroupsAdapter groupsAdapter;

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
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(FORWARD_MESSAGES_KEY)) {
                isForward = true;
                forwardMessages = (LinkedList<Long>) bundle.getSerializable(FORWARD_MESSAGES_KEY);
            }
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        List<View> pages = new ArrayList<>();

        View pageChats = inflater.inflate(R.layout.fragment_chats_dialogs, container, false);
        chatsRecyclerView = pageChats.findViewById(R.id.fragment_chats_recycler_view);
//        progressBarChats = pageChats.findViewById(R.id.chats_progress_bar);
//        progressBarChats.setVisibility(View.GONE);
        pages.add(pageChats);

        View pageAll = inflater.inflate(R.layout.fragment_chats, container, false);
        chatsAllRecyclerView = pageAll.findViewById(R.id.fragment_dialog_recycler_view);

        pages.add(pageAll);

        View pageGroups = inflater.inflate(R.layout.fragment_chats_groups, container, false);
        groupsRecyclerView = pageGroups.findViewById(R.id.fragment_group_recycler_view);
//        progressBarGroups = pageGroups.findViewById(R.id.groups_progress_bar);
//        progressBarGroups.setVisibility(View.GONE);
        pages.add(pageGroups);

        progressBarAllChats = pageAll.findViewById(R.id.chats_all_progress_bar);
        progressBarAllChats.setVisibility(View.GONE);

        SlidingChatsAdapter pagerAdapter = new SlidingChatsAdapter(pages);
        ViewPager viewPager = new ViewPager(getContext());

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    Utils.setActionBarWithTitle(getActivity(), "Тет-а-тет");
                else if (position == 1)
                    Utils.setActionBarWithTitle(getActivity(), "Чаты");
                else
                    Utils.setActionBarWithTitle(getActivity(), "Группы");
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);

        initChats();

        getActivity().invalidateOptionsMenu();
        setHasOptionsMenu(true);

        return viewPager;
    }

    private void initChats() {
//        chatsRecyclerView.addOnItemTouchListener(
//                new RecyclerItemClickListener(getContext(), chatsRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(View view, int position) {
//                        final ChatsItem chatsItem = chatsAdapter.getItem(position);
//                        final boolean isGroup = chatsItem instanceof ChatsGroupItem;
//                        final int chatID = chatsItem.chatID;
//
//                        final Bundle bundle = new Bundle();
//                        bundle.putInt("chat_id", chatID);
//
//                        if (isGroup)
//                            bundle.putParcelableArrayList("groupUsers", GroupsManager.getInstance().groupsUsers.get(chatID));
//
//                        if(isForward)
//                            bundle.putSerializable(FORWARD_MESSAGES_KEY, forwardMessages);
//
//                        final FragmentChat fragmentChat = FragmentChat.newInstance();
//                        fragmentChat.setArguments(bundle);
//                        Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), fragmentChat, null);
//                    }
//
//                    @Override
//                    public void onLongItemClick(View view, int position) {
//                    }
//                })
//        );

        chatsAdapter = new ChatsAdapter(chatsItemsList, getActivity(), null);
        chatsAllRecyclerView.setHasFixedSize(true);
        chatsAllRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsAllRecyclerView.setAdapter(chatsAdapter);

        dialogsAdapter = new DialogsAdapter(dialogItemsList);
        chatsRecyclerView.setHasFixedSize(true);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsRecyclerView.setAdapter(dialogsAdapter);

        groupsAdapter = new GroupsAdapter(groupItemsList);
        groupsRecyclerView.setHasFixedSize(true);
        groupsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        groupsRecyclerView.setAdapter(groupsAdapter);

        if (NetworkManager.getInstance().isAuthorized()) {
            if (!isLoading) {
                isLoading = true;
                if (hintView != null)
                    hintView.setVisibility(View.GONE);

                MessagesManager.getInstance().loadChats(!NetworkManager.getInstance().isConnected());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isForward)
            Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_chats));
        else
            Utils.setActionBarWithTitle(getActivity(), "Выберите получателя"); //TODO:string
        Utils.showBottomBar(getActivity());
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.dialogsNeedReload);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didDisconnectedFromTheServer);
        MessagesManager.getInstance().currentChatID = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.dialogsNeedReload);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didDisconnectedFromTheServer);
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent id, Object... args) {
        Utils.stageQueue.postRunnable(() -> {
            if (id == NotificationManager.NotificationEvent.dialogsNeedReload) {
                ApplicationLoader.applicationHandler.post(() ->{
                    if (progressBarAllChats != null)
                        progressBarAllChats.setVisibility(View.GONE);
                });

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
                            if (photo.id == 0)
                                photo.id = MediaManager.getInstance().generatePhotoID();

                            if (photo.user_id == 0)
                                photo.user_id = -group.id;
                            array.add(new ChatsGroupItem(group.id, photo, lastMsgPhoto, title, lastMessageText, lastMessage.date, lastMessage.itemType));
                        }
                    }
                }

                if (!array.isEmpty()) {
                    Collections.sort(array, (chatItem1, chatItem2) -> Long.compare(chatItem1.time, chatItem2.time) * -1);
                    chatsAdapter.chatsItemsList.clear();
                    chatsAdapter.chatsItemsList.addAll(array);
                    ApplicationLoader.applicationHandler.post(() -> {
                        chatsAdapter.notifyDataSetChanged();
                    });
                } else {
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (hintView != null) {
                            hintView.setVisibility(View.VISIBLE);
                        }
                    });
                }

//            if (swipeRefreshLayout != null) {
//                swipeRefreshLayout.setRefreshing(false);
//            }

                isLoading = false;
            } else if (id == NotificationManager.NotificationEvent.didDisconnectedFromTheServer) {
                View connectingView = createConnectingCustomView();//TODO:выключение такого тулбара, когда сного появиться связь с сервером

                ApplicationLoader.applicationHandler.post(()->{
                    Utils.setActionBarWithCustomView(getActivity(), connectingView);
                });

//            isLoading = false;
//            if (swipeRefreshLayout != null) {
//                swipeRefreshLayout.setRefreshing(false);
//            }
            } else if (id == NotificationManager.NotificationEvent.didEstablishedSecuredConnection) {
                ApplicationLoader.applicationHandler.post(()->{
                    Utils.setActionBarWithTitle(getActivity(), "Чаты");
                });
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_chats_menu, menu);

        MenuItem item = menu.findItem(R.id.create_group_item);
        item.setOnMenuItemClickListener(menuItem -> {
            Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentCreateGroup.newInstance(), null);
            return true;
        });

        MenuItem itemSearch = menu.findItem(R.id.search_chat);
        itemSearch.setOnMenuItemClickListener((menuItem) -> {
            final FragmentSearch fragmentSearch = new FragmentSearch();
            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            Utils.replaceFragmentWithAnimationSlideFade(fragmentManager, fragmentSearch, null);
            return true;
        });
    }

    private View createConnectingCustomView() {
        final View customView = getLayoutInflater().inflate(R.layout.chats_connecting_action_bar, null);
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
}
