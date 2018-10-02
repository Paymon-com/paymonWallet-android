package ru.paymon.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.navigation.Navigation;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.adapters.ChatsAdapter;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.pagedlib.ChatDiffUtilCallback;
import ru.paymon.android.utils.ItemClickSupport;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.ChatsViewModel;
import ru.paymon.android.viewmodels.MainViewModel;

import static ru.paymon.android.view.AbsFragmentChat.CHAT_GROUP_USERS;
import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

public class FragmentChats extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private ChatsAdapter chatsAdapter;
    private ChatsViewModel chatsViewModel;
    private MainViewModel mainViewModel;
    private LiveData<Boolean> showProgress;
    //    private LiveData<LinkedList<ChatsItem>> chatsItemsData;
    private LiveData<Boolean> isAuthorized;
    private RecyclerView chatsAllRecyclerView;
    private LiveData<PagedList<ChatsItem>> allChatsItemLiveData;
    //    private LiveData<PagedList<ChatsItem>> dialogsChatsItemLiveData;
//    private LiveData<PagedList<ChatsItem>> groupsChatsItemLiveData;
        private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView dialogsIndicator;
    private ImageView chatsIndicator;
    private ImageView groupsIndicator;
    private int sortedBy;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatsViewModel = ViewModelProviders.of(getActivity()).get(ChatsViewModel.class);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
        showProgress = chatsViewModel.getProgressState();
//        chatsItemLiveData = chatsViewModel.getChats();
//        dialogsChatsItemLiveData = chatsViewModel.getDialogsChats();
//        groupsChatsItemLiveData = chatsViewModel.getGroupChats();
//        allChatsItemLiveData = chatsViewModel.getChats();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        ImageButton createGroupButton = view.findViewById(R.id.create_group_image_button);
        chatsAllRecyclerView = view.findViewById(R.id.fragment_dialog_recycler_view);
        Button dialogsButton = view.findViewById(R.id.dialogs_button);
        Button chatsButton = view.findViewById(R.id.chats_button);
        Button groupsButton = view.findViewById(R.id.groups_button);
        dialogsIndicator = view.findViewById(R.id.dialogs_image);
        chatsIndicator = view.findViewById(R.id.chats_image);
        groupsIndicator = view.findViewById(R.id.groups_image);
        swipeRefreshLayout = view.findViewById(R.id.fragment_chats_swipe_layout);
        createGroupButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentCreateGroup));
//        toolbarSearch.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentSearch));
        swipeRefreshLayout.setOnRefreshListener(this);

        chatsAllRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        chatsAllRecyclerView.setLayoutManager(linearLayoutManager);
        chatsAllRecyclerView.addItemDecoration(new DividerItemDecoration(chatsAllRecyclerView.getContext(), linearLayoutManager.getOrientation()));

        ItemClickSupport.addTo(chatsAllRecyclerView).setOnItemClickListener((recyclerView, position, v) -> {
            if (chatsAdapter.getCurrentList() == null) return;
            ChatsItem chatsItem = chatsAdapter.getCurrentList().get(position);
            if (chatsItem == null) return;
            final Bundle bundle = new Bundle();
            bundle.putInt(CHAT_ID_KEY, chatsItem.chatID);
            if (chatsItem.isGroup) {
                bundle.putParcelableArrayList(CHAT_GROUP_USERS, GroupsManager.getInstance().groupsUsers.get(chatsItem.chatID));
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentGroupChat, bundle);
            } else {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentChat, bundle);
            }
        });

        chatsAdapter = new ChatsAdapter(new ChatDiffUtilCallback());
        chatsAllRecyclerView.setAdapter(chatsAdapter);

        chatsButton.setOnClickListener(v -> sortChats(2));
        groupsButton.setOnClickListener(v -> sortChats(1));
        dialogsButton.setOnClickListener(v -> sortChats(0));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        showProgress = chatsViewModel.getProgressState();
//        chatsItemsData = chatsViewModel.getChatsData();
//        dialogsItemsData = chatsViewModel.getDialogsData();
//        groupItemsData = chatsViewModel.getGroupsData();
//        isAuthorized = mainViewModel.getAuthorizationState();

        sortChats(chatsViewModel.sortedBy);

//        Observer observer = o -> {
//            if (!(o instanceof PagedList)) return;
//            chatsAdapter.submitList((PagedList<ChatsItem>) o);
//        };

//        allChatsItemLiveData.observe(this, observer);
//        dialogsChatsItemLiveData.observe(this, observer);
//        groupsChatsItemLiveData.observe(this, observer);

        chatsViewModel.getProgressState().observe(getActivity(), show -> {
            if (show == null) return;
            swipeRefreshLayout.setRefreshing(show);
        });

        mainViewModel.getAuthorizationState().observe(getActivity(), (state) -> {
//            if (state == null) return;
//            if (!swipeRefreshLayout.isRefreshing() && state)
            if (!chatsViewModel.isChatsLoaded)
                chatsViewModel.updateChatsData();
        });


        ((LinearLayoutManager) chatsAllRecyclerView.getLayoutManager()).scrollToPosition(chatsViewModel.chatsScrollY);

//        chatsViewModel.getChatsData().observe(getActivity(), data -> {
//            if (data == null) return;
//            chatsAdapter.chatsItemsList.clear();
//            chatsAdapter.chatsItemsList.addAll(data);
//            chatsAdapter.notifyDataSetChanged();
//        });
    }


    @Override
    public void onResume() {
        super.onResume();
        Utils.showBottomBar(getActivity());
        MessagesManager.getInstance().currentChatID = 0;
    }

    @Override
    public void onPause() {
        chatsViewModel.chatsScrollY = ((LinearLayoutManager) chatsAllRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        super.onPause();
    }

    @Override
    public void onRefresh() {
        chatsViewModel.updateChatsData();
    }

    private void sortChats(int sortBy) {
        if (sortedBy == sortBy) return;

        switch (sortBy) {
            case 0:
                chatsIndicator.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                groupsIndicator.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                dialogsIndicator.setBackgroundColor(getResources().getColor(R.color.blue_bright));
                allChatsItemLiveData = chatsViewModel.getDialogsChats();
                break;
            case 1:
                chatsIndicator.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                groupsIndicator.setBackgroundColor(getResources().getColor(R.color.blue_bright));
                dialogsIndicator.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                allChatsItemLiveData = chatsViewModel.getGroupChats();
                break;
            default:
                chatsIndicator.setBackgroundColor(getResources().getColor(R.color.blue_bright));
                groupsIndicator.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                dialogsIndicator.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                allChatsItemLiveData = chatsViewModel.getChats();
                break;
        }

        allChatsItemLiveData.removeObservers(getActivity());
        allChatsItemLiveData.observe(getActivity(), pagedList -> {
            chatsAdapter.submitList(pagedList);
            chatsAdapter.notifyDataSetChanged();
//            chatsAllRecyclerView.setAdapter(chatsAdapter);
        });

        sortedBy = sortBy;
        chatsViewModel.sortedBy = sortBy;
    }
}
