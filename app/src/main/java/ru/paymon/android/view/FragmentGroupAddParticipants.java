package ru.paymon.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.adapters.CreateGroupAdapter;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.ChatsViewModel;

import static ru.paymon.android.view.AbsFragmentChat.CHAT_ID_KEY;

public class FragmentGroupAddParticipants extends Fragment {
    private int chatID;
    private RPC.Group group;
    private ArrayList<UserItem> addGroupList = new ArrayList<>();
    private CreateGroupAdapter adapter;
    private DialogProgress dialogProgress;
    private ChatsViewModel chatsViewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatsViewModel = ViewModelProviders.of(getActivity()).get(ChatsViewModel.class);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(CHAT_ID_KEY)) {
            chatID = bundle.getInt(CHAT_ID_KEY);
        }

        group = ApplicationLoader.db.groupDao().getById(chatID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_add_participants, container, false);

        ImageButton backToolbar = (ImageButton) view.findViewById(R.id.toolbar_back_btn);
        Button acceptButton = (Button) view.findViewById(R.id.toolbar_next_btn);

        backToolbar.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

        LiveData<PagedList<ChatsItem>> liveChatsItems = chatsViewModel.getDialogsChats();
        liveChatsItems.observe(this, chatsItems -> {
            if (chatsItems == null) return;

            addGroupList = new ArrayList<>();
            for (ChatsItem chatItem : chatsItems) {
                int id = chatItem.chatID;
                RPC.UserObject user = ApplicationLoader.db.userDao().getById(id);
                if (group.users.contains(user)) continue;
                addGroupList.add(new UserItem(user.id, Utils.formatUserName(user), user.photoURL));
            }
            adapter.list.clear();
            adapter.list.addAll(addGroupList);
            adapter.notifyDataSetChanged();
        });

        acceptButton.setOnClickListener(v -> Utils.netQueue.postRunnable(() -> {
            ApplicationLoader.applicationHandler.post(dialogProgress::show);

            RPC.PM_group_addParticipants addParticipantsRequest = new RPC.PM_group_addParticipants();
            addParticipantsRequest.id = chatID;
            for (UserItem createGroupItem : adapter.list) {
                if (createGroupItem.checked)
                    addParticipantsRequest.userIDs.add(createGroupItem.uid);
            }

            final long requestID = NetworkManager.getInstance().sendRequest(addParticipantsRequest, (response, error) -> {
                if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.cancel();
                        Toast toast = Toast.makeText(getContext(),
                                getString(R.string.you_did_not_choose_anyone), Toast.LENGTH_SHORT);//TODO sting
                        toast.show();
                    });
                    return;
                }

                for (Integer uid : addParticipantsRequest.userIDs) {
                    RPC.UserObject user = ApplicationLoader.db.userDao().getById(uid);
                    ArrayList<RPC.UserObject> userObjects = ApplicationLoader.db.groupDao().getById(chatID).users;
                    if (user != null) {
                        userObjects.add(user);
                    }
                }

                ApplicationLoader.applicationHandler.post(() -> {
                    if (dialogProgress != null && dialogProgress.isShowing())
                        dialogProgress.dismiss();
                    dialogProgress.dismiss();
                    Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack();
                });
            });
            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
        }));

        RecyclerView contactsList = (RecyclerView) view.findViewById(R.id.fragment_add_participants_rv);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        contactsList.setLayoutManager(llm);
        contactsList.setHasFixedSize(true);

        adapter = new CreateGroupAdapter(addGroupList);
        contactsList.setAdapter(adapter);

        EditText editText = (EditText) view.findViewById(R.id.edit_text_add_participants);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();

                if (text.isEmpty()) {
                    for (UserItem user : adapter.list)
                        user.isHidden = false;
                    adapter.notifyDataSetChanged();
                    return;
                }

                if (text.trim().isEmpty()) return;

                for (UserItem user : adapter.list)
                    user.isHidden = !user.name.toLowerCase().contains(text.toLowerCase());

                adapter.notifyDataSetChanged();
            }
        });

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
    }
}
