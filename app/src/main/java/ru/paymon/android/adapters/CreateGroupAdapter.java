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
import ru.paymon.android.models.CreateGroupItem;

public class CreateGroupAdapter extends RecyclerView.Adapter<CreateGroupAdapter.ViewHolder> {
    public List<CreateGroupItem> list;

    public CreateGroupAdapter(List<CreateGroupItem> list){this.list = list;}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.create_group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CreateGroupItem createGroup = list.get(position);
        holder.name.setText(createGroup.name);
//        holder.photo.setPhoto(createGroup.photo);
        holder.checkBox.setChecked(createGroup.checked);

        View.OnClickListener clickListener = (view) -> createGroup.checked = holder.checkBox.isChecked();

        holder.checkBox.setOnClickListener(clickListener);
//        holder.photo.setOnClickListener(clickListener);
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
