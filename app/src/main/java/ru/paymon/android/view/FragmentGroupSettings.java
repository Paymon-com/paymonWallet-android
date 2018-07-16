package ru.paymon.android.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.adapters.GroupSettingsAdapter;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.data.CreateGroupItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.Packet;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class FragmentGroupSettings extends Fragment {
    private int chatID;
    private DialogProgress dialogProgress;
    private boolean isCreator;

    private Button addParticipants;
    private RecyclerView contactsList;
    private EditText titleView;
    private CircleImageView photoView;
    private RPC.Group group;
    private GroupSettingsAdapter adapter;
    private LinkedList<CreateGroupItem> list = new LinkedList<>();
    private static FragmentGroupSettings instance;

    public static synchronized FragmentGroupSettings newInstance() {
        instance = new FragmentGroupSettings();
        return instance;
    }

    public static synchronized FragmentGroupSettings getInstance() {
        if (instance == null)
            instance = new FragmentGroupSettings();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey("chat_id")) {
            chatID = bundle.getInt("chat_id");
        }

        int creatorID = group.creatorID;
        isCreator = (creatorID == User.currentUser.id);
        group = GroupsManager.getInstance().groups.get(chatID);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_settings, container, false);

        titleView = (EditText) view.findViewById(R.id.group_settings_title);
        contactsList = (RecyclerView) view.findViewById(R.id.group_settings_participants_rv);
        photoView = (CircleImageView) view.findViewById(R.id.group_settings_photo);

        photoView.setPhoto(group.photo);
        titleView.setText(group.title);
        titleView.setOnEditorActionListener((textView, i, keyEvent) -> {
            Utils.netQueue.postRunnable(() -> {
                ApplicationLoader.applicationHandler.post(dialogProgress::show);

                if (i == IME_ACTION_DONE) {
                    String title = titleView.getText().toString();
                    RPC.PM_group_setSettings setSettings = new RPC.PM_group_setSettings();
                    setSettings.id = chatID;
                    setSettings.title = title;
                    final long requestID = NetworkManager.getInstance().sendRequest(setSettings, (response, error) -> {

                        if (response != null) {
                            GroupsManager.getInstance().groups.get(chatID).title = title;
                        }

                        if (error != null || response == null) {
                            ApplicationLoader.applicationHandler.post(() -> {
                                if (dialogProgress != null && dialogProgress.isShowing())
                                    dialogProgress.cancel();
                                Toast toast = Toast.makeText(getContext(),
                                        "У вас ошибка", Toast.LENGTH_SHORT);//TODO string
                                toast.show();
                            });
                            return;
                        }

                        ApplicationLoader.applicationHandler.post(() -> {
                            if (dialogProgress != null && dialogProgress.isShowing())
                                dialogProgress.dismiss();
                        });

                    });

                    ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
                }
            });
            return true;
        });

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        contactsList.setLayoutManager(llm);

        addParticipants = (Button) view.findViewById(R.id.group_settings_add);
        addParticipants.setOnClickListener(view1 -> {
            final Bundle bundle = new Bundle();
            bundle.putInt("chat_id", chatID);
            final FragmentGroupAddParticipants fragmentGroupAddParticipants = new FragmentGroupAddParticipants();
            fragmentGroupAddParticipants.setArguments(bundle);
            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            Utils.replaceFragmentWithAnimationSlideFade(fragmentManager, fragmentGroupAddParticipants, null);
        });

        adapter = new GroupSettingsAdapter(list);
        contactsList.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setActionBarWithTitle(getActivity(), getString(R.string.group_settings));
        Utils.hideBottomBar(getActivity());
        Utils.setArrowBackInToolbar(getActivity());

        list.clear();
        ArrayList<RPC.UserObject> users = GroupsManager.getInstance().groupsUsers.get(chatID);
        for (RPC.UserObject user : users) {
            RPC.PM_photo photo = new RPC.PM_photo();
            photo.id = user.photoID;
            photo.user_id = user.id;
            list.add(new CreateGroupItem(user.id, Utils.formatUserName(user), photo));
        }
    }
}
