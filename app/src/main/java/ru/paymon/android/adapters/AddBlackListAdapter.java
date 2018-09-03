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
import ru.paymon.android.models.AddBlackListItem;
import ru.paymon.android.models.AlertDialogCustomBlackListItem;

public class AddBlackListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public List<AddBlackListItem> list;

    public AddBlackListAdapter(List<AddBlackListItem> list){
        this.list = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.create_group_item, parent, false);
        return new AddBlackListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AddBlackListItem addBlackListItem = list.get(position);
        AddBlackListViewHolder addBlackListViewHolder = (AddBlackListViewHolder) holder;

        addBlackListViewHolder.name.setText(addBlackListItem.name);
//        addBlackListViewHolder.photo.setPhoto(addBlackListItem.photo);
        addBlackListViewHolder.checkBox.setChecked(addBlackListItem.checked);

        View.OnClickListener clickListener = (view) -> addBlackListItem.checked = addBlackListViewHolder.checkBox.isChecked();

        addBlackListViewHolder.name.setOnClickListener(clickListener);
//        addBlackListViewHolder.photo.setOnClickListener(clickListener);
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
