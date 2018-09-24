package ru.paymon.android.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import ru.paymon.android.Config;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.adapters.AdministratorsAdapter;
import ru.paymon.android.adapters.BlackListAdapter;
import ru.paymon.android.adapters.GroupSettingsAdapter;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.ImagePicker;
import ru.paymon.android.utils.Utils;

import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;

public class FragmentGroupSettings extends Fragment {
    private int chatID;
    private DialogProgress dialogProgress;
    boolean isCreator;
    private EditText titleView;
    private RPC.Group group;
    private LinkedList<UserItem> list = new LinkedList<>();
    private LinkedList<UserItem> listAlertDialogBlackList = new LinkedList<>();
    private LinkedList<UserItem> listAlertDialogAdministrators = new LinkedList<>();
    private static FragmentGroupSettings instance;
    private CircularImageView photoView;

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
        photoView = (CircularImageView) view.findViewById(R.id.group_settings_photo);

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);

        photoView.setOnClickListener((v -> {
//            ((MainActivity) getActivity()).requestAppPermissions(new String[]{
//                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                            Manifest.permission.CAMERA},
//                    R.string.msg_permissions_required, CAMERA_PERMISSIONS);
//            Intent chooseImageIntent = ImagePicker.getPickImageIntent(ApplicationLoader.applicationContext, "выберите");//TODO:string
//            startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
        }));

        if (!group.photoURL.url.isEmpty())
            Utils.loadPhoto(group.photoURL.url, photoView);

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
            BlackListAdapter adapter = new BlackListAdapter(listAlertDialogBlackList, group.id, group.creatorID, dialogProgress);
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
            AdministratorsAdapter adapter = new AdministratorsAdapter(listAlertDialogAdministrators, group.id, group.creatorID, dialogProgress);
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
        Utils.hideBottomBar(getActivity());

        list.clear();
        ArrayList<RPC.UserObject> users = GroupsManager.getInstance().groupsUsers.get(chatID);
        for (RPC.UserObject user : users) {
            list.add(new UserItem(user.id, Utils.formatUserName(user), user.photoURL));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Utils.netQueue.postRunnable(() -> {
                ApplicationLoader.applicationHandler.post(dialogProgress::show);

                final String imagePath = ImagePicker.getImagePathFromResult(ApplicationLoader.applicationContext, 234, resultCode, data);
                RPC.PM_group_setPhoto group_setPhoto = new RPC.PM_group_setPhoto(group.id);

                final long requestID = NetworkManager.getInstance().sendRequest(group_setPhoto, (response, error) -> {
                    if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                        ApplicationLoader.applicationHandler.post(() -> {
                            if (dialogProgress != null && dialogProgress.isShowing())
                                dialogProgress.cancel();

                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext())
                                    .setMessage(R.string.photo_upload_failed) //TODO:string
                                    .setCancelable(true);
                            android.support.v7.app.AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        });
                        return;
                    }

                    if (response instanceof RPC.PM_boolTrue) {
                        FileManager.getInstance().startUploading(imagePath, new FileManager.IUploadingFile() {
                            @Override
                            public void onFinish() {
                                Log.e(Config.TAG, "Group photoURL successfully uploaded");
                                ApplicationLoader.applicationHandler.post(() -> {
                                    if (dialogProgress != null && dialogProgress.isShowing())
                                        dialogProgress.dismiss();
                                    if(!group.photoURL.url.isEmpty())
                                        Utils.loadPhoto(group.photoURL.url, photoView);
                                });
                            }

                            @Override
                            public void onProgress(int percent) {

                            }

                            @Override
                            public void onError(int code) {
                                Log.e(Config.TAG, "Error while uploading group photoURL, error code: " + code);
                                ApplicationLoader.applicationHandler.post(() -> {
                                    if (dialogProgress != null && dialogProgress.isShowing())
                                        dialogProgress.cancel();
                                });
                            }
                        });
                    }

                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.dismiss();
                    });
                });

                ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
            });
        }
    }
}
