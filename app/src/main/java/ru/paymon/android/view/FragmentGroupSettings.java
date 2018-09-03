package ru.paymon.android.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.adapters.AlertDialogCustomAdministratorsAdapter;
import ru.paymon.android.adapters.AlertDialogCustomBlackListAdapter;
import ru.paymon.android.adapters.GroupSettingsAdapter;
import ru.paymon.android.models.AlertDialogCustomAdministratorsItem;
import ru.paymon.android.models.AlertDialogCustomBlackListItem;
import ru.paymon.android.models.CreateGroupItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class FragmentGroupSettings extends Fragment {
    private int chatID;
    private DialogProgress dialogProgress;
    boolean isCreator;
    private EditText titleView;
    private RPC.Group group;
    private LinkedList<CreateGroupItem> list = new LinkedList<>();
    private LinkedList<AlertDialogCustomBlackListItem> listAlertDialogBlackList = new LinkedList<>();
    private LinkedList<AlertDialogCustomAdministratorsItem> listAlertDialogAdministrators = new LinkedList<>();
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

            group = GroupsManager.getInstance().groups.get(chatID);
            int creatorID = group.creatorID;
            isCreator = (creatorID == User.currentUser.id);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_settings, container, false);

        ImageView backToolbar = (ImageView) view.findViewById(R.id.toolbar_back_btn);

        backToolbar.setOnClickListener(view1 -> getActivity().getSupportFragmentManager().popBackStack());

        titleView = (EditText) view.findViewById(R.id.group_settings_title);
        RecyclerView contactsList = (RecyclerView) view.findViewById(R.id.group_settings_participants_rv);
        CircularImageView photoView = (CircularImageView) view.findViewById(R.id.group_settings_photo);

        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(true);

//        photoView.setPhoto(group.photo);
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
                                        getString(R.string.enter_group_title), Toast.LENGTH_SHORT);//TODO string
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

        Button addParticipants = (Button) view.findViewById(R.id.group_settings_add);
        addParticipants.setOnClickListener(view1 -> {
            final Bundle bundle = new Bundle();
            bundle.putInt("chat_id", chatID);
            final FragmentGroupAddParticipants fragmentGroupAddParticipants = new FragmentGroupAddParticipants();
            fragmentGroupAddParticipants.setArguments(bundle);
            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            Utils.replaceFragmentWithAnimationSlideFade(fragmentManager, fragmentGroupAddParticipants, null);
        });

        Button blackListButton = (Button) view.findViewById(R.id.group_settings_black_list);
        blackListButton.setOnClickListener((view1) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.black_list);//TODO:string
            view1 = getLayoutInflater().inflate(R.layout.alert_dialog_custom_black_list, null);
            builder.setView(view1);
            builder.setPositiveButton(R.string.button_add, (dialogInterface, i) -> {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Utils.replaceFragmentWithAnimationSlideFade(fragmentManager, new FragmentGroupAddBlackList(), null);
            });
            RecyclerView blackList = (RecyclerView) view1.findViewById(R.id.alert_dialog_custom_black_list_rv);
            AlertDialogCustomBlackListAdapter adapter = new AlertDialogCustomBlackListAdapter(listAlertDialogBlackList, group.id, group.creatorID, dialogProgress);
            blackList.setLayoutManager(new LinearLayoutManager(getContext()));
            blackList.setAdapter(adapter);
            builder.setCancelable(true);
            builder.show();
        });

        Button adminListButton = (Button) view.findViewById(R.id.group_settings_administrators);
        adminListButton.setOnClickListener(view12 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.administrators);//TODO:string
            view12 = getLayoutInflater().inflate(R.layout.alert_dialog_custom_administrators, null);
            builder.setView(view12);
            builder.setPositiveButton(R.string.button_add, (dialogInterface, i) -> {
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Utils.replaceFragmentWithAnimationSlideFade(fragmentManager, new FragmentGroupAddAdministrators(), null);
            });
            RecyclerView adminsList = (RecyclerView) view12.findViewById(R.id.alert_dialog_custom_administrators_rv);
            AlertDialogCustomAdministratorsAdapter adapter = new AlertDialogCustomAdministratorsAdapter(listAlertDialogAdministrators, group.id, group.creatorID, dialogProgress);
            adminsList.setLayoutManager(new LinearLayoutManager(getContext()));
            adminsList.setAdapter(adapter);
            builder.setCancelable(true);
            builder.show();
        });

        Button leaveGroup = (Button) view.findViewById(R.id.group_settings_leave_group);
        leaveGroup.setOnClickListener(view13 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.leave_group).setMessage(R.string.are_you_sure).setPositiveButton(R.string.yes, (dialogInterface, i) -> {

            }).setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel());
            builder.create().show();
        });

        GroupSettingsAdapter adapter = new GroupSettingsAdapter(list, group.id, group.creatorID, dialogProgress);
        contactsList.setAdapter(adapter);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Utils.setActionBarWithTitle(getActivity(), getString(R.string.group_settings));
        Utils.hideBottomBar(getActivity());
        //Utils.setArrowBackInToolbar(getActivity());

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
