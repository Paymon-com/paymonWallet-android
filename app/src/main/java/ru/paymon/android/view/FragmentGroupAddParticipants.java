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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.CreateGroupAdapter;
import ru.paymon.android.models.CreateGroupItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class FragmentGroupAddParticipants extends Fragment {
    private static FragmentGroupAddParticipants instance;
    private int chatID;
    private RPC.Group group;
    private LinkedList<CreateGroupItem> addGroupList = new LinkedList<>();
    private CreateGroupAdapter adapter;
    private DialogProgress dialogProgress;

    public static synchronized FragmentGroupAddParticipants newInstance() {
        instance = new FragmentGroupAddParticipants();
        return instance;
    }

    public static synchronized FragmentGroupAddParticipants getInstance() {
        if (instance == null)
            instance = new FragmentGroupAddParticipants();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("chat_id")) {
            chatID = bundle.getInt("chat_id");
        }

        group = GroupsManager.getInstance().groups.get(chatID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_add_participants, container, false);

        RecyclerView contactsList = (RecyclerView) view.findViewById(R.id.fragment_add_participants_rv);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        contactsList.setLayoutManager(llm);

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
                LinkedList<CreateGroupItem> sortedUserList = new LinkedList<>();

                String text = editable.toString();

                if(text.trim().isEmpty()) return;

                for (CreateGroupItem user : addGroupList) {
                    if(user.name.toLowerCase().contains(text.toLowerCase())){
                        sortedUserList.add(user);
                    }
                }

                adapter = new CreateGroupAdapter(sortedUserList);
                contactsList.setAdapter(adapter);
            }
        });


        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(true);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setActionBarWithTitle(getActivity(), getString(R.string.add_participants));
        Utils.setArrowBackInToolbar(getActivity());
        Utils.hideBottomBar(getActivity());

        addGroupList.clear();
        SparseArray<RPC.UserObject> userContacts = UsersManager.getInstance().userContacts;

        for (int i = 0; i < userContacts.size(); i++) {
            RPC.UserObject user = userContacts.get(userContacts.keyAt(i));
            if (group.users.contains(user) || user.id == User.currentUser.id) continue;
            RPC.PM_photo photo = new RPC.PM_photo();
            photo.id = user.photoID;
            photo.user_id = user.id;
            addGroupList.add(new CreateGroupItem(user.id, Utils.formatUserName(user), photo));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.create_group_menu, menu);

        MenuItem addParticipantsButton = menu.findItem(R.id.create_group_done_item);

        addParticipantsButton.setOnMenuItemClickListener(menuItem -> {
            Utils.netQueue.postRunnable(() -> {
                ApplicationLoader.applicationHandler.post(dialogProgress::show);

                RPC.PM_group_addParticipants addParticipantsRequest = new RPC.PM_group_addParticipants();
                addParticipantsRequest.id = chatID;
                for (CreateGroupItem createGroupItem : addGroupList) {
                    if (createGroupItem.checked) {
                        addParticipantsRequest.userIDs.add(createGroupItem.uid);
                    }
                }

                final long requestID = NetworkManager.getInstance().sendRequest(addParticipantsRequest, (response, error) -> {
                    if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                        ApplicationLoader.applicationHandler.post(() -> {
                            if (dialogProgress != null && dialogProgress.isShowing())
                                dialogProgress.cancel();
                            Toast toast = Toast.makeText(getContext(),
                                    "Вы никого не выбрали", Toast.LENGTH_SHORT);//TODO sting
                            toast.show();
                        });
                        return;
                    }

                    for (Integer uid : addParticipantsRequest.userIDs) {
                        RPC.UserObject user = UsersManager.getInstance().users.get(uid);
                        ArrayList<RPC.UserObject> userObjects = GroupsManager.getInstance().groupsUsers.get(chatID);
                        if (user != null) {
                            userObjects.add(user);
                        }
                    }

                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.dismiss();dialogProgress.dismiss();
                        getActivity().getSupportFragmentManager().popBackStack();
                    });
                });
                ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
            });

            return false;
        });
    }
}
