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
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.CreateGroupAdapter;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.AbsFragmentChat.CHAT_ID_KEY;

public class FragmentGroupAddParticipants extends Fragment {
    private static FragmentGroupAddParticipants instance;
    private int chatID;
    private RPC.Group group;
    private ArrayList<UserItem> addGroupList = new ArrayList<>();
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
        if (bundle != null && bundle.containsKey(CHAT_ID_KEY)) {
            chatID = bundle.getInt(CHAT_ID_KEY);
        }

        group = GroupsManager.getInstance().groups.get(chatID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_add_participants, container, false);

        ImageButton backToolbar = (ImageButton) view.findViewById(R.id.toolbar_back_btn);
        ImageButton acceptToolbar = (ImageButton) view.findViewById(R.id.toolbar_next_btn);

        backToolbar.setOnClickListener(v-> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

        acceptToolbar.setOnClickListener(view12 -> Utils.netQueue.postRunnable(() -> {
            ApplicationLoader.applicationHandler.post(dialogProgress::show);

            RPC.PM_group_addParticipants addParticipantsRequest = new RPC.PM_group_addParticipants();
            addParticipantsRequest.id = chatID;
            for (UserItem createGroupItem : addGroupList) {
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
                                getString(R.string.you_did_not_choose_anyone), Toast.LENGTH_SHORT);//TODO sting
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
                    Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack();
                });
            });
            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
        }));

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
                ArrayList<UserItem> sortedUserList = new ArrayList<>();

                String text = editable.toString();

                if(text.trim().isEmpty()) return;

                for (UserItem user : addGroupList) {
                    if(user.name.toLowerCase().contains(text.toLowerCase())){
                        sortedUserList.add(user);
                    }
                }

                adapter = new CreateGroupAdapter(sortedUserList);
                contactsList.setAdapter(adapter);
            }
        });


        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());

        addGroupList.clear();
        SparseArray<RPC.UserObject> userContacts = UsersManager.getInstance().userContacts;

        for (int i = 0; i < userContacts.size(); i++) {
            RPC.UserObject user = userContacts.get(userContacts.keyAt(i));
            if (group.users.contains(user) || user.id == User.currentUser.id) continue;
            addGroupList.add(new UserItem(user.id, Utils.formatUserName(user), user.photoURL));
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
                for (UserItem createGroupItem : addGroupList) {
                    if (createGroupItem.checked) {
                        addParticipantsRequest.userIDs.add(createGroupItem.uid);
                    }
                }

                final long requestID = NetworkManager.getInstance().sendRequest(addParticipantsRequest, (response, error) -> {
                    if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                        ApplicationLoader.applicationHandler.post(() -> {
                            if (dialogProgress != null && dialogProgress.isShowing())
                                dialogProgress.cancel();
                            Toast toast = Toast.makeText(getContext(), getString(R.string.you_did_not_choose_anyone), Toast.LENGTH_SHORT);//TODO sting
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
                        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack();
                    });
                });
                ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
            });

            return false;
        });
    }
}
