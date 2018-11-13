package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.paymon.android.R;
import ru.paymon.android.components.CircularImageView;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.utils.Utils;

public class CreateGroupAdapter extends RecyclerView.Adapter<CreateGroupAdapter.ViewHolder> {
    public ArrayList<UserItem> list;
    public Map<Integer, UserItem> checkedMap;

    public CreateGroupAdapter(ArrayList<UserItem> list) {
        this.list = list;
        checkedMap = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_create_group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserItem userItem = list.get(position);

        if (userItem != null)
            holder.bind(userItem);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private CircularImageView photo;
        private CheckBox checkBox;

        ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.create_user_name);
            photo = (CircularImageView) itemView.findViewById(R.id.create_user_photo);
            checkBox = (CheckBox) itemView.findViewById(R.id.cell_create_group_checkbox);
        }

        public void bind(UserItem userItem) {
            if (userItem.isHidden) {
                itemView.setVisibility(View.GONE);
                final ViewGroup.LayoutParams params = itemView.getLayoutParams();
                params.height = 0;
                itemView.setLayoutParams(params);
                return;
            } else {
                itemView.setVisibility(View.VISIBLE);
                final ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                itemView.setLayoutParams(params);
            }

            name.setText(userItem.name);
            checkBox.setChecked(checkedMap.containsKey(userItem.uid));

            if (!userItem.photo.url.isEmpty())
                Utils.loadPhoto(userItem.photo.url, photo);

            View.OnClickListener clickListener = (view) -> {
                checkBox.setChecked(!checkBox.isChecked());
                userItem.checked = checkBox.isChecked();
                if (checkBox.isChecked())
                    checkedMap.put(userItem.uid, userItem);
                else
                    checkedMap.remove(userItem.uid);
            };

            checkBox.setOnCheckedChangeListener((v, isChecked) -> {
                userItem.checked = checkBox.isChecked();
                if (checkBox.isChecked())
                    checkedMap.put(userItem.uid, userItem);
                else
                    checkedMap.remove(userItem.uid);
            });
            photo.setOnClickListener(clickListener);
            name.setOnClickListener(clickListener);
        }
    }
}
