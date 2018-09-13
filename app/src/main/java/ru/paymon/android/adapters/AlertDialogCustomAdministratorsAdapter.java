package ru.paymon.android.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.models.AlertDialogCustomAdministratorsItem;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.DialogProgress;
import ru.paymon.android.view.FragmentFriendProfile;

import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;


public class AlertDialogCustomAdministratorsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private LinkedList<AlertDialogCustomAdministratorsItem> list;
    private boolean isCreator;
    private int chatID;
    private RPC.Group group;
    private DialogProgress dialogProgress;

    public AlertDialogCustomAdministratorsAdapter(LinkedList<AlertDialogCustomAdministratorsItem> list, int chatID, int creatorID, DialogProgress dialogProgress){
        this.list = list;
        this.chatID = chatID;
        this.dialogProgress = dialogProgress;
        isCreator = creatorID == User.currentUser.id;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_group_settings, parent, false);
        AlertDialogAdministratorsViewHolder holder = new AlertDialogAdministratorsViewHolder(view);
        context = holder.itemView.getContext();
        return new AlertDialogAdministratorsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AlertDialogCustomAdministratorsItem alertDialogCustomAdministratorsItem = list.get(position);
        AlertDialogAdministratorsViewHolder alertDialogAdministratorsViewHolder = (AlertDialogAdministratorsViewHolder) holder;

        alertDialogAdministratorsViewHolder.name.setText(alertDialogCustomAdministratorsItem.name);
//        alertDialogAdministratorsViewHolder.photoURL.setPhoto(alertDialogCustomAdministratorsItem.photoURL);

        if (alertDialogCustomAdministratorsItem.uid == User.currentUser.id)
            alertDialogAdministratorsViewHolder.removeButton.setVisibility(View.GONE);

        alertDialogAdministratorsViewHolder.removeButton.setOnClickListener(view -> {
            //TODO:удаление
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class AlertDialogAdministratorsViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private CircularImageView photo;
        private ImageView removeButton;

        private AlertDialogAdministratorsViewHolder(View itemView) {
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

                final FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
                Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentFriendProfile, null);
            };
            itemView.setOnClickListener(listener);
            name.setOnClickListener(listener);
//            photoURL.setOnClickListener(listener);
        }
    }
}
