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

public class CreateGroupAdapter extends RecyclerView.Adapter<CreateGroupAdapter.ViewHolder> {
    public List<UserItem> list;

    public CreateGroupAdapter(List<UserItem> list){this.list = list;}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_create_group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserItem userItem = list.get(position);
        holder.name.setText(userItem.name);
        holder.checkBox.setChecked(userItem.checked);

        if (!userItem.photo.url.isEmpty())
            Utils.loadPhoto(userItem.photo.url, holder.photo);

        View.OnClickListener clickListener = (view) -> userItem.checked = holder.checkBox.isChecked();

        holder.checkBox.setOnClickListener(clickListener);
        holder.photo.setOnClickListener(clickListener);
        holder.name.setOnClickListener(clickListener);
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
    }
}
