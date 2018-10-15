package ru.paymon.android.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.CreateGroupAdapter;
import ru.paymon.android.models.ChatsItem;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.ChatsViewModel;

import static ru.paymon.android.view.AbsFragmentChat.CHAT_GROUP_USERS;

public class FragmentCreateGroup extends Fragment {
    public ArrayList<UserItem> createGroupItemList = new ArrayList<>();
    private CreateGroupAdapter adapter;
    private ChatsViewModel chatsViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatsViewModel = ViewModelProviders.of(getActivity()).get(ChatsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        Button acceptButton = (Button) view.findViewById(R.id.toolbar_next_btn);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.toolbar_back_btn);
        EditText editText = (EditText) view.findViewById(R.id.edit_text_create_chats);
        RecyclerView contactsRecView = (RecyclerView) view.findViewById(R.id.fragment_create_group_rv);

        backButton.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

        acceptButton.setOnClickListener(v -> {
            ArrayList<UserItem> arrayList = new ArrayList<>();
            for (UserItem user : adapter.list) {
                if (user.checked)
                    arrayList.add(user);
            }
            if (arrayList.size() <= 0) {
                Toast.makeText(getContext(), "Никто не выбран!", Toast.LENGTH_LONG).show();
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putSerializable(CHAT_GROUP_USERS, arrayList);
            DialogFragmentCreateGroup dialogFragmentCreateGroup = DialogFragmentCreateGroup.newInstance();
            dialogFragmentCreateGroup.setArguments(bundle);
            dialogFragmentCreateGroup.show(getActivity().getSupportFragmentManager(), null);
        });

        contactsRecView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        contactsRecView.setLayoutManager(llm);

        SparseArray<RPC.UserObject> userContacts = new SparseArray<>();
        LiveData<PagedList<ChatsItem>> liveChatsItems = chatsViewModel.getDialogsChats();
        liveChatsItems.observe(this, chatsItems -> {
            if (chatsItems == null) return;

            for (ChatsItem chatItem : chatsItems) {
                int id = chatItem.chatID;
                userContacts.append(id, UsersManager.getInstance().getUser(id));
            }

            createGroupItemList = new ArrayList<>();
            for (int i = 0; i < userContacts.size(); i++) {
                RPC.UserObject user = userContacts.get(userContacts.keyAt(i));
                UserItem createGroupItem = new UserItem(user.id, Utils.formatUserName(user), user.photoURL);
                if (createGroupItemList.contains(createGroupItem) || user.id == User.currentUser.id)
                    continue;
                createGroupItemList.add(createGroupItem);
            }

            adapter = new CreateGroupAdapter(createGroupItemList);
            contactsRecView.setAdapter(adapter);
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    for (UserItem user : adapter.list)
                        user.isHidden = false;
                    adapter.notifyDataSetChanged();
                    return;
                }

                String text = editable.toString();

                if (text.trim().isEmpty()) return;

                for (UserItem user : adapter.list) {
                    user.isHidden = !user.name.toLowerCase().contains(text.toLowerCase());
                }

                adapter.notifyDataSetChanged();
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
    }
}
