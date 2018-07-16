package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.data.CreateGroupItem;

public class GroupSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener{
    LinkedList<CreateGroupItem> list;

    public GroupSettingsAdapter(LinkedList<CreateGroupItem> list) {
        this.list = list;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_group_settings, parent, false);
        RecyclerView.ViewHolder vh = new GroupsSettingsViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CreateGroupItem createGroup = list.get(position);
        GroupsSettingsViewHolder groupsSettingsViewHolder = (GroupsSettingsViewHolder) holder;

        groupsSettingsViewHolder.name.setText(createGroup.name);
        groupsSettingsViewHolder.photo.setPhoto(createGroup.photo);
        groupsSettingsViewHolder.removeButton.setImageResource(R.drawable.ic_close);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onClick(View view) {

    }

    private class GroupsSettingsViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private CircleImageView photo;
        private ImageView removeButton;

        private GroupsSettingsViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.cell_group_participant_name);
            photo = (CircleImageView) itemView.findViewById(R.id.cell_group_participant_photo);
            removeButton = (ImageView) itemView.findViewById(R.id.cell_group_participant_remove);

            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getLayoutPosition();
                    final int uid = list.get(position).uid;
                    if (uid == User.currentUser.id) return;
                }
            });
        }
    }

}
