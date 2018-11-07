package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.components.CircularImageView;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.components.DialogProgress;


public class GroupSettingsAdapter extends RecyclerView.Adapter<GroupSettingsAdapter.GroupsSettingsViewHolder> {
    public LinkedList<UserItem> list;
    private boolean isCreator;
    private int chatID;
    private RPC.Group group;
    private DialogProgress dialogProgress;

    public GroupSettingsAdapter(LinkedList<UserItem> list, int chatID, int creatorID, DialogProgress dialogProgress) {
        this.list = list;
        this.chatID = chatID;
        this.dialogProgress = dialogProgress;
        isCreator = creatorID == User.currentUser.id;
    }

    @NonNull
    @Override
    public GroupsSettingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_group_settings, parent, false);
        return new GroupsSettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupsSettingsViewHolder holder, int position) {
        UserItem userItem = list.get(position);

        if (userItem == null) return;

        holder.bind(userItem);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class GroupsSettingsViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private CircularImageView photo;
        private ImageView removeButton;

        private GroupsSettingsViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.cell_group_participant_name);
            photo = (CircularImageView) itemView.findViewById(R.id.cell_group_participant_photo);
            removeButton = (ImageView) itemView.findViewById(R.id.cell_group_participant_remove);

            removeButton.setImageResource(R.drawable.ic_close);

            if (!isCreator)
                removeButton.setVisibility(View.GONE);
        }

        public void bind(UserItem userItem) {
            name.setText(userItem.name);

            if (!userItem.photo.url.isEmpty())
                Utils.loadPhoto(userItem.photo.url, photo);

            if (userItem.uid == User.currentUser.id)
                removeButton.setVisibility(View.GONE);

            removeButton.setOnClickListener((view) ->
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                builder.setTitle(R.string.deletion_confirmation)
                        .setMessage(R.string.delete_participants)
                        .setCancelable(true).setPositiveButton(R.string.yes, (dialogInterface, i) -> deleteParticipant(userItem)).setNegativeButton(R.string.no, (dialogInterface, i) -> {
                });

                AlertDialog alert = builder.create();
                alert.show();
            });
        }
    }

    private void deleteParticipant(UserItem createGroupItem) {
        Utils.netQueue.postRunnable(() -> {
            ApplicationLoader.applicationHandler.post(dialogProgress::show);

            RPC.PM_group_removeParticipant removeParticipant = new RPC.PM_group_removeParticipant();
            removeParticipant.id = chatID;
            removeParticipant.userID = createGroupItem.uid;
            group = GroupsManager.getInstance().getGroup(chatID);

            long requestID = NetworkManager.getInstance().sendRequest(removeParticipant, (response, error) -> {
                if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.cancel();
                        Toast.makeText(ApplicationLoader.applicationContext, R.string.import_export_keys_dialog_failure_title, Toast.LENGTH_LONG).show();
                    });
                }

                if (response instanceof RPC.PM_boolTrue) {
                    group.users.remove((Integer)removeParticipant.userID);
                    GroupsManager.getInstance().putGroup(group);

                    list.clear();
                    for (Integer uid : group.users) {
                        final RPC.UserObject user = UsersManager.getInstance().getUser(uid);
                        list.add(new UserItem(user.id, Utils.formatUserName(user), user.photoURL));
                    }
                }
                ApplicationLoader.applicationHandler.post(() -> {
                    if (dialogProgress != null && dialogProgress.isShowing())
                        dialogProgress.cancel();
                    notifyDataSetChanged();
                });
            });
            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
        });
    }
}
