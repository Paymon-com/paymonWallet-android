package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.R;

public class ContactsInviteUnregisteredAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ContactsInviteUnregisteredAdapter(){

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_contacts_invite_unregistered, parent, false);
        RecyclerView.ViewHolder vh = new ContactsInviteUnegisteredViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private class ContactsInviteUnegisteredViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView phoneNumber;
        private CheckBox checkBox;

        private ContactsInviteUnegisteredViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name_contact_invite_unregistered);
            phoneNumber = (TextView) itemView.findViewById(R.id.phone_contact_invite_unregistered);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_contact_invite_unregistered);
        }
    }
}
