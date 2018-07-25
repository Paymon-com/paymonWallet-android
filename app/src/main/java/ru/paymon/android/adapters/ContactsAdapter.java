package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.UsersManager;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.models.ContactsLineItem;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LinkedList<RPC.UserObject> commonItems = new LinkedList<>();;

    enum ViewTypes {
        USER,
        LINE
    }

    public ContactsAdapter(LinkedList<RPC.UserObject> contactsItems, LinkedList<RPC.UserObject> contactsGlobalItems) {
        commonItems.addAll(contactsItems);
        commonItems.add(new ContactsLineItem());
        commonItems.addAll(contactsGlobalItems);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;
        ViewTypes viewTypes = ViewTypes.values()[viewType];
        switch (viewTypes) {
            case USER:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_contacts_item, parent, false);
                vh = new ContactsItemViewHolder(view);
                break;
            case LINE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_line_item, parent, false);
                vh = new LineItemViewHolder(view);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ContactsItemViewHolder) {
            RPC.UserObject user =  commonItems.get(position);
            ContactsAdapter.ContactsItemViewHolder contactsItemViewHolder = (ContactsAdapter.ContactsItemViewHolder) holder;
            contactsItemViewHolder.photo.setPhoto(new RPC.PM_photo(user.id, user.photoID));
            contactsItemViewHolder.name.setText(Utils.formatUserName(user));
        }
    }

    public int getItemViewType(int position) {
        RPC.UserObject user = commonItems.get(position);
        int viewType = ViewTypes.USER.ordinal();
        if(user instanceof ContactsLineItem)
            viewType = ViewTypes.LINE.ordinal();

        return viewType;
    }

    @Override
    public int getItemCount() {
        return commonItems.size();
    }

    private class ContactsItemViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView photo;
        public TextView name;

        public ContactsItemViewHolder(View itemView) {
            super(itemView);
            photo = (CircleImageView) itemView.findViewById(R.id.contacts_photo);
            name = (TextView) itemView.findViewById(R.id.contacts_name);
        }
    }

    private class LineItemViewHolder extends RecyclerView.ViewHolder {
        public View line;

        public LineItemViewHolder(View itemView) {
            super(itemView);
            line = (View) itemView.findViewById(R.id.divider);
        }
    }

    public RPC.UserObject getItem(int position){
        return commonItems.get(position);
    }
}
