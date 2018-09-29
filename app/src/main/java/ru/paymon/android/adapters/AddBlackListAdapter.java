package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.List;

import ru.paymon.android.R;
import ru.paymon.android.models.UserItem;
import ru.paymon.android.utils.Utils;

public class AddBlackListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public List<UserItem> list;
    public AddBlackListAdapter(List<UserItem> list){
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_create_group_item, parent, false);
        return new AddBlackListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        UserItem userItem = list.get(position);
        AddBlackListViewHolder addBlackListViewHolder = (AddBlackListViewHolder) holder;

        addBlackListViewHolder.name.setText(userItem.name);

        if (!userItem.photo.url.isEmpty())
            Utils.loadPhoto(userItem.photo.url, addBlackListViewHolder.photo);

        addBlackListViewHolder.checkBox.setChecked(userItem.checked);

        View.OnClickListener clickListener = (view) -> userItem.checked = addBlackListViewHolder.checkBox.isChecked();

        addBlackListViewHolder.name.setOnClickListener(clickListener);
        addBlackListViewHolder.photo.setOnClickListener(clickListener);
        addBlackListViewHolder.checkBox.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class AddBlackListViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private CircularImageView photo;
        private CheckBox checkBox;

        public AddBlackListViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.create_user_name);
            photo = (CircularImageView) itemView.findViewById(R.id.create_user_photo);
            checkBox = (CheckBox) itemView.findViewById(R.id.cell_create_group_checkbox);
        }
    }
}
