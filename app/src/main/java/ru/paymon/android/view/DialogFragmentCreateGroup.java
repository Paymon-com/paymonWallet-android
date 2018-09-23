package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class DialogFragmentCreateGroup extends DialogFragment {
    private DialogProgress dialogProgress;
    private EditText title;
    private LinkedList<UserItem> createGroupList = new LinkedList<>();


    public static synchronized DialogFragmentCreateGroup newInstance() {
        return new DialogFragmentCreateGroup();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("create_group_list")) {
                createGroupList = (LinkedList<UserItem>) bundle.getSerializable("create_group_list");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_create_group, container);

        title = (EditText) view.findViewById(R.id.dialog_fragment_with_edit_edit_text);
        Button buttonCancel = (Button) view.findViewById(R.id.dialog_fragment_with_edit_cancel);
        Button buttonAgree = (Button) view.findViewById(R.id.dialog_fragment_with_edit_ok);

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
                        Toast toast = Toast.makeText(getContext(),
                                "Ошибка", Toast.LENGTH_SHORT);//TODO string
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
                    bundle.putInt("chat_id", group.id);
                    bundle.putParcelableArrayList("users", group.users);
                    FragmentChat fragmentChat = FragmentChat.newInstance();
                    fragmentChat.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    fragmentTransaction.replace(R.id.container, fragmentChat);
                    fragmentTransaction.commit();
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
