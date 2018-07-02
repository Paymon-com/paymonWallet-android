package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.paymon.android.R;

public class ContactsInviteRegisteredAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public ContactsInviteRegisteredAdapter() {

    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_contacts_invite_registered, parent, false);
        RecyclerView.ViewHolder vh = new ContactsInviteRegisteredViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private class ContactsInviteRegisteredViewHolder extends RecyclerView.ViewHolder {
        private TextView userName;
        private TextView phoneNumber;
        private CheckBox checkBox;
        private ImageView imageView;

        private ContactsInviteRegisteredViewHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.name_contact_invite_registered);
            phoneNumber = (TextView) itemView.findViewById(R.id.phone_contact_invite_registered);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_contact_invite_registered);
            imageView = (ImageView) itemView.findViewById(R.id.image_contact_invite_registered);
        }
    }


}
