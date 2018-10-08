package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

//import ru.paymon.android.UsersManager;

public class ContactsGlobalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<RPC.UserObject> contactsGlobalItems = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_contacts_item, parent, false);
        RecyclerView.ViewHolder vh = new ContactsGlobalItemViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RPC.UserObject userGlobal = contactsGlobalItems.get(position);
        ContactsGlobalItemViewHolder contactsGlobalItemViewHolder = (ContactsGlobalItemViewHolder) holder;

        if (!userGlobal.photoURL.url.isEmpty())
            Utils.loadPhoto(userGlobal.photoURL.url, contactsGlobalItemViewHolder.photo);

        contactsGlobalItemViewHolder.name.setText(Utils.formatUserName(userGlobal));
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
