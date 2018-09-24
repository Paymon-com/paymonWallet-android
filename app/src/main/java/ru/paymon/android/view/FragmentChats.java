package ru.paymon.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import androidx.navigation.Navigation;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.adapters.ChatsAdapter;
import ru.paymon.android.adapters.SlidingChatsAdapter;
import ru.paymon.android.components.ChatsViewPager;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.ChatsViewModel;
import ru.paymon.android.viewmodels.MainViewModel;

import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

public class FragmentChats extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static FragmentChats instance;
    private ChatsAdapter chatsAdapter, dialogsAdapter, groupsAdapter;
    private ChatsViewModel chatsViewModel;
    private MainViewModel mainViewModel;
    private LiveData<Boolean> showProgress;
    private LiveData<LinkedList<ChatsItem>> chatsItemsData;
    private LiveData<LinkedList<ChatsItem>> groupItemsData;
    private LiveData<LinkedList<ChatsItem>> dialogsItemsData;
    private LiveData<Boolean> isAuthorized;
    private LiveData<Boolean> isNetworkConnected;
    private LiveData<Boolean> isServerConnected;


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
        chatsViewModel = ViewModelProviders.of(getActivity()).get(ChatsViewModel.class);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        showProgress = chatsViewModel.getProgressState();
        chatsViewModel.updateChatsData();
        chatsItemsData = chatsViewModel.getChatsData();
        dialogsItemsData = chatsViewModel.getDialogsData();
        groupItemsData = chatsViewModel.getGroupsData();
        isAuthorized = mainViewModel.getAuthorizationState();
        isNetworkConnected = mainViewModel.getNetworkConnectionState();
        isServerConnected = mainViewModel.getServerConnectionState();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        ChatsViewPager viewPager = view.findViewById(R.id.fragment_chats_view_pager);
        View pageDialogs = inflater.inflate(R.layout.fragment_chats_page, container, false);
        View pageAll = inflater.inflate(R.layout.fragment_chats_page, container, false);
        View pageGroups = inflater.inflate(R.layout.fragment_chats_page, container, false);
        View toolbar = view.findViewById(R.id.toolbar);
//        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        ImageButton toolbarCreateGroup = toolbar.findViewById(R.id.toolbar_create_group_btn);
        ImageButton toolbarSearch = toolbar.findViewById(R.id.toolbar_search_btn);
        RecyclerView chatsRecyclerView = pageDialogs.findViewById(R.id.fragment_dialog_recycler_view);
        RecyclerView chatsAllRecyclerView = pageAll.findViewById(R.id.fragment_dialog_recycler_view);
        RecyclerView groupsRecyclerView = pageGroups.findViewById(R.id.fragment_dialog_recycler_view);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.fragment_chats_swipe_layout);

        swipeRefreshLayout.setOnRefreshListener(this);

        showProgress.observe(getActivity(), show -> {
            if (show == null) return;
            swipeRefreshLayout.setRefreshing(show);
        });

        Observer<Boolean> stateObserver = (state) -> {
            if (isAuthorized.getValue() != null && isAuthorized.getValue()
                    && isNetworkConnected.getValue() != null && isNetworkConnected.getValue()
                    && isServerConnected.getValue() != null && isServerConnected.getValue()) {
                chatsViewModel.updateChatsData();
            }
        };

        isAuthorized.observe(getActivity(), stateObserver);
        isNetworkConnected.observe(getActivity(), stateObserver);
        isServerConnected.observe(getActivity(), stateObserver);

        chatsItemsData.observe(getActivity(), data -> {
            if (data == null) return;
            chatsAdapter.chatsItemsList.clear();
            chatsAdapter.chatsItemsList.addAll(data);
            chatsAdapter.notifyDataSetChanged();
        });

        groupItemsData.observe(getActivity(), data -> {
            if (data == null) return;
            groupsAdapter.chatsItemsList.clear();
            groupsAdapter.chatsItemsList.addAll(data);
            groupsAdapter.notifyDataSetChanged();
        });

        dialogsItemsData.observe(getActivity(), data -> {
            if (data == null) return;
            dialogsAdapter.chatsItemsList.clear();
            dialogsAdapter.chatsItemsList.addAll(data);
            dialogsAdapter.notifyDataSetChanged();
        });

        toolbarCreateGroup.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentCreateGroup));
        toolbarSearch.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentSearch));

        List<View> pages = Arrays.asList(pageDialogs, pageAll, pageGroups);
        SlidingChatsAdapter pagerAdapter = new SlidingChatsAdapter(pages);

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
        MessagesManager.getInstance().currentChatID = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private ChatsAdapter.IChatsClickListener iChatsClickListener = (chatID, isGroup) -> {
        final Bundle bundle = new Bundle();
        if (isGroup)
            bundle.putParcelableArrayList("users", GroupsManager.getInstance().groupsUsers.get(chatID));
        bundle.putInt(CHAT_ID_KEY, chatID);
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentChat, bundle);
    };

    @Override
    public void onRefresh() {
        chatsViewModel.updateChatsData();
    }


//    private View createConnectingCustomView() {
//        final View customView = getLayoutInflater().inflate(R.layout.toolbar_connecting, null);
//        final ConstraintLayout pointLayout = (ConstraintLayout) customView.findViewById(R.id.connecting_anim_layout);
//
//        Animation rotateAnimation = new RotateAnimation(0, 360, 50f, 50f);
//        rotateAnimation.setDuration(3000);
//
//        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                rotateAnimation.reset();
//                pointLayout.startAnimation(rotateAnimation);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        pointLayout.startAnimation(rotateAnimation);
//
//        return customView;
//    }
}
