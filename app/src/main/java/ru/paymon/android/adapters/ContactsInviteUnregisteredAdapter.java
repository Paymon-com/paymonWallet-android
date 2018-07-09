package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.models.Contact;

public class ContactsInviteUnregisteredAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<Contact> unregisteredContacts;

    public ContactsInviteUnregisteredAdapter(ArrayList<Contact> unregisteredContacts){
        this.unregisteredContacts = unregisteredContacts;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_contacts_invite_unregistered, parent, false);
        RecyclerView.ViewHolder vh = new UnregisteredContactsViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Contact contact = unregisteredContacts.get(position);
        UnregisteredContactsViewHolder unregisteredContactsViewHolder = (UnregisteredContactsViewHolder) holder;
        unregisteredContactsViewHolder.name.setText(contact.name);
        unregisteredContactsViewHolder.phoneNumber.setText(contact.phone);
    }

    @Override
    public int getItemCount() {
        return unregisteredContacts.size();
    }

    private class UnregisteredContactsViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView phoneNumber;
        private CheckBox checkBox;

        private UnregisteredContactsViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name_contact_invite_unregistered);
            phoneNumber = (TextView) itemView.findViewById(R.id.phone_contact_invite_unregistered);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_contact_invite_unregistered);
        }
    }
}
