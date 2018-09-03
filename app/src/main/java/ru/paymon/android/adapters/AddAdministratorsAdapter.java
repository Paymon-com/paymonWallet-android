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
import ru.paymon.android.models.AddAdministratorsItem;

public class AddAdministratorsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public List<AddAdministratorsItem> list;

    public AddAdministratorsAdapter(List<AddAdministratorsItem> list){
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.create_group_item, parent, false);
        return new AddAdministratorsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AddAdministratorsItem addAdministratorsItem = list.get(position);
        AddAdministratorsViewHolder addAdministratorsViewHolder = (AddAdministratorsViewHolder) holder;

        addAdministratorsViewHolder.name.setText(addAdministratorsItem.name);
//        addAdministratorsViewHolder.photo.setPhoto(addAdministratorsItem.photo);
        addAdministratorsViewHolder.checkBox.setChecked(addAdministratorsItem.checked);

        View.OnClickListener clickListener = (view) -> addAdministratorsItem.checked = addAdministratorsViewHolder.checkBox.isChecked();

        addAdministratorsViewHolder.name.setOnClickListener(clickListener);
//        addAdministratorsViewHolder.photo.setOnClickListener(clickListener);
        addAdministratorsViewHolder.checkBox.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private class AddAdministratorsViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private CircularImageView photo;
        private CheckBox checkBox;

        public AddAdministratorsViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.create_user_name);
            photo = (CircularImageView) itemView.findViewById(R.id.create_user_photo);
            checkBox = (CheckBox) itemView.findViewById(R.id.cell_create_group_checkbox);
        }
    }
}
