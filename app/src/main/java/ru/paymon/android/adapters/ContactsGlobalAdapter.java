package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.UsersManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class ContactsGlobalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public LinkedList<RPC.UserObject> contactsGlobalItems = new LinkedList<>();

    public ContactsGlobalAdapter() {
        for (int i = 0; i < UsersManager.getInstance().searchUsers.size(); i++){
            RPC.UserObject userGlobal = UsersManager.getInstance().searchUsers.get(UsersManager.getInstance().searchUsers.keyAt(i));
            contactsGlobalItems.add(userGlobal);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_contacts_item, parent, false);
        RecyclerView.ViewHolder vh = new ContactsGlobalItemViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RPC.UserObject addFriend = contactsGlobalItems.get(position);
        ContactsGlobalItemViewHolder contactsGlobalItemViewHolder = (ContactsGlobalItemViewHolder) holder;
//        contactsGlobalItemViewHolder.photo.setPhoto(new RPC.PM_photo(addFriend.id, addFriend.photoID));
        contactsGlobalItemViewHolder.name.setText(Utils.formatUserName(addFriend));

    }

    @Override
    public int getItemCount() {
        return contactsGlobalItems.size();
    }

    private class ContactsGlobalItemViewHolder extends RecyclerView.ViewHolder {
        public CircularImageView photo;
        public TextView name;

        public ContactsGlobalItemViewHolder(View itemView) {
            super(itemView);
            photo = (CircularImageView) itemView.findViewById(R.id.contacts_photo);
            name = (TextView) itemView.findViewById(R.id.contacts_name);
        }
    }
}
