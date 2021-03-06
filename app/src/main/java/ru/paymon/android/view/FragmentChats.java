package ru.paymon.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.navigation.Navigation;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.adapters.ChatsAdapter;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.pagedlib.ChatDiffUtilCallback;
import ru.paymon.android.utils.ItemClickSupport;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.ChatsViewModel;
import ru.paymon.android.viewmodels.MainViewModel;

import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

public class FragmentChats extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private ChatsAdapter chatsAdapter;
    private ChatsViewModel chatsViewModel;
    private MainViewModel mainViewModel;
    private RecyclerView chatsAllRecyclerView;
    private LiveData<PagedList<ChatsItem>> allChatsItemLiveData;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText search;
    private int sortedBy = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatsViewModel = ViewModelProviders.of(getActivity()).get(ChatsViewModel.class);
        mainViewModel = ViewModelProviders.of(getActivity()).get(MainViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        ImageButton createGroupButton = view.findViewById(R.id.create_group_image_button);
        SwitchCompat switchConversations = (SwitchCompat) view.findViewById(R.id.switchConversation);
        SwitchCompat switchGroups = (SwitchCompat) view.findViewById(R.id.switchGroup);
        search = view.findViewById(R.id.editText);
        chatsAllRecyclerView = view.findViewById(R.id.fragment_dialog_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.fragment_chats_swipe_layout);

        createGroupButton.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentCreateGroup));
        swipeRefreshLayout.setOnRefreshListener(this);

        chatsAllRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        chatsAllRecyclerView.setLayoutManager(linearLayoutManager);

        ItemClickSupport.addTo(chatsAllRecyclerView).setOnItemClickListener((recyclerView, position, v) -> {
            if (chatsAdapter.getCurrentList() == null) return;
            ChatsItem chatsItem = chatsAdapter.getCurrentList().get(position);
            if (chatsItem == null) return;
            final Bundle bundle = new Bundle();
            bundle.putInt(CHAT_ID_KEY, chatsItem.chatID);
            if (chatsItem.isGroup) {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentGroupChat, bundle);
            } else {
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentChat, bundle);
            }
        });

        chatsAdapter = new ChatsAdapter(new ChatDiffUtilCallback());
        chatsAllRecyclerView.setAdapter(chatsAdapter);

        switchConversations.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked && !switchGroups.isChecked())
                sortChats(0, false);
            else if ((isChecked && switchGroups.isChecked()) || (!isChecked && !switchGroups.isChecked()))
                sortChats(2, false);
            else if (!isChecked && switchGroups.isChecked())
                sortChats(1, false);
        });

        switchGroups.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked && !switchConversations.isChecked())
                sortChats(1, false);
            else if ((isChecked && switchConversations.isChecked()) || (!isChecked && !switchConversations.isChecked()))
                sortChats(2, false);
            else if (!isChecked && switchConversations.isChecked())
                sortChats(0, false);
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable search) {
                chatsViewModel.searchText = search.toString();
                chatsViewModel.isSearchActivated = !search.toString().isEmpty();
                sortChats(chatsViewModel.sortedBy, true);
            }
        });

        chatsAllRecyclerView.setItemAnimator(null);
        int resId = R.anim.layout_animation_fall_down;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getContext(), resId);
        chatsAllRecyclerView.setLayoutAnimation(animation);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sortChats(chatsViewModel.sortedBy, false);

        chatsViewModel.getProgressState().observe(getActivity(), show -> {
            if (show == null) return;
            swipeRefreshLayout.setRefreshing(show);
        });

        mainViewModel.getAuthorizationState().observe(getActivity(), (state) -> {
            Utils.netQueue.postRunnable(() -> {
                if (state)
                    chatsViewModel.updateChatsData();
            }, 100);
        });

        search.setText(chatsViewModel.searchText);

        ((LinearLayoutManager) chatsAllRecyclerView.getLayoutManager()).scrollToPosition(chatsViewModel.chatsScrollY);
    }


    @Override
    public void onResume() {
        super.onResume();
        Utils.showBottomBar(getActivity());
        MessagesManager.getInstance().currentChatID = 0;
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
        onRefresh();
    }

    @Override
    public void onPause() {
        chatsViewModel.chatsScrollY = ((LinearLayoutManager) chatsAllRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        sortedBy = -1;
        super.onPause();
    }

    @Override
    public void onRefresh() {
        chatsViewModel.updateChatsData();
    }

    private void sortChats(int sortBy, boolean isSearch) {
        if (!isSearch && sortedBy == sortBy) return;

        if (allChatsItemLiveData != null)
            allChatsItemLiveData.removeObservers(getActivity());

        switch (sortBy) {
            case 0:
                if (!chatsViewModel.isSearchActivated)
                    allChatsItemLiveData = chatsViewModel.getDialogsChats();
                else
                    allChatsItemLiveData = chatsViewModel.getDialogsChatsBySearch(chatsViewModel.searchText);
                break;
            case 1:
                if (!chatsViewModel.isSearchActivated)
                    allChatsItemLiveData = chatsViewModel.getGroupChats();
                else
                    allChatsItemLiveData = chatsViewModel.getGroupsChatsBySearch(chatsViewModel.searchText);
                break;
            default:
                if (!chatsViewModel.isSearchActivated)
                    allChatsItemLiveData = chatsViewModel.getChats();
                else
                    allChatsItemLiveData = chatsViewModel.getChatsBySearch(chatsViewModel.searchText);
                break;
        }

        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.item_animation_fall_down);
        animation.setDuration(500);
        chatsAllRecyclerView.startAnimation(animation);

        allChatsItemLiveData.observe(getActivity(), pagedList -> {
            chatsAdapter.submitList(pagedList);
        });

        sortedBy = sortBy;
        chatsViewModel.sortedBy = sortBy;
    }
}
