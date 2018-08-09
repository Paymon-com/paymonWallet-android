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
import ru.paymon.android.models.CreateGroupItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class DialogFragmentCreateGroup extends DialogFragment {
    private DialogProgress dialogProgress;
    private EditText title;
    private Button buttonAgree;
    private Button buttonCancel;
    private LinkedList<CreateGroupItem> createGroupList = new LinkedList<>();


    public static synchronized DialogFragmentCreateGroup newInstance() {
        return new DialogFragmentCreateGroup();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("create_group_list")) {
                createGroupList = (LinkedList<CreateGroupItem>) bundle.getSerializable("create_group_list");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_create_group, container);

        title = (EditText) view.findViewById(R.id.dialog_fragment_with_edit_edit_text);
        buttonCancel = (Button) view.findViewById(R.id.dialog_fragment_with_edit_cancel);
        buttonAgree = (Button) view.findViewById(R.id.dialog_fragment_with_edit_ok);

        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(true);

        buttonAgree.setOnClickListener(buttonAgreeClickListener);

        return view;
    }

    private View.OnClickListener buttonAgreeClickListener = (view) -> {
//        if (!Utils.nameCorrect(title.getText().toString())) {
//            Toast.makeText(ApplicationLoader.applicationContext, "Название хуйня", Toast.LENGTH_LONG).show();//TODO:string
//            return;
//        }

        Utils.netQueue.postRunnable(() -> {
            ApplicationLoader.applicationHandler.post(dialogProgress::show);

            RPC.PM_createGroup createGroupRequest = new RPC.PM_createGroup();
            createGroupRequest.title = title.getText().toString().trim();
            for (CreateGroupItem createGroupItem : createGroupList) {
                if (createGroupItem.checked) {
                    createGroupRequest.userIDs.add(createGroupItem.uid);
                }
            }

            //TODO:проверить что происходит на серваке при разных пришедших результатах
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

//                    if (User.currentUser == null) return;

                    //region MSG_ITEM //TODO:на сервак перенос
//                    RPC.PM_messageItem msg = new RPC.PM_messageItem();
//                    msg.id = MessagesManager.generateMessageID();
//                    msg.flags = MESSAGE_FLAG_FROM_ID;
//                    msg.date = (int) (System.currentTimeMillis() / 1000);
//                    msg.from_id = User.currentUser.id;
//                    RPC.Peer peer;
//                    peer = new RPC.PM_peerGroup();
//                    peer.group_id = ((RPC.Group) response).id;
//                    msg.to_id = peer;
//                    msg.unread = true;
//                    msg.itemType = FileManager.FileType.ACTION;
//
//                    String text = String.format("%s %s \"%s\"", Utils.formatUserName(User.currentUser), getActivity().getString(R.string.sys_group_msg), group.title);
//
//                    msg.text = text;
//
//
//                    NetworkManager.getInstance().sendRequest(msg, (response2, error2) -> {//TODO:перенос в UI поток
//                        ApplicationLoader.applicationHandler.post(() -> {
//                            if (response2 == null || error2 != null) return;
//
//                            RPC.PM_updateMessageID update = (RPC.PM_updateMessageID) response2;
//
//                            msg.id = update.newID;
//                            MessagesManager.getInstance().putMessage(msg);
                    NotificationManager.getInstance().postNotificationName(NotificationManager.NotificationEvent.dialogsNeedReload);
                    getDialog().dismiss();
                    Bundle bundle = new Bundle();
                    bundle.putInt("chat_id", group.id);
                    bundle.putParcelableArrayList("users", group.users);
                    FragmentChat fragmentChat = new FragmentChat();
                    fragmentChat.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    fragmentTransaction.replace(R.id.container, fragmentChat);
                    fragmentTransaction.commit();
//                        });
//                    });
                    //endregion
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
