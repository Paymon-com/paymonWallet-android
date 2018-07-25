package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.CreateGroupAdapter;
import ru.paymon.android.models.CreateGroupItem;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class FragmentCreateGroup extends Fragment {
    public LinkedList<CreateGroupItem> createGroupItemList = new LinkedList<>();
    private static FragmentCreateGroup instance;
    private CreateGroupAdapter adapter;


    public static synchronized FragmentCreateGroup newInstance(){
        instance = new FragmentCreateGroup();
        return instance;
    }

    public static synchronized FragmentCreateGroup getInstance(){
        if (instance == null)
            instance = new FragmentCreateGroup();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Utils.setActionBarWithTitle(getActivity(), getString(R.string.create_group));
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_group, container, false);

        RecyclerView contactsRecView = (RecyclerView) view.findViewById(R.id.fragment_create_group_rv);
        contactsRecView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        contactsRecView.setLayoutManager(llm);

        EditText editText =(EditText) view.findViewById(R.id.edit_text_create_chats);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                LinkedList<CreateGroupItem> sortedUserList = new LinkedList<>();

                String text = editable.toString();

                if(text.trim().isEmpty()) return;

                for (CreateGroupItem user : createGroupItemList) {
                    if(user.name.toLowerCase().contains(text.toLowerCase())){
                        sortedUserList.add(user);
                    }
                }

                adapter = new CreateGroupAdapter(sortedUserList);
                contactsRecView.setAdapter(adapter);
            }
        });

        SparseArray<RPC.UserObject> userContacts = UsersManager.getInstance().userContacts;

        for (int i = 0; i < userContacts.size(); i++) {
            RPC.UserObject user = userContacts.get(userContacts.keyAt(i));
            RPC.PM_photo photo = new RPC.PM_photo();
            photo.id = user.photoID;
            photo.user_id = user.id;
            CreateGroupItem createGroupItem = new CreateGroupItem(user.id, Utils.formatUserName(user), photo);
            if (createGroupItemList.contains(createGroupItem) || user.id == User.currentUser.id) continue;
            createGroupItemList.add(createGroupItem);
        }

        adapter = new CreateGroupAdapter(createGroupItemList);
        contactsRecView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
        Utils.setActionBarWithTitle(getActivity(), "Создание группы");//TODO:String
        Utils.setArrowBackInToolbar(getActivity());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.create_group_menu, menu);

        MenuItem createGroupButton = menu.findItem(R.id.create_group_done_item);
        createGroupButton.setOnMenuItemClickListener(menuItem -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("create_group_list", createGroupItemList);
            DialogFragmentCreateGroup dialogFragmentCreateGroup = DialogFragmentCreateGroup.newInstance();
            dialogFragmentCreateGroup.setArguments(bundle);
            dialogFragmentCreateGroup.show(getActivity().getSupportFragmentManager(), null);
            return true;
        });
    }
}
