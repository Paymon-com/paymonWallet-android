package ru.paymon.android.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import ru.paymon.android.components.CircularImageView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.vanniktech.rxpermission.Permission;

import java.util.ArrayList;
import java.util.LinkedList;

import androidx.navigation.Navigation;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.adapters.GroupSettingsAdapter;
import ru.paymon.android.components.DialogProgress;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.FileManager;
import ru.paymon.android.utils.ItemClickSupport;
import ru.paymon.android.utils.Utils;

import static android.app.Activity.RESULT_OK;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static ru.paymon.android.view.AbsFragmentChat.CHAT_ID_KEY;


public class FragmentGroupSettings extends Fragment {
    private int chatID;
    private DialogProgress dialogProgress;
    boolean isCreator;
    private EditText titleView;
    private RPC.Group group;
    private LinkedList<UserItem> list = new LinkedList<>();
    private LinkedList<UserItem> listAlertDialogBlackList = new LinkedList<>();
    private LinkedList<UserItem> listAlertDialogAdministrators = new LinkedList<>();
    private CircularImageView photoView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(CHAT_ID_KEY)) {
            chatID = bundle.getInt(CHAT_ID_KEY);

            group = GroupsManager.getInstance().getGroup(chatID);
            int creatorID = group.creatorID;
            isCreator = (creatorID == User.currentUser.id);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_settings, container, false);

        ImageView backToolbar = (ImageView) view.findViewById(R.id.toolbar_back_btn);

        backToolbar.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

        titleView = (EditText) view.findViewById(R.id.group_settings_title);
        RecyclerView contactsList = (RecyclerView) view.findViewById(R.id.group_settings_participants_rv);
        photoView = (CircularImageView) view.findViewById(R.id.group_settings_photo);

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);

        photoView.setOnClickListener(v -> {
            ApplicationLoader.rxPermission
                    .requestEach(Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Permission>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(Permission permission) {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {
                    if (ApplicationLoader.rxPermission.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE) && ApplicationLoader.rxPermission.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.OFF)
                                .setCropShape(CropImageView.CropShape.OVAL)
                                .setFixAspectRatio(true)
                                .setMinCropWindowSize(Config.minAvatarSize, Config.minAvatarSize)
                                .start(getContext(), FragmentGroupSettings.this);
                    } else {
                        Toast.makeText(getContext(), R.string.insufficient_rights, Toast.LENGTH_LONG).show();
                    }
                }
            });
        });

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
                            RPC.Group group = GroupsManager.getInstance().getGroup(chatID);
                            group.title = title;
                            GroupsManager.getInstance().putGroup(group);
                        }

                        if (error != null || response == null) {
                            ApplicationLoader.applicationHandler.post(() -> {
                                if (dialogProgress != null && dialogProgress.isShowing())
                                    dialogProgress.cancel();
                                Toast toast = Toast.makeText(getContext(),
                                        getString(R.string.enter_group_title), Toast.LENGTH_SHORT);
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
        contactsList.setHasFixedSize(true);
        contactsList.setLayoutManager(llm);

        Button addParticipants = (Button) view.findViewById(R.id.group_settings_add);
        addParticipants.setOnClickListener(view1 -> {
            final Bundle bundle = new Bundle();
            bundle.putInt(CHAT_ID_KEY, chatID);
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.FragmentGroupAddParticipants, bundle);
        });

//        Button blackListButton = (Button) view.findViewById(R.id.group_settings_black_list);
//        blackListButton.setOnClickListener((view1) -> {
//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            builder.setTitle(R.string.black_list);
//            view1 = getLayoutInflater().inflate(R.layout.alert_dialog_custom_black_list, null);
//            builder.setView(view1);
//            builder.setPositiveButton(R.string.button_add, (dialogInterface, i) -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentGroupAddBlackList));
//            RecyclerView blackList = (RecyclerView) view1.findViewById(R.id.alert_dialog_custom_black_list_rv);
//            BlackListAdapter adapter = new BlackListAdapter(listAlertDialogBlackList, group.id, group.creatorID, dialogProgress);
//            blackList.setLayoutManager(new LinearLayoutManager(getContext()));
//            blackList.setAdapter(adapter);
//            builder.setCancelable(true);
//            builder.show();
//        });

//        Button adminListButton = (Button) view.findViewById(R.id.group_settings_administrators);
//        adminListButton.setOnClickListener(view12 -> {
//            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
//            builder.setTitle(R.string.administrators);
//            view12 = getLayoutInflater().inflate(R.layout.alert_dialog_custom_administrators, null);
//            builder.setView(view12);
//            builder.setPositiveButton(R.string.button_add, (dialogInterface, i) -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentGroupAddAdministrators));
//            RecyclerView adminsList = (RecyclerView) view12.findViewById(R.id.alert_dialog_custom_administrators_rv);
//            AdministratorsAdapter adapter = new AdministratorsAdapter(listAlertDialogAdministrators, group.id, group.creatorID, dialogProgress);
//            adminsList.setLayoutManager(new LinearLayoutManager(getContext()));
//            adminsList.setAdapter(adapter);
//            builder.setCancelable(true);
//            builder.show();
//        });

        Button leaveGroup = (Button) view.findViewById(R.id.group_settings_leave_group);
        leaveGroup.setOnClickListener(view13 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom));
            builder.setMessage(R.string.are_you_sure).setPositiveButton(R.string.yes, (dialogInterface, i) -> {

            }).setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.cancel());
            builder.create().show();
        });

        GroupSettingsAdapter adapter = new GroupSettingsAdapter(list, group.id, group.creatorID, dialogProgress);
        contactsList.setAdapter(adapter);

        ItemClickSupport.addTo(contactsList).setOnItemClickListener((recyclerView, position, v) -> {
            final int uid = adapter.list.get(position).uid;
            final Bundle bundle = new Bundle();
            bundle.putInt(CHAT_ID_KEY, uid);
            Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentFriendProfile, bundle);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());

        list.clear();
        ArrayList<RPC.UserObject> users = GroupsManager.getInstance().getGroupUsers(chatID);
        for (RPC.UserObject user : users) {
            list.add(new UserItem(user.id, Utils.formatUserName(user), user.photoURL));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Utils.netQueue.postRunnable(() -> {
                    ApplicationLoader.applicationHandler.post(dialogProgress::show);

                    final RPC.PM_group_setPhoto setProfilePhotoRequest = new RPC.PM_group_setPhoto(chatID);

                    final long requestID = NetworkManager.getInstance().sendRequest(setProfilePhotoRequest, (response, error) -> {
                        if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                            ApplicationLoader.applicationHandler.post(() -> {
                                if (dialogProgress != null && dialogProgress.isShowing())
                                    dialogProgress.cancel();

                                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom))
                                        .setMessage(R.string.photo_upload_failed)
                                        .setCancelable(true);
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            });
                            return;
                        }

                        if (response instanceof RPC.PM_boolTrue) {
                            FileManager.getInstance().startUploading(result.getUri().getPath(), true, Config.maxAvatarSize, 85, new FileManager.IUploadingFile() {
                                @Override
                                public void onFinish() {
                                    Log.e(Config.TAG, "Group photoURL successfully uploaded");
                                    ApplicationLoader.applicationHandler.postDelayed(() -> {
                                        if (dialogProgress != null && dialogProgress.isShowing())
                                            dialogProgress.dismiss();
                                        if (!group.photoURL.url.isEmpty()) {
                                            group = GroupsManager.getInstance().getGroup(group.id);
                                            Utils.loadPhoto(group.photoURL.url, photoView);
                                        }
                                    }, 500);
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
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("AAA", error.getMessage());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
