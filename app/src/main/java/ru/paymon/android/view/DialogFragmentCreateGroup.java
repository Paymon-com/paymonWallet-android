package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.components.DialogProgress;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.AbsFragmentChat.CHAT_GROUP_USERS;
import static ru.paymon.android.view.AbsFragmentChat.CHAT_ID_KEY;

public class DialogFragmentCreateGroup extends DialogFragment {
    private DialogProgress dialogProgress;
    private EditText title;
    private ArrayList<UserItem> createGroupList = new ArrayList<>();


    public static synchronized DialogFragmentCreateGroup newInstance() {
        return new DialogFragmentCreateGroup();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(CHAT_GROUP_USERS)) {
                createGroupList = (ArrayList<UserItem>) bundle.getSerializable(CHAT_GROUP_USERS);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_create_group, container);

        title = (EditText) view.findViewById(R.id.dialog_fragment_with_edit_edit_text);
        ConstraintLayout buttonCancel = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_with_edit_cancel);
        ConstraintLayout buttonAgree = (ConstraintLayout) view.findViewById(R.id.dialog_fragment_with_edit_ok);

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);

        buttonAgree.setOnClickListener(buttonAgreeClickListener);
        buttonCancel.setOnClickListener(view1 -> getDialog().dismiss());

        return view;
    }

    private View.OnClickListener buttonAgreeClickListener = (view) -> {
        Utils.netQueue.postRunnable(() -> {
            ApplicationLoader.applicationHandler.post(dialogProgress::show);

            RPC.PM_createGroup createGroupRequest = new RPC.PM_createGroup();
            createGroupRequest.title = title.getText().toString().trim();
            for (UserItem createGroupItem : createGroupList) {
                if (createGroupItem.checked) {
                    createGroupRequest.userIDs.add(createGroupItem.uid);
                }
            }

            final long requestID = NetworkManager.getInstance().sendRequest(createGroupRequest, (response, error) -> {
                if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.cancel();
                        Toast toast = Toast.makeText(getContext(), R.string.other_error, Toast.LENGTH_SHORT);
                        toast.show();
                    });
                    return;
                }

                if (response instanceof RPC.Group) {
                    RPC.Group group = (RPC.Group) response;
                    GroupsManager.getInstance().putGroup(group);
                    NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.dialogsNeedReload);
                    getDialog().dismiss();
                    Bundle bundle = new Bundle();
                    bundle.putInt(CHAT_ID_KEY, group.id);
                    ApplicationLoader.applicationHandler.post(()-> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentGroupChat, bundle));
                }

                ApplicationLoader.applicationHandler.post(() -> {
                    if (dialogProgress != null && dialogProgress.isShowing())
                        dialogProgress.dismiss();
                });
            });
            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
        });
    };
}
