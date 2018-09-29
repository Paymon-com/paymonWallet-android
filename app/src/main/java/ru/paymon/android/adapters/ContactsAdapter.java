package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public LinkedList<RPC.UserObject> contactsItems = new LinkedList<>();

    public ContactsAdapter() {
        for (int i = 0; i < UsersManager.getInstance().userContacts.size(); i++) {
            RPC.UserObject user = UsersManager.getInstance().userContacts.get(UsersManager.getInstance().userContacts.keyAt(i));
            contactsItems.add(user);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_contacts_item, parent, false);
        return new ContactsItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RPC.UserObject user = contactsItems.get(position);
        ContactsItemViewHolder contactsItemViewHolder = (ContactsItemViewHolder) holder;

        if (!user.photoURL.url.isEmpty())
            Utils.loadPhoto(user.photoURL.url, contactsItemViewHolder.photo);

        contactsItemViewHolder.name.setText(Utils.formatUserName(user));
        contactsItemViewHolder.login.setText(String.format("@%s", user.login));
    }

    @Override
    public int getItemCount() {
        return contactsItems.size();
    }

    private class ContactsItemViewHolder extends RecyclerView.ViewHolder {
        public CircularImageView photo;
        public TextView name;
        public TextView login;

        public ContactsItemViewHolder(View itemView) {
            super(itemView);
            photo = (CircularImageView) itemView.findViewById(R.id.contacts_photo);
            name = (TextView) itemView.findViewById(R.id.contacts_name);
//            login = (TextView) itemView.findViewById(R.id.contacts_login);
        }
    }

}
