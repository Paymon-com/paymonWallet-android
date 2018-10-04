package ru.paymon.android.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.DialogProgress;
import ru.paymon.android.view.FragmentFriendProfile;

import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;


public class GroupSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private LinkedList<UserItem> list;
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_group_settings, parent, false);
        GroupsSettingsViewHolder holder = new GroupsSettingsViewHolder(view);
        context = holder.itemView.getContext();
        return new GroupsSettingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserItem userItem = list.get(position);
        GroupsSettingsViewHolder groupsSettingsViewHolder = (GroupsSettingsViewHolder) holder;

        groupsSettingsViewHolder.name.setText(userItem.name);

        if (!userItem.photo.url.isEmpty())
            Utils.loadPhoto(userItem.photo.url, groupsSettingsViewHolder.photo);

        if (userItem.uid == User.currentUser.id)
            groupsSettingsViewHolder.removeButton.setVisibility(View.GONE);

        groupsSettingsViewHolder.removeButton.setOnClickListener((view) ->
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Подтверждение удаления")
                    .setMessage("Вы хотите удалить участника?")
                    .setCancelable(true).setPositiveButton("Да", (dialogInterface, i) -> deleteParticipant(userItem)).setNegativeButton("Нет", (dialogInterface, i) -> {
            });

            AlertDialog alert = builder.create();
            alert.show();
        });
    }

    private void deleteParticipant(UserItem createGroupItem) {
        Utils.netQueue.postRunnable(() -> {
            ApplicationLoader.applicationHandler.post(dialogProgress::show);

            RPC.PM_group_removeParticipant removeParticipant = new RPC.PM_group_removeParticipant();
            removeParticipant.id = chatID;
            removeParticipant.userID = createGroupItem.uid;
            group = ApplicationLoader.db.groupDao().getById(chatID);

            long requestID = NetworkManager.getInstance().sendRequest(removeParticipant, (response, error) -> {
                if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.cancel();
                        Toast.makeText(ApplicationLoader.applicationContext, "oshibka", Toast.LENGTH_LONG).show();
                    });
                }

                if (response instanceof RPC.PM_boolTrue) {
                    RPC.UserObject userToRemove = ApplicationLoader.db.userDao().getById(removeParticipant.userID);
                    group.users.remove(userToRemove);

                    for (UserItem item : list) {
                        if (item.uid == userToRemove.id)
                            list.remove(item);
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

    @Override
    public int getItemCount() {
        return list.size();
    }


    private class GroupsSettingsViewHolder extends RecyclerView.ViewHolder {
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

            View.OnClickListener listener = v -> {
                int position = getLayoutPosition();
                final int uid = list.get(position).uid;
                final Bundle bundle = new Bundle();
                bundle.putInt(CHAT_ID_KEY, uid);
                final FragmentFriendProfile fragmentFriendProfile = new FragmentFriendProfile();
                fragmentFriendProfile.setArguments(bundle);

                final FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
                Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentFriendProfile, null);//TODO:NAV FIX
            };
            itemView.setOnClickListener(listener);
            name.setOnClickListener(listener);
            photo.setOnClickListener(listener);
        }
    }

}
